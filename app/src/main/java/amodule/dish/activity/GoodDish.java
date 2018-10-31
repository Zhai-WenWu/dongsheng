package amodule.dish.activity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.dish.adapter.AdapterGoodDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.tool.UtilString;

/**
 * 今日佳作
 * @author Administrator
 *
 */
public class GoodDish extends BaseActivity implements OnClickListener{

	private ArrayList<Map<String,String>> listData;
	private AdapterGoodDish adapter;
	private ListView listview_today;
	/** 每页的数据数量 */
	private int mEveryPageNum = 0;
	/** 当前page */
	private int mCurrentPage = 0;
	private boolean isShowPast=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("今日佳作", 2, 0, R.layout.a_view_bar_title_gooddish, R.layout.a_home_gooddish);
		initView();
		initData();
	}

	private void initView() {
		listview_today=(ListView) findViewById(R.id.gooddish_listview_today);
		findViewById(R.id.share_layout).setOnClickListener(this);
	}
	
	private void initData() {
		listData= new ArrayList<Map<String,String>>();
		adapter= new AdapterGoodDish(this, listview_today, listData);
		loadManager.setLoading(listview_today, adapter, true, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadDishData();
			}
		});
		
	}

	private void loadDishData(){
		mCurrentPage++;
		loadManager.loading(listview_today,false);
		String params= "?type=1&page="+mCurrentPage;
		ReqInternet.in().doGet(StringManager.api_homeTodayGood+params, new InternetCallback() {
			
			@Override
			public void loaded(int flag, String url, Object msg) {
				int loadCount=0;
				if(flag>=ReqInternet.REQ_OK_STRING){
					ArrayList<Map<String,String>> map= UtilString.getListMapByJson(msg);
					ArrayList<Map<String,String>> map_today= UtilString.getListMapByJson(map.get(0).get("todayData"));
					ArrayList<Map<String,String>> map_last= UtilString.getListMapByJson(map.get(0).get("pastData"));
					if(!isShowPast){
						for (int i = 0,length=map_today.size() ; i < length; i++) {
							listData.add(map_today.get(i));
						}
					}
					if(map_last.size()>0){
						if(!isShowPast){
							Map<String,String> map_temp=new HashMap<String, String>();
							map_temp.put("isPast", "1");
							listData.add(map_temp);
							isShowPast=true;
						}
						for (int i = 0,length=map_last.size() ; i < length; i++) {
							listData.add(map_last.get(i));
							loadCount++;
						}
					
					}
				}
				if(mEveryPageNum == 0){
					mEveryPageNum = loadCount;
				}
				adapter.notifyDataSetChanged();
				loadManager.loadOver(flag,listview_today,loadCount);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_layout:
			XHClick.mapStat(this, "a_share400", "菜谱", "今日佳作");
			barShare = new BarShare(GoodDish.this, "今日佳作","菜谱");
			String title =  "今日佳作";
			String clickUrl = StringManager.api_homeTodayGoodShare;
			String content = "大厨家的菜，跟着大厨做点好吃的";
			Resources res = getResources();
			Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.share_launcher);
			barShare.setShare(title, content,bmp, clickUrl);
			barShare.openShare();
			break;
		}
	}
}
