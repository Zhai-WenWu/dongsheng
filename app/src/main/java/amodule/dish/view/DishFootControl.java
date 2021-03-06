package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.answer.activity.AskEditActivity;
import amodule.quan.activity.FollowSubject;
import amodule.quan.activity.ShowSubject;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

import static amodule.dish.activity.DetailDishWeb.tongjiId;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * 菜谱详情页底部view的控制器
 * 菜谱底部原生所有内容：相关美食帖、广告、发帖入口、点赞点踩、提问入口
 * Created by XiangHa on 2017/7/13.
 */
public class DishFootControl implements View.OnClickListener{

    private Activity mAct;
    private LinearLayout mAdLayout,userDishLayout;
    private RelativeLayout mRecomentLayout;
    private TextView mRecommentNum,mRelevantTv;

    private DishGgDataViewNew dishAdDataView;

    private String code,mDishName,authorCode;

    private String dishLikeStatus = "3";//当前点击状态。3-无操作，1-反对，2-赞同


    //处理底部浮动view
    private LinearLayout goodLayoutParent,hoverLayout, questionBtnContainer;
    private ImageView mGoodImg,mNoLikeImg,hoverGoodImg;
    private TextView mHoverNum,mHoverTv;
    private String askStatus="";

    public DishFootControl(Activity act){
        mAct = act;
        this.code = code;
        init();
    }
    private void init(){
        mAct.findViewById(R.id.a_dish_detail_new_footer).setVisibility(View.GONE);
        mAdLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_tieshi_ad);
        mRecomentLayout = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_xiangguan);
        userDishLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll_linear);

        mRecommentNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_tv_num);
        mRelevantTv = (TextView) mAct.findViewById(R.id.a_dish_detail_new_relevantTv);

        dishAdDataView = new DishGgDataViewNew(mAct,R.layout.view_dish_tips_ad_layout_new);
        dishAdDataView.getRequest(mAct, mAdLayout);
        mRecomentLayout.setOnClickListener(this);
        mRelevantTv.setOnClickListener(this);
        mRecommentNum.setOnClickListener(this);

        initFudongView();
    }

    /**
     * 处理底部浮动的view
     */
    private void initFudongView(){
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.GONE);
        goodLayoutParent = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_layout);
        hoverLayout= (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_layout);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good).setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_trample).setOnClickListener(this);
        mHoverTv= (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_tv);
        questionBtnContainer= (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_question_container);
        questionBtnContainer.setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_linear).setOnClickListener(this);
        mHoverNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_number);
        mGoodImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_good_img);
        mNoLikeImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_img);
        hoverGoodImg= (ImageView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_show);
        hoverGoodImg.setOnClickListener(this);
    }

    public void initData(String code){
        this.code = code;
        setRequestAskButtonStatus();
    }
    /**
     * 设置菜谱名称
     * @param dishName
     */
    public void setDishInfo(String dishName) {
        mDishName = dishName;
    }

    /**
     * 处理用户相关推荐数据
     * @param dishJson
     */
    public void initUserDish(String dishJson){
        ArrayList<Map<String, String>> arrayList = getListMapByJson(dishJson);
        mRelevantTv.setVisibility(View.VISIBLE);
        if(arrayList.size() > 0) {
            Map<String, String> TieMap = arrayList.get(0);
            mRecommentNum.setText(TieMap.get("totalNum") + "个作品");
            LayoutInflater inflater = LayoutInflater.from(mAct);
            arrayList = getListMapByJson(TieMap.get("list"));
            if (arrayList.size() > 0) {
                mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.VISIBLE);
                mRecomentLayout.setVisibility(View.VISIBLE);
            } else {
                mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.GONE);
                return;
            }
            View view;
            int index = 0, size = arrayList.size() - 1;
            for (final Map<String, String> map : arrayList) {
                view = inflater.inflate(R.layout.a_dish_detail_new_footer_item, null);
                ImageView dishImg = (ImageView) view.findViewById(R.id.a_dish_detail_show_img);
                ImageView userImg = (ImageView) view.findViewById(R.id.a_dish_detail_user_icon);
                final ImageView zanImg = (ImageView) view.findViewById(R.id.a_dish_detail_zan);
                TextView userName = (TextView) view.findViewById(R.id.a_dish_detail_user_name);
                TextView dishTime = (TextView) view.findViewById(R.id.a_dish_detail_time);
                final TextView zanNumber = (TextView) view.findViewById(R.id.a_dish_detail_zan_numer);

                String customer = map.get("customer");
                ArrayList<Map<String, String>> customerArray = StringManager.getListMapByJson(customer);
                if(customerArray.size() > 0) {
                    Map<String, String> customerMap = customerArray.get(0);
                    userName.setText(customerMap.get("nickName"));
                    setViewImage(userImg, customerMap.get("img"), 500);
                    String userCode = customerMap.get("code");
                    setGotoFriendHome(userImg, userCode);
                    setGotoFriendHome(userName, userCode);
                }

                final String zanNumberStr = map.get("likeNum");
                final String subjectCode = map.get("code");
                setViewImage(dishImg, map.get("img"), 0);
                final boolean isLike = "2".equals(map.get("isLike"));
                zanImg.setImageResource(isLike ? R.drawable.z_menu_praiseselected : R.drawable.z_menu_praisenomal);
                dishTime.setText(map.get("timeShow"));
                zanNumber.setText(zanNumberStr);
                zanImg.setOnClickListener(new View.OnClickListener() {
                    boolean newIsLike = isLike;
                    @Override
                    public void onClick(View v) {
                        if(newIsLike){
                            Tools.showToast(mAct,"已点过赞");
                        }else {
                            XHClick.mapStat(mAct, tongjiId, "哈友相关作品", "点赞按钮点击量");
                            if(!LoginManager.isLogin()){//未登录，直接去登录
                                Intent intent = new Intent(mAct, LoginByAccout.class);
                                mAct.startActivity(intent);
                                return;
                            }
                            LinkedHashMap<String, String> map = new LinkedHashMap<>();
                            map.put("subjectCode", subjectCode);
                            map.put("type", "likeList");
                            ReqEncyptInternet.in().doEncypt(StringManager.api_quanSetSubject, map, new InternetCallback() {
                                @Override
                                public void loaded(int flag, String s, Object o) {
                                    if (flag >= ReqInternet.REQ_OK_STRING) {
                                        try {
                                            newIsLike = true;
                                            zanImg.setImageResource(R.drawable.i_good_activity);
                                            int zanNum = Integer.parseInt(zanNumberStr);
                                            zanNumber.setText(String.valueOf(++zanNum));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(mAct, tongjiId, "哈友相关作品", "相关作品帖子点击量");
                        Intent it = new Intent(mAct, ShowSubject.class);
                        it.putExtra("code", subjectCode);
                        mAct.startActivity(it);
                    }
                });
                View itemParent = view.findViewById(R.id.dish_footer_item);
                if (index == 0) {
                    int dp20 = Tools.getDimen(mAct, R.dimen.dp_20);
                    itemParent.setPadding(dp20, 0, 0, 0);
                } else if (index == size) {
                    int dp20 = Tools.getDimen(mAct, R.dimen.dp_20);
                    int dp8 = Tools.getDimen(mAct, R.dimen.dp_8);
                    itemParent.setPadding(dp8, 0, dp20, 0);
                }
                index++;
                userDishLayout.addView(view);
            }
        }else{
            userDishLayout.removeAllViews();

            mRecomentLayout.setVisibility(View.GONE);
            mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.GONE);
        }
    }
    public void initLikeState(String data){
        ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(data);
        if(arrayList.size() > 0){
            Map<String,String> map = arrayList.get(0);
            mHoverNum.setText(map.get("num"));
            //点赞/点踩的状态(1:点踩，2:点赞，3:无)
            dishLikeStatus=map.get("status");
        }else{
            mHoverNum.setText("0");
        }
        handlerDishLikeState(dishLikeStatus);
    }

     private void setGotoFriendHome(View view, final String userCode){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(mAct, tongjiId, "哈友相关作品", "相关作品帖子用户头像点击量");
                Intent intent = new Intent(mAct, FriendHome.class);
                intent.putExtra("code",userCode);
                mAct.startActivity(intent);
            }
        });
    }



    public static final int TAG_ID = R.string.tag;
    private int imgResource = R.drawable.i_nopic;
    public void setViewImage(final ImageView v, final String value, int roundImgPixels) {
        if (value == null) return;
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (value.length() < 10)
                return;
            v.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
                    .load(value)
                    .setImageRound(roundImgPixels)
                    .setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setSaveType(FileManager.save_cache)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                        ImageView img = null;
                        if (v.getTag(TAG_ID).equals(value))
                            img = v;
                        if (img != null && bitmap != null) {
                            // 图片圆角和宽高适应
                            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            UtilImage.setImgViewByWH(v, bitmap, 0, 0, false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.a_dish_detail_new_xiangguan: //点击相关作品
                Intent intent = new Intent(mAct, FollowSubject.class);
                intent.putExtra("dishCode",code);
                intent.putExtra("title",mDishName);
                mAct.startActivity(intent);
                break;
            case R.id.a_dish_detail_new_relevantTv: //晒我做的这道菜
                Intent showIntent = new Intent(mAct, UploadSubjectNew.class);
                showIntent.putExtra("dishCode",code);
                showIntent.putExtra("name",mDishName);
                showIntent.putExtra("skip", true);
                showIntent.putExtra("cid", "1");
                mAct.startActivity(showIntent);
                XHClick.mapStat(mAct, tongjiId, "晒我做的这道菜", "晒我做的这道菜点击量");
                break;
            case R.id.a_dish_detail_new_footer_hover_question_container: //提问作者
                if(!LoginManager.isLogin()){
                    mAct.startActivity(new Intent(mAct,LoginByAccout.class));
                    return;
                }
                if("2".equals(askStatus)){
                    Intent intentAsk= new Intent(mAct, AskEditActivity.class);
                    intentAsk.putExtra("code",code);
                    intentAsk.putExtra("authorCode",authorCode);
                    mAct.startActivity(intentAsk);
                }else if("3".equals(askStatus)){
                    XHClick.mapStat(mAct, tongjiId, "底部浮动", "向作者提问点击量");
                    Tools.showToast(mAct,"已提醒作者");
                }

                break;
             case R.id.a_dish_detail_new_footer_hover_good: //有用
                 if(!LoginManager.isLogin()){
                     mAct.startActivity(new Intent(mAct,LoginByAccout.class));
                     return;
                 }
                 XHClick.mapStat(mAct, tongjiId, "底部浮动", "点赞按钮点击量");
//                 if("2".equals(dishLikeStatus)) {//当前是点赞
//                     handlerDishLikeState("3");
//                 }else{
//                     handlerDishLikeState("2");
//                 }
                 onChangeLikeState(true,true);
                 hindGoodLayout();
                break;
             case R.id.a_dish_detail_new_footer_hover_trample: //没用
                 XHClick.mapStat(mAct, tongjiId, "底部浮动", "点踩按钮点击量");
//                 if("1".equals(dishLikeStatus)) {//当前是点赞
//                     handlerDishLikeState("3");
//                 }else{
//                     handlerDishLikeState("1");
//                 }
                 onChangeLikeState(false,false);
                 hindGoodLayout();

                break;
            case R.id.a_dish_detail_new_footer_hover_good_linear:
            case R.id.a_dish_detail_new_footer_hover_good_show: //展现点赞
                if(!LoginManager.isLogin()){
                    mAct.startActivity(new Intent(mAct,LoginByAccout.class));
                    return;
                }
                hoverLayout.setVisibility(View.GONE);
                goodLayoutParent.setVisibility(View.VISIBLE);
                break;
            case R.id.a_dish_detail_new_tv_num:
                XHClick.mapStat(mAct, tongjiId, "哈友相关作品", "更多作品点击量");
                Intent intentn = new Intent(mAct, FollowSubject.class);
                intentn.putExtra("dishCode",code);
                intentn.putExtra("title",mDishName);
                mAct.startActivity(intentn);
                break;
        }
    }

    /**
     * 处理view
     */
    public void hindGoodLayout(){
        goodLayoutParent.setVisibility(View.GONE);
        hoverLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 处理点赞点踩状态变化
     */
    private void handlerDishLikeState(String dishStatus){
        if("2".equals(dishStatus)){//点赞
            mGoodImg.setImageResource(R.drawable.i_good_activity);
            mNoLikeImg.setImageResource(R.drawable.i_not_good);
            hoverGoodImg.setImageResource(R.drawable.i_dish_detail_zan_good);

        }else if("1".equals(dishStatus)){//点踩
            mNoLikeImg.setImageResource(R.drawable.i_not_good_activity);
            mGoodImg.setImageResource(R.drawable.i_good_black);
            hoverGoodImg.setImageResource(R.drawable.i_dish_detail_zan_nolike);
        }else if("3".equals(dishStatus)){
            mGoodImg.setImageResource(R.drawable.i_good_black);
            mNoLikeImg.setImageResource(R.drawable.i_not_good);
            hoverGoodImg.setImageResource(R.drawable.i_dish_detail_zan);
        }
    }

    /**
     * 网络处理点赞和点踩
     * @param isLike
     * @param isGoodButton 是否是点赞按钮
     */
    private void onChangeLikeState(final boolean isLike, final boolean isGoodButton){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("code",code);
        map.put("status",isLike ? "2" : "1");
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeHate,map, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
                    if(arrayList.size() > 0){
                        mHoverNum.setText(arrayList.get(0).get("num"));
                    }
                    if(isLike){//点赞
                        if("2".equals(dishLikeStatus)){
                            dishLikeStatus="3";
                        }else {
                            dishLikeStatus="2";
                        }
                    }else{//取消点赞
                        if("1".equals(dishLikeStatus)){
                            dishLikeStatus="3";
                        }else {
                            dishLikeStatus="1";
                        }
                    }
                }
                handlerDishLikeState(dishLikeStatus);
            }
        });
    }

    /**
     * scrollview滚动监听
     */
    public void onSrollView(){
        //判断当前view是否在
        for (int i = 0; i < mAdLayout.getChildCount(); i++) {
            View dishAdDataView = mAdLayout.getChildAt(i);
            if (dishAdDataView != null && dishAdDataView instanceof DishGgDataViewNew) {
                int[] viewLocation = new int[2];
                dishAdDataView.getLocationOnScreen(viewLocation);
                if ((viewLocation[1] > Tools.getStatusBarHeight(mAct)
                        && viewLocation[1] < Tools.getScreenHeight() - ToolsDevice.dp2px(mAct, 57))) {
                    ((DishGgDataViewNew) dishAdDataView).onListScroll();
                }
            }
        }
    }

    /**
     * 隐藏底部view
     */
    public void hideFootView(){
        mAct.findViewById(R.id.a_dish_detail_new_footer).setVisibility(View.GONE);
    }

    /**
     * 展示底部view
     */
    public void showFootView(){
        mAct.findViewById(R.id.a_dish_detail_new_footer).setVisibility(View.VISIBLE);
    }

    /**
     * 重新刷新当前状态
     */
    public void handlerAskStatus(){
        setRequestAskButtonStatus();
    }
    /**
     * 处理当前用户状态。
     */
    private void setRequestAskButtonStatus(){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("code",code);
        ReqEncyptInternet.in().doEncypt(StringManager.api_askButtonStatus,map, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqInternet.REQ_OK_STRING){
                    Map<String,String> map= StringManager.getFirstMap(o);
                    if(map.containsKey("button")&&!TextUtils.isEmpty(map.get("button"))&&!"0".equals(StringManager.getFirstMap(map.get("button")).get("style"))) {
                        handlerAskButton(StringManager.getFirstMap(map.get("button")));
                    }else{
                        if (map.containsKey("status") && !TextUtils.isEmpty(map.get("status"))) {
                            handlrAskStatus(map.get("status"));
                        }
                    }
                }
            }
        });
    }

    /**
     * 对按钮处理
     * 会员去看看。
     */
    private void handlerAskButton(final Map<String,String> map){
        if(map.containsKey("title")&&map.containsKey("url")) {
            mAct.findViewById(R.id.a_dish_detail_new_footer_hover_layout).setVisibility(View.GONE);
            mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.VISIBLE);
            RelativeLayout bottom_layout = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_member_rela);
            bottom_layout.setVisibility(View.VISIBLE);
            TextView vip_immediately = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_member_tv);
            vip_immediately.setText(map.get("title"));
            if(map.containsKey("color"))vip_immediately.setTextColor(Color.parseColor(map.get("color")));
            if(map.containsKey("bgColor")){
                bottom_layout.setBackgroundColor(Color.parseColor(map.get("bgColor")));
                vip_immediately.setBackgroundColor(Color.parseColor(map.get("bgColor")));
            }
            //点击展示。
            vip_immediately.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCommon.openUrl(mAct,map.get("url"),false);
                }
            });
        }


    }
    /**
     * 处理状态：1：不显示，2：向作者提问，3：提醒作者开通问答，4：未登陆状态
     * @param status
     */
    private void handlrAskStatus(String status){
        askStatus=status;
        switch (status){
            case "2":
                mHoverTv.setText("向作者提问");
                break;
            case "3":
                mHoverTv.setText("提醒作者开通问答");
                break;
        }
        if("2".equals(status)||"3".equals(status)||"4".equals(status)){
            mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.VISIBLE);
            hoverLayout.setVisibility(View.VISIBLE);
        }else{
            mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.GONE);
            hoverLayout.setVisibility(View.GONE);
        }
    }
    public void setAuthorCode(String authorCode){
        this.authorCode= authorCode;
    }
     /**
      * 页面销毁时调用
     */
    public void onDestroy(){
        if(dishAdDataView!=null){
            dishAdDataView.onDestroy();
            dishAdDataView=null;
        }
    }

}
