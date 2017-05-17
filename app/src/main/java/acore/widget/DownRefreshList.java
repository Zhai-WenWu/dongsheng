package acore.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import xh.basic.tool.UtilLog;

/**
 * 带有下拉刷新的listview
 * 外部重新设置onscroll时需设定 firstItemIndex
 * 加载完毕后需调用onRefreshComplete()
 * @author Jerry
 *
 */
@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
public class DownRefreshList extends ListView implements OnScrollListener {

	public int firstItemIndex;		//外部重新设置onscroll时需设定此值。列表中首行索引，用来记录其与头部距离
	public String smallText="最近更新:";
	public String bigDownText="下拉刷新";
	public String bigReleaseText="松开刷新";
	public int paddingBottom=Tools.getDimen(this.getContext(), R.dimen.dp_10);
	/**
	 *下拉到可以松开刷新啦
	 */
	private final static int RELEASE_To_REFRESH = 0;
	/**
	 *继续下拉直到可以松开刷新
	 */
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;

	private LinearLayout headView; // 头部
	private TextView tipsTextview;// 下拉刷新
	private TextView lastUpdatedTextView;// 最新更新
	private ImageView arrowImageView;// 箭头
	private ImageView progressBar;// 刷新进度条  ------已由罗明改动.现在是一个imageView

	private RotateAnimation animation;// 旋转特效 刷新中箭头翻转 向下变向上
	private RotateAnimation reverseAnimation;
	
	private LinearLayout linear_text;//显示文字信息的布局框
	public FrameLayout framelayout_refresh;//显示刷新的图片的布局框
	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;

	private int headContentHeight;// 头部高度
	private int loadContentHeight = 30;// 头部高度

	private int startY;// 高度起始位置，用来记录与头部距离

	private int state;// 下拉刷新中、松开刷新中、正在刷新中、完成刷新

	private boolean isBack;
	private OnRefreshListener refreshListener;// 刷新监听
	private Animation anim;//旋转动画
	
	public DownRefreshList(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCacheColorHint(Color.parseColor("#00000000"));
		init(context);
	}
	
	private void init(Context context) {
		loadContentHeight = ToolsDevice.dp2px(context, 40);
		LayoutInflater inflater = LayoutInflater.from(context);
		//动画样式
		anim = AnimationUtils.loadAnimation(context, R.anim.feekback_progress_anim);
		headView = (LinearLayout) inflater.inflate(R.layout.c_widget_down_refresh, null);// listview拼接headview
		//显示刷新文字的
		linear_text = (LinearLayout) headView.findViewById(R.id.linear_text);
		//显示刷新图片的
		framelayout_refresh = (FrameLayout) headView.findViewById(R.id.framelayout_refresh);
		
		arrowImageView = (ImageView) headView.findViewById(R.id.head_arrowImageView);// headview中箭头view
		progressBar = (ImageView) headView.findViewById(R.id.head_progressBar);// headview中各view
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);// headview中各view
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);// headview中各view
		//TODO
		headView.setPadding(0, -1 * headContentHeight, 0, paddingBottom);// setPadding(int
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();// 头部高度
		headView.invalidate();// Invalidate the whole view

