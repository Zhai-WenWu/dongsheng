package amodule.user.activity;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

public class ModifyPassword extends BaseActivity implements OnClickListener{
	private EditText old_password, new_password;

	private ImageView iv_oldPsw,iv_newPsw;
	
	private boolean isShowOldPsw = false,isShowNewPsw = false,isShowEnsurePsw = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("修改密码", 2, 0, R.layout.c_view_bar_title, R.layout.a_my_modify_password);
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadManager.hideProgressBar();
	}

	private void init() {
		// title设置
		findViewById(R.id.leftImgBtn).setVisibility(View.GONE);
		TextView leftText = (TextView) findViewById(R.id.leftText);
		leftText.setVisibility(View.VISIBLE);
		leftText.setClickable(true);
		leftText.setText("取消");
		int dp_10 = Tools.getDimen(this, R.dimen.dp_10);
		leftText.setPadding(dp_10 , 0 , dp_10 , 0);
		leftText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ModifyPassword.this.onBackPressed();
			}
		});
		TextView rightText = (TextView) findViewById(R.id.rightText);
		rightText.setVisibility(View.VISIBLE);
		rightText.setClickable(true);
		rightText.setText("保存");
		rightText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				modifyPassword();
			}
		});
		
		iv_oldPsw = (ImageView)findViewById(R.id.iv_oldPsw);
		iv_newPsw = (ImageView)findViewById(R.id.iv_newPsw);
		iv_oldPsw.setOnClickListener(this);
		iv_newPsw.setOnClickListener(this);

		// 初始化EditText
		old_password = (EditText) findViewById(R.id.my_setting_sex_content);
		new_password = (EditText) findViewById(R.id.my_setting_birthday_title);
	}

	private void modifyPassword() {
		String old_pwd, new_pwd;
		old_pwd = old_password.getText().toString();
		new_pwd = new_password.getText().toString();
		if (old_pwd.length() == 0) {
			Tools.showToast(ModifyPassword.this, "请输入旧密码");
			return;
		}
		if (new_pwd.length() == 0) {
			Tools.showToast(ModifyPassword.this, "请输入新密码");
			return;
		}
		if (old_pwd.equals(new_pwd)) {
			Tools.showToast(ModifyPassword.this, "新密码不能与原密码相同");
			return;
		}
		String url = StringManager.api_setUserData;
		String param = "type=pwd&p1=" + old_pwd + "&p2=" + new_pwd + "&p3=" + new_pwd;
		ReqInternet.in().doPost(url, param, new InternetCallback() {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					Main.colse_level = 1;
					finish();
				}
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.iv_oldPsw:
			isShowOldPsw = !isShowOldPsw;
			changePsw(old_password,iv_oldPsw,isShowOldPsw);
			break;
		case R.id.iv_newPsw:
			isShowNewPsw = !isShowNewPsw;
			changePsw(new_password,iv_newPsw,isShowNewPsw);
			break;
		}
	}
	
	/**
	 * 切换EditText和ImageView是否显示密码
	 * @param et
	 * @param iv
	 * @param isShowPsw
	 */
	public void changePsw(EditText et,ImageView iv,boolean isShowPsw){
		int index = et.getSelectionStart();
		if(isShowPsw){
			iv.setImageResource(R.drawable.z_user_icon_look_pass);
			et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		}
		else{
			iv.setImageResource(R.drawable.z_user_icon_unlook_pass);
			et.setTransformationMethod(PasswordTransformationMethod.getInstance()); 
		}
		et.postInvalidate();
		et.setSelection(index);
	}

}
