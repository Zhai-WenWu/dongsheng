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
import amodule._common.helper.WidgetDataHelper;
import amodule._common.widget.baseview.BaseSubTitleView;

/**
 * Created by sll on 2017/11/15.
 */

public class CommonSubTitleView extends BaseSubTitleView {

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
        Map<String, String> titleMap = StringManager.getFirstMap(map.get("title"));
        WidgetDataHelper.setTextToView(mTitle1,titleMap.get("text1"));
        WidgetDataHelper.setTextToView(mTitle2,titleMap.get("text2"));
        this.setTitle1ClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url1 = titleMap.get("url1");
                if (TextUtils.isEmpty(url1))
                    return;
                AppCommon.openUrl((Activity) CommonSubTitleView.this.getContext(), url1, true);
            }
        });
        this.setTitle2ClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url2 = titleMap.get("url2");
                if (TextUtils.isEmpty(url2))
                    return;
                AppCommon.openUrl((Activity) CommonSubTitleView.this.getContext(), url2, true);
                if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                    XHClick.mapStat(getContext(),id,twoLevel,twoLevel+titleMap.get("text2"));
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

    public void setTitle1Text(int resid) {
        if (resid > 0) {
            mTitle1.setText(resid);
            mTitle1.setVisibility(View.VISIBLE);
        } else {
            mTitle1.setVisibility(View.GONE);
        }
    }

    public void setTitle1TextColor(int color) {
        mTitle1.setTextColor(color);
    }

    public void setTitle2Text(int resid) {
        if (resid > 0) {
            mTitle2.setVisibility(View.GONE);
        } else {
            mTitle2.setText(resid);
            mTitle2.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle2TextColor(int color) {
        mTitle2.setTextColor(color);
    }

    String id,twoLevel,threeLevel;
    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel =twoLevel;
        this.threeLevel =threeLevel;
    }
}
