package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.Tools;
import amodule.quan.adapter.AdapterCircle;
import amodule.user.activity.FriendHome;
import xh.basic.tool.UtilString;

import static com.xiangha.R.id.follow_rela;

/**
 * 推荐好友
 *
 * @author Administrator
 */
public class RecommendFriendView extends CircleItemBaseLinearLayout {
    private Context context;
    private List<Map<String, String>> recCutomerArray = new ArrayList<Map<String,String>>();
    private LinearLayout recommend_friends_linear;
    private Animation animation;
    private ArrayList<Map<String, String>> recommendList;
    private RecommendCutomerCallBack callBack;
    private boolean isCallback = false;//是否回调
    private Map<String,String> mapCustomers;
    private String staticID ="";

    public RecommendFriendView(Context context) {
        super(context);
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.circle_recommend, this, true);
        recommend_friends_linear = (LinearLayout) findViewById(R.id.recommend_friends_linear);
        setRecommendCutomerCallBack(null);
    }

    public void setRecommendCutomerCallBack(RecommendCutomerCallBack callBack) {
        if (callBack == null) {
            this.callBack = new RecommendCutomerCallBack() {
                @Override
                public void noUseData() {
                    Tools.showToast(context, "回调");
                }
            };
        } else this.callBack = callBack;
    }

    /**
     * 初始化数据
     *
     * @param recommendMap
     */
    public void initView(Map<String, String> recommendMap) {
        this.mapCustomers= recommendMap;
        if (recommendList == null || recommendList.size() <= 0) {
            recommendList = UtilString.getListMapByJson(recommendMap.get("customers"));
        }
        final int childCount = getChildCount();
        for (int index = childCount - 1; index > 1; index--) {
            removeViewAt(index);
        }
        for (int i = 0; i < recommendList.size(); i++) {
            Map<String, String> map = recommendList.get(i);
            RelativeLayout layout = new RelativeLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            layout.setTag(String.valueOf(i));
            recommend_friends_linear.addView(layout);
            ((RelativeLayout) recommend_friends_linear.getChildAt(i)).addView(getView(map, i, false));
        }
        findViewById(R.id.recommendFriend_more).setOnClickListener(onClickListener);
        findViewById(R.id.change_img).setOnClickListener(onClickListener);
        findViewById(R.id.change_tv).setOnClickListener(onClickListener);

    }

    /**
     * 修改关注状态
     */
    private void setFollowState(View view, Map<String, String> cursterMap) {
        ImageView follow_img = (ImageView) view.findViewById(R.id.follow_img);
        TextView follow_tv = (TextView) view.findViewById(R.id.follow_tv);
        if (cursterMap.containsKey("folState") && "2".equals(cursterMap.get("folState"))) {//未关注
            view.findViewById(follow_rela).setVisibility(View.VISIBLE);
            int dp_10 = Tools.getDimen(context,R.dimen.dp_10);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(dp_10,dp_10);
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            follow_img.setLayoutParams(layoutParams);
            follow_img.setBackgroundResource(R.drawable.dish_follow_a);
            view.findViewById(follow_rela).setBackgroundResource(R.drawable.bg_circle_follow_5);
            follow_tv.setText("关注");
            String color = Tools.getColorStr(context,R.color.comment_color);
            follow_tv.setTextColor(Color.parseColor(color));
        } else if (cursterMap.containsKey("folState") && "3".equals(cursterMap.get("folState"))) {//已关注
            view.findViewById(follow_rela).setVisibility(View.VISIBLE);
            int dp_12 = Tools.getDimen(context,R.dimen.dp_12);
            int dp_9 = Tools.getDimen(context,R.dimen.dp_9);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(dp_12,dp_9);
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            layoutParams.setMargins(0,Tools.getDimen(context,R.dimen.dp_1),0,0);
            follow_img.setLayoutParams(layoutParams);
            follow_img.setBackgroundResource(R.drawable.circle_follow_user_right);
            follow_tv.setText("已关注");
            follow_tv.setTextColor(Color.parseColor("#999999"));
            view.findViewById(follow_rela).setBackgroundColor(Color.parseColor("#fffffe"));
        } else {
            view.findViewById(follow_rela).setVisibility(View.GONE);
        }
    }

    private void followAddView(View view) {
        view.clearAnimation();
        if (recCutomerArray == null || recCutomerArray.size() <= mUserIndexCallback.getUserIndex()) {
//            Tools.showToast(context, "当前无数据");
            return;
        }
        final int index = Integer.parseInt(String.valueOf(view.getTag()));
        Animation animation_out = AnimationUtils.loadAnimation(context, R.anim.x_in_slide_out);
        animation_out.setFillAfter(true);
        view.startAnimation(animation_out);

        RelativeLayout layout = (RelativeLayout) recommend_friends_linear.getChildAt(index);
        ((RelativeLayout) recommend_friends_linear.getChildAt(index)).removeAllViews();
        final View view_item = getView(recCutomerArray.get(mUserIndexCallback.getUserIndex()), index, true);
        ((RelativeLayout) recommend_friends_linear.getChildAt(index)).addView(view_item);
        Animation animation_in = AnimationUtils.loadAnimation(context, R.anim.x_in_slide_in);
        animation_in.setFillAfter(true);
        animation_in.setStartOffset(100);
        view_item.startAnimation(animation_in);
        animation_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view_item.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 创建view
     *
     * @param map
     * @param position
     * @param isAdd    ---是否是动画创建view
     * @return
     */
    private View getView(Map<String, String> map, final int position, boolean isAdd) {
        final View view_item = LayoutInflater.from(context).inflate(R.layout.circle_recommend_item, null);
        ImageView iv_userImg = (ImageView) view_item.findViewById(R.id.auther_userImg);
        ImageView iv_userType = (ImageView) view_item.findViewById(R.id.cusType);
        TextView tv_name = (TextView) view_item.findViewById(R.id.tv_name);
        TextView tizi_num = (TextView) view_item.findViewById(R.id.tizi_num);
        TextView caipu_num = (TextView) view_item.findViewById(R.id.caipu_num);

        setViewImage(iv_userImg, map.get("img"));
        setViewText(tv_name, map.get("nickName"));
        if (map.containsKey("isGourmet")) {
            AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), iv_userType);
        }
        tizi_num.setVisibility((map.containsKey("subjectNumber") && !TextUtils.isEmpty(map.get("subjectNumber"))) ? View.VISIBLE : View.GONE);
        caipu_num.setVisibility((map.containsKey("dishNumber") && !TextUtils.isEmpty(map.get("dishNumber"))) ? View.VISIBLE : View.GONE);
        if (map.containsKey("subjectNumber") && !TextUtils.isEmpty(map.get("subjectNumber"))) {
            tizi_num.setText("贴子" + map.get("subjectNumber"));
        }
        if (map.containsKey("dishNumber") && !TextUtils.isEmpty(map.get("dishNumber"))) {
            caipu_num.setText("菜谱" + map.get("dishNumber"));
        }
        if(map.containsKey("color") && !TextUtils.isEmpty(map.get("color"))){
            tv_name.setTextColor(Color.parseColor(map.get("color")));
        }else tv_name.setTextColor(Color.parseColor("#333333"));
        if (TextUtils.isEmpty(map.get("dishNumber")) && TextUtils.isEmpty(map.get("subjectNumber"))) {
            view_item.findViewById(R.id.user_about).setVisibility(View.GONE);
        }
        setFollowState(view_item, map);
        final String userCode = map.get("code");
        view_item.findViewById(follow_rela).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(recommendList.get(position).get("folState").equals("3")){
                    goFriendHome(userCode);
                }else{
                    XHClick.mapStat(getContext(),staticID,"推荐关注","关注");
                    AppCommon.onAttentionClick(recommendList.get(position).get("code"), "follow");
                    recommendList.get(position).put("folState", "3");
                    setFollowState(view_item,recommendList.get(position));
                    mapCustomers.put("customers",ArrayToJson(recommendList).toString());
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        followAddView(view_item);
                    }
                },500);

            }
        });
        //点击到用户中心

        view_item.findViewById(R.id.layout_user_root).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (userCode != null && !userCode.equals("") && !userCode.equals("0")) {
                    goFriendHome(userCode);
                    XHClick.mapStat(getContext(),staticID,"推荐关注","用户");
                }
            }
        });
        if(position==recommendList.size()-1){
            view_item.findViewById(R.id.bottom_line).setVisibility(View.GONE);
        }else{view_item.findViewById(R.id.bottom_line).setVisibility(View.VISIBLE);}
        view_item.setTag(String.valueOf(position));

        if (isAdd && recCutomerArray != null && recCutomerArray.size() > 0) {
            recommendList.remove(position);
            recommendList.add(position, recCutomerArray.get(mUserIndexCallback.getUserIndex()));
            mapCustomers.put("customers",ArrayToJson(recommendList).toString());
            mUserIndexCallback.plusUserIndex();
            if (recCutomerArray.size()-mUserIndexCallback.getUserIndex() <= 3 && !isCallback) {
                callBack.noUseData();
                isCallback = true;
            }
        }
        return view_item;
    }

    /**
     * 个人中心
     * @param userCode
     */
    private void goFriendHome(String userCode){
        //友盟统计
        XHClick.mapStat(context, "a_quan_homepage", "推荐好友", "");
        Intent intent = new Intent(context, FriendHome.class);
        Bundle bundle = new Bundle();
        bundle.putString("code", userCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
    /**
     * 设置关注备选数据
     *
     * @param recCutomerArrays
     */
    public void setRecCutomerArray(List<Map<String, String>> recCutomerArrays) {
        this.recCutomerArray=recCutomerArrays ;
        isCallback = false;
    }

    /**
     * 备选数据使用回调
     */
    public interface RecommendCutomerCallBack {
        /**
         * 剩余数量未5时调用
         */
        public void noUseData();
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            XHClick.mapStat(getContext(),staticID,"推荐关注","换一组");
            if(recCutomerArray.size()<=mUserIndexCallback.getUserIndex()){
                Tools.showToast(context,"暂无更多");
                callBack.noUseData();
                isCallback = true;
                return;
            }
            for (int i = 0; i < recommend_friends_linear.getChildCount(); i++) {
                followAddView(((RelativeLayout) recommend_friends_linear.getChildAt(i)).getChildAt(0));
            }
        }
    };
    /**
     * Array转json
     *
     * @return
     */
    private JSONArray ArrayToJson(ArrayList<Map<String, String>> listmap) {

        JSONArray jsonArray= new JSONArray();
        try {
            for (int i = 0, size = listmap.size(); i < size; i++) {
                Map<String, String> maps= listmap.get(i);
                Iterator<Map.Entry<String, String>> enty = maps.entrySet().iterator();
                JSONObject jsonObject = new JSONObject();
                while (enty.hasNext()) {
                    Map.Entry<String, String> entry = enty.next();
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
    public String getStaticID() {
        return staticID;
    }

    public void setStaticID(String staticID) {
        this.staticID = staticID;
    }

    public interface UserIndexCallback{
        public int getUserIndex();
        public void plusUserIndex();
    }
    @NonNull
    private UserIndexCallback mUserIndexCallback;
    public void setUserIndexCallback(@NonNull UserIndexCallback callback){
        this.mUserIndexCallback = callback;
    }
}