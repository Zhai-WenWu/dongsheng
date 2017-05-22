package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import xh.basic.internet.UtilInternet;
import xh.windowview.XhDialog;


/**
 * Created by ：fei_teng on 2017/2/16 18:41.
 */

public class LoginByPhoneIndentify extends BaseLoginActivity implements View.OnClickListener {

    private IdentifyInputView login_identify;
    private NextStepView btn_next_step;
    private PhoneNumInputView phone_info;
    private TextView tv_help;
    private String zoneCode;
    private String phoneNum;


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
        login_identify = (IdentifyInputView) findViewById(R.id.login_identify);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tv_help = (TextView) findViewById(R.id.tv_help);
        tv_help.setOnClickListener(this);

        if (TextUtils.isEmpty(zoneCode)) {
            zoneCode = lastLoginAccout.getAreaCode();
            phoneNum = lastLoginAccout.getPhoneNum();
        }

        phone_info.init("手机号", zoneCode, phoneNum,
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "点击国家代码");
                        Intent intent = new Intent(LoginByPhoneIndentify.this, CountryListActivity.class);
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
                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "输入手机号，点击获取验证码");
                    Toast.makeText(LoginByPhoneIndentify.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    login_identify.btnClickTrue();
                    return;
                }

                String errorType = LoginCheck.checkPhoneFormatWell(LoginByPhoneIndentify.this, zoneCode, phoneNum);
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    checkPhoneRegisted(LoginByPhoneIndentify.this, zoneCode, phoneNum, new BaseLoginCallback() {
                        @Override
                        public void onSuccess() {
                            loadManager.showProgressBar();
                            reqIdentifyCode(zoneCode, phoneNum, new SMSSendCallback() {
                                @Override
                                public void onSendSuccess() {
                                    loadManager.hideProgressBar();
                                    login_identify.startCountDown();
                                }

                                @Override
                                public void onSendFalse() {
                                    loadManager.hideProgressBar();
                                    login_identify.btnClickTrue();
                                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录",
                                            "失败原因：验证码超限");
                                }
                            });

                        }

                        @Override
                        public void onFalse(int flag) {
                            login_identify.btnClickTrue();
                            if(flag>= UtilInternet.REQ_OK_STRING) {
                                final XhDialog xhDialog = new XhDialog(LoginByPhoneIndentify.this);
                                xhDialog.setTitle("该手机号尚未注册，" + "\n是否注册新账号？")
                                        .setCanselButton("不注册", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录",
                                                        "失败原因：弹框未注册，选择不注册");
                                                xhDialog.cancel();
                                            }
                                        })
                                        .setSureButton("注册", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                register(LoginByPhoneIndentify.this, phone_info.getZoneCode(), phone_info.getPhoneNum());
                                                XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录",
                                                        "失败原因：弹框未注册，选择注册");
                                                xhDialog.cancel();
                                            }
                                        })
                                        .setSureButtonTextColor("#007aff")
                                        .setCancelButtonTextColor("#007aff");
                                xhDialog.show();
                            }
                        }
                    });
                }else{
                    login_identify.btnClickTrue();
                }
            }
        });

        btn_next_step.init("登录", "", "", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "输入验证码，点击登录");
                String errorType = LoginCheck.checkPhoneFormatWell(LoginByPhoneIndentify.this, phone_info.getZoneCode(),
                        phone_info.getPhoneNum());
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    logInByIdentify(LoginByPhoneIndentify.this, phone_info.getZoneCode(),
                            phone_info.getPhoneNum(), login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "登录成功");
                                    backToForward();
                                }

                                @Override
                                public void onFalse(int flag) {
                                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录",
                                            "失败原因：验证码错误");
                                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录",
                                            "登录失败");
                                }
                            });
                } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "失败原因：手机号不是11位");
                } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
                    XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "失败原因：手机号格式错误");
                }
            }

            @Override
            public void onClickLeftView() {

            }

            @Override
            public void onClickRightView() {

            }
        });

    }


    private void refreshNextStepBtnStat() {

        boolean canClickNextBtn = false;
        canClickNextBtn = !phone_info.isDataAbsence()
                && !login_identify.isIdentifyCodeEmpty();
        btn_next_step.setClickCenterable(canClickNextBtn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_help:
                XHClick.mapStat(LoginByPhoneIndentify.this, PHONE_TAG, "手机验证码登录", "点击遇到问题");
                gotoFeedBack();
                break;
            default:
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
