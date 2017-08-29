package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.logic.SetDataView.ClickFunc;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import amodule.quan.activity.CircleFind;
import amodule.quan.activity.CircleHome;
import amodule.quan.tool.MyQuanDataControl;
import amodule.quan.tool.MyQuanDataControl.DataCallback;
/**
 * 我的圈子
 * @author FangRuijiao
 */
public class MyCircleView extends LinearLayout{
	private Context mCon;
	private View mView;
	private TableLayout mTableLayout;
	private ImageView mShowAll;
	private TextView mHintTextView;
	private List<Map<String, String>> mData;
	
	public int column = 2;
	public int rowCount = 0;
	private int tbHei;
	
	private MyCircleViewListener mListener;
	
	public MyCircleView(Context context) {
		super(context);
		mCon = context;
		initView();
	}
	
	public MyCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCon = context;
		initView();
	}
	
	private void initView(){
		this.setGravity(Gravity.CENTER_HORIZONTAL);
		this.setOrientation(LinearLayout.VERTICAL);
		this.setBackgroundColor(Color.parseColor(getResources().getString(R.color.common_bg)));
		LayoutInflater inLayout = LayoutInflater.from(mCon);
		mView = inLayout.inflate(R.layout.view_my_circle, null);
		mHintTextView = (TextView)mView.findViewById(R.id.view_my_cicle_hint);
		mTableLayout = (TableLayout) mView.findViewById(R.id.view_my_circle);
		tbHei = Tools.getDimen(mCon, R.dimen.dp_108);
		setTableLayoutHeiht(tbHei);
		mShowAll = (ImageView)mView.findViewById(R.id.view_my_circle_show_all);
		final View showAllLine = mView.findViewById(R.id.view_my_circle_show_all_ll);
		mShowAll.setTag(1);
		showAllLine.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(Integer.parseInt(mShowAll.getTag().toString()) == 1){
					setTableLayoutHeiht(TableLayout.LayoutParams.WRAP_CONTENT);
					tbHei = (int) (Tools.getMeasureHeight(mTableLayout) * 2f / rowCount); 
					mShowAll.setTag(0);
					mShowAll.setImageResource(R.drawable.i_view_circle_up);
				}else{
					setTableLayoutHeiht(tbHei);
					mShowAll.setTag(1);
					mShowAll.setImageResource(R.drawable.i_view_circle_down);
				}
			}
		});
	}
	
	private void setTableLayoutHeiht(int height){
		TableLayout.LayoutParams tbParams = new TableLayout.LayoutParams();
		tbParams.width = TableLayout.LayoutParams.MATCH_PARENT;
		tbParams.height = height;
		mTableLayout.setLayoutParams(tbParams);
	}
	

	/**
	 * 初始化UI
	 */
	private void init(){
		this.addView(mView);
		AdapterSimple mAdapter = new AdapterSimple(mTableLayout, mData,
				R.layout.view_my_circle_item,
				new String[]{"name","dayHotNum"},
				new int[]{R.id.view_my_circle_item_title,R.id.view_my_circle_item_num});
		mAdapter.imgResource=R.drawable.bg_round_zannum;
		view(mTableLayout, column, mAdapter, 
				new int[]{ R.id.view_my_circle_item_parent}, 
				new SetDataView.ClickFunc[]{
					new SetDataView.ClickFunc() {
						@Override
						public void click(int index, View view) {
							Intent it = new Intent(mCon,CircleHome.class);
							it.putExtra("cid", mData.get(index).get("cid"));
							mCon.startActivity(it);
						}
					}
				}
		);
	}
	
	/**
	 * 设置table中View
	 * @param table TableLayout
	 * @param span 一行有多少个View
	 * @param adapter ImgSimpleAdapter
	 * @param clickId 点击事件id
	 * @param func 点击事件
	 */
	public void view(TableLayout table, int span, AdapterSimple adapter, int[] clickId, ClickFunc[] func) {
		Context context = table.getContext();
		TableRow row = new TableRow(context);
		row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, Tools.getDimen(context, R.dimen.dp_54)));
		int i = 0;
		TableRow.LayoutParams lp = new TableRow.LayoutParams(0, Tools.getDimen(context, R.dimen.dp_54));
		lp.weight = 1;
		for (i = 0; i < adapter.getCount(); i++) {
			View view = adapter.getView(i, null, row);
			// 添加响应
			if (func != null) {
				if (clickId != null) {
					for (int j = 0; j < clickId.length; j++) {
						view.findViewById(clickId[j]).setOnClickListener(SetDataView.getClicker(i, j > func.length - 1 ? func[func.length - 1] : func[j]));
					}
				} else
					view.setOnClickListener(SetDataView.getClicker(i, func[0]));
			}
			view.setLayoutParams(lp);
			row.addView(view);
			if ((i + 1) % span == 0) {
				table.addView(row);
				row = new TableRow(context);
			}
		}
		
		LayoutInflater inflater = LayoutInflater.from(mCon);
		View addView = inflater.inflate(R.layout.view_my_circle_item_add_mound, null);
		addView.setLayoutParams(lp);
		if(i % span == 0){
			row = new TableRow(context);
			addView.setLayoutParams(lp);
			row.addView(addView);
			i++;
		}else{
			row.addView(addView);
			i++;
		}
		table.addView(row);
		// 补一行
		if (i % span > 0) {
			// 补充数量
			while (i % span > 0) {
				View view = new View(table.getContext());
				TableRow.LayoutParams lp2 = new TableRow.LayoutParams(0, Tools.getDimen(context, R.dimen.dp_54));
				lp2.weight = 1;
				row.addView(view, lp2);
				i++;
			}
		}
		
		addView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//去发现圈子
				Intent intent = new Intent(mCon, CircleFind.class);
				mCon.startActivity(intent);
			}
		});
		rowCount = mTableLayout.getChildCount();
	}
	
	/**
	 * 初始化我的圈子的数据
	 */
	public void initData(){
		if(LoginManager.isLogin()){
			mHintTextView.setText("我的圈子");
		}else{
			mHintTextView.setText("推荐圈子");
		}
		setTableLayoutHeiht(tbHei);
		mShowAll.setTag(1);
		mShowAll.setImageResource(R.drawable.i_view_circle_down);
		MyQuanDataControl.getNewMyQuanData(mCon, new DataCallback() {
			
			@Override
			public void setMyQuanData(ArrayList<Map<String,String>> newData) {
				if(newData.size() == 0){
					mView.setVisibility(View.GONE);
					return;
				}
				mView.setVisibility(View.VISIBLE);
				for(Map<String, String> map : newData){
					int dayHotNum = Integer.parseInt(map.get("dayHotNum"));
					if(dayHotNum > 999999){
						map.put("dayHotNum", "999999+");
					}else{
						map.put("dayHotNum", dayHotNum + "");
					}
				}
				if(newData.size() <= column * 2 - 1) 
					mShowAll.setVisibility(View.GONE);
				else 
					mShowAll.setVisibility(View.VISIBLE);
				if(mData == null || isModify(newData)){
					if(mListener != null)
						mListener.onIsRefreshData(true);
					mData = newData;
					MyCircleView.this.removeAllViews();
					mTableLayout.removeAllViews();
					init();
				}else if(mListener != null)
					mListener.onIsRefreshData(false);
			}
		});
	}
	/**
	 * 刷新
	 */
	public void requestMyCirclerData() {
		initData();
	}
	
	public void setListener(MyCircleViewListener listener){
		mListener = listener;
	}
	
	private boolean isModify(List<Map<String, String>> newData){
		if(mData.size() != newData.size())
			return true;
		Map<String, String> oldMap,newMap;
		for(int i = 0; i < mData.size(); i++){
			oldMap = mData.get(i);
			newMap = newData.get(i);
			if(oldMap.size() != newMap.size())
				return true;
			for(String key : newMap.keySet()){
				if(!oldMap.containsKey(key))
					return true;
				if(!newMap.get(key).equals(oldMap.get(key)))
					return true;
			}
		}
		return false;
	}
	
	public interface MyCircleViewListener{
		/**
		 * 刷新页面时数据是否有改变
		 * @param isChange
		 * true：改变
		 * false：没改吧
		 */
		public void onIsRefreshData(boolean isChange);
	}
}
