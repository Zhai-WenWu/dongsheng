package amodule.search.view;

import java.util.ArrayList;
import java.util.Map;

import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.adapter.AdapterFavoriteNous;
import android.test.suitebuilder.annotation.Suppress;
import android.view.View;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xiangha.R;

public class SearchFavoriteNouse extends SearchFavorite{
	
	public SearchFavoriteNouse(BaseActivity act){
		super(act);
	}
	
	@Override
	@Suppress
	public void initDataAdapter() {
		data_list.setVisibility(View.VISIBLE);
		
		adapter = new AdapterFavoriteNous(mAct,data_list, dishVideo,
				R.layout.a_nous_item, 
				new String[] { "img", "title", "content", "allClick" }, 
				new int[] { R.id.iv_nousCover, R.id.tv_nousTitle, R.id.tv_nousContent1, R.id.tv_allClick});
		((AdapterFavoriteNous)adapter).contentWidth = ToolsDevice.getWindowPx(mAct).widthPixels - Tools.getDimen(mAct, R.dimen.dp_120);
		
		getData();
	}
	
	private void initNouseNoDataAdapter() {
		data_list.setVisibility(View.GONE);
		scv_so.setVisibility(View.VISIBLE);
		setHearNoData(true);
		AdapterFavoriteNous adapter = new AdapterFavoriteNous(mAct,scv_so, dishVideo,
				R.layout.a_nous_item, 
				new String[] { "img", "title", "content", "allClick" }, 
				new int[] { R.id.iv_nousCover, R.id.tv_nousTitle, R.id.tv_nousContent1, R.id.tv_allClick });
		adapter.contentWidth = ToolsDevice.getWindowPx(mAct).widthPixels - Tools.getDimen(mAct, R.dimen.dp_120);//12=15*2+80+10

		//如果是离线则改变save等级
		SetDataView.ScrollView(scv_so,adapter, null, new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
			@Override
			public void click(int index, View v) {
				Map<String, String> map = dishVideo.get(index);
				AppCommon.openUrl(mAct, "nousInfo.app?code=" + map.get("code"), true);
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
//		String getUrl = "http://api.xiangha.com/home5/getUserData/?code=763423177&type=favNous&page=" + currentPage;
		String getUrl = StringManager.api_soFavorite + "type=zhishi&cusCode=" + LoginManager.userInfo.get("code") + "&page=" + currentPage + "&c=" + mSearchContent;
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
						Map<String, String> map2 = objInfo.get(i);
						map2.put("allClick", map2.get("allClick") + "浏览");
						dishVideo.add(map2);
					}
					
					if(loadCount == 0 && currentPage == 1){
						setHearNoData(false);
					}else{
						if(isHavso || currentPage != 1){
							data_list.setVisibility(View.VISIBLE);
							adapter.notifyDataSetChanged();
						}else{
							initNouseNoDataAdapter();
						}
					}
				}
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = mAct.loadManager.changeMoreBtn( flag, everyPage, loadCount, currentPage,dishVideo.size() == 0);
			}
		});
	}
	
}
