package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollviewDish extends ScrollView {
	
	private onScrollViewChange mOsvc;

	public ScrollviewDish(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollviewDish(Context context) {
		super(context);
	}
	
	public void setonScrollViewChange(onScrollViewChange osvc){
		mOsvc = osvc;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOsvc != null) mOsvc.onScrollChanged(l, t, oldl, oldt);
	}
	
	
	public interface onScrollViewChange{
		public void onScrollChanged(int l, int t, int oldl, int oldt);
	}
	
}
