package third.mall.view;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGesture extends GestureDetector {

public MyGesture(OnGestureListener listener, Handler handler) {
		super(listener, handler);
	}

public MyGesture(OnGestureListener listener) {
		super(listener);
	}

	public MyGesture(Context context, OnGestureListener listener, Handler handler) {
		super(context, listener, handler);
	}

	public MyGesture(Context context, OnGestureListener listener, Handler handler, boolean unused) {
		super(context, listener, handler, unused);
	}

	private float down_y;
	
	public MyGesture(Context context, OnGestureListener listener) {
		super(context, listener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			down_y=ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float move_y=ev.getY();
			if(Math.abs(move_y-down_y)>20){
				System.out.println("move_y-down_y::"+(move_y-down_y));
				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:

			break;

		}
		return super.onTouchEvent(ev);
	}
}
