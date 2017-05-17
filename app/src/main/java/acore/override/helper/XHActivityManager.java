package acore.override.helper;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * 存放当前activity
 */
public class XHActivityManager{
    private static XHActivityManager xhActivityManager;
    private XHActivityManager(){}
    public  static XHActivityManager getInstance(){
        if(xhActivityManager==null){
            synchronized (XHActivityManager.class){
                if(xhActivityManager==null)xhActivityManager= new XHActivityManager();
            }
        }
        return xhActivityManager;
    }

    private WeakReference<Activity> currentActivityWeakRef;

    /**
     * 获取当前activity对象
     * @return
     */
    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (currentActivityWeakRef != null) {
            currentActivity = currentActivityWeakRef.get();
        }
        return currentActivity;
    }

    /**
     * 设置当前activity对象
     * @param activity
     */
    public void setCurrentActivity(Activity activity) {
        if(currentActivityWeakRef!=null){
            currentActivityWeakRef=null;
        }
        currentActivityWeakRef = new WeakReference<Activity>(activity);
    }
}
