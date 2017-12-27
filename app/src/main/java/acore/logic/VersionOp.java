package acore.logic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.download.down.VersionUpload;
import com.popdialog.base.BaseDialogControl;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangha.R;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilLog;

import static com.popdialog.AllPopDialogControler.TAG;

public class VersionOp extends BaseDialogControl {
    @SuppressLint("StaticFieldLeak")
    private volatile static VersionOp versionOp = null;
    private String path = FileManager.getSDCacheDir();
    private String apkName = "香哈菜谱";
    private VersionUpload versionUpload;
    /**是否有相关提示*/
    private boolean mShowPro;
    /** 是否《必须》升级 */
    public boolean isMustUpdata = false;
    /** 是否《需要》升级 */
    public boolean isNeedUpdata = false;
    /**是否静默安装*/
    private boolean misSilentInstall = false;

    private int appNum, hintNum;
    private String newNum, nowNum;

    private static String tongjiId = "a_silent";

    private VersionOp(Activity activity) {
        super(activity);
    }

    public static VersionOp getInstance() {
        if (versionOp == null) {
            versionOp = new VersionOp(XHActivityManager.getInstance().getCurrentActivity());
        }
        return versionOp;
    }

    @Override
    public void isShow(String data, final OnPopDialogCallback callback) {
        checkUpdate(false, new OnCheckUpdataCallback() {
            @Override
            public void onPreUpdate() {
            }

            @Override
            public void onNeedUpdata() {
                isNeedUpdata = true;
                callback.onCanShow();
            }

            @Override
            public void onNotNeed() {
                isNeedUpdata = false;
                callback.onNextShow();
            }

            @Override
            public void onFail() {
                isNeedUpdata = false;
                callback.onNextShow();
            }
        });
    }

    @Override
    public void show() {
        if (isMustUpdata || !misSilentInstall) {
            Log.i(TAG, "VersionOp :: show() :: starUpdate()");
            versionUpload.starUpdate(!mShowPro, silentListener);
        } else {
            Log.i(TAG, "VersionOp :: show() :: silentInstall()");
            File file = new File(path + apkName + "_" + newNum + ".apk");
            VersionUpload.silentInstall(isMustUpdata, XHActivityManager.getInstance().getCurrentActivity(), Uri.fromFile(file),
                    VersionUpload.INTALL_TYPE_NEXT_STAR, true, nowNum, newNum, appNum, hintNum, silentListener);
        }
    }

    @Override
    public Activity getCurrentActivity() {
        return null;
    }

