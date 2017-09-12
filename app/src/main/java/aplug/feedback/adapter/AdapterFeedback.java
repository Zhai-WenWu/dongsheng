package aplug.feedback.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.activity.FriendHome;
import aplug.basic.SubBitmapTarget;
import aplug.feedback.activity.Feedback;
import aplug.feedback.activity.ShowImage;
import aplug.imageselector.ShowImageActivity;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilLog;

import static aplug.feedback.activity.Feedback.DEFAULT_CONTENT;

@SuppressLint("InflateParams")
public class AdapterFeedback extends AdapterSimple {
	private Feedback mAct;
	private List<Map<String, String>> mData;

	@SuppressWarnings("unchecked")
	public AdapterFeedback(Feedback feekback, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mData = (List<Map<String, String>>) data;
		mParent = parent;
		mAct = feekback;
	}

	@SuppressLint("RtlHardcoded")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mAct).inflate(R.layout.a_xh_item_feedback, null);
			viewHolder.feekback_reply_date_layout = (RelativeLayout) convertView.findViewById(R.id.feekback_reply_date_layout);
			viewHolder.feekback_reply_date = (TextView) convertView.findViewById(R.id.feekback_reply_date);
			viewHolder.feekback_user_layout = (RelativeLayout) convertView.findViewById(R.id.feekback_user_layout);
			viewHolder.feekback_user_ico = (ImageView) convertView.findViewById(R.id.feekback_user_ico);
			viewHolder.feekback_user_reply_content = (TextView) convertView.findViewById(R.id.feekback_user_reply_content);
			viewHolder.feekback_user_send_img = (ImageView) convertView.findViewById(R.id.feekback_user_send_img);
			viewHolder.feekback_progress_img = (ImageView) convertView.findViewById(R.id.feekback_progress_img);
			viewHolder.feekback_progress_text = (ImageView) convertView.findViewById(R.id.feekback_progress_text);
			
			viewHolder.feekback_admin_layout = (RelativeLayout) convertView.findViewById(R.id.feekback_admin_layout);
			viewHolder.feekback_admin_reply = (RelativeLayout) convertView.findViewById(R.id.feekback_admin_reply);
			viewHolder.feekback_admin_reply_activity = (RelativeLayout) convertView.findViewById(R.id.feekback_admin_reply_activity);
			viewHolder.feekback_admin_reply_title = (TextView) convertView.findViewById(R.id.feekback_admin_reply_title);
			viewHolder.feekback_admin_reply_content = (TextView) convertView.findViewById(R.id.feekback_admin_reply_content);
			viewHolder.feekback_admin_reply_img = (ImageView) convertView.findViewById(R.id.feekback_admin_reply_img);
			viewHolder.feekback_admin_activity_title = (TextView) convertView.findViewById(R.id.feekback_admin_activity_title);
			viewHolder.feekback_admin_activity_content = (TextView) convertView.findViewById(R.id.feekback_admin_activity_content);
			viewHolder.feekback_admin_activity_img = (ImageView) convertView.findViewById(R.id.feekback_admin_activity_img);
			convertView.setTag(viewHolder);
		} else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//用户头像设置
		setViewImage(viewHolder.feekback_user_ico, LoginManager.userInfo.get("img") != null ? LoginManager.userInfo.get("img") : "hide");
		viewHolder.feekback_user_ico.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(LoginManager.userInfo.get("code") != null){
					Intent intent = new Intent(mAct, FriendHome.class);
					Bundle bundle = new Bundle();
					bundle.putString("code", LoginManager.userInfo.get("code"));
					intent.putExtras(bundle);
					mAct.startActivity(intent);
				}
			}
		});
		
		final Map<String, String> map = mData.get(position);
		String current = map.get("addTime");
		String lastTime = null;
		if (position > 0) {
			lastTime = mData.get(position - 1).get("addTime");
		}
		String time = mData.get(position).get("timeShow");
		try {
			time = Tools.dealTime(lastTime, current, "yyyy-MM-dd HH:mm:ss", time);
		} catch (ParseException e) {
			UtilLog.reportError("反馈addTime解析异常", e);
		}
		if (!time.equals("hide")) {
			viewHolder.feekback_reply_date_layout.setVisibility(View.VISIBLE);
		} else {
			viewHolder.feekback_reply_date_layout.setVisibility(View.GONE);
		}
		setViewText(viewHolder.feekback_reply_date, time);
		String author = map.get("author");
		if (author.equals("1")) {
			RelativeLayout view;
			if (map.get("type").equals("1")) {
				setViewText(viewHolder.feekback_admin_reply_title, map.get("title"));
				setViewText(viewHolder.feekback_admin_reply_content, map.get("content"));
				//设置长按复制
				setCopyListener(viewHolder.feekback_admin_reply_content, map.get("content"));
				if (map.containsKey("img") && !map.get("img").equals("hide") && !TextUtils.isEmpty(map.get("img"))){
					setViewImage(viewHolder.feekback_admin_reply_img, map.get("img"));
					viewHolder.feekback_admin_reply_img.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							showBigImg(map.get("img"));
						}
					});
				}else{
					viewHolder.feekback_admin_reply_img.setVisibility(View.GONE);
				}
				if(map.get("content").equals(DEFAULT_CONTENT)){
					viewHolder.feekback_admin_layout.setPadding(viewHolder.feekback_admin_layout.getPaddingLeft(), 
																										Tools.getDimen(mAct, R.dimen.dp_10), 
																										viewHolder.feekback_admin_layout.getPaddingRight(), 
																										0);
				}
				if (viewHolder.feekback_admin_reply_content.getLineCount() == 1)
					viewHolder.feekback_admin_reply_content.setGravity(Gravity.CENTER_VERTICAL);
				else
					viewHolder.feekback_admin_reply_content.setGravity(Gravity.LEFT);
