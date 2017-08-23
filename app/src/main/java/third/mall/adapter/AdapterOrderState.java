package third.mall.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.PageStatisticsUtils;
import aplug.feedback.activity.Feedback;
import third.mall.activity.CommodDetailActivity;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import third.mall.tool.ToolView;
import third.mall.view.HorizontalListView;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 订单详情页
 * 
 * @author yu
 *
 */
public class AdapterOrderState extends MallAdapterSimple {
	private List<? extends Map<String, ?>> data;
	private Context context;
	private BaseActivity activity;
	private String url;
	private String mall_stat_statistic;
	public AdapterOrderState(BaseActivity activity,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.context = parent.getContext();
		this.activity = activity;
	}
	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = (Map<String, String>) data.get(position);

		// 缓存视图
		ViewCache viewCache = null;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(context).inflate(R.layout.a_mall_order_item, parent, false);
			viewCache.setView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map, position);
		return convertView;
	}
	public class ViewCache{
		
		// 商家
		private RelativeLayout buycommod_commod_merchant_rela;
		private ImageView buycommod_commod_merchant_iv;
		private TextView buycommod_commod_merchant_name;
		// 订单信息
		private LinearLayout myorder_state_linear_explian;
		private TextView myorder_state_order_tv_explian;
		private TextView myorder_state_order_time_explian;
		// 商品
		private RelativeLayout buycommod_commod_explian_rela;
		private ImageView buycommod_commod_explian_iv;
		private TextView buycommod_commod_explian_text;
		private ListView listview;
		// 单价数量
		private TextView buycommod_commod_explian_data_price;
		private TextView buycommod_commod_explian_data_num;
		// 金额邮费
		private TextView buycommod_commod_price_res;
		private TextView buycommod_commod_price_pos,buycommod_commod_price_fav,buycommod_commod_price_fav_ticket;
		private ArrayList<Map<String, String>> listMapByJson;
		private ArrayList<Map<String, String>> listMapByJson_order;
		private RelativeLayout rela_remark,rela_shop;
		private TextView text_remark;
		private TextView tv_shop_tel;
		private RelativeLayout buycommod_commod_price_fav_rela,buycommod_commod_price_fav_ticket_rela,fav_ticket_rela;
		public void setView(View view ){
			// 商家
			buycommod_commod_merchant_rela=(RelativeLayout) view.findViewById(R.id.buycommod_commod_merchant_rela);
			buycommod_commod_merchant_iv = (ImageView) view.findViewById(R.id.buycommod_commod_merchant_iv);
			buycommod_commod_merchant_name = (TextView) view.findViewById(R.id.buycommod_commod_merchant_name);
			// 订单信息
			myorder_state_linear_explian = (LinearLayout) view.findViewById(R.id.myorder_state_linear_explian);
			myorder_state_order_tv_explian = (TextView) view.findViewById(R.id.myorder_state_order_tv_explian);
			myorder_state_order_time_explian = (TextView) view.findViewById(R.id.myorder_state_order_time_explian);
			// 商品
			buycommod_commod_explian_rela = (RelativeLayout) view.findViewById(R.id.buycommod_commod_explian_rela);
			buycommod_commod_explian_iv = (ImageView) view.findViewById(R.id.buycommod_commod_explian_iv);
			buycommod_commod_explian_text = (TextView) view.findViewById(R.id.buycommod_commod_explian_text);
			listview = (ListView) view.findViewById(R.id.listview);
			// 单价数量
			buycommod_commod_explian_data_price = (TextView) view.findViewById(R.id.buycommod_commod_explian_data_price);
			buycommod_commod_explian_data_num = (TextView) view.findViewById(R.id.buycommod_commod_explian_data_num);
			// 金额邮费
			buycommod_commod_price_res = (TextView) view.findViewById(R.id.buycommod_commod_price_res);
			buycommod_commod_price_pos = (TextView) view.findViewById(R.id.buycommod_commod_price_pos);
			//满减
			buycommod_commod_price_fav_rela =(RelativeLayout) view.findViewById(R.id.buycommod_commod_price_fav_rela);
			buycommod_commod_price_fav = (TextView) view.findViewById(R.id.buycommod_commod_price_fav);
			//优惠券
			fav_ticket_rela=(RelativeLayout) view.findViewById(R.id.fav_ticket_rela);
			buycommod_commod_price_fav_ticket_rela =(RelativeLayout) view.findViewById(R.id.buycommod_commod_price_fav_ticket_rela);
			buycommod_commod_price_fav_ticket = (TextView) view.findViewById(R.id.shoporder_commod_price_fav_ticket);
			//用户留言———联系商家
			rela_remark=(RelativeLayout) view.findViewById(R.id.rela_remark);
			rela_shop=(RelativeLayout) view.findViewById(R.id.rela_shop);
			text_remark=(TextView) view.findViewById(R.id.text_remark);
			tv_shop_tel=(TextView) view.findViewById(R.id.tv_shop_tel);
			
		}
		public void setValue(Map<String,String> map,int position){
			buycommod_commod_price_res.setText("¥" + map.get("product_amt"));
			buycommod_commod_price_pos.setText("+ ¥" + map.get("postage_amt"));
			//满减
			if(map.containsKey("coupon_amt")&&!TextUtils.isEmpty(map.get("coupon_amt"))&&Float.parseFloat(map.get("coupon_amt"))>0){
				buycommod_commod_price_fav_rela.setVisibility(View.VISIBLE);
				buycommod_commod_price_fav.setText("- ¥" + map.get("coupon_amt"));
			}else buycommod_commod_price_fav_rela.setVisibility(View.GONE);
			//优惠券
			if(map.containsKey("discount_amt")&&!TextUtils.isEmpty(map.get("discount_amt"))&&Float.parseFloat(map.get("discount_amt").toString())>0){
				fav_ticket_rela.setVisibility(View.VISIBLE);
				buycommod_commod_price_fav_ticket.setText("- ¥" + map.get("discount_amt"));
			}else fav_ticket_rela.setVisibility(View.GONE);
			
			listMapByJson = UtilString.getListMapByJson(map.get("order_product"));
			listMapByJson_order = UtilString.getListMapByJson(map.get("order_shop"));
			
			/***************************处理用户留言，和联系商家start****************************************8 */
			if(map.containsKey("remark")&&!TextUtils.isEmpty(map.get("remark"))){
				rela_remark.setVisibility(View.VISIBLE);
				text_remark.setText("备注："+map.get("remark"));
			}else rela_remark.setVisibility(View.GONE);
			if(listMapByJson_order.get(0).containsKey("shop_tel")&&!TextUtils.isEmpty(listMapByJson_order.get(0).get("shop_tel"))){
				rela_shop.setVisibility(View.VISIBLE);
			//不显示电话号码
//				tv_shop_tel.setText(listMapByJson_order.get(0).get("shop_tel"));
			}else
				rela_shop.setVisibility(View.GONE);
			rela_shop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					context.startActivity(new Intent(context, Feedback.class));
//					showDialog(listMapByJson_order.get(0).get("shop_tel"));
				}
			});
			/***************************处理用户留言，和联系商家end****************************************8 */
			
			setName(listMapByJson_order.get(0).get("shop_name"), buycommod_commod_merchant_iv, buycommod_commod_merchant_name);
			if (listMapByJson.size() > 0) {
				buycommod_commod_explian_rela.setVisibility(View.GONE);
				listview.setVisibility(View.VISIBLE);
				for (int i = 0; i < listMapByJson.size(); i++) {
					listMapByJson.get(i).put("title", UtilString.getListMapByJson(listMapByJson.get(i).get("info")).get(0).get("title"));
				}
				AdapterSimple adapterSimple = new AdapterSimple(listview, listMapByJson, R.layout.a_mall_order_state_item, new String[] { "img", "num", "sale_price", "title" }, new int[] {
						R.id.order_state_iv, R.id.order_state_data_num, R.id.order_state_data_price, R.id.order_state_text });
				listview.setAdapter(adapterSimple);
				listview.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						XHClick.mapStat(context, "a_mail_order","商品","");
						 MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
						 Intent intent= new Intent(context,CommodDetailActivity.class);
						 intent.putExtra("product_code", UtilString.getListMapByJson(listMapByJson.get(position).get("info")).get(0).get("product_code"));
						intent.putExtra(MallBaseActivity.PAGE_FROM, PageStatisticsUtils.getPageName(activity));
						context.startActivity(intent);
					}
				});
				ToolView.setListViewHeightBasedOnChildren(listview);
				// setListView(listview, list_img);
				buycommod_commod_merchant_rela.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						XHClick.mapStat(context, "a_mail_order","店铺","");
						MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
						String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
						String url=MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home)+"?shop_code="+listMapByJson_order.get(0).get("shop_code")+"&"+mall_stat;
						AppCommon.openUrl(activity, url, true);
					}
				});

			}
			if (map.containsKey("order_log")) {//
				myorder_state_linear_explian.setVisibility(View.VISIBLE);
				ArrayList<Map<String, String>> listMapByJson_log = UtilString.getListMapByJson(map.get("order_log"));
				myorder_state_order_time_explian.setText(listMapByJson_log.get(0).get("log_time"));
				String log_remark = listMapByJson_log.get(0).get("log_remark");
				if (TextUtils.isEmpty(listMapByJson_log.get(0).get("log_time")))
					myorder_state_linear_explian.setVisibility(View.GONE);
				else
					myorder_state_linear_explian.setVisibility(View.VISIBLE);
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
							name +=  jsonArray.getString(1);
						}
