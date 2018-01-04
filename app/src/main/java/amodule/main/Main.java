package amodule.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.popdialog.util.GoodCommentManager;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.AllPopDialogHelper;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import acore.widget.XiangHaTabHost;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.dish.tools.UploadDishControl;
import amodule.main.Tools.MainInitDataControl;
import amodule.main.Tools.WelcomeControls;
import amodule.main.activity.MainCircle;
import amodule.main.activity.MainHomePage;
import amodule.main.activity.MainMyself;
import amodule.main.view.WelcomeDialog;
import amodule.user.activity.MyMessage;
import aplug.shortvideo.ShortVideoInit;
import third.ad.control.AdControlHomeDish;
import third.ad.tools.AdConfigTools;
import third.cling.control.ClingPresenter;
import third.mall.alipay.MallPayActivity;
import third.push.xg.XGLocalPushServer;
import third.qiyu.QiYvHelper;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;

import static acore.tools.Tools.getApiSurTime;
import static com.xiangha.R.id.iv_itemIsFine;

@SuppressWarnings("deprecation")
public class Main extends Activity implements OnClickListener, IObserver {
    public static final String TAG="xianghaTag";

    private String[] tabTitle = {"学做菜", "社区", "消息", "我的"};
    private Class<?>[] classes = new Class<?>[]{MainHomePage.class, MainCircle.class, MyMessage.class, MainMyself.class};
    private int[] tabImgs = new int[]{R.drawable.tab_index, R.drawable.tab_circle, R.drawable.tab_four, R.drawable.tab_myself};
    public static final int TAB_HOME = 0;
    public static final int TAB_CIRCLE = 1;
    public static final int TAB_MESSAGE = 2;
    public static final int TAB_SELF = 3;

    @SuppressLint("StaticFieldLeak")
    public static Main allMain;
    @SuppressLint("StaticFieldLeak")
    public static MainBaseActivity mainActivity;

    public static Timer timer;
    //
    /**
     * 页面关闭层级
     * 把层级>=close_level的层级关闭
     * */
    public static int colse_level = 1000;

    public Map<String, MainBaseActivity> allTab = new HashMap<>();

    private View[] tabViews;
    private XiangHaTabHost tabHost;
    private LinearLayout linear_item;
    private LocalActivityManager mLocalActivityManager;

    private int doExit = 0;
    private int defaultTab = 0;
    private String url = null;
    private final int everyReq = 4 * 60;

    private boolean WelcomeDialogstate = false;//false表示当前无显示,true已经显示
    private boolean mainOnResumeState = false;//false 无焦点，true 获取焦点
    private MainInitDataControl mainInitDataControl;

    private long homebackTime;
    private boolean isForeground = true;
    private int nowTab = 0;//当前选中tab
    public static boolean isShowWelcomeDialog = false;//是否welcomedialog在展示，false未展示，true正常展示,static 避免部分手机不进行初始化和回收
    //是否已经进行初始化
    private boolean isInit=false;
    private WelcomeDialog welcomeDialog;//dialog,显示
    private QiYvHelper.UnreadCountChangeListener mUnreadCountListener;
    private WelcomeControls welcomeControls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main.this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
        setContentView(R.layout.xh_main);
        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        LogManager.printStartTime("zhangyujian","main::oncreate::start::");
        //腾讯统计

