package amodule.answer.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import acore.override.XHApplication;

/**
 * Created by sll on 2017/8/7.
 */

public class FloatingWindow {

    private static final String TAG = "FloatingWindow";
    private static FloatingWindow mFloatingWindow;
    private boolean mIsShowing = false;
    private FloatingRootView mRootView;
    private View mContentView;
    private WindowManager mWindowManager;

    private boolean mCancelable = false;

    private FloatingWindow() {
        mWindowManager = (WindowManager) XHApplication.in().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mRootView = new FloatingRootView(XHApplication.in().getApplicationContext());
        RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mRootView.setLayoutParams(rootParams);
    }

    public synchronized static FloatingWindow getInstance() {
        if (mFloatingWindow == null)
            mFloatingWindow = new FloatingWindow();
        return mFloatingWindow;
    }

    public void setContentView(View contentView) {
        mContentView = contentView;
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
        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        contentParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRootView.addView(mContentView, contentParams);
        mRootView.invalidate();
        mIsShowing = true;
    }

    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        setOnBackPressed();
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
        mContentView = null;
        mIsShowing = false;
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
