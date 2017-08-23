package acore.tools;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Created by Fang Ruijiao on 2017/8/23.
 */
public class PageStatisticsUtils {

    /**
     * 统计页面停留时间
     * @param resumeTime ；显示到界面的时间
     * @param pauseTime ：离开时的时间
     */
    public static void onPausePage(Activity activity, long resumeTime, long pauseTime){
        onPausePage(activity.getClass().getName(),resumeTime,pauseTime);
    }

    public static void onPageChange(String from,Activity to){
        onPageChange(from,getPageName(to));
    }

    public static void onPageChange(String from,String to){
        Log.i("pageStatistics","from:" + from + "  to:" + to);
    }

    private static void onPausePage(String name,long resumeTime, long pauseTime){
        Log.i("pageStatistics",name + "  pageTime:" + (pauseTime - resumeTime));
    }

    public static String getPageName(Context context){
        return context.getClass().getName();
    }

}
