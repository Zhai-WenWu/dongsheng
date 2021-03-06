package acore.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.download.container.DownloadCallBack;
import com.download.down.DownLoad;
import com.download.tools.FileUtils;
import com.popdialog.AllPopDialogControler;
import com.popdialog.FullSrceenDialogControl;
import com.popdialog.GoodCommentDialogControl;
import com.popdialog.GuideDialogControl;
import com.popdialog.base.BaseDialogControl;
import com.popdialog.db.FullScreenModule;
import com.xiangha.R;

import java.util.Map;

import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.notification.controller.NotificationSettingController;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import amodule.main.activity.MainHomePage;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.tools.AdConfigTools;

import static acore.logic.ConfigMannager.KEY_DIVERSION;
import static acore.logic.ConfigMannager.KEY_GOODCOMMENT;
import static acore.logic.ConfigMannager.KEY_PUSHJSON;
import static acore.notification.controller.NotificationSettingController.pushSetHome;
import static acore.notification.controller.NotificationSettingController.push_show_home;
import static com.popdialog.AllPopDialogControler.log;
import static third.ad.tools.AdPlayIdConfig.FULL_SRCEEN_ACTIVITY;

/**
 * PackageName : acore.logic
 * Created by MrTrying on 2017/9/20 10:37.
 * E_mail : ztanzeyu@gmail.com
 */

public class AllPopDialogHelper {

    private static final String INERVAL_XML = "intervalSP";
    private static final String KEY_INERVAL_COUNT = "interval_count";

    /**所有弹框的控制器*/
    private AllPopDialogControler allPopDialogControler;

    public AllPopDialogHelper(Activity activity) {
        AllPopDialogControler.DEBUG = false;
        this.allPopDialogControler = new AllPopDialogControler(activity, VersionOp.getVerName(activity),
                new AllPopDialogControler.OnGetAllDataCallback() {
                    @Override
                    public String getGuideData() {
                        log("AllPopDialogHelper :: getGuideData");
                        return ConfigMannager.getConfigByLocal(KEY_DIVERSION);
                    }

                    @Override
                    public void loadFullScreenData(AllPopDialogControler.GetFullScreenDataCallback callback) {
                        log("AllPopDialogHelper :: getFullScreenData");
                        if(AdConfigTools.getInstance().isLoadOver){
                            handlerFullData(callback);
                        }else{
                            AdConfigTools.getInstance().getAdConfigInfo(new InternetCallback() {
                                @Override
                                public void loaded(int i, String s, Object o) {
                                    handlerFullData(callback);
                                }
                            });
                        }
                    }

                    private void handlerFullData(AllPopDialogControler.GetFullScreenDataCallback callback){
                        String intervalCountValue = FileManager.loadShared(activity,INERVAL_XML,KEY_INERVAL_COUNT).toString();
                        int intervalCount = TextUtils.isEmpty(intervalCountValue)?0:Integer.parseInt(intervalCountValue);
                        final String path = FileManager.getDataDir() + FULL_SRCEEN_ACTIVITY + ".xh";
                        String data = FileManager.readFile(path);
                        Map<String,String> config = StringManager.getFirstMap(data);
                        Map<String, String> map =  StringManager.getFirstMap(config.get("quanping"));
                        String intervalValue = map.get("interval");
                        int interval = TextUtils.isEmpty(intervalValue) ? 0 : Integer.parseInt(intervalValue);
                        log(intervalCountValue);
                        data = map.get("list");
                        log(data);
                        if(callback != null){
                            callback.loadFullScreenData(intervalCount > interval?data:"");
                        }
                    }

                    @Override
                    public String getGoodCommentData() {
                        log("AllPopDialogHelper :: getGoodCommentData");
                        return ConfigMannager.getConfigByLocal(KEY_GOODCOMMENT);
                    }

                    @Override
                    public String getPushData() {
                        log("AllPopDialogHelper :: getPushData");
                        return ConfigMannager.getConfigByLocal(KEY_PUSHJSON);
                    }
                });
        //初始化
        initialize();
    }

