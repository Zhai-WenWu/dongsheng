package amodule.main.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.ad.tools.GdtAdTools;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

import static android.os.Looper.getMainLooper;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * 欢迎页弹框
 */
public class WelcomeDialog extends Dialog {
    private final static int DEFAULT_TIME = 4;
    private Activity activity;
    protected View view;
    protected int height;

    private ImageView imageView;
    private TextView textSkip, textLead;
    private RelativeLayout adHintLayout;
    private RelativeLayout mADSkipContainer;
    private RelativeLayout mADLayout;
    //广告处理
    private ArrayList<String> list_ad = new ArrayList<>();//存储广告类型的集合
    private ArrayList<String> ad_data = new ArrayList<>();//存储对应数据的集合
    private int index_ad = 0;
    private InMobiNative nativeAd;
    private boolean isAdLoadOk = false;
    private boolean isAdLeadClick = false;
    private Handler mMainHandler = null;
    private String tongjiId = "a_ad";

    private int mAdTime = DEFAULT_TIME;
    private final long mAdIntervalTime = 1000;
    private boolean isOnGlobalLayout = false;//是否渲染完成;
    private boolean isInit = false;//是否加载过
    private int num = 0;//绘制被调用的次数

    public WelcomeDialog(@NonNull Activity act) {
        this(act, DEFAULT_TIME);
    }

    public WelcomeDialog(@NonNull Activity act, int adTime) {
        super(act, R.style.welcomeDialog);
        this.activity = act;
        this.mAdTime = adTime;
        Window window = this.getWindow();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        this.view = this.getLayoutInflater().inflate(R.layout.xh_welcome, null);
        setContentView(view);

        // 对话框设置监听
        this.setOnDismissListener(onDismissListener);
        this.view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isOnGlobalLayout && num == 2) {
                    isOnGlobalLayout = true;
                    if (dialogShowCallBack != null) dialogShowCallBack.dialogOnLayout();
                }
                ++num;
            }
        });
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
        imageView = (ImageView) view.findViewById(R.id.iv_welcome);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });
        mADSkipContainer = (RelativeLayout) view.findViewById(R.id.skip_container);
        mADLayout = (RelativeLayout) view.findViewById(R.id.ad_layout);
        textLead = (TextView) findViewById(R.id.ad_vip_lead);
        textSkip = (TextView) view.findViewById(R.id.ad_skip);
        textLead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(activity, tongjiId, "点击会员去广告", "");
                endCountDown();
                AppCommon.openUrl(activity, StringManager.api_vip, true);
                closeDialog();
            }
        });
        textSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(activity, tongjiId, "点击跳过", "");
                endCountDown();
                closeDialog();
            }
        });
