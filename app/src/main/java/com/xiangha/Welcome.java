package com.xiangha;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.ad.tools.WelcomeAdTools;
import xh.basic.tool.UtilImage;

public class Welcome extends BaseActivity {
    private TextView textSkip, textLead;
    private boolean isInit;
    private boolean isAdLeadClick = false;
    private int mAdTime = 8;//默认时间
    private boolean isAdLoadOk = false;
    private Handler mMainHandler = null;
    private final long mAdIntervalTime = 1000;

    private RelativeLayout mADLayout;

    private String tongjiId = "a_ad";
    private boolean isclose=false;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = this.getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        super.onCreate(savedInstanceState);
        //初始化
        setContentView(R.layout.xh_welcome);
        XHClick.mapStat(this, "kaiping_two", "开屏","");
        init();
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
        mADLayout = (RelativeLayout) findViewById(R.id.ad_layout);
        textSkip = (TextView) findViewById(R.id.ad_skip);
        textLead = (TextView) findViewById(R.id.ad_vip_lead);
        textLead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdLeadClick=true;
                closeActivity();
            }
        });
        textSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivity();
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
                            closeActivity();
                            return;
                        }
                        showSkipContainer();
                        isAdLoadOk = true;
                        XHClick.mapStat(Welcome.this, "ad_show_index", "开屏", "sdk_gdt");
                        Log.i("zhangyujian","开屏展示");
                    }

                    @Override
                    public void onAdFailed(String reason) {
                    }

                    @Override
                    public void onAdDismissed() {
                        Log.i("zhangyujian","onAdDismissed");
                        Log.i("zhangyujian","onAdDismissed::;"+isclose);
                        closeActivity();
                    }

                    @Override
                    public void onAdClick() {
                        Log.i("zhangyujian","onAdClick");
                        closeActivity();
                        XHClick.mapStat(Welcome.this, "ad_click_index", "开屏", "sdk_gdt");
                        Log.i("zhangyujian","开屏点击");
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
        //设置XHBanner回调
        WelcomeAdTools.getInstance().setmXHBannerCallback(
                new WelcomeAdTools.XHBannerCallback() {
                    @Override
                    public void onAdLoadSucceeded(final String url, final String loadingUrl) {
                        //处理view
                        mADLayout.setVisibility(View.GONE);
                        mADLayout.removeAllViews();
                        isAdLoadOk = true;
                        View view = LayoutInflater.from(Welcome.this).inflate(R.layout.view_ad_inmobi, null);
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
                                            closeActivity();
                                            return;
                                        }
                                        showSkipContainer();
                                        UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(Welcome.this).widthPixels, 0, false);
                                        XHClick.mapStat(Welcome.this, "ad_show_index", "开屏", "xh");
                                    }
                                }
                            });

                        //点击
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //友盟统计
                                XHClick.track(Welcome.this, "点击启动页广告");
                                XHClick.mapStat(Welcome.this, "ad_click_index", "开屏", "xh");

                                if (!TextUtils.isEmpty(loadingUrl)) {
                                    Handler handler = new Handler(getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppCommon.openUrl(Welcome.this, loadingUrl, true);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showSkipContainer(){
        findViewById(R.id.line_1).setVisibility(View.VISIBLE);
        findViewById(R.id.ad_linear).setVisibility(View.VISIBLE);
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
                closeActivity();
                return;
            }
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            isInit = true;
            startCountDown(false);
            //
            WelcomeAdTools.getInstance().handlerAdData(false, new WelcomeAdTools.AdNoDataCallBack() {
                @Override
                public void noAdData() {
                    if(LoginManager.isShowAd()){
                        XHClick.mapStat(Welcome.this,"ad_no_show","开屏","");
                    }
                }
            }, true);
        }
    }
    /**
     * 关闭dialog
     */
    public void closeActivity() {
        Log.i("zhangyujian","isclose:::"+isclose);
        if(!isclose) {
            isclose=true;
            if (mMainHandler != null) {
                mMainHandler.removeCallbacksAndMessages(null);
                mMainHandler = null;
            }
            if (isAdLeadClick) {
                try {
                    AppCommon.openUrl(Welcome.this, StringManager.getVipUrl(false) + "&vipFrom=开屏广告会员免广告", true);
                } catch (Exception e) {};
            }
            this.finish();
//            isclose=false;
        }
//        this.overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
    }
}
