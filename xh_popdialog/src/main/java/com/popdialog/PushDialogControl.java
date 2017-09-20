package com.popdialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.popdialog.base.BaseDialogControl;
import com.popdialog.util.FileManager;
import com.popdialog.util.PushManager;
import com.popdialog.util.StringManager;
import com.popdialog.view.XhDialog;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by XiangHa on 2017/5/3.
 * 当用户关闭推送通知时，定时（时间间隔：每天只弹框1次，第二次弹框的间隔时间要大于24小时）弹框提醒用户开启，共默认弹2次
 * 内容均可在线参数控制
 */
public class PushDialogControl extends BaseDialogControl {
    private XhDialog xhDialog;
    private String title, content, leftText, rightText;
    //弹框次数；一个版本最多弹5次；每次间隔时间
    private int showNum = 2, maxNumInVersion = 50, spaceTime = 24 * 60; //默认间隔24小时
    private int currentShowNumber = 0;
    private String versionCode;
    private OnPushDialogStatisticsCallback onPushDialogStatisticsCallback;

    public PushDialogControl(Activity activity,String versionCode) {
        super(activity);
        this.versionCode = versionCode;
    }

    @Override
    public void isShow(String data, OnPopDialogCallback callback) {
        if (mActivity == null || TextUtils.isEmpty(data)){
            callback.onNextShow();
            return;
        }
        String show_time = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME);
        String show_num_all = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
        int all_num = 0;
        if (!TextUtils.isEmpty(show_num_all)) {
            all_num = Integer.parseInt(show_num_all);
        }
        //当距离上次的好评弹框72小时才弹推送弹框
        if (all_num > 0
                && !TextUtils.isEmpty(show_time)
                && System.currentTimeMillis() - Long.parseLong(show_time) < 72 * 60 * 60 * 1000) {
            if (callback != null) {
                callback.onNextShow();
            }
            return;
        }
        boolean isShowNot = PushManager.isNotificationEnabled(mActivity);
        boolean isShowDia = isShowPushDialog(data);
        //Log.i("FRJ","isShowNot:" + isShowNot + "    isShowDia:" + isShowDia);
        if (!isShowNot && isShowDia) {
            callback.onCanShow();
        } else {
            callback.onNextShow();
        }
    }

    @Override
    public void show() {
        xhDialog = new XhDialog(mActivity);
        xhDialog.setTitle(title).setMessage(content)
                .setCanselButton(leftText, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        xhDialog.cancel();
                        if(onPushDialogStatisticsCallback != null){
                            onPushDialogStatisticsCallback.onSureStatistics();
                        }
                    }
                })
                .setSureButton(rightText, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PushManager.requestPermission(mActivity);
                        xhDialog.cancel();
                        if(onPushDialogStatisticsCallback != null){
                            onPushDialogStatisticsCallback.onCannelStatistics();
                        }
                    }
                }).show();
        FileManager.saveShared(mActivity, FileManager.PUSH_INFO, versionCode, String.valueOf(++currentShowNumber));
        FileManager.saveShared(mActivity, FileManager.PUSH_INFO, FileManager.PUSH_TIME, String.valueOf(System.currentTimeMillis()));
    }

    /**是否展示推送弹框*/
    private boolean isShowPushDialog(String data) {
        if (!loadData(data)) {
            return false;
        }
        String pushNmuberStr = String.valueOf(FileManager.loadShared(mActivity, FileManager.PUSH_INFO, versionCode));
        if (!TextUtils.isEmpty(pushNmuberStr)) {
            currentShowNumber = Integer.parseInt(pushNmuberStr);
        }
        if (currentShowNumber < showNum && currentShowNumber < maxNumInVersion) {
            String pushTime = String.valueOf(FileManager.loadShared(mActivity, FileManager.PUSH_INFO, FileManager.PUSH_TIME));
            if (TextUtils.isEmpty(pushTime)) {
                return true;
            }
            long currentTime = System.currentTimeMillis();
            long old = Long.parseLong(pushTime);
            if (currentTime - old > spaceTime * 60 * 1000) {
                return true;
            }
        }
        return false;
    }

    /**加载数据*/
    private boolean loadData(String data) {
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(data);
        if (arrayList.size() > 0) {
            try {
                Map<String, String> map = arrayList.get(0);
                title = map.get("title");
                content = map.get("content");
                leftText = map.get("leftText");
                rightText = map.get("rightText");
                showNum = Integer.parseInt(map.get("showNumber"));
                spaceTime = Integer.parseInt(map.get("spaceTime"));
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

//    /**请求权限*/
//    public void requestPermission() {
//        Toast.makeText(mActivity,"请手动设置通知权限",Toast.LENGTH_SHORT).show();
//        if (PushManager.isGoAppSetting()) {
//            Intent intent2 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
//            intent2.setData(uri);
//            mActivity.startActivity(intent2);
//        } else {
//            Intent intent = new Intent(Settings.ACTION_SETTINGS);
//            mActivity.startActivity(intent);
//        }
//    }

    public interface OnPushDialogStatisticsCallback{
        void onSureStatistics();
        void onCannelStatistics();
    }

    /*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public OnPushDialogStatisticsCallback getOnPushDialogStatisticsCallback() {
        return onPushDialogStatisticsCallback;
    }

    public void setOnPushDialogStatisticsCallback(OnPushDialogStatisticsCallback onPushDialogStatisticsCallback) {
        this.onPushDialogStatisticsCallback = onPushDialogStatisticsCallback;
    }
}
