package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

/**
 * 菜谱详情页面
 */
public class DishHoverViewControl implements View.OnClickListener{
    private Activity mAct;
    private String dishLikeStatus = "3";//当前点击状态。3-无操作，1-反对，2-赞同
    //处理底部浮动view
    private LinearLayout goodLayoutParent,hoverLayout;
    private ImageView mGoodImg,mNoLikeImg,hoverGoodImg;
    private TextView mHoverNum,mHoverTv,showCaipuTv;
    private String askStatus="";
    private String code,authorCode,dishName;
    private Map<String,String> mapQA;
    public DishHoverViewControl(Activity activity){
        mAct=activity;
    }
    public void initView(){
        initFudongView();
    }
    public void setAuthorCode(String authorCode){
        this.authorCode = authorCode;
    }
    /**
     * 处理底部浮动的view
     */
    private void initFudongView(){
        goodLayoutParent = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_layout);
        hoverLayout= (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_layout);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good).setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_trample).setOnClickListener(this);
        mHoverTv= (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_tv);
        showCaipuTv= (TextView) mAct.findViewById(R.id.a_dish_detail_hover_show_caipu);
        mHoverTv.setOnClickListener(this);
        showCaipuTv.setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_linear).setOnClickListener(this);
        mHoverNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_number);
        mGoodImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_good_img);
        mNoLikeImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_img);
        hoverGoodImg= (ImageView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_show);
        hoverGoodImg.setOnClickListener(this);
        hoverGoodImg.setVisibility(View.GONE);
    }

    /**
     * 处理数据
     * @param maps
     */
    public void initData(Map<String,String> maps,String dishcode,String dishName){
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover).setVisibility(View.VISIBLE);
        hoverGoodImg.setVisibility(View.VISIBLE);
        this.code= dishcode;
        this.dishName = dishName;
        String temp = maps.get("likeNum");
        handlerDishLikeState(maps.get("likeStatus"),temp);
        initStateButton(StringManager.getFirstMap(maps.get("qaButton")));
    }

    private void initStateButton(Map<String,String> mapQA){
        if(mapQA==null||mapQA.size()<=0)return;
        this.mapQA= mapQA;
        int roundRadius = Tools.getDimen(mAct,R.dimen.dp_3); // 8dp 圆角半径
        String bgColor=mapQA.containsKey("bgColor")&&!TextUtils.isEmpty(mapQA.get("bgColor"))?mapQA.get("bgColor"):"#f23030";
        int fillColor = Color.parseColor(bgColor);//内部填充颜色
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(Color.parseColor(bgColor));
        gd.setCornerRadius(roundRadius);
        mHoverTv.setBackgroundDrawable(gd);
        mHoverTv.setTextColor(Color.parseColor(mapQA.containsKey("color")&&!TextUtils.isEmpty(mapQA.get("color"))?mapQA.get("color"):"#fffffe"));
        mHoverTv.setText(mapQA.get("text"));
        XHClick.mapStat(mAct, DetailDish.tongjiId_detail, "向作者提问按钮状态", mapQA.get("text"));

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.a_dish_detail_new_footer_hover_tv: //提问作者
//                if(!LoginManager.isLogin()){
//                    mAct.startActivity(new Intent(mAct,LoginByAccout.class));
//                    return;
//                }
                if(mapQA!=null&&mapQA.containsKey("isJump")&&"2".equals(mapQA.get("isJump"))){
                   AppCommon.openUrl(mAct,mapQA.get("url"),false);
                }else{
                    XHClick.mapStat(mAct, DetailDish.tongjiId_detail, "底部浮动", "向作者提问点击量");
                    if(mapQA!=null&&mapQA.containsKey("toast")&&!TextUtils.isEmpty(mapQA.get("toast"))){
                        Tools.showToast(mAct,mapQA.get("toast"));
                    }
                }
                break;
            case R.id.a_dish_detail_new_footer_hover_good: //有用
                if(!LoginManager.isLogin()){
                    mAct.startActivity(new Intent(mAct,LoginByAccout.class));
                    return;
                }
                XHClick.mapStat(mAct, DetailDish.tongjiId_detail, "底部浮动", "点赞按钮点击量");
                onChangeLikeState(true,true);
                hindGoodLayout();
                break;
            case R.id.a_dish_detail_new_footer_hover_trample: //没用
                XHClick.mapStat(mAct, DetailDish.tongjiId_detail, "底部浮动", "点踩按钮点击量");
                onChangeLikeState(false,false);
                hindGoodLayout();
                break;
            case R.id.a_dish_detail_new_footer_hover_good_linear:
            case R.id.a_dish_detail_new_footer_hover_good_show: //展现点赞
                if(!LoginManager.isLogin()){
                    mAct.startActivity(new Intent(mAct,LoginByAccout.class));
                    return;
                }
                if(mapQA==null)return;
                hoverLayout.setVisibility(View.GONE);
                goodLayoutParent.setVisibility(View.VISIBLE);
                break;
            case R.id.a_dish_detail_hover_show_caipu:
                Intent showIntent = new Intent(mAct, UploadSubjectNew.class);
                showIntent.putExtra("dishCode",code);
                showIntent.putExtra("name",dishName);
                showIntent.putExtra("skip", true);
                showIntent.putExtra("cid", "1");
                mAct.startActivity(showIntent);
                XHClick.mapStat(mAct, DetailDish.tongjiId_detail, "底部浮动", "晒美食点击量");
                break;
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
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeHate,map, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object o) {
                String numtext="";
                if(i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> mapTemp = StringManager.getFirstMap(o);
                    if (mapTemp!=null&&mapTemp.size() > 0) {
                        numtext = mapTemp.get("num");
                    }
                    if (mapTemp.size() > 0&&TextUtils.isEmpty(mapTemp.get("status"))) {
                        dishLikeStatus= mapTemp.get("status");
                    }else {
                        if (isLike) {//点赞
                            if ("2".equals(dishLikeStatus)) {
                                dishLikeStatus = "3";
                            } else {
                                dishLikeStatus = "2";
                            }
                        } else {//取消点赞
                            if ("1".equals(dishLikeStatus)) {
                                dishLikeStatus = "3";
                            } else {
                                dishLikeStatus = "1";
                            }
                        }
                    }
                }
                handlerDishLikeState(dishLikeStatus,numtext);
            }
        });
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
    private void handlerDishLikeState(String dishStatus,String likeNum){
        if("0".equals(likeNum))likeNum="";
        if("2".equals(dishStatus)){//点赞
            mGoodImg.setImageResource(R.drawable.iv_good_good_arrow);
            mNoLikeImg.setImageResource(R.drawable.iv_bad_no_arrow);
            hoverGoodImg.setImageResource(R.drawable.iv_dish_all_good);
            mHoverNum.setTextColor(Color.parseColor("#f23030"));
            mHoverNum.setText("有用"+likeNum);
        }else if("1".equals(dishStatus)){//点踩
            mNoLikeImg.setImageResource(R.drawable.iv_bad_good_arrow);
            mGoodImg.setImageResource(R.drawable.iv_good_no_arrow);
            hoverGoodImg.setImageResource(R.drawable.iv_dish_all_bad);
            mHoverNum.setTextColor(Color.parseColor("#f23030"));
            mHoverNum.setText("没用");
        }else if("3".equals(dishStatus)){
            mGoodImg.setImageResource(R.drawable.iv_good_no_arrow);
            mNoLikeImg.setImageResource(R.drawable.iv_bad_no_arrow);
            hoverGoodImg.setImageResource(R.drawable.iv_dish_all_no);
            mHoverNum.setTextColor(Color.parseColor("#333333"));
            mHoverNum.setText("有用"+likeNum);
        }
    }
}
