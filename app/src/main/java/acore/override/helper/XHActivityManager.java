package acore.override.helper;

import android.app.Activity;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    private ArrayList<Activity> activityArrayList= new ArrayList<>();
    public void addActivity(Activity activity){
        activityArrayList.add(activity);
    }
    public void removeActivity(Activity activity){
        activityArrayList.remove(activity);
    }

    /**
     * 刷新接口
     */
    public interface RefreshCallBack{
        public void refreshCallBack();
    }

    /**
     * 刷新activity
     */
    public void refreshActivity(){
        int size=activityArrayList.size();
        if(size<=0)return;
        for(int i= 0;i<size;i++){
            Activity v=activityArrayList.get(i);
            if(v instanceof RefreshCallBack)
                ((RefreshCallBack) v).refreshCallBack();
        }
    }
}
