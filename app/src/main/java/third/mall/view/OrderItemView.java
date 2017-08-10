package third.mall.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import third.mall.activity.MyOrderActivity;
import third.mall.activity.OrderStateActivity;
import third.mall.activity.ShoppingActivity;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallStringManager;
import third.mall.view.MallButtonView.InterfaceViewCallback;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 订单第一次层级
 * @author Administrator
 *
 */
public class OrderItemView extends ViewItemBase {

	private MyOrderActivity context;
	private Map<String, String> map;
	private RelativeLayout myorder_merchant_rela;
	private ImageView myorder_merchant_iv,order_logistics_back;
	private TextView myorder_merchant_name;
	private TextView myorder_merchant_state;

	private LinearLayout myorder_state_linear;
	private TextView myorder_state_order_tv;
	private TextView myorder_state_order_tv_explian;
	private TextView myorder_state_order_time;

	private RelativeLayout myorder_explian_rela;
	private ImageView myorder_explian_iv;
	private TextView myorder_explian_text;
	private HorizontalListView listView;

	private RelativeLayout myorder_price_rela;
	private TextView myorder_price_text_number;

	private ListView listview_item;
	private RelativeLayout rela_mall_order;
	private RelativeLayout myorder_commod_rela;
	private LinearLayout myorder_but_linear;
	private RelativeLayout myorder_item_rela;
	private int id;
	private int position;
	private InterfaceCallBack callBack;
	private RelativeLayout myorder_explian_iv_rela;
	private Map<String, String> map_order;
	private String url;
	private String mall_stat_statistic;

