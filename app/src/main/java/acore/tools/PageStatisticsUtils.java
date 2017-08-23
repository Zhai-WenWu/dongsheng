package acore.tools;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Fang Ruijiao on 2017/8/23.
 */
public class PageStatisticsUtils {

    private static Map<String,String> pageMap;

    /**
     * 统计页面停留时间
     * @param resumeTime ；显示到界面的时间
     * @param pauseTime ：离开时的时间
     */
    public static void onPausePage(Activity activity, long resumeTime, long pauseTime){
        onPausePage(getPageName(activity),resumeTime,pauseTime);
    }

    public static void onPageChange(Activity from,String to){
        onPageChange(getPageName(from),to);
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
        String activityName = context.getClass().getName();
        String name = activityName;
        if(pageMap != null && pageMap.containsKey(activityName)){
            String pageName = pageMap.get(activityName);
            name = pageMap.get(pageName);
            if(TextUtils.isEmpty(name)) name = pageName;
        }
        return name;
    }

    public static void getPageInfo(final Context con){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg= FileManager.getFromAssets(con, "pageStatistics");
                ArrayList<Map<String,String>> listmap = StringManager.getListMapByJson(msg);
                if(listmap.size() > 0){
                    pageMap = listmap.get(0);
                }
            }
        }).start();
    }

}
