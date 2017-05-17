package third.mall.adapter;

import java.util.List;
import java.util.Map;

import third.mall.view.MallAdvertItemView;
import acore.override.adapter.AdapterSimple;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class AdapterMallAdvert extends AdapterSimple{

	private Activity  context;
	private List<? extends Map<String, ?>> data;

	public AdapterMallAdvert(Activity context,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.context= context;
		this.data= data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String,String> map= (Map<String, String>) data.get(position);
		ViewHolder viewHolder;
		if(null==convertView){
			viewHolder=new ViewHolder(new MallAdvertItemView(context));
			convertView= viewHolder.view;
			convertView.setTag(viewHolder);
		}else viewHolder=(ViewHolder) convertView.getTag();
		viewHolder.setValues(map);
		return convertView;
	}
	
	class ViewHolder{
		private MallAdvertItemView view;
		public ViewHolder(MallAdvertItemView view){
			this.view=view;
		}
		public void setValues(Map<String,String> map){
			view.setData(map);
		}
	}
}
