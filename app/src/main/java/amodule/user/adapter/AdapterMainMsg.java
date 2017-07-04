package amodule.user.adapter;

import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.widget.TextViewShow;
import xh.basic.tool.UtilImage;
import acore.logic.AppCommon;
import acore.override.adapter.AdapterSimple;
import amodule.user.activity.FriendHome;
import amodule.user.activity.MyMessage;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import aplug.basic.SubBitmapTarget;

/** 
 * @Description:
 * @Title: AdapterMainMsg.java Copyright: Copyright (c) xiangha.com 2014~2017
 *
 * @author: intBird   修改 zeyu_t
 * @date: 2014年2月13日 下午12:06:35   
 */
public class AdapterMainMsg extends AdapterSimple {

	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mLayoutInflater;
	private static MyMessage mAct;

	public AdapterMainMsg(MyMessage act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mData = data;
		mLayoutInflater = LayoutInflater.from(parent.getContext());
		mAct = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 数据来源
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mData.get(position);
		// 缓存视图
		ViewCacheNormal viewCacheNormal = null;
		// 获取样式
		if (convertView == null) {
			viewCacheNormal = new ViewCacheNormal();
			convertView = mLayoutInflater.inflate(R.layout.list_item_quan_message, parent, false);
			viewCacheNormal.setView(convertView);
			convertView.setTag(viewCacheNormal);
		} else {
			viewCacheNormal = (ViewCacheNormal) convertView.getTag();
		}
		viewCacheNormal.setValue(map);
		if(position == getCount()-1){
			convertView.findViewById(R.id.iv_spector).setVisibility(View.GONE);
		}else{
			convertView.findViewById(R.id.iv_spector).setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	public class ViewCacheNormal {
		final int viewUser = 0;// 访问用户
		final int viewOther = 3;// 访问other;

		LinearLayout item_root_view;
		ImageView iv_item_user_img;
		ImageView iv_item_sub_img;
		ImageView iv_userType;
		RelativeLayout linear_content;

		TextView tv_item_user_name;
		TextView tv_item_admin_name;
		TextView tv_item_admin;
		TextViewShow tv_item_content;
		ImageView iv_item_zan;
		TextView tv_item_time;

		/**
		 * 初始化界面
		 * @param view
		 */
		public void setView(View view) {
			item_root_view = (LinearLayout) view.findViewById(R.id.item_root_view);
			iv_item_user_img = (ImageView) view.findViewById(R.id.iv_item_user_img);
			iv_userType = (ImageView) view.findViewById(R.id.iv_userType);
			tv_item_admin = (TextView) view.findViewById(R.id.tv_item_admin);
			tv_item_admin_name = (TextView) view.findViewById(R.id.tv_item_admin_name);
			iv_item_sub_img = (ImageView) view.findViewById(R.id.iv_item_sub_img);
			linear_content = (RelativeLayout) view.findViewById(R.id.linear_content);
			tv_item_user_name = (TextView) view.findViewById(R.id.tv_item_user_name);
			tv_item_content = (TextViewShow) view.findViewById(R.id.tv_item_content);
			tv_item_content.setHaveCopyFunction(false);
			iv_item_zan = (ImageView) view.findViewById(R.id.iv_item_zan);
			tv_item_time = (TextView) view.findViewById(R.id.tv_item_time);
		}

		/**
		 * 设置数据和事件
		 * @param map
		 */
		public void setValue(Map<String, String> map) {
			AdapterMainMsg.this.setViewImage(iv_item_user_img, map.get("nickImg"));
			AdapterMainMsg.this.setViewImage(iv_item_sub_img, map.get("img"));
			AdapterMainMsg.this.setViewText(tv_item_user_name, map.get("nickName"));
			AdapterMainMsg.this.setViewText(tv_item_admin_name, map.get("adminName"));
			AdapterMainMsg.this.setViewText(tv_item_admin, map.get("admin"));
			AdapterMainMsg.this.setViewText(tv_item_content, map.get("content"));
			AdapterMainMsg.this.setViewImage(iv_item_zan, map.get("isLike"));
			AdapterMainMsg.this.setViewText(tv_item_time, map.get("addTimeShow"));
			if(map.get("isGourmet") != null)
				AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), iv_userType);
			parseBkColor(map, item_root_view);

			setClickEvent(map, iv_item_user_img, viewUser);
			setClickEvent(map, tv_item_user_name, viewUser);
			setClickEvent(map, item_root_view, map.get("msgType").equals("3") ? viewUser : viewOther);
			setClickEvent(map, tv_item_content, map.get("msgType").equals("3") ? viewUser : viewOther);
		}

		/**
		 * 设置新消息的背景颜色;
		 * @param map
		 * @param v
		 */
		public void parseBkColor(Map<String, String> map, View v) {
			if (map.containsKey("bgColor")) {
				v.setBackgroundColor(Color.parseColor(map.get("bgColor")));
			} else {
				v.setBackgroundColor(Color.parseColor("#00000000"));
			}
		}

		/**
		 * 点击事件
		 * @param map
		 * @param view
		 * @param type
		 */
		private void setClickEvent(final Map<String, String> map, final View view, final int type) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (type) {
					case viewUser:
						if (!"2".equals(map.get("msgType"))) {
							Intent intent1 = new Intent(mAct, FriendHome.class);
							Bundle bundle = new Bundle();
							bundle.putString("code", map.get("nickCode"));
							if(map.get("msgType").equals("3"))
								bundle.putString("newsId", map.get("id"));
							intent1.putExtras(bundle);
							mAct.startActivity(intent1);
						}
						break;
					case viewOther:
						XHClick.track(view.getContext(), "点击消息列表页");
						String url=null;
						if(map.get("state").equals("1"))
							url = map.get("url")+ "&newsId=" + map.get("id");
						else
							url = map.get("url");
						AppCommon.openUrl(mAct, url, true);
//						mData.remove(map);
						break;
					}
					// 点击后延迟1秒移除颜色值
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							if (map.containsKey("bgColor"))
								map.remove("bgColor");
							for (int i = 0; i < mData.size(); i++) {
								if (mData.get(i).get("msgType").equals("1") && mData.get(i).get("type").equals("3")) {
									@SuppressWarnings("unchecked")
									Map<String, String> data = (Map<String, String>) mData.get(i);
									data.put("state", "2");
									if (data.containsKey("bgColor"))
										data.remove("bgColor");
								}
							}
							notifyDataSetChanged();
						}
					}, 1000);
				}
			});
		}
	}
	
	@Override
	public SubBitmapTarget getTarget(final ImageView v, final String url) {
		return new SubBitmapTarget() {
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = null;
				if (v.getTag(TAG_ID).equals(url))
					img = v;
				if (img != null && bitmap != null) {
					// 图片圆角和宽高适应
					img.setScaleType(scaleType);
					if (img.getId() == R.id.iv_item_user_img) {
						bitmap = UtilImage.toRoundCorner(img.getResources(), bitmap, roundType, 500);
					} else {
						bitmap = UtilImage.toRoundCorner(img.getResources(), bitmap, roundType, roundImgPixels);
					}
					UtilImage.setImgViewByWH(img, bitmap, imgWidth, imgHeight, imgZoom);
				}
			}
		};
	}
	
}
