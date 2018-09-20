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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.encoder.VideoCodecs;
import com.aliyun.struct.snap.AliyunSnapVideoParam;
import com.annimon.stream.Stream;
import com.popdialog.db.FullSrceenDB;
import com.popdialog.util.GoodCommentManager;
import com.popdialog.util.PushManager;
import com.quze.videorecordlib.VideoRecorderCommon;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.MessageTipController;
import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.notification.controller.NotificationSettingController;
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
import amodule._common.conf.GlobalVariableConfig;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.UploadDishData;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.dish.tools.UploadDishControl;
import amodule.lesson.activity.LessonHome;
import amodule.main.Tools.MainInitDataControl;
import amodule.main.Tools.WelcomeControls;
import amodule.main.activity.MainCircle;
import amodule.main.activity.MainHomePage;
import amodule.main.activity.MainMyself;
import amodule.main.delegate.ISetMessageTip;
import amodule.shortvideo.activity.ShortPublishActivity;
import amodule.user.activity.login.LoginByAccout;
import aplug.shortvideo.ShortVideoInit;
import third.ad.control.AdControlHomeDish;
import third.ad.db.XHAdSqlite;
import third.ad.tools.AdConfigTools;
import third.aliyun.work.AliyunCommon;
import third.aliyun.work.EditorActivity;
import third.aliyun.work.MediaActivity;
import third.cling.control.ClingPresenter;
import third.mall.alipay.MallPayActivity;
import third.push.localpush.LocalPushDataManager;
import third.push.localpush.LocalPushManager;
import third.push.xg.XGTagManager;
import third.qiyu.QiYvHelper;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;

import static android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION;
import static com.xiangha.R.id.iv_itemIsFine;

@SuppressWarnings("deprecation")
public class Main extends Activity implements OnClickListener, IObserver, ISetMessageTip {
    public static final String TAG = "xianghaTag";

    private String[] tabTitle = {"首页", "名厨菜","发布", "社区", "我的"};
    private Class<?>[] classes = new Class<?>[]{MainHomePage.class, LessonHome.class, VideoEditActivity.class,MainCircle.class, MainMyself.class};
    private int[] tabImgs = new int[]{R.drawable.tab_index, R.drawable.tab_vip, R.drawable.tab_circle,R.drawable.tab_circle, R.drawable.tab_myself};
    public static final int TAB_HOME = 0;
    public static final int TAB_LESSON = 1;
    public static final int TAB_CIRCLE = 3;
    public static final int TAB_SELF = 4;

