package amodule.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.xiangha.R;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.avtivity.HomeSearch;
import amodule.search.data.SearchConstant;
import amodule.user.adapter.AdapterGastronome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

/**
 * TODO
 * 
 * @author zeyue_t
 * @time 2015年5月18日下午7:28:51
 */
public class GourmetList extends BaseActivity {

	private RelativeLayout search_fake_layout;
	private TextView rightText, layout_text_cover;
	private ListView leftListView, rightListView;
	private AdapterGastronome adapterGastronome;
	private AdapterSimple adapterClassify;
	private ArrayList<Map<String, String>> leftData = null, rightData = null;

	private Handler handler;
	private static final int MSG_SWITCH_TYPE_OK = 1;
	private static final int MSG_NEW_DATA_OK = 2;
	private static final int LOAD_LEVEL_TWO_UI = 3;
	private static final int NO_DATA = 4;
	private int index = 0;
	private int currentPage = 0, everyPage = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("美食家", 2, 0, R.layout.c_view_bar_title, R.layout.a_gourmet_list);
		initHandler();
		initView();
		initData();
		setClickListener();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		handler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
	}

	private void initHandler() {
		handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_SWITCH_TYPE_OK:
					AppCommon.scorllToIndex(rightListView, 0);
					AppCommon.scorllToIndex(leftListView, index);
				case MSG_NEW_DATA_OK:
					loadManager.hideProgressBar();
					rightListView.setVisibility(View.VISIBLE);
					findViewById(R.id.noData).setVisibility(View.INVISIBLE);
					break;
				case NO_DATA:
					loadManager.hideProgressBar();
					rightListView.setVisibility(View.INVISIBLE);
					findViewById(R.id.noData).setVisibility(View.VISIBLE);
					break;
				case LOAD_LEVEL_TWO_UI:
					currentPage = 0;
					everyPage = 0;
					rightData.clear();
					rightListView.setVisibility(View.INVISIBLE);
					getData();
					break;
				}
				return false;
			}
		});
	}

	private void initView() {
		search_fake_layout = (RelativeLayout) findViewById(R.id.search_fake_layout);
		rightText = (TextView) findViewById(R.id.rightText);
		layout_text_cover = (TextView) findViewById(R.id.layout_text_cover);
		leftListView = (ListView) findViewById(R.id.classify_left_list);
		rightListView = (ListView) findViewById(R.id.classify_right_list);
	}

	private void initData() {
		// 设置title右侧text
		rightText.setTextSize(Tools.getDimenSp(this, R.dimen.sp_15));
		rightText.setText("申请认证");
		rightText.setVisibility(View.VISIBLE);
		// 设置搜索文字
		layout_text_cover.setText("搜索哈友");

		leftData = new ArrayList<Map<String, String>>();
		rightData = new ArrayList<Map<String, String>>();
		// 初始化leftList的数据
		Map<String, String> map_one = new HashMap<String, String>();
		map_one.put("title", "推荐");
		map_one.put("url", StringManager.api_quanTopUrl + "?type=day");
		map_one.put("select", "1");
		leftData.add(map_one);
		String jsonGourmetStr = AppCommon.getAppData(this, "gourmet");
		ArrayList<Map<String, String>> data = UtilString.getListMapByJson(jsonGourmetStr);
		try {
			for(Map<String, String> map : data){
				map.put("title", map.get(""));
				map.put("url", StringManager.api_soList + "?type=customer&tagName=" + URLEncoder.encode(map.get(""), HTTP.UTF_8));
				map.put("select", "0");
				leftData.add(map);
//				LogManager.print("d", map.toString());
			}
		} catch (UnsupportedEncodingException e) {
			UtilLog.reportError("URLEncoder异常", e);
		}
		// 设置leftList的item的属性
		int list_width = ToolsDevice.getWindowPx(this).widthPixels * 150 / 750;
		int list_height = ToolsDevice.getWindowPx(this).widthPixels * 180 / 1500;
		adapterClassify = new AdapterSimple(leftListView, leftData, 
				R.layout.a_xh_gastronome_left_item, 
				new String[] { "title" , "select"},
				new int[] { R.id.classify_left_title , R.id.classify_left_root });
		adapterClassify.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				int id = view.getId();
				switch(id){
				case R.id.classify_left_root:
					TextView title = (TextView) view.findViewById(R.id.classify_left_title);
					if (data.equals("0")) {
						title.setTextColor(Color.parseColor("#666666"));
						view.setBackgroundColor(Color.TRANSPARENT);
						view.findViewById(R.id.line_left).setVisibility(View.GONE);
					} else {
						title.setTextColor(Color.parseColor("#333333"));
						view.setBackgroundColor(Color.parseColor("#FFFFFF"));
						view.findViewById(R.id.line_left).setVisibility(View.VISIBLE);
					}
					return true;
				}
				return false;
			}
		});
		adapterClassify.viewWidth = list_width;
		adapterClassify.viewHeight = list_height;
		leftListView.getLayoutParams().width = list_width;
		leftListView.setAdapter(adapterClassify);
		leftListView.setDivider(null);

		adapterGastronome = new AdapterGastronome(rightListView, rightData, 0, null, null);
		adapterGastronome.roundImgPixels = Tools.getDimen(this, R.dimen.dp_300);
		rightListView.setDivider(null);
	}

	private void setClickListener() {
		search_fake_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GourmetList.this, HomeSearch.class);
				intent.putExtra(SearchConstant.SEARCH_TYPE, SearchConstant.SEARCH_HAYOU);
				startActivity(intent);
			}
		});
		rightText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppCommon.openUrl(GourmetList.this, StringManager.api_approveGourmet, true);
			}
		});
		leftListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				loadManager.showProgressBar();
				for (int i = 0; i < leftData.size(); i++) {
					if (i == position)
						leftData.get(i).put("select", "1");
					else
						leftData.get(i).put("select", "0");
				}
				adapterClassify.notifyDataSetChanged();
				index = position;
				handler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
			}
		});
		loadManager.setLoading(rightListView, adapterGastronome, true, new OnClickListener() {
			@Override
			public void onClick(View v) {
				getData();
			}
		});
	}

	private void getData() {
		// 添加加载更多
		currentPage++;
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,leftData.size() == 0);
		String url = leftData.get(index).get("url") + "&page=" + currentPage;
		ReqInternet.in().doGet(url, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> listTop = UtilString.getListMapByJson(returnObj);
					if (listTop.size() > 0) {
						if (listTop.get(0).containsKey("customers"))
							listTop = UtilString.getListMapByJson(listTop.get(0).get("customers"));
						Map<String, String> map = new HashMap<String, String>();
						for (int i = 0; i < listTop.size(); i++) {
							map = listTop.get(i);
							map.put("allLikeNum", "被赞" + map.get("allLikeNum"));
							map.put("allSubjectNum", "美食帖" + map.get("allSubjectNum"));
							map.put("userCode", map.get("code"));
							rightData.add(map);
						}
						loadCount = listTop.size();
						adapterGastronome.notifyDataSetChanged();
						if (currentPage == 1) {
							handler.sendEmptyMessage(MSG_SWITCH_TYPE_OK);
						} else
							handler.sendEmptyMessage(MSG_NEW_DATA_OK);
					}else{
						if (currentPage == 1) 
							handler.sendEmptyMessage(NO_DATA);
					}
				} else {
					toastFaildRes(flag,true,returnObj);
				}
				if (everyPage == 0)	
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage,leftData.size() == 0);
			}
		});
	}
}
