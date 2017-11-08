package amodule.main.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.login.widget.YiYuanBindDialog;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.TagTextView;
import amodule.answer.activity.QAMsgListActivity;
import amodule.dish.activity.OfflineDish;
import amodule.dish.db.DataOperate;
import amodule.main.Main;
import amodule.main.view.HintMyselfDialog;
import amodule.user.activity.BrowseHistory;
import amodule.user.activity.FansAndFollwers;
import amodule.user.activity.FriendHome;
import amodule.user.activity.MyFavorite;
import amodule.user.activity.MyFavoriteNew;
import amodule.user.activity.MyManagerInfo;
import amodule.user.activity.ScoreStore;
import amodule.user.activity.Setting;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.feedback.activity.Feedback;
import third.mall.activity.MallMyFavorableActivity;
import third.mall.activity.MyOrderActivity;
import third.mall.alipay.MallPayActivity;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;

/**
 * @Description:
 * @Title: MainMyself.java Copyright: Copyright (c) xiangha.com 2014~2017
 * @author: FangRuijiao
 * @date: 2016年11月13日 下午15:22:50
 */
public class MainMyself extends MainBaseActivity implements OnClickListener, IObserver {
    // 布局
    private RelativeLayout right_myself, userPage;
    private LinearLayout gourp1, gourp2,gourp3;
    private String[] name1 = {"我的订单"},
            name2 = {"我的会员", "当前设备已开通会员", "我的问答", "我的收藏",/*"缓存下载","浏览历史"*/},
            name3 = {"反馈帮助","设置"};
    private String[] clickTag1 = {"order"},
            clickTag2 = {"vip", "yiyuan", "qa", "favor",/*"download","hitstory"*/},
            clickTag3 = {"helpe","setting"};

    private final String tongjiId = "a_mine";

    //我的问答
    private View mQAItemView;
    private String mQAType = "1";//问答类型，我问：1（默认）  我答：2

    //权益迁移
    private View mYiYuanVIPView;

    //头部控件
    private TextView goManagerInfo,name,myself_please_login;
    private ImageView myself_iv,myself_lv,iv_userType,isVipImg;

    private TextView dishNum,subjectNum, followNum;
    private TextView moneyNum,scoreNum, couponNum;
    private TagTextView my_renzheng,my_vip;

    private TextView vipInfo,vipNewHint, qaInfo, qaNewHint;
    private ImageView vipIcon, qaIcon;

    private boolean mYiYuanDialogShowing;

