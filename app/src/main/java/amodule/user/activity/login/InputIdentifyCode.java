package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.SpeechaIdentifyInputView;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/20 15:41.
 */

public class InputIdentifyCode extends BaseLoginActivity implements View.OnClickListener {

    private ImageView top_left_view;
    private TextView tv_identify_info;
    private IdentifyInputView login_identify;
    private SpeechaIdentifyInputView speechaIdentifyInputView;
    private NextStepView btn_next_step;
    private String zoneCode;
    private String phoneNum;
    private TextView tv_help;
    private String origin;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_register_two);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);

        if (ORIGIN_MODIFY_PSW.equals(origin)) {
            loadManager.showProgressBar();
            reqIdentifyCode(zoneCode, phoneNum, new SMSSendCallback() {
                @Override
                public void onSendSuccess() {
                    loadManager.hideProgressBar();
                    login_identify.startCountDown();
                    tv_identify_info.setText("短信验证码已发送至 +" + zoneCode + " " + hidePhoneNum(phoneNum));
                    tv_identify_info.setVisibility(View.VISIBLE);
                }
                @Override
                public void onSendFalse() {
                    loadManager.hideProgressBar();
                }
            });

        }
    }


    private void initData() {

        Intent intent = getIntent();
        origin = intent.getStringExtra(PATH_ORIGIN);
        zoneCode = intent.getStringExtra(ZONE_CODE);
        phoneNum = intent.getStringExtra(PHONE_NUM);
    }

    private void initView() {
        top_left_view = (ImageView) findViewById(R.id.top_left_view);
        tv_identify_info = (TextView) findViewById(R.id.tv_identify_info);
        tv_help = (TextView) findViewById(R.id.tv_help);
        login_identify = (IdentifyInputView) findViewById(R.id.phone_identify);
        speechaIdentifyInputView = (SpeechaIdentifyInputView) findViewById(R.id.login_speeach_identify);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        top_left_view.setOnClickListener(this);
        tv_help.setOnClickListener(this);
        tv_identify_info.setVisibility(View.INVISIBLE);
        speechaIdentifyInputView.setOnSpeechaClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.showProgressBar();
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

        login_identify.init("请输入验证码", new IdentifyInputView.IdentifyInputViewCallback() {
            @Override
            public void onCountDownEnd() {
                refreshNextStepBtnState();
                if("86".equals(zoneCode)) {
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

                if (ORIGIN_FIND_PSW.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "验证码页，点击获取验证码");
                } else if (ORIGIN_BIND_PHONE_NUM.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "验证码页，点获取验证码");
                } else if (ORIGIN_MODIFY_PSW.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "修改密码", "验证手机号页，点获取验证码");
                }
                loadManager.showProgressBar();
                reqIdentifyCode(zoneCode, phoneNum, new SMSSendCallback() {
                    @Override
                    public void onSendSuccess() {
                        loadManager.hideProgressBar();
                        login_identify.startCountDown();
                        tv_identify_info.setText("短信验证码已发送至 +" + zoneCode + " " + formatPhone(phoneNum));
                        if (ORIGIN_REGISTER.equals(origin) || ORIGIN_REGISTER.equals(origin)) {
                            tv_identify_info.setText("短信验证码已发送至 +" + zoneCode + " " + hidePhoneNum(phoneNum));
                        } else {
                            tv_identify_info.setText("短信验证码已发送至 +" + zoneCode + " " + formatPhone(phoneNum));
                        }

                        tv_identify_info.setVisibility(View.VISIBLE);
                        speechaIdentifyInputView.setState(false);
                    }

                    @Override
                    public void onSendFalse() {
                        loadManager.hideProgressBar();
                        login_identify.setOnBtnClickState(true);
                        speechaIdentifyInputView.setState(true);
                        if (ORIGIN_FIND_PSW.equals(origin)) {
                            XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "忘记密码", "失败原因：验证码超限");
                        } else if (ORIGIN_REGISTER.equals(origin)) {
                            XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "验证码失败：验证码超限");
                        } else if (ORIGIN_BIND_PHONE_NUM.equals(origin)) {
                            XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "失败原因：验证码超限");
                        } else if (ORIGIN_MODIFY_PSW.equals(origin)) {
                            XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "修改密码", "失败原因：验证码超限");
                        }
                    }
                });
            }
        });

        btn_next_step.init("下一步",  new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                if (ORIGIN_REGISTER.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "验证码页，点击下一步");
                } else if (ORIGIN_BIND_PHONE_NUM.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "验证码页，点下一步");
                } else if (ORIGIN_FIND_PSW.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "忘记密码", "获取验证码页，点下一步");
                } else if (ORIGIN_MODIFY_PSW.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "修改密码", "验证手机号页，点下一步");
                }

                if (ORIGIN_REGISTER.equals(origin)) {
                    logInByIdentify(InputIdentifyCode.this, zoneCode, phoneNum, login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    gotoSetSecrt(zoneCode, phoneNum, origin, login_identify.getIdentify());
                                    if (ORIGIN_REGISTER.equals(origin)) {
                                        XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "注册成功");
                                    }
                                }

                                @Override
                                public void onFalse(int flag) {
                                    if (ORIGIN_REGISTER.equals(origin)) {
                                        XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "验证码失败：验证码错误");
                                        XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "注册失败");
                                    }
                                }
                            });
                } else if (ORIGIN_BIND_PHONE_NUM.equals(origin) || ORIGIN_BIND_FROM_WEB.equals(origin)) {

                    bindPhone(InputIdentifyCode.this, zoneCode, phoneNum,
                            login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {

                                    gotoSetSecrt(zoneCode, phoneNum, origin, login_identify.getIdentify());
                                    if(ORIGIN_BIND_PHONE_NUM.equals(origin)){
                                        XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "绑定成功");
                                    }

                                }

                                @Override
                                public void onFalse(int flag) {

                                    if(ORIGIN_BIND_PHONE_NUM.equals(origin)){
                                        XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "失败原因：验证码错误");
                                        XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "绑定失败");
                                    }else if (ORIGIN_MODIFY_PSW.equals(origin)) {
                                        XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "修改密码", "失败原因：验证码错误");
                                        XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "修改密码", "修改失败");
                                    }
                                }
                            });
                } else {

                    checkIdentifyCode(InputIdentifyCode.this, zoneCode, phoneNum,
                            login_identify.getIdentify(), new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    gotoSetSecrt(zoneCode, phoneNum, origin, login_identify.getIdentify());
                                }

                                @Override
                                public void onFalse(int flag) {
                                    if (ORIGIN_FIND_PSW.equals(origin)) {
                                        XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "忘记密码", "失败原因：验证码错误");
                                    }
                                }
                            });
                }

            }
        });
    }


    private void refreshNextStepBtnState() {
        String identify = login_identify.getIdentify();
        if (TextUtils.isEmpty(identify)) {
            btn_next_step.setClickCenterable(false);
        } else {
            btn_next_step.setClickCenterable(true);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_help:
                if (ORIGIN_REGISTER.equals(origin)) {
                    XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "验证码页，点击遇到问题");
                }
                gotoFeedBack();
                break;

            default:
                break;
        }

    }


    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
            return;
        }

        if (ORIGIN_FIND_PSW.equals(origin)) {
            XHClick.mapStat(this, PHONE_TAG, "忘记密码", "获取验证码页，点返回");
        } else if (ORIGIN_REGISTER.equals(origin)) {
            XHClick.mapStat(this, PHONE_TAG, "注册", "验证码页，点返回");
        } else if (ORIGIN_BIND_PHONE_NUM.equals(origin)) {
            XHClick.mapStat(this, PHONE_TAG, "绑定手机号", "验证码页，点返回");
        } else if (ORIGIN_MODIFY_PSW.equals(origin)) {
            XHClick.mapStat(this, TAG_ACCOCUT, "修改密码", "验证码页，点返回");
        }else if(ORIGIN_BIND_PHONE_NUM.equals(origin)){
            XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "绑定手机号", "验证码页，点返回");
        } else if (ORIGIN_MODIFY_PSW.equals(origin)) {
            XHClick.mapStat(InputIdentifyCode.this, TAG_ACCOCUT, "修改密码", "验证码页，点返回");
        }

        if (ORIGIN_REGISTER.equals(origin)) {
            String title = "";
            String cancelText = "";
            String sureText = "";
            if (ORIGIN_REGISTER.equals(origin)) {
                title = "注册即将完成" + "\n确认中断并返回？";
                cancelText = "中断";
                sureText = "继续";
            } else {
                title = "绑定成功，请设置密码？";
                cancelText = "下次再说";
                sureText = "设置密码";
            }

            final XhDialog xhDialog = new XhDialog(this);
            xhDialog.setTitle(title);
            xhDialog.setSureButton(sureText, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ORIGIN_REGISTER.equals(origin)) {
                        XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "弹框是否中断，选择继续");
                    }
                    xhDialog.cancel();
                }
            });
            xhDialog.setCanselButton(cancelText, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ORIGIN_REGISTER.equals(origin)) {
                        XHClick.mapStat(InputIdentifyCode.this, PHONE_TAG, "注册", "弹框是否中断，选择中断");
                        finish();
                    } else {
                        backToForward();
                    }
                    xhDialog.cancel();
                }
            })
                    .setSureButtonTextColor("#007aff")
                    .setCancelButtonTextColor("#007aff").show();
        } else {

            finish();
        }
    }


    private String formatPhone(String tel) {

        if (tel != null && tel.length() == 11) {
            tel = tel.replaceAll("^(\\d{3})(\\d{4})(\\d{4})$", "$1 $2 $3");
        }
        return tel;

    }

}
