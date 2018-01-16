package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 可以滑动的linearlayout
 *
 * @author Jerry
 */
public class LayoutScroll extends LinearLayout implements OnGestureListener {

    public Scroller scroller = null;
    int scrollHeight = 0, flag = 0;
    public GestureDetector det = null;
    public boolean allow = true;

    public LayoutScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        this.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (det != null) det.onTouchEvent(event);
                return false;
            }
        });
    }

    /**
     * 初始化，设置滑动值和手势
     *
     * @param scrollHeight
     */
    public void init(int scrollHeight) {
        this.scrollHeight = scrollHeight;
        det = new GestureDetector(this.getContext(), this);
    }

    /**
     * 设置滑动手势监听的view，设定后当view滑动时，scroll也会有滑动响应
     *
     * @param view
     */
    public void setTouchView(View view) {
        view.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (det != null) det.onTouchEvent(event);
                return false;
            }
        });
    }

    public void animatScroll(int startX, int startY, int dx, int dy, int time) {
        if (scroller.isFinished() && allow) {
            scroller.startScroll(startX, startY, dx, dy, time);
            invalidate();
        }
    }

    public void animatScroll(int dx, int dy, int time) {
        if (scroller.isFinished()) {
            scroller.startScroll(scroller.getCurrX(), scroller.getCurrY(),
                    dx, dy - scroller.getCurrY(), time);
            invalidate();
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
//		Log.d("d","onDown"+e.getY());
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
//		Log.d("d","onFling_"+velocityY);
        if (velocityY < 0 && scroller.getCurrY() == 0) {
            hide();
        } else if (velocityY > 0 && scroller.getCurrY() == scrollHeight) {
            show();
        }
        return false;
    }


    @Override
    public void onLongPress(MotionEvent e) {
        if (scroller.getCurrY() == 0) {
            hide();
        } else if (scroller.getCurrY() == scrollHeight) {
            show();
        }
//		Log.d("d","onLongPress"+e.getY());
    }

    public void hide() {
        animatScroll(0, 0, 0, scrollHeight, 1000);
    }

    public void show() {
        animatScroll(0, scrollHeight, 0, -scrollHeight, 1000);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e1 != null && e2 != null) {
//			Log.d("d","onScroll_"+flag+"_"+distanceY+":"+scroller.getCurrY()+":"+scrollHeight);
            //开始滑动
            if (flag >= 0 && distanceY > 10) {
                flag = 3;
                if (scroller.getCurrY() == 0) hide();
            } else if (flag <= 0 && distanceY < 0) {
                flag = -3;
                if (scroller.getCurrY() == scrollHeight) show();
            }
            if (flag < 0 && distanceY > 0) flag++;
            else if (flag > 0 && distanceY < 0) flag--;
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
//		Log.d("d","onShowPress"+e.getY());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//		Log.d("d","onSingleTapUp"+e.getY());
        return false;
    }

}
