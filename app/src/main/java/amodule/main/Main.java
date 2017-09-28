/**
 * @author Jerry
 * 2013-4-24 上午10:31:01
 * Copyright: Copyright (c) xiangha.com 2011
 */

package amodule.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.dialogManager.DialogControler;
import acore.dialogManager.GoodCommentManager;
import acore.dialogManager.PushManager;
import acore.dialogManager.VersionOp;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.XiangHaTabHost;
import amodule.answer.activity.AnswerEditActivity;
import amodule.answer.activity.AskEditActivity;
import amodule.answer.db.AskAnswerSQLite;
import amodule.answer.model.AskAnswerModel;
import amodule.article.activity.ArticleUploadListActivity;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadParentSQLite;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.UploadDishData;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.dish.tools.UploadDishControl;
import amodule.main.Tools.MainInitDataControl;
import amodule.main.activity.MainChangeSend;
import amodule.main.activity.MainCircle;
import amodule.main.activity.MainHome;
import amodule.main.activity.MainMyself;
import amodule.main.view.MainBuoy;
import amodule.main.view.WelcomeDialog;
import amodule.quan.tool.MyQuanDataControl;
import amodule.user.activity.MyMessage;
import aplug.basic.ReqInternet;
import aplug.shortvideo.ShortVideoInit;
import third.ad.control.AdControlHomeDish;
import third.mall.MainMall;
import third.mall.alipay.MallPayActivity;
import third.mall.aplug.MallCommon;
import third.push.xg.XGLocalPushServer;
import third.qiyu.QiYvHelper;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;
import xh.windowview.XhDialog;

import static acore.tools.Tools.getApiSurTime;
import static com.xiangha.R.id.iv_itemIsFine;


@SuppressWarnings("deprecation")
public class Main extends Activity implements OnClickListener {
    public static Main allMain;
    public static Timer timer;
    /** 把层级>=close_level的层级关闭 */
    public static int colse_level = 1000;
    public static MainBaseActivity mainActivity;

    public Map<String, MainBaseActivity> allTab = new HashMap<>();

    private View[] tabViews;
    private XiangHaTabHost tabHost;
    private LinearLayout linear_item;
    private RelativeLayout mRootLayout;
    private RelativeLayout changeSendLayout;
    private MainBuoy mBuoy;
    // 页面关闭层级
    private LocalActivityManager mLocalActivityManager;

    private Class<?>[] classes = new Class<?>[]{MainHome.class, MainMall.class,
            MainCircle.class, MyMessage.class, MainMyself.class};
    private String[] tabTitle = {"首页", "商城", "社区", "消息", "我的"};
    private int[] tabImgs = new int[]{R.drawable.tab_index, R.drawable.tab_mall,
            R.drawable.tab_found, R.drawable.tab_four, R.drawable.tab_myself};
    private int doExit = 0;
    private int defaultTab = 0;
    private String url = null;
    // 每过everyReq请求一次，runTime+1
    private int runTime = 100, everyReq = 4 * 60;
    private boolean quanRefreshState = false;

    private boolean WelcomeDialogstate = false;//false表示当前无显示,true已经显示
    private boolean mainOnResumeState = false;//false 无焦点，true 获取焦点
    private MainInitDataControl mainInitDataControl;

    private long homebackTime;
    private boolean isForeground = true;
    private int nowTab = 0;//当前选中tab
    public static boolean isShowWelcomeDialog = false;//是否welcomedialog在展示，false未展示，true正常展示,static 避免部分手机不进行初始化和回收
    private boolean isInit=false;//是否已经进行初始化
    private WelcomeDialog welcomeDialog;//dialog,显示

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main.this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
        setContentView(R.layout.xh_main);

        LogManager.printStartTime("zhangyujian","main::oncreate::start::");
        //腾讯统计
        StatConfig.setDebugEnable(false);
        StatConfig.setInstallChannel(this, ChannelUtil.getChannel(this));
        StatConfig.setSendPeriodMinutes(1);//设置发送策略：每一分钟发送一次
        StatService.setContext(this.getApplication());