//        mADSkipContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                closeDialog();
//            }
//        });

        ImageView image = (ImageView) view.findViewById(R.id.image);
        Glide.with(activity).load(R.drawable.welcome_big).into(image);
        int imageWidth = ToolsDevice.getWindowPx(activity).widthPixels / 3;
        image.setPadding(0, imageWidth - 6, 0, 0);
        image.getLayoutParams().width = imageWidth;
        image.setVisibility(View.VISIBLE);

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
                textSkip.setText("跳过 " + String.valueOf(mAdTime));
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

    /**
     * 处理AD服务端数据
     */
    private void handlerAdData() {
        list_ad.clear();
        ad_data.clear();
        index_ad = 0;
        String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
        if (TextUtils.isEmpty(data)) return;
        ArrayList<Map<String, String>> list = getListMapByJson(data);
        Map<String, String> map = list.get(0);
        if (map.containsKey(AdPlayIdConfig.WELCOME)) {

            ArrayList<Map<String, String>> listTemp = getListMapByJson(map.get(AdPlayIdConfig.WELCOME));
            if (!listTemp.get(0).containsKey("adConfig")) {
                return;
            }
            ArrayList<Map<String, String>> listTemp_config = getListMapByJson(listTemp.get(0).get("adConfig"));
            if (listTemp_config.get(0).containsKey("1")) {
                String temp_1 = listTemp_config.get(0).get("1");
                handlerData(temp_1, list_ad);
            }
            if (listTemp_config.get(0).containsKey("2")) {
                String temp_1 = listTemp_config.get(0).get("2");
                handlerData(temp_1, list_ad);
            }
            if (listTemp_config.get(0).containsKey("3")) {
                String temp_1 = listTemp_config.get(0).get("3");
                handlerData(temp_1, list_ad);
            }
            if (listTemp_config.get(0).containsKey("4")) {
                String temp_1 = listTemp_config.get(0).get("4");
                handlerData(temp_1, list_ad);
            }
            nextAd();
        }
    }

    private void handlerData(String temp, ArrayList<String> list_ad) {
        Map<String, String> map_ad = StringManager.getFirstMap(temp);
        if (map_ad.get("open").equals("2")) {
            list_ad.add(map_ad.get("type"));
            ad_data.add(map_ad.get("data"));
        }
    }

    /**
     * 下一个广告数据
     */
    private void nextAd() {
        if (list_ad.size() > index_ad) {
            if (XHScrollerAdParent.TAG_GDT.equals(list_ad.get(index_ad))) {//gdt
                if (LoginManager.isShowAd())
                    displayGdtAD();
            } else if (XHScrollerAdParent.TAG_INMOBI.equals(list_ad.get(index_ad))) {//inmobi
                if (LoginManager.isShowAd())
                    getInMobi();
            }
        }
    }

    /**
     * 处理inmobi广告
     */
    private void getInMobi() {
        String adid = analysData(ad_data.get(index_ad));
        if (TextUtils.isEmpty(adid)) return;
        nativeAd = new InMobiNative(activity, Long.parseLong(adid),
                new InMobiNative.NativeAdListener() {
                    @Override
                    public void onAdLoadSucceeded(InMobiNative inMobiNative) {
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
                                            InMobiNative.bind(imageView, nativeAd);
                                            XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_inmobi");
                                        }
                                    }
                                });

                            //点击
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //点击统计
                                    nativeAd.reportAdClick(null); //此方法参数通常传null}

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
                        index_ad++;
                        nextAd();
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
        //加载广告
        nativeAd.load();
        Map<String, String> map = new HashMap<>();
        map.put("x-forwarded-for", "8.8.8.8");
        nativeAd.setExtras(map);
    }

    //展示AD
    private void displayGdtAD() {
        String adid = analysData(ad_data.get(index_ad));
        if (TextUtils.isEmpty(adid)) return;
        GdtAdTools.newInstance().showSplashAD(activity, mADLayout, textSkip, adid,
                new third.ad.tools.GdtAdTools.GdtSplashAdListener() {
                    @Override
                    public void onAdPresent() {
                        mADSkipContainer.setVisibility(View.VISIBLE);
                        isAdLoadOk = true;
                        AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "gdt", "", "show", "开屏广告位");
                        XHClick.mapStat(activity, "ad_show_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onAdFailed(String reason) {
                        index_ad++;
                        nextAd();
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
                });
    }

    private String analysData(String data) {
        LinkedHashMap<String, String> map_link = UtilString.getMapByString(data, "&", "=");
        String adid = "";
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        return adid;
    }

    @Override
    public void show() {
        super.show();
        if (dialogShowCallBack != null) dialogShowCallBack.dialogState(true);
    }

    /**
     * 关闭dialog
     */
    public void closeDialog() {

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
    }

    public DialogShowCallBack dialogShowCallBack;

    public void setDialogShowCallBack(@NonNull DialogShowCallBack callBack) {
        this.dialogShowCallBack = callBack;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            isInit = true;
            startCountDown(false);
            handlerAdData();
        }
    }

    @Override
    public void onBackPressed() {//关闭dialog的返回键
//        if (isOnGlobalLayout) super.onBackPressed();
    }
}
