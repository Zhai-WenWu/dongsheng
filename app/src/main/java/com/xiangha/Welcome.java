package com.xiangha;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.dialogManager.VersionOp;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.db.ShowBuyData;
import amodule.dish.db.ShowBuySqlite;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.UploadDishControl;
import amodule.main.Main;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.service.alarm.PushAlarm;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.ad.tools.GdtAdTools;
import third.ad.tools.InMobiAdTools;
import third.ad.tools.TencenApiAdTools;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

import static java.lang.System.currentTimeMillis;
import static xh.basic.tool.UtilString.getListMapByJson;

public class Welcome extends BaseActivity {
    private ImageView imageView;
    private TextView textSkip, textLead;
    private RelativeLayout adHintLayout;
    private boolean isLoaded = false;

    private final int DEFAULT_MODE = 0;
    private final int ONCLICK_MODE = 1;

    private boolean isAdLoadOk = false;
    private boolean isAdLeadClick = false;
    private boolean isInit;

    private RelativeLayout mADSkipContainer;
    private RelativeLayout mADLayout;

    private String tongjiId = "a_ad";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //初始化
        InMobiAdTools.getInstance().initSdk(XHApplication.in());
        setContentView(R.layout.xh_welcome);
        XHClick.track(XHApplication.in(), "启动app");
        initWelcome();
        new Thread(new Runnable() {
            @Override
            public void run() {
                XHClick.saveFirstStartTime(XHApplication.in());
                XHClick.registerMonthSuperProperty(XHApplication.in());
            }
        }).start();
    }

    /**
     * 老版兼容问题
     */
    private void compatibleData() {
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
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            saveDataInDB(json);
                            UtilFile.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_buyBurden);
                        }
                    }).start();
                }
                // 245版32以后，数据库字段更新
                String verName = VersionOp.getVerName(XHApplication.in());
                verName = verName.replace(".", "");
                // Log.i("FRJ","verName: " + verName);
                if (Integer.parseInt(verName) <= 245) {
                    try {
                        UploadDishSqlite sqlite = new UploadDishSqlite(XHApplication.in());
                        sqlite.insert(sqlite.selectById(1));
                        sqlite.deleteById(1);
                    } catch (Exception e) {
                        UploadDishSqlite sqlite = new UploadDishSqlite(XHApplication.in());
                        sqlite.deleteDatabase(XHApplication.in());
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
     * 应用数据初始化
     */
    private void init() {
        //讯飞语音： 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(XHApplication.in(), SpeechConstant.APPID + "=56ce9191");

        // 取消自我唤醒
        XGPushManager.clearLocalNotifications(XHApplication.in());
        PushAlarm.closeTimingWake(XHApplication.in());

        // 自动登录
        AppCommon.getCommonData(null);
        AppCommon.saveAppData();

//		// 存储device
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(FileManager.xmlKey_device, ToolsDevice.getPhoneDevice(this));
//		UtilFile.saveShared(this, FileManager.xmlFile_appInfo, map);
//
//		// 存储启动时间
//		map = new HashMap<String, String>();
//		map.put(FileManager.xmlKey_startTime, System.currentTimeMillis() + "");
//		UtilFile.saveShared(this, FileManager.xmlFile_appInfo, map);
//		//修改所有上传中的普通菜谱状态
//		UploadDishControl.getInstance().updataAllUploadingDish(getApplicationContext());

        startCountDown(false);
        compatibleData();

        AppCommon.clearCache();
        TencenApiAdTools.getTencenApiAdTools().getLocation();

        new Thread() {
            @Override
            public void run() {
                super.run();

                // 存储device
                Map<String, String> map = new HashMap<String, String>();
                map.put(FileManager.xmlKey_device, ToolsDevice.getPhoneDevice(XHApplication.in()));
                UtilFile.saveShared(XHApplication.in(), FileManager.xmlFile_appInfo, map);

                // 存储启动时间
                map = new HashMap<String, String>();
                map.put(FileManager.xmlKey_startTime, currentTimeMillis() + "");
                UtilFile.saveShared(XHApplication.in(), FileManager.xmlFile_appInfo, map);
                //修改所有上传中的普通菜谱状态
                UploadDishControl.getInstance().updataAllUploadingDish(XHApplication.in());

                //清楚上传中的数据库数据
                SubjectSqlite subjectSqlite = SubjectSqlite.getInstance(XHApplication.in());
                ArrayList<SubjectData> array = subjectSqlite.selectByState(SubjectData.UPLOAD_ING);
                for (SubjectData data : array) {
                    subjectSqlite.deleteById(data.getId());
                }
            }
        }.start();

    }

    private void initWelcome() {
        // 初始化
        imageView = (ImageView) findViewById(R.id.iv_welcome);
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isLoaded)
                    loadIndex(ONCLICK_MODE);
            }
        });
        mADSkipContainer = (RelativeLayout) findViewById(R.id.skip_container);
        mADLayout = (RelativeLayout) findViewById(R.id.ad_layout);
        textLead = (TextView) findViewById(R.id.ad_vip_lead);
        textSkip = (TextView) findViewById(R.id.ad_skip);
        textLead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(Welcome.this, tongjiId, "点击会员去广告", "");
                isAdLeadClick = true;
                endCountDown();
                if (!isLoaded)
                    loadIndex(DEFAULT_MODE);
            }
        });
        textSkip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(Welcome.this, tongjiId, "点击跳过", "");
                endCountDown();
                if (!isLoaded)
                    loadIndex(DEFAULT_MODE);
            }
        });
        ImageView image = (ImageView) findViewById(R.id.image);
        Glide.with(this).load(R.drawable.welcome_big).into(image);
        int imageWidth = ToolsDevice.getWindowPx(this).widthPixels / 3;
        image.setPadding(0, imageWidth - 6, 0, 0);
        image.getLayoutParams().width = imageWidth;
        image.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        handlerAdData();
    }


    //展示AD
    private void displayGdtAD() {
        Log.i("zhangyujian", "gdt::::kai");
        String adid = analysData(ad_data.get(index_ad));
        if (TextUtils.isEmpty(adid)) return;
        GdtAdTools.newInstance().showSplashAD(this, mADLayout, textSkip, adid,
                new third.ad.tools.GdtAdTools.GdtSplashAdListener() {
                    @Override
                    public void onAdPresent() {
                        mADSkipContainer.setVisibility(View.VISIBLE);
                        isAdLoadOk = true;
                        AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "gdt", "", "show", "开屏广告位");
                        XHClick.mapStat(Welcome.this, "ad_show_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onAdFailed(String reason) {
                        index_ad++;
                        nextAd();
                    }

                    @Override
                    public void onAdDismissed() {
                        if (!isLoaded)
                            loadIndex(DEFAULT_MODE);
                    }

                    @Override
                    public void onAdClick() {
                        if (!isLoaded)
                            loadIndex(ONCLICK_MODE);
                        AdConfigTools.getInstance().postTongji(AdPlayIdConfig.WELCOME, "gdt", "", "click", "开屏广告位");
                        XHClick.mapStat(Welcome.this, "ad_click_index", "开屏", "sdk_gdt");
                    }

                    @Override
                    public void onADTick(long millisUntilFinished) {
                        int time = (int) (millisUntilFinished / 1000);
                        mADSkipContainer.setVisibility(View.VISIBLE);
                        textSkip.setText("跳过 " + time);
                        if (time == 1 && !isAdLoadOk) {
                            if (!isLoaded) {
                                findViewById(R.id.ad_layout).setVisibility(View.GONE);
                                loadIndex(DEFAULT_MODE);
                            }
                        }
                    }
                });
    }

    /**
     * 加载主界面
     *
     * @param mode
     */
    private void loadIndex(int mode) {
        // 开启主界面
//		if (isLoaded)
        isLoaded = true;
        try {
            Intent intent = new Intent(this, Main.class);
            intent.putExtras(this.getIntent());
            switch (mode) {
                case DEFAULT_MODE:
                    openFromOther(intent);
                    break;
                case ONCLICK_MODE:
                    openFromOther(intent);
                    break;
            }
            if (mADLayout != null)
                mADLayout.setVisibility(View.GONE);
            if (mADSkipContainer != null)
                mADSkipContainer.setVisibility(View.GONE);
            startActivity(intent);
            if (isAdLeadClick) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppCommon.openUrl(Welcome.this, StringManager.api_vip, true);
                        Welcome.this.finish();
                    }
                }, 200);
            } else {
                Welcome.this.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 外部吊起app
    private void openFromOther(Intent intent) {
        // 从通知吊起整个应用
        if (this.getIntent().getExtras() != null) {
            Bundle bundle = this.getIntent().getExtras();
            if ("notify".equals(bundle.getString("from"))) {
                intent.putExtra("url", bundle.getString("url"));
                return;
            }
        }
        // 从外部直接吊起整个应用
        if (this.getIntent().getData() != null) {
            if (Main.allMain != null) {
                Main.allMain.doExitMain();
            }
            String url = this.getIntent().getData().toString();
            intent.putExtra("url", url);
        }
    }

    private void saveDataInDB(String json) {
        ShowBuyData buyData = new ShowBuyData();
        ShowBuySqlite sqlite = new ShowBuySqlite(this);
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
                int id = sqlite.insert(this, buyData);
                if (id > 0)
                    AppCommon.buyBurdenNum++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sqlite.close();
    }

    @Override
    public void onBackPressed() {
        if (!isLoaded)
            loadIndex(DEFAULT_MODE);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        endCountDown();
    }

    private int mAdTime = 4;
    private final long mAdIntervalTime = 1000;
    private Runnable mCountDownRun = new Runnable() {
        @Override
        public void run() {
            endCountDown();
            if (mAdTime <= 0) {
                if (!isLoaded)
                    loadIndex(DEFAULT_MODE);
                return;
            }
            if (textSkip != null)
                textSkip.setText("跳过 " + String.valueOf(mAdTime));
            mAdTime--;
            startCountDown(true);
        }
    };

    private Handler mMainHandler = null;

    private void startCountDown(boolean delayed) {
        if (mMainHandler == null) {
            mMainHandler = new Handler(getMainLooper());
        }
        mMainHandler.postDelayed(mCountDownRun, delayed ? mAdIntervalTime : 0);
    }

    private void endCountDown() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(getMainLooper());
        }
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private ArrayList<String> list_ad = new ArrayList<>();//存储广告类型的集合
    private ArrayList<String> ad_data = new ArrayList<>();//存储对应数据的集合
    private int index_ad = 0;
    private InMobiNative nativeAd;

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
            ArrayList<Map<String, String>> listTemp_config = StringManager.getListMapByJson(listTemp.get(0).get("adConfig"));
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

    private void getInMobi() {
        String adid = analysData(ad_data.get(index_ad));
        if (TextUtils.isEmpty(adid)) return;
        nativeAd = new InMobiNative(this, Long.parseLong(adid),
                new InMobiNative.NativeAdListener() {
                    @Override
                    public void onAdLoadSucceeded(InMobiNative inMobiNative) {
                        try {
                            isAdLoadOk = true;
                            //成功
                            JSONObject content = new JSONObject((String) inMobiNative.getAdContent());
                            //json字符串解析数据
                            Log.i("zhanguyujian", "XHScrollerInMobi:::" + content);
                            final String landingURL = content.getString("landingURL");
                            String url = content.getJSONObject("screenshots").getString("url");
                            //处理view
                            mADLayout.removeAllViews();
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
                                            UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(Welcome.this).widthPixels, 0, false);
                                            mADSkipContainer.setVisibility(View.VISIBLE);
                                            //曝光
                                            InMobiNative.bind(imageView, nativeAd);
                                            XHClick.mapStat(Welcome.this, "ad_show_index", "开屏", "sdk_inmobi");
                                        }
                                    }
                                });

                            //点击
                            imageView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //点击统计
                                    nativeAd.reportAdClick(null); //此方法参数通常传null}

                                    //友盟统计
                                    XHClick.track(Welcome.this, "点击启动页广告");
                                    XHClick.mapStat(Welcome.this, "ad_click_index", "开屏", "sdk_inmobi");

                                    if (!TextUtils.isEmpty(landingURL)) {
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                AppCommon.openUrl(Welcome.this, landingURL, true);
                                            }
                                        });


                                    }
                                    if (!isLoaded)
                                        loadIndex(ONCLICK_MODE);

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
                        if (!isLoaded)
                            loadIndex(ONCLICK_MODE);
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

    private String analysData(String data) {
        LinkedHashMap<String, String> map_link = UtilString.getMapByString(data, "&", "=");
        String adid = "";
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        return adid;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInit) {
            init();
            isInit = true;

            //从XHAppliance onCreate方法
//			ReqInternet.init(getApplicationContext());
            //从XHAppliance onCreate方法
//			LoadImage.init(getApplicationContext());

            new Thread() {
                @Override
                public void run() {
                    super.run();
                    MobclickAgent.setDebugMode(true);
                    OnlineConfigAgent.getInstance().updateOnlineConfig(XHApplication.in());

                    CookieManager.getInstance().removeAllCookie();
                }
            }.start();


            AdConfigTools.getInstance().setRequest(XHApplication.in());
            AppCommon.saveConfigData(XHApplication.in());
        }
    }
}
