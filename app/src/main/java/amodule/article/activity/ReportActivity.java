package amodule.article.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.MsgScrollView;
import amodule.article.view.ReportItem;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by sll on 2017/5/26.
 */

public class ReportActivity extends BaseActivity {

    private MsgScrollView mReportScrooView;
    private LinearLayout mReportContainer;
    private TextView mAdminDesc;
    private LinearLayout mAdminContainer;
    private TextView mReportInfo;
    private TextView mName;
    private Button mCommitBtn;

    private String mUserCode;
    private String mCode;
    private String mType;
    private String mCommendId;
    private String mReplayId;
    private String mReportName;

    private boolean mFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("举报", 2, 0, R.layout.c_view_bar_title, R.layout.report_layout);
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        mUserCode = intent.getStringExtra("userCode");
        mCode = intent.getStringExtra("code");
        mCommendId = intent.getStringExtra("commendId");
        mReplayId = intent.getStringExtra("replayId");
        mReportName = intent.getStringExtra("reportName");
        initView();
        addListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LoginManager.isLogin() && !mFlag) {
            mFlag = true;
            Intent intent = new Intent(this, LoginByAccout.class);
            this.startActivity(intent);
            return;
        }
        if (!LoginManager.isLogin() && mFlag) {
            finish();
            return;
        }
        getReportData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView() {
        mReportScrooView = (MsgScrollView) findViewById(R.id.report_scrollview);
        mReportContainer = (LinearLayout) findViewById(R.id.report_container);
        mAdminDesc = (TextView) findViewById(R.id.admin_report_desc);
        mAdminContainer = (LinearLayout) findViewById(R.id.admin_report_container);
        mReportInfo = (TextView) findViewById(R.id.report_info);
        mCommitBtn = (Button) findViewById(R.id.report_commit);
        mName = (TextView) findViewById(R.id.title);
        mName.setText(mReportName);
    }

    private void addListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.report_info:
                        AppCommon.openUrl(ReportActivity.this, StringManager.api_agreementReport, true);
                        break;
                    case R.id.report_commit:
                        onCommitClick();
                        break;
                }
            }
        };
        mReportInfo.setOnClickListener(clickListener);
        mCommitBtn.setOnClickListener(clickListener);

    }

    private void getReportData() {
        String url = StringManager.API_COMMENTS_REPORT + "?type=" + mType;
        ReqEncyptInternet.in().doEncypt(url, "", new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= UtilInternet.REQ_OK_STRING) {
                    onDataReady(StringManager.getListMapByJson(o));
                } else {
                    onDataReady(null);
                }
            }
        });
    }

    private ReportItem mLastSelectedReportChild;
    private ReportItem mLastSelectedAdminChild;

    private void onDataReady(ArrayList<Map<String, String>> mapDatas) {
        if (mapDatas == null || mapDatas.size() < 1) {

            return;
        }
        Map<String, String> map = mapDatas.get(0);
        if (map != null) {
            String reason = map.get("reason");
            if (!TextUtils.isEmpty(reason)) {
                ArrayList<Map<String, String>> reasons = StringManager.getListMapByJson(reason);
                if (reasons != null && reasons.size() > 0) {
                    addView(mReportContainer, reasons.get(0));
                }
            }
            if (LoginManager.isManager()) {
                String moreReason = map.get("more_reason");
                if (!TextUtils.isEmpty(moreReason)) {
                    ArrayList<Map<String, String>> moreReas = StringManager.getListMapByJson(moreReason);
                    if (moreReas != null && moreReas.size() > 0) {
                        addView(mAdminContainer, moreReas.get(0));
                    }
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

    private void addView(final ViewGroup parentView, Map<String, String> map) {
        if (map != null && !map.isEmpty() && parentView != null) {
            int index = 0;
            Set<Map.Entry<String, String>> entry = map.entrySet();
            Iterator iterator = entry.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> data = (Map.Entry<String, String>) iterator.next();
                if (data != null) {
                    final ReportItem item = new ReportItem(this);
                    parentView.addView(item, index);
                    item.setData(data.getKey(), data.getValue(), index);
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
                    index++;
                }
            }
        }
    }

    public void onCommitClick() {
        if (mLastSelectedReportChild == null) {
            Tools.showToast(ReportActivity.this, "请选择理由");
            return;
        }
        mCommitBtn.setClickable(false);
        String url = StringManager.API_COMMIT_REPORT + "?type=" + mType + "&code=" + mCode + "&commendId=" + mCommendId + "&replayId=" + mReplayId
                + "&reportUcode=" + mUserCode + "&reasonId=" + mLastSelectedReportChild.getKey() + "&operationId=" + (mLastSelectedAdminChild == null ? "" : mLastSelectedAdminChild.getKey());
        ReqEncyptInternet.in().doEncypt(url, "", new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                ReportActivity.this.finish();
            }
        });
    }
}
