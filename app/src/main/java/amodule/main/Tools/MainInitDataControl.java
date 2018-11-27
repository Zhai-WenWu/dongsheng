package amodule.main.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.popdialog.view.XHADView;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;

import org.eclipse.jetty.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AllPopDialogHelper;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.MessageTipController;
import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.logic.ConfigMannager;
import acore.logic.polling.AppHandlerAsyncPolling;
import acore.logic.polling.IHandleMessage;
import acore.logic.polling.PollingConfig;
import acore.override.XHApplication;
import acore.tools.ChannelUtil;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.answer.activity.AnswerEditActivity;
import amodule.answer.activity.AskEditActivity;
import amodule.answer.db.AskAnswerSQLite;
import amodule.answer.model.AskAnswerModel;
import amodule.article.activity.ArticleUploadListActivity;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadParentSQLite;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.DishOffData;
import amodule.dish.db.ShowBuySqlite;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.dish.tools.UploadDishControl;
import amodule.main.Main;
import amodule.main.activity.MainHomePage;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import amodule.search.db.MatchWordsDbUtil;
import aplug.basic.InternetCallback;
import aplug.basic.XHInternetCallBack;
import aplug.service.base.ServiceManager;
import aplug.web.tools.XHTemplateManager;
import third.ad.tools.AdConfigTools;
import third.mall.aplug.MallCommon;
import third.push.localpush.LocalPushManager;
import third.push.xg.XGTagManager;
import third.qiyu.QiYvHelper;
import xh.basic.tool.UtilFile;

import static java.lang.System.currentTimeMillis;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * app初始化数据类---只在这里进行初始化
 */
public class MainInitDataControl {
    private int delayedTime = 7 * 1000;

    private IHandleMessage mIHandleMessage;

    /**
     * welcome之前初始化
     */
    public void initWelcomeBefore(Context context) {

    }

