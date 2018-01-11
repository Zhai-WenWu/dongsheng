package acore.logic;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.xiangha.Welcome;

import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import amodule.main.activity.MainHome;
import amodule.other.listener.HomeKeyListener;
import amodule.other.listener.HomeKeyListener.OnHomePressedListener;
import aplug.basic.ReqEncyptInternet;
import third.ad.tools.AdConfigTools;
import third.ad.tools.WelcomeAdTools;
import third.mall.aplug.MallCommon;
import xh.basic.tool.UtilFile;

import static amodule.main.Main.colse_level;

public class ActivityMethodManager {
    private Activity mAct;
    //监听home键的
    private HomeKeyListener mHomeWatcher;

    public ActivityMethodManager(Activity mAct) {
        this.mAct = mAct;
        //用于标记umeng推送记录数据
        PushAgent.getInstance(mAct).onAppStart();
        /*随机广告*/
        randPromotion();
    }

    public void onResume(int level) {
        //Log.i("FRJ", "level:" + level);
        //Log.i("FRJ", "colse_level:" + colse_level);
        MobclickAgent.onResume(mAct);
        StatService.onResume(mAct);//mta腾讯统计
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
                if (时间差值 >= MIN){
                        //&& 时间差值 <= MAX) {
                    //获取已启动次数
                    String currentCountStr = FileManager.loadShared(mAct, FileManager.xmlFile_appInfo, "splashOpenSecond").toString();
                    int currentCount = 0;
                    if (!TextUtils.isEmpty(currentCountStr)) {
                        currentCount = Integer.parseInt(currentCountStr);
                    }
                    Log.i("tzy","已启动次数 ： " + currentCount);
                    final int showCount = WelcomeAdTools.getInstance().getShownum();
                    Log.i("tzy","启动MAX次数 ： " + showCount);
//                    if (0 >= showCount || currentCount <= showCount) {
                        int adShowTime = WelcomeAdTools.getInstance().getDuretimes();
                        if(!Main.isShowWelcomeDialog) {
//                            new WelcomeDialog(mAct, adShowTime).show();
                            Log.i("zhangyujian","二次开屏");
                            //请求广告位
                            AdConfigTools.getInstance().getAdConfigInfo();
                            mAct.startActivity(new Intent(mAct, Welcome.class));
                        }
                        //更新开启次数
                        FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, "splashOpenSecond", String.valueOf(++currentCount));
//                    }
                }
            }
        }

        registerHomeListener();
        // 控制页面关闭
        if (colse_level <= level) {
            if (level == 1 && colse_level != 0) {
                if (Main.allMain != null) {
                    Main.allMain.setCurrentTabByClass(MainHome.class);
                }
                colse_level = 1000;
            } else {
                mAct.finish();
            }
        }
        //电商是3，登录界面是4，其他页面是5，加次判断是为了解决从首页发视频菜谱，跳到上传列表，通过colseLevel关闭发视频菜谱页面，饼跳到菜谱列表页面
        else if (colse_level != 6 || level < 4) {
            colse_level = 1000;
        }

    }

    public void onPause() {
        MobclickAgent.onPause(mAct);
        StatService.onPause(mAct);//mta腾讯统计
        XHClick.getStopTime(mAct);
        XHClick.sendBrowseCodes(mAct);
        if (mHomeWatcher != null)
            mHomeWatcher.stopWatch();
    }

    public void onStop() {
    }

    public void onDestroy() {
        //清除还没有请求的接口
        ReqEncyptInternet.in().clearListIntenert();
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
                    Log.i("zhangyujian","HomeKeyListener111");
                    if (WelcomeAdTools.getInstance().isOpenSecond()) {
                    Log.i("zhangyujian","onHomePressed");
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

    /** 随机广告推广的问题 */
    private  void randPromotion() {
        //获取 Acitivty 完整路径名
        String classKey = mAct.getComponentName().getClassName();
        //获取 config 中的 randPromotion 数据
        Map<String,String> randProConfigMap = StringManager.getFirstMap(AppCommon.getConfigByLocal("randPromotion"));
        if(randProConfigMap.containsKey(classKey)
                && "2".equals(randProConfigMap.get(classKey))){
            //数据
            String text = AppCommon.loadRandPromotionData().trim();
            if(!TextUtils.isEmpty(text)){
                //写如剪切板
                Tools.inputToClipboard(mAct,text);
                Log.i("tzy","inputToClipboard :: text = " + text);
            }
        }
    }

    /** 网络方法上传单图 */
    public static final int UPLOAD_SINGLE = 1;
    /** 发菜谱上传图片 */
    public static final int UPLOAD_DISH = 2;
    /** 美食圈上传图片 */
    public static final int UPLOAD_QUAN = 3;

}
