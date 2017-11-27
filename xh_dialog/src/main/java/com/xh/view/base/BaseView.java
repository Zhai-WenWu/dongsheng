package com.xh.view.base;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.xh.deleget.ViewLifecycleDelegate;

/**
 * 所有显示弹框View的基类，自定义的View都需要继承自此基类。
 * Created by sll on 2017/10/27.
 */

public class BaseView extends RelativeLayout {

    private ViewLifecycleDelegate mDelegate;

    private int mLayoutId;

    public BaseView(Context context, int layoutId) {
        super(context);
        mLayoutId = layoutId;
        initView(context);
    }

    public BaseView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        mLayoutId = layoutId;
        initView(context);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        mLayoutId = layoutId;
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int layoutId) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mLayoutId = layoutId;
        initView(context);
    }

    protected void initView(Context context) {
        LayoutInflater.from(context).inflate(mLayoutId, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDelegate != null)
            mDelegate.onViewAttachedWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDelegate != null) {
            mDelegate.onViewDetachedFromWindow();
            mDelegate = null;
        }
    }

    public void setViewLifecycleDelegate(ViewLifecycleDelegate delegate) {
        mDelegate = delegate;
    }
}