//				if(map.containsKey("url") && !map.get("url").equals("")){
//					viewHolder.feekback_admin_reply_content.setTextIsSelectable(false);
//				}
				viewHolder.feekback_admin_reply_activity.setVisibility(View.GONE);
				viewHolder.feekback_admin_reply.setVisibility(View.VISIBLE);
				view = viewHolder.feekback_admin_reply;
			} else {
				setViewText(viewHolder.feekback_admin_activity_title, map.get("title"));
				setViewText(viewHolder.feekback_admin_activity_content, map.get("content"));
				setViewImage(viewHolder.feekback_admin_activity_img, map.get("img"));
				viewHolder.feekback_admin_reply_activity.setVisibility(View.VISIBLE);
				viewHolder.feekback_admin_reply.setVisibility(View.GONE);
				view = viewHolder.feekback_admin_reply_activity;
			}
			if(map.containsKey("url") && !map.get("url").equals("")){
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(map.get("type").equals("3") ){
							try{
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setData(Uri.parse(map.get("url")));
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
								mAct.startActivity(intent);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else AppCommon.openUrl(mAct, map.get("url"), true);
					}
				});
			}
			viewHolder.feekback_admin_layout.setVisibility(View.VISIBLE);
//			viewHolder.feekback_admin_layout.getLayoutParams().width = Tools.getWindowPx(mAct).widthPixels / 3 * 2;
			viewHolder.feekback_user_layout.setVisibility(View.GONE);
		} else if (author.equals("2")) {
			viewHolder.feekback_user_layout.setVisibility(View.VISIBLE);
			viewHolder.feekback_admin_layout.setVisibility(View.GONE);
			setViewText(viewHolder.feekback_user_reply_content, map.get("content"));
			//设置长按复制
			setCopyListener(viewHolder.feekback_user_reply_content, map.get("content"));
			if (viewHolder.feekback_user_reply_content.getLineCount() == 1)
				viewHolder.feekback_user_reply_content.setGravity(Gravity.CENTER_VERTICAL);
			else
				viewHolder.feekback_user_reply_content.setGravity(Gravity.LEFT);
			if (map.get("progress_text").equals("start")) {
				viewHolder.feekback_progress_img.setVisibility(View.GONE);
				viewHolder.feekback_progress_text.setVisibility(View.VISIBLE);
				Animation anim = AnimationUtils.loadAnimation(mAct, R.anim.feekback_progress_anim);
				viewHolder.feekback_progress_text.startAnimation(anim);
			} else {
				viewHolder.feekback_progress_img.setVisibility(View.GONE);
				viewHolder.feekback_progress_text.setVisibility(View.GONE);
				viewHolder.feekback_progress_text.clearAnimation();
			}
			if (map.containsKey("img") && !map.get("img").equals("hide")) {
				if (map.get("img").indexOf("http") == 0) {
					setViewImage(viewHolder.feekback_user_send_img, map.get("img"));
				} else {
					viewHolder.feekback_user_send_img.setVisibility(View.VISIBLE);
					Bitmap bmp = UtilImage.imgPathToBitmap(map.get("img"),  ToolsDevice.getWindowPx(mAct).widthPixels/4, 0, false, null);
					//因为刷新是大图有bug，所以注释
//					if(map.get("once") != null && map.get("once").equals("0")){
//						map.put("once", "1");
//						bmp = ToolsImage.imgPathToBitmap(map.get("img"),  ToolsDevice.getWindowPx(mAct).widthPixels/4, 0, false);
//					}
//					else
//						bmp = ToolsImage.imgPathToBitmap(map.get("img"),  0, 0, false);
					bmp = UtilImage.toRoundCorner(viewHolder.feekback_user_send_img.getResources(), bmp, 1, 10);
					viewHolder.feekback_user_send_img.setScaleType(scaleType);
					viewHolder.feekback_user_send_img.setImageBitmap(bmp);
				}
				if (map.get("progress_img").equals("start")) {
					viewHolder.feekback_progress_img.setVisibility(View.VISIBLE);
					Animation anim = AnimationUtils.loadAnimation(mAct, R.anim.feekback_progress_anim);
					viewHolder.feekback_progress_img.startAnimation(anim);
				} else {
					viewHolder.feekback_progress_img.setVisibility(View.GONE);
					viewHolder.feekback_progress_img.clearAnimation();
				}
			} else {
				viewHolder.feekback_user_send_img.setVisibility(View.GONE);
			}
		}
		viewHolder.feekback_user_send_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("url", map.get("img"));
				intent.setClass(mAct, ShowImageActivity.class);
				mAct.startActivity(intent);
			}
		});
		return convertView;
	}

	private void setCopyListener(View view,final String content){
		if(!TextUtils.isEmpty(content)){
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Tools.inputToClipboard(mAct,content);
					Tools.showToast(mAct,"复制成功");
					return true;
				}
			});
		}
	}

	private void showBigImg(String img){
		Intent intent = new Intent(mAct, ShowImage.class);
		intent.putExtra("img", img);
		mAct.startActivity(intent);
	}

	static class ViewHolder {
		RelativeLayout feekback_reply_date_layout;
		TextView feekback_reply_date;
		RelativeLayout feekback_admin_reply;
		TextView feekback_admin_reply_title;
		ImageView feekback_admin_reply_img;
		TextView feekback_admin_reply_content;
		RelativeLayout feekback_user_layout;
		RelativeLayout feekback_admin_layout;
		TextView feekback_user_reply_content;
		ImageView feekback_user_send_img;
		ImageView feekback_progress_img;
		ImageView feekback_progress_text;
		RelativeLayout feekback_admin_reply_activity;
		TextView feekback_admin_activity_title;
		TextView feekback_admin_activity_content;
		ImageView feekback_admin_activity_img;
		ImageView feekback_user_ico;
	}

	@Override
	public SubBitmapTarget getTarget(final ImageView v, final String url) {
		return new SubBitmapTarget(){
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = null;
				if (v.getTag(TAG_ID).equals(url))
					img = v;
				if (img != null && bitmap != null) {
					// 图片圆角和宽高适应
					v.setScaleType(scaleType);
					if (v.getId() == R.id.feekback_user_ico) {
						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, 500);
						v.setImageBitmap(bitmap);
					} else if (v.getId() == R.id.feekback_admin_activity_img) {
						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, 10);
						int width = Tools.getDimen(mAct, R.dimen.dp_50);
						UtilImage.setImgViewByWH(v, bitmap, width, width, false);
					} else {
						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, 10);
						int width = bitmap.getWidth() * ToolsDevice.getWindowPx(mAct).heightPixels / 5 / bitmap.getHeight();
						int height = ToolsDevice.getWindowPx(mAct).heightPixels / 5;
						UtilImage.setImgViewByWH(v, bitmap, width, height, true);
					}
				}
			}};
	}

}