package acore.logic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.download.container.DownloadCallBack;
import com.download.down.DownLoad;
import com.download.tools.FileUtils;
import com.popdialog.AllPopDialogControler;
import com.popdialog.FullSrceenDialogControl;
import com.popdialog.GoodCommentDialogControl;
import com.popdialog.GuideDialogControl;
import com.popdialog.PushDialogControl;
import com.popdialog.base.BaseDialogControl;
import com.xiangha.R;

import java.util.Map;

import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.main.Main;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;

import static com.popdialog.AllPopDialogControler.TAG;

/**
 * PackageName : acore.logic
 * Created by MrTrying on 2017/9/20 10:37.
 * E_mail : ztanzeyu@gmail.com
 */

public class AllPopDialogHelper {
    /**所有弹框的控制器*/
    AllPopDialogControler allPopDialogControler;

    public AllPopDialogHelper(Activity activity) {
        this.allPopDialogControler = new AllPopDialogControler(activity, VersionOp.getVerName(activity),
                new AllPopDialogControler.OnGetAllDataCallback() {
                    @Override
                    public String getGuideData() {
                        Log.i(TAG,"AllPopDialogHelper :: getGuideData");
                        return AppCommon.getConfigByLocal("diversion");
                    }

                    @Override
                    public String getFullScreenData() {
                        Log.i(TAG,"AllPopDialogHelper :: getFullScreenData");
                        String data = Tools.map2Json(AdConfigTools.getInstance().getAdConfigData(AdPlayIdConfig.FULLSCREEN));
                        if("true".equals(FileManager.loadShared(activity,FileManager.xmlFile_appInfo,"once").toString())){
                            data = "";
                        }
                        return data;
                    }

                    @Override
                    public String getGoodCommentData() {
                        Log.i(TAG,"AllPopDialogHelper :: getGoodCommentData");
                        return AppCommon.getConfigByLocal("goodComment");
                    }

                    @Override
                    public String getPushData() {
                        Log.i(TAG,"AllPopDialogHelper :: getPushData");
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
                Log.i(TAG,"Guide :: onClickSure");
                Log.i(TAG,"Guide :: map = " + map.toString());
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
                Log.i(TAG,"Guide :: onClickCannel");
                Log.i(TAG,"Guide :: map = " + map.toString());
                XHClick.mapStat(XHApplication.in(), "a_NewDiversion", twoLevel, text);//统计
            }
        });

        //全屏广告
        allPopDialogControler.setOnFullScreenStatusCallback(new FullSrceenDialogControl.OnFullScreenStatusCallback() {
            @Override
            public void onShow() {
                Log.i(TAG,"FullScreen :: 展示");
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "ad_show_index", "全屏", "xh");//统计
            }

            @Override
            public void onClickImage(Map<String, String> map) {
                Log.i(TAG,"FullScreen :: 点击图片");
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "ad_click_index", "全屏", "xh");//统计
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), map.get("url"), true);
            }

            @Override
            public void onClickClose() {
                Log.i(TAG,"FullScreen :: 点击关闭");
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "a_fullcereen_ad", "手动关闭", "");
            }
        });
        allPopDialogControler.setOnLoadImageCallback(new FullSrceenDialogControl.OnLoadImageCallback() {
            @Override
            public void onLoadImage(String imageUrl, final FullSrceenDialogControl.OnAfterLoadImageCallback callback) {
                Log.i(TAG,"FullScreen :: 加载图片 :: imageUrl = " + imageUrl);
                //加载图片
                LoadImage.with(XHApplication.in())
                        .load(imageUrl)
                        .setSaveType(LoadImage.SAVE_LONG)
                        .build()
                        .into(new SubBitmapTarget() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                if (callback != null) {
                                    Log.i(TAG,"FullScreen :: 执行回调，显示图片");
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
                Log.i(TAG,"GoodComment :: onClickSure");
                XHClick.mapStat(XHApplication.in(), "a_NewEvaluate", twoLevel, text);
            }

            @Override
            public void onClickCannel(String twoLevel, String text) {
                Log.i(TAG,"GoodComment :: onClickCannel");
                XHClick.mapStat(XHApplication.in(), "a_NewEvaluate", twoLevel, text);
                XHClick.mapStat(XHApplication.in(), "a_evaluate420", "首页弹框关闭", "");
            }
        });

        //推送
        allPopDialogControler.setOnPushDialogStatisticsCallback(new PushDialogControl.OnPushDialogStatisticsCallback() {
            @Override
            public void onSureStatistics() {
                Log.i(TAG,"Push :: onSureStatistics");
                XHClick.mapStat(XHApplication.in(), "a_push", "是", "");
            }

            @Override
            public void onCannelStatistics() {
                Log.i(TAG,"Push :: onCannelStatistics");
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
                                Log.i(TAG, "VersionUpdata :: onCanShow");
                                Log.i(TAG,"VersionUpdata :: versionOp.isMustUpdata:" + VersionOp.getInstance().isMustUpdata);
                                if (VersionOp.getInstance().isMustUpdata) {
                                    Log.i(TAG, "强制升级");
                                    VersionOp.getInstance().show();
                                } else {
                                    if (onStartCallback != null) {
                                        onStartCallback.onStart();
                                    }
                                }
                            }

                            @Override
                            public void onNextShow() {
                                Log.i(TAG, "去执行导流");
                                if (onStartCallback != null) {
                                    onStartCallback.onStart();
                                }
                            }
                        });
                    }
                }, new AllPopDialogControler.OnStartFailCallback() {
                    @Override
                    public boolean onStartFail() {
                        if (VersionOp.getInstance().isNeedUpdata) {
                            Log.i(TAG, "普通升级");
                            VersionOp.getInstance().show();
                            return true;
                        }
                        Log.i(TAG, "不需要升级");
                        return false;
                    }
                }
        );
    }

}
