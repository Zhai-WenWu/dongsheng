package amodule.user.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.xianghatest.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilFile;

public class ChangeUrl extends BaseActivity {

    private String mInitXHData, mInitMallData,
            mProtocol = StringManager.defaultProtocol,
            mXHDomain = StringManager.defaultDomain, mXHPort,
            mMallDefDomain = ".ds.xiangha.com",
            mMallDomain = mMallDefDomain, mMallPort,
            mXHTestDomain = ".ixiangha.com", mMallTestDomain = ".ds.mamaweiyang.net";

    private TextView mFinishTV;
    private RadioGroup mProtocolGroup;
    private RadioButton mProtocolBtn1;
    private RadioButton mProtocolBtn2;

    private EditText mXHPortEt;
    private Switch mXHSwitchBtn;

    private EditText mMallPortEt;
    private Switch mMallSwitchBtn;

    private Switch mRequsetFailTipSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("切换url", 2, 0, R.layout.c_view_bar_title, R.layout.a_core_change_url);
        initView();
        initData();
        addListener();
        loadManager.hideProgressBar();
    }

    private void addListener() {

        mFinishTV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String tempXHData = mProtocol + mXHDomain + mXHPort;
                if (!tempXHData.equals(mInitXHData)) {//香哈
                    if (!TextUtils.isEmpty(mXHDomain) && mXHTestDomain.equals(mXHDomain) && TextUtils.isEmpty(mXHPort)) {
                        Tools.showToast(ChangeUrl.this, "请填写香哈测试端口号");
                        return;
                    }
                    String tempPort = "";
                    if (!TextUtils.isEmpty(mXHPort)) {
                        tempPort = ":" + mXHPort;
                    }
                    String newDomain = mXHDomain + tempPort;
                    StringManager.changeUrl(mProtocol, newDomain);
                    UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_domain, newDomain);
                    UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_protocol, mProtocol);
                }
                String tempMallData = mMallDomain + mMallPort;
                if (!tempMallData.equals(mInitMallData)) {//电商
                    if (!TextUtils.isEmpty(mMallDomain) && mMallTestDomain.equals(mMallDomain) && TextUtils.isEmpty(mMallPort)) {
                        Tools.showToast(ChangeUrl.this, "请填写电商测试端口号");
                        return;
                    }
                    String tempPort = "";
                    if (!TextUtils.isEmpty(mMallPort)) {
                        tempPort = ":" + mMallPort;
                    }
                    String newDomain = mMallDomain + tempPort;
                    MallStringManager.changeUrl(newDomain);
                    UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_mall_domain, newDomain);
                }
                ChangeUrl.this.finish();
            }
        });

        mProtocolGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup rg, int radioButtonId) {
                switch (radioButtonId) {
                    case R.id.protocol1:
                        mProtocol = "https://";
                        break;
                    case R.id.protocol2:
                        mProtocol = "http://";
                        break;
                }
            }
        });

        CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.xh_switch:
                        if (isChecked) {
                            mXHDomain = mXHTestDomain;
                            mXHPort = mXHPortEt.getText().toString().trim();
                        } else {
                            mXHDomain = StringManager.defaultDomain;
                            mXHPort = "";
                        }
                        break;
                    case R.id.mall_switch:
                        if (isChecked) {
                            mMallDomain = mMallTestDomain;
                            mMallPort = mMallPortEt.getText().toString().trim();
                        } else {
                            mMallDomain = mMallDefDomain;
                            mMallPort = "";
                        }
                        break;
                }
            }
        };

        mXHSwitchBtn.setOnCheckedChangeListener(changeListener);
        mMallSwitchBtn.setOnCheckedChangeListener(changeListener);

        mXHPortEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mXHPort = s.toString();
                if (!mXHSwitchBtn.isChecked() && !TextUtils.isEmpty(mXHPort)) {
                    mXHSwitchBtn.setChecked(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mMallPortEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMallPort = s.toString();
                if (!mMallSwitchBtn.isChecked() && !TextUtils.isEmpty(mMallPort)) {
                    mMallSwitchBtn.setChecked(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mRequsetFailTipSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FileManager.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_request_tip, isChecked ? "2" : "1");
            }
        });
    }

    private void initView() {
        mFinishTV = (TextView) findViewById(R.id.rightText);
        mFinishTV.setVisibility(View.VISIBLE);
        mFinishTV.setText("完成");
        mProtocolGroup = (RadioGroup) findViewById(R.id.rg_protocol);
        mProtocolBtn1 = (RadioButton) findViewById(R.id.protocol1);
        mProtocolBtn2 = (RadioButton) findViewById(R.id.protocol2);
        mXHPortEt = (EditText) findViewById(R.id.xh_et);
        mMallPortEt = (EditText) findViewById(R.id.mall_et);
        mXHSwitchBtn = (Switch) findViewById(R.id.xh_switch);
        mMallSwitchBtn = (Switch) findViewById(R.id.mall_switch);
        mRequsetFailTipSwitch = (Switch) findViewById(R.id.request_fail_tip_switch);
    }

    private void initData() {
        String protocol = (String) UtilFile.loadShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_protocol);
        if (!TextUtils.isEmpty(protocol)) {
            mProtocol = protocol;
            if (protocol.contains("http://"))
                mProtocolBtn2.setChecked(true);
            else
                mProtocolBtn1.setChecked(true);
        } else
            mProtocolBtn1.setChecked(true);
        String xhDomain = (String) UtilFile.loadShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_domain);
        if (!TextUtils.isEmpty(xhDomain)) {
            if (xhDomain.contains(mXHTestDomain + ":")) {
                String str[] = xhDomain.split(":");
                String domain = str[0];
                if (!TextUtils.isEmpty(domain))
                    mXHDomain = domain;
                String port = str[1];
                if (!TextUtils.isEmpty(port)) {
                    mXHPort = port;
                    mXHPortEt.setText(port);
                    mXHSwitchBtn.setChecked(true);
                }
            } else {
                mXHSwitchBtn.setChecked(false);
            }

        } else {
            mXHSwitchBtn.setChecked(false);
        }
        mInitXHData = mProtocol + mXHDomain + mXHPort;
        String mallDomain = (String) UtilFile.loadShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_mall_domain);
        if (!TextUtils.isEmpty(mallDomain)) {
            if (mallDomain.contains(mMallTestDomain + ":")) {
                String str[] = mallDomain.split(":");
                String domain = str[0];
                if (!TextUtils.isEmpty(domain))
                    mMallDomain = domain;
                String port = str[1];
                if (!TextUtils.isEmpty(port)) {
                    mMallPort = port;
                    mMallPortEt.setText(port);
                    mMallSwitchBtn.setChecked(true);
                }
            } else {
                mMallSwitchBtn.setChecked(false);
            }

        } else {
            mMallSwitchBtn.setChecked(false);
        }
        mInitMallData = mMallDomain + mMallPort;
        //初始化是否开启请求返回值提示开关
        mRequsetFailTipSwitch.setChecked(Tools.isOpenRequestTip(this));

    }
}
