package third.mall.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import third.mall.adapter.AdapterShoppingItem.interNumProudct;
import third.mall.aplug.MallStringManager;
import third.mall.dialog.FavorableDialog;
import third.mall.dialog.FavorableDialog.showCallBack;
import third.mall.tool.ToolView;
import third.mall.view.ViewPromotion;
import third.mall.widget.ListViewForScrollView;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 购物车一层adapter
 * 
 * @author Administrator
 *
 */
public class AdapterShopping extends AdapterSimple {

	private BaseActivity context;
	private ArrayList<Map<String, String>> data;
	private interShopCat intershopcat;
	private static final String SHOP_TYPE="shop_type";
	private static final String DIALOG_TYPE="dialog_type";
	public AdapterShopping(BaseActivity context, View parent,ArrayList<Map<String, String>> data, int resource, String[] from,
			int[] to) {
		super(parent, data, resource, from, to);
		this.context = context;
		this.data = data;

	}
	public void setInterShopCat(interShopCat intershopcat){
		this.intershopcat= intershopcat;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = data.get(position);
		// 缓存视图
		ViewCache viewCache;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(context).inflate(R.layout.a_mall_shopping_listview, parent, false);
			viewCache.setView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map,position);
		if(intershopcat!=null)
			viewCache.setInterShop(intershopcat);

		return convertView;
	}

	private class ViewCache {
		private ImageView choose_iv_boss;
		private ImageView shopping_logo_iv;
		private TextView shopping_logo_tv,get_favorable_tv;
		private ListViewForScrollView listview_shopping;
		private RelativeLayout listview_money_rela;
		private RelativeLayout rela_shopping;
		private TextView money_num_tv;
		private interShopCat intershop;
		private String choose_state_shop="1";//选择状态 0-未选中，1-选中
		private String edit_shop="0";//选择状态 0-结算，1-编辑
		private ArrayList<Map<String, String>> list_product;
		private ArrayList<Map<String, String>> list_shop;
		private ViewPromotion view_promotion;
		private FavorableDialog dialog;
		public void setInterShop(interShopCat intershop){
			this.intershop =intershop; 
		}
		public void setView(View view) {
			rela_shopping=(RelativeLayout) view.findViewById(R.id.rela_shopping);
			choose_iv_boss=(ImageView) view.findViewById(R.id.choose_iv_boss);
			shopping_logo_iv=(ImageView) view.findViewById(R.id.shopping_logo_iv);
			shopping_logo_tv = (TextView) view.findViewById(R.id.shopping_logo_tv);
			listview_shopping = (ListViewForScrollView) view.findViewById(R.id.listview_shopping);
			listview_money_rela = (RelativeLayout) view.findViewById(R.id.listview_money_rela);
			money_num_tv = (TextView) view.findViewById(R.id.money_num_tv);
			view_promotion=(ViewPromotion) view.findViewById(R.id.view_promotion);
			get_favorable_tv =(TextView) view.findViewById(R.id.get_favorable_tv);
		}

		public void setValue(final Map<String, String> map,int position) {
			//展示商家
			list_shop= UtilString.getListMapByJson(map.get("shop_info"));
			String  shop_name=list_shop.get(0).get("shop_name");
			if(list_shop.get(0).containsKey("shop_has_coupon")&& "2".equals(list_shop.get(0).get("shop_has_coupon"))){
				get_favorable_tv.setVisibility(View.VISIBLE);
			}else get_favorable_tv.setVisibility(View.GONE);
			
			view_promotion.setStyle(ViewPromotion.style_all);
			view_promotion.setChangeStyle();
			String shop_postage_desc= list_shop.get(0).get("shop_postage_desc");
			String shop_promotion_desc= list_shop.get(0).get("shop_promotion_desc");
			if(!TextUtils.isEmpty(shop_postage_desc)||!TextUtils.isEmpty(shop_promotion_desc)){
				view_promotion.setVisibility(View.VISIBLE);
				view_promotion.setData(shop_postage_desc, shop_promotion_desc);
			}else view_promotion.setVisibility(View.GONE);
			
			shopping_logo_tv.setText(shop_name);
			setShopLogo(shop_name, shopping_logo_iv);
			//子adapter
			list_product=UtilString.getListMapByJson(map.get("product_list"));
			//act设置状态了
			if(map.containsKey("choose_state_shop")){
				choose_state_shop= map.get("choose_state_shop");
			}
			//
			if(!map.containsKey("choose_state_shop")||"1".equals(map.get("choose_state_shop"))){
				choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_choose);
			}else if("0".equals(map.get("choose_state_shop"))){
				choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_no_choose);
			}
			//当前商家状态
			map.put("choose_state_shop", choose_state_shop);
			//当前用户处于什么状态
			if(map.containsKey("edit_shop")){
				edit_shop= map.get("edit_shop");
			}
			map.put("edit_shop", edit_shop);
			
			if("1".equals(edit_shop)){
				listview_money_rela.setVisibility(View.GONE);
			}else{
				listview_money_rela.setVisibility(View.VISIBLE);
			}
			if("1".equals(edit_shop)){//删除状态
				choose_iv_boss.setEnabled(true);
			}else{//结算状态
			}
			final AdapterShoppingItem item= new AdapterShoppingItem(context, listview_shopping, list_product, R.layout.a_mall_shopping_listview_item, new String[]{}, new int[]{});
			listview_shopping.setAdapter(item);
			money_num_tv.setText("¥"+ToolView.getNumberPart(setMoney(list_product,false,map)+""));
			ToolView.setListViewHeightBasedOnChildren(listview_shopping);
			//商品状态回调
			item.setInterNumproudct(new interNumProudct() {

				@Override
				public void setProudctChooseAndchange(ArrayList<Map<String, String>> datas, int position) {
					float fl_product=setMoney(UtilString.getListMapByJson(map.get("product_list")),true,map);
					float fl_now=setMoney(datas,true,map);
					money_num_tv.setText("¥"+ToolView.getNumberPart(fl_now+""));
					map.put("product_list", setJsonArraylist(datas).toString());
					
					//设置选中状态
					setChooseState(datas,map,false,fl_product, fl_now);
					intershop.setChooseAndChangeShop(data, fl_product, fl_now);
					
				}
			});
			
			//选中点击
			choose_iv_boss.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(choose_state_shop.equals("0")){//选中
						float fl_product= setMoney(UtilString.getListMapByJson(map.get("product_list")), true,map);
						float fl_now= setMoney(UtilString.getListMapByJson(map.get("product_list")), false,map);
						money_num_tv.setText("¥"+ToolView.getNumberPart(fl_now+""));
						for (int i = 0; i < list_product.size(); i++) {
							if(list_product.get(i).containsKey("edit_product")&&"1".equals(list_product.get(i).get("edit_product"))){
								list_product.get(i).put("choose_state", "1");
							}else{
								int saleable_num= Integer.parseInt(list_product.get(i).get("saleable_num"));
								if(saleable_num<=0)
									list_product.get(i).put("choose_state", "0");
								else
									list_product.get(i).put("choose_state", "1");
							}
						}
						map.put("product_list",  setJsonArraylist(list_product).toString());
						choose_state_shop="1";
						map.put("choose_state_shop", choose_state_shop);
						choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_choose);
						if(intershop!=null)
							intershop.setChooseAndChangeShop(data,fl_product,fl_now);
					}else if(choose_state_shop.equals("1")){//取消
						float fl_product= setMoney(UtilString.getListMapByJson(map.get("product_list")), true,map);
						float fl_now=0;
						money_num_tv.setText("¥"+ToolView.getNumberPart(fl_now+""));
						choose_state_shop="0";
						for (int i = 0; i < list_product.size(); i++) {
							list_product.get(i).put("choose_state", "0");
						}
						map.put("choose_state_shop", choose_state_shop);
						map.put("product_list",  setJsonArraylist(list_product).toString());
						choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_no_choose);
						if(intershop!=null)
							intershop.setChooseAndChangeShop(data,fl_product,fl_now);
					}
					item.notifyDataSetChanged();
					
				}
			});
			
			setClickListener(shopping_logo_iv, SHOP_TYPE);
			setClickListener(shopping_logo_tv, SHOP_TYPE);
			setClickListener(get_favorable_tv, DIALOG_TYPE);
