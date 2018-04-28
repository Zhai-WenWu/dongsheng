package acore.tools;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;

/**
 * 页面统计类
 * ---单例模式
 * Created by XiangHa on 2017/8/23.
 */
public class PageStatisticsUtils {

    private static PageStatisticsUtils pageStatisticsUtils;
    private PageStatisticsUtils(){}
    public static PageStatisticsUtils getInstance(){
        synchronized (PageStatisticsUtils.class){
            if(pageStatisticsUtils==null){
                pageStatisticsUtils= new PageStatisticsUtils();
            }
        }
        return pageStatisticsUtils;
    }
    //内置规则的类
    private static Map<String,String> pageMap;

    /**
     * 统计页面停留时间
     * @param resumeTime ；显示到界面的时间
     * @param pauseTime ：离开时的时间
     */
    public void onPausePage(Activity activity, long resumeTime, long pauseTime){
        onPausePage(getPageName(activity),resumeTime,pauseTime);
    }

    public void onPageChange(Activity from,String to){
        onPageChange(getPageName(from),to);
    }

    public void onPageChange(String from,Activity to){
        onPageChange(from,getPageName(to));
    }

    public void onPageChange(String from,String to){
       //YLKLog.i("pageStatistics.......路径","from:" + from + "  to:" + to);
    }

    private void onPausePage(String name,long resumeTime, long pauseTime){
       //YLKLog.i("pageStatistics.时间",name + "  pageTime:" + (pauseTime - resumeTime));
        XHClick.savePageStatictis(Uri.encode(name),String.valueOf((pauseTime - resumeTime)/1000));
    }

    /**
     * 获取当前页面名称
     * @param context
     * @return
     */
    public String getPageName(Context context){
        String activityName = context.getClass().getName();
        String name = activityName;
        if(pageMap != null && pageMap.containsKey(activityName)){
            String pageName = pageMap.get(activityName);
            name = pageMap.get(pageName);
            if(TextUtils.isEmpty(name)) name = pageName;
        }
        return name;
    }

    /**
     * 获取内置数据的匹配资源
     * @param con
     */
    public void getPageInfo(final Context con){
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
