package third.mall.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import third.mall.view.FavorableItemView;
import third.mall.view.FavorableItemView.interfaceCallBack;

public class AdapterFavorable extends AdapterSimple{
	private Context  context;
	private List<? extends Map<String, ?>> data;

	public AdapterFavorable(Context context,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.context= context;
		this.data= data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String,String> map= (Map<String,String>)data.get(position);
		ViewHolder viewHolder;
		if(null==convertView){
			viewHolder=new ViewHolder(new FavorableItemView(context));
			convertView= viewHolder.view;
			convertView.setTag(viewHolder);
		}else viewHolder=(ViewHolder) convertView.getTag();
		viewHolder.setValues(map);
		return convertView;
	}
	
	class ViewHolder{
		private FavorableItemView view;
		public ViewHolder(FavorableItemView view){
			this.view=view;
		}
		public void setValues(final Map<String,String> map){
			view.setData(map,new interfaceCallBack() {
				
				@Override
				public void sucess() {
					map.put("already_have", "2");
					notifyDataSetChanged();
				}
			});
		}
	}

	
}