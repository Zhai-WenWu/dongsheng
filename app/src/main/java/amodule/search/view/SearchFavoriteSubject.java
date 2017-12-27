package amodule.search.view;

import android.content.Intent;
import android.test.suitebuilder.annotation.Suppress;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.ShowSubject;
import amodule.search.adapter.AdapterSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class SearchFavoriteSubject extends SearchFavorite{
	
	public SearchFavoriteSubject(BaseActivity act){
		super(act);
		searchType = "贴子";
	}
	
	@Override
	@Suppress
	public void initDataAdapter() {
		adapter = new AdapterSearch(data_list, dishVideo,
				R.layout.a_favorite_search_item_subject,
				new String[] { "title", "content", "nickName", "commentNum", "likeNum" }, 
				new int[] { R.id.quansearch_title,R.id.quansearch_content, R.id.quansearch_userName, 
					R.id.quansearch_ping, R.id.quansearch_zan });
		adapter.playImgWH = Tools.getDimen(mAct, R.dimen.dp_29);
		
		data_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Map<String, String> ingre = dishVideo.get(position - 1);
				Intent intent = new Intent(mAct, ShowSubject.class);
				intent.putExtra("code",ingre.get("code") );
				intent.putExtra("title", ingre.get("title"));
				mAct.startActivity(intent);
			}
		});
		
		getData();
	}
	
	private void initDishNoDataAdapter() {
		data_list.setVisibility(View.GONE);
		scv_so.setVisibility(View.VISIBLE);
		setHearNoData(true);
		adapter = new AdapterSearch(scv_so, dishVideo,
				R.layout.a_favorite_search_item_subject,
				new String[] { "title", "content", "nickName", "commentNum", "likeNum" }, 
				new int[] { R.id.quansearch_title,R.id.quansearch_content, R.id.quansearch_userName, 
					R.id.quansearch_ping, R.id.quansearch_zan });
		adapter.playImgWH = Tools.getDimen(mAct, R.dimen.dp_29);
		
		//如果是离线则改变save等级
		SetDataView.ScrollView(scv_so,adapter, null, new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
			@Override
			public void click(int index, View v) {
				Map<String, String> ingre = dishVideo.get(index);
				Intent intent = new Intent(mAct, DetailDish.class);
				intent.putExtra("code", ingre.get("code"));
				intent.putExtra("name", ingre.get("name"));
				mAct.startActivity(intent);
			}
		}});
	}
	
	/**
	 * 搜索菜谱
	 */
	@Override
	@Suppress
	public void getSoData(final boolean isForward){
		if(isForward){
			currentPage = 1;
		}else{
			currentPage++;
		}
		mAct.loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,dishVideo.size() == 0);
		String getUrl = StringManager.api_soFavorite + "type=subject&cusCode=" + LoginManager.userInfo.get("code") + "&page=" + currentPage + "&c=" + mSearchContent;
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				data_list.onRefreshComplete();
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(isForward){
						dishVideo.clear();
					}
					boolean isHavso;
					// 解析数据
					ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
					ArrayList<Map<String, String>> objInfo = UtilString.getListMapByJson(list.get(0).get("favso"));
					//搜索收藏的菜谱有内容
					if(objInfo.size() > 0){
						isHavso = true;
					}else{
						isHavso = false;
						objInfo = UtilString.getListMapByJson(list.get(0).get("recommend"));
					}
					
					loadCount = objInfo.size();
					for (int i = 0; i < objInfo.size(); i++) {
						Map<String, String> map = objInfo.get(i);
						map.put("commentNum", map.get("commentNum").equals("0") ? "" : map.get("commentNum") + "评论");
						map.put("likeNum", map.get("likeNum").equals("0") ? "" : "/" + map.get("likeNum") + "赞");
						map.put("content", map.get("content").equals("") ? "   " : map.get("content"));
						if(!map.containsKey("hasVideo")) map.put("hasVideo", "1");
						dishVideo.add(map);
					}
					if(loadCount == 0 && currentPage == 1){
						setHearNoData(false);
					}else{
						if(isHavso || currentPage != 1){
							data_list.setVisibility(View.VISIBLE);
							adapter.notifyDataSetChanged();
						}else{
							initDishNoDataAdapter();
						}
					}
				}
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = mAct.loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,dishVideo.size() == 0);
			}
		});
	}
}
