package aplug.stickheaderlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class StickHeader extends LinearLayout {
	public StickHeader(Context context) {
		super(context);
	}
	
	public StickHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public StickHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	private int mMovingMin = 30;
	private boolean mIsMoving;

	float x_down;
	float y_down;
	float x_move;
	float y_move;
	float moveDistanceX;
	float moveDistanceY;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsMoving = false;
			x_down = ev.getRawX();
			y_down = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			x_move = ev.getRawX();
			y_move = ev.getRawY();
			moveDistanceX = (int) (x_move - x_down);
			moveDistanceY = (int) (y_move - y_down);
			// 判断是否在滑动
			mIsMoving = Math.abs(moveDistanceY) > mMovingMin || Math.abs(moveDistanceX) > mMovingMin;
			break;
		case MotionEvent.ACTION_UP:
			// 如果是在滑动中up事件被拦截
			if (mIsMoving) {// mIsMoving &&
				ev.setAction(MotionEvent.ACTION_CANCEL);
			}
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

}
