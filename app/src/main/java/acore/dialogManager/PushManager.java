package acore.dialogManager;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import xh.windowview.XhDialog;

/**
 * Created by XiangHa on 2017/5/3.
 * 当用户关闭推送通知时，定时（时间间隔：每天只弹框1次，第二次弹框的间隔时间要大于24小时）弹框提醒用户开启，共默认弹2次
 * 内容均可在线参数控制
 */
public class PushManager extends DialogManagerParent {
    private XhDialog xhDialog;
    private String title,content,leftText,rightText;
    //弹框次数；一个版本最多弹5次；每次间隔时间
    private int showNum = 2,maxNumInVersion = 50,spaceTime = 24 * 60; //默认间隔24小时
    private int currentShowNumber = 0;


    @Override
    public void isShow(OnDialogManagerCallback callback) {
        String show_time = (String)FileManager.loadShared(XHApplication.in(),FileManager.GOODCOMMENT_SHOW_TIME,FileManager.GOODCOMMENT_SHOW_TIME);
        String show_num_all = (String) FileManager.loadShared(XHApplication.in(), FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
        int all_num = 0;
        if(!TextUtils.isEmpty(show_num_all)){
            all_num = Integer.parseInt(show_num_all);
        }
        //当距离上次的好评弹框72小时才弹推送弹框
        if(all_num > 0
                && !TextUtils.isEmpty(show_time)
                && System.currentTimeMillis() - Long.parseLong(show_time) < 72 * 60 * 60 * 1000){
            if(callback != null){
                callback.onGone();
            }
            return;
        }
        boolean isShowNot = isNotificationEnabled();
        boolean isShowDia = isShowPushDialog();
        //Log.i("FRJ","isShowNot:" + isShowNot + "    isShowDia:" + isShowDia);
        if(!isShowNot && isShowDia){
            callback.onShow();
        }else{
            callback.onGone();
        }
    }

    @Override
    public void show() {
        xhDialog = new XhDialog(XHActivityManager.getInstance().getCurrentActivity());
        xhDialog.setTitle(title).setMessage(content)
                .setCanselButton(leftText, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(XHApplication.in(),"a_push","否","");
                        xhDialog.cancel();
                    }
                }).setSureButton(rightText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(XHApplication.in(),"a_push","是","");
                requestPermission();
                xhDialog.cancel();
            }
        }).show();
        FileManager.saveShared(XHApplication.in(),FileManager.PUSH_INFO, VersionOp.getVerName(XHApplication.in()),String.valueOf(++currentShowNumber));
        FileManager.saveShared(XHApplication.in(),FileManager.PUSH_INFO, FileManager.PUSH_TIME,String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void cancel() {
        if(xhDialog != null){
            xhDialog.cancel();
        }
    }

    private boolean isShowPushDialog(){
        if(!loadData()){
            return false;
        }
        String pushNmuberStr = String.valueOf(FileManager.loadShared(XHApplication.in(),FileManager.PUSH_INFO, VersionOp.getVerName(XHApplication.in())));
        if(!TextUtils.isEmpty(pushNmuberStr)){
            currentShowNumber = Integer.parseInt(pushNmuberStr);
        }
        if (currentShowNumber < showNum && currentShowNumber < maxNumInVersion) {
            String pushTime = String.valueOf(FileManager.loadShared(XHApplication.in(),FileManager.PUSH_INFO,FileManager.PUSH_TIME));
            if(TextUtils.isEmpty(pushTime)){
                return true;
            }
            long currentTime = System.currentTimeMillis();
            long old = Long.parseLong(pushTime);
            if(currentTime - old > spaceTime * 60 * 1000){
                return true;
            }
        }
        return false;
    }

    private boolean loadData(){
        String jsonStr = AppCommon.getConfigByLocal("pushJson");

        ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(jsonStr);
        if(arrayList.size() > 0){
            try {
                Map<String, String> map = arrayList.get(0);
                title = map.get("title");
                content = map.get("content");
                leftText = map.get("leftText");
                rightText = map.get("rightText");
                showNum = Integer.parseInt(map.get("showNumber"));
                spaceTime = Integer.parseInt(map.get("spaceTime"));
            }catch (Exception e){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public static boolean isNotificationEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(XHApplication.in());
        return notificationManagerCompat.areNotificationsEnabled();
    }

    public static void requestPermission() {
        Tools.showToast(XHApplication.in(),"请手动设置通知权限");
        if (isGoAppSetting()){
            Intent intent2 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", XHActivityManager.getInstance().getCurrentActivity().getPackageName(), null);
            intent2.setData(uri);
            XHActivityManager.getInstance().getCurrentActivity().startActivity(intent2);
        }else {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
        }
    }

    public static boolean isGoAppSetting(){
        String brand = android.os.Build.BRAND;
        //Log.i("FRJ","brand:" + brand);
        if(TextUtils.isEmpty(brand)
                || brand.contains("Lenovo"))
            return false;
        return true;
    }


    public static void tongjiPush(){
        boolean isEnabled = isNotificationEnabled();
        String pushTag = String.valueOf(FileManager.loadShared(XHApplication.in(),FileManager.PUSH_INFO,FileManager.PUSH_TAG));
        String newTag = isEnabled ? "2" : "1";
        if(!newTag.equals(pushTag)){
            FileManager.saveShared(XHApplication.in(),FileManager.PUSH_INFO,FileManager.PUSH_TAG,newTag);
            XHClick.mapStat(XHApplication.in(),"a_push_user",isEnabled?"开启推送":"关闭推送","");
        }
    }
}
