package com.popdialog;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.mrtrying.xh_popdialog.R;
import com.popdialog.base.BaseDialogControl;
import com.popdialog.util.FileManager;
import com.popdialog.util.StringManager;
import com.popdialog.util.Tools;
import com.popdialog.util.ToolsDevice;
import com.popdialog.view.PopDialog;

import java.util.List;
import java.util.Map;

/**
 * 倒流弹框
 * Created by XiangHa on 2017/3/9.
 */
public class GuideDialogControl extends BaseDialogControl {

    private PopDialog dialog;
    private Map<String, String> map;
    private int count;

    private OnGuideClickCallback onGuideSureCallback;

    public GuideDialogControl(Activity activity) {
        super(activity);
    }

    @Override
    public void isShow(String data, final OnPopDialogCallback callback) {
        if (mActivity == null || TextUtils.isEmpty(data)) {
            callback.onNextShow();
            return;
        }
        //解析数据
        List<Map<String, String>> returnData = StringManager.getListMapByJson(data);
        if (returnData.size() > 0) {
            map = returnData.get(0);
            if (TextUtils.isEmpty(map.get("title"))
                    || TextUtils.isEmpty(map.get("popText"))
                    || TextUtils.isEmpty(map.get("confirmButtonText"))
                    || TextUtils.isEmpty(map.get("cancelButtonText"))
                    || TextUtils.isEmpty(map.get("type"))
                    || TextUtils.isEmpty(map.get("showNum"))
                    || TextUtils.isEmpty(map.get("packageName"))
                    || TextUtils.isEmpty(map.get("alertType"))
                    || TextUtils.isEmpty(map.get("url"))) {
                callback.onNextShow();
                return;
            }
            //只有当未安装要倒流的app，才提升下载
            if (ToolsDevice.isAppInPhone(mActivity, map.get("packageName")) == 0) {
                String countUrl = (String) FileManager.loadShared(mActivity, FileManager.xmlFile_appUrl, FileManager.xmlKey_confirmCount);
                String countStr = (String) FileManager.loadShared(mActivity, FileManager.xmlFile_popdialog, FileManager.xmlKey_confirmCount);
                String lastShowTime = String.valueOf(FileManager.loadShared(mActivity, FileManager.xmlFile_popdialog, FileManager.xmlKey_confirmLastShowTime));
                String showTimeInterval = map.get("showTimeInterval");
                boolean isShowDialog = false;
                if (TextUtils.isEmpty(lastShowTime) || TextUtils.isEmpty(showTimeInterval))
                    isShowDialog = true;
                else {
                    Long lastTime = Long.parseLong(lastShowTime);
                    int spaceTime = Integer.parseInt(showTimeInterval);
                    if (System.currentTimeMillis() - lastTime >= spaceTime * 60 * 1000) {
                        isShowDialog = true;
                    }
                }

                if (!TextUtils.isEmpty(countUrl) && !countUrl.equals(map.get("url"))) {
                    countStr = "";
                }
                count = "".equals(countStr) ? 0 : Integer.parseInt(countStr);
                int showNum = Integer.parseInt(map.get("showNum"));
                if (count < showNum && isShowDialog) {
                    callback.onCanShow();
                } else callback.onNextShow();
            } else callback.onNextShow();
        } else callback.onNextShow();
    }

    @Override
    public void show() {
        count++;
        FileManager.saveShared(mActivity, FileManager.xmlFile_appUrl, FileManager.xmlKey_confirmCount, map.get("url"));
        FileManager.saveShared(mActivity, FileManager.xmlFile_popdialog, FileManager.xmlKey_confirmCount, "" + count);
        //展示
        showDialog(map);
    }

    private void showDialog(final Map<String, String> map) {
        if (mActivity == null || map == null || map.isEmpty())
            return;
        String alertType = map.get("alertType");
        String message = map.get("popText");
        int lineNumber = Tools.getTextNumbers(mActivity, Tools.getDimen(mActivity, R.dimen.dp_222), Tools.dip2px(mActivity, (float) 12.5));
        boolean cancelBold = false, sureBold = false;
        String titleColor = "#000000", messageColor = "#000000";
        String confirmButtonColor = "#007aff", cancelButtonColor = "#007aff";
        int layoutId = message.length() > lineNumber ? R.layout.dialog_common : R.layout.dialog_goodcomment;
        final String twoLevel;
        if ("1".equals(alertType)) {
            cancelBold = true;
            twoLevel = "原生左侧加粗";
        } else if ("2".equals(alertType)) {
            sureBold = true;
            twoLevel = "原生右侧加粗";
        } else {
            twoLevel = "自定义样式";
            titleColor = "#333333";
            messageColor = "#999999";
            confirmButtonColor = map.get("confirmButtonColor");
            cancelButtonColor = map.get("cancelButtonColor");
        }
        dialog = new PopDialog(mActivity, layoutId);
        dialog.setTitle(map.get("title"), titleColor)
                .setMessage(message, messageColor)
                .setSureButton(map.get("confirmButtonText"), confirmButtonColor, sureBold, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        if (onGuideSureCallback != null) {
                            onGuideSureCallback.onClickSure(map, twoLevel, map.get("confirmButtonText"));
                        }
                    }
                })
                .setCanselButton(map.get("cancelButtonText"), cancelButtonColor, cancelBold, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        if (onGuideSureCallback != null) {
                            onGuideSureCallback.onClickCannel(map, twoLevel, map.get("cancelButtonText"));
                        }
                    }
                }).show();
        FileManager.saveShared(mActivity, FileManager.xmlFile_popdialog, FileManager.xmlKey_confirmLastShowTime, String.valueOf(System.currentTimeMillis()));
    }

    public interface OnGuideClickCallback {
        void onClickSure(Map<String, String> map, String twoLevel, String text);

        void onClickCannel(Map<String, String> map, String twoLevel, String text);
    }

    /*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public OnGuideClickCallback getOnGuideClickCallback() {
        return onGuideSureCallback;
    }

    public void setOnGuideClickCallback(OnGuideClickCallback onGuideSureCallback) {
        this.onGuideSureCallback = onGuideSureCallback;
    }
}
