package acore.override;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.mob.MobApplication;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import acore.dialogManager.VersionOp;
import acore.override.helper.XHActivityManager;
import acore.tools.ChannelUtil;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import third.growingio.GrowingIOController;
import third.mall.aplug.MallReqInternet;
import third.push.umeng.UMPushServer;

import static com.sina.sinavideo.coreplayer.util.AndroidUtil.getProcessName;

public class XHApplication extends MobApplication {
    /**包名*/
    private static final String PACKAGE_NAME = "com.xiangha";
    private static XHApplication mAppApplication;

    //仿造单例，获取application对象
    public static XHApplication in() {
        return mAppApplication;
    }

    public long startTime;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        initHotfix();
    }

    @Override
    public void onCreate() {
        SophixManager.getInstance().queryAndLoadNewPatch();
        Log.i("zhangyujian", "进程名字::" + getProcessName(this));
        startTime = System.currentTimeMillis();
        super.onCreate();
        mAppApplication = this;
        initUmengPush();
        String processName = getProcessName(this);
        if (processName != null) {
            if (processName.equals(PACKAGE_NAME)) {//多进程多初始化，只对xiangha进程进行初始化
                initData();
            }
        }
        long endTime=System.currentTimeMillis();
        Log.i("zhangyujian","XhApplication::oncreate::"+(endTime-startTime));
    }

    /**
     * 初始化数据
     * ---250毫秒耗时
     */
    private void initData() {
        //设置umeng的appId,和渠道名
        String channel = ChannelUtil.getChannel(this);
        MobclickAgent.UMAnalyticsConfig config = new MobclickAgent.UMAnalyticsConfig(this, "545aeac6fd98c565c20004ad", channel);
        MobclickAgent.startWithConfigure(config);

        //bugly集成
        initBugly(getApplicationContext());

        //初始化config变量
        XHConf.init(this);
        MallReqInternet.init(getApplicationContext());
        ReqInternet.init(getApplicationContext());
        ReqEncyptInternet.init(getApplicationContext());
        LoadImage.init(getApplicationContext());

        //GrowingIO初始化
        new GrowingIOController().init(this);
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                XHActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                //记录当前activity
                XHActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }


    @Override
    public void onTerminate() {
        mAppApplication = null;
        super.onTerminate();
        //整体摧毁的时候调用这个方法
    }

    private void initUmengPush() {
        try {
            new UMPushServer(this).register();
        } catch (Exception e) {
            //防止
        }
    }

    /**
     * 初始化bugly
     *
     * @param context
     */
    public void initBugly(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
                strategy.setAppChannel(ChannelUtil.getChannel(context));
                strategy.setAppReportDelay(5 * 1000);
                //		测试阶段建议设置成true，发布时设置为false
                String versoinName = VersionOp.getVerName(context);
                Log.i("tzy","versoinName = " + versoinName);
                String[] temp = versoinName.split("\\.");
                Log.i("tzy","temp = " + temp.toString());
                String testAppID = "4146e8557a";//测试APP id
                String AppID = "1150004142";//正式APP id
                boolean isTest = temp.length != 3;
                Log.i("tzy","isTest = " + isTest);
                CrashReport.initCrashReport(context, isTest ? testAppID:AppID , isTest, strategy);
                CrashReport.setUserId(ToolsDevice.getXhIMEI(context));
            }
        }).start();

    }

    private void initHotfix() {
        SophixManager.getInstance().setContext(this)
                .setAppVersion(VersionOp.getVerName(this))
                .setAesKey("v587xiangha05186")
                .setEnableDebug(false)
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onLoad(final int mode, final int code, final String info, final int handlePatchVersion) {
                        String msg = new StringBuilder("").append("Mode:").append(mode)
                                .append(" Code:").append(code)
                                .append(" Info:").append(info)
                                .append(" HandlePatchVersion:").append(handlePatchVersion).toString();
                        Log.i("tzy_hot",msg);
                    }
                }).initialize();

    }
}
