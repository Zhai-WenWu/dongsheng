package acore.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.StringManager;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

public class HomeSwitchImage extends RelativeLayout {
	private static final String IMAGE_LOAD_STATE_KEY = "state";
	private static final String IMAGE_LOAD_NULL = "null";
	private static final String IMAGE_LOAD_LOADING = "loading";
	private static final String IMAGE_LOAD_LOADOVER = "loadover";
	private static final String DEFAULT_HASVIDEO = "1";

	private ImageViewVideo mRealImage;
	private ImageViewVideo mFakeImage;
	private ImageView mImageMeals;
	private TextView mTextDate;

	private ArrayList<Map<String, String>> mImageArray = new ArrayList<>();
	private String mImageSaveLevel = LoadImage.SAVE_CACHE;
	private int mCurrentIndex = 0;

	public HomeSwitchImage(Context context) {
		this(context, null, 0);
	}

	public HomeSwitchImage(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HomeSwitchImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		intiView(context, attrs, defStyle);
	}

	private void intiView(Context context, AttributeSet attrs, int defStyle) {
		mRealImage = new ImageViewVideo(context);
		mRealImage.setScaleType(ScaleType.CENTER_CROP);
		addView(mRealImage, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

		mFakeImage = new ImageViewVideo(context);
		mFakeImage.setScaleType(ScaleType.CENTER_CROP);
		addView(mFakeImage, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

		LayoutInflater layoutInflate = LayoutInflater.from(context);
		layoutInflate.inflate(R.layout.a_home_gallery_imageview, this);
		mImageMeals = (ImageView) findViewById(R.id.a_home_gallery_image);
		mImageMeals.setVisibility(View.GONE);
		layoutInflate.inflate(R.layout.a_home_gallery_textview, this);
		LinearLayout textLayout = (LinearLayout) findViewById(R.id.a_home_gallery_text_layout);
		mTextDate = (TextView) findViewById(R.id.a_home_gallery_text);
		mTextDate.setVisibility(View.GONE);

		RelativeLayout.LayoutParams rlParams = (RelativeLayout.LayoutParams) mImageMeals.getLayoutParams();
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		rlParams = (RelativeLayout.LayoutParams) textLayout.getLayoutParams();
		rlParams.addRule(RelativeLayout.LEFT_OF, R.id.a_home_gallery_image);
		rlParams.addRule(RelativeLayout.ALIGN_TOP, R.id.a_home_gallery_image);
	}

	/**
	 * 当前菜单的数据
	 *
	 * @param dataMap
	 */
	public void initData(Map<String, String> dataMap) {
		if (dataMap == null) {
			return;
		}
		//设置时间
		setDate(dataMap);
		//设置早餐，中餐，晚餐
		setMeals(dataMap);
		mImageArray = StringManager.getListMapByJson(dataMap.get("list"));
		//初始化数据集合
		final int length = mImageArray.size();
		if (length == 0) {
			return;
		}
		for (int index = 0; index < length; index++) {
			upadteImageState(index, IMAGE_LOAD_NULL);
		}
		//设置第一张图片
		loadImage(0, false);
	}

	private void setMeals(Map<String, String> dataMap) {
		//设置早中晚的图片
		String type = dataMap.get("type");
		int iconId = 0;
		if ("1".equals(type)) {
			iconId = R.drawable.i_ico_homepage_sancan_zao;
		} else if ("2".equals(type)) {
			iconId = R.drawable.i_ico_homepage_sancan_zhong;
		} else if ("3".equals(type)) {
			iconId = R.drawable.i_ico_homepage_sancan_wan;
		}
		if (iconId != 0) {
			mImageMeals.setImageResource(iconId);
			mImageMeals.setVisibility(View.VISIBLE);
		}
	}

	private void setDate(Map<String, String> dataMap) {
		String date = dataMap.get("date");
		if (date != null) {
			mTextDate.setText(date);
			mTextDate.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 为真正的imageview设置图片
	 *
	 * @param index
	 */
	private void loadImage(final int index, final boolean isPreLoad) {
		if (mImageArray.size() > index) {
			final Map<String, String> imageData = mImageArray.get(index);
			if (imageData != null) {
				String imageUrl = imageData.get("img");
				final String hasVideo = imageData.get("hasVideo");
				if (IMAGE_LOAD_LOADING.equals(imageData.get(IMAGE_LOAD_STATE_KEY))) {
					return;
				}
				//更新load状态为ing
				upadteImageState(index, IMAGE_LOAD_LOADING);
				BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
						.load(imageUrl)
						.setSaveType(mImageSaveLevel)
						.setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
							@Override
							public boolean onResourceReady(Bitmap bitmap, GlideUrl glideUrl, Target<Bitmap> target,
							                               boolean arg3, boolean arg4) {
								return false;
							}

							@Override
							public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
								//更新load状态为null
								upadteImageState(index, IMAGE_LOAD_NULL);
								return false;
							}
						})
						.build();
				if (bitmapRequest != null) {
					bitmapRequest.into(new SubBitmapTarget() {
						@Override
						public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
							upadteImageState(index, IMAGE_LOAD_LOADOVER);
							if (isPreLoad) {
								return;
							}
							//设置当前图片
							setCurrentImage(index, bitmap, hasVideo);
							mFakeImage.setImageBitmap(bitmap, hasVideo);
						}
					});
				}
			}
		}
	}

