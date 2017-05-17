package third.mall.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.SubBitmapTarget;
import aplug.basic.LoadImage;
import aplug.imageselector.ShowImageActivity;
import third.mall.adapter.AdapterRecommed;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallAddShopping;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.tool.ToolView;
import third.mall.view.DetailGetFavorableView;
import third.mall.view.ViewPromotion;
import third.mall.widget.MyScrollView;
import third.mall.widget.MyScrollView.ScrollViewInterface;
import third.mall.widget.ScrollViewContainer;
import third.mall.widget.ScrollViewContainer.ScrollviewContaninerInter;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 商品详情页
 *
 * @author yu
 */
public class CommodDetailActivity extends BaseActivity implements OnClickListener {

	private ViewPager viewpager;
	private ImageView[] imageviews;
	private WebView explain_detail_webview;
	private Map<String, String> map;
	private String code;
	private MyScrollView mall_commod_scroll;
	private Handler handler;
	private static final int SHOW_OK = 1;
	private ArrayList<Map<String, String>> images;
	private ScrollViewContainer mall_ScrollViewContainer;
	private boolean load_state = true;
	private ImageView commod_add;
	private TextView mall_news_num;
	private TextView commod_shop;
	private MallCommon common;
	private HorizontalScrollView product_recommed_hsv;
	private LinearLayout product_recommed_ll;
	private RelativeLayout commod_title, bar_title;
	private boolean product_deserve_state = false;
	private TextView title;
	private Rect scrollBounds;
	private RelativeLayout share_layout;
	private RelativeLayout back;
	private ImageView leftImgBtn;
	private ImageView img_share;
	private TextView mall_news_num_two;
	private Map<String, String> map_statistic = new HashMap<String, String>();
	private String actionUrl;
	private String mall_stat_statistic;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			code = bundle.getString("product_code");
			for (int i = 1; i < 100; i++) {
				if (!TextUtils.isEmpty(bundle.getString("fr" + i))) {
					map_statistic.put("fr" + i, bundle.getString("fr" + i));
					if (!TextUtils.isEmpty(bundle.getString("fr" + i + "_msg"))) {
						map_statistic.put("fr" + i + "_msg", bundle.getString("fr" + i + "_msg"));
					}
				} else {
					break;
				}
			}
			if (!TextUtils.isEmpty(bundle.getString("xhcode"))) {
				map_statistic.put("xhcode", bundle.getString("xhcode"));
			}
		}
		initActivity("商品详情", 3, 0, 0, R.layout.a_mall_commod_detail);
		common = new MallCommon(this);
		initView();
		initData();
		initTitle();
		XHClick.track(this,"浏览商品");
	}

	private void initTitle() {
		if (Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}

	/**
	 * 初始化布局
	 */
	private void initView() {
		findViewById(R.id.linear_buy).setVisibility(View.GONE);
		commod_title = (RelativeLayout) findViewById(R.id.commod_title);
		bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
		commod_title.getBackground().setAlpha(0);
		bar_title.getBackground().setAlpha(0);
		title = (TextView) findViewById(R.id.title);

		explain_detail_webview = (WebView) findViewById(R.id.explain_detail_webview);
		explain_detail_webview.getSettings().setJavaScriptEnabled(true);// 执行角标文件
		explain_detail_webview.setScrollBarStyle(0);// 滚动条风格，为0就是不给滚动条留空间，滚动条覆盖在网页上
		explain_detail_webview.setHorizontalScrollBarEnabled(false);
		explain_detail_webview.setVerticalScrollBarEnabled(true);
		explain_detail_webview.getSettings().setDefaultTextEncodingName("UTF-8");

		// LinearLayout commod_consult=(LinearLayout)
		// findViewById(R.id.commod_consult);
		// commod_consult.setOnClickListener(this);
		findViewById(R.id.commod_mercat).setOnClickListener(this);
		findViewById(R.id.home_mercat).setOnClickListener(this);
		findViewById(R.id.commod_shop_linear).setOnClickListener(this);
		back = (RelativeLayout) findViewById(R.id.back);
		back.setOnClickListener(this);
		leftImgBtn = (ImageView) findViewById(R.id.leftImgBtn);
		img_share = (ImageView) findViewById(R.id.img_share);
		share_layout = (RelativeLayout) findViewById(R.id.share_layout);
		share_layout.setOnClickListener(this);
		findViewById(R.id.share_layout).setVisibility(View.GONE);
		mall_commod_scroll = (MyScrollView) findViewById(R.id.mall_commod_scroll);
		commod_shop = (TextView) findViewById(R.id.commod_shop);
		commod_shop.setOnClickListener(this);
		commod_add = (ImageView) findViewById(R.id.commod_add);
		mall_news_num = (TextView) findViewById(R.id.mall_news_num);
		mall_news_num_two = (TextView) findViewById(R.id.mall_news_num_two);
		setShopcatNum();
		scrollBounds = new Rect();
		mall_commod_scroll.getHitRect(scrollBounds);
		mall_commod_scroll.setInterfaceSv(new ScrollViewInterface() {

			@Override
			public void setYandState(float y, boolean state) {
				if (viewpager.getLocalVisibleRect(scrollBounds)) {
					commod_title.getBackground().setAlpha(0);
					bar_title.getBackground().setAlpha(0);
					title.setText("");
					leftImgBtn.setImageResource(R.drawable.z_z_topbar_ico_back_white);
					img_share.setImageResource(R.drawable.z_z_topbar_ico_share);
					back.setBackgroundResource(R.drawable.mall_product_detail_back);
					share_layout.setBackgroundResource(R.drawable.mall_product_detail_back);
				} else {
					commod_title.getBackground().setAlpha(255);
					bar_title.getBackground().setAlpha(255);
					String color = Tools.getColorStr(CommodDetailActivity.this,R.color.common_top_bg);
					bar_title.setBackgroundColor(Color.parseColor(color));
					commod_title.setBackgroundColor(Color.parseColor(color));
					title.setText("商品详情");
					back.setBackgroundColor(Color.parseColor("#00ffffff"));
					share_layout.setBackgroundColor(Color.parseColor("#00ffffff"));
//					leftImgBtn.setImageResource(R.drawable.z_z_topbar_ico_back);
//					img_share.setImageResource(R.drawable.z_z_topbar_ico_share_red);
				}
			}
		});

		mall_ScrollViewContainer = (ScrollViewContainer) findViewById(R.id.mall_ScrollViewContainer);
		mall_ScrollViewContainer.setInterface(new ScrollviewContaninerInter() {

			@Override
			public void setState(int state) {
				if (load_state) {
					explain_detail_webview.loadUrl(MallStringManager.replaceUrl(MallStringManager.mall_web_product_detail) + "?product_code=" + code);
					XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods","上拉查看详细介绍","");
				}
			}

			@Override
			public void changeTitleState(boolean state) {
				commod_title.getBackground().setAlpha(255);
				title.setText("商品详情");
				back.setBackgroundColor(Color.parseColor("#00ffffff"));
				share_layout.setBackgroundColor(Color.parseColor("#00ffffff"));
//				leftImgBtn.setImageResource(R.drawable.z_z_topbar_ico_back);
//				img_share.setImageResource(R.drawable.z_z_topbar_ico_share_red);
			}
		});
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		RelativeLayout viewpager_layout = (RelativeLayout) findViewById(R.id.viewpager_layout);
		viewpager_layout.setLayoutParams(new RelativeLayout.LayoutParams(width, width));

	}

	/**
	 * 展示购物车数量
	 */
	private void setShopcatNum() {
		if (MallCommon.num_shopcat > 0) {
			if (MallCommon.num_shopcat > 9) {
				mall_news_num.setVisibility(View.GONE);
				mall_news_num_two.setVisibility(View.VISIBLE);
				if (MallCommon.num_shopcat > 99)
					mall_news_num_two.setText("99+");
				else
					mall_news_num_two.setText("" + MallCommon.num_shopcat);
			} else {
				mall_news_num.setVisibility(View.VISIBLE);
				mall_news_num_two.setVisibility(View.GONE);
				mall_news_num.setText("" + MallCommon.num_shopcat);
			}
		} else {
			mall_news_num.setVisibility(View.GONE);
			mall_news_num_two.setVisibility(View.GONE);
		}
	}

	/**
	 * 第一次初始化数据
	 */
	@SuppressLint("HandlerLeak")
	private void initData() {

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case SHOW_OK:
						loadManager.hideProgressBar();
						mall_commod_scroll.setVisibility(View.VISIBLE);
						findViewById(R.id.share_layout).setVisibility(View.VISIBLE);
						findViewById(R.id.linear_buy).setVisibility(View.VISIBLE);
						break;
				}
				super.handleMessage(msg);
			}
		};
		// webview 加载监听
		explain_detail_webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				load_state = true;
				findViewById(R.id.widget_progress).setVisibility(View.VISIBLE);
				findViewById(R.id.explain_detail_but_linear).setVisibility(View.GONE);
				findViewById(R.id.explain_detail_linear).setVisibility(View.GONE);
				findViewById(R.id.explain_detail_webview).setVisibility(View.GONE);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				load_state = false;
				findViewById(R.id.widget_progress).setVisibility(View.GONE);
				findViewById(R.id.explain_detail_but_linear).setVisibility(View.VISIBLE);
				findViewById(R.id.explain_detail_linear).setVisibility(View.VISIBLE);
				findViewById(R.id.explain_detail_webview).setVisibility(View.VISIBLE);
				super.onPageFinished(view, url);
			}
		});

		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRequest();
			}
		});
	}

	/**
	 * 请求网络数据
	 */
	private void setRequest() {
		actionUrl = MallStringManager.mall_api_product_info + "?product_code=" + code;
		for (String key : map_statistic.keySet()) {
			actionUrl += "&" + key + "=" + map_statistic.get(key);
		}
		MallReqInternet.in().doGet(actionUrl, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				loadManager.loadOver(flag, 1, true);
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					parseInfo(listMapByJson);
					if (stat != null && stat.length > 0 && !TextUtils.isEmpty((String) stat[0])) {
						mall_stat_statistic = (String) stat[0];
					}
					handler.sendEmptyMessage(SHOW_OK);
				} else {

				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (MallCommon.num_shopcat > 0) {
			mall_news_num.setVisibility(View.VISIBLE);
			if (MallCommon.num_shopcat > 99)
				mall_news_num.setText("99+");
			else
				mall_news_num.setText("" + MallCommon.num_shopcat);
		} else {
			mall_news_num.setVisibility(View.GONE);
		}
	}

	/**
	 * 解析数据
	 *
	 * @param listMapByJson
	 */
	private void parseInfo(ArrayList<Map<String, String>> listMapByJson) {
		map = listMapByJson.get(0);
		//设置商品状态
		setStatus();
		// 设置商品标题
		setProductTitle();
		// 设置价格
		setProductPrice();
		// 设置商品状态-----邮费的不太状态
		setProductState();
		//设置领券
		setGetFavorable();

		// 值得买
		setProductDesrve();

		// 推荐商品
		setRecommedProduct();
		// 商品信息
		setProductDesChunk();
		// 设置满包邮
		setViewPromotion();
		// 轮转图
		images = UtilString.getListMapByJson(map.get("images"));
		initViewPager(images);
		if (map.containsKey("product_introduce_flag") && "2".equals(map.get("product_introduce_flag"))) {
			mall_ScrollViewContainer.setState_two(false);
			findViewById(R.id.explain_detail_but_linear).setVisibility(View.VISIBLE);
		} else {
			mall_ScrollViewContainer.setState_two(true);
			findViewById(R.id.explain_detail_but_linear).setVisibility(View.GONE);
		}
	}

	/**
	 * 设置获取优惠券
	 */
	private void setGetFavorable() {
		RelativeLayout view_getfavorable = (RelativeLayout) findViewById(R.id.view_getfavorable);
		if (map.containsKey("shop_coupon_package") && !TextUtils.isEmpty(map.get("shop_coupon_package")) && !"[]".equals(map.get("shop_coupon_package"))) {
			DetailGetFavorableView view = new DetailGetFavorableView(this);
			view_getfavorable.setVisibility(View.VISIBLE);
			ArrayList<Map<String, String>> list = UtilString.getListMapByJson(map.get("shop_coupon_package"));
			ArrayList<String> strs = new ArrayList<String>();
			for (int i = 0, size = list.size(); i < size; i++) {
				if (list.get(i).containsKey("desc")) {
					strs.add(list.get(i).get("desc"));
				}
			}
			view.setdata(strs, map.get("shop_code"));
			view_getfavorable.addView(view);
		} else view_getfavorable.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置当前商品状态
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setStatus() {
		// 销售
		if ("0".equals(map.get("saleable_num")) || !"2".equals(map.get("status"))) {
			TextView product_lose_tv = (TextView) findViewById(R.id.product_lose_tv);
			if (!"2".equals(map.get("status")))
				product_lose_tv.setText("已下架");
			else if ("0".equals(map.get("saleable_num")))
				product_lose_tv.setText("暂时无货，非常抱歉!");

			commod_shop.setEnabled(false);
			commod_shop.setTextColor(Color.parseColor("#70ffffff"));
			commod_shop.setBackgroundColor(this.getResources().getColor(R.color.comment_color));
			findViewById(R.id.product_lose_rela).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.product_lose_rela).setVisibility(View.GONE);
			commod_shop.setEnabled(true);
			commod_shop.setTextColor(Color.parseColor("#ffffff"));
			commod_shop.setBackgroundColor(this.getResources().getColor(R.color.comment_color));
			commod_shop.setAlpha(255);
		}
	}

	/**
	 * 设置满减，满包邮优惠
	 */
	private void setViewPromotion() {
		findViewById(R.id.view_promotion_line).setVisibility(View.GONE);
		findViewById(R.id.view_promotion).setVisibility(View.GONE);
		if (map.containsKey("shop_postage_desc") && map.containsKey("shop_promotion_desc")) {
			ViewPromotion view_promotion = (ViewPromotion) findViewById(R.id.view_promotion);
			view_promotion.setStyle(ViewPromotion.style_null);
			if (!TextUtils.isEmpty(map.get("shop_postage_desc")) || !TextUtils.isEmpty(map.get("shop_promotion_desc"))) {
				view_promotion.setVisibility(View.VISIBLE);
				findViewById(R.id.view_promotion_line).setVisibility(View.VISIBLE);
				view_promotion.setData(map.get("shop_postage_desc"), map.get("shop_promotion_desc"));
			}
		}

	}

	/**
	 * 设置商品邮费的不同状态 图文混排方式
	 */
	private void setProductState() {
		TextView title_commod_tv = (TextView) findViewById(R.id.title_commod_tv);
		if (map.containsKey("product_character") && !TextUtils.isEmpty(map.get("product_character")) && UtilString.getListMapByJson(map.get("product_character")).size() > 0) {
			ArrayList<Map<String, String>> listmap_charact = UtilString.getListMapByJson(map.get("product_character"));
			String content = "";
			for (int i = 0; i < listmap_charact.size(); i++) {
				content += "<img src='" + R.drawable.mall_product_postage_numbal + "'/>&nbsp;" + listmap_charact.get(i).get("char_title") + "&nbsp;&nbsp;&nbsp;&nbsp;";
			}
			title_commod_tv.setVisibility(View.VISIBLE);
			findViewById(R.id.title_commod_line).setVisibility(View.VISIBLE);
			title_commod_tv.setText(Html.fromHtml(content, getImageGetterInstance(), null));
		} else {
			title_commod_tv.setVisibility(View.GONE);
			findViewById(R.id.title_commod_line).setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * ImageGetter用于text图文混排
	 *
	 * @return
	 */
	public ImageGetter getImageGetterInstance() {
		ImageGetter imgGetter = new Html.ImageGetter() {
			@Override
			public Drawable getDrawable(String source) {
				int dp_15 = (int) CommodDetailActivity.this.getResources().getDimension(R.dimen.dp_15);
				int id = Integer.parseInt(source);
				Drawable d = getResources().getDrawable(id);
				d.setBounds(0, 0, dp_15, dp_15);
				return d;
			}
		};
		return imgGetter;
	}

	/**
	 * 设置标题内容
	 */
	private void setProductTitle() {
		// 标题内容
		TextView title_commod_content = (TextView) findViewById(R.id.title_commod_content);
		title_commod_content.setText(map.get("title"));
		// 标题描述
		TextView title_commod_content_sub = (TextView) findViewById(R.id.title_commod_content_sub);
		if (map.containsKey("sub_title") && !TextUtils.isEmpty(map.get("sub_title"))) {
			title_commod_content_sub.setVisibility(View.VISIBLE);
			title_commod_content_sub.setText(map.get("sub_title"));
		} else {
			title_commod_content_sub.setVisibility(View.GONE);
		}
	}

	/**
	 * 设置价格
	 */
	private void setProductPrice() {
		// 促销价
		TextView title_commod_price_now = (TextView) findViewById(R.id.title_commod_price_now);
		title_commod_price_now.setText("¥" + map.get("discount_price"));
		// 正常价格
		TextView title_commod_price_before = (TextView) findViewById(R.id.title_commod_price_before);
		title_commod_price_before.setText("¥" + map.get("price"));
		title_commod_price_before.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		if (map.get("price").equals(map.get("discount_price"))) {
			title_commod_price_before.setVisibility(View.GONE);
		} else {
			title_commod_price_before.setVisibility(View.VISIBLE);
		}
		// 邮费
		TextView title_commod_price_postage = (TextView) findViewById(R.id.title_commod_price_postage);
		title_commod_price_postage.setText(map.get("product_postage_desc"));
		// 邮费说明
		TextView title_saled_num = (TextView) findViewById(R.id.title_saled_num);
		title_saled_num.setText("已售" + map.get("saled_num"));
	}

	/**
	 * 商品信息描述
	 */
	private void setProductDesChunk() {
		findViewById(R.id.product_explain_rela).setVisibility(View.VISIBLE);
		findViewById(R.id.product_goto).setOnClickListener(this);
		if (map.containsKey("description_chunk") && !TextUtils.isEmpty(map.get("description_chunk")) && UtilString.getListMapByJson(map.get("description_chunk")).size() > 0) {
			ArrayList<Map<String, String>> listmap_des = UtilString.getListMapByJson(map.get("description_chunk"));
			for (int i = 0; i < listmap_des.size(); i++) {
				String content = listmap_des.get(i).get("key");
				content = content.replace("<", "【");
				content = content.replace(">", "】");
				listmap_des.get(i).put("key", content);
			}
			// 信息列表
			findViewById(R.id.explain_tv_content).setVisibility(View.GONE);
			findViewById(R.id.product_explain).setVisibility(View.VISIBLE);
			TableLayout product_explain = (TableLayout) findViewById(R.id.product_explain);
			AdapterSimple simple = new AdapterSimple(product_explain, listmap_des, R.layout.a_mall_product_explain_item, new String[]{"key", "value"}, new int[]{R.id.tv_title, R.id.tv_content});
			SetDataView.view(product_explain, 1, simple, null, null);
		} else {
			// 说明
			findViewById(R.id.explain_tv_content).setVisibility(View.VISIBLE);
			findViewById(R.id.product_explain).setVisibility(View.GONE);
			TextView explain_tv_content = (TextView) findViewById(R.id.explain_tv_content);
			explain_tv_content.setText(map.get("description"));
		}
	}

	/**
	 * 处理值得买
	 */
	private void setProductDesrve() {
		if (map.containsKey("product_copy") && !TextUtils.isEmpty(map.get("product_copy"))) {
			ArrayList<Map<String, String>> list_product_copy = UtilString.getListMapByJson(map.get("product_copy"));
			final TextView product_deserve_content = (TextView) findViewById(R.id.product_deserve_content);
			ImageView product_deserve_iv = (ImageView) findViewById(R.id.product_deserve_iv);
			String image = list_product_copy.get(0).get("logo");
			setImageView(product_deserve_iv, image, true);
			final String desc = "值得买理由：" + list_product_copy.get(0).get("desc");
			// String content=desc;
			String content = desc + "";
			int num = setTextViewNum();
			int now_num = (int) (num * 2.6);
			if (content.length() > now_num) {
				product_deserve_state = true;
				content = content.substring(0, now_num);
				content += "...";
			} else {
				product_deserve_state = false;
			}
			product_deserve_content.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (product_deserve_state) {
						product_deserve_content.setText(desc);
						setTextViewColor(product_deserve_content, 6);
					}

				}
			});
			product_deserve_content.setText(content);
			setTextViewColor(product_deserve_content, 6);
			findViewById(R.id.product_deserve).setVisibility(View.VISIBLE);

		} else {
			findViewById(R.id.product_deserve).setVisibility(View.GONE);
		}
	}

	/**
	 * 设置推荐商品
	 */
	private void setRecommedProduct() {
		if (map.containsKey("product_recommend") && !TextUtils.isEmpty(map.get("product_recommend")) && UtilString.getListMapByJson(map.get("product_recommend")).size() > 0) {
			final ArrayList<Map<String, String>> list_product_recommed = UtilString.getListMapByJson(map.get("product_recommend"));
			findViewById(R.id.product_recommed_rela).setVisibility(View.VISIBLE);
			product_recommed_hsv = (HorizontalScrollView) findViewById(R.id.product_recommed_hsv);
			product_recommed_ll = (LinearLayout) findViewById(R.id.product_recommed_ll);
			AdapterRecommed recommed = new AdapterRecommed(product_recommed_hsv, list_product_recommed, R.layout.a_mall_product_recommed_item, new String[]{"img", "title", "price"}, new int[]{
					R.id.recommed_iv, R.id.recommed_content, R.id.recommed_price});
			int width = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_65)) / 3;
			recommed.viewWidth = width + Tools.getDimen(this, R.dimen.dp_1);
			SetDataView.ClickFunc[] expertClick = {new SetDataView.ClickFunc() {
				@Override
				public void click(int index, View v) {
					XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods","猜你喜欢","点击商品");
					MallClickContorl.getInstance().setStatisticUrl(actionUrl, null, mall_stat_statistic, CommodDetailActivity.this);
					Intent intent = new Intent(CommodDetailActivity.this, CommodDetailActivity.class);
					intent.putExtra("product_code", list_product_recommed.get(index).get("product_code"));
					startActivity(intent);
				}
			}};
			SetDataView.horizontalView(product_recommed_hsv, recommed, null, expertClick);
			// 去除最后一个item的paddingRight
			if (recommed.getCount() - 1 >= 0 && product_recommed_ll.getChildCount() > 0) {
				RelativeLayout rl = (RelativeLayout) product_recommed_ll.getChildAt(recommed.getCount() - 1);
				rl.getLayoutParams().width = width;
				rl.setPadding(0, 0, 0, 0);
			}

		} else {
			findViewById(R.id.product_recommed_rela).setVisibility(View.GONE);
		}

	}

	/**
	 * 初始化viewpager
	 *
	 * @param images
	 */
	private void initViewPager(ArrayList<Map<String, String>> images) {
		viewpager = (ViewPager) findViewById(R.id.viewpager);
		LinearLayout point_linear = (LinearLayout) findViewById(R.id.point_linear);
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < images.size(); i++) {
			ImageView iv = new ImageView(this);
			iv.setScaleType(ScaleType.FIT_XY);
			setImageView(iv, images.get(i).get(""), false);
			views.add(iv);
		}
		imageviews = new ImageView[views.size()];
		for (int i = 0; i < views.size(); i++) {
			ImageView iv = new ImageView(this);
			int dp_2_5 = Tools.getDimen(this, R.dimen.dp_2_5);
			iv.setPadding(dp_2_5, 0, dp_2_5, 0);
			imageviews[i] = iv;
			imageviews[i].setImageResource(R.drawable.z_home_banner_bg_pic_white);
			if (i == 0) {
				imageviews[i].setImageResource(R.drawable.z_home_banner_bg_pic_active);
			}
			if (imageviews.length > 1) {
				point_linear.setVisibility(View.VISIBLE);
			}
			point_linear.addView(imageviews[i]);
		}
		viewpager.setAdapter(new MyViewPagerAdapter(views));
		viewpager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				for (int i = 0; i < imageviews.length; i++) {
					imageviews[arg0].setImageResource(R.drawable.z_home_banner_bg_pic_active);
					if (arg0 != i) {
						imageviews[i].setImageResource(R.drawable.z_home_banner_bg_pic_white);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	/**
	 * 加载图片
	 *
	 * @param iv
	 * @param imageUrl
	 * @param state    true 圆形，false正常
	 */
	private void setImageView(final ImageView iv, String imageUrl, final boolean state) {
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
				.load(imageUrl)
				.build();
		if (bitmapRequest != null)
			bitmapRequest.into(new SubBitmapTarget() {
				@Override
				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
					if (state)
						iv.setImageBitmap(ToolView.toRoundBitmap(bitmap));
					else
						iv.setImageBitmap(bitmap);
				}
			});
	}

	/**
	 * viewpager adpater
	 *
	 * @author yu
	 */
	private class MyViewPagerAdapter extends PagerAdapter {

		private List<View> list_iv;

		public MyViewPagerAdapter(List<View> list_iv) {
			super();
			this.list_iv = list_iv;
		}

		@Override
		public int getCount() {
			return list_iv.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(View container, final int position) {
			((ViewPager) container).addView(list_iv.get(position), 0);
			View view = list_iv.get(position);

			// 点击
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("url", images.get(position).get(""));
					intent.setClass(CommodDetailActivity.this, ShowImageActivity.class);
					startActivity(intent);
				}
			});
			return list_iv.get(position);
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(list_iv.get(position));
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	/**
	 * 对话框
	 */
	private void setDialog(ArrayList<String> strs) {
		final Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(R.layout.a_mall_commod_dialog);
		dialog.setCanceledOnTouchOutside(false);
		Window window = dialog.getWindow();
		LinearLayout commod_number_linear = (LinearLayout) window.findViewById(R.id.commod_number_linear);
		for (int i = 0; i < strs.size(); i++) {
			if (i != 0) {
				View lineView = new ImageView(CommodDetailActivity.this);
				lineView.setBackgroundResource(R.color.c_gray_dddddd);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ToolsDevice.dp2px(CommodDetailActivity.this, 0.5f));
				commod_number_linear.addView(lineView, params);
			}
			View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.a_mall_commod_dialog_text, null);
			final TextView textview = (TextView) view.findViewById(R.id.number);
			textview.setText(strs.get(i));
			commod_number_linear.addView(view);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.cancel();
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + textview.getText()));
					startActivity(intent);
				}
			});
		}
		window.findViewById(R.id.commod_dialog_cancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		window.findViewById(R.id.commod_number_dialog).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// case R.id.commod_consult://咨询
			// if(map!=null){
			// Intent intent = new Intent(this, Feedback.class);
			// intent.putExtra("feekUrl", map.get("m_url"));
			// intent.putExtra("from", "4");
			// this.startActivity(intent);
			// }
			// break;
			case R.id.home_mercat://回到主页
				XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "底部导航", "首页");
				AppCommon.openUrl(CommodDetailActivity.this, "xhds.home.app", true);
				CommodDetailActivity.this.finish();
				break;
			case R.id.product_goto:
				MallClickContorl.getInstance().setStatisticUrl(actionUrl, null, mall_stat_statistic, this);
				XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "进入店铺", "");
				String mall_stat_pro = (String) UtilFile.loadShared(this, FileManager.MALL_STAT, FileManager.MALL_STAT);
				String url_pro = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home) + "?shop_code=" + map.get("shop_code") + "&" + mall_stat_pro;
				AppCommon.openUrl(this, url_pro, true);
				break;
			case R.id.commod_mercat:// 商家店铺
				MallClickContorl.getInstance().setStatisticUrl(actionUrl, null, mall_stat_statistic, this);
				XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "底部导航", "店铺");
				String mall_stat = (String) UtilFile.loadShared(this, FileManager.MALL_STAT, FileManager.MALL_STAT);
				String url = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home) + "?shop_code=" + map.get("shop_code") + "&" + mall_stat;
				AppCommon.openUrl(this, url, true);
				break;
			case R.id.commod_shop_linear:// 购物车
				XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "底部导航", "购物车");
				if (LoginManager.isLogin()) {
					MallClickContorl.getInstance().setStatisticUrl(actionUrl, null, mall_stat_statistic, this);
					;
					this.startActivity(new Intent(this, ShoppingActivity.class));
				} else {
					Intent intent_user = new Intent(this, LoginByAccout.class);
					startActivity(intent_user);
					return;
				}
				break;

			case R.id.back:
				this.finish();
				break;
			case R.id.share_layout:
				XHClick.track(CommodDetailActivity.this,"分享商品");
				XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "分享", "");
				doshare();
				break;
			case R.id.commod_shop:// 加入购物车
				if (LoginManager.isLogin()) {
					// addShoppingCommod(map.get("product_code"), "1");
					common.addShoppingcat(this, map.get("product_code"), new InterfaceMallAddShopping() {

						@Override
						public void addProduct(int state) {
							if (state >= 50) {
								XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "底部导航", "加入购物车");
								setAddAnimation();
							}
						}
					});
				} else {
					Intent intent_user = new Intent(this, LoginByAccout.class);
					startActivity(intent_user);
					return;
				}
				break;
