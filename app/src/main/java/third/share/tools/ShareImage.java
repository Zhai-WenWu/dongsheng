/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package third.share.tools;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import acore.tools.ObserverManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * PackageName : third.share
 * Created by MrTrying on 2017/8/30 18:30.
 * E_mail : ztanzeyu@gmail.com
 */

public class ShareImage {
    private final int SHARE_OK = 1;
    private final int SHARE_ERROR = 2;
    private final int SHARE_CANCLE = 3;

    public static final String QQ_ZONE = QZone.NAME;
    public static final String QQ_NAME = QQ.NAME;
    public static final String WEI_XIN = Wechat.NAME;
    public static final String WEI_QUAN = WechatMoments.NAME;
    public static final String SINA_NAME = SinaWeibo.NAME;
    public static final String SHORT_MESSAGE = ShortMessage.NAME;

    Context mContext;

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
            sendMessage(platform.getName(),SHARE_OK);
            notifyShareResult(platform.getName(),"2");
        }

        @Override
        public void onError(Platform platform, int arg1, Throwable arg2) {
            notifyShareResult(platform.getName(),"1");
            sendMessage(platform.getName(),SHARE_ERROR);
            arg2.printStackTrace();

        }

        @Override
        public void onCancel(Platform platform, int arg1) {
            notifyShareResult(platform.getName(),"1");
            sendMessage(platform.getName(),SHARE_CANCLE);
        }

        private void sendMessage(String name,int status){
            Message msg = shareHandler.obtainMessage();
            msg.what = status;
            msg.obj = name;
            shareHandler.sendMessage(msg);
        }
    };

    public void notifyShareResult(String platform,String success){
        String jsCallbackParams = "";
        if (QQ_NAME.equals(platform)) {
            jsCallbackParams = "QQ";
        } else if (QQ_ZONE.equals(platform)) {
            jsCallbackParams = "QZone";
        } else if (WEI_XIN.equals(platform)) {
            jsCallbackParams = "Wechat";
        } else if (WEI_QUAN.equals(platform)) {
            jsCallbackParams = "WechatMoments";
        } else if (SINA_NAME.equals(platform)) {
            jsCallbackParams = "SinaWeibo";
        }
        Map<String,String> data = new HashMap<>();
        data.put("platform",platform);
        data.put("status",success);
        data.put("callbackParams", jsCallbackParams);
        ObserverManager.getInstance().notify(ObserverManager.NOTIFY_SHARE,this,data);
    }

    public Handler shareHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int flag = msg.what;
            String pla = msg.obj.toString();
            String[] pf = getPlatformParam(pla);
            switch (flag) {
                case SHARE_OK:
                    Tools.showToast(mContext, pf[0] + "分享成功");
                    break;
                case SHARE_ERROR:
                    if (("微信".equals(pf[0]) || pf[0].contains("微信")) && ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0) {
                        Tools.showToast(mContext, "未检测到相关应用");
                    } else
                        Tools.showToast(mContext, pf[0] + "分享失败");
                    break;
                case SHARE_CANCLE:
                    Tools.showToast(mContext, pf[0] + "取消分享");
                    break;
            }
            return false;
        }
    });

    public String[] getPlatformParam(String name) {
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

}
