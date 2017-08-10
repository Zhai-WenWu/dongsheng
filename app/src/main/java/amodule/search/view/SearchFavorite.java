package amodule.search.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.widget.DownRefreshList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xianghatest.R;

public class SearchFavorite {
	
	public BaseActivity mAct;
	
	public DownRefreshList data_list;
	public ScrollView scv_so;
	public View view,hearView ;
	public TextView tv_noData_search;
	
	public AdapterSimple adapter;
	public List<Map<String,String>> dishVideo;
	public String mSearchContent;
	
	public int currentPage = 0;
	public int everyPage = 0;
	
	public String searchType = "菜谱";
	
	public SearchFavorite(BaseActivity act){
		mAct = act;
		initView();
	}
	
	public void searData(String searchContent){
		mSearchContent = searchContent;
		initDataAdapter();
	}
	
	public void newSearch(String searchContent) {
		mSearchContent = searchContent;
		currentPage = 0;
		dishVideo.clear();
		data_list.setVisibility(View.GONE);
		scv_so.setVisibility(View.GONE);
		((LinearLayout)scv_so.getChildAt(0)).removeAllViews();
		resultLoad();
	}
	
	public void initView() {
		dishVideo= new ArrayList<Map<String,String>>();
		view = LayoutInflater.from(mAct).inflate(R.layout.a_favorite_search_result,null);
		data_list = (DownRefreshList)view.findViewById(R.id.data_list);
		scv_so = (ScrollView)view.findViewById(R.id.scv_so_no_data);
		LayoutInflater inflater = LayoutInflater.from(mAct);
		hearView = inflater.inflate(R.layout.a_favorite_search_head_item,null);
		tv_noData_search = (TextView)hearView.findViewById(R.id.tv_noData_search);
	}
	
	public View getListView(){
		return view;
	}
	
	public void initDataAdapter(){
		
	}
	
	protected void resultLoad() {
		mAct.loadManager.setLoading(data_list, adapter, true, new OnClickListener() {
			@Override 
			public void onClick(View arg0) {
				if (!mSearchContent.equals("")) {
					getData();
				} else {
					mAct.loadManager.hideProgressBar();
				}
			}
		});
	}
	
	public void getData() {
		mAct.loadManager.setLoading(data_list, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getSoData(false);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				getSoData(true);
			}
		});
	}
	
	/**
	 * 搜索菜谱
	 */
	public void getSoData(final boolean isForward){
		
	}
	
	public void setHearNoData(boolean isHavRecom){
		data_list.setVisibility(View.GONE);
		scv_so.setVisibility(View.VISIBLE);
		tv_noData_search.setText(mSearchContent);
		((LinearLayout)scv_so.getChildAt(0)).addView(hearView);
		if(isHavRecom){
			hearView.findViewById(R.id.tv_noData_tuijian).setVisibility(View.VISIBLE);
		}else{
			hearView.findViewById(R.id.tv_noData_tuijian).setVisibility(View.GONE);
		}
	}

}
