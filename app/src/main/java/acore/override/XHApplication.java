package acore.override;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mobads.AdView;
import com.baidu.mobads.AppActivity;
import com.mob.MobApplication;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.analytics.MobclickAgent;
import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.ChannelUtil;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import third.growingio.GrowingIOController;
import third.mall.aplug.MallReqInternet;
import third.push.umeng.UMPushServer;
import third.qiyu.QiYvHelper;

public class XHApplication extends MobApplication {
    /**包名*/
    public static final String ONLINE_PACKAGE_NAME = "com.xiangha";
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
    }

    @Override
    public void onCreate() {
        startTime = System.currentTimeMillis();
        try{
            super.onCreate();
        }catch (SecurityException e) {
            //捕捉已知MobSDK定位bug
            e.printStackTrace();
        }catch (Exception e){
            CrashReport.postCatchedException(e);
        }
        LogManager.printStartTime("zhangyujian","XhApplication::super.oncreate::");
        mAppApplication = this;

        //初始化umeng推送
        initUmengPush();

        String processName = Tools.getProcessName(this);
        Log.i("zhangyujian", "进程名字::" + processName);
        if (processName != null && processName.equals(ToolsDevice.getPackageName(this))) {//多进程多初始化，只对xiangha进程进行初始化
            initData();
        }
        LogManager.printStartTime("zhangyujian","XhApplication::oncreate::");
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

        //设置百度appid
        Map<String,String> map = StringManager.getFirstMap(AppCommon.getConfigByLocal("baiduappid"));
        if(map.containsKey("appid") && !TextUtils.isEmpty(map.get("appid"))){
            AdView.setAppSid(this, map.get("appid"));
        }else
            AdView.setAppSid(this, "db905294");

        // 百度AD-设置'广告着陆页'动作栏的颜色主题
        AppActivity.getActionBarColorTheme().setBackgroundColor(Color.parseColor(getString(R.color.common_top_bg)));
        int commonTopTextColor = Color.parseColor(getString(R.color.common_top_text));
        AppActivity.getActionBarColorTheme().setTitleColor(commonTopTextColor);
        AppActivity.getActionBarColorTheme().setCloseColor(commonTopTextColor);
        AppActivity.getActionBarColorTheme().setProgressColor(commonTopTextColor);

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
        //七鱼初始化 init方法无需放入主进程中执行，其他的初始化，有必要放在放入主进程
        QiYvHelper.getInstance().initSDK(this);

        initX5();
    }

    /**
     * 初始化X5浏览器
     */
    private void initX5() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
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
                //测试APP id = "4146e8557a";
                //正式APP id = "1150004142";
                String appId = Tools.isDebug(context) ?  "4146e8557a" : "1150004142";
                CrashReport.initCrashReport(context, appId , Tools.isDebug(context), strategy);
                CrashReport.setUserId(ToolsDevice.getXhIMEI(context));
            }
        }).start();
    }

}
