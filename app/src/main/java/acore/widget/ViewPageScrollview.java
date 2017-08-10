package acore.widget;

import xh.basic.tool.UtilImage;
import acore.tools.Tools;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.xianghatest.R;

public class ViewPageScrollview extends ScrollView {

	private Context context;
	private RelativeLayout linearLayout;
	private RelativeLayout relativeLayoutBg;
	private ImageView imageView;
//	private RelativeLayout relativeLayout;
	private int startHeight;// 动画控件的初始高度
	private int startWidth;// 动画控件的初始宽度
//	private int screenHeight;// 屏幕高度
	private int screenWidth;// 屏幕宽度
	private int distanceScreenTop;// 动画控件距离屏幕顶部的距离
	private int heightChange;// 动画控件变化后的高度
//	private int widthChange;// 动画控件需要放大的宽度.
//	private int widthChangeTo;
//	private int height148 ;
	// 计算滑动时的比值ֵ
//	private int ratio;
	
	
	
	private float xDistance, yDistance, xLast, yLast;
	private int scollHight;
	private Bitmap newb;
	private Canvas canvas;
	public float yTouch;
	public boolean isTop = false;
	public float topDistance = 0;
	private Bitmap newsb;

	public ViewPageScrollview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 
	 * @param context
	 *            传入的activity
	 * @param linearLayout
	 *            悬浮动画的控件所在布局
	 * @param distanceScreenTop
	 *            悬浮动画控件距离屏幕顶部的像素距离.即scrollview滑动多少后将动画固定悬浮.
	 * @param widthChange
	 *            想要动画的宽度变化的多少即控件距离屏幕左右边缘大小之和.
	 * @param sousuo_view 
	 * @param height
	 *            想要动画的高度缩小到多少.
	 * 
	 */
	public void setScrollData(Activity context,RelativeLayout relativeLayoutBar, RelativeLayout layoutBar , int distanceScreenTop,
			int widthChange, int widthChangeTo, int heightChange,
			int height95,ImageView imageView) {
		this.context = context;
		this.linearLayout = layoutBar;
		this.relativeLayoutBg = relativeLayoutBar;
		this.distanceScreenTop = distanceScreenTop;
		this.heightChange = heightChange;
//		this.widthChangeTo = widthChangeTo;
//		this.height148 = height95;
		this.imageView = imageView;
		// 初始大小
		android.view.ViewGroup.LayoutParams params = linearLayout
				.getLayoutParams();
		// 屏幕大小
		// 屏幕
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 屏幕宽度
		screenWidth = dm.widthPixels;
		// 屏幕高度
//		screenHeight = dm.heightPixels;
		// 获取初始化时悬浮动画的宽和高
		startWidth = screenWidth - widthChange;
		startHeight = params.height;
//		scollHight = distanceScreenTop/2;
		scollHight = (distanceScreenTop + startHeight - heightChange)/2;
		newb = Bitmap.createBitmap(screenWidth, Tools.getDimen(context, R.dimen.dp_34), Config.ARGB_8888);
		canvas = new Canvas(newb);
		// 获取比值
//		ratio = distanceScreenTop / (widthChange - widthChangeTo);
	}
	/* (non-Javadoc)
	 * @see android.view.View#onScrollChanged(int, int, int, int)
	 * 在if语句中的运算时因为搜索框变小的时候高度减小,所以要加大
	 */
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		android.view.ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
		RelativeLayout.LayoutParams bgParams=(RelativeLayout.LayoutParams) relativeLayoutBg.getLayoutParams();
		//由于在上滑过程中,搜索框是在不断变小的,但是还要和底部对其,所以就不断的向下移动.就会产生距离的差异.
		if (t <= (distanceScreenTop + startHeight - heightChange)) {
			//设置悬浮动画的坐标
			
//			relativeLayoutBg.setY(distanceScreenTop - t);
			//改变动画的大小,实现动态效果
			//加这个大于0 的判断主要是为了解决由于在某些高大上的手机上(华为),它修改 了系统的滑动方法,造成了动画变形的问题.
//			if (t >= 0) {
//				params.width = startWidth + t / ratio;
//			}else {
//				params.width = startWidth;
//			}
			//20150505改为透明背景后去除变形效果
			int height = (int) (startHeight - ((float) (startHeight - heightChange) / distanceScreenTop)
					* t);
//			params.height = (int) (startHeight - ((float) (startHeight - heightChange) / distanceScreenTop)
//					* t);
			params.height = height;
			params.width = startWidth;
			linearLayout.setLayoutParams(params);
			bgParams.setMargins(bgParams.leftMargin, distanceScreenTop - t + startHeight - height, bgParams.rightMargin, bgParams.bottomMargin);
//			//设置背景(有圆角)
//			relativeLayout.setBackgroundResource(R.drawable.bg_round_sousuo);
//			relativeLayoutBg.setBackgroundColor(Color.parseColor("#45E3E3E3"));
			relativeLayoutBg.setLayoutParams(bgParams);
			if (t >= scollHight) {
				//这个是20150506为了那个渐变效果才加的方法
				float m = (float)(t - scollHight)/scollHight;
				int argb = Color.argb((int) (m*174), 255, 255, 255);
				relativeLayoutBg.setBackgroundColor(argb);
				
				int bgColor = (int) (m*20);
				
				
	//			relativeLayout.setBackgroundResource(R.drawable.bg_round_sousuo);
				
				
				canvas.drawARGB(255, 255-bgColor, 255-bgColor, 255-bgColor);
	            canvas.drawBitmap(newb, 0, 0, null); 
	            newsb = UtilImage.toRoundCorner(getResources(), newb, 1, Tools.getDimen(context, R.dimen.dp_5));
	            imageView.setImageBitmap(newsb);
			}else {
				relativeLayoutBg.setBackgroundColor(Color.argb(0, 255, 255, 255));
//				Canvas canvas =  new Canvas(newb);
				canvas.drawARGB(255, 255, 255, 255);
	            canvas.drawBitmap(newb, 0, 0, null); 
	            newsb = UtilImage.toRoundCorner(getResources(), newb, 1, Tools.getDimen(context, R.dimen.dp_5));
	            imageView.setImageBitmap(newsb);
			}
			
		    
		} else {
			//固定悬浮动画的坐标
//			relativeLayoutBg.setY(0);
			bgParams.setMargins(bgParams.leftMargin, 0, bgParams.rightMargin, bgParams.bottomMargin);
			//固定悬浮动画的大小,当用户死命的往上滑,不会造成搜索框变形不明显
//			params.width = screenWidth - widthChangeTo;
//			params.width = startWidth + distanceScreenTop / ratio;
			//20150505改为透明背景后去除变形效果
			params.height = heightChange;
			params.width = startWidth;
			linearLayout.setLayoutParams(params);
			//设置背景
//				relativeLayout.setBackgroundResource(R.drawable.bg_round_sousuo3);
			if (t > (distanceScreenTop + startHeight - heightChange)) {
				//防止发生突变
				relativeLayoutBg.setBackgroundColor(Color.argb(174, 255, 255, 255));
//				relativeLayoutBg.setBackgroundResource(R.drawable.bg_round_sousuo_homepage);
				//中间的变化
//				imageView.setBackgroundColor(Color.argb(255, 235, 235, 235));
//				Bitmap bitmap = imageView.getDrawingCache();
//				Bitmap bitmap2 = Tools.toRoundCorner(getResources(), bitmap, 1, Tools.dp2px(context, Tools.getDimen(context, R.dimen.dp_5)));
////				imageView.setImageBitmap(bitmap2);
//				imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap2));
				
//				Canvas canvas =  new Canvas(newb);
				canvas.drawARGB(255, 235, 235, 235);
	            canvas.drawBitmap(newb, 0, 0, null); 
	            newsb = UtilImage.toRoundCorner(getResources(), newb, 1, Tools.getDimen(context, R.dimen.dp_5));
	            imageView.setImageBitmap(newsb);
			}else {
//				relativeLayoutBg.setBackgroundColor(Color.parseColor("#E3E3E3"));
			}
			relativeLayoutBg.setLayoutParams(bgParams);
//			relativeLayout.setBackgroundColor(Color.parseColor("#ff0000"));
		}
	}
	// 防止跟ViewPager冲突，暂时不用
	/**后来加的,为了在首页布局中获取scrollview滑动时的一些值.
	 * if (getScrollY() == 0) {
				yTouch = ev.getY();
				isTop = true;
			}else {
				isTop = false;
				yTouch = ev.getY();
				topDistance = getScrollY();
			}
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			if (getScrollY() == 0) {
				yTouch = ev.getY();
				isTop = true;
			}else {
				isTop = false;
				yTouch = ev.getY();
				topDistance = getScrollY();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance) {
				return false;
			}
		}
//		if (getScrollY() == 0) {
//			return true;
//		}
		return super.onInterceptTouchEvent(ev);
	}
}
