package amodule.quan.adapter;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import amodule.user.view.FollowView;
import android.view.View;
import android.view.ViewGroup;

import com.xianghatest.R;

public class AdapterCircleFind extends AdapterSimple{

	private List<? extends Map<String, String>> mData;
	
	public AdapterCircleFind(View parent, List<? extends Map<String, String>> data,
			int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mData = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		FollowView followView = (FollowView)view.findViewById(R.id.a_circle_find_item_follow);
		followView.FOLLOW = "2";
		followView.FOLLOW_NOT = "1";
		followView.FOLLOW_GONE = "0";
		followView.setData(StringManager.api_circleApply, "&cid=" + mData.get(position).get("cid"), "type", mData.get(position).get("isFollow"));
		return view;
	}
}
