package com.xh.manager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.Nullable;

import com.xh.deleget.DialogLifecycleDelegate;
import com.xh.dialog.XHDialog;

/**
 * Dialog管理类，对于标题、副标题、按钮、有间距类型的Dialog统一通过这个类创建
 * Created by sll on 2017/10/27.
 */

public class DialogManager {

    private Context mContext;

    private XHDialog mDialog;
    private ViewManager mViewManager;

    private boolean mCancelable = true;

    public DialogManager(@Nullable Context context, DialogLifecycleDelegate delegate) {
        mContext = context;
        mDialog = new XHDialog(context, delegate);

    }

    public DialogManager(@Nullable Context context) {
        mContext = context;
        mDialog = new XHDialog(context);
    }

    public XHDialog getDialog() {
        return mDialog;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener cancelListener) {
        if (cancelListener != null)
            mDialog.setOnCancelListener(cancelListener);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
        if (dismissListener != null)
            mDialog.setOnDismissListener(dismissListener);
    }

    /**
     * 创建Dialog
     * @param viewManager 要显示的View的管理类
     * @return
     */
    public DialogManager createDialog(@Nullable ViewManager viewManager) {
        mViewManager = viewManager;
        return this;
    }

    /**
     * 去掉原有的纵向间距
     */
    public DialogManager noPadding() {
        mDialog.noPadding();
        return this;
    }

    public DialogManager setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        return this;
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    public void show() {
        if (mViewManager == null)
            return;
        Activity mAct = null;
        if (mContext instanceof Activity)
            mAct = (Activity) mContext;
        if (mAct != null) {
            if (mAct.isFinishing())
                return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mAct.isDestroyed())
                return;
        }
        mDialog.setCancelable(mCancelable);
        mDialog.show();
    }

    public void cancel() {
        if (isShowing()) {
            mDialog.cancel();
        }
    }

    public void dismiss() {
        if (isShowing()) {
            mDialog.dismiss();
        }
    }
}
