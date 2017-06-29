package acore.override.activity.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xiangha.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.login.AccountInfoBean;
import acore.logic.login.LoginCheck;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.main.view.CommonBottonControl;
import amodule.user.activity.login.AddNewPhone;
import amodule.user.activity.login.BindPhone;
import amodule.user.activity.login.BindPhoneNum;
import amodule.user.activity.login.ChangePhone;
import amodule.user.activity.login.CheckSrcret;
import amodule.user.activity.login.InputIdentifyCode;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.activity.login.LoginByPhoneIndentify;
import amodule.user.activity.login.LoginbyEmail;
import amodule.user.activity.login.RegisterByPhoneOne;
import amodule.user.activity.login.SetPersonalInfo;
import amodule.user.activity.login.SetSecretActivity;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.feedback.activity.Feedback;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import third.push.xg.XGPushServer;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/15 20:21.
 */

public class BaseLoginActivity extends BaseActivity {

    public static final String PHONE_TAG = "a_login520";
    public final static String TAG_ACCOCUT = "a_security520";

    public static final String SMS_SDK_VERSION = "2.0.2";
    public static final String smsAppkey = "10e22f093f255";
    public static final String smsAppsecret = "bb71787a9ec63116377a83c3ecac048a";
    public String text = "正在登录";
    protected int mGetCountryId = 100;
    protected static final int SET_USER_IMG = 5000;
    protected AccountInfoBean lastLoginAccout;
    public EventHandler eventHandler = null;
    protected BaseActivity mAct;
    protected static final String ZONE_CODE = "zone_code";
    protected static final String PHONE_NUM = "phone_num";
    protected static final String PHONE_IDENTITY = "phone_identity";
    public static final String PATH_ORIGIN = "path_ origin";
    protected static final String ORIGIN_REGISTER = "origin_register";
    protected static final String ORIGIN_FIND_PSW = "origin_find_psw";
    protected static final String ORIGIN_MODIFY_PSW = "origin_modify_psw";
    protected static final String ORIGIN_BIND_PHONE_NUM = "origin_bind_phone_num";
    protected static final String ORIGIN_BIND_FROM_WEB = "origin_bind_phone_web";

    protected static final String TYPE_MOTIFY_SMS = "verifyCode";
    protected static final String SECRET = "secret";

    protected static final String EMAIL_LOGIN_TYPE = "email_login_type";
    protected static final String PHONE_LOGIN_TYPE = "phone_login_type";
    protected static final String THIRD_LOGIN_TYPE = "third_login_type";
    protected static final String QQ_LOGIN_TYPE = "qq_login_type";
    protected static final String WEIXIN_LOGIN_TYPE = "weixin_login_type";
    protected static final String WEIBO_LOGIN_TYPE = "third_login_type";
    protected static final String MEIZU_LOGIN_TYPE = "third_login_type";
    protected static final String LOGINTYPE = "loginType";

    //更换手机方式
    protected static final String MOTIFYTYPE = "motifytype";
    protected static final String TYPE_SMS = "type_sms";
    protected static final String TYPE_PSW = "type_psw";

    public static final int LOGIN_QQ = 1;
    public static final int LOGIN_WX = 2;
    public static final int LOGIN_WB = 3;

    protected int err_count_secret;
    private ArrayList<BaseLoginActivity> activityList = new ArrayList<BaseLoginActivity>();
    protected SMSSendCallback callback;
    private String phoneNumber="";//手机号码
    private String countyrCode="";//验证码

