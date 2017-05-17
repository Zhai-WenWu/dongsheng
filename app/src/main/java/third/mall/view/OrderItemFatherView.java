package third.mall.view;

import java.util.ArrayList;
import java.util.Map;

import third.mall.activity.MyOrderActivity;
import third.mall.view.OrderItemView.InterfaceCallBack;
import xh.basic.tool.UtilString;
import android.widget.LinearLayout;

public class OrderItemFatherView extends LinearLayout{

	private MyOrderActivity context;
	private String url;
	private String mall_stat_statistic;
	public OrderItemFatherView(MyOrderActivity context) {
		super(context);
		this.context= context;
		this.setOrientation(1);
	}
	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic=mall_stat_statistic;
	}

	public void setData(Map<String,String> map,int id){
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
		removeAllViews();
		for (int i = 0,size=listMapByJson.size(); i < size; i++) {
			OrderItem2View itemView= new OrderItem2View(context);
			itemView.setData(listMapByJson.get(i), i, id);
			itemView.setUrl(url,mall_stat_statistic);
			itemView.setInterfaceCallBack(new InterfaceCallBack() {
				
				@Override
				public void delItem(int position) {
					
				}
			});
			this.addView(itemView);
		}
	}
}
