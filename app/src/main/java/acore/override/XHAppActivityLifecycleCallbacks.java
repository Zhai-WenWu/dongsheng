package acore.override;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * activity生命周期监听---14以上方法启用
 */
public class XHAppActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks{
    private Activity activity;
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//        Log.i("zhangyujian","onActivityCreated:::"+activity.getComponentName().getClassName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
//        Log.i("zhangyujian","onActivityStarted:::"+activity.getComponentName().getClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
//        Log.i("zhangyujian","onActivityResumed:::"+activity.getComponentName().getClassName());
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
//        Log.i("zhangyujian","onActivityPaused:::"+activity.getComponentName().getClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
//        Log.i("zhangyujian","onActivityStopped:::"+activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//        Log.i("zhangyujian","onActivitySaveInstanceState:::"+activity.getComponentName().getClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        Log.i("zhangyujian","onActivityDestroyed:::"+activity.getComponentName().getClassName());
    }

    /**
     * 获取当前activity对象
     * @return
     */
    public Activity getCurrentActivity() {
        return activity;
    }

    /**
     * 设置对象
     * @param activity
     */
    public void setCurrentActivity(Activity activity) {
        if(this.activity!=null)this.activity=null;
        this.activity = activity;
    }
}
