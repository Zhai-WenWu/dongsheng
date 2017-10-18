package third.mall.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import third.mall.activity.ShoppingActivity;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallAddShopping;

public class AdapterShopRecommed extends MallAdapterSimple{

	private BaseActivity activity;
	private Context context;
	private MallCommon common;
	private List<? extends Map<String, ?>> data;
	private String statistickey;
	public AdapterShopRecommed(BaseActivity activity,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,String statistickey) {
		super(parent, data, resource, from, to);
		this.data= data;
		this.activity= activity;
		this.context= parent.getContext();
		common=new MallCommon(context);
		this.statistickey= statistickey;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = (Map<String, String>) data.get(position);
		// 缓存视图
		ViewCache viewCache;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(context).inflate(R.layout.a_mall_shop_recommend_item_grid, parent, false);
			viewCache.setView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map,position);

		return convertView;
	}
	class ViewCache{
		
		private ImageView product_iv;
		private TextView product_name;
		private TextView product_num;
		private TextView product_state;
		private TextView product_price;
		private ImageView product_add;
		
		public void setView(View view){
			product_iv = (ImageView) view.findViewById(R.id.product_iv);
			product_name = (TextView) view.findViewById(R.id.product_name);
			product_num = (TextView) view.findViewById(R.id.product_num);
			product_state = (TextView) view.findViewById(R.id.product_state);
			product_price = (TextView) view.findViewById(R.id.product_price);
			product_add = (ImageView) view.findViewById(R.id.product_add);
			
		}
		public void setValue(final Map<String,String> map,int position){
			setViewImage(product_iv, map.get("img"));
			product_name.setText(map.get("title"));
			product_num.setText("已售"+map.get("saled_num")+"件");
			product_state.setText(map.get("postage"));
			product_price.setText("¥"+map.get("real_price"));
			product_add.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					common.addShoppingcat(context, map.get("product_code"),new InterfaceMallAddShopping() {
						
						@Override
						public void addProduct(int state) {
							if(state>=50){
								 if (activity instanceof ShoppingActivity) {
									 ((ShoppingActivity)activity).setRequest(true);
								}
								if(!TextUtils.isEmpty(statistickey)) {
									XHClick.mapStat(activity, statistickey, "你可能喜欢", "加入购物车");
								}
								//统计
								MallCommon.setStatictisFrom("猜你喜欢");
								Tools.showToast(context, "已添加到购物车");
							}
						}
					});
				}
			});
		}
	}
}
