package acore.override;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AppActivity;
import com.mob.MobApplication;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import com.xiangha.R;

import java.util.Map;

import acore.logic.AllPopDialogHelper;
import acore.logic.ConfigMannager;
import acore.override.helper.XHActivityManager;
import acore.tools.ChannelUtil;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.XHConf;
import aplug.service.CoreService;
import third.location.LocationHelper;
import third.push.umeng.UMPushServer;
import third.push.xg.XGPushServer;

import static acore.logic.ConfigMannager.KEY_BAIDUAPPID;

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
        mAppApplication = this;

        startTime = System.currentTimeMillis();
        loadLibs();
        WebView.setWebContentsDebuggingEnabled(Tools.isDebug(this));
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        LogManager.printStartTime("zhangyujian","XhApplication::11111.oncreate::");
        boolean isOnce = TextUtils.isEmpty(FileManager.loadShared(this,FileManager.xmlFile_appInfo,"once").toString());
        FileManager.saveShared(this,FileManager.xmlFile_appInfo,"once",String.valueOf(isOnce));
        //记录开启次数
        AllPopDialogHelper.updateIntervalCount(this);
        try{
            super.onCreate();//耗时100毫秒，处理掉
        }catch (Exception e){
            e.printStackTrace();
        }
        LogManager.printStartTime("zhangyujian","XhApplication::222222.oncreate::");

        //初始化umeng推送
        initUmengPush();
        new XGPushServer(this).initPush();

        String processName = Tools.getProcessName(this);
        Log.i("zhangyujian", "进程名字::" + processName);
        if (processName != null && processName.equals(ToolsDevice.getPackageName(this))) {//多进程多初始化，只对xiangha进程进行初始化
            initData();
        }
        LogManager.printStartTime("zhangyujian","XhApplication::oncreate::");
    }

    /**
     * 初始化数据
     * ---150毫秒耗时
     */
    private void initData() {
        //设置umeng的appId,和渠道名
        String channel = ChannelUtil.getChannel(this);
        MobclickAgent.UMAnalyticsConfig config = new MobclickAgent.UMAnalyticsConfig(this, "545aeac6fd98c565c20004ad", channel);
        MobclickAgent.startWithConfigure(config);
        LogManager.printStartTime("zhangyujian","XhApplication:0000:initData::");
        //bugly集成
        initBugly(getApplicationContext());

        //初始化config变量
        XHConf.init(this);
        LoadImage.init(this);
        LogManager.printStartTime("zhangyujian","XhApplication:1111:initData::");
        //设置百度appid
        Map<String,String> map = StringManager.getFirstMap(ConfigMannager.getConfigByLocal(KEY_BAIDUAPPID));
        if(map.containsKey("appid") && !TextUtils.isEmpty(map.get("appid"))){
            AdView.setAppSid(this, map.get("appid"));
        }else
            AdView.setAppSid(this, "db905294");

        // 百度AD-设置'广告着陆页'动作栏的颜色主题
        try{
            AppActivity.getActionBarColorTheme().setBackgroundColor(Color.parseColor(getString(R.color.common_top_bg)));
            int commonTopTextColor = Color.parseColor(getString(R.color.common_top_text));
            AppActivity.getActionBarColorTheme().setTitleColor(commonTopTextColor);
            AppActivity.getActionBarColorTheme().setCloseColor(commonTopTextColor);
            AppActivity.getActionBarColorTheme().setProgressColor(commonTopTextColor);
        }catch (Exception e){
            Log.i("", "initData: SB");
        }

        LogManager.printStartTime("zhangyujian","XhApplication:222:initData::");
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
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
        startLocation();
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

    private void startLocation() {
        LocationHelper.getInstance().startLocation();
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
                CrashReport.setUserId(ToolsDevice.getXhCode(context));
            }
        }).start();
    }
    private void loadLibs() {
        System.loadLibrary("live-openh264");
        System.loadLibrary("QuCore-ThirdParty");
        System.loadLibrary("QuCore");
    }

}
