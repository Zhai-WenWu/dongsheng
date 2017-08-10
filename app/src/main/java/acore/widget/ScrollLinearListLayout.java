package acore.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.ArrayList;

import acore.tools.Tools;

/**
 * 这是一个由Listview控制LinearLayout同步滑动的自定义RelativeLayout
 * 其中视图可以分为2层（从视觉角度看层数越大，越是在上方）
 * 	1.ListView层
 * 	2.假的header层
 * @author Eva
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScrollLinearListLayout extends RelativeLayout implements HeaderLinearLayout.OnSizeChangedListener {
	/**填充在ListView中的空白header，高度与mStickHeader保持同步*/
	private View placeHolderView;
	/**放置mListView的跟布局*/
	private FrameLayout rootFrameLayout;
	/**用于滑动的ListView*/
	private ListView mListView = null;
	/**与mListView同步滑动的假header*/
	private HeaderLinearLayout mStickHeader = null;
	/**
	 * 假header的高度
	 * 有可能是变化的
	 * */
	private int mStickHeaderHeight;
	/**可以缩放的Layout*/
	private ZoomLayout mZoomLayout;

	public ScrollLinearListLayout(Context context) {
		this(context, null, 0);
	}

	public ScrollLinearListLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollLinearListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context, attrs, defStyleAttr);
	}

	private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
		// add root
		rootFrameLayout = new FrameLayout(context);
		mListView = new ListView(context);
		mListView.setOverScrollMode(OVER_SCROLL_NEVER);
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setCacheColorHint(Color.parseColor("#00000000"));
		rootFrameLayout.addView(mListView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(rootFrameLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		// add header
		mStickHeader = new HeaderLinearLayout(context);
		mStickHeader.setOrientation(LinearLayout.VERTICAL);
		addView(mStickHeader, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

	}
	
	@Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
		 if (getChildCount() < 2) {
            super.addView(child, index, params);
        } else {
            if (mStickHeader.getChildCount() < 2) {
            	mStickHeader.addView(child, params);
                return;
            }
            if (rootFrameLayout.getChildCount() > 1) {
                throw new IllegalStateException("only can host 3 elements");
            }
            rootFrameLayout.addView(child, params);
        }
    }

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		//初始化空白的header
		placeHolderView = new View(getContext());
		mListView.addHeaderView(placeHolderView,null,false);
		//设置mListView属性
		mListView.setSelector(R.drawable.btn_nocolor);
		mListView.setBackgroundColor(getResources().getColor(R.color.common_bg));
		mListView.setDividerHeight(0);
		//设置mListView的OnScrollListener同步ListView的滑动
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override	
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(mOnScrollListenerArray != null){
					for(AbsListView.OnScrollListener listener : mOnScrollListenerArray){
						listener.onScrollStateChanged(view, scrollState);
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				//假header同步滑动的主要代码
				if(firstVisibleItem ==  0){
					View theView = mListView.getChildAt(0);
					if(theView != null ){
						mStickHeader.scrollTo(0, -theView.getTop());
					}
				}else{
					mStickHeader.scrollTo(0, 8000);
				}
				//用于外部添加的滑动listener同步
				if(mOnScrollListenerArray != null){
					for(AbsListView.OnScrollListener listener : mOnScrollListenerArray){
						listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
					}
				}
			}
		});
		//这是布局size改变是的listener
		mStickHeader.setOnSizeChangedListener(this);
	}

	@Override
	public void onHeaderSizeChanged(int w, int h, int oldw, int oldh) {
		//用于同步假header和ListView中空白header的高度
		mStickHeaderHeight = Tools.getMeasureHeight(mStickHeader);
		if (mStickHeaderHeight != 0 && placeHolderView != null) {
			ViewGroup.LayoutParams params = placeHolderView.getLayoutParams();
			if (params != null) {
				params.height = mStickHeaderHeight;
				placeHolderView.setLayoutParams(params);
			}
		}
	}
	
	public float mDownX = 0;
	public float mDownY = 0;
	float moveDistanceX;
	float moveDistanceY;
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(mOnTouchListener != null){
			mOnTouchListener.onTouch(this, event);
		}
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mDownY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float x_move = event.getRawX();
			float y_move = event.getRawY();
			moveDistanceX = (int) (x_move - mDownX);
			moveDistanceY = (int) (y_move - mDownY);
			break;
		case MotionEvent.ACTION_UP:
			//判断是否可以算点击事件
			isClick = !(Math.abs(event.getY() - mDownY) >= 40 
				|| Math.abs(event.getX() - mDownX) >= 40);
			ScrollLinearListLayout.isMoveX = false;
			ScrollLinearListLayout.isMoveY = false;
			break;
		default:
			break;
		}
		
		dispatchTouchEventSelf(event);
		return true;
	}

	private boolean dispatchTouchEventSelf(MotionEvent event) {
		//将事件分发给其他的childView
		if(mZoomLayout != null && mZoomLayout.getHeaderHeight() > 0){
			if(!isMoveX && !isClick){
				mZoomLayout.dispatchTouchEvent(event);
			}
			//若处于缩放状态event不能下发给其他的layout
			if(mZoomLayout.isZoom()){
				return true;
			}
		}
		if(!isMoveX){
			rootFrameLayout.dispatchTouchEvent(event);
		}
		if(!isMoveY)
			mStickHeader.dispatchTouchEvent(event);
		return false;
	}
	
	public static boolean isMoveX = false;
	public static boolean isMoveY = false;
	/**是否可以点击*/
	public static boolean isClick = true;
	/**
	 * 该自定义Layout中的click事件需要通过该方法获取clickListener
	 * 处理是否是click事件
	 * */
	public static OnClickListener getOnClickListener(final OnClickListener listener){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isClick){
					listener.onClick(v);
				}
			}
		};
	}
	
	public ListView getListView(){
		return mListView;
	}

	public void setZoomLayout(ZoomLayout layout){
		this.mZoomLayout = layout;
	}
	
	public interface OnHeaderScrollListener{
		public void scrollTo(int x , int y);
	}
	
	/**
	 * 提供给外部添加的OnScrollListener集合
	 * 主要用于和listview的OnScrollListener
	 * */
	private ArrayList<AbsListView.OnScrollListener> mOnScrollListenerArray = new ArrayList<>();
	public void addOnScrollListener(AbsListView.OnScrollListener listener){
		mOnScrollListenerArray.add(listener);
	}
	
	private OnTouchListener mOnTouchListener;
	/** 仅仅用与次控件获得touch事件是使用 */
	public void setTouchListener(OnTouchListener listener){
		this.mOnTouchListener = listener;
	}
	
}
