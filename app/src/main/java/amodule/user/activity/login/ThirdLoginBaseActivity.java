package amodule.user.activity.login;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.login.CountComputer;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.FileManager;
import acore.tools.Tools;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import third.push.xg.XGPushServer;
import third.share.tools.ShareTools;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;

/**
 * Created by Fang Ruijiao on 2017/8/14.
 */

public class ThirdLoginBaseActivity extends BaseLoginActivity {
    private static final int EMPOWER_OK = 1;
    private static final int EMPOWER_ERROR = 2;
    private static final int EMPOWER_CANCLE = 3;
    private static final int INFO_ERROR = 4;

    public static Map<String, String> userInfo = new HashMap<>(); // 当前登录用户信息
    private String mPlatformName;
    private String platform;
    private String loginType = "";

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int flag = msg.what;
            switch (flag) {
                case EMPOWER_OK:
//                    Tools.showToast(ThirdLoginBaseActivity.this, "授权完成");
                    String param = msg.obj.toString();

                    if (ShareTools.WEI_XIN.equals(platform)) {
                        loginType = WEIXIN_LOGIN_TYPE;
                    } else if (ShareTools.QQ_NAME.equals(platform)) {
                        loginType = QQ_LOGIN_TYPE;
                    } else if (ShareTools.SINA_NAME.equals(platform)) {
                        loginType = WEIBO_LOGIN_TYPE;
                    }
                    userLogin(ThirdLoginBaseActivity.this, loginType, param, "", "",
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {

                                    userInfo = (Map<String, String>) UtilFile.loadShared(ThirdLoginBaseActivity.this,
                                            FileManager.xmlFile_userInfo, "");
                                    String tel = userInfo.get("tel");
                                    if (TextUtils.isEmpty(tel)) {
                                        if (CountComputer.getTipCount(ThirdLoginBaseActivity.this) < 2) {
                                            gotoBindPhone(ThirdLoginBaseActivity.this, loginType);
                                            CountComputer.saveTipCount(ThirdLoginBaseActivity.this);
                                        } else {
                                            backToForward();
                                        }
                                    } else {
                                        backToForward();
                                    }
                                    //统计
                                    statisticsData(platform, "登录成功");
                                }

                                @Override
                                public void onFalse(int flag) {
                                    //统计
                                    statisticsData(platform, "登录失败");
                                }
                            });
                    break;
                case EMPOWER_ERROR:
                    Tools.showToast(ThirdLoginBaseActivity.this, "登录失败");
                    loadManager.hideProgressBar();
                    break;
                case EMPOWER_CANCLE:
                    Tools.showToast(ThirdLoginBaseActivity.this, "登录失败");
                    loadManager.hideProgressBar();
                    break;
                case INFO_ERROR:
                    Tools.showToast(ThirdLoginBaseActivity.this, mPlatformName + "平台获取信息失败");
                    loadManager.hideProgressBar();
                    break;
            }
            return false;
        }
    });

    public void statisticsData(@NonNull String platform, String value) {
        if (ShareTools.QQ_NAME.equals(platform)) {
            XHClick.mapStat(ThirdLoginBaseActivity.this, PHONE_TAG, "QQ登录", value);
        } else if (ShareTools.WEI_XIN.equals(platform)) {
            XHClick.mapStat(ThirdLoginBaseActivity.this, PHONE_TAG, "微信登录", value);
        } else if (ShareTools.SINA_NAME.equals(platform)) {
            XHClick.mapStat(ThirdLoginBaseActivity.this, PHONE_TAG, "微博登录", value);
        }
    }


    /**
     * 授权。如果授权成功，则获取用户信息</br>
     */
    public void thirdAuth(final Activity mAct, final String platform, final String mPlatformName) {
        this.mPlatformName = mPlatformName;
        this.platform = platform;
        loadManager.showProgressBar();
//        Tools.showToast(mAct, "授权开始");
//        ShareSDK.initSDK(mAct);
        Platform pf = ShareSDK.getPlatform(platform);
        if (pf.isAuthValid()) {
            pf.removeAccount(true);
        }
        //false为客户端   true为网页版
        pf.SSOSetting(false);
        //设置监听
        //Setting listener
        pf.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {
                handler.sendEmptyMessage(EMPOWER_ERROR);
                UtilLog.reportError("用户授权出错", null);
                Log.i("zhangyujian","用户授权出错::Platform::"+arg0.getName()+"::"+arg1);
            }

            @Override
            public void onComplete(Platform plat, int action, HashMap<String, Object> res) {
                String param = "";
                if (action == Platform.ACTION_USER_INFOR) {
                    String devCode = XGPushServer.getXGToken(mAct);
                    PlatformDb plfDb = plat.getDb();
                    param = "type=thirdLogin&devCode=" + devCode +
                            "&p1=" + plfDb.getToken() +
                            "&p2=" + plfDb.getUserId() +
                            "&p3=" + mPlatformName +
                            "&p4=" + plfDb.getUserName() +
                            "&p5=" + plfDb.getUserIcon().replaceAll("40$", "100$") +
                            "&p6=" + getGender(plfDb.getUserGender());
                    if (platform.equals(ShareTools.WEI_XIN)) {
                        param += "&p7=" + res.get("unionid").toString();
                    }
                    Log.i("zhangyujian","---------第三方用户信息----------" + res.toString());
                    UtilLog.print("d", "---------第三方用户信息----------" + res.toString());
                }
                //
                if (param.equals("")) {
                    handler.sendEmptyMessage(INFO_ERROR);
                    return;
                }
                Message msg = handler.obtainMessage();
                msg.what = EMPOWER_OK;
                msg.obj = param;
                handler.sendMessage(msg);
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {
                handler.sendEmptyMessage(EMPOWER_CANCLE);
            }
        });
        //获取登录用户的信息，如果没有授权，会先授权，然后获取用户信息
        //Perform showUser action,in order to get user info;
        pf.showUser(null);
    }

    private static String getGender(String gender) {
        switch (gender) {
            case "m":
                return "2";//男
            case "f":
                return "3";//女
            default:
                return "1";//默认为中性
        }
    }
}
