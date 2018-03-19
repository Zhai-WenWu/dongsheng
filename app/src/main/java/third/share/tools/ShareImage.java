/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package third.share.tools;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import acore.tools.ObserverManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import static third.share.tools.ShareTools.CANCEL;
import static third.share.tools.ShareTools.ERROR;
import static third.share.tools.ShareTools.OK;
import static third.share.tools.ShareTools.QQ_NAME;
import static third.share.tools.ShareTools.QQ_ZONE;
import static third.share.tools.ShareTools.SHORT_MESSAGE;
import static third.share.tools.ShareTools.SINA_NAME;
import static third.share.tools.ShareTools.WEI_QUAN;
import static third.share.tools.ShareTools.WEI_XIN;

/**
 * PackageName : third.share
 * Created by MrTrying on 2017/8/30 18:30.
 * E_mail : ztanzeyu@gmail.com
 */

public class ShareImage {

    Context mContext;

    private ShareTools.ActionListener mActionListener;

    public void setActionListener(ShareTools.ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public ShareImage(Context context) {
        this.mContext = context;
    }

    public void share(String type, String imageUrl) {
        Platform.ShareParams sp = new Platform.ShareParams();
        if(imageUrl.startsWith("http")){
            sp.setImageUrl(imageUrl);
        }else if(new File(imageUrl).exists()){
            sp.setImagePath(imageUrl);
        }else{
            Tools.showToast(mContext,"图片链接有误");
            return;
        }
        if (WEI_QUAN.equals(type) || WEI_XIN.equals(type)) {
            sp.setShareType(Platform.SHARE_IMAGE);
        }

        Platform platform = getPlatform(type);
        if (platform == null)
            return;

        platform.setPlatformActionListener(paListener); // 设置分享事件回调
        // 执行图文分享
        platform.share(sp);
    }

    private Platform getPlatform(@NonNull String type) {
        if (QQ_ZONE.equals(type)
                || QQ_NAME.equals(type)
                || WEI_XIN.equals(type)
                || WEI_QUAN.equals(type)
                || SINA_NAME.equals(type)
                || SHORT_MESSAGE.equals(type)) {
            return ShareSDK.getPlatform(type);
        } else {
            return null;
        }
    }

    /** 平台回调监听 */
    PlatformActionListener paListener = new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            if (mActionListener != null)
                mActionListener.onComplete(ShareTools.Option.SHARE.getType(), OK, platform, null);
            else
                handleCallback(ShareTools.Option.SHARE.getType(), OK, platform, null);
        }

        @Override
        public void onError(Platform platform, int arg1, Throwable arg2) {
            if (mActionListener != null)
                mActionListener.onError(ShareTools.Option.SHARE.getType(), ERROR, platform, null);
            else
                handleCallback(ShareTools.Option.SHARE.getType(), ERROR, platform, null);

        }

        @Override
        public void onCancel(Platform platform, int arg1) {
            if (mActionListener != null)
                mActionListener.onCancel(ShareTools.Option.SHARE.getType(), CANCEL, platform, null);
            else
                handleCallback(ShareTools.Option.SHARE.getType(), CANCEL, platform, null);
        }
    };

    private String getDefJsCallbackParams(String platform) {
        String ret = "";
        if (TextUtils.isEmpty(platform))
            return ret;
        if (QQ_NAME.equals(platform)) {
            ret = "QQ";
        } else if (QQ_ZONE.equals(platform)) {
            ret = "QZone";
        } else if (WEI_XIN.equals(platform)) {
            ret = "Wechat";
        } else if (WEI_QUAN.equals(platform)) {
            ret = "WechatMoments";
        } else if (SINA_NAME.equals(platform)) {
            ret = "SinaWeibo";
        }
        return ret;
    }

    private void notifyShareResult(String platform,String success, String jsCallbackParams){
        Map<String,String> data = new HashMap<>();
        data.put("platform",platform);
        data.put("status",success);
        data.put("callbackParams", jsCallbackParams);
        ObserverManager.getInstance().notify(ObserverManager.NOTIFY_SHARE,this,data);
    }

    private Handler shareHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int flag = msg.what;
            String pla = msg.obj.toString();
            String[] pf = getPlatformParam(pla);
            switch (flag) {
                case OK:
                    Tools.showToast(mContext, pf[0] + "分享成功");
                    String jsCallbackParams = null;
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        jsCallbackParams = bundle.getString("bundle");
                    }
                    notifyShareResult(pla, "2", TextUtils.isEmpty(jsCallbackParams) ?
                            getDefJsCallbackParams(pla) : jsCallbackParams);
                    break;
                case ERROR:
                    if (("微信".equals(pf[0]) || pf[0].contains("微信")) && ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0) {
                        Tools.showToast(mContext, "未检测到相关应用");
                    } else
                        Tools.showToast(mContext, pf[0] + "分享失败");
                    notifyShareResult(pla, "1", getDefJsCallbackParams(pla));
                    break;
                case CANCEL:
                    Tools.showToast(mContext, pf[0] + "取消分享");
                    notifyShareResult(pla, "1", getDefJsCallbackParams(pla));
                    break;
            }
            return false;
        }
    });

    private String[] getPlatformParam(String name) {
        if (QQ_NAME.equals(name)) {
            return new String[]{"QQ","1"};
        } else if (QQ_ZONE.equals(name)) {
            return new String[]{"QQ空间","2"};
        } else if (WEI_XIN.equals(name)) {
            return new String[]{"微信","3"};
        } else if (WEI_QUAN.equals(name)) {
            return new String[]{"微信朋友圈","4"};
        } else if (SINA_NAME.equals(name)) {
            return new String[]{"新浪","5"};
        } else if (SHORT_MESSAGE.equals(name)) {
            return new String[]{"短信","6"};
        }
        return new String[]{"",""};
    }

    private void handleCallback(int optionType, int callbackType, Platform platform, String
            jsonStr) {
        Message msg = shareHandler.obtainMessage();
        msg.what = callbackType;
        msg.obj = platform.getName();
        msg.arg1 = optionType;
        if (!TextUtils.isEmpty(jsonStr)) {
            Bundle bundle = new Bundle();
            bundle.putString("bundle", jsonStr);
            msg.setData(bundle);
        }
        shareHandler.sendMessage(msg);
    }

    public void notifyCallback(int optionType, int callbackType, Platform platform, String
            jsonStr) {
        handleCallback(optionType, callbackType, platform, jsonStr);
    }

}
