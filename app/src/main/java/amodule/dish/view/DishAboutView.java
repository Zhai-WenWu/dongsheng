package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import third.mall.tool.ToolView;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.tongjiId;
import static com.xiangha.R.id.caipu_follow_rela;

/**
 * 简介
 */
public class DishAboutView extends ItemBaseView {
    private boolean showExplainState= false;
    private TextView dish_work_exp_tv;
    private Map<String,String> mapAbout;
    private Map<String,String> mapUser,mapPower;
    private TextView dish_follow_tv;
    private Activity activity;
    public DishAboutView(Context context) {
        super(context, R.layout.view_dish_header_about);
    }

    public DishAboutView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_header_about);
    }

    public DishAboutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_header_about);
    }

    @Override
    public void init() {
        super.init();

    }

    /**
     * 设置菜谱基础数据
     * @param map
     * @param activitys
     */
    public void setData(Map<String,String> map, Activity activitys){
        this.mapAbout = map;
        this.activity= activitys;
        setDishData();
        setExplainData(map.get("info"));
        setUserData(StringManager.getFirstMap(map.get("customer")));
    }

    /**
     * 设置用户数据
     * @param map
     */
    public void setUserData(Map<String,String> map){
        this.mapUser= map;
        setUserData();
    }

    /**
     * 设置用户权限信息
     * @param map
     */
    public void setUserPowerData(Map<String,String> map){
        if(map==null||map.size()<=0)return;
        this.mapPower= map;
        dish_follow_tv= (TextView) findViewById(R.id.dish_follow_tv);
        findViewById(R.id.cusType).setVisibility(mapPower.containsKey("isGourmet")&&"2".equals(mapPower.get("isGourmet"))?View.VISIBLE:View.GONE);
        setFollowState(mapPower);
        //点击关注
        findViewById(R.id.dish_follow_rela).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LoginManager.isLogin()) {
                    Intent intent = new Intent(context, LoginByAccout.class);
                    context.startActivity(intent);
                    return;
                }
                if(mapUser.containsKey("isFav")&&mapUser.get("isFav").equals("1")){
                    XHClick.mapStat(activity, DetailDish.tongjiId, "用户点击", "关注点击");
                    AppCommon.onAttentionClick(mapUser.get("customerCode"), "follow");
                    mapPower.put("isFav","2");
                    Tools.showToast(context,"已关注");
                    setFollowState(mapPower);
                }else{
                    XHClick.mapStat(activity, DetailDish.tongjiId, "用户点击", "已关注点击");
                    Intent intent = new Intent(activity, FriendHome.class);
                    intent.putExtra("code",mapUser.get("customerCode"));
                    activity.startActivityForResult(intent,1000);
//                    AppCommon.openUrl(activity,"userIndex.app?code="+lists.get(0).get("code"),true);
                }
            }
        });
        if(LoginManager.isLogin()
                && LoginManager.userInfo.get("code") != null
                && LoginManager.userInfo.get("code").equals(mapUser.get("customerCode"))){
            findViewById(R.id.dish_follow_rela).setVisibility(View.GONE);
        } else {
            findViewById(R.id.dish_follow_rela).setVisibility(View.VISIBLE);
        }
    }
    /**
     * 设置菜谱数据
     */
    private void setDishData(){
        findViewById(R.id.title_dish_exp_rela).setVisibility(View.VISIBLE);
        //独家是否显示
        TextView dish_title= (TextView) findViewById(R.id.dish_title);
        TextView dish_explain= (TextView) findViewById(R.id.dish_explain);
        if(mapAbout.containsKey("name")&&!TextUtils.isEmpty(mapAbout.get("name"))){
            dish_title.setText(mapAbout.get("name"));
        }
        String exclusive="";
        if(mapAbout.containsKey("isExclusive")&&mapAbout.get("isExclusive").equals("2"))exclusive="独家・";
        dish_explain.setText(exclusive+mapAbout.get("allClick")+"浏览・"+mapAbout.get("favorites")+"收藏");
    }

    private final int mInfoStrLength = 13;
    /**
     * 设置用户数据
     */
    private void setUserData(){
        ImageView cusImg= (ImageView) findViewById(R.id.auther_userImg);
        setViewImage(cusImg,mapUser.get("img"));
        if(mapUser.containsKey("isGourmet")&& mapUser.get("isGourmet").equals("2")){
            findViewById(R.id.cusType).setVisibility(View.VISIBLE);
        }else findViewById(R.id.cusType).setVisibility(View.GONE);
        TextView dish_user_name= (TextView) findViewById(R.id.dish_user_name);
        dish_user_name.setText(mapUser.get("nickName"));
        TextView dish_user_time= (TextView) findViewById(R.id.dish_user_time);
        String userIntro = handleUserIntro(mapUser.get("info"));
        dish_user_time.setText(TextUtils.isEmpty(userIntro) ? getResources().getString(R.string.user_intro_def) : userIntro);

        cusImg.setOnClickListener(onClickListener);
        dish_user_name.setOnClickListener(onClickListener);
        dish_user_name.setOnClickListener(onClickListener);
    }

    /**
     * Handling user's intro.
     * @param introStr original intro string
     * @return new intro string
     */
    private String handleUserIntro (String introStr) {
        if (TextUtils.isEmpty(introStr))
            return "";
        else {
            String regex = "^\\s*|\t*|\r*|\n*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(introStr);
            introStr = matcher.replaceAll("");
            if (introStr.length() > mInfoStrLength) {
                introStr = introStr.substring(0, mInfoStrLength) + "...";
            }
            return introStr;
        }
    }

    /**
     * 修改关注状态
     */
    private void setFollowState(Map<String, String> cursterMap) {
        if (cursterMap.containsKey("isFav") && "1".equals(cursterMap.get("isFav"))) {//未关注
            findViewById(R.id.dish_follow_rela).setVisibility(View.VISIBLE);
            findViewById(R.id.dish_follow_rela).setBackgroundResource(R.drawable.bg_circle_red_stroke_5);
            dish_follow_tv.setText("关注");
            String color = Tools.getColorStr(context, R.color.comment_color);
            dish_follow_tv.setTextColor(Color.parseColor(color));
        } else if (cursterMap.containsKey("isFav") && "2".equals(cursterMap.get("isFav"))) {//已关注
            findViewById(R.id.dish_follow_rela).setVisibility(View.VISIBLE);
            dish_follow_tv.setText("已关注");
            dish_follow_tv.setTextColor(Color.parseColor("#999999"));
            findViewById(R.id.dish_follow_rela).setBackgroundColor(Color.parseColor("#fffffe"));
        } else {
            findViewById(R.id.dish_follow_rela).setVisibility(View.GONE);
        }
    }
   
    /**
     *用户点击跳转页面
     */
    private View.OnClickListener onClickListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            XHClick.mapStat(activity, tongjiId, "用户点击", "头像点击量");
            AppCommon.openUrl(activity, UtilString.getListMapByJson(mapAbout.get("customer")).get(0).get("url"),true);
        }
    };

    /**
     * 设置心得描述数据
     * @param info
     */
    private void setExplainData(final String info){
        dish_work_exp_tv= (TextView) findViewById(R.id.dish_work_exp_tv);
        if(info.length()>0) {
            dish_work_exp_tv.setVisibility(View.VISIBLE);
            int num = setTextViewNum(0);
            num = (int) (num * 29.5);
            if (info.length() >= num) {
                showExplainState = true;
                String temp = info.substring(0, num);
                temp += "...展示";
                dish_work_exp_tv.setText(temp);
                setTextViewColor(dish_work_exp_tv, num, temp.length());

            } else {
                showExplainState = false;
                dish_work_exp_tv.setText(info);
            }
            dish_work_exp_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (showExplainState) {
                        showExplainState = false;
                        dish_work_exp_tv.setText(info);
                        dish_work_exp_tv.setTextColor(Color.parseColor("#333333"));
                    }
                }
            });
        }else dish_work_exp_tv.setVisibility(View.GONE);

    }
    /**
     * 获取值得买每行的字数
     *
     * @return
     */
    private int setTextViewNum(int distance_commend) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_16);//字体的大小
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance*2-distance_commend ;
        int tv_pad = ToolView.dip2px(context, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }
    /**
     * 设置文字变化
     *
     * @param view
     */
    private void setTextViewColor(TextView view, int start, int end) {
        SpannableStringBuilder builder = new SpannableStringBuilder(view.getText().toString());
        String color = Tools.getColorStr(context, R.color.comment_color);
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor(color));
        builder.setSpan(redSpan, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(builder);
    }
    /**
     * 获取值得买每行的字数
     *
     * @return
     */
    private int setTitleTextViewNum(int distance_commend) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_23);//字体的大小
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance*2-distance_commend ;
        int tv_pad = ToolView.dip2px(context, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }
}
