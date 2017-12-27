package third.mall.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import acore.widget.SwitchButton;
import acore.widget.SwitchButton.OnChangeListener;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 收货地址
 * @author yu
 *
 */
public class AddressActivity extends MallBaseActivity implements OnClickListener{

	private EditText consignee_edit;
	private EditText number_edit;
	private TextView address_edit;
	private EditText address_explian_edit;
	private String code;
	public static final int code_select=1000;
	private String address_id;//为空时添加数据，有数据时修改数据
	private Handler handler;
	private static final int ok_success=1002;
	private MallCommon common;
	private SwitchButton sb_address;
	private String address_type="1";
	public static final int result_200=200;
	public static final int result_201=201;
	private String url="";
	private String params="";
	private String mall_stat_statistic;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Bundle bundle= getIntent().getExtras();
		if(bundle!=null){
			address_id= bundle.getString("address_id");
		}
		initActivity("编辑收货地址", 3, 0, 0, R.layout.a_mall_user_address);
		common=new MallCommon(this);
		initView();
		initData();
		setListener();
//		initTitle();
	}

	private void initTitle() {
		if(Tools.isShowTitle()) {
			int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
			int height = topbarHeight + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}
	private void initView() {
		consignee_edit = (EditText) findViewById(R.id.consignee_edit);
		number_edit = (EditText) findViewById(R.id.number_edit);
		address_edit = (TextView) findViewById(R.id.address_edit);
		address_explian_edit = (EditText) findViewById(R.id.address_explian_edit);
		number_edit.setInputType(InputType.TYPE_CLASS_PHONE);
		TextView title=(TextView) findViewById(R.id.title);
		title.setVisibility(View.VISIBLE);
		if(TextUtils.isEmpty(address_id)){
			title.setText("添加收货地址");
			findViewById(R.id.tv_del).setVisibility(View.GONE);
		}else{
			title.setText("编辑收货地址");
			findViewById(R.id.tv_del).setVisibility(View.VISIBLE);
		}
		findViewById(R.id.tv_del).setOnClickListener(this);
		findViewById(R.id.save_address).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		address_edit.setOnClickListener(this);
		sb_address = (SwitchButton) findViewById(R.id.sb_address);
		sb_address.setState(false);
		sb_address.setOnChangeListener(new OnChangeListener() {
			@Override
			public void onChange(SwitchButton sb, boolean state) {
				if(state)address_type="2";
				else address_type="1";
			}
		});
		editTextJubge();
	}
	private void initData() {
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ok_success:
					loadManager.hideProgressBar();
					findViewById(R.id.address_middle).setVisibility(View.VISIBLE);
					break;

				}
				super.handleMessage(msg);
			}
		};
		if(!TextUtils.isEmpty(address_id)){
			findViewById(R.id.address_middle).setVisibility(View.GONE);
			loadManager.setLoading(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setGetRequest();
				}
			});
		}else{
			handler.sendEmptyMessage(ok_success);
		}
	}
	/**
	 * 设置监听
	 */
	private void setListener() {
		TextWatcher baseTextWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				editTextJubge();
			}
		};

		//名称监听
		consignee_edit.addTextChangedListener(baseTextWatcher);
		//电话号码
		number_edit.addTextChangedListener(baseTextWatcher);
		//详细地址
		address_explian_edit.addTextChangedListener(baseTextWatcher);
		address_edit.addTextChangedListener(baseTextWatcher);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_address:
			jubgeData();
			break;
		case R.id.address_edit:
			setStatisticIndex();
			Intent intent = new Intent(this, SelectAddressActivity.class);
			this.startActivityForResult(intent, code_select);
			break;
		case R.id.back:
			this.finish();
			break;
		case R.id.tv_del:
			showdelAddress();
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null)
			return;
		if(resultCode == code_select){
			String addressName = data.getStringExtra("addressName");
			code = data.getStringExtra("addressCode");
			address_edit.setText(addressName);
		}
	}
	
	private void jubgeData(){
		String detail= address_edit.getText().toString().trim()+address_explian_edit.getText().toString().trim();
		String name= consignee_edit.getText().toString().trim();
		String mobile= number_edit.getText().toString().trim();
		//正则
		String type_name="^[\u4e00-\u9fa5_a-zA-Z0-9]+$";
		if(!name.matches(type_name)){
			Tools.showToast(this, "收货人格式不正确，请正确填写");
			return;
		}
		String type_address="^[\u4e00-\u9fa5_a-zA-Z0-9#（）()-—]+$";
		String type_number="^[0-9]+$";
		if(!mobile.matches(type_number)){
			Tools.showToast(this, "请正确填写联系方式");
			return;
		}
		//获取指定的支付其他不要
		StringBuffer stu= new StringBuffer();
		for (int i = 0; i < detail.length(); i++) {
			char item= detail.charAt(i);
			String str= item+"";
			if(str.matches(type_address)){
				stu.append(str);
				
			}
		}
		detail= stu.toString();
		if(mobile.length()<11){
			Tools.showToast(this, "请填写正确的号码");
			return ;
		}
		if(TextUtils.isEmpty(address_id)){
			setAddRequest(detail, name, mobile);
		}else{
			setSetRequest(detail, name, mobile);
		}
	}
	/**
	 * 保存地址
	 */
	private void setAddRequest(final String detail,final String name,final String mobile){
		url=MallStringManager.mall_api_addShippingAddress;
		params= "region_id="+code+"&detail="+detail+"&name="+name+"&mobile="+mobile+"&type="+address_type;
		MallReqInternet.in().doPost(url, params, new MallInternetCallback() {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					AddressActivity.this.setResult(200);
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					setDataForReult(listMapByJson.get(0));
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setAddRequest(detail, name, mobile);
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
					Tools.showToast(AddressActivity.this, map.get("msg"));
				}
			
			}
		});
	}
	/**
	 * 修改地址
	 */
	private void setSetRequest(final String detail,final String name,final String mobile){
		setStatisticIndex();
		params= "address_id="+address_id+"&region_id="+code+"&detail="+detail+"&name="+name+"&mobile="+mobile+"&type="+address_type;
		url=MallStringManager.mall_api_setShippingAddress;
		MallReqInternet.in().doPost(url, params, new MallInternetCallback() {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					setDataForReult(listMapByJson.get(0));
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {

							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setSetRequest(detail, name, mobile);
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
					Tools.showToast(AddressActivity.this, map.get("msg"));
				}

			}
		});
	}
	/**
	 * 获取详细地址
	 */
	private void setGetRequest(){
		url= MallStringManager.mall_api_getShippingAddressDetail+"?address_id="+address_id;
		MallReqInternet.in().doGet(url, new MallInternetCallback() {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(!TextUtils.isEmpty(address_id))
					loadManager.loadOver(flag, 1,true);
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					parseInfoAddress(listMapByJson);
					handler.sendEmptyMessage(ok_success);
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setGetRequest();
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
				}
			
			}
		});
	}
	/**
	 * 解析数据
	 * @param listMapByJson
	 */
	private void parseInfoAddress(ArrayList<Map<String, String>> listMapByJson) {
		consignee_edit.setText(listMapByJson.get(0).get("consumer_name"));
		number_edit.setText(listMapByJson.get(0).get("consumer_mobile"));
		String address_detail= listMapByJson.get(0).get("address_detail");
		code=listMapByJson.get(0).get("region_id");
		String address_type= listMapByJson.get(0).get("address_type");
		if(listMapByJson.get(0).containsKey("address_type")&&"2".equals(listMapByJson.get(0).get("address_type"))){
			sb_address.setState(true);
		}else sb_address.setState(false);
		ArrayList<Map<String, String>> listMapByJson_detail= UtilString.getListMapByJson(listMapByJson.get(0).get("region_detail"));
		String address="";
		int num=0;
		while(num<listMapByJson_detail.size()-1){
			for (int i = 0; i < listMapByJson_detail.size(); i++) {
				address+=listMapByJson_detail.get(i).get("region_name");
				num=i;
			}
		}
		address_edit.setText(address);
		String address_explian=address_detail.replace(address, "");
		address_explian_edit.setText(address_explian);
		
	}
	private void setDataForReult(Map<String,String> map) {
		setStatisticIndex();
		Intent intents = new Intent();
		intents.putExtra("consumer_name", map.get("consumer_name"));
		intents.putExtra("consumer_mobile", map.get("consumer_mobile"));
		intents.putExtra("address_detail", map.get("address_detail"));
		intents.putExtra("address_id", map.get("address_id"));
		AddressActivity.this.setResult(result_200, intents);
		AddressActivity.this.finish();
	}
	/**
	 * 检验数据
	 */
	private void editTextJubge(){
		String name= consignee_edit.getText().toString().trim();
		String number= number_edit.getText().toString().trim();
		String address= address_edit.getText().toString().trim();
		String explian= address_explian_edit.getText().toString().trim();
		if(name.length()>0&&number.length()>0&&address.length()>0&&explian.length()>0){
			findViewById(R.id.save_address).setBackgroundDrawable(this.getResources().getDrawable(R.drawable.rong_red_but_nologin));
			findViewById(R.id.save_address).setEnabled(true);
		}else{
			findViewById(R.id.save_address).setBackgroundDrawable(this.getResources().getDrawable(R.drawable.mall_back_no));
			findViewById(R.id.save_address).setEnabled(false);
			
		}
	}
	/**
	 * 删除dialog
	 */
	private void showdelAddress(){
		final DialogManager dialogManager = new DialogManager(this);
		dialogManager.createDialog(new ViewManager(dialogManager)
				.setView(new TitleMessageView(this).setText("确定删除该地址吗？"))
				.setView(new HButtonView(this)
						.setNegativeText("取消", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialogManager.cancel();
							}
						})
						.setPositiveText("确定", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialogManager.cancel();
								delAddress();
							}
						}))).show();
	}
	/**
	 * 删除地址
	 */
	private void delAddress(){
		setStatisticIndex();
		params="address_id="+address_id;
		url=MallStringManager.mall_delShippingAddress;
		MallReqInternet.in().doPost(url, params, new MallInternetCallback() {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					setResultDel();
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									delAddress();
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
				}
			
			}
		});
	}
	/**
	 * 设置数据变化
	 */
	private void setResultDel(){
		Intent intent= new Intent();
		intent.putExtra("address_id", address_id);
		AddressActivity.this.setResult(AddressActivity.result_201,intent);
		AddressActivity.this.finish();
	}
	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, params,mall_stat_statistic, this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(handler!=null){
			handler.removeCallbacksAndMessages(null);
			handler=null;
		}
	}
}
