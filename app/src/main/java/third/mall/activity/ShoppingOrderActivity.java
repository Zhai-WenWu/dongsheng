package third.mall.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import third.mall.adapter.AdapterShoppingOrder;
import third.mall.adapter.AdapterShoppingOrder.OrderChangeCallBack;
import third.mall.alipay.MallAlipay;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallPayType;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import third.mall.tool.ToolView;
import third.mall.wx.WxPay;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 确认订单页面
 * @author yujian
 *
 */
public class ShoppingOrderActivity extends MallBaseActivity implements OnClickListener {

	private TextView shoporder_consignee_man_name;
	private TextView shoporder_consignee_man_number;
	private TextView shoporder_consignee_man_address;
	private ListView listview;
	private TextView shoporder_commod_price_end;
	private Object msg_order;
	private ArrayList<Map<String, String>> list_data;
	private String total_amt;
	private MallCommon common;
	private Handler handler;
	private String consignee_name = "";
	private String consignee_tel = "";
	private String consignee_address = "";
	private String address_id = "";
	private static final int SHOW_OK = 1;
	public static final int OK_ADDRESS = 100;
	private String order_info;
	private ScrollView order_scrollview;
	private ImageView pay_wechat;
	private ImageView pay_alipay;
	private MallPayType payType;
	private JSONArray jsonArray = new JSONArray();
	private AdapterShoppingOrder adapter;
	private float all_float=0;
	private String url="";
	private String mall_stat_statistic;
	private Map<String,String> map_favorable= new HashMap<String, String>();//处理优惠券信息

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			msg_order = bundle.get("msg_order");
			order_info = bundle.getString("order_info");
			url= bundle.getString("url");
			mall_stat_statistic= bundle.getString("stat");
		}
		initActivity("确认订单", 3, 0, 0, R.layout.a_mall_shop_order);
		common = new MallCommon(this);
		payType = new MallPayType(this);
		initView();
		initData();
