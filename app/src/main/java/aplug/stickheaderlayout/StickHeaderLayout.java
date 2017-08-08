package aplug.stickheaderlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.xianghatest.R;

import java.util.ArrayList;

/**
 * Any problem about the library. Contact me
 * <p/>
 * https://github.com/w446108264/StickHeaderLayout
 * shengjun8486@gmail.com
 * <p/>
 * Created by sj on 15/11/22.
 */
@SuppressLint("NewApi")
public class StickHeaderLayout extends RelativeLayout implements ScrollHolder, HeaderLinearLayout.OnSizeChangedListener {
	public static final String TAG = "StickHeaderLayout";
	
    private int mScrollMinY = 10;
    private int mMovingMin = 30;

    private ViewGroup mViewGroup;
//    private FrameLayout rootFrameLayout;
    private HeaderScrollView headerScrollView;
    public HeaderLinearLayout mStickheader;
    private View placeHolderView;

    private View mScrollItemView;
    private int mScrollViewId;

    private int mStickHeaderHeight;
    private int mStickViewHeight;
    private int mMinHeaderTranslation;
    private boolean mIsHorizontalScrolling;
    private boolean mIsMoving;

    public View getStickHeaderView() {
        return mStickheader;
    }

    public StickHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StickHeaderLayout);
        if (typedArray != null) {
            mScrollViewId = typedArray.getResourceId(R.styleable.StickHeaderLayout_scrollViewId, mScrollViewId);
            typedArray.recycle();
        }

        // add root
//        rootFrameLayout = new FrameLayout(context);
//        addView(rootFrameLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // add header
        headerScrollView = new HeaderScrollView(context);
        headerScrollView.setFillViewport(true);
        mStickheader = new HeaderLinearLayout(context);
        mStickheader.setOrientation(LinearLayout.VERTICAL);
        headerScrollView.addView(mStickheader, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(headerScrollView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() < 1) {
            super.addView(child, index, params);
        } else {
            if (mStickheader.getChildCount() < 2) {
                mStickheader.addView(child, params);
            }else{
            	super.addView(child, 0,params);
            	mViewGroup = (ViewGroup) child;
            }
//            if (rootFrameLayout.getChildCount() > 1) {
//                throw new IllegalStateException("only can host 3 elements");
//            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mScrollItemView = mScrollViewId != 0 ? findViewById(mScrollViewId) : getChildAt(0);

        if (mScrollItemView instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) mScrollItemView;

            View contentView = scrollView.getChildAt(0);
            scrollView.removeView(contentView);

            LinearLayout childLayout = new LinearLayout(getContext());
            childLayout.setOrientation(LinearLayout.VERTICAL);

            placeHolderView = new View(getContext());
            childLayout.addView(placeHolderView, ViewGroup.LayoutParams.MATCH_PARENT, 0);
            childLayout.addView(contentView);
            scrollView.addView(childLayout);

            if (scrollView instanceof NotifyingListenerScrollView) {
                ((NotifyingListenerScrollView) scrollView).setOnScrollChangedListener(new NotifyingListenerScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                        onScrollViewScroll(who, l, t, oldl, oldt, 0);
                    }
                });
            }
        } else if (mScrollItemView instanceof ListView) {
            ListView listView = (ListView) mScrollItemView;

            placeHolderView = new View(getContext());
            listView.addHeaderView(placeHolderView);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    onListViewScroll(view, firstVisibleItem, visibleItemCount, totalItemCount, 0);
                }
            });
        } else if (mScrollItemView instanceof WebView) {
//            rootFrameLayout.removeView(mScrollItemView);
//            NestingWebViewScrollView scrollView = new NestingWebViewScrollView(getContext());
//            LinearLayout childLayout = new LinearLayout(getContext());
//            childLayout.setOrientation(LinearLayout.VERTICAL);
//            placeHolderView = new View(getContext());
//            childLayout.addView(placeHolderView, ViewGroup.LayoutParams.MATCH_PARENT, 0);
//            childLayout.addView(mScrollItemView);
//            scrollView.addView(childLayout);
//            rootFrameLayout.addView(scrollView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//            scrollView.setOnScrollChangedListener(new NotifyingListenerScrollView.OnScrollChangedListener() {
//                @Override
//                public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
//                    onScrollViewScroll(who, l, t, oldl, oldt, 0);
//                }
//            });
        }

        mStickheader.setOnSizeChangedListener(this);
    }

    private void updatePlaceHeight() {
        if (mStickHeaderHeight != 0 && mStickViewHeight != 0) {
            mMinHeaderTranslation = -mStickHeaderHeight + mStickViewHeight;

            if (placeHolderView != null) {
                ViewGroup.LayoutParams params = placeHolderView.getLayoutParams();
                if (params != null) {
                    params.height = mStickHeaderHeight;
                    placeHolderView.setLayoutParams(params);
                }
            }

            if (onPlaceHoderListenerListeners != null) {
                for(OnPlaceHoderListener listener : onPlaceHoderListenerListeners){
                    listener.onSizeChanged(mStickHeaderHeight, mMinHeaderTranslation);
                }
            }
        }
    }

    @Override
    public void onListViewScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
        scrollHeader(getListScrollY(view));
    }

    @Override
    public void onScrollViewScroll(ScrollView view, int x, int y, int oldX, int oldY, int pagePosition) {
        scrollHeader(view.getScrollY());
    }

    public void scrollHeader(int scrollY) {
        if (onPlaceHoderListenerListeners != null) {
            for(OnPlaceHoderListener listener : onPlaceHoderListenerListeners){
                listener.onScrollChanged(scrollY);
            }
        }
        float translationY = Math.max(-scrollY, mMinHeaderTranslation);
        headerTranslationY(translationY);
    }

    public void headerTranslationY(float translationY){
        mStickheader.setTranslationY(translationY);
        if (onPlaceHoderListenerListeners != null) {
            for(OnPlaceHoderListener listener : onPlaceHoderListenerListeners){
                listener.onHeaderTranslationY(translationY);
            }
        }
    }

    private int getListScrollY(AbsListView view) {
        View child = view.getChildAt(0);
        if (child == null) {
            return 0;
        }

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = child.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mStickHeaderHeight;
        }

        return -top + firstVisiblePosition * child.getHeight() + headerHeight;
    }

    float x_down;
    float y_down;
    float x_move;
    float y_move;
    float moveDistanceX;
    float moveDistanceY;
    float xCurrent = 0;
	float yCurrent = 0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if(mOnTouchListener != null){
    		mOnTouchListener.onTouch(this, ev);
    	}
    	View view = findViewById(R.id.circle_tab);
        int[] location = new int[2];
        if (view != null)
            view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsHorizontalScrolling = false;
                mIsMoving = false;
                x_down = ev.getRawX();
                y_down = ev.getRawY();
                xCurrent = ev.getX();
                yCurrent = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                x_move = ev.getRawX();
                y_move = ev.getRawY();
                moveDistanceX = (int) (x_move - x_down);
                moveDistanceY = (int) (y_move - y_down);
