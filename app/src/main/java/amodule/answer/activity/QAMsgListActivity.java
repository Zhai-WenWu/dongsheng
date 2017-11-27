package amodule.answer.activity;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.popdialog.util.PushManager;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.IObserver;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by sll on 2017/7/27.
 */

public class QAMsgListActivity extends BaseFragmentActivity implements IObserver {

    private LinearLayout mTabContainer;
    private ImageView mBackImg;
    private XHWebView mWebView;
    private WebviewManager mWebViewManager;
    private JsAppCommon mJsCommon;

    private int mCurrSelectedPos = -1;

    private ArrayList<Map<String, String>> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.qa_msglist_layout);
        initView();
        addListener();
        getTabData();
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_UPLOADOVER);
    }

    private void initView() {
        mBackImg = (ImageView) findViewById(R.id.back_img);
        mTabContainer = (LinearLayout) findViewById(R.id.tab_container);
        mWebViewManager = new WebviewManager(this, loadManager, true);
        mWebView = mWebViewManager.createWebView(R.id.XHWebview);
        mWebViewManager.setJSObj(mWebView, mJsCommon = new JsAppCommon(this, mWebView, loadManager, null));
    }

    private void addListener() {
        mBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mJsCommon.setOnGetDataListener(new JsAppCommon.OnGetDataListener() {
            @Override
            public void getData(final String data) {
                QAMsgListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onTabDataReady(mTabContainer != null && mTabContainer.getChildCount() > 0, data);
                    }
                });
            }
        });
        mWebViewManager.setOnWebviewLoadFinish(new WebviewManager.OnWebviewLoadFinish() {
            @Override
            public void onLoadFinish() {
                mWebView.postInvalidateDelayed(200);
            }
        });

    }

    private void getTabData() {
        loadManager.showProgressBar();
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
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();
        mWebView.setVisibility(View.VISIBLE);
       loadMsgList(false);
    }

    private void onTabDataReady(boolean refNum, Object data) {
        loadManager.hideProgressBar();
        if (data == null || TextUtils.isEmpty(data.toString())) {
            finish();
            return;
        }
        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(data);
        mDatas = datas;
        int defSelectPos = 0;
        for (int i = 0; i < datas.size(); i ++) {
            Map<String, String> map = datas.get(i);
            if (map == null || map.isEmpty())
                continue;
            String msgNum = map.get("msgNum");
            if (refNum) {
                setMsgNum(msgNum, i);
                continue;
            }
            String title = map.get("title");
            boolean isSelect = "2".equals(map.get("isSelect"));
            if (isSelect)
                defSelectPos = i;
            if (!TextUtils.isEmpty(title)) {
                View tabView = LayoutInflater.from(this).inflate(R.layout.tab_strip_numlayout, null, false);
                TextView tab = (TextView) tabView.findViewById(R.id.psts_tab_title);
                tab.setText(title);
                final int pos = i;
                tabView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSelection(pos);
                        loadMsgList(true);
                    }
                });
                mTabContainer.addView(tabView);
                setMsgNum(msgNum, i);
            }
        }
        if (mTabContainer.getChildCount() == 0)
            return;
        if (!refNum)
            setSelection(defSelectPos);
    }

    /**
     * 设置消息气泡
     * @param numStr
     * @param position
     */
    private void setMsgNum (String numStr, int position) {
        try {
            TextView numView = (TextView) mTabContainer.getChildAt(position).findViewById(R.id.num);
            int num = Integer.parseInt(numStr);
            if (num > 99) {
                numView.setText(numStr + "+");
            } else if (num > 0) {
                numView.setText(numStr);
                numView.setVisibility(View.VISIBLE);
            } else {
                numView.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置选中的tab
     * @param position 要选中的位置
     */
    private void setSelection(int position) {
        if (mCurrSelectedPos == position)
            return;
        else {
            boolean isInit = mCurrSelectedPos == -1;
            if (mCurrSelectedPos > -1) {
                View oldSelectView = mTabContainer.getChildAt(mCurrSelectedPos);
                oldSelectView.setSelected(false);
                TextView tvNum = (TextView) oldSelectView.findViewById(R.id.num);
                if (tvNum.getVisibility() == View.VISIBLE)
                    tvNum.setVisibility(View.INVISIBLE);
            }
            mCurrSelectedPos = position;
            View currSelectedView = mTabContainer.getChildAt(mCurrSelectedPos);
            currSelectedView.setSelected(true);
            if (!isInit)
                currSelectedView.findViewById(R.id.num).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 加载消息列表
     * @param isRef
     */
    private void loadMsgList (final boolean isRef) {
        if (mWebView == null)
            return;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                loadManager.setLoading(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWebView.loadUrl(StringManager.replaceUrl(StringManager.API_QA_QAMSGLIST + "?notify=" + (PushManager.isNotificationEnabled(QAMsgListActivity.this) ? "2" : "1") + (isRef ? "&type=" + mDatas.get(mCurrSelectedPos).get("type"): "")));
                    }
                });
            }
        });
    }

    private boolean mIsFromPause;
    @Override
    protected void onResume() {
        super.onResume();
        if (mIsFromPause) {
            mIsFromPause = false;
            loadManager.hideProgressBar();
        }
        if (mRefreshCurrFragment) {
            mRefreshCurrFragment = false;
            loadMsgList(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsFromPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstence().unRegisterObserver(this);
    }

    private boolean mRefreshCurrFragment;
    @Override
    public void notify(String name, Object sender, Object data) {
        if (ObserverManager.NOTIFY_UPLOADOVER.equals(name) && data != null) {
            if (data instanceof Boolean)
                mRefreshCurrFragment = (boolean) data;
        }
    }
}