    /**
     * welcome oncreate初始化
     */
    public void initWelcomeOncreate() {
        Log.i("zhangyujian", "initWelcomeOncreate");
        long startTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CookieManager.getInstance().removeAllCookie();
                } catch (Exception ignored) {
                }
                XHClick.saveFirstStartTime(XHApplication.in());
                XHClick.registerMonthSuperProperty(XHApplication.in());
            }
        }).start();
        long endTime = System.currentTimeMillis();
        Log.i("zhangyujian", "initWelcomeOncreate::时间:" + (endTime - startTime));

    }

    /**
     * welcome布局完成之后之后初始化
     */
    public void initWelcomeAfter(final Activity activity) {
        Log.i("zhangyujian", "initWelcomeAfter");
        long startTime = System.currentTimeMillis();
        initWelcome(activity);
        new Thread() {
            @Override
            public void run() {
                super.run();
                MobclickAgent.setDebugMode(true);

                //待处理问题。
//                HomeToutiaoAdControl.getInstance().getAdData(activity);
                ToolsDevice.saveXhCode(activity);
                activity.deleteDatabase("statictis.db");
            }
        }.start();
        ConfigMannager.saveConfigData(XHApplication.in(), new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (Main.allMain != null && Main.allMain.getCurrentTab() == Main.TAB_HOME && Main.allMain.allTab != null && Main.allMain.allTab.containsKey(MainHomePage.KEY)) {
                    MainHomePage mainIndex = (MainHomePage) Main.allMain.allTab.get(MainHomePage.KEY);
                    mainIndex.handleVipGuideStatus();
                }
            }
        });
        ServiceManager.startProtectService(XHApplication.in());
        long endTime2 = System.currentTimeMillis();
        Log.i("zhangyujian", "initWelcomeAfter::时间:" + (endTime2 - startTime));

    }

    /**
     * Main之后初始化
     */
    public void initMainBefore() {

    }

    /**
     * Main oncreate初始化
     */
    public void initMainOncreate() {

    }

    /**
     * Main之后初始化
     */
    public void iniMainAfter(final Activity act) {
        Log.i("zhangyujian", "iniMainAfter");
        long startTime = System.currentTimeMillis();
        //初始化语音
        new Thread(() -> {
            //讯飞语音： 请勿在“=”与 appid 之间添加任务空字符或者转义符
            SpeechUtility.createUtility(act, SpeechConstant.APPID + "=56ce9191");
        }).start();
        // 发送页面存活时间
        XHClick.sendLiveTime(act);
        //电商首页数据
        MallCommon.getDsInfo(act, null);
        //请求广告位
        AdConfigTools.getInstance().getAdConfigInfo();
        long endTime = System.currentTimeMillis();
        //七鱼初始化 init方法无需放入主进程中执行，其他的初始化，有必要放在放入主进程
        QiYvHelper.getInstance().initSDK(act);

        OffDishToFavoriteControl.addCollection(act);
        LocalPushManager.stopLocalPush(act);

        PollingConfig.COURSE_GUIDANCE.registerIHandleMessage(mIHandleMessage);
        AppHandlerAsyncPolling.getInstance().startPollingImmediately(PollingConfig.COURSE_GUIDANCE);
        Log.i("zhangyujian", "iniMainAfter::时间:" + (endTime - startTime));
    }

    public void setIHandleMessage(IHandleMessage IHandleMessage) {
        mIHandleMessage = IHandleMessage;
    }

    private void setXGTag() {
        XGTagManager manager = new XGTagManager();
        if (!LoginManager.isLogin())
            manager.addXGTag(XGTagManager.APP_NEW);
        String official = (String) UtilFile.loadShared(XHApplication.in(), FileManager.xg_config, FileManager.xg_config_official);
        if (TextUtils.isEmpty(official)) {
            UtilFile.saveShared(XHApplication.in(), FileManager.xg_config, FileManager.xg_config_official, "official");
            manager.addXGTag(XGTagManager.OFFICIAL);
        }
    }

    /**
     * main在界面展示后初始化
     *
     * @param act
     */
    public void initMainOnResume(final Activity act) {
        Log.i("zhangyujian", "initMainOnResume");

        LoginManager.initYiYuanBindState(act, null);

        long startTime = System.currentTimeMillis();

        //更模版
        new XHTemplateManager().checkUplateAllTemplate();

        delayedExcute(() -> {
            ToolsDevice.sendCrashAndAppInfoToServer(act.getApplicationContext(), LoginManager.userInfo.get("code"));
            //更新热词匹配数据库
            new MatchWordsDbUtil().checkUpdateMatchWordsDb(act);

            //获取圈子静态数据
            AppCommon.saveCircleStaticData(act);

            AppCommon.saveUrlRuleFile(act);
            AppCommon.saveAppData();

            //取消自我唤醒
            XGPushManager.clearLocalNotifications(act);
        });

        //获取随机推广数据
        AppCommon.saveRandPromotionData(act);
        if (act != null && XHADView.getInstence(act) != null) {
            XHADView.getInstence(act).setCanShowCallback(() -> Main.allMain != null
                    && Main.allMain.getCurrentTab() == 0);
        }

        //判断弹屏旧数据库是否存在
        if (act != null && act.getDatabasePath("fullsrceen.db").exists()) {
            UtilFile.delDirectoryOrFile(act.getDatabasePath("fullsrceen.db").getPath());
        }

        new AllPopDialogHelper(act).start();

        new Thread(this::setXGTag).start();

        onMainResumeStatics();

        long endTime2 = System.currentTimeMillis();
        Log.i("zhangyujian", "initMainOnResume::时间::3::" + (endTime2 - startTime));
    }

    /**
     * 页面展示后，发送需要统计的数据
     */
    private void onMainResumeStatics() {
        new Thread(() -> {
            Object userCountStatics = FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, "userCount");
            if (!"2".equals(userCountStatics)) {
                String channel = ChannelUtil.getChannel(XHApplication.in());
                if (channel.contains(".")) {
                    String[] channels = channel.split("\\.");
                    channel = channels[channels.length - 1];
                }
                XHClick.mapStat(XHApplication.in(), "a_usercount", channel, ToolsDevice.getVerName(XHApplication.in()));
                FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_appInfo, "userCount", "2");
            }
        }).start();
    }

    /**
     * Welcome应用数据初始化
     */
    private void initWelcome(final Context context) {
        Log.i("zhangyujian", "initWelcome");
        long startTime = System.currentTimeMillis();

        // 自动登录
        MessageTipController.newInstance().getCommonData(null);

        compatibleData(context);

        AppCommon.clearCache();

        long endTime4 = System.currentTimeMillis();
        Log.i("zhangyujian", "initWelcome::时间:::3:" + (endTime4 - startTime));

        new Thread() {
            @Override
            public void run() {
                super.run();
                // 存储device
                Map<String, String> map = new HashMap<String, String>();
                map.put(FileManager.xmlKey_device, ToolsDevice.getPhoneDevice(context));
                UtilFile.saveShared(context, FileManager.xmlFile_appInfo, map);
                XHInternetCallBack.clearCookie();

                // 存储启动时间
                map = new HashMap<>();
                map.put(FileManager.xmlKey_startTime, currentTimeMillis() + "");
                UtilFile.saveShared(context, FileManager.xmlFile_appInfo, map);
                //修改所有上传中的普通菜谱状态
                UploadDishControl.getInstance().updataAllUploadingDish(context.getApplicationContext());

                //清除上传中的数据库数据
                SubjectSqlite subjectSqlite = SubjectSqlite.getInstance(context);
                ArrayList<SubjectData> array = subjectSqlite.selectByState(SubjectData.UPLOAD_ING);
                for (SubjectData data : array) {
                    subjectSqlite.deleteById(data.getId());
                }
                FileManager.saveShared(context, FileManager.SHOW_NO_WIFI, FileManager.SHOW_NO_WIFI, "0");
            }
        }.start();

    }

    /**
     * 老版兼容问题
     */
    private void compatibleData(final Context context) {
        new Thread() {
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
                    new Thread(() -> {
                        saveDataInDB(json, context);
                        UtilFile.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_buyBurden);
                    }).start();
                }
                // 245版32以后，数据库字段更新
                String verName = VersionOp.getVerName(context);
                verName = verName.replace(".", "");
                // //Log.i("FRJ","verName: " + verName);
                if (Integer.parseInt(verName) <= 245) {
                    try {
                        UploadDishSqlite sqlite = new UploadDishSqlite(context);
                        sqlite.insert(sqlite.selectById(1));
                        sqlite.deleteById(1);
                    } catch (Exception e) {
                        UploadDishSqlite sqlite = new UploadDishSqlite(context);
                        sqlite.deleteDatabase(context);
                        // //Log.i("FRJ","----------isDelete: " + isDelete);
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
     *
     * @param json
     */
    private void saveDataInDB(String json, Context context) {
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

    private void delayedExcute(@NonNull Runnable runnable) {
        if (runnable == null) return;
        new Handler(Looper.getMainLooper()).postDelayed(runnable, delayedTime);
    }

    public void mainAfterUpload(Activity activity) {
        if (showQAUploading(activity))
            return;
        if (showUploading(activity, new UploadArticleSQLite(XHApplication.in().getApplicationContext()), EditParentActivity.DATA_TYPE_ARTICLE, "您的文章还未上传完毕，是否继续上传？"))
            return;
        if (showUploadingVideo(activity)) {
            return;
        }
    }

    private boolean showQAUploading(Activity activity) {
        boolean show = false;
        final AskAnswerSQLite sqLite = new AskAnswerSQLite(XHApplication.in().getApplicationContext());
        final AskAnswerModel model = sqLite.queryFirstData();
        String msg = "";
        Intent intent = null;
        Class tempC = null;
        if (model != null) {
            intent = new Intent();
            intent.putExtra("fromHome", true);
            intent.putExtra("code", model.getmDishCode());
            intent.putExtra("qaCode", model.getmQACode());
            intent.putExtra("authorCode", model.getmAuthorCode());
            intent.putExtra("qaTitle", model.getmTitle());
            intent.putExtra("answerCode", model.getmAnswerCode());
            boolean isAskAgain = false;
            boolean isAnswerAgain = false;
            String qaType = model.getmType();
            if (!TextUtils.isEmpty(qaType)) {
                switch (qaType) {
                    case AskAnswerModel.TYPE_ANSWER:
                        msg = "您有一个回答尚未发布，是否继续？";
                        tempC = AnswerEditActivity.class;
                        break;
                    case AskAnswerModel.TYPE_ANSWER_AGAIN:
                        msg = "您有一个回答尚未发布，是否继续？";
                        tempC = AnswerEditActivity.class;
                        isAnswerAgain = true;
                        break;
                    case AskAnswerModel.TYPE_ASK:
                        msg = "您有一个问题尚未发布，是否继续？";
                        tempC = AskEditActivity.class;
                        break;
                    case AskAnswerModel.TYPE_ASK_AGAIN:
                        msg = "您有一个问题尚未发布，是否继续？";
                        tempC = AskEditActivity.class;
                        isAskAgain = true;
                        break;
                }
            }
            intent.putExtra("mIsAnswerMore", isAnswerAgain ? "2" : "1");
            intent.putExtra("isAskMore", isAskAgain ? "2" : "1");
            if (tempC == null)
                return show;
            show = true;
            intent.setClass(activity, tempC);
            final Intent finalIntent = intent;
            final DialogManager dialogManager = new DialogManager(activity);
            dialogManager.createDialog(new ViewManager(dialogManager)
                    .setView(new TitleMessageView(activity).setText(msg))
                    .setView(new HButtonView(activity)
                            .setNegativeText("否", v -> {
                                dialogManager.cancel();
                                new Thread(() -> sqLite.deleteAll()).start();
                            })
                            .setPositiveTextColor(Color.parseColor("#007aff"))
                            .setPositiveText("是", v -> {
                                dialogManager.cancel();
                                activity.startActivity(finalIntent);
                            }))).show();
        }
        return show;
    }

    private boolean showUploading(Activity activity, final UploadParentSQLite sqLite, final int dataType, String title) {
        final UploadArticleData uploadArticleData = sqLite.getUploadIngData();
        if (uploadArticleData != null) {
            final DialogManager dialogManager = new DialogManager(activity);
            dialogManager.createDialog(new ViewManager(dialogManager)
                    .setView(new TitleMessageView(activity).setText(title))
                    .setView(new HButtonView(activity)
                            .setNegativeText("取消", v -> {
                                dialogManager.cancel();
                                uploadArticleData.setUploadType(UploadDishData.UPLOAD_PAUSE);
                                sqLite.update(uploadArticleData.getId(), uploadArticleData);
                            })
                            .setPositiveTextColor(Color.parseColor("#007aff"))
                            .setPositiveText("确定", v -> {
                                Intent intent = new Intent(activity, ArticleUploadListActivity.class);
                                intent.putExtra("draftId", uploadArticleData.getId());
                                intent.putExtra("dataType", dataType);
                                intent.putExtra("coverPath", uploadArticleData.getImg());
                                String videoPath = "";
                                ArrayList<Map<String, String>> videoArray = uploadArticleData.getVideoArray();
                                if (videoArray.size() > 0) {
                                    videoPath = videoArray.get(0).get("video");
                                }
                                intent.putExtra("finalVideoPath", videoPath);
                                dialogManager.cancel();
                                activity.startActivity(intent);
                            }))).show();
            return true;
        }
        return false;
    }

    private boolean showUploadingVideo(final Context act) {
        UploadVideoSQLite sqLite = new UploadVideoSQLite(act);
        final int uploadingId = sqLite.hasUploading();
        if (uploadingId != -1) {
            if (sqLite.checkOver(UploadDishData.UPLOAD_FAIL)) {
                sqLite.deleteById(uploadingId);
                return false;
            } else {
                sqLite.update(uploadingId, UploadDishData.UPLOAD_FAIL);
                Toast.makeText(act, "您有上传失败的作品，已保存至个人主页", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
}
