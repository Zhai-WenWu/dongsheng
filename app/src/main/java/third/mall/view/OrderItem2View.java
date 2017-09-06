package third.mall.view;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import aplug.basic.ReqInternet;
import third.mall.activity.MyOrderActivity;
import third.mall.activity.OrderStateActivity;
import third.mall.activity.PublishEvalutionMultiActivity;
import third.mall.activity.PublishEvalutionSingleActivity;
import third.mall.activity.ShoppingActivity;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import third.mall.override.MallOrderBaseActivity;
import third.mall.view.MallButtonView.InterfaceViewCallback;
import third.mall.view.OrderItemView.InterfaceCallBack;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 子订单item样式
 *
 * @author yujian
 *
 */
public class OrderItem2View extends ViewItemBase {

	private MyOrderActivity activity;
	private RelativeLayout myorder_merchant_rela_2;
	private ImageView myorder_merchant_iv_2,order_logistics_back;
	private TextView myorder_merchant_name_2;
	private TextView myorder_merchant_state_2;
	private TextView myorder_check_logistics;

	private LinearLayout myorder_state_linear_2;
	private TextView myorder_state_order_tv_2;
	private TextView myorder_state_order_tv_explian_2;
	private TextView myorder_state_order_time_2;

	private RelativeLayout myorder_explian_rela_2;
	private ImageView myorder_explian_iv_2;
	private TextView myorder_explian_text_2;
	private HorizontalListView listView_2;

	private RelativeLayout myorder_price_rela_2;
	private TextView myorder_price_text_number_2;

	private ListView listview_item_2;
	private RelativeLayout rela_mall_order_2,myorder_explian_iv_2_rela;
	private LinearLayout myorder_button_linear;
//	private int id;
	private InterfaceCallBack callBack;
	private String url= "";
	private String mall_stat_statistic;

	public OrderItem2View(MyOrderActivity context) {
		super(context);
		this.activity = context;
		LayoutInflater.from(context).inflate(R.layout.a_mall_myorder_item_2, this, true);
		initView();
	}

