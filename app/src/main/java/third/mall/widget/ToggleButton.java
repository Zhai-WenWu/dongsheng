package third.mall.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.xianghatest.R;

public class ToggleButton extends View {
	/**
	 * 监听接口
	 */
	private OnChangedListener listener;
	/**
	 * 按下时的x和当前的x
	 */
	private float downX, nowX;
	/**
	 * 当前的状态
	 */
	private boolean nowStatus = false;
	/**
	 * 开启状态下的 绘图bitmap
	 */
	private Bitmap bg_on;
	/**
	 * 关闭状态下的 绘图bitmap
	 */
	private Bitmap bg_off;
	/**
	 * 滑动块的绘图 bitmap
	 */
	private Bitmap slipper_btn;
	/**
	 * 是否是滑动状态
	 */
	private boolean onSlip;

	public ToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ToggleButton(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 测量开关控件尺寸的大小
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(bg_on.getWidth(), bg_on.getHeight());
	}

	/**
	 * 在activity中,初始化开关状态
	 * 
	 * @param state
	 */
	public void setState(boolean state) {
		if (state) {
			nowX = bg_on.getWidth();
		} else {
			nowX = 0;
		}
		nowStatus = state;
		invalidate();
	}
	public void init(Context context) {
		this.bg_on = BitmapFactory.decodeResource(getResources(),
				R.drawable.mall_offswitch);
		this.bg_off = BitmapFactory.decodeResource(getResources(),
				R.drawable.mall_onswitch);
		this.slipper_btn = BitmapFactory.decodeResource(getResources(),
				R.drawable.mall_buttonslip);
		
		setBackgroundColor(context.getResources().getColor(R.color.transparent));
	}


	public void setImageRes(int onSwitch, int offSwitch, int buttonSlip) {
		this.bg_on = BitmapFactory.decodeResource(getResources(), onSwitch);
		this.bg_off = BitmapFactory.decodeResource(getResources(), offSwitch);
		this.slipper_btn = BitmapFactory.decodeResource(getResources(),
				buttonSlip);
	}
	/**
	 * 滑动监听事件
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (event.getX() > bg_off.getWidth()
					|| event.getY() > bg_off.getHeight()) {
				return false;
			} else {
				onSlip = true;
				downX = event.getX();
				nowX = downX;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			nowX = event.getX();
			break;
		case MotionEvent.ACTION_UP:
			onSlip = false;
			if (downX == event.getX()) {
				nowStatus = !nowStatus;
				nowX = bg_on.getWidth() - slipper_btn.getWidth();
			} else if (event.getX() >= (bg_on.getWidth() / 2)) {
				nowStatus = true;
				nowX = bg_on.getWidth() - slipper_btn.getWidth();
			} else {
				nowStatus = false;
				nowX = 0;
			}
			if (listener != null) {
				listener.OnChanged(ToggleButton.this, nowStatus);
			}
			// 刷新界面
			invalidate();
			break;
		}
		return true;
	}
	/**
	 * onDraw 绘制图片
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int top = (bg_on.getHeight()-slipper_btn.getHeight())/2;
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x = 0;

		// 根据nowX设置背景，开或者关状态
		Log.i("name::", ""+nowX+":::"+(bg_on.getWidth() / 2));
		if (nowX < (bg_on.getWidth() / 2)) {
			Log.i("bg_off::", ""+nowX);
			canvas.drawBitmap(bg_off, matrix, paint);				// 画出关闭时的背景
		} else {
			Log.i("bg_on::", ""+nowX);
			canvas.drawBitmap(bg_on, matrix, paint);				// 画出打开时的背景
		}
		if (onSlip) {												// 是否是在滑动状态
			if (nowX >= bg_on.getWidth())							// 是否划出指定范围,不能让滑块跑到外头,必须做这个判断
				x = bg_on.getWidth() - slipper_btn.getWidth() / 2;// 减去滑块1/2的长度
			else
				x = nowX - slipper_btn.getWidth() / 2;
		} else {
			if (nowStatus) {										// 根据当前的状态设置滑块的x值
				x = bg_on.getWidth() - slipper_btn.getWidth();
			} else {
				x = 0;
			}
		}

		// 对滑块滑动进行异常处理，不能让滑块出界
		if (x < 0) {
			x = 0;
		} else if (x > bg_on.getWidth() - slipper_btn.getWidth()) {
			x = bg_on.getWidth() - slipper_btn.getWidth();
		}

		// 画出滑块
		if (x > 0) {
			canvas.drawBitmap(slipper_btn, x - top, top+1, paint);
		} else {
			canvas.drawBitmap(slipper_btn, x + top, top+1, paint);
		}
	}

	/**
	 * 回调接口
	 * 
	 * @author len
	 * 
	 */

	public interface OnChangedListener {
		public void OnChanged(ToggleButton toggle, boolean checkState);
	}

	/**
	 * 为WiperSwitch设置一个监听，供外部调用的方法
	 * 
	 * @param listener
	 */
	public void setOnChangedListener(OnChangedListener listener) {
		this.listener = listener;
	}

	public boolean getState() {
		return nowStatus;
	}

	public void getState(boolean state) {
		nowStatus = state;
		if (state) {
			nowX = bg_on.getWidth() / 2;
			invalidate();
		} else {
			nowX = 0;
			onSlip = false;
			invalidate();
		}
	}
}
