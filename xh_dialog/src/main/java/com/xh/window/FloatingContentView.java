package com.xh.window;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xh.deleget.ViewLifecycleDelegate;
import com.xh.dialog.R;

/**
 * 悬浮窗的内容View
 * Created by sll on 2017/8/8.
 */

public class FloatingContentView extends RelativeLayout {

    private ViewLifecycleDelegate mDelegate;

    private RelativeLayout mRootContainer;
    private LinearLayout mInnerContainer;
    private LinearLayout mInnerMsgContainer;
    private LinearLayout mInnerBtnContainer;
    private View mEmpty1;
    private View mEmpty2;
    public FloatingContentView(Context context) {
        super(context);
        initView(context);
    }

    public FloatingContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FloatingContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_content_layout, this);
        mRootContainer = (RelativeLayout) findViewById(R.id.root_container);
        mInnerContainer = (LinearLayout) findViewById(R.id.inner_container);
        mInnerMsgContainer = (LinearLayout) findViewById(R.id.inner_msg_container);
        mInnerBtnContainer = (LinearLayout) findViewById(R.id.inner_btn_container);
        mEmpty1 = findViewById(R.id.empty1);
        mEmpty2 = findViewById(R.id.empty2);
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

    /**
     * 去掉内容View中原有的纵向间距
     */
    public void noPadding() {
        mEmpty1.setVisibility(View.GONE);
        mEmpty2.setVisibility(View.GONE);
    }

    public void setViewLifecycleDelegate(ViewLifecycleDelegate delegate) {
        mDelegate = delegate;
    }

    public LinearLayout getInnerContainer() {
        return mInnerContainer;
    }

    public LinearLayout getInnerMsgContainer() {
        return mInnerMsgContainer;
    }

    public LinearLayout getInnerBtnContainer() {
        return mInnerBtnContainer;
    }

    public RelativeLayout getRootContainer() {
        return mRootContainer;
    }

}
