package acore.widget.rvlistview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Description : //TODO
 * PackageName : acore.widget.rvlistview
 * Created on 2017/11/30 17:26.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class RvHorizatolListView extends RvListView {
    public RvHorizatolListView(Context context) {
        super(context);
    }

    public RvHorizatolListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RvHorizatolListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private  int lastX = -1;
    private  int lastY = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        int dealtX = 0;
        int dealtY = 0;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dealtX = 0;
                dealtY = 0;
                // 保证子View能够接收到Action_move事件
//                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                dealtX += Math.abs(x - lastX);
                dealtY += Math.abs(y - lastY);
//                Log.i("dispatchTouchEvent", "dealtX:=" + dealtX);
//                Log.i("dispatchTouchEvent", "dealtY:=" + dealtY);
                // 这里是够拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截
                if (dealtX >= dealtY) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {

                }
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;

        }
        return super.dispatchTouchEvent(ev);
    }

}