    /**
     * 检查更新
     * @param showPro
     * @param callback
     */
    private void checkUpdate(boolean showPro, final OnCheckUpdataCallback callback) {
        mShowPro = showPro;
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        //手动升级
        if (mShowPro)
            map.put("update", "1");
        //请求检查升级接口
        ReqInternet.in().doPost(StringManager.api_versionInfo, map,
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, final String url, Object returnObj) {
                        if (flag >= ReqInternet.REQ_OK_STRING) {
                            Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
                            try {
                                Map<String, String> map = StringManager.getFirstMap(returnObj);
                                //当需要升级时，服务端才返回升级数据
                                if (map != null && !map.isEmpty()) {
                                    //当前版本
                                    nowNum = getVerName(XHApplication.in());
                                    //新版本
                                    newNum = map.get("code");
                                    String content = map.get("content");
                                    String updateUrl = map.get("url");
                                    //cishu为：0，不是无限，是无次数，最大此时为127
                                    hintNum = Integer.parseInt(map.get("cishu"));
                                    //是否需要强制升级
                                    appNum = Integer.parseInt(map.get("appNum"));
                                    isMustUpdata = appNum == 0;
                                    //升级中是否可用	1-不可用，2-可用
                                    boolean isPlay = "2".equals(map.get("play"));
                                    boolean isNeedUpdata = false;
                                    if (!mShowPro) {
                                        misSilentInstall = isSilentInstall(isNeedUpdata, VersionUpload.INTALL_TYPE_NEXT_STAR, nowNum, newNum, appNum, hintNum);
                                        isNeedUpdata = misSilentInstall;
                                    }
                                    versionUpload = new VersionUpload(XHActivityManager.getInstance().getCurrentActivity(), content, R.drawable.ic_launcher, nowNum, newNum,
                                            isMustUpdata, isPlay, hintNum, appNum, updateUrl, path, apkName, vsUpListener);
                                    if (!misSilentInstall){
                                        isNeedUpdata = versionUpload.isUpdata(!mShowPro);
                                    }
                                    Log.i(TAG, "checkUpdate :: isNeedUpdata = " + isNeedUpdata + " ; isMustUpdata = " + isMustUpdata);
                                    if (isNeedUpdata) {
                                        callback.onNeedUpdata();
                                    } else {
                                        callback.onNotNeed();
                                    }
                                } else {
                                    //不需要升级
                                    if (mShowPro) {
                                        Tools.showToast(mAct, "已是最新版本！");
                                    }
                                    callback.onNotNeed();
                                }
                            } catch (Exception e) {
                                //Bugly上报异常
                                CrashReport.postCatchedException(e,new Thread());
                                if (mShowPro)
                                    Tools.showToast(mAct, "获取新版本错误，请稍后再试");
                                callback.onFail();
                                return;
                            }
                        } else {
                            callback.onFail();
                        }
                    }
                });
    }

    /**
     * * 检查更新
     *
     * @param callback
     * @param showPro 是否显示更新进度框
     */
    public void toUpdate(final OnCheckUpdataCallback callback, final boolean showPro) {
        mShowPro = showPro;
        if (callback != null) {
            callback.onPreUpdate();
        }
        checkUpdate(showPro, new OnCheckUpdataCallback() {
            @Override
            public void onPreUpdate() {

            }

            @Override
            public void onNeedUpdata() {
                versionUpload.starUpdate(!mShowPro, silentListener);
                if (callback != null) {
                    callback.onNeedUpdata();
                }
            }

            @Override
            public void onNotNeed() {
                if (callback != null) {
                    callback.onNotNeed();
                }
            }

            @Override
            public void onFail() {
                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }

    private VersionUpload.VersionUpdateListener vsUpListener = new VersionUpload.VersionUpdateListener() {
        @Override
        public void onActionDown() {
            super.onActionDown();
            XHClick.onEvent(XHActivityManager.getInstance().getCurrentActivity(), "appUpdate", "立即");
        }

        @Override
        public void onLaterUpdate() {
            super.onLaterUpdate();
            XHClick.onEvent(XHActivityManager.getInstance().getCurrentActivity(), "appUpdate", "稍后");
        }

        @Override
        public void downOk(Uri uri, boolean isSilent) {

        }

        @Override
        public void onUnShowDialog(int flag) {
            super.onUnShowDialog(flag);
        }

        @Override
        public void downError(String arg0) {
            Log.i("xianghaTag","arg0::"+arg0);
            Tools.showToast(XHActivityManager.getInstance().getCurrentActivity(), arg0);
        }
    };

    /**
     * 按照指定type提示安装框
     *
     * @param type ：弹安装框的时间
     */
    private boolean isSilentInstall(boolean isMustUp, int type, String nowNum, String newNum, int appNum, int hintNum) {
        File file = new File(path + apkName + "_" + newNum + ".apk");
        return VersionUpload.isSilentInstall(isMustUp, XHActivityManager.getInstance().getCurrentActivity(), Uri.fromFile(file), type, true, nowNum, newNum, appNum, hintNum, silentListener);
    }

    // 获取当前版本
    public static String getVerName(Context context) {
        String verCode = "0.0.0";
        try {
            if (context != null)
                verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            UtilLog.reportError("版本号获取异常", e);
        }
        return verCode;
    }

    public void onDesotry() {
        if (versionUpload != null) {
            versionUpload.cancelDownLoad();
        }
    }

    private boolean isCancel = true;
    private VersionUpload.VersionUpdateSilentListener silentListener = new VersionUpload.VersionUpdateSilentListener() {
        @Override
        public void onCancel() {
            if (isCancel)
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "点击弹框关闭“手机返回键”", "");
        }

        @Override
        public void onShow() {
            isCancel = true;
            XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "静默更新弹框次数”", "");
        }

        @Override
        public void onSureClick() {
            isCancel = false;
            XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "点击弹框确认", "");
        }
    };

    public interface OnCheckUpdataCallback {
        void onPreUpdate();

        void onNeedUpdata();

        void onNotNeed();

        void onFail();
    }

}
