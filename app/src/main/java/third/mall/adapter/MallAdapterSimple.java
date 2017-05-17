package third.mall.adapter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import xh.basic.tool.UtilImage;
import acore.override.adapter.AdapterSimple;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import aplug.basic.LoadImage;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

public class MallAdapterSimple extends AdapterSimple {

	public MallAdapterSimple(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
	}

	@Override
	public void setViewImage(final ImageView v, String value) {
		v.setVisibility(View.VISIBLE);
		// 异步请求网络图片
		if (value.indexOf("http") == 0) {
			if (v.getTag() != null && v.getTag().equals(value))
				return;

			v.setScaleType(ScaleType.CENTER_CROP);
			//设置默认图
			if (v.getId() == R.id.iv_userImg_one ||
					v.getId() == R.id.iv_userImg_two ||
					v.getId() == R.id.iv_userImg_three) {

			} else {
				InputStream is = v.getResources().openRawResource(imgResource);
				Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
				if (roundImgPixels > 0)
					bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
				UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
			}

			if (value.length() < 10)
				return;

			v.setTag(TAG_ID, value);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
					.load(value)
					.setImageRound(roundImgPixels)
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
			Bitmap bmp = UtilImage.imgPathToBitmap(value, imgWidth, imgHeight, false, null);
			v.setScaleType(scaleType);
			v.setImageBitmap(bmp);
		}
		// 如果为ignore,则忽略图片
	}
}
