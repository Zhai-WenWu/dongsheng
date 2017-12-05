package acore.logic.load;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.tencent.android.tpush.XGPushConfig;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import acore.widget.LayoutScroll;
import acore.widget.ScrollLinearListLayout;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import amodule.answer.view.UploadingView;
import amodule.quan.adapter.AdapterCircle;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import aplug.stickheaderlayout.PlaceHoderHeaderLayout;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import cn.srain.cube.views.ptr.PtrDefaultHandler;
import cn.srain.cube.views.ptr.PtrFrameLayout;
import third.push.xg.XGPushServer;

@SuppressLint("InflateParams")
public class LoadManager {
	public Context mContext;
	/** progress管理类 */
	public LoadProgressManager mLoadProgress;
	public LoadMoreManager mLoadMore;
	public DialogManager mProgressDialog = null;
	public static String tok = "";
	public static int FOOTTIME_PAGE = -2;//特殊的值，用于标示时间戳翻页。

	private String[] mLoadMoreTextArray = {
			"— 吃,也是一种艺术 —",
							/*"------------ 爱生活，尽在香哈新煮意 ------------",
							"------------ 爱生活，爱香哈 ------------",
							" ------------ 香哈，让生活更美好 ------------"*/};

	public LoadManager(Context context, RelativeLayout layout) {
		mContext = context;
		mLoadProgress = new LoadProgressManager(context, layout);
		mLoadMore = new LoadMoreManager(context);
	}

	/**
	 * 设置页面加载、重载等按钮，并开始重载
	 *
	 * @param clicker：回调
	 */
	public void setLoading(final View.OnClickListener clicker) {
		setLoading(clicker, true);
	}

