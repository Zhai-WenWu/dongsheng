package amodule.answer.activity;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.IObserver;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.widget.PagerSlidingTabStrip;
import amodule.answer.adapter.QAMsgPagerAdapter;
import amodule.answer.model.QAMsgModel;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by sll on 2017/7/27.
 */

public class QAMsgListActivity extends BaseFragmentActivity implements IObserver {
    private PagerSlidingTabStrip mTabStrip;
    private ViewPager mViewPager;
    private QAMsgPagerAdapter mPagerAdapter;
    private ImageView mBackImg;
    private XHWebView mWebView;
    private WebviewManager mWebViewManager;
    private JsAppCommon mJsCommon;

    private ArrayList<QAMsgModel> mModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.qa_msglist_layout);
        initView();
        initData();
        addListener();
        getTabData();
        ObserverManager.getInstence().registerObserver(this, ObserverManager.NOTIFY_UPLOADOVER);
    }

    private void initData() {
        mPagerAdapter = new QAMsgPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);
        mTabStrip.setViewPager(mViewPager);
    }

    private void initView() {
        mBackImg = (ImageView) findViewById(R.id.back_img);
        mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tab);
        mTabStrip.setIndicatorHeight(0);
        mWebViewManager = new WebviewManager(this, loadManager, true);
        mWebView = mWebViewManager.createWebView(R.id.XHWebview);
        mWebViewManager.setJSObj(mWebView, mJsCommon = new JsAppCommon(this, mWebView, loadManager, null));
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    private void addListener() {
        mTabStrip.setListener();
        mTabStrip.setmDelegatePageListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPagerAdapter.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
                        onTabDataReady(data);
                    }
                });
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
        CookieSyncManager.getInstance().sync();
        mWebView.loadUrl(StringManager.replaceUrl(StringManager.API_QA_QAMSGLIST));
    }

    private void onTabDataReady(Object data) {
        loadManager.hideProgressBar();
        if (data == null || TextUtils.isEmpty(data.toString())) {
            finish();
            return;
        }
        if (mModels == null)
            mModels = new ArrayList<QAMsgModel>();
        if (!mModels.isEmpty())
            mModels.clear();
        int selectedPosition = 0;
        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(data);
        for (int i = 0; i < datas.size(); i ++) {
            Map<String, String> map = datas.get(i);
            if (map == null || map.isEmpty())
                continue;
            QAMsgModel model = new QAMsgModel();
            boolean isSelect = map.get("isSelect") == "2";
            if (isSelect)
                selectedPosition = i;
            model.setmPosition(i);
            model.setmType(map.get("type"));
            model.setmTitle(map.get("title"));
            model.setmIsSelect(isSelect);
            model.setmMsgNum(map.get("msgNum"));
            mModels.add(model);
        }
        mPagerAdapter.setData(mModels);
        mViewPager.setCurrentItem(selectedPosition);
        mTabStrip.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefreshCurrFragment) {
            mRefreshCurrFragment = false;
            mPagerAdapter.getCurrentFragment().refreshFragment();
        }
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
