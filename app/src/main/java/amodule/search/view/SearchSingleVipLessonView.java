package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import acore.tools.ToolsDevice;
import acore.widget.TagTextView;
import amodule.main.view.item.BaseItemView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.web.FullScreenWeb;
import xh.basic.internet.UtilInternet;

public class SearchSingleVipLessonView extends BaseItemView{

    private ImageView mLessonImg;
    private TextView mTitleText;
    private TextView mInfoText;
    private TextView mNameText;
    private TextView mDescText;
    private TagTextView mVideoDuration;

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
        View shadowView = findViewById(R.id.shadow_layout);
        int itemWidth = ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context,R.dimen.dp_20) * 2 + shadowView.getPaddingLeft() + shadowView.getPaddingRight();
        setLayoutParams(new ViewGroup.LayoutParams(itemWidth,ViewGroup.LayoutParams.WRAP_CONTENT));
        mLessonImg = findViewById(R.id.lesson_img);
        mDescText = findViewById(R.id.lesson_desc);
        mTitleText = findViewById(R.id.lesson_title);
        mInfoText = findViewById(R.id.lesson_info);
        mNameText = findViewById(R.id.lesson_name);
        mVideoDuration = findViewById(R.id.video_duration);
        setVisibility(View.GONE);
    }

    public void setData(Map<String, String> dataMap) {
        mDataMap = dataMap;
        if (mDataMap != null && !mDataMap.isEmpty()) {
            setViewText(mTitleText,mDataMap.get("name"));
            setViewText(mDescText,mDataMap.get("title"));
            setViewText(mInfoText,mDataMap.get("subTitle"));
            setViewText(mNameText,mDataMap.get("nickName"));
            setViewImage(mLessonImg, mDataMap.get("img"));
            Map<String, String> videoMap = StringManager.getFirstMap(mDataMap.get("video"));
            setViewTextAndVisibility(mVideoDuration,videoMap.get("duration"));
            setVisibility(View.VISIBLE);
            XHClick.mapStat(getContext(), mStatisticsId, "顶部VIP内容展现量", "");
        } else {
            setVisibility(View.GONE);
        }
    }

}
