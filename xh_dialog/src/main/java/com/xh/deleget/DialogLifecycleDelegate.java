package com.xh.deleget;

/**
 * Dialog的生命周期监听
 * Created by sll on 2017/10/26.
 */

public interface DialogLifecycleDelegate {
    void onCreate();
    void onStart();
    void onStop();
}
