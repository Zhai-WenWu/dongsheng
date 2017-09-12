package third.mall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.PageStatisticsUtils;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.ReqInternet;
import third.mall.adapter.AdapterOrderState;
import third.mall.adapter.AdapterShopRecommed;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallPayState;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallPayType;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import third.mall.view.MallButtonView;
import third.mall.view.MallButtonView.InterfaceViewCallback;
import third.mall.widget.ListViewForScrollView;
import third.mall.widget.MyGridView;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 订单状态--详情
 * @author yu
 *
 */
public class OrderStateActivity extends MallBaseActivity implements OnClickListener{
	public static final int request_order=2000;
	public static final int result_del= 2001;
	public static final int result_cancel= 2002;
	public static final int result_sure= 2003;
	public static final int result_comment_success = 2004;
	public static final int result_comment_part_success = 2005;

	private String order_id;
	private String order_satus;
	private ListViewForScrollView listview;
	private ArrayList<Map<String, String>> listData ;
	private AdapterOrderState adapter;
	private ArrayList<Map<String, String>> listMapByJson,listMapByJson_payment;
	private TextView buycommod_consignee_man_name,buycommod_consignee_man_number,buycommod_consignee_man_address
	,buycommod_order_number_text,copy_order_number_text,buycommod_commod_price_end;
	private Handler handler;
	private static final int SHOW_OK=1;
	private String status;
	private MallCommon common;
	private ImageView order_logistics_iv;
	private TextView order_logistics_now_content;
	private TextView order_logistics_now_time;
	private ArrayList<Map<String,String>> list_recommend= new ArrayList<>();
	private ImageView pay_wechat;
	private ImageView pay_alipay;
	private MallPayType payType;
	private TextView tv_status;
	private LinearLayout order_status_linear;
	View viewpay ;
	private int code = -1;
	private int position = -1;
	private int state_now;//当前状态
	private String url_statistic;
	private String mall_stat_statistic;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Bundle bundle= getIntent().getExtras();
		if(bundle!=null){
			order_id= bundle.getString("order_id");
			order_satus= bundle.getString("order_satus");
			code= bundle.getInt("code");
			position= bundle.getInt("position");
		}
		initActivity("订单信息", 3, 0, 0, R.layout.a_mall_order_explian);
		common=new MallCommon(this);
		payType= new MallPayType(this);
		initView();
		initData();
		XHClick.track(this,"浏览订单详情");
	}

	private void initView() {
		TextView title=(TextView)findViewById(R.id.title);
		title.setText("订单信息");
		tv_status=(TextView) findViewById(R.id.tv_status);
		findViewById(R.id.back).setOnClickListener(this);
		listview = (ListViewForScrollView) findViewById(R.id.listview);
		//收货地址
		buycommod_consignee_man_name = (TextView) findViewById(R.id.buycommod_consignee_man_name);
		buycommod_consignee_man_number = (TextView) findViewById(R.id.buycommod_consignee_man_number);
		buycommod_consignee_man_address = (TextView) findViewById(R.id.buycommod_consignee_man_address);
		//订单号
		buycommod_order_number_text =(TextView) findViewById(R.id.buycommod_order_number_text);
		copy_order_number_text = (TextView) findViewById(R.id.copy_order_number_text);
		//价格
		buycommod_commod_price_end=(TextView) findViewById(R.id.buycommod_commod_price_end);
		
		findViewById(R.id.buycommod_consignee_rela).setVisibility(View.GONE);
		findViewById(R.id.price_bata_rela).setVisibility(View.GONE);
		findViewById(R.id.buycommod_rela).setVisibility(View.GONE);
		//物流信息
		findViewById(R.id.order_logistics_rela).setVisibility(View.GONE);
		findViewById(R.id.order_logistics_rela).setOnClickListener(this);
		order_logistics_now_content = (TextView) findViewById(R.id.order_logistics_now_content);
		order_logistics_now_time = (TextView) findViewById(R.id.order_logistics_now_time);
		
		findViewById(R.id.product_recomend_rela).setVisibility(View.GONE);
		//支付方式选择
		findViewById(R.id.pay_type_alipay).setOnClickListener(this);
		findViewById(R.id.pay_type_wechat).setOnClickListener(this);
		pay_wechat = (ImageView) findViewById(R.id.pay_wechat);
		pay_alipay = (ImageView) findViewById(R.id.pay_alipay);
		order_status_linear=(LinearLayout) findViewById(R.id.order_status_linear);
	}

	private void initData() {
		loadManager.showProgressBar();
		listData= new ArrayList<>();
		adapter = new AdapterOrderState(this,listview, listData, R.layout.a_mall_order_item, 
				new String[]{},new int[]{});
		listview.setDivider(null);
		adapter.scaleType=ScaleType.CENTER_CROP;
		listview.setAdapter(adapter);
		
		handler= new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SHOW_OK:
					loadManager.hideProgressBar();
					findViewById(R.id.buycommod_rela).setVisibility(View.VISIBLE);
					findViewById(R.id.buycommod_consignee_rela).setVisibility(View.VISIBLE);
					findViewById(R.id.price_bata_rela).setVisibility(View.VISIBLE);
					remeasureGridView();
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};
		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRequest();
			}
		});
		//设置支付状态
		setPayView();
	}

	/**
	 * 处理view
	 * @param view 底部按钮中的textview
	 */
	private void setButtonViewLayout(View view){
		int dp_69= (int) getResources().getDimension(R.dimen.dp_69);
		int dp_27= (int) getResources().getDimension(R.dimen.dp_27);
		int dp_5= (int) getResources().getDimension(R.dimen.dp_5);
		RelativeLayout.LayoutParams layout= new LayoutParams(dp_69,dp_27);
		layout.setMargins(dp_5, 0, 0, 0);
		view.setLayoutParams(layout);
		((TextView)view).setTextSize(Tools.getDimenSp(this, R.dimen.sp_13));
	}

	private void setEvalutionViewLayout(View view){
		int dp_56= (int) getResources().getDimension(R.dimen.dp_56);
		int dp_27= (int) getResources().getDimension(R.dimen.dp_27);
		int dp_5= (int) getResources().getDimension(R.dimen.dp_5);
		RelativeLayout.LayoutParams layout= new LayoutParams(dp_56,dp_27);
		layout.setMargins(dp_5, 0, 0, 0);
		view.setLayoutParams(layout);
		((TextView)view).setTextSize(Tools.getDimenSp(this, R.dimen.sp_13));
	}

	private void setRequest() {
		url_statistic =MallStringManager.mall_api_order_info_v2+"?type="+order_satus+"&id="+order_id;
		MallReqInternet.in().doGet(url_statistic, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {

				loadManager.loadOver(flag, 1,true);
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String, String>> listMapByJson_one =UtilString.getListMapByJson(msg);
					listMapByJson =UtilString.getListMapByJson(listMapByJson_one.get(0).get("order_info"));
					if("order".equals(order_satus)){
						parseInfoOrder(listMapByJson);
						listMapByJson_payment=listMapByJson;
					}else if("payment_order".equals(order_satus)){
						parseInfoPaymentOrder(listMapByJson);
						listMapByJson_payment=UtilString.getListMapByJson(listMapByJson.get(0).get("order_list"));
					}
					setOrderStatus(Integer.parseInt(status), listMapByJson.get(0));
					for (int i = 0; i < listMapByJson_payment.size(); i++) {
						listData.add(listMapByJson_payment.get(i));
					}
					if(listMapByJson_one.get(0).containsKey("recommend_product")){
						ArrayList<Map<String, String>> list= UtilString.getListMapByJson(listMapByJson_one.get(0).get("recommend_product"));
						for (int j = 0; j < list.size(); j++) {
							list_recommend.add(list.get(j));
						}
						setRecommendProduct();
					}
					adapter.setUrl(url_statistic,mall_stat_statistic);
					adapter.notifyDataSetChanged();
					handler.sendEmptyMessage(SHOW_OK);
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setRequest();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.order_logistics_rela://物流信息
			XHClick.mapStat(this, "a_mail_order","物流信息","");
			setStatisticIndex();
			getShoppingurl();
			break;
		case R.id.pay_type_alipay://支付宝
			setPayType(false);
			break;
		case R.id.pay_type_wechat://微信
			setPayType(true);
			break;
		}
	}

	/** 去支付 */
	private void setToPay(final View view){
		MallCommon.payment_order_id="";
		view.setEnabled(false);
		setStatisticIndex();
		if("order".equals(order_satus)){
			common.malldirect(listMapByJson_payment.get(0).get("order_id"),this ,new InterfaceMallPayState(){
				@Override
				public void payState(int state) {
					view.setEnabled(true);
				}
			});
		}else if("payment_order".equals(order_satus)){
			common.malldirect(listMapByJson.get(0).get("payment_order_id"),this,new InterfaceMallPayState(){
				@Override
				public void payState(int state) {
					view.setEnabled(true);
				}
			});
		}
	}

	/**
	 * 设置支付类型
	 * @param state_type
	 * 				true:微信 false；支付宝
	 */
	private void setPayType(boolean state_type){
		boolean state = payType.setPayType(state_type ? "1" : "2");

		if(state)
			setPayView();
	}

	/** 设置支付view */
	private void setPayView(){
		//获取支付方式选择
		if("1".equals(MallPayType.pay_type)){
			pay_wechat.setImageResource(R.drawable.z_mall_shopcat_choose);
			pay_alipay.setImageResource(R.drawable.z_mall_shopcat_no_choose);
		}else{
			pay_wechat.setImageResource(R.drawable.z_mall_shopcat_no_choose);
			pay_alipay.setImageResource(R.drawable.z_mall_shopcat_choose);
		}
	}

	/**
	 * order类型解析
	 * @param listMapByJson
	 */
	private void parseInfoOrder(ArrayList<Map<String, String>> listMapByJson) {
		
		buycommod_consignee_man_name.setText(listMapByJson.get(0).get("consignee_name"));
		buycommod_consignee_man_number.setText(listMapByJson.get(0).get("consignee_tel"));
		buycommod_consignee_man_address.setText(listMapByJson.get(0).get("consignee_address"));
		buycommod_commod_price_end.setText("合计：¥"+listMapByJson.get(0).get("order_amt"));
		status= listMapByJson.get(0).get("status");
		String name= listMapByJson.get(0).get("order_status_desc");
		setStatus(status,name);
		if("4".equals(status)||"5".equals(status)){
			findViewById(R.id.order_logistics_rela).setVisibility(View.VISIBLE);
			order_logistics_now_content.setText("物流信息："+listMapByJson.get(0).get("shipping_bill_no")+"("+listMapByJson.get(0).get("shipping_type")+")");
			order_logistics_now_time.setText(listMapByJson.get(0).get("update_time"));
		}else{
			findViewById(R.id.order_logistics_rela).setVisibility(View.GONE);
		}
		setOrderNumberShowView(listMapByJson.get(0));
	}

	/**
	 * PaymentOrder解析
	 * @param listMapByJson
	 */
	private void parseInfoPaymentOrder(ArrayList<Map<String, String>> listMapByJson) {
		ArrayList<Map<String, String>> listMap_order= UtilString.getListMapByJson(listMapByJson.get(0).get("order_list"));
		if(listMap_order.size()>0){
			buycommod_consignee_man_name.setText(listMap_order.get(0).get("consignee_name"));
			buycommod_consignee_man_number.setText(listMap_order.get(0).get("consignee_tel"));
			buycommod_consignee_man_address.setText(listMap_order.get(0).get("consignee_address"));
		}
		buycommod_commod_price_end.setText("合计：¥"+listMapByJson.get(0).get("amt"));
		status= listMapByJson.get(0).get("status");
		String name= listMapByJson.get(0).get("payment_order_status_desc");
		setStatus(status,name);
		setOrderNumberShowView(listMapByJson.get(0));
	}
	
	private void setOrderNumberShowView(final Map<String,String> map){
		//订单
		String data="订单号：";
		if("order".equals(order_satus)){
			data+=map.get("order_id");
			if(map.containsKey("payment_type_desc")&&map.containsKey("order_timing_info")){
				if(!TextUtils.isEmpty(map.get("payment_type_desc"))||!TextUtils.isEmpty(map.get("order_timing_info"))){
					buycommod_order_number_text.setVisibility(View.VISIBLE);
                    setCopyText(map.get("order_id"));
					if(!TextUtils.isEmpty(map.get("payment_type_desc"))){
						data+="\n支付方式："+map.get("payment_type_desc");
					}
					if(!TextUtils.isEmpty(map.get("order_timing_info"))){
						String temp=map.get("order_timing_info");
						try {
							JSONArray jsonArray = new JSONArray(temp);
							for (int i = 0,size=jsonArray.length(); i < size; i++) {
								data+="\n"+jsonArray.getString(i);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					
				}
			}
		}else if("payment_order".equals(order_satus)){
			data+=map.get("payment_order_id");
			data+="\n下单时间："+map.get("create_time");
            setCopyText(map.get("payment_order_id"));
		}
		buycommod_order_number_text.setText(data);
	}

	private void setCopyText(final String text){
        if(TextUtils.isEmpty(text)){
            copy_order_number_text.setVisibility(View.GONE);
            return;
        }
        copy_order_number_text.setVisibility(View.VISIBLE);
        copy_order_number_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.inputToClipboard(OrderStateActivity.this,text);
                Tools.showToast(OrderStateActivity.this,"复制成功");
            }
        });
    }

	/**
	 * 设置状态
	 * @param status true:显示  false:隐藏
	 */
	private void setStatus(String status,String name){
		tv_status.setText(name);
		findViewById(R.id.order_pay).setVisibility(status.equals("1")?View.VISIBLE:View.GONE);
	}
	
	/** 物流信息 */
	private void getShoppingurl(){
		url_statistic = MallStringManager.mall_getShippingUrl+"?order_id="+listMapByJson_payment.get(0).get("order_id");
		MallReqInternet.in().doGet(url_statistic, new MallInternetCallback(this) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					ArrayList<Map<String,String>> listmap= UtilString.getListMapByJson(msg);
					String urls=listmap.get(0).get("url");
					AppCommon.openUrl(OrderStateActivity.this, urls, true);
				}
			
			}
		});
	}

	boolean remeasure = false;
	AdapterShopRecommed recommend;
	GridView gridview;

	private void setRecommendProduct(){
		if(list_recommend.size()>0){
			gridview =(GridView) findViewById(R.id.gridview);
			recommend = new AdapterShopRecommed(this,gridview, list_recommend, R.layout.a_mall_shop_recommend_item_grid, new String[]{}, new int[]{},"a_mail_order");
			gridview.setAdapter(recommend);
			gridview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					XHClick.mapStat(OrderStateActivity.this, "a_mail_order","你可能喜欢","点击商品");
					setStatisticIndex();
					Intent intent = new Intent(OrderStateActivity.this,CommodDetailActivity.class);
					intent.putExtra("product_code", list_recommend.get(position).get("product_code"));
					OrderStateActivity.this.startActivity(intent);
				}
			});
			findViewById(R.id.product_recomend_rela).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.product_recomend_rela).setVisibility(View.GONE);
		}
	}

	private void remeasureGridView(){
		gridview.post(new Runnable() {
			@Override
			public void run() {
				if(!remeasure){
					remeasure = true;
					int totalHeight = 0;
					int length = recommend.getCount() / gridview.getNumColumns();
					if(recommend.getCount() % gridview.getNumColumns() > 0){
						length++;
					}
					for(int i = 0 ; i < length ; i ++){
						View listItem = recommend.getView(i*gridview.getNumColumns(), null, gridview);
						listItem.measure(0, 0);
						totalHeight += listItem.getMeasuredHeight();
					}

					ViewGroup.LayoutParams params = gridview.getLayoutParams();
					params.height = totalHeight + Tools.getDimen(OrderStateActivity.this,R.dimen.dp_40) + length * gridview.getVerticalSpacing();
					gridview.setLayoutParams(params);
				}
			}
		});
	}
	
	private void setOrderStatus(int status,final Map<String,String> map){
		MallButtonView buttonView= new MallButtonView(this);

		if("payment_order".equals(order_satus)){//未拆单之前的样子
			switch (status) {
			case 1:
				//取消订单
				View view_cancel= buttonView.createViewCancelOrder(new InterfaceViewCallback() {
					
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","取消订单");
						Log.i("取消订单", "取消订单");
						state_now= result_cancel;
						order_status_linear.removeAllViews();
						tv_status.setText("已取消");// 描述
						MallButtonView buttonView =new MallButtonView(OrderStateActivity.this);
						View view_del=buttonView.createViewDelOrder(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","删除订单");
								Log.i("删除3", "删除出3");
								state_now= result_del;
								OrderStateActivity.this.finish();
							}
						}, map,MallButtonView.detail_state_payment,url_statistic,mall_stat_statistic);
						setButtonViewLayout(view_del.findViewById(R.id.textview));
						order_status_linear.addView(view_del);
						
						View view_repeat=buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","再次购买");
								Log.i("去购物车3", "去购物车3");
								Intent intent= new Intent(OrderStateActivity.this,ShoppingActivity.class);
								OrderStateActivity.this.startActivity(intent);
							}
						}, map, MallButtonView.detail_state_payment,url_statistic,mall_stat_statistic);
						setButtonViewLayout(view_repeat.findViewById(R.id.textview));
						order_status_linear.addView(view_repeat);
							
					}
				}, map,url_statistic,mall_stat_statistic);
				setButtonViewLayout(view_cancel.findViewById(R.id.textview));
				order_status_linear.addView(view_cancel);
				viewpay=buttonView.createViewPay(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","去支付");
						XHClick.track(OrderStateActivity.this,"购买商品");
						setToPay(viewpay);
					}
				}, map);
				setButtonViewLayout(viewpay.findViewById(R.id.textview));
				order_status_linear.addView(viewpay);
				break;
			case 2:
				
				break;
			default:
				ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
				String order_status= listMapByJson.get(0).get("status"); 
				if(!TextUtils.isEmpty(order_status))
					if(7==Integer.parseInt(order_status)){
						View view_del=buttonView.createViewDelOrder(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","删除订单");
								state_now= result_del;
								OrderStateActivity.this.finish();
							}
						}, map,MallButtonView.detail_state_payment,url_statistic,mall_stat_statistic);
						setButtonViewLayout(view_del.findViewById(R.id.textview));
						order_status_linear.addView(view_del);
						
						View view_repeat=buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","再次购买");
								Intent intent= new Intent(OrderStateActivity.this,ShoppingActivity.class);
                                                                        MallCommon.statictisFrom+=TextUtils.isEmpty(MallCommon.statictisFrom)?"再次购买":PAGE_LOGO+"再次购买";
								OrderStateActivity.this.startActivity(intent);
							}
						}, map, MallButtonView.detail_state_payment,url_statistic,mall_stat_statistic);
						setButtonViewLayout(view_repeat.findViewById(R.id.textview));
						order_status_linear.addView(view_repeat);
					}
				break;
			}
		}else if("order".equals(order_satus)){//拆单
			switch (status) {
			case 1://待付款
//				View view_cancel=buttonView.createViewCancelOrder(new InterfaceViewCallback() {
//					@Override
//					public void sucessCallBack() {
//					}
//				}, map);
//				setButtonViewLayout(view_cancel.findViewById(R.id.textview));
//				order_status_linear.addView(view_cancel);
//				//需测试
//				viewpay=buttonView.createViewPay(new InterfaceViewCallback() {
//					@Override
//					public void sucessCallBack() {
//						setToPay(viewpay);
//					}
//				}, map);
//				setButtonViewLayout(viewpay.findViewById(R.id.textview));
//				order_status_linear.addView(viewpay);
				break;
			case 2://已支付
			case 3://已确认
				break;
			case 4://已发货
				View view_log=buttonView.createViewGoLog(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						getShoppingurl();
					}
				});
				setButtonViewLayout(view_log.findViewById(R.id.textview));
				order_status_linear.addView(view_log);
				
				View view_receipt=buttonView.createViewReceipt(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						state_now= result_sure;
						map.put("status", "5");
						map.put("order_status_desc", "已完成");
						tv_status.setText("已完成");// 描述
						order_status_linear.removeAllViews();
						MallButtonView buttonView= new MallButtonView(OrderStateActivity.this);
						//查看物流
						View view_go= buttonView.createViewGoLog(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","查看物流");
								getShoppingurl();
							}
						});
						setButtonViewLayout(view_go.findViewById(R.id.textview));
						order_status_linear.addView(view_go);
						//再次购买
						View view_repeat=buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
							
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","再次购买");
								goShopping("再次购买-已发货");
							}
						}, map, MallButtonView.detail_state_order,url_statistic,mall_stat_statistic);
						setButtonViewLayout(view_repeat.findViewById(R.id.textview));
						order_status_linear.addView(view_repeat);

						View view_comment = buttonView.createViewComment(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								ArrayList<Map<String,String>> productArr = StringManager.getListMapByJson(map.get("order_product"));
								gotoComment(map,productArr);
							}
						});
						setEvalutionViewLayout(view_comment.findViewById(R.id.textview));
						order_status_linear.addView(view_comment);
					}
				}, map,url_statistic,mall_stat_statistic);
				setButtonViewLayout(view_receipt.findViewById(R.id.textview));
				order_status_linear.addView(view_receipt);
				break;
			case 5://已完成
				View view_log1=buttonView.createViewGoLog(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","查看物流");
						getShoppingurl();
					}
				});
				setButtonViewLayout(view_log1.findViewById(R.id.textview));
				order_status_linear.addView(view_log1);
				
				View view_reqeat= buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","再次购买");
						goShopping("再次购买-已完成");
					}
				}, map, MallButtonView.detail_state_order,url_statistic,mall_stat_statistic);
				setButtonViewLayout(view_reqeat.findViewById(R.id.textview));
				order_status_linear.addView(view_reqeat);

				if("1".equals(map.get("comment_status"))){
					View view_comment = buttonView.createViewComment(new InterfaceViewCallback() {
						@Override
						public void sucessCallBack() {
							ArrayList<Map<String,String>> productArr = StringManager.getListMapByJson(map.get("order_product"));
							gotoComment(map,productArr);
						}
					});
					setEvalutionViewLayout(view_comment.findViewById(R.id.textview));
					order_status_linear.addView(view_comment);
//				}else if("2".equals(map.get("comment_status"))){
//					View view_commented = buttonView.createViewCommented();
//					setEvalutionViewLayout(view_commented.findViewById(R.id.textview));
//					order_status_linear.addView(view_commented);
				}
				break;
			case 6://已取消
			case 8://已退款
				View view_reqeat1= buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","再次购买");
						goShopping("再次购买-取消或退款");
					}
				}, map, MallButtonView.detail_state_order,url_statistic,mall_stat_statistic);
				setButtonViewLayout(view_reqeat1.findViewById(R.id.textview));
				order_status_linear.addView(view_reqeat1);
				break;
			case 7://订单超时
				View view_del= buttonView.createViewDelOrder(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","删除订单");
						state_now= result_del;
						OrderStateActivity.this.finish();
					}
				}, map, MallButtonView.detail_state_payment,url_statistic,mall_stat_statistic);
				setButtonViewLayout(view_del.findViewById(R.id.textview));
				order_status_linear.addView(view_del);
				View view_reqeat2= buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						XHClick.mapStat(OrderStateActivity.this, "a_mail_order","底部按钮点击","再次购买");
						goShopping("再次购买-订单超时");
					}
				}, map, MallButtonView.detail_state_payment,url_statistic,mall_stat_statistic);
				setButtonViewLayout(view_reqeat2.findViewById(R.id.textview));
				order_status_linear.addView(view_reqeat2);
				break;
	
			}
		}
	}

	/** 去购物车 */
	private void goShopping(String info){
		Intent intent= new Intent(OrderStateActivity.this,ShoppingActivity.class);
		OrderStateActivity.this.startActivity(intent);
	}

	/**
	 * 去评价
	 * @param orderMap 订单数据
	 * @param productArray 所有商品数据
	 */
	private void gotoComment(final Map<String, String> orderMap, ArrayList<Map<String, String>> productArray){
		XHClick.mapStat(this,XHClick.comcomment_icon,"订单详情-评价按钮","");
		if(productArray.size() == 1){
			Map<String, String> productMap = StringManager.getFirstMap(productArray.get(0).get("info"));
			if(TextUtils.isEmpty(productMap.get("img"))){
				productMap.put("img",productArray.get(0).get("img"));
			}
			gotoCommentSingle(orderMap , productMap);
		}else if(productArray.size() > 1){
			gotoCommentMulti(orderMap);
		}
	}

	/**
	 * 发布评论列表
	 * @param map 订单数据
	 */
	private void gotoCommentMulti(final Map<String, String> map){
		Intent intent = new Intent(this, PublishEvalutionMultiActivity.class);
		intent.putExtra(PublishEvalutionMultiActivity.EXTRAS_ORDER_ID, map.get("order_id"));
		intent.putExtra(PublishEvalutionMultiActivity.EXTRAS_POSITION, position);
		intent.putExtra(PublishEvalutionMultiActivity.EXTRAS_ID, code);
		startActivityForResult(intent, OrderStateActivity.request_order);
	}

	/**
	 * 发布评论
	 * @param orderMap 订单数据
	 * @param productMap 商品数据
	 */
	private void gotoCommentSingle(final Map<String, String> orderMap,Map<String, String> productMap){
		Intent intent = new Intent(this, PublishEvalutionSingleActivity.class);
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ORDER_ID,orderMap.get("order_id"));
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_CODE,productMap.get("product_code"));
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_IMAGE,productMap.get("img"));
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_POSITION, position);
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ID, code);
		startActivityForResult(intent, OrderStateActivity.request_order);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OrderStateActivity.request_order){
			if(resultCode == OrderStateActivity.result_comment_success){
				state_now = OrderStateActivity.result_comment_success;
				updateEvalutionStatus();//改变评价按钮状态
			}
		}
	}

	/**改变评价按钮状态*/
	private void updateEvalutionStatus(){
		order_status_linear.removeViewAt(order_status_linear.getChildCount() - 1);
//		MallButtonView buttonView= new MallButtonView(this);
//		View view_commented = buttonView.createViewCommented();
//		setEvalutionViewLayout(view_commented.findViewById(R.id.textview));
//		order_status_linear.addView(view_commented);
	}

	@Override
	public void finish() {
		if(code != -1 && position != -1){
			Intent intent= new Intent();
			intent.putExtra("code", String.valueOf(code));
			intent.putExtra("position", String.valueOf(position));
			setResult(state_now, intent);
		}else{
			setResult(state_now);
		}
		super.finish();
	}

	/** 对电商按钮进行统计 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url_statistic, null,mall_stat_statistic, this);
	}
}
