/**
 * 
 * @author intBird 20140227.
 * 
 */
package amodule.user.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import amodule.dish.db.UploadDishSqlite;

public class AdapterMyselfDraft extends AdapterSimple {
	private ArrayList<Map<String, String>> mData;
	private Context mContext;

	public AdapterMyselfDraft(Context context, View parent,List<? extends Map<String, ?>> data, int resource, String[] from,int[] to) {
		super(parent, data, resource, from, to);
		mData = (ArrayList<Map<String, String>>) data;
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);
		ImageView iv_delete = (ImageView)convertView.findViewById(R.id.draft_item_dele);
		iv_delete.setVisibility(View.VISIBLE);
		setClickEvent(mData.get(position), iv_delete);
		return convertView;
	}
	
	private void setClickEvent(final Map<String, String> mData2,final View view) {
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				new AlertDialog.Builder(mContext)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("删除草稿")
						.setMessage("确定要删除选中草稿?")
						.setPositiveButton("确定",new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int which) {
									XHClick.onEventValue(mContext, "dishOperate", "dishOperate", "删除", 1);
									if (new UploadDishSqlite(mContext.getApplicationContext()).deleteById(Integer.parseInt(mData2.get("id")))) {
										Tools.showToast(mContext, "已成功删除");
										mData.remove(mData2);
										notifyDataSetChanged();
									} else {
										Tools.showToast(mContext, "删除失败!");
									}
								}
							})
						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
								}
							}).create().show();
			}
		});
	}

//	class ViewCache {
//
//		final int viewDel = 4;// 删除操作;
//		final int viewModify = 5;// 删除操作;
//
//		ImageView iv_delete;
//		TextView tv_name;
//		ImageView iv_img;
//		TextView tv_ingre;
//		TextView tv_addTime;
//		
////		{R.id.draft_item_dele,R.id.draft_tv_name,
////		R.id.draft_item_img,R.id.draft_tv_ingre, 
////		R.id.draft_tv_addTime};
//
//		public void setView(View view, int... param) {
//			iv_delete = (ImageView) view.findViewById(param[0]);
//			tv_name = (TextView) view.findViewById(param[1]);
//			iv_img = (ImageView) view.findViewById(param[2]);
//			tv_ingre = (TextView) view.findViewById(param[3]);
//			tv_addTime = (TextView) view.findViewById(param[4]);
//		}
//
//		public void setValue(Map<String, String> map) {
//			iv_delete.setVisibility(View.VISIBLE);
//			setClickEvent(map, iv_delete, viewDel);
//			AdapterMyselfDraft.this.setViewText(tv_name, map.get("name"));
//			AdapterMyselfDraft.this.setViewImage(iv_img, (!map.containsKey("img") || map.get("img")== null || "".equals(map.get("img"))) ? R.drawable.i_nopic + "" : map.get("img"));
////			AdapterMyselfDraft.this.setViewText(tv_ingre,getFood(map.get("food")));
//			AdapterMyselfDraft.this.setViewText(tv_addTime, map.get("addTime"));
//		}
//		
//		private String getFood(String count){
//			ArrayList<Map<String, String>> list = UtilString.getListMapByJson(count);
//			String food = "";
//			for (int i = 0; i < list.size(); i++) {
//				Map<String, String> map = list.get(i);
//				food = food + map.get("name") + " ";
//			}
//			return food;
//		}
//
//		private void setClickEvent(final Map<String, String> map,final View view, final int type) {
//			view.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(final View v) {
//					switch (type) {
//					case viewDel:
//						new AlertDialog.Builder(mContext)
//								.setIcon(android.R.drawable.ic_dialog_alert)
//								.setTitle("删除草稿")
//								.setMessage("确定要删除选中草稿?")
//								.setPositiveButton("确定",new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog,int which) {
//											XHClick.onEventValue(mContext, "dishOperate", "dishOperate", "删除", 1);
//											if (new UploadDishSqlite(mContext.getApplicationContext()).deleteById(Integer.parseInt(map.get("id")))) {
//												Tools.showToast(mContext, "已成功删除");
//												mData.remove(map);
//												notifyDataSetChanged();
//											} else {
//												Tools.showToast(mContext, "删除失败!");
//											}
//										}
//									})
//								.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(
//												DialogInterface dialog,
//												int which) {
//											return;
//										}
//									}).create().show();
//						break;
//					case viewModify:
//						XHClick.onEventValue(mContext, "dishOperate", "dishOperate", "修改草稿", 1);
//						Intent intent = new Intent();
//						intent.setClass(mContext, UploadDishActivity.class);
//						intent.putExtra("id", map.get("id"));
//						intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_DRAFT);
//						mContext.startActivity(intent);
//						break;
//					}
//				}
//			});
//		}
//	}
}