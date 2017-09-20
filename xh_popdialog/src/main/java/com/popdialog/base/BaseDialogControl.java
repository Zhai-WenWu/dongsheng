package com.popdialog.base;

import android.app.Activity;

/**
 * PackageName : com.popdialog
 * Created by MrTrying on 2017/9/19 15:45.
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class BaseDialogControl {
    protected Activity mActivity;

    public BaseDialogControl(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 是否能展示
     * @param data 数据
     * @param callback 回调
     */
    public abstract void isShow(String data, OnPopDialogCallback callback);

    /**展示弹框*/
    public abstract void show();

    /**弹框回调*/
    public interface OnPopDialogCallback {
        /**能弹*/
        void onCanShow();
        /**弹下一个弹框*/
        void onNextShow();
    }
}
