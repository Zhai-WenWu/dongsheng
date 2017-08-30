package third.share;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;

import acore.logic.XHClick;
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

    public void share(String type, String imageUrl, String content) {
        Platform.ShareParams sp = new Platform.ShareParams();
        if (QQ_ZONE.equals(type) || WEI_QUAN.equals(type) || SINA_NAME.equals(type)) {
            if (!TextUtils.isEmpty(content)) {
                sp.setText(content);
            }
        }
        sp.setImageUrl(imageUrl);
        if (WEI_QUAN.equals(type) || WEI_XIN.equals(type)) {
            sp.setShareType(Platform.SHARE_IMAGE);
        }

        Platform platform = getPlatform(type);
        if (platform == null) {
            return;
        }
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

    PlatformActionListener paListener = new PlatformActionListener() {

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            Message msg = shareHandler.obtainMessage();
            msg.what = SHARE_OK;
            msg.obj = platform.getName();
            shareHandler.sendMessage(msg);
        }

        @Override
        public void onError(Platform plf, int arg1, Throwable arg2) {
            arg2.printStackTrace();
            Message msg = shareHandler.obtainMessage();
            msg.what = SHARE_ERROR;
            msg.obj = plf.getName();
            shareHandler.sendMessage(msg);
        }

        @Override
        public void onCancel(Platform plf, int arg1) {
            Message msg = shareHandler.obtainMessage();
            msg.what = SHARE_CANCLE;
            msg.obj = plf.getName();
            shareHandler.sendMessage(msg);
        }
    };

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
                    if (("微信".equals(pf[0]) || pf[0].indexOf("微信") > -1) && ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0) {
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
        String[] pf = new String[2];
        if (QQ_NAME.equals(name)) {
            pf[0] = "QQ";
            pf[1] = "1";
        } else if (QQ_ZONE.equals(name)) {
            pf[0] = "QQ空间";
            pf[1] = "2";
        } else if (WEI_XIN.equals(name)) {
            pf[0] = "微信";
            pf[1] = "3";
        } else if (WEI_QUAN.equals(name)) {
            pf[0] = "微信朋友圈";
            pf[1] = "4";
        } else if (SINA_NAME.equals(name)) {
            pf[0] = "新浪";
            pf[1] = "5";
        } else if (SHORT_MESSAGE.equals(name)) {
            pf[0] = "短信";
            pf[1] = "6";
        }
        return pf;
    }

}
