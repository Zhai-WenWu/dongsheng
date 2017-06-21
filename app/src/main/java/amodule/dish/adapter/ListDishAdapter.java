package amodule.dish.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Map;

import amodule.dish.view.ListDishItemView;

/**
 *
 * @author sll
 * @date 2017年06月22日
 */
public class ListDishAdapter extends BaseAdapter {
	private ArrayList<Map<String, String>> mDatas;
	private Context mContext;

	public ListDishAdapter(Context context) {
		mContext = context;
	}

	public void setData(ArrayList<Map<String, String>> datas) {
		mDatas = datas;
	}


	@Override
	public int getCount() {
		return mDatas == null ? 0 : mDatas.size();
	}

	@Override
	public Map<String, String> getItem(int position) {
		return mDatas == null ? null : mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {
			convertView = new ListDishItemView(mContext);
			holder = new ViewHolder((ListDishItemView) convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mDatas != null && mDatas.size() > position)
			holder.setData(mDatas.get(position));
		return convertView;
	}

	private class ViewHolder {
		private ListDishItemView mItemView;
		public ViewHolder (ListDishItemView itemView){
			mItemView = itemView;
		}

		public void setData(Map<String, String> data) {
			if (data == null || data.size() <= 0 || mItemView == null)
				return;
			mItemView.setData(data);
		}
	}
}