	public void setCurrentImage(int currentIndex, Bitmap bitmap, String hasVideo) {
		this.mCurrentIndex = currentIndex;
		mRealImage.setImageBitmap(bitmap, hasVideo);
	}

	public void setCurrentFakeImage(Bitmap bitmap, String hasVideo) {
		mFakeImage.setImageBitmap(bitmap, hasVideo);
	}

	/**
	 * 更新图片的load状态
	 *
	 * @param index
	 * @param state
	 *
	 * @return 是否更新成功
	 */
	private boolean upadteImageState(int index, String state) {
		if (mImageArray.size() > index) {
			Map<String, String> imageData = mImageArray.get(index);
			if (mImageArray != null) {
				imageData.put(IMAGE_LOAD_STATE_KEY, state);
				return true;
			}
		}
		return false;
	}

	/**
	 * 预加载所有图片
	 */
	public void preLoadImage() {
		final int length = mImageArray.size();
		for (int index = 0; index < length; index++) {
			loadImage(index, true);
		}
	}

	/**
	 * 切换图片
	 */
	public void switchImage(final int index) {
		//初始化默认值
//		Bitmap nextBitmap = null;
//		final int nextIndex = mCurrentIndex;
//		String hasVideo = DEFAULT_HASVIDEO;
		final int length = mImageArray.size();
		final int next = (index + mCurrentIndex) % length;
		final Map<String, String> imgData = mImageArray.get(next);
		if (imgData != null && IMAGE_LOAD_LOADOVER.equals(imgData.get(IMAGE_LOAD_STATE_KEY))) {
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
					.load(imgData.get("img"))
					.build();
			if (bitmapRequest != null) {
				bitmapRequest.into(new SubBitmapTarget() {
					Bitmap nextBitmap = null;
					int nextIndex = mCurrentIndex;
					String hasVideo = DEFAULT_HASVIDEO;

					@Override
					public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
						if (bitmap != null) {
							nextBitmap = bitmap;
							//获取下一个图片的index
							nextIndex = next;
							//获取下一个图片是否有video的状态
							hasVideo = imgData.get("hasVideo");
							startAnimation(nextIndex, nextBitmap, hasVideo);
						}
					}
				});
			}
		}else if(index <= length){
			switchImage(index + 1);
		}
	}

	private void startAnimation(final int nextIndex, final Bitmap nextBitmap, final String hasVideo) {
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(1000);
		alphaAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mFakeImage.setVisibility(View.VISIBLE);
				setCurrentImage(nextIndex, nextBitmap, hasVideo);
				if (mOnSyncDataDelegate != null) {
					mOnSyncDataDelegate.onSyncCurrentImage(nextIndex, nextBitmap, hasVideo);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mFakeImage.setVisibility(View.INVISIBLE);
				mFakeImage.setImageBitmap(nextBitmap, hasVideo);
				mFakeImage.clearAnimation();
			}
		});
		mFakeImage.startAnimation(alphaAnimation);
	}

	private float currentX, oldX;//只是用于统计的两个参数

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				oldX = event.getX();
				break;
			case MotionEvent.ACTION_MOVE:
				currentX = event.getX();
				break;
			case MotionEvent.ACTION_UP:
				if (Math.abs(currentX - oldX) > 3) {
					//7.28新加统计
					XHClick.mapStat(getContext(), "a_index_switch", "三餐推荐", "左右切换");
				}
				break;
			default:
				break;
		}
		return super.onTouchEvent(event);
	}

	private OnSyncDataDelegate mOnSyncDataDelegate;

	public interface OnSyncDataDelegate {
		public void onSyncCurrentImage(int nextIndex, Bitmap nextBitmap, String hasVideo);
	}

	public void setOnSyncDataDelegate(OnSyncDataDelegate delegate) {
		this.mOnSyncDataDelegate = delegate;
	}

	public ImageView getRealImage() {
		return mRealImage;
	}

	public void setRealImage(ImageViewVideo mRealImage) {
		this.mRealImage = mRealImage;
	}

	public ImageView getFakeImage() {
		return mFakeImage;
	}

	public void setFakeImage(ImageViewVideo mFakeImage) {
		this.mFakeImage = mFakeImage;
	}

	public ArrayList<Map<String, String>> getImageStateArray() {
		return mImageArray;
	}

}
