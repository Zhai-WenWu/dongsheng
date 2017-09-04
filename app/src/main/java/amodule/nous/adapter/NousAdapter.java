/*
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.nous.adapter;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xiangha.R;

public class NousAdapter extends AdapterSimple {
	public Activity mAct;
	public static final int styleTop = 1;
	public static final int styleNormal = 2;

	private LayoutInflater mLayoutInflater;

	public NousAdapter(Activity mAct,View parent, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mLayoutInflater = LayoutInflater.from(mAct);
		this.mAct=mAct;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return styleTop;
		else
			return styleNormal;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (getItemViewType(position)) {
		case styleTop:
			convertView = mLayoutInflater.inflate(R.layout.list_item_nous_index1, parent, false);
			ImageView img=(ImageView) convertView.findViewById(R.id.iv_nousCover);
			int width=ToolsDevice.getWindowPx(mAct).widthPixels-Tools.getDimen(mAct, R.dimen.dp_15)*2+Tools.getDimen(mAct, R.dimen.dp_10)*2;
			img.getLayoutParams().width=width;
			img.getLayoutParams().height=width*355/650;
			break;
		case styleNormal:
			convertView = mLayoutInflater.inflate(R.layout.list_item_nous_index2, parent, false);
			break;
		}
		return super.getView(position, convertView, parent);
	}
}
