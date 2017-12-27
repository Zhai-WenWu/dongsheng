package amodule.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import acore.widget.FlowLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.tools.JSAction;

import com.xiangha.R;

public class ChooseDish extends BaseActivity{
	private int mMaxChooseNum = 5;
	private TextView mRightText;
	private FlowLayout mFlowLayout;
	private DownRefreshList mDishList;
	private EditText mSearchEdit;
	
	private List<Map<String,String>> mData,mSearchData;
	private List<Map<String,String>> mChooseData;
	private AdapterSimple mDishAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle  bundle = this.getIntent().getExtras();
		if(bundle != null){
			String maxNum = bundle.getString("num");
			String data = bundle.getString("data");
			if(maxNum != null && !maxNum.equals("null") && maxNum.length() > 0)
				mMaxChooseNum = Integer.parseInt(maxNum);
			if(data != null && data.length() > 10)
				mChooseData = UtilString.getListMapByJson(data);
		}
		initActivity("选择作品", 2, 0, R.layout.c_view_bar_title, R.layout.a_gourmet_choose_dish);
		initView();
		initData();
		setListener();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadManager.hideProgressBar();
	}
	
	private void initView() {
		mRightText = (TextView) findViewById(R.id.rightText);
		mFlowLayout = (FlowLayout) findViewById(R.id.dish_choose_flow_layout);
		mDishList= (DownRefreshList) findViewById(R.id.dish_list);
		mSearchEdit = (EditText) findViewById(R.id.ed_search);
	}
	
	private void initData() {
		//初始化确定按钮
		mRightText.setText("确定");
		mRightText.setVisibility(View.VISIBLE);
		findViewById(R.id.leftText).setVisibility(View.INVISIBLE);
		mSearchEdit.setHint("搜索菜谱");
		//初始化数据集合
		mData = new ArrayList<Map<String,String>>();
		mSearchData = new ArrayList<Map<String,String>>();
		if(mChooseData == null)
			mChooseData = new ArrayList<Map<String,String>>();
		mDishAdapter = new AdapterSimple(mDishList, mSearchData, 
				R.layout.a_gourmet_choose_dish_list_item, 
				new String[]{"img","name","choose"}, 
				new int[]{R.id.dish_img , R.id.dish_name , R.id.choose_dish_ico});
		mDishAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				int viewId = view.getId();
				switch(viewId){
					case R.id.choose_dish_ico:
						if(data != null){
							boolean chooseState = Boolean.parseBoolean(data.toString());
							view.setBackgroundResource(chooseState ? R.drawable.i_ico_ok : R.drawable.i_ico_nook);
						}
					return true;
				}
				return false;
			}
		});
		int viewHeight = (int)(ToolsDevice.getWindowPx(this).widthPixels * 10 / 64f);
		mDishAdapter.viewHeight = viewHeight;
		mDishAdapter.imgHeight = viewHeight - 14;
		mDishAdapter.imgWidth = (int)(mDishAdapter.imgHeight * 144 /  86f);
		mDishAdapter.imgZoom = true;
	}

	private void setListener() {
		//设置搜索监听
		mSearchEdit.addTextChangedListener(mSearchTextWatcher);
		//设置确定后的JS调取
		mRightText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JSAction.resumeAction = "allDish_allDish(\'" + Tools.list2Json(mChooseData) + "\')";
				finish();
			}
		});
		//这是mFlowLayout的clickView和click事件
		mFlowLayout.setOnItemClickListenerById(R.id.choose_dish_del, new FlowLayout.OnItemClickListenerById() {
			@Override
			public void onClick(View v, int position) {
				Map<String,String>  map = mChooseData.get(position);
				modifyChooseState(map);
				mDishAdapter.notifyDataSetChanged();
				mFlowLayout.refreshLayout();
			}
		});
		//为mFlowLayout初始化
		mFlowLayout.initFlowLayout(mChooseData, 
				R.layout.a_gourmet_choose_dish_item, 
				new String[]{"name"}, 
				new int[]{R.id.choose_dish_name});
		//设置
		mDishList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int currentIndex = --position;
				Map<String,String> map = mSearchData.get(currentIndex);
				modifyChooseState(map);
				mDishAdapter.notifyDataSetChanged();
				mFlowLayout.refreshLayout();
			}
		});
		loadManager.setLoading(mDishList, mDishAdapter, false, new OnClickListener() {
			@Override
			public void onClick(View v) {
				getAllDishList(false);
			}
		}, new OnClickListener() {
			@Override
			public void onClick(View v) {
				getAllDishList(true);
			}
		});
	}
	
	//修改所有集合中的数据的chooseState
	private void modifyChooseState(Map<String, String> map) {
		int index = Integer.parseInt(map.get("index"));
		int searchIndex = mSearchData.indexOf(map);
		boolean isChoose = false;
		if(map.get("choose") != null){
			isChoose = Boolean.parseBoolean(map.get("choose"));
		}
		if(isChoose){
			mChooseData.remove(map);
			mData.get(index).put("choose", "false");
			if(searchIndex >= 0)
				mSearchData.get(searchIndex).put("choose", "false");
		}else{
			if(mChooseData.size() == mMaxChooseNum){
				Tools.showToast(getApplicationContext(), "只能选择" + mMaxChooseNum + "个代表作品");
				return;
			}
			mData.get(index).put("choose", "true");
			if(searchIndex >= 0)
				mSearchData.get(searchIndex).put("choose", "true");
			mChooseData.add(map);
		}
	}
	
	//获取user所有的dish列表
	private void getAllDishList(boolean isRefresh) {
		if(isRefresh){
			mSearchData.clear();
			mData.clear();
		}
		ReqInternet.in().doGet(StringManager.api_getUserDishAll, new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if(flag > 1){
					UtilLog.print("d", returnObj.toString());
					List<Map<String,String>> returnData = UtilString.getListMapByJson(returnObj);
					for(int i = 0 ; i < returnData.size() ; i++){
						Map<String,String> map = returnData.get(i);
						for(Map<String,String> chooseMap: mChooseData){
							chooseMap.put("choose", "true");
							if(chooseMap.get("code").equals(map.get("code"))){
								chooseMap.put("index", "" + i);
//								favorites=0, code=40053788, choose=false, name=这是菜谱？
								chooseMap.put("img", map.get("img"));
								chooseMap.put("allClick", map.get("allClick"));
								chooseMap.put("favorites", map.get("favorites"));
								map.put("choose", "true");
								break;
							}else{
								map.put("choose", "false");
							}
						}
						map.put("index", "" + i);
						mData.add(map);
					}
					mSearchData.addAll(mData);
					mDishList.setVisibility(View.VISIBLE);
					mDishList.onRefreshComplete();
				}
			}
		});
	}
	
	// 搜索监听
	private TextWatcher mSearchTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mSearchData.clear();
			mSearchData.addAll(mData);
			String searchWord = s.toString();
			if (searchWord.length() != 0) {
				for(Map<String,String> map : mSearchData)
					if (map.get("name").indexOf(searchWord) < 0)
						mSearchData.remove(map);
			}
			mDishAdapter.notifyDataSetChanged();
			mDishList.onRefreshComplete();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};
	
}
