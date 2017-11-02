package com.xh.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.xh.view.base.BaseButtonView;
import com.xh.view.base.BaseView;

/**
 * 悬浮窗，显示在Application的window层。
 * Created by sll on 2017/8/7.
 */

public class FloatingWindow {

    private static final String TAG = "FloatingWindow";
    private static FloatingWindow mFloatingWindow;
    private boolean mIsShowing = false;
    private FloatingRootView mRootView;
    private FloatingContentView mContentView;
    private WindowManager mWindowManager;

    private boolean mCancelable = false;
    private int mGravity = RelativeLayout.CENTER_IN_PARENT;

    private FloatingWindow(Context applicationContext) {
        mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        mRootView = new FloatingRootView(applicationContext);
        mContentView = new FloatingContentView(applicationContext);
        RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mRootView.setLayoutParams(rootParams);
    }

    public synchronized static FloatingWindow getInstance(Context applicationContext) {
        if (mFloatingWindow == null)
            mFloatingWindow = new FloatingWindow(applicationContext);
        return mFloatingWindow;
    }

    /**
     * 设置View
     * @param view
     * @return
     */
    public FloatingWindow setView(@Nullable BaseView view) {
        if (view instanceof BaseButtonView)
            mContentView.getInnerBtnContainer().addView(view);
        else
            mContentView.getInnerMsgContainer().addView(view);
        return mFloatingWindow;
    }

    /**
     * 去掉内部原有的纵向间距
     * @return
     */
    public FloatingWindow noPadding() {
        mContentView.noPadding();
        return mFloatingWindow;
    }

    public void showFloatingWindow() {
        if (mIsShowing || mContentView == null)
            return;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mWindowManager.addView(mRootView, params);
        RelativeLayout.LayoutParams innerParams = (RelativeLayout.LayoutParams) mContentView.getInnerContainer().getLayoutParams();
        innerParams.addRule(mGravity);
        mContentView.getInnerContainer().setLayoutParams(innerParams);
        mRootView.addView(mContentView);
        mRootView.invalidate();
        mIsShowing = true;
    }

    public FloatingWindow setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        setOnBackPressed();
        return mFloatingWindow;
    }

    /**
     * 设置Window内部View在屏幕中显示的位置，默认RelativeLayout.CENTER_IN_PARENT
     * @param gravity
     * @return
     */
    public FloatingWindow setGravity(int gravity) {
        mGravity = gravity;
        return mFloatingWindow;
    }

    private void setOnBackPressed() {
        if (mCancelable && mRootView != null) {
            mRootView.setOnBackPressedListener(new FloatingRootView.OnBackPressedListener() {
                @Override
                public boolean onBackPressed() {
                    if (mIsShowing) {
                        cancelFloatingWindow();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void cancelFloatingWindow() {
        if (mContentView == null || !mIsShowing)
            return;
        mRootView.removeAllViews();
        mWindowManager.removeView(mRootView);
        mRootView = null;
        mContentView = null;
        mIsShowing = false;
        mFloatingWindow = null;
        if (mCancelable && mOnCancelListener != null)
            mOnCancelListener.onCancel();
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public OnCancelListener mOnCancelListener;
    public void setOnCancelListener(OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

}
