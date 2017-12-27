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
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class SearchFavoriteDish extends SearchFavorite{
	
	public SearchFavoriteDish(BaseActivity act){
		super(act);
	}
	
	@Override
	@Suppress
	public void initDataAdapter() {
		adapter = new AdapterSimple(data_list, dishVideo,
				R.layout.a_favorite_search_item_dish, 
				new String[] {/*"video",*/"name","burdens", "isFine", /*"isMakeImg",*/ "allClick", "favorites"},
				new int[] {/*R.id.tv_item_hasVideo,*/R.id.tv_itemDishName, R.id.tv_itemBurden,R.id.iv_itemIsFine, /*R.id.tv_item_make,*/ R.id.allclick,R.id.tv_collect });
		adapter.playImgWH = Tools.getDimen(mAct, R.dimen.dp_29);
		adapter.videoImgId = R.id.itemImg1;
		
		data_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Map<String, String> ingre = dishVideo.get(position - 1);
				Intent intent = new Intent(mAct, DetailDish.class);
				intent.putExtra("code", ingre.get("code"));
				intent.putExtra("name", ingre.get("name"));
				intent.putExtra("img", ingre.get("img"));
				mAct.startActivity(intent);
			}
		});
		
		getData();
	}
	
	private void initDishNoDataAdapter() {
		setHearNoData(true);
		adapter = new AdapterSimple(scv_so, dishVideo,
				R.layout.a_favorite_search_item_dish, 
				new String[] {/*"video",*/"name","burdens", "isFine", /*"isMakeImg",*/ "allClick", "favorites"},
				new int[] {/*R.id.tv_item_hasVideo,*/R.id.tv_itemDishName, R.id.tv_itemBurden,R.id.iv_itemIsFine, /*R.id.tv_item_make,*/ R.id.allclick,R.id.tv_collect });
		adapter.playImgWH = Tools.getDimen(mAct, R.dimen.dp_29);
		adapter.videoImgId = R.id.itemImg1;
		
		//如果是离线则改变save等级
		SetDataView.ScrollView(scv_so,adapter, null, new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
			@Override
			public void click(int index, View v) {
				Map<String, String> ingre = dishVideo.get(index);
				Intent intent = new Intent(mAct, DetailDish.class);
				intent.putExtra("code", ingre.get("code"));
				intent.putExtra("name", ingre.get("name"));
				intent.putExtra("img", ingre.get("img"));
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
		String getUrl = StringManager.api_soFavorite + "type=dish&cusCode=" + LoginManager.userInfo.get("code") + "&page=" + currentPage + "&c=" + mSearchContent;
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
					for (int i = 0; i < objInfo.size(); i++) {
						Map<String, String> map = objInfo.get(i);
						map.put("allClick", map.get("allClick") + "浏览");
						map.put("favorites", map.get("favorites") + "收藏");
						map.put("isMakeImg", map.get("isMakeImg").equals("2") ? "步骤图" : "hide");
						map.put("isFine", map.get("level").equals("3") ? "精" : "hide");
						if(!map.containsKey("hasVideo")){
							map.put("hasVideo", "1");
						}
						map.put("video", "2".equals(map.get("hasVideo")) ? "[视频]" : "hide");
						dishVideo.add(objInfo.get(i));
					}
					loadCount = objInfo.size();
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
