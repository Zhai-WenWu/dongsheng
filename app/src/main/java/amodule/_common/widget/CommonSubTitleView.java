package amodule._common.widget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.ITitleStaticCallback;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule._common.widget.baseview.BaseSubTitleView;

/**
 * Created by sll on 2017/11/15.
 */

public class CommonSubTitleView extends BaseSubTitleView implements ITitleStaticCallback {

    private TextView mTitle1;
    private TextView mTitle2;

    public CommonSubTitleView(Context context) {
        super(context, R.layout.subtitle_relativelayout);
    }

    public CommonSubTitleView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.subtitle_relativelayout);
    }

    public CommonSubTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.subtitle_relativelayout);
    }

    @Override
    protected void initView() {
        mTitle1 = (TextView) findViewById(R.id.text1);
        mTitle2 = (TextView) findViewById(R.id.text2);
    }

    @Override
    protected void onDataReady(Map<String, String> map) {
        Map<String, String> titleMap = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_TITLE));
        WidgetUtility.setTextToView(mTitle1, titleMap.get("text1"));
        WidgetUtility.setTextToView(mTitle2, titleMap.get("text2"));
        this.setTitle1ClickListener(v -> {
            String url1 = titleMap.get("url1");
            if (TextUtils.isEmpty(url1))
                return;
            AppCommon.openUrl((Activity) CommonSubTitleView.this.getContext(), url1, true);
        });
        this.setTitle2ClickListener(v -> {
            String url2 = titleMap.get("url2");
            if (TextUtils.isEmpty(url2))
                return;
            AppCommon.openUrl((Activity) CommonSubTitleView.this.getContext(), url2, true);
            if (mStatisticCallback != null) {
                mStatisticCallback.onStatistic(id, twoLevel, threeLevel, -1);
            } else {
                if (!TextUtils.isEmpty(id)) {
                    if (!TextUtils.isEmpty(twoLevel)) {
                        XHClick.mapStat(getContext(), id, twoLevel, twoLevel + titleMap.get("text2"));
                    }
                }
            }
        });
    }

    public void setTitle1ClickListener(OnClickListener listener) {
        if (listener != null) {
            mTitle1.setOnClickListener(listener);
        }
    }

    public void setTitle2ClickListener(OnClickListener listener) {
        if (listener != null) {
            mTitle2.setOnClickListener(listener);
        }
    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }

    private StatisticCallback mStatisticCallback;

    @Override
    public void setTitleStaticCallback(StatisticCallback callback) {
        if (null == callback) return;
        mStatisticCallback = callback;
    }
}
