package amodule.main.view.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.GalleryViewPager;
import acore.widget.ScrollLinearListLayout;
import amodule.dish.activity.VideoDish;
import amodule.main.activity.MainHomePageNew;
import amodule.main.adapter.AdapterTopUser;
import amodule.other.activity.NewClassify;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.AdParent;
import third.ad.AdParent.AdIsShowListener;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.BannerAd.OnBannerListener;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.mall.MainMall;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

/**
 * @author FangRuijiao
 *         首页内容试图控制
 */
public class HomeContentControl {
	private MainHomePageNew mHomePage;
	private float currentX, oldX;//只是用于统计的两个参数

	private final String navigationName[] = {"菜谱分类", "视频", "美食养生", "商城"};
	private final int navigationImg[] = {R.drawable.z_home_main_classification, R.drawable.z_home_main_video,
			R.drawable.z_home_main_live, R.drawable.z_home_main_mall};
	/**
	 * 轮播图View集合
	 */
	private ArrayList<View> views;
	private ArrayList<BannerAd> bannerAds;
	private XHAllAdControl allAdControl;
	private String statistics="index_images_";//统计id

	public HomeContentControl(MainHomePageNew homePage) {
		mHomePage = homePage;
		setNavigation();
	}

	public void setData(Map<String, String> map) {
//		ArrayList<Map<String, String>> subArray = StringManager.getListMapByJson(map.get("slide"));
		// 推广位--轮播
		setDoublePlay();
		//设置热门用户
		setTopUser(StringManager.getListMapByJson(map.get("activeUser")));
		setNouse(UtilString.getListMapByJson(map.get("nous")));
		//初始化广告
		initAd();
		setSpecical(UtilString.getListMapByJson(map.get("topic")));
	}

	private void setNavigation() {
		LinearLayout ll_icon = (LinearLayout) mHomePage.findViewById(R.id.a_home_main_ll_icon);
		DisplayMetrics metrics = ToolsDevice.getWindowPx(mHomePage);
		int mWidthPx = metrics.widthPixels;
		int margin = (mWidthPx - Tools.getDimen(mHomePage, R.dimen.dp_30) * 2 - Tools.getDimen(mHomePage, R.dimen.dp_56) * 4) / 3 / 2;
		for (int i = 0; i < ll_icon.getChildCount(); i++) {
			RelativeLayout chilad = (RelativeLayout) ll_icon.getChildAt(i);
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) chilad.getLayoutParams();
			params.setMargins(margin, 0, margin, 0);
			if (i == 0) params.setMargins(0, 0, margin, 0);
			else if (i == ll_icon.getChildCount() - 1) params.setMargins(margin, 0, 0, 0);
			ImageView ivIcon = (ImageView) chilad.findViewById(R.id.iv_icon);
			TextView tvName = (TextView) chilad.findViewById(R.id.tv_name);
			ivIcon.setImageResource(navigationImg[i]);
			tvName.setText(navigationName[i]);
			chilad.setTag(i + 1);
			chilad.setOnClickListener(navigationClick);
		}
	}

    private OnClickListener navigationClick = ScrollLinearListLayout.getOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = Integer.parseInt(v.getTag().toString());
            switch (tag) {
                case 1:
                    XHClick.track(v.getContext(), "点击首页的菜谱分类");
                    XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "导航", "菜谱分类");
                    Intent dishClass = new Intent(mHomePage, NewClassify.class);
                    dishClass.putExtra("type", "caipu");
                    mHomePage.startActivity(dishClass);
                    break;
                case 2:
					XHClick.track(v.getContext(), "点击首页的视频");
                    XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "导航", "美食视频");
                    Intent video = new Intent(mHomePage, VideoDish.class);
                    mHomePage.startActivity(video);
                    break;
                case 3:
					XHClick.track(v.getContext(), "点击首页的美食养生");
                    XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "导航", "美食养生");
                    Intent live = new Intent(mHomePage, NewClassify.class);
                    live.putExtra("type", "jiankang");
                    mHomePage.startActivity(live);
                    break;
                case 4:
					XHClick.track(v.getContext(), "点击首页的商城");
                    XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "导航", "闪购");
