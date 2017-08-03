package amodule.main.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.dialogManager.VersionOp;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishListActivity;
import amodule.dish.db.DishOffData;
import amodule.dish.db.ShowBuySqlite;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.dish.tools.UploadDishControl;
import amodule.main.view.home.HomeToutiaoAdControl;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import amodule.search.db.MatchWordsDbUtil;
import aplug.service.alarm.PushAlarm;
import aplug.service.base.ServiceManager;
import third.ad.tools.AdConfigTools;
import third.ad.tools.TencenApiAdTools;
import third.mall.aplug.MallCommon;
import third.push.xg.XGLocalPushServer;
import xh.basic.tool.UtilFile;
import xh.windowview.XhDialog;

import static java.lang.System.currentTimeMillis;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * app初始化数据类---只在这里进行初始化
 */
public class MainInitDataControl {

    /**
     * welcome之前初始化
     */
    public void initWelcomeBefore(Context context){

    }
    /**
     * welcome oncreate初始化
     */
    public void initWelcomeOncreate(){
        Log.i("zhangyujian","initWelcomeOncreate");
        long startTime= System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CookieManager.getInstance().removeAllCookie();
                XHClick.saveFirstStartTime(XHApplication.in());
                XHClick.registerMonthSuperProperty(XHApplication.in());
            }
        }).start();
        long endTime=System.currentTimeMillis();
        Log.i("zhangyujian","initWelcomeOncreate::时间:"+(endTime-startTime));

    }
    /**
     * welcome之后初始化
     */
    public void initWelcomeAfter(final Activity activity){
        Log.i("zhangyujian","initWelcomeAfter");
        long startTime= System.currentTimeMillis();
        initWelcome(activity);
        new Thread() {
            @Override
            public void run() {
                super.run();
                MobclickAgent.setDebugMode(true);
                OnlineConfigAgent.getInstance().updateOnlineConfig(activity);


                //待处理问题。
                HomeToutiaoAdControl.getInstance().getAdData(activity);
                ToolsDevice.saveXhIMEI(activity);
                OffDishToFavoriteControl.offDishToFavorite(activity);
            }
        }.start();
        AdConfigTools.getInstance().setRequest(XHApplication.in());
        AppCommon.saveConfigData(XHApplication.in());
        long endTime2=System.currentTimeMillis();
        Log.i("zhangyujian","initWelcomeAfter::时间:"+(endTime2-startTime));

    }
    /**
     * Main之后初始化
     */
    public void initMainBefore(){

    }
    /**
     * Main oncreate初始化
     */
    public void initMainOncreate(){

    }
    /**
     * Main之后初始化
     */
    public void iniMainAfter(Activity act){
        Log.i("zhangyujian","iniMainAfter");
        long startTime= System.currentTimeMillis();
        ToolsDevice.sendCrashAndAppInfoToServer(act.getApplicationContext(), LoginManager.userInfo.get("code"));
        // 发送页面存活时间
        XHClick.sendLiveTime(act);
        //电商首页数据
        MallCommon.getDsInfo(act, null);

        //更新热词匹配数据库
        new MatchWordsDbUtil().checkUpdateMatchWordsDb(act);

        ServiceManager.startProtectService(act);

        AppCommon.saveUrlRuleFile(act);
        //请求本地推送data
        new XGLocalPushServer(act).getNousLocalPushData();

        //获取圈子静态数据
        AppCommon.saveCircleStaticData(act);

        AdConfigTools.getInstance().getAdConfigInfo();
        long endTime=System.currentTimeMillis();
        Log.i("zhangyujian","iniMainAfter::时间:"+(endTime-startTime));
    }

    /**
     * main在界面展示后初始化
     * @param act
     */
    public void initMainOnResume(final Activity act){
        Log.i("zhangyujian","initMainOnResume");
        long startTime= System.currentTimeMillis();
        //讯飞语音： 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(act, SpeechConstant.APPID +"=56ce9191");

        TencenApiAdTools.getTencenApiAdTools().getLocation();
        UploadDishSqlite sqlite = new UploadDishSqlite(act);
        final int draftId = sqlite.getFailNeedHintId();
        if (draftId > 0) {
            final XhDialog xhDialog = new XhDialog(act);
            xhDialog.setTitle("您的视频菜谱还未上传完毕，是否继续上传？")
                    .setCanselButton(" 取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            xhDialog.cancel();
                        }
                    }).setSureButton("去查看", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(act, UploadDishListActivity.class);
                    it.putExtra("draftId",draftId);
                    act.startActivity(it);
                    xhDialog.cancel();
                }
            }).show();
        }
        UploadDishControl.getInstance().updataAllUploadingDish(act.getApplicationContext());
        long endTime2=System.currentTimeMillis();
        Log.i("zhangyujian","initMainOnResume::时间::3::"+(endTime2-startTime));
    }

    /**
     * Welcome应用数据初始化
     */
    private void initWelcome(final Context context) {
        Log.i("zhangyujian","initWelcome");
        long startTime= System.currentTimeMillis();

        // 取消自我唤醒
        XGPushManager.clearLocalNotifications(context);
        PushAlarm.closeTimingWake(context);
        // 自动登录
        AppCommon.getCommonData(null);
        AppCommon.saveAppData();
        compatibleData(context);

        AppCommon.clearCache();

        long endTime4=System.currentTimeMillis();
        Log.i("zhangyujian","initWelcome::时间:::3:"+(endTime4-startTime));

        new Thread(){
            @Override
            public void run() {
                super.run();

                // 存储device
                Map<String, String> map = new HashMap<String, String>();
                map.put(FileManager.xmlKey_device, ToolsDevice.getPhoneDevice(context));
                UtilFile.saveShared(context, FileManager.xmlFile_appInfo, map);

                // 存储启动时间
                map = new HashMap<String, String>();
                map.put(FileManager.xmlKey_startTime, currentTimeMillis() + "");
                UtilFile.saveShared(context, FileManager.xmlFile_appInfo, map);
                //修改所有上传中的普通菜谱状态
                UploadDishControl.getInstance().updataAllUploadingDish(context.getApplicationContext());

                //清除上传中的数据库数据
                SubjectSqlite subjectSqlite = SubjectSqlite.getInstance(context);
                ArrayList<SubjectData> array = subjectSqlite.selectByState(SubjectData.UPLOAD_ING);
                for(SubjectData data : array){
                    subjectSqlite.deleteById(data.getId());
                }
            }
        }.start();

    }
    /**
     * 老版兼容问题
     */
    private void compatibleData(final Context context) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                // 删除老版文件
                if (UtilFile.ifFileModifyByCompletePath(UtilFile.getDataDir() + "indexData.xh", -1) != null) {
                    UtilFile.delDirectoryOrFile(UtilFile.getDataDir() + "indexData.xh");
                    UtilFile.delDirectoryOrFile(UtilFile.getSDDir() + "dish");
                }
                // 改老版的购物单文件到数据库中
                final String json = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_buyBurden);
                if (json.length() > 0) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            saveDataInDB(json,context);
                            UtilFile.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_buyBurden);
                        }
                    }).start();
                }
                // 245版32以后，数据库字段更新
                String verName = VersionOp.getVerName(context);
                verName = verName.replace(".", "");
                // Log.i("FRJ","verName: " + verName);
                if (Integer.parseInt(verName) <= 245) {
                    try {
                        UploadDishSqlite sqlite = new UploadDishSqlite(context);
                        sqlite.insert(sqlite.selectById(1));
                        sqlite.deleteById(1);
                    } catch (Exception e) {
                        UploadDishSqlite sqlite = new UploadDishSqlite(context);
                        sqlite.deleteDatabase(context);
                        // Log.i("FRJ","----------isDelete: " + isDelete);
                        e.printStackTrace();
                    }
                }
                //清理sd的xiangha文件夹，老版有杂物
                UtilFile.delDirectoryOrFile(UtilFile.getSDDir());
            }
        }.start();
    }

    /**
     * 处理数据,这里的context 再进行校验
     * @param json
     */
    private void saveDataInDB(String json,Context context) {
        DishOffData buyData = new DishOffData();
        ShowBuySqlite sqlite = new ShowBuySqlite(context);
        ArrayList<Map<String, String>> arrayList = getListMapByJson(json);
        for (int i = 0; i < arrayList.size(); i++) {
            buyData.setCode(arrayList.get(i).get("code"));
            buyData.setName(arrayList.get(i).get("name"));
            buyData.setAddTime(Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0));

            JSONArray array = new JSONArray();
            try {
                array = new JSONArray(json);
                String newJson = array.get(i).toString();
                buyData.setJson(newJson);
                int id = sqlite.insert(context, buyData);
                if (id > 0)
                    AppCommon.buyBurdenNum++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sqlite.close();
    }
}
