package amodule.user.view;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.main.Main;
import amodule.main.activity.MainCircle;
import amodule.quan.activity.ShowSubject;
import amodule.search.adapter.AdapterSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/** 
 * @Description:
 * @Title: FavoriteQuan.java Copyright: Copyright (c) xiangha.com 2014~2017
 * @author: luomin
 * @date: 2015年3月17日 下午2:29:08   
 */
public class FavoriteQuan {
	private DownRefreshList list_quan;
	private View view;
	
	private BaseActivity mAct;
	// 加载管理
	LoadManager loadManager = null;
	private AdapterSimple adapterImageNew;
	private ArrayList<Map<String, String>> listDataTabNew;
	
	public boolean LoadOver = false;
	private int currentPageTabNew = 0, everyPageTabNew = 0;
//	private ImageView viewquan_refresh_img;
	public FavoriteQuan(BaseActivity act) {
		super();
		this.mAct = act;
	}
	
	public FavoriteQuan(){
		super();
	}
	
	public View onCreateView() {
		view = LayoutInflater.from(mAct).inflate(R.layout.favorite_subject, null);
		loadManager = mAct.loadManager;
		currentPageTabNew = 0;
		everyPageTabNew = 0;
		LoadOver = false;
		return view;
	}
	
	public void onDestroy(){
		mAct = null;
		view = null;
		adapterImageNew = null;
		listDataTabNew.clear();
	}

	public void init() {
		LayoutParams lp = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		int margin = Tools.getDimen(mAct, R.dimen.dp_10);
		lp.setMargins(margin, margin, margin,Tools.getDimen(mAct, R.dimen.dp_3));
		view.findViewById(R.id.btn_goFavorite).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Main.allMain.setCurrentTabByClass(MainCircle.class);
				Main.colse_level = 1;
				mAct.finish();
			}
		});
		list_quan = (DownRefreshList) view.findViewById(R.id.quan_list);
		list_quan.setDivider(null);
		list_quan.paddingBottom = 0;
		listDataTabNew = new ArrayList<>();
		adapterImageNew = new AdapterSearch(list_quan, listDataTabNew, 
				R.layout.a_search_home_item_quan,
				new String[] { "title", "content", "nickName", "commentNum", "likeNum" }, 
				new int[] { R.id.quansearch_title,R.id.quansearch_content, R.id.quansearch_userName, 
					R.id.quansearch_ping, R.id.quansearch_zan });
		adapterImageNew.scaleType=ScaleType.CENTER_CROP;
		list_quan.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent2=new Intent(mAct,ShowSubject.class);
				intent2.putExtra("code",listDataTabNew.get(arg2 - 1).get("code") );
				intent2.putExtra("title", listDataTabNew.get(arg2 - 1).get("title"));
				mAct.startActivity(intent2);
			}
		});
		list_quan.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				final Map<String, String> map = listDataTabNew.get(arg2-1);
				final DialogManager dialogManager = new DialogManager(mAct);
				dialogManager.createDialog(new ViewManager(dialogManager)
						.setView(new TitleView(mAct).setText("取消收藏"))
						.setView(new MessageView(mAct).setText("确定要取消收藏?"))
						.setView(new HButtonView(mAct)
								.setNegativeText("取消", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
									}
								})
								.setPositiveText("确定", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
										doFavorite(map);
										listDataTabNew.remove(map);
										adapterImageNew.notifyDataSetChanged();
									}
								}))).show();
				return true;
			}
		});
		setLoad();
	}
	
	// 收藏响应
	private void doFavorite(final Map<String, String> map) {
//		AppCommon.onFavoriteClick(mAct,"subject", map.get("code"), new InternetCallback(mAct) {
//			@Override
//			public void loaded(int flag, String url, Object returnObj) {
//
//			}
//		});
	}

	//设置加载
	private void setLoad() {
		if (!LoadOver) {
			mAct.loadManager.showProgressBar();
			loadManager.setLoading(list_quan, adapterImageNew, true,
					new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							getTabNewData(false);
						}
					}, new OnClickListener() {
						@Override
						public void onClick(View v) {
							getTabNewData(true);
						}
					});
			LoadOver = true;
		}
	}

	public void loader() {
		getTabNewData(true);
	}

	/**
	 * 获取美食圈最新的tab选项数据
	 * @param isForward
	 */
	private void getTabNewData(final boolean isForward) {
		if (isForward) {
			currentPageTabNew = 1;
		} else
			currentPageTabNew++;

		String floorTime = "";
		// 如果第一次加载,floorTime="",其他加载依时间次序向下排队;
		if (listDataTabNew.size() > 0 && !isForward) {
				floorTime = listDataTabNew.get(listDataTabNew.size() - 1).get("floorTime");
		}
		String getUrl = StringManager.api_getUSerData + "?code="+ LoginManager.userInfo.get("code")+ "&type=favSubject&page=" + currentPageTabNew;
		loadManager.changeMoreBtn(list_quan,UtilInternet.REQ_OK_STRING, -1, -1, currentPageTabNew,listDataTabNew.size() == 0);
		ReqInternet.in().doGet(getUrl, new InternetCallback(mAct) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if (isForward) {
						listDataTabNew.clear();
					}
					// 解析数据,获取该次加载的数量;
					loadCount = praseInfo(returnObj.toString(), listDataTabNew);
					adapterImageNew.notifyDataSetChanged();
					// 如果是重新加载的,选中第一个tab.
					if (isForward)
						list_quan.setSelection(1);
//					viewquan_refresh_img.clearAnimation();
				}
				//判断有无数据
				if (listDataTabNew.size() == 0 && flag > 1) {
					view.findViewById(R.id.myself_favorite_noData).setVisibility(View.VISIBLE);
					list_quan.setVisibility(View.GONE);
				}else {
					view.findViewById(R.id.myself_favorite_noData).setVisibility(View.GONE);
					list_quan.setVisibility(View.VISIBLE);
				}
				if (everyPageTabNew == 0)
					everyPageTabNew = loadCount;
				currentPageTabNew = loadManager.changeMoreBtn(list_quan,flag,everyPageTabNew, loadCount, currentPageTabNew,listDataTabNew.size() == 0);
				list_quan.onRefreshComplete();
			}
		});
	}

	/**
	 * 解析最新和最热两个很相似的数据.
	 * @param jsonObj 返回的数据
	 * @return
	 */
	private int praseInfo(String jsonObj, ArrayList<Map<String, String>> listTag) {
		ArrayList<Map<String, String>> listMap = UtilString.getListMapByJson(jsonObj);
		ArrayList<Map<String, String>> objInfo = UtilString.getListMapByJson(listMap.get(0).get("obj"));
		int loadCount = 0;
		for (int i = 0; i < objInfo.size(); i++) {
			loadCount++;
			Map<String, String> map = objInfo.get(i);
			map.put("commentNum", map.get("commentNum").equals("0") ? "" : map.get("commentNum") + "评论");
			map.put("likeNum", map.get("likeNum").equals("0") ? "" : "/" + map.get("likeNum") + "赞");
			map.put("content", map.get("content").equals("") ? "   " : map.get("content"));
			if(!map.containsKey("hasVideo")) map.put("hasVideo", "1");
			listTag.add(map);
		}
		return loadCount;
	}

}
