package amodule.user.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.user.activity.login.LoginByAccout;

@SuppressLint("ResourceAsColor")
public class AdapterFansFollwers extends AdapterSimple {
	public ArrayList<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
	public TextView item_choose;
	private Map<String, String> map;
	public int UI_TYPE = 1;
	public BaseActivity act;

	@SuppressWarnings("unchecked")
	public AdapterFansFollwers(BaseActivity act,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.act=act;
		this.arrayList = (ArrayList<Map<String, String>>) data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Map<String, String> textmap = arrayList.get(position );
		ImageView lv_img=(ImageView) view.findViewById(R.id.fans_user_lv);
		ImageView iv_userType=(ImageView) view.findViewById(R.id.iv_userType);
		AppCommon.setLvImage(Integer.parseInt(textmap.get("lv").replace("lv", "")), lv_img);
		if(textmap.get("isGourmet") != null)
			AppCommon.setUserTypeImage(Integer.parseInt(textmap.get("isGourmet")), iv_userType);
		final DownRefreshList listView = (DownRefreshList) parent;
		item_choose = (TextView) view.findViewById(R.id.fans_user_item_choose);
		if (textmap.get("folState").equals("folState1")) {
			item_choose.setVisibility(View.GONE);
		} else if (textmap.get("folState").equals("folState2")) {
			item_choose.setText("关注");
			String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
			item_choose.setTextColor(Color.parseColor(color));
			item_choose.setBackgroundResource(R.drawable.bg_round_dfcommoncolor_2);
		} else if (textmap.get("folState").equals("folState3")) {
			item_choose.setText("已关注");// C9C9C9
			item_choose.setTextColor(Color.parseColor("#C9C9C9"));
			item_choose.setBackgroundResource(R.drawable.bg_round_grey2);
		}
		item_choose.setClickable(true);
		item_choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(LoginManager.isLogin()){
					TextView tv = (TextView) v;
					int index = listView.getPositionForView(v);
					map = arrayList.get(index - UI_TYPE);
					AppCommon.onAttentionClick(map.get("code"), "follow");
					if (map.get("folState").equals("folState3")) {
						map.put("folState", "folState2");
						tv.setText("关注");
						String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
						tv.setTextColor(Color.parseColor(color));
						tv.setBackgroundResource(R.drawable.bg_round_dfcommoncolor_2);
					} else if (map.get("folState").equals("folState2")) {
						map.put("folState", "folState3");
						tv.setText("已关注");
						tv.setTextColor(Color.parseColor("#C9C9C9"));
						tv.setBackgroundResource(R.drawable.bg_round_grey2);
					}
					notifyDataSetChanged();
				}else{
					Intent intent=new Intent(act,LoginByAccout.class);
					act.startActivity(intent);
				}
			}
		});
		return view;
	}
}