    @SuppressLint("StaticFieldLeak")
    public static Main allMain;
    @SuppressLint("StaticFieldLeak")
    public static MainBaseActivity mainActivity;
    /**
     * 页面关闭层级
     * 把层级>=close_level的层级关闭
     */
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
    private QiYvHelper.UnreadCountChangeListener mUnreadCountListener;
    private WelcomeControls welcomeControls;
    public static Timer timer;

    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main.this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
        setContentView(R.layout.xh_main);//耗时250毫秒
        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        //腾讯统计
        initMTA();
        allMain = this;
        init();
    }
    private void init(){
        WelcomeDialogstate=false;
        isShowWelcomeDialog=true;
        mainInitDataControl = new MainInitDataControl();
        welcomeControls= LoginManager.isShowAd()?new WelcomeControls(this,callBack):
                new WelcomeControls(this,1,callBack);
        LogManager.printStartTime("zhangyujian","main::oncreate::");
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
//                new AllPopDialogHelper(Main.this).start();
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
                    MessageTipController.newInstance().setMessageCount();
                }
                addQiYvListener();
                if(mainInitDataControl!=null)mainInitDataControl.mainAfterUpload(Main.this);
                FileManager.saveShared(Main.this,FileManager.app_welcome,VersionOp.getVerName(Main.this),"1");
            }
        }
        @Override
        public void welcomeFree() {
            LogManager.printStartTime("zhangyujian","main::welcomeFree:111:");
            initUI();
            initData();
            setCurrentTabByIndex(defaultTab);
            LogManager.printStartTime("zhangyujian","main::welcomeFree:2222:");
            AdControlHomeDish.getInstance();
            initThrid();
            initOther();
            initRunTime();
            mainInitDataControl.initWelcomeOncreate();
            mainInitDataControl.initWelcomeAfter(Main.this);
        }
    };
    /** 处理一下非明确功能的逻辑 */
    private void initOther() {
        String[] times = FileManager.getSharedPreference(XHApplication.in(), FileManager.xmlKey_appKillTime);
        if (times != null && times.length > 1 && !TextUtils.isEmpty(times[1])) {
            Tools.getApiSurTime("killback", Long.parseLong(times[1]), System.currentTimeMillis());
        }
    }
    /**
     * 初始化七鱼未读消息数
     */
    private void initQiYvUnreadCount() {
        MessageTipController.newInstance().loadQiyuUnreadCount();
    }

    /**
     * 设置七鱼未读消息监听
     */
    private void addQiYvListener() {
        QiYvHelper.getInstance().addOnUrlItemClickListener((context, url) -> {
            if (TextUtils.isEmpty(url)) return;
            if (url.contains("m.ds.xiangha.com")
                    && url.contains("product_code=")) {//商品详情链接
                String[] strs = url.split("\\?");
                if (strs.length > 1 && !TextUtils.isEmpty(strs[1])) {
                    AppCommon.openUrl(Main.this, "xhds.product.info.app?" + strs[1], true);
                }
            } else {
                AppCommon.openUrl(Main.this, "xiangha://welcome?showWeb.app?url=" + Uri.encode(url), true);
            }
        });
        if (mUnreadCountListener == null) {
            mUnreadCountListener = count -> {
                if (count >= 0) {
                    MessageTipController.newInstance().setQiyvMessage(count);
                }
            };
        }
        QiYvHelper.getInstance().addUnreadCountChangeListener(mUnreadCountListener, true);
    }

    /** 腾讯统计 */
    private void initMTA() {
        //原始：Aqc1150004142
        //VIP：A1DGHJVJ938H
        StatConfig.setAppKey(LoginManager.isVIPLocal(XHApplication.in())?"A1DGHJVJ938H":"Aqc1150004142");
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
        ObserverManager.getInstance().registerObserver(this, ObserverManager.NOTIFY_LOGIN);
        ObserverManager.getInstance().registerObserver(this, ObserverManager.NOTIFY_LOGOUT);
        ObserverManager.getInstance().registerObserver(this, ObserverManager.NOTIFY_MESSAGE_REFRESH);

    }

    /**
     * 初始化布局
     */
    @SuppressLint("HandlerLeak")
    private void initUI() {
        String colors = Tools.getColorStr(Main.this, R.color.common_top_bg);
        Tools.setStatusBarColor(Main.this, Color.parseColor(colors));

        tabHost = findViewById(R.id.xiangha_tabhost);
        tabHost.setup(mLocalActivityManager);
        linear_item = findViewById(R.id.linear_item);
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
        initAliyunVideo();
    }

    public void onChangeSend(View view){
        AliyunCommon.getInstance().startRecord(this);
    }

    Handler mTimerHandler = null;
    Runnable mRunnable = null;
    // 时刻取得导航提醒
    public void initRunTime() {
        if(mTimerHandler == null){
            mTimerHandler = new Handler();
            execute();
        }
    }

    private void execute(){
        if(mTimerHandler != null){
            if(mRunnable == null){
                mRunnable = () -> {
                    MessageTipController.newInstance().getCommonData(null);
                    execute();
                };
                mTimerHandler.post(mRunnable);
            }else{
                mTimerHandler.postDelayed(mRunnable,everyReq * 1000);
            }
        }
    }

    public void stopTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacks(mRunnable);
            mTimerHandler = null;
            mRunnable = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.printStartTime("zhangyujian", "main::onResume::");
        mainOnResumeState = true;
        mLocalActivityManager.dispatchResume();
        if (colse_level == 0) {
            System.exit(0);
        }
        if (!isForeground) {
            long newHomebackTime = System.currentTimeMillis();
            Tools.getApiSurTime("homeback", homebackTime, newHomebackTime);
        }
        isForeground = true;
        //去我的页面
        if (MallPayActivity.pay_state
                && tabViews != null
                && tabViews.length >= classes.length - 1
                && tabViews[classes.length - 1] != null) {
            onClick(tabViews[classes.length - 1].findViewById(R.id.tab_layout));
        }

        //去商城页面
