package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class ScrollLinearHorizontalView extends HorizontalScrollView {
	
	private boolean isJudgeXY = true;
	
	public ScrollLinearHorizontalView(Context context) {
		this(context, null, 0);
	}
	
	public ScrollLinearHorizontalView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ScrollLinearHorizontalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	float x_down;
	float y_down;
	float moveDistanceX;
	float moveDistanceY;
	final int mMovingMin = 100;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(isJudgeXY){
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				x_down = ev.getRawX();
				y_down = ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float x_move = ev.getRawX();
				float y_move = ev.getRawY();
				moveDistanceX = (int) (x_move - x_down);
				moveDistanceY = (int) (y_move - y_down);
				// 判断是否在滑动
				if(Math.abs(moveDistanceY) > mMovingMin){
					ScrollLinearListLayout.isMoveY = true;
					ScrollLinearListLayout.isMoveX = false;
				}
				if (Math.abs(moveDistanceX) > mMovingMin) {
					ScrollLinearListLayout.isMoveX = true;
				} 
				break;
			default:
				break;
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	public void setIsJudgeXY(boolean isJudgeXY){
		this.isJudgeXY = isJudgeXY;
	}
	
	public boolean getIsJudgeXY(){
		return isJudgeXY;
	}
}
