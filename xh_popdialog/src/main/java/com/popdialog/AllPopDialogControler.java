package com.popdialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.popdialog.base.BaseDialogControl;

/**
 * PackageName : com
 * Created by MrTrying on 2017/9/19 15:44.
 * E_mail : ztanzeyu@gmail.com
 */

public class AllPopDialogControler {

    private Activity activity;
    /** 版本号 */
    private String versionCode;
    /** 获取数据回调 */
    private OnGetAllDataCallback onGetAllDataCallback;


    public AllPopDialogControler(Activity activity, String versionCode, OnGetAllDataCallback callback) {
        this.activity = activity;
        this.versionCode = versionCode;
        this.onGetAllDataCallback = callback;
    }

    /**
     * 开始，此处有特殊逻辑
     * @param onPreStartCallback
     *      内部逻辑开始前先执行外部逻辑
     * @param onStartFailCallback
     *      导流不显示则再次执行外部逻辑
     */
    public void start(final OnPreStartCallback onPreStartCallback,@NonNull final OnStartFailCallback onStartFailCallback){
        if(onPreStartCallback == null){
            toGuideDialog(onStartFailCallback);
            return;
        }
        onPreStartCallback.onPreStart(new OnStartCallback() {
            @Override
            public void onStart() {
                toGuideDialog(onStartFailCallback);
            }
        });
    }

    /** ============================================ 导流弹框 ============================================ */

    /** 导流弹框回调 */
    private GuideDialogControl.OnGuideClickCallback onGuideClickCallback;

    private void toGuideDialog(final OnStartFailCallback onStartFailCallback) {
        if (onGetAllDataCallback == null) {
            return;
        }
        final GuideDialogControl guideDialogControl = new GuideDialogControl(activity);
        guideDialogControl.setOnGuideClickCallback(onGuideClickCallback);
        guideDialogControl.isShow(onGetAllDataCallback.getGuideData(), new BaseDialogControl.OnPopDialogCallback() {
            @Override
            public void onCanShow() {
                Log.i("tzy", "toGetGuidData onShow()");
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        guideDialogControl.show();
                    }
                }, 5 * 1000);
            }

            @Override
            public void onNextShow() {
                if(onStartFailCallback == null
                        || !onStartFailCallback.onStartFail()){
                    toFullScreenDialog();
                }
            }
        });
    }

    /** ============================================ 全屏广告 ============================================ */

    private FullSrceenDialogControl.OnFullScreenStatusCallback onFullScreenStatusCallback;
    private FullSrceenDialogControl.OnLoadImageCallback onLoadImageCallback;

    private void toFullScreenDialog() {
        if (onGetAllDataCallback == null) {
            return;
        }
        final FullSrceenDialogControl fullSrceenDialogControl = new FullSrceenDialogControl(activity);
        fullSrceenDialogControl.setOnFullScreenStatusCallback(onFullScreenStatusCallback);
        fullSrceenDialogControl.setOnLoadImageCallback(onLoadImageCallback);
        fullSrceenDialogControl.isShow(onGetAllDataCallback.getFullScreenData(), new BaseDialogControl.OnPopDialogCallback() {
            @Override
            public void onCanShow() {
                Log.i("tzy", "toFullScreenDialog onCanShow()");
                fullSrceenDialogControl.show();
            }

            @Override
            public void onNextShow() {
                Log.i("tzy", "toFullScreenDialog onNextShow()");
                toGoodCommentDialog();
            }
        });
    }

    /** ============================================ 好评弹框 ============================================ */
    private GoodCommentDialogControl.OnGoodCommentClickCallback onGoodCommentClickCallback;
    private void toGoodCommentDialog() {
        if (onGetAllDataCallback == null) {
            return;
        }
        final GoodCommentDialogControl goodCommentDialogControl = new GoodCommentDialogControl(activity);
        goodCommentDialogControl.setOnGoodCommentClickCallback(onGoodCommentClickCallback);
        goodCommentDialogControl.isShow(onGetAllDataCallback.getGoodCommentData(), new BaseDialogControl.OnPopDialogCallback() {
            @Override
            public void onCanShow() {
                Log.i("tzy", "toGetGoodData onShow()");
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goodCommentDialogControl.show();
                    }
                }, 9 * 1000);
            }

            @Override
            public void onNextShow() {
                toPushDialog();
            }
        });
    }

    /** ============================================ 推送弹框 ============================================ */
    private PushDialogControl.OnPushDialogStatisticsCallback onPushDialogStatisticsCallback;
    private void toPushDialog() {
        if (onGetAllDataCallback == null) {
            return;
        }
        final PushDialogControl pushDialogControl = new PushDialogControl(activity, versionCode);
        pushDialogControl.setOnPushDialogStatisticsCallback(onPushDialogStatisticsCallback);
        pushDialogControl.isShow(onGetAllDataCallback.getPushData(), new BaseDialogControl.OnPopDialogCallback() {
            @Override
            public void onCanShow() {
                pushDialogControl.show();
            }

            @Override
            public void onNextShow() {

            }
        });
    }

    /** ============================================ Interface ============================================ */

    public interface OnGetAllDataCallback {
        String getGuideData();

        String getFullScreenData();

        String getGoodCommentData();

        String getPushData();
    }

    public interface OnPreStartCallback{
        void onPreStart(OnStartCallback onStartCallback);
    }

    public interface OnStartCallback{
        void onStart();
    }

    public interface OnStartFailCallback{
        boolean onStartFail();
    }

    /*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public GuideDialogControl.OnGuideClickCallback getOnGuideClickCallback() {
        return onGuideClickCallback;
    }

    public void setOnGuideClickCallback(GuideDialogControl.OnGuideClickCallback onGuideClickCallback) {
        this.onGuideClickCallback = onGuideClickCallback;
    }

    public FullSrceenDialogControl.OnFullScreenStatusCallback getOnFullScreenStatusCallback() {
        return onFullScreenStatusCallback;
    }

    public void setOnFullScreenStatusCallback(FullSrceenDialogControl.OnFullScreenStatusCallback onFullScreenStatusCallback) {
        this.onFullScreenStatusCallback = onFullScreenStatusCallback;
    }

    public FullSrceenDialogControl.OnLoadImageCallback getOnLoadImageCallback() {
        return onLoadImageCallback;
    }

    public void setOnLoadImageCallback(FullSrceenDialogControl.OnLoadImageCallback onLoadImageCallback) {
        this.onLoadImageCallback = onLoadImageCallback;
    }

    public GoodCommentDialogControl.OnGoodCommentClickCallback getOnGoodCommentClickCallback() {
        return onGoodCommentClickCallback;
    }

    public void setOnGoodCommentClickCallback(GoodCommentDialogControl.OnGoodCommentClickCallback onGoodCommentClickCallback) {
        this.onGoodCommentClickCallback = onGoodCommentClickCallback;
    }

    public PushDialogControl.OnPushDialogStatisticsCallback getOnPushDialogStatisticsCallback() {
        return onPushDialogStatisticsCallback;
    }

    public void setOnPushDialogStatisticsCallback(PushDialogControl.OnPushDialogStatisticsCallback onPushDialogStatisticsCallback) {
        this.onPushDialogStatisticsCallback = onPushDialogStatisticsCallback;
    }
}
