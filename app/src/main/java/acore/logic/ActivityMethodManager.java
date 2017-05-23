package acore.logic;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.xiangha.R;

import java.util.Map;

import acore.tools.FileManager;
import acore.widget.XHADView;
import amodule.main.Main;
import amodule.main.activity.MainHomePageNew;
import amodule.main.view.WelcomeDialog;
import amodule.other.listener.HomeKeyListener;
import amodule.other.listener.HomeKeyListener.OnHomePressedListener;
import aplug.feedback.activity.Feedback;
import third.ad.tools.WelcomeAdTools;
import third.mall.aplug.MallCommon;
import xh.basic.tool.UtilFile;

import static amodule.main.Main.colse_level;

public class ActivityMethodManager {
    private Activity mAct;
    //监听home键的
    private HomeKeyListener mHomeWatcher;

    public ActivityMethodManager(Activity mAct) {
        XHADView adScrollView = XHADView.getInstence(mAct);
        if (adScrollView != null) {
            adScrollView.refreshContext(mAct);
        }
        this.mAct = mAct;
        //用于标记umeng推送记录数据
        PushAgent.getInstance(mAct).onAppStart();
    }

    public void onResume(int level) {
        Log.i("FRJ", "level:" + level);
        Log.i("FRJ", "colse_level:" + colse_level);
        XHADView adScrollView = XHADView.getInstence(mAct);
        if (adScrollView != null) {
            adScrollView.refreshContext(mAct);
        }
        MobclickAgent.onResume(mAct);
        XHClick.getStartTime(mAct);
        // 应用到后台时如果数据被清理，需要重新自动登录
        if (LoginManager.userInfo.size() == 0) {
            Map<String, String> userInfoMap = (Map<String, String>) UtilFile.loadShared(mAct, FileManager.xmlFile_userInfo, "");
            if (userInfoMap.get("userCode") != null && userInfoMap.get("userCode").length() > 1) {
                LoginManager.loginByAuto(mAct);
                MallCommon.getSaveMall(mAct);//处理电商
            }
        }

        //判断是否显示
        if (WelcomeAdTools.getInstance().isOpenSecond()) {
            String switchTimeStr = FileManager.loadShared(mAct, FileManager.xmlFile_appInfo, "switchTime").toString();
        Log.i("tzy","switchTimeStr");
            if (!TextUtils.isEmpty(switchTimeStr)) {
                //清空切换时间
                FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, "switchTime", "");

                long switchTime = Long.parseLong(switchTimeStr);
                long currentTime = System.currentTimeMillis();
                final long MIN = WelcomeAdTools.getInstance().getSplashmins() * 1000;
                final long MAX = WelcomeAdTools.getInstance().getSplashmaxs() * 1000;
                long 时间差值 = currentTime - switchTime;
                Log.i("tzy","" + 时间差值);
                if (时间差值 >= MIN && 时间差值 <= MAX) {
                    //获取已启动次数
                    String currentCountStr = FileManager.loadShared(mAct, FileManager.xmlFile_appInfo, "splashOpenSecond").toString();
                    int currentCount = 0;
                    if (!TextUtils.isEmpty(currentCountStr)) {
                        currentCount = Integer.parseInt(currentCountStr);
                    }
                    Log.i("tzy","已启动次数 ： " + currentCount);
                    final int showCount = WelcomeAdTools.getInstance().getShownum();
                    Log.i("tzy","启动MAX次数 ： " + showCount);
                    if (0 >= showCount || currentCount <= showCount) {
                        int adShowTime = WelcomeAdTools.getInstance().getDuretimes();
                        if(!Main.allMain.isShowWelcomeDialog)
                            new WelcomeDialog(mAct, adShowTime).show();
                        //更新开启次数
                        FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, "splashOpenSecond", String.valueOf(++currentCount));
                    }
                }
            }
        }

        registerHomeListener();
        // 控制页面关闭
        if (colse_level <= level) {
            if (level == 1 && colse_level != 0) {
                if (Main.allMain != null) {
                    Main.allMain.setCurrentTabByClass(MainHomePageNew.class);
                }
                colse_level = 1000;
            } else
                mAct.finish();
        }
        //电商是3，登陆界面是4，其他页面是5，加次判断是为了解决从首页发视频菜谱，跳到上传列表，通过colseLevel关闭发视频菜谱页面，饼跳到菜谱列表页面
        else if (colse_level != 6 || level < 4)
            colse_level = 1000;
    }

    public void onPause() {
        MobclickAgent.onPause(mAct);
        XHClick.getStopTime(mAct);
        XHClick.sendBrowseCodes(mAct);
        if (mHomeWatcher != null)
            mHomeWatcher.stopWatch();
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        mAct.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_feedback:
                Intent intent = new Intent(mAct, Feedback.class);
                mAct.startActivity(intent);
                break;
            case R.id.menu_exit:
                if (Main.allMain != null) {
                    colse_level = 1;
                    mAct.finish();
                    Main.allMain.setDoExit(1);
                    Main.allMain.doExit(mAct, false);
                }
                break;
        }
    }

    /**
     * 注册Home键的监听
     */
    private void registerHomeListener() {
        if (mHomeWatcher == null) {
            mHomeWatcher = new HomeKeyListener(mAct);
            mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    // 进行点击Home键的处理
                    XHClick.HomeKeyListener(mAct);

                    if (WelcomeAdTools.getInstance().isOpenSecond()) {
                    Log.i("tzy","onHomePressed");
                        FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, "switchTime", String.valueOf(System.currentTimeMillis()));
                        WelcomeAdTools.getInstance().handlerAdData(true);
                    }
                }

                // 进行长按Home键的处理
                @Override
                public void onHomeLongPressed() {
                }
            });
        }
        mHomeWatcher.startWatch();
    }

    /** 网络方法上传单图 */
    public static final int UPLOAD_SINGLE = 1;
    /** 发菜谱上传图片 */
    public static final int UPLOAD_DISH = 2;
    /** 美食圈上传图片 */
    public static final int UPLOAD_QUAN = 3;

}
