package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.widget.IconTextSpan;
import amodule.main.view.item.BaseItemView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.web.FullScreenWeb;
import xh.basic.internet.UtilInternet;

public class SearchVIPLessonView extends BaseItemView implements View.OnClickListener {

    private ImageView mBackgroundImg;
    private ImageView mLessonImg;
    private ImageView mPlayImg;
    private TextView mTitleText;
    private TextView mInfoText;
    private TextView mNameText;
    private Button mPlayBtn;

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
        mBackgroundImg = (ImageView) findViewById(R.id.lesson_back_img);
        mLessonImg = (ImageView) findViewById(R.id.lesson_img);
        mPlayImg = (ImageView) findViewById(R.id.lesson_play_img);
        mTitleText = (TextView) findViewById(R.id.lesson_title);
        mInfoText = (TextView) findViewById(R.id.lesson_info);
        mNameText = (TextView) findViewById(R.id.lesson_name);
        mPlayBtn = (Button) findViewById(R.id.lesson_play_btn);
        mPlayBtn.setClickable(false);
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
        if (mDataMap != null)
            mDataMap.clear();
        if (o != null) {
            setVisibility(View.VISIBLE);
            mDataMap = StringManager.getFirstMap(o);
            String tag = mDataMap.get("tag");
            Map<String, String> tagMap = StringManager.getFirstMap(tag);
            String tagTitle = tagMap.get("title");
            String tagColor = tagMap.get("color");
            String tagBg = tagMap.get("bgColor");
            String title = mDataMap.get("title");
            IconTextSpan.Builder builder = new IconTextSpan.Builder();
            builder.setBgColorInt(Color.parseColor(tagBg))
                    .setTextColorInt(Color.parseColor(tagColor))
                    .setTextSize(10F)
                    .setRightMargin(3F)
                    .setText(tagTitle)
                    .setBgHeight(16F)
                    .setRadius(1F);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(" ").append(title);
            ssb.setSpan(builder.build(getContext()), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTitleText.setText(ssb);
            String subTitle = mDataMap.get("subTitle");
            mInfoText.setText(subTitle);
            String name = mDataMap.get("customer");
            mNameText.setText(name);
            mPlayImg.setVisibility(TextUtils.equals(mDataMap.get("isVideo"), "2") ? View.VISIBLE : View.GONE);
            setViewImage(mLessonImg, mDataMap.get("img"));
            if (callback != null)
                callback.callback(mDataMap.get("code"));
            XHClick.mapStat(getContext(), mStatisticsId, "顶部VIP内容展现量", "");
        } else {
            setVisibility(View.GONE);
            if (callback != null)
                callback.callback(null);
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

    public interface LessonCallback {
        void callback(String code);
    }
}
