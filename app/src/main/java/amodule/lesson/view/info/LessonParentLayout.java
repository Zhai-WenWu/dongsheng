package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Map;

import acore.tools.StringManager;
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
        if (mTopExtraLayout == null) {
            mTopExtraLayout = new DelayLoadExtraLayout(getContext());
            addView(mTopExtraLayout);
        }
        if (mContentLayout == null) {
            mContentLayout = new LinearLayout(getContext());
            addView(mContentLayout);
        }
        if (mBottomExtraLayout == null) {
            mBottomExtraLayout = new DelayLoadExtraLayout(getContext());
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
    public void setData(Map<String, String> data) {

        Map<String, String> widgetDataMap = StringManager.getFirstMap(data.get(KEY_WIDGET_EXTRA));
        setTopExtraData(widgetDataMap);
        setBottomExtraData(widgetDataMap);
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
