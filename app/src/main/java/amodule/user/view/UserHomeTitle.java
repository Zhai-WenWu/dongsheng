package amodule.user.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.TextViewLimitLine;
import amodule.user.activity.FansAndFollwers;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.activity.login.UserSetting;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.imageselector.ShowImageActivity;
import third.share.activity.ShareActivityDialog;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

/**
 * Created by XiangHa on 2016/9/19.
 */
public class UserHomeTitle {

    private TextView tv_follow;
    public TextViewLimitLine friend_info;
    private ImageView userImg, friend_lv,friend_vip, iv_userType, bigImg,friend_report;
    private Map<String, String> userinfo_map;

    private View mParentTitleView;
    private BaseActivity mAct;
    private String mUserCode;

    private boolean isMyself = false;
    private String tongjiId = "a_user";

    public UserHomeTitle(BaseActivity act, View titleView, String userCode){
        mAct = act;
        mParentTitleView = titleView;
        mUserCode = userCode;

        userImg = (ImageView) mParentTitleView.findViewById(R.id.a_user_home_title_icon);
        iv_userType = (ImageView) mParentTitleView.findViewById(R.id.a_user_home_title_userType);
        bigImg = (ImageView) mParentTitleView.findViewById(R.id.a_user_home_title_img_bg);
        friend_lv = (ImageView) mParentTitleView.findViewById(R.id.i_user_lv);
        friend_vip = (ImageView) mParentTitleView.findViewById(R.id.i_user_vip);
        friend_report = (ImageView) mParentTitleView.findViewById(R.id.a_user_home_title_reprot);
        tv_follow = (TextView) mParentTitleView.findViewById(R.id.a_user_home_title_follow);
        friend_report.setClickable(true);
        friend_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mAct, ShareActivityDialog.class);
                intent.putExtra("tongjiId", isMyself ? "a_my":"a_user");
                intent.putExtra("nickName", userinfo_map.get("nickName"));
                intent.putExtra("imgUrl", userinfo_map.get("img"));
                intent.putExtra("code", userinfo_map.get("code"));
                intent.putExtra("clickUrl", "http://m.xiangha.com/i/" + userinfo_map.get("code"));
                intent.putExtra("isHasReport",!isMyself); //自己的主页不现实举报
                mAct.startActivity(intent);
            }
        });
        friend_lv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(mAct, StringManager.api_getCustomerRank + "?code=" + mUserCode, true);
            }
        });
        initTitle();
    }

    private void initTitle() {
        if (Tools.isShowTitle()) {
            mAct.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int dp_200 = mAct.getResources().getDimensionPixelSize(R.dimen.dp_200);
            int height = dp_200 + Tools.getStatusBarHeight(mAct);

            RelativeLayout bar_title = (RelativeLayout) mParentTitleView.findViewById(R.id.a_user_home_title_img_layout);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            //对返回键进行处理
            ImageView friend_back = (ImageView) mParentTitleView.findViewById(R.id.a_user_home_title_back);
            RelativeLayout.LayoutParams LayoutParams_back = (RelativeLayout.LayoutParams) friend_back.getLayoutParams();
            LayoutParams_back.topMargin = Tools.getStatusBarHeight(mAct);
            friend_back.setLayoutParams(LayoutParams_back);
            //对举报进行处理
            RelativeLayout.LayoutParams LayoutParams_report = (RelativeLayout.LayoutParams) friend_report.getLayoutParams();
            LayoutParams_report.topMargin = Tools.getStatusBarHeight(mAct) - mAct.getResources().getDimensionPixelSize(R.dimen.dp_6);
            friend_report.setLayoutParams(LayoutParams_report);
        }
    }

    // 设置用户信息
    public void setUserData(Object returnObj) {
        ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
        userinfo_map = list.get(0);
        if (userinfo_map.get("code") != null) {
            if (userinfo_map.get("code").equals(LoginManager.userInfo.get("code"))) {
                isMyself = true;
                tongjiId = "a_my";
            }

            String userIcon = userinfo_map.get("img");
            // 设置用户基本信息
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
                    .load(userIcon)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                        bigImg.setImageBitmap(UtilImage.BoxBlurFilter(bitmap, 9, 9, 9));
                        userImg.setImageBitmap(UtilImage.toRoundCorner(mAct.getResources(), bitmap, 1,
                                ToolsDevice.dp2px(mAct, 500)));
                    }
                });
            friend_info = (TextViewLimitLine) mParentTitleView.findViewById(R.id.a_user_home_title_info);
            if (userinfo_map.get("info").equals("")) {
                friend_info.setVisibility(View.GONE);
            } else {
                friend_info.setVisibility(View.VISIBLE);
                friend_info.setText(getNewInfo(userinfo_map.get("info").trim()));
//                + "Facebook的新闻页是一个复杂的listview控件，如何使它获得流畅的滚动体验一直困扰我们。 首先，新闻页的每一条新闻的可见区域非常大，包含一系列的文本以及照片；其次，新闻的展现类型也很多样，除了");
            }
            TextView friend_name = (TextView) mParentTitleView.findViewById(R.id.a_user_home_title_name);
            final TextView fans = (TextView) mParentTitleView.findViewById(R.id.a_user_home_title_fans);
            TextView clickInfo = (TextView) mParentTitleView.findViewById(R.id.a_user_home_title_click);
            TextView addTime = (TextView) mParentTitleView.findViewById(R.id.a_user_home_title_addTime);

            boolean hasLv = AppCommon.setLvImage(Integer.valueOf(userinfo_map.get("lv")), friend_lv);
            boolean isVip = AppCommon.setVip(mAct,friend_vip,userinfo_map.get("vip"),tongjiId,"个人信息", AppCommon.VipFrom.FRIEND_HOME);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) friend_name.getLayoutParams();
            if(hasLv && isVip){
                layoutParams.setMargins(0,0,Tools.getDimen(mAct,R.dimen.dp_50),0);
            }else if(hasLv || isVip){
                layoutParams.setMargins(0,0,Tools.getDimen(mAct,R.dimen.dp_25),0);
            }
            friend_name.setLayoutParams(layoutParams);

            if (userinfo_map.get("customerTagoName") != null) {
                int gourmet = Integer.valueOf(userinfo_map.get("isGourmet"));
                AppCommon.setUserTypeImage(gourmet, iv_userType);
                mParentTitleView.findViewById(R.id.a_user_home_title_qualification).setVisibility(gourmet == 1 ? View.GONE : View.VISIBLE);
                if (gourmet != 1) {
                    TextView gourmetName = (TextView) mParentTitleView.findViewById(R.id.a_user_home_title_qualification);
                    gourmetName.setText("香哈认证：" + userinfo_map.get("customerTagoName"));
                }
            }
            String userName = userinfo_map.get("nickName");
            friend_name.setText(userName);
            StringBuffer addTimeBuffer = new StringBuffer();
            if("2".equals(userinfo_map.get("sex"))){
                addTimeBuffer.append("男 ");
            }else if("3".equals(userinfo_map.get("sex"))){
                addTimeBuffer.append("女 ");
            }
            addTimeBuffer.append(userinfo_map.get("inTime"));
            addTimeBuffer.append("加入");
            addTime.setText(addTimeBuffer.toString());

            fans.setText(userinfo_map.get("fanNum") + "粉丝・");
            StringBuffer buffer = new StringBuffer();
            buffer.append(userinfo_map.get("likeNum"));
            buffer.append("赞・");
            buffer.append(userinfo_map.get("popNum"));
            buffer.append("人气");
            clickInfo.setText(buffer.toString());