        allMain = this;
        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        String[] times = FileManager.getSharedPreference(XHApplication.in(), FileManager.xmlKey_appKillTime);
        if (times != null && times.length > 1 && !TextUtils.isEmpty(times[1])) {
            Tools.getApiSurTime("killback", Long.parseLong(times[1]), System.currentTimeMillis());
        }
        LogManager.print("i", "Main -------- onCreate");

        // 当软件后台重启时,根据保存的值,回到关闭前状态的text的字体显示
        if (savedInstanceState != null) {
            defaultTab = Integer.parseInt(savedInstanceState.getString("currentTab"));
            if (defaultTab == 0 && mBuoy != null && !TextUtils.isEmpty(mBuoy.getFloatIndex()) && "1".equals(mBuoy.getFloatIndex())
                    || defaultTab == 1 && mBuoy != null && !TextUtils.isEmpty(mBuoy.getFloatSubjectList()) && "1".equals(mBuoy.getFloatSubjectList())
                    || defaultTab == 2
                    || defaultTab == 3) {
                if (mBuoy != null) {
                    mBuoy.clearAnimation();
                    mBuoy.hide();
                    mBuoy.setClosed(true);
                    mBuoy.setMove(true);
                }
            }
        }
        mainInitDataControl = new MainInitDataControl();
        welcomeDialog = LoginManager.isShowAd() ?
                new WelcomeDialog(Main.allMain,dialogShowCallBack) : new WelcomeDialog(Main.allMain,1,dialogShowCallBack);
        welcomeDialog.show();
        LogManager.printStartTime("zhangyujian","main::oncreate::");
        QiYvHelper.getInstance().addOnUrlItemClickListener(new QiYvHelper.OnUrlItemClickListener() {
            @Override
            public void onURLClicked(Context context, String url) {
                if (!TextUtils.isEmpty(url)) {
                    if (url.contains("m.ds.xiangha.com") && url.contains("product_code=")) {//商品详情链接
                        String[] strs = url.split("\\?");
                        if (strs != null && strs.length > 1) {
                            String params = strs[1];
                            if (!TextUtils.isEmpty(params)) {
                                AppCommon.openUrl(Main.this, "xhds.product.info.app?" + params, true);
                            }
                        }
                    } else {
                        AppCommon.openUrl(Main.this, "xiangha://welcome?showWeb.app?url=" + Uri.encode(url), true);
                    }
                }
            }
        });
    }

    /**
     * welcomeDialog的回调封装
     */
    private WelcomeDialog.DialogShowCallBack dialogShowCallBack = new WelcomeDialog.DialogShowCallBack() {
        @Override
        public void dialogState(boolean show) {
            if (!show) {//展示后关闭
                Log.i("zhangyujian", "________________________________________________________");
                if (mainInitDataControl != null) {
                    mainInitDataControl.initMainOnResume(Main.this);
                    mainInitDataControl.iniMainAfter(Main.this);
                }
                showIndexActivity();
                WelcomeDialogstate = true;
                openUri();
                new DialogControler().showDialog();
                PushManager.tongjiPush();
                isShowWelcomeDialog = false;

                OffDishToFavoriteControl.addCollection(Main.this);
                //初始化电商页面统计
                PageStatisticsUtils.getInstance().getPageInfo(getApplicationContext());

                if (showQAUploading())
                    return;
                if (showUploading(new UploadArticleSQLite(XHApplication.in().getApplicationContext()), EditParentActivity.DATA_TYPE_ARTICLE, "您的文章还未上传完毕，是否继续上传？"))
                    return;
                if (showUploading(new UploadVideoSQLite(XHApplication.in().getApplicationContext()), EditParentActivity.DATA_TYPE_VIDEO, "您的视频还未上传完毕，是否继续上传？"))
                    return;
            }
        }

        private boolean showQAUploading() {
            boolean show = false;
            final AskAnswerSQLite sqLite = new AskAnswerSQLite(XHApplication.in().getApplicationContext());
            final AskAnswerModel model = sqLite.queryFirstData();
            String msg = "";
            Intent intent = null;
            Class tempC = null;
            if (model != null) {
                intent = new Intent();
                intent.putExtra("fromHome", true);
                intent.putExtra("code", model.getmDishCode());
                intent.putExtra("qaCode", model.getmQACode());
                intent.putExtra("authorCode", model.getmAuthorCode());
                intent.putExtra("qaTitle", model.getmTitle());
                intent.putExtra("answerCode", model.getmAnswerCode());
                boolean isAskAgain = false;
                boolean isAnswerAgain = false;
                String qaType = model.getmType();
                if (!TextUtils.isEmpty(qaType)) {
                    switch (qaType) {
                        case AskAnswerModel.TYPE_ANSWER:
                            msg = "您有一个回答尚未发布，是否继续？";
                            tempC = AnswerEditActivity.class;
                            break;
                        case AskAnswerModel.TYPE_ANSWER_AGAIN:
                            msg = "您有一个回答尚未发布，是否继续？";
                            tempC = AnswerEditActivity.class;
                            isAnswerAgain = true;
                            break;
                        case AskAnswerModel.TYPE_ASK:
                            msg = "您有一个问题尚未发布，是否继续？";
                            tempC = AskEditActivity.class;
                            break;
                        case AskAnswerModel.TYPE_ASK_AGAIN:
                            msg = "您有一个问题尚未发布，是否继续？";
                            tempC = AskEditActivity.class;
                            isAskAgain = true;
                            break;
                    }
                }
                intent.putExtra("mIsAnswerMore", isAnswerAgain ? "2" : "1");
                intent.putExtra("isAskMore", isAskAgain ? "2" : "1");
                if (tempC == null)
                    return show;
                show = true;
                intent.setClass(Main.this, tempC);
                final Intent finalIntent = intent;
                final XhDialog dialog = new XhDialog(Main.this);
                dialog.setTitle(msg)
                        .setCancelable(true)
                        .setCanselButton("否", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sqLite.deleteAll();
                                    }
                                }).start();
                            }
                        })
                        .setSureButton("是", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Main.this.startActivity(finalIntent);
                                dialog.cancel();
                            }
                        })
                        .show();
            }
            return show;
        }

        private boolean showUploading(final UploadParentSQLite sqLite, final int dataType, String title) {
            final UploadArticleData uploadArticleData = sqLite.getUploadIngData();
            if (uploadArticleData != null) {
                final XhDialog xhDialog = new XhDialog(Main.this);
                xhDialog.setTitle(title)
                        .setCanselButton("取消", new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                uploadArticleData.setUploadType(UploadDishData.UPLOAD_PAUSE);
                                sqLite.update(uploadArticleData.getId(),uploadArticleData);
                                xhDialog.cancel();
                            }
                        }).setSureButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Main.this, ArticleUploadListActivity.class);
                        intent.putExtra("draftId", uploadArticleData.getId());
                        intent.putExtra("dataType", dataType);
                        intent.putExtra("coverPath", uploadArticleData.getImg());
                        String videoPath = "";
                        ArrayList<Map<String,String>> videoArray = uploadArticleData.getVideoArray();
                        if(videoArray.size() > 0){
                            videoPath = videoArray.get(0).get("video");
                        }
                        intent.putExtra("finalVideoPath", videoPath);
                        startActivity(intent);
                        xhDialog.cancel();
                    }
                }).setSureButtonTextColor("#333333")
                        .setCancelButtonTextColor("#333333")
                        .show();
                return true;
            }
            return false;
        }

        @Override
        public void dialogOnLayout() {
            Log.i("zhangyujian", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            if(!isInit){
                dialogOnCreate();
            }
            AdControlHomeDish.getInstance();
            setCurrentTabByIndex(defaultTab);
            init();
            initRunTime();
            mainInitDataControl.initWelcomeOncreate();
            mainInitDataControl.initWelcomeAfter(Main.this);
            if(LoginManager.isLogin())MallCommon.getShoppingNum(Main.this,null,null);

        }

        @Override
        public void dialogOnCreate() {
            initUI();
            initData();
        }

        @Override
        public void dialogAdComplete() {
        }
    };

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
    private void init() {
        //初始化短视频拍摄
        //从Welcome方法
        ShortVideoInit.init(Main.this);
        //从Welcome方法
//        QbSdk.initX5Environment(Main.this, null);
    }

    /**
     * 初始化布局
     */
    @SuppressLint("HandlerLeak")
    private void initUI() {

        String colors = Tools.getColorStr(Main.this, R.color.common_top_bg);
        Tools.setStatusBarColor(Main.this, Color.parseColor(colors));

        mRootLayout = (RelativeLayout) findViewById(R.id.main_root_layout);

        //实例化有用到mRootLayout，必须按着顺序执行
        mBuoy = new MainBuoy(this);
        tabHost = (XiangHaTabHost) findViewById(R.id.xiangha_tabhost);
        tabHost.setup(mLocalActivityManager);
        linear_item = (LinearLayout) findViewById(R.id.linear_item);
        ImageView btn_changeSend = (ImageView) findViewById(R.id.btn_changeSend);
        changeSendLayout = (RelativeLayout) findViewById(R.id.btn_changeSend_layout);
        changeSendLayout.setVisibility(View.GONE);
        int btn_width = ToolsDevice.getWindowPx(this).widthPixels / 5;
        int padding = (btn_width - Tools.getDimen(this, R.dimen.dp_55)) / 2;
        int dp_3 = Tools.getDimen(this,R.dimen.dp_3);
        int cha = padding / 4;
        cha = 0;
        changeSendLayout.getLayoutParams().width = btn_width;
        btn_changeSend.getLayoutParams().width = btn_width;
        btn_changeSend.setPadding(padding + cha+dp_3, dp_3, padding - cha+dp_3, dp_3);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        tabViews = new View[classes.length];
        for (int i = 0; i < tabTitle.length; i++) {
            tabViews[i] = linear_item.getChildAt(i);
            LinearLayout layout = (LinearLayout) tabViews[i].findViewById(R.id.tab_linearLayout);
            layout.setOnClickListener(this);

            TextView tv = ((TextView) tabViews[i].findViewById(R.id.textView1));
            tv.setText(tabTitle[i]);

            ImageView imgView = (ImageView) tabViews[i].findViewById(iv_itemIsFine);
            imgView.setImageResource(tabImgs[i]);

//			if (i == 2) {
//				tv.setVisibility(View.GONE);
//				imgView.setVisibility(View.GONE);
//			}
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
        int margin = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_5) * 2
                - Tools.getDimen(this, R.dimen.dp_70) * 5) / 4 / 2;
        int length = linear_item.getChildCount();
        for (int i = 0; i < length; i++) {
            setTabItemMargins(linear_item, i, margin, margin);
        }
        setTabItemMargins(linear_item, 0, 0, margin);
        setTabItemMargins(linear_item, length - 1, margin, 0);

    }

    public void setTabItemMargins(ViewGroup viewGroup, int index, int leftMargin, int rightMargin) {
        RelativeLayout child = (RelativeLayout) viewGroup.getChildAt(index);
        LinearLayout.LayoutParams params_child = (LinearLayout.LayoutParams) child.getLayoutParams();
        params_child.setMargins(leftMargin, 0, leftMargin, 0);
    }

    // 时刻取得导航提醒
    private void initRunTime() {
        timer = new Timer();
        final Handler handler = new Handler();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        runTime++;
                        AppCommon.getCommonData(null);
                    }
                });
            }
        };
        timer.schedule(tt, everyReq * 1000, everyReq * 1000);
    }

    public void onChangeSend(View v) {
        MyQuanDataControl.getNewMyQuanData(this, null);
        XHClick.mapStat(this, "a_index530", "底部导航栏", "点击底部发布按钮");
        XHClick.mapStat(this, MainCircle.STATISTICS_ID, "发贴", null);
        XHClick.mapStat(this, "a_down", "+", "");
        Intent intent = new Intent(this, MainChangeSend.class);
        startActivity(intent);
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
        //设置未读消息数
        QiYvHelper.getInstance().getUnreadCount(new QiYvHelper.NumberCallback() {
            @Override
            public void onNumberReady(int count) {
                Main.setNewMsgNum(3, AppCommon.quanMessage + AppCommon.feekbackMessage + AppCommon.myQAMessage + count);
            }
        });
        //去我的页面
        if (MallPayActivity.pay_state) {
            onClick(tabViews[4].findViewById(R.id.tab_linearLayout));
        }
        //去商城页面
//        if (MallPayActivity.mall_state) {
//            onClick(tabViews[1].findViewById(R.id.tab_linearLayout));
//        }
        GoodCommentManager.setStictis(Main.this);
        openUri();

    }

    /**
     * 外部吊起app
     */
    private void openUri() {
        if (mainOnResumeState && WelcomeDialogstate) {
            //这个问题待验证
//        Intent intent = this.getIntent();
//        if (intent != null) {
//            url = intent.getStringExtra("url");
//            if (url != null) {
//                AppCommon.openUrl(this, url, true);
//                intent.removeExtra("url");
//                url = null;
//            }
//        }
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
            super.onSaveInstanceState(outState);
        }catch (Exception ignored){}
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
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
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doExit = 0;
                    }
                }, 1000 * 5);
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                colse_level = 0;
                UploadDishControl.getInstance().updataAllUploadingDish(getApplicationContext());
                // 开启自我唤醒
                if (act != null) new XGLocalPushServer(act).initLocalPush();
                // 关闭时发送页面停留时间统计
                if (act != null) XHClick.finishToSendPath(act);
                // 关闭页面停留时间统计计时器
                XHClick.closeHandler();
                ReqInternet.in().finish();
                VersionOp.getInstance().onDesotry();
                finish();
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
                ((TextView) tabViews[j].findViewById(R.id.textView1)).setTextColor(Color.parseColor("#ff533c"));
                tabViews[j].findViewById(iv_itemIsFine).setSelected(true);
                tabViews[j].findViewById(iv_itemIsFine).setPressed(false);
                if (j == 2) {
                    MainCircle mainCircle = (MainCircle) allTab.get("MainCircle");
                    mainCircle.setQuanmCurrentPage();
                }
            } else {
                TextView textView = (TextView) tabViews[j].findViewById(R.id.textView1);
                textView.setTextColor(Color.parseColor("#1b1b1f"));
                if (j == 1) textView.setText(tabTitle[j]);
                tabViews[j].findViewById(iv_itemIsFine).setSelected(false);
                tabViews[j].findViewById(iv_itemIsFine).setPressed(false);
            }
        }
        if (index == 2) {//特殊美食圈的逻辑
            changeSendLayout.setVisibility(View.VISIBLE);
        } else {
            changeSendLayout.setVisibility(View.GONE);
        }
        if (nowTab == 0 && index != 0) {//当前是首页，切换到其他页面
            if (allTab.containsKey("MainIndex")) {
                MainHome mainIndex = (MainHome) allTab.get("MainIndex");
                mainIndex.saveNowStatictis();
                XHClick.newHomeStatictis(true, null);
            }
        } else if (nowTab != 0 && index == 0) {//当前是其他页面，切换到首页
            if (allTab.containsKey("MainIndex")) {
                MainHome mainIndex = (MainHome) allTab.get("MainIndex");
                mainIndex.setRecommedTime(System.currentTimeMillis());
                mainIndex.onResumeFake();
            }
        }
        //特殊逻辑
