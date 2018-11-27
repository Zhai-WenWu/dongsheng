package acore.logic.load;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import java.util.concurrent.atomic.AtomicBoolean;

import acore.tools.Tools;
import acore.widget.DownRefreshList;
import acore.widget.DownRefreshList.OnRefreshListener;
import acore.widget.LayoutScroll;
import acore.widget.ScrollLinearListLayout;
import acore.widget.rvlistview.RvListView;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

public class AutoLoadMore {

	// 设置GridView的自动加载更多
	public static void setAutoMoreListen (final GridView gridView, final Button loadMore, final View.OnClickListener clicker) {
		gridView.setOnScrollListener (new OnScrollListener () {
			int visibleLast = -1, firstVisible = 0;
			boolean alowLoad = true, lastPage = false;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				firstVisible = firstVisibleItem;
				visibleLast = firstVisibleItem + visibleItemCount;
			}

			@Override
			public void onScrollStateChanged (AbsListView view, int scrollState) {
				// 标记是否在顶部
				if (firstVisible > 0) {
					view.setTag ("notTop");
				} else {
					view.setTag ("atTop");
				}
				if (scrollState == SCROLL_STATE_IDLE && gridView.getAdapter () != null) {
					if (loadMore.getText ().equals ("没有更多咯") && !lastPage) {
						Tools.showToast (view.getContext (), "没有更多咯");
						lastPage = true;
						return;
					}
					if (view.getAdapter ().getCount () - 4 <= visibleLast && loadMore.isEnabled ()) {
						// 避免疯狂加载
						if (alowLoad) {
							alowLoad = false;
							Tools.showToast (view.getContext (), "正在加载...");
							clicker.onClick (loadMore);
							new Handler ().postDelayed (new Runnable () {
								@Override
								public void run () {
									alowLoad = true;
								}
							}, 500);
						}
					}
				}
			}
		});
	}

	/**
	 * 设置ListView的自动加载更多
	 *
	 * @param list：
	 * @param loadMore：
	 * @param clicker
	 */
	public static void setAutoMoreListen (final ListView list, final Button loadMore, final View.OnClickListener clicker) {
		if(list.getFooterViewsCount() > 0){
			list.removeFooterView(loadMore);
		}
		list.addFooterView (loadMore);
		list.setOnScrollListener(getListViewScrollListener(loadMore, clicker, true,null));
	}

	/**
	 * 设置ListView的自动加载更多
	 *
	 * @param list：
	 * @param loadMore：
	 * @param clicker
	 */
	public static void setAutoMoreListen (final ListView list, final Button loadMore, final View.OnClickListener clicker, final OnListScrollListener scrollListener) {
		if(list.getFooterViewsCount() > 0){
			list.removeFooterView(loadMore);
		}
		list.addFooterView (loadMore);
		list.setOnScrollListener(getListViewScrollListener(loadMore, clicker, true,null));
	}

	/**
	 * 适用于ScrollLinearListLayout的loadMore设置
	 *
	 * @param scrollLinearListLayout
	 * @param list
	 * @param loadMore
	 * @param clicker
	 */
	public static void setAutoMoreListen (ScrollLinearListLayout scrollLinearListLayout, final ListView list, final Button loadMore, final OnClickListener clicker) {
		if(list.getFooterViewsCount() > 0){
			list.removeFooterView(loadMore);
		}
		list.addFooterView (loadMore);
		scrollLinearListLayout.addOnScrollListener(getListViewScrollListener(loadMore, clicker, true,null));
	}

	@NonNull
	private static OnScrollListener getListViewScrollListener(Button loadMore, OnClickListener clicker, boolean isAuto,OnScrollListener onScrollListener) {
		return new OnScrollListener() {
			int previousVisibleFirst = -1;
			long previousTime=System.currentTimeMillis();
			int visibleLast = -1;
			int totalCount = 0;
			AtomicBoolean allow = new AtomicBoolean(true);
			int currentState;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(onScrollListener != null){
					onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				}
				visibleLast = firstVisibleItem + visibleItemCount;
				if (!allow.get()) {
					allow.set(totalCount != view.getAdapter().getCount());
				}
				if (allow.get()) {
					if (view.getAdapter() != null && view.getAdapter().getCount() - 4 <= visibleLast
							&& (currentState != SCROLL_STATE_IDLE || isAuto)
							&& (System.currentTimeMillis() - previousTime > 400)) {
						Log.i("tzy", "onScroll: " + (System.currentTimeMillis() - previousTime));
						allow.set(false);
						previousTime = System.currentTimeMillis();
						totalCount = view.getAdapter().getCount();
						if (clicker != null) {
							clicker.onClick(loadMore);
						}
					}
				}
				previousVisibleFirst = firstVisibleItem;
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if(onScrollListener != null){
					onScrollListener.onScrollStateChanged(arg0, scrollState);
				}
			}
		};
	}

	/**
	 * 设置ListView或DownRefreshList+标签浮动效果搭配的自动加载更多
	 * list上默认有一个headView
	 *
	 * @param list         litView
	 * @param scrollLayout 浮动显隐项，显示为invisible
	 * @param backLayout   底层linearLayout
	 * @param loadMore
	 * @param clicker      加载更多
	 * @param downClicker  下拉刷新，下拉后立刻自动调用onRefreshComplete
	 */
	public static void setAutoMoreListen (final ListView list, final LayoutScroll scrollLayout, final ViewGroup backLayout, final Button loadMore,
										  final View.OnClickListener clicker, final View.OnClickListener downClicker, final LoadManager.ViewScrollCallBack viewScrollCallBack) {
		if(list.getFooterViewsCount() > 0){
			list.removeFooterView(loadMore);
		}
		list.addFooterView (loadMore);
		//含有下拉刷新
		if (list instanceof DownRefreshList) {
			//设置下拉事件
			((DownRefreshList) list).setonRefreshListener (new OnRefreshListener () {
				@Override
				public void onRefresh () {
					downClicker.onClick (null);
					((DownRefreshList) list).onRefreshComplete ();
				}
			});
		}
		list.setOnScrollListener (new OnScrollListener () {
			int visibleLast = -1, preFirst = 0, preTop = 0, scrollHeight = 0, bottomHeight = 0;
			boolean alowLoad = true;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(viewScrollCallBack!=null)
					viewScrollCallBack.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
				visibleLast = firstVisibleItem + visibleItemCount + 4;
				if (list instanceof DownRefreshList) {
					((DownRefreshList) list).firstItemIndex = firstVisibleItem;
					firstVisibleItem--;
				}
				if (list.getChildAt (0) != null) {
					int top = - list.getChildAt (0).getTop ();
					//如果是DownRefreshList的第一个，为了保证滑动正常
					if (firstVisibleItem == -1) {
						top = 5000 - list.getChildAt (0).getBottom ();
					}
					if (preFirst != firstVisibleItem) {
						preTop = top;
					}
					//切换tab时让listView显示，则会掉起onScroll，识别并处理初始化
					if (list.getTag () != null) {
						preTop = top;
						preFirst = firstVisibleItem;
						scrollHeight = scrollLayout.getHeight ();
						bottomHeight = backLayout.getHeight ();
						if (list.getTag ().equals ("float")) {
							//如果足够向下滑动则保留浮动
							if (list.getChildCount () > (list instanceof DownRefreshList ? 5 : 4)) {
								//强制向下滑动并保留浮动框
								scrollLayout.animatScroll (0, 0, 0);
								if ((firstVisibleItem == 0 && top < bottomHeight - scrollHeight) || (firstVisibleItem == -1 && 5000 - top < bottomHeight - scrollHeight)) {
									backLayout.scrollTo (0, bottomHeight);
									preFirst = list instanceof DownRefreshList ? 1 : 0;
									new Handler ().post (new Runnable () {

										@Override
										public void run () {
											//滑动到刚显示浮动框的位置
											list.setSelection (list instanceof DownRefreshList ? 2 : 1);
										}
									});
								}
							} else {
								backLayout.scrollTo (0, 0);
								scrollLayout.animatScroll (0, scrollHeight, 0);
							}
						} else {
							final int height = Integer.parseInt (list.getTag ().toString ());
							backLayout.scrollTo (0, height);
							scrollLayout.animatScroll (0, scrollHeight, 0);
							new Handler ().post (new Runnable () {

								@Override
								public void run () {
									list.setSelectionFromTop (list instanceof DownRefreshList ? 1 : 0, -height);
								}
							});
						}
						list.setTag (null);
						return;
					}
					//同步滑动
					backLayout.scrollTo (0, top + firstVisibleItem * 5000);
					if (firstVisibleItem == 0) {
						//隐藏浮动框
						if (top < bottomHeight - scrollHeight && scrollHeight > 0) {
							scrollLayout.scroller.abortAnimation ();
							scrollLayout.setVisibility (View.VISIBLE);
							scrollLayout.animatScroll (0, scrollHeight, 0);
						}
					} else if (firstVisibleItem > 0 && (preTop != top || preFirst != firstVisibleItem)) {
						//先判断上拉，在判断下拉
						if (preFirst == firstVisibleItem) {
							if (preTop < top) {
								scrollLayout.animatScroll (0, scrollHeight, 500);
							} else if (preTop > top) {
								scrollLayout.animatScroll (0, 0, 500);
							}
						} else if (firstVisibleItem > preFirst) {
							scrollLayout.animatScroll (0, scrollHeight, 500);
						} else {
							scrollLayout.animatScroll (0, 0, 500);
						}
					}
					preTop = top;
					preFirst = firstVisibleItem;
				}
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if(viewScrollCallBack!=null)
					viewScrollCallBack.onScrollStateChanged(arg0,scrollState);
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					scrollHeight = scrollLayout.getHeight ();
					bottomHeight = backLayout.getHeight ();
				}
				if (scrollState == SCROLL_STATE_IDLE &&
						list.getAdapter () != null &&
						list.getAdapter ().getCount () - 4 <= visibleLast &&
						loadMore.isEnabled ()) {
					// 避免疯狂加载
					if (alowLoad) {
						alowLoad = false;
						clicker.onClick (loadMore);
						new Handler ().postDelayed (new Runnable () {
							@Override
							public void run () {
								alowLoad = true;
							}
						}, 500);
					}
				}
			}
		});
	}
	/**
	 * 设置ListView或DownRefreshList+标签浮动效果搭配的自动加载更多
	 * list上默认有一个headView
	 *
	 * @param list         litView
	 * @param scrollLayout 浮动显隐项，显示为invisible
	 * @param backLayout   底层linearLayout
	 * @param loadMore
	 * @param clicker      加载更多
	 * @param downClicker  下拉刷新，下拉后立刻自动调用onRefreshComplete
	 */
	public static void setAutoMoreListen (final ListView list, final LayoutScroll scrollLayout, final ViewGroup backLayout, final Button loadMore, final View.OnClickListener clicker, final View.OnClickListener downClicker) {
		setAutoMoreListen ( list,  scrollLayout, backLayout, loadMore,  clicker, downClicker,null);
	}

	// 设置DownRefreshList的自动加载更多
	public static void setAutoMoreListen (final DownRefreshList list, final Button loadMore, final View.OnClickListener clicker, final View.OnClickListener downClicker) {
		setAutoMoreListen ( list,  loadMore, clicker, downClicker,null);
	}

	// 设置DownRefreshList的自动加载更多-----带接口回调
	public static void setAutoMoreListen (final DownRefreshList list, final Button loadMore, final View.OnClickListener clicker, final View.OnClickListener downClicker, final LoadManager.ViewScrollCallBack viewScrollCallBack) {
		if (loadMore != null) {
			if(list.getFooterViewsCount() > 0){
				list.removeFooterView(loadMore);
			}
			list.addFooterView (loadMore);
		}
		//设置下拉事件
		list.setonRefreshListener (new OnRefreshListener () {
			@Override
			public void onRefresh () {
				downClicker.onClick (null);
			}
		});
		//设置加载更多
		list.setOnScrollListener (new OnScrollListener () {
			int visibleLast = -1;
			boolean alowLoad = true;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				list.firstItemIndex = firstVisibleItem;
				visibleLast = firstVisibleItem + visibleItemCount;
				if(viewScrollCallBack!=null)viewScrollCallBack.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if(viewScrollCallBack!=null)viewScrollCallBack.onScrollStateChanged(arg0,scrollState);
				if (loadMore != null && scrollState == SCROLL_STATE_IDLE &&
						list.getAdapter () != null &&
						list.getAdapter ().getCount () - 4 <= visibleLast &&
						loadMore.isEnabled ()) {
					// 避免疯狂加载
					if (alowLoad) {
						alowLoad = false;
						clicker.onClick (loadMore);
						new Handler ().postDelayed (new Runnable () {
							@Override
							public void run () {
								alowLoad = true;
							}
						}, 500);
					}
				}
			}
		});
	}

	public static void setAutoMoreListen(RvListView listView,final Button loadMore, final View.OnClickListener clicker){
		listView.addFooterView(loadMore);
		final RecyclerView.LayoutManager layoutManager = listView.getLayoutManager();
		final RecyclerView.Adapter adapter = listView.getAdapter();
		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			int visibleLast = -1;
			int totalCount;
			boolean allow = true;
			long previousTime = System.currentTimeMillis();
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
				if (mLayoutManager instanceof LinearLayoutManager) {
					visibleLast = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
				} else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
					int[] lastItemArr = ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null);
					if (lastItemArr.length > 0) {
						visibleLast = lastItemArr[lastItemArr.length - 1];
					}
				}
				if (!allow) {
					allow = totalCount != recyclerView.getAdapter().getItemCount();
				}
				if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() - 4 <= visibleLast
						&& allow &&(System.currentTimeMillis()-previousTime>500)) {
					previousTime = System.currentTimeMillis();
					allow = false;
					totalCount = recyclerView.getAdapter().getItemCount();
					if (clicker != null) {
						clicker.onClick(loadMore);
					}
				}
			}
		});
	}

	/** 用于在PlaceHoderHeaderLayout中同步listView的滑动接口 */
	public interface OnListScrollListener {
		public void onScrollStateChanged (AbsListView view, int scrollState);

		public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
	}

}
