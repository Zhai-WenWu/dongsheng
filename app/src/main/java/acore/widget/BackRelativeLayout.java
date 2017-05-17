package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BackRelativeLayout extends RelativeLayout {

	public BackRelativeLayout(Context context) {
		super(context);
	}
	
	public BackRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public BackRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch(event.getKeyCode()){
		case KeyEvent.KEYCODE_BACK:
			if(mOnBackListener != null){
				mOnBackListener.onBack(BackRelativeLayout.this);
			}
			break;
		}
		return super.dispatchKeyEvent(event);
	}
	
	private OnBackListener mOnBackListener;
	public interface OnBackListener{
		public void onBack(View v);
	}
	public void setOnBackListener(OnBackListener listener){
		this.mOnBackListener = listener;
	}
	

}
