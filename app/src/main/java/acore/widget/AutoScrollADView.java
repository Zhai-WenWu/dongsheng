package acore.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.tools.Tools;

/**
 * PackageName : acore.widget
 * Created by MrTrying on 2016/8/25 16:35.
 * E_mail : ztanzeyu@gmail.com
 */
public class AutoScrollADView extends LinearLayout {

	//控件高度
	private float mAdverHeight = 0f;
	//间隔时间
	private final int mGap = 7000;
	//动画间隔时间
	private final int mAnimDuration = 1000;
	private ADViewAdapter mAdapter;
	//显示的view
	private View mFirstView;
	private View mSecondView;
	//播放的下标
	private int mPosition;
	//线程的标识
	private boolean isStarted;

	private String stiaticID = "";

	public AutoScrollADView(Context context) {
		this(context, null);
	}

	public AutoScrollADView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AutoScrollADView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	/**
	 * 初始化属性
	 *
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		//设置为垂直方向
		setOrientation(VERTICAL);
	}

	public void setmAdverHeight(float height) {
		this.mAdverHeight = height;
	}

	/** 设置数据 */
	public void setAdapter(ADViewAdapter adapter) {
		this.mAdapter = adapter;
		setupAdapter();
	}

	/** 开启线程 */
	public void start() {
		if (!isStarted && mAdapter.getCount() > 1) {
			isStarted = true;
			postDelayed(mRunnable, mGap);//间隔mgap刷新一次UI
		}
	}

	/**
	 * 暂停滚动
	 */
	public void stop() {
		//移除handle更新
		removeCallbacks(mRunnable);
		//暂停线程
		isStarted = false;
	}

	/**
	 * 设置数据适配
	 */
	private void setupAdapter() {
		//移除所有view
		removeAllViews();
		//只有一条数据,不滚东
		if (mAdapter.getCount() == 1) {
			mFirstView = mAdapter.getView(this);
			mAdapter.setItem(mFirstView, (Map<String, String>) mAdapter.getItem(0));
			addView(mFirstView,LayoutParams.MATCH_PARENT, (int) mAdverHeight);
		} else {
			//多个数据
			mFirstView = mAdapter.getView(this);
			mSecondView = mAdapter.getView(this);
			mAdapter.setItem(mFirstView, (Map<String, String>) mAdapter.getItem(0));
			mAdapter.setItem(mSecondView, (Map<String, String>) mAdapter.getItem(1));
			//把2个添加到此控件里
			addView(mFirstView,LayoutParams.MATCH_PARENT, (int) mAdverHeight);
			addView(mSecondView,LayoutParams.MATCH_PARENT, (int) mAdverHeight);
			mPosition = 1;
			isStarted = false;
			performSwitch();
		}
	}


