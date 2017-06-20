package aplug.service;

import android.content.Intent;
import android.text.TextUtils;

import java.util.Calendar;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.Tools;
import aplug.service.base.NormalService;
import aplug.service.base.ServiceManager;
import aplug.service.listener.ScreenObserver;
import third.push.xg.XGPushServer;

/**
 * PackageName : aplug.service
 * Created by MrTrying on 2016/7/8 14:39.
 * E_mail : ztanzeyu@gmail.com
 */
public class CoreService extends NormalService {

    /** 屏幕监听 */
    private ScreenObserver screenObserver = null;
    private boolean flag = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //保存第一次启动时间
        getAppFirstStartTime();
        XHClick.onEventValue(getApplicationContext(), "start_core", "core", "start", 1);
        screenObserver = new ScreenObserver(getApplicationContext());
        screenObserver.startObserver(new ScreenObserver.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                flag = false;
            }

            @Override
            public void onScreenOff() {
                flag = true;
            }

            @Override
            public void onUserPresent() {
                flag = false;
            }
        });
        startBehindActivity();
    }

    private void startBehindActivity() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(ServiceManager.CORE_POLLING);
                        if (flag) {
                            /**XG注册*/
                            registerXG();
                            /**打开activity*/
                            openActivity();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    private void registerXG() {
        try {
            XHClick.onEventValue(getApplicationContext(), "register_xg", "xg", "register", 1);
            String userCode = FileManager.loadShared(getApplicationContext(), FileManager.xmlFile_userInfo, "userCode").toString();
            new XGPushServer(getApplicationContext()).initPush(userCode);
        } catch (Exception e) {
            //针对oppo手机处理
        }
    }

    private void openActivity() {
        if (isOpenActivity()) {
            //记录
            int todayCount = getTodayCount();
            FileManager.saveShared(getApplicationContext(), FileManager.xmlFile_wake, "count", String.valueOf(++todayCount));
            Intent intent = new Intent(this, ServiceOpenActivity.class);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CoreService.this.startActivity(intent);
        }
    }

    private boolean isOpenActivity() {
        String timeStr = getAppFirstStartTime();

        long millis = Long.parseLong(timeStr);
        int dayCount = getGapCount(millis);
        //获取当前时间
        int todayCount = getTodayCount();
        //TODO 测试
        if(dayCount <= 0) return todayCount <4;
        //正式逻辑
        if (dayCount >= 1
                || dayCount < 4) {
            //
            return todayCount < 4;
        } else if (dayCount >= 4
                || dayCount < 7) {
            //
            return todayCount < 3;
        } else if (dayCount >= 7
                || dayCount < 14) {
            //
            return todayCount < 2;
        } else if (dayCount >= 14) {
            //
            return todayCount < 1;
        }
        return false;
    }

    /** 获取当天日期 */
    private String getCurrentDate() {
        long currentTimeMillis = System.currentTimeMillis();
        return Tools.timeFormat(currentTimeMillis, "yyyyMMdd");
    }

    /** 获取当天次数 */
    private int getTodayCount() {
        int todayCount = 0;
        String date = FileManager.loadShared(getApplicationContext(), FileManager.xmlFile_wake, "date").toString();
        if (getCurrentDate().equals(date)) {
            String countStr = FileManager.loadShared(getApplicationContext(), FileManager.xmlFile_wake, "count").toString();
            todayCount = TextUtils.isEmpty(countStr) ? 0 : Integer.parseInt(countStr);
        }else{
            FileManager.saveShared(getApplicationContext(), FileManager.xmlFile_wake, "date", getCurrentDate());
            FileManager.saveShared(getApplicationContext(), FileManager.xmlFile_wake, "count", "0");
        }
        return todayCount;
    }

    /**
     * 计算第一次启动时间与当前时间差
     *
     * @param startMillis
     *
     * @return
     */
    private static int getGapCount(long startMillis) {
        java.util.Date startDate = new java.util.Date(startMillis);
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        java.util.Date endDate = new java.util.Date(System.currentTimeMillis());
        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * 存储第一次启动时间
     */
    private String getAppFirstStartTime() {
        String timeStr = FileManager.loadShared(getApplicationContext(), FileManager.xmlFile_appInfo, FileManager.xmlKey_firstStart_v2).toString();
        if (TextUtils.isEmpty(timeStr)) {
            timeStr = String.valueOf(System.currentTimeMillis());
            FileManager.saveShared(getApplicationContext(), FileManager.xmlFile_appInfo, FileManager.xmlKey_firstStart_v2, timeStr);
        }
        return timeStr;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        screenObserver.shutdownObserver();
    }
}