//				AppCommon.openUrl(mHomePage, "xhds.home.app",true);
					Intent goodDish = new Intent(mHomePage, MainMall.class);
					mHomePage.startActivity(goodDish);
					break;
			}
		}
	});

	private ArrayList<String> aplayId;

	/**
	 * 推广位
	 * 设置热门话题,热门菜谱
	 */
	private void setDoubleActivity() {
		RelativeLayout relativeLayout = (RelativeLayout) mHomePage.findViewById(R.id.a_home_main_gallery_layout_huati);
		int showNum = views.size();
		if (showNum > 0) {
			LinearLayout layout = (LinearLayout) mHomePage.findViewById(R.id.a_home_main_ll_gallery_huati);
			layout.removeAllViews();
			final ImageView[] navImgs = new ImageView[showNum];
			for (int i = 0; i < navImgs.length; i++) {
				navImgs[i] = new ImageView(mHomePage);
				int dp_2_5 = Tools.getDimen(mHomePage, R.dimen.dp_2_5);
				navImgs[i].setPadding(dp_2_5, 0, dp_2_5, 0);
				navImgs[i].setBackgroundColor(Color.TRANSPARENT);
				navImgs[i].setImageResource(R.drawable.bg_round_white5);
				if (navImgs.length > 1) {
					layout.setVisibility(View.VISIBLE);
					layout.addView(navImgs[i]);
					navImgs[i].getLayoutParams().width = Tools.getDimen(mHomePage, R.dimen.dp_11_5);
					navImgs[i].getLayoutParams().height = Tools.getDimen(mHomePage, R.dimen.dp_6_5);
				}
			}
			navImgs[0].setImageResource(R.drawable.z_home_banner_bg_pic_active);
			final GalleryViewPager myGalleryHot = (GalleryViewPager) mHomePage.findViewById(R.id.a_home_main_gallery_huati);
			myGalleryHot.setIsJudgeXY(true);
			LayoutInflater inflater = LayoutInflater.from(mHomePage);
			if (showNum > 1) {
				//香哈头条上方广告
				View view = inflater.inflate(R.layout.ad_banner_view, null);
				views.add(0, view);
				RelativeLayout layoutParent = (RelativeLayout) view.findViewById(R.id.ad_banner_view);
				BannerAd bannerAd = new BannerAd(mHomePage, statistics+showNum, layoutParent, onBannerListener);
				AdParent[] adsBottom = {bannerAd};
				AdsShow adBottom = new AdsShow(adsBottom, aplayId.get(showNum - 1));
				adBottom.onResumeAd();

				View view0 = inflater.inflate(R.layout.ad_banner_view, null);
				views.add(view0);
				RelativeLayout layoutParent0 = (RelativeLayout) view0.findViewById(R.id.ad_banner_view);
				BannerAd bannerAd0 = new BannerAd(mHomePage, statistics+"1", layoutParent0, onBannerListener);
				AdParent[] adsBottom0 = {bannerAd0};
				AdsShow adBottom0 = new AdsShow(adsBottom0, aplayId.get(0));
				adBottom0.onResumeAd();
			}

			myGalleryHot.init(views, 10000, true, new GalleryViewPager.Helper() {
				@Override
				public void onChange(View view, int position) {
//					if(myGalleryHot.auto == 2){
//						XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_SWITCH_ID, "banner1", "左右切换");
//					}
					for (int i = 0; i < navImgs.length; i++) {
						if (i == position) {
							navImgs[i].setImageResource(R.drawable.z_home_banner_bg_pic_active);
						} else
							navImgs[i].setImageResource(R.drawable.bg_round_white5);
					}
				}

				@Override
				public void onClick(View view, int position) {
					//7.28新添加统计
					XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "banner", "香哈头条上方广告");
					bannerAds.get(position).onAdClick();
				}
			}, true);
			myGalleryHot.start();
		} else {
			relativeLayout.setVisibility(View.GONE);
		}
	}

	private OnBannerListener onBannerListener = new OnBannerListener() {

		@Override
		public void onImgShow(int imgH) {
			GalleryViewPager myGalleryHot = (GalleryViewPager) mHomePage.findViewById(R.id.a_home_main_gallery_huati);
			myGalleryHot.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, imgH));
		}

		@Override
		public void onShowAd() {
			mHomePage.findViewById(R.id.a_home_main_gallery_layout_huati).setVisibility(View.VISIBLE);
		}

	};

	private void initDoubleView(final int index) {
		if (index > 2) {
			setDoubleActivity();
			return;
		}
		final View view = LayoutInflater.from(mHomePage).inflate(R.layout.ad_banner_view, null);
		RelativeLayout layoutParent = (RelativeLayout) view.findViewById(R.id.ad_banner_view);
		final BannerAd bannerAd = new BannerAd(mHomePage, statistics+(index+1), layoutParent, onBannerListener);
		bannerAd.isShowAd(aplayId.get(index), new AdIsShowListener() {

			@Override
			public void onIsShowAdCallback(AdParent adParent, boolean isShow) {
				if (isShow) {
					views.add(view);
					bannerAds.add(bannerAd);
					AdParent[] adsBottom = {bannerAd};
					AdsShow adBottom = new AdsShow(adsBottom, aplayId.get(index));
					adBottom.onResumeAd();
				}
				initDoubleView(index + 1);
			}
		});
	}

	private void setDoublePlay() {
		views = new ArrayList<View>();
		bannerAds = new ArrayList<BannerAd>();
		aplayId = new ArrayList<String>();
		aplayId.add(AdPlayIdConfig.MAIN_HOME_SWITCH_ONE);
		aplayId.add(AdPlayIdConfig.MAIN_HOME_SWITCH_TWO);
		aplayId.add(AdPlayIdConfig.MAIN_HOME_SWITCH_THREE);
		initDoubleView(0);
	}

	private void setNouse(ArrayList<Map<String, String>> listData) {
		View nouseHint = mHomePage.findViewById(R.id.a_home_main_item_nous_hint);
		View nouseView1 = mHomePage.findViewById(R.id.a_home_main_item_nous1);
		View nouseLine = mHomePage.findViewById(R.id.a_home_main_item_nous_line);
		View nouseView2 = mHomePage.findViewById(R.id.a_home_main_item_nous2);
		if (listData == null || listData.size() == 0) {
			nouseHint.setVisibility(View.GONE);
			nouseView1.setVisibility(View.GONE);
			nouseLine.setVisibility(View.GONE);
			nouseView2.setVisibility(View.GONE);
			return;
		}
		nouseHint.setVisibility(View.VISIBLE);
		setNouseView(nouseView1, listData.get(0), 0);
		if (listData.size() > 1) {
			nouseLine.setVisibility(View.VISIBLE);
			setNouseView(nouseView2, listData.get(1), 1);
		}
	}

	private void setNouseView(View nouseParent, final Map<String, String> map, final int index) {
		ImageView iv = (ImageView) nouseParent.findViewById(R.id.a_home_main_item_nous_iv);
		TextView title = (TextView) nouseParent.findViewById(R.id.a_home_main_item_nous_title);
		TextView from = (TextView) nouseParent.findViewById(R.id.a_home_main_item_nous_from);
		TextView click = (TextView) nouseParent.findViewById(R.id.a_home_main_item_nous_click);
		TextView ping = (TextView) nouseParent.findViewById(R.id.a_home_main_item_nous_ping);
		setImageView(iv, map.get("img"), 0, 1);
		title.setText(map.get("title"));
		from.setText(map.get("classifyname"));
		String clickNum = map.get("allClick");
		if (!TextUtils.isEmpty(clickNum) && Integer.parseInt(clickNum) > 0) {
			click.setText(map.get("allClick") + "浏览");
			click.setVisibility(View.VISIBLE);
		} else {
			click.setVisibility(View.GONE);
		}
		String commentCount = map.get("commentCount");
		if (!TextUtils.isEmpty(commentCount) && Integer.parseInt(commentCount) > 0) {
			ping.setText(map.get("commentCount") + "评论");
			ping.setVisibility(View.VISIBLE);
		} else {
			ping.setVisibility(View.GONE);
		}
		nouseParent.setVisibility(View.VISIBLE);
		nouseParent.setOnClickListener(ScrollLinearListLayout.getOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				String pamesString = "nousInfo.app?code="+map.get("code");
				XHClick.track(v.getContext(), "点击首页的香哈头条");
                XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "香哈头条", (index + 1) + "");
                if (map.containsKey("isUrl") && !TextUtils.isEmpty(map.get("isUrl"))) {
                    AppCommon.openUrl(mHomePage, map.get("isUrl"), true);
                } else {
                    AppCommon.openUrl(mHomePage, StringManager.api_nouseInfo + map.get("code"), true);
//					Main.allMain.setCurrentTabByClass(HomeNous.class);
//					Intent intent = new Intent(mHomePage, HomeNous.class);
//					mHomePage.startActivity(intent);
				}
			}
		}));
	}

	/**
	 * 设置轮播view
	 *
	 * @param iv        图片view
	 * @param imgUrl    图片url
	 * @param roundType = 1; // 1为全圆角，2上半部分圆角
	 */
	private void setImageView(final ImageView iv, String imgUrl, final int roundImgPixels, final int roundType) {
		if (roundImgPixels > 0) {
			InputStream is = iv.getResources().openRawResource(R.drawable.i_nopic);
			Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
			bitmap = UtilImage.toRoundCorner(iv.getResources(), bitmap, roundType, roundImgPixels);
			iv.setImageBitmap(bitmap);
		} else
			iv.setImageResource(R.drawable.i_nopic);

		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mHomePage)
				.load(imgUrl)
				.setImageRound(roundImgPixels)
				.build();
		if (bitmapRequest != null)
			bitmapRequest.into(new SubBitmapTarget() {
				@Override
				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
					if (roundType == 1 || roundType == 2) {
						bitmap = UtilImage.toRoundCorner(iv.getResources(), bitmap, 1, ToolsDevice.dp2px(mHomePage, 11));
						iv.setImageBitmap(bitmap);
					} else {
						iv.setImageBitmap(bitmap);
					}
					iv.setVisibility(View.VISIBLE);
				}
			});
	}

	// 设置美食家
	private void setTopUser(final ArrayList<Map<String, String>> listData) {
		HorizontalScrollView hsv_expert = (HorizontalScrollView) mHomePage.findViewById(R.id.a_home_main_hsv_expert);
		LinearLayout.LayoutParams llParams = (LayoutParams) hsv_expert.getLayoutParams();
		llParams.width = ToolsDevice.getWindowPx(mHomePage).widthPixels;
		hsv_expert.setLayoutParams(llParams);
		LinearLayout ll_expert = (LinearLayout) mHomePage.findViewById(R.id.a_home_main_ll_expert);
		ll_expert.removeAllViews();
		AdapterTopUser adapter = new AdapterTopUser(hsv_expert, listData, R.layout.a_home_main_item_topuser,
				new String[]{"img", "nickName"}, new int[]{R.id.iv_userImg, R.id.tv_name});
		adapter.imgResource = R.drawable.bg_round_zannum;
		adapter.roundImgPixels = ToolsDevice.dp2px(mHomePage, 500);
		//添加统计
		hsv_expert.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						oldX = event.getX();
						break;
					case MotionEvent.ACTION_MOVE:
						currentX = event.getX();
						break;
					case MotionEvent.ACTION_UP:
						if (Math.abs(currentX - oldX) > 3) {
							XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_SWITCH_ID, "人气美食家", "左右切换");
						}
						break;
					default:
						break;
				}

				return false;
			}
		});

        SetDataView.ClickFunc[] expertClick = {new SetDataView.ClickFunc() {
            @Override
            public void click(final int index, View v) {
                OnClickListener clickListener = ScrollLinearListLayout.getOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //7.28新加统计
						XHClick.track(v.getContext(), "点击首页的人气推荐");
                        XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "人气美食家", "点击" + (index + 1));
                        Intent intent = new Intent(mHomePage, FriendHome.class);
                        intent.putExtra("code", listData.get(index).get("code"));
                        intent.putExtra("floorNum", 0);
                        intent.putExtra("floorNickCode", 0);
                        mHomePage.startActivity(intent);
                    }
                });
                clickListener.onClick(v);
            }
        }};
        int dimen55 = Tools.getDimen(mHomePage, R.dimen.dp_55);
        //屏幕的宽 - 5个头像的总宽 / 6个间距 = 每个间距的长度
        int width = (ToolsDevice.getWindowPx(mHomePage).widthPixels - dimen55 * 5) / 6;
        adapter.viewWidth = dimen55 + width;
        SetDataView.horizontalView(hsv_expert, adapter, null, expertClick);
        // 去除最后一个item的paddingRight
        if (adapter.getCount() - 1 >= 0 && ll_expert.getChildCount() > 0) {
            RelativeLayout rl = (RelativeLayout) ll_expert.getChildAt(adapter.getCount() - 1);
            rl.getLayoutParams().width = dimen55;
            rl.setPadding(0, 0, 0, 0);
        }
    }

	/**
	 * 设置首页的新广告位
	 */
	private void initAd() {
		/** 香哈头条下面 */

        final RelativeLayout adTipLayout = (RelativeLayout) mHomePage.findViewById(R.id.a_home_main_ad_layout_nouse_bottom);
        HomeToutiaoAdControl.getInstance().setToutiaoAdView(mHomePage,adTipLayout);
		/** 人气美食家下面 */
		RelativeLayout layoutParent = (RelativeLayout) mHomePage.findViewById(R.id.a_home_main_ad);
		BannerAd bannerAd = new BannerAd(mHomePage, "index_banner", layoutParent, true);
		AdParent[] ads = new AdParent[1];
		ads[0] = bannerAd;
		AdsShow ad = new AdsShow(ads, AdPlayIdConfig.MAIN_BANNER_TITLE);
		ad.onResumeAd();
	}

	/**
	 * 专题
	 * @param arrayList
	 */
	private void setSpecical(ArrayList<Map<String, String>> arrayList) {
		LayoutInflater inflater = LayoutInflater.from(mHomePage);
		LinearLayout special = (LinearLayout) mHomePage.findViewById(R.id.a_home_main_special);
		special.removeAllViews();
		int size = arrayList.size();
		if (arrayList != null && size == 0) {
			return;
		}
		for (int i = 0; i < size; i++) {
			View view = inflater.inflate(R.layout.a_home_main_item_special, null);
			special.addView(view);
			setSpecicalViewData(view, arrayList.get(i));
		}
		if (AdConfigTools.getInstance().isShowAd(AdPlayIdConfig.MAIN_HOME_SPECICAL_BOTTOM, AdParent.ADKEY_BANNER)) {
			Map<String, String> map = AdConfigTools.getInstance().getAdConfigData(AdPlayIdConfig.MAIN_HOME_SPECICAL_BOTTOM);
			ArrayList<Map<String, String>> array = UtilString.getListMapByJson(map.get("banner"));
			if (array != null && array.size() > 0) {
				map = array.get(0);
				map.put("title", map.get("name"));
				map.put("subtitle", map.get("subhead"));
				String imgData = map.get("imgs");
				if (!TextUtils.isEmpty(imgData)) {
					array = UtilString.getListMapByJson(imgData);
					if (array != null && array.size() > 0) {
						map.put("imgs", array.get(0).get("surpriseImg"));
						View view = inflater.inflate(R.layout.a_home_main_item_special, null);
						special.addView(view);
						setSpecicalViewData(view, map);
					}
				}
			}
		}
	}

	private void setSpecicalViewData(View specicalParent, final Map<String, String> map) {
		ImageView iv = (ImageView) specicalParent.findViewById(R.id.a_home_main_item_special_iv);
		TextView title = (TextView) specicalParent.findViewById(R.id.a_home_main_item_special_title);
		TextView content = (TextView) specicalParent.findViewById(R.id.a_home_main_item_special_content);
		setImageView(iv, map.get("imgs"), 0, -1);
		title.setText(map.get("title"));
		content.setText(map.get("subtitle"));
		specicalParent.setVisibility(View.VISIBLE);
		specicalParent.setOnClickListener(ScrollLinearListLayout.getOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String pamesString = map.get("url");
                if (TextUtils.isEmpty(pamesString))
                    return;
				XHClick.track(v.getContext(), "点击首页的专题");
                AppCommon.openUrl(mHomePage, pamesString, true);
                if (pamesString.contains("?"))
                    XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "精选专题", pamesString.substring(0, pamesString.indexOf("?")));
                else
                    XHClick.mapStat(mHomePage, MainHomePageNew.STATISTICS_ID, "精选专题", "");

			}
		}));
	}
}