//			listview_shopping.setOnItemClickListener(new OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					 Intent intent= new Intent(context,CommodDetailActivity.class);
//					 intent.putExtra("product_code", list_product.get(position).get("code"));
//					 context.startActivity(intent);
//				}
//			});
		}
		private void setClickListener(View view,final String type){
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(type.equals(SHOP_TYPE)){//商家
						String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
						String url=MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home)+"?shop_code="+list_shop.get(0).get("shop_code")+"&"+mall_stat;
						AppCommon.openUrl(context, url, true);
					}else if(type.equals(DIALOG_TYPE)){//领券
						if(dialog==null){
							dialog= new FavorableDialog(context,list_shop.get(0).get("shop_code"));
							dialog.setCallBack(new showCallBack() {
								
								@Override
								public void setShow() {
									dialog.show();
								}
							});
						}else{
							dialog.show();
						}
					}
				}
			});
		}
		
		/**
		 * 设置状态
		 * @param data_product
		 * @param map
		 * @param state--是否执行回调
		 */
		private void setChooseState(ArrayList<Map<String, String>> data_product,Map<String, String> map,boolean state,float fl_product,float fl_now){
			for (int i = 0; i < data_product.size(); i++) {
				if(data_product.get(i).containsKey("choose_state")){
					String num= data_product.get(i).get("saleable_num");
					
					if(!TextUtils.isEmpty(num)){
						int sale_num= Integer.parseInt(num);
						if("1".equals(edit_shop)){
							if(data_product.get(i).get("choose_state").equals("0")){
								choose_state_shop="0";
								break;
							}else if(data_product.get(i).get("choose_state").equals("1")){
								choose_state_shop="1";
							}
						}else{
						if(sale_num<=0){
							choose_state_shop="1";
						}else{
							if(data_product.get(i).get("choose_state").equals("0")){
								choose_state_shop="0";
								break;
							}else if(data_product.get(i).get("choose_state").equals("1")){
								choose_state_shop="1";
							}
						}
						}
					}
				}
			}
			if(choose_state_shop.equals("0")){//有未选择
				choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_no_choose);
			}else if(choose_state_shop.equals("1")){//全部选中
				choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_choose);
			}
			map.put("choose_state_shop", choose_state_shop);
			//回调到activity
			if(intershop!=null&&state)
				intershop.setChooseAndChangeShop(data,fl_product,fl_now);
		}
		
		/**
		 *设置商家小计价格
		 */
		private float setMoney(ArrayList<Map<String, String>> list_product,boolean state,Map<String,String> map){
			//小计
			float fl_price=0;
			if(list_product.size()>1){
				for (int i = 0; i < list_product.size(); i++) {
					Map<String,String> map_product= list_product.get(i);
					if("2".equals(map_product.get("stock_flag"))){
						int num= Integer.parseInt(map_product.get("num"));
						int saleable_num= Integer.parseInt(map_product.get("saleable_num"));
						if(num>saleable_num){
							map_product.put("num", saleable_num+"");
						}
					}
					if(state){
						if(!map_product.containsKey("choose_state")||"1".equals(map_product.get("choose_state"))){
						if(!TextUtils.isEmpty(map_product.get("discount_price"))&&!TextUtils.isEmpty(map_product.get("num")))
							fl_price += Float.parseFloat(map_product.get("discount_price"))*Integer.parseInt(map_product.get("num"));
						}
					}else{
						if(!TextUtils.isEmpty(map_product.get("discount_price"))&&!TextUtils.isEmpty(map_product.get("num")))
							fl_price += Float.parseFloat(map_product.get("discount_price"))*Integer.parseInt(map_product.get("num"));
						}
					}
			}else if(list_product.size()==1){
				Map<String,String> map_product= list_product.get(0);
				if("2".equals(map_product.get("stock_flag"))){
					int num= Integer.parseInt(map_product.get("num"));
					int saleable_num= Integer.parseInt(map_product.get("saleable_num"));
					if(num>saleable_num){
						map_product.put("num", saleable_num+"");
					}
				}
				if(state){
						if(!map_product.containsKey("choose_state")||"1".equals(map_product.get("choose_state"))){
							if(!TextUtils.isEmpty(map_product.get("discount_price"))&&!TextUtils.isEmpty(map_product.get("num")))
							fl_price= Float.parseFloat(map_product.get("discount_price"))*Integer.parseInt(map_product.get("num"));
						}
				}else{
					if(!TextUtils.isEmpty(map_product.get("discount_price"))&&!TextUtils.isEmpty(map_product.get("num")))
					fl_price += Float.parseFloat(map_product.get("discount_price"))*Integer.parseInt(map_product.get("num"));
				}
			}
			return setFloat(fl_price);
		}
		private float setFloat(float value){
			BigDecimal b= new BigDecimal(value);
			return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		}
		/**
		 * 设置商家头像
		 * @param name
		 * @param view
		 */
		private void setShopLogo(String name,ImageView view){
			if(name.contains("香哈")){
				view.setBackgroundResource(R.drawable.mall_myorder_myself);
			}else{
				view.setBackgroundResource(R.drawable.mall_buycommod_commod_merchant_iv);
			}
		}
	}
	
	/**
	 * 商家状态回调
	 * @author Administrator
	 *
	 */
	public interface interShopCat{
		/**
		 * 
		 * @param data---整个数据
		 * @param fl_product---商家原先价格
		 * @param fl_new----商家现在价格
		 */
		void setChooseAndChangeShop(ArrayList<Map<String, String>> data,float fl_product,float fl_new);
	}
	private JSONArray setJsonArraylist(ArrayList<Map<String, String>> list_product){
		JSONArray jsonArray= new JSONArray();
		try{
		for (int i = 0; i < list_product.size(); i++) {
			Map<String,String> map=list_product.get(i);
			JSONObject object = new JSONObject();
			object.put("code", map.get("code"));
			object.put("title", map.get("title"));
			object.put("img", map.get("img"));
			object.put("discount_price", map.get("discount_price"));
			object.put("num", map.get("num"));
			object.put("max_sale_num", map.get("max_sale_num"));
			object.put("choose_state", map.get("choose_state"));
			object.put("saleable_num", map.get("saleable_num"));
			object.put("stock_flag", map.get("stock_flag"));
			if(map.containsKey("edit_product"))
				object.put("edit_product", map.get("edit_product"));
			jsonArray.put(object);
		}
		return jsonArray;
		}catch(JSONException e){
			e.printStackTrace();
		}
		return jsonArray;
	}
}
