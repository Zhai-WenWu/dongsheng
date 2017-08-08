/**
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.user.adapter;

import android.content.Intent;
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
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import xh.windowview.XhDialog;

public class AdapterMyDish extends AdapterSimple {

	private List<? extends Map<String, ?>> mData;
	private LayoutInflater mLayoutInflater;
	private BaseActivity mAct;

	public AdapterMyDish(BaseActivity act, View parent,List<? extends Map<String, ?>> data, int resource, String[] from,int[] to) {
		super(parent, data, resource, from, to);
		mData = data;
		mLayoutInflater = LayoutInflater.from(parent.getContext());
		mAct = act;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getItemViewType(int position) {
		int style = Integer.parseInt(mData.get(position).get("style").toString());
		switch (style) {
		case styleCamera:
			return styleCamera;
		case styleNormal:
			return styleNormal;
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mData.get(position);
		ViewCache viewCache = null;
		ViewCamera viewCamera = null;
		int style = getItemViewType(position);
		if (convertView == null) {
			switch (style) {
			case styleCamera:
				viewCamera = new ViewCamera();
				convertView = mLayoutInflater.inflate(R.layout.list_item_myself_dish_top, parent, false);
				int[] ids = new int[] { R.id.linear_root ,R.id.tv_integralInfo};
				viewCamera.setView(convertView, ids);
				convertView.setTag(viewCamera);
				break;
			case styleNormal:
				viewCache = new ViewCache();
				convertView = mLayoutInflater.inflate(R.layout.a_dish_my_list_item, parent, false);
				int[] viewId = new int[] {R.id.itemImg1, R.id.tv_itemDishName,
						R.id.tv_item_make, R.id.iv_itemIsFine, R.id.tv_itemBurden,
						R.id.allclick,R.id.tv_collect, R.id.tv_delete, R.id.tv_isUp
						,R.id.tv_item_hasVideo,R.id.iv_itemIsGood,R.id.dish_delete_iv};
				viewCache.setView(convertView, viewId);
				convertView.setTag(viewCache);
				break;
			}
		} else {
			switch (style) {
			case styleNormal:
				viewCache = (ViewCache) convertView.getTag();
				break;
			case styleCamera:
				viewCamera = (ViewCamera) convertView.getTag();
				break;
			}
		}
		switch (style) {
		case styleNormal:
			viewCache.setValue(map);
			break;
		case styleCamera:
			viewCamera.setValue();
			break;
		}
		return convertView;
	}

	class ViewCamera {
		LinearLayout fl_root; 
		TextView tv_integralInfo;

		private void setView(View view, int... id) {
			fl_root = (LinearLayout) view.findViewById(id[0]);
			tv_integralInfo = (TextView) view.findViewById(id[1]);
			setEvent();
		}

		private void setValue() {
			setEvent();
		}

		private void setEvent() {
			fl_root.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					XHClick.onEventValue(mAct, "uploadDish", "uploadDish", "从个人发", 1);
					Intent intent = new Intent();
					intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_NEW);
					intent.setClass(mAct, UploadDishActivity.class);
					mAct.startActivity(intent);
				}
			});
			tv_integralInfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = StringManager.api_integralInfo + "?code="+ LoginManager.userInfo.get("code");
					AppCommon.openUrl(mAct, url, true);
				}
			});
		}
	}

	class ViewCache {
		ImageViewVideo iv_mydish;
		TextView tv_title;
		TextView tv_makeImg;
		TextView tv_isFine;
		TextView tv_burden;
		TextView tv_look;
		TextView tv_collect;
		TextView tv_bianji;
		TextView tv_isUp;
		TextView tv_hasVideo;
		TextView tv_isGood;
		ImageView iv_delete;

		public void setView(View view, int... param) {
//			R.id.tv_look,R.id.tv_collect
			iv_mydish = (ImageViewVideo) view.findViewById(param[0]);
			tv_title = (TextView) view.findViewById(param[1]);
			tv_makeImg = (TextView) view.findViewById(param[2]);
			tv_isFine = (TextView) view.findViewById(param[3]);
			tv_burden = (TextView) view.findViewById(param[4]);
			tv_look = (TextView) view.findViewById(param[5]);
			tv_collect = (TextView) view.findViewById(param[6]);
			tv_bianji = (TextView) view.findViewById(param[7]);
			tv_isUp = (TextView) view.findViewById(param[8]);
			tv_hasVideo = (TextView) view.findViewById(param[9]);
			tv_isGood = (TextView) view.findViewById(param[10]);
			iv_delete = (ImageView) view.findViewById(param[11]);
		}
 
		public void setValue(final Map<String, String> map) {			
//			AdapterMyDish.this.setViewImage(iv_mydish,map.get("img").length() == 0 ? R.drawable.i_nopic + "": map.get("img"));
			iv_mydish.playImgWH = Tools.getDimen(mAct, R.dimen.dp_25);
			iv_mydish.parseItemImg(map.get("img"), map.get("hasVideo"), false);
			
			AdapterMyDish.this.setViewText(tv_title, map.get("name"));
//			AdapterMyDish.this.setViewText(tv_makeImg,map.get("isMakeImg").equals("2")?"步骤图":"");
			AdapterMyDish.this.setViewText(tv_isFine,map.get("level").equals("3")?"精":"");
			AdapterMyDish.this.setViewText(tv_isGood,map.get("level").equals("2")?"优":"");
			AdapterMyDish.this.setViewText(tv_isUp,map.get("isUp"));
			AdapterMyDish.this.setViewText(tv_burden, map.get("burdens"));
			AdapterMyDish.this.setViewText(tv_look, map.get("allClick"));
			AdapterMyDish.this.setViewText(tv_collect, map.get("favorites"));
//			AdapterMyDish.this.setViewText(tv_hasVideo, map.get("video"));
			String state = getState(map);
			AdapterMyDish.this.setViewText(tv_bianji, state);
			if("审核失败".equals(state)){
				iv_delete.setVisibility(View.VISIBLE);
				iv_delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final XhDialog dialog = new XhDialog(mAct);
						dialog.setTitle("真的要删除这个菜谱么?").
						setSureButton("删除", new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								onDeleteClick(v,map.get("code"));
								dialog.cancel();
							}
						}).setCanselButton("取消", new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								dialog.cancel();
							}
						});
						dialog.show();
					}
				});
			}else{
				iv_delete.setVisibility(View.GONE);
				iv_delete.setOnClickListener(null);
			}
		}
		
		private String getState(final Map<String, String> map){
			if(map.get("draft").equals(UploadDishData.UPLOAD_ING) || map.get("draft").equals(UploadDishData.UPLOAD_FAIL)){
				return map.get("draft");
			}
			String state = map.get("state");
			int number = Integer.parseInt(state);
			switch(number){
				case 2: state = "审核中"; break;
				case 5: state = "提交审核"; break;
				case 6: state = "hide"; break;//审核通过 
				case 7: state = "审核失败"; break;
			}
			return state;
		}
	}
	
	public void onDeleteClick(View v,String dishCode){
		
	}
	
	//样式
	public static final int styleNormal = 1;
	public static final int styleCamera = 2;
}
