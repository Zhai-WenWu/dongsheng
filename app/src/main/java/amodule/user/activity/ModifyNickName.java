package amodule.user.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.user.activity.login.UserSetting;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

public class ModifyNickName extends BaseActivity {
	public EditText edit_nickname, edit_info;
	public TextView rightText, user_about;

	public String nickname = "", info = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("修改昵称", 2, 0, R.layout.c_view_bar_title, R.layout.a_my_user_modify_nickname);
		init();
		Bundle bundle = this.getIntent().getExtras();
		if (bundle.containsKey("nickname")) {
			nickname = bundle.getString("nickname");
			if(nickname.length()>0)
				edit_nickname.setText(nickname);
			findViewById(R.id.my_setting_nickname).setVisibility(View.VISIBLE);
			findViewById(R.id.my_setting_info).setVisibility(View.GONE);
			user_about.setVisibility(View.VISIBLE);
		} else if (bundle.containsKey("info")) {
			info = bundle.getString("info");
			TextView title=(TextView) findViewById(R.id.title);
			if(title!=null) title.setText("修改简介");
			if (info.length() > 0) edit_info.setText(info);
			else edit_info.setHint("记下您与美食的缘分或者随便什么吧，祝您在香哈玩的愉快。");
			findViewById(R.id.my_setting_nickname).setVisibility(View.GONE);
			findViewById(R.id.my_setting_info).setVisibility(View.VISIBLE);
			user_about.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadManager.hideProgressBar();
	}

	private void init() {
		// title初始化
		rightText = (TextView) findViewById(R.id.rightText);
		rightText.setVisibility(View.VISIBLE);
		rightText.setText("保存");
		rightText.setClickable(true);

		user_about = (TextView) findViewById(R.id.user_about);
		edit_nickname = (EditText) findViewById(R.id.old_nickname);
		edit_info = (EditText) findViewById(R.id.my_setting_info_content);
		setClickListener();
	}

	public void setClickListener() {
		rightText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (user_about.getVisibility() == View.VISIBLE)
					saveNickName();
				else
					saveInfo();
			}
		});

		edit_nickname.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    if (!TextUtils.isEmpty(nickname)) {
                        if (nickname.length() > 15) {
                            ((TextView) ModifyNickName.this.findViewById(R.id.user_about)).setText("不能超过15个汉字或字符");
                        } else {
                            edit_nickname.setText(nickname);
                        }
                    }

            }
        });
        edit_info.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					edit_info.setText(info);
			}
		});
	}

	public static final String INTERVAL_CHAR = "\n";
	protected void saveInfo() {
		info = edit_info.getText().toString();
		if(info.indexOf(INTERVAL_CHAR) >= 0){
			String newData= "";
			String[] strs = info.split(INTERVAL_CHAR);
			for(String str : strs){
				if(!TextUtils.isEmpty(str.trim())){
					newData += (str + INTERVAL_CHAR);
				}
			}
			int lastIndex = newData.lastIndexOf(INTERVAL_CHAR);
			info = newData.substring(0,lastIndex);
		}
		if(TextUtils.isEmpty(info)){
			info = "对美食的敬意，便是与你分享";
		}
		String params = "type=setOther&info=" + info;
		ReqInternet.in().doPost(StringManager.api_setUserData, params, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					UserSetting.sendRefreshMsg();
					ModifyNickName.this.finish();
				}
			}
		});
	}

	// 保存nickname
	protected void saveNickName() {
		nickname = edit_nickname.getText().toString();
		if (nickname.length() > 0) {
			String params = "type=nickName&p1=" + nickname;
			ReqInternet.in().doPost(StringManager.api_setUserData, params, new InternetCallback(this) {

				@Override
				public void loaded(int flag, String url, Object returnObj) {
					if (flag >= UtilInternet.REQ_OK_STRING) {
						UserSetting.sendRefreshMsg();
						ModifyNickName.this.finish();
					}
				}
			});
		} else
			Tools.showToast(ModifyNickName.this, "请输入昵称");
	}
}
