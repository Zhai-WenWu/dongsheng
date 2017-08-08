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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
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
    public final static int DEFAULT_TIME = 8;
    private Activity activity;
    protected View view;
    protected int height;

    private TextView textSkip,textLead;
    private RelativeLayout mADLayout;
//    private WelcomeRelativeLayout welcomeRelativeLayout;
    private boolean isAdLoadOk = false;
    private Handler mMainHandler = null;
    private boolean isAdComplete=true;
    private boolean isAdLeadClick = false;
//    private WelcomeRelativeLayout welcomeRelativeLayout;

    private int mAdTime = 8;//默认时间
    private final long mAdIntervalTime = 1000;
    private boolean isOnGlobalLayout = false;//是否渲染完成;
    private boolean isInit = false;//是否加载过
    private int num = 0;//绘制被调用的次数
    private boolean isTwoShow=false;

    public WelcomeDialog(@NonNull Activity act) {
        this(act, DEFAULT_TIME,null);
    }
    public WelcomeDialog(@NonNull Activity act, int adTime) {
        this(act, adTime,null);
    }
    public WelcomeDialog(@NonNull Activity act, int adTime,boolean isTwoShow) {
        this(act, adTime,null);
        this.isTwoShow= isTwoShow;
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

        Main.isShowWelcomeDialog=true;//至当前dialog状态
        long endTime=System.currentTimeMillis();
        Log.i("zhangyujian","dialog::start::"+(endTime-XHApplication.in().startTime)+"::::"+adShowTime);
        this.activity = act;
        this.mAdTime = adShowTime;
        this.dialogShowCallBack=callBack;
        Window window = this.getWindow();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        this.view = this.getLayoutInflater().inflate(R.layout.xh_welcome, null);
        setContentView(view);
        init();
        // 对话框设置监听
        this.setOnDismissListener(onDismissListener);
        this.view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i("zhangyujian","num:::::::::::::::::::::::"+num);
                layoutCallBack();
                ++num;
            }
        });
        if(dialogShowCallBack!=null)dialogShowCallBack.dialogOnCreate();
        long endTime3=System.currentTimeMillis();
        Log.i("zhangyujian","dialog::oncreate::"+(endTime3-XHApplication.in().startTime));
    }

    private void layoutCallBack(){
        if (!isOnGlobalLayout &&mAdTime<=5 &&((!isAdLoadOk && num>=2)||(isAdLoadOk &&num >= 4))) {
            isOnGlobalLayout = true;
            if (dialogShowCallBack != null) dialogShowCallBack.dialogOnLayout();
        }
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
        mADLayout = (RelativeLayout) view.findViewById(R.id.ad_layout);
        textSkip = (TextView) view.findViewById(R.id.ad_skip);
        textLead = (TextView) findViewById(R.id.ad_vip_lead);
        textLead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("zhangyujian","展示点击：textLeadtextLead：");
                isAdLeadClick=true;
                closeDialog();
            }
        });
        textSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("zhangyujian","展示点击：：跳过");
                closeDialog();
            }
        });
        initAd();
    }

    private void initAd() {
        //设置广点通广告回调
        WelcomeAdTools.getInstance().setmGdtCallback(
                new WelcomeAdTools.GdtCallback() {
                    @Override
                    public void onAdPresent() {
                        mADLayout.setVisibility(View.GONE);
                        Log.i("zhangyujian","GdtCallback");
                        if(mAdTime>5){
                            endCountDown();
                            mAdTime=5;
                            startCountDown(false);
                        }else if(mAdTime<3){
                            closeDialog();
                            return;
                        }
                        showSkipContainer();
                        isAdLoadOk = true;
                        AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "gdt", "", "show", "开屏广告位");
                        XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onAdFailed(String reason) {
                    }

                    @Override
                    public void onAdDismissed() {
                        Log.i("zhangyujian","onAdDismissed");
                        closeDialog();
                    }

                    @Override
                    public void onAdClick() {
                        Log.i("zhangyujian","onAdClick");
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
        //百度开屏
        WelcomeAdTools.getInstance().setBaiduCallback(new WelcomeAdTools.BaiduCallback() {
            @Override
            public void onAdPresent() {
                mADLayout.setVisibility(View.GONE);
                Log.i("tzy","BDCallback");
                if(mAdTime>5){
                    endCountDown();
                    mAdTime=5;
                    startCountDown(false);
                }else if(mAdTime<3){
                    closeDialog();
                    return;
                }
                showSkipContainer();
                isAdLoadOk = true;
                AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "baidu", "", "show", "开屏广告位");
                XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_baidu");
            }

            @Override
            public void onAdDismissed() {
                Log.i("tzy","onAdDismissed");
                closeDialog();
            }

            @Override
            public void onAdFailed(String s) {

            }

            @Override
            public void onAdClick() {
                Log.i("tzy","onAdClick");
                closeDialog();
                AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "baidu", "", "click", "开屏广告位");
                XHClick.mapStat(activity, "ad_click_index", "开屏", "sdk_baidu");
            }

            @Override
            public ViewGroup getADLayout() {
                return mADLayout;
            }
        });
        //设置XHBanner回调
        WelcomeAdTools.getInstance().setmXHBannerCallback(
                new WelcomeAdTools.XHBannerCallback() {
                    @Override
                    public void onAdLoadSucceeded(final String url, final String loadingUrl) {
                        //处理view
                        mADLayout.setVisibility(View.GONE);
                        mADLayout.removeAllViews();
                        isAdLoadOk = true;
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
                                        if(mAdTime>5){
                                            endCountDown();
                                            mAdTime=5;
                                            startCountDown(false);
                                        }else if(mAdTime<3){
                                            closeDialog();
                                            return;
                                        }
                                        showSkipContainer();
                                        UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(activity).widthPixels, 0, false);
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
    private void showSkipContainer(){
        textLead.setVisibility(View.VISIBLE);
        textSkip.setVisibility(View.VISIBLE);
        mADLayout.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        findViewById(R.id.image).setVisibility(View.GONE);
        mADLayout.startAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                textLead.setVisibility(View.VISIBLE);
                textSkip.setVisibility(View.VISIBLE);
                mADLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private Runnable mCountDownRun = new Runnable() {
        @Override
        public void run() {
            endCountDown();
            if (mAdTime <= 0||(mAdTime<=2&&!isAdLoadOk&& LoginManager.isShowAd())) {
                closeDialog();
                return;
            }
            layoutCallBack();
            Log.i("zhangyujian","mAdTime:::"+mAdTime);
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
        Main.isShowWelcomeDialog=false;//至当前dialog状态
        Log.i("zhangyujian","closeDialog");
        WelcomeDialog.this.dismiss();
        if(isAdLeadClick) {
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), StringManager.api_vip, true);
        }
        activity.overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WelcomeAdTools.getInstance().handlerAdData(false, null,isTwoShow);
                }
            }).start();

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