    /**
     * 初始化
     */
    private void initialize() {
        allPopDialogControler.setmGetCurrentActiivtyCallback(() -> {
            if(Main.allMain != null
                    && Main.allMain.allTab != null
                    && Main.allMain.allTab.get(MainHomePage.KEY) != null){
                return Main.allMain.allTab.get(MainHomePage.KEY);
            }
            return XHActivityManager.getInstance().getCurrentActivity();
        });
        //导流回调
        allPopDialogControler.setOnGuideClickCallback(new GuideDialogControl.OnGuideClickCallback() {
            @Override
            public void onClickSure(Map<String, String> map, String twoLevel, String text) {
                if (map == null) {
                    return;
                }
                log("Guide :: onClickSure");
                log("Guide :: map = " + map.toString());
                String url = map.get("url");
                String type = map.get("type");
                if ("1".equals(type)) {
                    try {
                        final DownLoad downLoad = new DownLoad(Main.allMain);
                        downLoad.setDownLoadTip("开始下载", map.get("name") + ".apk", "正在下载", R.drawable.ic_launcher, false);
                        downLoad.starDownLoad(url, FileManager.getSDCacheDir(), map.get("name"), true, new DownloadCallBack() {

                            @Override
                            public void starDown() {
                                super.starDown();
                                Tools.showToast(Main.allMain, "开始下载");
                            }

                            @Override
                            public void downOk(Uri uri) {
                                super.downOk(uri);
                                FileUtils.install(Main.allMain, uri);
                                downLoad.cacelNotification();
                            }

                            @Override
                            public void downError(String s) {
                                Tools.showToast(Main.allMain, "下载失败：" + s);
                                downLoad.cacelNotification();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if ("2".equals(type)) {
                    //app内部打开
                    AppCommon.openUrl(Main.allMain, url, true);
                } else if ("3".equals(type)) {
                    //浏览器打开
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    Main.allMain.startActivity(intent);
                }
                XHClick.mapStat(XHApplication.in(), "a_NewDiversion", twoLevel, text);//统计
            }

            @Override
            public void onClickCannel(Map<String, String> map, String twoLevel, String text) {
                if (map == null) {
                    return;
                }
                log("Guide :: onClickCannel");
                log("Guide :: map = " + map.toString());
                XHClick.mapStat(XHApplication.in(), "a_NewDiversion", twoLevel, text);//统计
            }
        });

        //全屏广告
        allPopDialogControler.setOnFullScreenStatusCallback(new FullSrceenDialogControl.OnFullScreenStatusCallback() {
            @Override
            public void onPreShow(FullScreenModule module) {
                statFullScreen(module,"DropDownBox_ShouldHaveBeenShown","","", "");
            }

            @Override
            public void onShow(FullScreenModule module) {
                log("FullScreen :: 展示");
                FileManager.saveShared(XHActivityManager.getInstance().getCurrentActivity(), INERVAL_XML, KEY_INERVAL_COUNT, "0");
                statFullScreen(module,"DropDownBox_ActuallySucceedShow","ad_show_index","全屏", "xh");
            }

            @Override
            public void onClickImage(FullScreenModule module) {
                log("FullScreen :: 点击图片");
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), module.getUrl(), true);
                statFullScreen(module,"DropDownBox_Click","ad_click_index","全屏", "xh");
            }

            @Override
            public void onClickClose(FullScreenModule module) {
                log("FullScreen :: 点击关闭");
                statFullScreen(module,"DropDownBox_Close","a_fullcereen_ad","手动关闭", "");
            }

            private void statFullScreen(FullScreenModule module,String btn,String eventID,String twoLevel, String threeLevel) {
                final Activity activity = XHActivityManager.getInstance().getCurrentActivity();
                if(activity != null){
                    StatisticsManager.saveData(StatModel.createSpecialActionModel(activity.getClass().getSimpleName(),"","",
                            btn,"","",module.getStatJson()));
                    XHClick.mapStat(activity, eventID, twoLevel, threeLevel);
                }
            }
        });
        allPopDialogControler.setOnLoadImageCallback((imageUrl, callback) -> {
            log("FullScreen :: 加载图片 :: imageUrl = " + imageUrl);
            //加载图片
            LoadImage.with(XHApplication.in())
                    .load(imageUrl)
                    .setSaveType(LoadImage.SAVE_LONG)
                    .build()
                    .listener(new RequestListener<GlideUrl, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, GlideUrl glideUrl, Target<Bitmap> target, boolean b) {
                            if (callback != null) {
                                log("FullScreen :: 执行回调，显示图片");
                                callback.onAfterLoadImage(null);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap bitmap, GlideUrl glideUrl, Target<Bitmap> target, boolean b, boolean b1) {
                            return false;
                        }
                    })
                    .into(new SubBitmapTarget() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                            if (callback != null) {
                                log("FullScreen :: 执行回调，显示图片");
                                callback.onAfterLoadImage(bitmap);
                            }
                        }
                    });
        });

        //好评
        allPopDialogControler.setOnGoodCommentClickCallback(new GoodCommentDialogControl.OnGoodCommentClickCallback() {
            @Override
            public void onClickSure(String twoLevel, String text) {
                log("GoodComment :: onClickSure");
                XHClick.mapStat(XHApplication.in(), "a_NewEvaluate", twoLevel, text);
            }

            @Override
            public void onClickCannel(String twoLevel, String text) {
                log("GoodComment :: onClickCannel");
                XHClick.mapStat(XHApplication.in(), "a_NewEvaluate", twoLevel, text);
                XHClick.mapStat(XHApplication.in(), "a_evaluate420", "首页弹框关闭", "");
            }
        });

        //推送
//        allPopDialogControler.setOnPushDialogStatisticsCallback(new PushDialogControl.OnPushDialogStatisticsCallback() {
//            @Override
//            public void onSureStatistics() {
//                log("Push :: onSureStatistics");
//                XHClick.mapStat(XHApplication.in(), "a_push", "是", "");
//            }
//
//            @Override
//            public void onCannelStatistics() {
//                log("Push :: onCannelStatistics");
//                XHClick.mapStat(XHApplication.in(), "a_push", "否", "");
//            }
//        });
        allPopDialogControler.setOnPushDialogStatisticsCallback(new AllPopDialogControler.PushViewShowCallBack() {
            @Override
            public void viewShowState(boolean b) {
                if (b) NotificationSettingController.showNotification(push_show_home, pushSetHome);
            }
        });
    }

