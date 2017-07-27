package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
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

import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * 菜谱底部原生所有内容：相关美食帖、广告、发帖入口、点赞点踩、提问入口
 * Created by Fang Ruijiao on 2017/7/13.
 */
public class DishFootControl implements View.OnClickListener{

    private Activity mAct;
    private LinearLayout mAdLayout,goodLayoutParent,goodLayout,trampleLayout,userDishLayout;
    private RelativeLayout mRecomentLayout;
    private TextView mRecommentNum,mRelevantTv,mHoverNum,mQuizTv;
    private ImageView mGoodShow,mGoodImg;

    private String code,mDishName;

    private boolean dishLikeState = false;

    public DishFootControl(Activity act,String code){
        mAct = act;
        this.code = code;
        init();
    }

    private void init(){
        mAdLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_tieshi_ad);
        mRecomentLayout = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_xiangguan);

        goodLayoutParent = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_layout);
        goodLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good);
        trampleLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_trample);
        userDishLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll_linear);

        mRecommentNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_tv_num);
        mRelevantTv = (TextView) mAct.findViewById(R.id.a_dish_detail_new_relevantTv);
        mHoverNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_number);
        mQuizTv = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_tv);
        mGoodImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_good_img);
        mGoodShow = (ImageView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_show);

        DishAdDataViewNew dishAdDataView = new DishAdDataViewNew(mAct);
        dishAdDataView.getRequest(mAct, mAdLayout);
        mRecomentLayout.setOnClickListener(this);
        goodLayout.setOnClickListener(this);
        trampleLayout.setOnClickListener(this);
        mRelevantTv.setOnClickListener(this);
        mQuizTv.setOnClickListener(this);
        mGoodShow.setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.VISIBLE);
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
            for (final Map<String, String> map : arrayList) {
                view = inflater.inflate(R.layout.a_dish_detail_new_footer_item, null);
                ImageView dishImg = (ImageView) view.findViewById(R.id.a_dish_detail_show_img);
                ImageView userImg = (ImageView) view.findViewById(R.id.a_dish_detail_user_icon);
                ImageView zanImg = (ImageView) view.findViewById(R.id.a_dish_detail_zan);
                TextView userName = (TextView) view.findViewById(R.id.a_dish_detail_user_name);
                TextView dishTime = (TextView) view.findViewById(R.id.a_dish_detail_time);
                TextView zanNumber = (TextView) view.findViewById(R.id.a_dish_detail_zan_numer);

                String customer = map.get("customer");
                ArrayList<Map<String, String>> customerArray = StringManager.getListMapByJson(customer);
                if(customerArray.size() > 0) {
                    Map<String, String> customerMap = customerArray.get(0);
                    userName.setText(customerMap.get("nickName"));
                    setViewImage(userImg, customerMap.get("img"), 500);
                    String userCode = map.get("userCode");
                    setGotoFriendHome(userImg, userCode);
                    setGotoFriendHome(userName, userCode);
                }

                final String zanNumberStr = map.get("likeNum");
                final String subjectCode = map.get("subjectCode");
                setViewImage(dishImg, map.get("img"), 0);
                zanImg.setImageResource(TextUtils.isEmpty(map.get("isLike")) ? R.drawable.z_quan_home_body_ico_good : R.drawable.z_quan_home_body_ico_good_active);
                dishTime.setText(map.get("timeShow"));
                zanNumber.setText(zanNumberStr);
                zanImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tools.showToast(mAct, zanNumberStr);
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent it = new Intent(mAct, ShowSubject.class);
                        it.putExtra("code", subjectCode);
                        mAct.startActivity(it);
                    }
                });
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
            dishLikeState = "3".equals(map.get("status"));
        }else{
            mHoverNum.setText("0");
            dishLikeState = false;
        }
    }

     private void setGotoFriendHome(View view, final String userCode){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mAct.startActivity(intent);
                break;
            case R.id.a_dish_detail_new_relevantTv: //晒我做的这道菜
                Intent showIntent = new Intent(mAct, UploadSubjectNew.class);
                showIntent.putExtra("title",mDishName);
                showIntent.putExtra("dishCode",code);
                showIntent.putExtra("skip", true);
                mAct.startActivity(showIntent);
                break;
            case R.id.a_dish_detail_new_footer_hover_tv: //提问作者
                Tools.showToast(mAct,"提问作者");
                break;
             case R.id.a_dish_detail_new_footer_hover_good: //有用
                Tools.showToast(mAct,"有用");
                 if(!dishLikeState){
                     dishLikeState = !dishLikeState;
                     mGoodImg.setImageResource(R.drawable.i_good_activity);
                     onChangeLikeState(dishLikeState);
                 }
                 hindGoodLayout();
                break;
             case R.id.a_dish_detail_new_footer_hover_trample: //没用
                Tools.showToast(mAct,"没用");
                 if(dishLikeState){
                     dishLikeState = !dishLikeState;
                     mGoodImg.setImageResource(R.drawable.i_good);
                     onChangeLikeState(dishLikeState);
                 }
                 hindGoodLayout();
                break;
            case R.id.a_dish_detail_new_footer_hover_good_show: //展现点赞
                goodLayoutParent.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void hindGoodLayout(){
        goodLayoutParent.setVisibility(View.GONE);
    }

    private void onChangeLikeState(boolean isLike){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("code",code);
        map.put("status",isLike ? "2" : "1");
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeHate,map, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
                    if(arrayList.size() > 0){
                        mHoverNum.setText(arrayList.get(0).get("num"));
                    }
                }
            }
        });
    }
}
