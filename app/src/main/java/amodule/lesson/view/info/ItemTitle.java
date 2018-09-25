package amodule.lesson.view.info;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule._common.delegate.IBindExtraArrayMap;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.StatisticCallback;
import amodule._common.utility.WidgetUtility;
import amodule._common.widget.baseWidget.BaseExtraLinearLayout;

/**
 * Description :
 * PackageName : amodule.lesson.view.info
 * Created by tanze on 2018/3/30 11:06.
 * e_mail : ztanzeyu@gmail.com
 */
public class ItemTitle extends LinearLayout implements IBindMap,IBindExtraArrayMap {
    private BaseExtraLinearLayout mExtraLinearLayout;
    private TextView mTitle;
    private StatisticCallback mStatisticCallback;
    public ItemTitle(Context context) {
        this(context,null);
    }

    public ItemTitle(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ItemTitle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeUI();
    }

    private void initializeUI() {
        setBackgroundColor(Color.parseColor("#FFFFFF"));
        LayoutInflater.from(getContext()).inflate(R.layout.item_lesson_title,this);
        mExtraLinearLayout = (BaseExtraLinearLayout) findViewById(R.id.top_extra_layout);
        mTitle = (TextView) findViewById(R.id.item_title);
        mTitle.setLayoutParams(new LinearLayout.LayoutParams(ToolsDevice.getWindowPx(getContext()).widthPixels, LayoutParams.WRAP_CONTENT));
    }

    public void setTitle(String title){
        WidgetUtility.setTextToView(mTitle,title);
    }

    @Override
    public void setData(Map<String, String> data) {
        WidgetUtility.setTextToView(mTitle,data.get("text1"));
        setExtraData(StringManager.getListMapByJson(data.get("top")));
    }

    @Override
    public void setExtraData(List<Map<String, String>> array) {
        if(mExtraLinearLayout != null ){
            mExtraLinearLayout.setData(array,true,true);
        }
    }

    public void showTopPadding(){
        findViewById(R.id.extra_padding_top).setVisibility(VISIBLE);
    }

    public void setStatisticCallback(StatisticCallback statisticCallback) {
        mStatisticCallback = statisticCallback;
    }
}
