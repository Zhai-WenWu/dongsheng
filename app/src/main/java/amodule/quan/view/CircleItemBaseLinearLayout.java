package amodule.quan.view;

import java.io.InputStream;

import xh.basic.tool.UtilImage;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import aplug.basic.SubBitmapTarget;
import aplug.basic.LoadImage;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

public class CircleItemBaseLinearLayout extends LinearLayout {

	public final int TAG_ID = R.string.tag;
	public int imgResource = R.drawable.mall_recommed_product_backgroup;
	public int roundImgPixels = 0, imgWidth = 0, imgHeight = 0,// 以像素为单位
			roundType = 1; // 1为全圆角，2上半部分圆角
	public boolean imgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
	public String imgLevel = FileManager.save_cache; // 图片保存等级
	public ScaleType scaleType = ScaleType.CENTER_CROP;
	public boolean isAnimate = false;// 控制图片渐渐显示
	private Context context;

	public CircleItemBaseLinearLayout(Context context) {
		super(context);
		this.context = context;
	}

	public void setViewImage(final ImageView v, String value) {
		v.setVisibility(View.VISIBLE);
		// 异步请求网络图片
		if (value.indexOf("http") == 0) {
			if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
				return;
			if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
				roundImgPixels = ToolsDevice.dp2px(context, 500);
				v.setImageResource(R.drawable.bg_round_user_icon);
			} else {
				v.setImageResource(imgResource);
			}
			v.setScaleType(ScaleType.CENTER_CROP);
			if (value.length() < 10)
				return;
			v.setTag(TAG_ID, value);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
					.load(value)
					.setSaveType(imgLevel)
					.build();
			if (bitmapRequest != null)
				bitmapRequest.into(getTarget(v, value));
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
			v.setScaleType(ScaleType.CENTER_CROP);
			v.setImageResource(imgResource);
			v.setTag(TAG_ID, value);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
					.load(value)
					.setSaveType(imgLevel)
					.build();
			if (bitmapRequest != null)
				bitmapRequest.into(getTarget(v, value));
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
					// 图片圆角和宽高适应auther_userImg
					if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
						v.setScaleType(ScaleType.CENTER_CROP);
						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, ToolsDevice.dp2px(context, 500));
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

	/**
	 * 处理文字是否显示
	 *
	 * @param v
	 * @param text
	 */
	public void setViewText(TextView v, String text) {
		if (text == null || text.length() == 0 || text.equals("hide") || " ".equals(text))
			v.setVisibility(View.GONE);
		else {
			v.setVisibility(View.VISIBLE);
			v.setText(text.trim());
		}
	}
}
