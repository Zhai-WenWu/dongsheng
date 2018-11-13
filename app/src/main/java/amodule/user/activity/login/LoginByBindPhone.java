package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xiangha.R;

import java.io.Serializable;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SpeechaIdentifyInputView;
import amodule.vip.DeviceVipStatModel;
import amodule.vip.IStat;

public class LoginByBindPhone extends BaseLoginActivity implements View.OnClickListener {

    private IdentifyInputView login_identify;
    private NextStepView btn_next_step;
    private PhoneNumInputView phone_info;
    private SpeechaIdentifyInputView speechaIdentifyInputView;
    private String zoneCode;
    private String phoneNum;

    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_by_bindphone);
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
        if (TextUtils.isEmpty(zoneCode) || TextUtils.isEmpty(phoneNum)) {
            zoneCode = lastLoginAccout.getAreaCode();
            phoneNum = lastLoginAccout.getPhoneNum();
        }
    }

    private void initView() {
        phone_info = findViewById(R.id.phone_info);
        speechaIdentifyInputView = findViewById(R.id.login_speeach_identify);
        login_identify = findViewById(R.id.login_identify);
        btn_next_step = findViewById(R.id.btn_next_step);

        findViewById(R.id.tv_help).setOnClickListener(this);
        findViewById(R.id.tv_agreenment).setOnClickListener(this);
        findViewById(R.id.top_left_view).setOnClickListener(this);

        speechaIdentifyInputView.setOnSpeechaClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.showProgressBar();
                String phoneNum = phone_info.getPhoneNum();
                reqIdentifySpeecha(phoneNum, new BaseLoginCallback() {
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

        phone_info.init("手机号", zoneCode, phoneNum,
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "点击国家代码");
                        Intent intent = new Intent(LoginByBindPhone.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);
                    }

                    @Override
                    public void onPhoneInfoChanged() {
                        refreshNextStepBtnStat();
                    }
                });

        login_identify.init("请输入4位验证码", new IdentifyInputView.IdentifyInputViewCallback() {

            @Override
            public void onTick(long millisUntilFinished) {
                if(isFirst && millisUntilFinished >= 20 * 1000){
                    final String zoneCode = phone_info.getZoneCode();
                    if ("86".equals(zoneCode)) {
                        isFirst = false;
                        speechaIdentifyInputView.setVisibility(View.VISIBLE);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn_next_step.getLayoutParams();
                        layoutParams.setMargins(0, Tools.getDimen(mAct, R.dimen.dp_36), 0, 0);
                        speechaIdentifyInputView.setState(true);
                    }
                }
            }

            @Override
            public void onCountDownEnd() {
                refreshNextStepBtnStat();
                final String zoneCode = phone_info.getZoneCode();
                if ("86".equals(zoneCode)) {
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
                    XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "输入手机号，点击获取验证码");
                    Toast.makeText(LoginByBindPhone.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    login_identify.setOnBtnClickState(true);
                    return;
                }

                String errorType = LoginCheck.checkPhoneFormatWell(LoginByBindPhone.this, zoneCode, phoneNum);
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
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
                            XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录",
                                    "失败原因：验证码超限");
                        }
                    });
                } else {
                    login_identify.setOnBtnClickState(true);
                    speechaIdentifyInputView.setState(true);
                }
            }
        });

        btn_next_step.init("登录并绑定", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                Serializable serializable = getIntent().getSerializableExtra(DeviceVipStatModel.TAG);
                XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "输入验证码，点击登录");
                String errorType = LoginCheck.checkPhoneFormatWell(LoginByBindPhone.this, phone_info.getZoneCode(),
                        phone_info.getPhoneNum());
                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    IStat statImpl = null;
                    if (serializable != null && serializable instanceof IStat) {
                        statImpl = (IStat) serializable;
                    }
                    logInByIdentify(LoginByBindPhone.this, phone_info.getZoneCode(),
                            phone_info.getPhoneNum(), login_identify.getIdentify(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "登录成功");
                                    backToForward();
                                }

                                @Override
                                public void onFalse(int flag) {
                                    XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录",
                                            "失败原因：验证码错误");
                                    XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录",
                                            "登录失败");
                                }
                            }, statImpl);
                } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
                    XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "失败原因：手机号不是11位");
                } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
                    XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "失败原因：手机号格式错误");
                }
                if (serializable != null && serializable instanceof DeviceVipStatModel) {//与设备会员绑定相关
                    DeviceVipStatModel model = new DeviceVipStatModel("登录并绑定按钮点击次数", null);
                    XHClick.mapStat(LoginByBindPhone.this, model.getEventID(), model.getTwoLevelVipBindPage(), model.getThreeLevel1());
                }
                StatisticsManager.saveData(StatModel.createBtnClickModel(LoginByBindPhone.class.getSimpleName(), "登录绑定", "登录并绑定"));
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
            case R.id.top_left_view:
                onPressTopBar();
                break;
            case R.id.tv_help: //遇到问题
                XHClick.mapStat(LoginByBindPhone.this, PHONE_TAG, "手机验证码登录", "点击遇到问题");
                gotoFeedBack();
                break;
            case R.id.tv_agreenment: //香哈协议
                XHClick.mapStat(this, PHONE_TAG, "注册", "手机号页，点香哈协议");
                AppCommon.openUrl(mAct, StringManager.api_agreementXiangha, true);
                break;
        }
    }

    @Override
    protected void onCountrySelected(String country_code) {
        super.onCountrySelected(country_code);
        phone_info.setZoneCode("+" + country_code);
        if(!"86".equals(country_code)){
            speechaIdentifyInputView.setVisibility(View.GONE);
            speechaIdentifyInputView.setState(false);
            isFirst = true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "绑定失败", Toast.LENGTH_SHORT).show();
        XHClick.mapStat(this, PHONE_TAG, "手机验证码登录", "点击返回");
        Serializable serializable = getIntent().getSerializableExtra(DeviceVipStatModel.TAG);
        if (serializable != null && serializable instanceof DeviceVipStatModel) {//与设备会员绑定相关
            DeviceVipStatModel model = new DeviceVipStatModel("返回按钮_绑定失败toast次数", null);
            XHClick.mapStat(this, model.getEventID(), model.getTwoLevelVipBindPage(), model.getThreeLevel1());
        }
        StatisticsManager.saveData(StatModel.createBtnClickModel(getClass().getSimpleName(), "登录绑定", "返回建"));
    }

    @Override
    protected void onPressTopBar() {
        super.onPressTopBar();
        XHClick.mapStat(this, PHONE_TAG, "手机验证码登录", "点击返回");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String phoneNum = intent.getStringExtra(PHONE_NUM);
        String zoneCode = intent.getStringExtra(ZONE_CODE);
        if(!TextUtils.isEmpty(phoneNum) && !TextUtils.isEmpty(zoneCode)){
            phone_info.setInfo(zoneCode,phoneNum);
        }
    }
}
