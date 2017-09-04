package amodule.quan.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xiangha.R;

public class UploadSubjectTopControl {
	private UploadSubjectNew mUpSubNew;
	private UploadSubjectTopCallBack mUopCallBack;
	/**
	 * 进入发贴页面是否选择默认要发的item项,之所以用全局静态是因为发贴是先选图再进入发贴页面
	 */
	public String defalutCid = "-1";
	/**
	 * 默认选择第一个item
	 */
	private int mChooseItemIndex = -1;
	
	private ScrollView mScrollview;
	private LinearLayout mModuleLinear;
	private TextView mUploadItem,mCurrentChoose;
	private View mTopModule;
	private ImageView mArrow;
	
	private LayoutInflater mInflater;
	
	private ArrayList<Map<String, String>> topList;
	
	public UploadSubjectTopControl(UploadSubjectNew upSubNew,UploadSubjectTopCallBack topCallBack){
		mUpSubNew = upSubNew;
		mUopCallBack = topCallBack;
		topList = new ArrayList<Map<String, String>>();
		initTopView();
	}
	
	@SuppressLint("ResourceAsColor")
	public void initTopData(){
		CircleSqlite circleSqlite = new CircleSqlite(mUpSubNew);
		ArrayList<CircleData> array = circleSqlite.getAllCircleData();
		if(array == null || array.size() == 0)
			return;
		int index = 0;
		for(CircleData item : array){
			View view = mInflater.inflate(R.layout.a_common_post_subject_item_top_module, null);
			TextView moudleName = (TextView)view.findViewById(R.id.common_post_item_moudle_name);
			Map<String,String> map = new HashMap<String, String>();
			String name = item.getName();
			map.put("name", name);
			map.put("cid", item.getCid());
			if("-1".equals(defalutCid) && index == 0 || defalutCid.equals(item.getCid())){ 
				mChooseItemIndex = index;
				mCurrentChoose = moudleName;
				mCurrentChoose.setTextColor(Color.parseColor(Tools.getColorStr(mCurrentChoose.getContext(),R.color.comment_color)));
				mUploadItem.setText(name);
			}
			moudleName.setTag(index);
			index ++;
			topList.add(map);
			moudleName.setText(name);
			mModuleLinear.addView(view);
			moudleName.setOnClickListener(onMoudleItemClick);
		}
		//当给了默认index，但是此index却没有在array里面
		if(mChooseItemIndex == -1){
			mChooseItemIndex = 0;
			mCurrentChoose = (TextView) mModuleLinear.getChildAt(0).findViewById(R.id.common_post_item_moudle_name);
			mCurrentChoose.setTextColor(Color.parseColor(Tools.getColorStr(mCurrentChoose.getContext(),R.color.comment_color)));
			mUploadItem.setText(array.get(0).getName());
		}
		mScrollview.setVisibility(View.VISIBLE);
		mUpSubNew.findViewById(R.id.a_common_post_subject_item_top).setVisibility(View.VISIBLE);
	}
	
	private void initTopView() {
		mScrollview = (ScrollView)mUpSubNew.findViewById(R.id.scrollview);
		mInflater = LayoutInflater.from(mUpSubNew);
		mModuleLinear = (LinearLayout) mUpSubNew.findViewById(R.id.post_sub_item_top_module_linear);
		mUploadItem = (TextView)mUpSubNew.findViewById(R.id.upload_item);
		mTopModule = mUpSubNew.findViewById(R.id.post_sub_item_top_module);
		mArrow = (ImageView) mUpSubNew.findViewById(R.id.post_sub_item_top_arrow);
		mUpSubNew.findViewById(R.id.a_common_subject_ll).setVisibility(View.VISIBLE);
//		mUpSubNew.findViewById(R.id.a_common_subject_ll).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				setMoudleVisible(mTopModule.getVisibility() == View.GONE);
//			}
//		});
//		mModuleLinear.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				setMoudleVisible(false);
//			}
//		});
//		mTopModule.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				setMoudleVisible(false);
//			}
//		});
	}
	
	private OnClickListener onMoudleItemClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView moudleName = (TextView)v;
			int index = Integer.parseInt(v.getTag().toString());
			Map<String,String>map = topList.get(index);
			mChooseItemIndex = index;
			mCurrentChoose.setTextColor(0xffe7e7e7);
			mCurrentChoose = moudleName;
			moudleName.setTextColor(Color.parseColor(Tools.getColorStr(moudleName.getContext(),R.color.comment_color)));
			mUploadItem.setText(map.get("name"));
//			setMoudleVisible(false);
			mUopCallBack.onItemClick();
		}
	};
	
	private void setMoudleVisible(boolean visible){
		if(visible){
			setScroceble(false);
			mTopModule.setVisibility(View.VISIBLE);
			mArrow.setImageResource(R.drawable.i_arrow_up);
			ToolsDevice.keyboardControl(false, mUpSubNew, mArrow);
			XHClick.mapStat(mUpSubNew, UploadSubjectNew.mTongjiId, "圈子点击", "");
		}else{
			setScroceble(true);
			mTopModule.setVisibility(View.GONE);
			mArrow.setImageResource(R.drawable.i_arrow_down);
		}
	}
	
	private void setScroceble(final boolean isScroceble){
		mScrollview.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return !isScroceble;
			}
		});
	}
	
	public String getItemId(){
		if(mChooseItemIndex >= topList.size())
			return "";
		return topList.get(mChooseItemIndex).get("cid");
	}
	
	public String getChooseItemName(){
		if(mChooseItemIndex >= topList.size())
			return "";
		return topList.get(mChooseItemIndex).get("name");
	}
	
	public int getChooseItemIndex(){
		return mChooseItemIndex;
	}
	
	public interface UploadSubjectTopCallBack{
		public void onItemClick();
	}
}
