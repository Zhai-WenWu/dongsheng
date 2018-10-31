package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import acore.widget.IconTextSpan;
import acore.widget.TagTextView;
import amodule.main.view.item.BaseItemView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.web.FullScreenWeb;
import xh.basic.internet.UtilInternet;

public class SearchVIPLessonView extends BaseItemView implements View.OnClickListener {

    private ImageView mLessonImg;
    private TextView mTitleText;
    private TextView mInfoText;
    private TextView mNameText;
    private TagTextView mVideoDuration;
    private RelativeLayout mShadowLayout;

    private Map<String, String> mDataMap;

    private final String mStatisticsId = "a_searesult_vip";

    public SearchVIPLessonView(Context context) {
        super(context);
        initView(context);
    }

    public SearchVIPLessonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchVIPLessonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_search_viplesson, this);
        mLessonImg = (ImageView) findViewById(R.id.lesson_img);
        mTitleText = (TextView) findViewById(R.id.lesson_title);
        mInfoText = (TextView) findViewById(R.id.lesson_info);
        mNameText = (TextView) findViewById(R.id.lesson_name);
        mVideoDuration = findViewById(R.id.video_duration);
        mShadowLayout = findViewById(R.id.shadow_layout);
        ConstraintLayout layout = findViewById(R.id.shadow_parent);
        int dp_20 = Tools.getDimen(getContext(), R.dimen.dp_20);
        layout.setPadding(dp_20-mShadowLayout.getPaddingLeft(),dp_20/2-mShadowLayout.getPaddingTop(),
                dp_20-mShadowLayout.getPaddingRight(),dp_20-mShadowLayout.getPaddingBottom());
        setOnClickListener(this);
        setVisibility(View.GONE);
    }

    public void searchLesson(String searchKey, LessonCallback callback) {
        if (TextUtils.isEmpty(searchKey))
            return;
        String params = "keywords=" + searchKey;
        ReqEncyptInternet.in().doEncypt(StringManager.API_SEARCH_COURSE_DISH, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= UtilInternet.REQ_OK_STRING) {
                    onDataReady(o, callback);
                } else {
                    onDataReady(null, callback);
                }
            }
        });
    }

    private void onDataReady(Object o, LessonCallback callback) {
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mDataMap != null)
                    mDataMap.clear();
                if (o != null) {
                    mDataMap = StringManager.getFirstMap(o);
                }
                if(mDataMap!= null && !mDataMap.isEmpty()){
                    String tag = mDataMap.get("tag");
                    Map<String, String> tagMap = StringManager.getFirstMap(tag);
                    String tagColor = tagMap.get("color");
                    String tagBg = tagMap.get("bgColor");
                    String title = mDataMap.get("title");
                    if (tagBg == null || tagColor == null && callback != null) {
                        callback.callback(null);
                        return;
                    }
                    if (title == null) {
                        title = "";
                    }
                    mTitleText.setText(title);
                    String subTitle = mDataMap.get("subTitle");
                    mInfoText.setText(subTitle);
                    String name = mDataMap.get("customer");
                    mNameText.setText(name);
                    setViewImage(mLessonImg, mDataMap.get("img"));
                    Map<String,String> videoMap = StringManager.getFirstMap(mDataMap.get("video"));
                    String videoDuration = videoMap.get("duration");
                    if(!TextUtils.isEmpty(videoDuration)){
                        mVideoDuration.setText(videoDuration);
                        mVideoDuration.setVisibility(VISIBLE);
                    }else{
                        mVideoDuration.setVisibility(GONE);
                    }
                    if (callback != null)
                        callback.callback(mDataMap.get("code"));
                    setVisibility(View.VISIBLE);
                    XHClick.mapStat(getContext(), mStatisticsId, "顶部VIP内容展现量", "");
                } else {
                    setVisibility(View.GONE);
                    if (callback != null)
                        callback.callback(null);
                }
            }
        });
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

    public interface LessonCallback {
        void callback(String code);
    }
}
