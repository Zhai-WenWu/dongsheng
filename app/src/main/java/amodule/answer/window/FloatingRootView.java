package amodule.answer.window;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/**
 * Created by sll on 2017/8/8.
 */

public class FloatingRootView extends RelativeLayout {

    private OnBackPressedListener mBackPressedListener;

    public FloatingRootView(Context context) {
        super(context);
    }

    public FloatingRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackPressedListener(OnBackPressedListener backPressedListener) {
        mBackPressedListener = backPressedListener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN && mBackPressedListener != null) {
            return mBackPressedListener.onBackPressed();
        }
        return super.dispatchKeyEvent(event);
    }

    public interface OnBackPressedListener{
        boolean onBackPressed();
    }
}
