package amodule.health.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ScrollviewDish;
import acore.widget.ScrollviewDish.onScrollViewChange;
import amodule.dish.activity.ListDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.tools.AdPlayIdConfig;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:DetailHealth.java Copyright: Copyright (c) 2014~2017
 * 健康养生宜吃忌吃
 * @author zeyu_t
 * @date 2014年10月14日
 */
public class DetailHealth extends BaseActivity {
	private TextView detail_tv, yichi_tv, jichi_tv;
	private LinearLayout layout_yichi, layout_jichi;
	private ScrollviewDish scrollView;

	private ArrayList<ArrayList<Map<String, String>>> yichiInfo = new ArrayList<ArrayList<Map<String, String>>>();
	private ArrayList<ArrayList<Map<String, String>>> jichiInfo = new ArrayList<ArrayList<Map<String, String>>>();
	
	private String name = "", code = "", datatype = "";
	private int ico_id;
	private boolean moreFlag = true;
	
	private String tongjiId = "a_Health";
	
	private int statusBarHeight = 0;
	private AdsShow adTip;
	private RelativeLayout adTipLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			name = bundle.getString("name");
			code = bundle.getString("code");
			datatype = bundle.getString("type");
			ico_id = bundle.getInt("ico_id");
		}
		initActivity("", 2, 0, R.layout.c_view_bar_title, R.layout.a_health_detail);
		initView();
		initAd();
		// 设置加载
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				contentLoad();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Rect outRect = new Rect();  
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);  
		statusBarHeight = outRect.top;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		yichiInfo.clear();
		jichiInfo.clear();
	}

	private void initView() {
		initBarView();
		scrollView = (ScrollviewDish) findViewById(R.id.sroll_detailContent);
		layout_yichi = (LinearLayout) findViewById(R.id.health_detail_yichi);
		layout_jichi = (LinearLayout) findViewById(R.id.health_detail_jichi);
		detail_tv = (TextView) findViewById(R.id.detail_info_tv);
		yichi_tv = (TextView) findViewById(R.id.yichi_info_tv);
		jichi_tv = (TextView) findViewById(R.id.jichi_info_tv);
		scrollView.setonScrollViewChange(new onScrollViewChange() {
			int screenHeight = ToolsDevice.getWindowPx(getApplicationContext()).heightPixels;
			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
				if (adTipLayout == null || adTip == null)
					return;
				int[] location = new int[2];
				adTipLayout.getLocationOnScreen(location);
				adTip.isOnScreen(location[1] > statusBarHeight && location[1] < screenHeight);
			}
		});
	}

	/**
	 * 当前不再使用广告
	 */
	private void initAd(){
		adTipLayout = (RelativeLayout) findViewById(R.id.health_detail_ad_jichi_layout);
//		//广点通banner广告
//		RelativeLayout bannerLayout = (RelativeLayout)findViewById(R.id.health_detail_ad_jichi_layout_gdt);
//		GdtAdNew gdtAd = new GdtAdNew(this,"", bannerLayout,0, GdtAdTools.ID_HEALTH,GdtAdNew.CREATE_AD_BANNER);
//		gdtAd.isNeedOnScreen = true;
		//只显示自己banner
		BannerAd bannerAdBurden = new BannerAd(this,"other_health", adTipLayout);

		AdParent[] adsTipParent = {bannerAdBurden};
		adTip = new AdsShow(adsTipParent, AdPlayIdConfig.DETAIL_HEALTH);
		mAds = new AdsShow[]{adTip};
	}
	
	private void initBarView() {
		ImageView img_share = (ImageView) findViewById(R.id.rightImgBtn2);
		img_share.setImageResource(R.drawable.z_z_topbar_ico_share);
		img_share.setVisibility(View.VISIBLE);
		img_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doShare();
			}
		});
	}

	private void contentLoad() {
		loadManager.showProgressBar();
		String url = StringManager.api_getIngreList + "?type=" + datatype + "&g1=" + code;
		ReqInternet.in().doGet(url, new InternetCallback(this.getApplicationContext()) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> classify = null;
					name = UtilString.getListMapByJson(returnObj).get(0).get("name");
					if(name != null){
						((TextView)findViewById(R.id.title)).setText(name + "宜吃忌吃");
					}
					String info = UtilString.getListMapByJson(returnObj).get(0).get("info");
					final String oldText = Tools.getSegmentedStr(info).toString();
					if (oldText.length() > 200) {
						final String newText = oldText.substring(0, 150) + "……查看更多>>";
						detail_tv.setText(newText);
						detail_tv.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (moreFlag) {
									moreFlag = false;
									detail_tv.setText(newText);
								} else {
									moreFlag = true;
									detail_tv.setText(oldText);
								}
							}
						});
					} else
						detail_tv.setText(oldText);
					String data = UtilString.getListMapByJson(returnObj).get(0).get("data");
					classify = UtilString.getListMapByJson(data);
					for (int i = 0; i < classify.size(); i++) {
						ArrayList<Map<String, String>> ingres = null;
						ingres = UtilString.getListMapByJson(classify.get(i).get("classify"));
						for (int j = 0; j < ingres.size(); j++) {
							ArrayList<Map<String, String>> ingre = UtilString.getListMapByJson(ingres.get(j).get("ingre"));
							Map<String, String> map = ingre.get(0);
							map.put("classifyName", ingres.get(j).get("name"));
							if (i == 0)
								yichiInfo.add(ingre);
							else
								jichiInfo.add(ingre);
						}
						info = classify.get(i).get("info");
						if (i == 0) {
							if (yichiInfo.size() == 0){
								findViewById(R.id.layout_detail_yichi).setVisibility(View.GONE);
								findViewById(R.id.health_detail_btn_yichi).setClickable(false);
							}
							else
								findViewById(R.id.layout_detail_yichi).setVisibility(View.VISIBLE);
							yichi_tv.setText(Tools.getSegmentedStr(info));
							setTableData(yichiInfo, layout_yichi,"宜吃食材点击");
						} else {
							if (jichiInfo.size() == 0) {
								findViewById(R.id.layout_detail_jichi).setVisibility(View.GONE);
								findViewById(R.id.health_detail_btn_jichi).setClickable(false);
							} else
								findViewById(R.id.layout_detail_jichi).setVisibility(View.VISIBLE);
							jichi_tv.setText(Tools.getSegmentedStr(info));
							setTableData(jichiInfo, layout_jichi,"忌吃食材点击");
						}
					}
				}
				loadManager.hideProgressBar();
			}
		});
	}

	protected void setTableData(ArrayList<ArrayList<Map<String, String>>> info, LinearLayout parent,final String two) {
		for (int i = 0; i < info.size(); i++) {
			final ArrayList<Map<String, String>> list = info.get(i);
			// 控件高度
			int height = (ToolsDevice.getWindowPx(DetailHealth.this).widthPixels - Tools.getDimen(DetailHealth.this, R.dimen.dp_72)) / 5;//72=16*2+10*4
			LayoutInflater.from(DetailHealth.this).inflate(R.layout.a_health_table_detail, parent);
			TextView classify_tv = (TextView) parent.getChildAt(i).findViewById(R.id.detail_classify_tv);
			classify_tv.setText(info.get(i).get(0).get("classifyName"));
			// 取控件textView当前的布局参数
			LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) classify_tv.getLayoutParams();
			// 控件的高强制设成
			linearParams.height = height;
			TableLayout table = (TableLayout) parent.getChildAt(i).findViewById(R.id.table_detail_health);
			AdapterSimple adapter = new AdapterSimple(table, list, 
					R.layout.a_health_item_table_detail, 
					new String[] { "img", "name" },
					new int[] { R.id.table_detail_health_img, R.id.table_detail_health_tv });
			adapter.imgHeight = height;
			adapter.imgWidth = height;
			adapter.imgZoom = true;
			SetDataView.view(table, 5, adapter, 
					new int[] { R.id.table_detail_health_img, R.id.table_detail_health_tv },
					new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
						@Override
						public void click(int index, View v) {
							XHClick.mapStat(DetailHealth.this, tongjiId,two,"");
							Map<String, String> ingre = list.get(index);
							Intent intent = new Intent(DetailHealth.this, DetailIngre.class);
							intent.putExtra("name", ingre.get("name"));
							intent.putExtra("code", ingre.get("code"));
							startActivity(intent);
						};
					} });
		}
	}

	public void bottomBarClick(View view) {
		int dp_15 = Tools.getDimen(DetailHealth.this, R.dimen.dp_15);
		switch (view.getId()) {
		case R.id.health_detail_btn_jieshao:
			scrollView.smoothScrollTo(0, 0);
			break;
		case R.id.health_detail_btn_yichi:
			scrollView.smoothScrollTo(0, detail_tv.getHeight() - dp_15);
			break;
		case R.id.health_detail_btn_jichi:
			int yichiHeight = ((LinearLayout)findViewById(R.id.layout_detail_yichi)).getHeight();
			scrollView.smoothScrollTo(0, detail_tv.getHeight() + yichiHeight - dp_15);
			break;
		case R.id.health_detail_btn_caipu:
			XHClick.mapStat(DetailHealth.this, tongjiId, "宜吃菜谱", "");
			Intent intent = new Intent(DetailHealth.this, ListDish.class);
			intent.putExtra("name", name+"养生宜吃");
			intent.putExtra("type", datatype);
			intent.putExtra("g1", code);
			startActivity(intent);
			break;
		}
	}
	
	private void doShare() {
		XHClick.mapStat(DetailHealth.this, "a_share400", "养生", "宜吃忌吃详情");
		barShare = new BarShare(DetailHealth.this, "宜吃忌吃详情","养生");
		String type = BarShare.IMG_TYPE_RES;
		String title = name + "吃什么好，超有用！";
		String clickUrl = StringManager.wwwUrl + "jiankang/" + code;
		// 我在香哈菜谱发现可以按照时辰养生，#标题#，懂得呵护自己，时刻关注养生。
		String content = "我在看香哈菜谱【" + name + "饮食宜忌】，看起来超级实用，推荐你也试试~";
		String imgUrl = ico_id == 0 ? "" + R.drawable.share_launcher : "" + ico_id;
		barShare.setShare(type, title, content, imgUrl, clickUrl);
		barShare.openShare();
	}

}
