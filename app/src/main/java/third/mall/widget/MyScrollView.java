package third.mall.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	float down_y;
	float move_y;
	float ok_y;
	private int now_y;
	boolean state=true;
	private ScrollViewInterface sv_interface;
	public void setInterfaceSv(ScrollViewInterface sv_interface){
		this.sv_interface= sv_interface;
	}
	public MyScrollView(Context context) {
		super(context);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			down_y= (float) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			move_y= ev.getY();
			float y=move_y-down_y;
			if(y<0){//逐渐显示出来
				if(now_y<255){
					sv_interface.setYandState(y, state);
				}

			}else{//逐渐变淡
				if(now_y>0&&now_y<=255){
					sv_interface.setYandState(y, state);
				}
			}

			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			ok_y= ev.getY();
			int y_i= (int) Math.floor(ok_y-down_y);
			if(y_i<0){//逐渐显示出来
				if(now_y<255){
					sv_interface.setYandState(y_i, state);
				}
			}else{//逐渐变淡
				if(now_y>0&&now_y<=255){
					sv_interface.setYandState(y_i, state);
				}
			}
			sv_interface.setYandState(ok_y, state);
			break;

		}
		return super.dispatchTouchEvent(ev);
	}

	private float xDistance, yDistance, yLast, xLast;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				xDistance = yDistance = 0f;
				xLast = ev.getX();
				yLast = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float curX = ev.getX();
				final float curY = ev.getY();

				xDistance += Math.abs(curX - xLast);
				yDistance += Math.abs(curY - yLast);
				xLast = curX;
				yLast = curY;

				/**
				 * X轴滑动距离大于Y轴滑动距离，也就是用户横向滑动时，返回false，ScrollView不处理这次事件，
				 * 让子控件中的TouchEvent去处理，所以横向滑动的事件交由子控件处理， ScrollView只处理纵向滑动事件
				 */
				if (xDistance > yDistance) {
					return false;
				}
		}

		return super.onInterceptTouchEvent(ev);
	}

	public interface ScrollViewInterface{
		public abstract void setYandState(float y,boolean state);
	}

	public int getNow_y() {
		return now_y;
	}
	public void setNow_y(int now_y) {
		this.now_y = now_y;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mScrollChangedListener != null)
			mScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
	}

	public interface OnScrollChangedListener {
		public abstract void onScrollChanged(int l, int t, int oldl, int oldt);
	}

	private OnScrollChangedListener mScrollChangedListener;

	public void setOnScrollChangedListener (OnScrollChangedListener listener) {
		mScrollChangedListener = listener;
	}
}
