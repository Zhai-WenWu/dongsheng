package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;

/**
 * Created by ：fei_teng on 2017/2/17 10:08.
 */

public class BindPhoneNum extends BaseLoginActivity {

    private PhoneNumInputView phone_info;
    private NextStepView btn_next_step;
    protected String origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_bind_phone_num);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    protected void initData() {
        origin = ORIGIN_BIND_PHONE_NUM;
    }

    private void initView() {

        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);

        phone_info.init("手机号", "86", "",
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(BindPhoneNum.this, TAG_ACCOCUT, "绑定手机号", "手机号页，点国家代码");
                        Intent intent = new Intent(BindPhoneNum.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);

                    }

                    @Override
                    public void onPhoneInfoChanged() {

                        btn_next_step.setClickCenterable(!phone_info.isDataAbsence());
                    }
                });

        btn_next_step.init("下一步", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                XHClick.mapStat(BindPhoneNum.this, TAG_ACCOCUT, "绑定手机号", "手机号页，点下一步");
                String errorType = LoginCheck.checkPhoneFormatWell(BindPhoneNum.this,
                        phone_info.getZoneCode(),
                        phone_info.getPhoneNum());

                if (LoginCheck.WELL_TYPE.equals(errorType)) {
                    checkPhoneRegisted(BindPhoneNum.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                            new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {
                                    XHClick.mapStat(BindPhoneNum.this, TAG_ACCOCUT, "绑定手机号", "失败原因：手机号被绑定");
                                    Toast.makeText(BindPhoneNum.this,"这个手机号已被其他账号绑定",Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFalse(int flag) {
                                    gotoInputIdentify(BindPhoneNum.this, phone_info.getZoneCode(),
                                            phone_info.getPhoneNum(), origin);
                                }
                            });
                }else if(LoginCheck.NOT_11_NUM.equals(errorType)){
                    XHClick.mapStat(BindPhoneNum.this, TAG_ACCOCUT, "绑定手机号", "失败原因：手机号不是11位");
                }else if(LoginCheck.ERROR_FORMAT.equals(errorType)){
                    XHClick.mapStat(BindPhoneNum.this, TAG_ACCOCUT, "绑定手机号", "失败原因：手机号格式错误");
                }

            }
        });

        if(ORIGIN_BIND_FROM_WEB.equals(origin)){
            Toast.makeText(this,"兑换商品需要绑定手机号",Toast.LENGTH_SHORT).show();
        }

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
        } else{
            XHClick.mapStat(this, TAG_ACCOCUT, "绑定手机号", "手机号页，点返回");
            finish();
        }
    }
}
