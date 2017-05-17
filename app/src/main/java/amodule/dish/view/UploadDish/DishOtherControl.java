package amodule.dish.view.UploadDish;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;

/**
 * 其他选项控制
 * @author tzy
 * @data 2016年3月1日上午11:58:25
 */
 public class DishOtherControl {
	 public static final String KEY_READYTIME = "readyTime";
	 public static final String KEY_COOKTIME = "cookTime";
	 public static final String KEY_TASTE = "taste";
	 public static final String KEY_DIFF = "diff";
	 public static final String KEY_EXCLUSIVE = "exclusive";
	 
	private LinearLayout mDishOtherView;
	private List<DishOtherItemView> itemArray;
	String[] strArray = {"准备时间","烹饪时间","口味","难度","独家上传"};
	String[] keyArray = {KEY_READYTIME,KEY_COOKTIME,KEY_TASTE,KEY_DIFF,KEY_EXCLUSIVE};
	String[] codeArray = {"","","","",""};
	
	private BaseActivity mAct;
	private UploadDishData mDishData;

	private boolean mIsHasDujia = true;

	public DishOtherControl(BaseActivity act, LinearLayout dishOtherView, UploadDishData dishData){
		this.mAct = act;
		this.mDishOtherView = dishOtherView;
		this.mDishData = dishData;
		initData();
		initView();
	}
	public DishOtherControl(BaseActivity act, LinearLayout dishOtherView, UploadDishData dishData,boolean isHasDujia){
		this.mAct = act;
		this.mDishOtherView = dishOtherView;
		this.mDishData = dishData;
		mIsHasDujia = isHasDujia;
		initData();
		initView();
	}

	private void initData() {
		codeArray[0] = mDishData.getReadyTime() == null ? "" : mDishData.getReadyTime();
		codeArray[1] = mDishData.getCookTime() == null ? "" : mDishData.getCookTime();
		codeArray[2] = mDishData.getTaste() == null ? "" : mDishData.getTaste();
		codeArray[3] = mDishData.getDiff() == null ? "" : mDishData.getDiff();
		codeArray[4] = mDishData.getExclusive() == null ? "" : mDishData.getExclusive();
	}

	private void initView() {
		if(mDishOtherView == null){
			return;
		}
		itemArray = new ArrayList<>();
		String optionData = AppCommon.getAppData(mAct, "option");
		List<Map<String,String>> data = StringManager.getListMapByJson(optionData);
		for(Map<String,String> map : data){
			int type = Integer.parseInt(map.get("type"));
			List<Map<String,String>> contentData = new ArrayList<>();
			switch(type){
			case 4:
			case 5:
			case 7:
				contentData = StringManager.getListMapByJson(map.get("data"));
				addItemView(DishOtherItemView.STYLE_NORMAL, type - 4, contentData);
				break;
			case 6:
				contentData = StringManager.getListMapByJson(map.get("data"));
				addItemView(DishOtherItemView.STYLE_SPECIAL, type - 4, contentData);
				break;
			case 8:
				if(!mIsHasDujia) return;
				mAct.findViewById(R.id.dish_other_exclusive_hint).setVisibility(View.VISIBLE);
				contentData = StringManager.getListMapByJson(map.get("data"));
				addItemView(DishOtherItemView.STYLE_CHOSE, type - 4, contentData);
			default:
				break;
			}
		}
	}

	private void addItemView(int style , final int index , List<Map<String, String>> contentData) {
		final DishOtherItemView itemView = new DishOtherItemView(mAct);
		itemView.setActivity(mAct);
		itemView.setTitle(strArray[index]);
		itemView.setKey(keyArray[index]);
		for(Map<String,String> map : contentData){
			map.put("selected", "selectedfalse");
		}
		for(Map<String,String> map : contentData){
			boolean selected = false;
			String[] strArray = codeArray[index].split(",");
			for(String code : strArray){
				selected = code.equals(map.get("code"));
				if(selected){
					itemView.setCode(map.get("code"));
					itemView.setTextInfo(map.get("name"));
					break;
				}
			}
			if(selected){
				map.put("selected", "selected" + selected);
				break;
			}
		}
		itemView.setPopupWindowData(style , contentData);
		itemView.setClickable(true);
		itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "其他选项", strArray[index]);
				itemView.showPopuWindow();
			}
		});
		if(index != strArray.length - 1){
			itemView.hasMarginToLine();
		}
		mDishOtherView.addView(itemView);
		itemArray.add(itemView);
	}
	
//	public String getContentData(List<Map<String,String>> data , String key){
//		String value = "";
//		if(data.size() > 0){
//			Map<String,String> map = data.get(0);
//			if(map.containsKey(key)){
//				value = map.get(key);
//			}
//		}
//		return value;
//	}
	
	/**
	 * 获取其他的四组参数
	 * @return 包含参数的map
	 */
	public Map<String,String> getParams(){
		Map<String,String> map = new HashMap<>();
		if(itemArray != null){
			for(DishOtherItemView itemView : itemArray){
				map.put(itemView.getKey(), itemView.getCode());
			}
		}
		return map;
	}
	
	public String getParam(String key){
		Map<String,String> map = getParams();
		return map.get(key) == null ? "" : map.get(key);
	}
	
	public String getReadyTime(){
		return getParam(KEY_READYTIME);
	}
	public String getCookTime(){
		return getParam(KEY_COOKTIME);
	}
	public String getTaste(){
		return getParam(KEY_TASTE);
	}
	public String getDiff(){
		return getParam(KEY_DIFF);
	}
	
	public String getExclusive(){
		return getParam(KEY_EXCLUSIVE);
	}

}
