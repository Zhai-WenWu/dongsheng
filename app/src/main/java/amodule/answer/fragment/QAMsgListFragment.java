package amodule.answer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.Map;

import acore.dialogManager.PushManager;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.LogManager;
import acore.tools.StringManager;
import amodule.answer.model.QAMsgModel;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by sll on 2017/7/28.
 */

public class QAMsgListFragment extends Fragment {

    private static final String MDATA = "model";
    private QAMsgModel mModelData;

    private BaseFragmentActivity mActivity;
    private LoadManager mLoadManager;
    private XHWebView mWebView;
    private WebviewManager mWebViewManager;

    private RelativeLayout mWebContainer;

    private boolean mIsCreateView;

    public static QAMsgListFragment newInstance(QAMsgModel arguments) {
        QAMsgListFragment fragment = new QAMsgListFragment();
        fragment.setModelData(arguments);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MDATA, arguments);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qa_msg_container, null);
        mWebContainer = (RelativeLayout) view.findViewById(R.id.web_container);

        mWebViewManager = new WebviewManager(mActivity, mLoadManager, false);
        mWebView = mWebViewManager.createWebView(0);
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebViewManager.setJSObj(mWebView, new JsAppCommon(mActivity, mWebView, mLoadManager, null));
        mWebViewManager.setOnWebviewLoadFinish(new WebviewManager.OnWebviewLoadFinish() {
            @Override
            public void onLoadFinish() {
                mLoadManager.hideProgressBar();
            }
        });
        mWebContainer.addView(mWebView);
        Map<String,String> header= ReqInternet.in().getHeader(mActivity);
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
        mIsCreateView = true;
        if (getUserVisibleHint())
            loadUrl();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setModelData(QAMsgModel modelData) {
        mModelData = modelData;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseFragmentActivity) context;
        mLoadManager = mActivity.loadManager;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mIsCreateView)
            loadUrl();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadUrl() {
        if (mLoadManager == null || mWebView == null)
            return;
        mLoadManager.showProgressBar();
        mWebView.loadUrl(StringManager.replaceUrl(StringManager.API_QA_QAMSGLIST + "?notify=" + (PushManager.isNotificationEnabled() ? "2" : "1") + "&type=" + mModelData.getmType()));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsCreateView = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void refreshFragment () {
        loadUrl();
    }
}
