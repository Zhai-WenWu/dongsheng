package com.xh.manager;

import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.xh.view.base.BaseButtonView;
import com.xh.view.base.BaseView;

/**
 * View的管理类，DialogManager中Dialog显示的View通过这个类来添加。
 * Created by sll on 2017/10/27.
 */

public class ViewManager {
    protected DialogManager mDialogManager;
    protected ViewGroup mInnerLayout;
    protected ViewGroup mTitleMessageLayout;
    protected ViewGroup mBtnLayout;
    public ViewManager(@Nullable DialogManager dialogManager) {
        mDialogManager = dialogManager;
        mInnerLayout = mDialogManager.getDialog().getInnerContainer();
        mTitleMessageLayout = mDialogManager.getDialog().getInnerMsgContainer();
        mBtnLayout = mDialogManager.getDialog().getInnerBtnContainer();
    }

    public ViewManager setView(@Nullable BaseView view) {
        if (view instanceof BaseButtonView)
            mBtnLayout.addView(view);
        else
            mTitleMessageLayout.addView(view);
        return this;
    }

    /**
     * 设置Dialog内部View在屏幕中显示的位置，默认RelativeLayout.CENTER_IN_PARENT
     * @param gravity
     * @return
     */
    public ViewManager setGravity(int gravity) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mInnerLayout.getLayoutParams();
        params.addRule(gravity);
        mInnerLayout.setLayoutParams(params);
        return this;
    }
}
