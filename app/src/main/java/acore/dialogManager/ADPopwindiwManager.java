package acore.dialogManager;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import acore.widget.XHADView;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.AdParent;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;

import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * PackageName : acore.dialogManager
 * Created by MrTrying on 2017/6/26 10:02.
 * E_mail : ztanzeyu@gmail.com
 */

public class ADPopwindiwManager extends DialogManagerParent {

    @Override
    public void isShow(@NonNull OnDialogManagerCallback callback) {
        if (isShowAD(AdPlayIdConfig.FULLSCREEN, AdParent.ADKEY_BANNER)) {
            show();
            if (callback != null)
                callback.onShow();
        } else {
            if (callback != null)
                callback.onGone();
        }
    }

    @Override
    public void show() {
        craeteAD();
    }

    @Override
    public void cancel() {
    }

    /**
     * 设置弹框推广位
     */
    private void craeteAD() {
        Log.i("tzy", "craeteAD");
        //广告
        final Map<String, String> map = getWelcomeInfo();
        // 设置welcome图片
        if (map != null && map.get("img") != null && map.get("img").length() > 10) {
        //显示几次
        String showNumStr = TextUtils.isEmpty(map.get("showNum")) ? "0" : map.get("showNum");
        int showNum = Integer.parseInt(TextUtils.isEmpty(showNumStr) ? "0" : showNumStr);
        String currentShowNumStr = (String) FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.xmlKey_showNum);
        //当前已经显示了几次
        int currentShowNum = Integer.parseInt(TextUtils.isEmpty(currentShowNumStr) ? "0" : currentShowNumStr);
        if ((showNum == 0 || currentShowNum < showNum)) {
            if (showNum != 0) {
                currentShowNum++;
            }
            FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.xmlKey_showNum, currentShowNum + "");
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                    .load(map.get("img"))
                    .setSaveType(LoadImage.SAVE_LONG)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                        adShow(map, bitmap);
                    }
                });
        }
        } else {
            final XHADView adScrollView = XHADView.getInstence(XHActivityManager.getInstance().getCurrentActivity());
            if (adScrollView != null) {
                adScrollView.hide();
            }
        }
    }

    private boolean isShowAD(String adPlayId, String adKey) {
        Map<String, String> mData = AdConfigTools.getInstance().getAdConfigData(adPlayId);
        if (!TextUtils.isEmpty(adPlayId) && AdPlayIdConfig.FULLSCREEN.equals(adPlayId) && !TextUtils.isEmpty(adKey) && AdParent.ADKEY_BANNER.equals(adKey)) {
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
        final XHADView adScrollView = XHADView.getInstence(XHActivityManager.getInstance().getCurrentActivity());
        if (adScrollView != null) {
            adScrollView.refreshContext(XHActivityManager.getInstance().getCurrentActivity());
            adScrollView.setImage(bmp);
            //展示统计
            XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "ad_show_index", "全屏", "xh");
            adScrollView.setADClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击统计
                    XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), "ad_click_index", "全屏", "xh");
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), map.get("url"), true);
                    adScrollView.hide();
                }
            });
            int displayTime = Integer.parseInt(map.get("times"));
            int delay = Integer.parseInt(map.get("delay"));
            //必须调用该方法初始化
            adScrollView.initTimer(delay * 1000, displayTime * 1000);
//            adScrollView.initTimer(0, 0);
        }
    }

    // 保存welcome页数据
    public static void saveWelcomeInfo(String json) {
        Map<String, String> map = getWelcomeInfo();
        if (json == null || json.length() < 10) {
            delWelcomeInfo(map);
        } else {
            String file_content = FileManager.readFile(FileManager.getDataDir() + FileManager.file_welcome);
            file_content = file_content.trim();
            if (!file_content.equals(json)) {
                delWelcomeInfo(map);
                FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.xmlKey_showNum, "0");
                FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_welcome, json, false);
                if (map != null && map.containsKey("img")) {
                    String imgUrl = map.get("img");
                    BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                            .load(imgUrl)
                            .setSaveType(LoadImage.SAVE_LONG)
                            .build();
                    if (bitmapRequest != null) {
                        bitmapRequest.into(new SubBitmapTarget() {
                            @Override
                            public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {

                            }
                        });
                    }
                }
            }
        }
    }

    // 删除welcome页数据
    private static void delWelcomeInfo(Map<String, String> map) {
        if (map != null && map.get("img") != null)
            ImgManager.delImg(map.get("img"));
        FileManager.delDirectoryOrFile(FileManager.getDataDir() + FileManager.file_welcome);
    }

    // 从文件获取welcome页数据
    public static Map<String, String> getWelcomeInfo() {
        String file_content = FileManager.readFile(FileManager.getDataDir() + FileManager.file_welcome);
        if (file_content.length() > 10)
            return getListMapByJson(file_content).get(0);
        return null;
    }
}
