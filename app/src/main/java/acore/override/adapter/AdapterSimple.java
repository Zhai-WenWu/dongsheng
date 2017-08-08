package acore.override.adapter;

import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

public class AdapterSimple extends SimpleAdapter {
	public int imgResource = R.drawable.i_nopic;
	public int roundImgPixels = 0, imgWidth = 0, imgHeight = 0,// 以像素为单位
			roundType = 1; // 1为全圆角，2上半部分圆角
	public boolean imgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
	public String imgLevel = FileManager.save_cache; // 图片保存等级
	public ScaleType scaleType = ScaleType.CENTER_CROP;
	public int viewWidth = 0; // viewWidth的最小宽度
	public int viewHeight = 0; // viewHeight的最小宽度
	public boolean isAnimate = false;//控制图片渐渐显示
	public View mParent;

	public String urlKey = "img", hasVideoKey = "hasVideo";
	public int videoImgId = R.id.iv_video_img;
	public static final int TAG_ID = R.string.tag;
	public int playImgWH = 41;

	private List<? extends Map<String, ?>> mData;

	public AdapterSimple(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent.getContext(), data, resource, from, to);
		mParent = parent;
		mData = data;
		playImgWH = Tools.getDimen(mParent.getContext(), R.dimen.dp_41);
	}

	@Override
	public void setViewImage(final ImageView v, String value) {
		if(value == null)return;
		v.setVisibility(View.VISIBLE);
		// 异步请求网络图片
		if (value.indexOf("http") == 0) {
			if (value.length() < 10)
				return;
			v.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
			v.setScaleType(ScaleType.CENTER_CROP);
			v.setTag(TAG_ID, value);
			if(mParent.getContext()==null)return;
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
					.load(value)
					.setImageRound(roundImgPixels)
					.setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
					.setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
					.setSaveType(imgLevel)
					.build();
			if (bitmapRequest != null) {
				bitmapRequest.into(getTarget(v, value));
			}
		}
		// 直接设置为内部图片
		else if (value.indexOf("ico") == 0) {
			InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
			Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
			bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
			UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
		}
		// 隐藏
		else if (value.equals("hide") || value.length() == 0)
			v.setVisibility(View.GONE);
			// 直接加载本地图片
		else if (!value.equals("ignore")) {
			if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
				return;
			v.setTag(TAG_ID, value);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
					.load(value)
					.setImageRound(roundImgPixels)
					.setSaveType(imgLevel)
					.build();
			if (bitmapRequest != null) {
				bitmapRequest.placeholder(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
						.error(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
						.into(getTarget(v, value));
			}
		}
		// 如果为ignore,则忽略图片
	}

	public SubBitmapTarget getTarget(final ImageView v, final String url) {
		return new SubBitmapTarget() {
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = null;
				if (v.getTag(TAG_ID).equals(url))
					img = v;
				if (img != null && bitmap != null) {
					// 图片圆角和宽高适应 
					v.setScaleType(scaleType);

					UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
					if (isAnimate) {
//						AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//						alphaAnimation.setDuration(300);
//						v.setAnimation(alphaAnimation);
					}
				}
			}
		};
	}

	@Override
	public void setViewImage(ImageView v, int value) {
		setViewImage(v, value + "");
	}

	@Override
	public void setViewText(TextView v, String text) {
		if (text == null || text.length() == 0 || text.equals("hide"))
			v.setVisibility(View.GONE);
		else {
			v.setVisibility(View.VISIBLE);
			v.setText(text.trim());
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try{if(position>=mData.size())return null;
		}catch (Exception e){ e.printStackTrace();}
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mData.get(position);
		if (viewWidth > 0 || viewHeight > 0) {
			View view = super.getView(position, convertView, parent);
			ViewGroup.LayoutParams lp = view.getLayoutParams();
			if (viewWidth > 0)
				lp.width = viewWidth;
			if (viewHeight > 0)
				lp.height = viewHeight;
			// lp.setMargins(Tools.dp2px(mParent.getContext(), 2.5f), 0,
			// Tools.dp2px(mParent.getContext(), 2.5f), 0);
			view.setLayoutParams(lp);
			return view;
		}
		if (map.containsKey(urlKey) && map.containsKey(hasVideoKey)) {
			View view = super.getView(position, convertView, parent);
			ImageViewVideo ivv = (ImageViewVideo) view.findViewById(videoImgId);
			if (ivv != null) {
				ivv.playImgWH = playImgWH;
				ivv.parseItemImg(scaleType, map.get(urlKey), map.get(hasVideoKey), true, imgResource, imgLevel);
			}
			return view;
		}
		return super.getView(position, convertView, parent);
	}

	public ViewGroup getParent() {
		return (ViewGroup) mParent;
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (observer != null) {
			super.unregisterDataSetObserver(observer);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		if (isAnimate) {
			isAnimate = false;
			super.notifyDataSetChanged();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					isAnimate = true;
				}
			}, 100);
		} else
			super.notifyDataSetChanged();
	}
}
