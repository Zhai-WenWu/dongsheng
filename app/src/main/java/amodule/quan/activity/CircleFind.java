package amodule.quan.activity;

import java.util.ArrayList;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.widget.DownRefreshList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.feedback.activity.Feedback;

import com.xiangha.R;
/**
 * 发现圈子
 * @author FangRuijiao
 */
public class CircleFind extends BaseActivity{
	
	private DownRefreshList mLvSur;
	private AdapterSimple adapter;
	private ArrayList<Map<String, String>> listData;
	private View mCircleFootView;
	
	private int currentPage = 0, everyPage = 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("发现圈子", 2, 0, R.layout.c_view_bar_title, R.layout.a_circle_find);
		init();
	}
	
	private void init(){
		mLvSur = (DownRefreshList)findViewById(R.id.a_circle_find_list);
		listData = new ArrayList<>();
		adapter = new AdapterSimple(mLvSur, listData, R.layout.a_circle_find_item,
				new String[]{"name","info","img"},
				new int[]{ R.id.a_circle_find_item_title,R.id.a_circle_find_item_content,R.id.a_circle_find_item_iv});
		adapter.imgResource=R.drawable.bg_round_zannum;
		loadManager.setLoading(mLvSur, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getCircelData(false);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				getCircelData(true);
			}
		});
		LayoutInflater inflater = LayoutInflater.from(this);
		mCircleFootView = inflater.inflate(R.layout.a_circle_find_item_foot, null);
		mCircleFootView.setVisibility(View.GONE);
		mLvSur.addFooterView(mCircleFootView);
		mCircleFootView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent_feekback = new Intent(CircleFind.this, Feedback.class);
				intent_feekback.putExtra(Feedback.EXTRA_FROM,Feedback.FROM_FIND_CIRCLE);
				startActivity(intent_feekback);
			}
		});
		mLvSur.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent it = new Intent(CircleFind.this,CircleHome.class);
				it.putExtra("cid", listData.get(position - 1).get("cid"));
				startActivity(it);
			}
		});
	}
	
	/**
	 * 数据解析展示
	 */
	private void getCircelData(final boolean isForward){ 
		final Button loadMore = loadManager.getSingleLoadMore(null);
		if (isForward) {
			currentPage = 1;
			if (loadMore != null)
				loadMore.setVisibility(View.GONE);
		} else {
			currentPage++;
		}
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listData.size() == 0);
		String getUrl = StringManager.api_circleFind + "?page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//刷新清理数据
					if(isForward) listData.clear();
					// 解析数据
					ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
					Map<String, String> map;
					for(int i = 0; i < listReturn.size(); i ++){
						map = listReturn.get(i);
						listData.add(map);
					}
					loadCount = listReturn.size();
					adapter.notifyDataSetChanged();
					// 如果是重新加载的,选中第一个tab.
					if (isForward)
						mLvSur.setSelection(1);
				}
//				if (everyPage == 0)
//					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,listData.size() == 0);
				mLvSur.onRefreshComplete();
				if(loadMore != null){
					if(!loadMore.isEnabled()){
						mCircleFootView.setVisibility(View.VISIBLE);
						loadMore.setVisibility(View.GONE);
					}else{
						mCircleFootView.setVisibility(View.GONE);
						loadMore.setVisibility(View.VISIBLE);
					}
				}else{
					mCircleFootView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
}
