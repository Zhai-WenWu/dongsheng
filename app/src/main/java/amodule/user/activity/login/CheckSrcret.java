package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SecretInputView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by ：fei_teng on 2017/2/22 21:57.
 */

public class CheckSrcret extends BaseLoginActivity implements View.OnClickListener {

    private ImageView top_left_view;
    private PhoneNumInputView phone_info;
    private SecretInputView ll_secret;
    private NextStepView btn_next_step;
    private TextView tv_help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_check_sercet);
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    private void initView() {

        top_left_view = (ImageView) findViewById(R.id.top_left_view);
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        ll_secret = (SecretInputView) findViewById(R.id.ll_secret);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tv_help = (TextView) findViewById(R.id.tv_help);

        top_left_view.setOnClickListener(this);
        tv_help.setOnClickListener(this);


        phone_info.init("旧手机号", "86", "",
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(CheckSrcret.this, TAG_ACCOCUT, "修改手机号",
                                "方法2验证密码页，点国家代码");
                        Intent intent = new Intent(CheckSrcret.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);
                    }

                    @Override
                    public void onPhoneInfoChanged() {
                        onInputDataChanged();
                    }
                });

        ll_secret.init("不少于6位，字母、数字或字符", new SecretInputView.SecretInputViewCallback() {
            @Override
            public void onInputSecretChanged() {
                onInputDataChanged();
            }

            @Override
            public void OnClicksecret() {
                XHClick.mapStat(CheckSrcret.this, TAG_ACCOCUT, "修改手机号",
                        "方法2验证密码页，点密码眼睛");
            }
        });
        ll_secret.showSecret();

        btn_next_step.init("下一步",  new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(CheckSrcret.this, TAG_ACCOCUT, "修改手机号",
                        "方法2验证密码页，点下一步");
                checkSecret();
            }
        });
    }

    private void checkSecret() {

        String errorType = LoginCheck.checkPhoneFormatWell(this, phone_info.getZoneCode(), phone_info.getPhoneNum());
        if (!LoginCheck.WELL_TYPE.equals(errorType)) {
            return;
        }

        if(ll_secret.getPassword().length() < 6){
            Toast.makeText(this,"手机号或密码错误",Toast.LENGTH_SHORT).show();
            return;
        }

        loadManager.showProgressBar();
        String param = "phone=" + phone_info.getPhoneNum() + "&&cc=" + phone_info.getZoneCode()
                + "&&password=" + ll_secret.getPassword();
        ReqInternet.in().doPost(StringManager.api_checkAccount, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    loadManager.hideProgressBar();
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(msg);
                    if (maps.size() > 0) {
                        Map<String, String> map = maps.get(0);
                        String result = map.get("result");
                        if ("2".equals(result)) {
                            gotoAddNewPhone(CheckSrcret.this, phone_info.getZoneCode(),
                                    phone_info.getPhoneNum(),
                                   TYPE_PSW);
                        } else {
                            XHClick.mapStat(context, TAG_ACCOCUT, "修改手机号", " 方法2失败原因：手机号或密码错误");
                            Toast.makeText(context, "手机号或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void onInputDataChanged() {

        boolean canClickNextBtn = !(phone_info.isDataAbsence() || ll_secret.isEmpty());
        btn_next_step.setClickCenterable(canClickNextBtn);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.top_left_view:
                XHClick.mapStat(this, TAG_ACCOCUT, "修改手机号", "方法2验证密码页，点返回");
                finish();
                break;
            case R.id.tv_help:
                XHClick.mapStat(this, TAG_ACCOCUT, "修改手机号", "方法2验证密码页，点遇到问题");
                gotoFeedBack();
                break;
            default:
                break;
        }
    }
}