	/**
	 * 设置页面加载、重载等按钮，并开始重载
	 *
	 * @param clicker：回调
	 */
	public void setLoading(final View.OnClickListener clicker, boolean isBlanker) {
		if (isBlanker) showProgressBar();
		mLoadProgress.setFailClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLoadFaildBar();
				showProgressBar();
				clicker.onClick(v);
			}
		});
		if (LoadManager.tok != null && LoadManager.tok.length() != 0) {
			clicker.onClick(null);
		} else {
			// 长时间未使用情况下，重新获取tok
			AppCommon.getCommonData(new InternetCallback(XHApplication.in()) {
				@Override
				public void loaded(int flag, String url, Object returnObj) {
					LogManager.print("d", "重新获取tok____" + LoadManager.tok);
					clicker.onClick(null);
				}
			});
		}
	}

	/**
	 * 确保有token
	 *
	 * @param callback 加载事件
	 */
	public void setLoading(final String name, final InternetCallback callback) {
		if (XGPushConfig.getToken(mContext).length() >= 40) {
			callback.loaded(ReqInternet.REQ_OK_STRING, "", null);
		} else if (FileManager.loadShared(mContext, FileManager.xmlFile_appInfo, FileManager.xmlKey_XGToken).toString().length() >= 40) {
			callback.loaded(ReqInternet.REQ_OK_STRING, "", null);
		} else {
			String errCode = "";
			XGPushServer pushServer = new XGPushServer(mContext);
			if (LoginManager.userInfo.containsKey("code") && !LoginManager.userInfo.get("code").equals(""))
				errCode = pushServer.initPush(LoginManager.userInfo.get("code"));
			else
				errCode = pushServer.initPush();
			if (errCode.equals("")) {
				callback.loaded(ReqInternet.REQ_OK_STRING, "", null);
			} else {
				LogManager.reportError("信鸽注册_errCode_" + errCode, null);
				Tools.showToast(mContext, "请打开网络连接，再重试~");
				mLoadProgress.setFailClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideLoadFaildBar();
						setLoading(name, callback);
					}
				});
			}
		}
	}

	/**
	 * 设置页面加载、重载等按钮，并开始重载
	 *
	 * @param list    list对象
	 * @param adapter 数据adapter，如果list已有adapter则忽略
	 * @param hasMore 是否加载更多页
	 * @param clicker 加载事件
	 */
	public void setLoading(Object list, ListAdapter adapter, boolean hasMore, final View.OnClickListener clicker) {
		if (list instanceof ListView) {
			ListView listView = (ListView) list;
			if (listView.getAdapter() == null) {
				if (hasMore) {
					Button loadMore = mLoadMore.newLoadMoreBtn(list, clicker);
					AutoLoadMore.setAutoMoreListen(listView, loadMore, clicker);
				}
				listView.setAdapter(adapter);
			}
		} else if (list instanceof GridView) {
			GridView gridView = (GridView) list;
			if (gridView.getAdapter() == null) {
				if (hasMore) {
					Button loadMore = mLoadMore.newLoadMoreBtn(list, clicker);
					AutoLoadMore.setAutoMoreListen(gridView, loadMore, clicker);
				}
				gridView.setAdapter(adapter);
			}
		}
		setLoading(clicker);
	}

	/**
	 * 设置页面加载、重载等按钮，并开始重载
	 *
	 * @param rvListView    list对象
	 * @param adapter 数据adapter，如果list已有adapter则忽略
	 * @param hasMore 是否加载更多页
	 * @param clicker 加载事件
	 */
	public void setLoading(@NonNull RvListView rvListView, @NonNull RvBaseAdapter adapter, @NonNull boolean hasMore, @NonNull View.OnClickListener clicker) {
		if(rvListView!=null && rvListView.getAdapter()==null){
			rvListView.setAdapter( adapter);
			if(hasMore){
				Button loadMore = mLoadMore.newLoadMoreBtn(rvListView, clicker);
				AutoLoadMore.setAutoMoreListen(rvListView, loadMore, clicker);
			}
		}
		setLoading(clicker);
	}

	public void setLoading(ListView list, ListAdapter adapter, boolean hasMore, final View.OnClickListener clicker, AutoLoadMore.OnListScrollListener listScrollListener) {
		if (list.getAdapter() == null) {
			if (hasMore) {
				Button loadMore = mLoadMore.newLoadMoreBtn(list, clicker);
				AutoLoadMore.setAutoMoreListen(list, loadMore, clicker,listScrollListener);
			}
			list.setAdapter(adapter);
		}
		setLoading(clicker);
	}

	/**
	 * 设置下拉加载的页面加载、重载等按钮，并开始重载
	 *
	 * @param list        下拉list对象
	 * @param adapter     数据adapter，如果list已有adapter则忽略
	 * @param hasMore     是否加载更多页
	 * @param clicker     加载事件
	 * @param downClicker 下拉加载事件
	 */
	public void setLoading(DownRefreshList list, ListAdapter adapter, boolean hasMore, View.OnClickListener clicker,
	                       View.OnClickListener downClicker) {
		if (list.getAdapter() == null) {
			Button loadMore = null;
			if (hasMore) {
				loadMore = mLoadMore.newLoadMoreBtn(list, clicker);
			}
			AutoLoadMore.setAutoMoreListen(list, loadMore, clicker, downClicker);
			list.setAdapter(adapter);
		}
		setLoading(clicker);
	}
	/**
	 * 设置下拉加载的页面加载、重载等按钮，并开始重载
	 *
	 * @param list        下拉list对象
	 * @param adapter     数据adapter，如果list已有adapter则忽略
	 * @param hasMore     是否加载更多页
	 * @param clicker     加载事件
	 * @param downClicker 下拉加载事件
	 * @param viewScrollCallBack view滚动事件回调
	 */
	public void setLoading(DownRefreshList list, ListAdapter adapter, boolean hasMore, View.OnClickListener clicker,
						   View.OnClickListener downClicker,ViewScrollCallBack viewScrollCallBack) {
		if (list.getAdapter() == null) {
			Button loadMore = null;
			if (hasMore) {
				loadMore = mLoadMore.newLoadMoreBtn(list, clicker);
			}
			AutoLoadMore.setAutoMoreListen(list, loadMore, clicker, downClicker,viewScrollCallBack);
			list.setAdapter(adapter);
		}
		setLoading(clicker);
	}

	/**
	 * 设置ListView或DownRefreshList+标签浮动效果搭配的页面加载、重载等按钮，并开始重载
	 *
	 * @param listView
	 * @param adapter      数据adapter，如果list已有adapter则忽略
	 * @param bottomLayout 浮动显隐项，显示为invisible
	 * @param headView     listView的头部，透明部分高度等于浮动项
	 * @param clicker      加载更多
	 */
	public void setLoading(ListView listView, ListAdapter adapter, LayoutScroll scrollLayout, ViewGroup bottomLayout,
								   View headView, final View.OnClickListener clicker, final View.OnClickListener downClicker, boolean isBlanck) {

		setLoading(listView, adapter, scrollLayout,  bottomLayout,
				headView, clicker, downClicker,isBlanck,null);
	}

	/**
	 * 设置ListView或DownRefreshList+标签浮动效果搭配的页面加载、重载等按钮，并开始重载
	 *
	 * @param listView
	 * @param adapter      数据adapter，如果list已有adapter则忽略
	 * @param bottomLayout 浮动显隐项，显示为invisible
	 * @param headView     listView的头部，透明部分高度等于浮动项
	 * @param clicker      加载更多
	 */
	public void setLoading(ListView listView, ListAdapter adapter, LayoutScroll scrollLayout, ViewGroup bottomLayout,
						   View headView, final View.OnClickListener clicker, final View.OnClickListener downClicker,
						   boolean isBlanck,ViewScrollCallBack viewScrollCallBack) {
		if (listView.getAdapter() == null) {
			if (headView != null)
				listView.addHeaderView(headView, null, false);
			Button loadMore = mLoadMore.newLoadMoreBtn(listView, clicker);
			AutoLoadMore.setAutoMoreListen(listView, scrollLayout, bottomLayout, loadMore, clicker,
					downClicker,viewScrollCallBack);
			listView.setAdapter(adapter);
		}
		setLoading(clicker, isBlanck);
	}
	/**
	 * 专门提供于PlaceHoderHeaderLayout使用的加载
	 * 传入PlaceHoderHeaderLayout对象设置自动加载
	 *
	 * @param placeHoderHeaderLayout
	 * @param listView
	 * @param adapter
	 * @param hasMore
	 * @param clicker
	 */
	public void setLoading(PlaceHoderHeaderLayout placeHoderHeaderLayout, ListView listView,
								   AdapterCircle adapter, boolean hasMore, OnClickListener clicker) {
		if (listView.getAdapter() == null) {
			if (hasMore) {
				Button loadMore = mLoadMore.newLoadMoreBtn(listView, clicker);
				AutoLoadMore.setAutoMoreListen(placeHoderHeaderLayout, listView, loadMore, clicker);
			}
			listView.setAdapter(adapter);
		}
		setLoading(clicker);
	}

	/**
	 * 使用下拉刷新框架的加载
	 * @param refreshLayout
	 * @param listView
	 * @param adapter
	 * @param hasMore
	 * @param refreshListener
	 * @param loadMoreListener
	 */
	public void setLoading(PtrFrameLayout refreshLayout, ListView listView, BaseAdapter adapter,
								   boolean hasMore, final OnClickListener refreshListener, final OnClickListener loadMoreListener) {
		refreshLayout.setPtrHandler(new PtrDefaultHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				refreshListener.onClick(null);
			}
		});
		if (listView.getAdapter() == null) {
			if (hasMore) {
				Button loadMore = mLoadMore.newLoadMoreBtn(listView, loadMoreListener);
				AutoLoadMore.setAutoMoreListen(listView, loadMore, loadMoreListener);
			}
			listView.setAdapter(adapter);
		}
		setLoading(loadMoreListener);
	}

	/**
	 * 使用下拉刷新框架的加载
	 * @param refreshLayout
	 * @param listView
	 * @param adapter
	 * @param hasMore
	 * @param refreshListener
	 * @param loadMoreListener
	 */
	public void setLoading(PtrClassicFrameLayout refreshLayout, ListView listView, BaseAdapter adapter,
								   boolean hasMore, final OnClickListener refreshListener, final OnClickListener loadMoreListener ,
								   AutoLoadMore.OnListScrollListener scrollListener) {
		refreshLayout.setPtrHandler(new PtrDefaultHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				refreshListener.onClick(null);
			}
		});
		if (listView.getAdapter() == null) {
			listView.setAdapter(adapter);
			if (hasMore) {
				Button loadMore = mLoadMore.newLoadMoreBtn(listView, loadMoreListener);
				AutoLoadMore.setAutoMoreListen(listView, loadMore, loadMoreListener,scrollListener);

			}
		}
		setLoading(loadMoreListener);
	}

	public void setLoading(PtrClassicFrameLayout refreshLayout, RvListView listView, RvBaseAdapter adapter,
						   boolean hasMore,final OnClickListener refreshListener, final OnClickListener loadMoreListener){
		setLoading(refreshLayout, listView, adapter, hasMore, true, refreshListener, loadMoreListener);
	}

	public void setLoading(PtrClassicFrameLayout refreshLayout, RvListView listView, RvBaseAdapter adapter,
						   boolean hasMore, boolean showProgressbar, final OnClickListener refreshListener, final OnClickListener loadMoreListener){
		refreshLayout.setPtrHandler(new PtrDefaultHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				refreshListener.onClick(null);
			}
		});
		if (listView.getAdapter() == null) {
			listView.setAdapter(adapter);
			if (hasMore) {
				Button loadMore = mLoadMore.newLoadMoreBtn(listView, loadMoreListener);
				AutoLoadMore.setAutoMoreListen(listView, loadMore, loadMoreListener);
			}
		}
		setLoading(loadMoreListener, showProgressbar);
	}

	/**
	 * 专门提供于ScrollLinearListLayout使用的加载
	 * 传入ScrollLinearListLayout对象设置自动加载
	 *
	 * @param scrollLinearListLayout
	 * @param listView
	 * @param adapter
	 * @param hasMore
	 * @param clicker
	 */
	public void setLoading(ScrollLinearListLayout scrollLinearListLayout, ListView listView,
						   BaseAdapter adapter, boolean hasMore, OnClickListener clicker) {
		if (listView.getAdapter() == null) {
			if (hasMore) {
				Button loadMore = mLoadMore.newLoadMoreBtn(listView, clicker);
				AutoLoadMore.setAutoMoreListen(scrollLinearListLayout, listView, loadMore, clicker);
			}
			listView.setAdapter(adapter);
		}
		setLoading(clicker);
	}

	/**
	 * * 设置加载更多按钮的状态,一个页面若有一个个loadMare则无需传key，直接取就行
	 *
	 * @param flag
	 * @param everyPageNum ：每一页的行数
	 * @param actPageNum   ：当前下载的行数
	 * @param nowPage      ：现在页数
	 * @param isBlankSpace : 当前页面是否是在空白页的基础上加载的
	 *
	 * @return
	 *
	 * @return：现在的页数
	 */
	public int changeMoreBtn(int flag, int everyPageNum, int actPageNum, int nowPage, boolean isBlankSpace) {
		return changeMoreBtn(null, flag, everyPageNum, actPageNum, nowPage, isBlankSpace);
	}

	/**
	 * ,一个页面若有多个loadMare则需要传入对应的key拿到对应的loadMore
	 *
	 * @param key
	 * @param flag
	 * @param everyPageNum
	 * @param actPageNum
	 * @param nowPage
	 * @param isBlankSpace
	 *
	 * @return
	 */
	public int changeMoreBtn(Object key, int flag, int everyPageNum, int actPageNum, int nowPage, boolean isBlankSpace) {
		return changeMoreBtn(key,flag,everyPageNum,actPageNum,nowPage,isBlankSpace,mLoadMoreTextArray[0]);
	}

	/**
	 * ,一个页面若有多个loadMare则需要传入对应的key拿到对应的loadMore
	 *
	 * @param key
	 * @param flag
	 * @param everyPageNum
	 * @param actPageNum
	 * @param nowPage
	 * @param isBlankSpace
	 *
	 * @return
	 */
	public int changeMoreBtn(Object key, int flag, int everyPageNum, int actPageNum, int nowPage, boolean isBlankSpace,String nodataHint) {
		Button loadMoreBtn = getSingleLoadMore(key);
		if (loadMoreBtn == null) {
			return 0;
		}
		loadMoreBtn.setVisibility(View.VISIBLE);
		if (flag >= ReqInternet.REQ_OK_STRING) {
			if (isBlankSpace) {
				showProgressBar();
				hideLoadFaildBar();
			}
			// 激活加载更多时
			if (actPageNum == -1 && everyPageNum == -1) {
				if (nowPage <= 1)
					loadMoreBtn.setVisibility(View.GONE);
				else
					loadMoreBtn.setText("加载中...");
				loadMoreBtn.setEnabled(false);
				return nowPage;
			}
			// 加载完毕
			else if ((actPageNum > 0) ||
					(everyPageNum == LoadManager.FOOTTIME_PAGE && actPageNum > 0)) {
				loadMoreBtn.setText("点击加载更多");
				loadMoreBtn.setEnabled(true);
			} else {
				loadMoreBtn.setText(nodataHint);
				loadMoreBtn.setEnabled(false);
			}
			if (actPageNum <= 0 && nowPage == 1)
				loadMoreBtn.setVisibility(View.GONE);
		}
		return loadOver(key, flag, nowPage, isBlankSpace);
	}

	/**
	 * 加载完毕
	 *
	 * @param flag
	 * @param nowPage
	 * @param isBlankSpace ：当前页面是否是百页，也就是页面有没有数据
	 *
	 * @return
	 */
	public int loadOver(int flag, int nowPage, boolean isBlankSpace) {
		return loadOver(null, flag, nowPage, isBlankSpace);
	}

	public int loadOver(Object key, int flag, int nowPage, boolean isBlankSpace) {
		Button loadMoreBtn = getSingleLoadMore(key);
		if (flag >= ReqInternet.REQ_OK_STRING) {
			hideProgressBar();
			hideLoadFaildBar();
		}
		// 加载失败
		else if (nowPage == 1) {
			XHConf.net_timeout += 1000;
			if (loadMoreBtn != null)
				loadMoreBtn.setEnabled(true);
			if (isShowingProgressBar()) {
				hideProgressBar();
				if (isBlankSpace) {
					showLoadFaildBar();
				}
			}
			nowPage--;
		} else if (loadMoreBtn != null) {
			hideProgressBar();
			loadMoreBtn.setText("加载失败，点击重试");
			loadMoreBtn.setEnabled(true);
			nowPage--;
		}
		return nowPage;
	}

	public Button getSingleLoadMore(Object key) {
		Button loadMoreBtn = null;
		if (mLoadMore != null) {
			return mLoadMore.getLoadMoreBtn(key);
		}
		return loadMoreBtn;
	}

	//开启和关闭进度框
	public void startProgress(String title) {
		if (mContext != null) {
			if (mProgressDialog != null && mProgressDialog.isShowing())
				dismissProgress();
			mProgressDialog = new DialogManager(mContext);
			mProgressDialog.createDialog(new ViewManager(mProgressDialog).setView(new UploadingView(mContext).setText(title))).noPadding().show();
		}
	}

	public void dismissProgress() {
		if (mContext != null && mProgressDialog != null) {
			mProgressDialog.cancel();
			mProgressDialog = null;
		}
	}

	public void setFailClickListener(OnClickListener listener) {
		if (mLoadProgress != null) {
			mLoadProgress.setFailClickListener(listener);
		}
	}

	public boolean isShowingProgressBar() {
		return mLoadProgress != null && mLoadProgress.isShowingProgressBar();
	}

	public void showProgressBar() {
		if (mLoadProgress != null) {
			mLoadProgress.showProgressBar();
		}
	}

	public void hideProgressBar() {
		if (mLoadProgress != null) {
			mLoadProgress.hideProgressBar();
		}
	}

	public boolean isShowingLoadFaildBar() {
		return mLoadProgress != null && mLoadProgress.isShowingLoadFailBar();
	}

	public void showLoadFaildBar() {
		if (mLoadProgress != null) {
			mLoadProgress.showLoadFailBar();
		}
	}

	public void hideLoadFaildBar() {
		if (mLoadProgress != null) {
			mLoadProgress.hideLoadFailBar();
		}
	}

	public void showProgressShadow() {
		if (mLoadProgress != null) {
			mLoadProgress.showProgressShadow();
		}
	}

	public void setLoadFaildBarClick(final OnClickListener click) {
		mLoadProgress.setFailClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLoadFaildBar();
				click.onClick(v);
			}
		});
	}

	/**
	 * view onscroll事件滚动回调
	 */
	public interface ViewScrollCallBack{
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
		public void onScrollStateChanged(AbsListView arg0,int scrollState);
	}
}