//		Log.v("test", "width:" + headContentWidth + " height:"+ headContentHeight);

		addHeaderView(headView);// 添加进headview
		setOnScrollListener(this);// 滚动监听

		animation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);// 特效animation设置

		reverseAnimation = new RotateAnimation(-180, 0,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);// 特效reverseAnimation设置
		onRefreshComplete();
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2,// 滚动事件
			int arg3) {
		firstItemIndex = firstVisiableItem;// 得到首item索引
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 手按下 对应下拉刷新状态
			if (firstItemIndex == 0 && !isRecored) {// 如果首item索引为0，且尚未记录startY,则在下拉时记录之，并执行isRecored
													// = true;
				startY = (int) event.getY();
				isRecored = true;

			}
//			Log.i("FRJ", "在down时候记录当前位置‘");
			break;
		case MotionEvent.ACTION_UP:// 手松开 对应松开刷新状态

			if (state != REFRESHING) {// 手松开有4个状态：下拉刷新、松开刷新、正在刷新、完成刷新。如果当前不是正在刷新
				if (state == DONE) {// 如果当前是完成刷新，什么都不做
				}
				if (state == PULL_To_REFRESH) {// 如果当前是下拉刷新，状态设为完成刷新（意即下拉刷新中就松开了，实际未完成刷新），执行changeHeaderViewByState()
					state = DONE;
					changeHeaderViewByState();

					//Log.v(TAG, "由下拉刷新状态，到done状态");
				}
				if (state == RELEASE_To_REFRESH) {// 如果当前是松开刷新，状态设为正在刷新（意即松开刷新中松开手，才是真正地刷新），执行changeHeaderViewByState()
					state = REFRESHING;
					changeHeaderViewByState();
					onRefresh();// 真正刷新，所以执行onrefresh，执行后状态设为完成刷新

				}
			}

			isRecored = false;// 手松开，则无论怎样，可以重新记录startY,因为只要手松开就认为一次刷新已完成
			isBack = false;
//			Log.i("FRJ", "由松开刷新状态，到done状态");

			break;

		case MotionEvent.ACTION_MOVE:// 手拖动，拖动过程中不断地实时记录当前位置
//			Log.i("FRJ", "在move时候记录下位置");
			int tempY = (int) event.getY();
			if (!isRecored && firstItemIndex == 0) {// 如果首item索引为0，且尚未记录startY,则在拖动时记录之，并执行isRecored
													// = true;
				isRecored = true;
				startY = tempY;
			}
			if (state != REFRESHING && isRecored) {// 如果状态不是正在刷新，且已记录startY：tempY为拖动过程中一直在变的高度，startY为拖动起始高度
				// 可以松手去刷新了
				if (state == RELEASE_To_REFRESH) {// 如果状态是松开刷新
					// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
					if ((tempY - startY < loadContentHeight)// 如果实时高度大于起始高度，且两者之差小于头部高度，则状态设为下拉刷新
							&& (tempY - startY) > 0) {
						state = PULL_To_REFRESH;
						changeHeaderViewByState();

						//Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
					}
					// 一下子推到顶了
					else if (tempY - startY <= 0) {// 如果实时高度小于等于起始高度了，则说明到顶了，状态设为完成刷新
						state = DONE;
						changeHeaderViewByState();

						//Log.v(TAG, "由松开刷新状态转变到done状态");
					}
					// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
					else {// 如果当前拖动过程中既没有到下拉刷新的地步，也没有到完成刷新（到顶）的地步，则保持松开刷新状态
						// 不用进行特别的操作，只用更新paddingTop的值就行了
					}
				}
				// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
				if (state == PULL_To_REFRESH) {// 如果状态是下拉刷新
					// 下拉到可以进入RELEASE_TO_REFRESH的状态
					if (tempY - startY >= loadContentHeight) {// 如果实时高度与起始高度之差大于等于头部高度，则状态设为松开刷新
						state = RELEASE_To_REFRESH;
						isBack = true;
						changeHeaderViewByState();
						
						//Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
					}
					// 上推到顶了
					else if (tempY - startY <= 0) {// 如果实时高度小于等于起始高度了，则说明到顶了，状态设为完成刷新
						state = DONE;
						changeHeaderViewByState();
						
						//Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
					}
				}

				// done状态下
				if (state == DONE) {// 如果状态是完成刷新
					if (tempY - startY > 0) {// 如果实时高度大于起始高度了，则状态设为下拉刷新
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
					}
				}

				// 更新headView的size
				if (state == PULL_To_REFRESH) {// 如果状态是下拉刷新，更新headview的size ?
					headView.setPadding(0, -1 * headContentHeight+ (tempY - startY), 0, paddingBottom);
					headView.invalidate();
				}

				// 更新headView的paddingTop
				if (state == RELEASE_To_REFRESH) {// 如果状态是松开刷新，更新
													// headview的paddingtop ?
					if(tempY - startY<headContentHeight+50){
						headView.setPadding(0, tempY - startY - headContentHeight,0, paddingBottom);
						headView.invalidate();
					}
				}
			}
			break;
		}
		try{
			return super.onTouchEvent(event);
		}catch(Exception e){
			//没有彻底解决
			UtilLog.reportError("数据源被清空，但是界面没有更新存在时间差", e);
			return false;
		}
	}

	/**
	 *  当状态改变时候，调用该方法，以更新界面
	 */
	private void changeHeaderViewByState() {
		//TODO 当状态改变时候，调用该方法，以更新界面
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			progressBar.clearAnimation();
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText(bigReleaseText);
			//Log.v(TAG, "当前状态，松开刷新");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			progressBar.clearAnimation();
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);
				tipsTextview.setText(bigDownText);
			} else {
				tipsTextview.setText(bigDownText);
			}
			//Log.v(TAG, "当前状态，下拉刷新");
			break;

		case REFRESHING:
			headView.setPadding(0, 0, 0, paddingBottom);
			headView.invalidate();

			progressBar.setVisibility(View.VISIBLE);;
			progressBar.startAnimation(anim);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("正在加载...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			//Log.v(TAG, "当前状态,正在刷新...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, paddingBottom);
			headView.invalidate();

			progressBar.setVisibility(View.GONE);
			progressBar.clearAnimation();
			
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.z_ico_refresh);
			tipsTextview.setText(bigDownText);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			//Log.v(TAG, "当前状态，done");
			break;
		}
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	@SuppressLint("SimpleDateFormat")
	public void onRefreshComplete() {
		state = DONE;
		if(smallText.length()>0){
			SimpleDateFormat df=new SimpleDateFormat("HH:mm");
			lastUpdatedTextView.setText(smallText + df.format(new Date()));// 刷新完成时，头部提醒的刷新日期
		}
		changeHeaderViewByState();
	}

	@SuppressLint("SimpleDateFormat")
	public void onRefreshStart() {
		state = REFRESHING;
		changeHeaderViewByState();
	}
	public void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	// 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
	/**
	 * 此方法为让外界调用后隐藏加载是的图片,显示文字信息
	 */
	public void imageHide(){
		linear_text.setVisibility(View.VISIBLE);
		framelayout_refresh.setVisibility(View.GONE);
		headView.findViewById(R.id.iv_hint).setVisibility(View.GONE);
	}
	/**
	 * 此方法为让外界调用后只显示刷新的图片信息.隐藏文字.
	 */
	public void textHide(){
		framelayout_refresh.setVisibility(View.VISIBLE);
		linear_text.setVisibility(View.GONE);
		headView.findViewById(R.id.iv_hint).setVisibility(View.VISIBLE);
	}
	
	// 滑动距离及坐标  
	private float xDistance, yDistance, xLast, yLast;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {  
			case MotionEvent.ACTION_DOWN:  
				xDistance = yDistance = 0f;  
				xLast = ev.getX();  
				yLast = ev.getY();  
			break;  
			case MotionEvent.ACTION_MOVE:  
				final float curX = ev.getX();  
				final float curY = ev.getY();  
		
				xDistance += Math.abs(curX - xLast);  
				yDistance += Math.abs(curY - yLast);  
				xLast = curX;  
				yLast = curY;  
		
				if(xDistance > yDistance){  
					return false;  
				}  
			break;
		}	
		return super.onInterceptTouchEvent(ev);
	}
	
	public void setEmptyViewVisible(boolean isVisible){
		headView.findViewById(R.id.head_empty_view).setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}
}
