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

import acore.override.view.ItemBaseView;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import third.mall.tool.ToolView;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.tongjiId;
import static com.xiangha.R.id.caipu_exp_title;
import static com.xiangha.R.id.caipu_follow_rela;

/**
 * 简介
 */
public class DishAboutView extends ItemBaseView {
    private boolean showExplainState= false;
    private TextView caipu_work_exp_tv;
    private Map<String,String> mapAbout;
    private ImageView caipu_follow_img;
    private TextView caipu_follow_tv;
    private Activity activity;
    private ArrayList<Map<String,String>> lists ;
    public DishAboutView(Context context) {
        super(context,R.layout.view_dish_header_about);
    }

    public DishAboutView(Context context, AttributeSet attrs) {
        super(context, attrs,R.layout.view_dish_header_about);
    }

    public DishAboutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,R.layout.view_dish_header_about);
    }

    @Override
    public void init() {
        super.init();

    }

    public void reset(){

    }

    public void setData(Map<String, String> map, Activity activitys, Map<String, String> permissionMap){
        this.mapAbout = map;
        this.activity= activitys;
        setCaipuData();
        setUserData(UtilString.getListMapByJson(map.get("customer")));
        setExplainData(map.get("info"));
    }

    /**
     * 设置菜谱数据
     */
    private void setCaipuData(){

        //独家是否显示
        int num=setTitleTextViewNum(0);
        findViewById(caipu_exp_title).setVisibility(View.GONE);
//        if(mapAbout.containsKey("exclusive")&&mapAbout.get("exclusive").equals("2")){
//            findViewById(caipu_exp_title).setVisibility(View.VISIBLE);
//            num=setTitleTextViewNum(Tools.getDimen(context,R.dimen.dp_34));
//        }else {
//            num=setTitleTextViewNum(0);
//        }
        TextView caipu_exp_title_one= (TextView) findViewById(R.id.caipu_exp_title_one);
        TextView caipu_exp_title_two= (TextView) findViewById(R.id.caipu_exp_title_two);
        String name=mapAbout.get("name");
        if(num<name.length()){
            caipu_exp_title_two.setVisibility(View.VISIBLE);
            caipu_exp_title_one.setVisibility(View.VISIBLE);
            caipu_exp_title_one.setText(name.substring(0,num));
            caipu_exp_title_two.setText(name.substring(num,name.length()));
        }else{
            caipu_exp_title_one.setText(name);
            caipu_exp_title_two.setVisibility(View.GONE);
            caipu_exp_title_one.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.caipu_exp_title_one).setVisibility(View.VISIBLE);
        TextView caipu_exp_title_fav= (TextView) findViewById(R.id.caipu_exp_title_fav);
        String exclusive="";
        if(mapAbout.containsKey("exclusive")&&mapAbout.get("exclusive").equals("2"))exclusive="独家・";

        caipu_exp_title_fav.setText(exclusive+mapAbout.get("allClick")+"浏览・"+mapAbout.get("favorites")+"收藏");
    }

    private final int mInfoStrLength = 13;
    /**
     * 设置用户数据
     */
    private void setUserData(final ArrayList<Map<String,String>> list){
        this.lists=list;
        ImageView cusImg= (ImageView) findViewById(R.id.auther_userImg);
        setViewImage(cusImg,lists.get(0).get("img"));
        if(lists.get(0).containsKey("isGourmet")&& lists.get(0).get("isGourmet").equals("2")){
            findViewById(R.id.cusType).setVisibility(View.VISIBLE);
        }else findViewById(R.id.cusType).setVisibility(View.GONE);
        TextView caipu_user_name= (TextView) findViewById(R.id.caipu_user_name);
        caipu_user_name.setText(lists.get(0).get("nickName"));
//        lists.get(0).put("color","#ff533c");
        if(lists.get(0).containsKey("color")){
            caipu_user_name.setTextColor(Color.parseColor(lists.get(0).get("color")));
        }
        ImageView vipImg = (ImageView) findViewById(R.id.caipu_user_vip);
        AppCommon.setVip(activity,vipImg,lists.get(0).get("vip"));
        TextView caipu_user_time= (TextView) findViewById(R.id.caipu_user_time);
        String userIntro = handleUserIntro(lists.get(0).get("info"));
        caipu_user_time.setText(TextUtils.isEmpty(userIntro) ? getResources().getString(R.string.user_intro_def) : userIntro);
//        if(mapAbout.containsKey("timeShowV43")&& !TextUtils.isEmpty(mapAbout.get("timeShowV43"))){
//            caipu_user_time.setVisibility(View.VISIBLE);
//            caipu_user_time.setText("创建于"+mapAbout.get("timeShowV43"));
//        }else caipu_user_time.setVisibility(View.GONE);

        caipu_follow_img= (ImageView) findViewById(R.id.caipu_follow_img);
        caipu_follow_tv= (TextView) findViewById(R.id.caipu_follow_tv);
        setFollowState(lists.get(0));
        //点击关注
        findViewById(caipu_follow_rela).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LoginManager.isLogin()) {
                    Intent intent = new Intent(context, LoginByAccout.class);
                    context.startActivity(intent);
                    return;
                }
                if(lists.get(0).containsKey("isFav")&&lists.get(0).get("isFav").equals("1")){
                    XHClick.mapStat(activity, DetailDish.tongjiId, "用户点击", "关注点击");
                    AppCommon.onAttentionClick(lists.get(0).get("code"), "follow");
                    lists.get(0).put("isFav","2");
                    Tools.showToast(context,"已关注");
                    setFollowState(lists.get(0));
                }else{
                    XHClick.mapStat(activity, DetailDish.tongjiId, "用户点击", "已关注点击");
                    Intent intent = new Intent(activity, FriendHome.class);
                    intent.putExtra("code",lists.get(0).get("code"));
                    activity.startActivityForResult(intent,1000);
//                    AppCommon.openUrl(activity,"userIndex.app?code="+lists.get(0).get("code"),true);
                }
            }
        });
        if(LoginManager.isLogin()
                && LoginManager.userInfo.get("code") != null
                && LoginManager.userInfo.get("code").equals(lists.get(0).get("code"))){
            findViewById(caipu_follow_rela).setVisibility(View.GONE);
        } else {
            findViewById(caipu_follow_rela).setVisibility(View.VISIBLE);
        }
        cusImg.setOnClickListener(onClickListener);
        caipu_user_name.setOnClickListener(onClickListener);
        caipu_user_time.setOnClickListener(onClickListener);
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
            findViewById(caipu_follow_rela).setVisibility(View.VISIBLE);
            int dp_10 = Tools.getDimen(context,R.dimen.dp_10);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(dp_10,dp_10);
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            caipu_follow_img.setLayoutParams(layoutParams);
            caipu_follow_img.setBackgroundResource(R.drawable.dish_follow_a);
            findViewById(caipu_follow_rela).setBackgroundResource(R.drawable.bg_circle_follow_5);
            caipu_follow_tv.setText("关注");
            String color = Tools.getColorStr(context,R.color.comment_color);
            caipu_follow_tv.setTextColor(Color.parseColor(color));
        } else if (cursterMap.containsKey("isFav") && "2".equals(cursterMap.get("isFav"))) {//已关注
            findViewById(caipu_follow_rela).setVisibility(View.VISIBLE);
            int dp_12 = Tools.getDimen(context,R.dimen.dp_12);
            int dp_9 = Tools.getDimen(context,R.dimen.dp_9);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(dp_12,dp_9);
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            layoutParams.setMargins(0,Tools.getDimen(context,R.dimen.dp_1),0,0);
            caipu_follow_img.setLayoutParams(layoutParams);
            caipu_follow_img.setBackgroundResource(R.drawable.circle_follow_user_right);
            caipu_follow_tv.setText("已关注");
            caipu_follow_tv.setTextColor(Color.parseColor("#999999"));
            findViewById(caipu_follow_rela).setBackgroundColor(Color.parseColor("#fffffe"));
        } else {
            findViewById(caipu_follow_rela).setVisibility(View.GONE);
        }
    }
    /**
     * 修改关注状态
     */
    public void setNewFollowState(String folState) {

        if ("2".equals(folState)) {//未关注\
            lists.get(0).put("isFav","1");
            findViewById(caipu_follow_rela).setVisibility(View.VISIBLE);
            int dp_10 = Tools.getDimen(context,R.dimen.dp_10);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(dp_10,dp_10);
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            caipu_follow_img.setLayoutParams(layoutParams);
            caipu_follow_img.setBackgroundResource(R.drawable.dish_follow_a);
            findViewById(caipu_follow_rela).setBackgroundResource(R.drawable.bg_circle_follow_5);
            caipu_follow_tv.setText("关注");
            String color = Tools.getColorStr(context,R.color.comment_color);
            caipu_follow_tv.setTextColor(Color.parseColor(color));
        } else if ( "2".equals(folState)) {//已关注
            lists.get(0).put("isFav","1");
            findViewById(caipu_follow_rela).setVisibility(View.VISIBLE);
            int dp_12 = Tools.getDimen(context,R.dimen.dp_12);
            int dp_9 = Tools.getDimen(context,R.dimen.dp_9);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(dp_12,dp_9);
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            layoutParams.setMargins(0,Tools.getDimen(context,R.dimen.dp_1),0,0);
            caipu_follow_img.setLayoutParams(layoutParams);
            caipu_follow_img.setBackgroundResource(R.drawable.circle_follow_user_right);
            caipu_follow_tv.setText("已关注");
            caipu_follow_tv.setTextColor(Color.parseColor("#999999"));
            findViewById(caipu_follow_rela).setBackgroundColor(Color.parseColor("#fffffe"));
        } else {

        }

    }
    /**
     *用户点击跳转页面
     */
    private OnClickListener onClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            XHClick.mapStat(activity, tongjiId, "用户点击", "头像点击量");
            AppCommon.openUrl(activity,UtilString.getListMapByJson(mapAbout.get("customer")).get(0).get("url"),true);
        }
    };

    /**
     * 设置心得描述数据
     * @param info
     */
    private void setExplainData(final String info){
       caipu_work_exp_tv= (TextView) findViewById(R.id.caipu_work_exp_tv);
        if(info.length()>0) {
            caipu_work_exp_tv.setVisibility(View.VISIBLE);
            int num = setTextViewNum(0);
            num = (int) (num * 29.5);
            if (info.length() >= num) {
                showExplainState = true;
                String temp = info.substring(0, num);
                temp += "...展示";
                caipu_work_exp_tv.setText(temp);
                setTextViewColor(caipu_work_exp_tv, num, temp.length());

            } else {
                showExplainState = false;
                caipu_work_exp_tv.setText(info);
            }
            caipu_work_exp_tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (showExplainState) {
                        showExplainState = false;
                        caipu_work_exp_tv.setText(info);
                        caipu_work_exp_tv.setTextColor(Color.parseColor("#333333"));
                    }
                }
            });
        }else caipu_work_exp_tv.setVisibility(View.GONE);

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
    private void setTextViewColor(TextView view, int start,int end) {
        SpannableStringBuilder builder = new SpannableStringBuilder(view.getText().toString());
        String color = Tools.getColorStr(context,R.color.comment_color);
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