//						if (i > 0 && i == jsonArray.length() - 1) {
//							name += ")";
//						}
					}

					myorder_state_order_tv_explian.setText(name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				myorder_state_linear_explian.setVisibility(View.GONE);
			}
		}
		/**
		 * 弹出对话框
		 * @param des
		 */
//		private void showDialog(final String des){
//			final Dialog dialog= new Dialog(context,R.style.dialog);
//			dialog.setContentView(R.layout.a_mall_alipa_dialog);
//			Window window=dialog.getWindow();
//			window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
//			TextView dialog_message= (TextView) window.findViewById(R.id.dialog_message);
//			dialog_message.setText(des);
//			TextView dialog_cancel= (TextView) window.findViewById(R.id.dialog_cancel);
//			TextView dialog_sure= (TextView) window.findViewById(R.id.dialog_sure);
//			dialog_cancel.setText("取消");
//			dialog_sure.setText("确定");
//			dialog_cancel.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					dialog.cancel();
//				}
//			});
//			dialog_sure.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					toTel(des);
//					dialog.cancel();
//				}
//			});
//			dialog.show();
//		}
		/**
		 * 打电话
		 */
		private void toTel(String number){
			XHClick.mapStat(context, "a_mail_order","联系卖家","");
			Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+number));  
			context.startActivity(intent); 
		}
	}

	/**
	 * 横向滑动
	 * 
	 * @param listview
	 * @param list_img
	 */
	private void setListView(HorizontalListView listview, ArrayList<String> list_img) {
		ArrayList<Map<String, String>> list_data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list_img.size(); i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("img", list_img.get(i));
			list_data.add(map);
		}
		AdapterSimple adapter_list = new AdapterSimple(listview, list_data, R.layout.a_mall_myorder_list_item, new String[] { "img" }, new int[] { R.id.imageview_list });
		listview.setAdapter(adapter_list);
	}

	private void setName(String name, ImageView view, TextView tv) {
		tv.setText(name);
		if ("香哈".equals(name) || "香哈自营".equals(name)) {
			view.setBackgroundResource(R.drawable.mall_myorder_myself);
		} else {
			view.setBackgroundResource(R.drawable.mall_buycommod_commod_merchant_iv);
		}

	}
}
