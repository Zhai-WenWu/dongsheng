package third.mall.adapter;

import java.util.List;
import java.util.Map;

import third.mall.activity.MyOrderActivity;
import third.mall.view.OrderItemFatherView;
import third.mall.view.OrderItemView;
import third.mall.view.OrderItemView.InterfaceCallBack;
import acore.override.adapter.AdapterSimple;
import android.view.View;
import android.view.ViewGroup;

/**
 * 新的itemview
 * 
 * @author yujian
 *
 */
public class AdapterMyOrderNew extends AdapterSimple {

	private List<? extends Map<String, ?>> data;
	private MyOrderActivity context;
	private int id = 0;
	private String url="";
	private String mall_stat_statistic;
	public AdapterMyOrderNew(MyOrderActivity context, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, int id) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.context = context;
		this.id = id;
	}
	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String, String> map = (Map<String, String>) data.get(position);
		String payment_order_satus = map.get("payment_order_status");
		if ("2".equals(payment_order_satus)) {
			ViewHodler viewHodler;
			if (convertView == null || !(convertView.getTag() instanceof ViewHodler)) {
				viewHodler = new ViewHodler(new OrderItemFatherView(context));
				convertView = viewHodler.view;
				convertView.setTag(viewHodler);
			} else
				viewHodler = (ViewHodler) convertView.getTag();
			viewHodler.setData(map);
		} else {
			ViewHodlerPayment viewHolderPayment;
			if (convertView == null || !(convertView.getTag() instanceof ViewHodlerPayment)) {
				viewHolderPayment = new ViewHodlerPayment(new OrderItemView(context));
				convertView = viewHolderPayment.view;
				convertView.setTag(viewHolderPayment);
			} else
				viewHolderPayment = (ViewHodlerPayment) convertView.getTag();
			viewHolderPayment.setData(map, position);
		}
		return convertView;
	}

	class ViewHodlerPayment {// 未拆单
		OrderItemView view;

		public ViewHodlerPayment(OrderItemView view) {
			this.view = view;
		}

		public void setData(Map<String, String> map, int position) {
			view.setData(map, position, id);
			view.setUrl(url,mall_stat_statistic);
			view.setInterfaceCallBack(new InterfaceCallBack() {

				@Override
				public void delItem(int position) {
					data.remove(position);
					notifyDataSetChanged();
				}
			});
		}
	}

	class ViewHodler {// 拆单
		OrderItemFatherView view;

		public ViewHodler(OrderItemFatherView view) {
			this.view = view;
		}

		public void setData(Map<String, String> map) {
			view.setData(map, id);
			view.setUrl(url,mall_stat_statistic);
		}
	}
}
