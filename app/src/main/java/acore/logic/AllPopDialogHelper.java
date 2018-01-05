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
import com.popdialog.PushDialogControl;
import com.popdialog.base.BaseDialogControl;
import com.popdialog.db.FullSrceenModule;
import com.xiangha.R;

import java.util.Map;

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

import static com.popdialog.AllPopDialogControler.log;
import static com.tencent.stat.StatTrackLog.log;
import static third.ad.tools.AdPlayIdConfig.FULL_SRCEEN_ACTIVITY;

/**
 * PackageName : acore.logic
 * Created by MrTrying on 2017/9/20 10:37.
 * E_mail : ztanzeyu@gmail.com
 */

public class AllPopDialogHelper {

    public static final String INERVAL_XML = "intervalSP";
    public static final String KEY_INERVAL_COUNT = "interval_count";

    /**所有弹框的控制器*/
    AllPopDialogControler allPopDialogControler;

    public AllPopDialogHelper(Activity activity) {
        AllPopDialogControler.DEBUG = false;
        this.allPopDialogControler = new AllPopDialogControler(activity, VersionOp.getVerName(activity),
                new AllPopDialogControler.OnGetAllDataCallback() {
                    @Override
                    public String getGuideData() {
                        log("AllPopDialogHelper :: getGuideData");
                        return AppCommon.getConfigByLocal("diversion");
                    }

                    @Override
                    public void loadFullScreenData(AllPopDialogControler.GetFullScreenDataCallback callback) {
                        String onceValue = FileManager.loadShared(activity,FileManager.xmlFile_appInfo,"once").toString();
                        boolean isOnce = TextUtils.isEmpty(onceValue) || "true".equals(onceValue);
                        if(isOnce){
                            if(callback != null) callback.loadFullScreenData("");
                            return;
                        }
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
                        String data = AdConfigTools.getInstance().getAdConfigData(FULL_SRCEEN_ACTIVITY).get("quanping");
                        Map<String, String> map =  StringManager.getFirstMap(data);
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
                        return AppCommon.getConfigByLocal("goodComment");
                    }

                    @Override
                    public String getPushData() {
                        log("AllPopDialogHelper :: getPushData");
                        return AppCommon.getConfigByLocal("pushJson");
                    }
                });
        //初始化
        initialize();
    }

    /** 初始化 */
    private void initialize() {
        allPopDialogControler.setmGetCurrentActiivtyCallback(new AllPopDialogControler.GetCurrentActiivtyCallback() {
            @Override
            public Activity getCurrentActivity() {
                if(Main.allMain != null
                        && Main.allMain.allTab != null
                        && Main.allMain.allTab.get(MainHomePage.KEY) != null){
                    return Main.allMain.allTab.get(MainHomePage.KEY);
                }
                return XHActivityManager.getInstance().getCurrentActivity();
            }
        });
        //导流回调
        allPopDialogControler.setOnGuideClickCallback(new GuideDialogControl.OnGuideClickCallback() {
            @Override
            public void onClickSure(Map<String, String> map, String twoLevel, String text) {
                if(map == null){
                    return;
                }
                log("Guide :: onClickSure");
                log("Guide :: map = " + map.toString());
                String url = map.get("url");
                String type = map.get("type");
                if ("1".equals(type)) {
                    try {
                        final DownLoad downLoad = new DownLoad(Main.allMain);
                        downLoad.setNotifaction("开始下载", map.get("name") + ".apk", "正在下载", R.drawable.ic_launcher, false);
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
                if(map == null){
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
            public void onShow() {
                log("FullScreen :: 展示");
                FileManager.saveShared(XHActivityManager.getInstance().getCurrentActivity(),INERVAL_XML,KEY_INERVAL_COUNT,"0");
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "ad_show_index", "全屏", "xh");//统计
            }

            @Override
            public void onClickImage(FullSrceenModule module) {
                log("FullScreen :: 点击图片");
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "ad_click_index", "全屏", "xh");//统计
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), module.getUrl(), true);
            }

            @Override
            public void onClickClose() {
                log("FullScreen :: 点击关闭");
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "a_fullcereen_ad", "手动关闭", "");
            }
        });
        allPopDialogControler.setOnLoadImageCallback(new FullSrceenDialogControl.OnLoadImageCallback() {
            @Override
            public void onLoadImage(String imageUrl, final FullSrceenDialogControl.OnAfterLoadImageCallback callback) {
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
            }
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
        allPopDialogControler.setOnPushDialogStatisticsCallback(new PushDialogControl.OnPushDialogStatisticsCallback() {
            @Override
            public void onSureStatistics() {
                log("Push :: onSureStatistics");
                XHClick.mapStat(XHApplication.in(), "a_push", "是", "");
            }

            @Override
            public void onCannelStatistics() {
                log("Push :: onCannelStatistics");
                XHClick.mapStat(XHApplication.in(), "a_push", "否", "");
            }
        });
    }

    /**开始*/
    public void start() {
        allPopDialogControler.start(
                new AllPopDialogControler.OnPreStartCallback() {
                    @Override
                    public void onPreStart(final AllPopDialogControler.OnStartCallback onStartCallback) {
                        VersionOp.getInstance().isShow("", new BaseDialogControl.OnPopDialogCallback() {
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
                                log( "去执行导流");
                                if (onStartCallback != null) {
                                    onStartCallback.onStart();
                                }
                            }
                        });
                    }
                },
                new AllPopDialogControler.OnStartFailCallback() {
                    @Override
                    public boolean onStartFail() {
                        if (VersionOp.getInstance().isNeedUpdata) {
                            log( "普通升级");
                            VersionOp.getInstance().show();
                            return true;
                        }
                        log( "不需要升级");
                        return false;
                    }
                }
        );
    }

    public static void updateIntervalCount(Context context){
        String intervalCountValue = FileManager.loadShared(context,INERVAL_XML,KEY_INERVAL_COUNT).toString();
        int intervalCount = TextUtils.isEmpty(intervalCountValue)?0:Integer.parseInt(intervalCountValue);
        intervalCount++;
        FileManager.saveShared(context,INERVAL_XML,KEY_INERVAL_COUNT,String.valueOf(intervalCount));
    }

}
