package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import amodule.answer.activity.AskEditActivity;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 菜谱详情页面
 */
public class DishHoverViewControl implements View.OnClickListener{
    private Activity mAct;
    private String dishLikeStatus = "3";//当前点击状态。3-无操作，1-反对，2-赞同
    //处理底部浮动view
    private LinearLayout goodLayoutParent,hoverLayout;
    private ImageView mGoodImg,mNoLikeImg,hoverGoodImg;
    private TextView mHoverNum,mHoverTv;
    private String askStatus="";
    private String code,authorCode;

    public DishHoverViewControl(Activity activity){
        mAct=activity;
    }
    public void initView(){
        initFudongView();
    }
    public void setCode(String code){
        this.code=code;
        setRequestAskButtonStatus();
    }
    public void setAuthorCode(String authorCode){
        this.authorCode = authorCode;
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
        mHoverTv.setOnClickListener(this);
        mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_linear).setOnClickListener(this);
        mHoverNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_number);
        mGoodImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_good_img);
        mNoLikeImg = (ImageView) mAct.findViewById(R.id.a_dish_hover_img);
        hoverGoodImg= (ImageView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_good_show);
        hoverGoodImg.setOnClickListener(this);
    }

    /**
     * 处理点赞数据
     */
    public void initLikeState(ArrayList<Map<String,String>> arrayList){
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
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.a_dish_detail_new_footer_hover_tv: //提问作者
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
                onChangeLikeState(true,true);
                hindGoodLayout();
                break;
            case R.id.a_dish_detail_new_footer_hover_trample: //没用
                XHClick.mapStat(mAct, tongjiId, "底部浮动", "点踩按钮点击量");
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
    /**
     * 处理当前用户状态。
     */
    private void setRequestAskButtonStatus(){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("code",code);
        ReqEncyptInternet.in().doEncypt(StringManager.api_askButtonStatus,map, new InternetCallback(mAct) {
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
}
