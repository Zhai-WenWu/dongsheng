package acore.override.activity.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xianghatest.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.login.AccountInfoBean;
import acore.logic.login.LoginCheck;
import acore.override.helper.XHActivityManager;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.main.view.CommonBottonControl;
import amodule.user.activity.login.AddNewPhone;
import amodule.user.activity.login.BindPhone;
import amodule.user.activity.login.ChangePhone;
import amodule.user.activity.login.CheckSrcret;
import amodule.user.activity.login.InputIdentifyCode;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.activity.login.LoginbyEmail;
import amodule.user.activity.login.LostSecret;
import amodule.user.activity.login.SetPersonalInfo;
import amodule.user.activity.login.SetSecretActivity;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
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

    public static final String SMS_SDK_VERSION = "3.0.0";
    public String text = "正在登录";
    protected int mGetCountryId = 100;
    protected static final int SET_USER_IMG = 5000;
    protected AccountInfoBean lastLoginAccout;
    protected BaseActivity mAct;
    protected static final String ZONE_CODE = "zone_code";
    protected static final String PHONE_NUM = "phone_num";
    protected static final String PHONE_IDENTITY = "phone_identity";
    public static final String PATH_ORIGIN = "path_ origin";
    protected static final String ORIGIN_REGISTER = "origin_register";
    protected static final String ORIGIN_FIND_PSW = "origin_find_psw";
    protected static final String ORIGIN_MODIFY_PSW = "origin_modify_psw";
    public static final String ORIGIN_BIND_PHONE_NUM = "origin_bind_phone_num";
    protected static final String ORIGIN_BIND_FROM_WEB = "origin_bind_phone_web";

    protected static final String EMAIL_LOGIN_TYPE = "email_login_type";
    protected static final String PHONE_LOGIN_TYPE = "phone_login_type";
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
    private EventHandler eventHandler = null;

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

        setCommonStyle();

        lastLoginAccout = LoginCheck.getLastLoginAccout(this);

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
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            LinearLayout linearView = new LinearLayout(this);
            int height = Tools.getStatusBarHeight(this);
            linearView.setBackgroundColor(getResources().getColor(R.color.backgroup_color));
            linearView.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
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
                        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_LOGIN, null, true);
                        callback.onSuccess();
                        if (EMAIL_LOGIN_TYPE.equals(loginType) || PHONE_LOGIN_TYPE.equals(loginType)) {
                            if (TextUtils.isEmpty(zoneCode)) {
                                LoginCheck.saveLastLoginAccoutInfo(mAct, AccountInfoBean.ACCOUT_MAILBOX, "", "", phoneNum);
                            } else {
                                LoginCheck.saveLastLoginAccoutInfo(mAct, AccountInfoBean.ACCOUT_PHONE, zoneCode, phoneNum, "");
                            }
                        }
                    } else {
                        onSercretError(loginType, zoneCode, phoneNum);
                        callback.onFalse(flag);
                        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_LOGIN, null, false);
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
                xhDialog.setTitle("账号或密码错误，\n可以通过短信验证码完成登录")
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
                                XHClick.mapStat(mAct, PHONE_TAG, "邮箱登录", "失败原因：弹框电脑找回密码");
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
     * 手机验证码登录
     *
     * @param mAct
     * @param phoneNum     手机号
     * @param identifyCode 验证码
     */
    protected void logInByIdentify(final Activity mAct, final String countryCode, final String phoneNum,
                                   String identifyCode, final BaseLoginCallback callback) {
        String param = new StringBuffer().append("phoneNum=").append(phoneNum)
                .append("&zone=").append(countryCode)
                .append("&verCode=").append(identifyCode)
                .append("&sdkVes=").append(SMS_SDK_VERSION).toString();
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
                    Map<String, String> map = StringManager.getFirstMap(msg);
                    String result = map.get("result");
                    if ("2".equals(result)) {
                        callback.onSuccess();
                    } else {
                        callback.onFalse(flag);
                        Log.e("checkRegisted", map.get("reason"));
                    }
                }
            }
        });
    }

    protected void checkPhoneRegisted(final Context context, final String zoneCode,
                                      final String phoneNum, final BaseLoginCallback callback) {
        //判断网络
        if (!ToolsDevice.getNetActiveState(context)) {
            Tools.showToast(context, "网络错误，请检查网络或重试");
            return;
        }
        //发起请求
        String param = "phone=" + phoneNum + "&phoneZone=" + zoneCode;
        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_checkPhoneRegisterState, param, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    boolean registed = false;
                    Map<String, String> map = StringManager.getFirstMap(msg);
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
                } else {
                    //请求失败
                    callback.onFalse(flag);
                }
            }
        });
    }

    protected void reqIdentifySpeecha(final String phoneNum, final BaseLoginCallback callback) {
        if (TextUtils.isEmpty(phoneNum)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            callback.onFalse(-1);
            return;
        }
        String errorType = LoginCheck.checkPhoneFormatWell(this, "86", phoneNum);
        if (LoginCheck.WELL_TYPE.equals(errorType)) {
            ReqEncyptInternet.in().doEncypt(StringManager.api_sendVoiceVerify, "phone=" + phoneNum,
                    new InternetCallback(this) {
                        @Override
                        public void loaded(int flag, String s, Object data) {
                            if (flag >= ReqInternet.REQ_OK_STRING) {
                                Map<String, String> map = StringManager.getFirstMap(data);
                                if (TextUtils.isEmpty(map.get("errorCode"))) {
                                    callback.onSuccess();
                                    return;
                                }
                            } else if (String.valueOf(data).contains("网络错误")) {
                                Tools.showToast(BaseLoginActivity.this.getApplicationContext(), String.valueOf(data));
                            }
                            sendFalseRequest(data.toString(), "86", phoneNum);
                            callback.onFalse(flag);
                        }
                    });
        } else {
            callback.onFalse(-1);
        }
    }

    /**
     * 发送请求获取验证码
     *
     * @param countyrCode
     * @param phone_number
     * @param callback
     */
    protected boolean reqIdentifyCode(final String countyrCode, final String phone_number, @NonNull final SMSSendCallback callback) {
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(final int event, final int result, final Object data) {
                SyntaxTools.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // 回调完成
                            if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                                // 提交验证码成功

                            } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                                // 获取验证码成功
                                if (callback != null)
                                    callback.onSendSuccess();
                            } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                                // 返回支持发送验证码的国家列表

                            }
                        } else if (result == SMSSDK.RESULT_ERROR) {
                            handlerError(data);
                            if (callback != null)
                                callback.onSendFalse();
                            LogManager.print("w", data.toString() + "");
                            sendFalseRequest(data.toString(), countyrCode, phone_number);
                        }
                    }
                });
            }

            /**
             *
             * @param data
             */
            private void handlerError(Object data) {
                try {
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");//错误描述
                    int status = object.optInt("status");//错误代码
                    Log.i("login", "sendSMS() status:" + status + " ; \n des:" + des);
                    Log.i("login", "sendSMS() des:" + des);
                    //统计错误情况
                    statisticsErrorCode(status);
                    if (462 == status || 472 == status) {
                        Toast.makeText(mAct, "请勿频繁发送验证码", Toast.LENGTH_SHORT).show();
                    } else if (463 == status || 464 == status || 465 == status
                            || 476 == status || 477 == status || 478 == status) {
                        Toast.makeText(mAct, "短信验证码使用次数已超限，请明日再试", Toast.LENGTH_SHORT).show();
                    } else {
                        Tools.showToast(mAct,des);
                    }
                } catch (Exception e) {
                }
            }
        };
        //注册事件回调
        SMSSDK.registerEventHandler(eventHandler);
        //请求验证码
        SMSSDK.getVerificationCode(countyrCode, phone_number);
        return true;
    }

    /**
     * 发送失败统计,自己的统计
     *
     * @param data
     * @param countyrCode
     */
    private void sendFalseRequest(String data, String countyrCode, String phoneNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("log=").append(data);
        if (!TextUtils.isEmpty(phoneNumber))
            stringBuilder.append("&phoneNumber=").append(phoneNumber);
        if (!TextUtils.isEmpty(countyrCode))
            stringBuilder.append("&code=").append(countyrCode);
        ReqInternet.in().doPost(StringManager.api_smsReport, stringBuilder.toString(),
                new InternetCallback(mAct.getApplicationContext()) {
                    @Override
                    public void loaded(int flag, String url, Object returnObj) {
                    }
                });
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
                    Map<String, String> map = StringManager.getFirstMap(returnObj);
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
        Intent intent = new Intent(context, LoginByAccout.class);
        intent.putExtra(ZONE_CODE, countyrCode);
        intent.putExtra(PHONE_NUM, phone_number);
        startActivity(intent);
    }

    protected void gotoFeedBack() {
        startActivity(new Intent(this, Feedback.class));
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
        startActivity(new Intent(context, CheckSrcret.class));
    }

    protected void gotoAddNewPhone(Context context, String zoneCode, String phoneNum, String motifyType) {
        Intent intent = new Intent(context, AddNewPhone.class);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        intent.putExtra(MOTIFYTYPE, motifyType);
        startActivity(intent);
    }

    protected void gotoLoginByIndetify(Context context, String zoneCode, String phoneNum) {
        Intent intent = new Intent(context, LoginByAccout.class);
        intent.putExtra(ZONE_CODE, zoneCode);
        intent.putExtra(PHONE_NUM, phoneNum);
        startActivity(intent);
    }

    protected void gotoSetPersonInfo(Context context, String loginType) {
        Intent intent = new Intent(context, SetPersonalInfo.class);
        intent.putExtra(LOGINTYPE, loginType);
        startActivity(intent);
    }

    protected void gotoBindPhone(Context context, String loginType) {
        Intent intent = new Intent(context, BindPhone.class);
        intent.putExtra(LOGINTYPE, loginType);
        startActivity(intent);
    }

    public static void gotoBindPhoneNum(Context context) {
        Intent bindPhoneIntent = new Intent(context, LostSecret.class);
        bindPhoneIntent.putExtra(PATH_ORIGIN, ORIGIN_BIND_PHONE_NUM);
        context.startActivity(bindPhoneIntent);
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
        if (eventHandler != null)
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

    private final String statisticsErrorCode_ID = "sms_error_id";
    private String ServerError = "服务器错误码";
    private String LocalError = "本地错误码";

    private void statisticsErrorCode(int status) {
        switch (status) {
            case 400:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_客户端请求不能被识别");
                break;
            case 405:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_请求的AppKey为空");
                break;
            case 406:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_请求的AppKey不存在");
                break;
            case 407:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_请求提交的数据缺少必要的数据");
                break;
            case 408:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_无效的请求参数");
                break;
            case 418:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_内部接口调用失败");
                break;
            case 420:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_本地请求duid文件不存在");
                break;
            case 450:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_无权执行该操作");
                break;
            case 454:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_数据格式错误");
                break;
            case 455:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_签名无效");
                break;
            case 456:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_提交的手机号码或者区号为空");
                break;
            case 457:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_提交的手机号格式不正确");
                break;
            case 458:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_手机号码在发送黑名单中");
                break;
            case 459:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_获取appKey控制发送短信的数据失败");
                break;
            case 460:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_无权限发送短信");
                break;
            case 461:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_不支持该地区发送短信");
                break;
            case 462:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_每分钟发送次数超限");
                break;
            case 463:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_手机号码每天发送次数超限");
                break;
            case 464:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_每台手机每天发送次数超限");
                break;
            case 465:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_号码在App中每天发送短信的次数超限");
                break;
            case 466:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_校验的验证码为空");
                break;
            case 467:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_5分钟内校验错误超过3次，验证码失效");
                break;
            case 468:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_用户提交校验的验证码错误");
                break;
            case 469:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_没有打开通过网页端发送短信的开关");
                break;
            case 470:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_账户的短信余额不足");
                break;
            case 471:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_通过服务端发送或验证短信的IP错误");
                break;
            case 472:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_客户端请求发送短信验证过于频繁");
                break;
            case 473:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_服务端根据duid获取平台错误");
                break;
            case 474:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_没有打开服务端验证开关");
                break;
            case 475:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_appKey的应用信息不存在");
                break;
            case 476:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_当前appkey发送短信的数量超过限额");
                break;
            case 477:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_当前手机号发送短信的数量超过限额");
                break;
            case 478:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_当前手机号在当前应用内发送超过限额");
                break;
            case 500:
                XHClick.mapStat(this, statisticsErrorCode_ID, ServerError, status + "_服务器内部错误");
                break;

            case 600:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_请求太频繁，API使用受限制");
                break;
            case 601:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_短信发送受限");
                break;
            case 602:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_无法发送此地区短信");
                break;
            case 603:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_请填写正确的手机号码");
                break;
            case 604:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_当前服务暂不支持此国家");
                break;
            case 606:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_无权访问该接口");
                break;
            case 607:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_Request header错误：Contet-Length错误");
                break;
            case 608:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_检查meta是否配置了appkey");
                break;
            case 609:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_Request header错误：Sign为空");
                break;
            case 610:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_Request header错误：UserAgent为空");
                break;
            case 611:
                XHClick.mapStat(this, statisticsErrorCode_ID, LocalError, status + "_AppSecret为空");
                break;
        }
    }
}
