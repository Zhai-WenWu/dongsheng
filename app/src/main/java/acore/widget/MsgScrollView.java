package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MsgScrollView extends ScrollView{
	
	private float xDistance, yDistance, xLast, yLast;
	private boolean isSwitchMove = false;
	public MsgScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	// 防止跟switchbutton冲突
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isSwitchMove = false;
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			
			if (isSwitchMove) {
				return false;
			}
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > 2 * yDistance) {
				isSwitchMove = true;
				return false;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
