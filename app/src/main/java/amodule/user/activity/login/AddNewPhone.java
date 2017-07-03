package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SpeechaIdentifyInputView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Created by ：fei_teng on 2017/2/22 21:31.
 */

public class AddNewPhone extends BaseLoginActivity implements View.OnClickListener {

    private IdentifyInputView login_identify;
    private SpeechaIdentifyInputView speechaIdentifyInputView;
    private NextStepView btn_next_step;
    private PhoneNumInputView phone_info;
    private TextView tv_help;
    private String zoneCode;
    private String phoneNum;
    private String motifyType;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_add_phone);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    private void initData() {
        Intent intent = getIntent();
        zoneCode = intent.getStringExtra(ZONE_CODE);
        phoneNum = intent.getStringExtra(PHONE_NUM);
        motifyType = intent.getStringExtra(MOTIFYTYPE);
    }

    private void initView() {
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        login_identify = (IdentifyInputView) findViewById(R.id.login_identify);
        speechaIdentifyInputView = (SpeechaIdentifyInputView) findViewById(R.id.login_speeach_identify);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tv_help = (TextView) findViewById(R.id.tv_help);
        tv_help.setOnClickListener(this);
        speechaIdentifyInputView.setOnSpeechaClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechaIdentifyInputView.setState(false);
                login_identify.setOnBtnClickState(false);
                login_identify.startCountDown();
                String phoneNum = phone_info.getPhoneNum();
                ReqEncyptInternet.in().doEncypt(StringManager.api_sendVoiceVerify, "phone=" + phoneNum, new InternetCallback(AddNewPhone.this) {
                    @Override
                    public void loaded(int i, String s, Object o) {

                    }
                });
            }
        });

        phone_info.init("手机号", "86", "",
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        dataStatics("方法1新手机号页，点国家代码", "方法2新手机号页，点国家代码");
                        Intent intent = new Intent(AddNewPhone.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);
                    }

                    @Override
                    public void onPhoneInfoChanged() {
                        refreshNextStepBtnStat();
                    }
                });

        login_identify.init("请输入4位验证码",
                new IdentifyInputView.IdentifyInputViewCallback() {
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

                        final String newZoneCode = phone_info.getZoneCode();
                        final String newPhoneNum = phone_info.getPhoneNum();
                        if (TextUtils.isEmpty(newZoneCode) || TextUtils.isEmpty(newPhoneNum)) {
                            Toast.makeText(AddNewPhone.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                            login_identify.setOnBtnClickState(true);
                            return;
                        }

                        String error_type = LoginCheck.checkPhoneFormatWell(AddNewPhone.this, newZoneCode, newPhoneNum);
                        if (LoginCheck.WELL_TYPE.equals(error_type)) {
                            if (newZoneCode.equals(zoneCode) && newPhoneNum.equals(phoneNum)) {
                                Toast.makeText(AddNewPhone.this, "你已经绑定这个手机号了", Toast.LENGTH_SHORT).show();
                                login_identify.setOnBtnClickState(true);
                                dataStatics("方法1失败原因：已经绑定这个手机号", "方法2失败原因：已经绑定这个手机号");
                                return;
                            }

                            dataStatics("方法1新手机号页，点获取验证码", "方法2新手机号页，点获取验证码");
                            checkPhoneRegisted(AddNewPhone.this, newZoneCode, newPhoneNum, new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(AddNewPhone.this, "这个手机号已被其他账号绑定", Toast.LENGTH_SHORT).show();
                                    login_identify.setOnBtnClickState(true);
                                    dataStatics("方法1失败原因：已经绑定其他账号", "方法2失败原因：已经绑定其他账号");
                                }

                                @Override
                                public void onFalse(int flag) {
                                    loadManager.showProgressBar();
                                    login_identify.setOnBtnClickState(true);
                                    reqIdentifyCode(newZoneCode, newPhoneNum,
                                            new SMSSendCallback() {
                                                @Override
                                                public void onSendSuccess() {
                                                    loadManager.hideProgressBar();
                                                    login_identify.startCountDown();
                                                    speechaIdentifyInputView.setState(false);
                                                }

                                                @Override
                                                public void onSendFalse() {
                                                    login_identify.setOnBtnClickState(true);
                                                    loadManager.hideProgressBar();
                                                    speechaIdentifyInputView.setState(true);
                                                    dataStatics("方法1失败原因：验证码超限", "方法2失败原因：验证码超限");
                                                }
                                            }
                                    );
                                }
                            });
                        } else {
                            login_identify.setOnBtnClickState(true);
                            speechaIdentifyInputView.setState(true);
                        }
                    }
                });

        btn_next_step.init("完成", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                dataStatics("方法1新手机号页，点完成", "方法2新手机号页，点完成");
                String type = TYPE_MOTIFY_SMS;
                String errorType = LoginCheck.checkPhoneFormatWell(AddNewPhone.this,
                        phone_info.getZoneCode(), phone_info.getPhoneNum());

                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    motifyPhone(AddNewPhone.this, type, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                            login_identify.getIdentify(), zoneCode, phoneNum,
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {

                                    dataStatics("方法1，修改成功", "方法2，修改成功");
                                    Toast.makeText(AddNewPhone.this, "修改成功", Toast.LENGTH_SHORT).show();
                                    backToForward();
                                }

                                @Override
                                public void onFalse(int flag) {
                                    dataStatics("方法1，修改失败", "方法2，修改失败");
                                    dataStatics("方法1失败原因：验证码错误", "方法2失败原因：验证码错误");
                                    Toast.makeText(AddNewPhone.this, "验证码错误", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
                    dataStatics("方法1失败原因：手机号不是11位", "方法2失败原因：手机号不是11位");

                } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
                    dataStatics("方法1失败原因：手机号格式错误", "方法2失败原因：手机号格式错误");
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_help:
                dataStatics("方法1新手机号页，点遇到问题", "方法2新手机号页，点遇到问题");
                gotoFeedBack();
                break;
            default:
                break;
        }
    }

    private void refreshNextStepBtnStat() {

        boolean canClickNextBtn = false;
        canClickNextBtn = !phone_info.isDataAbsence()
                && !login_identify.isIdentifyCodeEmpty();
        btn_next_step.setClickCenterable(canClickNextBtn);
    }

    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        } else {
            dataStatics("方法1新手机号页，点返回", "方法2新手机号页，点返回");
            finish();
        }
    }


    private void dataStatics(String msg1, String msg2) {
        if (TYPE_SMS.equals(motifyType)) {
            XHClick.mapStat(AddNewPhone.this, TAG_ACCOCUT, "修改手机号", msg1);
        } else if (TYPE_PSW.equals(motifyType)) {
            XHClick.mapStat(AddNewPhone.this, TAG_ACCOCUT, "修改手机号", msg2);
        }
    }
}
