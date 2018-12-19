package amodule.lesson.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

public class VerticalViewPager extends ViewPager {

    private double transform;
    private int showPosition;
    public int scrollState;

    public int getShowPosition() {
        return showPosition;
    }

    public VerticalViewPager(Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScroller();
        setPageTransformer(true, new VerticalPageTransformer());
        setOverScrollMode(OVER_SCROLL_NEVER);
        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mOnScrollDistance != null)
                    mOnScrollDistance.scrollDistance(positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                showPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                scrollState = state;
                Log.i("zww", "state: " + state);
                if (mOnScrollDistance != null)
                    mOnScrollDistance.scrollEnd(state);
            }
        });
    }

    public void setScale(double scale) {
        transform = (1 - scale);
    }

    private class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {
            if ((position > -transform && position < 0) || position > (1 - transform)) {
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);
            } else if (position > 0 && position <= 1 - transform) {
                float yPosition = (float) ((1 - transform) * view.getHeight());
                view.setTranslationY(yPosition);
            } else if (position <= -transform) {
                float yPosition = (float) (-transform * view.getHeight());
                view.setTranslationY(yPosition);
            }
            view.setTranslationX(view.getWidth() * -position);
        }
    }

    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;
        ev.setLocation(newX, newY);
        return ev;
    }

    private int downX;
    private int downY;
    private int distanceX;
    private int distanceY;
    private boolean intercepted;
    private int evY;
    private int evX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getShowPosition() == 1 && mOnWebScrollTop != null && !mOnWebScrollTop.canScroll()) {
            return false;
        }
        super.onInterceptTouchEvent(swapXY(ev));
        swapXY(ev);
        evX = (int) ev.getX();
        evY = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = evX;
                downY = evY;
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                distanceX = Math.abs(evX - downX);
                distanceY = Math.abs(evY - downY);
                int distance = evY - downY;
                if (distanceY > distanceX && (getShowPosition() == 0 || (getShowPosition() == 1 && distance > 0))) {
                    return true;
                }
                break;
        }
        return intercepted;
    }

    private int eventY;
    private int eventX;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        eventY = (int) event.getY();
        eventX = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (Math.abs(evX - downX) > 0 || scrollState == 1) {
                    if (getShowPosition() == 0) {
                        setCurrentItem(1);
                    } else if (getShowPosition() == 1) {
                        setCurrentItem(0);
                    }
                }
                return true;
        }
        return super.onTouchEvent(swapXY(event));
    }

    private OnWebScrollTop mOnWebScrollTop;

    public void setWebScrollTop(OnWebScrollTop mOnWebScrollTop) {
        this.mOnWebScrollTop = mOnWebScrollTop;
    }

    private void setScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new ViewPagerScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ViewPagerScroller extends Scroller {
        public ViewPagerScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, 300);
        }
    }

    public interface OnWebScrollTop {
        boolean canScroll();
    }

    private OnScrollDistance mOnScrollDistance;

    public void setScrollDistance(OnScrollDistance onScrollDistance) {
        this.mOnScrollDistance = onScrollDistance;
    }

    public interface OnScrollDistance {
        void scrollDistance(float distance);

        void scrollEnd(int state);
    }

}
