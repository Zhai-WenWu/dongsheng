package amodule.dish.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.avtivity.HomeSearch;

/**
 * Title:AdapterListDish.java Copyright: Copyright (c) 2014~2017
 *
 * @author zeyu_t
 * @date 2014年10月14日
 */
public class AdapterListDish extends AdapterSimple {
	private List<? extends Map<String, ?>> data;
	private BaseActivity mAct;
	public int viewHeight = 0;
	private int height = 0 ;

	public AdapterListDish(BaseActivity mAct, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,
						   String type) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.mAct = mAct;
		int screenWidth = ToolsDevice.getWindowPx(mAct).widthPixels;
		int dp_30 = Tools.getDimen(mAct, R.dimen.dp_30);
		height = (screenWidth - dp_30) / 3 * 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) data.get(position);
		setViewImage((ImageView) view.findViewById(R.id.img), map.get("img"));
		parseUserIcon(view, map.get("customer"));
		String title = map.get("alias");
		if (TextUtils.isEmpty(title))
			title = map.get("name");
		parseTitle(view, title);
		setOnclick(map, view);
		parsePlayImg(view, map.get("hasVideo"));
		return view;
	}

	/**
	 * 显示播放按钮
	 * @param view
	 * @param hasVideo
	 */
	private void parsePlayImg(View view, String hasVideo) {
		if (view == null)
			return;
		if ("2".equals(hasVideo))
			view.findViewById(R.id.play_img).setVisibility(View.VISIBLE);
		else
			view.findViewById(R.id.play_img).setVisibility(View.GONE);
	}

	/**
	 * 标题
	 * @param view
	 * @param title
	 */
	private void parseTitle(View view, String title) {
		if (view == null || TextUtils.isEmpty(title))
			return;
		((TextView)view.findViewById(R.id.title_top)).setText(title);
	}

	/**
	 * 显示美食家图标
	 * @param view
	 * @param customer
	 */
	private void parseUserIcon(View view, String customer) {
		if (view == null || TextUtils.isEmpty(customer))
			return;
		ArrayList<Map<String, String>> customerStrs = StringManager.getListMapByJson(customer);
		if (customerStrs != null && !customerStrs.isEmpty()) {
			Map<String, String> customerStr = customerStrs.get(0);
			if (customerStr != null && !customerStr.isEmpty()) {
				String isGourmet = customerStr.get("isGourmet");
				if ("2".equals(isGourmet)) {
					view.findViewById(R.id.gourmet_icon).setVisibility(View.VISIBLE);
				} else
					view.findViewById(R.id.gourmet_icon).setVisibility(View.GONE);
			}
		}
	}

	// 绑定点击动作
	private void setOnclick(final Map<String, String> map, final View view) {
		if (map == null || map.isEmpty())
			return;
		view.findViewById(R.id.icon_search_container).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mAct, HomeSearch.class);
				Bundle bundle = new Bundle();
				String serachKey = map.get("name");
				bundle.putString("s", serachKey);
				intent.putExtras(bundle);
				mAct.startActivity(intent);
			}
		});
	}
}
