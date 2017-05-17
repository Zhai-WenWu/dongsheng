package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.IdentifyInputView;
import amodule.user.view.NextStepView;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/22 21:04.
 */

public class ChangePhone extends BaseLoginActivity implements View.OnClickListener {

    private IdentifyInputView phone_identify;
    private NextStepView btn_next_step;
    private TextView tv_identify_info;
    private String zoneCode;
    private String phoneNum;
    private TextView tv_help;
    private TextView tv_phone_missed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_change_phone);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }

    private void initData() {
        Intent intent = getIntent();
        zoneCode = intent.getStringExtra(ZONE_CODE);
        phoneNum = intent.getStringExtra(PHONE_NUM);
    }

    private void initView() {

        phone_identify = (IdentifyInputView) findViewById(R.id.phone_identify);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tv_help = (TextView) findViewById(R.id.tv_help);
        tv_phone_missed = (TextView) findViewById(R.id.tv_phone_missed);
        tv_identify_info = (TextView) findViewById(R.id.tv_identify_info);
        tv_identify_info.setVisibility(View.VISIBLE);
        tv_identify_info.setText("旧手机号是+" + zoneCode + " "
                + hidePhoneNum(phoneNum) + "，请获取验证码");
        tv_help.setOnClickListener(this);
        tv_phone_missed.setOnClickListener(this);

        phone_identify.init("验证码", new IdentifyInputView.IdentifyInputViewCallback() {
            @Override
            public void onCountDownEnd() {
                refreshNextStepBtnState();
            }

            @Override
            public void onInputDataChanged() {
                refreshNextStepBtnState();
            }

            @Override
            public void onCliclSendIdentify() {

                XHClick.mapStat(ChangePhone.this, TAG_ACCOCUT, "修改手机号", "方法1验证码页，点获取验证码");
                phone_identify.startCountDown();
                loadManager.showProgressBar();
                reqIdentifyCode(zoneCode, phoneNum, new SMSSendCallback() {
                    @Override
                    public void onSendSuccess() {
                        loadManager.hideProgressBar();
                        phone_identify.startCountDown();
                    }

                    @Override
                    public void onSendFalse() {
                        loadManager.hideProgressBar();
                    }
                });

            }
        });

        btn_next_step.init("下一步", "", "", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                XHClick.mapStat(ChangePhone.this, TAG_ACCOCUT, "修改手机号", "方法1验证码页，点下一步");
                checkIdentifyCode(ChangePhone.this, zoneCode, phoneNum, phone_identify.getIdentify(),
                        new BaseLoginCallback() {
                            @Override
                            public void onSuccess() {
                                gotoAddNewPhone(ChangePhone.this, zoneCode, phoneNum, TYPE_SMS);
                            }

                            @Override
                            public void onFalse() {
                                Toast.makeText(ChangePhone.this, "验证码错误", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onClickLeftView() {

            }

            @Override
            public void onClickRightView() {

            }
        });
    }


    private void refreshNextStepBtnState() {
        String identify = phone_identify.getIdentify();
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
                XHClick.mapStat(ChangePhone.this, TAG_ACCOCUT, "修改手机号", "方法1验证码页，点遇到问题");
                gotoFeedBack();
                break;
            case R.id.tv_phone_missed:
                XHClick.mapStat(ChangePhone.this, TAG_ACCOCUT, "修改手机号", "方法1验证码页，点手机号丢失或停用");
                final XhDialog xhDialog = new XhDialog(ChangePhone.this);
                xhDialog.setTitle("手机号丢失或停用？" + "\n请输入原手机号和密码验证")
                        .setCanselButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                XHClick.mapStat(ChangePhone.this, TAG_ACCOCUT, "修改手机号", "手机号丢失停用弹框，点取消");
                                xhDialog.cancel();
                            }
                        })
                        .setSureButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                XHClick.mapStat(ChangePhone.this, TAG_ACCOCUT, "修改手机号", "手机号丢失停用弹框，点确定");
                                gotoCheckSecret(ChangePhone.this);
                                xhDialog.cancel();
                            }
                        })
                        .setSureButtonTextColor("#007aff")
                        .setCancelButtonTextColor("#007aff");
                xhDialog.show();
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        } else {
            XHClick.mapStat(this, TAG_ACCOCUT, "修改手机号", "方法1验证码页，点返回");
            finish();
        }
    }
}
