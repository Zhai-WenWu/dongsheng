package amodule.lesson.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;


public class OverWriteScrollView extends ScrollView {
    private OnScrollChangedListener onScrollChangedListener = null;

    public OverWriteScrollView(Context context) {
        super(context);
    }

    public OverWriteScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OverWriteScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(OverWriteScrollView scrollView, int x, int y, int oldx, int oldy);
    }
}
