package third.mall.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import third.mall.view.AddressChangeItemView;

/**
 * 地址选择adapter
 * @author Administrator
 *
 */
public class AdapterAddressChange extends AdapterSimple{
	private List<? extends Map<String, ?>> data;
	private BaseActivity context;
	private String now_address_id;
	public AdapterAddressChange(BaseActivity context,String now_address_id,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.now_address_id= now_address_id;
		this.data = data;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = (Map<String, String>)data.get(position);
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = holder.view = new AddressChangeItemView(context);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.setData(map);
		return convertView;
	}
	public void setNowAddress(String now_address_id){
		this.now_address_id= now_address_id;
		notifyDataSetChanged();
	}
	private class ViewHolder{
		AddressChangeItemView view;
		public void setData(Map<String, String> map){
			if(view!=null){
				view.setChangeData(map);
				if(!TextUtils.isEmpty(now_address_id))
					view.setChangeState(now_address_id);
			}
			
		}
	}
}
