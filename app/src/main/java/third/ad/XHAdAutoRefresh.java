package third.ad;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import acore.logic.ActivityMethodManager;

/**
 * Description :
 * PackageName : third.ad
 * Created by tanzeyu on 14/03/2018 11:03.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHAdAutoRefresh {

    private static volatile XHAdAutoRefresh sInstance = null;

    //TODO
    public static long intervalTime = 5 * 3 * 1000;

    private XHAdAutoRefresh() {
    }

    public static XHAdAutoRefresh getInstance() {
        if (null == sInstance) {
            synchronized (XHAdAutoRefresh.class) {
                if (null == sInstance) {
                    sInstance = new XHAdAutoRefresh();
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
                && mTimerHandler != null
                && mRunnable != null){
            Log.i("tzy", "startTimer: " + intervalOnResumeTime + "ms");
            mTimerHandler.postDelayed(mRunnable,intervalOnResumeTime);
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
