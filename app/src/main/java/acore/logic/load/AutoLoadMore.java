package acore.logic.load;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import acore.tools.Tools;
import acore.widget.DownRefreshList;
import acore.widget.DownRefreshList.OnRefreshListener;
import acore.widget.LayoutScroll;
import acore.widget.ScrollLinearListLayout;
import acore.widget.rvlistview.RvBaseAdapter;
import acore.widget.rvlistview.RvListView;
import aplug.stickheaderlayout.PlaceHoderHeaderLayout;

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
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && gridView.getAdapter () != null) {
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
		list.setOnScrollListener (new OnScrollListener () {
			int visibleLast = -1;
			boolean alowLoad = true;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visibleLast = firstVisibleItem + visibleItemCount;
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && list.getAdapter () != null && list.getAdapter ().getCount () - 4 <= visibleLast && loadMore.isEnabled ()) {
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
		list.setOnScrollListener (new OnScrollListener () {
			int visibleLast = -1;
			boolean alowLoad = true;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visibleLast = firstVisibleItem + visibleItemCount;
				if (scrollListener != null) {
					scrollListener.onScroll (view, firstVisibleItem, visibleItemCount, totalItemCount);
				}
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && list.getAdapter () != null && list.getAdapter ().getCount () - 4 <= visibleLast && loadMore.isEnabled ()) {
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
				if (scrollListener != null) {
					scrollListener.onScrollStateChanged (arg0, scrollState);
				}
			}
		});
	}

	/**
	 * 适用于PlaceHoderHeaderLayout的loadMore设置
	 *
	 * @param placeHoderHeaderLayout
	 * @param list
	 * @param loadMore
	 * @param clicker
	 */
	public static void setAutoMoreListen (PlaceHoderHeaderLayout placeHoderHeaderLayout, final ListView list, final Button loadMore, final OnClickListener clicker) {
		if(list.getFooterViewsCount() > 0){
			list.removeFooterView(loadMore);
		}
		list.addFooterView (loadMore);
		placeHoderHeaderLayout.setOnListScrollListener (new OnListScrollListener () {
			int visibleLast = -1;
			boolean alowLoad = true;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visibleLast = firstVisibleItem + visibleItemCount;
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && list.getAdapter () != null && list.getAdapter ().getCount () - 4 <= visibleLast && loadMore.isEnabled ()) {
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
		scrollLinearListLayout.addOnScrollListener (new AbsListView.OnScrollListener () {
			int visibleLast = -1;
			boolean alowLoad = true;

			@Override
			public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visibleLast = firstVisibleItem + visibleItemCount;
			}

			@Override
			public void onScrollStateChanged (AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
						&& list.getAdapter () != null
						&& list.getAdapter ().getCount () - 4 <= visibleLast
						&& loadMore.isEnabled ()) {
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
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE &&
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
				if (loadMore != null && scrollState == OnScrollListener.SCROLL_STATE_IDLE &&
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
		final LinearLayoutManager layoutManager = (LinearLayoutManager)listView.getLayoutManager();
		final RecyclerView.Adapter adapter = listView.getAdapter();
		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			boolean isLoading = false;
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
				if (lastVisibleItemPosition + 1 >= adapter.getItemCount() - 4) {
					if (!isLoading) {
						isLoading = true;
//						clicker.onClick (loadMore);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								clicker.onClick (loadMore);
								isLoading = false;
							}
						}, 100);
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
