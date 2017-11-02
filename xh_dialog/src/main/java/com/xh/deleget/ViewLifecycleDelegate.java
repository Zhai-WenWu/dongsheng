package com.xh.deleget;

/**
 * View在window的状态监听
 * Created by sll on 2017/10/27.
 */

public interface ViewLifecycleDelegate {
    void onViewAttachedWindow();
    void onViewDetachedFromWindow();
}
