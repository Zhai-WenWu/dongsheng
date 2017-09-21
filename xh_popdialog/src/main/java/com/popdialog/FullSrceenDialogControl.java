package com.popdialog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.popdialog.base.BaseDialogControl;
import com.popdialog.util.FileManager;
import com.popdialog.util.FullScreenManager;
import com.popdialog.util.StringManager;
import com.popdialog.view.XHADView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * PackageName : acore.dialogManager
 * Created by MrTrying on 2017/6/26 10:02.
 * E_mail : ztanzeyu@gmail.com
 */

public class FullSrceenDialogControl extends BaseDialogControl {
    /**加载图片回调*/
    private OnLoadImageCallback onLoadImageCallback;
    /**弹框状态回调*/
    private OnFullScreenStatusCallback onFullScreenStatusCallback;
    /**广告数据*/
    private Map<String, String> map = new HashMap<>();

    public FullSrceenDialogControl(Activity activity) {
        super(activity);
    }

    @Override
    public void isShow(String data, @NonNull OnPopDialogCallback callback) {
        if (mActivity == null || TextUtils.isEmpty(data)){
            callback.onNextShow();
            return;
        }
        if (isShowAd(StringManager.getFirstMap(data))
                && craeteAD()) {
            callback.onCanShow();
        } else {
            callback.onNextShow();
        }
    }

    @Override
    public void show() {
        if(onLoadImageCallback != null && map != null && map.isEmpty()){
            onLoadImageCallback.onLoadImage(map.get("img"), new OnAfterLoadImageCallback() {
                @Override
                public void onAfterLoadImage(Bitmap bitmap) {
                    adShow(map, bitmap);
                }
            });
        }
    }

    /**
     * 设置弹框推广位
     */
    private boolean craeteAD() {
        Log.i("tzy", "craeteAD");
        //广告
        map = FullScreenManager.getWelcomeInfo(mActivity);
        // 设置welcome图片
        if (map != null && map.get("img") != null && map.get("img").length() > 10) {
            //显示几次
            String showNumStr = TextUtils.isEmpty(map.get("showNum")) ? "0" : map.get("showNum");
            int showNum = Integer.parseInt(TextUtils.isEmpty(showNumStr) ? "0" : showNumStr);
            String currentShowNumStr = (String) FileManager.loadShared(mActivity, FileManager.xmlFile_popdialog, FileManager.xmlKey_fullSrceenShowNum);
            //当前已经显示了几次
            int currentShowNum = Integer.parseInt(TextUtils.isEmpty(currentShowNumStr) ? "0" : currentShowNumStr);
            if ((showNum == 0 || currentShowNum < showNum)) {
                if (showNum != 0) {
                    currentShowNum++;
                }
                FileManager.saveShared(mActivity, FileManager.xmlFile_popdialog, FileManager.xmlKey_fullSrceenShowNum, currentShowNum + "");
                return true;
            }
        } else {
            final XHADView adScrollView = XHADView.getInstence(mActivity);
            if (adScrollView != null) {
                adScrollView.hide();
            }

        }
        return false;
    }

    private boolean isShowAd(Map<String, String> mData){
        if (mData.containsKey("adConfig")) {
            String valueConfig = mData.get("adConfig");
            if (!TextUtils.isEmpty(valueConfig)) {
                ArrayList<Map<String, String>> configMaps = StringManager.getListMapByJson(valueConfig);
                if (configMaps != null && configMaps.size() > 0) {
                    for (Map<String, String> map : configMaps) {
                        if (map != null) {
                            String keyNum = "";
                            if (map.containsKey("1")) {
                                keyNum = String.valueOf(1);
                            } else if (map.containsKey("2")) {
                                keyNum = String.valueOf(2);
                            } else if (map.containsKey("3")) {
                                keyNum = String.valueOf(3);
                            } else if (map.containsKey("4")) {
                                keyNum = String.valueOf(4);
                            }
                            String valueNum = map.get(keyNum);
                            if (!TextUtils.isEmpty(valueNum) && "2".equals(StringManager.getFirstMap(valueNum).get("open")) && "personal".equals(StringManager.getFirstMap(valueNum).get("type")))
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //显示AD
    private void adShow(final Map<String, String> map, Bitmap bmp) {
        Log.i("tzy", "adShow");
        if (bmp == null
                || TextUtils.isEmpty(map.get("times"))
                || TextUtils.isEmpty(map.get("delay"))) {
            return;
        }
        //需要时activity的context
        final XHADView adScrollView = XHADView.getInstence(mActivity);
        if (adScrollView != null) {
            adScrollView.setOnManualCloseStatisticsCallback(new XHADView.OnManualCloseStatisticsCallback() {
                @Override
                public void onManualClose() {
                    if(onFullScreenStatusCallback != null){
                        onFullScreenStatusCallback.onClickClose();
                    }
                }
            });
            adScrollView.refreshContext(mActivity);
            adScrollView.setImage(bmp);
            if(onFullScreenStatusCallback != null){
                onFullScreenStatusCallback.onShow();
            }
            adScrollView.setADClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adScrollView.hide();
                    if(onFullScreenStatusCallback != null){
                        onFullScreenStatusCallback.onClickImage(map);
                    }
                }
            });
            int displayTime = Integer.parseInt(map.get("times"));
            int delay = Integer.parseInt(map.get("delay"));
            //必须调用该方法初始化
            adScrollView.initTimer(delay * 1000, displayTime * 1000);
//            adScrollView.initTimer(0, 0);
        }
    }

    /**全屏AD弹框相关回调*/
    public interface OnFullScreenStatusCallback{
        /**展示*/
        void onShow();
        /**点击图片*/
        void onClickImage(Map<String,String> map);
        /**点击关闭*/
        void onClickClose();
    }

    public interface OnLoadImageCallback{
        void onLoadImage(String imageUrl,OnAfterLoadImageCallback callback);
    }

    public interface OnAfterLoadImageCallback{
        void onAfterLoadImage(Bitmap bitmap);
    }

    /*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public OnFullScreenStatusCallback getOnFullScreenStatusCallback() {
        return onFullScreenStatusCallback;
    }

    public void setOnFullScreenStatusCallback(OnFullScreenStatusCallback onFullScreenStatusCallback) {
        this.onFullScreenStatusCallback = onFullScreenStatusCallback;
    }

    public OnLoadImageCallback getOnLoadImageCallback() {
        return onLoadImageCallback;
    }

    public void setOnLoadImageCallback(OnLoadImageCallback onLoadImageCallback) {
        this.onLoadImageCallback = onLoadImageCallback;
    }
}
