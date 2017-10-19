package acore.logic;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;

import com.tencent.smtt.sdk.CookieSyncManager;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.login.widget.MsgNotifyDialog;
import acore.override.XHApplication;
import acore.override.activity.base.WebActivity;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import amodule.answer.db.AskAnswerSQLite;
import amodule.dish.db.DataOperate;
import amodule.main.Main;
import amodule.main.activity.MainChangeSend;
import amodule.user.activity.MyManagerInfo;
import amodule.user.activity.Setting;
import amodule.user.activity.login.AccoutActivity;
import amodule.user.activity.login.UserSetting;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.web.tools.JsAppCommon;
import third.growingio.GrowingIOController;
import third.mall.aplug.MallCommon;
import third.push.umeng.UMPushServer;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;

import static xh.basic.tool.UtilString.getListMapByJson;

public class LoginManager {
    public static final int LOGIN_QQ = 1;
    public static final int LOGIN_WX = 2;
    public static final int LOGIN_WB = 3;
    public static final String[] PLATFORMS = {"QQ", "微信", "新浪"};

    public static Map<String, String> userInfo = new HashMap<>(); // 当前登录用户信息

    private static String mPlatformName = "QQ";

    private static boolean mIsShowAd = true,isLoadFile = false;

    /**
     * 自动登录
     */
    @SuppressWarnings("unchecked")
    public static void loginByAuto(final Activity act) {
        //获取用户本地信息
        userInfo = (Map<String, String>) UtilFile.loadShared(act, FileManager.xmlFile_userInfo, "");
        int length = userInfo.size();
        if(length == 0){
            //			logout(act);
            new XGPushServer(act).initPush();
            //设置GrowingIO用户数据
            new GrowingIOController().setUserProperties(act,userInfo);
        }else if(length > 0 || length <= 12){
            String params = "type=getData&devCode=" + XGPushServer.getXGToken(act);
            ReqInternet.in().doPost(StringManager.api_getUserInfo, params, new InternetCallback(act) {
                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    if (flag >= UtilInternet.REQ_OK_STRING) {
                        //统计
                        XHClick.onEvent(act, "login", "自动");
                        //保存用户数据
                        saveUserInfo(act, returnObj);
                        //设置用户其他
                        setUserOther(act, returnObj);
                    } else
                        logout(act);
                }
            });
        }else if(length == 13){
            //设置GrowingIO用户数据
            new GrowingIOController().setUserProperties(act,userInfo);
            //统计
            XHClick.onEvent(act, "login", "自动");
            //设置用户其他
            setUserOther(act, null);
            //电商
            MallCommon.setDsToken(act);
        }
	}

    /**
     * 登录成功
     */
    public static void loginSuccess(final Activity mAct, Object returnObj, boolean isThirdAuth) {
        if (mAct != null)
            XHClick.track(mAct, "登录成功");
        if (mAct != null)
            XHClick.track(mAct, "登录成功");
        //保存用户数据
        saveUserInfo(mAct, returnObj);
        XHClick.registerUserRegTimeSuperProperty(mAct);

        //设置用户其他
        setUserOther(mAct, returnObj);
        //电商
        MallCommon.onRegisterSuccessCallback = new MallCommon.OnRegisterSuccessCallback() {
            @Override
            public void onRegisterSuccess() {
                //刷新webview，确保有电商cookie
                if (JsAppCommon.isReloadWebView) {
                    JsAppCommon.isReloadWebView = false;
                    WebActivity.reloadWebView();
                }
            }
        };
        MallCommon.setDsToken(mAct);

        //设置关闭层级，返回
        if (isThirdAuth) {
            Main.colse_level = 4;
            mAct.finish();
        }

    }

    public static void loginSuccess(final Activity mAct, Object returnObj) {
        loginSuccess(mAct, returnObj, false);
        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_LOGIN, null, true);
    }

    /**
     * 登录失败
     * @param obj 失败后返回的数据
     */
    public static void loginFail(Object obj) {
        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_LOGIN, null, false);
    }

    /**
     * 第二次保存数据
     *
     * @param mAct
     * @param returnObj
     */
    public static void setDataUser(Activity mAct, Object returnObj) {
        saveUserInfo(mAct, returnObj);
        setUserOther(mAct, returnObj);
    }

    /**
     * 退出登录
     *
     * @param mAct
     */
    public static void logout(final Activity mAct) {
        String params = "type=userOut&devCode=" + XGPushServer.getXGToken(mAct);
        ReqInternet.in().doPost(StringManager.api_getUserInfo, params, new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    //xm解绑
                    new UMPushServer(mAct).addAlias(userInfo.get("code"));
                    //清除数据
                    userInfo = new HashMap<>();
                    //设置GrowingIO用户数据
                    new GrowingIOController().setUserProperties(mAct,userInfo);
                    UtilFile.delShared(mAct, FileManager.xmlFile_userInfo, "");
                    //清空消息数角标
                    AppCommon.quanMessage = 0;
                    Main.setNewMsgNum(3, AppCommon.quanMessage);
                    //XG解绑
                    new XGPushServer(mAct).initPush();
                    //如果是用户设置页面finish掉自己
                    if (mAct instanceof UserSetting || mAct instanceof MyManagerInfo || mAct instanceof Setting)
                        mAct.finish();
                    //清除电商的数据
                    MallCommon.delSaveMall(mAct);
                    getUserPowers(mAct);
                    getActivityInfo(mAct);

                    //清除无用数据
                    clearUserData(mAct);
                    //清除Cookie
                    removeAllCookie(mAct);
                    
                }
                ObserverManager.getInstence().notify(ObserverManager.NOTIFY_LOGOUT, null, flag >= UtilInternet.REQ_OK_STRING);
            }
        });
    }

    private static void removeAllCookie(Context context) {
        if (context == null)
            return;
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookies(null);
        } else {
            cookieManager.removeSessionCookie();
            cookieManager.removeAllCookie();
        }

    }

    /**
     * 清除用户无用数据，如问答草稿
     * @param activity
     */
    public static void clearUserData(final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //清除问答草稿数据
                AskAnswerSQLite sqLite = new AskAnswerSQLite(activity);
                sqLite.deleteAll();
            }
        }).start();
    }

    /**
     * 修改用户数(一次修改一个数据)
     *
     * @param context
     * @param key
     * @param value
     */
    public static void modifyUserInfo(Context context, String key, String value) {
        userInfo.put(key, value);
        UtilFile.saveShared(context, FileManager.xmlFile_userInfo, userInfo);
    }

	/**
	 * 保存用户数据
	 *
	 * @param mAct
	 * @param returnObj login成功后返回的数据
	 */
	public static void saveUserInfo(Activity mAct, Object returnObj) {
		ArrayList<Map<String, String>> returnList = getListMapByJson(returnObj);
		if (returnList.size() > 0) {
			Map<String, String> map = getListMapByJson(returnObj).get(0);
			if (map.size() >= 8) {
				userInfo.put("nickName", map.get("nickName")); // 昵称
				userInfo.put("img", map.get("img"));
				userInfo.put("code", map.get("code")); // 用户code
				userInfo.put("userCode", map.get("userCode")); // 加密userCode，校验登录
				userInfo.put("isManager", map.get("isManager"));
				userInfo.put("crowd", map.get("crowd") == null ? "" : map.get("crowd"));
				userInfo.put("followNum", map.get("followNum"));
				userInfo.put("inviteCode", map.get("inviteCode") == null ? "" : map.get("inviteCode"));
				userInfo.put("lv", map.get("lv"));
				XHClick.registerSuperProperty(mAct, "用户等级", map.get("lv"));
				userInfo.put("isGourmet", map.get("isGourmet"));
				userInfo.put("tel", map.get("tel"));
				userInfo.put("vip", map.get("vip"));
				userInfo.put("email", TextUtils.isEmpty(map.get("email")) ? "" : map.get("email"));
				userInfo.put("regTime", TextUtils.isEmpty(map.get("regTime")) ? "" : map.get("regTime"));
				UtilLog.print("d", "是否是管理员: " + map.get("isManager"));
				new UMPushServer(mAct).addAlias(map.get("code"));
				if(map.containsKey("sex"))userInfo.put("sex",map.get("sex"));
				//储存用户信息
				UtilFile.saveShared(mAct, FileManager.xmlFile_userInfo, userInfo);
			}
			//设置GrowingIO用户数据
			new GrowingIOController().setUserProperties(mAct,map);
		}
	}
    //设置用户其他
    private static void setUserOther(final Activity mAct, Object returnObj) {
        ArrayList<Map<String, String>> returnList = getListMapByJson(returnObj);
        if (returnList.size() > 0) {
            Map<String, String> map = getListMapByJson(returnObj).get(0);
            //设置离线菜单MAX数量
            if (!TextUtils.isEmpty(map.get("downDish")))
                DataOperate.setDownDishLimit(mAct, Integer.valueOf(map.get("downDish")));
            if (!TextUtils.isEmpty(map.get("nextDownDish")))
                AppCommon.nextDownDish = Integer.parseInt(map.get("nextDownDish"));
        }
        //设置用户关注数量
        if (userInfo.get("followNum") != null)
            AppCommon.follwersNum = Integer.parseInt(userInfo.get("followNum"));
        //XG注册
        if (userInfo.get("code") != null)
            new XGPushServer(mAct).initPush(userInfo.get("code"));
        getUserPowers(mAct);
        getActivityInfo(mAct);
    }

    private static void getUserPowers(Activity act) {

        ReqInternet.in().doGet(StringManager.api_getUserPowers, new InternetCallback(act) {
            @Override
            public void loaded(int flag, String s, Object o) {
                //Log.i("FRJ","getUserPowers()" + flag + "    o:" + o);
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(o);
                    if (arrayList.size() > 0) {
                        ArrayList<Map<String, String>> sendArrayList = StringManager.getListMapByJson(arrayList.get(0).get("send"));
                        if (sendArrayList.size() > 0) {
                            MainChangeSend.sendMap = sendArrayList.get(0);
                        } else {
                            MainChangeSend.sendMap = null;
                        }
                        String adBlock = arrayList.get(0).get("adBlock");
                        //Log.i("FRJ","getUserPowers adBlock:" + adBlock);
                        if(!TextUtils.isEmpty(adBlock) && "2".equals(adBlock)){
                            mIsShowAd = false;
                        }else{
                            mIsShowAd = true;
                        }
                    } else {
                        MainChangeSend.sendMap = null;
                        mIsShowAd = true;
                    }
                }else{
                    mIsShowAd = true;
                }
                setIsShowAd();
            }
        });
    }

    private static void getActivityInfo(Activity activity) {
        ReqInternet.in().doGet(StringManager.api_getActivityInfo, new InternetCallback(activity) {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(o);
                    if (arrayList.size() > 0) {
                        arrayList = StringManager.getListMapByJson(arrayList.get(0).get("dishVideo"));
                        if (arrayList.size() > 0) {
                            MainChangeSend.dishVideoMap = arrayList.get(0);
                        } else {
                            MainChangeSend.dishVideoMap = null;
                        }
                    } else {
                        MainChangeSend.dishVideoMap = null;
                    }
                }
            }
        });
    }

    /**
     * 判断是否有去掉广告的权利
     * @return true:显示广告   false：去掉广告
     */
    public static boolean isShowAd(){
        if(!isLoadFile){
            isLoadFile = true;
            mIsShowAd = getIsShowAd();
        }
        //在线参数，判断vivo市场单独处理（广告是否开启）
        String showAD= AppCommon.getConfigByLocal("vivoAD");//release 2表示显示发布，显示广告，1不显示广告
        if(showAD!=null&&!TextUtils.isEmpty(showAD)&&"1".equals(StringManager.getFirstMap(showAD).get("release"))){
            return false;
        }
        if (isTempVip())
            return false;
        return mIsShowAd;
    }

    private static synchronized void setIsShowAd(){
//        //Log.i("FRJ","setIsShowAd():" + mIsShowAd);
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_adIsShow,"isShowAd",mIsShowAd?"2":"1");
    }

    private static boolean getIsShowAd(){
        Object data = FileManager.loadShared(XHApplication.in(),FileManager.xmlFile_adIsShow,"isShowAd");
//        //Log.i("FRJ","getIsShowAd():" + data);
        if(data != null && "1".equals(String.valueOf(data)))
            return false;
        return true;
    }

    public static boolean isVIP(){
        if(userInfo != null && userInfo.containsKey("vip")){
            Map<String,String> vipMap = StringManager.getFirstMap(userInfo.get("vip"));
            return "2".equals(vipMap.get("isVip"));
        }
        return false;
    }

    /**
     * 是否是临时vip
     * @return
     */
    public static boolean isTempVip() {
        return "2".equals(FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, "isTempVip"));
    }

    /**
     * 设置是否是临时vip
     * @param tempVip
     */
    public static void setTempVip(final boolean tempVip) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"isTempVip",tempVip ? "2" : "");
            }
        }).start();
    }

    public static boolean isBindMobilePhone(){
        return userInfo != null
                && userInfo.containsKey("tel")
                && !TextUtils.isEmpty(userInfo.get("tel"));
    }

    /**
     * 是否显示发贴子任务入口
     *
     * @return
     */
    public static boolean isShowsendsubjectButton() {
        if (MainChangeSend.sendMap == null) {
            return true;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("sendQuan"));
        }
    }

    /**
     * 是否显示发菜谱任务入口
     *
     * @return
     */
    public static boolean isShowsendDishButton() {
        if (MainChangeSend.sendMap == null) {
            return true;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("sendDish"));
        }
    }

    /**
     * 是否显示发小视频入口
     *
     * @return
     */
    public static boolean isShowShortVideoButton() {
        if (MainChangeSend.sendMap == null) {
            return false;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("sendQuanVideo"));
        }
    }

    /**
     * 是否显示发菜谱视频按钮
     *
     * @return
     */
    public static boolean isShowSendVideoDishButton() {
        if (MainChangeSend.sendMap == null) {
            return false;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("sendDishVideo"));
        }
    }

    /**
     * 是否显示录制视频按钮
     *
     * @return
     */
    public static boolean isShowRecorderVideoButton() {
        if (MainChangeSend.sendMap == null) {
            return false;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("recordedDishVideo"));
        }
    }

    /**
     * 是否有权限发文章
     * @return
     */
    public static boolean isShowSendArticleButton() {
        if (MainChangeSend.sendMap == null) {
            return false;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("sendArticle"));
        }
    }

    /**
     * 是否有权限发视频
     * @return
     */
    public static boolean isShowSendVideoButton() {
        if (MainChangeSend.sendMap == null) {
            return false;
        } else {
            return "2".equals(MainChangeSend.sendMap.get("sendVideo"));
        }
    }

    /**
     * 是否可以发布小视频
     *
     * @return
     */
    public static boolean canPublishShortVideo() {
        if (LoginManager.userInfo != null && LoginManager.userInfo.containsKey("tel"))
            return LoginManager.userInfo.get("tel").length() > 3;
        else
            return false;
    }




    /**
     * 判断是否管理员
     * 3--马甲
     * 2--管理员
     * 1--普通用户
     *
     * @return false---是普通用户 ，true--管理员
     */
    public static boolean isManager() {
        if (LoginManager.userInfo != null && LoginManager.userInfo.containsKey("isManager"))
            return !LoginManager.userInfo.get("isManager").equals("1");
        else
            return false;
    }

    /**
     * 获取性别
     */
    public static String getSex() {
        if (userInfo.containsKey("sex")) {
            String name = userInfo.get("sex");
            int index = name.indexOf("^");
            if (index == name.length() - 1) {
                return "未设置";
            } else {
                return name.substring(index + 1, name.length());
            }

        } else return "中性";
    }

    /**
     * 判断是否登录
     * @return
     */
    public static boolean isLogin() {
        return userInfo != null && userInfo.size() > 0;
    }

    public static String getUserRegTime (Context context) {
        String regTime = "";
        Object obj = UtilFile.loadShared(context, FileManager.xmlFile_userInfo, "regTime");
        if (obj != null)
            regTime = obj.toString();
        return regTime;
    }

    private static boolean mAutoBindYiYuanVIP;

    /**
     * 设置自动绑定vip
     * @param auto
     */
    public static void setAutoBindYiYuanVIP(boolean auto) {
        mAutoBindYiYuanVIP = auto;
    }

    /**
     * vip是否绑定
     * @return
     */
    public static boolean isAutoBindYiYuanVIP() {
        return mAutoBindYiYuanVIP;
    }

    /**
     * 绑定vip
     * @param context
     */
    public static void bindYiYuanVIP(final Context context) {
        mAutoBindYiYuanVIP = false;
        ReqEncyptInternet.in().doEncypt(StringManager.api_yiyuan_binduser, "", new InternetCallback(context) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= UtilInternet.REQ_OK_STRING) {
                    Map<String, String> state = StringManager.getFirstMap(o);
                    if ("2".equals(state.get("state"))) {
                        setTempVip(false);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final Context currContext = XHActivityManager.getInstance().getCurrentActivity();
                                final MsgNotifyDialog dialog = new MsgNotifyDialog(currContext);
                                dialog.setMsgBtnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.cancel();
                                        XHClick.mapStat(currContext, "a_vip_movesuccess", "点击我知道了按钮", "");
                                    }
                                });
                                dialog.show(currContext.getString(R.string.yiyuan_succ_title), currContext.getString(R.string.yiyuan_succ_desc), currContext.getString(R.string.str_know));
                            }
                        }, 200);
                        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_YIYUAN_BIND, null, state);
                    } else {
                        //绑定失败
                        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_YIYUAN_BIND, null, state);
                    }
                } else {
                    //绑定失败
                    ObserverManager.getInstence().notify(ObserverManager.NOTIFY_YIYUAN_BIND, null, null);
                }
            }
        });
    }

    /**
     * 初始化临时会员绑定状态
     * @param context
     * @param callback
     */
    public static void initYiYuanBindState(Context context, final Runnable callback) {
        ReqEncyptInternet.in().doEncypt(StringManager.api_yiyuan_bindstate, "", new InternetCallback(context) {
            @Override
            public void loaded(int i, String s, Object obj) {
                Map<String, String> data = StringManager.getFirstMap(obj);
                Object vipTransfer = FileManager.loadShared(context, FileManager.xmlFile_appInfo, "vipTransfer");
                boolean isTempVip = "2".equals(data.get("isBindingVip"));
                LoginManager.setTempVip(isTempVip);
                Map<String, String> dataContentMap = StringManager.getFirstMap(data.get("data"));
                Map<String, String> vipContentMap = StringManager.getFirstMap(dataContentMap.get("vip"));
                String vipFirstTime = vipContentMap.get("first_time");
                String vipLastTime = vipContentMap.get("last_time");
                String vipMaturityTime = vipContentMap.get("maturity_time");
                try {
                    //单位都是秒
                    long firstTime = Long.parseLong(vipFirstTime);
                    long lastTime = Long.parseLong(vipLastTime);
                    long maturityTime = Long.parseLong(vipMaturityTime);
                    long dialogTime = lastTime + 20 * 24 * 60 * 60;
                    long currTime = System.currentTimeMillis() / 1000;
                    FileManager.saveShared(context, FileManager.xmlFile_appInfo, "shouldShowDialog", (isTempVip && !"2".equals(vipTransfer) && dialogTime <= maturityTime && currTime >= dialogTime && currTime <= maturityTime) ? "2" : "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (callback != null)
                    callback.run();
            }
        });
    }

}
