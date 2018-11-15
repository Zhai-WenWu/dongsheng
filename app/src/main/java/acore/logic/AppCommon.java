package acore.logic;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.download.container.DownloadCallBack;
import com.download.down.DownLoad;
import com.download.tools.FileUtils;
import com.xiangha.R;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.XHApplication;
import acore.override.activity.base.WebActivity;
import acore.override.activity.mian.MainBaseActivity;
import acore.override.helper.XHActivityManager;
import acore.tools.Base64Utils;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.health.activity.HealthTest;
import amodule.health.activity.MyPhysique;
import amodule.main.Main;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.FullScreenWeb;
import aplug.web.ShowWeb;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;
import com.xh.windowview.BottomDialog;

import static acore.logic.ConfigMannager.KEY_RANDPROMOTIONNEW;
import static acore.logic.stat.StatConf.STAT_TAG;
import static xh.basic.tool.UtilFile.readFile;
import static xh.basic.tool.UtilFile.readFileBuffer;
import static xh.basic.tool.UtilString.getListMapByJson;
public class AppCommon {

    public static int buyBurdenNum = 0; // 离线清单条数
    public static int follwersNum = -1; // 关注人数

    public static int nextDownDish = -1;
    public static int maxDownDish = 10000;

    public static final String XH_PROTOCOL = "xiangha://welcome?";
    public static void openUrl( String url, Boolean openThis) {
        if(XHActivityManager.getInstance().getCurrentActivity()!=null)
            openUrl(XHActivityManager.getInstance().getCurrentActivity(),url,openThis);
    }
    /**
     * 打开url 如果url能打开原生页面就开原生
     * 如果不能就开webview，如果openThis为true,或已打开的webview太多，则直接使用WebView打开Url
     *
     * @param act
     * @param url
     * @param openThis
     */
    public static void openUrl(final Activity act, String url, Boolean openThis) {
        Log.d("tzy","openUrl::url = " + url);
        url = handleUrl(url);
        if (TextUtils.isEmpty(url) || act == null)
            return;
        Bundle bundle = new Bundle();
        Intent intent = null;
        try {
            // 开启url，同时识别是否是原生的
            bundle.putString("url", url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //下载apk---直接中断
        if (url.contains("download.app")) {
            String temp = url.substring(url.indexOf("?") + 1, url.length());
            LinkedHashMap<String, String> map_link = UtilString.getMapByString(temp, "&", "=");
            String downUrl = Uri.decode(map_link.get("url"));
            String appName = Uri.decode(map_link.get("appname"));
            boolean showProgressDialog = "2".equals(map_link.get("showDialog"));
            try {
                final DownLoad downLoad = new DownLoad(act);
                downLoad.setActionStr("下载");
                downLoad.setShowProgressDialog(showProgressDialog);
                downLoad.setDownLoadTip("开始下载", appName + ".apk", "正在下载", R.drawable.ic_launcher, false);
                downLoad.starDownLoad(downUrl, FileManager.getSDCacheDir(), appName, true, new DownloadCallBack() {
                    @Override
                    public void starDown() {
                        super.starDown();
                        Tools.showToast(XHApplication.in(), "开始下载");
                    }

                    @Override
                    public void downOk(Uri uri) {
                        super.downOk(uri);
                        FileUtils.install(XHApplication.in(), uri);
                        downLoad.cacelNotification();
                    }

                    @Override
                    public void downError(String s) {
                        Tools.showToast(XHApplication.in(), "下载失败：" + s);
                        downLoad.cacelNotification();
                    }
                });
            } catch (Exception e) {
                Tools.showToast(XHApplication.in(), "下载异常");
                e.printStackTrace();
            }
            return;
        } else if (url.contains("MyRebate.app")//我的返现页面
                || url.contains("GoodsList.app")//返现商品列表
                ) {
            return;
        } else if (url.indexOf("link.app") == 0) {//外链
            String temp = url.substring(url.indexOf("?") + 1, url.length());
            LinkedHashMap<String, String> map_link = UtilString.getMapByString(temp, "&", "=");
            String openUrl = Uri.decode(map_link.get("url"));
            Intent intentLink = new Intent();
            intentLink.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(openUrl);
            intentLink.setData(content_url);
            try{
                act.startActivity(intentLink);
            }catch (Exception ignored){}
            return;

        } else if (url.indexOf("nativeWeb.app") == 0) {//外链 或者是打开某个app
            String temp = url.substring(url.indexOf("?") + 1, url.length());
            LinkedHashMap<String, String> map_link = UtilString.getMapByString(temp, "&", "=");
            // other app---协议链接
            String protocolUrl = map_link.get("protocolurl");
            // browser---浏览器链接
            String browserUrl = map_link.get("browserurl");
            String packageName = map_link.get("package");
            //
            protocolUrl=Uri.decode(protocolUrl);
            browserUrl=Uri.decode(browserUrl);
            packageName=Uri.decode(packageName);

            Intent intentLink = new Intent();
            intentLink.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(protocolUrl);
            //不为 null 且未安装app
            if (!TextUtils.isEmpty(packageName)
                    && ToolsDevice.isAppInPhone(XHApplication.in(),packageName) == 0){
                content_url = Uri.parse(browserUrl);
            }
            intentLink.setData(content_url);
            try{
                act.startActivity(intentLink);
            }catch (Exception ignored){}
            return;

        }

        //解析生成 intent
        intent = parseURL(XHApplication.in(), bundle, url);
        LogManager.print(XHConf.log_tag_net, "d", "------------------解析网页url------------------\n" + url);
        if (intent == null) {
            bundle.putString("url", StringManager.replaceUrl(url));
            if (Main.colse_level <= 2) {
                if (!(act instanceof MainBaseActivity))
                    act.finish();
                return;
            }
            if (url.contains("fullScreen=2")) {//兼容老版本开启 FullScreenWeb
                intent = new Intent(act, FullScreenWeb.class);
                intent.putExtra("url", StringManager.replaceUrl(url));
            } else if (act instanceof WebActivity) {
                final WebActivity allAct = (WebActivity) act;
                boolean isSelfLoad = allAct.selfLoadUrl(url, openThis);
                if (!isSelfLoad && !url.contains(".app")) {
                    intent = new Intent(act, ShowWeb.class);
                    intent.putExtras(bundle);
                }
            } else if (!url.contains(".app") || url.indexOf("aboutus") == 0) {
                intent = new Intent(act, ShowWeb.class);
                intent.putExtras(bundle);
            }
        } else if (url.contains("nousInfo")) {
            String code = intent.getStringExtra("code");
            AppCommon.openUrl(act, StringManager.api_nouseInfo + code, true);
            intent = null;
        }
        //必须这样再判断，避免给的url是原生，但parseURL方法中没解析
        if (intent != null) {
            act.startActivity(intent);
        }
    }

    private static String handleUrl(String url) {
        //url为null直接不处理
        if (TextUtils.isEmpty(url)) return null;
        if (!url.startsWith(XH_PROTOCOL) && !url.startsWith("http")
                && (!url.contains(".app") && !url.contains("circleHome"))
                ) return null;

        // 如果识别到外部开启链接，则解析
        if (url.startsWith(XH_PROTOCOL) && url.length() > XH_PROTOCOL.length()) {
            String tmpUrl = url.substring(XH_PROTOCOL.length());
//            tmpUrl = Uri.decode(tmpUrl);
            if (tmpUrl.startsWith("url=")) {
                tmpUrl = tmpUrl.substring("url=".length());
            }
            if (TextUtils.isEmpty(tmpUrl)) {
                url = StringManager.wwwUrl;
            } else {
                url = tmpUrl;
            }
        }

        //按#分割，urls【1】是表示外部吊起的平台例如360
        if (url.contains("#")) {
            String[] urls = url.split("#");
            if (!TextUtils.isEmpty(url) && urls.length > 1) {
                //不会有.app了，变成包名加类名啦
                int indexs = url.indexOf(".app");
                String data = url.substring(0, indexs + (".app".length()));
                XHClick.mapStat(XHApplication.in(), "a_from_other", urls[1], data);
            }
            if (!TextUtils.isEmpty(urls[0]))
                url = urls[0];
        }
        return url;
    }

    public static Intent parseURL(Context context, String url) {
        return parseURL(context, new Bundle(), handleUrl(url));
    }

    /**
     * 解析url是否为原生页面
     *
     * @param act
     * @param bundle
     * @param url
     * @return
     */
    public static Intent parseURL(Context act, Bundle bundle, String url) {
        if (url.contains("stat=1")) {
            //服务端做统计用的
            ReqInternet.in().doGet(StringManager.api_setAppUrl + "?url=" + url, new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    LogManager.print("d", "res=" + flag + "----data=" + returnObj.toString());
                }
            });
        }
        Intent intent = null;
        //特殊处理体质
        if (url.contains("tizhitest.app")) {
            String result = isHealthTest();
            if (result.equals("")) {
                intent = new Intent(act, HealthTest.class);
            } else {
                intent = new Intent(act, MyPhysique.class);
                bundle.putString("params", result);
                intent.putExtras(bundle);
            }
            return intent;
        }
        //开浏览器
        if (url.contains("internet.app")) {
            String[] urls = url.split("=");
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[1]));
            return intent;
        }
        if (url.contains("ingreInfo.app?type=tizhi") || url.contains("ingreInfo.app?type=jieqi")) {
            url = url.replace("ingreInfo.app", "jiankang.app");
        }
        //常规解析
        String newUrl = old2new(act, url);
        try {
            String[] urls = newUrl.split("\\?");
            if (urls.length > 0) {
//                String urlTemp="amodule.dish.activity.DetailDish".equals(urls[0])?"amodule.dish.activity.DishTestActivity":urls[0];
                final Class<?> c = Class.forName(urls[0]);
                if (urls[0].contains("amodule.main.activity.")
                        || urls[0].contains("amodule.user.activity.MyFavorite")
                        || urls[0].contains("amodule.lesson.activity.LessonHome")
                        ) {
                    Main.colse_level = 2;
                    if (Main.allMain != null) {
                        Main.allMain.setCurrentTabByClass(c);
                    }
                    return intent;
                }
                if (urls.length > 1) {
                    bundle = new Bundle();
                    //web要的不是参数，是url链接，故这样处理，但此版要兼容老版，故会在老版处理，此新版不用单独处理
                    if (urls.length == 3) {
                        urls[1] = urls[1] + "?" + urls[2];
                        String key = urls[1].substring(0, urls[1].indexOf("="));
                        String value = urls[1].substring(urls[1].indexOf("=") + 1, urls[1].length());
                        bundle.putString(Uri.decode(key), Uri.decode(value));
                    } else {
                        String[] parameter = urls[1].split("&");

                        for (String p : parameter) {
                            String[] value = p.split("=");
                            if (value.length == 2) {
                                bundle.putString(URLDecoder.decode(value[0], "utf-8"), URLDecoder.decode(value[1], "utf-8"));
                            }
                        }
                    }
                }
                if (url.contains("MyDishNew.app") || url.contains("MySubject.app")) {
                    if (LoginManager.isLogin()) {
                        bundle = new Bundle();
                        bundle.putString("code", LoginManager.userInfo.get("code"));
                    } else {
                        Intent it = new Intent(act, LoginByAccout.class);
                        return it;
                    }
                    if (url.contains("MyDishNew.app")) {
                        bundle.putInt("index", 1);
                    } else {
                        bundle.putInt("index", 0);
                    }
                }
                intent = new Intent(act, c);
                if (bundle != null)
                    intent.putExtras(bundle);
                return intent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intent;
    }

    private static String old2new(Context context, String oldUrl) {
        String newUrl = "";
        String[] urls = oldUrl.split("\\?");
        Map<String, String> urlRule = geturlRule(context);
        String urlKey = urls[0];
        if (urls[0].lastIndexOf("/") >= 0) {
            urlKey = urls[0].substring(urls[0].lastIndexOf("/") + 1);
        }
        if (urlRule == null || urlRule.get(urlKey) == null) {
//			if(!oldUrl.contains(".app")){
//				return "aplug.web.ShowWeb?url=" + oldUrl;
//			}
            return oldUrl;
        }
        newUrl = oldUrl.replace(urls[0], urlRule.get(urlKey));
        return newUrl;
    }

    /**
     * 存储App数据
     * 菜谱分类、
     */
    public synchronized static void saveAppData() {
        final String appDataPath = FileManager.getDataDir() + FileManager.file_appData;
        if (FileManager.ifFileModifyByCompletePath(appDataPath, 6 * 60) == null) {
            ReqInternet.in().doGet(StringManager.api_appData + "?type=newData", new InternetCallback() {
                @Override
                public void loaded(int flag, String url, final Object returnObj) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        saveAppDataToFile(returnObj.toString());
                    } else {
                        String json = FileManager.getFromAssets(XHActivityManager.getInstance().getCurrentActivity(), FileManager.file_appData);
                        saveAppDataToFile(json);
                    }
                }

                private void saveAppDataToFile(final String json) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            FileManager.saveFileToCompletePath(appDataPath, json, false);
                        }
                    }.start();
                }
            });
        }
    }
    /**
     * 获取appData
     *
     * @param context
     * @param key     为null则返回整个数据，否则返回key所对应的数据
     * @return
     */
    public static String getAppData(Context context, String key) {
        String jsonStr = "";
        final String appDataPath = FileManager.getDataDir() + FileManager.file_appData;
        String appDataStr = readFileBuffer(appDataPath);
        List<Map<String, String>> dataArray = getListMapByJson(appDataStr);
        if (dataArray == null || dataArray.size() == 0) {
            appDataStr = FileManager.getFromAssets(context, FileManager.file_appData);
            dataArray = getListMapByJson(appDataStr);
        }
        if (TextUtils.isEmpty(key)) {
            return appDataStr;
        }
        if (dataArray.size() > 0 && dataArray.get(0).containsKey(key)) {
            jsonStr = dataArray.get(0).get(key);
        }
        return jsonStr;
    }

    /**
     * 关注请求
     *
     * @param code
     */
    public static void onAttentionClick(String code, final String type) {
        onAttentionClick(code, type, null);
    }

    /**
     * 关注请求
     *
     * @param code
     */
    public static void onAttentionClick(String code, final String type, final Runnable succRun) {
        if (code != null) {
            ReqInternet.in().doPost(StringManager.api_setUserData, "type=" + type + "&p1=" + code.toString(),
                    new InternetCallback() {
                        @Override
                        public void loaded(int flag, String url, Object returnObj) {
                            if (flag >= ReqInternet.REQ_OK_STRING) {
                                LoginManager.modifyUserInfo(XHApplication.in(), "followNum", returnObj.toString());
                                AppCommon.follwersNum = Integer.valueOf(returnObj.toString());
                                if (succRun != null)
                                    succRun.run();
                                //关注监听回调
                                ObserverManager.getInstance().notify(ObserverManager.NOTIFY_FOLLOW,null,false);
                            }
                        }
                    });
        }
    }

    /**
     * @param lv        等级
     * @param imageView 等级图片
     */
    public static boolean setLvImage(int lv, ImageView imageView) {
        int[] lv_img_id = {R.drawable.z_z_ico_level_01, R.drawable.z_z_ico_level_02, R.drawable.z_z_ico_level_03,
                R.drawable.z_z_ico_level_04, R.drawable.z_z_ico_level_05, R.drawable.z_z_ico_level_06,
                R.drawable.z_z_ico_level_07, R.drawable.z_z_ico_level_08, R.drawable.z_z_ico_level_09,
                R.drawable.z_z_ico_level_10, R.drawable.z_z_ico_level_11, R.drawable.z_z_ico_level_12,
                R.drawable.z_z_ico_level_13, R.drawable.z_z_ico_level_14, R.drawable.z_z_ico_level_15,
                R.drawable.z_z_ico_level_16, R.drawable.z_z_ico_level_17, R.drawable.z_z_ico_level_18};
        if (lv == 0) {
            imageView.setVisibility(View.GONE);
            return false;
        } else {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(lv_img_id[lv - 1]);
            return true;
        }
    }
    /**
     * @param imageView
     */
    public static boolean setUserTypeImage(int isGourmet, ImageView imageView) {
        int[] lv_img_id = {R.drawable.z_user_gourmet_ico};
        if (isGourmet == 2) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setBackgroundResource(lv_img_id[0]);
            return true;
        } else {
            imageView.setVisibility(View.GONE);
            return false;
        }
    }

    // 适配ListView的滑动
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void scorllToIndex(ListView listView, int index) {
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 11) {
            int lastVisible = listView.getLastVisiblePosition();
            if (index > lastVisible) {
                listView.setSelection(index);
            } else
                listView.smoothScrollToPositionFromTop(index, 0);
        } else if (version < 11 && version >= 8) {
            int firstVisible = listView.getFirstVisiblePosition();
            int lastVisible = listView.getLastVisiblePosition();
            if (index < firstVisible)
                listView.smoothScrollToPosition(index);
            else
                listView.smoothScrollToPosition(index + lastVisible - firstVisible - 2);
        }
    }

    /**
     * 清理数据缓存
     */
    public static void clearCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 清除部分缓存
                FileManager.delDirectoryOrFile(FileManager.getSDDir() + FileManager.save_cache, 150);
            }
        }).start();
    }

    public static boolean getTodayTastHintIsShow(Context con) {
        boolean isShowTaskInfo = false;
        if (UtilFile.loadShared(con, "score_store", "user_task") == "") {
            isShowTaskInfo = true;
        } else {
            int year = Tools.getDate("year");
            int month = Tools.getDate("month");
            int date = Tools.getDate("date");
            String user_taks = (String) UtilFile.loadShared(con, "score_store", "user_task");
            String[] user_taks_data = user_taks.split("_");
            if (user_taks_data.length == 3) {
                if (Integer.parseInt(user_taks_data[0]) < year) {
                    isShowTaskInfo = true;
                } else if (Integer.parseInt(user_taks_data[1]) < month) {
                    isShowTaskInfo = true;
                } else if (Integer.parseInt(user_taks_data[2]) < date) {
                    isShowTaskInfo = true;
                }
            }
        }
        return isShowTaskInfo;
    }

    /**
     * 判断是否有体质测试结果
     */
    public static String isHealthTest() {
        if (LoginManager.isLogin()) {// 是否有用户
            if (LoginManager.userInfo.containsKey("crowd") && LoginManager.userInfo.get("crowd") != null)
                return LoginManager.userInfo.get("crowd");
        } else {
            if (UtilFile.ifFileModifyByCompletePath(UtilFile.getDataDir() + FileManager.file_healthResult, -1) != null) {// 本地是否有测试结果
                return readFile(UtilFile.getDataDir() + FileManager.file_healthResult).trim();
            }
        }
        return "";
    }

    public synchronized static void saveUrlRuleFile(Context context) {
        final String urlRulePath = FileManager.getDataDir() + FileManager.file_urlRule;
//        final String urlRulePath = FileManager.getSDDir() + FileManager.file_urlRule;
        //方便测试
//		if(FileManager.ifFileModifyByCompletePath(urlRulePath, -1) == null){
//			String urlRuleData = FileManager.getFromAssets(context, FileManager.file_urlRule);
//			FileManager.saveFileToCompletePath(urlRulePath, urlRuleData, false);
//		}
//		if(FileManager.ifFileModifyByCompletePath(urlRulePath, 6 * 60) == null){
        String uptime = "";
        String urlRuleJson = readFile(urlRulePath);
        if (!TextUtils.isEmpty(urlRuleJson)) {
            List<Map<String, String>> data = getListMapByJson(urlRuleJson);
            if (data.size() > 0) {
                uptime = data.get(0).get("uptime");
            }
        }
        String url = StringManager.api_getWebRule;
        String params = TextUtils.isEmpty(uptime) ? "" : "?uptime=" + uptime;
        ReqInternet.in().doGet(url + params, new InternetCallback() {

            @Override
            public void loaded(int flag, String url, final Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (msg != null && !TextUtils.isEmpty(msg.toString())) {
                        List<Map<String, String>> returnData = getListMapByJson(msg);
                        if (returnData.size() > 0) {
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    FileManager.delDirectoryOrFile(urlRulePath);
                                    FileManager.saveFileToCompletePath(urlRulePath, msg.toString(), false);
                                    if(urlRuleMap != null)
                                        urlRuleMap.clear();
                                }
                            }.start();
                        }
                    }
                }
            }
        });
