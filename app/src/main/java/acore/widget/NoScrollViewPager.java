package acore.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 不支持滚动的viewpager
 */
public class NoScrollViewPager extends ViewPager{

    private boolean noScrollView=false;
    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(noScrollView){
            return false;
        }else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(noScrollView){
            return false;
        }else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public void setNoScrollView(boolean noScrollView){
        this.noScrollView=noScrollView;
    }
    public boolean getNoScrollViewState(){
        return noScrollView;
    }

}
