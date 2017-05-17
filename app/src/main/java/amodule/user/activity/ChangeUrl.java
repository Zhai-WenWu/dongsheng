package amodule.user.activity;

import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilFile;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

public class ChangeUrl extends BaseActivity{
	TextView tv_nowDomain;
	EditText et_port;
	String domain="",port="";
	private TextView mall_tv_now_domain;
	private  String mall_domain="",mall_port="";
	private EditText mall_et_input1;
	private boolean state_xiangha=false;
	private boolean state_mall=false;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		initActivity("切换url", 2, 0, R.layout.c_view_bar_title, R.layout.a_core_change_url);
		init();
		initData();
		initMallData();
		loadManager.hideProgressBar();
	}
	
	private void init(){
		tv_nowDomain = (TextView)findViewById(R.id.tv_now_domain);
		et_port=(EditText)findViewById(R.id.et_input1);
		TextView rightText = (TextView)findViewById(R.id.rightText);
		rightText.setVisibility(View.VISIBLE);
		rightText.setText("完成");
		
		RadioGroup group = (RadioGroup)findViewById(R.id.rg_domain);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup rg, int radioButtonId) {
                RadioButton rb = (RadioButton)ChangeUrl.this.findViewById(radioButtonId);
                state_xiangha=true;
                domain=rb.getText().toString();
                setUrl();
            }
        });
		et_port.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				port=et_port.getText().toString();
				state_xiangha=true;
				if(port.length()>0) port=":"+port;
				setUrl();
				return false;
			}
		});
		
		rightText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(state_xiangha){//香哈
					if(domain.length()<5){
						Toast.makeText(getApplicationContext(), "请选择域", Toast.LENGTH_SHORT).show();
						return;
					}
					port=et_port.getText().toString();
					if(port.length()>0) port=":"+port;
					StringManager.changeUrl(domain+port);
					UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo,FileManager.xmlKey_domain, domain+port);
					if(!state_mall)
						ChangeUrl.this.finish();
				}
				if(state_mall){//电商
					if(mall_domain.length()<5){
						Toast.makeText(getApplicationContext(), "请选择域", Toast.LENGTH_SHORT).show();
						return;
					}
					mall_port=mall_et_input1.getText().toString();
					if(mall_port.length()>0) mall_port=":"+mall_port;
					MallStringManager.changeUrl(mall_domain+mall_port);
					UtilFile.saveShared(ChangeUrl.this, FileManager.xmlFile_appInfo,FileManager.xmlKey_mall_domain, mall_domain+mall_port);
					ChangeUrl.this.finish();
					
				}
			}
		});
		initViewMall();
	}
	
	private void initData(){
		tv_nowDomain.setText("当前选择域："+StringManager.apiUrl.replace("http://api", "").replace("/", ""));
	}
	private void setUrl(){
		tv_nowDomain.setText("当前选择域："+domain+port);
	}
	/**
	 * 初始化电商数据
	 */
	private void initViewMall() {
		mall_tv_now_domain = (TextView) findViewById(R.id.mall_tv_now_domain);
		
		RadioGroup group = (RadioGroup)findViewById(R.id.mall_rg_domain);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				state_mall=true;
				RadioButton rb = (RadioButton)ChangeUrl.this.findViewById(checkedId);
				mall_domain= rb.getText().toString();
				setMallUrl();
			}
		});
		mall_et_input1 = (EditText) findViewById(R.id.mall_et_input1);
		mall_et_input1.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				state_mall=true;
				mall_port=mall_et_input1.getText().toString();
				if(mall_port.length()>0) mall_port=":"+mall_port;
				setMallUrl();
				return false;
			}
		});
	}
	private void initMallData(){
		mall_tv_now_domain.setText("当前选择域："+MallStringManager.mall_apiUrl.replace("http://api", "").replace("/", ""));
	}
	/**
	 * 设置当前显示url
	 */
	private void setMallUrl(){
		mall_tv_now_domain.setText("当前选择域："+mall_domain+mall_port);
	}
}
