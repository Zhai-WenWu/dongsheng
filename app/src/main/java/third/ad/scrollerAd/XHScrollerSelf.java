package third.ad.scrollerAd;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.helper.XHActivityManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.ad.db.bean.XHSelfNativeData;
import third.ad.tools.AdConfigTools;

/**
 * 自有广告
 */
public class XHScrollerSelf extends XHScrollerAdParent {
    public static final String IMG_KEY = "littleImage";
    private XHSelfNativeData mNativeData = null;

    public XHScrollerSelf(String data, String mAdPlayId, int num) {
        super(mAdPlayId, num);
        key = "xh";
        adid = data;
    }

    @Override
    public void onResumeAd(String oneLevel, String twoLevel) {
        onAdShow(oneLevel, twoLevel, key);
        Log.i("zhangyujian", "广告展示:::" + XHScrollerAdParent.ADKEY_BANNER + ":::位置::" + twoLevel);
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onThirdClick(String oneLevel, String twoLevel) {
        if (null != mNativeData) {
            if ("1".equals(mNativeData.getDbType())) {
                showSureDownload(mNativeData, mAdPlayId, adid, key, mNativeData.getId());
            } else {
                onAdClick(oneLevel, twoLevel, key);
                handlerAdClick();
            }
        }
        Log.i("zhangyujian", "广告点击:::" + XHScrollerAdParent.ADKEY_BANNER + ":::位置::" + twoLevel);
    }

    @Override
    protected void postTongji(String event) {
        if (mNativeData != null && !TextUtils.isEmpty(mNativeData.getId())) {
            AdConfigTools.getInstance().postStatistics(event, mAdPlayId, adid, key, mNativeData.getId());
        }
    }

    /**
     * @param nativeData
     * @param adPlayId
     * @param key
     * @param adid
     */
    public static void showSureDownload(XHSelfNativeData nativeData, String adPlayId, String key, String adid, String id) {
        String message = ToolsDevice.getNetWorkSimpleType(XHActivityManager.getInstance().getCurrentActivity());
        Activity activity = XHActivityManager.getInstance().getCurrentActivity();
        if (activity != null && (!activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !activity.isDestroyed()))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(XHActivityManager.getInstance().getCurrentActivity());
            builder.setTitle("温馨提示")
                    .setMessage("当前为" + message + "网络，开始下载应用？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        AdConfigTools.getInstance().postStatistics("download", adPlayId, adid, key, id);
                        if (nativeData != null) {
                            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), nativeData.getUrl(), true);
                        }
                        Log.i("zhangyujian", "广告确认下载:::" + XHScrollerAdParent.ADKEY_BANNER + ":::位置::");
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }

    private void handlerAdClick() {
        if (mNativeData != null) {
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mNativeData.getUrl(), true);
        }
    }

    @Override
    public void getAdDataWithBackAdId(@NonNull final XHAdDataCallBack xhAdDataCallBack) {
        if (!isShow() || mNativeData == null) {//判断是否显示---不显示
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
            return;
        }
        if (!LoginManager.isShowAd()) {//特权不能去除活动广告
            if ("2".equals(mNativeData.getAdType())) {
                xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
                return;
            }
        }
        final Map<String, String> map = new HashMap<>();
        map.put("title", mNativeData.getBrandName());
        map.put("desc", mNativeData.getDesc());
        map.put("adType", mNativeData.getAdType());
        map.put("imgUrl", mNativeData.getBigImage());
        map.put(IMG_KEY, mNativeData.getLittleImage());
        map.put("iconUrl", mNativeData.getLogoImage());
        map.put("type", XHScrollerAdParent.ADKEY_BANNER);
        map.put("hide", "1");//2隐藏，1显示
        xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_BANNER, map);
    }

    public void setNativeData(XHSelfNativeData nativeData) {
        mNativeData = nativeData;
        if (mNativeData != null && "1".equals(mNativeData.getDbType())) {
            String appname = TextUtils.isEmpty(mNativeData.getBrandName()) ? Tools.getMD5(mNativeData.getUrl()) : mNativeData.getBrandName();
            mNativeData.setUrl("download.app?url=" + Uri.encode(mNativeData.getUrl()) + "&appname=" + appname);
        }
    }
}