	public void setInterfaceCallBack(InterfaceCallBack callBacks) {
		if (callBacks == null) {
			callBack = new InterfaceCallBack() {

				@Override
				public void delItem(int position) {

				}
			};
		} else
			this.callBack = callBacks;
	}
	public void setUrl(String url,String mall_stat_statistic){
		this.url=url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	private void initView() {
		// 商家
		myorder_merchant_rela_2 = (RelativeLayout) findViewById(R.id.myorder_merchant_rela_2);
		myorder_merchant_iv_2 = (ImageView) findViewById(R.id.myorder_merchant_iv_2);
		order_logistics_back = (ImageView) findViewById(R.id.order_logistics_back);
		order_logistics_back.setVisibility(View.GONE);
		myorder_merchant_name_2 = (TextView) findViewById(R.id.myorder_merchant_name_2);
		myorder_merchant_state_2 = (TextView) findViewById(R.id.myorder_merchant_state_2);
		myorder_check_logistics = (TextView) findViewById(R.id.myorder_check_logistics);
		// 订单状态.取消.退款.已发送
		myorder_state_linear_2 = (LinearLayout) findViewById(R.id.myorder_state_linear_2);
		myorder_state_order_tv_2 = (TextView) findViewById(R.id.myorder_state_order_tv_2);
		myorder_state_order_tv_explian_2 = (TextView) findViewById(R.id.myorder_state_order_tv_explian_2);
		myorder_state_order_time_2 = (TextView) findViewById(R.id.myorder_state_order_time_2);
		// 商品：单件
		myorder_explian_rela_2 = (RelativeLayout) findViewById(R.id.myorder_explian_rela_2);
		myorder_explian_iv_2_rela = (RelativeLayout) findViewById(R.id.myorder_explian_iv_2_rela);
		myorder_explian_iv_2 = (ImageView) findViewById(R.id.myorder_explian_iv_2);
		myorder_explian_text_2 = (TextView) findViewById(R.id.myorder_explian_text_2);
		// 商品：多件
		listView_2 = (HorizontalListView) findViewById(R.id.listview_2);
		// 价格
		myorder_price_rela_2 = (RelativeLayout) findViewById(R.id.myorder_price_rela_2);
		myorder_price_text_number_2 = (TextView) findViewById(R.id.myorder_price_text_number_2);
		myorder_button_linear = (LinearLayout) findViewById(R.id.myorder_button_linear);

		rela_mall_order_2 = (RelativeLayout) findViewById(R.id.rela_mall_order_2);
		rela_mall_order_2.setVisibility(VISIBLE);
	}

	public void setData(final Map<String, String> map, final int position, final int id) {
		// data
		myorder_price_text_number_2.setText(map.get("order_amt"));
		final ArrayList<Map<String, String>> listMapByJson_product = UtilString.getListMapByJson(map.get("order_product"));

		if(map.containsKey("shop_code")){
			order_logistics_back.setVisibility(View.VISIBLE );
			setShopOnClick(order_logistics_back, map);
			setShopOnClick(myorder_merchant_name_2, map);
			setShopOnClick(myorder_merchant_iv_2, map);
		}else{
			order_logistics_back.setVisibility(View.GONE);
		}
		setmerchant(true, map.get("shop_name"), myorder_merchant_name_2, myorder_merchant_iv_2);
		myorder_merchant_name_2.setText(map.get("shop_name"));

		if (listMapByJson_product.size() == 1) {
			setProductNum(myorder_explian_rela_2, myorder_explian_text_2, myorder_explian_iv_2, listView_2, false, null, listMapByJson_product.get(0).get("img"),
					listMapByJson_product.get(0).get("proudct_title"));
		} else if (listMapByJson_product.size() > 1) {
			ArrayList<String> list_product = new ArrayList<String>();
			for (int i = 0; i < listMapByJson_product.size(); i++) {
				list_product.add(listMapByJson_product.get(i).get("img"));
			}
			setProductNum(myorder_explian_rela_2, myorder_explian_text_2, myorder_explian_iv_2, listView_2, true, list_product, null, null);
		}
		// 点击事件
		listView_2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int positions, long ids) {
				setStatisticIndex();
				Intent intent = new Intent(activity, OrderStateActivity.class);
				intent.putExtra("order_id", map.get("order_id"));
				intent.putExtra("order_satus", "order");
				intent.putExtra("position", position);
				intent.putExtra("code", id);
				if(activity instanceof MallOrderBaseActivity) {
					intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
					intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "商品多件");
				}
				activity.startActivityForResult(intent, OrderStateActivity.request_order);
			}
		});
		String satus = map.get("order_status");
		String comment_status = map.get("comment_status");
		myorder_merchant_state_2.setText(map.get("order_status_desc"));// 描述
		MallButtonView buttonView = new MallButtonView(activity);
		myorder_button_linear.removeAllViews();
		myorder_check_logistics.setVisibility(GONE);
		if (satus.equals("1")) {// 待支付
			myorder_state_linear_2.setVisibility(View.GONE);
			String color = Tools.getColorStr(getContext(),R.color.comment_color);
			myorder_merchant_state_2.setTextColor(Color.parseColor(color));
			// 取消订单
			myorder_button_linear.addView(buttonView.createViewCancelOrder(new InterfaceViewCallback() {
				@Override
				public void sucessCallBack() {
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","取消订单");
				}
			}, map,url,mall_stat_statistic));
			// 去支付
			myorder_button_linear.addView(buttonView.createViewToPay(new InterfaceViewCallback() {
				@Override
				public void sucessCallBack() {
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","去支付");
				}
			}));
		} else if (satus.equals("2") || satus.equals("3")) {// 2已支付，3商家已确认
			myorder_state_linear_2.setVisibility(View.GONE);
			myorder_merchant_state_2.setTextColor(activity.getResources().getColor(R.color.comment_color));
		} else if (satus.equals("4")) {// 商家已发货--------订单显示。
			myorder_state_linear_2.setVisibility(View.VISIBLE);
			ArrayList<Map<String, String>> listMapByJson_order = UtilString.getListMapByJson(map.get("order_log"));
			myorder_state_order_time_2.setText(map.get("update_time"));
			myorder_state_order_tv_2.setText("快递：" + map.get("shipping_bill_no") + "(" + map.get("shipping_type") + ")");
			myorder_merchant_state_2.setTextColor(activity.getResources().getColor(R.color.comment_color));
			// 查看物流
			myorder_check_logistics.setVisibility(VISIBLE);
			// 确认收货
			myorder_button_linear.addView(buttonView.createViewReceipt(new InterfaceViewCallback() {
				@Override
				public void sucessCallBack() {
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","确认收货");
					activity.isRefresh = true;
					activity.ids.add(String.valueOf(id));
					if (id == 4) {
						callBack.delItem(position);
					} else {
						myorder_state_linear_2.setVisibility(View.GONE);
						map.put("order_status", "5");
						map.put("comment_status","1");
						map.put("order_status_desc", "已完成");
						myorder_merchant_state_2.setText("完成");
						myorder_merchant_state_2.setTextColor(Color.parseColor("#333333"));
						myorder_button_linear.removeAllViews();
						MallButtonView buttonView = new MallButtonView(activity);
						// 查看物流
						myorder_check_logistics.setVisibility(VISIBLE);
						// 再次购买
						myorder_button_linear.addView(buttonView.createViewRepeatOrder(new InterfaceViewCallback() {

							@Override
							public void sucessCallBack() {
								Intent intent = new Intent(activity, ShoppingActivity.class);
								if(activity instanceof MallOrderBaseActivity) {
									intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
									intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "再次购买-订单发货");
								}
								activity.startActivity(intent);
								XHClick.mapStat(activity, "a_mail_orders","按钮点击","再次购买");
							}
						}, map, buttonView.list_state_order,url,mall_stat_statistic));
						//评价
						myorder_button_linear.addView(buttonView.createViewComment(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
                                //去评价
                                gotoComment(map,listMapByJson_product,position,id);
							}
						}));
					}
				}
			}, map,url,mall_stat_statistic));
		} else if (satus.equals("5")) {// 已完成
			myorder_state_linear_2.setVisibility(View.GONE);
			myorder_merchant_state_2.setTextColor(activity.getResources().getColor(R.color.comment_color));
			// 查看物流
			myorder_check_logistics.setVisibility(VISIBLE);
			// 再次购买
			myorder_button_linear.addView(buttonView.createViewRepeatOrder(new InterfaceViewCallback() {

				@Override
				public void sucessCallBack() {
					Intent intent = new Intent(activity, ShoppingActivity.class);
					if(activity instanceof MallOrderBaseActivity) {
						intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
						intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "再次购买-订单完成");
					}
					activity.startActivity(intent);
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","再次购买");
				}
			}, map, buttonView.list_state_order,url,mall_stat_statistic));
            //已评价
			if("2".equals(comment_status)){
				myorder_button_linear.addView(buttonView.createViewCommented());
             //评价
			}else if("1".equals(comment_status)){
				myorder_button_linear.addView(buttonView.createViewComment(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						//去评价
                        gotoComment(map,listMapByJson_product,position,id);
					}
				}));
			}
		} else if (satus.equals("6") || satus.equals("8")) {// 商家已取消-------订单取消
			myorder_state_linear_2.setVisibility(View.VISIBLE);
			ArrayList<Map<String, String>> listMapByJson_order = UtilString.getListMapByJson(map.get("order_log"));
			myorder_state_order_time_2.setText(listMapByJson_order.get(0).get("log_time"));
			String log_remark = listMapByJson_order.get(0).get("log_remark");
			try {
				JSONArray jsonArray = new JSONArray(log_remark);
				// String
				// name=jsonArray.getString(0)+"("+jsonArray.getString(1)+")";
				String name = "";
				for (int i = 0; i < jsonArray.length(); i++) {
					if (i == 0) {
						name += jsonArray.getString(0);
					}
					if (i == 1) {
						name += jsonArray.getString(1);
					}
					// if(i>0&&i==jsonArray.length()-1){
					// name+=")";
					// }
				}
				myorder_state_order_tv_2.setText(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			myorder_merchant_state_2.setTextColor(activity.getResources().getColor(R.color.comment_color));
			// 再次购买
			myorder_button_linear.addView(buttonView.createViewRepeatOrder(new InterfaceViewCallback() {

				@Override
				public void sucessCallBack() {
					Intent intent = new Intent(activity,ShoppingActivity.class);
					if(activity instanceof MallOrderBaseActivity) {
						intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
						intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "再次购买-订单取消");
					}
					activity.startActivity(intent);
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","再次购买");
				}
			}, map, buttonView.list_state_order,url,mall_stat_statistic));
		} else if (satus.equals("7")) {// 订单支付超时---订单已退款
			myorder_state_linear_2.setVisibility(View.GONE);
			ArrayList<Map<String, String>> listMapByJson_order = UtilString.getListMapByJson(map.get("order_log"));
			myorder_state_order_time_2.setText(listMapByJson_order.get(0).get("log_time"));
			String log_remark = listMapByJson_order.get(0).get("log_remark");
			myorder_state_order_tv_2.setText(log_remark);
			myorder_merchant_state_2.setTextColor(activity.getResources().getColor(R.color.comment_color));
			// 删除订单
			myorder_button_linear.addView(buttonView.createViewDelOrder(new InterfaceViewCallback() {

				@Override
				public void sucessCallBack() {
					activity.isRefresh = true;
					activity.ids.add(String.valueOf(id));
					callBack.delItem(position);
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","删除订单");
				}
			}, map, buttonView.list_state_payment,url,mall_stat_statistic));
			// 再次购买
			myorder_button_linear.addView(buttonView.createViewRepeatOrder(new InterfaceViewCallback() {

				@Override
				public void sucessCallBack() {
					Intent intent = new Intent(activity, ShoppingActivity.class);
					if(activity instanceof MallOrderBaseActivity) {
						intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
						intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "再次购买-订单退款");
					}
					activity.startActivity(intent);
					XHClick.mapStat(activity, "a_mail_orders","按钮点击","再次购买");
				}
			}, map, buttonView.list_state_payment,url,mall_stat_statistic));
		}
		setListener(map, position,id);// 监听
	}

	private void setShopOnClick(View view,final Map<String, String> map){
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				XHClick.mapStat(activity, "a_mail_orders","点击店铺","");
				MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, activity);
				String mall_stat=(String) UtilFile.loadShared(activity, FileManager.MALL_STAT, FileManager.MALL_STAT);
				String url = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home) + "?shop_code=" + map.get("shop_code")+"&"+mall_stat;
				AppCommon.openUrl(activity, url, true);
			}
		});
	}

	/** 设置监听 */
	private void setListener(final Map<String, String> map, final int position,final int id) {
		// 全部点击去到订单详情页面
		rela_mall_order_2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setStatisticIndex();
				XHClick.mapStat(activity, "a_mail_orders","点击到订单详情页","");
				Intent intent = new Intent(activity, OrderStateActivity.class);
				intent.putExtra("order_id", map.get("order_id"));
				intent.putExtra("order_satus", "order");
				intent.putExtra("position", position);
				intent.putExtra("code", id);
				if(activity instanceof MallOrderBaseActivity) {
					intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
					intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "商品单件");
				}
				activity.startActivityForResult(intent, OrderStateActivity.request_order);
			}
		});

		myorder_check_logistics.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getShoppingurl(map,position);
				XHClick.mapStat(activity, "a_mail_orders","按钮点击","查看物流");
			}
		});
	}

	/** 物流信息 */
	private void getShoppingurl(Map<String, String> map,int position) {
		setStatisticIndex();
		url = MallStringManager.mall_getShippingUrl + "?order_id=" + map.get("order_id");
		MallReqInternet.in().doGet(url, new MallInternetCallback(activity) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if (flag >= ReqInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> listmap = UtilString.getListMapByJson(msg);
					String urls = listmap.get(0).get("url");
					AppCommon.openUrl(activity, urls, true);
				}

			}
		});
	}

	private void gotoComment(final Map<String, String> orderMap, ArrayList<Map<String, String>> productArray, final int position,final int id){
		if(0 == id){
			XHClick.mapStat(getContext(),XHClick.comcomment_icon,"我的订单-【全部】的评价按钮","");
		}else if(5 == id){
			XHClick.mapStat(getContext(),XHClick.comcomment_icon,"我的订单-【待评价】的评价按钮","");
		}
        //去评价
        if(productArray.size() == 1){
            gotoCommentSingle(orderMap , productArray.get(0) , position,id);
        }else if(productArray.size() > 1){
            gotoCommentMulti(orderMap, position,id);
        }
    }

    /**
     *
     * @param map
     * @param position
     */
	private void gotoCommentMulti(final Map<String, String> map, final int position ,int id){
		Intent intent = new Intent(activity, PublishEvalutionMultiActivity.class);
		intent.putExtra(PublishEvalutionMultiActivity.EXTRAS_ORDER_ID, map.get("order_id"));
		intent.putExtra(PublishEvalutionMultiActivity.EXTRAS_POSITION, position);
		intent.putExtra(PublishEvalutionMultiActivity.EXTRAS_ID, id);
		if(activity instanceof MallOrderBaseActivity) {
			intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
		}
		activity.startActivityForResult(intent, OrderStateActivity.request_order);
	}

    /**
     *
     * @param orderMap
     * @param productMap
     * @param position
     */
	private void gotoCommentSingle(final Map<String, String> orderMap,Map<String, String> productMap, final int position,int id){
		Intent intent = new Intent(activity, PublishEvalutionSingleActivity.class);
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ORDER_ID,orderMap.get("order_id"));
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_IMAGE,productMap.get("img"));
		intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_CODE,productMap.get("proudct_code"));
        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_POSITION, position);
        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ID, id);
		if(activity instanceof MallOrderBaseActivity) {
			intent.putExtra(MallBaseActivity.PAGE_FROM, activity.getNowFrom());
		}
		activity.startActivityForResult(intent, OrderStateActivity.request_order);
	}

	/**
	 * * 单图模式，多图模式
	 *
	 * @param isImageNum
	 *            true:多图 false:单图
	 * @param one
	 *            ---单图
	 * @param one_tv
	 *            ---单图文字
	 * @param one_iv
	 *            ---单图图片
	 * @param listview
	 *            ---多图
	 * @param strs
	 *            -----多图url数组
	 * @param url
	 *            ---单图图片数据
	 * @param value
	 *            ---单图文字数据
	 */
	private void setProductNum(RelativeLayout one, TextView one_tv, ImageView one_iv, HorizontalListView listview, boolean isImageNum, ArrayList<String> strs, String url, String value) {
		if (isImageNum) {// 多图
			ArrayList<Map<String, String>> list_data = new ArrayList<>();
			for (int i = 0; i < strs.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("img", strs.get(i));
				list_data.add(map);
			}
			one.setVisibility(View.GONE);
			myorder_explian_iv_2_rela.setVisibility(View.GONE);
			listview.setVisibility(View.VISIBLE);
			AdapterSimple adapter_list = new AdapterSimple(listview, list_data, R.layout.a_mall_myorder_list_item, new String[] { "img" }, new int[] { R.id.imageview_list });
			listview.setAdapter(adapter_list);
		} else {// 单图
			one.setVisibility(View.VISIBLE);
			myorder_explian_iv_2_rela.setVisibility(View.VISIBLE);
			listview.setVisibility(View.GONE);
			setViewImage(one_iv, url);
			one_tv.setText(value);
		}
	}

	/**
	 * 设置商品名称，图标
	 *
	 * @param state
	 *            ---true:商家，false：香哈
	 * @param value
	 *            ---商家名称
	 */
	private void setmerchant(boolean state, String value, TextView tv, ImageView view) {
		if (state) {// 商家
			tv.setText(value);
			view.setBackgroundResource(R.drawable.mall_buycommod_commod_merchant_iv);
		} else {// 香哈
			tv.setText("香哈");
			view.setBackgroundResource(R.drawable.mall_myorder_myself);
		}
	}

	/** 对电商按钮进行统计 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, activity);
	}
}
