/**
 * @author intBird 20140213.
 */
package amodule.user.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.widget.TextViewShow;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.widget.ImageViewVideo;
import amodule.quan.activity.ShowSubject;
import amodule.user.activity.FriendHome;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xiangha.R;

public class AdapterMySelfSubject extends AdapterSimple {
	public static final int styleNormal = 1;
	public static final int styleCamera = 2;

	public int subjectImgWidth = 0;

	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mLayoutInflater;
	private BaseActivity mAct;

	public AdapterMySelfSubject(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mData = data;
		mLayoutInflater = LayoutInflater.from(parent.getContext());
		mAct = act;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		int style = Integer.parseInt(mData.get(position).get("style").toString());
		switch (style) {
			case styleNormal:
				return styleNormal;
			case styleCamera:
				return styleCamera;
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mData.get(position);
		ViewCache viewCache = null;

		int style = getItemViewType(position);
		if (convertView == null) {
			switch (style) {
				case styleNormal:
					viewCache = new ViewCache();
					convertView = mLayoutInflater.inflate(R.layout.list_item_myself_subject_main, parent, false);
					int[] viewId = new int[]{R.id.rela_left_content, R.id.tv_item_left_day, R.id.tv_item_left_mounth, R.id.tv_item_left_year,
							R.id.linear_right_content, R.id.tv_item_right_title, R.id.tv_item_right_content, R.id.linear_imgs,
							R.id.tv_item_right_time, R.id.tv_item_right_ping, R.id.tv_item_right_zan, R.id.tv_item_right_delete,
							R.id.linear_imgs_left, R.id.linear_imgs_right, R.id.iv_img_1, R.id.iv_img_2, R.id.iv_img_3, R.id.item_root_view,
							R.id.tv_item_right_isfine, R.id.rela_right_foot};
					viewCache.setView(convertView, viewId);
					videoImgId = R.id.iv_img_1;
					convertView.setTag(viewCache);
					break;
				case styleCamera:
					convertView = mLayoutInflater.inflate(R.layout.list_item_myself_subject_new_top, parent, false);
					ImageView closeImg = (ImageView) convertView.findViewById(R.id.my_subject_top_close);
					closeImg.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							mData.remove(0);
							AdapterMySelfSubject.this.notifyDataSetChanged();
						}
					});
					break;
			}
		} else {
			switch (style) {
				case styleNormal:
					viewCache = (ViewCache) convertView.getTag();
					break;
			}
		}
		switch (style) {
			case styleNormal:
				viewCache.setValue(map);
				break;
		}
		return convertView;
	}

	class ViewCache {
		final int viewUser = 0;// 访问用户
		final int viewZan = 1;// 点赞操作;
		final int viewPing = 2;// 点评操作;
		final int viewSub = 3;// 访问内容;
		final int viewDel = 4;// 删除操作;
		RelativeLayout rela_left_content;
		TextView tv_item_left_day;
		TextView tv_item_left_mounth;
		TextView tv_item_left_year;

		LinearLayout linear_right_content;
		TextView tv_item_right_isfine;
		TextViewShow tv_item_right_title;
		TextView tv_item_right_content;
		LinearLayout linear_imgs;
		TextView tv_item_right_time;
		TextView tv_item_right_ping;
		TextView tv_item_right_zan;
		TextView tv_item_right_delete;

		LinearLayout linear_imgs_left;
		ImageViewVideo iv1;
		ImageView iv2;
		LinearLayout linear_imgs_right;
		ImageView iv3;
		ImageView iv4;
		LinearLayout item_root_view;
		RelativeLayout rela_right_foot;

		public void setView(View view, int... param) {
			rela_left_content = (RelativeLayout) view.findViewById(param[0]);
			tv_item_left_day = (TextView) view.findViewById(param[1]);
			tv_item_left_mounth = (TextView) view.findViewById(param[2]);
			tv_item_left_year = (TextView) view.findViewById(param[3]);
			linear_right_content = (LinearLayout) view.findViewById(param[4]);
			tv_item_right_title = (TextViewShow) view.findViewById(param[5]);
			tv_item_right_content = (TextView) view.findViewById(param[6]);
			linear_imgs = (LinearLayout) view.findViewById(param[7]);
			tv_item_right_time = (TextView) view.findViewById(param[8]);
			tv_item_right_ping = (TextView) view.findViewById(param[9]);
			tv_item_right_zan = (TextView) view.findViewById(param[10]);
			tv_item_right_delete = (TextView) view.findViewById(param[11]);

			linear_imgs_left = (LinearLayout) view.findViewById(param[12]);
			linear_imgs_right = (LinearLayout) view.findViewById(param[13]);
			iv1 = (ImageViewVideo) linear_imgs_left.findViewById(param[14]);
			iv2 = (ImageView) linear_imgs_right.findViewById(param[15]);
			iv3 = (ImageView) linear_imgs_right.findViewById(param[16]);

			item_root_view = (LinearLayout) view.findViewById(param[17]);
			tv_item_right_isfine = (TextView) view.findViewById(param[18]);
			rela_right_foot = (RelativeLayout) view.findViewById(param[19]);
		}

		public void setValue(Map<String, String> map) {
			// itemRootView的显示和隐藏
			if (map.get("hide").equals("yes")) {
				item_root_view.setVisibility(View.GONE);
				// 隐藏的item直接返回;
				return;
			} else
				item_root_view.setVisibility(View.VISIBLE);

			rela_right_foot.getLayoutParams().width = subjectImgWidth;

			AdapterMySelfSubject.this.setViewText(tv_item_left_day, map.get("day"));
			parseLeftHide(rela_left_content, map.get("day"));
			AdapterMySelfSubject.this.setViewText(tv_item_left_mounth, map.get("month"));
			AdapterMySelfSubject.this.setViewText(tv_item_left_year, map.get("year"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_title, map.get("title"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_content, map.containsKey("content") ? map.get("content") : "");
			parseImgJson(linear_imgs, map.get("imgs"), map.get("hasVideo"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_time, map.get("timeShow"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_ping, map.get("commentNum"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_zan, map.get("likeNum"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_delete, map.get("delete"));
			AdapterMySelfSubject.this.setViewText(tv_item_right_isfine, map.get("isFine"));
			setClickEvent(map, tv_item_right_title, viewSub);
			setClickEvent(map, linear_imgs, viewSub);
			setClickEvent(map, tv_item_right_content, viewSub);
			setClickEvent(map, tv_item_right_delete, viewDel);
		}

		// 隐藏左侧相同日期
		public void parseLeftHide(RelativeLayout layout, String value) {
			if (value.equals("hide_day")) {
				layout.setVisibility(View.GONE);
			} else
				layout.setVisibility(View.VISIBLE);
		}

		/**
		 * 根据ImagView 的json值在父容器中创建更多的ImagView;
		 *
		 * @param layout
		 * @param value
		 * @param hasVideo
		 */
		public void parseImgJson(LinearLayout layout, final String value, String hasVideo) {
			ArrayList<Map<String, String>> imgsShow = UtilString.getListMapByJson(value);
			int imgCount = imgsShow.size() >= 3 ? 3 : imgsShow.size();
			if ("2".equals(hasVideo) && imgCount > 1) {
				imgCount = 1;
			}
			// 设置总容器图片大小;
			LayoutParams lp = new LayoutParams(subjectImgWidth, subjectImgWidth);
			layout.setLayoutParams(lp);
			// 图片恢复初始状态
			lp = new LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			lp.weight = 1;
			linear_imgs_left.setLayoutParams(lp);
			linear_imgs_right.setLayoutParams(lp);
			iv1.setPadding(0, 0, 0, 0);
			iv2.setPadding(0, 0, 0, 0);
			iv3.setPadding(0, 0, 0, 0);
			switch (imgCount) {
				case 0://无图片显示
					tv_item_right_title.setVisibility(View.VISIBLE);
					iv1.setVisibility(View.GONE);
					iv2.setVisibility(View.GONE);
					iv3.setVisibility(View.GONE);
					linear_imgs_left.setVisibility(View.GONE);
					linear_imgs_right.setVisibility(View.GONE);
					LayoutParams lps = new LayoutParams(0, 0);
					layout.setLayoutParams(lps);
					break;
				case 1:
					iv1.setVisibility(View.VISIBLE);
					iv2.setVisibility(View.GONE);
					iv3.setVisibility(View.GONE);
					linear_imgs_left.setVisibility(View.VISIBLE);
					linear_imgs_right.setVisibility(View.GONE);
					iv1.parseItemImg(imgsShow.get(0).get(""), hasVideo, true);
//				AdapterMySelfSubject.this.setViewImage(iv1, imgsShow.get(0).get(""));
					break;
				case 2:
					iv1.setVisibility(View.VISIBLE);
					iv2.setVisibility(View.VISIBLE);
					iv3.setVisibility(View.GONE);
					linear_imgs_left.setVisibility(View.VISIBLE);
					linear_imgs_right.setVisibility(View.VISIBLE);
					lp = new LayoutParams(subjectImgWidth, subjectImgWidth / 2);
					layout.setLayoutParams(lp);
					lp = new LayoutParams(subjectImgWidth / 2, subjectImgWidth / 2);
					linear_imgs_left.setLayoutParams(lp);
					linear_imgs_right.setLayoutParams(lp);
//				iv1.setPadding(0, 0, padding, 0);
					AdapterMySelfSubject.this.setViewImage(iv1, imgsShow.get(0).get(""));
					AdapterMySelfSubject.this.setViewImage(iv2, imgsShow.get(1).get(""));
					break;
				case 3:
					iv1.setVisibility(View.VISIBLE);
					iv2.setVisibility(View.VISIBLE);
					iv3.setVisibility(View.VISIBLE);
					linear_imgs_left.setVisibility(View.VISIBLE);
					linear_imgs_right.setVisibility(View.VISIBLE);
					lp = new LayoutParams(subjectImgWidth, subjectImgWidth / 3 * 2);
					layout.setLayoutParams(lp);
					lp = new LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
					lp.weight = 2;
					linear_imgs_left.setLayoutParams(lp);
//				iv1.setPadding(0, 0, padding, 0);
//				iv2.setPadding(0, 0, 0, padding);
					AdapterMySelfSubject.this.setViewImage(iv1, imgsShow.get(0).get(""));
					AdapterMySelfSubject.this.setViewImage(iv2, imgsShow.get(1).get(""));
					AdapterMySelfSubject.this.setViewImage(iv3, imgsShow.get(2).get(""));
					break;
			}
		}

		private void setClickEvent(final Map<String, String> map, final View view, final int type) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (type) {
						case viewUser:
							Intent intentUser = new Intent(mAct, FriendHome.class);
							Bundle bundle = new Bundle();
							bundle.putString("code", map.get("nickCode"));
							intentUser.putExtras(bundle);
							mAct.startActivity(intentUser);
							break;
						case viewSub:
							Intent intentSub = new Intent(mAct, ShowSubject.class);
							intentSub.putExtra("code", map.get("code"));
							intentSub.putExtra("title", map.get("title"));
							intentSub.putExtra("floorNum", map.get("num"));
							mAct.startActivity(intentSub);
							break;
						case viewDel:
							new AlertDialog.Builder(mAct).setIcon(android.R.drawable.ic_dialog_alert).setTitle("删除发布的内容")
									.setMessage("确定要删除该内容?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String params = "type=delFloor&subjectCode=" + map.get("code") + "&floorId=" + map.get("floorId")
											+ "&commentId=" + map.get("commentId");
									// 请求网络;
									ReqInternet.in().doPost(StringManager.api_quanSetSubject, params, new InternetCallback(mAct) {
										@Override
										public void loaded(int flag, String url, Object returnObj) {
											if (flag >= UtilInternet.REQ_OK_STRING) {
												Map<String, String> map = UtilString.getListMapByJson(returnObj.toString()).get(0);
												if (map.get("type").equals("2")) {
													map.put("hide", "yes");// 隐藏该条目;
												}
											}
										}
									});
									mData.remove(map);
									notifyDataSetChanged();
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}).create().show();
							break;
					}
				}
			});
		}
	}
}
