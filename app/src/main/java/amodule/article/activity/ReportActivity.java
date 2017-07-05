package amodule.article.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

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
    private RelativeLayout mBackBtn;

    private String mUserCode = "";
    private String mCode = "";
    private String mType = "";
    private String mCommentId = "";
    private String mReplayId = "";
    private String mReportName = "";
    private String mReportContent = "";
    private String mReportType = "2";//1:主题，2:评论（默认），3:回复

    private boolean mFlag;
    private boolean mLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("举报", 2, 0, 0, R.layout.report_layout);
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        mCode = intent.getStringExtra("code"); //主题code
        mUserCode = intent.getStringExtra("userCode"); //被举报人的code
        mReportName = intent.getStringExtra("reportName");//被举报人的名字
        mCommentId = intent.getStringExtra("commentId");
        mReplayId = intent.getStringExtra("replayId");
        mReportContent = intent.getStringExtra("reportContent");
        //获取举报内容列表参数 1、主题；2、评论；3、回复（默认2）
        mReportType = intent.getStringExtra("reportType");
//        initTitle();
        initView();
        addListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LoginManager.isLogin()
                || (!TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                        && !TextUtils.isEmpty(mUserCode)
                        && mUserCode.equals(LoginManager.userInfo.get("code")))) {
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

    private void initTitle() {
        if(Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);

            RelativeLayout bar_title = (RelativeLayout)findViewById(R.id.barTitle);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }

    private void initView() {
        mReportScrooView = (MsgScrollView) findViewById(R.id.report_scrollview);
        mReportContainer = (LinearLayout) findViewById(R.id.report_container);
        mAdminDesc = (TextView) findViewById(R.id.admin_report_desc);
        mAdminContainer = (LinearLayout) findViewById(R.id.admin_report_container);
        mReportInfo = (TextView) findViewById(R.id.report_info);
        mCommitBtn = (Button) findViewById(R.id.report_commit);
        mName = (TextView) findViewById(R.id.title);
        mName.setText("举报 " + mReportName);
        mBackBtn = (RelativeLayout) findViewById(R.id.back);
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
                    case R.id.back:
                        ReportActivity.this.finish();
                        break;
                }
            }
        };
        mReportInfo.setOnClickListener(clickListener);
        mCommitBtn.setOnClickListener(clickListener);
        mBackBtn.setOnClickListener(clickListener);
    }

    private void getReportData() {
        loadManager.showProgressBar();
        ReqEncyptInternet.in().doEncypt(StringManager.API_COMMENTS_REPORT, "type=" + mType + "&reportType=" + mReportType, new InternetCallback(this) {
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

    private ReportItem mLastSelectedReportChild;
    private ReportItem mLastSelectedAdminChild;

    private void onDataReady(int flag, ArrayList<Map<String, String>> mapDatas) {
        mLoaded = true;
        if (mapDatas == null || mapDatas.size() < 1) {
            loadManager.loadOver(flag, 1, true);
            return;
        }
        loadManager.hideProgressBar();
        Map<String, String> map = mapDatas.get(0);
        if (map != null) {
            String reason = map.get("reason");
            if (!TextUtils.isEmpty(reason)) {
                ArrayList<Map<String, String>> reasons = StringManager.getListMapByJson(reason);
                if (reasons != null && reasons.size() > 0) {
                    addView(mReportContainer, reasons.get(0));
                }
            }
            String moreReason = map.get("more_reason");
            if (!TextUtils.isEmpty(moreReason)) {
                ArrayList<Map<String, String>> moreReas = StringManager.getListMapByJson(moreReason);
                if (moreReas != null && moreReas.size() > 0) {
                    addView(mAdminContainer, moreReas.get(0));
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
            if (parentView == mReportContainer) {
                for (int i = 0; i < map.size(); i ++) {
                    if (parentView == mReportContainer) {
                        String key = String.valueOf(i + 1);
                        String value = map.get(key);
                        if (!TextUtils.isEmpty(value)) {
                            int index = parentView.getChildCount();
                            ReportItem item = getItemView(parentView, key, value, index);
                            if (item != null)
                                parentView.addView(item, index);

                        }
                    } else if (parentView == mAdminContainer) {
                    }
                }
            } else if (parentView == mAdminContainer) {
                String value1 = map.get("40");
                if (!TextUtils.isEmpty(value1)) {
                    int index = parentView.getChildCount();
                    ReportItem item = getItemView(parentView, "40", value1, index);
                    if (item != null)
                        parentView.addView(item, index);
                }
                String value = map.get("20");
                if (!TextUtils.isEmpty(value)) {
                    int index = parentView.getChildCount();
                    ReportItem item = getItemView(parentView, "20", value, index);
                    if (item != null)
                        parentView.addView(item, index);
                }
            }
        }
    }

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

    public void onCommitClick() {
        if (mLastSelectedReportChild == null) {
            Tools.showToast(ReportActivity.this, "请选择举报原因");
            return;
        }
        mCommitBtn.setClickable(false);
        String params = "type=" + mType
                + "&code=" + mCode
                + "&commentId=" + (mCommentId == null ? "" : mCommentId)
                + "&replayId=" + (mReplayId == null ? "" : mReplayId)
                + "&reportUcode=" + mUserCode
                + "&reasonId=" + mLastSelectedReportChild.getKey()
                + "&operationId=" + (mLastSelectedAdminChild == null ? "" : mLastSelectedAdminChild.getKey())
                + "&reportContent=" + mReportContent;
        ReqEncyptInternet.in().doEncypt(StringManager.API_COMMIT_REPORT, params, new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                ReportActivity.this.finish();
            }
        });
    }
}
