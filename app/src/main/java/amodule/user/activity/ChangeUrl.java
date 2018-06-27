package amodule.user.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import aplug.basic.XHInternetCallBack;
import third.location.LocationSys;
import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilFile;

public class ChangeUrl extends BaseActivity {

    private String mInitXHData, mInitMallData, mInitProtocol,
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

    private Switch mRequsetFailTipSwitch, ds_from_switch;

    private EditText mInputEdit;
    private Button mGotoBtn;
    private EditText mInputEdit2;
    private Button mGotoBtn2;

    private Button mClearAddress;
    private TextView mFirstAddressText;
    private TextView mSecondAddressText;
    private String mFirstAddressName;
    private String mFirstAddressId;
    private String mSecondAddressName;
    private String mSecondAddressId;
    private PopupWindow mAddressPoup;
    private RecyclerView mAddressRecy;
    private AddressAdapter mAddressAdapter;
    private int mCurrentAddressLevel;
    private Map<String, String> mFirstAddressMap;
    private Map<String, Map<String, String>> mSecondAddressMap;
    private ArrayList<String> mAddressNames;
    private boolean mAddressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("切换url", 2, 0, R.layout.c_view_bar_title, R.layout.a_core_change_url);
        initView();
        addListener();
        initData();
        loadManager.hideProgressBar();
        if (LoginManager.isLogin() && LoginManager.isManager()) {
            Tools.showToast(XHApplication.in(), ChannelUtil.getChannel(this));
        }
    }

    private void addListener() {

        mFinishTV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean protocolChanged = !TextUtils.equals(mInitProtocol, mProtocol);
                String tempXHData = mProtocol + mXHDomain + mXHPort;
                if (!tempXHData.equals(mInitXHData) || protocolChanged) {//香哈
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
                }
                UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_protocol, mProtocol);
                String tempMallData = mMallDomain + mMallPort;
                if (!tempMallData.equals(mInitMallData) || protocolChanged) {//电商
                    if (!TextUtils.isEmpty(mMallDomain) && mMallTestDomain.equals(mMallDomain) && TextUtils.isEmpty(mMallPort)) {
                        Tools.showToast(ChangeUrl.this, "请填写电商测试端口号");
                        return;
                    }
                    String tempPort = "";
                    if (!TextUtils.isEmpty(mMallPort)) {
                        tempPort = ":" + mMallPort;
                    }
                    String newDomain = mMallDomain + tempPort;
                    MallStringManager.changeUrl(mProtocol, newDomain);
                    UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_mall_domain, newDomain);
                }
                if (!TextUtils.isEmpty(mFirstAddressName) && !TextUtils.isEmpty(mFirstAddressId)) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("firstAddressName", mFirstAddressName);
                        jsonObject.put("firstAddressId", mFirstAddressId);
                        if (!TextUtils.isEmpty(mSecondAddressName) && !TextUtils.isEmpty(mSecondAddressId)) {
                            jsonObject.put("secondAddressName", mSecondAddressName);
                            jsonObject.put("secondAddressId", mSecondAddressId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.location_info, jsonObject.toString());
                    //如果配置了位置信息，则需要保存数据。
                    String append = null;
                    if (!TextUtils.isEmpty(mFirstAddressId)) {
                        append = "#" + mFirstAddressId + (TextUtils.isEmpty(mSecondAddressId) ? "" : ("#" + mSecondAddressId));
                    }
                    FileManager.saveShared(XHApplication.in(), FileManager.file_location, FileManager.file_location, 111.1111 + "#" + 222.222 + (append == null ? "" : append));
                    XHInternetCallBack.setIsCookieChange(true);
                } else {
                    UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.location_info, "");
                    //如果没有配置位置信息，则需要重新定位
                    LocationSys locationSys = new LocationSys(ChangeUrl.this);
                    locationSys.starLocation(new LocationSys.LocationSysCallBack() {
                        @Override
                        public void onLocationFail() {

                        }
                    });
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
        ds_from_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FileManager.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_ds_from_show, isChecked ? "2" : "1");
            }
        });

        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputStr = null;
                switch (v.getId()) {
                    case R.id.goto_btn:
                        inputStr = mInputEdit.getText().toString();
                        if (TextUtils.isEmpty(inputStr)) {
                            Toast.makeText(ChangeUrl.this, "输入内容为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!inputStr.startsWith(AppCommon.XH_PROTOCOL))
                            inputStr = AppCommon.XH_PROTOCOL + inputStr;
                        break;
                    case R.id.goto_btn2:
                        inputStr = mInputEdit2.getText().toString();
                        if (TextUtils.isEmpty(inputStr)) {
                            Toast.makeText(ChangeUrl.this, "输入内容为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final String fixedStr = "UIWebView.app?url=";
                        if (!inputStr.startsWith(fixedStr))
                            inputStr = fixedStr + URLEncoder.encode(inputStr);
                        else {
                            inputStr = fixedStr + URLEncoder.encode(inputStr.substring(fixedStr.length()));
                        }
                        break;
                    case R.id.first_text:
                        if (mAddressLoading) {
                            Toast.makeText(ChangeUrl.this, "正在加载数据，请稍后", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showAddressPopup(1);
                        break;
                    case R.id.second_text:
                        if (mAddressLoading) {
                            Toast.makeText(ChangeUrl.this, "正在加载数据，请稍后", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (TextUtils.isEmpty(mFirstAddressName) && TextUtils.isEmpty(mFirstAddressId)) {
                            Toast.makeText(ChangeUrl.this, "请先选择一级地区", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showAddressPopup(2);
                        break;
                    case R.id.clear_text:
                        mFirstAddressName = "";
                        mFirstAddressId = "";
                        mSecondAddressName = "";
                        mSecondAddressId = "";
                        clearAddress();
                        break;
                }
                AppCommon.openUrl(ChangeUrl.this, inputStr, true);
            }
        };

        mGotoBtn.setOnClickListener(l);
        mGotoBtn2.setOnClickListener(l);

        mClearAddress.setOnClickListener(l);
        mFirstAddressText.setOnClickListener(l);
        mSecondAddressText.setOnClickListener(l);
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
        ds_from_switch = (Switch) findViewById(R.id.ds_from_switch);
        ((TextView) findViewById(R.id.text_channel)).setText("渠道号:  " + ChannelUtil.getChannel(this));

        mInputEdit = (EditText) findViewById(R.id.edit_text);
        mGotoBtn = (Button) findViewById(R.id.goto_btn);
        mInputEdit2 = (EditText) findViewById(R.id.edit_text2);
        mGotoBtn2 = (Button) findViewById(R.id.goto_btn2);

        mClearAddress = (Button) findViewById(R.id.clear_text);
        mFirstAddressText = (TextView) findViewById(R.id.first_text);
        mSecondAddressText = (TextView) findViewById(R.id.second_text);
    }

    private void initData() {
        String protocol = (String) UtilFile.loadShared(ChangeUrl.this, FileManager.xmlFile_appInfo, FileManager.xmlKey_protocol);
        if (!TextUtils.isEmpty(protocol)) {
            mInitProtocol = protocol;
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
        //电商来源提示
        String ds_form = FileManager.loadShared(this, FileManager.xmlFile_appInfo, FileManager.xmlKey_ds_from_show).toString();
        ds_from_switch.setChecked("2".equals(ds_form));

        String locationInfo = FileManager.loadShared(this, FileManager.xmlFile_appInfo, FileManager.location_info).toString();
        Map<String, String> locationMap = StringManager.getFirstMap(locationInfo);
        String firstAddressName = locationMap.get("firstAddressName");
        String firstAddressId = locationMap.get("firstAddressId");
        mFirstAddressId = TextUtils.isEmpty(firstAddressId) ? "" : firstAddressId;
        String secondAddressName = locationMap.get("secondAddressName");
        String secondAddressId = locationMap.get("secondAddressId");
        mSecondAddressId = TextUtils.isEmpty(secondAddressId) ? "" : secondAddressId;
        mFirstAddressName = TextUtils.isEmpty(firstAddressName) ? "" : firstAddressName;
        mSecondAddressName = TextUtils.isEmpty(secondAddressName) ? "" : secondAddressName;
        setFirstAddress(mFirstAddressName, mFirstAddressId);
        setSecondAddress(mSecondAddressName, mSecondAddressId);
        loadAddressData();
    }

    private void loadAddressData() {
        mAddressLoading = true;
        mFirstAddressMap = new HashMap<>();
        mSecondAddressMap = new HashMap<>();
        mAddressNames = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String cityJson = FileManager.getFromAssets(XHApplication.in(), "city.json");
                ArrayList<Map<String, String>> cityMaps = StringManager.getListMapByJson(cityJson);
                for (Map<String, String> cityMap : cityMaps){
                    Set<Map.Entry<String, String>> entries = cityMap.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        try {
                            int id = Integer.parseInt(entry.getKey());
                            Map<String, String> secondMap = StringManager.getFirstMap(entry.getValue());
                            mSecondAddressMap.put(String.valueOf(id), secondMap);
                        } catch (Exception e) {
                            mFirstAddressMap.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                onAddressDataReady();
            }
        }).start();
    }

    private void onAddressDataReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddressLoading = false;
            }
        });
    }

    private void setFirstAddress(String addressName, String addressId) {
        mFirstAddressText.setText(TextUtils.isEmpty(addressName) ? "" : (addressName + "(" + addressId + ")"));
    }

    private void setSecondAddress(String addressName, String addressId) {
        mSecondAddressText.setText(TextUtils.isEmpty(addressName) ? "" : (addressName + "(" + addressId + ")"));
    }

    private  void clearAddress() {
        setFirstAddress("", "");
        setSecondAddress("", "");
    }

    @Override
    public void onBackPressed() {
        if (mAddressPoup != null && mAddressPoup.isShowing()) {
            mAddressPoup.dismiss();
            return;
        }
        super.onBackPressed();
    }

    /**
     * 展示地址列表
     * @param addressLevel 1.一级地区 2.二级地区
     */
    private void showAddressPopup(int addressLevel) {
        if (mAddressRecy == null) {
            mAddressRecy = new RvListView(this);
            mAddressRecy.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        if (mAddressAdapter == null) {
            mAddressAdapter = new AddressAdapter(this, mAddressNames);
            mAddressRecy.setAdapter(mAddressAdapter);
        }
        if (mAddressPoup == null) {
            mAddressPoup = new PopupWindow(this);
            mAddressPoup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
            mAddressPoup.setHeight(getWindowManager().getDefaultDisplay().getHeight() / 2);
            mAddressPoup.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.myself_gray_bg)));
            mAddressPoup.setContentView(mAddressRecy);
            mAddressPoup.setOutsideTouchable(true);
        }

        if (mAddressPoup.isShowing()) {
            return;
        }
        mCurrentAddressLevel = addressLevel;
        mAddressNames.clear();
        switch (addressLevel) {
            case 1:
                for (String name : mFirstAddressMap.keySet()) {
                    mAddressNames.add(name);
                }
                break;
            case 2:
                for (String name : mSecondAddressMap.get(mFirstAddressId).keySet()) {
                    mAddressNames.add(name);
                }
                break;
        }
        mAddressAdapter.setData(mAddressNames);
        mAddressPoup.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        mAddressAdapter.notifyDataSetChanged();
    }

    class AddressAdapter extends RvBaseAdapter<String> {

        public AddressAdapter(Context context, @Nullable List<String> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<String> onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.dp_10), 0, getResources().getDimensionPixelSize(R.dimen.dp_10));
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER);
            return new AddressHolder(textView);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

    class AddressHolder extends RvBaseViewHolder<String> implements OnClickListener {

        private TextView mTextView;
        private String mData;
        public AddressHolder(@NonNull TextView itemView) {
            super(itemView);
            mTextView = itemView;
            mTextView.setOnClickListener(this);
        }

        @Override
        public void bindData(int position, @Nullable String data) {
            mData = data;
            mTextView.setText(data);
        }

        @Override
        public void onClick(View v) {
            switch (mCurrentAddressLevel) {
                case 1:
                    mFirstAddressName = this.mData;
                    mFirstAddressId = mFirstAddressMap.get(mFirstAddressName);
                    mSecondAddressName = "";
                    mSecondAddressId = "";
                    mAddressPoup.dismiss();
                    setFirstAddress(mFirstAddressName, mFirstAddressId);
                    setSecondAddress(mSecondAddressName, mSecondAddressId);
                    break;
                case 2:
                    mSecondAddressName = this.mData;
                    mSecondAddressId = mSecondAddressMap.get(mFirstAddressId).get(mSecondAddressName);
                    mAddressPoup.dismiss();
                    setSecondAddress(mSecondAddressName, mSecondAddressId);
                    break;
            }
        }
    }
}
