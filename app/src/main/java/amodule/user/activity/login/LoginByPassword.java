package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.logic.login.LoginCheck;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.view.NextStepView;
import amodule.user.view.PhoneNumInputView;
import amodule.user.view.SecretInputView;
import third.share.ShareTools;
import xh.windowview.XhDialog;

/**
 * 通过账号密码登录
 * Created by Fang Ruijiao on 2017/8/14.
 */
public class LoginByPassword extends ThirdLoginBaseActivity implements View.OnClickListener{
    private PhoneNumInputView phone_info;
    private SecretInputView ll_secret;
    private NextStepView btn_next_step;
    private TextView tvRegister;
    private TextView tvIdentify;
    private TextView tvLostsercet,tv_agreenment;
    private ImageView imageQq, imageWeixin, imageWeibo, imageMailbox;
    private ImageView top_left_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_login_by_accout);
        initView();
        initTitle();
        ToolsDevice.modifyStateTextColor(this);
        XHClick.track(this, "浏览登录页");
    }

    private void initView() {
        top_left_view = (ImageView) findViewById(R.id.top_left_view);
        phone_info = (PhoneNumInputView) findViewById(R.id.phone_info);
        ll_secret = (SecretInputView) findViewById(R.id.ll_secret);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        tvRegister = (TextView) findViewById(R.id.tv_register);
        tvIdentify = (TextView) findViewById(R.id.tv_identify);
        tvLostsercet = (TextView) findViewById(R.id.tv_lostsercet);
        tv_agreenment = (TextView) findViewById(R.id.tv_agreenment);

        imageQq = (ImageView) findViewById(R.id.iv_qq);
        imageWeixin = (ImageView) findViewById(R.id.iv_weixin);
        imageWeibo = (ImageView) findViewById(R.id.iv_weibo);
        imageMailbox = (ImageView) findViewById(R.id.iv_mailbox);

        top_left_view.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvIdentify.setOnClickListener(this);
        tvLostsercet.setOnClickListener(this);
        tv_agreenment.setOnClickListener(this);
        imageMailbox.setOnClickListener(this);
        imageWeixin.setOnClickListener(this);
        imageQq.setOnClickListener(this);
        imageWeibo.setOnClickListener(this);

        Intent intent = getIntent();
        String zone_code = intent.getStringExtra(ZONE_CODE);
        String phone_num = intent.getStringExtra(PHONE_NUM);

        if (TextUtils.isEmpty(zone_code) || TextUtils.isEmpty(phone_num)) {
            zone_code = lastLoginAccout.getAreaCode();
            phone_num = lastLoginAccout.getPhoneNum();
        }

        phone_info.init("手机号", zone_code, phone_num,
                new PhoneNumInputView.PhoneNumInputViewCallback() {
                    @Override
                    public void onZoneCodeClick() {
                        XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "点击国家代码");
                        Intent intent = new Intent(LoginByPassword.this, CountryListActivity.class);
                        startActivityForResult(intent, mGetCountryId);
                    }

                    @Override
                    public void onPhoneInfoChanged() {
                        onInputDataChanged();
                    }
                });

        ll_secret.init("密码", new SecretInputView.SecretInputViewCallback() {
            @Override
            public void onInputSecretChanged() {
                onInputDataChanged();
            }

            //密码是否显示
            @Override
            public void OnClicksecret() {
                XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "点击密码验证");
            }
        });

        btn_next_step.init("登录", new NextStepView.NextStepViewCallback() {
            @Override
            public void onClickCenterBtn() {
                XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "点击登录");
                gotoLogin();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register: //注册
                register(this,phone_info.getZoneCode(),phone_info.getPhoneNum());
                break;
            case R.id.tv_identify: //验证码登录
                gotoLoginByIndetify(this,phone_info.getZoneCode(),phone_info.getPhoneNum());
                break;
            case R.id.tv_lostsercet: //找回密码
                startActivity(new Intent(this, LostSecret.class));
                break;
            case R.id.tv_agreenment: //香哈协议
                XHClick.mapStat(this, PHONE_TAG, "注册", "手机号页，点香哈协议");
                AppCommon.openUrl(mAct, StringManager.api_agreementXiangha, true);
                break;
            case R.id.iv_weixin:
                int number = ToolsDevice.isAppInPhone(this, "com.tencent.mm");
                if (number == 0)
                    Tools.showToast(this, "需安装微信客户端才可以登录");
                else
                    thirdAuth(this, ShareTools.WEI_XIN, "微信");
                break;
            case R.id.iv_weibo:
                thirdAuth(this, ShareTools.SINA_NAME, "新浪");
                break;
            case R.id.iv_qq:
                thirdAuth(this, ShareTools.QQ_NAME, "QQ");
                break;
            case R.id.top_left_view:
                XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "点击返回", "");
                backToForward();
                break;
            case R.id.iv_mailbox:
                gotoLoginByEmail(this);
                break;
            default:
                break;
        }
    }

    private void onInputDataChanged() {
        boolean canClickNextBtn = !TextUtils.isEmpty(ll_secret.getPassword()) && !phone_info.isDataAbsence();
        btn_next_step.setClickCenterable(canClickNextBtn);
    }

    private void gotoLogin() {
        String errorType = LoginCheck.checkPhoneFormatWell(this, phone_info.getZoneCode(), phone_info.getPhoneNum());
        if (LoginCheck.WELL_TYPE.equals(errorType)) {
            checkPhoneRegisted(LoginByPassword.this, phone_info.getZoneCode(), phone_info.getPhoneNum(),
                    new BaseLoginActivity.BaseLoginCallback() {
                        @Override
                        public void onSuccess() {
                            loginByAccout(LoginByPassword.this, PHONE_LOGIN_TYPE, phone_info.getZoneCode(),
                                    phone_info.getPhoneNum(),
                                    ll_secret.getPassword(), new BaseLoginActivity.BaseLoginCallback() {
                                        @Override
                                        public void onSuccess() {
                                            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "登录成功");
                                            backToForward();
                                        }

                                        @Override
                                        public void onFalse(int flag) {
                                            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "登录失败");
                                            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "失败原因：账号或密码错");
                                        }
                                    });
                        }

                        @Override
                        public void onFalse(int flag) {
//
                            final XhDialog xhDialog = new XhDialog(LoginByPassword.this);
                            xhDialog.setTitle("网络有问题或手机号未注册")
                                    .setCanselButton("取消", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录",
                                                    "失败原因：弹框未注册，选择不注册");
                                            xhDialog.cancel();
                                        }
                                    })
                                    .setSureButton("立即注册", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            register(LoginByPassword.this, phone_info.getZoneCode(),
                                                    phone_info.getPhoneNum());
                                            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录",
                                                    "失败原因：弹框未注册，选择注册");
                                            xhDialog.cancel();
                                        }
                                    }).setSureButtonTextColor("#007aff")
                                    .setCancelButtonTextColor("#007aff");
                            xhDialog.show();
                        }
                    });
        } else if (LoginCheck.NOT_11_NUM.equals(errorType)) {
            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "失败原因：手机号不是11位");
        } else if (LoginCheck.ERROR_FORMAT.equals(errorType)) {
            XHClick.mapStat(LoginByPassword.this, PHONE_TAG, "手机号登录", "失败原因：手机号格式错误");
        }
    }

    @Override
    protected void onCountrySelected(String country_code) {
        super.onCountrySelected(country_code);
        phone_info.setZoneCode("+" + country_code);
    }
}
