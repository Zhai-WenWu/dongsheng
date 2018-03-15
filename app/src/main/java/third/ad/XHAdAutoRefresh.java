package third.ad;

import android.os.Handler;
import android.os.Looper;

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
    public static long intervalTime = 5 * 2 * 1000;

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

    Handler mTimerHandler;
    Runnable mRunnable;
    ActivityMethodManager mManager;

    // 时刻取得导航提醒
    public void startTimer(ActivityMethodManager activityMethodManager) {
        this.mManager = activityMethodManager;
        if(mTimerHandler == null){
            mTimerHandler = new Handler(Looper.getMainLooper());
            execute();
        }
    }

    public void stopTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacks(mRunnable);
            mTimerHandler = null;
            mRunnable = null;
        }
        mManager = null;
    }

    private void execute(){
        if(mTimerHandler != null){
            if(mRunnable == null){
                mRunnable = () -> {
                    //更新广告数据
                    autoRefreshSelfAD();
                    execute();
                };
                mTimerHandler.post(mRunnable);
            }else{
                mTimerHandler.postDelayed(mRunnable,intervalTime);
            }
        }
    }

    private void autoRefreshSelfAD() {
        if(mManager != null){
            mManager.autoRefreshSelfAD();
        }
    }

}
