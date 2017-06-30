package acore.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.io.InputStream;

import acore.tools.FileManager;
import acore.tools.ImgManager;
import acore.tools.Tools;
import aplug.basic.SubBitmapTarget;
import aplug.basic.LoadImage;
import xh.basic.tool.UtilImage;

import static android.R.attr.value;

public class ImageViewVideo extends ImageView {

	private static final int TAG_ID = R.string.tag;
	private Context mContext;
	private boolean mIsHasVideo;
	private Bitmap mPlayBitmap, mBitmap;

	public int playImgWH = 41;

	int left, top;


	public ImageViewVideo(Context context) {
		super(context);
		mContext = context;
		playImgWH = Tools.getDimen(mContext, R.dimen.dp_41);
	}

	public ImageViewVideo(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		mBitmap = bm;
	}

	public void setImageBitmap(Bitmap bm, boolean isHasVideo) {
		mIsHasVideo = isHasVideo;
		super.setImageBitmap(bm);
		mBitmap = bm;
	}

	public void setImageBitmap(Bitmap bm, String isHasVideo) {
		mIsHasVideo = "2".equals(isHasVideo);
		super.setImageBitmap(bm);
		mBitmap = bm;
	}


	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mIsHasVideo) {
			InputStream is = getResources().openRawResource(R.drawable.home_item_play);
			mPlayBitmap = UtilImage.inputStreamTobitmap(is);
			left = getWidth() / 2 - playImgWH / 2;
			top = getHeight() / 2 - playImgWH / 2;
			//对图片的切割显示
			final Rect rect = new Rect(0, 0, mPlayBitmap.getWidth(), mPlayBitmap.getHeight());
			//图片在画布上的显示位置和大小
			final Rect dst = new Rect(left, top, left + playImgWH, top + playImgWH);
			//通过paint设置抗锯齿
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			canvas.drawBitmap(mPlayBitmap, rect, dst, paint);
		}
	}

	public Bitmap getBitmap() {
		Bitmap btp = null;
		if (mBitmap != null && mPlayBitmap != null) {
			int mBW = mBitmap.getWidth();
			int mBH = mBitmap.getHeight();
			btp = Bitmap.createBitmap(mBW, mBH, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(btp);
			//对图片的切割显示
			Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
			//图片在画布上的显示位置和大小
			Rect dst = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
			canvas.drawBitmap(mBitmap, rect, dst, new Paint());

			left = mBW / 2 - playImgWH / 2;
			top = mBH / 2 - playImgWH / 2;
//			//对图片的切割显示
			rect = new Rect(0, 0, mPlayBitmap.getWidth(), mPlayBitmap.getHeight());
//			//图片在画布上的显示位置和大小
			dst = new Rect(left, top, left + playImgWH, top + playImgWH);
			canvas.drawBitmap(mPlayBitmap, rect, dst, new Paint());
		}
		return btp;
	}

	/**
	 * @param scaleType   :
	 * @param imgValue
	 * @param hasVideo
	 * @param isAnimate
	 * @param imgResource
	 * @param imgLevel
	 */
	public void parseItemImg(ScaleType scaleType, final String imgValue, final String hasVideo, final boolean isAnimate, int imgResource, String imgLevel) {
		if (imgValue == null || imgValue.equals("") || hasVideo == null || hasVideo.equals(""))
			return;
		setVisibility(View.VISIBLE);
		if (imgValue.indexOf("http") == 0) {
			setScaleType(scaleType);
			//设置默认图
			InputStream is = getResources().openRawResource(imgResource);
			Bitmap bitmap = ImgManager.inputStreamTobitmap(is);
			setImageBitmap(bitmap, false);
			if (imgValue.length() < 10)
				return;
			setTag(TAG_ID, imgValue);
			if(mContext==null)return;

			// 如果为ignore,则忽略图片
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mContext)
					.load(imgValue)
					.setSaveType(imgLevel)
					.setPlaceholderId(imgResource)
					.build();
			if (bitmapRequest != null) {
				bitmapRequest.into(getTarget(imgValue, hasVideo, isAnimate));
			}
		}// 直接设置为内部图片
		else if (imgValue.indexOf("ico") == 0) {
			InputStream is = getResources().openRawResource(Integer.parseInt(imgValue.replace("ico", "")));
			Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
			bitmap = UtilImage.toRoundCorner(getResources(), bitmap, 1, 0);
			setImageBitmap(bitmap, "2".equals(hasVideo));
		}// 隐藏
		else if (imgValue.equals("hide") || imgValue.length() == 0)
			setVisibility(View.GONE);
			// 直接加载本地图片
		else if (!imgValue.equals("ignore")) {
			InputStream is = getResources().openRawResource(imgResource);
			Bitmap bitmap = ImgManager.inputStreamTobitmap(is);
			setImageBitmap(bitmap, false);
			if (imgValue.length() < 10)
				return;
			setTag(TAG_ID, imgValue);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mContext).load(imgValue)
					.setSaveType(imgLevel)
					.setPlaceholderId(imgResource)
					.build();
			if (bitmapRequest != null) {
				bitmapRequest.into(getTarget(imgValue, hasVideo, isAnimate));
			}
		}
	}

	private SubBitmapTarget getTarget(final String imgValue, final String hasVideo, final boolean isAnimate) {
		return new SubBitmapTarget() {
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = null;
				if (getTag(TAG_ID) != null && !TextUtils.isEmpty(imgValue) && getTag(TAG_ID).equals(imgValue))
					img = ImageViewVideo.this;
				if (img != null && bitmap != null) {
					setImageBitmap(bitmap, "2".equals(hasVideo));
					if (isAnimate) {
//						AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//						alphaAnimation.setDuration(300);
//						setAnimation(alphaAnimation);
					}
				}
			}
		};
	}

	public void parseItemImg(ScaleType scaleType, String imgValue, final boolean hasVideo, final boolean isAnimate, int imgResource, String imgLevel) {
		String hsVideo = "1";
		if (hasVideo)
			hsVideo = "2";
		parseItemImg(scaleType, imgValue, hsVideo, isAnimate, imgResource, imgLevel);
	}

	public void parseItemImg(String imgValue, String hasVideo, final boolean isAnimate) {
		parseItemImg(ScaleType.CENTER_CROP, imgValue, hasVideo, isAnimate, R.drawable.i_nopic, FileManager.save_cache);
	}
	public void parseItemImg(String imgValue, boolean hasVideo, final boolean isAnimate) {
		parseItemImg(ScaleType.CENTER_CROP, imgValue, hasVideo, isAnimate, R.drawable.i_nopic, FileManager.save_cache);
	}

}
