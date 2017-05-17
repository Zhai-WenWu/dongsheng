package third.mall.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import third.mall.dialog.UseFavorableDialog;
import third.mall.dialog.UseFavorableDialog.changeCallBack;
import third.mall.tool.ToolView;
import third.mall.view.ViewPromotion;
import xh.basic.tool.UtilString;
import acore.override.adapter.AdapterSimple;
import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

public class AdapterShoppingOrder extends AdapterSimple {

	private List<? extends Map<String, ?>> data;
	private Activity context;
	private OrderChangeCallBack callback;

	public AdapterShoppingOrder(Activity context, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.context = context;
	}
	public void setCallBack(OrderChangeCallBack callbacks){
		if(callbacks==null){
			callback= new OrderChangeCallBack() {
				
				@Override
				public void setChangeData(String shop_code,String code, String before_amt, String now_amt) {
					
				}
			};
		}else this.callback= callbacks;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = (Map<String, String>) data.get(position);
		// 缓存视图
		ViewCache viewCache = null;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(context).inflate(R.layout.a_mall_shop_order_item, parent, false);
			viewCache.setView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map, position);

		return convertView;
	}

	public List<? extends Map<String, ?>> getData(){
		return data;
	}
	private class ViewCache {

		private ImageView shoporder_commod_merchant_iv;
		private TextView shoporder_commod_merchant_name;
		private ListView shopping_list;
		private TextView shoporder_commod_price_res;
		private TextView shoporder_commod_price_pos, shoporder_commod_price_fav,shoporder_commod_price_fav_ticket;
		private RelativeLayout shoporder_commod_merchant_rela;
		private ArrayList<Map<String, String>> list_product;
		private ViewPromotion view_promotion;
		private EditText edittext;
		private RelativeLayout buycommod_commod_price_fav_rela,buycommod_commod_price_fav_ticket_rela,fav_ticket_rela;
		private ArrayList<Map<String, String>> coupon_list;
		private String product_amt;
		private String coupon_code;
		private String coupon_amt;
		private String shop_code;
		public void setView(View view) {
			shoporder_commod_merchant_rela = (RelativeLayout) view.findViewById(R.id.shoporder_commod_merchant_rela);
			shoporder_commod_merchant_iv = (ImageView) view.findViewById(R.id.shoporder_commod_merchant_iv);
			shoporder_commod_merchant_name = (TextView) view.findViewById(R.id.shoporder_commod_merchant_name);
			view_promotion = (ViewPromotion) view.findViewById(R.id.view_promotion);
			shopping_list = (ListView) view.findViewById(R.id.shopping_list);
			shoporder_commod_price_res = (TextView) view.findViewById(R.id.shoporder_commod_price_res);
			shoporder_commod_price_pos = (TextView) view.findViewById(R.id.shoporder_commod_price_pos);
			//满减
			buycommod_commod_price_fav_rela=(RelativeLayout) view.findViewById(R.id.buycommod_commod_price_fav_rela);
			shoporder_commod_price_fav = (TextView) view.findViewById(R.id.shoporder_commod_price_fav);
			//优惠券
			buycommod_commod_price_fav_ticket_rela=(RelativeLayout) view.findViewById(R.id.buycommod_commod_price_fav_ticket_rela);
			shoporder_commod_price_fav_ticket = (TextView) view.findViewById(R.id.shoporder_commod_price_fav_ticket);
			fav_ticket_rela=(RelativeLayout) view.findViewById(R.id.fav_ticket_rela);
			
			view_promotion=(ViewPromotion) view.findViewById(R.id.view_promotion);
			edittext=(EditText) view.findViewById(R.id.edittext);
		}
		public void setValue(final Map<String, String> map, int position) {
			
			ArrayList<Map<String, String>> shop_list = UtilString.getListMapByJson(map.get("shop_info"));
			String shop_name = shop_list.get(0).get("shop_name");
			shop_code = shop_list.get(0).get("shop_code");
			shoporder_commod_merchant_name.setText(shop_name);
			map.put("remarks", "");
			edittext.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					map.put("remarks", edittext.getText().toString());
//					interfaceCace.setShopNameAndContent(shop_code, edittext.getText().toString());
				}
			});
			view_promotion.setStyle(ViewPromotion.style_all);
			view_promotion.setChangeStyle();
			String shop_postage_desc= shop_list.get(0).get("shop_postage_desc");
			String shop_promotion_desc= shop_list.get(0).get("shop_promotion_desc");
			if(!TextUtils.isEmpty(shop_postage_desc)||!TextUtils.isEmpty(shop_promotion_desc)){
				view_promotion.setVisibility(View.VISIBLE);
				Log.i("order_shop", shop_promotion_desc+""+shop_postage_desc);
				view_promotion.setData(shop_postage_desc, shop_promotion_desc);
			}else view_promotion.setVisibility(View.GONE);
			
			setShopLogo(shop_name);
			setShopListener(map);
			product_amt = UtilString.getListMapByJson(map.get("amt_info")).get(0).get("product_amt");
			String postage_amt = UtilString.getListMapByJson(map.get("amt_info")).get(0).get("postage_amt");
			String promotion_amt = UtilString.getListMapByJson(map.get("amt_info")).get(0).get("promotion_amt");
			shoporder_commod_price_res.setText("¥" + product_amt);
			shoporder_commod_price_pos.setText("+ ¥" + postage_amt);
			if(!TextUtils.isEmpty(promotion_amt)&&Float.parseFloat(promotion_amt)>0){
				shoporder_commod_price_fav.setText("- ¥" + promotion_amt);
				buycommod_commod_price_fav_rela.setVisibility(View.VISIBLE);
			}else{
				buycommod_commod_price_fav_rela.setVisibility(View.GONE);
			}
			list_product = UtilString.getListMapByJson(map.get("product_info"));
			for (int i = 0; i < list_product.size(); i++) {
				Map<String, String> map_temp = list_product.get(i);
				String num = map_temp.get("product_num");
				map.put("product_num", "X" + num);
			}
			AdapterSimple simple = new AdapterSimple(shopping_list, list_product, R.layout.a_mall_shop_order_listview_item, new String[] { "title", "img", "product_num", "sale_price" }, new int[] {
					R.id.item_order_text, R.id.item_order_iv, R.id.item_order_data_num, R.id.item_order_data_price });
			shopping_list.setAdapter(simple);
			shopping_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					Intent intent = new Intent(context, CommodDetailActivity.class);
