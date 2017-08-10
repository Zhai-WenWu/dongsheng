package amodule.main.view.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.LogManager;
import acore.tools.StringManager;
import amodule.main.bean.HomeModuleBean;
import aplug.basic.XHConf;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;

/**
 * Created by sll on 2017/6/26.
 */

public class BaseHomeFragment extends Fragment {
    /** 保存板块信息的key */
    protected static final String MODULEDATA = "moduleData";
    private MainBaseActivity mActivity;
    private WebviewManager mWebViewManager;
    private LoadManager mLoadManager;
    private HomeModuleBean mModuleBean;
    public XHWebView mWebview;
    private int mPosition;

    private RelativeLayout mRootView;

    private boolean LoadOver = false;

    public static BaseHomeFragment instance(HomeModuleBean moduleBean) {
        BaseHomeFragment fragment = new BaseHomeFragment();
        fragment.setModuleBean(moduleBean);
        return (BaseHomeFragment) setArgumentsToFragment(fragment, moduleBean);
    }

    public static Fragment setArgumentsToFragment(Fragment fragment, HomeModuleBean moduleBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MODULEDATA, moduleBean);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setModuleBean(HomeModuleBean mPlateData) {
        this.mModuleBean = mPlateData;
    }

    public HomeModuleBean getModuleBean() {
        return mModuleBean;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = new RelativeLayout(container.getContext());
        mRootView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        LoadOver = false;
        initView();
        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainBaseActivity)activity;
        mLoadManager = mActivity.loadManager;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        loadWeb(isVisibleToUser);
    }

    private void initView() {
        mWebViewManager = new WebviewManager(mActivity, mLoadManager, false);
        mWebview = mWebViewManager.createWebView(0,false);
        mWebViewManager.setJSObj(mWebview, new JsAppCommon(mActivity, mWebview, mLoadManager,null));
        mRootView.addView(mWebview, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    private void loadWeb(boolean isVisibleToUser) {
        if (mLoadManager == null || !isVisibleToUser || LoadOver)
            return;
        mLoadManager.setLoading(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadWebData(false);
            }
        });
    }

    public void loadWebData(boolean isRefresh) {
        if (mModuleBean == null)
            return;
        if (mWebViewManager != null) {
            mWebViewManager.setOpenMode(mModuleBean.getOpenMode());
        }
        String webUrl = mModuleBean.getWebUrl();
        if (TextUtils.isEmpty(webUrl))
            return;
        if (isRefresh)
            mWebview.setScrollY(0);
        if (mModuleBean != null && "2".equals(mModuleBean.getIsSelf())) {
            Map<String,String> header= MallReqInternet.in().getHeader(mActivity);
            String cookieKey= MallStringManager.mall_web_apiUrl.replace(MallStringManager.appWebTitle, "");
            String cookieStr=header.containsKey("Cookie")?header.get("Cookie"):"";
            String[] cookie = cookieStr.split(";");
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            for (int i = 0; i < cookie.length; i++) {
                if(cookie[i].indexOf("device")==0) cookie[i]=cookie[i].replace(" ", "");
                LogManager.print(XHConf.log_tag_net,"d", "设置cookie："+i+"::"+cookie[i]);
                cookieManager.setCookie(cookieKey, cookie[i]);
            }
        }
        mWebview.loadUrl(webUrl);
        LoadOver = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebview != null) {
            mWebview.stopLoading();
            mWebview.removeAllViews();
            mWebview.destroy();
            mWebview = null;
        }
        if (mRootView != null) {
            mRootView.removeAllViews();
            mRootView = null;
        }
    }
}