//            if (!mUserCode.equals("0") && !mUserCode.equals(LoginManager.userInfo.get("code"))) {
            // 他人主页则加上图片放大功能
            mParentTitleView.findViewById(R.id.a_user_home_title_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(mAct, tongjiId, "个人信息", "头像");
                    Intent intent = new Intent();
                    intent.putExtra("url", userinfo_map.get("bigImg"));
                    intent.setClass(mAct, ShowImageActivity.class);
                    mAct.startActivity(intent);
                }
            });
//            }
            fans.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(mAct, tongjiId, "个人信息", "粉丝");
                    Intent fansIntent = new Intent(mAct,FansAndFollwers.class);
                    fansIntent.putExtra("page","0");
                    fansIntent.putExtra("code",userinfo_map.get("code"));
                    mAct.startActivity(fansIntent);
                }
            });
            changeFollow(userinfo_map.get("folState"));
            if (userinfo_map.get("folState").equals("1")) //1-自己页面，2-没有关注，3-已关注
                tv_follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(mAct, tongjiId, "个人信息", "设置");
                        Intent intent = new Intent(mAct, UserSetting.class);
                        mAct.startActivity(intent);
                    }
                });
            else
                tv_follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!LoginManager.isLogin()) {
                            Intent intent = new Intent(mAct, LoginByAccout.class);
                            mAct.startActivity(intent);
                            return;
                        }
                        AppCommon.onAttentionClick(userinfo_map.get("code"), "follow");
                        XHClick.mapStat(mAct, tongjiId, "个人信息", "关注");
                        // 没有关注，关注成功
                        if (userinfo_map.get("folState").equals("2")) {
                            userinfo_map.put("folState", "3");
                        }
                        // 已关注，取消关注
                        else if (userinfo_map.get("folState").equals("3"))
                            userinfo_map.put("folState", "2");
                        changeFollow(userinfo_map.get("folState"));
                    }
                });
        }
    }

    public static final String INTERVAL_CHAR = "\n";
    private String getNewInfo(String data) {
        if (TextUtils.isEmpty(data) || data.replace(" ", "").length() == 0)
            return "";
        String newData = "";
        if (data.indexOf(INTERVAL_CHAR) < 0) {
            return data;
        }
        String[] strs = data.split(INTERVAL_CHAR);
        for (String str : strs) {
            if (!TextUtils.isEmpty(str.trim())) {
                newData += (str + INTERVAL_CHAR);
            }
        }
        int lastIndex = newData.lastIndexOf(INTERVAL_CHAR);
        newData = newData.substring(0, lastIndex);
        return newData;
    }

    private void changeFollow(String folState) {
        switch (Integer.parseInt(folState)) {
            case 1:
                tv_follow.setText("编辑资料");
                tv_follow.setTextColor(Color.parseColor("#FFFFFF"));
                tv_follow.setBackgroundResource(R.drawable.user_home_btn_folow);
                break;
            case 2:
                tv_follow.setText("关注");

                tv_follow.setTextColor(Color.parseColor("#FFFFFF"));
                tv_follow.setBackgroundResource(R.drawable.user_home_btn_folow);
                break;
            case 3:
                tv_follow.setText("已关注");
                tv_follow.setTextColor(Color.parseColor("#bebebe"));
                tv_follow.setBackgroundResource(R.drawable.user_home_btn_folow_gray);
                break;
        }
        Intent intent= new Intent();
        intent.putExtra("folState",folState);
        mAct.setResult(100,intent);
        tv_follow.setVisibility(View.VISIBLE);

//粉丝关注跳转
//        if (tabIndex != 0 && tabIndex != 1) {
//            Intent intent = new Intent(FriendHome.this, FansAndFollwers.class);
//            intent.putExtra("code", userinfo_map.get("code"));
//            if (tabIndex == 2) {
//                intent.putExtra("page", "1");
//            } else if (tabIndex == 3) {
//                intent.putExtra("page", "0");
//            }
//            startActivity(intent);
//            return;
//        }
    }

    public void notifyAttentionInfo() {
        if (userinfo_map != null && userinfo_map.containsKey("folState")) {
            userinfo_map.put("folState", "3");
            changeFollow("3");
        }
    }
}
