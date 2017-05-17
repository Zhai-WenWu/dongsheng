/**
 * @author Jerry
 * 2014-12-10 上午10:31:01
 * Copyright: Copyright (c) xiangha.com 2014
 */
package acore.widget;

import java.util.ArrayList;

import xh.basic.tool.UtilLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class GalleryViewPager extends ViewPager{
	
	public int auto; // 1一直滑动，2暂时不滑动，0停止滑动
    private Thread thread;
    private int nowPosition;
    private boolean isMoveLoad = false;
    private boolean isJudgeXY = false;
    
	public GalleryViewPager(Context context) {
		super(context);
	}
	
	public GalleryViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//暂停、播放、停止自动播放
	public void stop(){ auto=0;}
	public void start(){ auto=1;}
	public void pause(){ auto=2;}
	
	/**
	 * 初始化轮播
	 * @param views		轮播的view
	 * @param time		轮播时间间隔 ,毫秒
	 * @param helper	轮播事件
	 */
	@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
	public void init(final ArrayList<? extends View> views,final int time,boolean isMove,final Helper helper,boolean isMainHome){
		nowPosition=1;
		AdapterViewPager adapterVP=new AdapterViewPager(views);
		this.setAdapter(adapterVP);
        this.setCurrentItem(nowPosition);
		if (thread == null && isMove && !isMoveLoad) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					GalleryViewPager.this.setCurrentItem(nowPosition+1,true);
				}
			};
			
			thread=new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							UtilLog.print("e", "轮转图错误");
						}
						switch (auto) {
						case 0:break;
						case 1:
							handler.sendEmptyMessage(0);
							break;
						case 2:
							auto = 1;
							break;
						}
					}
				}
			});
			isMoveLoad = true;
			thread.start();
		}
		// 手动滑动时暂停自动
		this.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(auto == 0){
					auto = 0;
				}else{
					auto = 2;
				}
				return false;
			}
		});
		//绑定事件
		this.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if ( views.size() > 1) { //多于1，才会循环跳转
					if ( position < 1) { //首位之前，跳转到末尾（N）
						final int positionItem = views.size()-2;
						//延时加载
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								GalleryViewPager.this.setCurrentItem(positionItem, false);
							}
						}, 300);
						return;
					} else if ( position > views.size()-2) { //末位之后，跳转到首位（1）
						final int positionItem = 1;
						//延时加载
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								GalleryViewPager.this.setCurrentItem(positionItem, false); //false:不显示跳转过程的动画
							}
						}, 300);
						return;
					}
				}
				
				nowPosition = position;
				if(helper != null){
					if(views.size()>1){
						if(position==0) {
							helper.onChange(views.get(views.size()-3) , views.size()-3);
						} else if(position == views.size() - 1) {
							helper.onChange(views.get(0) , 0);
						} else {
							helper.onChange(views.get(position - 1) , position-1);
						}
					} else {
						helper.onChange(views.get(0) , 0);
					}
				}
			}

			@Override public void onPageScrollStateChanged(int arg0) {}
			@Override public void onPageScrolled(int arg0, float arg1, int arg2) {}
		});
		if(isMainHome){
			//绑定点击事件
			for(int i = 0 ; i < views.size() ; i ++){
				final int index=i;
				views.get(i).setOnClickListener(ScrollLinearListLayout.getOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(views.size() > 1){
							if(index == 0) {
								helper.onClick(v , views.size()- 3);
							} else if(index == views.size() - 1){
								helper.onClick(v, 0);
							} else {
								helper.onClick(v, index-1);
							} 
						}
						else{
							helper.onClick(v,0);
						} 
					}
				}));
			}
		}else{
			//绑定点击事件
			for(int i = 0 ; i < views.size() ; i ++){
				final int index=i;
				views.get(i).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(views.size() > 1){
							if(index == 0) {
								helper.onClick(v , views.size()- 3);
							} else if(index == views.size() - 1){
								helper.onClick(v, 0);
							} else {
								helper.onClick(v, index-1);
							} 
						}
						else{
							helper.onClick(v,0);
						} 
					}
				});
			}
		}
	}
	public void init(final ArrayList<? extends View> views,final int time,boolean isMove,final Helper helper){
		init(views, time, isMove, helper,false);
	}
	
	float x_down;
	float y_down;
	float moveDistanceX;
	float moveDistanceY;
	final int mMovingMin = 100;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		try {
			if(isJudgeXY){
				switch (ev.getAction()) {
					case MotionEvent.ACTION_DOWN:
						x_down = ev.getRawX();
						y_down = ev.getRawY();
						break;
					case MotionEvent.ACTION_MOVE:
						float x_move = ev.getRawX();
						float y_move = ev.getRawY();
						moveDistanceX = (int) (x_move - x_down);
						moveDistanceY = (int) (y_move - y_down);
						// 判断是否在滑动
						if(Math.abs(moveDistanceY) > mMovingMin){
							ScrollLinearListLayout.isMoveY = true;
							ScrollLinearListLayout.isMoveX = false;
						}
						if (Math.abs(moveDistanceX) > mMovingMin) {
							ScrollLinearListLayout.isMoveX = true;
						}
						break;
					default:
						break;
				}
			}
			return super.dispatchTouchEvent(ev);
		}catch (Exception e){
			e.printStackTrace();
		}
		return  false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	public void setIsJudgeXY(boolean isJudgeXY){
		this.isJudgeXY = isJudgeXY;
	}
	
	private class AdapterViewPager extends PagerAdapter{

	    private ArrayList<? extends View> mViews;
	    public AdapterViewPager(ArrayList<? extends View> views){
	    	this.mViews=views;
	    }
	    
	    @Override
		public int getCount() {
			return mViews.size();
		}
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView(mViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mViews.get(position);
			container.addView(view);
			return view;
		}
	}
	
	public interface Helper {
		/**
		 * 点击响应
		 * @param 点中的view
		 * @param 点到的位置
		 */
		public void onClick(View view, int position);
		/**
		 * 切换响应
		 * @param 切换的view
		 * @param 切换到位置
		 */
		public void onChange(View view , int position);
	}
}
