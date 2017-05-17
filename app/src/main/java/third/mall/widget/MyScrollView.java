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
//				System.out.println("1::"+y+":::now_y::"+now_y);
				if(now_y<255){
					sv_interface.setYandState(y, state);
				}
				
			}else{//逐渐变淡
//				System.out.println("2::"+y+":::now_y::"+now_y);
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

	public interface ScrollViewInterface{
		public abstract void setYandState(float y,boolean state);
	}

	public int getNow_y() {
		return now_y;
	}
	public void setNow_y(int now_y) {
		this.now_y = now_y;
	}
	
}
