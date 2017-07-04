package amodule.dish.activity.upload;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.widget.DragGridView;
import amodule.dish.adapter.AdapterDishMakeOption;
import xh.basic.tool.UtilString;
import xh.windowview.XhDialog;

public class UploadDishMakeOptionActivity extends BaseActivity {
	private List<Map<String, String>> dataSourceList = new ArrayList<>();
	private AdapterDishMakeOption mDragAdapter;
	
	public static final String MAKE_ITEM_OPTION_DATA = "result_data";

	private boolean isVideoMake = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("调整步骤", 5, 0,R.layout.c_view_bar_title_operation_dish,R.layout.a_dish_upload_make_option);
		isVideoMake = getIntent().getBooleanExtra("isVideoMake",false);
		initView();
	}
	
	private void initView() {
		findViewById(R.id.leftImgBtn).setVisibility(View.GONE);
		TextView backTv = (TextView)findViewById(R.id.leftText);
		TextView save_Tv = (TextView) findViewById(R.id.rightText);
		backTv.setVisibility(View.VISIBLE);
		backTv.setTextSize(Tools.getDimenSp(this, R.dimen.sp_16));
		save_Tv.setVisibility(View.VISIBLE);
		save_Tv.setText("保存");
		save_Tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
				finish();
			}
		});
		
		String makesJson = getIntent().getStringExtra("makesJson");
		dataSourceList = UtilString.getListMapByJson(makesJson);
		Map<String, String> map;
		for(int i = 0; i < dataSourceList.size(); i++){
			map = dataSourceList.get(i);
			map.put("makesStep", String.valueOf(i+1));
			String path = map.get("makesImg");
			if(TextUtils.isEmpty(path)) {
				String videoInfo = map.get("videoInfo");
				try {
					if (!TextUtils.isEmpty(videoInfo)) {
						JSONObject videoObject = new JSONObject(videoInfo);
						String imgPath = (String) videoObject.get("imgPath");
						map.put("makesImg",imgPath);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		DragGridView mDragGridView = (DragGridView) findViewById(R.id.dish_make_dragGridView);
		mDragGridView.setNumColumns(1);
		mDragAdapter = new AdapterDishMakeOption(this, dataSourceList,isVideoMake);
		mDragGridView.setAdapter(mDragAdapter);
		if(dataSourceList.size() > 0){
			mDragGridView.setVisibility(View.VISIBLE);
			findViewById(R.id.dish_make_no_data_hint).setVisibility(View.GONE);
		}else{
			save_Tv.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onBackPressed() {
		if(mDragAdapter.isEdit){
			final XhDialog hintDialog = new XhDialog(this);
			hintDialog.setTitle("是否保存调整后的步骤")
			.setCanselButton("取消", new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					hintDialog.cancel();
					UploadDishMakeOptionActivity.super.onBackPressed();
				}
			})
			.setSureButton("保存", new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					saveData();
					hintDialog.cancel();
					UploadDishMakeOptionActivity.super.onBackPressed();
				}
			});
			hintDialog.show();
		}else{
			UploadDishMakeOptionActivity.super.onBackPressed();
		}
	}
	
	private void saveData(){
		// 返回已选择的图片数据
		Intent data = new Intent();
		data.putExtra(MAKE_ITEM_OPTION_DATA, getDishMakeData());
		setResult(RESULT_OK, data);
	}

	/**
	 * 组合步骤做法和效果图数据 根据dataType返回草稿数据、所有数据
	 */
	public String getDishMakeData() {
		List<Map<String, String>> list = mDragAdapter.getData();
		JSONArray jsonArray = new JSONArray();
		int setpIndex = 1;
		for (int index = 0; index < list.size(); index++) {
			Map<String, String> map = list.get(index);
			try {
				// 数据库存储使用Json格式
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("makesStep", setpIndex);
				jsonObj.put("makesInfo", map.get("makesInfo"));
				jsonObj.put("makesImg", map.get("makesImg")); //普通菜谱信息
				jsonObj.put("videoInfo", map.get("videoInfo")); //视频菜谱信息
				jsonArray.put(jsonObj);
				setpIndex ++;
			} catch (JSONException ex) {
			}
		}
		return jsonArray.toString();
	}
}
