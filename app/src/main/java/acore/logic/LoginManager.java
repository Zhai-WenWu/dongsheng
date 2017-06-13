package acore.logic;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.android.tpush.horse.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.activity.base.WebActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.dish.db.DataOperate;
import amodule.main.Main;
import amodule.main.activity.MainChangeSend;
import amodule.user.activity.MyManagerInfo;
import amodule.user.activity.Setting;
import amodule.user.activity.login.AccoutActivity;
import amodule.user.activity.login.UserSetting;
import aplug.basic.InternetCallback;
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
    private static BaseActivity mAct;

    public static boolean mIsShowAd = true,isLoadFile = false;

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
            new GrowingIOController().setUserProperties(mAct,userInfo);
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
            new GrowingIOController().setUserProperties(mAct,userInfo);
            //统计
            XHClick.onEvent(act, "login", "自动");
            //设置用户其他
            setUserOther(act, null);
            //电商
            MallCommon.setDsToken(act);
        }
	}


    /**
     * 登陆成功
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
        if (JsAppCommon.isReloadWebView) {
            JsAppCommon.isReloadWebView = false;
            WebActivity.reloadWebView();
        }
        //电商
        MallCommon.setDsToken(mAct);

        //设置关闭层级，返回
        if (isThirdAuth) {
            Main.colse_level = 4;
            mAct.finish();
        }

    }

    public static void loginSuccess(final Activity mAct, Object returnObj) {
        loginSuccess(mAct, returnObj, false);
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
     * 退出登陆
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
                    getUserPowers();
                    getActivityInfo();
                }
            }
        });
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
        getUserPowers();
        getActivityInfo();
    }

    private static void getUserPowers() {

        ReqInternet.in().doGet(StringManager.api_getUserPowers, new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String s, Object o) {
                Log.i("FRJ","getUserPowers()" + flag + "    o:" + o);
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
                        Log.i("FRJ","getUserPowers adBlock:" + adBlock);
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

    private static void getActivityInfo() {
        ReqInternet.in().doGet(StringManager.api_getActivityInfo, new InternetCallback(mAct) {
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


    public static void userLogin(final BaseActivity mAct, String param) {
        ReqInternet.in().doPost(StringManager.api_getUserInfo, param, new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    //判断用户是否已经登陆
                    if (!LoginManager.isLogin()) {
                        //统计
                        XHClick.onEvent(mAct, "login", mPlatformName);
                        loginSuccess(mAct, returnObj,true);
//						if(userInfo.get("nickName")!=null)
//							Tools.showToast(mAct, "欢迎" + (userInfo.get("nickName").length()==0?"":userInfo.get("nickName")) + "回来！");
                    }

                    //如果是用户设置页面，第三方登陆为绑定，需要修改UI界面
                    if (mAct instanceof AccoutActivity) {
                        for (int i = 0; i < PLATFORMS.length; i++) {
                            if (mPlatformName.equals(PLATFORMS[i]))
                                ((AccoutActivity) mAct).getData();
                        }
                    }
                } else {
                    toastFaildRes(flag, true, returnObj);
                }
                mAct.loadManager.hideProgressBar();
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
//        Log.i("FRJ","isShowAd():" + mIsShowAd);
        return mIsShowAd;
    }

    private static synchronized void setIsShowAd(){
//        Log.i("FRJ","setIsShowAd():" + mIsShowAd);
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_adIsShow,"isShowAd",mIsShowAd?"2":"1");
    }

    private static boolean getIsShowAd(){
        Object data = FileManager.loadShared(XHApplication.in(),FileManager.xmlFile_adIsShow,"isShowAd");
//        Log.i("FRJ","getIsShowAd():" + data);
        if(data != null && "1".equals(String.valueOf(data)))
            return false;
        return true;
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


}