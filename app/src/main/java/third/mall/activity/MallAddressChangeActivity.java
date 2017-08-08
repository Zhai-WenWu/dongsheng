package third.mall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import third.mall.adapter.AdapterAddressChange;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 地址选择
 * @author Administrator
 *
 */
public class MallAddressChangeActivity extends BaseActivity implements OnClickListener{

	private ListView address_list;
	private ArrayList<Map<String, String>> ListData=new ArrayList<Map<String,String>>();
	private AdapterAddressChange adapter;
	private MallCommon common;
	private String now_address_id;
	private boolean state_now=false;
	private boolean state_del=true;
	private String url="";
	private String mall_stat_statistic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("", 3, 0, 0, R.layout.a_mall_addresschange);
		Bundle bundle= getIntent().getExtras();
		if(bundle!=null)
			now_address_id=bundle.getString("now_address_id");
		common= new MallCommon(this);
		initView();
		initData();
		initTitle();
	}

	private void initTitle() {
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}
	private void initView() {
		TextView title=(TextView) findViewById(R.id.title);
		title.setText("选择收货地址");
		findViewById(R.id.back).setOnClickListener(this);
		address_list = (ListView) findViewById(R.id.address_list);
		findViewById(R.id.add_address).setOnClickListener(this);
		findViewById(R.id.add_address).setVisibility(View.GONE);
	}
	private void initData() {
		adapter= new AdapterAddressChange(this,now_address_id,address_list, ListData, R.layout.view_mall_addresschange_item, null, null);
		loadManager.showProgressBar();
		loadManager.setLoading(address_list, adapter, true, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setRequest(false);
			}
		});
		address_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				now_address_id=ListData.get(position).get("address_id");
				MallAddressChangeActivity.this.finish();
			}
		});
	}
	
	private void setRequest(final boolean isFirst){
		ListData.clear();
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, 1,ListData.size()==0);
		url=MallStringManager.mall_getShippingAddress;
		MallReqInternet.init(this).doGet(url, new MallInternetCallback(this) {
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {

				loadManager.loadOver(flag, 1,true);
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					for (int i = 0; i < listMapByJson.size(); i++) {
						ListData.add(listMapByJson.get(i));
					}
					if(isFirst&&ListData.size()>0){
						now_address_id=ListData.get(0).get("address_id");
						adapter.setNowAddress(now_address_id);
					}
					if(ListData.size()<=0&& state_del){
						Intent intent = new Intent(MallAddressChangeActivity.this,AddressActivity.class);
						MallAddressChangeActivity.this.startActivityForResult(intent, ShoppingOrderActivity.OK_ADDRESS);
					}
					adapter.notifyDataSetChanged();
					findViewById(R.id.add_address).setVisibility(View.VISIBLE);
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					
					@SuppressWarnings("unchecked")
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setRequest(false);
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
				}
				loadManager.hideProgressBar();
			
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.add_address:
			setStatisticIndex();
			Intent intent = new Intent(this,AddressActivity.class);
			this.startActivityForResult(intent, ShoppingOrderActivity.OK_ADDRESS);
			break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ShoppingOrderActivity.OK_ADDRESS:
			if(resultCode==AddressActivity.result_200){
				if(data!=null){
					state_now=true;
					this.setResult(AddressActivity.result_200, data);
				}
				this.finish();
			}else if(resultCode==AddressActivity.result_201){
				state_del=false;
				String address_id_201=data.getStringExtra("address_id");
				if(address_id_201.equals(now_address_id))
					setRequest(true);
				else 
					setRequest(false);
					
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public void finish() {
		if(!state_now){
			int size= ListData.size();
			for (int i = 0; i <size; i++) {
				if(ListData.get(i).get("address_id").equals(now_address_id)){
					Intent intent= new Intent();
					intent.putExtra("address_id", ListData.get(i).get("address_id"));
					intent.putExtra("consumer_name", ListData.get(i).get("consumer_name"));
					intent.putExtra("consumer_mobile", ListData.get(i).get("consumer_mobile"));
					intent.putExtra("address_detail", ListData.get(i).get("address_detail"));
					MallAddressChangeActivity.this.setResult(AddressActivity.result_200, intent);
				}
			}
		}
		super.finish();
	}
	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, this);
	}
}