//			case R.id.mall_num_rela:
//				if (LoginManager.isLogin()) {
//					MallClickContorl.getInstance().setStatisticUrl(actionUrl, null, mall_stat_statistic, this);
//					this.startActivity(new Intent(this, ShoppingActivity.class));
//				} else {
//					Intent intent_user = new Intent(this, UserLoginOptions.class);
//					startActivity(intent_user);
//					return;
//				}
//				break;
		}
	}

	private void doshare() {
		barShare = new BarShare(this, "商品详情页", "");
		String type = BarShare.IMG_TYPE_WEB;
		String title = map.get("product_share_title");
		String clickUrl = map.get("product_share_url");
		String content = map.get("product_share_desc");
		String imgUrl = images.get(0).get("");
		//分享添加路径统计
		Object msg = UtilFile.loadShared(this, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT);
		ArrayList<Map<String, String>> list = UtilString.getListMapByJson(msg);
		String url_temp = MallStringManager.mall_api_product_info.replace(MallStringManager.mall_apiUrl, "");
		if (list != null && list.size() > 0 && list.get(0).containsKey(url_temp)) {
			clickUrl += "&fr1=" + list.get(0).containsKey(url_temp) + "_share&fr1_msg=" + code;
		}
		barShare.setShare(type, title, content, imgUrl, clickUrl);
		barShare.openShare();
	}

	/**
	 * 设置文字变化
	 *
	 * @param view
	 * @param size
	 */
	private void setTextViewColor(TextView view, int size) {
		SpannableStringBuilder builder = new SpannableStringBuilder(view.getText().toString());
		String color = Tools.getColorStr(this,R.color.comment_color);
		ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor(color));
		builder.setSpan(redSpan, 0, size, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(builder);
	}

	/**
	 * 执行动画
	 */
	private void setAddAnimation() {
		AnimationSet set = new AnimationSet(true);
		AlphaAnimation alpha = new AlphaAnimation(1.0f, 0);
		alpha.setDuration(700);
		alpha.setFillAfter(true);
		set.addAnimation(alpha);
		ScaleAnimation scale = new ScaleAnimation(1f, 0, 1f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(700);
		set.addAnimation(scale);
		commod_add.startAnimation(set);

		alpha.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				commod_add.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				commod_add.setVisibility(View.GONE);
				commod_add.clearAnimation();

			}
		});
		MallCommon.num_shopcat++;

		setShopcatNum();
		ScaleAnimation scale_text = new ScaleAnimation(1f, 1.1f, 1f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scale_text.setDuration(700);
		if (MallCommon.num_shopcat < 9)
			mall_news_num.startAnimation(scale_text);
		else
			mall_news_num_two.startAnimation(scale_text);
	}

	/**
	 * 获取值得买每行的字数
	 *
	 * @return
	 */
	private int setTextViewNum() {
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_14);
		int distance = (int) this.getResources().getDimension(R.dimen.dp_15);
		int img_waith = (int) this.getResources().getDimension(R.dimen.dp_45);

		int waith = wm.getDefaultDisplay().getWidth();
		int tv_waith = waith - distance * 3 - img_waith;
		int tv_pad = ToolView.dip2px(this, 1.0f);
		int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
		return num;
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (level == TRIM_MEMORY_UI_HIDDEN) {//是否ui资源

		}
	}

	@Override
	protected void onPause() {
		commod_title.getBackground().setAlpha(255);
		bar_title.getBackground().setAlpha(255);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mall_ScrollViewContainer = null;
		common = null;

		System.gc();
	}
}
