package amodule.user.activity.login;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SecretInputView;
import amodule.user.view.SpeechaIdentifyInputView;

/**
 * Created by ：fei_teng on 2017/2/21 16:40.
 */

public class BindPhone extends BaseLoginActivity implements View.OnClickListener {

    private TextView tv_top_right;
    private PhoneNumInputView phone_info;
    private IdentifyInputView login_identify;
    private SpeechaIdentifyInputView speechaIdentifyInputView;
    private SecretInputView user_secret;
    private NextStepView btn_next_step;
    private String loginType;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_bind_phone);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    private void initData() {
        loginType = getIntent().getStringExtra(LOGINTYPE);
    }


    private void initView() {

        tv_top_right = (TextView) findViewById(R.id.tv_top_right);
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        login_identify = (IdentifyInputView) findViewById(R.id.phone_identify);
        speechaIdentifyInputView = (SpeechaIdentifyInputView) findViewById(R.id.login_speeach_identify);
        user_secret = (SecretInputView) findViewById(R.id.user_secret);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        speechaIdentifyInputView.setOnSpeechaClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.showProgressBar();
                String phoneNum = phone_info.getPhoneNum();
                reqIdentifySpeecha(phoneNum,new BaseLoginCallback(){
                    @Override
                    public void onSuccess() {
                        loadManager.hideProgressBar();
                        speechaIdentifyInputView.setState(false);
                        login_identify.setOnBtnClickState(false);
                        login_identify.startCountDown();
                    }

                    @Override
                    public void onFalse(int flag) {
                        loadManager.hideProgressBar();
                    }
                });
            }
        });

        tv_top_right.setOnClickListener(this);
        phone_info.init("手机号", "86", "", new PhoneNumInputView.PhoneNumInputViewCallback() {
            @Override
            public void onZoneCodeClick() {
                refreshNextStepBtnState();
            }

            @Override
            public void onPhoneInfoChanged() {
                refreshNextStepBtnState();
            }
        });

        login_identify.init("请输入验证码", new IdentifyInputView.IdentifyInputViewCallback() {
            @Override
            public void onCountDownEnd() {
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
                refreshNextStepBtnState();
            }

            @Override
            public void onCliclSendIdentify() {

                checkPhoneRegisted(BindPhone.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                        new BaseLoginCallback() {
                            @Override
                            public void onSuccess() {
                                dataStatistics("绑定失败：手机号已被绑定");
                                login_identify.setOnBtnClickState(true);
                                Toast.makeText(BindPhone.this, "已被其他账号绑定，不能重复绑定", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFalse(int flag) {
                                dataStatistics("绑定手机号页，获取验证码");
                                loadManager.showProgressBar();
                                login_identify.startCountDown();
                                reqIdentifyCode(phone_info.getZoneCode(), phone_info.getPhoneNum(),new SMSSendCallback() {
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
                                            dataStatistics("绑定失败：验证码超限");
                                        }
                                    });
                            }
                        });
            }
        });


        user_secret.init("6-20位，字母、数字或字符", new SecretInputView.SecretInputViewCallback() {
            @Override
            public void onInputSecretChanged() {
                refreshNextStepBtnState();
            }

            @Override
            public void OnClicksecret() {
                dataStatistics("绑定手机号页，点击密码眼睛");
            }
        });
        user_secret.showSecret();

        btn_next_step.init("提交", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                String errorType = LoginCheck.checkPhoneFormatWell(BindPhone.this, phone_info.getZoneCode(),
                        phone_info.getPhoneNum());
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    if (!LoginCheck.isSecretFormated(user_secret.getPassword())) {
                        dataStatistics("绑定失败：密码不符合要求");
                        return;
                    }

                    bindPhone(BindPhone.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                            login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    gotoSetPersonInfo(BindPhone.this, loginType);
                                    dataStatistics("绑定手机号成功");
                                }

                                @Override
                                public void onFalse(int flag) {
                                    dataStatistics("绑定手机号失败");
                                    dataStatistics("绑定失败：验证码错误");
                                }
                            });

                } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
                    dataStatistics("绑定失败：手机号不是11位");
                } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
                    dataStatistics("绑定失败：手机号格式错误");
                }

            }
        });
    }


    private void refreshNextStepBtnState() {

        btn_next_step.setClickCenterable(
                !(phone_info.isDataAbsence()
                        || login_identify.isIdentifyCodeEmpty()
                        || user_secret.isEmpty())
        );

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_top_right:
                dataStatistics("跳过绑定手机号");
                gotoSetPersonInfo(this, loginType);
                break;
            default:
                break;
        }
    }


    private void dataStatistics(String threeLevel) {
        if (QQ_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(BindPhone.this, PHONE_TAG, "QQ登录", threeLevel);
        } else if (WEIXIN_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(BindPhone.this, PHONE_TAG, "微信登录", threeLevel);
        } else if (WEIBO_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(BindPhone.this, PHONE_TAG, "微博登录", threeLevel);
        } else if (MEIZU_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(BindPhone.this, PHONE_TAG, "魅族登录", threeLevel);
        } else if (EMAIL_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(BindPhone.this, PHONE_TAG, "邮箱登录", threeLevel);
        }
    }

    @Override
    public void finish() {
        Main.colse_level = 4;
        super.finish();
    }
}