    /**
     * 开始
     */
    public void start() {
        allPopDialogControler.start(
                onStartCallback -> VersionOp.getInstance().isShow("", new BaseDialogControl.OnPopDialogCallback() {
                    @Override
                    public void onCanShow() {
                        log( "VersionUpdata :: onCanShow");
                        log("VersionUpdata :: versionOp.isMustUpdata:" + VersionOp.getInstance().isMustUpdata);
                        if (VersionOp.getInstance().isMustUpdata) {
                            log( "强制升级");
                            VersionOp.getInstance().show();
                        } else {
                            if (onStartCallback != null) {
                                onStartCallback.onStart();
                            }
                        }
                    }

                    @Override
                    public void onNextShow() {
                        if (onStartCallback != null) {
                            onStartCallback.onStart();
                        }
                    }
                }),
                () -> {
                    if (VersionOp.getInstance().isNeedUpdata) {
                        log( "普通升级");
                        VersionOp.getInstance().show();
                        return true;
                    }
                    log( "不需要升级");
                    return false;
                }
        );
    }

    public static void updateIntervalCount(Context context) {
        String intervalCountValue = FileManager.loadShared(context, INERVAL_XML, KEY_INERVAL_COUNT).toString();
        int intervalCount = TextUtils.isEmpty(intervalCountValue) ? 0 : Integer.parseInt(intervalCountValue);
        intervalCount++;
        FileManager.saveShared(context, INERVAL_XML, KEY_INERVAL_COUNT, String.valueOf(intervalCount));
    }

}
