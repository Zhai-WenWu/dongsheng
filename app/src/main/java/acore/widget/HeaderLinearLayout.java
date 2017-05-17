package acore.widget;

import acore.tools.LogManager;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 配合ScrollLinearListLayout使用的layout
 * 在ScrollLinearListLayout中是假header使用
 * @author Eva
 *
 */
public class HeaderLinearLayout extends LinearLayout {
	private OnSizeChangedListener mOnSizeChangedListener;

	public HeaderLinearLayout(Context context) {
		super(context);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, View.MeasureSpec.UNSPECIFIED);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
//		LogManager.print("i", "currentW=" + w + " ; currentH=" + h + " ; oldw=" + oldw + " ; oldh=" + oldh);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onHeaderSizeChanged(w, h, oldw, oldh);
		}
	}

	interface OnSizeChangedListener {
		public void onHeaderSizeChanged(int w, int h, int oldw, int oldh);
	}

	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}
}