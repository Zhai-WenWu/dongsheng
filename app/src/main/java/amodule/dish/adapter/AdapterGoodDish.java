package amodule.dish.adapter;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import amodule.dish.view.TodayGoodView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * 今日推荐adapter
 * @author Administrator
 *
 */
public class AdapterGoodDish extends AdapterSimple{

	private List<Map<String,String>> listData;
	private Context context;
	public AdapterGoodDish(Context context,View parent, List<? extends Map<String, ?>> data) {
		super(parent, data, 0, null, null);
		this.context= context;
		this.listData=(List<Map<String, String>>) data;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String, String> map = (Map<String, String>) listData.get(position);
		DishHolder holder = null;
		if(convertView == null){//
			holder = new DishHolder();
			holder.view = new TodayGoodView(context);
			convertView = holder.view;
			convertView.setTag(holder);
		}else{
			holder = (DishHolder) convertView.getTag();
		}
		holder.setData(map);
		return convertView;
	}

	public class DishHolder{
		TodayGoodView view;
		public void setData(Map<String, String> map){
			if(view != null){
				view.setData(map);
			}
		}
	}
}
