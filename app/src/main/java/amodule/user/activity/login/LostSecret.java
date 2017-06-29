package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/21 15:04.
 */
public class LostSecret extends BaseLoginActivity {

    private TextView tv_title;
    private PhoneNumInputView phone_info;
    private NextStepView btn_next_step;
    private TextView tv_agreenment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_register_one);
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_agreenment = (TextView) findViewById(R.id.tv_agreenment);
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);

        tv_title.setText("找回密码");
        tv_agreenment.setText("邮箱账号，请用电脑访问xiangha.com找回密码！");

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

        btn_next_step.init("下一步", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码","输入手机号页，点下一步");
                String errorType = LoginCheck.checkPhoneFormatWell(LostSecret.this, phone_info.getZoneCode(),phone_info.getPhoneNum());

                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    checkPhoneRegisted(LostSecret.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    gotoInputIdentify(LostSecret.this, phone_info.getZoneCode(),
                                            phone_info.getPhoneNum(), ORIGIN_FIND_PSW);
                                }

                                @Override
                                public void onFalse(int flag) {

                                    final XhDialog xhDialog = new XhDialog(LostSecret.this);
                                    xhDialog.setTitle("该手机号尚未注册，"+"\n是否注册新账号？")
                                            .setCanselButton("不注册", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码",
                                                            "失败原因：弹框未注册，选择不注册");
                                                    xhDialog.cancel();
                                                }
                                            })
                                            .setSureButton("注册", new View.OnClickListener() {
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
                            });
                }else if(LoginCheck.NOT_11_NUM.equals(errorType)){
                    XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码","失败原因：手机号不是11位");
                }else if(LoginCheck.ERROR_FORMAT.equals(errorType)){
                    XHClick.mapStat(LostSecret.this, PHONE_TAG, "忘记密码","失败原因：手机号格式错误");
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
