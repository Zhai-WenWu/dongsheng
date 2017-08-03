package amodule.user.activity.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import amodule.user.view.NextStepView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.datepicker.BarDatePicker;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Created by ：fei_teng on 2017/2/20 22:08.
 */

public class SetPersonalInfo extends BaseLoginActivity implements View.OnClickListener {

    private ImageView iv_setting_img;
    private EditText et_nickname;
    private TextView tv_msg_tip;
    private TextView tv_birthday;
    private TextView tv_gender;
    private NextStepView btn_next_step;
    private BarDatePicker date_picker;
    String[] sex_items;
    ArrayList<Map<String, String>> sex_list;
    private String nickName;
    private String sex;
    private String birthday;
    private String loginType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActivity("", 4, 0, 0, R.layout.a_login_set_personal_info);
        initData();
        initView();
    }

    private void initView() {
        iv_setting_img = (ImageView) findViewById(R.id.iv_setting_img);
        et_nickname = (EditText) findViewById(R.id.et_nickname);
        tv_gender = (TextView) findViewById(R.id.tv_gender);
        tv_birthday = (TextView) findViewById(R.id.tv_birthday);
        tv_msg_tip = (TextView) findViewById(R.id.tv_msg_tip);
        btn_next_step = (NextStepView) findViewById(R.id.btn_next_step);
        date_picker = (BarDatePicker) findViewById(R.id.setting_date_picker);
        date_picker.setDate("1985年1月1日");

        setHeaderView();
        iv_setting_img.setOnClickListener(this);
        tv_gender.setOnClickListener(this);
        tv_birthday.setOnClickListener(this);
        et_nickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    saveNickName();
                }
            }
        });

        tv_gender.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    saveUserData("sex", tv_gender.getText().toString());
                }
            }
        });

        tv_birthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                }

            }
        });
        btn_next_step.init("进入香哈",
                new NextStepView.NextStepViewCallback() {
                    @Override
                    public void onClickCenterBtn() {
                        dataStatistics("点击进入香哈");
                        backToForward();
                    }
                });

        btn_next_step.setClickCenterable(true);
    }

    private void setHeaderView() {

        Glide.with(SetPersonalInfo.this).load(R.drawable.a_login_personaldata_head).asBitmap().centerCrop()
                .into(new BitmapImageViewTarget(iv_setting_img) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(SetPersonalInfo.this.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        iv_setting_img.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_setting_img:
                modifyUserImg();
                break;
            case R.id.tv_gender:
                showGenderSelect();
                break;
            case R.id.tv_birthday:
                showBirthdayOption();
                break;
            default:
                break;

        }


    }

    //修改头像
    private void modifyUserImg() {
        Intent intent = new Intent();
        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_SINGLE);
        intent.setClass(this, ImageSelectorActivity.class);
        startActivityForResult(intent, SET_USER_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SET_USER_IMG
                && data != null) {
            ArrayList<String> array = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);//ArrayList<String>
            if (array.size() > 0) {
                String imgUrl = array.get(0);
                if (!TextUtils.isEmpty(imgUrl)) {
                    showCover(imgUrl);
                    uploadImg(imgUrl);
                } else {
                    Tools.showToast(this, "选择图片有误，请重新选择");
                }
            }
        }
    }


    private void showCover(String imgUrl) {
        Glide.with(SetPersonalInfo.this).load(imgUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(iv_setting_img) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(SetPersonalInfo.this.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                iv_setting_img.setImageDrawable(circularBitmapDrawable);
            }
        });
    }


    private void uploadImg(String imgUrl) {
        LinkedHashMap<String, String> fileMap = new LinkedHashMap<String, String>();
        fileMap.put("type", "img");
        fileMap.put("uploadImg_file_1", imgUrl);
        Tools.showToast(getApplicationContext(), "图片正在上传,请稍等!");
        dataStatistics("完善资料页，上传头像");
        ReqInternet.in().doPostImg(StringManager.api_setUserData, fileMap, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {

                }
            }
        });
    }

    // 保存nickname
    private void saveNickName() {
        String nickname = et_nickname.getText().toString();

        dataStatistics("完善资料页，修改昵称");
        if (TextUtils.isEmpty(nickname)) {

        } else if (nickname.length() > 15) {
            tv_msg_tip.setText("中英文均可，不能超过15个汉字或字符");
            tv_msg_tip.setTextColor(Color.parseColor("#ff533c"));
            btn_next_step.setClickCenterable(false);
        } else {
            String params = "type=nickName&p1=" + nickname;
            ReqInternet.in().doPost(StringManager.api_setUserData, params, new InternetCallback(this) {
                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    if (flag >= UtilInternet.REQ_OK_STRING) {
                        onMotifyNickName(true);
                    } else {
                        Tools.showToast(SetPersonalInfo.this, returnObj.toString());
                        onMotifyNickName(false);
                    }

                }
            });
        }
    }


    private void onMotifyNickName(boolean isSuccess) {
        if (isSuccess) {
            tv_msg_tip.setText("修改成功");
            dataStatistics("完善资料页，修改昵称成功");
        } else {
            tv_msg_tip.setText("该昵称已被占用，换一个吧");
        }
        tv_msg_tip.setTextColor(Color.parseColor("#ff533c"));
        btn_next_step.setClickCenterable(isSuccess);
    }


    //显示日期选择器
    private void showBirthdayOption() {
        date_picker.setOkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData("birthday", date_picker.getDate(BarDatePicker.TYPE_DEFAULT));
                String birthday = date_picker.getDate(BarDatePicker.TYPE_STRING);
                tv_birthday.setText(birthday);
                dataStatistics("完善资料页，选择年龄");
                date_picker.hide();
            }
        });
        date_picker.show();
    }


    // 初始化选项数据和用户数据
    private void initData() {
        loginType = getIntent().getStringExtra(LOGINTYPE);
        // 选项数据加载
        String jsonOptionStr = AppCommon.getAppData(this, "option");
        final ArrayList<Map<String, String>> list = UtilString.getListMapByJson(jsonOptionStr);
        for (int i = 0; i < list.size(); i++) {
            String type = list.get(i).get("type");
            parseOption(type, list.get(i));
        }


        loadManager.showProgressBar();
        ReqInternet.in().doPost(StringManager.api_getUserInfo, "type=getData", new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.hideProgressBar();
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> lists = StringManager.getListMapByJson(returnObj);
                    Map<String, String> map = lists.get(0);
                    if (map != null && map.size() > 0) {
                        nickName = map.get("nickName");
                        sex = map.get("sex");
                        birthday = map.get("birthday");
                        String img = map.get("img");

                        if (!TextUtils.isEmpty(nickName))
                            et_nickname.setText(nickName);

                        if (!TextUtils.isEmpty(sex)) {
                            sex = sex.substring(sex.lastIndexOf("^") + 1);
                            if (!TextUtils.isEmpty(sex)) {
                                tv_gender.setText(sex);
                            }
                        }

                        if (!TextUtils.isEmpty(birthday)) {
                            birthday = birthday.substring(birthday.indexOf("^") + 1);
                            setUseBirthday(birthday);
                        }

                        if (!TextUtils.isEmpty(img)) {
                            showCover(img);
                        }
                    }
                }
            }
        });
    }


    // 解析选项数据
    private void parseOption(String type, Map<String, String> map) {
        switch (Integer.valueOf(type)) {
            case 1:
                sex_list = UtilString.getListMapByJson(map.get("data"));
                sex_items = new String[sex_list.size()];
                for (int i = 0; i < sex_items.length; i++)
                    sex_items[i] = sex_list.get(i).get("name");
                break;
        }
    }

    //显示选择性别框性
    private void showGenderSelect() {
        AlertDialog.Builder builder_sex = new AlertDialog.Builder(this);
        builder_sex.setTitle("请选择：");
        builder_sex.setItems(sex_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击后弹出窗口选择了第几项
                tv_gender.setText(sex_list.get(which).get("name"));
                dataStatistics("完善资料页，选择性别");
                saveUserData("sex", which + 2 + "");
                //去掉中性选项，中性原来是1，男 2，女 3，which是从0开始，以前+1,现在需+2
            }
        });
        builder_sex.create().show();
    }

    // 发送请求，保存用户信息
    private void saveUserData(String key, String value) {
        String param = "type=setOther&" + key + "=" + value;
        ReqInternet.in().doPost(StringManager.api_setUserData, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                Tools.showToast(SetPersonalInfo.this, returnObj.toString());
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                //使EditText触发一次失去焦点事件
                v.setFocusable(false);
//                v.setFocusable(true); //这里不需要是因为下面一句代码会同时实现这个功能
                v.setFocusableInTouchMode(true);
                return true;
            }
        }
        return false;
    }

    private void setUseBirthday(String birthday) {
        birthday = birthday.substring(birthday.lastIndexOf("^") + 1, birthday.length());
        String[] birthTime = birthday.split("-");
        if (!"0000".equals(birthTime[0])){
            tv_birthday.setText(birthTime[0] + "年" + birthTime[1] + "月" + birthTime[2] + "日");
            date_picker.setDate(birthday);
        }
    }

    @Override
    protected void onPressTopBar() {
        dataStatistics("跳过完善资料");
        backToForward();
    }

    private void dataStatistics(String threeLevel) {
        if (QQ_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(this, PHONE_TAG, "QQ登录", threeLevel);
        } else if (WEIXIN_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(this, PHONE_TAG, "微信登录", threeLevel);
        } else if (WEIBO_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(this, PHONE_TAG, "微博登录", threeLevel);
        } else if (MEIZU_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(this, PHONE_TAG, "魅族登录", threeLevel);
        } else if (EMAIL_LOGIN_TYPE.equals(loginType)) {
            XHClick.mapStat(this, PHONE_TAG, "邮箱登录", threeLevel);
        } else if (ORIGIN_REGISTER.equals(loginType)) {
            XHClick.mapStat(this, PHONE_TAG, "注册", threeLevel);
        }
    }

    @Override
    public void finish() {
        Main.colse_level = 4;
        super.finish();
    }
}
