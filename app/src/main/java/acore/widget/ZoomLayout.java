package acore.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import acore.tools.LogManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZoomLayout extends RelativeLayout {
	/** 缩放动画的主要控制 */
	private ScalingRunnalable mScalingRunnalable;
	/** 该Layout自身的高度，在自身onMeasure完成以后获得 */
	private int mLayoutHeight = 0;
	/** 屏幕高度 */
	private int mScreenHeight;
	/** 缩放时触发事件的高度 */
	private int mCriticalHeight;
	/** 缩放时触发事件的高度与自身高度的比例 */
	private float mCriticalScale = 1.3f;
	/** 顶部状态栏高度 */
	private int mStateBarHeight = -1;
	int mActivePointerId = -1;
	boolean once = false;
	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float paramAnonymousFloat) {
			float f = paramAnonymousFloat - 1.0F;
			return 1.0F + f * (f * (f * (f * f)));
		}
	};
	
	private boolean isZoom = true;
	
	public ZoomLayout(Context context) {
		this(context, null,0);
	}
	
	public ZoomLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public ZoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
		//初始化动画相关
		this.mScalingRunnalable = new ScalingRunnalable();
		//获取屏幕高度
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
		this.mScreenHeight = localDisplayMetrics.heightPixels;
		
		this.post(new Runnable() {
			@Override
			public void run() {
				if(!once){
					once = true;
					//获取自身高度
					mLayoutHeight = getHeight();
					//计算触发事件高度
					mCriticalHeight = (int) (mLayoutHeight * mCriticalScale);
					//获取状态栏高度
					Rect outRect = new Rect();  
					((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect); 
					mStateBarHeight = outRect.top;
				}
			}
		});
	}

	/** 结束滑动 */
	private void endScraling() {
//		if (this.getBottom() >= this.mLayoutHeight)
//			LogManager.print("d", "endScraling");
		this.mScalingRunnalable.startAnimation(200L);
	}
	
	/** 还原数据 */
	private void reset() {
		this.mActivePointerId = -1;
		this.mLastMotionY = -1.0F;
		this.mMaxScale = -1.0F;
		this.mLastScale = -1.0F;
	}
	
	float mDownMotionX = 0;
	float mDownMotionY = 0;
	float mMoveMotionX = 0;
	float mMoveMotionY = 0;
	int mMoveDistanceX = 0;
	int mMoveDistanceY = 0;
	float mLastMotionY = -1.0F;
	float mLastScale = -1.0F;
	float mMaxScale = -1.0F;
	int mScrollMinY = 10;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(mLayoutHeight == 0){
			return super.dispatchTouchEvent(event);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_DOWN:
			try {
				if (!this.mScalingRunnalable.mIsFinished) {
					this.mScalingRunnalable.abortAnimation();
				}
				this.mDownMotionX = event.getX();
				this.mDownMotionY = event.getY();
				this.mLastMotionY = event.getY();
				this.mActivePointerId = event.getPointerId(0);
				this.mMaxScale = (this.mScreenHeight / this.mLayoutHeight);
				this.mLastScale = (this.getBottom() / this.mLayoutHeight);
			}catch (Exception e){e.printStackTrace();}

			break;
		case MotionEvent.ACTION_MOVE:
			//判断横滑，如果是横滑则不执行缩放
			try {
				this.mMoveMotionX = event.getX();
				this.mMoveMotionY = event.getY();
				this.mMoveDistanceX = (int) (mMoveMotionX - mDownMotionX);
				this.mMoveDistanceY = (int) (mMoveMotionY - mDownMotionY);
				if (Math.abs(mMoveDistanceY) <= mScrollMinY || (Math.abs(mMoveDistanceY) <= Math.abs(mMoveDistanceX))) {
					return super.dispatchTouchEvent(event);
				}
				if(!isZoom){
					return super.dispatchTouchEvent(event);
				}
				//判断位置是否在顶部，在顶部才能进行缩放
				int[] location = new int[2];
				getLocationOnScreen(location);
				if(location[1] - mStateBarHeight < 0){
					return super.dispatchTouchEvent(event);
				}
				int j = event.findPointerIndex(this.mActivePointerId);
				if (j == -1) {
					LogManager.print("e", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
				} else {
					if (this.mLastMotionY == -1.0F)
						this.mLastMotionY = event.getY(j);
					if (this.getBottom() >= this.mLayoutHeight) {
						ViewGroup.LayoutParams localLayoutParams = this.getLayoutParams();
						float f = ((event.getY(j) - this.mLastMotionY + this.getBottom())
								/ this.mLayoutHeight - this.mLastScale) / 2.0F + this.mLastScale;
						if ((this.mLastScale <= 1.0D) && (f < this.mLastScale)) {
							localLayoutParams.height = this.mLayoutHeight;
							this.setLayoutParams(localLayoutParams);
						}
						this.mLastScale = Math.min(Math.max(f, 1.0F), this.mMaxScale);
						localLayoutParams.height = ((int) (this.mLayoutHeight * this.mLastScale));
						if (localLayoutParams.height < this.mScreenHeight){
							this.setLayoutParams(localLayoutParams);
						}
						this.mLastMotionY = event.getY(j);
						return super.dispatchTouchEvent(event);
					}
					this.mLastMotionY = event.getY(j);
				}
			}catch (Exception e){
				e.printStackTrace();
			}

			break;
		case MotionEvent.ACTION_UP:
			//松手是判断时候达到触发点
			ViewGroup.LayoutParams localLayoutParams = this.getLayoutParams();
			if(localLayoutParams.height - mCriticalHeight >= 0){
				if(mCriticalHeightDelegate != null){
					mCriticalHeightDelegate.onActivate();
				}
			} 
			//还原状态
			reset();
			endScraling();
			break;
		case MotionEvent.ACTION_CANCEL:
			try {
				int i = event.getActionIndex();
				this.mLastMotionY = event.getY(i);
				this.mActivePointerId = event.getPointerId(i);
			}catch (Exception e){e.printStackTrace();}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			try {
				onSecondaryPointerUp(event);
				//此处有奔溃
				this.mLastMotionY = event.getY(event.findPointerIndex(this.mActivePointerId));
			}catch (Exception e){
				//不做任何处理
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
		}
		return super.dispatchTouchEvent(event);
	}
	
	private void onSecondaryPointerUp(MotionEvent event) {
		int i = (event.getAction()) >> 8;
		if (event.getPointerId(i) == this.mActivePointerId)
			if (i != 0) {
				this.mLastMotionY = event.getY(0);
				this.mActivePointerId = event.getPointerId(0);
			}
	}
	
	public int getHeaderHeight(){
		return mLayoutHeight;
	}
	
	public void setIsZoom(boolean isZoom){
		this.isZoom = isZoom;
	}
	
	public boolean isZoom(){
		if(mLayoutHeight != 0){
			return getHeight() > mLayoutHeight;
		}
		return false;
	}
	
	private CriticalHeightDelegate mCriticalHeightDelegate = null;
	public CriticalHeightDelegate getCriticalHeightDelegate(){
		return mCriticalHeightDelegate;
	}
	public void setCriticalHeightDelegate(CriticalHeightDelegate delegate){
		this.mCriticalHeightDelegate = delegate;
	}
	public interface CriticalHeightDelegate{
		public void onActivate();
	}

	class ScalingRunnalable implements Runnable {
		long mDuration;
		boolean mIsFinished = true;
		float mScale;
		long mStartTime;

		ScalingRunnalable() {}

		public void abortAnimation() {
			this.mIsFinished = true;
		}

		public boolean isFinished() {
			return this.mIsFinished;
		}

		public void run() {
			float f2;
			ViewGroup.LayoutParams localLayoutParams;
			if ((!this.mIsFinished) && (this.mScale > 1.0D)) {
				float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) this.mStartTime)
						/ (float) this.mDuration;
				f2 = this.mScale - (this.mScale - 1.0F) * ZoomLayout.sInterpolator.getInterpolation(f1);
				localLayoutParams = ZoomLayout.this.getLayoutParams();
				if (f2 > 1.0F) {
					localLayoutParams.height = ZoomLayout.this.mLayoutHeight;
					localLayoutParams.height = ((int) (f2 * ZoomLayout.this.mLayoutHeight));
					ZoomLayout.this.setLayoutParams(localLayoutParams);
					ZoomLayout.this.post(this);
					return;
				}
				this.mIsFinished = true;
			}
		}

		public void startAnimation(long paramLong) {
			this.mStartTime = SystemClock.currentThreadTimeMillis();
			this.mDuration = paramLong;
			this.mScale = ((float) (ZoomLayout.this.getBottom()) / ZoomLayout.this.mLayoutHeight);
			this.mIsFinished = false;
			ZoomLayout.this.post(this);
		}
	}

}
