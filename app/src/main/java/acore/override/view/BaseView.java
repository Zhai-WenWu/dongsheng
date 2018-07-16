package acore.override.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 基于relativelayout的view处理
 */
public class BaseView extends RelativeLayout  {
    public Context context;
    protected String statisticsId = "";

    public BaseView(Context context, int layoutId) {
        super(context);
        this.context = context;
        initLayoutView(layoutId);
    }

    public BaseView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        this.context = context;
        initLayoutView(layoutId);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initLayoutView(layoutId);
    }

    protected void initLayoutView(int layoutId) {
        LayoutInflater.from(context).inflate(layoutId, this, true);
    }

    /***
     * textview设置数据
     * @param textView
     * @param data
     */
    public void setTextViewData(TextView textView, String data) {
        setTextViewData(textView, data,GONE);
    }

    public void setTextViewData(TextView textView, String data, int defaultVisibility) {
        if (textView != null) textView.setVisibility(defaultVisibility);
        if (!TextUtils.isEmpty(data)) {
            textView.setVisibility(VISIBLE);
            textView.setText(data);
        }
    }

    public void setStatisticsId(String statisticsId) {
        this.statisticsId = statisticsId;
    }

}
