package amodule.answer.activity;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.dialogManager.PushManager;
import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.StringManager;
import acore.widget.PagerSlidingTabStrip;
import amodule.answer.adapter.QAMsgPagerAdapter;
import amodule.answer.model.QAMsgModel;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by sll on 2017/7/27.
 */

public class QAMsgListActivity extends BaseFragmentActivity {
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
                        if (data == null) {
                            finish();
                        } else {
                            onTabDataReady(data);
                        }
                    }
                });
            }
        });

    }

    //TODO 测试的url，注意后期删除
    private void getTabData() {
        mWebView.loadUrl("http://appweb.ixiangha.com:9812/qa/myQa?notify=" + (PushManager.isNotificationEnabled() ? "2" : "1"));
    }

    private void onTabDataReady(Object data) {
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
}
