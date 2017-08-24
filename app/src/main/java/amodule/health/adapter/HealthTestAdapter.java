package amodule.health.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

public class HealthTestAdapter extends AdapterSimple {
	public int[] selections = { R.id.health_item_selection_1, R.id.health_item_selection_2, R.id.health_item_selection_3,
			R.id.health_item_selection_4, R.id.health_item_selection_5 };
	public int viewHeight = 0;
	public ArrayList<Map<String, String>> testList = new ArrayList<>();
	public View nextView = null;
	public int n = 0;
	public boolean sex_selected = false;

	@SuppressWarnings("unchecked")
	public HealthTestAdapter(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.testList = (ArrayList<Map<String, String>>) data;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		if (testList.get(position).containsKey("classifyName")) 
			view.findViewById(R.id.health_item_select_classify).setVisibility(View.VISIBLE);
		for (int selection : selections) {
			final LinearLayout health_item_selection = (LinearLayout) view.findViewById(selection);
			ViewGroup.LayoutParams lp = health_item_selection.getLayoutParams();
			lp.height = viewHeight;
		}
		if (testList.get(position).containsKey("selected")) {
			String selected_num = testList.get(position).get("selected");
			int count = Integer.valueOf(selected_num.substring(0, 1));
			LinearLayout selection_layout = (LinearLayout) view.findViewById(R.id.health_selection_layout);
			LinearLayout answer_layout = (LinearLayout) selection_layout.getChildAt(count - 1);
			TextView text1 = (TextView) answer_layout.getChildAt(0);
			TextView text2 = (TextView) answer_layout.getChildAt(1);
			TextView question_num = (TextView) view.findViewById(R.id.health_item_select_question_num);
			TextView title = (TextView) view.findViewById(R.id.health_item_select_question_text);
			TextView answer = (TextView) view.findViewById(R.id.health_item_select_answer);
			
			answer_layout.setBackgroundResource(R.drawable.bg_round_green_test);
			question_num.setTextColor(Color.parseColor("#999999"));
			title.setTextColor(Color.parseColor("#999999"));
			answer.setText(text1.getText());
			text1.setTextColor(Color.WHITE);
			text2.setTextColor(Color.WHITE);
		} else if (n == 0) {
			view.findViewById(R.id.health_selection_layout).setVisibility(View.VISIBLE);
			n++;
		}
		return view;
	}

}
