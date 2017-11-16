package amodule._common.plugin;

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
import amodule._common.delegate.IStatictusData;
import amodule._common.widgetlib.AllWeightLibrary;

import static amodule._common.helper.WidgetDataHelper.KEY_BOTTOM;
import static amodule._common.helper.WidgetDataHelper.KEY_STYLE;
import static amodule._common.helper.WidgetDataHelper.KEY_TOP;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_DATA;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_EXTRA;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_TYPE;
import static amodule._common.widgetlib.IWidgetLibrary.NO_FIND_ID;

/**
 * PackageName : amodule._common.plugin
 * Created by MrTrying on 2017/11/10 19:20.
 * E_mail : ztanzeyu@gmail.com
 */

public class WidgetVerticalLayout extends AbsWidgetVerticalLayout<Map<String, String>> implements IStatictusData {

    LayoutInflater mInflater;

    LinearLayout mExtraTop, mExtraBottom;

    public WidgetVerticalLayout(Context context) {
        super(context);
    }

    public WidgetVerticalLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetVerticalLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initialize() {
        mInflater = LayoutInflater.from(getContext());
        mExtraTop = new LinearLayout(getContext());
        mExtraTop.setOrientation(VERTICAL);
        addView(mExtraTop, 0);
        mExtraBottom = new LinearLayout(getContext());
        mExtraBottom.setOrientation(VERTICAL);
        addView(mExtraBottom);
    }

    @Override
    public void setData(Map<String, String> data) {
        if (null == data || data.isEmpty()) return;
        String widgetType = data.get(KEY_WIDGET_TYPE);
        String widgetData = data.get(KEY_WIDGET_DATA);
        Map<String, String> dataMap = StringManager.getFirstMap(widgetData);

        String style = dataMap.get(KEY_STYLE);
        final int viewId = AllWeightLibrary.of().findWidgetViewID(widgetType, style);
        if (viewId > NO_FIND_ID) {
            View view = findViewById(viewId);
            if (null != view) {
                if (view instanceof IBindMap && !TextUtils.isEmpty(widgetData)) {
                    ((IBindMap) view).setData(dataMap);
                }
                if (view instanceof IStatictusData) {
                    ((IStatictusData) view).setStatictusData(id, twoLevel, threeLevel);
                }
            }
            //加载额外数据
            String widgetExtra = data.get(KEY_WIDGET_EXTRA);
            if (TextUtils.isEmpty(widgetExtra)) {
                return;
            }
            Map<String, String> widgetExtraMap = StringManager.getFirstMap(widgetExtra);
            if (widgetExtraMap.isEmpty()) {
                return;
            }
            updateTopView(StringManager.getListMapByJson(widgetExtraMap.get(KEY_TOP)));
            updateBottom(StringManager.getListMapByJson(widgetExtraMap.get(KEY_BOTTOM)));
        }
    }

    @Override
    public void updateTopView(List<Map<String, String>> array) {
        if (mExtraTop != null && mExtraTop.getChildCount() > 0) {
            mExtraTop.removeAllViews();
        }
        if (null == array || array.isEmpty()) {
            return;
        }

        Stream.of(array).forEach(data -> {
            addViewByData(mExtraTop, data, false);
        });
    }

    @Override
    public void updateBottom(List<Map<String, String>> array) {
        if (mExtraBottom != null && mExtraBottom.getChildCount() > 0) {
            mExtraBottom.removeAllViews();
        }
        if (null == array || array.isEmpty()) {
            return;
        }

        Stream.of(array).forEach(data -> {
            addViewByData(mExtraBottom, data, true);
        });
    }

    /**
     * 根据数据添加view
     *
     * @param data
     * @param isOrder 是否按顺序添加
     */

    private void addViewByData(LinearLayout layout, Map<String, String> data, boolean isOrder) {
        String widgetType = data.get(KEY_WIDGET_TYPE);
        String widgetData = data.get(KEY_WIDGET_DATA);
        Map<String, String> dataMap = StringManager.getFirstMap(widgetData);
        String style = dataMap.get(KEY_STYLE);
        final int layoutID = AllWeightLibrary.of().findWidgetLayoutID(widgetType, style);
        if (layoutID > NO_FIND_ID) {
            View view = mInflater.inflate(layoutID, null, true);
            if (null != view && view instanceof IBindMap
                    && !TextUtils.isEmpty(widgetData)) {
                ((IBindMap) view).setData(dataMap);
                layout.addView(view, isOrder ? -1 : 0);
            }
        }
    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }
}
