package acore.widget;

import acore.tools.ToolsDevice;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 实现监听左右滑动的事件，哪个view需要的时候直接setOnTouchListener就可以用了
 * @author LinZhiquan
 *
 */
public class GestureListener extends SimpleOnGestureListener implements OnTouchListener {
	/** 左右滑动的最短距离 */
	private int distance_X ;
	private int distance_Y;
	
	private GestureDetector gestureDetector;
	
	public GestureListener(Context context) {
		super();
		gestureDetector = new GestureDetector(context, this);
		distance_X =  ToolsDevice.dp2px(context, 60);
		distance_Y =  ToolsDevice.dp2px(context, 130);
//		Log.i("FFF","distance_X = "+ distance_X + "distance_Y" + distance_Y);
	}

	/**
	 * 向左滑的时候调用的方法，子类应该重写
	 * @return
	 */
	public boolean left() {
		return false;
	}
	
	/**
	 * 向右滑的时候调用的方法，子类应该重写
	 * @return
	 */
	public boolean right() {
		return false;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	boolean isStopMove = false;
	float x1,y1,x2,y2;
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		  //继承了Activity的onTouchEvent方法，直接监听点击事件  
        if(event.getAction() == MotionEvent.ACTION_DOWN) {  
            //当手指按下的时候  
            x1 = event.getX();  
            y1 = event.getY();  
            isStopMove = false;
        }  
//        isStopMove = false;
        if(event.getAction() != MotionEvent.ACTION_MOVE){
        	 x2 = event.getX();  
             y2 = event.getY();  
        	isStopMove = true;
        }
	    if(isStopMove) {  
//	    	//Log.i("FRJ","x1 = " + x1 + "  x2 = " + x2);
//	    	//Log.i("FRJ","y1 = " + y1 + "  y2 = " + y2);
//	    	//Log.i("FRJ","x1 - x2 = " + (x1-x2));
//	    	//Log.i("FRJ","y1- y2 = " +(y1- y2));
	       if(Math.abs(y1-y2) < distance_Y){
				 //当手指离开的时候  
				if(x1 - x2 > distance_X) {  
				        left();
				 } else if(x2 - x1 > distance_X) {  
				      right();
				 }  
	        }
        }
		gestureDetector.onTouchEvent(event);
		return false;
	}
}


