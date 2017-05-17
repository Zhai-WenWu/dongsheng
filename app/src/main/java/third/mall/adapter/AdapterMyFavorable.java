package third.mall.adapter;

import java.util.List;
import java.util.Map;

import third.mall.view.MyFavorableItemView;
import acore.override.adapter.AdapterSimple;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class AdapterMyFavorable extends AdapterSimple{
	private Activity  context;
	private List<? extends Map<String, ?>> data;
	private String id;
	private String url;
	private String mall_stat_statistic;
	public AdapterMyFavorable(Activity context,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,String id) {
		super(parent, data, resource, from, to);
		this.context= context;
		this.data= data;
		this.id= id;
	}

	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String,String> map= (Map<String, String>) data.get(position);
		ViewHolder viewHolder;
		if(null==convertView){
			viewHolder=new ViewHolder(new MyFavorableItemView(context));
			convertView= viewHolder.view;
			convertView.setTag(viewHolder);
		}else viewHolder=(ViewHolder) convertView.getTag();
		viewHolder.setValues(map);
		return convertView;
	}
	
	class ViewHolder{
		private MyFavorableItemView view;
		public ViewHolder(MyFavorableItemView view){
			this.view=view;
		}
		public void setValues(Map<String,String> map){
			view.setData(map,id);
			view.setUrl(url,mall_stat_statistic);
		}
	}

	
}