	public OrderItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OrderItemView(MyOrderActivity context) {
		super(context);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.a_mall_myorder_item, this, true);
		initView();
	}
	/**
	 * 设置监听接口
	 * @param callBacks
	 */
	public void setInterfaceCallBack(InterfaceCallBack callBacks){
		if(callBacks==null){
			callBack= new InterfaceCallBack() {
				@Override
				public void delItem(int position) {
				}
			};
		}else this.callBack=callBacks;
	}

	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	/**
	 * 初始化view
	 */
	private void initView() {
		// 商家
		myorder_merchant_rela = (RelativeLayout) findViewById(R.id.myorder_merchant_rela);
		myorder_merchant_iv = (ImageView) findViewById(R.id.myorder_merchant_iv);
		order_logistics_back = (ImageView) findViewById(R.id.order_logistics_back);
		order_logistics_back.setVisibility(View.GONE);
		myorder_merchant_name = (TextView) findViewById(R.id.myorder_merchant_name);
		myorder_merchant_state = (TextView) findViewById(R.id.myorder_merchant_state);
		// 订单状态.取消.退款.已发送
		myorder_state_linear = (LinearLayout) findViewById(R.id.myorder_state_linear);
		myorder_state_order_tv = (TextView) findViewById(R.id.myorder_state_order_tv);
		myorder_state_order_tv_explian = (TextView) findViewById(R.id.myorder_state_order_tv_explian);
		myorder_state_order_time = (TextView) findViewById(R.id.myorder_state_order_time);
		// 商品：单件
		myorder_explian_rela = (RelativeLayout) findViewById(R.id.myorder_explian_rela);
		myorder_explian_iv_rela=(RelativeLayout) findViewById(R.id.myorder_explian_iv_rela);
		myorder_explian_iv = (ImageView) findViewById(R.id.myorder_explian_iv);
		myorder_explian_text = (TextView) findViewById(R.id.myorder_explian_text);
		// 商品：多件
		listView = (HorizontalListView) findViewById(R.id.listview);
		// 价格
		myorder_price_rela = (RelativeLayout) findViewById(R.id.myorder_price_rela);
		myorder_price_text_number = (TextView) findViewById(R.id.myorder_price_text_number);
		myorder_but_linear = (LinearLayout) findViewById(R.id.myorder_but_linear);

		listview_item = (ListView) findViewById(R.id.listview_item);
		rela_mall_order = (RelativeLayout) findViewById(R.id.rela_mall_order);
		myorder_commod_rela = (RelativeLayout) findViewById(R.id.myorder_commod_rela);
		myorder_item_rela = (RelativeLayout) findViewById(R.id.myorder_item_rela);
	}

	/**
	 * 初始化数据
	 */
	public void setData(final Map<String, String> map, final int position,final int id) {
		// data
		this.id= id;
		myorder_price_text_number.setText("¥:" + map.get("amt"));// 价格
		String payment_order_satus = map.get("payment_order_status");
		myorder_state_linear.setVisibility(View.GONE);
		MallButtonView buttonView = new MallButtonView(context);
		myorder_but_linear.removeAllViews();
		int dp_15 = (int) context.getResources().getDimension(R.dimen.dp_15);
		myorder_item_rela.setPadding(0, dp_15, 0, 0);

		//对单子状态进行区分
		if (payment_order_satus.equals("1")) {// 未收款---未拆单-----一个item
			listview_item.setVisibility(View.GONE);
			rela_mall_order.setVisibility(View.VISIBLE);
			// ----等待收貨
			myorder_merchant_state.setTextColor(context.getResources().getColor(R.color.comment_color));
			myorder_merchant_state.setText(map.get("payment_order_status_desc"));// 描述
			setTextImageState(map, position);
			// 取消订单
			View view_cancel = buttonView.createViewCancelOrder(new InterfaceViewCallback() {

				@Override
				public void sucessCallBack() {
					XHClick.mapStat(context, "a_mail_orders","按钮点击","取消订单");
					context.isRefresh = true;
					context.ids.add(String.valueOf(id));
					if (id == 0) {// 全部
						myorder_but_linear.removeAllViews();
						myorder_merchant_state.setText("已取消");// 描述
						myorder_merchant_state.setTextColor(Color.parseColor("#333333"));
						MallButtonView buttonView = new MallButtonView(context);
						View view_del = buttonView.createViewDelOrder(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(context, "a_mail_orders","按钮点击","删除订单");
								callBack.delItem(position);
								context.isRefresh = true;
								ArrayList<String> idss = context.ids;
								for (int i = 0, size = idss.size(); i < size; i++) {
									if (String.valueOf(id).equals(idss.get(i)))
										return;
								}
								context.ids.add(String.valueOf(id));
							}
						}, map, MallButtonView.list_state_payment,url,mall_stat_statistic);
						myorder_but_linear.addView(view_del);

						View view_repeat = buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
							@Override
							public void sucessCallBack() {
								XHClick.mapStat(context, "a_mail_orders","按钮点击","再次购买");
								Intent intent = new Intent(context, ShoppingActivity.class);
								context.startActivity(intent);
							}
						}, map, MallButtonView.list_state_payment,url,mall_stat_statistic);
						myorder_but_linear.addView(view_repeat);

					} else if (id == 1) {// 待付款
						callBack.delItem(position);
					}
				}
			}, map,url,mall_stat_statistic);
			myorder_but_linear.addView(view_cancel);
			// 去支付
			View view_topay = buttonView.createViewToPay(new InterfaceViewCallback() {

				@Override
				public void sucessCallBack() {
					goOrderState(map, position);
					XHClick.mapStat(context, "a_mail_orders","按钮点击","去支付");
				}
			});
			myorder_but_linear.addView(view_topay);

		} else if (payment_order_satus.equals("2")) {// 付款成功---已拆单---每个作为独立的item
			listview_item.setVisibility(View.VISIBLE);
			rela_mall_order.setVisibility(View.GONE);
			myorder_item_rela.setPadding(0, 0, 0, 0);
			// 子类adapter
//			ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
//			AdapterMyOrderItem adapter_item = new AdapterMyOrderItem(context, listview_item, listMapByJson, R.layout.a_mall_myorder_item_2, null, null, id);
//			listview_item.setAdapter(adapter_item);
//			ToolView.setListViewHeightBasedOnChildren(listview_item);
		} else {// 其他状态
			listview_item.setVisibility(View.GONE);
			rela_mall_order.setVisibility(View.VISIBLE);
			// ----等待收貨
			myorder_merchant_state.setText(map.get("payment_order_status_desc"));// 描述
			myorder_merchant_state.setTextColor(context.getResources().getColor(R.color.comment_color));
			setTextImageState(map, position);
			// 对显示view进行区分
			setButtonView(map, buttonView, position);

		}

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int positions, long id) {
				goOrderState(map, position);
			}
		});
	}
	/**
	 * 对view进行显示
	 */
	private void setButtonView(Map<String,String> map,MallButtonView buttonView,final int position){
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
		String order_status= listMapByJson.get(0).get("order_status");
		if(!TextUtils.isEmpty(order_status))
			if(7==Integer.parseInt(order_status)){
				View view_del=buttonView.createViewDelOrder(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						context.isRefresh=true;
						context.ids.add(String.valueOf(id));
						callBack.delItem(position);
					}
				}, map,MallButtonView.list_state_payment,url,mall_stat_statistic);
				myorder_but_linear.addView(view_del);

				View view_repeat=buttonView.createViewRepeatOrder(new InterfaceViewCallback() {
					@Override
					public void sucessCallBack() {
						Intent intent= new Intent(context,ShoppingActivity.class);
						context.startActivity(intent);
					}
				}, map, MallButtonView.list_state_payment,url,mall_stat_statistic);
				myorder_but_linear.addView(view_repeat);
			}
	}

	/**
	 * 设置不同状态
	 *
	 * @param map
	 */
	private void setTextImageState(final Map<String, String> map,final int position) {
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
		if (listMapByJson.size() == 1) {
			map_order = listMapByJson.get(0);
			setmerchant(true, map_order.get("shop_name"), myorder_merchant_name, myorder_merchant_iv);
			ArrayList<Map<String, String>> listMapByJson_product = UtilString.getListMapByJson(map_order.get("order_product"));
			if (listMapByJson_product.size() > 0 && listMapByJson_product.size() < 2) {// 单商家单商品
				setProductNum(myorder_explian_rela, myorder_explian_text, myorder_explian_iv, listView, false, null, listMapByJson_product.get(0).get("img"),
						listMapByJson_product.get(0).get("proudct_title"));

			} else if (listMapByJson_product.size() > 1) {// 单商家多商品
				ArrayList<String> list_product = new ArrayList<String>();
				for (int i = 0; i < listMapByJson_product.size(); i++) {
					list_product.add(listMapByJson_product.get(i).get("img"));
				}
				setProductNum(myorder_explian_rela, myorder_explian_text, myorder_explian_iv, listView, true, list_product, null, null);
			}

		} else if (listMapByJson.size() > 1) {// 多个商家
			setmerchant(false, null, myorder_merchant_name, myorder_merchant_iv);
			ArrayList<String> list_product = new ArrayList<String>();
			for (int i = 0; i < listMapByJson.size(); i++) {
				ArrayList<Map<String, String>> listMapByJson_order = UtilString.getListMapByJson(listMapByJson.get(i).get("order_product"));
				if (listMapByJson_order.size() == 1) {
					list_product.add(listMapByJson_order.get(0).get("img"));

				} else if (listMapByJson_order.size() > 1) {
					for (int j = 0; j < listMapByJson_order.size(); j++) {
						list_product.add(listMapByJson_order.get(j).get("img"));
					}
				}
			}
			setProductNum(myorder_explian_rela, myorder_explian_text, myorder_explian_iv, listView, true, list_product, null, null);
		}
		rela_mall_order.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goOrderState(map, position);
			}
		});
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
			ArrayList<Map<String, String>> list_data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < strs.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("img", strs.get(i));
				list_data.add(map);
			}
			one.setVisibility(View.GONE);
			listview.setVisibility(View.VISIBLE);
			AdapterSimple adapter_list = new AdapterSimple(listview, list_data, R.layout.a_mall_myorder_list_item, new String[] { "img" }, new int[] { R.id.imageview_list });
			listview.setAdapter(adapter_list);
		} else {// 单图
			one.setVisibility(View.VISIBLE);
			listview.setVisibility(View.GONE);
			setViewImage(one_iv, url);
			one_tv.setText(value);
		}
	}

	/**
	 * 设置店铺点击
	 * @param view
	 * @param map
	 */
	private void setShopOnClick(View view,final Map<String, String> map){
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				XHClick.mapStat(context, "a_mail_orders","点击店铺","");
				MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
				String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
				String url = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home) + "?shop_code=" + map.get("shop_code")+"&"+mall_stat;
				AppCommon.openUrl(context, url, true);
			}
		});
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
			if (value.equals("香哈自营")) {
				view.setBackgroundResource(R.drawable.mall_myorder_myself);
			} else {
				if(map_order.containsKey("shop_code")){
					order_logistics_back.setVisibility(View.VISIBLE);
					setShopOnClick(order_logistics_back, map_order);
					setShopOnClick(myorder_merchant_name, map_order);
					setShopOnClick(myorder_merchant_iv, map_order);
				}else{
					order_logistics_back.setVisibility(View.GONE);
				}
				view.setBackgroundResource(R.drawable.mall_buycommod_commod_merchant_iv);
			}

		} else {// 香哈
			tv.setText("香哈");
			order_logistics_back.setVisibility(View.GONE);
			view.setBackgroundResource(R.drawable.mall_myorder_myself);
		}
	}
	/**
	 * 去详情页
	 * @param map
	 */
	private void goOrderState(Map<String, String> map,int position){
		setStatisticIndex();
		XHClick.mapStat(context, "a_mail_orders","点击到订单详情页","");
		Intent intent = new Intent(context, OrderStateActivity.class);
		intent.putExtra("order_id", map.get("payment_order_id"));
		intent.putExtra("order_satus", "payment_order");
		intent.putExtra("position", position);
		intent.putExtra("code", id);
		context.startActivityForResult(intent, OrderStateActivity.request_order);
	}

	public interface InterfaceCallBack{
		public void delItem(int position);
	}
	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
	}
}
