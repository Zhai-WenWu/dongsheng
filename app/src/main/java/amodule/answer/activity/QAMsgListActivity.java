package amodule.answer.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.dialogManager.PushManager;
import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.StringManager;
import amodule.answer.adapter.QAMsgPagerAdapter;
import amodule.answer.model.QAMsgModel;
import amodule.answer.view.NumTabController;
import amodule.answer.view.NumTabStripView;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by sll on 2017/7/27.
 */

public class QAMsgListActivity extends BaseFragmentActivity {
    private LinearLayout mTabContainer;
    private ViewPager mViewPager;
    private QAMsgPagerAdapter mPagerAdapter;
    private ImageView mBackImg;
    private XHWebView mWebView;
    private WebviewManager mWebViewManager;
    private JsAppCommon mJsCommon;

    private ArrayList<QAMsgModel> mModels;

    private NumTabController mNumTabController;

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
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(5);
        mNumTabController = new NumTabController(this, mTabContainer);
    }

    private void initView() {
        mBackImg = (ImageView) findViewById(R.id.back_img);
        mTabContainer = (LinearLayout) findViewById(R.id.tab_container);
        mWebViewManager = new WebviewManager(this, loadManager, true);
        mWebView = mWebViewManager.createWebView(R.id.XHWebview);
        mWebViewManager.setJSObj(mWebView, mJsCommon = new JsAppCommon(this, mWebView, loadManager, null));
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
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
            public void getData(String data) {
                if (data == null) {
                    finish();
                } else {
                    onTabDataReady(data);
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                QAMsgListActivity.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mNumTabController.setOnTabClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(((NumTabStripView)v).getPosition());
            }
        });
    }

    private void onPageSelected(int position) {
        if (mTabContainer == null || mTabContainer.getChildCount() == 0 || position >= mTabContainer.getChildCount())
            return;

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
        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(data);
        for (Map<String, String> map : datas) {
            QAMsgModel model = new QAMsgModel();
            model.setmTitle(map.get("title"));
            model.setmIsSelect(map.get("isSelected") == "2");
            model.setmMsgNum(map.get("msgNum"));
            mModels.add(model);
        }
        mNumTabController.setData(mModels);
        mPagerAdapter.setData(mModels);
    }
}
