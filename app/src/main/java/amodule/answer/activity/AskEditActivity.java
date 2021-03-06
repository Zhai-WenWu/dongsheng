package amodule.answer.activity;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.popdialog.util.PushManager;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xh.window.FloatingWindow;
import com.xiangha.R;

import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.LogManager;
import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.answer.model.AskAnswerModel;
import amodule.answer.upload.AskAnswerUploadListPool;
import amodule.answer.view.AskAnswerImgController;
import amodule.answer.view.UploadingView;
import amodule.upload.UploadListControl;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.XHConf;
import aplug.basic.XHInternetCallBack;
import aplug.web.tools.WebviewManager;
import xh.basic.internet.UtilInternet;

/**
 * Created by sll on 2017/7/17.
 */

public class AskEditActivity extends BaseEditActivity implements AskAnswerUploadListPool.UploadOverListener {

    private TextView mPriceText;
    private ImageView mBlackBtn;
    private RelativeLayout mAskDesc;

    private String mAskPrice;//提问价格
    private String mWebUrl;//打开Web支付页面

    private ConnectionChangeReceiver mConnectionChangeReceiver;
    private UploadListPool mListPool;
    private UploadPoolData mUploadPoolData;
    private boolean mIsStopUpload;
    private boolean mIsAno;

    private String mQAID;
    private String mQADetailUrl;//问答详情页的url

