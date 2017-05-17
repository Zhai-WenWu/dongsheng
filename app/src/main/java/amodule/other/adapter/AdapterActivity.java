/**
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.other.adapter;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

@SuppressLint("ResourceAsColor")
public class AdapterActivity extends AdapterSimple {

	public int subjectImgWidth = 0;
	private BaseActivity act;
	private List<? extends Map<String, ?>> data;

	public AdapterActivity(BaseActivity act, View parent,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.act = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		ImageView imageView = (ImageView) view.findViewById(R.id.activity_img);
		imageView.getLayoutParams().height = imgHeight;

		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) data.get(position);
		TextView textView = (TextView) view.findViewById(R.id.activity_type);
		
		ImageView labelView = (ImageView) view.findViewById(R.id.activity_labelView);
		if (map.containsKey("state")) {
			if (map.get("state").equals("1")) {
				labelView.setImageResource(R.drawable.activity_nostart);
			}else if (map.get("state").equals("2")) {
				labelView.setImageResource(R.drawable.activity_being);
			}else if (map.get("state").equals("3")) {
				labelView.setImageResource(R.drawable.activity_end);
			}
		}
		if (map.get("type").equals("1")) {
			textView.setText("活动");
			textView.setBackgroundResource(R.color.comment_color);
		} else if (map.get("type").equals("2")){
			textView.setText("专题");
			textView.setBackgroundResource(R.color.activity_bg2);
		}
		setOnViewClick(view,map.get("url"));
		
		return view;
	}

	private void setOnViewClick(View view, final String url) {
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppCommon.openUrl(act, url, true);
			}
		});
	}

}
