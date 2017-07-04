package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class HorizontalTab extends ViewGroup{

	public HorizontalTab(Context context) {
		this(context,null);
	}
	
	public HorizontalTab(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public HorizontalTab(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

}
