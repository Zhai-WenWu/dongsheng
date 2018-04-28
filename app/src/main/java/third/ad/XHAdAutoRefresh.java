package third.ad;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

import acore.logic.ActivityMethodManager;
import acore.logic.ConfigMannager;
import acore.tools.StringManager;

/**
 * Description :
 * PackageName : third.ad
 * Created by tanzeyu on 14/03/2018 11:03.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHAdAutoRefresh {

    private static volatile XHAdAutoRefresh sInstance = null;

    public static long intervalTime = 5 * 60 * 1000;

    private XHAdAutoRefresh() {
    }

    public static XHAdAutoRefresh getInstance() {
        if (null == sInstance) {
            synchronized (XHAdAutoRefresh.class) {
                if (null == sInstance) {
                    sInstance = new XHAdAutoRefresh();
                    Map<String, String> params = StringManager.getFirstMap(ConfigMannager.getConfigByLocal(ConfigMannager.KEY_NEW_AD_CONFIG));
                    String str = params.get("refreshTime");
                    if (!TextUtils.isEmpty(str)) {
                        int time = Integer.parseInt(str);
                        if (time > 0)
                            intervalTime = time * 1000;
                    }
                }
            }
        }
        return sInstance;
    }

    private Handler mTimerHandler;
    private Runnable mRunnable;
    private ActivityMethodManager mManager;

    // 时刻取得导航提醒
    public void startTimer(ActivityMethodManager activityMethodManager,long intervalOnResumeTime) {
        this.mManager = activityMethodManager;
        if(mTimerHandler == null){
            mTimerHandler = new Handler(Looper.getMainLooper());
        }
        execute();
        if(intervalOnResumeTime > 0
                && intervalOnResumeTime < intervalTime
                && mTimerHandler != null){
           //YLKLog.i("tzy", "startTimer: " + intervalOnResumeTime + "ms");
            mTimerHandler.postDelayed(postSingle(),intervalOnResumeTime);
        }
    }

    public void stopTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
        mManager = null;
    }

    private void execute(){
        if(mTimerHandler != null){
            initRunnable();
            mTimerHandler.postDelayed(mRunnable,intervalTime);
        }
    }

    private Runnable postSingle() {
        return new Runnable() {

            @Override
            public void run() {
                //更新广告数据
                autoRefreshSelfAD();
            }
        };
    }

    private void initRunnable() {
        if(mRunnable == null){
            mRunnable = () -> {
                //更新广告数据
                autoRefreshSelfAD();
                execute();
            };
        }
    }

    private void autoRefreshSelfAD() {
        if(mManager != null){
            mManager.autoRefreshSelfAD();
        }
    }

}
