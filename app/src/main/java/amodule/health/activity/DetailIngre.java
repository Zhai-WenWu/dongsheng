 
package amodule.health.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;



import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.LayoutScroll;
import acore.widget.ScrollviewDish;
import acore.widget.ScrollviewDish.onScrollViewChange;
import amodule.health.adapter.AdapterPager;
import amodule.search.avtivity.HomeSearch;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import aplug.feedback.activity.Feedback;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.tools.AdPlayIdConfig;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

import static com.xiangha.R.id.ingre_taboo_noData;

/**
 * Title:DetailIngre.java Copyright: Copyright (c) 2014~2017
 * 功效与作用，相克与宜搭
 * @author zeyu_t
 * @date 2014年10月14日
 */
@SuppressLint("InflateParams")
public class DetailIngre extends BaseActivity {
	private ViewPager viewPager = null;
	private TableLayout calorie;
	private LinearLayout ll_info,ingre_taboo_data_layout;
	private ImageView iv_ingerImage;
	private TextView tv_noData_taboo, tv_noData_info, ingre_about_caipu_taboo, ingre_about_caipu_info, fankui_taboo,
			fankui_info, titleV;
	private ScrollviewDish scrollView_info,scrollView_taboo;
	private View[] contentViews = new View[2];
	private TextView[]  tv_tags=new TextView[2];
	
	private Intent intent_feek ;
	private Map<String, String> ingreMap = new HashMap<>();
	private Map<String, String> tabooMap = new HashMap<>();//相克临时数据集合
	public String code = "", ingre = "", page = "0";
	private boolean info_loadOver = false, taboo_loadOver = false;
	
	private String tongjiId = "a_Ingredients";
	private LayoutScroll scrollLayout;
	
