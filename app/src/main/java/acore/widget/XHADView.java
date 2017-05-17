package acore.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.xiangha.R;

import java.util.Timer;
import java.util.TimerTask;

import acore.logic.XHClick;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.BackRelativeLayout.OnBackListener;
/**
 * 用于显示广告的view
 * @author Eva	
 *
 */
public class XHADView extends ScrollView{
	private static XHADView mADScrollView;
	//必须为activity的context
	private Context mContext = null;
	private Activity mActivity = null;
	//创建广告是添加在此WindowManager中
	private WindowManager mWindowManager;
	//mADImage的parent
	private LinearLayout mWapper = null;
//	广告的ImageView的容器
	private RelativeLayout mRelativeLayout = null;
	//广告的ImageView
	private ImageView mADImage = null;
	//关闭广告
	private ImageView mClose = null;
	//广告中透明的View
	private View mEmptyView = null;
	
	private boolean onceMeasure = false;
	private boolean onceAddWindow = false; 
	private static boolean isClosed = false;
	private int mScreenHeight = 0;
	private boolean once = false;
	//计时器
	private Timer mTimer;
	final Handler handler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			try{
				if(msg.arg1 >= 0)
					show(msg.arg1);
			}catch(Exception e){
				LogManager.reportError("" + e.getMessage(), e);
			}
			return false;
		}
	});
	private BackRelativeLayout mLayout;
	
	public static XHADView getInstence(Activity context){
		if(isClosed){
			return null;
		}
		if(mADScrollView == null){
			mADScrollView = new XHADView(context);
		}
		return mADScrollView;
	}

	public XHADView(Context context) {
		this(context,null);
	}
	
	public XHADView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	} 

	public XHADView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//初始化默认的mContext
		this.mContext = context;

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				if(!once){
					once = true;
					WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
					DisplayMetrics outMetrics = new DisplayMetrics();
					windowManager.getDefaultDisplay().getMetrics(outMetrics);
					//获取状态栏高度
					Rect outRect = new Rect();
					((Activity) mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
					int mStateBarHeight = outRect.top;
					mScreenHeight = outMetrics.heightPixels - mStateBarHeight;
				}
			}
		});
		
		initUI(context);
	}
	
	//添加对应的UI
	private void initUI(final Context context) {
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		mWapper = new LinearLayout(context);
		mWapper.setOrientation(LinearLayout.VERTICAL);
		addView(mWapper,layoutParams);

		mRelativeLayout = new RelativeLayout(context);
		RelativeLayout.LayoutParams rlParams = null;
		mADImage = new ImageView(context);
		ViewGroup.LayoutParams imgLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ToolsDevice.getWindowPx(mContext).heightPixels);
		mADImage.setScaleType(ScaleType.CENTER_CROP);
		mRelativeLayout.addView(mADImage,imgLayoutParams);
		
		mClose = new ImageView(context);
		mClose.setScaleType(ScaleType.FIT_XY);
		int width = Tools.getDimen(mContext, R.dimen.dp_25);
		int height = width;
		rlParams = new RelativeLayout.LayoutParams(width,height);
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		int topMargins = Tools.getDimen(context, R.dimen.dp_30);
		int rightMargins = Tools.getDimen(context, R.dimen.dp_17_5);
		rlParams.setMargins(0, topMargins, rightMargins, 0);
		mRelativeLayout.addView(mClose,rlParams);
		
		
		rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,Tools.getDimen(mContext, R.dimen.dp_30));
		rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		ImageView topSlipOff = new ImageView(mContext);
		topSlipOff.setImageResource(R.drawable.ad_top_slip_off);
		topSlipOff.setScaleType(ScaleType.CENTER_CROP);
		mRelativeLayout.addView(topSlipOff,rlParams);
		
		mWapper.addView(mRelativeLayout, layoutParams);
		
		mEmptyView = new View(context);
		mEmptyView.setBackgroundResource(android.R.color.transparent);
		mWapper.addView(mEmptyView, layoutParams);
		
		mClose.setImageResource(R.drawable.ad_close);
		mClose.setClickable(true);
		mClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				XHClick.mapStat(XHADView.this.getContext(), "a_fullcereen_ad", "手动关闭", "");
				smoothScrollTo(0, mScreenHeight);
			}
		});
	}
	
	/**
	 * 必须初始化的方法
	 * @param delay
	 * @param displayTime
	 */
	public void initTimer(int delay,final int displayTime) {
		if(delay < 0 || displayTime < 0){
			return;
		}
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Message msg = new Message();
				msg.arg1 = displayTime;
				handler.sendMessage(msg);
			}
		};
		mTimer = new Timer(true);
		mTimer.schedule(task, delay);
		//timer.cancel(); //退出计时器
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(!onceMeasure){
			mEmptyView.getLayoutParams().height = mScreenHeight;
			mRelativeLayout.getLayoutParams().height = mScreenHeight;
			onceMeasure = true;
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		int action = ev.getAction();
		switch (action){
		case MotionEvent.ACTION_UP:
			LogManager.print("d", "onTouchEvent up y=" + getScrollY());
			int scrollY = getScrollY();
			if (scrollY >= mScreenHeight/6){
				XHClick.mapStat(this.getContext(), "a_fullcereen_ad", "手动关闭", "");
				this.smoothScrollTo(0, mScreenHeight);
			}else{
				this.smoothScrollTo(0, 0);
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		LogManager.print("d", "oldY = " + oldt + " ; currentY = " + t);
		int[] location = new int[2];
		if (mEmptyView != null)
			mEmptyView.getLocationOnScreen(location);
		if(getScrollY() == mScreenHeight){
			hide();
		}
	}
	
	//添加到WindowManager中，并显示
	private void show(int displayTime){
		isClosed = false;
		if(mActivity != null){
			if(mActivity.isFinishing()){//mActivity.isDestroyed() API-level-17
				return;
			}
			mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
			mWapper.setVisibility(View.GONE);
			WindowManager.LayoutParams windowLayoutParas = new WindowManager.LayoutParams();
			windowLayoutParas.type = WindowManager.LayoutParams.TYPE_APPLICATION;
			windowLayoutParas.format = PixelFormat.RGBA_8888;
			
			if(!onceAddWindow){
				mLayout = new BackRelativeLayout(mContext);
				mLayout.setOnBackListener(new OnBackListener() {
					@Override
					public void onBack(View v) {
						XHClick.mapStat(mContext, "a_fullcereen_ad", "手动关闭", "");
						smoothScrollTo(0, mScreenHeight);
					}
				});
				mLayout.addView(mADScrollView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
				mWindowManager.addView(mLayout, windowLayoutParas);
				onceAddWindow =true;
			}
			setVisibility(View.VISIBLE);
			mWapper.setVisibility(View.VISIBLE);
			Animation animStart = AnimationUtils.loadAnimation(getContext(), R.anim.translate_start);
			mWapper.startAnimation(animStart);
			scrollTo(0, 0);
			if(displayTime == 0){
				return;
			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					 hide();
				}
			}, displayTime);
		}
	}
	
	//隐藏并销毁
	public void hide(){
		if(!isClosed){
			isClosed = true;
		}
		try {
			if(isClosed){
				this.smoothScrollTo(0, mScreenHeight);
				onDestroy();
			}
		}catch (Exception e){

		}
	}
	
	private void onDestroy(){
		this.setVisibility(View.GONE);
		
		if(mWindowManager != null 
				&& mActivity != null 
				&& !mActivity.isFinishing() 
				&& mLayout != null){
			mWindowManager.removeView(mLayout);
		}
		if(mTimer != null){
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
		mADScrollView = null;
	}
	
	/**
	 * 刷新自己所持有的context，以此来刷新显示之后所依附的Activity
	 * @param act
	 */
	public void refreshContext(Activity act){
		this.mActivity = act;
	}
	
	/**
	 * 设置ADImage的click事件
	 * @param clickListener
	 */
	public void setADClickListener(OnClickListener clickListener){
		if(mADImage != null && clickListener != null){
			mADImage.setOnClickListener(clickListener);
		}
	}
	
	/**
	 * 设置ADImage的图片
	 * @param bm
	 */
	public void setImage(Bitmap bm){
		if(mADImage != null && bm != null){
			mADImage.setImageBitmap(bm);
		}
	}
	
	/**
	 * 
	 * @return true 显示 ： false 消失
	 */
	public boolean isVisibilityWapper(){
		if(mWapper == null){
			return false;
		}
		return mWapper.getVisibility() == View.VISIBLE;
	}

}
