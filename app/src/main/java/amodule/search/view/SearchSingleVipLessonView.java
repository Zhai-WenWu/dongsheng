package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.TagTextView;
import amodule.main.view.item.BaseItemView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.web.FullScreenWeb;
import xh.basic.internet.UtilInternet;

public class SearchSingleVipLessonView extends BaseItemView implements View.OnClickListener {

    private ImageView mLessonImg;
    private TextView mTitleText;
    private TextView mInfoText;
    private TextView mNameText;
    private TagTextView mVideoDuration;
    private RelativeLayout mShadowLayout;

    private Map<String, String> mDataMap;

    private final String mStatisticsId = "a_searesult_vip";

    public SearchSingleVipLessonView(Context context) {
        super(context);
        initView(context);
    }

    public SearchSingleVipLessonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchSingleVipLessonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_single_search_viplesson, this);
        mLessonImg = findViewById(R.id.lesson_img);
        mTitleText = findViewById(R.id.lesson_title);
        mInfoText = findViewById(R.id.lesson_info);
        mNameText = findViewById(R.id.lesson_name);
        mVideoDuration = findViewById(R.id.video_duration);
        mShadowLayout = findViewById(R.id.shadow_layout);
        ConstraintLayout layout = findViewById(R.id.shadow_parent);
        int dp_20 = Tools.getDimen(getContext(), R.dimen.dp_20);
        layout.setPadding(dp_20 - mShadowLayout.getPaddingLeft(), dp_20 / 2 - mShadowLayout.getPaddingTop(),
                dp_20 - mShadowLayout.getPaddingRight(), dp_20 - mShadowLayout.getPaddingBottom());
        setOnClickListener(this);
        setVisibility(View.GONE);
    }

    public void setData(Map<String, String> dataMap) {
        if (mDataMap != null)
            mDataMap.clear();
        mDataMap = dataMap;
        if (mDataMap != null && !mDataMap.isEmpty()) {
            String title = mDataMap.get("title");
            if (title == null) {
                title = "";
            }
            mTitleText.setText(title);
            String subTitle = mDataMap.get("subTitle");
            mInfoText.setText(subTitle);
            String name = mDataMap.get("customer");
            mNameText.setText(name);
            setViewImage(mLessonImg, mDataMap.get("img"));
            Map<String, String> videoMap = StringManager.getFirstMap(mDataMap.get("video"));
            String videoDuration = videoMap.get("duration");
            if (!TextUtils.isEmpty(videoDuration)) {
                mVideoDuration.setText(videoDuration);
                mVideoDuration.setVisibility(VISIBLE);
            } else {
                mVideoDuration.setVisibility(GONE);
            }
            setVisibility(View.VISIBLE);
            XHClick.mapStat(getContext(), mStatisticsId, "顶部VIP内容展现量", "");
        } else {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        String jumpBuyVipUrl = mDataMap.get("jumpBuyVip");
        if (!TextUtils.isEmpty(jumpBuyVipUrl) && !LoginManager.isVIP()) {
            Intent intent = AppCommon.parseURL(XHActivityManager.getInstance().getCurrentActivity(), jumpBuyVipUrl);
            intent.putExtra(FullScreenWeb.BACK_PAGE, mDataMap.get("url"));
            XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
        } else {
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mDataMap.get("url"), true);
        }
        XHClick.mapStat(getContext(), mStatisticsId, "顶部VIP内容点击量", "");
    }

}