	private int statusBarHeight = 0;
	private RelativeLayout adTipLayout;
	private AdsShow adBurden;
	private boolean isShow=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			code = bundle.getString("code");
			ingre = bundle.getString("name");
			if (bundle.getString("page") != null) {
				page = bundle.getString("page");
			}
		}
		intent_feek = new Intent(DetailIngre.this, Feedback.class);
		initActivity(ingre, 2, 0, R.layout.c_view_bar_title, R.layout.a_ingre_detial);
		titleV = (TextView) findViewById(R.id.title);
		initBarView();
		init();
		XHClick.track(DetailIngre.this,"浏览食材详情页");
	}

	private void initBarView() {
		// 分享功能
		ImageView img_share = (ImageView) findViewById(R.id.rightImgBtn2);
		img_share.setVisibility(View.VISIBLE);
		img_share.setImageResource(R.drawable.z_z_topbar_ico_share);
		img_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doShare();
			}
		});
	}

	private void init() {
		// 配置viewPager
		viewPager = (ViewPager) findViewById(R.id.ingre_detail_viewPager);

		TextView tv_info = (TextView) findViewById(R.id.ingre_detail_info);
		TextView tv_taboo = (TextView) findViewById(R.id.ingre_detail_taboo);
		tv_tags[0]=tv_info;
		tv_tags[1]=tv_taboo;
		if (page.equals("0"))
			tv_tags[0].setTextColor(Color.parseColor("#333333"));
		else
			tv_tags[1].setTextColor(Color.parseColor("#333333"));
		
		ll_info = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.a_ingre_detial_info, null);
		scrollView_info = (ScrollviewDish) ll_info.findViewById(R.id.ingre_info_scroll);
		calorie = (TableLayout) ll_info.findViewById(R.id.ingre_calorie);
		iv_ingerImage = (ImageView) ll_info.findViewById(R.id.inger_detail_image);
		fankui_info = (TextView) ll_info.findViewById(R.id.fankui_info);
		tv_noData_info = (TextView) ll_info.findViewById(R.id.ingre_info_noData);
		ingre_about_caipu_info = (TextView) ll_info.findViewById(R.id.ingre_about_caipu_info);
		ingre_about_caipu_info.setText(ingre + "相关菜谱");
		scrollView_info.setonScrollViewChange(new onScrollViewChange() {
			int screenHeight = ToolsDevice.getWindowPx(getApplicationContext()).heightPixels;
			@Override
			public void onScrollChanged(int l, int t, int oldl, int oldt) {
				if (adTipLayout == null || adBurden == null)
					return;
				int[] location = new int[2];
				adTipLayout.getLocationOnScreen(location);
				adBurden.isOnScreen(location[1] > statusBarHeight && location[1] < screenHeight);
			}
		});
		contentViews[0] = ll_info;

		RelativeLayout ll_taboo = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.a_ingre_detial_taboo, null);
		scrollView_taboo = (ScrollviewDish) ll_taboo.findViewById(R.id.ingre_taboo_scroll);
		ingre_taboo_data_layout=(LinearLayout) ll_taboo.findViewById(R.id.ingre_taboo_data_layout);
		fankui_taboo = (TextView) ll_taboo.findViewById(R.id.fankui_taboo);
		tv_noData_taboo = (TextView) ll_taboo.findViewById(ingre_taboo_noData);
		ingre_about_caipu_taboo = (TextView) ll_taboo.findViewById(R.id.ingre_about_caipu_taboo);
		ingre_about_caipu_taboo.setText(ingre + "相关菜谱");
		contentViews[1] = ll_taboo;

		AdapterPager pagerAdapter = new AdapterPager(contentViews);
		viewPager.setAdapter(pagerAdapter);
		setListener();
		if(page.equals("0"))
			contentLoad();//预加载（）
		initAd();
		scrollLayout = (LayoutScroll)findViewById(R.id.scroll_body);
		scrollLayout.setTouchView(scrollView_info);
	}

	/**
	 * 当前不再使用广告
	 */
	private void initAd(){
		adTipLayout = (RelativeLayout)ll_info.findViewById(R.id.ingre_detial_ad_layout);
		BannerAd bannerAdBurden = new BannerAd(this,"other_restain", adTipLayout);
		AdParent[] adsParent = {bannerAdBurden};
		adBurden = new AdsShow(adsParent,AdPlayIdConfig.DETAIL_INGRE);
		mAds = new AdsShow[]{adBurden};
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 设置加载
		viewPager.setCurrentItem(Integer.valueOf(page));
		Rect outRect = new Rect();
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
		statusBarHeight = outRect.top;
	}

	private void setListener() {
		for(int i=0;i<tv_tags.length;i++){
			final int index=i;
			tv_tags[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					viewPager.setCurrentItem(index);
				}
			});
		}
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override 
			public void onPageSelected(int position) {
				tv_tags[position].setTextColor(Color.parseColor("#333333"));
				tv_tags[1-position].setTextColor(Color.parseColor("#999999"));
				findViewById(R.id.ingre_detail_taboo_line).setVisibility(position == 1 ? View.VISIBLE : View.GONE);
				findViewById(R.id.ingre_detail_info_line).setVisibility(position == 1 ? View.GONE : View.VISIBLE);
				contentLoad();
				if(position == 1) {
					if(isShow)
					scrollLayout.setTouchView(scrollView_taboo);
				}else scrollLayout.setTouchView(scrollView_info);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	//点击选项加载内容
	private void contentLoad() {
		int tabId = viewPager.getCurrentItem();
		switch (tabId) {
		case 0:
			//统计
			XHClick.onEventValue(this, "pageIngre", "pageIngre", "功效作用",	1);
			getIngerInfo("info");
			break;
		case 1:
			//统计
			XHClick.onEventValue(this, "pageIngre", "pageIngre", "相克宜搭",	1);
			getIngerInfo("taboo");
			break;
		}
	}

	/**
	 * 获取食材详情
	 * @param type
	 */
	private void getIngerInfo(final String type) {
		//有数据不请求网络
		if("info".equals(type)&&info_loadOver&&ingreMap.size()>0){
			setIngerInfo(ingreMap);
			return;
		}
		if("taboo".equals(type)&&taboo_loadOver&&tabooMap.size()>0){
			setIngerXiangkeYida(UtilString.getListMapByJson(tabooMap.get("taboo")));
			return;
		}
		String url = StringManager.api_ingreInfo + "?code=" + code + "&type=" + type;
		ReqInternet.in().doGet(url, new InternetCallback(this.getApplicationContext()) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ingreMap = UtilString.getListMapByJson(returnObj).get(0);
					if ("info".equals(type) && !info_loadOver) {
						setIngerInfo(ingreMap);
						info_loadOver = true;
					}
					if ("taboo".equals(type) && !taboo_loadOver) {
						setIngerXiangkeYida(UtilString.getListMapByJson(ingreMap.get("taboo")));
						tabooMap=ingreMap;
						taboo_loadOver = true;
					}
					loadManager.hideProgressBar();
				} else {
					showNotFound(type);
				}
				initScroll();
			}
		});
	}

	private void initScroll(){
		// 设置滚动相关
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
				int searchHeight = (int) getResources().getDimension(R.dimen.dp_36);
//				int scrollHeight=getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
				int scrollHeight=ToolsDevice.getWindowPx(DetailIngre.this).heightPixels;
				scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,scrollHeight));
				scrollLayout.init(searchHeight);
