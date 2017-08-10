package amodule.quan.view;

import java.util.Map;

import acore.tools.Tools;
import amodule.quan.activity.ShowSubject;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;
/**
 * 公告置顶view
 * @author Administrator
 *
 */
public class CircleHeaderTopAndNoticeView extends LinearLayout{

	private Map<String, String> maps; 
	private Context mContext;
	public CircleHeaderTopAndNoticeView(Context context,Map<String, String> map) {
		super(context);
		this.maps=map;
		this.mContext= context;
		LayoutInflater.from(context).inflate(R.layout.circle_headerview_item_hot, this, true);
		initView();
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent2 = new Intent(mContext, ShowSubject.class);
				intent2.putExtra("code", maps.get("code"));
				mContext.startActivity(intent2);
			}
		});
	}

	private void initView() {
		TextView tv_item_style = (TextView) findViewById(R.id.tv_item_style);
		TextView tv_item_title = (TextView)findViewById(R.id.tv_item_title);
		TextView tv_item_num = (TextView) findViewById(R.id.tv_item_num);
		String style = maps.get("style");
		if (!TextUtils.isEmpty(style)) {
			if (style.equals("3")) {
				tv_item_style.setText("置顶");
				String color = Tools.getColorStr(getContext(),R.color.comment_color);
				tv_item_style.setTextColor(Color.parseColor(color));
				tv_item_style.setBackgroundResource(R.drawable.round_red);
			} else if (style.equals("2")) {
				tv_item_style.setText("公告");
				tv_item_style.setTextColor(Color.parseColor("#00cc33"));
				tv_item_style.setBackgroundResource(R.drawable.round_blue);
			} else if (style.equals("4")) {
				tv_item_style.setText("活动");
				String color = Tools.getColorStr(getContext(),R.color.comment_color);
				tv_item_style.setTextColor(Color.parseColor(color));
				tv_item_style.setBackgroundResource(R.drawable.round_red);
			}else {
				tv_item_style.setText("公告");
				tv_item_style.setTextColor(Color.parseColor("#00cc33"));
				tv_item_style.setBackgroundResource(R.drawable.round_blue);
			}
		}
		tv_item_title.setText(maps.get("title"));
		changeMiddleView(maps, this);
		setTag(maps.get("code"));
	}
	/**
	 * 改变中间view:只改变评论消息数据
	 * 
	 * @param map
	 */
	public void changeMiddleView(Map<String, String> map, View view) {
		if (map.containsKey("commentNum") && !TextUtils.isEmpty(map.get("commentNum")) && Integer.parseInt(map.get("commentNum")) > 0) {
			view.findViewById(R.id.linear_item_num).setVisibility(View.VISIBLE);
			((TextView) view.findViewById(R.id.tv_item_num)).setText(map.get("commentNum"));
		} else {
			view.findViewById(R.id.linear_item_num).setVisibility(View.GONE);
		}
	}
}