//        if (MallPayActivity.mall_state) {
//            onClick(tabViews[1].findViewById(R.id.tab_linearLayout));
//        }
        GoodCommentManager.setStictis(Main.this, (typeStr, timeStr) ->
                XHClick.mapStat(Main.this, "a_evaluate420", typeStr, timeStr)
        );
        initRunTime();
        openUri();
        LogManager.printStartTime("zhangyujian","main::onResume:end::");
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
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        stopTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!Tools.isAppOnForeground()) {
            isForeground = false;
            homebackTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("tzy", "onKeyDown: "+event.getKeyCode());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        FileManager.setSharedPreference(XHApplication.in(), FileManager.xmlKey_appKillTime, String.valueOf(System.currentTimeMillis()));
        super.finish();
    }

    @Override
    public void startActivity(Intent intent) {
        intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION);
        super.startActivity(intent);
        // 设置切换动画，从下边进入，上边退出
        overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /*try catch 住 super方法，尝试解决 IllegalStateException 异常*/
        try {
            outState.putString("currentTab", "" + defaultTab);
            super.onSaveInstanceState(outState);
        } catch (Exception ignored) {
        }
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
     * 准备退出
     *
     * @param act 上下文
     * @param goBack 为true代表只退到首页
     */
    public void doExit(Activity act, boolean goBack) {
        if(!WelcomeDialogstate)return;
        // 如果是返回键则退出到首页
        AppCommon.clearCache();
        // 退出的弹框
        if (tabHost.getCurrentTab() == 0 || !goBack) {
            if (doExit < 1) {
                doExit++;
                Tools.showToast(this, "再点击一次退出应用");
                new Handler().postDelayed(() -> doExit = 0, 1000 * 5);
            } else {
                stopTimer();
                colse_level = 0;
                //请求广告位
                XHAdSqlite.newInstance(this).deleteOverdueConfig();
                AdConfigTools.getInstance().getAdConfigInfo();
                UploadDishControl.getInstance().updataAllUploadingDish(getApplicationContext());
                try {
                    // 开启自我唤醒
                    if (act != null) {
//                        new XGLocalPushServer(act).initLocalPush();

                        new LocalPushDataManager(act).initLocalPush();
                        LocalPushManager.execute(act, System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L, null, null);
                    }
                } catch (Exception e) {
                }
                // 关闭时发送页面停留时间统计
                if (act != null){
                    XHClick.finishToSendPath(act);
                    new FullSrceenDB(act).clearExpireAllData();
                }
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

                ((TextView) tabViews[j].findViewById(R.id.textView1)).setTextColor(Color.parseColor("#fa273b"));
                tabViews[j].findViewById(iv_itemIsFine).setSelected(true);
                tabViews[j].findViewById(iv_itemIsFine).setPressed(false);
            } else {
                TextView textView = (TextView) tabViews[j].findViewById(R.id.textView1);
                textView.setTextColor(Color.parseColor("#888888"));
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
        if (index == 0) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if(nowTab!=index){
            NotificationSettingController.removePermissionSetView();
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
                } else if (i == TAB_LESSON && allTab.containsKey(LessonHome.KEY) && tabHost.getCurrentTab() == i) {
                    //当所在页面正式你要刷新的页面,就直接刷新
                    LessonHome lesson = (LessonHome) allTab.get(LessonHome.KEY);
                    if (lesson != null)
                        lesson.refresh();

                } else if (i == TAB_SELF && allTab.containsKey(MainMyself.KEY)) {
                    //在onResume方法添加了刷新方法
//                    MainMyself mainMyself = (MainMyself) allTab.get(MainMyself.KEY);
//                    mainMyself.scrollToTop();
                    this.startActivity(new Intent(this,ShortPublishActivity.class));
                } else if (i == TAB_CIRCLE && allTab.containsKey(MainCircle.KEY) && i == nowTab) {
                    MainCircle circle = (MainCircle) allTab.get(MainCircle.KEY);
                    if (circle != null)
                        circle.refresh();
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
    protected void onPostResume() {
        super.onPostResume();
        LogManager.printStartTime("zhangyujian","onPostResume：：");
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogManager.printStartTime("zhangyujian","onAttachedToWindow：：");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            LogManager.printStartTime("zhangyujian","onWindowFocusChanged：1111：：");
            isInit = true;
            handlerPostDelay();
        }
        //此处可以进行分级处理:暂时无需要
        LogManager.printStartTime("zhangyujian", "main::onWindowFocusChanged:::");
    }
    private Handler handlerPostInit;
    private void handlerPostDelay(){
        if(handlerPostInit==null)handlerPostInit= new Handler(Looper.getMainLooper());
        handlerPostInit.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(welcomeControls!=null)welcomeControls.startShow();
            }
        },100);
    }
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i("zhangyujian", "main::onPostCreate");
    }

    @Override
    protected void onDestroy() {
        //activity关闭之前必须关闭dilaog
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
        mUnreadCountListener = null;
        if (!LoginManager.isLogin())
            QiYvHelper.getInstance().destroyQiYvHelper();
        ClingPresenter.getInstance().onDestroy(this);
        GlobalVariableConfig.restoreConf();
        String notifyStatistics = (String) UtilFile.loadShared(this, FileManager.notification_permission, "statistics");
        if (!TextUtils.equals(notifyStatistics, "2")) {
            UtilFile.saveShared(this, FileManager.notification_permission, "statistics", "2");
            boolean open = PushManager.isNotificationEnabled(XHApplication.in());
            XHClick.mapStat(XHApplication.in(), "a_open_push", open ? "开启了系统通知权限" : "未开启系统通知权限", "");
        }
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name) {
            case ObserverManager.NOTIFY_LOGIN:
                if (data != null && data instanceof Boolean && (Boolean) data) {
                    addQiYvListener();
                    QiYvHelper.getInstance().onUserLogin();
                    MessageTipController.newInstance().loadQiyuUnreadCount();
                    MessageTipController.newInstance().setMessageCount();

                    XGTagManager manager = new XGTagManager();
                    manager.removeXGTag(XGTagManager.APP_NEW);
                }
                break;
            case ObserverManager.NOTIFY_LOGOUT:
                if (data != null && data instanceof Boolean) {
                    if ((Boolean) data) {
                        QiYvHelper.getInstance().onUserLogout();
                        XGTagManager manager = new XGTagManager();
                        manager.addXGTag(XGTagManager.APP_NEW);
                    }
                }
                break;
            case ObserverManager.NOTIFY_MESSAGE_REFRESH:
                setMessageTip(MessageTipController.newInstance().getMessageNum());
                break;
            default:
                break;
        }
    }

    @Override
    public void setMessageTip(int tipCournt) {
//        Log.i("tzy", "MainCircle::setMessageTip: " + tipCournt);
        if (allTab != null) {
            Stream.of(allTab)
                    .filter(value -> value.getValue() != null && value.getValue() instanceof ISetMessageTip)
                    .forEach(value -> ((ISetMessageTip) value.getValue()).setMessageTip(tipCournt));
        }
    }

    private void checkShowGuidance() {
        String show = (String) FileManager.loadShared(this, FileManager.xmlKey_homeGuidanceShow, "show");
        if (TextUtils.equals("2", show))
            return;
        FileManager.saveShared(this, FileManager.xmlKey_homeGuidanceShow, "show", "2");
        View contentView = LayoutInflater.from(this).inflate(R.layout.home_guidance_layout, null);
        PopupWindow pw = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
        contentView.findViewById(R.id.guidance_img).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });
        pw.setBackgroundDrawable(null);
        pw.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }
    private void initAliyunVideo(){
        AliyunCommon.getInstance().setAliyunVideoDataCallBack(new AliyunCommon.AliyunVideoDataCallBack() {
            @Override
            public void videoCallBack(String videoPath, String imgPath, String otherData) {
                Log.i("xianghaTag","videoPath:::"+videoPath+"::::"+imgPath+"::::"+otherData);
                if(!TextUtils.isEmpty(videoPath)&&!TextUtils.isEmpty(imgPath)){
                    Intent intent = new Intent(Main.this, ShortPublishActivity.class);
                    intent.putExtra("videoPath",videoPath);
                    intent.putExtra("imgPath",imgPath);
                    intent.putExtra("otherData",otherData);
                    if(!TextUtils.isEmpty(AliyunCommon.topicCode)&&!TextUtils.isEmpty(AliyunCommon.topicName)) {
                        intent.putExtra("topicCode", AliyunCommon.topicCode);
                        intent.putExtra("topicName", AliyunCommon.topicName);
                    }
                    Main.this.startActivity(intent);
                }
            }
        });
        //选择本地
        VideoRecorderCommon.instance().setStartMediaActivityCallback(new VideoRecorderCommon.StartMediaActivityCallback() {
            @Override
            public void startMediaActivity() {

                Intent intent = new Intent(Main.this, MediaActivity.class);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, 3);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, CropKey.RATIO_MODE_9_16);
                intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, false);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, VideoQuality.HD);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, 0);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, 0);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 25);
                intent.putExtra(AliyunSnapVideoParam.CROP_MODE, ScaleMode.PS);
                intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, 3000);
                intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, 3000);
                intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, 20000);
                intent.putExtra(AliyunSnapVideoParam.SORT_MODE, AliyunSnapVideoParam.SORT_MODE_MERGE);
                intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, VideoCodecs.H264_HARDWARE);
                startActivity(intent);
            }
        });
        //到裁剪页面
        VideoRecorderCommon.instance().setStartEditActivityCallback(new VideoRecorderCommon.StartEditActivityCallback() {
            @Override
            public void startEditActivity(Bundle bundle) {
                Log.i("xianghaTag","setStartEditActivityCallback");
                startActivity(new Intent(Main.this, EditorActivity.class).putExtras(bundle));
            }
        });
        VideoRecorderCommon.instance().setVideoStatictisCallBack(new VideoRecorderCommon.videoStatictisCallBack() {
            @Override
            public void staticticClick(String eventID, String twoLevel, String threeLevel) {
                XHClick.onEvent(XHApplication.in(),eventID,twoLevel,threeLevel);
            }
        });
    }
}
