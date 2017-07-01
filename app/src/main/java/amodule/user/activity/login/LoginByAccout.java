package amodule.user.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.login.CountComputer;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SecretInputView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import third.login.MzGrant_New;
import third.push.xg.XGPushServer;
import third.share.ShareTools;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/16 10:14.
 */

public class LoginByAccout extends BaseLoginActivity implements View.OnClickListener {

    private PhoneNumInputView phone_info;
    private SecretInputView ll_secret;
    private NextStepView btn_next_step;
    private LinearLayout contentLayout;
    private LinearLayout otherLoginLayout;
    private TextView tvRegister;
    private TextView tvIdentify;
    private TextView tvLostsercet;
    private ImageView imageQq;
    private ImageView imageWeixin;
    private ImageView imageWeibo;
    private ImageView imageMailbox;
    private ImageView imageMeizu;
    private ImageView top_left_view;

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
//                    Tools.showToast(LoginByAccout.this, "授权完成");
                    String param = msg.obj.toString();

                    if (ShareTools.WEI_XIN.equals(platform)) {
                        loginType = WEIXIN_LOGIN_TYPE;
                    } else if (ShareTools.QQ_NAME.equals(platform)) {
                        loginType = QQ_LOGIN_TYPE;
                    } else if (ShareTools.SINA_NAME.equals(platform)) {
                        loginType = WEIBO_LOGIN_TYPE;
                    }
                    userLogin(LoginByAccout.this, loginType, param, "", "",
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {

                                    userInfo = (Map<String, String>) UtilFile.loadShared(LoginByAccout.this,
                                            FileManager.xmlFile_userInfo, "");
                                    String tel = userInfo.get("tel");
                                    if (TextUtils.isEmpty(tel)) {
                                        if (CountComputer.getTipCount(LoginByAccout.this) < 2) {
                                            gotoBindPhone(LoginByAccout.this,loginType);
                                            CountComputer.saveTipCount(LoginByAccout.this);
                                        } else {
                                            backToForward();
                                        }
                                    } else {
                                        backToForward();
                                    }

                                    if (ShareTools.QQ_NAME.equals(platform)) {
                                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "QQ登录", "登录成功");
                                    } else if (ShareTools.WEI_XIN.equals(platform)) {
                                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "微信登录", "登录成功");
                                    } else if (ShareTools.SINA_NAME.equals(platform)) {
                                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "微博登录", "登录成功");
                                    }

                                }

                                @Override
                                public void onFalse(int flag) {
                                    if (ShareTools.QQ_NAME.equals(platform)) {
                                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "QQ登录", "登录失败");
                                    } else if (ShareTools.WEI_XIN.equals(platform)) {
                                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "微信登录", "登录失败");
                                    } else if (ShareTools.SINA_NAME.equals(platform)) {
                                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "微博登录", "登录失败");
                                    }
                                }
                            });

                    break;
                case EMPOWER_ERROR:
                    Tools.showToast(LoginByAccout.this, "登录失败");
                    loadManager.hideProgressBar();
                    break;
                case EMPOWER_CANCLE:
                    Tools.showToast(LoginByAccout.this, "登录失败");
                    loadManager.hideProgressBar();
                    break;
                case INFO_ERROR:
                    Tools.showToast(LoginByAccout.this, mPlatformName + "平台获取信息失败");
                    loadManager.hideProgressBar();
                    break;
            }
            return false;
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_by_accout);
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
        XHClick.track(this, "浏览登录页");
    }


    private void initView() {
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        otherLoginLayout = (LinearLayout) findViewById(R.id.ll_other_login);
        top_left_view = (ImageView) findViewById(R.id.top_left_view);
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        ll_secret = (SecretInputView) findViewById(R.id.ll_secret);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tvRegister = (TextView) findViewById(R.id.tv_register);
        tvIdentify = (TextView) findViewById(R.id.tv_identify);
        tvLostsercet = (TextView) findViewById(R.id.tv_lostsercet);
        imageQq = (ImageView) findViewById(R.id.iv_qq);
        imageWeixin = (ImageView) findViewById(R.id.iv_weixin);
        imageWeibo = (ImageView) findViewById(R.id.iv_weibo);
        imageMailbox = (ImageView) findViewById(R.id.iv_mailbox);
        imageMeizu = (ImageView) findViewById(R.id.iv_meizu);
        imageWeixin = (ImageView) findViewById(R.id.iv_weixin);
        imageWeixin = (ImageView) findViewById(R.id.iv_weixin);

        contentLayout.post(new Runnable() {
            @Override
            public void run() {
                if(contentLayout.getHeight() < ToolsDevice.getWindowPx(LoginByAccout.this).heightPixels){
                    contentLayout.removeView(otherLoginLayout);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    rl.addView(otherLoginLayout,layoutParams);
                }
            }
        });

        top_left_view.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvIdentify.setOnClickListener(this);
        tvLostsercet.setOnClickListener(this);
        imageMailbox.setOnClickListener(this);
        imageWeixin.setOnClickListener(this);
        imageQq.setOnClickListener(this);
        imageWeibo.setOnClickListener(this);
        imageMeizu.setOnClickListener(this);

        findViewById(R.id.v_meizu_space).setVisibility(View.GONE);
        findViewById(R.id.iv_meizu).setVisibility(View.GONE);

        Intent intent = getIntent();
        String zone_code = intent.getStringExtra(ZONE_CODE);
        String phone_num = intent.getStringExtra(PHONE_NUM);

        if (TextUtils.isEmpty(zone_code) || TextUtils.isEmpty(phone_num)) {
            zone_code = lastLoginAccout.getAreaCode();
            phone_num = lastLoginAccout.getPhoneNum();
        }


        phone_info.init("手机号", zone_code, phone_num,
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "点击国家代码");
                        Intent intent = new Intent(LoginByAccout.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);
                    }

                    @Override
                    public void onPhoneInfoChanged() {
                        onInputDataChanged();
                    }
                });

        ll_secret.init("密码", new SecretInputView.SecretInputViewCallback() {
            @Override
            public void onInputSecretChanged() {
                onInputDataChanged();
            }

            //密码是否显示
            @Override
            public void OnClicksecret() {
                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "点击密码眼睛");
            }
        });


        btn_next_step.init("登录", null, null, new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "点击登录");
                gotoLogin();
            }

            @Override
            public void onClickLeftView() {

            }

            @Override
            public void onClickRightView() {

            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_register:
                register(this, "", "");
                break;
            case R.id.tv_identify:
                Intent intent = new Intent();
                intent.setClass(this, LoginByPhoneIndentify.class);
                startActivity(intent);
                break;
            case R.id.tv_lostsercet:
                Intent intent1 = new Intent();
                intent1.setClass(this, LostSecret.class);
                startActivity(intent1);
                break;
            case R.id.iv_weixin:
                int number = ToolsDevice.isAppInPhone(this, "com.tencent.mm");
                if (number == 0)
                    Tools.showToast(this, "需安装微信客户端才可以登录");
                else {
                    thirdAuth(this, ShareTools.WEI_XIN, "微信");
                }
                break;
            case R.id.iv_weibo:
                thirdAuth(this, ShareTools.SINA_NAME, "新浪");
                break;
            case R.id.iv_qq:
                thirdAuth(this, ShareTools.QQ_NAME, "QQ");
                break;
            case R.id.iv_meizu:
                MzGrant_New.loginFlyme(this);
                break;
            case R.id.top_left_view:
                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "点击返回", "");
                backToForward();
                break;
            case R.id.iv_mailbox:
                gotoLoginByEmail(this);
                break;
            default:
                break;

        }

    }


    private void onInputDataChanged() {

        boolean canClickNextBtn = false;

        if (TextUtils.isEmpty(ll_secret.getPassword())) {
            canClickNextBtn = false;
        } else {
            canClickNextBtn = !phone_info.isDataAbsence();
        }
        btn_next_step.setClickCenterable(canClickNextBtn);
    }


    private void gotoLogin() {
        String errorType = LoginCheck.checkPhoneFormatWell(this, phone_info.getZoneCode(), phone_info.getPhoneNum());
        if (LoginCheck.WELL_TYPE.equals(errorType)) {
            checkPhoneRegisted(LoginByAccout.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                    new BaseLoginCallback() {
                        @Override
                        public void onSuccess() {

                            loginByAccout(LoginByAccout.this, PHONE_LOGIN_TYPE, phone_info.getZoneCode(),
                                    phone_info.getPhoneNum(),
                                    ll_secret.getPassword(), new BaseLoginCallback() {
                                        @Override
                                        public void onSuccess() {
                                            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "登录成功");
                                            backToForward();
                                        }

                                        @Override
                                        public void onFalse(int flag) {
                                            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "登录失败");
                                            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录",
                                                    "失败原因：账号或密码错");

                                        }
                                    });
                        }

                        @Override
                        public void onFalse(int flag) {
//
                            final XhDialog xhDialog = new XhDialog(LoginByAccout.this);
                            xhDialog.setTitle("网络有问题或手机号未注册")
                                    .setCanselButton("取消", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录",
                                                    "失败原因：弹框未注册，选择不注册");
                                            xhDialog.cancel();
                                        }
                                    })
                                    .setSureButton("立即注册", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            register(LoginByAccout.this, phone_info.getZoneCode(),
                                                    phone_info.getPhoneNum());
                                            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录",
                                                    "失败原因：弹框未注册，选择注册");
                                            xhDialog.cancel();
                                        }
                                    }).setSureButtonTextColor("#007aff")
                                    .setCancelButtonTextColor("#007aff");
                            xhDialog.show();
                        }
                    });
        } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "失败原因：手机号不是11位");
        } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
            XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机号登录", "失败原因：手机号格式错误");
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
        ShareSDK.initSDK(mAct);
        Platform pf = ShareSDK.getPlatform(mAct, platform);
        if (pf.isValid()) {
            pf.removeAccount();
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
                            "&p5=" + plfDb.getUserIcon().replaceAll("40$","100$") +
                            "&p6=" + getGender(plfDb.getUserGender());
                    if (platform.equals(ShareTools.WEI_XIN)) {
                        param += "&p7=" + res.get("unionid").toString();
                    }
                    UtilLog.print("d", "---------第三方用户信息----------" + res.toString());
                }
                //
                if (param.equals("")) {
                    handler.sendEmptyMessage(INFO_ERROR);
                    return;
                }
                Message msg = new Message();
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
        if ("m".equals(gender))//男
            return "2";
        else if ("f".equals(gender))//女
            return "3";
        else //默认为中性
            return "1";
    }

    @Override
    protected void onCountrySelected(String country_code) {
        super.onCountrySelected(country_code);
        phone_info.setZoneCode("+" + country_code);
    }

}