    /**
     * Activity标题初始化
     *
     * @param title       ：标题
     * @param level       ：等级
     * @param color       :背景色
     * @param barTitleXml ：标题xml
     * @param contentXml  ：主内容xml
     */
    public void initActivity(String title, int level, int color, int barTitleXml, int contentXml) {
        this.level = level;
        mAct = this;
        className = this.getComponentName().getClassName();
        Log.i("zhangyujian", "className::" + className);
        control = new CommonBottonControl();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(control.setCommonBottonView(className, this, contentXml));
        mCommonBottomView = control.mCommonBottomView;

        activityList.add(this);

        if (Tools.isShowTitle()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setCommonStyle();

        lastLoginAccout = LoginCheck.getLastLoginAccout(this);

        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, final Object data) {
                Log.i("tzy","afterEvent callback = " + callback);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        // 提交验证码成功

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        // 获取验证码成功
                        SyntaxTools.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("tzy","RESULT_COMPLETE callback = " + callback);
                                if(callback != null)
                                    callback.onSendSuccess();
                            }
                        });
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        // 返回支持发送验证码的国家列表

                    }
                } else if (result == SMSSDK.RESULT_ERROR) {
                    SyntaxTools.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Throwable throwable = (Throwable) data;
                                throwable.printStackTrace();
                                JSONObject object = new JSONObject(throwable.getMessage());
                                String des = object.optString("detail");//错误描述
                                int status = object.optInt("status");//错误代码
                                Log.i("login","sendSMS() status:" + status);
                                Log.i("login","sendSMS() des:" + des);
                                if (462 == status || 472 == status) {
                                    Toast.makeText(mAct, "请勿频繁发送验证码", Toast.LENGTH_SHORT).show();
                                } else if (463 == status
                                        || 464 == status
                                        || 465 == status
                                        || 476 == status
                                        || 477 == status
                                        || 478 == status) {
                                    Toast.makeText(mAct, "短信验证码使用次数已超限，请明日再试", Toast.LENGTH_SHORT).show();
                                } else {
                                }

                            } catch (Exception e) {
                            }
                            Log.i("tzy","RESULT_ERROR callback = " + callback);
                            if(callback != null)
                            callback.onSendFalse();
                            LogManager.print("w", data.toString() + "");
                            String param = "log=" + data.toString();
                            if(!TextUtils.isEmpty(phoneNumber)) param += "&phoneNumber="+phoneNumber;
                            if(!TextUtils.isEmpty(countyrCode)) param += "&code="+countyrCode;
                            ReqInternet.in().doPost(StringManager.api_smsReport, param, new InternetCallback(mAct.getApplicationContext()) {
                                @Override
                                public void loaded(int flag, String url, Object returnObj) {
                                    if (flag >= ReqInternet.REQ_OK_STRING) {

                                    } else {
//                                        toastFaildRes(flag, true, returnObj);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        };
        //注册SDK
        SMSSDK.initSDK(this, smsAppkey, smsAppsecret);


        View rl_topbar = findViewById(R.id.rl_topbar);
        if (rl_topbar != null) {
            rl_topbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPressTopBar();

                }
            });
        }
    }

    protected void initTitle() {
        if (Tools.isShowTitle()) {
            LinearLayout linearView = new LinearLayout(this);
            int height = Tools.getStatusBarHeight(this);
            linearView.setBackgroundColor(getResources().getColor(R.color.backgroup_color));
            linearView.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            ((RelativeLayout) findViewById(R.id.rl_topbar)).addView(linearView, 0, params);
        }
    }

    protected void onPressTopBar() {
        onBackPressed();
    }

    /**
     * 账号号登录
     *
     * @param mAct
     * @param zoneCode 区号
     * @param accout   账号
     * @param pwd      密码
     */
    public void loginByAccout(final BaseLoginActivity mAct, String loginType, final String zoneCode,
                              String accout, String pwd, final BaseLoginCallback callback) {
        String param = new StringBuffer().append("type=pwdLogin")
                .append("&devCode=").append(XGPushServer.getXGToken(mAct))
                .append("&p1=").append(accout)
                .append("&p2=").append(pwd)
                .append("&phoneZone=").append(zoneCode)
                .toString();
        userLogin(mAct, loginType, param, zoneCode, accout, callback);
    }

    public void userLogin(final BaseLoginActivity mAct, final String loginType, String param, final String zoneCode,
                          final String phoneNum, final BaseLoginCallback callback) {
        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_getUserInfo, param, new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(returnObj);
                    if (maps != null && maps.size() > 0) {
                        err_count_secret = 0;
                        LoginManager.loginSuccess(mAct, returnObj.toString());
                        callback.onSuccess();
                        if (EMAIL_LOGIN_TYPE.equals(loginType) || PHONE_LOGIN_TYPE.equals(loginType)) {
                            if (TextUtils.isEmpty(zoneCode)) {
                                LoginCheck.saveLastLoginAccoutInfo(mAct, AccountInfoBean.ACCOUT_MAILBOX,
                                        "", "", phoneNum);
                            } else {
                                LoginCheck.saveLastLoginAccoutInfo(mAct, AccountInfoBean.ACCOUT_PHONE,
                                        zoneCode, phoneNum, "");
                            }
                        }

                    } else {
                        onSercretError(loginType, zoneCode, phoneNum);
                        callback.onFalse(flag);
                    }
                } else {
                    onSercretError(loginType, zoneCode, phoneNum);
                    callback.onFalse(flag);
                }
            }
        });
    }


    private void onSercretError(String loginType, final String zoneCode, final String phoneNum) {
        if (!(PHONE_LOGIN_TYPE.equals(loginType) || EMAIL_LOGIN_TYPE.equals(loginType)))
            return;
        err_count_secret++;
        if (err_count_secret > 2) {
            if (!TextUtils.isEmpty(zoneCode)) {
                final XhDialog xhDialog = new XhDialog(mAct);
                xhDialog.setTitle("账号或密码错误，"+"\n可以通过短信验证码完成登录")
                        .setCanselButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                XHClick.mapStat(mAct, "a_login520", "手机号登录", "失败原因：弹框验证码登录，选择取消");
                                xhDialog.cancel();
                            }
                        })
                        .setSureButton("短信登录", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                gotoLoginByIndetify(mAct, zoneCode, phoneNum);
                                XHClick.mapStat(mAct, "a_login520", "手机号登录", "失败原因：弹框验证码登录，选择去登录");
                                xhDialog.cancel();
                            }
                        })
                        .setSureButtonTextColor("#007aff")
                        .setCancelButtonTextColor("#007aff");
                xhDialog.show();
            } else {

                final XhDialog xhDialog = new XhDialog(mAct);
                xhDialog.setTitle("邮箱注册的账号，请使用电脑访问www.xiangha.com找回密码!")
                        .setSureButton("我知道了", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                XHClick.mapStat(mAct, PHONE_TAG,"邮箱登录", "失败原因：弹框电脑找回密码");
                                xhDialog.cancel();
                            }
                        })
                        .setSureButtonTextColor("#007aff")
                        .setCancelButtonTextColor("#007aff");
                xhDialog.show();
            }

        }
    }


    /**
     * 手机验证码登陆
     *
     * @param mAct
     * @param phoneNum     手机号
     * @param identifyCode 验证码
     */
    protected void logInByIdentify(final Activity mAct, final String countryCode, final String phoneNum,
                                   String identifyCode, final BaseLoginCallback callback) {
        String param = "phoneNum=" + phoneNum + "&zone=" + countryCode + "&verCode=" + identifyCode
                + "&sdkVes=" + SMS_SDK_VERSION;
        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_phoneLogin, param, new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    LoginManager.loginSuccess(mAct, returnObj);
                    callback.onSuccess();
                    LoginCheck.saveLastLoginAccoutInfo(mAct, AccountInfoBean.ACCOUT_PHONE,
                            countryCode, phoneNum, "");

                } else {
                    callback.onFalse(flag);
                }
            }
        });
    }

    protected void checkEmailRegisted(final Context context, final String email,
                                      final BaseLoginCallback callback) {
        String param = "email=" + email;
        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_checkEmailRegisterState, param, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(msg);
                    if (maps.size() > 0) {
                        Map<String, String> map = maps.get(0);
                        String result = map.get("result");
                        if ("2".equals(result)) {
                            callback.onSuccess();
                        } else {
                            callback.onFalse(flag);
                            Log.e("checkRegisted", map.get("reason"));
                        }
                    }
                } else {
                    toastFaildRes(flag, true, msg);
                }
            }
        });
    }

    protected void checkPhoneRegisted(final Context context, final String zoneCode,
                                      final String phoneNum, final BaseLoginCallback callback) {
        //判断网络
        if(!ToolsDevice.getNetActiveState(context)){
            Tools.showToast(context,"网络错误，请检查网络或重试");
            return;
        }
        //发起请求
        String param = "phone=" + phoneNum + "&&phoneZone=" + zoneCode;
        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_checkPhoneRegisterState, param, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    boolean registed = false;
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(msg);
                    if (maps.size() > 0) {
                        Map<String, String> map = maps.get(0);
                        String result = map.get("result");
                        if ("2".equals(result)) {
                            registed = true;
                        } else {
                            Log.e("checkRegisted", map.get("reason"));
                        }
                        if (registed) {
                            callback.onSuccess();
                        } else {
                            //没有注册
                            callback.onFalse(flag);
                        }
                    }
                } else {
                    //请求失败
                    callback.onFalse(flag);
                    toastFaildRes(flag, true, msg);
                }
            }
        });
    }

    /**
     * 发送请求获取验证码
     *
     * @param countyrCode
     * @param phone_number
     */
    protected boolean reqIdentifyCode(String countyrCode, String phone_number,SMSSendCallback callback) {
        Log.i("tzy","reqIdentifyCode");
        this.callback = callback;
        Log.i("tzy","callback = " + callback.toString());
        this.countyrCode=countyrCode;
        this.phoneNumber=phone_number;
        SMSSDK.registerEventHandler(eventHandler);
        SMSSDK.getVerificationCode(countyrCode, phone_number);
        return true;
    }

    /**
     * 设置，修改密码
     *
     * @param context
     * @param zoneCode 区号
     * @param pwd      密码
     */
    public void modifySecret(final Context context, String zoneCode, String phoneNum,
                             String pwd, final BaseLoginCallback callback) {
        String param = "password=" + pwd + "&zone=" + zoneCode + "&phone=" + phoneNum;
        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_setSecret, param, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    callback.onSuccess();
                    LoginManager.loginSuccess((Activity) context, returnObj);
                } else {
                    callback.onFalse(flag);
                    Log.e("modifySecret", returnObj.toString());
                }
                loadManager.hideProgressBar();
            }
        });
    }


    protected void checkIdentifyCode(Context context, String zoneCode, String phoneNum,
                                     String identify_code, final BaseLoginCallback callback) {

        String url = StringManager.api_compareVerCode;
        String param = "phoneNum=" + phoneNum + "&zone=" + zoneCode + "&verCode="
                + identify_code + "&sdkVes=" + SMS_SDK_VERSION;
        loadManager.showProgressBar();
        ReqInternet.in().doPost(url, param, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    callback.onSuccess();
                } else {
                    callback.onFalse(flag);
                }
            }
        });
    }

    protected void motifyPhone(Context context, String type, String newZoneCode, String newPhoneNum,
                               String verifyCode, String currentZoneCode,
                               String currentPhone, final BaseLoginCallback callback) {

        String param = "type=" + type + "&changePhone=" + newPhoneNum + "&changePhoneZone="
                + newZoneCode + "&sdkVes=" + SMS_SDK_VERSION + "&currentPhoneZone=" + currentZoneCode
                + "&currentPhone=" + currentPhone + "&verifyCode=" + verifyCode;

        ReqInternet.in().doPost(StringManager.api_modifyPhone, param, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    boolean success = false;
                    ArrayList<Map<String, String>> lists = StringManager.getListMapByJson(returnObj);
                    Map<String, String> map = lists.get(0);
                    if (map != null && map.size() > 0) {
                        if ("2".equals(map.get("result"))) {
                            success = true;
                        }
                    }
                    if (success) {
                        callback.onSuccess();
                    } else {
                        callback.onFalse(flag);
                    }
                } else {
                    callback.onFalse(flag);
                }
            }
        });
    }

    protected void bindPhone(Context context, String zoneCode, final String phoneNum,
                             String vertiCode, final BaseLoginCallback callback) {
        loadManager.showProgressBar();
        String param = "phoneNum=" + phoneNum + "&zone=" + zoneCode + "&verCode=" + vertiCode +
                "&sdkVes=" + BaseLoginActivity.SMS_SDK_VERSION;
        ReqInternet.in().doPost(StringManager.api_phoneBind, param, new InternetCallback(context) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    LoginManager.userInfo.put("tel", phoneNum);
                    callback.onSuccess();
                } else {
                    callback.onFalse(flag);
                }
            }
        });
    }

    protected void register(Context context, String countyrCode, String phone_number) {
        Intent intent = new Intent(context, RegisterByPhoneOne.class);
        intent.putExtra(ZONE_CODE, countyrCode);
        intent.putExtra(PHONE_NUM, phone_number);
        startActivity(intent);
    }

    protected void gotoLogin(String zoneCode, String phoneNum) {
        Intent intent = new Intent(mAct, LoginByAccout.class);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        startActivity(intent);
        finish();
    }

    protected void gotoFeedBack() {
        Intent intent = new Intent(this, Feedback.class);
        startActivity(intent);
    }

    protected void gotoSetSecrt(String zoneCode, String phoneNum, String origin, String identifyCode) {
        Intent intent = new Intent(this, SetSecretActivity.class);
        intent.putExtra(PATH_ORIGIN, origin);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        intent.putExtra(PHONE_IDENTITY, identifyCode);
        startActivity(intent);
    }

    protected void gotoInputIdentify(Context context, String zoneCode, String phoneNum, String origin) {
        Intent intent = new Intent(context, InputIdentifyCode.class);
        intent.putExtra(PATH_ORIGIN, origin);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        startActivity(intent);
    }

    protected void gotoChangePhone(Context context, String zoneCode, String phoneNum) {
        Intent intent = new Intent(context, ChangePhone.class);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        startActivity(intent);
    }

    protected void gotoCheckSecret(Context context) {
        Intent intent = new Intent(context, CheckSrcret.class);
        startActivity(intent);
    }

    protected void gotoAddNewPhone(Context context, String zoneCode, String phoneNum,String motifyType) {
        Intent intent = new Intent(context, AddNewPhone.class);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        intent.putExtra(MOTIFYTYPE, motifyType);
        startActivity(intent);
    }

    protected void gotoLoginByIndetify(Context context, String zoneCode, String phoneNum) {
        Intent intent = new Intent(context, LoginByPhoneIndentify.class);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        startActivity(intent);
    }

    protected void gotoSetPersonInfo(Context context,String loginType) {
        Intent intent = new Intent(context, SetPersonalInfo.class);
        intent.putExtra(LOGINTYPE,loginType);
        startActivity(intent);
    }

    protected void gotoBindPhone(Context context, String loginType) {
        Intent intent = new Intent(context, BindPhone.class);
        intent.putExtra(LOGINTYPE, loginType);
        startActivity(intent);
    }

    protected void gotoBindPhoneNum(Context context) {
        startActivity(new Intent(context, BindPhoneNum.class));
    }

    public void gotoLoginByEmail(Context context) {
        startActivity(new Intent(context, LoginbyEmail.class));
    }

    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(eventHandler != null)
            SMSSDK.unregisterEventHandler(eventHandler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mGetCountryId && data != null) {
            String country_code = data.getStringExtra("countryId");
            onCountrySelected(country_code);
        }
    }

    protected void onCountrySelected(String country_code) {
    }

    public interface BaseLoginCallback {

        void onSuccess();

        void onFalse(int flag);
    }

    protected String hidePhoneNum(String phoneNum) {

        if (phoneNum != null && phoneNum.length() == 11) {
            phoneNum = phoneNum.replaceAll("^(\\d{3})(\\d+)(\\d{2})$", "$1******$3");
        }
        return phoneNum;
    }

    protected void backToForward() {
        Main.colse_level = 4;
        finish();
    }


    public interface SMSSendCallback {

        void onSendSuccess();

        void onSendFalse();
    }
}
