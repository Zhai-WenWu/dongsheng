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
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.SecretInputView;
import xh.windowview.XhDialog;

/**
 * Created by ：fei_teng on 2017/2/20 18:12.
 */

public class SetSecretActivity extends BaseLoginActivity {

    private SecretInputView user_set_secret;
    private NextStepView btn_next_step;
    private String path_from;
    private TextView tv_secret_title;
    private String zoneCode;
    private String phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_set_secret);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
    }


    private void initData() {
        Intent intent = getIntent();
        path_from = intent.getStringExtra(PATH_ORIGIN);
        zoneCode = intent.getStringExtra(ZONE_CODE);
        phoneNum = intent.getStringExtra(PHONE_NUM);
    }

    private void initView() {
        user_set_secret = (SecretInputView) findViewById(R.id.user_set_secret);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tv_secret_title = (TextView) findViewById(R.id.tv_secret_title);

        if (ORIGIN_REGISTER.equals(path_from) || ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
            tv_secret_title.setText("请设置密码");
        } else if (ORIGIN_FIND_PSW.equals(path_from)) {
            tv_secret_title.setText("设置新密码");
        }

        user_set_secret.init("6-20位，字母、数字或字符", new SecretInputView.SecretInputViewCallback() {
            @Override
            public void onInputSecretChanged() {
                if (!TextUtils.isEmpty(user_set_secret.getPassword())) {
                    btn_next_step.setClickCenterable(true);
                } else {
                    btn_next_step.setClickCenterable(false);
                }
            }

            @Override
            public void OnClicksecret() {
                if (ORIGIN_FIND_PSW.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "忘记密码", "设置新密码页，点密码眼睛");
                } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "绑定手机号", "设置密码页，点密码眼睛");
                } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "修改密码", "设置密码页，点密码眼睛");
                }else if(ORIGIN_REGISTER.equals(path_from)){
                    XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "注册", "设置密码页，点密码眼睛");
                }

            }
        });
        user_set_secret.showSecret();

        btn_next_step.init("确定", "", "", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {

                if (ORIGIN_FIND_PSW.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "忘记密码", "设置新密码页，点完成");
                } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "绑定手机号", "设置密码页，点完成");
                } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "修改密码", "设置新密码页，点完成");
                }else if(ORIGIN_REGISTER.equals(path_from)){
                    XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "注册", "设置密码页，点击确定");
                }

                boolean isFormatWell = LoginCheck.isSecretFormated(user_set_secret.getPassword());
                if (!isFormatWell) {
                    Toast.makeText(SetSecretActivity.this, "密码为6-20位字母、数字或字符", Toast.LENGTH_SHORT).show();

                    if (ORIGIN_FIND_PSW.equals(path_from)) {
                        XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "忘记密码", "失败原因：密码不符合要求");
                    } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                        XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "绑定手机号", "失败原因：密码不符合要求");
                    } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                        XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "修改密码", "失败原因：密码不符合要求");
                    }else if(ORIGIN_REGISTER.equals(path_from)){
                        XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "注册", "失败原因：密码不符合要");
                    }
                    return;
                } else {
                    modifySecret(SetSecretActivity.this, zoneCode, phoneNum,
                            user_set_secret.getPassword(), new BaseLoginCallback() {
                                @Override
                                public void onSuccess() {

                                    if (ORIGIN_FIND_PSW.equals(path_from)) {
                                        loginByAccout(SetSecretActivity.this, "", zoneCode, phoneNum,
                                                user_set_secret.getPassword(), new BaseLoginCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "忘记密码", "登录成功");
                                                        backToForward();
                                                    }

                                                    @Override
                                                    public void onFalse() {
                                                        XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "忘记密码", "登录失败");
                                                    }
                                                });
                                    } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                                        Toast.makeText(SetSecretActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                                        backToForward();
                                    } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                                        Toast.makeText(SetSecretActivity.this, "修改密码成功", Toast.LENGTH_SHORT).show();
                                        XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "修改密码", "修改成功");
                                        backToForward();
                                    } else if (ORIGIN_REGISTER.equals(path_from)) {
                                        gotoSetPersonInfo(SetSecretActivity.this, ORIGIN_REGISTER);
                                    }else if(ORIGIN_BIND_FROM_WEB.equals(path_from)){
                                        Toast.makeText(SetSecretActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                                        backToForward();
                                    }
                                }

                                @Override
                                public void onFalse() {

                                    if (ORIGIN_FIND_PSW.equals(path_from)) {
                                        XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "忘记密码", "失败原因：验证码错误");
                                    }

                                    if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                                        Toast.makeText(SetSecretActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
                                    } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                                        Toast.makeText(SetSecretActivity.this, "修改密码失败", Toast.LENGTH_SHORT).show();
                                    } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                                        XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "修改密码", "修改失败");
                                    }else if(ORIGIN_BIND_FROM_WEB.equals(path_from)){
                                    } else {
                                        Toast.makeText(SetSecretActivity.this, "设置密码失败", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
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


    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        } else {


            if (ORIGIN_FIND_PSW.equals(path_from)) {
                XHClick.mapStat(this, PHONE_TAG, "忘记密码", "设置新密码页，点返回");
            } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "绑定手机号", "设置密码页，点返回");
            } else if (ORIGIN_MODIFY_PSW.equals(path_from)) {
                XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "修改密码", "设置密码页，点返回");
            }else if(ORIGIN_REGISTER.equals(path_from)){
                XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "注册", "设置密码页，点返回");
            }


            if (ORIGIN_REGISTER.equals(path_from) || ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                showDialog();
            } else {
                finish();
            }


        }
    }

    private void showDialog() {

        String title = "";
        String cancelText = "";
        String sureText = "";
        if (ORIGIN_REGISTER.equals(path_from)) {
            title = "注册成功,请设置密码";
            cancelText = "直接登录";
            sureText = "设置密码";
        } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
            title = "绑定成功，请设置密码？";
            cancelText = "下次再说";
            sureText = "设置密码";
        }

        final XhDialog xhDialog = new XhDialog(this);
        xhDialog.setTitle(title);
        xhDialog.setSureButton(sureText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhDialog.cancel();

                if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "绑定手机号", "弹框设置密码，选择设置密码");
                } else if(ORIGIN_REGISTER.equals(path_from)){
                    XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "注册", "弹框设置密码，选择设置密码");
                }
            }
        });
        xhDialog.setCanselButton(cancelText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhDialog.cancel();
                if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                    Toast.makeText(SetSecretActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                } else if (ORIGIN_BIND_PHONE_NUM.equals(path_from)) {
                    XHClick.mapStat(SetSecretActivity.this, TAG_ACCOCUT, "绑定手机号", "弹框设置密码，选择下次再说");
                } else if(ORIGIN_REGISTER.equals(path_from)){
                    XHClick.mapStat(SetSecretActivity.this, PHONE_TAG, "注册", "弹框设置密码，选择直接登录");
                }
                backToForward();
            }
        })
                .setSureButtonTextColor("#007aff")
                .setCancelButtonTextColor("#007aff").show();
    }
}
