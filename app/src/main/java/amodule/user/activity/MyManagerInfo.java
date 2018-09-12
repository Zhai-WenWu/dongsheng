package amodule.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.SpecialOrder;
import acore.override.XHApplication;
import acore.tools.FileManager;
import aplug.basic.ReqEncyptInternet;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xiangha.R;

/**
 * MyManagerInfo
 * Copyright: Copyright (c) 2014~2017
 *
 * @author ruijiao_fang
 * @date 2014年11月28日
 */
public class MyManagerInfo extends BaseActivity implements OnClickListener{

    private EditText otherUser_code;

    private List<Map<String, String>> list;
    private String userCode = "";
    private TextView tv_module_state,tv_module_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("马甲账号", 2, 0, R.layout.c_view_bar_title, R.layout.a_my_manager);
        list = new ArrayList<>();
        getData();
    }

    @Override
    public void finish() {
        if (userCode != null && userCode.length() > 1) {
            //修改userCode
            LoginManager.modifyUserInfo(MyManagerInfo.this, "userCode", userCode);
            //自动登录
            LoginManager.loginByAuto(MyManagerInfo.this);
        }
        super.finish();
    }

    private void initUI() {
        TableLayout table = (TableLayout) findViewById(R.id.tl_manager);
        AdapterSimple adapter = new AdapterSimple(table, list,
                R.layout.a_my_item_manager,
                new String[]{"nickName", "mns"},
                new int[]{R.id.tv_name, R.id.tv_infoNumber});
        SetDataView.view(table, 3, adapter, new int[]{R.id.tv_name, R.id.tv_infoNumber},
                new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
                    @Override
                    public void click(int index, View view) {
                        Map<String, String> userMap = list.get(index);
                        userCode = userMap.get("userCode");
                        loadManager.showProgressBar();
                        //退出登录
                        LoginManager.logout(MyManagerInfo.this);
                    }
                }});
        otherUser_code = (EditText) findViewById(R.id.otherUser_code);
        Button otherUser_login = (Button) findViewById(R.id.otherUser_login);
        otherUser_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otherUser_code.getText().toString() != null && otherUser_code.getText().toString().length() > 0) {
                    String inputContent = otherUser_code.getText().toString();
                    //检测是否是指令
                    if(SpecialOrder.of().handlerOrder(MyManagerInfo.this,inputContent)){
                        otherUser_code.setText("");
                        return;
                    }
                    userCode = inputContent;
                    loadManager.showProgressBar();
                    //退出登录
                    LoginManager.logout(MyManagerInfo.this);
                } else {
                    Toast.makeText(getApplicationContext(), "请输入UserCode", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.manager_wrapper).setVisibility(View.VISIBLE);
        findViewById(R.id.module_start_linear).setOnClickListener(this);
        findViewById(R.id.module_exit_linear).setOnClickListener(this);
        tv_module_state = findViewById(R.id.tv_module_state);
        tv_module_num = findViewById(R.id.tv_module_num);
        String key= (String) FileManager.loadShared(XHApplication.in(),FileManager.key_header_mode,FileManager.key_header_mode);
        setModuleChange(!TextUtils.isEmpty(key));
        setRequest();

    }


    //获取马甲户信息
    private void getData() {
        loadManager.showProgressBar();
        String url = StringManager.api_getUserInfo;
        String params = "type=getMajia&devCode=" + XGPushServer.getXGToken(this);
        ReqInternet.in().doPost(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    // 解析数据
                    Map<String, String> map = UtilString.getListMapByJson(returnObj).get(0);
                    list = UtilString.getListMapByJson(map.get("majia"));
                    for (int i = 0; i < list.size(); i++) {
                        map = list.get(i);
                        if (map.get("mns").equals("0"))
                            map.put("mns", "");
                    }
                    initUI();
                }
                loadManager.hideProgressBar();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.module_exit_linear:
                FileManager.saveShared(XHApplication.in(),FileManager.key_header_mode,FileManager.key_header_mode,"");
                setModuleChange(false);
                break;
            case R.id.module_start_linear:
                FileManager.saveShared(XHApplication.in(),FileManager.key_header_mode,FileManager.key_header_mode,"comment");
                setRequest();
                setModuleChange(true);
                break;
        }
    }
    public void setRequest(){
        ReqEncyptInternet.in().doGetEncypt(StringManager.API_FORUM_GETCOMMENTMODENUMBYDATE,"", new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                Log.i("xianghaTag","flag::"+flag+"::msg::"+msg);
                if(flag>=ReqInternet.REQ_OK_STRING){
                    Log.i("xianghaTag","msg::"+msg);
                    if(TextUtils.isEmpty((CharSequence) msg)||"[]".equals(String.valueOf(msg))) {
                        tv_module_num.setText("0");
                    }else{
                        tv_module_num.setText("今日"+String.valueOf(msg));
                    }
                }
            }
        });
    }
    private void setModuleChange(boolean isForum){
        if(isForum){//当前评论模式
            findViewById(R.id.module_start_linear).setBackgroundResource(R.drawable.bg_circle_blue_6);
            findViewById(R.id.module_exit_linear).setBackgroundResource(R.drawable.bg_circle_white_6);
            tv_module_state.setTextColor(Color.parseColor("#fffffe"));
            tv_module_num.setTextColor(Color.parseColor("#fffffe"));
            ((TextView)findViewById(R.id.tv_normal)).setTextColor(Color.parseColor("#333333"));
        }else{//非评论模式
            findViewById(R.id.module_start_linear).setBackgroundResource(R.drawable.bg_circle_white_6);
            findViewById(R.id.module_exit_linear).setBackgroundResource(R.drawable.bg_circle_blue_6);
            tv_module_state.setTextColor(Color.parseColor("#333333"));
            tv_module_num.setTextColor(Color.parseColor("#333333"));
            ((TextView)findViewById(R.id.tv_normal)).setTextColor(Color.parseColor("#fffffe"));
        }
    }
}
