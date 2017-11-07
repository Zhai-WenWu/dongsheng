package amodule.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.dish.activity.MenuDish;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import third.push.xg.XGPushServer;
import third.share.activity.ShareImageActivity;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
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
public class MyManagerInfo extends BaseActivity {
    public static final String GrowingIOOrder = "//growingioopen";
    public static final String SHAREIMAGE = "//shareimage";
    public static final String START_MENU = "//startmenu";
    public final int REQUEST_SELECT_IMAGE = 0x1;
    private EditText otherUser_code;

    private List<Map<String, String>> list;
    private String userCode = "";

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
                    if(inputOrder(inputContent)) return;
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
    }

    /**
     * 执行指令
     * @param order
     * @return
     */
    private boolean inputOrder(String order){
        switch(order){
            case GrowingIOOrder:
                String isInputOrder = FileManager.loadShared(MyManagerInfo.this, FileManager.file_appData, FileManager.xmlKey_growingioopen).toString();
                boolean isOpen = "true".equals(isInputOrder);
                FileManager.saveShared(MyManagerInfo.this, FileManager.file_appData, FileManager.xmlKey_growingioopen,  isOpen ? "false" : "true");
                otherUser_code.setText("");
                Tools.showToast(MyManagerInfo.this,isOpen?"GrowingIO随即模式":"GrowingIO强制开启模式");
                return true;
            case SHAREIMAGE:
                startActivityForResult(new Intent(this, ImageSelectorActivity.class)
                                .putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE,ImageSelectorConstant.MODE_SINGLE)
                        ,REQUEST_SELECT_IMAGE);
                otherUser_code.setText("");
                return true;
            case START_MENU:
                startActivity(new Intent(this, MenuDish.class));
                finish();
                otherUser_code.setText("");
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> imageArr = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                    Log.i("tzy","" + imageArr.toString());
                    if(imageArr != null && imageArr.size() > 0)
                        ShareImageActivity.openShareImageActivity(this,imageArr.get(0));
                }
                break;
            default:break;
        }
    }

    //获取马甲户信息
    private void getData() {
        loadManager.showProgressBar();
        String url = StringManager.api_getUserInfo;
        String params = "type=getMajia&devCode=" + XGPushServer.getXGToken(this);
        ReqInternet.in().doPost(url, params, new InternetCallback(this) {
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
}
