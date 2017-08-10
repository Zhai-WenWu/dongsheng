package amodule.user.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import acore.widget.GalleryViewPager;
import amodule.other.activity.InviteFriend;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.adapter.AdapterScoreStore;
import aplug.basic.SubBitmapTarget;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

public class ScoreStore extends BaseActivity implements OnClickListener {
	private DownRefreshList lv_sur;
	private View storeHintView;
	private GalleryViewPager gallery;
	private LinearLayout llGallery, activityView;
	private ViewGroup.LayoutParams galleryParams;
	private boolean galleryImg = false;
	private View viewParent;

	private AdapterScoreStore adapter;
	private ArrayList<Map<String, String>> listDataMySuro;

	private static final int MSG_MYSUBJECT_OK = 1;
	private static Handler handler = null;
	private int currentPage = 0, everyPage = 0;
	private boolean actIsOk = false, goodsIsOk = false;

	public boolean isBlankSpace = true;
	public String noGoTime, goTime;

	private TextView mScore, mTask;
	private boolean isShowTaskInfo = true;

	public final String STATISTICS_ID = "a_score420";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("积分商城", 2, 0, R.layout.c_view_bar_title, R.layout.a_user_score_store);
		initUI();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initUI() {
		TextView tv = (TextView) findViewById(R.id.rightText);
		tv.setVisibility(View.VISIBLE);
		tv.setText("兑换记录");
		tv.setOnClickListener(this);
		lv_sur = (DownRefreshList) findViewById(R.id.lv_sur);

		activityView = new LinearLayout(this);
		activityView.setOrientation(LinearLayout.VERTICAL);
		lv_sur.addHeaderView(activityView);
		LayoutInflater inflater = LayoutInflater.from(this);
		// 头部除了活动的步伐
		View scoreView = inflater.inflate(R.layout.a_user_score_store_item_title, null);
		mScore = (TextView) scoreView.findViewById(R.id.tv_score);
		mTask = (TextView) scoreView.findViewById(R.id.tv_task);
		scoreView.findViewById(R.id.user_score).setOnClickListener(this);
		scoreView.findViewById(R.id.score_rule).setOnClickListener(this);
		scoreView.findViewById(R.id.rl_choujiang).setOnClickListener(this);
		scoreView.findViewById(R.id.day_task).setOnClickListener(this);
		scoreView.findViewById(R.id.rl_frends).setOnClickListener(this);
		scoreView.findViewById(R.id.user_task_info).setVisibility(View.GONE);

		activityView.addView(scoreView);
		viewParent = inflater.inflate(R.layout.a_common_surprised_item_title_activity, null);
		llGallery = (LinearLayout) viewParent.findViewById(R.id.ll_gallery);
		gallery = (GalleryViewPager) viewParent.findViewById(R.id.gallery);
		galleryParams = gallery.getLayoutParams();

		storeHintView = inflater.inflate(R.layout.a_user_score_store_list_bottom_hint, null);
		storeHintView.setVisibility(View.GONE);
		lv_sur.addFooterView(storeHintView);

		lv_sur.setDivider(null);

		listDataMySuro = new ArrayList<Map<String, String>>();
		adapter = new AdapterScoreStore(this, lv_sur, listDataMySuro, 0, null, null);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
					case MSG_MYSUBJECT_OK: // tab Hot数据加载完成;
						if (actIsOk && goodsIsOk) {
							isBlankSpace = false;
							lv_sur.setVisibility(View.VISIBLE);
							lv_sur.onRefreshComplete();
						}
						break;
				}
			}
		};
	}

	/*
	 * 加载数据,false加载  true 更新
	 */
	private void initData() {
		isShowTaskInfo = AppCommon.getTodayTastHintIsShow(this);
//		getTaskCount();
//		getActivityData();
		loadManager.setLoading(lv_sur, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!actIsOk)
					refurbish();
				else
					loadFromServer(false);
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				refurbish();
			}
		});
		loadManager.setFailClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refurbish();
			}
		});
	}

	public void onGoTopClick(View v) {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version >= 11) {
			lv_sur.smoothScrollToPosition(0);
		} else if (version < 11 && version >= 8) {
			lv_sur.setSelection(0);
		}
	}

	private void refurbish() {
		actIsOk = false;
		goodsIsOk = false;
		getTaskCount();
		getActivityData();
		loadFromServer(true);

	}

	/**
	 * 获取网络数据
	 *
	 * @param isForward 是否是向上加载
	 */
	private void loadFromServer(final boolean isForward) {
		// 向上加载/加载上一页.
		if (isForward) {
			currentPage = 1;
		}
		// 向下加载;
		else {
			currentPage++;
		}
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, isBlankSpace);
		String getUrl = StringManager.api_getProductList + "?page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if (isForward) listDataMySuro.clear();
					if (currentPage == 1) {
						goodsIsOk = true;
					}
					loadCount = parseInfo(listDataMySuro, returnObj);
					adapter.notifyDataSetChanged();
				} else {
					lv_sur.setVisibility(View.VISIBLE);
					lv_sur.onRefreshComplete();
				}
				handler.sendEmptyMessage(MSG_MYSUBJECT_OK);
				if (everyPage == 0)
					everyPage = loadCount;
				currentPage = loadManager.changeMoreBtn(flag, everyPage, loadCount, currentPage, isBlankSpace);
				Button loadMore = loadManager.getSingleLoadMore(null);
				if (loadMore != null) {
					if (!loadMore.isEnabled()) {
						storeHintView.setVisibility(View.VISIBLE);
						loadMore.setVisibility(View.GONE);
					} else {
						storeHintView.setVisibility(View.GONE);
						loadMore.setVisibility(View.VISIBLE);
					}
				} else {
					storeHintView.setVisibility(View.GONE);
				}
			}
		});
	}

	private void getActivityData() {
		String getUrl = StringManager.api_getSurpriseActivity;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag > 1) {
					ArrayList<Map<String, String>> sliderList = UtilString.getListMapByJson(returnObj);
//					Map<String,String> map = new HashMap<String, String>();
//					map.put("appImg", "http://static.xiangha.com/store/201507/1000_1000/241118473665.jpg/300x300");
//					sliderList.add(map);
					setGallery(sliderList);
				}
			}
		});
	}

	private void setGallery(final ArrayList<Map<String, String>> sliderList) {
		if (sliderList == null || sliderList.size() <= 0) {
			actIsOk = true;
			handler.sendEmptyMessage(MSG_MYSUBJECT_OK);
			return;
		}
		int sliderListSize = sliderList.size();
		final ImageView[] navImg = new ImageView[sliderListSize];
		llGallery.removeAllViews();
		for (int i = 0; i < navImg.length; i++) {
			navImg[i] = new ImageView(this);
			int dp_2_5 = Tools.getDimen(this, R.dimen.dp_2_5);
			navImg[i].setPadding(dp_2_5, 0, dp_2_5, 0);
			navImg[i].setBackgroundColor(Color.TRANSPARENT);
			navImg[i].setImageResource(R.drawable.bg_round_white5);
			if (navImg.length > 1) {
				llGallery.addView(navImg[i]);
				navImg[i].getLayoutParams().width = Tools.getDimen(this, R.dimen.dp_12_5);
				navImg[i].getLayoutParams().height = Tools.getDimen(this, R.dimen.dp_7_5);
			}
		}
		activityView.removeView(viewParent);
		galleryImg = false;
		activityView.addView(viewParent, 0);
		if (navImg.length > 0)
			navImg[0].setImageResource(R.drawable.z_home_banner_bg_pic_active);
		// 装配gallery的view
		ArrayList<View> views = new ArrayList<View>();
		for (int i = 0; i < sliderListSize; i++) {
			Map<String, String> map = sliderList.get(i);
			views.add(getGalleryView(map));
		}
		if (sliderListSize > 1) {
			views.add(0, getGalleryView(sliderList.get(sliderListSize - 1)));
			views.add(getGalleryView(sliderList.get(0)));
		}
		gallery.init(views, 10000, true, new GalleryViewPager.Helper() {

			@Override
			public void onChange(View view, int position) {
				for (int i = 0; i < navImg.length; i++) {
					if (i == position) {
						navImg[i].setImageResource(R.drawable.z_home_banner_bg_pic_active);
					} else
						navImg[i].setImageResource(R.drawable.bg_round_white5);
				}
			}

			@Override
			public void onClick(View view, int position) {
				Map<String, String> map = sliderList.get(position);
				String url = map.get("url");
				if (url != null && url != "") {
					XHClick.mapStat(ScoreStore.this, "a_surprise", "Banner", "");
					AppCommon.openUrl(ScoreStore.this, url, true);
				}
			}

		});
		gallery.start();
		actIsOk = true;
		handler.sendEmptyMessage(MSG_MYSUBJECT_OK);
	}

	private View getGalleryView(Map<String, String> map) {
		ImageView iv = new ImageView(this);
		iv.setMinimumHeight(Tools.getDimen(this, R.dimen.dp_200));
		setGalleryView(iv, map.get("appImg"));
		return iv;
	}

	private void setGalleryView(final ImageView iv, String imgUrl) {
		iv.setBackgroundResource(R.drawable.i_nopic);
		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
				.load(imgUrl)
				.build();
		if (bitmapRequest != null)
			bitmapRequest.into(new SubBitmapTarget() {
				@Override
				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
					iv.setImageBitmap(bitmap);
					if (!galleryImg) {
						galleryImg = true;
						int screnW = ToolsDevice.getWindowPx(ScoreStore.this).widthPixels;
						int bitW = bitmap.getWidth();
						int bitH = bitmap.getHeight();
						galleryParams.height = screnW * bitH / bitW;
						gallery.setLayoutParams(galleryParams);
					}
				}
			});
	}

	private int parseInfo(final ArrayList<Map<String, String>> listData, Object returnObj) {
		ArrayList<Map<String, String>> listMySelf = UtilString.getListMapByJson(returnObj);
		for (int i = 0; i < listMySelf.size(); i += 2) {
			Map<String, String> m = new HashMap<String, String>();
			String left = Tools.map2Json(listMySelf.get(i));
			m.put("left", left);
			if (listMySelf.size() > i + 1) {
				String right = Tools.map2Json(listMySelf.get(i + 1));
				m.put("right", right);
			}
			listDataMySuro.add(m);
		}
		return listMySelf.size();
	}

	private void getTaskCount() {
		ReqInternet.in().doGet(StringManager.api_getTaskCount, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> array = UtilString.getListMapByJson(returnObj);
					if (array != null && array.size() > 0) {
						Map<String, String> map = array.get(0);
						if (map.containsKey("score")) {
							mScore.setText(map.get("score"));
						} else mScore.setText("0");
						String numTask = map.get("numTask");
						String countTask = map.get("countTask");
						if (!TextUtils.isEmpty(numTask) && !TextUtils.isEmpty(countTask)) {
							//如果今天还没有查看了任务并且今天的任务一个没有完成，就显示任务红点
							if (isShowTaskInfo && Integer.parseInt(numTask) > 0) {
								isShowTaskInfo = false;
								saveTodayTastInfo();
							}
							mTask.setText("任务(" + numTask + "/" + countTask + ")");
						} else {
							mTask.setText("任务 (0/5)");
						}
						//此设备记录今天还没显示过‘提示任务红点’，并且今天完成的数量!>0(表示在其他设备也没完成今日任务)，就显示红点
						if (isShowTaskInfo)
							findViewById(R.id.user_task_info).setVisibility(View.VISIBLE);
						else
							findViewById(R.id.user_task_info).setVisibility(View.GONE);
					}
				} else {

				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (!LoginManager.isLogin()) {
			switch (v.getId()) {
				case R.id.score_rule:
					Tools.showToast(this, "登录后即可查看积分规则");
				case R.id.user_score:
				case R.id.rightText: //兑换记录
				case R.id.day_task:
				case R.id.rl_choujiang:
					startActivity(new Intent(this, LoginByAccout.class));
					return;
			}
		}


		switch (v.getId()) {
			case R.id.user_score: //我的积分
				XHClick.mapStat(this, STATISTICS_ID, "我的积分", "");
				AppCommon.openUrl(this, StringManager.api_scoreList + "?code=" + LoginManager.userInfo.get("code"), true);
				break;
			case R.id.score_rule: //积分规则
				XHClick.mapStat(this, STATISTICS_ID, "积分规则", "");
				String url = StringManager.api_integralInfo + "?code=" + LoginManager.userInfo.get("code");
				AppCommon.openUrl(this, url, true);
				break;
			case R.id.rl_choujiang: //积分抽奖
				XHClick.mapStat(this, STATISTICS_ID, "积分抽奖", "");
				String choujiangurl = StringManager.api_jifenDraw + "?code=" + LoginManager.userInfo.get("code");
				AppCommon.openUrl(this, choujiangurl, true);
				break;
			case R.id.day_task: //任务
				XHClick.mapStat(this, STATISTICS_ID, "任务", "");
				if (isShowTaskInfo) {
					isShowTaskInfo = false;
					findViewById(R.id.user_task_info).setVisibility(View.GONE);
					saveTodayTastInfo();
				}
				String dayTasklurl = StringManager.api_getDailyTask + "?code=" + LoginManager.userInfo.get("code");
				AppCommon.openUrl(this, dayTasklurl, true);
				break;
			case R.id.rl_frends: //邀请好友
				XHClick.mapStat(this, STATISTICS_ID, "邀请好友", "");
				Intent intentInviteFriend = new Intent(this, InviteFriend.class);
				startActivity(intentInviteFriend);
				break;
			case R.id.rightText: //兑换记录
				XHClick.mapStat(this, STATISTICS_ID, "兑换记录", "");
				String integralurl = StringManager.api_exchangeList + "?code=" + LoginManager.userInfo.get("code");
				AppCommon.openUrl(this, integralurl, true);
				break;
		}
	}

	private void saveTodayTastInfo() {
		int year = Tools.getDate("year");
		int month = Tools.getDate("month");
		int date = Tools.getDate("date");
		UtilFile.saveShared(this, "score_store", "user_task", year + "_" + month + "_" + date);
	}
}