//			}
//		},100);
	}

	// 获取食材详情
	private void setIngerInfo(Map<String, String> ingreInfo) {
		if (ingreInfo.size() == 0) {
			showNotFound("info");
		} else {
			ArrayList<Map<String, String>> infos = UtilString.getListMapByJson(ingreInfo.get("info"));
			String ingerImg = ingreInfo.get("img");
			if (ingerImg.length() > 0) {
				BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
					.load(ingerImg)
					.setImageRound(ToolsDevice.dp2px(DetailIngre.this, 5000))
					.build();
				if(bitmapRequest != null)
					bitmapRequest.into(new SubBitmapTarget(){
						@Override
						public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
							iv_ingerImage.setImageBitmap(bitmap);
							iv_ingerImage.setVisibility(View.VISIBLE);
						}
					});
			}
			LinearLayout layout = (LinearLayout) ll_info.findViewById(R.id.inger_detail_layout);
			for (int i = 0; i < infos.size(); i += 2) {
				if (layout.getChildCount() > 0 & i == 0) {
					layout.removeAllViews();
				}
				// 标题
				TextView text = new TextView(getApplicationContext());
//				float size = Tools.getDimenSp(this, R.dimen.sp_18);
				text.setPadding(0, 0, 0, Tools.getDimen(this, R.dimen.dp_7_5));
				text.setTextSize(Tools.getDimenSp(this, R.dimen.sp_18));
				text.setText(Html.fromHtml(infos.get(i).get("")));
				text.setTextColor(Color.parseColor("#000000"));
				layout.addView(text);
				// 内容
				text = new TextView(getApplicationContext());
				text.setPadding(0, 0, 0, Tools.getDimen(this, R.dimen.dp_20));
				text.setLineSpacing(Tools.getDimen(this, R.dimen.dp_5), 1);

				text.setTextSize(Tools.getDimenSp(this, R.dimen.sp_16));
				text.setTextColor(Color.parseColor("#555555"));
				text.setText(infos.get(i + 1).get(""));
				layout.addView(text);
			}
			// 列表加载
			final ArrayList<Map<String, String>> calorieInfo;
			if (UtilString.getListMapByJson(ingreInfo.get("element")) != null) {
				calorieInfo = UtilString.getListMapByJson(ingreInfo.get("element"));
				for (int i = 0; i < calorieInfo.size(); i++) {
					if (calorieInfo.get(i).get("pinyin").length() < 1)
						calorieInfo.get(i).put("searchIco", "hide");
					else
						calorieInfo.get(i).put("searchIco", "ico" + R.drawable.z_xiangke_ico_so);
				}
				AdapterSimple adapter = new AdapterSimple(calorie, calorieInfo, 
						R.layout.table_cell_calorie, 
						new String[] { "name","content", "searchIco" }, 
						new int[] { R.id.itemText1, R.id.itemText2, R.id.itemImg });
				if (calorie.getChildCount() > 1) {
					calorie.removeAllViews();
				}
				SetDataView.view(calorie, 1, adapter, null, new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
					@Override
					public void click(int index, View v) {
						Map<String, String> ingre = calorieInfo.get(index);
						Intent intent = new Intent(DetailIngre.this, ElementHealth.class);
						intent.putExtra("name", ingre.get("name"));
						intent.putExtra("pinyin", ingre.get("pinyin"));
						startActivity(intent);
						XHClick.mapStat(DetailIngre.this, tongjiId, "功效与作用", "热量表的点击");
					}
				} });
			}
			if (calorie.getChildCount() > 1) {
				calorie.setVisibility(View.VISIBLE);
			}
			tv_noData_info.setVisibility(View.GONE);
			tv_noData_taboo.setVisibility(View.GONE);
			ingre_about_caipu_info.setVisibility(View.VISIBLE);
			fankui_info.setVisibility(View.VISIBLE);
			// 图片加载完成后消失对话框;
		}
	}

	// 获取食材相克/宜搭
	private void setIngerXiangkeYida(ArrayList<Map<String, String>> tabooList) {
		if (tabooList.size() == 0) {
			showNotFound("taboo");
		} else {
			LinearLayout ll_xiangke = (LinearLayout) findViewById(R.id.ingre_taboo_xiangke);
			LinearLayout ll_yida = (LinearLayout) findViewById(R.id.ingre_taboo_yida);
			findViewById(R.id.ingre_taboo_xiangke_title).setVisibility(View.GONE);
			findViewById(R.id.ingre_taboo_yida_title).setVisibility(View.GONE);
			// 生成内容
			for (int i = 0; i < tabooList.size(); i++) {
				if (ll_yida.getChildCount() > 0 & i == 0) {
					ll_yida.removeAllViews();
				}
				if (ll_xiangke.getChildCount() > 0 & i == 0) {
					ll_xiangke.removeAllViews();
				}
				Map<String, String> map = tabooList.get(i);
				TextView contentText = new TextView(DetailIngre.this);
				contentText.setClickable(true);
				contentText.setPadding(0, 
						Tools.getDimen(this, R.dimen.dp_12),
						0, 
						Tools.getDimen(this, R.dimen.dp_7));//12=7+5
				contentText.setTextColor(Color.parseColor("#000000"));
				contentText.setTextSize(Tools.getDimenSp(this, R.dimen.sp_16));
				contentText.setLineSpacing(Tools.getDimen(this, R.dimen.dp_5), 1);
				if (map.get("state").equals("1")) {
					// 相克
					contentText.setText((Html.fromHtml("<font color='#999999'>" + ingre + "+</font>" + "<font color='#DD4545'>"
							+ map.get("name") + "</font>" + "<font color='#666666'>" + ":" + "</font><font color='#555555'>"
							+ map.get("content") + "</font>")));
					ll_xiangke.addView(contentText);
					ll_xiangke.setVisibility(View.VISIBLE);
					findViewById(R.id.ingre_taboo_xiangke_title).setVisibility(View.VISIBLE);
				} else {
					// 宜搭
					contentText.setMovementMethod(LinkMovementMethod.getInstance());
					contentText.setText(addClickablePart(ingre, map.get("name"), map.get("content"), map.get("code")));
					ll_yida.addView(contentText);
					ll_yida.setVisibility(View.VISIBLE);
					findViewById(R.id.ingre_taboo_yida_title).setVisibility(View.VISIBLE);
				}
			}
			isShow=true;
			ingre_about_caipu_taboo.setVisibility(View.VISIBLE);
			if (Tools.getMeasureHeight(ingre_taboo_data_layout) + Tools.getDimen(this, R.dimen.dp_85) + Tools.getMeasureHeight(fankui_taboo) 
					< ToolsDevice.getWindowPx(this).heightPixels) {
				findViewById(R.id.fankui_taboo_2).setVisibility(View.GONE);
				fankui_taboo.setVisibility(View.VISIBLE);
				isShow=false;
				scrollLayout.setTouchView(tv_noData_taboo);
			} else {
				findViewById(R.id.fankui_taboo_2).setVisibility(View.GONE);
				fankui_taboo.setVisibility(View.VISIBLE);
				isShow=true;
				scrollLayout.setTouchView(scrollView_taboo);
			}
			tv_noData_info.setVisibility(View.GONE);
			tv_noData_taboo.setVisibility(View.GONE);
			ingre_taboo_data_layout.setVisibility(View.VISIBLE);
		}
	}

	// 显示未找到信息
	private void showNotFound(String type) {
		loadManager.hideProgressBar();
		if (type.equals("info")) {
			tv_noData_info.setText("暂无食材数据!");
			tv_noData_info.setVisibility(View.VISIBLE);
			ingre_about_caipu_info.setVisibility(View.GONE);
			fankui_info.setVisibility(View.GONE);
		} else if (type.equals("taboo")) {
			tv_noData_taboo.setText("暂无相克数据!");
			tv_noData_taboo.setVisibility(View.VISIBLE);
			isShow=false;
			ingre_taboo_data_layout.setVisibility(View.GONE);
		}
	}

	public void bottomClick(View v) {
		switch (v.getId()) {
		case R.id.ingre_about_caipu_info:
		case R.id.ingre_about_caipu_taboo:
			Intent intent = new Intent(DetailIngre.this, HomeSearch.class);
			intent.putExtra("type", "caipu");
			intent.putExtra("code", code);
			intent.putExtra("s", ingre);
			intent.putExtra("from", "食材相关菜谱");
			startActivity(intent);
			int tabId = viewPager.getCurrentItem();
			switch (tabId) {
			case 0:
				XHClick.mapStat(DetailIngre.this, tongjiId, "功效与作用", "相关菜谱的点击");
				break;
			case 1:
				//统计
				XHClick.mapStat(DetailIngre.this, tongjiId, "相克/宜搭", "相关菜谱的点击");
				break;
			}
			break;
		case R.id.fankui_info:
			try {
				intent_feek.putExtra("feekUrl", "http://www.xiangha.com/shicai/"+URLEncoder.encode(ingre, "utf-8"));
				startActivity(intent_feek);
			} catch (UnsupportedEncodingException e) {
				UtilLog.reportError("URLEncoder异常", e);
			}
			break;
		case R.id.fankui_taboo:
			intent_feek.putExtra("feekUrl", "http://www.xiangha.com/xiangke/"+code);
			startActivity(intent_feek);
			break;
		case R.id.fankui_taboo_2:
			intent_feek.putExtra("feekUrl", "http://www.xiangha.com/xiangke/"+code);
			startActivity(intent_feek);
			break;
		}
	}

	// 分享搜索内容
	private void doShare() {
		XHClick.mapStat(this, "a_share400", "食材", "");
		if (ingreMap.size() == 0) {
			Tools.showToast(this, "正在加载数据,请稍后...");
			return;
		}
		String type, title = "", content = "", imgUrl, clickUrl = StringManager.wwwUrl;
		type = BarShare.IMG_TYPE_WEB;
		imgUrl = ingreMap.get("img");
		switch (viewPager.getCurrentItem()) {
		case 0:
			barShare = new BarShare(this, "功效与作用","食材");
			title = ingreMap.get("name") + "的营养功效，你可能不知道！";
			clickUrl += "shicai/" + ingreMap.get("code");
			content = "现在才知道原来吃" + ingreMap.get("name") + "有这么多好处，推荐你也看看。（香哈菜谱）";
			break;
		case 1:
			if (ingreMap.get("taboo")==null||ingreMap.get("taboo").length() < 5) {
				Tools.showToast(this, "这个食材没有相克信息哦~");
				break;
			}
			barShare = new BarShare(this, "相克/宜搭","食材");
			title = "禁忌！" + ingreMap.get("name") + "食物相克大全";
			clickUrl += "xiangke/" + ingreMap.get("code");
			content = "我在看香哈菜谱【" + ingreMap.get("name") + "相克大全】，平时一些搭配可能是错的，推荐一下~ ";
			break;
		}
		if (title.length() > 0 && barShare != null) {
			barShare.setShare(type, title, content, imgUrl, clickUrl);
			barShare.openShare();
		}
	}

	public void loadData() {
		viewPager.setCurrentItem(viewPager.getCurrentItem());
	}

	/**
	 * 
	 * @param name 搜索词
	 * @param name2 相克/宜搭词
	 * @param content 描述
	 * @return
	 */
	private SpannableStringBuilder addClickablePart(final String name, final String name2, String content, final String ingre_code) {
		SpannableStringBuilder spannableString = new SpannableStringBuilder(name + "+");
		spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")), 
				0, spannableString.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		SpannableStringBuilder style = new SpannableStringBuilder(name2);
		SpannableStringBuilder content_new = new SpannableStringBuilder(":"+content);
		content_new.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), 
				0, content_new.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		style.setSpan(new ClickableSpan() {

			@Override
			public void onClick(View v) {
				titleV.setText(name2);
				ingre = name2;
				code = ingre_code;
				info_loadOver = false;
				taboo_loadOver = false;
				contentLoad();
				XHClick.mapStat(DetailIngre.this, tongjiId, "相克/宜搭", "宜搭食材的点击");
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				// 设置文本颜色
				ds.setColor(Color.parseColor("#51A011"));
				// 去掉下划线
				ds.setUnderlineText(false);
			}

		}, 0, style.length(), 0);
		return spannableString.append(style.append(content_new));
	} // end of addClickablePart
}
