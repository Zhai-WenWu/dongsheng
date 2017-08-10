package amodule.dish.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.DetailDish;
import xh.basic.tool.UtilString;

public class AdapterClassifyDish extends AdapterSimple{
	public static final int styleNormal = 1;
	public static final int styleTitle = 2;

	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mLayoutInflater;
	private Activity mAct;

	public AdapterClassifyDish(Activity act, View parent,List<? extends Map<String, ?>> data, int resource, String[] from,int[] to) {
		super(parent, data, resource, from, to);
		mAct = act;
		mLayoutInflater = LayoutInflater.from(parent.getContext());
		mData = data;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mData.get(position);
		ViewCache viewCache = null;
		if (convertView == null) {
			 viewCache = new ViewCache();
			 convertView =mLayoutInflater.inflate(R.layout.a_dish_classify_item,parent, false);
			 int[] viewId = new int[] { R.id.ll_left,R.id.ll_right};
			 viewCache.setView(convertView, viewId);
			 convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map);
		return convertView;
	}

	class ViewCache {
		private LinearLayout ll_left,ll_right;
		private ImageViewVideo left_iv_dish,right_iv_dish;
		private TextView left_tv_dish_name,left_tv_dish_score;
		private TextView right_tv_dish_name,right_tv_dish_score;
		
		public void setView(View view, int... param) {
			ll_left = (LinearLayout)view.findViewById(param[0]);
			ll_right = (LinearLayout)view.findViewById(param[1]);
//			int dp10 = ToolsDevice.dp2px(mAct, 10);
//			int dp7 = ToolsDevice.dp2px(mAct, 7);
//			int viewWi = (mWidthPx - dp10 - dp10 - dp7) / 2;
			left_iv_dish = (ImageViewVideo)ll_left.findViewById(R.id.iv_video_img);
			left_tv_dish_name = (TextView)ll_left.findViewById(R.id.tv_dish_name);
			left_tv_dish_score = (TextView)ll_left.findViewById(R.id.tv_dish_unit);
//			android.view.ViewGroup.LayoutParams params1 = left_iv_dish.getLayoutParams();
//			params1.width = viewWi;
//			params1.height = viewWi;
			right_iv_dish = (ImageViewVideo)ll_right.findViewById(R.id.iv_video_img);
			right_tv_dish_name = (TextView)ll_right.findViewById(R.id.tv_dish_name);
			right_tv_dish_score = (TextView)ll_right.findViewById(R.id.tv_dish_unit);
//			android.view.ViewGroup.LayoutParams params2 = right_iv_dish.getLayoutParams();
//			params2.width = viewWi;
//			params2.height = viewWi;
		}

		public void setValue(Map<String, String> map) {
			final Map<String,String> left = UtilString.getListMapByJson(map.get("left")).get(0);
			left_tv_dish_name.setText(left.get("name"));
			left_tv_dish_score.setText("播放 " + left.get("allClick"));
			left_iv_dish.parseItemImg(left.get("img"), left.get("hasVideo"),false);
			left_iv_dish.playImgWH = Tools.getDimen(mAct, R.dimen.dp_29);
//			setViewImage(left_iv_dish,left.get("imgShows"));
			ll_left.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mAct, DetailDish.class);
					intent.putExtra("code", left.get("code"));
					intent.putExtra("name", left.get("name"));
					mAct.startActivity(intent);
				}
			});
			if(map.containsKey("right")){
				final Map<String,String> right = UtilString.getListMapByJson(map.get("right")).get(0);
				right_tv_dish_name.setText(right.get("name"));
				right_tv_dish_score.setText("播放 " + right.get("allClick"));
				right_iv_dish.parseItemImg(right.get("img"), right.get("hasVideo"),false);
				right_iv_dish.playImgWH = Tools.getDimen(mAct, R.dimen.dp_29);
//				setViewImage(right_iv_dish,right.get("imgShows"));
				ll_right.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mAct, DetailDish.class);
						intent.putExtra("code", right.get("code"));
						intent.putExtra("name", right.get("name"));
						mAct.startActivity(intent);
					}
				});
				ll_right.setVisibility(View.VISIBLE);
			}else{
				ll_right.setVisibility(View.INVISIBLE);
			}
		}
	}
}
