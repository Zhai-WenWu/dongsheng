package amodule.user.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.InternetCallback;

public class AdapterMyselfFavorite extends AdapterSimple {

	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mLayoutInflater;
	private BaseActivity mAct;
	public int textMaxWidth = 0; // textview的最大宽度

	public AdapterMyselfFavorite(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mData = data;
		mLayoutInflater = LayoutInflater.from(parent.getContext());
		mAct = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String, String> map = (Map<String, String>) mData.get(position);
		ViewCache viewCache = null;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = mLayoutInflater.inflate(R.layout.a_my_item_myself_favourite, null);
			int[] viewId = new int[] { R.id.myself_favourite_img, R.id.myself_favourite_dishName, R.id.myself_favourite_make,
					R.id.imyself_favourite_isFine, R.id.myself_favourite_burden, R.id.myself_favourite_allClick,
					R.id.myself_favourite_favorites, R.id.myself_favourite_delete,R.id.myself_favourite_hasVideo,
				R.id.tag_exclusive_layout,R.id.myself_favourite_catch};
			viewCache.setView(convertView, viewId);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map);
		return convertView;
	}
	
	@Override
	public void setViewText(TextView v, String text) {
		super.setViewText(v, text);
		if (textMaxWidth > 0 && v.getId() != R.id.myself_favourite_burden) {
			v.setMaxWidth(textMaxWidth);
		}
	}

	class ViewCache {
		final int viewDel = 4;// 删除操作;
		ImageView localCatch;
		ImageViewVideo iv_img;
		RelativeLayout tag_exclusive_layout;
		TextView tv_dishName, tv_make, tv_burdens, tv_delete, tv_isFine, tv_allClick, tv_favorites,tv_hasVideo;

		public void setView(View view, int... param) {
			iv_img = (ImageViewVideo) view.findViewById(param[0]);
			tv_dishName = (TextView) view.findViewById(param[1]);
			tv_make = (TextView) view.findViewById(param[2]);
			tv_isFine = (TextView) view.findViewById(param[3]);
			tv_burdens = (TextView) view.findViewById(param[4]);
			tv_allClick = (TextView) view.findViewById(param[5]);
			tv_favorites = (TextView) view.findViewById(param[6]);
			tv_delete = (TextView) view.findViewById(param[7]);
			tv_hasVideo = (TextView) view.findViewById(param[8]);
			tag_exclusive_layout= (RelativeLayout) view.findViewById(param[9]);
			localCatch= (ImageView) view.findViewById(param[10]);
			int textMaxWidth = ToolsDevice.getWindowPx(view.getContext()).widthPixels
					- ToolsDevice.dp2px(view.getContext(), 2 * 15 + 120 + 4 + 15 + 15);
			tv_dishName.setMaxWidth(textMaxWidth);
		}

		public void setValue(Map<String, String> map) {
			// img,dishName, make,isFine,burden,time,delete
//			AdapterMyselfFavorite.this.setViewImage(iv_img, map.get("img").length() == 0 ? R.drawable.i_nopic + "" : map.get("img"));
			iv_img.playImgWH = Tools.getDimen(mAct, R.dimen.dp_34);
			iv_img.parseItemImg(map.get("img"), map.get("hasVideo"), false);
			
			AdapterMyselfFavorite.this.setViewText(tv_dishName, map.get("name"));
//			AdapterMyselfFavorite.this.setViewText(tv_make, map.get("isMakeImg"));
			AdapterMyselfFavorite.this.setViewText(tv_isFine, map.get("isFine"));
			AdapterMyselfFavorite.this.setViewText(tv_burdens, map.get("burdens"));
			AdapterMyselfFavorite.this.setViewText(tv_allClick, map.get("allClick"));
			AdapterMyselfFavorite.this.setViewText(tv_favorites, map.get("favorites"));

			tag_exclusive_layout.setVisibility("2".equals(map.get("exclusive"))?View.VISIBLE:View.GONE);
			localCatch.setVisibility("2".equals(map.get("isLocal"))?View.VISIBLE:View.GONE);
//			AdapterMyselfFavorite.this.setViewText(tv_hasVideo, map.get("video"));
//			tv_delete.setVisibility(View.GONE);
//			setClickEvent(map, tv_delete, viewDel);
		}

		private void setClickEvent(final Map<String, String> map, final View view, final int type) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					switch (type) {
					case viewDel:
						new AlertDialog.Builder(mAct).setIcon(android.R.drawable.ic_dialog_alert).setTitle("取消收藏").setMessage("确定要取消收藏?")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										doFavorite(map);
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

		// 收藏响应
		private void doFavorite(final Map<String, String> map) {
			AppCommon.onFavoriteClick(mAct,"favorites", map.get("code"), new InternetCallback(mAct) {
				@Override
				public void loaded(int flag, String url, Object returnObj) {

				}
			});
		}
	}
}
