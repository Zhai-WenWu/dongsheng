package amodule.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.main.Main;
import amodule.user.adapter.AdapterMyselfDraft;

/**
 * 我的草稿
 * @author ruijiao_fang
 * @date 2014年11月7日
 */
public class MyDraft extends BaseActivity {
	private ArrayList<Map<String, String>> listDataMyDish;
	/**结果显示*/
	private TextView tvHint;
	private AdapterMyselfDraft adapter;
	public boolean isBlankSpace = true;
	private ListView theListView;
	
	private int needOpenId = -1;

	public static int oldCloseLevel = -1;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			adapter.notifyDataSetChanged();
			if(listDataMyDish.size() == 0){
				theListView.setVisibility(View.GONE);
				tvHint.setVisibility(View.VISIBLE);
			}
			else{
				theListView.setVisibility(View.VISIBLE);
				tvHint.setVisibility(View.GONE);
			}
			loadManager.hideProgressBar();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("我的草稿", 5,0, R.layout.c_view_bar_title, R.layout.a_my_draft);
		needOpenId = getIntent().getIntExtra("id", -1);
		initView();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		needOpenId = intent.getIntExtra("id", -1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(oldCloseLevel != -1) {
			Main.colse_level = oldCloseLevel;
			oldCloseLevel = -1;
		}
		loadFormLocal();
	}

	private void initView() {
		TextView tv_upDish = (TextView)(findViewById(R.id.rightText));
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv_upDish.getLayoutParams();
		layoutParams.setMargins(0,0,Tools.getDimen(this,R.dimen.dp_3),0);
		tv_upDish.setText("新建菜谱");
		tv_upDish.setVisibility(View.VISIBLE);

		tvHint = (TextView)findViewById(R.id.tv_hint);
		theListView = (ListView) findViewById(R.id.list_draft);
		theListView.setVisibility(View.GONE);
		TextView tvHind = new TextView(this);
		int dp15 = Tools.getDimen(this,R.dimen.dp_15);
		int dp5 = Tools.getDimen(this,R.dimen.dp_5);
		tvHind.setPadding(dp15,dp5,dp15,dp5);
		tvHind.setBackgroundColor(Color.parseColor("#fbf5cf"));
		tvHind.setTextColor(getResources().getColor(R.color.comment_color));
		tvHind.setTextSize(Tools.getDimenSp(this,R.dimen.sp_12));
		tvHind.setText("菜谱上传之前，请不要删除系统相册中的图片，否则会导致图片丢失。");
		theListView.addHeaderView(tvHind);
		listDataMyDish = new ArrayList<>();
		adapter = new AdapterMyselfDraft(this,theListView, listDataMyDish, R.layout.a_dish_item_draft,
				new String[]{"name","img"}, new int[] {R.id.draft_tv_name,R.id.iv_video_img});
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.imgWidth = ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_20);
		theListView.setAdapter(adapter);
		
		theListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if(position == 0) return;
				// 去掉两个头部位置;
				XHClick.onEventValue(MyDraft.this, "uploadDish", "uploadDish", "编辑草稿发", 1);
				Map<String, String> mapInfo = listDataMyDish.get(position - 1);
				if (mapInfo != null) {
					needOpenId = -1;
					Intent intent = new Intent();
					intent.setClass(MyDraft.this, UploadDishActivity.class);
					intent.putExtra("id",Integer.parseInt(mapInfo.get("id")));
					intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_DRAFT);
					intent.putExtra(UploadDishActivity.DISH_TYPE_KEY , "2".equals(mapInfo.get("videoType")) ? UploadDishActivity.DISH_TYPE_VIDEO : UploadDishActivity.DISH_TYPE_NORMAL);
					startActivity(intent);
				}
			}
		});
		
		tv_upDish.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				XHClick.onEventValue(MyDraft.this, "uploadDish", "uploadDish", "从草稿发", 1);
				Intent intent = new Intent();
				intent.setClass(MyDraft.this, UploadDishActivity.class);
				startActivity(intent);
			}
		});
		isBlankSpace = false;
	}

	/** 
	 * 从数据库获取草稿数据
	 * @TODO
	 */
	private void loadFormLocal() {
		loadManager.showProgressBar();
		tvHint.setVisibility(View.GONE);
		listDataMyDish.clear();
		new Thread(new Runnable(){
			@Override
			public void run() {
				if(isBlankSpace){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				UploadDishSqlite sqlite = new UploadDishSqlite(getApplicationContext());
				ArrayList<Map<String, String>> listDrafts = sqlite.getAllDataInDB();
				//不能这个直接赋值，因为Adpter记得是ArrayList的地址
//				listDataMyDish = listDrafts;
				for (int i = 0; i < listDrafts.size(); i++) {
					Map<String, String> map = listDrafts.get(i);
					if (map != null && map.get("draft").equals(UploadDishData.UPLOAD_DRAF)) {
						if(!map.containsKey("hasVideo"))
							map.put("hasVideo", "1");
						if ("2".equals(map.get("videoType"))) {
							map.put("hasVideo", "2");
						}
						listDataMyDish.add(map);
					}
				}
				handler.sendEmptyMessage(0);
			}
			
		}).start();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(needOpenId > 0){
			Intent intent = new Intent();
			intent.setClass(MyDraft.this, UploadDishActivity.class);
			intent.putExtra("id",needOpenId);
			intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_DRAFT);
			startActivity(intent);
		}
	}
	
}
