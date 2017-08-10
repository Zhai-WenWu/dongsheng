package amodule.search.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.Tools;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;

@SuppressLint("ResourceAsColor")
public class AdapterSearchUser extends AdapterSearch {
	private ArrayList<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
	private Activity mAct;

	public AdapterSearchUser(Activity mAct,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.arrayList = (ArrayList<Map<String, String>>) data;
		this.mAct = mAct;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		final Map<String, String> map = arrayList.get(position);
		ImageView lv_img=(ImageView) view.findViewById(R.id.fans_user_lv);
		ImageView iv_userType=(ImageView) view.findViewById(R.id.iv_userType);
		AppCommon.setLvImage(Integer.parseInt(map.get("lv").replace("lv", "")), lv_img);
		if(map.get("isGourmet") != null)
			AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), iv_userType);
		final TextView item_choose = (TextView) view.findViewById(R.id.fans_user_item_choose);
		setViewChooseState(item_choose, map.get("folState").replace("folState", ""));
		item_choose.setClickable(true);
		item_choose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!LoginManager.isLogin()) {
					Intent intent = new Intent(mParent.getContext(), LoginByAccout.class);
					mParent.getContext().startActivity(intent);
					return;
				}
				AppCommon.onAttentionClick(map.get("code"),"follow");
				if (map.get("folState").equals("folState3")) {
					map.put("folState", "folState2");
					item_choose.setText("关注");
					String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
					item_choose.setTextColor(Color.parseColor(color));
					item_choose.setBackgroundResource(R.drawable.bg_round_dfcommoncolor_2);
				} else if (map.get("folState").equals("folState2")) {
					map.put("folState", "folState3");
					item_choose.setText("已关注");
					item_choose.setTextColor(Color.parseColor("#C9C9C9"));
					item_choose.setBackgroundResource(R.drawable.bg_round_grey2);
				} else if (map.get("folState").equals("folState1")) {
					item_choose.setVisibility(View.GONE);
				}
				XHClick.mapStat(XHApplication.in(), "a_search_result", "哈友结果页", "点关注和取消关注");
			}
		});

		final ImageView fans_user_img = (ImageView) view.findViewById(R.id.fans_user_img);
		final TextView fans_user_name = (TextView) view.findViewById(R.id.fans_user_name);

		fans_user_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoHaYouDetail(position);
			}
		});
		fans_user_name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoHaYouDetail(position);
			}
		});

		return view;
	}

	private void gotoHaYouDetail(int position) {
		Intent intent = new Intent(mAct, FriendHome.class);
		Bundle bundle = new Bundle();
		bundle.putString("code", arrayList.get(position).get("code"));
		intent.putExtras(bundle);
		mAct.startActivity(intent);
		XHClick.mapStat(XHApplication.in(), "a_search_result", "哈友结果页", "点头像和用户名");
	}

	private void setViewChooseState(TextView iv_item_choose, String state) {
		if (state.equals("1")) {
			iv_item_choose.setVisibility(View.GONE);
		} else if (state.equals("3")) {
			iv_item_choose.setVisibility(View.VISIBLE);
			iv_item_choose.setText("已关注");
			iv_item_choose.setTextColor(Color.parseColor("#C9C9C9"));
			iv_item_choose.setBackgroundResource(R.drawable.bg_round_grey2);
		} else if (state.equals("2")) {
			iv_item_choose.setVisibility(View.VISIBLE);
			iv_item_choose.setText("关注");
			String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
			iv_item_choose.setTextColor(Color.parseColor(color));
			iv_item_choose.setBackgroundResource(R.drawable.bg_round_dfcommoncolor_2);
		}
	}

}
