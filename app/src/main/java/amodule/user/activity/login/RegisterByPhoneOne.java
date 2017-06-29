package amodule.user.activity.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import xh.windowview.XhDialog;

/**
 * Created by dao on 2017/2/19.
 */

public class RegisterByPhoneOne extends BaseLoginActivity implements View.OnClickListener {

    private PhoneNumInputView phone_info;
    private NextStepView btn_next_step;
    private TextView tv_agreenment;
    private String zoneCode;
    private String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_register_one);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
        XHClick.track(this, "浏览注册页");
    }

    private void initData() {
        Intent intent = getIntent();
         zoneCode = intent.getStringExtra(ZONE_CODE);
         phoneNum = intent.getStringExtra(PHONE_NUM);
    }


    private void initView() {
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tv_agreenment = (TextView) findViewById(R.id.tv_agreenment);
        setHelpViewStyle();

        tv_agreenment.setOnClickListener(this);

        phone_info.init("手机号", zoneCode, phoneNum,
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(RegisterByPhoneOne.this, PHONE_TAG, "注册", "手机号页，点国家代码");
                        Intent intent = new Intent(RegisterByPhoneOne.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);

                    }

                    @Override
                    public void onPhoneInfoChanged() {

                        if (!phone_info.isDataAbsence()) {
                            btn_next_step.setClickCenterable(true);
                        } else {
                            btn_next_step.setClickCenterable(false);
                        }
                    }
                });

        btn_next_step.init("下一步", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                XHClick.mapStat(RegisterByPhoneOne.this, PHONE_TAG, "注册", "手机号页，点下一步");
                String errorType = LoginCheck.checkPhoneFormatWell(RegisterByPhoneOne.this,
                        phone_info.getZoneCode(),
                        phone_info.getPhoneNum());

                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    checkPhoneRegisted(RegisterByPhoneOne.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    final XhDialog xhDialog = new XhDialog(RegisterByPhoneOne.this);
                                    xhDialog.setTitle("该手机号已注册，"+"\n是否去登录")
                                            .setCanselButton("否", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    XHClick.mapStat(RegisterByPhoneOne.this, PHONE_TAG,
                                                            "注册", "手机号失败：弹框已注册，选择不登录");
                                                    xhDialog.cancel();
                                                }
                                            })
                                            .setSureButton("是", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    xhDialog.cancel();
                                                    XHClick.mapStat(RegisterByPhoneOne.this, PHONE_TAG,
                                                            "注册", "手机号失败：弹框已注册，选择去登录");
                                                    gotoLogin(phone_info.getZoneCode(), phone_info.getPhoneNum());
                                                    finish();

                                                }
                                            })
                                            .setSureButtonTextColor("#007aff")
                                            .setCancelButtonTextColor("#007aff");
                                    xhDialog.show();
                                }

                                @Override
                                public void onFalse(int flag) {
                                    gotoInputIdentify(RegisterByPhoneOne.this, phone_info.getZoneCode(),
                                            phone_info.getPhoneNum(), ORIGIN_REGISTER);
                                }
                            });
                }else if(LoginCheck.NOT_11_NUM.equals(errorType)){
                    XHClick.mapStat(RegisterByPhoneOne.this, PHONE_TAG, "注册", "手机号失败：手机号不是11位");
                }else if(LoginCheck.ERROR_FORMAT.equals(errorType)){
                    XHClick.mapStat(RegisterByPhoneOne.this, PHONE_TAG, "注册", "手机号失败：手机号格式错");
                }

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_agreenment:
                XHClick.mapStat(this, PHONE_TAG, "注册", "手机号页，点香哈协议");
                AppCommon.openUrl(mAct, StringManager.api_agreementXiangha, true);
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        }else{
            XHClick.mapStat(this, PHONE_TAG, "注册", "手机号页，点返回");
            finish();
        }
    }

    @Override
    protected void onCountrySelected(String country_code) {
        super.onCountrySelected(country_code);
        phone_info.setZoneCode("+" + country_code);
    }

    private void setHelpViewStyle() {
        String agreement = "注册代表阅读并同意香哈用户协议";
        SpannableStringBuilder style = new SpannableStringBuilder(agreement);
        // 设置指定位置文字的颜色
        int start = agreement.indexOf("香哈用户协议");
        String color = Tools.getColorStr(this, R.color.c_black_text);
        style.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, agreement.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        tv_agreenment.setText(style);
    }
}

