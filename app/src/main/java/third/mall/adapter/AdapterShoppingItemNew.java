package third.mall.adapter;

import java.util.ArrayList;

import third.mall.bean.ProductBean;
import third.mall.view.MallShopProductView;
import third.mall.view.MallShopProductView.InterProudct;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 购物车itemview显示
 * @author yujian
 *
 */
public class AdapterShoppingItemNew extends BaseAdapter{

	private Context context;
	private ArrayList<ProductBean> list;
	private InterProudct interProudct;
	public AdapterShoppingItemNew(Context context,ArrayList<ProductBean> list){
		this.context=context;
		this.list=list;
	}
	public void setInterface(InterProudct interProudct){
		this.interProudct= interProudct;
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ProductBean bean = list.get(position);
		// 缓存视图
		ViewCache viewCache;
		if (convertView == null) {
			viewCache = new ViewCache(new MallShopProductView(context));
			convertView= viewCache.view;
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setData(bean);

		return convertView;
	}

	private class ViewCache{
		private MallShopProductView view;
		public ViewCache(MallShopProductView view ){
			this.view=view;
		}
		public void setData(ProductBean bean){
			view.setValue(bean);
			view.setInterface(new InterProudct() {
				
				@Override
				public void setChangeSucess() {
					interProudct.setChangeSucess();
				}
			});
		}
	}

}
