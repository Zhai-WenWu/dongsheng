package amodule.main.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xianghatest.R;

public class AdapterTopUser extends AdapterSimple {
	private Context mContext;
	private ArrayList<Map<String, String>> data;

	@SuppressWarnings("unchecked")
	public AdapterTopUser(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.mContext = parent.getContext();
		this.data = (ArrayList<Map<String, String>>) data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view =  super.getView(position, convertView, parent);
		Map<String,String> map = data.get(position);
		ImageView userIco = (ImageView) view.findViewById(R.id.iv_userImg);
		userIco.getLayoutParams().width = viewWidth - Tools.getDimen(mContext,  R.dimen.dp_15);
		userIco.getLayoutParams().height = viewWidth - Tools.getDimen(mContext,  R.dimen.dp_15);
		ImageView userType = (ImageView) view.findViewById(R.id.iv_userType);
		if(!TextUtils.isEmpty(map.get("isGourmet")))
			AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), userType);
		return view;
	}
}