//        changeSendLayout.setVisibility(View.VISIBLE);
        if(index == 0 || index == 2){
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
                MyMessage.notifiMessage(MyMessage.MSG_FEEKBACK_ONREFURESH, 0, "");
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
            if (v == tabViews[i].findViewById(R.id.tab_linearLayout) && allTab.size() > 0) {
                if (i == 2 && allTab.containsKey("MainCircle") && i == nowTab) {
                    MainCircle mainCircle = (MainCircle) allTab.get("MainCircle");
//					mainCircle.setCurrentList(0);
                    mainCircle.refresh();
                } else if (i == 0 && allTab.containsKey("MainIndex") && i == nowTab) {
                    MainHome mainIndex = (MainHome) allTab.get("MainIndex");
                    mainIndex.refreshContentView(true);
                } else if (i == 1 && allTab.containsKey("MainMall") && tabHost.getCurrentTab() == i) {  //当所在页面正式你要刷新的页面,就直接刷新
                    MainMall mall = (MainMall) allTab.get("MainMall");
                    mall.scrollTop();
                    mall.refresh();
//                    if (MallCommon.click_state)
//                        mall.refresh();
//                    MainCircle nous = (MainCircle) allTab.get("MainCircle");
//                    nous.refresh();
//                    if(quanRefreshState)
//                        setRoteAnimation(tabViews[1].findViewById(iv_itemIsFine));
                } else if (i == 4 && allTab.containsKey("MainMyself")) {
                    //在onResume方法添加了刷新方法
//                    MainMyself mainMyself = (MainMyself) allTab.get("MainMyself");
//                    mainMyself.scrollToTop();
                } else if (i == 3 && allTab.containsKey("MyMessage") && i == nowTab) {
                    MyMessage myMessage = (MyMessage) allTab.get("MyMessage");
                    myMessage.onRefresh();
                    XHClick.handlerPageStatic();
                }
                // 当软件所在页面正式你要刷新的页面,就直接刷新,不在跳了
//				if (tabHost.getCurrentTab() == i && i == 2) {
//					setCurrentTabByIndex(1);
//					return;
//				}
//				if (i == 2) {
//					setCurrentTabByIndex(1);
//					return;
//				} else {
                try {
                    setCurrentTabByIndex(i);
                } catch (Exception e) {
                    UtilLog.reportError("", e);
                }
//				}
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

    public RelativeLayout getRootLayout() {
        return mRootLayout;
    }

    public MainBuoy getBuoy() {
        return mBuoy;
    }

    public View getTabView(int index) {
        if (tabViews != null
                && tabViews.length == 5
                && index < 5
                && index > -1) {
            return tabViews[index];
        }
        return null;
    }

    /**
     * 执行一个旋转动画
     *
     * @param view
     */
    private void setRoteAnimation(View view) {
        RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(800);
        view.clearAnimation();
        view.startAnimation(animation);
    }

    /**
     * 设置导航美食圈按钮显示刷新状态
     *
     * @param state
     */
    public void setQuanRefreshState(boolean state){
        quanRefreshState=state;
        if(state) {
            ((ImageView)tabViews[2].findViewById(iv_itemIsFine)).setImageResource(R.drawable.tab_found_refresh);
            ((TextView) tabViews[2].findViewById(R.id.textView1)).setText("社区");
        }else{
            ((ImageView)tabViews[2].findViewById(iv_itemIsFine)).setImageResource(R.drawable.tab_found);
            ((TextView) tabViews[2].findViewById(R.id.textView1)).setText("社区");
        }
    }

    public int getDoExit() {
        return doExit;
    }

    public void setDoExit(int doExit) {
        this.doExit = doExit;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public LocalActivityManager getLocalActivityManager() {
        return mLocalActivityManager;
    }

    public MainBaseActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainBaseActivity mainActivity) {
        Main.mainActivity = mainActivity;
    }

    /**
     * welcome处理当前view
     */
    public void showIndexActivity() {
        if (allTab.containsKey("MainIndex")) {
            MainHome mainIndex = (MainHome) allTab.get("MainIndex");
            mainIndex.onActivityshow();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            isInit = true;
//            mainInitDataControl.iniMainAfter(Main.this);
        }
        //此处可以进行分级处理:暂时无需要
        Log.i("zhangyujian", "main::onWindowFocusChanged");
    }

    /**
     * 处理首页统计
     */
    public void handlerHomeStatistics() {
        if (allTab.containsKey("MainIndex")) {
            MainHome mainIndex = (MainHome) allTab.get("MainIndex");
            mainIndex.saveNowStatictis();
        }
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
        if (!LoginManager.isLogin())
            QiYvHelper.getInstance().destroyQiYvHelper();
    }
}
