package amodule.user.adapter;

import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.activity.ScoreStore;
import xh.basic.tool.UtilString;

public class AdapterScoreStore extends AdapterSimple {

	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mLayoutInflater;
	private ScoreStore mAct;
	private int mWidthPx;

	public AdapterScoreStore(ScoreStore act, View parent,List<? extends Map<String, ?>> data, int resource, String[] from,int[] to) {
		super(parent, data, resource, from, to);
		mAct = act;
		mLayoutInflater = LayoutInflater.from(parent.getContext());
		mData = data;
		DisplayMetrics metrics = ToolsDevice.getWindowPx(mAct);
		mWidthPx = metrics.widthPixels;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String, String> map = (Map<String, String>) mData.get(position);
		ViewCache viewCache;
		if (convertView == null) {
			 viewCache = new ViewCache();
			 convertView =mLayoutInflater.inflate(R.layout.a_common_surprised_item_goods,parent, false);
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
		private ImageView left_iv_goods,right_iv_goods;
		private TextView left_tv_goods_name,left_tv_goods_score,left_count;
		private TextView right_tv_goods_name,right_tv_goods_score,right_count;
		
		public void setView(View view, int... param) {
			ll_left = (LinearLayout)view.findViewById(param[0]);
			ll_right = (LinearLayout)view.findViewById(param[1]);
			int dp10 = ToolsDevice.dp2px(mAct, 10);
			int dp7 = ToolsDevice.dp2px(mAct, 7);
			int viewWi = (mWidthPx - dp10 - dp10 - dp7) / 2;
			left_iv_goods = (ImageView)ll_left.findViewById(R.id.iv_goods);
			left_tv_goods_name = (TextView)ll_left.findViewById(R.id.tv_goods_name);
			left_tv_goods_score = (TextView)ll_left.findViewById(R.id.tv_goods_score);
			left_count= (TextView) ll_left.findViewById(R.id.tv_count);
			android.view.ViewGroup.LayoutParams params1 = left_iv_goods.getLayoutParams();
			params1.width = viewWi;
			params1.height = viewWi;
			right_iv_goods = (ImageView)ll_right.findViewById(R.id.iv_goods);
			right_tv_goods_name = (TextView)ll_right.findViewById(R.id.tv_goods_name);
			right_tv_goods_score = (TextView)ll_right.findViewById(R.id.tv_goods_score);
			right_count= (TextView) ll_right.findViewById(R.id.tv_count);
			android.view.ViewGroup.LayoutParams params2 = right_iv_goods.getLayoutParams();
			params2.width = viewWi;
			params2.height = viewWi;
		}

		public void setValue(Map<String, String> map) {
			final Map<String,String> left = UtilString.getListMapByJson(map.get("left")).get(0);
			left_tv_goods_name.setText(Html.fromHtml(left.get("name")));
			left_tv_goods_score.setText(left.get("scoreNum"));
			setViewImage(left_iv_goods,left.get("imgShows"));
			if(left.containsKey("itemCount")&& !TextUtils.isEmpty(left.get("itemCount"))){
				left_count.setVisibility(View.VISIBLE);
				if(Integer.parseInt(left.get("itemCount"))>0){
					String itemCount = left.get("itemCount");
					int size = itemCount.length();
					SpannableString strTitle = new SpannableString("还剩"+itemCount+"件");
					strTitle.setSpan(new ForegroundColorSpan(mAct.getResources().getColor(R.color.comment_color)),2,2 + size, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
					left_count.setText(strTitle);
				}else{
					left_count.setTextColor(Color.parseColor("#9f9f9f"));
					left_count.setText("兑完了");
				}
			}else left_count.setVisibility(View.GONE);
			ll_left.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					XHClick.mapStat(mAct, mAct.STATISTICS_ID, "商品点击","");
					String code = left.get("code");
					if (code != null && code != "") {
						String url = StringManager.api_commodityDetail + "?item=" + code;
						 AppCommon.openUrl(mAct, url, true);
					}else{
						Tools.showToast(mAct, "该商品已下架");
					}
				}
			});
			if(map.containsKey("right")){
				final Map<String,String> right = UtilString.getListMapByJson(map.get("right")).get(0);
				right_tv_goods_name.setText(right.get("name"));
				right_tv_goods_score.setText(right.get("scoreNum"));
				setViewImage(right_iv_goods,right.get("imgShows"));
				ll_right.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						XHClick.mapStat(mAct, mAct.STATISTICS_ID,"商品点击","");
						String code = right.get("code");
						if (code != null && code != "") {
							String url = StringManager.api_commodityDetail + "?item=" + code;
							 AppCommon.openUrl(mAct, url, true);
						}else{
							Tools.showToast(mAct, "该商品已下架");
						}
					}
				});
				if(right.containsKey("itemCount")&& !TextUtils.isEmpty(right.get("itemCount"))){
					right_count.setVisibility(View.VISIBLE);
					String itemCount = right.get("itemCount");
					if(Integer.parseInt(itemCount)>0){
						int size = itemCount.length();
						SpannableString strTitle = new SpannableString("还剩"+itemCount+"件");
						strTitle.setSpan(new ForegroundColorSpan(mAct.getResources().getColor(R.color.comment_color)),2,2 + size, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
						right_count.setText(strTitle);
					}else{
						right_count.setText("兑完了");
						right_count.setTextColor(Color.parseColor("#9f9f9f"));
					}
				}else right_count.setVisibility(View.GONE);
				ll_right.setVisibility(View.VISIBLE);
			}else{
				ll_right.setVisibility(View.INVISIBLE);
			}
		}
	}
}
