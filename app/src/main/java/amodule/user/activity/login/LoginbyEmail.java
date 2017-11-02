package amodule.user.activity.login;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.SecretInputView;

/**
 * Created by ：fei_teng on 2017/3/2 15:06.
 */

public class LoginbyEmail extends BaseLoginActivity implements View.OnClickListener {

    private EditText et_mailbox;
    private SecretInputView ll_secret;
    private NextStepView btn_next_step;
    private ImageView iv_youxinag_del;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_by_email);
        initData();
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
        XHClick.track(this, "浏览登录页");
    }

    private void initView() {
        et_mailbox = (EditText) findViewById(R.id.et_mailbox);
        ll_secret = (SecretInputView) findViewById(R.id.ll_secret);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        iv_youxinag_del = (ImageView) findViewById(R.id.iv_youxinag_del);

        iv_youxinag_del.setOnClickListener(this);

        if (!TextUtils.isEmpty(lastLoginAccout.getMailBox())) {
            et_mailbox.setText(lastLoginAccout.getMailBox());
        }

        et_mailbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_youxinag_del.setVisibility(s.length() > 0?View.VISIBLE:View.GONE);
                onInputDataChanged();
            }
        });

        ll_secret.init("密码", new SecretInputView.SecretInputViewCallback() {
            @Override
            public void onInputSecretChanged() {
                onInputDataChanged();
            }

            @Override
            public void OnClicksecret() {
                XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "点击密码眼睛");
            }
        });

        btn_next_step.init("登录",new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "点击登录");
                loginByEmail();
            }
        });

    }

    private void loginByEmail() {
        if (LoginCheck.checkMailboxValid(this, et_mailbox.getText().toString())) {
            checkEmailRegisted(LoginbyEmail.this, et_mailbox.getText().toString(),
                    new BaseLoginCallback() {
                        @Override
                        public void onSuccess() {
                            loginByAccout(LoginbyEmail.this, EMAIL_LOGIN_TYPE, "",
                                    et_mailbox.getText().toString(),
                                    ll_secret.getPassword(), new BaseLoginCallback() {
                                        @Override
                                        public void onSuccess() {
                                            XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "登录成功");
                                            backToForward();
                                        }

                                        @Override
                                        public void onFalse(int flag) {
                                            XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "登录失败");
                                            XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "失败原因：账号或密码错");
                                        }
                                    });
                        }

                        @Override
                        public void onFalse(int flag) {
                            final DialogManager dialogManager = new DialogManager(LoginbyEmail.this);
                            dialogManager.createDialog(new ViewManager(dialogManager)
                                    .setView(new TitleMessageView(LoginbyEmail.this).setText("该邮箱尚未注册，"+"\n是否注册新账号？"))
                                    .setView(new HButtonView(LoginbyEmail.this)
                                            .setNegativeTextColor(Color.parseColor("#007aff"))
                                            .setNegativeText("不注册", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialogManager.cancel();
                                                    XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "失败原因：弹框未注册，选择不注册");
                                                }
                                            })
                                            .setPositiveTextColor(Color.parseColor("#007aff"))
                                            .setPositiveText("注册", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialogManager.cancel();
                                                    XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "失败原因：弹框未注册，选择注册");
                                                    register(LoginbyEmail.this, "", "");
                                                }
                                            }))).show();
                        }
                    });
        }else{
            XHClick.mapStat(LoginbyEmail.this, PHONE_TAG,"邮箱登录", "失败原因：邮箱格式不对");
        }
    }

    private void onInputDataChanged() {

        boolean canClickNextBtn;

        if (TextUtils.isEmpty(ll_secret.getPassword())) {
            canClickNextBtn = false;
        } else {
            canClickNextBtn = !TextUtils.isEmpty(et_mailbox.getText());
        }
        btn_next_step.setClickCenterable(canClickNextBtn);
    }


    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_youxinag_del:
                et_mailbox.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        XHClick.mapStat(this, PHONE_TAG, "邮箱登录", "点击返回");
    }
}