    private boolean mIsResuming;
    private boolean mIsFromPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(mIsAskMore ? "追问" : "我问", R.layout.ask_edit_activity);
    }

    private void setListener() {
        mBlackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsAno = !mIsAno;
                mAnonymity = mIsAno ? "2" : "1";
                switchBtn(mIsAno);
                XHClick.mapStat(AskEditActivity.this, "a_ask_publish", "点击匿名按钮", mIsAno ? "点击打开" : "点击关闭");
            }
        });
        mWebViewManager.setOnWebviewLoadFinish(new WebviewManager.OnWebviewLoadFinish() {
            @Override
            public void onLoadFinish() {
                mWebView.setBackgroundColor(0);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelUploadingDialog();
                        mWebView.setVisibility(View.VISIBLE);
                        mWebView.loadUrl("javascript:goAppPay()");
                        XHClick.mapStat(AskEditActivity.this, getTjId(), "点击发布按钮", "吊起支付弹窗");
                    }
                });
            }
        });
        mAskDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(AskEditActivity.this, StringManager.API_QA_QASTATEMENT, false);
                XHClick.mapStat(AskEditActivity.this, "a_ask_publish", "点击【查看问答细则及责任声明】", "");
            }
        });
        mImgController.setOnItemClickDelListener(new AskAnswerImgController.OnItemClickDelListener() {
            @Override
            public void onItemClickDel(Map<String, String> dataMap) {
                XHClick.mapStat(AskEditActivity.this, getTjId(), "删除图片", "");
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mQAType = mIsAskMore ? AskAnswerModel.TYPE_ASK_AGAIN : AskAnswerModel.TYPE_ASK;
    }

    @Override
    protected void initView(String title, int contentResId) {
        super.initView(title, contentResId);
        mPriceText = (TextView) findViewById(R.id.price_text);
        mBlackBtn = (ImageView) findViewById(R.id.black_btn);
        mAskDesc = (RelativeLayout) findViewById(R.id.ask_desc);
        RelativeLayout anoContainer = findViewById(R.id.anonymity_container);
        anoContainer.setVisibility(mIsAskMore ? View.GONE : View.VISIBLE);
        setListener();
        registnetworkListener();
        getLocalData();
    }

    private void registnetworkListener() {
        mConnectionChangeReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {
                Toast.makeText(AskEditActivity.this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
                allStartOrPause(false);
            }

            @Override
            public void wifi() {}

            @Override
            public void mobile() {
                if(!mIsStopUpload && mIsResuming) {
                    allStartOrPause(false);
                    hintNetWork();
                }
            }
        });
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionChangeReceiver,filter);
    }

    private void getLocalData() {
        loadManager.showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                onLocalDataReady(mSQLite.queryFirstData());
            }
        }).start();
    }

    private void onLocalDataReady(final AskAnswerModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mLoadPrice && !mIsAskMore && LoginManager.isLogin())
                    getPriceData();
                else
                    loadManager.hideProgressBar();
                if (model != null) {
                    mModel.setmId(model.getmId());
                    mModel.setmDishCode(model.getmDishCode());
                    if (!TextUtils.isEmpty(mDishCode) && mDishCode.equals(model.getmDishCode())) {
                        mModel = model;
                        mEditText.setText(model.getmText());
                        mIsAno = "2".equals(model.getmAnonymity());
                        if (!mIsAskMore)
                            switchBtn(mIsAno);
                        initImgControllerData(model);
                    }
                }
            }
        });
    }

    private void switchBtn(boolean on) {
        if (mBlackBtn == null)
            return;
        mBlackBtn.setImageResource(on ? R.drawable.i_switch_on : R.drawable.i_switch_off);
    }

    @Override
    protected void onLoginSucc() {
        if (!mLoadPrice && !mIsAskMore)
            getPriceData();
    }

    private boolean mLoadPrice = false;
    private void getPriceData() {
        if (TextUtils.isEmpty(mDishCode) || TextUtils.isEmpty(mAuthorCode) || TextUtils.isEmpty(mType)) {
            this.finish();
            return;
        }
        if (mLoadPrice)
            return;
        loadManager.showProgressBar();
        mLoadPrice = true;
        String params = "code=" + mDishCode + "&authorCode=" + mAuthorCode + "&type=" + mType;
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_GETPRICE, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                boolean success = i >= UtilInternet.REQ_OK_STRING;
                onPriceDataReady(success, success ? StringManager.getFirstMap(o) : null);
            }
        });
    }

    private void onPriceDataReady(boolean succ, Map<String, String> map) {
        loadManager.hideProgressBar();
        if (succ && map != null && !map.isEmpty()) {
            mAskPrice = map.get("price");
            if (!TextUtils.isEmpty(mAskPrice)) {
                mPriceText.setText((TextUtils.equals("0", mAskPrice) || TextUtils.equals("0.0", mAskPrice) || TextUtils.equals("0.00", mAskPrice)) ? "免费" : (mAskPrice + "元"));
                mPriceText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void cancelUpload() {
        if (mListPool == null || mIsStopUpload)
            return;
        XHClick.mapStat(this, getTjId(), "点击发布按钮", mIsAskMore ? "取消上传" : "点返回或X取消上传");
        mListPool.cancelUpload();
    }

    private void allStartOrPause(boolean isAllStart) {
        if (mListPool == null)
            return;
        if (isAllStart)
            showUploadingDialog();
        else
            cancelUploadingDialog();
        mIsStopUpload = !isAllStart;
        mListPool.allStartOrStop(isAllStart ? UploadListPool.TYPE_START : UploadListPool.TYPE_PAUSE);
    }

    private void hintNetWork(){
        final DialogManager dialogManager = new DialogManager(AskEditActivity.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
        .setView(new TitleMessageView(AskEditActivity.this).setText("当前不是WiFi环境，是否继续上传？"))
        .setView(new HButtonView(AskEditActivity.this).setNegativeTextColor(Color.parseColor("#333333"))
        .setNegativeText("取消", v -> dialogManager.cancel())
        .setPositiveTextColor(Color.parseColor("#333333"))
        .setPositiveText("确定", v -> {
            dialogManager.cancel();
            allStartOrPause(true);
        }))).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResuming = false;
        if (mIsFromPause && loadManager != null && loadManager.isShowingProgressBar())
            loadManager.hideProgressBar();
    }

    @Override
    protected boolean handleUpload() {
        if (mModel != null)
            mModel.setmPrice(mAskPrice);
        saveDraft();
        if ("null".equals(ToolsDevice.getNetWorkType(this))) {
            Toast.makeText(this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
            return true;
        }
        endTimer();
        if (mModel != null && mModel.getmId() != -1) {
            mListPool = UploadListControl.getUploadListControlInstance()
                    .add(AskAnswerUploadListPool.class,
                            (int) mModel.getmId(), "", "", "", new UploadListUICallBack() {
                                @Override
                                public void changeState() {

                                }

                                @Override
                                public void changeState(int pos, int index, UploadItemData data) {

                                }

                                @Override
                                public void uploadOver(boolean flag, String responseStr) {

                                }
                            });
            ((AskAnswerUploadListPool) mListPool).setUploadOverListener(this);
            mUploadPoolData = mListPool.getUploadPoolData();
            AskAnswerModel model = mUploadPoolData.getUploadAskAnswerData();
            if(model == null){
                finish();
            }
            if ("wifi".equals(ToolsDevice.getNetWorkType(this))) {
                allStartOrPause(true);
            } else {
                if (hasImgs()) {
                    allStartOrPause(false);
                    hintNetWork();
                } else {
                    allStartOrPause(true);
                }
            }
        }
        return true;
    }

    private void startPay() {
        if (TextUtils.isEmpty(mWebUrl) || TextUtils.isEmpty(mQAID))
            return ;
        String cookieKey= StringManager.appWebUrl.replace(StringManager.appWebTitle, "");
        Map<String,String> mapCookie= XHInternetCallBack.getCookieMap();
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        for(String str:mapCookie.keySet()){
            String temp=str+"="+mapCookie.get(str);
            if(temp.indexOf("device")==0) temp=temp.replace(" ", "");
            LogManager.print(XHConf.log_tag_net,"d", "设置cookie："+temp);
            cookieManager.setCookie(cookieKey, temp);
        }
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();
        mWebView.loadUrl(mWebUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResuming = false;
        mIsFromPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregistnetworkListener();
    }

    public void unregistnetworkListener(){
        if(mConnectionChangeReceiver != null){
            unregisterReceiver(mConnectionChangeReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        if (closePayWindow())
            return;
        super.onBackPressed();
    }

    public boolean closePayWindow() {
        if (mWebView.getVisibility() == View.VISIBLE) {
            mWebView.setVisibility(View.GONE);
            if (!mPayFinish && mIsAskMore) {
                XHClick.mapStat(this, getTjId(), "点击发布按钮", "未发布成功");
            }
            mPayFinish = false;
            return true;
        }
        return false;
    }

    @Override
    protected void onEditTextChanged(CharSequence s, int start, int before, int count) {
        mCountText.setText(s.length() + "/100");
        if (s.length() >= 100) {
            mCountText.setTextColor(Color.parseColor("#ff0000"));
            Toast.makeText(this, "不能继续输入", Toast.LENGTH_SHORT).show();
        } else {
            mCountText.setTextColor(Color.parseColor("#d4d4d4"));
        }
    }

    private DialogManager mUploadingDialog;
    private void showUploadingDialog() {
        if (mUploadingDialog != null && mUploadingDialog.isShowing())
            return;
        if (mUploadingDialog == null) {
            mUploadingDialog = new DialogManager(this);
            mUploadingDialog.createDialog(new ViewManager(mUploadingDialog)
            .setView(new UploadingView(this))).noPadding().setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancelUpload();
                }
            });
        }
        mUploadingDialog.show();
    }

    private void cancelUploadingDialog() {
        if (mUploadingDialog == null || !mUploadingDialog.isShowing())
            return;
        mUploadingDialog.cancel();
    }

    private void showPriceChangeDialog(String msg) {
        final DialogManager dialogManager = new DialogManager(AskEditActivity.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
        .setView(new TitleMessageView(AskEditActivity.this).setText(msg))
        .setView(new HButtonView(AskEditActivity.this)
        .setNegativeText("我知道了", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogManager.cancel();
            }
        }))).show();
        XHClick.mapStat(this, getTjId(), "点击发布按钮", "价格变动通知");
    }

    @Override
    public void onUploadOver(boolean flag, String response) {
        mIsStopUpload = true;
        cancelUploadingDialog();
        if (!flag && !TextUtils.isEmpty(response)) {
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> map = StringManager.getFirstMap(response);
        String type = map.get("type");
        String msg = map.get("msg");
        mQADetailUrl = map.get("appUrl");
        if (!TextUtils.isEmpty(type)) {
            try {
                int t = Integer.parseInt(type);
                if (t > 200 && !TextUtils.isEmpty(msg)) {// >200表示失败，
                    showPriceChangeDialog(msg);
                    onPriceDataReady(true, map);
                } else {// <=200表示成功，吊起支付弹窗
                    mQAID = map.get("id");
                    mWebUrl = map.get("payUrl");
                    if (mIsAskMore) {
                        if (flag) {
                            mSQLite.deleteData(mUploadPoolData.getDraftId());
                            if (mFromHome)
                                startQADetail();
                            ObserverManager.getInstance().notify(ObserverManager.NOTIFY_UPLOADOVER, null, true);
                            finish();
                        }
                    } else {
                        if ("0".equals(mAskPrice) || "0.0".equals(mAskPrice) || "0.00".equals(mAskPrice)) {
                            mSQLite.deleteData(mUploadPoolData.getDraftId());//删除草稿
                            startQADetail();
                            if (flag)
                                ObserverManager.getInstance().notify(ObserverManager.NOTIFY_UPLOADOVER, null, true);
                        } else {
                            startPay();
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        if (!flag && !ToolsDevice.getNetActiveState(this)) {
            XHClick.mapStat(this, getTjId(), "点击发布按钮", "因网络原因发布失败");
        }
    }


    private boolean mPayFinish;
    @Override
    protected void onPayFin(boolean succ, Object data) {
        mPayFinish = true;
        if (succ) {
            mSQLite.deleteData(mUploadPoolData.getDraftId());//删除草稿
            Tools.showToast(this, "支付成功");
            startQADetail();
        } else {
            if (mIsAskMore)
                XHClick.mapStat(this, getTjId(), "点击发布按钮", "未发布成功");
        }
    }

    private void startQADetail() {
        if (TextUtils.isEmpty(mQADetailUrl))
            return;
        AppCommon.openUrl(this, mQADetailUrl, false);
        if (!PushManager.isNotificationEnabled(AskEditActivity.this)) {
            getIsTip();
        }
        this.finish();
    }

    private void getIsTip() {
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_ISTIP, "type=1", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object obj) {
                //比较特殊的弹框，涉及到页面跳转不能影响弹框
                if (i >= UtilInternet.REQ_OK_STRING) {
                    Map<String, String> map = StringManager.getFirstMap(obj);
                    if (map != null && !map.isEmpty() && "2".equals(map.get("isTip"))) {
                        final FloatingWindow window = FloatingWindow.getInstance(XHApplication.in().getApplicationContext());
                        window.setView(new TitleMessageView(XHApplication.in().getApplicationContext())
                                .setText("开启推送通知，用户的追问或评价结果将在第一时间通知你，是否开启？"))
                                .setView(new HButtonView(XHApplication.in().getApplicationContext())
                                        .setPositiveText("是", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                window.cancelFloatingWindow();
                                                PushManager.requestPermission(AskEditActivity.this);
                                                XHClick.mapStat(AskEditActivity.this, "a_ask_push", "提问人推送", "是");
                                            }
                                        })
                                        .setNegativeText("否", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                window.cancelFloatingWindow();
                                                XHClick.mapStat(AskEditActivity.this, "a_ask_push", "提问人推送", "否");
                                            }
                                        })).setCancelable(true).showFloatingWindow();
                    }
                }
            }
        });
    }
}
