package amodule.answer.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.MsgScrollView;
import amodule.article.view.ReportItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;
import xh.windowview.XhDialog;

/**
 * Created by sll on 2017/7/17.
 */

public class QAReportActivity extends BaseActivity {

    private MsgScrollView mReportScrooView;
    private LinearLayout mReportContainer;
    private TextView mAdminDesc;
    private LinearLayout mAdminContainer;
    private ImageView mReportInfo;
    private TextView mName;
    private TextView mBlackText;
    private Button mCommitBtn;
    private RelativeLayout mBlackListContainer;
    private ImageView mBlackListSwitchBtn;

    private String mDishCode = "";//菜谱code
    private String mAskAuthorCode = "";//提问者的code （被举报的用户code）
    private String mAuthorCode = "";//菜谱作者的code
    private String mReportName = "";//被举报的用户名
    private String mQACode = "";//被举报的问答code

    private ReportItem mLastSelectedReportChild;
    private ReportItem mLastSelectedAdminChild;

    private boolean mLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("举报", 2, 0, R.layout.report_view_bar_title, R.layout.qa_report_layout);
        initData();
        initView();
        addListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LoginManager.isLogin()) {
            finish();
            return;
        }
        if (!mLoaded) {
            loadManager.setLoading(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getReportData();
                }
            }, false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDishCode = bundle.getString("dishCode");
            mAskAuthorCode = bundle.getString("askAuthorCode");
            mAuthorCode = bundle.getString("authorCode");
            mReportName = bundle.getString("reportName", "");
            mQACode = bundle.getString("qaCode");
        }
    }

    private void initView() {
        mReportScrooView = (MsgScrollView) findViewById(R.id.report_scrollview);
        mReportContainer = (LinearLayout) findViewById(R.id.report_container);
        mAdminDesc = (TextView) findViewById(R.id.admin_report_desc);
        mAdminContainer = (LinearLayout) findViewById(R.id.admin_report_container);
        mReportInfo = (ImageView) findViewById(R.id.icon_report);
        mCommitBtn = (Button) findViewById(R.id.report_commit);
        mName = (TextView) findViewById(R.id.title);
        mBlackText = (TextView) findViewById(R.id.blacklist_text);
        if (!TextUtils.isEmpty(mReportName))
            mName.setText("举报 " + mReportName);
        mBlackListContainer = (RelativeLayout) findViewById(R.id.blacklist_container);
        mBlackListSwitchBtn = (ImageView) findViewById(R.id.black_switch_btn);
    }

    private void addListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.icon_report:
                        AppCommon.openUrl(QAReportActivity.this, StringManager.api_agreementReport, true);
                        break;
                    case R.id.report_commit:
                        onCommitClick();
                        break;
                    case R.id.black_switch_btn:
                        if (!mBlackListSwitchBtn.isSelected()) {
                            final XhDialog dialog = new XhDialog(QAReportActivity.this);
                            dialog.setCancelable(true)
                                    .setTitle("进入黑名单的用户将永远不能向你的菜谱进行付费提问，是否拉黑？")
                                    .setCanselButton("否", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.cancel();
                                            XHClick.mapStat(QAReportActivity.this, "a_ask_report", "打开拉黑按钮", "选择【否】");
                                        }
                                    })
                                    .setSureButton("是", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.cancel();
                                            mBlackListSwitchBtn.setSelected(true);
                                            XHClick.mapStat(QAReportActivity.this, "a_ask_report", "打开拉黑按钮", "选择【是】（拉黑）");
                                        }
                                    })
                                    .show();
                        } else {
                            mBlackListSwitchBtn.setSelected(false);
                            XHClick.mapStat(QAReportActivity.this, "a_ask_report", "关闭拉黑按钮", "");
                        }
                        break;
                }
            }
        };
        mReportInfo.setOnClickListener(clickListener);
        mCommitBtn.setOnClickListener(clickListener);
        mBlackListSwitchBtn.setOnClickListener(clickListener);
    }

    /**
     * 点击提交按钮
     */
    public void onCommitClick() {
        if (mLastSelectedReportChild == null) {
            Tools.showToast(QAReportActivity.this, "请选择举报原因");
            return;
        }
        mCommitBtn.setClickable(false);
        ArrayList<String> keys = new ArrayList<String>();
        if (mLastSelectedReportChild != null && !TextUtils.isEmpty(mLastSelectedReportChild.getKey()))
            keys.add(mLastSelectedReportChild.getKey());
        if (mLastSelectedAdminChild != null && !TextUtils.isEmpty(mLastSelectedAdminChild.getKey()))
            keys.add(mLastSelectedAdminChild.getKey());
        if (mBlackListSwitchBtn != null && mBlackListSwitchBtn.isSelected())
            keys.add(mBlackListSwitchBtn.getTag().toString());
        String params = "";
        if (keys.size() > 0) {
            for (int i = 0; i < keys.size(); i ++) {
                params += ("keys[" + i + "]=" + keys.get(i));
                if (i < keys.size() - 1)
                    params += "&";
            }
        }
        params += (TextUtils.isEmpty(params) ? "" : "&") + "code=" + mDishCode + "&askAuthorCode=" + mAskAuthorCode + "&qaCode=" + mQACode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_COMMITREPORT, params, new InternetCallback(QAReportActivity.this) {
            @Override
            public void loaded(int i, String s, Object o) {
                QAReportActivity.this.finish();
            }
        });
    }

    /**
     * 获取举报数据
     */
    private void getReportData() {
        loadManager.showProgressBar();
        if (TextUtils.isEmpty(mAuthorCode)) {
            onDataReady(0, null);
            return;
        }
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_GETREPORT, "authorCode=" + mAuthorCode + "&askAuthorCode=" + mAskAuthorCode, new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= UtilInternet.REQ_OK_STRING) {
                    onDataReady(i, StringManager.getListMapByJson(o));
                } else {
                    onDataReady(i, null);
                }
            }
        });
    }

    /**
     * 举报数据加载完成
     */
    private void onDataReady(int flag, ArrayList<Map<String, String>> mapDatas) {
        mLoaded = true;
        if (mapDatas == null || mapDatas.size() < 1) {
            loadManager.loadOver(flag, 1, true);
            return;
        }
        loadManager.hideProgressBar();
        Map<String, String> map = mapDatas.get(0);
        if (map != null) {
            String reason = map.get("common");
            if (!TextUtils.isEmpty(reason)) {
                ArrayList<Map<String, String>> reasons = StringManager.getListMapByJson(reason);
                if (reasons != null && reasons.size() > 0) {
                    addView(mReportContainer, reasons);
                }
            }
            Map<String, String> mapBlack = StringManager.getFirstMap(map.get("userList"));
            if (mapBlack != null && !mapBlack.isEmpty()) {
                String key = mapBlack.get("key");
                String text = mapBlack.get("text");
                String isDef = mapBlack.get("isDefault");//是否拉黑 1.否， 2.是
                if (!TextUtils.isEmpty(text)) {
                    if (mBlackText != null) {
                        mBlackText.setText(text);
                        mBlackText.setVisibility(View.VISIBLE);
                    }
                    if (mBlackListContainer != null)
                        mBlackListContainer.setVisibility(View.VISIBLE);
                    if (mBlackListSwitchBtn != null) {
                        mBlackListSwitchBtn.setTag(key);
                        mBlackListSwitchBtn.setSelected("2".equals(isDef));
                    }
                }
            }
            String moreReason = map.get("adminList");
            if (!TextUtils.isEmpty(moreReason)) {
                ArrayList<Map<String, String>> moreReas = StringManager.getListMapByJson(moreReason);
                if (moreReas != null && moreReas.size() > 0) {
                    addView(mAdminContainer, moreReas);
                }
            }
        }
        boolean showContainer = false;
        if (mAdminContainer.getChildCount() > 0) {
            mAdminDesc.setVisibility(View.VISIBLE);
            showContainer = true;
        }
        if (mReportContainer.getChildCount() > 0) {
            showContainer = true;
        }
        if (showContainer)
            mReportScrooView.setVisibility(View.VISIBLE);
    }

    /**
     * 添加View
     * @param parentView
     * @param maps
     */
    private void addView(final ViewGroup parentView,ArrayList<Map<String, String>> maps) {
        if (maps != null && !maps.isEmpty() && parentView != null) {
            for (Map<String, String> map : maps) {
                String key = map.get("key");
                String value = map.get("text");
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    int index = parentView.getChildCount();
                    ReportItem item = getItemView(parentView, key, value, index);
                    parentView.addView(item, index);
                }
            }
        }
    }

    /**
     * 生成View
     * @param parentView
     * @param key
     * @param value
     * @param index
     * @return
     */
    private ReportItem getItemView(final ViewGroup parentView, String key, String value, int index) {
        final ReportItem item = new ReportItem(this);
        item.setData(key, value, index);
        item.setOnItemClickListener(new ReportItem.OnItemClickListener() {
            @Override
            public void onItemClick() {
                if (parentView == mReportContainer) {
                    if (mLastSelectedReportChild == null) {
                        mLastSelectedReportChild = item;
                        mLastSelectedReportChild.setSelected(true);
                    } else if (mLastSelectedReportChild == item) {
                        mLastSelectedReportChild.setSelected(false);
                        mLastSelectedReportChild = null;
                    } else {
                        mLastSelectedReportChild.setSelected(false);
                        mLastSelectedReportChild = item;
                        mLastSelectedReportChild.setSelected(true);
                    }
                } else if (parentView == mAdminContainer) {
                    if (mLastSelectedAdminChild == null) {
                        mLastSelectedAdminChild = item;
                        mLastSelectedAdminChild.setSelected(true);
                    } else if (mLastSelectedAdminChild == item) {
                        mLastSelectedAdminChild.setSelected(false);
                        mLastSelectedAdminChild = null;
                    } else {
                        mLastSelectedAdminChild.setSelected(false);
                        mLastSelectedAdminChild = item;
                        mLastSelectedAdminChild.setSelected(true);
                    }
                }
            }
        });
        return item;
    }
}
