package amodule.answer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.StringManager;
import amodule.answer.model.QAMsgModel;
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
        mWebViewManager.setJSObj(mWebView, new JsAppCommon(mActivity, mWebView, mLoadManager, null));
        mWebViewManager.setOnWebviewLoadFinish(new WebviewManager.OnWebviewLoadFinish() {
            @Override
            public void onLoadFinish() {
                mLoadManager.hideProgressBar();
            }
        });
        mWebContainer.addView(mWebView);
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
    public void onResume() {
        super.onResume();
        loadUrl();
    }

    private void loadUrl() {
        mLoadManager.showProgressBar();
        mWebView.loadUrl(StringManager.replaceUrl(StringManager.API_QA_QAMSGLIST + "&type=" + mModelData.getmType()));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
