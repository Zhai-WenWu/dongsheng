package acore.dialogManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.download.container.DownloadCallBack;
import com.download.down.DownLoad;
import com.download.tools.FileUtils;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.XhNewDialog;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * 倒流弹框
 * Created by XiangHa on 2017/3/9.
 */
public class GuideManager extends DialogManagerParent {
    public Activity mAct;

    private XhNewDialog dialog;
    private Map<String, String> map;
    private int count;

    @Override
    public void cancel() {
        if (dialog != null) dialog.cancel();
    }

    @Override
    public void show() {
        count++;
        FileManager.saveShared(mAct, FileManager.xmlFile_appUrl, FileManager.xmlKey_confirnCount, map.get("url"));
        FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, FileManager.xmlKey_confirnCount, "" + count);
        showDialog(map);
    }

    @Override
    public void isShow(final OnDialogManagerCallback callback) {
        mAct = XHActivityManager.getInstance().getCurrentActivity();
        if (mAct == null) return;
        ReqInternet.in().doGet(StringManager.api_toConfirn, new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);
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
                            callback.onGone();
                            return;
                        }
                        //只有当未安装要倒流的app，才提升下载
                        if (ToolsDevice.isAppInPhone(context, map.get("packageName")) == 0) {
                            String countUrl = (String) FileManager.loadShared(mAct, FileManager.xmlFile_appUrl, FileManager.xmlKey_confirnCount);
                            String countStr = (String) FileManager.loadShared(mAct, FileManager.xmlFile_appInfo, FileManager.xmlKey_confirnCount);
                            String lastShowTime = String.valueOf(FileManager.loadShared(mAct, FileManager.xmlFile_appInfo, FileManager.xmlKey_confirnLastShowTime));
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
//                                    showNum = 100;
                            if (count < showNum && isShowDialog) {
                                callback.onShow();
                            } else callback.onGone();
                        } else callback.onGone();
                    } else callback.onGone();
                } else callback.onGone();
            }
        });
    }

    private void showDialog(final Map<String, String> map) {
        mAct = XHActivityManager.getInstance().getCurrentActivity();
        if (mAct == null) return;
        String alertType = map.get("alertType");
//        alertType = "1";
        String message = map.get("popText");
//        message = "赞歌五星好评吧";
        int lineNumber = Tools.getTextNumbers(mAct, Tools.getDimen(mAct, R.dimen.dp_222), ToolsDevice.dp2px(mAct, (float) 12.5));
        boolean cancelBold = false, sureBold = false;
        String titleColor = "#000000", messageColor = "#000000";
        String confirmButtonColor = "#007aff", cancelButtonColor = "#007aff";
        String tongjiId = "a_NewDiversion"; //自定义
        String twoLevel;
        int layoutId;
        if (message.length() > lineNumber) {
            layoutId = R.layout.xh_new_dialog;
        } else {
            layoutId = R.layout.a_dialog_goodcomment;
        }
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


        dialog = new XhNewDialog(mAct, layoutId, tongjiId, twoLevel);
        dialog.setTitle(map.get("title"), titleColor)
                .setMessage(message, messageColor)
                .setSureButton(map.get("confirmButtonText"), confirmButtonColor, sureBold, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String type = map.get("type");
                        String url = map.get("url");
                        if ("1".equals(type)) {
                            try {
                                final DownLoad downLoad = new DownLoad(mAct);
                                downLoad.setNotifaction("开始下载", map.get("name") + ".apk", "正在下载", R.drawable.ic_launcher, false);
                                downLoad.starDownLoad(url, FileManager.getCameraDir(), map.get("name"), true, new DownloadCallBack() {

                                    @Override
                                    public void starDown() {
                                        super.starDown();
                                        Tools.showToast(mAct, "开始下载");
                                    }

                                    @Override
                                    public void downOk(Uri uri) {
                                        super.downOk(uri);
                                        FileUtils.install(mAct, uri);
                                        downLoad.cacelNotification();
                                    }

                                    @Override
                                    public void downError(String s) {
                                        Tools.showToast(mAct, "下载失败：" + s);
                                        downLoad.cacelNotification();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//							//直接下载
//							AppDownload.downloadApp(act, url, map.get("name"));
                        } else if ("2".equals(type)) {
                            //app内部打开
                            AppCommon.openUrl(mAct, url, true);
                        } else if ("3".equals(type)) {
                            //浏览器打开
                            Uri uri = Uri.parse(url);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            mAct.startActivity(intent);
                        }
                        dialog.cancel();
                    }
                })
                .setCanselButton(map.get("cancelButtonText"), cancelButtonColor, cancelBold, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                }).show();
        FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, FileManager.xmlKey_confirnLastShowTime, String.valueOf(System.currentTimeMillis()));
    }
}
