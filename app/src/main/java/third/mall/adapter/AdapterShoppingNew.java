package third.mall.adapter;

import java.util.ArrayList;

import third.mall.bean.MerchantBean;
import third.mall.view.MallShopMerchantView;
import third.mall.view.MallShopProductView.InterProudct;
import acore.override.activity.base.BaseActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 购物车一层adapter
 * 
 * @author Administrator
 *
 */
public class AdapterShoppingNew extends BaseAdapter {

	private BaseActivity context;
	private ArrayList<MerchantBean> data;
	private InterProudct interProudct;
	private String url="";
	private String mall_stat_statistic;
	public AdapterShoppingNew(BaseActivity context,ArrayList<MerchantBean> data) {
		this.context = context;
		this.data = data;
	}
	
	public void setStatistic(String url,String mall_stat_statistic){
		this.url=url;
		this.mall_stat_statistic=mall_stat_statistic;
	}
	public void setInterface(InterProudct interProudct){
		this.interProudct= interProudct;
	}
	@Override
	public int getCount() {
		return data.size();
	}
	@Override
	public Object getItem(int position) {
		return data.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MerchantBean bean = data.get(position);
		// 缓存视图
		ViewCache viewCache;
		if (convertView == null) {
			viewCache = new ViewCache(new MallShopMerchantView(context));
			convertView= viewCache.view;
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setData(bean);

		return convertView;
	}

	private class ViewCache{
		private MallShopMerchantView view;
		public ViewCache(MallShopMerchantView view ){
			this.view=view;
		}
		public void setData(MerchantBean bean){
			view.setData(bean,url,mall_stat_statistic);
			view.setInterface(new InterProudct() {
				
				@Override
				public void setChangeSucess() {
					interProudct.setChangeSucess();
				}
			});
		}
	}
}
