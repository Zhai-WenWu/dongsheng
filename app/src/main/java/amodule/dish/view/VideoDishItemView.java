package amodule.dish.view;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.dish.adapter.AdapterVideoDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class VideoDishItemView {
	
	private BaseActivity mAct;
	private String mCode;
	private DownRefreshList listDish;
	private ArrayList<Map<String, String>> listDataMySuro;
	private AdapterVideoDish adapter;
	private View view;
	private int currentPage = 0, everyPage = 0;
	public boolean LoadOver = false;
	private int headViewHeight = 0;
	
	private OnListScrollListener mScrollListener;
	
	public VideoDishItemView(BaseActivity act,String code){
		mAct = act;
		mCode = code;
	}
	
	public View onCreateView() {
		view = LayoutInflater.from(mAct).inflate(R.layout.a_video_dish_item_view, null);
		currentPage=0;
		LoadOver = false;
		return view;
	}
	
	public void init(OnListScrollListener scrollListener){
		mScrollListener = scrollListener;
		listDish = (DownRefreshList)view.findViewById(R.id.video_dish_list);
		listDish.setDivider(null);
		listDataMySuro = new ArrayList<Map<String, String>>();
		adapter = new AdapterVideoDish(mAct, listDish, listDataMySuro, R.layout.a_video_dish_list_item,
				new String[]{"img", "name", "userImg", "isGourmet", "userName", "allClick", "favorites"},
				new int[]{R.id.item_model_video, R.id.item_title_tv, R.id.iv_userImg, R.id.iv_userType, R.id.user_name, R.id.dish_time_item_allClick, R.id.dish_time_item_allFave}) {


		};
		
		headViewHeight = Tools.getDimen(mAct, R.dimen.dp_80);
		listDish.setEmptyViewVisible(true);
		
		listDish.setOnTouchListener(new OnTouchListener() {
			
			float oldPosition = 0;
			boolean isOne = true;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
					if(isOne){
						isOne = false;
						oldPosition = event.getY();
					}else{
						float newPosition = event.getY();
						onTouchOk(oldPosition,newPosition);
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					float newPosition = event.getY();
					onTouchOk(oldPosition,newPosition);
					isOne = true;
					break;
				case MotionEvent.ACTION_UP:
					float newPosition2 = event.getY();
					onTouchOk(oldPosition,newPosition2);
					isOne = true;
					break;
				}
				return false;
			}
		});
		getData();
	}
	
	private void onTouchOk(float oldPosition,float newPosition){
		if(newPosition < oldPosition && oldPosition - newPosition > headViewHeight){
			mScrollListener.onScrollUp();
		}
		if(newPosition > oldPosition && newPosition - oldPosition > headViewHeight / 2){
			mScrollListener.onScrollDown();
		}
	}
	
	private void getData() {
		if (!LoadOver) {
			mAct.loadManager.showProgressBar();
			mAct.loadManager.setLoading(listDish, adapter, true, new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					getDishData(false);
				}
			}, new OnClickListener() {
				@Override
				public void onClick(View v) {
					getDishData(true);
				}
			});
			LoadOver = true;
		}
	}
	
	/**
	 * 获取网络数据
	 * @param isForward 是否是向上加载
	 */
	private void getDishData(final boolean isForward) {
		// 向上加载/加载上一页.
		if (isForward) {
			currentPage = 1;
		}
		// 向下加载;
		else {
			currentPage ++;
		}
		mAct.loadManager.loading(listDish,listDataMySuro.size() == 0);
		String getUrl = StringManager.api_getVideoClassifyDish + "?code=" + mCode + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				mAct.loadManager.hideProgressBar();
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(currentPage == 1){
						listDish.setVisibility(View.VISIBLE);
					}
					if(isForward) listDataMySuro.clear();
					ArrayList<Map<String, String>> listMySelf = UtilString.getListMapByJson(returnObj);
					loadCount = listMySelf.size();
					for(int i = 0; i < loadCount; i++){
						Map<String, String> map = listMySelf.get(i);
						ArrayList<Map<String, String>> custome = UtilString.getListMapByJson(map.get("customer"));
						if(custome.size() > 0){
							Map<String, String> customeMap = custome.get(0);
							map.put("userImg", customeMap.get("img"));
							map.put("isGourmet", customeMap.get("isGourmet"));
							map.put("userName", customeMap.get("nickName"));
							map.put("userCode", customeMap.get("code"));
						}
						map.put("allClick",map.get("allClick") + "浏览");
						map.put("favorites",map.get("favorites") + "收藏");
						listDataMySuro.add(map);
					}
					adapter.notifyDataSetChanged();
				}
				if (everyPage == 0)
					everyPage = loadCount;
				mAct.loadManager.loadOver(flag,listDish,loadCount);
				listDish.onRefreshComplete();
			}
		});
	}
	
	public void onSwitchOnResume(){
		adapter.notifyDataSetChanged();
		listDish.setSelection(0);
	}
	
	public interface OnListScrollListener{
		/**
		 * 向上滑动
		 */
		public void onScrollUp();
		/**
		 * 向下滑动
		 */
		public void onScrollDown();
	}
}
