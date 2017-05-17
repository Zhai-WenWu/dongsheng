package third.mall.adapter;

import java.util.List;
import java.util.Map;

import third.mall.activity.MyOrderActivity;
import third.mall.view.OrderItem2View;
import third.mall.view.OrderItemView.InterfaceCallBack;
import acore.override.adapter.AdapterSimple;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author yujian
 *
 */
public class AdapterMyOrderItemNew extends AdapterSimple{
	private List<? extends Map<String, ?>> data;
	private MyOrderActivity context;
	private int id;
	private String url;
	private String mall_stat_statistic;
	public AdapterMyOrderItemNew(MyOrderActivity context,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,int id) {
		super(parent, data, resource, from, to);
		this.data= data;
		this.context= context;
		this.id= id;
	}
	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String,String> map = (Map<String, String>) data.get(position);
		// 缓存视图
		ViewHolder viewCache = null;
		if (convertView == null) {
			viewCache = new ViewHolder(new OrderItem2View(context));
			convertView =  viewCache.view;
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewHolder) convertView.getTag();
		}
		viewCache.setData(map, position);
		return convertView;
	}
	public class ViewHolder{
		OrderItem2View view;
		public ViewHolder(OrderItem2View view){
			this.view= view;
		}
		public void setData( Map<String, String> map,int position){
			view.setData(map, position, id);
			view.setUrl(url,mall_stat_statistic);
			view.setInterfaceCallBack(new InterfaceCallBack() {
				@Override
				public void delItem(int position) {
					
				}
			});
		}
	}

}