//                LogManager.print("i", "x=" + x + ";y=" + y);
//                LogManager.print("i", "x_down=" + x_down + ";y_down=" + y_down);
//                LogManager.print("i", "moveDistanceX=" + moveDistanceX + ";moveDistanceY=" + moveDistanceY);
                mIsHorizontalScrolling = !(Math.abs(moveDistanceY) > mScrollMinY && (Math.abs(moveDistanceY) > Math.abs(moveDistanceX)));
                //判断是否在滑动
                mIsMoving = Math.abs(moveDistanceY) > mMovingMin || Math.abs(moveDistanceX) > mMovingMin;
//                LogManager.print("i", "mIsMoving=" + mIsMoving);
//                LogManager.print("i", "mIsHorizontalScrolling=" + mIsHorizontalScrolling);
                break;
            default:
                break;
        }
//        Log.d(TAG, "dispatchTouchEvent y_down="+y_down+",y="+y);
        //判断滑动区域和横向滑动，若非ListView的可视部分则事件不再下发
        if(y_down <=  y){
        	if(!mIsHorizontalScrolling){
        		mViewGroup.dispatchTouchEvent(ev);
        	}
        }else if(view != null && y_down > y && y_down <= y + view.getHeight()){
        	if(!mIsMoving){
        		if(ev.getAction() == MotionEvent.ACTION_UP){
        			ev.setAction(MotionEvent.ACTION_CANCEL);
        			mScrollItemView.dispatchTouchEvent(ev);
        			ev.setAction(MotionEvent.ACTION_UP);
        		}else
        			mViewGroup.dispatchTouchEvent(ev);
        	}else if(!mIsHorizontalScrolling){
        		mViewGroup.dispatchTouchEvent(ev);
        	}
        }else{
        	mViewGroup.dispatchTouchEvent(ev);
        }
    	headerScrollView.dispatchTouchEvent(ev);
        return true;
    }

    public void setScrollMinY(int y) {
        mScrollMinY = y;
    }

    public boolean isHorizontalScrolling() {
        return mIsHorizontalScrolling;
    }

    ArrayList<OnPlaceHoderListener> onPlaceHoderListenerListeners = new ArrayList<>();

    public void addOnPlaceHoderListener(OnPlaceHoderListener onPlaceHoderListener) {
        if(onPlaceHoderListener != null){
            onPlaceHoderListenerListeners.add(onPlaceHoderListener);
        }
    }

    public interface OnPlaceHoderListener {
        void onSizeChanged(int headerHeight, int stickHeight);

        void onScrollChanged(int height);

        void onHeaderTranslationY(float translationY);
    }

    @Override
    public void onHeaderSizeChanged(int w, int h, int oldw, int oldh) {
        mStickHeaderHeight = mStickheader.getMeasuredHeight();
        mStickViewHeight = mStickheader.getChildAt(1).getMeasuredHeight();
        updatePlaceHeight();
    }
    
    private OnTouchListener mOnTouchListener;
    public void setOnTouchListener(OnTouchListener listener){
    	this.mOnTouchListener = listener;
    }
}
