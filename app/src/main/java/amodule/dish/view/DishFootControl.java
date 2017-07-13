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
import java.util.HashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.RelevantDishList;
import amodule.quan.activity.ShowSubject;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * Created by Fang Ruijiao on 2017/7/13.
 */

public class DishFootControl implements View.OnClickListener{

    private Activity mAct;
    private LinearLayout mAdLayout,goodLayoutParent,goodLayout,trampleLayout,userDishLayout;
    private RelativeLayout mRecomentLayout;
    private TextView mRecommentNum,mRelevantTv,mHoverNum,mQuizTv;
    private ImageView mGoodShow;

    private String code;

    public DishFootControl(Activity act){
        mAct = act;
    }

    public void init(String recomenNum,String hoverNum,String dishJson){

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
        mGoodShow = (ImageView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_show);

        DishAdDataViewNew dishAdDataView = new DishAdDataViewNew(mAct);
        dishAdDataView.getRequest(mAct, mAdLayout);
        mRecomentLayout.setOnClickListener(this);
        goodLayout.setOnClickListener(this);
        trampleLayout.setOnClickListener(this);
        mRelevantTv.setOnClickListener(this);
        mQuizTv.setOnClickListener(this);
        mGoodShow.setOnClickListener(this);
        mRecommentNum.setText(recomenNum + "道");
        mHoverNum.setText(hoverNum);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.VISIBLE);

        initUserDish(dishJson);
    }

    private void initUserDish(String dishJson){
        LayoutInflater inflater = LayoutInflater.from(mAct);
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dishJson);
        Map<String, String> dataMap;
        for(int i = 0; i < 4; i ++){
            dataMap = new HashMap<>();
            dataMap.put("zanNumber",String.valueOf(10 + i));
            dataMap.put("subjectCode","1020" + String.valueOf(10 + i));
            dataMap.put("userCode","2350" + String.valueOf(10 + i));
            dataMap.put("dishImg","http://s1.cdn.xiangha.com/caipu/201608/2512/251224023359.jpg/OTAweDYwMA");
            dataMap.put("userImg","http://s1.cdn.xiangha.com/i/201605/2719/57482b15bf2d9.jpg/MTAweDEwMA");
            dataMap.put("userName","米西" + i);
            dataMap.put("dishTime","1" + i + "小时前");
            arrayList.add(dataMap);
        }
        if(arrayList.size() > 0)
            mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.VISIBLE);
        else{
            mAct.findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.GONE);
            return;
        }
        View view;
        for(final Map<String,String> map : arrayList){
            view = inflater.inflate(R.layout.a_dish_detail_new_footer_item,null);
            ImageView dishImg = (ImageView) view.findViewById(R.id.a_dish_detail_show_img);
            ImageView userImg = (ImageView) view.findViewById(R.id.a_dish_detail_user_icon);
            ImageView zanImg = (ImageView) view.findViewById(R.id.a_dish_detail_zan);
            TextView userName = (TextView) view.findViewById(R.id.a_dish_detail_user_name);
            TextView dishTime = (TextView) view.findViewById(R.id.a_dish_detail_time);
            TextView zanNumber = (TextView) view.findViewById(R.id.a_dish_detail_zan_numer);

            final String zanNumberStr = map.get("zanNumber");
            final String subjectCode = map.get("subjectCode");
            final String userCode = map.get("userCode");

            setViewImage(dishImg,map.get("dishImg"),0);
            setViewImage(userImg,map.get("userImg"),500);
            zanImg.setImageResource(TextUtils.isEmpty(map.get("isZan")) ? R.drawable.z_quan_home_body_ico_good : R.drawable.z_quan_home_body_ico_good_active);
            userName.setText(map.get("userName"));
            dishTime.setText(map.get("dishTime"));
            zanNumber.setText(zanNumberStr);

            setGotoFriendHome(userImg,userCode);
            setGotoFriendHome(userName,userCode);
            zanImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tools.showToast(mAct,zanNumberStr);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(mAct, ShowSubject.class);
                    it.putExtra("code",subjectCode);
                    mAct.startActivity(it);
                }
            });
            userDishLayout.addView(view);
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
                Intent intent = new Intent(mAct, RelevantDishList.class);
                intent.putExtra("code",code);
                mAct.startActivity(intent);
                break;
            case R.id.a_dish_detail_new_relevantTv: //晒我做的这道菜
                Intent showIntent = new Intent(mAct, UploadSubjectNew.class);
                showIntent.putExtra("dishCode",code);
                mAct.startActivity(showIntent);
                break;
            case R.id.a_dish_detail_new_footer_hover_tv: //提问作者
                Tools.showToast(mAct,"提问作者");
                break;
             case R.id.a_dish_detail_new_footer_hover_good: //有用
                Tools.showToast(mAct,"有用");
                 hindGoodLayout();
                break;
             case R.id.a_dish_detail_new_footer_hover_trample: //没用
                Tools.showToast(mAct,"没用");
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
}
