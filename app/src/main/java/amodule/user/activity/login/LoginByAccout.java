package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xianghatest.R;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SpeechaIdentifyInputView;
import third.share.ShareTools;
import xh.basic.internet.UtilInternet;
import xh.windowview.XhDialog;


/**
 * 手机注册登录页面
 * Created by ：fei_teng on 2017/2/16 18:41.
 * Modify by: FangRuijiao on 2017/8/14
 */
public class LoginByAccout extends ThirdLoginBaseActivity implements View.OnClickListener {

    private IdentifyInputView login_identify;
    private NextStepView btn_next_step;
    private PhoneNumInputView phone_info;
    private SpeechaIdentifyInputView speechaIdentifyInputView;
    private TextView tv_password, tv_lostsercet, tv_help, tv_agreenment;
    private ImageView imageQq, imageWeixin, imageWeibo, imageMailbox;
    private String zoneCode;
    private String phoneNum;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActivity("", 4, 0, 0, R.layout.a_login_by_identify);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
        XHClick.track(this, "浏览登录页");
    }

    private void initData() {
        Intent intent = getIntent();
        zoneCode = intent.getStringExtra(ZONE_CODE);
        phoneNum = intent.getStringExtra(PHONE_NUM);
    }


    private void initView() {
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        speechaIdentifyInputView = (SpeechaIdentifyInputView) findViewById(R.id.login_speeach_identify);
        login_identify = (IdentifyInputView) findViewById(R.id.login_identify);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);

        tv_password = (TextView) findViewById(R.id.tv_password);
        tv_lostsercet = (TextView) findViewById(R.id.tv_lostsercet);
        tv_help = (TextView) findViewById(R.id.tv_help);
        tv_agreenment = (TextView) findViewById(R.id.tv_agreenment);
        imageQq = (ImageView) findViewById(R.id.iv_qq);
        imageWeixin = (ImageView) findViewById(R.id.iv_weixin);
        imageWeibo = (ImageView) findViewById(R.id.iv_weibo);
        imageMailbox = (ImageView) findViewById(R.id.iv_mailbox);

        tv_password.setOnClickListener(this);
        tv_lostsercet.setOnClickListener(this);
        tv_help.setOnClickListener(this);
        tv_agreenment.setOnClickListener(this);
        imageMailbox.setOnClickListener(this);
        imageWeixin.setOnClickListener(this);
        imageQq.setOnClickListener(this);
        imageWeibo.setOnClickListener(this);

        speechaIdentifyInputView.setOnSpeechaClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.showProgressBar();
                String phoneNum = phone_info.getPhoneNum();
                reqIdentifySpeecha(phoneNum, new BaseLoginCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("FRJ", "reqIdentifySpeecha() onSuccess()");
                        loadManager.hideProgressBar();
                        speechaIdentifyInputView.setState(false);
                        login_identify.setOnBtnClickState(false);
                        login_identify.startCountDown();
                    }

                    @Override
                    public void onFalse(int flag) {
                        Log.i("FRJ", "reqIdentifySpeecha() onFalse()");
                        loadManager.hideProgressBar();
                    }
                });
            }
        });

        if (TextUtils.isEmpty(zoneCode)) {
            zoneCode = lastLoginAccout.getAreaCode();
            phoneNum = lastLoginAccout.getPhoneNum();
        }

        phone_info.init("手机号", zoneCode, phoneNum,
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "点击国家代码");
                        Intent intent = new Intent(LoginByAccout.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);
                    }

                    @Override
                    public void onPhoneInfoChanged() {
                        refreshNextStepBtnStat();
                    }
                });

        login_identify.init("请输入4位验证码", new IdentifyInputView.IdentifyInputViewCallback() {
            @Override
            public void onCountDownEnd() {
                refreshNextStepBtnStat();
                final String zoneCode = phone_info.getZoneCode();
                if ("86".equals(zoneCode)) {
                    if (isFirst) {
                        isFirst = false;
                        speechaIdentifyInputView.setVisibility(View.VISIBLE);
                    }
                    speechaIdentifyInputView.setState(true);
                }
            }

            @Override
            public void onInputDataChanged() {
                refreshNextStepBtnStat();
            }

            @Override
            public void onCliclSendIdentify() {
                final String zoneCode = phone_info.getZoneCode();
                final String phoneNum = phone_info.getPhoneNum();
                if (TextUtils.isEmpty(zoneCode) || TextUtils.isEmpty(phoneNum)) {
                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "输入手机号，点击获取验证码");
                    Toast.makeText(LoginByAccout.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    login_identify.setOnBtnClickState(true);
                    return;
                }

                String errorType = LoginCheck.checkPhoneFormatWell(LoginByAccout.this, zoneCode, phoneNum);
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    checkPhoneRegisted(LoginByAccout.this, zoneCode, phoneNum, new BaseLoginCallback() {
                        @Override
                        public void onSuccess() {
                            loadManager.showProgressBar();
                            reqIdentifyCode(zoneCode, phoneNum, new SMSSendCallback() {
                                @Override
                                public void onSendSuccess() {
                                    loadManager.hideProgressBar();
                                    login_identify.startCountDown();
                                    speechaIdentifyInputView.setState(false);
                                }

                                @Override
                                public void onSendFalse() {
                                    loadManager.hideProgressBar();
                                    login_identify.setOnBtnClickState(true);
                                    speechaIdentifyInputView.setState(true);
                                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录",
                                            "失败原因：验证码超限");
                                }
                            });

                        }

                        @Override
                        public void onFalse(int flag) {
                            login_identify.setOnBtnClickState(true);
                            speechaIdentifyInputView.setState(true);
                            if (flag >= UtilInternet.REQ_OK_STRING) {
                                final XhDialog xhDialog = new XhDialog(LoginByAccout.this);
                                xhDialog.setTitle("网络有问题或手机号未注册？")
                                        .setCanselButton("取消", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录",
                                                        "失败原因：弹框未注册，选择不注册");
                                                xhDialog.cancel();
                                            }
                                        })
                                        .setSureButton("立即注册", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                register(LoginByAccout.this, phone_info.getZoneCode(), phone_info.getPhoneNum());
                                                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录",
                                                        "失败原因：弹框未注册，选择注册");
                                                xhDialog.cancel();
                                            }
                                        })
                                        .setSureButtonTextColor("#007aff")
                                        .setCancelButtonTextColor("#007aff")
                                        .show();
                            }
                        }
                    });
                } else {
                    login_identify.setOnBtnClickState(true);
                    speechaIdentifyInputView.setState(true);
                }
            }
        });

        btn_next_step.init("登录", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "输入验证码，点击登录");
                String errorType = LoginCheck.checkPhoneFormatWell(LoginByAccout.this, phone_info.getZoneCode(),
                        phone_info.getPhoneNum());
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    logInByIdentify(LoginByAccout.this, phone_info.getZoneCode(),
                            phone_info.getPhoneNum(), login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "登录成功");
                                    backToForward();
                                }

                                @Override
                                public void onFalse(int flag) {
                                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录",
                                            "失败原因：验证码错误");
                                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录",
                                            "登录失败");
                                }
                            });
                } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "失败原因：手机号不是11位");
                } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
                    XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "失败原因：手机号格式错误");
                }
            }
        });

    }


    private void refreshNextStepBtnStat() {
        boolean canClickNextBtn;
        canClickNextBtn = !phone_info.isDataAbsence() && !login_identify.isIdentifyCodeEmpty();
        btn_next_step.setClickCenterable(canClickNextBtn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_password: //密码登录
                startActivity(new Intent(this, LoginByPassword.class));
                break;
            case R.id.tv_lostsercet: //找回密码
                startActivity(new Intent(this, LostSecret.class));
                break;
            case R.id.tv_help: //遇到问题
                XHClick.mapStat(LoginByAccout.this, PHONE_TAG, "手机验证码登录", "点击遇到问题");
                gotoFeedBack();
                break;
            case R.id.tv_agreenment: //香哈协议
                XHClick.mapStat(this, PHONE_TAG, "注册", "手机号页，点香哈协议");
                AppCommon.openUrl(mAct, StringManager.api_agreementXiangha, true);
                break;
            case R.id.iv_weixin:
                int number = ToolsDevice.isAppInPhone(this, "com.tencent.mm");
                if (number == 0)
                    Tools.showToast(this, "需安装微信客户端才可以登录");
                else
                    thirdAuth(this, ShareTools.WEI_XIN, "微信");
                break;
            case R.id.iv_weibo:
                thirdAuth(this, ShareTools.SINA_NAME, "新浪");
                break;
            case R.id.iv_qq:
                thirdAuth(this, ShareTools.QQ_NAME, "QQ");
                break;
            case R.id.iv_mailbox:
                gotoLoginByEmail(this);
                break;
        }
    }


    @Override
    protected void onCountrySelected(String country_code) {
        super.onCountrySelected(country_code);
        phone_info.setZoneCode("+" + country_code);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        XHClick.mapStat(this, PHONE_TAG, "手机验证码登录", "点击返回");
    }

    @Override
    protected void onPressTopBar() {
        super.onPressTopBar();
        XHClick.mapStat(this, PHONE_TAG, "手机验证码登录", "点击返回");
    }
}
