package amodule.lesson.view.info;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import amodule._common.delegate.IBindMap;
import amodule._common.widget.DelayLoadExtraLayout;
import amodule.lesson.delegate.IShowNextItem;

import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_DATA;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_EXTRA;

/**
 * Description :
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/30 10:37.
 * e_mail : ztanzeyu@gmail.com
 */
public abstract class LessonParentLayout extends LinearLayout implements IBindMap, IShowNextItem {

    protected DelayLoadExtraLayout mTopExtraLayout, mBottomExtraLayout;
    protected LinearLayout mContentLayout;
    protected List<Map<String, String>> mDatas = new ArrayList<>();

    public LessonParentLayout(Context context) {
        this(context, null);
    }

    public LessonParentLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LessonParentLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        innerInitialize();
        initializeUI();
    }

    private void innerInitialize() {
        setOrientation(VERTICAL);
        int color = Color.parseColor("#FFFFFF");
//        setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (mTopExtraLayout == null) {
            mTopExtraLayout = new DelayLoadExtraLayout(getContext());
            mTopExtraLayout.setBackgroundColor(color);
            mTopExtraLayout.setDelayLoadData(true);
            addView(mTopExtraLayout);
        }
        if (mContentLayout == null) {
            mContentLayout = new LinearLayout(getContext());
            mContentLayout.setOrientation(VERTICAL);
            addView(mContentLayout);
        }
        if (mBottomExtraLayout == null) {
            mBottomExtraLayout = new DelayLoadExtraLayout(getContext());
            mBottomExtraLayout.setBackgroundColor(color);
            mBottomExtraLayout.setDelayLoadData(true);
            addView(mBottomExtraLayout);
        }
    }

    protected void initializeUI() {

    }

    @Override
    public void addView(View child) {
        if (child != mTopExtraLayout
                && child != mContentLayout
                && child != mBottomExtraLayout) {
            if (mContentLayout != null) {
                mContentLayout.addView(child);
            }
        } else {
            super.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if (child != mTopExtraLayout
                && child != mContentLayout
                && child != mBottomExtraLayout) {
            if (mContentLayout != null) {
                mContentLayout.addView(child,index);
            }
        } else {
            super.addView(child, index);
        }
    }

    @Override
    public void setData(Map<String, String> data) {
        Map<String, String> widgetDataMap = StringManager.getFirstMap(data.get(KEY_WIDGET_DATA));
        mDatas = StringManager.getListMapByJson(widgetDataMap.get("list"));
        if (mContentLayout.getChildCount() > 0) {
            mContentLayout.removeAllViews();
        }
        Map<String, String> widgetExtraDataMap = StringManager.getFirstMap(data.get(KEY_WIDGET_EXTRA));
        setTopExtraData(widgetExtraDataMap);
        setBottomExtraData(widgetExtraDataMap);
        showAllItem();
    }

    protected void setTopExtraData(Map<String, String> data) {
        if (mTopExtraLayout != null) {
            mTopExtraLayout.setData(StringManager.getListMapByJson(data.get("top")), true);
        }
    }

    protected void setBottomExtraData(Map<String, String> data) {
        if (mBottomExtraLayout != null) {
            mBottomExtraLayout.setData(StringManager.getListMapByJson(data.get("bottom")), true);
        }
    }

    public void showPadding(boolean isShow) {
        setPadding(0, 0, 0, isShow ? Tools.getDimen(getContext(), R.dimen.dp_5) : 0);
    }

    public boolean hasChildView() {
        return mContentLayout != null && mContentLayout.getChildCount() > 0;
    }

    public void showAllItem(){
        showTopNextItem();
        if(mContentLayout!=null){
            showInnerNextItem();
        }
        showBottomNextItem();
    }

    @Override
    public boolean showNextItem() {
        return showTopNextItem()
                || (mContentLayout != null && showInnerNextItem())
                || showBottomNextItem();
    }

    protected boolean showTopNextItem() {
        return mTopExtraLayout != null && mTopExtraLayout.showNextItem();
    }

    protected boolean showBottomNextItem() {
        return mBottomExtraLayout != null && mBottomExtraLayout.showNextItem();
    }

    protected abstract boolean showInnerNextItem();

}