//		initTitle();
		XHClick.track(this,"浏览确认订单页");
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
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("确认订单");
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.shoporder_consignee_rela).setOnClickListener(this);
		shoporder_consignee_man_name = (TextView) findViewById(R.id.shoporder_consignee_man_name);
		shoporder_consignee_man_number = (TextView) findViewById(R.id.shoporder_consignee_man_number);
		shoporder_consignee_man_address = (TextView) findViewById(R.id.shoporder_consignee_man_address);

		findViewById(R.id.shoporder_consignee_linear_none).setVisibility(View.VISIBLE);
		findViewById(R.id.shoporder_consignee_rela_data).setVisibility(View.GONE);
		listview = (ListView) findViewById(R.id.listview);
		shoporder_commod_price_end = (TextView) findViewById(R.id.shoporder_commod_price_end);
		findViewById(R.id.shoporder_commod_price_buy).setOnClickListener(this);
		order_scrollview = (ScrollView) findViewById(R.id.order_scrollview);

		// 支付方式选择
		findViewById(R.id.pay_type_wechat).setOnClickListener(this);
		findViewById(R.id.pay_type_alipay).setOnClickListener(this);
		pay_wechat = (ImageView) findViewById(R.id.pay_wechat);
		pay_alipay = (ImageView) findViewById(R.id.pay_alipay);

	}

	private void initData() {
		if (msg_order != null) {
			list_data = UtilString.getListMapByJson(UtilString.getListMapByJson(msg_order).get(0).get("sub_order"));
			total_amt = UtilString.getListMapByJson(msg_order).get(0).get("total_amt");
			getCreateAmt();
		}

		adapter = new AdapterShoppingOrder(this, listview, list_data, R.layout.a_mall_shop_order_item, new String[] {}, new int[] {});
		listview.setAdapter(adapter);
		ToolView.setListViewHeightBasedOnChildren(listview);
		order_scrollview.smoothScrollTo(0, 0);
		adapter.setCallBack(new OrderChangeCallBack() {
			
			@Override
			public void setChangeData(String shop_code, String code, String before_amt, String now_amt) {
				map_favorable.put(shop_code, code);
				if(!TextUtils.isEmpty(before_amt)){
					all_float+=Float.parseFloat(before_amt);
				}
				if(!TextUtils.isEmpty(now_amt)){
					all_float-=Float.parseFloat(now_amt);
				}
				setTextAllAmt();
			}
		});
		setTextAllAmt();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SHOW_OK:
					loadManager.hideLoadFaildBar();
					loadManager.hideProgressBar();
					break;

				}
				super.handleMessage(msg);
			}
		};
		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRequestAddress();
			}
		});
		// 设置支付状态
		setPayView();
	}
	private float getCreateAmt(){
		for (int i = 0,size= list_data.size(); i < size; i++) {
			float product_amt =Float.parseFloat(UtilString.getListMapByJson(list_data.get(i).get("amt_info")).get(0).get("product_amt"));
			all_float+=product_amt;
			all_float+=Float.parseFloat(UtilString.getListMapByJson(list_data.get(i).get("amt_info")).get(0).get("postage_amt"));
			all_float-=Float.parseFloat(UtilString.getListMapByJson(list_data.get(i).get("amt_info")).get(0).get("promotion_amt"));
			if(list_data.get(i).containsKey("shop_coupon_info")){
				ArrayList<Map<String, String>> list= UtilString.getListMapByJson(list_data.get(i).get("shop_coupon_info"));
				ArrayList<Map<String, String>> change_list=new ArrayList<Map<String,String>>();
				for (int j = 0,num=list.size(); j < num; j++) {
					if(Float.parseFloat(list.get(j).get("order_amt_reach"))<=product_amt){
						change_list.add(list.get(j));
					}
				}
				if(change_list.size()>0){
					all_float-=Float.parseFloat(change_list.get(change_list.size()-1).get("coupon_amt"));
					map_favorable.put(UtilString.getListMapByJson(list_data.get(i).get("shop_info")).get(0).get("shop_code"), change_list.get(change_list.size()-1).get("shop_coupon_code"));
				}
			}
		}
		return all_float;
	}

	/**
	 * 设置view金额
	 */
	private void setTextAllAmt(){
		if (all_float>0) {
			DecimalFormat   fnum  =   new  DecimalFormat("##0.00");    
			String  amt=fnum.format(all_float); 
			shoporder_commod_price_end.setText("实付款:¥" + amt);
			shoporder_commod_price_end.setVisibility(View.VISIBLE);
		} else {
			shoporder_commod_price_end.setVisibility(View.GONE);
		}
	}
	/**
	 * 获取收货地址
	 */
	private void setRequestAddress() {
		setStatisticIndex();
		loadManager.showProgressBar();
		url = MallStringManager.mall_api_getShippingAddress;
		MallReqInternet.in().doGet(url, new MallInternetCallback() {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				loadManager.showProgressBar();
				loadManager.loadOver(flag, 1, true);
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					setParseInfoAddress(listMapByJson);
					handler.sendEmptyMessage(SHOW_OK);
				} else if (flag == UtilInternet.REQ_CODE_ERROR && msg instanceof Map) {
					// 处理code过期问题
					Map<String, String> map = (Map<String, String>) msg;
					// 处理code过期问题
					if (MallCommon.code_past.equals(map.get("code"))) {
						common.setLoading(new InterfaceMallReqIntert() {

							@Override
							public void setState(int state) {
								if (state >= UtilInternet.REQ_OK_STRING) {
									setRequestAddress();
								} else if (state == UtilInternet.REQ_CODE_ERROR) {
									loadManager.loadOver(state, 1, true);
								}
							}
						});
					}
				}

			
			}
		});
	}

	/**
	 * 解析地址数据
	 * 
	 * @param listMapByJson
	 */
	private void setParseInfoAddress(ArrayList<Map<String, String>> listMapByJson) {
		if (listMapByJson.size() > 0) {
			Map<String,String> map = null;
			for (int i = 0,size=listMapByJson.size(); i < size; i++) {
				if(listMapByJson.get(i).containsKey("address_type")&&"2".equals(listMapByJson.get(i).get("address_type")))
					map=listMapByJson.get(i);
			}
			if(map!=null){
				consignee_name = map.get("consumer_name");
				consignee_tel = map.get("consumer_mobile");
				consignee_address = map.get("address_detail");
				address_id = map.get("address_id");
			}else{
				Object msg = UtilFile.loadShared(this, FileManager.MALL_ADDRESS, FileManager.MALL_ADDRESS);
				ArrayList<Map<String, String>> list_address = UtilString.getListMapByJson(msg);
				if(list_address.size()>0){
					consignee_name = list_address.get(0).get("consumer_name");
					consignee_tel = list_address.get(0).get("consumer_mobile");
					consignee_address = list_address.get(0).get("address_detail");
					address_id = list_address.get(0).get("address_id");
				}else{
					consignee_name = listMapByJson.get(0).get("consumer_name");
					consignee_tel = listMapByJson.get(0).get("consumer_mobile");
					consignee_address = listMapByJson.get(0).get("address_detail");
					address_id = listMapByJson.get(0).get("address_id");
				}
			}
			findViewById(R.id.shoporder_consignee_linear_none).setVisibility(View.GONE);
			findViewById(R.id.shoporder_consignee_rela_data).setVisibility(View.VISIBLE);
			shoporder_consignee_man_name.setText(consignee_name);
			shoporder_consignee_man_number.setText(consignee_tel);
			shoporder_consignee_man_address.setText(consignee_address);
		} else {
			consignee_name = "";
			consignee_tel = "";
			consignee_address = "";
			address_id = "";
			findViewById(R.id.shoporder_consignee_linear_none).setVisibility(View.VISIBLE);
			findViewById(R.id.shoporder_consignee_rela_data).setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OK_ADDRESS:
			if (data != null) {
				findViewById(R.id.shoporder_consignee_linear_none).setVisibility(View.GONE);
				findViewById(R.id.shoporder_consignee_rela_data).setVisibility(View.VISIBLE);
				consignee_name = data.getStringExtra("consumer_name");
				consignee_tel = data.getStringExtra("consumer_mobile");
				consignee_address = data.getStringExtra("address_detail");
				address_id = data.getStringExtra("address_id");
				shoporder_consignee_man_name.setText(consignee_name);
				shoporder_consignee_man_number.setText(consignee_tel);
				shoporder_consignee_man_address.setText(consignee_address);
				saveChooseAddress(address_id, consignee_name, consignee_tel, consignee_address);
			} else {
				setRequestAddress();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.shoporder_commod_price_buy:// 去支付
			XHClick.track(this,"购买商品");
			setCreateOrderByCart();
			break;
		case R.id.shoporder_consignee_rela:// 获取收货人信息
			if(TextUtils.isEmpty(address_id)){
				setStatisticIndex();
				Intent intent = new Intent(this, AddressActivity.class);
				intent.putExtra("now_address_id", address_id);
				this.startActivityForResult(intent, OK_ADDRESS);
			}else{
				setStatisticIndex();
				Intent intent = new Intent(this, MallAddressChangeActivity.class);
				intent.putExtra("now_address_id", address_id);
				this.startActivityForResult(intent, OK_ADDRESS);
			}
			break;
		case R.id.pay_type_alipay:// 支付宝
			setPayType(false);
			break;
		case R.id.pay_type_wechat:// 微信
			setPayType(true);
			break;
		}
	}

	/**
	 * true:微信 false；支付
	 * 
	 * @param state_type
	 */
	private void setPayType(boolean state_type) {
		boolean state = false;
		if (state_type) {
			state = payType.setPayType("1");
		} else {
			state = payType.setPayType("2");
		}
		if (state) {
			setPayView();
		}
	}

	/**
	 * 设置支付view
	 */
	private void setPayView() {
		// 获取支付方式选择
		if ("1".equals(MallPayType.pay_type)) {
			pay_wechat.setImageResource(R.drawable.z_mall_shopcat_choose);
			pay_alipay.setImageResource(R.drawable.z_mall_shopcat_no_choose);
		} else {
			pay_wechat.setImageResource(R.drawable.z_mall_shopcat_no_choose);
			pay_alipay.setImageResource(R.drawable.z_mall_shopcat_choose);
		}
	}

	private void setCreateOrderByCart() {
		String type = "";
		if ("1".equals(MallPayType.pay_type)) {
			type = "wx";
		} else {
			type = "alipay";
		}
		//对收货人信息进行校验
		if(TextUtils.isEmpty(consignee_name)||TextUtils.isEmpty(consignee_tel)||TextUtils.isEmpty(consignee_address)){
			Tools.showToast(this, "请添加收货人信息");
			return ;
		}
		setStatisticIndex();
		findViewById(R.id.shoporder_commod_price_buy).setEnabled(false);
		setJsonMap(adapter.getData());
		try {
			JSONObject object_address = new JSONObject();
			object_address.put("consignee_name", consignee_name);
			object_address.put("consignee_tel", consignee_tel);
			object_address.put("consignee_address", consignee_address);
			String param = "order_info=" + order_info + "&shipping_info=" + object_address.toString() 
					+ "&address_id=" + address_id + "&pay_type=" + type+"&remark="+jsonArray.toString()
					+"&shop_coupon_info="+setFavorableList().toString();
                            //判断ds_from数据
                            if(!TextUtils.isEmpty(MallCommon.statictisFrom)){
                                param+="&ds_from="+MallCommon.getStatictisFrom();
                            }
                            Log.i("wyl","param:::"+param);
			Log.i("remark", jsonArray.toString());
			MallReqInternet.in().doPost(MallStringManager.mall_createOrderByCart_v2, param, new MallInternetCallback() {
				@Override
				public void loadstat(int flag, String url, Object msg, Object... stat) {
					if (flag >= UtilInternet.REQ_OK_STRING) {
						ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
						setTypeJson(listMapByJson);
						MallCommon.payment_order_id = listMapByJson.get(0).get("payment_order_id");
						//
					} else if (flag == UtilInternet.REQ_CODE_ERROR && msg instanceof Map) {
						Map<String, String> map = (Map<String, String>) msg;
						if ("5000001".equals(map.get("code"))) {// 有库存不足
							Tools.showToast(ShoppingOrderActivity.this, "无货");
						} else {
							// 处理code过期问题
							if (MallCommon.code_past.equals(map.get("code"))) {
								common.setLoading(new InterfaceMallReqIntert() {

									@Override
									public void setState(int state) {
										if (state >= UtilInternet.REQ_OK_STRING) {
											setCreateOrderByCart();
										} else if (state == UtilInternet.REQ_CODE_ERROR) {
											loadManager.loadOver(state, 1, true);
										}
									}
								});
							}
							Tools.showToast(ShoppingOrderActivity.this, map.get("msg"));
						}
					}
					findViewById(R.id.shoporder_commod_price_buy).setEnabled(true);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 去支付
	 * 
	 * @param listMapByJson
	 */
	private void setTypeJson(ArrayList<Map<String, String>> listMapByJson) {
		setResult(Activity.RESULT_OK);
		if ("1".equals(MallPayType.pay_type)) {// 微信
			WxPay pay = WxPay.getInstance(this);
			pay.pay(listMapByJson);
		} else {// 支付
			MallAlipay alipay = MallAlipay.getInstance();
			alipay.pay(this,listMapByJson);
		}
	}

//	/**
//	 * 设置用户留言
//	 */
//	private void setRemark(String name, String content) {
//		try {
//			if (jsonArray.length() > 0) {
//				for (int i = 0,length=jsonArray.length(); i < length; i++) {
//					JSONObject jsonObject=(JSONObject) jsonArray.get(i);
////					UtilString.getListMapByJson(jsonObject);
//					Map<String,String> map = (Map<String, String>) jsonObject;
//					if(name.equals(map.get("shop_code"))){
//						jsonArray.remove(i);
//						break;
//					}
//				}
//				addJson(name, content);
//			}else{
//				addJson(name, content);
//			}
//		addJson(name, content);
//		} catch (Exception e) {
//		}
//	}

	private void setJsonMap(List<? extends Map<String, ?>> list){
		for (int i = 0; i < list.size(); i++) {
			String content= String.valueOf(list.get(i).get("remarks"));
			String name=UtilString.getListMapByJson(list.get(i).get("shop_info")).get(0).get("shop_code");
			if(!TextUtils.isEmpty(content)&&!content.equals("null"))
				addJson(name, content);
		}
	}
	/**
	 * 添加
	 * @param name
	 * @param content
	 * @throws JSONException
	 */
	private void addJson(String name, String content) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("shop_code", name);
			jsonObject.put("remark", content);
			jsonArray.put(jsonObject);
			
		} catch (Exception e) {
		}
	}
	private void saveChooseAddress(String address_id,String consumer_name,String consumer_mobile,String address_detail){
		try {
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("address_id", address_id);
			jsonObject.put("consumer_name", consumer_name);
			jsonObject.put("consumer_mobile", consumer_mobile);
			jsonObject.put("address_detail", address_detail);
			UtilFile.saveShared(this, FileManager.MALL_ADDRESS, FileManager.MALL_ADDRESS, jsonObject.toString());
		} catch (Exception e) {
		}
	}
	/**
	 * 处理优惠券
	 * @return
	 */
	private JSONArray setFavorableList(){
		JSONArray jsonArray= new JSONArray();
		for (String key:map_favorable.keySet()) {
			jsonArray.put(map_favorable.get(key));
		}
		return jsonArray;
	}
	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, this);
	}
}