//					intent.putExtra("product_code", list_product.get(position).get("product_code"));
//					context.startActivity(intent);
				}
			});
			ToolView.setListViewHeightBasedOnChildren(shopping_list);
			
			//处理优惠券信息
			if(map.containsKey("shop_coupon_info")){
				fav_ticket_rela.setVisibility(View.VISIBLE);
				buycommod_commod_price_fav_ticket_rela.setVisibility(View.VISIBLE);
				coupon_list=UtilString.getListMapByJson(map.get("shop_coupon_info"));
				setFavorableAmt();
			}else{
				buycommod_commod_price_fav_ticket_rela.setVisibility(View.GONE);
				fav_ticket_rela.setVisibility(View.GONE);
			} 
		}

		/**
		 * 处理当前商品状态
		 */
		private void setFavorableAmt() {
			ArrayList<Map<String, String>> change_list=new ArrayList<Map<String,String>>();
			if(coupon_list.size()>0){
				for (int i = 0,size=coupon_list.size(); i < size; i++) {
					if(Float.parseFloat(coupon_list.get(i).get("order_amt_reach"))<=Float.parseFloat(product_amt)){
						change_list.add(coupon_list.get(i));
					}
				}
				if(change_list.size()>0){
					coupon_amt=change_list.get(change_list.size()-1).get("coupon_amt");
					setFavorableText(true, "- ¥" +coupon_amt);
					coupon_code= change_list.get(change_list.size()-1).get("shop_coupon_code");
				}else {
					coupon_code="";
					setFavorableText(false, "未使用");
					}
			}else{
				coupon_code="";
				setFavorableText(false, "未使用");
			}
		}
		/**
		 * 设置优惠券使用状态
		 * @param state
		 * @param desc
		 */
		private void setFavorableText(boolean state,String desc){
			shoporder_commod_price_fav_ticket.setText(desc);
			if(state)
				shoporder_commod_price_fav_ticket.setTextColor(context.getResources().getColor(R.color.comment_color));
			else
				shoporder_commod_price_fav_ticket.setTextColor(Color.parseColor("#666666"));
		}
		/**
		 * 点击
		 * 
		 * @param map
		 */
		private void setShopListener(final Map<String, String> map) {
			shoporder_commod_merchant_rela.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					String url = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_detail) + "?shop_code=" + UtilString.getListMapByJson(map.get("shop_info")).get(0).get("shop_code");
//					AppCommon.openUrl(context, url, true);
				}
			});
			buycommod_commod_price_fav_ticket_rela.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					UseFavorableDialog dialog= new UseFavorableDialog(context,coupon_list,product_amt);
					dialog.setdata(coupon_code,new changeCallBack() {
						
						@Override
						public void setChangeData(String code, String amt) {
							callback.setChangeData(shop_code,code, coupon_amt, amt);
							coupon_code= code;
							coupon_amt=amt;
							if(!TextUtils.isEmpty(amt)){
								setFavorableText(true, "- ¥" +amt);
							}else setFavorableText(false, "未使用");
						}
					});
				}
			});
		}
		/**
		 * 商家logo
		 * 
		 * @param shop_name
		 */
		private void setShopLogo(String shop_name) {
			if (shop_name.contains("香哈")) {
				shoporder_commod_merchant_iv.setBackgroundResource(R.drawable.mall_myorder_myself);
			} else {
				shoporder_commod_merchant_iv.setBackgroundResource(R.drawable.mall_buycommod_commod_merchant_iv);
			}
		}
	}
	public interface OrderChangeCallBack{
		public void setChangeData(String shop_code,String code,String before_amt,String now_amt);
	}
}
