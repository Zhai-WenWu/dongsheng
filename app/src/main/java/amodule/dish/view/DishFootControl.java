package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
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

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.activity.FollowSubject;
import amodule.quan.activity.ShowSubject;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

import static amodule.dish.activity.DetailDish.tongjiId;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * 菜谱详情页底部view的控制器
 * 菜谱底部原生所有内容：相关美食帖、广告、发帖入口、点赞点踩、提问入口
 * Created by Fang Ruijiao on 2017/7/13.
 */
public class DishFootControl implements View.OnClickListener{

    private Activity mAct;
    private LinearLayout mAdLayout,userDishLayout;
    private RelativeLayout mRecomentLayout;
    private TextView mRecommentNum,mRelevantTv;

    private DishAdDataViewNew dishAdDataView;

    private String code,mDishName;

    private String dishLikeStatus = "1";//当前点击状态。0-无操作，1-反对，2-赞同

    //处理底部浮动view
    private LinearLayout goodLayoutParent,hoverLayout;
    private ImageView mGoodImg,mNoLikeImg,hoverGoodImg;
    private TextView mHoverNum;

    public DishFootControl(Activity act,String code){
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

        dishAdDataView = new DishAdDataViewNew(mAct);
        dishAdDataView.getRequest(mAct, mAdLayout);
        mRecomentLayout.setOnClickListener(this);
        mRelevantTv.setOnClickListener(this);
        mRecommentNum.setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.VISIBLE);
        initFudongView();
    }

    /**
     * 处理底部浮动的view
     */
    private void initFudongView(){
        goodLayoutParent = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_layout);
        hoverLayout= (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_layout);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good).setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_trample).setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_tv).setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_linear).setOnClickListener(this);
        mHoverNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_number);
        mGoodImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_good_img);
        mNoLikeImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_img);
        hoverGoodImg= (ImageView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_show);
        hoverGoodImg.setOnClickListener(this);

    }
    public void setDishInfo(String dishName) {
        mDishName = dishName;
    }

    public void initUserDish(String dishJson){
        ArrayList<Map<String, String>> arrayList = getListMapByJson(dishJson);
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
                zanImg.setImageResource(isLike ? R.drawable.z_quan_home_body_ico_good_active : R.drawable.z_quan_home_body_ico_good);
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
                            LinkedHashMap<String, String> map = new LinkedHashMap<>();
                            map.put("subjectCode", subjectCode);
                            map.put("type", "likeList");
                            ReqEncyptInternet.in().doEncypt(StringManager.api_quanSetSubject, map, new InternetCallback(mAct) {
                                @Override
                                public void loaded(int flag, String s, Object o) {
                                    if (flag >= ReqInternet.REQ_OK_STRING) {
                                        try {
                                            newIsLike = true;
                                            zanImg.setImageResource(R.drawable.z_quan_home_body_ico_good_active);
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
            //点赞/点踩的状态(1:无，2:点踩，3:点赞)
            dishLikeStatus=map.get("status");
        }else{
            mHoverNum.setText("0");
        }
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
                showIntent.putExtra("skip", true);
                showIntent.putExtra("cid", "1");
                mAct.startActivity(showIntent);
                XHClick.mapStat(mAct, tongjiId, "晒我做的这道菜", "晒我做的这道菜点击量");
                break;
            case R.id.a_dish_detail_new_footer_hover_tv: //提问作者
                XHClick.mapStat(mAct, tongjiId, "底部浮动", "向作者提问点击量");
                Tools.showToast(mAct,"提问作者");
                break;
             case R.id.a_dish_detail_new_footer_hover_good: //有用
                 XHClick.mapStat(mAct, tongjiId, "底部浮动", "点赞按钮点击量");
                 boolean dishLikeGood =true;
                 if("3".equals(dishLikeStatus)) {//当前是点赞
                     dishLikeGood = false;
                     handlerDishLikeState("1");
                 }else{
                     dishLikeGood =true;//去点赞
                     handlerDishLikeState("3");
                 }
                 onChangeLikeState(dishLikeGood,true);
                 hindGoodLayout();
                break;
             case R.id.a_dish_detail_new_footer_hover_trample: //没用
                 XHClick.mapStat(mAct, tongjiId, "底部浮动", "点踩按钮点击量");
                 boolean dishLikeHover =true;
                 if(TextUtils.isEmpty(dishLikeStatus)||!"2".equals(dishLikeStatus)) {//当前是点赞或没有操作
                     dishLikeHover = false;
                     handlerDishLikeState("2");
                     onChangeLikeState(dishLikeHover,false);
                 }else {
//                     dishLikeHover = true;//去点赞
                     dishLikeStatus="1";
                     handlerDishLikeState("1");
                 }
                 hindGoodLayout();

                break;
            case R.id.a_dish_detail_new_footer_hover_good_linear:
            case R.id.a_dish_detail_new_footer_hover_good_show: //展现点赞
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
        if("3".equals(dishStatus)){//点赞
            mGoodImg.setImageResource(R.drawable.i_good_activity);
            mNoLikeImg.setImageResource(R.drawable.i_not_good);
            hoverGoodImg.setImageResource(R.drawable.i_dish_detail_zan_good);

        }else if("2".equals(dishStatus)){//点踩
            mNoLikeImg.setImageResource(R.drawable.i_not_good_activity);
            mGoodImg.setImageResource(R.drawable.i_good_black);
            hoverGoodImg.setImageResource(R.drawable.i_dish_detail_zan_nolike);
        }else if("1".equals(dishStatus)){
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
        Log.i("zyj","isLike::"+isLike+":::dishLikeStatus"+dishLikeStatus);
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeHate,map, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object o) {
                Log.i("zyj","api_getDishLikeHate::"+o);
                if(i >= ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
                    if(arrayList.size() > 0){
                        mHoverNum.setText(arrayList.get(0).get("num"));
                    }
                    if(isLike){//点赞
                        dishLikeStatus="3";//点赞
                    }else{//取消点赞
                        if("3".equals(dishLikeStatus)){
                            if(isGoodButton)dishLikeStatus="1";//点赞
                            else dishLikeStatus="2";//点踩
                        }else if("2".equals(dishLikeStatus)){
                            dishLikeStatus="1";//点踩
                        }else if("1".equals(dishLikeStatus)){
                            dishLikeStatus="2";//点踩
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
            if (dishAdDataView != null && dishAdDataView instanceof DishAdDataViewNew) {
                int[] viewLocation = new int[2];
                dishAdDataView.getLocationOnScreen(viewLocation);
                if ((viewLocation[1] > Tools.getStatusBarHeight(mAct)
                        && viewLocation[1] < Tools.getScreenHeight() - ToolsDevice.dp2px(mAct, 57))) {
                    ((DishAdDataViewNew) dishAdDataView).onListScroll();
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
}