        allMain = this;
        init();

    }
    private void init(){

        mainInitDataControl = new MainInitDataControl();
        welcomeControls= LoginManager.isShowAd()?new WelcomeControls(this,callBack):
                new WelcomeControls(this,1,callBack);
        LogManager.printStartTime("zhangyujian","main::oncreate::");
        ClingPresenter.getInstance().onCreate(this, null);
    }

    private WelcomeControls.WelcomeCallBack callBack = new WelcomeControls.WelcomeCallBack() {
        @Override
        public void welcomeShowState(boolean isShow) {
            if (!isShow) {//展示后关闭
                Log.i("zhangyujian", "________________________________________________________");
                if (mainInitDataControl != null) {
                    mainInitDataControl.initMainOnResume(Main.this);
                    mainInitDataControl.iniMainAfter(Main.this);
                }
                WelcomeDialogstate = true;
                openUri();
                new AllPopDialogHelper(Main.this).start();
                com.popdialog.util.PushManager.tongjiPush(Main.this, isEnable ->
                        XHClick.mapStat(XHApplication.in(),"a_push_user",isEnable ? "开启推送" : "关闭推送","")
                );
                isShowWelcomeDialog = false;

                OffDishToFavoriteControl.addCollection(Main.this);
                //初始化电商页面统计
                PageStatisticsUtils.getInstance().getPageInfo(getApplicationContext());
                //处理
                if (LoginManager.isLogin()) {
                    initQiYvUnreadCount();
                    //防止七鱼回调有问题
                    Main.setNewMsgNum(2, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + AppCommon.qiyvMessage);
                }
                addQiYvListener();
                if(mainInitDataControl!=null)mainInitDataControl.mainAfterUpload(Main.this);
            }
        }

        @Override
        public void welcomeFree() {
            AdControlHomeDish.getInstance();
            initThrid();
            initMTA();
            initOther();
            initRunTime();
            mainInitDataControl.initWelcomeOncreate();
            mainInitDataControl.initWelcomeAfter(Main.this);
        }
    };
    /** 处理一下非明确功能的逻辑 */
    private void initOther(){
        String[] times = FileManager.getSharedPreference(XHApplication.in(), FileManager.xmlKey_appKillTime);
        if (times != null && times.length > 1 && !TextUtils.isEmpty(times[1])) {
            Tools.getApiSurTime("killback", Long.parseLong(times[1]), System.currentTimeMillis());
        }
    }

    /**
     * 初始化七鱼未读消息数
     */
    private void initQiYvUnreadCount() {
        QiYvHelper.getInstance().getUnreadCount(count -> {
            if (count >= 0) {
                if (nowTab == TAB_MESSAGE)
                    AppCommon.quanMessage = 0;
                AppCommon.qiyvMessage = count;
                if (count > 0)
                    Main.setNewMsgNum(2, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + AppCommon.qiyvMessage);
            }
        });
    }

    /**
     * 设置七鱼未读消息监听
     */
    private void addQiYvListener() {
        QiYvHelper.getInstance().addOnUrlItemClickListener((context, url) -> {
            if(TextUtils.isEmpty(url)) return;
            if (url.contains("m.ds.xiangha.com")
                    && url.contains("product_code=")) {//商品详情链接
                String[] strs = url.split("\\?");
                if (strs != null
                        && strs.length > 1
                        && !TextUtils.isEmpty(strs[1])) {
                    AppCommon.openUrl(Main.this, "xhds.product.info.app?" + strs[1], true);
                }
            } else {
                AppCommon.openUrl(Main.this, "xiangha://welcome?showWeb.app?url=" + Uri.encode(url), true);
            }
        });
        if (mUnreadCountListener == null) {
            mUnreadCountListener = count -> {
                if (count >= 0) {
                    if (nowTab == TAB_MESSAGE)
                        AppCommon.quanMessage = 0;
                    AppCommon.qiyvMessage = count;
                    if (count > 0)
                        Main.setNewMsgNum(2, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + AppCommon.qiyvMessage);
                }
            };
        }
        QiYvHelper.getInstance().addUnreadCountChangeListener(mUnreadCountListener, true);
    }

    /**腾讯统计*/
    private void initMTA(){
        StatConfig.setDebugEnable(false);
        StatConfig.setInstallChannel(this, ChannelUtil.getChannel(this));
        StatConfig.setSendPeriodMinutes(1);//设置发送策略：每一分钟发送一次
        StatService.setContext(this.getApplication());
    }


    /**
     * 外部传递参数
     */
    private void openFromOther() {
        // 获取外部参数;
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            defaultTab = bundle.getInt("tab");
            if ("notify".equals(bundle.getString("from"))) {
                url = bundle.getString("url");
                this.getIntent().removeExtra("url");
                this.getIntent().removeExtra("from");
                return;
            }
        }
        //外部知道吊起app
        if (this.getIntent().getData() != null) {
            url = this.getIntent().getData().toString();
            this.getIntent().setData(null);
        }
    }

    /**
     * 初始化第三方控件
     */
    private void initThrid() {
        //初始化短视频拍摄
        //从Welcome方法
        ShortVideoInit.init(Main.this);
        //从Welcome方法
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_LOGIN);
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_LOGOUT);
    }

    /**
     * 初始化布局
     */
    @SuppressLint("HandlerLeak")
    private void initUI() {
        String colors = Tools.getColorStr(Main.this, R.color.common_top_bg);
        Tools.setStatusBarColor(Main.this, Color.parseColor(colors));

        tabHost = (XiangHaTabHost) findViewById(R.id.xiangha_tabhost);
        tabHost.setup(mLocalActivityManager);
        linear_item = (LinearLayout) findViewById(R.id.linear_item);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        tabViews = new View[classes.length];
        for (int i = 0; i < tabTitle.length; i++) {
            tabViews[i] = linear_item.getChildAt(i);
            ConstraintLayout layout = (ConstraintLayout) tabViews[i].findViewById(R.id.tab_layout);
            layout.setOnClickListener(this);

            TextView tv = ((TextView) tabViews[i].findViewById(R.id.textView1));
            tv.setText(tabTitle[i]);

            ImageView imgView = (ImageView) tabViews[i].findViewById(iv_itemIsFine);
            imgView.setImageResource(tabImgs[i]);

            if (url != null && i == 0) {
                Intent homePage = new Intent(this, classes[i]);
                homePage.putExtra("url", url);
                tabHost.addContent(i + "", homePage);
                this.getIntent().removeExtra("url");
            } else {
                tabHost.addContent(i + "", new Intent(this, classes[i]));
            }
        }
        //处理布局margin
//        int margin = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_5) * 2
//                - Tools.getDimen(this, R.dimen.dp_70) * 5) / 4 / 2;
//        int length = linear_item.getChildCount();
//        for (int i = 0; i < length; i++) {
//            setTabItemMargins(linear_item, i, margin, margin);
//        }
//        setTabItemMargins(linear_item, 0, 0, margin);
//        setTabItemMargins(linear_item, length - 1, margin, 0);
    }

    // 时刻取得导航提醒
    private void initRunTime() {
        timer = new Timer();
        final Handler handler = new Handler();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> AppCommon.getCommonData(null));
            }
        };
        timer.schedule(tt, everyReq*1000, everyReq*1000);
