package amodule.answer.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.dialogManager.PushManager;
import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.answer.model.AskAnswerModel;
import amodule.answer.upload.AskAnswerUploadListPool;
import amodule.upload.UploadListControl;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.tools.WebviewManager;
import xh.basic.internet.UtilInternet;
import xh.windowview.XhDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView("我问", R.layout.ask_edit_activity);
    }

    private void setListener() {
        mBlackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsAno = !mIsAno;
                mAnonymity = mIsAno ? "2" : "1";
                mBlackBtn.setImageResource(mIsAno ? R.drawable.i_switch_on : R.drawable.i_switch_off);
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
                XHClick.mapStat(AskEditActivity.this, "a_ask_publish", "点击【查看问答细则及责任声明】", "");
                //TODO 点击责任声明
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
        setListener();
        registnetworkListener();
        getLocalData();
    }

    private void registnetworkListener() {
        mConnectionChangeReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {}

            @Override
            public void wifi() {}

            @Override
            public void mobile() {
                if(!mIsStopUpload) {
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
                onLocalDataReady(mSQLite.queryData(mDishCode, mQAType, mQACode));
            }
        }).start();
    }

    private void onLocalDataReady(final AskAnswerModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsAskMore)
                    getPriceData();
                else
                    loadManager.hideProgressBar();
                if (model != null) {
                    mModel = model;
                    mEditText.setText(model.getmText());
                    mIsAno = "2".equals(model.getmAnonymity());
                    String imgs = model.getmImgs();
                    if (!TextUtils.isEmpty(imgs)) {
                        ArrayList<Map<String, String>> imgsArr = StringManager.getListMapByJson(imgs);
                        if (imgsArr != null && !imgsArr.isEmpty()) {
                            for (Map<String, String> img : imgsArr)
                                mImgController.addData(img);
                        }
                    }
                }
            }
        });
    }

    private void getPriceData() {
        if (TextUtils.isEmpty(mDishCode) || TextUtils.isEmpty(mAuthorCode) || TextUtils.isEmpty(mType)) {
            this.finish();
            return;
        }
        String params = "code=" + mDishCode + "&authorCode=" + mAuthorCode + "&type=" + mType;
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_GETPRICE, params, new InternetCallback(this) {
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
            mPriceText.setText(mAskPrice + "元");
            mPriceText.setVisibility(View.VISIBLE);
        }
    }

    private void cancelUpload() {
        if (mListPool == null || mIsStopUpload)
            return;
        XHClick.mapStat(this, getTjId(), "点击发布按钮", mIsAskMore ? "取消上传" : "点返回或X取消上传");
        mListPool.cancelUpload();
    }

    private void allStartOrPause(boolean isAllStart) {
        showUploadingDialog();
        mIsStopUpload = !isAllStart;
        mListPool.allStartOrStop(isAllStart ? UploadListPool.TYPE_START : UploadListPool.TYPE_PAUSE);
    }

    private void hintNetWork(){
        final XhDialog xhDialog = new XhDialog(AskEditActivity.this);
        xhDialog.setTitle("当前不是WiFi环境，是否继续上传？")
                .setCanselButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        xhDialog.cancel();
                    }
                }).setSureButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhDialog.cancel();
                allStartOrPause(true);
            }
        }).setSureButtonTextColor("#333333")
                .setCancelButtonTextColor("#333333")
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean handleUpload() {
        if (mModel != null)
            mModel.setmPrice(mAskPrice);
        endTimer();
        saveDraft();
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
                allStartOrPause(false);
                hintNetWork();
            }
        }
        return true;
    }

    private void startPay() {
        if (!mIsAskMore) {
            if (TextUtils.isEmpty(mWebUrl) || TextUtils.isEmpty(mQAID))
                return ;
            Map<String,String> header= ReqInternet.in().getHeader(this);
            String cookieKey= StringManager.appWebUrl.replace(StringManager.appWebTitle, "");
            String cookieStr=header.containsKey("Cookie")?header.get("Cookie"):"";
            String[] cookie = cookieStr.split(";");
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            for (int i = 0; i < cookie.length; i++) {
                if(cookie[i].indexOf("device")==0) cookie[i]=cookie[i].replace(" ", "");
                LogManager.print(XHConf.log_tag_net,"d", "设置cookie："+i+"::"+cookie[i]);
                cookieManager.setCookie(cookieKey, cookie[i]);
            }
            CookieSyncManager.getInstance().sync();
            mWebView.loadUrl(mWebUrl);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    }

    private Dialog mUploadingDialog;
    private void showUploadingDialog() {
        if (mUploadingDialog != null && mUploadingDialog.isShowing())
            return;
        if (mUploadingDialog == null) {
            mUploadingDialog = new Dialog(this, R.style.dialog);
            mUploadingDialog.setContentView(R.layout.ask_upload_dialoglayout);
            mUploadingDialog.setCancelable(true);
            mUploadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
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
        final XhDialog dialog = new XhDialog(this);
        dialog.setMessage(msg).setCancelable(true).setCanselButton("我知道了", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        XHClick.mapStat(this, getTjId(), "点击发布按钮", "价格变动通知");
    }

    @Override
    public void onUploadOver(boolean flag, String response) {
        mIsStopUpload = true;
        cancelUploadingDialog();
        Map<String, String> map = StringManager.getFirstMap(response);
        String type = map.get("type");
        String msg = map.get("msg");
        mQADetailUrl = map.get("url");
        if (!TextUtils.isEmpty(type)) {
            try {
                int t = Integer.parseInt(type);
                if (t > 200 && !TextUtils.isEmpty(msg)) {// >200表示失败，
                    showPriceChangeDialog(msg);
                } else {// <=200表示成功，吊起支付弹窗
                    mQAID = map.get("id");
                    mWebUrl = map.get("url");
                    startPay();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        if (!flag) {
            XHClick.mapStat(this, getTjId(), "点击发布按钮", "因网络原因发布失败");
        }
    }


    private boolean mPayFinish;
    @Override
    protected void onPayFin(boolean succ, Object data) {
        mPayFinish = true;
        if (succ) {
            mSQLite.deleteData(mUploadPoolData.getDraftId());//删除草稿
            AppCommon.openUrl(this, mQADetailUrl, false);
            if (!PushManager.isNotificationEnabled()) {
                getQANum();
            }
            this.finish();
        } else {
            if (mIsAskMore)
                XHClick.mapStat(this, getTjId(), "点击发布按钮", "未发布成功");
        }
    }

    private void getQANum() {
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_NUM, "type=1", new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                Map<String, String> map = StringManager.getFirstMap(o);
                if (map != null && !map.isEmpty()) {
                    String numStr = map.get("num");
                    if (!TextUtils.isEmpty(numStr)) {
                        try {
                            final int num = Integer.parseInt(numStr);
                            if (num == 1 || num == 5) {
                                final XhDialog dialog = new XhDialog(AskEditActivity.this);
                                dialog.setTitle("开启推送通知，作者回答后将在第一时间通知你，是否开启？")
                                        .setCanselButton("否", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.cancel();
                                                XHClick.mapStat(AskEditActivity.this, "a_ask_push", "提问人第" + num + "次推送", "否");
                                            }
                                        })
                                        .setSureButton("是", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                XHClick.mapStat(AskEditActivity.this, "a_ask_push", "提问人第" + num + "次推送", "是");
                                                PushManager.requestPermission();
                                            }
                                        }).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
    }
}