//		}
    }

    private static Map<String, String> urlRuleMap = null;

    public static Map<String, String> geturlRule(Context context) {
        if (urlRuleMap == null || urlRuleMap.isEmpty()) {
            final String urlRulePath = FileManager.getDataDir() + FileManager.file_urlRule;
            String urlRuleJson = readFile(urlRulePath);
            if (TextUtils.isEmpty(urlRuleJson)) {
                urlRuleJson = FileManager.getFromAssets(context, FileManager.file_urlRule);
            }
            Map<String, String> data = StringManager.getFirstMap(urlRuleJson);
            urlRuleMap = StringManager.getFirstMap(data.get("data"));
        }
        return urlRuleMap;
    }

    /**
     * 保存所有圈子的静态数据
     * 每次开启应用都会请求
     */
    public static void saveCircleStaticData(final Context context) {
        final String allCircleJsonPath = FileManager.getDataDir() + FileManager.file_allCircle;
        final String allCircleJson = readFile(allCircleJsonPath);
        if (TextUtils.isEmpty(allCircleJson)) {
            CircleSqlite circleSqlite = new CircleSqlite(context);
            String allCircleJsonByAssets = FileManager.getFromAssets(context, FileManager.file_allCircle);
            saveCircleData(allCircleJsonPath, allCircleJsonByAssets, circleSqlite);
        }
        ReqInternet.in().doGet(StringManager.api_circleStaticData, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                //成功
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    //与本地数据对比，相同则return
                    if (allCircleJson.equals(msg)) {
                        return;
                    }
                    //删除数据
                    CircleSqlite circleSqlite = new CircleSqlite(context);
                    circleSqlite.deleteAll();
                    //保存所有数据
                    saveCircleData(allCircleJsonPath, msg, circleSqlite);
                    //失败，判断本地保存数据是否为空，若为空则获取assets文件保存到本地
                } else {

                }
            }
        });
    }

    private static void saveCircleData(final String allCircleJsonPath, Object msg, CircleSqlite circleSqlite) {
        //保存数据
        FileManager.saveFileToCompletePath(allCircleJsonPath, msg.toString(), false);
        List<Map<String, String>> array = getListMapByJson(msg);
        Map<String, String> map = null;
        if (array.size() > 0) {
            map = array.get(0);
            array = getListMapByJson(map.get("allQuan"));
            //数据库存储
            for (Map<String, String> mapCircle : array) {
                CircleData circleData = new CircleData();
                circleData.setCid(mapCircle.get("cid"));
                circleData.setName(mapCircle.get("name"));
                circleData.setRule(mapCircle.get("rule"));
                circleData.setSkip(mapCircle.get("skip"));
                circleData.setInfo(mapCircle.get("info"));
                circleData.setImg(mapCircle.get("img"));
                circleData.setCustomerNum(mapCircle.get("customerNum"));
                circleData.setDayHotNum(mapCircle.get("dayHotNum"));
                circleSqlite.insert(circleData);
            }
            //移除多余数据
            map.remove("allQuan");
            //转换成json存储
            String jsonStr = Tools.map2Json(map);
            String filePath = FileManager.getDataDir() + FileManager.file_indexModuleAndRecCircle;
            FileManager.saveFileToCompletePath(filePath, jsonStr, false);
        }
    }

    public static Map<String, Integer> createCount = new HashMap<>();


    public static boolean setVip(final Activity act, ImageView vipView, String data, VipFrom vipFrom) {
        return setVip(act, vipView, data, "", "", vipFrom);
    }

    public static boolean setVip(final Activity act, ImageView vipView, String data, VipFrom vipFrom, View.OnClickListener listener) {
        return setVip(act, vipView, data, "", "", "", vipFrom, listener);
    }

    public static boolean setVip(final Activity act, ImageView vipView, String data, final String eventId, final String twoLevel, VipFrom vipFrom) {
        return setVip(act, vipView, data, eventId, twoLevel, "", vipFrom, null);
    }

    public static boolean setVip(final Activity act, ImageView vipView, final String data, final String eventId, final String twoLevel, final String threadLevel, final VipFrom vipFrom, final View.OnClickListener listener) {
        boolean isVip = isVip(data);
        if (isVip) {
            vipView.setVisibility(View.VISIBLE);
            vipView.setImageResource(R.drawable.i_user_home_vip);
        } else {
            vipView.setVisibility(View.GONE);
        }
        vipView.setTag(STAT_TAG,"VIP按钮");
        vipView.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                if (!TextUtils.isEmpty(eventId))
                    XHClick.mapStat(act, eventId, twoLevel, TextUtils.isEmpty(threadLevel) ? "会员皇冠" : threadLevel);
                if (listener != null) listener.onClick(v);
                String from = "";
                if (vipFrom != null) {
                    switch (vipFrom) {
                        case COMMENT:
                            from = "用户评论皇冠按钮";
                            break;
                        case FRIEND_HOME:
                            from = "个人主页皇冠按钮";
                            break;
                        case MY_SELF:
                            from = "我的页面皇冠按钮";
                            break;
                        case POST_DETAIL:
                            from = "美食帖详情皇冠按钮";
                            break;
                        case POST_LIST:
                            from = "美食帖列表皇冠按钮";
                            break;
                    }
                }
                AppCommon.openUrl(act, StringManager.getVipUrl(false) + (TextUtils.isEmpty(from) ? "" : ("&vipFrom=" + from)), true);
            }
        });
        return isVip;
    }

    public static boolean isVip(String data) {
        boolean isVip = false;

        if (TextUtils.isEmpty(data))
            return false;
        if ("2".equals(data)) {
            isVip = true;
        } else {
            ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(data);
            if (arrayList.size() > 0) {
                Map<String, String> map = arrayList.get(0);
                if ("2".equals(map.get("isVip"))) {
                    isVip = true;
                }
            }
        }
        return isVip;
    }

    public static void setAdHintClick(final Activity act, View adHintView, final XHAllAdControl xhAllAdControl, final int index, final String listIndex) {
        setAdHintClick(act, adHintView, xhAllAdControl, index, listIndex, "", "");
    }

    public static void onAdHintClick(final Activity act, final XHAllAdControl xhAllAdControl, final int index, final String listIndex) {
        onAdHintClick(act, xhAllAdControl, index, listIndex, "", "");
    }

    public static void setAdHintClick(final Activity act, View adHintView, final XHAllAdControl xhAllAdControl, final int index, final String listIndex,
                                      final String eventID, final String twoLevel) {
        adHintView.setTag(STAT_TAG,"广告标签");
        adHintView.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                onAdHintClick(act, xhAllAdControl, index, listIndex, eventID, twoLevel);
            }
        });
    }

    public static void onAdHintClick(final Activity act, final XHAllAdControl xhAllAdControl, final int index, final String listIndex, final String eventID, final String twoLevel) {
        final BottomDialog bottomDialog = new BottomDialog(act);
        bottomDialog.setTopButton("赞助商提供的广告信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!TextUtils.isEmpty(eventID)) XHClick.mapStat(act,eventID,twoLevel,"点击赞助商提供广告");
                if (xhAllAdControl != null) xhAllAdControl.onAdClick(index, listIndex);
                bottomDialog.cancel();
            }
        }).setBottomButton("会员全站去广告", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(eventID))
                    XHClick.mapStat(act, eventID, twoLevel, "点击【会员全站去广告】按钮");
                if (LoginManager.isLogin()) {
                    AppCommon.openUrl(act, StringManager.getVipUrl(true) + "&vipFrom=会员全站去广告", true);
                } else {
                    AppCommon.openUrl(act, StringManager.getVipUrl(false) + "&vipFrom=会员全站去广告", true);
                }
                bottomDialog.cancel();
            }
        }).setBottomButtonColor("#59bdff").show();
    }
    /**
     * 保存随机推广
     *
     * @param context 上下文
     */
    public static void saveRandPromotionData(final Context context) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Map<String,String> randprotionMap =  StringManager.getFirstMap(ConfigMannager.getConfigByLocal(KEY_RANDPROMOTIONNEW));
                String url = randprotionMap.get("url");
                final ArrayList<Map<String,String>> replaceArr = StringManager.getListMapByJson(randprotionMap.get("replaceArray"));
                if(TextUtils.isEmpty(url)){
                    return;
                }
                url = url + "?rand=" + Math.abs(new Random().nextInt());
                ReqEncyptInternet.in().doEncypt(url, "",
                        new InternetCallback() {
                            @Override
                            public void loaded(int flag, String url, final Object msg) {
                                try {
                                    if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                                        String dataStr = msg.toString();
                                        if(!TextUtils.isEmpty(dataStr)){
                                            for(Map<String,String> replaceMap : replaceArr){
                                                String replaceValue = replaceMap.get("");
                                                if(!TextUtils.isEmpty(replaceValue)){
                                                    dataStr = dataStr.replace(replaceValue,"");
                                                }
                                            }
                                            byte[] dataByte = Base64Utils.decode(dataStr);
                                            dataStr = new String(dataByte);
                                            ArrayList<Map<String,String>> dataArr = StringManager.getListMapByJson(dataStr);
                                            int totalWeight=0;
                                            String text = "";
                                            for (Map<String,String> dict: dataArr)
                                            {
                                                totalWeight += Integer.parseInt(dict.get("weight")); //[dict["weight"] intValue];
                                            }
                                            if (totalWeight < 1) {
                                                //清空之前的数据
                                                FileManager.scynSaveFile(FileManager.getDataDir() + FileManager.file_randPromotionConfig, text, false);
                                                return ;
                                            }
                                            java.util.Random r = new java.util.Random();
                                            int randomWeight = Math.abs(r.nextInt()) % totalWeight;
                                            int tempWeight = 0;
                                            for (Map<String,String> dict : dataArr)
                                            {
                                                int weight = Integer.parseInt(dict.get("weight"));
                                                if (randomWeight < tempWeight + weight) {
                                                    text =  dict.get("sign") + dict.get("text") + dict.get("sign");
                                                    break;
                                                }
                                                tempWeight += weight;
                                            }
                                            FileManager.scynSaveFile(FileManager.getDataDir() + FileManager.file_randPromotionConfig, text, false);
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
            }
        }, 5 * 1000);//延时5s
    }

    /**
     * 获取随机推广数据
     *
     * @return 随机推广数据
     */
    public static String loadRandPromotionData() {
        return FileManager.readFile(FileManager.getDataDir() + FileManager.file_randPromotionConfig);
    }

    public enum VipFrom {
        MY_SELF,//"我的页面皇冠按钮"
        FRIEND_HOME,//"个人主页皇冠按钮"
        COMMENT,//"用户评论皇冠按钮"
        POST_DETAIL,//"美食帖详情皇冠按钮"
        POST_LIST;//"美食帖列表皇冠按钮"
    }


}
