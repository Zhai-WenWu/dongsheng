package amodule.search.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.TagTextView;
import amodule.main.view.item.BaseItemView;

public class SearchMultipleVipLessonView extends BaseItemView {

    private ImageView mLessonImg;
    private TextView mTitleText;
    private TextView mInfoText;
    private TextView mNameText;
    private TagTextView mVideoDuration;
    private ImageView lesson_icon_oval;

    private Map<String, String> mDataMap;

    private final String mStatisticsId = "a_searesult_vip";

    public SearchMultipleVipLessonView(Context context) {
        super(context);
        initView(context);
    }

    public SearchMultipleVipLessonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchMultipleVipLessonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_multiple_search_viplesson, this);
        mLessonImg = findViewById(R.id.lesson_img);
        View shadowView = findViewById(R.id.shadow_layout);
        int imageWidth = (int) ((ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_50)) / 2f);
        int imageHeight = (int) (imageWidth /324f*248);
        mLessonImg.getLayoutParams().width = imageWidth;
        mLessonImg.getLayoutParams().height = imageHeight;
        int itemWidth = imageWidth + shadowView.getPaddingLeft() + shadowView.getPaddingRight();
        int itemHeight = (int) (imageWidth / 324f * 361 + shadowView.getPaddingTop() + shadowView.getPaddingBottom());
        setLayoutParams(new ViewGroup.LayoutParams(itemWidth,itemHeight));
        mTitleText = findViewById(R.id.lesson_title);
        mNameText = findViewById(R.id.lesson_name);
        mInfoText = findViewById(R.id.lesson_info);
        mVideoDuration = findViewById(R.id.video_duration);
        lesson_icon_oval = findViewById(R.id.lesson_icon_oval);
        setVisibility(View.GONE);
    }

    public void setData(Map<String, String> dataMap,int position) {
        mDataMap = dataMap;
        if (mDataMap != null && !mDataMap.isEmpty()) {
            setViewText(mTitleText, mDataMap.get("name"));
            setViewText(mInfoText, mDataMap.get("subTitle"));
            lesson_icon_oval.setVisibility(mInfoText.getVisibility());
            setViewText(mNameText, StringManager.subOverString(mDataMap.get("nickName"),5));
            setViewImage(mLessonImg, mDataMap.get("img"));
            Map<String, String> videoMap = StringManager.getFirstMap(mDataMap.get("video"));
            setViewTextAndVisibility(mVideoDuration, videoMap.get("duration"));
            setVisibility(View.VISIBLE);
            XHClick.mapStat(getContext(), mStatisticsId, "顶部VIP内容展现量" + (position+1), "");
        } else {
            setVisibility(View.GONE);
        }
    }

}
