package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SpeechaIdentifyInputView;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/21 15:04.
 */
public class LostSecret extends BaseLoginActivity {

    private TextView tv_title;
    private PhoneNumInputView phone_info;
    private NextStepView btn_next_step;

    private IdentifyInputView login_identify;
    private SpeechaIdentifyInputView speechaIdentifyInputView;

    private boolean isFirst = true;
    protected String origin, nextStepStr = "登录";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_register_one);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    protected void initData() {
        Intent intent = getIntent();
        origin = intent.getStringExtra(PATH_ORIGIN);
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);

        if (getIsBindPhone() || ORIGIN_MODIFY_PSW.equals(origin)) {
            tv_title.setText("绑定手机");
            nextStepStr = "下一步";
            findViewById(R.id.tv_agreenment).setVisibility(View.GONE);
        }
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        login_identify = (IdentifyInputView) findViewById(R.id.phone_identify);
        speechaIdentifyInputView = (SpeechaIdentifyInputView) findViewById(R.id.login_speeach_identify);
        findViewById(R.id.tv_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoFeedBack();
            }
        });

        phone_info.init("手机号", "86", "", new PhoneNumInputView.PhoneNumInputViewCallback() {
            @Override
            public void onZoneCodeClick() {
                Intent intent = new Intent(LostSecret.this, CountryListActivity.class);
                startActivityForResult(intent, mGetCountryId);
                refreshNextStepBtnState();
            }

            @Override
            public void onPhoneInfoChanged() {
                refreshNextStepBtnState();
            }
        });

        speechaIdentifyInputView.setOnSpeechaClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.showProgressBar();
                reqIdentifySpeecha(phone_info.getPhoneNum(), new BaseLoginCallback() {
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

        login_identify.init("请输入验证码", new IdentifyInputView.IdentifyInputViewCallback() {
            @Override
            public void onCountDownEnd() {
                refreshNextStepBtnState();
                if ("86".equals(phone_info.getZoneCode())) {
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
                sendSmsClick();
            }
        });

        btn_next_step.init(nextStepStr, new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                if (getIsBindPhone() || ORIGIN_MODIFY_PSW.equals(origin)) {
                    XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "验证码页，点下一步");
                } else if (ORIGIN_FIND_PSW.equals(origin)) {
                    XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码", "获取验证码页，点下一步");
                }
                final String zoneCode = phone_info.getZoneCode();
                final String phoneNum = phone_info.getPhoneNum();

                if (ORIGIN_REGISTER.equals(origin)) { //注册
                    logInByIdentify(LostSecret.this, zoneCode, phoneNum, login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    gotoSetSecrt(zoneCode, phoneNum, origin, login_identify.getIdentify());
                                    if (ORIGIN_REGISTER.equals(origin)) {
                                        XHClick.mapStat(LostSecret.this, PHONE_TAG, "注册", "注册成功");
                                    }
                                }

                                @Override
                                public void onFalse(int flag) {
                                    if (ORIGIN_REGISTER.equals(origin)) {
                                        XHClick.mapStat(LostSecret.this, PHONE_TAG, "注册", "验证码失败：验证码错误");
                                        XHClick.mapStat(LostSecret.this, PHONE_TAG, "注册", "注册失败");
                                    }
                                }
                            });
                } else if (getIsBindPhone()) { //绑定手机号
                    bindPhone(LostSecret.this, zoneCode, phoneNum, login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "绑定成功");
                                }

                                @Override
                                public void onFalse(int flag) {
                                    XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "失败原因：验证码错误");
                                    XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "绑定失败");
                                }
                            });
                } else if(ORIGIN_FIND_PSW.equals(origin)){ //修改密码
                    checkIdentifyCode(LostSecret.this, zoneCode, phoneNum,
                            login_identify.getIdentify(), new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    gotoSetSecrt(zoneCode, phoneNum, origin, login_identify.getIdentify());
                                }

                                @Override
                                public void onFalse(int flag) {
                                    XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码", "失败原因：验证码错误");
                                }
                            });
                }
            }
        });
    }

    private boolean getIsBindPhone() {
        return ORIGIN_BIND_PHONE_NUM.equals(origin) || ORIGIN_BIND_FROM_WEB.equals(origin);
    }

    private void sendSmsClick() {
        if (getIsBindPhone()) {
            XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "验证码页，点获取验证码");
        } else if (ORIGIN_FIND_PSW.equals(origin)) {
            XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "忘记密码", "验证手机号页，点获取验证码");
        }else if(ORIGIN_MODIFY_PSW.equals(origin)){
            XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "修改密码", "验证手机号页，点获取验证码");
        }
        loadManager.showProgressBar();
        String errorType = LoginCheck.checkPhoneFormatWell(LostSecret.this, phone_info.getZoneCode(), phone_info.getPhoneNum());
        if (LoginCheck.WELL_TYPE.equals(errorType)) {
            checkPhoneRegisted(LostSecret.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                    new BaseLoginCallback() {
                        @Override
                        public void onSuccess() {
                            if (getIsBindPhone() || ORIGIN_MODIFY_PSW.equals(origin)) { //当动作是绑定手机号，但手机号被绑定了
                                loadManager.hideProgressBar();
                                XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "失败原因：手机号被绑定");
                                Toast.makeText(LostSecret.this, "这个手机号已被其他账号绑定", Toast.LENGTH_SHORT).show();
                            } else
                                sendSms();
                        }

                        @Override
                        public void onFalse(int flag) {
                            if (getIsBindPhone() || ORIGIN_MODIFY_PSW.equals(origin)) {
                                sendSms();
                            } else {
                                loadManager.hideProgressBar();
                                final XhDialog xhDialog = new XhDialog(LostSecret.this);
                                xhDialog.setTitle("网络有问题或手机号未注册？")
                                        .setCanselButton("取消", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码",
                                                        "失败原因：弹框未注册，选择不注册");
                                                xhDialog.cancel();
                                            }
                                        })
                                        .setSureButton("立即注册", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                register(LostSecret.this, phone_info.getZoneCode(),
                                                        phone_info.getPhoneNum());
                                                XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码",
                                                        "失败原因：弹框未注册，选择注册");
                                                finish();
                                                xhDialog.cancel();
                                            }
                                        })
                                        .setSureButtonTextColor("#007aff")
                                        .setCancelButtonTextColor("#007aff");
                                xhDialog.show();
                            }
                        }
                    });
        } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
            XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码", "失败原因：手机号不是11位");
        } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
            XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码", "失败原因：手机号格式错误");
        }
    }

    private void sendSms() {
        reqIdentifyCode(phone_info.getZoneCode(), phone_info.getPhoneNum(), new SMSSendCallback() {
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
                if (getIsBindPhone()) {
                    XHClick.mapStat(LostSecret.this, TAG_ACCOCUT, "绑定手机号", "失败原因：验证码超限");
                } else if (ORIGIN_FIND_PSW.equals(origin)) {
                    XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码", "失败原因：验证码超限");
                }
            }
        });
    }

    private void refreshNextStepBtnState() {
        btn_next_step.setClickCenterable(!phone_info.isDataAbsence());
    }

    @Override
    protected void onCountrySelected(String country_code) {
        super.onCountrySelected(country_code);
        phone_info.setZoneCode("+" + country_code);
    }

    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        } else {
            XHClick.mapStat(this, PHONE_TAG, "忘记密码", "输入手机号页，点返回");
            finish();
        }
    }
}
