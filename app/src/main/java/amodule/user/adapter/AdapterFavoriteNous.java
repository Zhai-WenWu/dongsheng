/**
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.user.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;

public class AdapterFavoriteNous extends AdapterSimple {

	public int contentWidth = 0;
	private BaseActivity mAct;
	private List<? extends Map<String, ?>> data;

	public AdapterFavoriteNous(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.mAct = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Map<String, String> map = (Map<String, String>)data.get(position);
		view.findViewById(R.id.search_fake_layout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.relativeLayout2).setVisibility(View.GONE);
		view.findViewById(R.id.nous_image).setVisibility(View.GONE);
		TextView tv_nousContent = (TextView) view.findViewById(R.id.tv_nousContent1);
		TextView tv_nousContent2 = (TextView) view.findViewById(R.id.tv_nousContent2);
		String content = map.get("content");
		int number = contentWidth / ToolsDevice.sp2px(mAct, Tools.getDimen(mAct,R.dimen.dp_15));
		int number2 = number + (number / 2);
		tv_nousContent.setText(content.substring(0, number));
		if (content.length()<= number2) {
			tv_nousContent2.setText(content.substring(number, content.length()));
		} else {
			tv_nousContent2.setText(content.substring(number, number2) + "...");
		}
		return view;
	}
}
