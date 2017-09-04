package amodule.user.activity.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.user.activity.ModifyNickName;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.datepicker.BarDatePicker;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class UserSetting extends BaseLoginActivity implements OnClickListener {
    private static final int MODIFY_USER_IMG = 5000;
    public static Handler handler = null;
    public static final int ONREFRESH = 1;

	private int tag = 0;
	private String name = "", birthday;
	private String[] sex_items, degre_items, marr_items;
	private BarDatePicker date_picker;
	private ScrollView my_setting_scrollview;
	private ImageView my_img, iv_userType;
	private TextView my_nickName, my_account, my_pwd, my_sex, my_birthday, my_degre, my_marriage, my_info;
	private ArrayList<Map<String, String>> sex_list, degre_list, marr_list;
	private ArrayList<TextView> text_list = new ArrayList<>();
	private RelativeLayout my_img_modify, back;
	private LinearLayout group_1, group_2, group_3, group_4;
	private String[] groupOneNames = {"昵称"};
	private String[] groupTwoNames = {"性别", "生日", "学历", "婚否", "简介"};
	private String[] groupThreeNames = {"绑定QQ", "绑定微信", "绑定微博"};
	private int[] groupThreeImgID = {R.drawable.z_me_account_ico_qq, R.drawable.z_me_account_ico_weixin, R.drawable.z_me_account_ico_weibo};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        initActivity("", 4, 0, 0, R.layout.a_my_user_setting);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ONREFRESH:
                        loadManager.setLoading("", new InternetCallback(mAct) {
                            @Override
                            public void loaded(int flag, String url, Object returnObj) {
                                loadManager.setLoading(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getUserData();
                                    }
                                });
                            }
                        });
                        break;
                }
            }
        };
        loadManager.showProgressBar();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
                initTitles();
            }
        }, 100);
    }

    private void initTitles() {
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }

    private void init() {
        // title初始化
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("个人信息");
        findViewById(R.id.leftText).setVisibility(View.GONE);
        findViewById(R.id.leftImgBtn).setVisibility(View.VISIBLE);
        back = (RelativeLayout) findViewById(R.id.back);
        my_img_modify = (RelativeLayout) findViewById(R.id.my_img_modify);
        my_setting_scrollview = (ScrollView) findViewById(R.id.my_setting_scrollview);
        my_img = (ImageView) findViewById(R.id.my_setting_img);
        iv_userType = (ImageView) findViewById(R.id.iv_userType);
        date_picker = (BarDatePicker) findViewById(R.id.my_setting_date_picker);
        date_picker.setDate("1985年1月1日");

        group_1 = (LinearLayout) findViewById(R.id.my_setting_group_1);
        group_2 = (LinearLayout) findViewById(R.id.my_setting_group_2);
        group_3 = (LinearLayout) findViewById(R.id.my_setting_group_3);
        group_4 = (LinearLayout) findViewById(R.id.my_setting_group_4);
        group_3.setVisibility(View.GONE);
        group_4.setVisibility(View.GONE);
        initGroupUI(group_1, groupOneNames);
        initGroupUI(group_2, groupTwoNames);
        initGroupUI(group_3, groupThreeNames, groupThreeImgID);

        my_nickName = (TextView) group_1.getChildAt(1).findViewById(R.id.item_user_setting_content);
//		my_account=(TextView) group_1.getChildAt(3).findViewById(R.id.item_user_setting_content);
//		my_pwd=(TextView) group_1.getChildAt(5).findViewById(R.id.item_user_setting_content);
        my_sex = (TextView) group_2.getChildAt(1).findViewById(R.id.item_user_setting_content);
        my_birthday = (TextView) group_2.getChildAt(3).findViewById(R.id.item_user_setting_content);
        my_degre = (TextView) group_2.getChildAt(5).findViewById(R.id.item_user_setting_content);
        my_marriage = (TextView) group_2.getChildAt(7).findViewById(R.id.item_user_setting_content);
        my_info = (TextView) group_2.getChildAt(9).findViewById(R.id.item_user_setting_content);

        text_list.add(my_sex);
        text_list.add(my_birthday);
        text_list.add(my_degre);
        text_list.add(my_marriage);

        initData();
        getUserData();
        setOnClick();
    }

    //初始化UI
    private void initGroupUI(LinearLayout group, final String[] groupTitle) {
        for (int i = 0; i < groupTitle.length; i++) {
            if (i == 0)
                addLineView(group, 0);
            LinearLayout itemView = (LinearLayout) LayoutInflater.from(UserSetting.this).inflate(R.layout.a_my_item_user_setting, group);
            RelativeLayout layout = (RelativeLayout) itemView.getChildAt(2 * i + 1);
            layout.setClickable(true);
            layout.setTag(tag++);
            layout.setOnClickListener(this);
            TextView text_title = (TextView) itemView.getChildAt(2 * i + 1).findViewById(R.id.item_user_setting_title);
            text_title.setVisibility(View.VISIBLE);
            text_title.setText(groupTitle[i]);
            if (i == groupTitle.length - 1)
                addLineView(group, 0);
            else
                addLineView(group, Tools.getDimen(mAct, R.dimen.dp_15));
        }
    }

    //初始化UI
    private void initGroupUI(LinearLayout group, final String[] groupTitle, final int[] imgID) {
        for (int i = 0; i < groupTitle.length; i++) {
            if (i == 0)
                addLineView(group, 0);
            LinearLayout itemView = (LinearLayout) LayoutInflater.from(UserSetting.this).inflate(R.layout.a_my_item_user_setting, group);
            RelativeLayout layout = (RelativeLayout) itemView.getChildAt(2 * i + 1);
            layout.setClickable(true);
            layout.setTag(tag++);
            layout.setOnClickListener(this);
            ImageView ico = (ImageView) itemView.getChildAt(2 * i + 1).findViewById(R.id.item_user_setting_bind_img);
            ico.setVisibility(View.VISIBLE);
            ico.setImageResource(imgID[i]);
            TextView text_title = (TextView) itemView.getChildAt(2 * i + 1).findViewById(R.id.item_user_setting_bind_title);
            text_title.setVisibility(View.VISIBLE);
            text_title.setText(groupTitle[i]);
            if (i == groupTitle.length - 1)
                addLineView(group, 0);
            else
                addLineView(group, Tools.getDimen(mAct, R.dimen.dp_15));
        }
    }

    // 初始化选项数据和用户数据
    private void initData() {
        // 选项数据加载
        String jsonOptionStr = AppCommon.getAppData(this, "option");
        ArrayList<Map<String, String>> list = UtilString.getListMapByJson(jsonOptionStr);
        for (int i = 0; i < list.size(); i++) {
            String type = list.get(i).get("type");
            parseOption(type, list.get(i));
        }
    }

    //获取用户数据
    private void getUserData() {
        String param = "type=getData&devCode=" + XGPushServer.getXGToken(this);
        ReqInternet.in().doPost(StringManager.api_getUserInfo, param, new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
                    if (listReturn.size() > 0) {
                        Map<String, String> user_info = listReturn.get(0);
                        name = user_info.get("name");
//						setUserPwd();//设置用户密码
                        setUserImg(user_info);//设置用户头像
                        setUserNickName(user_info);//设置用户昵称
//						setUserAccount(user_info);//设置用户账号
                        setUseBirthday(user_info);//设置用户生日
                        setUserInfo(user_info);//设置用户简介
                        setUserData(my_sex, user_info, "sex");//设置用户性别
                        setUserData(my_degre, user_info, "degre");//设置用户学历
                        setUserData(my_marriage, user_info, "marriage");//设置用户婚否
                    }
                } else {
                    loadManager.hideLoadFaildBar();
                    loadManager.setFailClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadManager.hideLoadFaildBar();
                            sendRefreshMsg();
                        }
                    });
                }
                my_setting_scrollview.setVisibility(View.VISIBLE);
                loadManager.hideProgressBar();
            }
        });
    }

    //设置用户昵称
    private void setUserNickName(Map<String, String> user_info) {
        if (user_info.get("nickName") != null && !"".equals(user_info.get("nickName"))) {
            my_nickName.setText(user_info.get("nickName"));
        } else {
            my_nickName.setText("未设置");
        }
        my_nickName.setVisibility(View.VISIBLE);
    }

    //设置用户密码
    private void setUserPwd() {
        RelativeLayout item = (RelativeLayout) group_1.getChildAt(5);
        TextView bind_text = (TextView) item.findViewById(R.id.item_user_setting_modify_pwd);
        my_pwd.setText("******");
        my_pwd.setVisibility(View.VISIBLE);
        bind_text.setText("修改密码");
        bind_text.setTextColor(Color.parseColor("#999999"));
        bind_text.setBackgroundResource(android.R.color.transparent);
        bind_text.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        bind_text.setPadding(0, 0, 0, 0);
        item.findViewById(R.id.item_user_setting_bind).setVisibility(View.VISIBLE);
    }

    //设置用户简介
    private void setUserInfo(Map<String, String> user_info) {
        my_info.setText("");
        if (user_info.get("info") != null && !"".equals(user_info.get("info"))) {
            my_info.setText(user_info.get("info"));
            my_info.setGravity(Gravity.LEFT | Gravity.TOP);
            my_info.setPadding(0, Tools.getDimen(UserSetting.this, R.dimen.dp_2), 0, 0);
            my_info.getLayoutParams().height = Tools.getDimen(UserSetting.this, R.dimen.dp_90);
            group_2.getChildAt(9).getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            group_2.getChildAt(9).setPadding(0, Tools.getDimen(UserSetting.this, R.dimen.dp_15), 0, 0);
        } else {
            my_info.setHint("记下您与美食的缘分或者随便什么吧，祝您在香哈玩的愉快。");
        }
        my_info.setVisibility(View.VISIBLE);
    }

    //设置用户数据
    private void setUserData(TextView tv, Map<String, String> user_info, String key) {
        if (user_info.get(key) != null && !"".equals(user_info.get(key))) {
            String content = user_info.get(key);
            content = content.substring(content.lastIndexOf("^") + 1, content.length());
            tv.setText(content);
            if (TextUtils.isEmpty(content)) {
                tv.setText("未设置");
            }
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setText("未设置");
            tv.setVisibility(View.VISIBLE);
        }
    }

    //设置用户头像
    private void setUserImg(Map<String, String> user_info) {
        if (LoginManager.isLogin() && !user_info.get("img").equals(LoginManager.userInfo.get("img")))
            LoginManager.modifyUserInfo(mAct, "img", user_info.get("img"));
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
                .load(user_info.get("img"))
                .setImageRound(ToolsDevice.dp2px(UserSetting.this, 500))
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(my_img);
        my_img.setVisibility(View.VISIBLE);
        if (user_info.get("isGourmet") != null)
            AppCommon.setUserTypeImage(Integer.parseInt(user_info.get("isGourmet")), iv_userType);
    }

    //设置用户账号
    private void setUserAccount(Map<String, String> user_info) {
        RelativeLayout item = (RelativeLayout) group_1.getChildAt(3);
        String account = "";
        String email = user_info.get("email");
        String tel = user_info.get("tel");
        if (tel != null && tel.length() != 0) {
            String str = tel.substring(3, 9);
            account = tel.replace(str, "******");
            item.findViewById(R.id.item_user_setting_bind).setVisibility(View.GONE);
            item.findViewById(R.id.item_user_setting_binded).setVisibility(View.VISIBLE);
            item.setClickable(false);
        } else if (email != null && email.length() != 0 && email.indexOf("@") >= 2) {
            char[] chars = new char[email.indexOf("@") - 2];
            email.getChars(1, email.indexOf("@") - 1, chars, 0);
            String str = new String();
            for (int i = 0; i < chars.length; i++)
                str = str + chars[i];
            account = email.replace(str, "****");
            item.findViewById(R.id.item_user_setting_bind).setVisibility(View.VISIBLE);
            item.findViewById(R.id.item_user_setting_binded).setVisibility(View.GONE);
            TextView text = (TextView) item.findViewById(R.id.item_user_setting_modify_pwd);
            text.setText("绑定手机号");
        }
        my_account.setText(account);
        my_account.setVisibility(View.VISIBLE);
    }

    //设置用户生日
    private void setUseBirthday(Map<String, String> user_info) {
        birthday = user_info.get("birthday");
        if (birthday != null && !"".equals(user_info.get("birthday"))) {
            birthday = birthday.substring(birthday.lastIndexOf("^") + 1, birthday.length());
            String[] birthTime = birthday.split("-");
            if (!"0000".equals(birthTime[0]))
                my_birthday.setText(birthTime[0] + "年" + birthTime[1] + "月" + birthTime[2] + "日");
            else
                my_birthday.setText("未设置");
        } else {
            my_birthday.setText("未设置");
        }
        my_birthday.setVisibility(View.VISIBLE);
    }


    // 绑定显示控制
    private void isBinded(LinearLayout bind_layout, TextView binded_text, boolean flag) {
        bind_layout.setVisibility(flag ? View.VISIBLE : View.GONE);
        binded_text.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    //显示日期选择器
    private void showBirthdayOption() {
        date_picker.setOkClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveUserData("birthday", date_picker.getDate(BarDatePicker.TYPE_DEFAULT));
                birthday = date_picker.getDate(BarDatePicker.TYPE_STRING);
                my_birthday.setText(birthday);
                date_picker.hide();
            }
        });
        if (!TextUtils.isEmpty(birthday) && !birthday.startsWith("0000"))
            date_picker.setDate(birthday);
        date_picker.show();
    }

    //显示其他的选择框
    private void showOptions(String[] options, final ArrayList<Map<String, String>> options_list, final int index) {
        AlertDialog.Builder builder_sex = new AlertDialog.Builder(UserSetting.this);
        builder_sex.setTitle("请选择：");
        builder_sex.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击后弹出窗口选择了第几项
                text_list.get(index).setText(options_list.get(which).get("name"));
                switch (index) {
                    case 0:
                        saveUserData("sex", which + 2 + "");
                        break; //去掉中性选项，中性原来是1，男 2，女 3，which是从0开始，以前+1,现在需+2
                    case 2:
                        saveUserData("degre", which + 1 + "");
                        break;
                    case 3:
                        saveUserData("marriage", which + 1 + "");
                        break;
                }
            }
        });
        builder_sex.create().show();
    }

    // 发送请求，保存用户信息
    private void saveUserData(String key, String code) {
        String param = "type=setOther&" + key + "=" + code;
        ReqInternet.in().doPost(StringManager.api_setUserData, param, new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                Tools.showToast(UserSetting.this, returnObj.toString());
            }
        });
    }

    // 解析选项数据
    private void parseOption(String type, Map<String, String> map) {
        switch (Integer.valueOf(type)) {
            case 1:
                sex_list = UtilString.getListMapByJson(map.get("data"));
                sex_items = new String[sex_list.size()];
                optionsToArray(sex_items, sex_list);
                break;
            case 2:
                degre_list = UtilString.getListMapByJson(map.get("data"));
                degre_items = new String[degre_list.size()];
                optionsToArray(degre_items, degre_list);
                break;
            case 3:
                marr_list = UtilString.getListMapByJson(map.get("data"));
                marr_items = new String[marr_list.size()];
                optionsToArray(marr_items, marr_list);
                break;
        }
    }

    //把选项放入array
    private void optionsToArray(String[] items, ArrayList<Map<String, String>> list) {
        for (int i = 0; i < items.length; i++)
            items[i] = list.get(i).get("name");
    }

    // 添加横线
    private void addLineView(LinearLayout layout, float leftMargins) {
        View lineView = new ImageView(UserSetting.this);
        lineView.setBackgroundResource(R.color.c_gray_dddddd);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ToolsDevice.dp2px(UserSetting.this, 0.5f));
        params.setMargins(ToolsDevice.dp2px(UserSetting.this, leftMargins), 0, 0, 0);
        layout.addView(lineView, params);
    }

    //修改头像
    private void modifyUserImg() {
        Intent intent = new Intent();
        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_SINGLE);
        intent.setClass(this, ImageSelectorActivity.class);
        startActivityForResult(intent, MODIFY_USER_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MODIFY_USER_IMG
                && data != null) {
            ArrayList<String> array = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);//ArrayList<String>
            if (array.size() > 0) {
                String imgUrl = array.get(0);
                if (!TextUtils.isEmpty(imgUrl)) {
                    uploadImg(imgUrl);
                } else {
                    Tools.showToast(this, "选择图片有误，请重新选择");
                }
            }
        }
    }

    private void uploadImg(String imgUrl) {
        LinkedHashMap<String, String> fileMap = new LinkedHashMap<String, String>();
        fileMap.put("type", "img");
        fileMap.put("uploadImg_file_1", imgUrl);
        Tools.showToast(getApplicationContext(), "图片正在上传,请稍等!");
        ReqInternet.in().doPostImg(StringManager.api_setUserData, fileMap, new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    sendRefreshMsg();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (Integer.parseInt(v.getTag() + "")) {
            case 0: // 修改nickname
                Intent modifyNickName = new Intent(UserSetting.this, ModifyNickName.class);
                modifyNickName.putExtra("nickname", my_nickName.getText().toString());
                startActivity(modifyNickName);
                break;
//		case 1: // 绑定手机
//			Intent bindPhone = new Intent(UserSetting.this, UserPhoneLogin.class);
//			bindPhone.putExtra("type", "bind");
//			bindPhone.putExtra("title", "绑定手机号");
//			startActivity(bindPhone);
//			break;
//		case 2: //修改密码
//			Intent modifyPwd=null;
//			if (name != null && name.equals("")) 
//				modifyPwd= new Intent(UserSetting.this, UserFindPWD.class);
//			 else 
//				 modifyPwd = new Intent(UserSetting.this, ModifyPassword.class);
//			startActivity(modifyPwd);
//			break;
            case 1: //修改sex
                showOptions(sex_items, sex_list, 0);
                break;
            case 2: //修改birthday
                showBirthdayOption();
                break;
            case 3: //修改degre
                showOptions(degre_items, degre_list, 2);
                break;
            case 4: //修改marriage
                showOptions(marr_items, marr_list, 3);
                break;
            case 5: //修改info
                Intent modifyInfo = new Intent(UserSetting.this, ModifyNickName.class);
                modifyInfo.putExtra("info", my_info.getText().toString());
                startActivity(modifyInfo);
                break;
        }
    }

    //设置监听
    private void setOnClick() {
        //修改头像
        my_img_modify.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserImg();
            }
        });
        //退出登录
        group_4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.showProgressBar();
                Main.colse_level = 3;
                LoginManager.logout(mAct);
            }
        });
        //返回
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSetting.this.onBackPressed();
            }
        });
    }

    public static void sendRefreshMsg() {
        if (handler != null)
            handler.sendEmptyMessage(ONREFRESH);
    }

    @Override
    public void onBackPressed() {
        if (date_picker.getVisibility() == View.VISIBLE) {
            date_picker.hide();
        } else
            super.onBackPressed();
    }
}
