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

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 选择地址
 * @author yu
 *
 */
public class SelectAddressActivity extends MallBaseActivity implements OnClickListener{

	private String provinceUrl = MallStringManager.mall_api_getprovinces;
	private String cityUrl = MallStringManager.mall_api_getcitys;
	private String countyUrl = MallStringManager.mall_api_getcountys;
	private String townsUrl = MallStringManager.mall_api_gettowns;
	private ListView address_list;
	private TextView select_tv;
	private String mall_index="";
	private AdapterSimple simple;
	private ArrayList<Map<String, String>> listMapByJson_all;
	private String index_code;
	private boolean state=false;
	private String url="",params="";
	private String mall_stat_statistic;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		initActivity("选择地址", 3, 0, 0, R.layout.a_mall_select_address);
		initView();
		initData();
//		initTitle();
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
		title.setText("选择地址");
		findViewById(R.id.back).setOnClickListener(this);
		address_list=(ListView) findViewById(R.id.address_list);
		select_tv=(TextView) findViewById(R.id.address_select_tv);
	}
	
	private void initData() {
		listMapByJson_all= new ArrayList<Map<String,String>>();
		simple= new AdapterSimple(address_list, listMapByJson_all, R.layout.a_mall_select_address_item, new String[]{"name"}, new int[]{R.id.address_tv_item});
		address_list.setAdapter(simple);
		mall_index=provinceUrl;
		address_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				select_tv.setVisibility(View.VISIBLE);
				String param="";
				if(state){
					return;
				}
				
				String index= select_tv.getText().toString();
				index+=listMapByJson_all.get(position).get("name");
				select_tv.setText(index);
				index_code= listMapByJson_all.get(position).get("code");
				
				if(mall_index.equals(provinceUrl)){
					mall_index=cityUrl;
					param= "province_id=";
				}else if(mall_index.equals(cityUrl)){
					mall_index=countyUrl;
					param= "city_id=";
				}else if(mall_index.equals(countyUrl)){
					mall_index=townsUrl;
					param= "county_id=";
				}else if(mall_index.equals(townsUrl)){
					setDataForReult(listMapByJson_all.get(position).get("code"));
				}
				
				param+=listMapByJson_all.get(position).get("code");
				setRequestData(param);
			}
		});
		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRequestData("");
			}
		});
	}
	private void setRequestData(String param) {
		state=true;
		if(!TextUtils.isEmpty(param)){
			setStatisticIndex();
			this.url=mall_index;
			this.params=param;
		}
		MallReqInternet.in().doPost(mall_index, param, new MallInternetCallback(this) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				listMapByJson_all.clear();
				state=false;
				loadManager.loadOver(flag, 1,true);
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					for (int i = 0; i < listMapByJson.size(); i++) {
						listMapByJson_all.add(listMapByJson.get(i));
					}
					if(mall_index.equals(townsUrl)&&listMapByJson_all.size()==0){
						setDataForReult(index_code);
					}
					simple.notifyDataSetChanged();
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

		}
	}
	private void setDataForReult(String code){
		Intent intent = new Intent();
		intent.setClass(SelectAddressActivity.this, AddressActivity.class);
		intent.putExtra("addressName", select_tv.getText().toString());
		intent.putExtra("addressCode", code);
		SelectAddressActivity.this.setResult(AddressActivity.code_select, intent);
		SelectAddressActivity.this.finish();
	}

	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, params,mall_stat_statistic, this);
	}
}
