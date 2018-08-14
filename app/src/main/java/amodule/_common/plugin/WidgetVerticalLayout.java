package amodule._common.plugin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IExtraDataCallback;
import amodule._common.delegate.IResetCallback;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.ISetAdController;
import amodule._common.delegate.ISetAdID;
import amodule._common.delegate.ISetShowIndex;
import amodule._common.delegate.ISetStatisticPage;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.ITitleStaticCallback;
import amodule._common.delegate.StatisticCallback;
import amodule._common.widget.baseWidget.BaseExtraLinearLayout;
import amodule._common.widgetlib.AllWeightLibrary;
import third.ad.scrollerAd.XHAllAdControl;

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

public class WidgetVerticalLayout extends AbsWidgetVerticalLayout<Map<String, String>>
        implements IStatictusData, ISaveStatistic,ISetAdID,IStatisticCallback,
        ITitleStaticCallback,ISetStatisticPage, ISetAdController, ISetShowIndex {

    public static final int LLM = LinearLayout.LayoutParams.MATCH_PARENT;
    public static final int LLW = LinearLayout.LayoutParams.WRAP_CONTENT;

    LayoutInflater mInflater;

    BaseExtraLinearLayout mExtraTop, mExtraBottom;

    private IExtraDataCallback mIExtraDataCallback;

    int currentID = -1;

    private Map<String, String> data;

    private XHAllAdControl mXHAllAdControl;

    private StatisticCallback mStatisticCallback,mTitleStatisticCallback;

    private int mShowIndex = -1;

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
    }

    public void setExtraDataCallback(IExtraDataCallback callback) {
        mIExtraDataCallback = callback;
    }

    @Override
    public void setData(Map<String, String> data) {
        if (null == data || data.isEmpty() || dataEquals(data)) {
            resetView();
            return;
        }
        this.data = data;

        String widgetType = data.get(KEY_WIDGET_TYPE);
        String widgetData = data.get(KEY_WIDGET_DATA);
        Map<String, String> dataMap = StringManager.getFirstMap(widgetData);

        String style = dataMap.get(KEY_STYLE);
        final int viewId = AllWeightLibrary.of().findWidgetViewID(widgetType, style);
        if (viewId > NO_FIND_ID) {
            View view = findViewById(viewId);
            if (null != view) {
                currentID = viewId;
                view.setVisibility(VISIBLE);
                if(view instanceof  ISetAdID){
                    ((ISetAdID)view).setAdID(adIDs);
                }
                if(view instanceof IStatisticCallback && mStatisticCallback != null){
                    ((IStatisticCallback)view).setStatisticCallback(mStatisticCallback);
                }
                if(view instanceof ITitleStaticCallback && mTitleStatisticCallback != null){
                    ((ITitleStaticCallback) view).setTitleStaticCallback((mTitleStatisticCallback));
                }
                if (view instanceof IStatictusData) {
                    ((IStatictusData) view).setStatictusData(id, twoLevel, threeLevel);
                }
                if(view instanceof ISetStatisticPage){
                    ((ISetStatisticPage) view).setStatisticPage(page);
                }
                if (view instanceof ISetAdController) {
                    ((ISetAdController)view).setAdController(mXHAllAdControl);
                }
                if (view instanceof ISetShowIndex) {
                    ((ISetShowIndex) view).setShowIndex(mShowIndex);
                }
                if (view instanceof IBindMap && !TextUtils.isEmpty(widgetData)) {
                    ((IBindMap) view).setData(dataMap);
                }
            } else {
                hideView();
            }
        } else {
            hideView();
        }
        //加载额外数据
        String widgetExtra = data.get(KEY_WIDGET_EXTRA);
        if (TextUtils.isEmpty(widgetExtra)) {
            return;
        }
        Map<String, String> widgetExtraMap = StringManager.getFirstMap(widgetExtra);
        if (widgetExtraMap.isEmpty()
                || (TextUtils.isEmpty(widgetExtraMap.get(KEY_TOP)) && TextUtils.isEmpty(widgetExtraMap.get(KEY_BOTTOM)))
                ) {
            if (mIExtraDataCallback != null) {
                mIExtraDataCallback.extraDataCallback(null);
            }
            return;
        }
        updateTopView(StringManager.getListMapByJson(widgetExtraMap.get(KEY_TOP)));
        updateBottom(StringManager.getListMapByJson(widgetExtraMap.get(KEY_BOTTOM)));
        if (mIExtraDataCallback != null) {
            mIExtraDataCallback.extraDataCallback(widgetExtraMap);
        }
    }

    private void resetView(){
        if(currentID > 0){
            excuteReset(findViewById(currentID));
        }
        if (mExtraTop != null)
            mExtraTop.resetExtraLayout();
        if (mExtraBottom != null)
            mExtraBottom.resetExtraLayout();
    }

    private void excuteReset(View view) {
        if(view != null && view instanceof IResetCallback){
            ((IResetCallback)view).reset();
        }
    }

    private void hideView() {
        int index = mExtraTop == null ? 0 : 1;
        getChildAt(index).setVisibility(GONE);
    }

    private boolean dataEquals(Map<String,String> current){
        if (data != null && current != null) {
            Map<String, String> m = (Map<String, String>) current;
            if (m.size() != data.size())
                return false;
            try {
                Iterator<Map.Entry<String, String>> i = data.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<String, String> e = i.next();
                    String key = e.getKey();
                    String value = e.getValue();
                    if (value == null) {
                        if (!(m.get(key)==null && m.containsKey(key)))
                            return false;
                    } else {
                        if (!value.equals(m.get(key)))
                            return false;
                    }
                }
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateTopView(List<Map<String, String>> array) {
        if (mExtraTop == null) {
            mExtraTop = new BaseExtraLinearLayout(getContext());
            mExtraTop.setLayoutParams(new LinearLayout.LayoutParams(LLM, LLW));
            mExtraTop.setOrientation(VERTICAL);
            addView(mExtraTop, 0);
        }
        mExtraTop.setStatictusData(id, twoLevel, threeLevel);
        mExtraTop.setData(array, false);
    }

    @Override
    public void updateBottom(List<Map<String, String>> array) {
        if (mExtraBottom == null) {
            mExtraBottom = new BaseExtraLinearLayout(getContext());
            mExtraBottom.setLayoutParams(new LinearLayout.LayoutParams(LLM, LLW));
            mExtraBottom.setOrientation(VERTICAL);
            addView(mExtraBottom);
        }
        mExtraBottom.setStatictusData(id, twoLevel, threeLevel);
        mExtraBottom.setData(array, true);
    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }

    @Override
    public void saveStatisticData(String page) {
        if (currentID > 0) {
            View view = findViewById(currentID);
            if (view != null && view instanceof ISaveStatistic) {
                ((ISaveStatistic) view).saveStatisticData(page);
            }
        }
        if (mExtraTop != null)
            mExtraTop.saveStatisticData(page);
        if (mExtraBottom != null)
            mExtraBottom.saveStatisticData(page);
    }

    List<String> adIDs;
    @Override
    public void setAdID(List<String> adIDs) {
        this.adIDs = adIDs;
    }

    @Override
    public void setStatisticCallback(@NonNull StatisticCallback callback) {
        mStatisticCallback = callback;
    }

    @Override
    public void setTitleStaticCallback(StatisticCallback callback) {
        mTitleStatisticCallback = callback;
    }

    String page = "";
    @Override
    public void setStatisticPage(String page) {
        this.page = page;
    }

    @Override
    public void setAdController(XHAllAdControl controller) {
        mXHAllAdControl = controller;
    }

    @Override
    public void setShowIndex(int showIndex) {
        mShowIndex = showIndex;
    }
}
