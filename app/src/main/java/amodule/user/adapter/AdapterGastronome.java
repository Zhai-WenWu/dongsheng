package amodule.user.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;

/**
 * @author zeyue_t
 *	@time 2015年5月19日上午9:15:59
 */
public class AdapterGastronome extends AdapterSimple {
	private Context mContext = null;
	private ArrayList<Map<String,String>> data = null;

	public AdapterGastronome(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mContext  = parent.getContext();
		this.data = (ArrayList<Map<String,String>>)data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.a_xh_gastronome_right_item, parent,false);
			holder.ll_root = (LinearLayout) convertView.findViewById(R.id.ll_root);
			holder.iv_userImg = (ImageView) convertView.findViewById(R.id.iv_userImg);
			holder.iv_userType = (ImageView) convertView.findViewById(R.id.iv_userType);
			holder.iv_lv = (ImageView) convertView.findViewById(R.id.iv_lv);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_SubjectNum = (TextView) convertView.findViewById(R.id.tv_SubjectNum);
			holder.tv_LikeNum = (TextView) convertView.findViewById(R.id.tv_LikeNum);
			holder.iv_item_choose = (TextView) convertView.findViewById(R.id.iv_item_choose);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Map<String,String> map = data.get(position);
		if(holder.iv_userImg!=null)setViewImage(holder.iv_userImg, map.get("img"));
		AppCommon.setLvImage(Integer.parseInt(map.get("lv")), holder.iv_lv);
		if(map.get("isGourmet") != null)
			AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), holder.iv_userType);
		setViewText(holder.tv_name, map.get("nickName"));
		setViewText(holder.tv_SubjectNum, map.get("allSubjectNum"));
		setViewText(holder.tv_LikeNum, map.get("allLikeNum"));
		setViewChooseState(holder.iv_item_choose,map.get("folState"));
		setClickListener(holder,map);
		return convertView;
	}
	
	private void setClickListener(final ViewHolder holder,final Map<String,String> map) {
		holder.ll_root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String userCode = map.get("userCode");
				if(userCode != null && !userCode.equals("") && !userCode.equals("0")){
					Intent intent = new Intent(mContext, FriendHome.class);
					Bundle bundle = new Bundle();
					bundle.putString("code", userCode);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				}
			}
		});
		holder.iv_item_choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!LoginManager.isLogin()) {
					Intent intent = new Intent(mContext, LoginByAccout.class);
					mContext.startActivity(intent);
					return;
				}
				AppCommon.onAttentionClick(map.get("code"), "follow");
				if (map.get("folState").equals("3")) {
					map.put("folState", "2");
					holder.iv_item_choose.setText("关注");
					holder.iv_item_choose.setTextColor(Color.parseColor("#FB6625"));
					holder.iv_item_choose.setBackgroundResource(R.drawable.bg_round_dfcommoncolor_2);
				} else if (map.get("folState").equals("2")) {
					map.put("folState", "3");
					holder.iv_item_choose.setText("已关注");
					holder.iv_item_choose.setTextColor(Color.parseColor("#C9C9C9"));
					holder.iv_item_choose.setBackgroundResource(R.drawable.bg_round_grey2);
				}
			}
		});
	}

	//设置用户关注状态
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

	class ViewHolder {
		LinearLayout ll_root;
		ImageView iv_userImg;
		ImageView iv_userType;
		ImageView iv_lv;
		TextView tv_name;
		TextView tv_SubjectNum;
		TextView tv_LikeNum;
		TextView iv_item_choose;
	}
}
