package amodule._common.widget.baseWidget;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.annimon.stream.Stream;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IResetCallback;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.ITitleStaticCallback;
import amodule._common.delegate.StatisticCallback;
import amodule._common.widgetlib.AllWeightLibrary;

import static amodule._common.helper.WidgetDataHelper.KEY_STYLE;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_DATA;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_TYPE;
import static amodule._common.widgetlib.IWidgetLibrary.NO_FIND_ID;

/**
 * Created by xiangha_android on 2018/3/30.
 */

public class BaseExtraLinearLayout extends LinearLayout implements IStatisticCallback, IStatictusData, ISaveStatistic {

    private List<Map<String, String>> mDatas;

    private StatisticCallback mStatisticCallback;
    private String mId, mTwoLevel, mThreeLevel;

    public BaseExtraLinearLayout(Context context) {
        super(context);
    }

    public BaseExtraLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseExtraLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public BaseExtraLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void resetExtraLayout(){
        resetExtraLayout(this);
    }

    private void resetExtraLayout(LinearLayout layout) {
        if(layout != null){
            for(int i = 0 ; i < layout.getChildCount();i++){
                excuteReset(layout.getChildAt(i));
            }
        }
    }

    private void excuteReset(View view) {
        if(view != null && view instanceof IResetCallback){
            ((IResetCallback)view).reset();
        }
    }

    public void setData(List<Map<String, String>> array, boolean isOrder) {
        mDatas = array;
        if (null == mDatas || mDatas.isEmpty()) {
            return;
        }
        if (getChildCount() > 0) {
            removeAllViews();
        }
        Stream.of(array).forEach(data -> addViewByData(data, isOrder));
        requestLayout();
        setVisibility(getChildCount() > 0 ? VISIBLE : GONE);
    }

    @Override
    public void setStatisticCallback(StatisticCallback callback) {
        mStatisticCallback = callback;
    }

    /**
     * 根据数据添加view
     *
     * @param data 数据
     * @param isOrder 是否按顺序添加
     */

    private void addViewByData(Map<String, String> data, boolean isOrder) {
        String widgetType = data.get(KEY_WIDGET_TYPE);
        String widgetData = data.get(KEY_WIDGET_DATA);
        if(TextUtils.isEmpty(widgetData)){
            return;
        }
        Map<String, String> dataMap = StringManager.getFirstMap(widgetData);
        String style = dataMap.get(KEY_STYLE);
        final int layoutID = AllWeightLibrary.of().findWidgetLayoutID(widgetType, style);
        if (layoutID > NO_FIND_ID) {
            View view = LayoutInflater.from(getContext()).inflate(layoutID, null, true);
            if (null != view ){
                if(view instanceof IStatisticCallback && mStatisticCallback != null){
                    ((IStatisticCallback)view).setStatisticCallback(mStatisticCallback);
                }
                if(view instanceof ITitleStaticCallback && mStatisticCallback != null){
                    ((ITitleStaticCallback)view).setTitleStaticCallback(mStatisticCallback);
                }
                if(view instanceof IStatictusData && mStatisticCallback != null){
                    ((IStatictusData)view).setStatictusData(mId,mTwoLevel,mThreeLevel);
                }
                if(view instanceof IBindMap
                        && !TextUtils.isEmpty(widgetData)) {
                    ((IBindMap) view).setData(dataMap);
                    BaseExtraLinearLayout.this.addView(view, isOrder ? -1 : 0);
                }
            }

        }
    }

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        mId = id;
        mTwoLevel = twoLevel;
        mThreeLevel = threeLevel;
    }

    @Override
    public void saveStatisticData(String page) {
        for(int i = 0 ; i < getChildCount() ; i++){
            View child = getChildAt(i);
            if (child != null && child instanceof ISaveStatistic) {
                ((ISaveStatistic) child).saveStatisticData(page);
            }
        }
    }
}