    private boolean mIsOnResuming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_common_myself);
        Main.allMain.allTab.put("MainMyself",this);
        loadManager.showProgressBar();
        initUI();
        XHClick.track(this,"浏览我的页面");
        loadManager.setLoading(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getYiYuanBindState();
            }
        });
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_LOGIN, ObserverManager.NOTIFY_YIYUAN_BIND, ObserverManager.NOTIFY_PAYFINISH);
    }

    @Override
    protected void onResume() {
        Main.mainActivity = this;
        super.onResume();
        mIsOnResuming = true;
        if (LoginManager.isLogin()) {
            if (mQAItemView != null)
                mQAItemView.setVisibility(View.VISIBLE);
            // 设置加载
            loadManager.setLoading(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getData();
                }
            });
        } else
            resetData();
        loadManager.hideProgressBar();
        if (Main.allMain != null && Main.allMain.getBuoy() != null) {
            Main.allMain.getBuoy().clearAnimation();
            Main.allMain.getBuoy().hide();
            Main.allMain.getBuoy().setClosed(true);
            Main.allMain.getBuoy().setMove(true);
        }
        //去我的订单
        if(MallPayActivity.pay_state){
            onListEventCommon("order");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsOnResuming = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstence().unRegisterObserver(this);
    }

    private void getYiYuanBindState() {
        LoginManager.initYiYuanBindState(this, new Runnable() {
            @Override
            public void run() {
                Object shouldShowDialog = FileManager.loadShared(MainMyself.this, FileManager.xmlFile_appInfo, "shouldShowDialog");
                onBindStateDataReady(LoginManager.isTempVip(), "2".equals(shouldShowDialog));
            }
        });
    }

    private void onBindStateDataReady(final boolean isTempVip, final boolean showDialog) {
        mYiYuanVIPView.setVisibility(isTempVip ? View.VISIBLE : View.GONE);
        if (showDialog) {
            showYiYuanDialog(true);
        }
    }

    // 重置用户个人信息
    private void resetData() {
        goManagerInfo.setVisibility(View.GONE);
        subjectNum.setText("0");
        dishNum.setText("0");
        followNum.setText("0");
        moneyNum.setText("0");
        scoreNum.setText("0");
        couponNum.setText("0");
        vipInfo.setVisibility(View.GONE);
        my_renzheng.setVisibility(View.GONE);
        my_vip.setVisibility(View.GONE);
        myself_lv.setVisibility(View.GONE);
        myself_please_login.setVisibility(View.VISIBLE);
        right_myself.setVisibility(View.GONE);
        iv_userType.setVisibility(View.INVISIBLE);
        myself_iv.setImageResource(R.drawable.z_me_head);
        if (mQAItemView != null) {
            mQAItemView.setVisibility(LoginManager.isLogin() ? View.VISIBLE : View.GONE);
        }
        loadManager.hideProgressBar();
    }

    private void initUI() {
        goManagerInfo = (TextView) findViewById(R.id.goManagerInfo);
        goManagerInfo.setText("马甲");
        goManagerInfo.setTextColor(0xffffff);
        goManagerInfo.setVisibility(View.GONE);
        goManagerInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainMyself.this, MyManagerInfo.class);
                startActivity(it);
            }
        });

        right_myself = (RelativeLayout) findViewById(R.id.right_myself);
        iv_userType = (ImageView) findViewById(R.id.iv_userType);
        isVipImg = (ImageView) findViewById(R.id.a_user_home_title_vip);
        subjectNum = (TextView) findViewById(R.id.my_subject);
        dishNum = (TextView) findViewById(R.id.my_dish);
        followNum = (TextView) findViewById(R.id.my_flow);
        moneyNum = (TextView) findViewById(R.id.my_money);
        scoreNum = (TextView) findViewById(R.id.my_score);
        couponNum = (TextView) findViewById(R.id.my_coupon);

        gourp1 = (LinearLayout) findViewById(R.id.myself_gourp1);
        gourp2 = (LinearLayout) findViewById(R.id.myself_gourp2);
        gourp3 = (LinearLayout) findViewById(R.id.myself_gourp3);

        LayoutInflater layoutInfater = LayoutInflater.from(this);
        itemInfalter(layoutInfater,gourp1 , name1,clickTag1);
        itemInfalter(layoutInfater,gourp2 , name2,clickTag2);
        itemInfalter(layoutInfater,gourp3 , name3,clickTag3);

        Object isShowVip = FileManager.loadShared(this,FileManager.xmlFile_appInfo,"isShowVip");
        Object isShowMoney = FileManager.loadShared(this,FileManager.xmlFile_appInfo,"isShowMoney");
        Object isShowOpinion = FileManager.loadShared(this,FileManager.xmlFile_appInfo,"isShowOpinion");
        Object isShowQA = FileManager.loadShared(this, FileManager.xmlFile_appInfo, "isShowQA");

        if(isShowMoney == null || TextUtils.isEmpty(String.valueOf(isShowMoney))){
            findViewById(R.id.my_money_hint).setVisibility(View.VISIBLE);
        }

        qaInfo = (TextView) gourp2.getChildAt(2).findViewById(R.id.text_right_myself);
        qaNewHint = (TextView) gourp2.getChildAt(2).findViewById(R.id.my_new_info);
        qaIcon = (ImageView) gourp2.getChildAt(2).findViewById(R.id.ico_right_myself);
        if (isShowQA == null || TextUtils.isEmpty(String.valueOf(isShowQA))) {
            notifyQAItemChanged(0, true, false);
        }

        vipInfo = (TextView) gourp2.getChildAt(0).findViewById(R.id.text_right_myself);
        vipIcon = (ImageView) gourp2.getChildAt(0).findViewById(R.id.ico_right_myself);
        vipNewHint = (TextView) gourp2.getChildAt(0).findViewById(R.id.my_new_info);
        if(isShowVip == null || TextUtils.isEmpty(String.valueOf(isShowVip))){
            vipNewHint.setVisibility(View.VISIBLE);
            vipIcon.setVisibility(View.GONE);
        }

        gourp3.getChildAt(1).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
        if (TextUtils.isEmpty(String.valueOf(isShowOpinion))) {
            gourp3.getChildAt(0).findViewById(R.id.my_new_info).setVisibility(View.VISIBLE);
            gourp3.getChildAt(0).findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
        }
        TextView setting = (TextView) gourp3.getChildAt(1).findViewById(R.id.text_right_myself);
        setting.setText("版本号：" + ToolsDevice.getVerName(this));

        userPage = (RelativeLayout) findViewById(R.id.rl_userPage);
        name = (TextView) findViewById(R.id.myself_name);
        myself_lv = (ImageView) findViewById(R.id.myself_lv);
        myself_iv = (ImageView) findViewById(R.id.myself_iv);
        myself_please_login = (TextView) findViewById(R.id.myself_please_login);
        my_renzheng = (TagTextView) findViewById(R.id.my_renzheng);
        my_vip = (TagTextView) findViewById(R.id.my_vip);

        myself_please_login.setOnClickListener(this);
        userPage.setOnClickListener(this);
        myself_lv.setOnClickListener(this);
        my_renzheng.setOnClickListener(this);
        my_vip.setOnClickListener(this);
        findViewById(R.id.ll_subject).setOnClickListener(this);
        findViewById(R.id.ll_dish).setOnClickListener(this);
        findViewById(R.id.ll_flow).setOnClickListener(this);
        findViewById(R.id.ll_money).setOnClickListener(this);
        findViewById(R.id.ll_score).setOnClickListener(this);
        findViewById(R.id.ll_coupon).setOnClickListener(this);

        myself_iv.setImageResource(R.drawable.z_me_head);
        if (LoginManager.isLogin()) {
            right_myself.setVisibility(View.VISIBLE);
            myself_please_login.setVisibility(View.GONE);
        } else {
            right_myself.setVisibility(View.GONE);
            myself_please_login.setVisibility(View.VISIBLE);
        }
        showHintMyself();
    }

    /**
     *提示弹框
     */
    private void showHintMyself(){
        if(!LoginManager.isLogin()){
            return;
        }
//        int x = Integer.parseInt(DataOperate.buyBurden(this, "x"));
        Object olddishdata = FileManager.loadShared(this, "olddishdata", "olddishdata");
        if(olddishdata!=null){
            Log.i("wyl","数据：：：：：00000：：："+olddishdata);
        }

        if (olddishdata!=null&&!TextUtils.isEmpty(String.valueOf(olddishdata))&&"2".equals(String.valueOf(olddishdata))) {
            Intent intent = new Intent(this, HintMyselfDialog.class);
            startActivity(intent);
            FileManager.saveShared(this, "olddishdata", "olddishdata", "1");
        }
    }

    /**
     * 获取用户信息
     */
    private void getData() {
        String params = "type=getData&devCode=" + XGPushServer.getXGToken(getApplicationContext());
        ReqInternet.in().doPost(StringManager.api_getUserInfo, params, new InternetCallback(getApplicationContext()) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    // loadManager.showProgressBar();
                    // 解析数据
                    LoginManager.setDataUser(MainMyself.this,returnObj);
                    Map<String, String> listMap = StringManager.getListMapByJson(returnObj).get(0);
                    // 设置离线菜单MAX数量
                    if(!TextUtils.isEmpty(listMap.get("downDish")))
                        DataOperate.setDownDishLimit(MainMyself.this, Integer.valueOf(listMap.get("downDish")));
                    if(!TextUtils.isEmpty(listMap.get("nextDownDish")))
                        AppCommon.nextDownDish = Integer.parseInt(listMap.get("nextDownDish"));
                    name.setText(listMap.get("nickName"));

                    subjectNum.setText(listMap.get("subjectNum"));
                    dishNum.setText(listMap.get("upNum"));
                    followNum.setText(listMap.get("followNum"));

                    scoreNum.setText(listMap.get("scoreNum"));
                    couponNum.setText(listMap.get("coupon"));

                    String vip = listMap.get("vip");
                    if(!TextUtils.isEmpty(vip)) {
                        ArrayList<Map<String,String>> vipArray = StringManager.getListMapByJson(vip);
                        if(vipArray.size()>0) {
                            Map<String,String> vipMap = vipArray.get(0);
                            moneyNum.setText(vipMap.get("xiangdou"));
                            String vipData = vipMap.get("text");
                            if (TextUtils.isEmpty(vipData)) {
                                vipInfo.setVisibility(View.GONE);
                                if(vipNewHint.getVisibility() != View.VISIBLE) vipIcon.setVisibility(View.VISIBLE);
                            } else {
                                vipInfo.setText(vipData);
                                vipInfo.setVisibility(View.VISIBLE);
                                vipIcon.setVisibility(View.GONE);
                                String vipDataColor = vipMap.get("color");
                                if(!TextUtils.isEmpty(vipDataColor)){
                                    vipInfo.setTextColor(Color.parseColor(vipDataColor));
                                }
                            }
                        }
                    }

                    AppCommon.setLvImage(Integer.valueOf(listMap.get("lv")), myself_lv);
                    boolean isVip = AppCommon.setVip(MainMyself.this,isVipImg,listMap.get("vip"),tongjiId,"头部", AppCommon.VipFrom.MY_SELF);
                    if(isVip){
                        my_vip.setText("会员续费");
                        my_vip.setSideColor(getResources().getColor(R.color.comment_color));
                        my_vip.setTextColor(getResources().getColor(R.color.comment_color));
                    }else{
                        isVipImg.setVisibility(View.VISIBLE);
                        isVipImg.setImageResource(R.drawable.i_user_home_unvip);

                        my_vip.setText("开通会员");
                        my_vip.setTextColor(getResources().getColor(R.color.common_top_text));
                        my_vip.setSideColor(getResources().getColor(R.color.common_top_text));
                    }
                    my_vip.setVisibility(View.VISIBLE);

                    if (listMap.get("isGourmet") != null){
                        boolean isGourmet = AppCommon.setUserTypeImage(Integer.valueOf(listMap.get("isGourmet")), iv_userType);
                        if(isGourmet){
                            my_renzheng.setText("已经认证");
                            my_renzheng.setSideColor(getResources().getColor(R.color.comment_color));
                            my_renzheng.setTextColor(getResources().getColor(R.color.comment_color));
                        }else{
                            my_renzheng.setText("申请认证");
                            my_renzheng.setTextColor(getResources().getColor(R.color.common_top_text));
                            my_renzheng.setSideColor(getResources().getColor(R.color.common_top_text));
                        }
                        my_renzheng.setVisibility(View.VISIBLE);
                    }
                    setUserImage(myself_iv, listMap.get("img"));

                    myself_please_login.setVisibility(View.GONE);
                    if (listMap.get("isManager").equals("3") || listMap.get("isManager").equals("2") || XHConf.log_isDebug) {
                        goManagerInfo.setVisibility(View.VISIBLE);
                    } else {
                        goManagerInfo.setVisibility(View.GONE);
                    }
                    myself_please_login.setVisibility(View.GONE);
                    right_myself.setVisibility(View.VISIBLE);
                }else if(flag == ReqInternet.REQ_CODE_ERROR){
                    LoginManager.logout(MainMyself.this);
                    resetData();
                }else {
                    if (LoginManager.userInfo.get("userCode") == null || LoginManager.userInfo.get("userCode") == "") {
                        LoginManager.logout(MainMyself.this);
                        resetData();
                    }
                    loadManager.hideProgressBar();
                }
            }

        });
    }

    private void itemInfalter(LayoutInflater layoutInfater,LinearLayout parent, String[] groupNames,String[] clickTags) {
        for (int i = 0; i < groupNames.length; i++) {
            String tag = clickTags[i];
            if ("yiyuan".equals(tag)) {
                mYiYuanVIPView = layoutInfater.inflate(R.layout.myself_viptransferitem, null);
                TextView text = (TextView) mYiYuanVIPView.findViewById(R.id.text_myself);
                text.setText(groupNames[i]);
                TextView descText = (TextView) mYiYuanVIPView.findViewById(R.id.text_desc);
                descText.setText(R.string.myself_viptransfer_itemdesc);
                TextView rightText = (TextView) mYiYuanVIPView.findViewById(R.id.text_right_myself);
                rightText.setText("权限迁移");
                mYiYuanVIPView.findViewById(R.id.ico_right_myself).setVisibility(View.GONE);
                mYiYuanVIPView.setVisibility(View.GONE);
                mYiYuanVIPView.setTag(tag);
                mYiYuanVIPView.setOnClickListener(this);
                parent.addView(mYiYuanVIPView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.dp_45)));
                continue;
            }

            View itemView =  layoutInfater.inflate(R.layout.a_common_myself_item, null);
            itemView.setClickable(true);
            itemView.setTag(tag);
            itemView.setOnClickListener(this);

            TextView text = (TextView) itemView.findViewById(R.id.text_myself);
            text.setText(groupNames[i]);
            parent.addView(itemView);
            if ("qa".equals(tag)) {
                mQAItemView = itemView;
                mQAItemView.setVisibility(View.GONE);
            }
            if (i == groupNames.length - 1) {
                itemView.findViewById(R.id.my_item_line).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            onListEventCommon(String.valueOf(v.getTag()));
        } else {
            onEventbranch(v.getId());
        }
    }

    private void onEventbranch(int index) {
        if (LoginManager.userInfo.size() == 0) {
            switch (index) {
                case R.id.ll_subject:
                case R.id.ll_dish:
                case R.id.ll_flow:
                case R.id.ll_money:
                case R.id.ll_score:
                case R.id.ll_coupon:
                case R.id.myself_lv: //用户等级
                case R.id.myself_please_login:
                case R.id.ico_right_myself:
                case R.id.myself_iv:
                    XHClick.mapStat(this, tongjiId, "头部", "登录");
                    Intent intent = new Intent(MainMyself.this, LoginByAccout.class);
                    startActivity(intent);
            }
        } else {
            LogManager.print("d", "事件点击index:" + index);
            switch (index) {
                case R.id.rl_userPage:
                    // 统计
                    XHClick.track(getApplicationContext(), "点击我的页面的头部");
                    XHClick.mapStat(this, tongjiId, "头部", "个人主页");
                    Intent intent = new Intent(MainMyself.this, FriendHome.class);
                    intent.putExtra("code", LoginManager.userInfo.get("code"));
                    startActivity(intent);
                    break;
                case R.id.my_renzheng: //认证美食家
                    XHClick.track(getApplicationContext(), "点击我的页面的头部");
                    XHClick.mapStat(this, tongjiId, "头部", "认证");
                    AppCommon.openUrl(this, "http://appweb.xiangha.com/approve/index", true);
                    break;
                case R.id.my_vip: //开通会员
                    XHClick.track(getApplicationContext(), "点击我的页面的头部");
                    XHClick.mapStat(this, tongjiId, "头部", "会员");
                    AppCommon.openUrl(this, StringManager.getVipUrl(true) + String.format("&vipFrom=我的页面%s按钮", LoginManager.isVIP() ? "会员续费" : "开通会员"), true);
                    break;
                case R.id.myself_lv: //用户等级
                    XHClick.track(getApplicationContext(), "点击我的页面的头部");
                    XHClick.mapStat(this, tongjiId, "头部", "等级");
                    AppCommon.openUrl(this, StringManager.api_getCustomerRank + "?code=" + LoginManager.userInfo.get("code"), true);
                    break;
                case R.id.ll_subject:
                    // 统计
                    XHClick.track(getApplicationContext(), "点击我的页面的贴子");
                    XHClick.mapStat(this, tongjiId, "导航", "贴子");
                    Intent intent_subject = new Intent(MainMyself.this, FriendHome.class);
                    intent_subject.putExtra("code", LoginManager.userInfo.get("code"));
                    startActivity(intent_subject);
                    break;
                case R.id.ll_dish:
                    // 统计
                    XHClick.track(getApplicationContext(), "点击我的页面的菜谱");
                    XHClick.mapStat(this, tongjiId, "导航", "菜谱");
                    Intent intent_dish = new Intent(MainMyself.this, FriendHome.class);
                    intent_dish.putExtra("code", LoginManager.userInfo.get("code"));
                    intent_dish.putExtra("index",1);
                    startActivity(intent_dish);
                    break;
                case R.id.ll_flow:
                    // 统计
                    XHClick.mapStat(this, tongjiId, "导航", "关注");
                    Intent intent_flow = new Intent(MainMyself.this, FansAndFollwers.class);
                    intent_flow.putExtra("page","2");
                    startActivity(intent_flow);
                    break;
                case R.id.ll_money:
                    XHClick.mapStat(this, tongjiId,"列表", "我的钱包");
                    AppCommon.openUrl(MainMyself.this,StringManager.api_money,true);
                    FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowMoney","2");
                    findViewById(R.id.my_money_hint).setVisibility(View.GONE);
                    break;
                case R.id.ll_score:
                    XHClick.track(getApplicationContext(), "点击我的页面的积分");
                    XHClick.mapStat(this, tongjiId,"列表", "积分商城");
//			AppCommon.openUrl(this, StringManager.api_scoreStore + "?code=" + LoginManager.userInfo.get("code"), true);
                    Intent scoreStore = new Intent(MainMyself.this, ScoreStore.class);
                    startActivity(scoreStore);
                    break;
                case R.id.ll_coupon:
                    XHClick.mapStat(this, tongjiId,"列表", "优惠券");
                    Intent intent_coupon = new Intent(this,MallMyFavorableActivity.class);
                    startActivity(intent_coupon);
                    break;
            }
        }
    }

    private void onListEventCommon(String clickTag){
        boolean isOption = false;
        if (LoginManager.userInfo.size() == 0) {
            switch (clickTag) {
                case "favor": //收藏
                case "score": //积分
                case "order": //订单
                case "coupon": //优惠券
                case "money": //钱包
                    isOption = true;
                    XHClick.mapStat(this, tongjiId, "头部", "登录");
                    Intent intent = new Intent(MainMyself.this, LoginByAccout.class);
                    startActivity(intent);
                    break;
            }
        }

        if(!isOption){
            switch (clickTag) {
                case "yiyuan"://权益迁移
                    new YiYuanBindDialog(this).show("权益迁移", getResources().getString(R.string.yiyuan_dialog_desc));
                    XHClick.mapStat(this, tongjiId, "权限迁移按钮", "");
                    break;
                case "qa"://我的问答
                    FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowQA","2");
                    notifyQAItemChanged (0, false, true);
                    startActivity(new Intent(MainMyself.this, QAMsgListActivity.class));
                    break;
                case "favor"://收藏
                    XHClick.track(getApplicationContext(), "点击我的页面的收藏");
                    XHClick.mapStat(this, tongjiId,"列表","收藏");
                    Intent intent_fav = new Intent(MainMyself.this, MyFavoriteNew.class);
                    startActivity(intent_fav);
                    break;
                case "hitstory"://浏览记录
                    XHClick.mapStat(this, tongjiId,"列表","看过");
                    Intent intent_history = new Intent(MainMyself.this, BrowseHistory.class);
                    startActivity(intent_history);
                    break;
                case "download"://离线菜谱
                    // 统计
                    XHClick.track(getApplicationContext(), "点击我的页面的下载");
                    XHClick.mapStat(this, tongjiId,"列表","下载");
                    Intent intent_off = new Intent(MainMyself.this, OfflineDish.class);
                    startActivity(intent_off);
                    break;
                case "order":
                    XHClick.mapStat(this, tongjiId,"列表", "订单");
                    Intent intent_order = new Intent(this,MyOrderActivity.class);
                    startActivity(intent_order);
                    break;
                case "vip": //会员
                    XHClick.mapStat(this, tongjiId,"列表", "我的会员");
                    AppCommon.openUrl(MainMyself.this, StringManager.getVipUrl(false) + "&vipFrom=我的页面我的会员按钮",true);
                    FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowVip","2");
                    vipNewHint.setVisibility(View.GONE);
                    if(vipInfo.getVisibility() != View.VISIBLE) vipIcon.setVisibility(View.VISIBLE);
                    break;
                case "helpe"://意见反馈
                    FileManager.saveShared(this,FileManager.xmlFile_appInfo,"isShowOpinion","2");
                    gourp3.getChildAt(0).findViewById(R.id.my_new_info).setVisibility(View.GONE);
                    gourp3.getChildAt(0).findViewById(R.id.ico_right_myself).setVisibility(View.VISIBLE);
                    Intent intent2 = new Intent(MainMyself.this, Feedback.class);
                    startActivity(intent2);
                    break;
                case "setting"://设置
                    XHClick.track(getApplicationContext(), "点击我的页面的设置");
                    XHClick.mapStat(this, tongjiId, "设置", "");
                    Intent intent_setting = new Intent(MainMyself.this, Setting.class);
                    intent_setting.putExtra("isGoManagerInfo",goManagerInfo.getVisibility() == View.VISIBLE);
                    startActivity(intent_setting);
                    break;
            }
        }
    }

    public void setUserImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        v.setScaleType(ScaleType.CENTER_CROP);
        v.setImageResource(R.drawable.bg_round_grey_e0e0e0_50);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
                .load(value)
                .setImageRound(ToolsDevice.dp2px(MainMyself.this, 500))
                .build();
        if(bitmapRequest != null)
            bitmapRequest.into(v);
    }

    private void notifyQAItemChanged (int numInfo, boolean showNewHint, boolean showIcon) {
        if (qaInfo == null || qaNewHint == null || qaIcon == null)
            return;
        qaInfo.setVisibility(numInfo > 0 ? View.VISIBLE : View.GONE);
        qaNewHint.setVisibility(showNewHint ? View.VISIBLE : View.GONE);
        qaIcon.setVisibility(showIcon ? View.VISIBLE : View.GONE);
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (name != null) {
            switch (name) {
                case ObserverManager.NOTIFY_LOGIN:
                    if (data != null && data instanceof Boolean && (Boolean)data && LoginManager.isTempVip() && !LoginManager.isAutoBindYiYuanVIP()) {
                        showYiYuanDialog(false);
                    }
                    break;
                case ObserverManager.NOTIFY_YIYUAN_BIND:
                    if (data != null && data instanceof Map) {
                        Map<String, String> state = (Map<String, String>) data;
                        if ("2".equals(state.get("state"))) {
                            mYiYuanVIPView.setVisibility(View.GONE);
                            if (mIsOnResuming)
                                getData();
                        }
                    }
                    break;
                case ObserverManager.NOTIFY_PAYFINISH:
                    if (data != null && data instanceof Boolean && (Boolean)data) {
                        getYiYuanBindState();
                    }
                    break;
            }
        }
    }

    /**
     * 显示权限迁移弹框
     * @param fromServer 是否来自服务器控制的弹框
     */
    private void showYiYuanDialog(boolean fromServer) {
        if (mYiYuanDialogShowing)
            return;
        mYiYuanDialogShowing = true;
        if (fromServer)
            FileManager.saveShared(MainMyself.this,FileManager.xmlFile_appInfo,"vipTransfer","2");
        YiYuanBindDialog bindDialog = new YiYuanBindDialog(this);
        bindDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mYiYuanDialogShowing = false;
            }
        });
        bindDialog.show(getString(R.string.yiyuan_bind_title), getString(R.string.yiyuan_dialog_desc));
    }
}