//        tempData();
//        tempThreadData();
//        getMainLooper().getThread().setPriority(10);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.printStartTime("zhangyujian","main::onResume::");
        mainOnResumeState = true;
        mLocalActivityManager.dispatchResume();
        if (colse_level == 0) {
            System.exit(0);
        }
        if (!isForeground) {
            long newHomebackTime = System.currentTimeMillis();
            getApiSurTime("homeback", homebackTime, newHomebackTime);
        }
        isForeground = true;
        //去我的页面
        if (MallPayActivity.pay_state) {
            onClick(tabViews[classes.length - 1].findViewById(R.id.tab_layout));
        }
        //去商城页面
//        if (MallPayActivity.mall_state) {
//            onClick(tabViews[1].findViewById(R.id.tab_linearLayout));
//        }
        GoodCommentManager.setStictis(Main.this, (typeStr, timeStr) ->
                XHClick.mapStat(Main.this, "a_evaluate420", typeStr, timeStr)
        );
        openUri();
        if(timer==null){
            initRunTime();
        }
    }

    /**
     * 外部吊起app
     */
    private void openUri() {
        if (mainOnResumeState && WelcomeDialogstate) {
            openFromOther();
            //外部开启页面
            if (!TextUtils.isEmpty(url)) {
                AppCommon.openUrl(this, url, true);
                url = null;
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mainOnResumeState = false;
        try {
            mLocalActivityManager.dispatchPause(isFinishing());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (!Tools.isAppOnForeground()) {
            isForeground = false;
            homebackTime = System.currentTimeMillis();
        }
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void finish() {
        FileManager.setSharedPreference(XHApplication.in(), FileManager.xmlKey_appKillTime, String.valueOf(System.currentTimeMillis()));
        super.finish();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // 设置切换动画，从下边进入，上边退出
        overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /*try catch 住 super方法，尝试解决 IllegalStateException 异常*/
        try{
            outState.putString("currentTab",""+defaultTab);
            super.onSaveInstanceState(outState);
        }catch (Exception ignored){}
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
//		super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mainActivity.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        doExit(mainActivity, true);
    }

    /**
     * 准备退出 goBack为true代表只退到首页
     *
     * @param act
     * @param goBack
     */
    public void doExit(Activity act, boolean goBack) {
        // 如果是返回键则退出到首页
        AppCommon.clearCache();
        // 退出的弹框
        if (tabHost.getCurrentTab() == 0 || !goBack) {
            if (doExit < 1) {
                doExit++;
                Tools.showToast(this, "再点击一次退出应用");
                new Handler().postDelayed(() -> doExit = 0, 1000 * 5);
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                colse_level = 0;
                //请求广告位
                AdConfigTools.getInstance().getAdConfigInfo();
                UploadDishControl.getInstance().updataAllUploadingDish(getApplicationContext());
                try {
                    // 开启自我唤醒
                    if (act != null) new XGLocalPushServer(act).initLocalPush();
                } catch (Exception e) {
                }
                // 关闭时发送页面停留时间统计
                if (act != null) XHClick.finishToSendPath(act);
                // 关闭页面停留时间统计计时器
                XHClick.closeHandler();
                VersionOp.getInstance().onDesotry();
                System.exit(0);
                UtilFile.saveShared(this, FileManager.MALL_STAT, FileManager.MALL_STAT, "");
            }
        } else {
            // 先设置MainIndex界面的搜索列表不显示
            setCurrentTabByIndex(0);
        }
    }

    public void setCurrentTabByClass(Class<?> cls) {
        for (int index = 0; index < classes.length; index++) {
            if (classes[index].equals(cls)) {
                setCurrentTabByIndex(index);
                break;
            }
        }
    }

    private void setCurrentTabByIndex(int index) {
        if (index > -1 && index < 5) {
            tabHost.setCurrentTab(index);
            setCurrentText(index);
            nowTab = index;
        }
    }

    /**
     * 处理页面切换按钮图片文字的变化
     *
     * @param index
     */
    public void setCurrentText(int index) {
        for (int j = 0; j < tabViews.length; j++) {
            if (j == index) {

                ((TextView) tabViews[j].findViewById(R.id.textView1)).setTextColor(Color.parseColor("#333333"));
                tabViews[j].findViewById(iv_itemIsFine).setSelected(true);
                tabViews[j].findViewById(iv_itemIsFine).setPressed(false);
            } else {
                TextView textView = (TextView) tabViews[j].findViewById(R.id.textView1);
                textView.setTextColor(Color.parseColor("#1b1b1f"));
                if (j == 1) textView.setText(tabTitle[j]);
                tabViews[j].findViewById(iv_itemIsFine).setSelected(false);
                tabViews[j].findViewById(iv_itemIsFine).setPressed(false);
            }
        }
        if (nowTab == 0 && index != 0) {//当前是首页，切换到其他页面
            if (allTab.containsKey(MainHomePage.KEY)) {
                XHClick.newHomeStatictis(true, null);
            }
        } else if (nowTab != 0 && index == 0) {//当前是其他页面，切换到首页
            if (allTab.containsKey(MainHomePage.KEY)) {
//                MainHomePage mainIndex = (MainHomePage) allTab.get(MainHomePage.KEY);
//                mainIndex.onResumeFake();
            }
        }
        if (index == TAB_MESSAGE) {
            AppCommon.quanMessage = 0;
            setNewMsgNum(index, AppCommon.qiyvMessage + AppCommon.myQAMessage + AppCommon.feekbackMessage);
        }
        //特殊逻辑
//        changeSendLayout.setVisibility(View.VISIBLE);
        if(index == 0){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public int getCurrentTab() {
        if (tabHost != null) {
            return tabHost.getCurrentTab();
        } else {
            return -1;
        }
    }

    /**
     * 解决外部吊起时，应用已经在后台运行，不跳转问题
     */
    public void doExitMain() {
        if (mainActivity != null)
            mainActivity.finish();
    }

    /**
     * 设置消息数
     */
    public static void setNewMsgNum(int tabIndex, int messageNum) {
        if (Main.allMain == null)
            return;
//		MainCircle.notifyMessageCount();
        int currentTab = Main.allMain.getCurrentTab();
        // 更新系统消息数,
        View view = Main.allMain.getTabView(tabIndex);
        if (view != null && currentTab != -1) {
            if (messageNum > 0) {
                TextView textMsgNum;
                String messageNumStr = messageNum + "";
                if (messageNum < 10) {
                    textMsgNum = ((TextView) view.findViewById(R.id.tv_tab_msg_num));
                    view.findViewById(R.id.tv_tab_msg_tow_num).setVisibility(View.GONE);
                } else {
                    textMsgNum = ((TextView) view.findViewById(R.id.tv_tab_msg_tow_num));
                    view.findViewById(R.id.tv_tab_msg_num).setVisibility(View.GONE);
                }
                UtilLog.print("d", "------------有新消息啦: " + messageNum);
                textMsgNum.setVisibility(View.VISIBLE);
                if (messageNum > 99) {
                    messageNumStr = 99 + "+";
                }
                textMsgNum.setText(messageNumStr);
            } else {
                view.findViewById(R.id.tv_tab_msg_num).setVisibility(View.GONE);
                view.findViewById(R.id.tv_tab_msg_tow_num).setVisibility(View.GONE);
            }
        }
        dispatchUpdateMsgNum();
    }

    private static void dispatchUpdateMsgNum() {
        dispatchMsgListNum();
    }

    private static void dispatchMsgListNum() {
        MyMessage.notifiMessage(MyMessage.MSG_DISPATCH_ONREFURESH, 0, "");
    }
    /**
     * 点击下方tab切换,并且加上美食圈点击后进去第一个页面并刷新
     */
    @Override
    public void onClick(View v) {
        for (int i = 0; i < tabViews.length; i++) {
            if (v == tabViews[i].findViewById(R.id.tab_layout) && allTab.size() > 0) {
                if (i == TAB_HOME && allTab.containsKey(MainHomePage.KEY) && i == nowTab) {
                    MainHomePage mainIndex = (MainHomePage) allTab.get(MainHomePage.KEY);
                    mainIndex.refresh();
                } else if (i == TAB_CIRCLE && allTab.containsKey(MainCircle.KEY) && tabHost.getCurrentTab() == i) {
                    //当所在页面正式你要刷新的页面,就直接刷新
                    MainCircle circle = (MainCircle) allTab.get(MainCircle.KEY);
                    if(circle != null)
                        circle.refresh();
                } else if (i == TAB_SELF && allTab.containsKey(MainMyself.KEY)) {
                    //在onResume方法添加了刷新方法
//                    MainMyself mainMyself = (MainMyself) allTab.get(MainMyself.KEY);
//                    mainMyself.scrollToTop();
                } else if (i == TAB_MESSAGE && allTab.containsKey(MyMessage.KEY) && i == nowTab) {
//                    MyMessage myMessage = (MyMessage) allTab.get(MyMessage.KEY);
//                    myMessage.onRefresh();
                }
                try {
                    setCurrentTabByIndex(i);
                } catch (Exception e) {
                    UtilLog.reportError("", e);
                }
                XHClick.mapStat(Main.this, "a_index530", "底部导航栏", "点击" + tabTitle[i]);
                XHClick.mapStat(Main.this, "a_down420", tabTitle[i] + "", "");
            }
        }
    }

    public static void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public View getTabView(int index) {
        if (tabViews != null
                && tabViews.length == 4
                && index < 4
                && index > -1) {
            return tabViews[index];
        }
        return null;
    }

    public LocalActivityManager getLocalActivityManager() {
        return mLocalActivityManager;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            isInit = true;
            if(welcomeControls!=null)welcomeControls.startShow();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    initUI();
                    initData();
                    setCurrentTabByIndex(defaultTab);
                }
            },1000);

        }
        //此处可以进行分级处理:暂时无需要
        LogManager.printStartTime("zhangyujian", "main::onWindowFocusChanged");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i("zhangyujian", "main::onPostCreate");
    }

    @Override
    protected void onDestroy() {
        //activity关闭之前必须关闭dilaog
        if(welcomeDialog!=null&&welcomeDialog.isShowing()){
            welcomeDialog.dismiss();
        }
        super.onDestroy();
        ObserverManager.getInstence().unRegisterObserver(this);
        mUnreadCountListener = null;
        if (!LoginManager.isLogin())
            QiYvHelper.getInstance().destroyQiYvHelper();
        ClingPresenter.getInstance().onDestroy(this);
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (ObserverManager.NOTIFY_LOGIN.equals(name)) {
            if (data != null && data instanceof Boolean && (Boolean)data) {
                addQiYvListener();
                if (nowTab == TAB_MESSAGE || allTab.containsKey(MyMessage.KEY)) {
                    MyMessage myMessage = (MyMessage) allTab.get(MyMessage.KEY);
                    if(myMessage != null){
                        myMessage.onRefresh();
                    }
                }
                QiYvHelper.getInstance().onUserLogin();
                QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
                    @Override
                    public void onNumberReady(int count) {
                        if (count >= 0) {
                            if (nowTab == TAB_MESSAGE)
                                AppCommon.quanMessage = 0;
                            AppCommon.qiyvMessage = count;
                            if (count > 0)
                                Main.setNewMsgNum(2, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + AppCommon.qiyvMessage);
                        }
                    }
                });
                if (nowTab == TAB_MESSAGE)
                    AppCommon.quanMessage = 0;
                //防止七鱼回调不回来
                Main.setNewMsgNum(2, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + AppCommon.qiyvMessage);
            }
        } else if (ObserverManager.NOTIFY_LOGOUT.equals(name)) {
            if (data != null && data instanceof Boolean) {
                if ((Boolean)data) {
                    if (nowTab == TAB_MESSAGE || allTab.containsKey(MyMessage.KEY)) {
                        MyMessage myMessage = (MyMessage) allTab.get(MyMessage.KEY);
                        myMessage.onRefresh();
                    }
                    QiYvHelper.getInstance().onUserLogout();
                }
            }
        }
    }
}
