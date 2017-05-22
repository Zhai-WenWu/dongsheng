package amodule.main.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.xiangha.R;

import org.json.JSONObject;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.ad.tools.WelcomeAdTools;
import xh.basic.tool.UtilImage;

import static android.os.Looper.getMainLooper;

/**
 * 欢迎页弹框
 */
public class WelcomeDialog extends Dialog {
    private final static int DEFAULT_TIME = 4;
    private Activity activity;
    protected View view;
    protected int height;

    private ImageView imageView;
    private TextView textSkip;
    private RelativeLayout mADSkipContainer;
    private RelativeLayout mADLayout;
    private boolean isAdLoadOk = false;
    private Handler mMainHandler = null;
    private boolean isAdComplete=true;

    private int mAdTime = 4;
    private final long mAdIntervalTime = 1000;
    private boolean isOnGlobalLayout = false;//是否渲染完成;
    private boolean isInit = false;//是否加载过
    private int num = 0;//绘制被调用的次数


    public WelcomeDialog(@NonNull Activity act) {
        this(act, DEFAULT_TIME,null);
    }
    public WelcomeDialog(@NonNull Activity act, int adTime) {
        this(act, adTime,null);
    }
    public WelcomeDialog(@NonNull Activity act, DialogShowCallBack callBack) {this(act, DEFAULT_TIME,callBack);}
    /**
     * 初始化dialog
     * @param act
     * @param adShowTime
     * @param callBack
     */
    public WelcomeDialog(@NonNull Activity act, int adShowTime,DialogShowCallBack callBack) {
        super(act, R.style.welcomeDialog);
        Main.allMain.isShowWelcomeDialog=true;//至当前dialog状态
        long endTime=System.currentTimeMillis();
        Log.i("zhangyujian","dialog::start::"+(endTime-XHApplication.in().startTime));
        this.activity = act;
        this.mAdTime = adShowTime;
        this.dialogShowCallBack=callBack;
        Window window = this.getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        this.view = this.getLayoutInflater().inflate(R.layout.xh_welcome, null);
        setContentView(view);
        init();
        // 对话框设置监听
        this.setOnDismissListener(onDismissListener);
        this.view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isOnGlobalLayout &&((!isAdLoadOk && num>=3)||(isAdLoadOk &&num >= 5))) {
                    isOnGlobalLayout = true;
                    if (dialogShowCallBack != null) dialogShowCallBack.dialogOnLayout();
                }
                ++num;
            }
        });

        if(dialogShowCallBack!=null)dialogShowCallBack.dialogOnCreate();
        long endTime3=System.currentTimeMillis();
        Log.i("zhangyujian","dialog::oncreate::"+(endTime3-XHApplication.in().startTime));
    }

    /**
     * 初始化view
     */
    private void init() {
        initWelcome();
        XHClick.track(XHApplication.in(), "启动app");
    }

    /**
     * 初始化view
     */
    private void initWelcome() {
        // 初始化
        imageView = (ImageView) view.findViewById(R.id.iv_welcome);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });
        mADSkipContainer = (RelativeLayout) view.findViewById(R.id.skip_container);
        mADLayout = (RelativeLayout) view.findViewById(R.id.ad_layout);
        textSkip = (TextView) view.findViewById(R.id.ad_skip);
        mADSkipContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });
        initAd();
    }
    /**
     * 处理图片
     */
    private void ViewImageWelcome(){
        ImageView image = (ImageView) view.findViewById(R.id.image);
        Glide.with(activity).load(R.drawable.welcome_big).into(image);
//        image.setBackgroundResource(R.drawable.welcome_big);
        int imageWidth = ToolsDevice.getWindowPx(activity).widthPixels / 3;
        image.setPadding(0, imageWidth - 6, 0, 0);
        image.getLayoutParams().width = imageWidth;
        image.setVisibility(View.VISIBLE);
        image.post(new Runnable() {
            @Override
            public void run() {
                Log.i("zhangyujian","");
            }
        });
    }

    private void initAd() {
        //设置广点通广告回调
        WelcomeAdTools.getInstance().setmGdtCallback(
                new WelcomeAdTools.GdtCallback() {
                    @Override
                    public void onAdPresent() {
                        mADSkipContainer.setVisibility(View.VISIBLE);
                        isAdLoadOk = true;
                        AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "gdt", "", "show", "开屏广告位");
                        XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onAdFailed(String reason) {

                    }

                    @Override
                    public void onAdDismissed() {
                        closeDialog();
                    }

                    @Override
                    public void onAdClick() {
                        closeDialog();
                        AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "gdt", "", "click", "开屏广告位");
                        XHClick.mapStat(activity, "ad_click_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onADTick(long millisUntilFinished) {
                    }

                    @Override
                    public ViewGroup getADLayout() {
                        return mADLayout;
                    }

                    @Override
                    public View getTextSikp() {
                        return textSkip;
                    }
                });
        //设置Inmobi广告回调
        WelcomeAdTools.getInstance().setmInMobiNativeCallback(
                new WelcomeAdTools.InMobiNativeCallback() {
                    @Override
                    public void onAdLoadSucceeded(final InMobiNative inMobiNative) {
                        try {
                            isAdLoadOk = true;
                            //成功
                            JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
                            //json字符串解析数据
                            final String landingURL = content.getString("landingURL");
                            String url = content.getJSONObject("screenshots").getString("url");
                            //处理view
                            mADLayout.removeAllViews();
                            View view = LayoutInflater.from(activity).inflate(R.layout.view_ad_inmobi, null);
                            final ImageView imageView = (ImageView) view.findViewById(R.id.image);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            mADLayout.addView(view);

                            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                                    .load(url)
                                    .build();
                            if (bitmapRequest != null)
                                bitmapRequest.into(new SubBitmapTarget() {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                        if (bitmap != null) {
                                            UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(activity).widthPixels, 0, false);
                                            mADSkipContainer.setVisibility(View.VISIBLE);
                                            //曝光
                                            InMobiNative.bind(imageView, inMobiNative);
                                            XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_inmobi");
                                        }
                                    }
                                });

                            //点击
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //点击统计
                                    inMobiNative.reportAdClick(null); //此方法参数通常传null}

                                    //友盟统计
                                    XHClick.track(activity, "点击启动页广告");
                                    XHClick.mapStat(activity, "ad_click_index", "开屏", "sdk_inmobi");

                                    if (!TextUtils.isEmpty(landingURL)) {
                                        Handler handler = new Handler(getMainLooper());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                AppCommon.openUrl(activity, landingURL, true);
                                            }
                                        });
                                    }
                                    closeDialog();
                                }
                            });
                            //回调数据
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        //失败

                    }

                    @Override
                    public void onAdDismissed(InMobiNative inMobiNative) {
                        //广告被点击之后回到app
                        closeDialog();
                    }

                    @Override
                    public void onAdDisplayed(InMobiNative inMobiNative) {
                    }

                    @Override
                    public void onUserLeftApplication(InMobiNative inMobiNative) {
                    }
                });
        //设置XHBanner回调
        WelcomeAdTools.getInstance().setmXHBannerCallback(
                new WelcomeAdTools.XHBannerCallback() {
                    @Override
                    public void onAdLoadSucceeded(final String url, final String loadingUrl) {
                        //处理view
                        mADLayout.removeAllViews();
                        View view = LayoutInflater.from(activity).inflate(R.layout.view_ad_inmobi, null);
                        final ImageView imageView = (ImageView) view.findViewById(R.id.image);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mADLayout.addView(view);

                        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                                .load(url)
                                .build();
                        if (bitmapRequest != null)
                            bitmapRequest.into(new SubBitmapTarget() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                    if (bitmap != null) {
                                        UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(activity).widthPixels, 0, false);
                                        mADSkipContainer.setVisibility(View.VISIBLE);
                                        XHClick.mapStat(activity, "ad_show_index", "开屏", "xh");
                                    }
                                }
                            });

                        //点击
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //友盟统计
                                XHClick.track(activity, "点击启动页广告");
                                XHClick.mapStat(activity, "ad_click_index", "开屏", "xh");

                                if (!TextUtils.isEmpty(loadingUrl)) {
                                    Handler handler = new Handler(getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppCommon.openUrl(activity, loadingUrl, true);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
    }

    private Runnable mCountDownRun = new Runnable() {
        @Override
        public void run() {
            endCountDown();
            if (mAdTime <= 0) {
                closeDialog();
                return;
            }
            if (textSkip != null)
                textSkip.setText(activity.getResources().getString(R.string.skip) + String.valueOf(mAdTime));
            mAdTime--;
            startCountDown(true);
        }
    };

    private void endCountDown() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(getMainLooper());
        }
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private void startCountDown(boolean delayed) {
        if (mMainHandler == null) {
            mMainHandler = new Handler(getMainLooper());
        }
        mMainHandler.postDelayed(mCountDownRun, delayed ? mAdIntervalTime : 0);
    }

    @Override
    public void show() {
        super.show();
        if (dialogShowCallBack != null)
            dialogShowCallBack.dialogState(true);
    }

    /**
     * 关闭dialog
     */
    public void closeDialog() {
        if(!isOnGlobalLayout&&dialogShowCallBack!=null){
            dialogShowCallBack.dialogOnLayout();
        }
        if(mMainHandler!=null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler=null;
        }
        Log.i("zhangyujian","closeDialog");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WelcomeDialog.this.dismiss();
                activity.overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
            }
        }, 280);
    }

    private OnDismissListener onDismissListener = new OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (dialogShowCallBack != null) dialogShowCallBack.dialogState(false);
        }
    };

    public interface DialogShowCallBack {
        /**
         * 当前状态，true:展示，false关闭
         *
         * @param show
         */
        public void dialogState(boolean show);

        /**
         * dialog渲染完成
         */
        public void dialogOnLayout();
        /**
         * dialog oncreate方法执行
         */
        public void dialogOnCreate();

        /**
         * dialog 加载完成
         */
        public void dialogAdComplete();
    }

    public DialogShowCallBack dialogShowCallBack;

    public void setDialogShowCallBack(@NonNull DialogShowCallBack callBack) {
        this.dialogShowCallBack = callBack;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            long endTime1=System.currentTimeMillis();
            Log.i("zhangyujian","dialog::onWindowFocusChanged:222:"+(endTime1-XHApplication.in().startTime));
            isInit = true;
            startCountDown(false);
            long endTime2=System.currentTimeMillis();
            Log.i("zhangyujian","dialog::onWindowFocusChanged:333:"+(endTime2-XHApplication.in().startTime));
            //
            WelcomeAdTools.getInstance().handlerAdData(false, new WelcomeAdTools.AdDataCallBack() {
                @Override
                public void noAdData() {
                    if(isAdComplete){
                        ViewImageWelcome();
                        isAdComplete=false;
                    }
                }
            });
            long endTime=System.currentTimeMillis();
            Log.i("zhangyujian","dialog::onWindowFocusChanged::"+(endTime-XHApplication.in().startTime));
        }
    }

    @Override
    public void onBackPressed() {
//        if (isOnGlobalLayout)
//            super.onBackPressed();
    }
}