	/**
	 * 测量控件的宽高
	 *
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (LayoutParams.WRAP_CONTENT == getLayoutParams().height) {
			getLayoutParams().height = (int) mAdverHeight;
		} else {
			mAdverHeight = getHeight();
		}

		if (mFirstView != null) {
//			mFirstView.getLayoutParams().height = (int) mAdverHeight;
		}
		if (mSecondView != null) {
//			mSecondView.getLayoutParams().height = (int) mAdverHeight;
		}
	}

	/** 垂直滚蛋 */
	private void performSwitch() {
		//属性动画控制控件滚动，y轴方向移动
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(getChildAt(0), "translationY", getChildAt(0).getTranslationY() - mAdverHeight);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(getChildAt(1), "translationY", getChildAt(1).getTranslationY() - mAdverHeight);
		//动画集
		AnimatorSet set = new AnimatorSet();
		set.playTogether(animator1, animator2);//2个动画一起
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {//动画结束
				mFirstView.setTranslationY(0);
				mSecondView.setTranslationY(0);
				View removedView = getChildAt(0);//获得第一个子布局
				mPosition++;
				//移除前一个view
				removeView(removedView);
				//设置显示的布局
				mAdapter.setItem(removedView, (Map<String, String>) mAdapter.getItem(mPosition % mAdapter.getCount()));
				//添加下一个view
				addView(removedView);
			}
		});
		set.setDuration(mAnimDuration);//持续时间
		set.start();//开启动画
	}

	private AnimRunnable mRunnable = new AnimRunnable();

	private class AnimRunnable implements Runnable {
		@Override
		public void run() {
			performSwitch();
			postDelayed(this, mGap);
		}
	}

	/** 销毁View的时候调用 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		//停止滚动
		stop();
	}

	/**
	 * 屏幕 旋转
	 *
	 * @param newConfig
	 */
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public static class ADViewAdapter {
		private List<Map<String, String>> mDatas;

		public ADViewAdapter(List<Map<String, String>> datas) {
			this.mDatas = datas;
			if (mDatas == null || mDatas.size() == 0) {
				throw new RuntimeException("nothing to show");
			}
		}

		/**
		 * 获取数据的条数
		 *
		 * @return
		 */
		public int getCount() {
			return mDatas == null ? 0 : mDatas.size();
		}

		/**
		 * 获取摸个数据
		 *
		 * @param position
		 *
		 * @return
		 */
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		/**
		 * 获取条目布局
		 *
		 * @param parent
		 *
		 * @return
		 */
		public View getView(AutoScrollADView parent) {
			return LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_headerview_item_hot, null);
		}

		/**
		 * 条目数据适配
		 *
		 * @param view
		 * @param data
		 */
		public void setItem(final View view, final Map<String, String> data) {
			view.findViewById(R.id.bottom_line).setVisibility(View.GONE);
			TextView tv_item_style = (TextView) view.findViewById(R.id.tv_item_style);
			TextView tv_item_title = (TextView) view.findViewById(R.id.tv_item_title);
			TextView tv_item_num = (TextView) view.findViewById(R.id.tv_item_num);
			String style = data.get("style");
			if (!TextUtils.isEmpty(style)) {
				if (style.equals("3")) {
					tv_item_style.setText("置顶");
					String color = Tools.getColorStr(view.getContext(),R.color.comment_color);
					tv_item_style.setTextColor(Color.parseColor(color));
					tv_item_style.setBackgroundResource(R.drawable.round_red);
				} else if (style.equals("2")) {
					tv_item_style.setText("公告");
					tv_item_style.setTextColor(Color.parseColor("#00cc33"));
					tv_item_style.setBackgroundResource(R.drawable.round_blue);
				} else if (style.equals("4")) {
					tv_item_style.setText("活动");
					String color = Tools.getColorStr(view.getContext(),R.color.comment_color);
					tv_item_style.setTextColor(Color.parseColor(color));
					tv_item_style.setBackgroundResource(R.drawable.round_red);
				} else {
					tv_item_style.setText("公告");
					tv_item_style.setTextColor(Color.parseColor("#00cc33"));
					tv_item_style.setBackgroundResource(R.drawable.round_blue);
				}
			}
			tv_item_title.setText(data.get("title"));
			changeMiddleView(data, view);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mOnItemClickListener != null){
						mOnItemClickListener.onClick(view,data);
					}
				}
			});
			view.setTag(data.get("code"));
		}

		/**
		 * 改变中间view:只改变评论消息数据
		 *
		 * @param map
		 */
		public void changeMiddleView(Map<String, String> map, View view) {
			if (map.containsKey("commentNum") && !TextUtils.isEmpty(map.get("commentNum")) && Integer.parseInt(map.get("commentNum")) > 0) {
				view.findViewById(R.id.linear_item_num).setVisibility(View.VISIBLE);
				((TextView) view.findViewById(R.id.tv_item_num)).setText(map.get("commentNum"));
			} else {
				view.findViewById(R.id.linear_item_num).setVisibility(View.GONE);
			}
		}

		private OnItemClickListener mOnItemClickListener;
		public interface OnItemClickListener{
			public void onClick(View view,Map<String,String> data);
		}
		public void setOnItemClickListener(OnItemClickListener listener){
			this.mOnItemClickListener = listener;
		}
	}

	public String getStiaticID() {
		return stiaticID;
	}

	public void setStiaticID(String stiaticID) {
		this.stiaticID = stiaticID;
	}
}