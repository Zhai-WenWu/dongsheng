package amodule.dish.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.VideoDish;
import amodule.user.activity.FriendHome;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

public class AdapterVideoDish extends AdapterSimple{

	private Context mCon;
	private List<? extends Map<String, ?>> mData;
	
	public AdapterVideoDish(Context con,View parent, List<? extends Map<String, ?>> data,int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		mCon = con;
		mData = data;
	}

	@Override
	public void setViewImage(ImageView v, String value) {
		if(v.getId() != R.id.item_model_video && v.getId() != R.id.iv_userType)
			super.setViewImage(v, value);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Map<String,String> map = (Map<String, String>) mData.get(position);
		View view = super.getView(position, convertView, parent);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				XHClick.mapStat(mCon, VideoDish.STATISTICS_ID ,"视频内容击量 ", String.valueOf(position + 1));
				Intent intent = new Intent(mCon, DetailDish.class);
				intent.putExtra("code", map.get("code"));
				intent.putExtra("img", map.get("img"));
				intent.putExtra("name", map.get("name"));
				mCon.startActivity(intent);
			}
		});
		view.findViewById(R.id.user_head_img_rela).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				userClick(map.get("userCode"));
			}
		});
		view.findViewById(R.id.user_name).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				userClick(map.get("userCode"));
			}
		});
		ImageViewVideo ivv = (ImageViewVideo) view.findViewById(R.id.item_model_video);
		ivv.parseItemImg(map.get("img"), map.get("hasVideo"), isAnimate);
		ivv.playImgWH= Tools.getDimen(mCon, R.dimen.dp_41);
		if(map.containsKey("isGourmet") && !"hide".equals("isGourmet")){
			ImageView ivUserType = (ImageView)view.findViewById(R.id.iv_userType);
			AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), ivUserType);
		}
		return view;
	}
	
	private void userClick(String userCode){
		XHClick.mapStat(mCon, VideoDish.STATISTICS_ID ,"用户头像点击 ", "");
		Intent intent = new Intent(mCon, FriendHome.class);
		intent.putExtra("code",userCode);
		mCon.startActivity(intent);
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
					// 图片圆角和宽高适应auther_userImg
					if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg ) {
						v.setScaleType(ScaleType.CENTER_CROP);
						bitmap = UtilImage.toRoundCorner(v.getResources(),bitmap, 1, ToolsDevice.dp2px(mParent.getContext(), 500));
						v.setImageBitmap(bitmap);
					} else {
						v.setScaleType(scaleType);
						UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
						if (isAnimate) {
//							AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//							alphaAnimation.setDuration(300);
//							v.setAnimation(alphaAnimation);
						}
					}
				}
			}
		};
	}

}
