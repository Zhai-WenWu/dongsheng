package amodule.quan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.quan.adapter.AdapterQuanShowSubject;
import amodule.quan.db.SubjectData;
import amodule.quan.tool.UploadSubjectControl;
import amodule.quan.tool.UploadSubjectControl.ReplyCallback;
import amodule.quan.view.BarSubjectFloorOwnerNew;
import amodule.quan.view.BarSubjectReply1;
import amodule.quan.view.BarSubjectReply2;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

import static amodule.quan.activity.FriendQuan.REQUEST_CODE_QUAN_FRIEND;

@SuppressLint ({"ClickableViewAccessibility", "HandlerLeak"})
public class ShowSubject extends BaseAppCompatActivity {
	private BarSubjectFloorOwnerNew louZhuHeadView;// 楼主的view
	// 踢出去的那些回复框和举报框
	private BarSubjectReply1 barSubjectReply1;//回复框1
	private BarSubjectReply2 barSubjectReply2;//回复框2
	//	private BarSubjectReport barSubjectReport;//举报框
	private DownRefreshList listSubject;
	private LinearLayout louZhuHeadViewLinearLayout;//存放楼主的布局
	private RelativeLayout reportLayout;//举报按钮
	private RelativeLayout shareLayout;//分享按钮
	private RelativeLayout favLayout;//收藏按钮
	private RelativeLayout recommendSubject;//推荐按钮
	//	private ImageView reportImageView;//举报按钮图片
	//	private ImageView shareImageView;//分享按钮图片
	private ImageView favoriteImageView;//收藏按钮图片
	private TextView favoriteTextView;//新收藏按钮下方文字

	private AdapterQuanShowSubject adapter;
	private ArrayList<Map<String, String>> listDataSubjectInfo;
	private Map<String, String> louZhuMap;//楼主的code name

	// 贴子的一些信息
	private String subCode = "", shareImg = "";
	private int destFloorNum = 0;
	private String subjectTitle = "", subjectContent = "", commentId;
	private int currentDownPage = 0, currentUpPage = 1, relEveryPage = 0;
	private boolean isMsgViewLoad = false, isOnce = true, isRefresh = false;
	private boolean isReplayFloorOwner = false;
	//消息跳转后创建一次回复某某人.再刷新或加载后不再弹出bar2
	private boolean isXiaoXi = true;//消息跳转到详情页后楼主是否可见
	private int commentNum;//楼主评论的次数
	private String isLike = "";//是否赞过楼主
	private int item;//点击消息跳转详情页后定位到指定楼层
	//点消息跳转过来的时候得到的name  code   floorId
	private String isSafa;//是否是从沙发列表页点击进入详情页的
	public static String types = "1";//美食贴类型
	private boolean isJingHua = false;//是否是精华贴
	private String classId = "";//模块Id

	private String STATISTICS_ID = "a_quan_detail_normal";
	private boolean isHasVideo = false;

	private String data_type = "";//推荐列表过来的数据
	private String module_type="";
	private Long startTime;//统计使用的时间

	boolean isBack = false;
	boolean isOnceStart = true;
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		super.onCreate(savedInstanceState);
		startTime = System.currentTimeMillis();
		initActivity("", 2, 0, 0, R.layout.a_quan_subject);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			subCode = bundle.getString("code");
			data_type= bundle.getString("data_type");
			module_type= bundle.getString("module_type");
			isReplayFloorOwner = !TextUtils.isEmpty(bundle.getString("isReplayFloorOwner"));
			//下面参数没有的时候就是null
			//			String floorNum = Integer.valueOf(TextUtils.isEmpty(floorNum) ? "0" : floorNum);
			destFloorNum = Integer.valueOf(!TextUtils.isEmpty(bundle.getString("floorNum")) ? bundle.getString("floorNum") : "0");
			commentId = bundle.getString("commentId");
			//是不是从沙发过来的,如果不是就是null(有两种参数1:yes 2:qiang)
			isSafa = bundle.getString("isSafa");
			//判断消息是否被删
			String status = bundle.getString("status");
			if (status != "" && status != null && status.equals("1")) {
				Tools.showToast(this, "此消息已被删除");
			}
			//消息是否读过
			if (bundle.getString("newsId") != null) {
				String params = "type=news&p1=" + bundle.getString("newsId");
				ReqInternet.in().doPost(StringManager.api_setUserData, params, new InternetCallback(this) {
					@Override
					public void loaded(int flag, String url, Object returnObj) {}
				});
			}
		} else {
			this.finish();
		}
		initTab();//初始化头部
		initView();//初始化view
		//当从消息点过来的时候,执行下面的方法
		if (destFloorNum != 0 && destFloorNum != 1) {
			isXiaoXi = false;
			getPageByFloor(false);
			if (destFloorNum > 6) {
				listSubject.imageHide();//显示文字的向上加载
			}
			listSubject.bigDownText = "下拉加载上一页";
			listSubject.bigReleaseText = "松开加载上一页";
		} else {
			initData();
		}

		final Button loadMore = loadManager.getSingleLoadMore(null);
		String loadMoreBgColor = Tools.getColorStr(this,R.color.common_bg);
		if(loadMore != null)loadMore.setBackgroundColor(Color.parseColor(loadMoreBgColor));

		//接口回调
		UploadSubjectControl.getInstance().setReplyCallback(new ReplyCallback() {

			@Override
			public void onReplySuccess(SubjectData uploadData, int flag, Object msg) {
				ArrayList<Map<String, String>> list = UtilString.getListMapByJson(msg);
				Map<String,String> map = list.get(0);
				int num = Integer.parseInt(map.get("num"));
				if ( num <= 6) {
					addFloor(map,msg.toString());
				} else if (num % 6 != 0 && loadMore != null && loadMore.isEnabled()) {
					// getSubjectInfo(false);//楼层较多,无需加载.

				} else if (num % 6 == 0 && loadMore != null && !loadMore.isEnabled()) {
					addFloor(map,msg.toString());
				} else if (num % 6 != 0 && loadMore != null && !loadMore.isEnabled()) {
					addFloor(map,msg.toString());
				}
			}

			@Override
			public void onReplyFailed(SubjectData uploadData) {
				Intent intent = new Intent(ShowSubject.this, UploadSubjectNew.class);
				intent.putExtra("id", uploadData.id);
				intent.putExtra("subjectCode", uploadData.getCode());
				startActivity(intent);
			}
		});
		//		initTitle();
		String color = Tools.getColorStr(this,R.color.common_top_bg);
		Tools.setStatusBarColor(this, Color.parseColor(color));
		//		setViewIndex();
		XHClick.track(this, "浏览美食贴");
		SpecialWebControl.initSpecialWeb(this,"subjectInfo","",subCode);
	}

	private void addFloor(Map<String,String> map, String msg){
		if (map.containsKey("code") && subCode.equals(map.get("code"))) {
			Map<String, String> newFloor = new HashMap<>();
			newFloor.put("floor", msg);
			// 立即添加回复信息到当前数据界面的下面;
			parseInfo(newFloor, false);
			adapter.notifyDataSetChanged();
		}
	}

	// 设置tab栏的点击举报和分享
	private void initTab() {
		RelativeLayout back = (RelativeLayout) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		reportLayout = (RelativeLayout) findViewById(R.id.report_layout);
		shareLayout = (RelativeLayout) findViewById(R.id.shar_layout);
		favLayout = (RelativeLayout) findViewById(R.id.fav_layout);

		//		//初始化全屏视屏
		//		fullscreen = (RelativeLayout) findViewById(R.id.fullscreen);
		//管理员权限      推荐美食贴
		recommendSubject = (RelativeLayout) findViewById(R.id.home_layout);
		ImageView imageView = (ImageView) findViewById(R.id.img_home);
		imageView.setVisibility(View.GONE);
		TextView recommendTextView = (TextView) findViewById(R.id.tv_home);
		recommendTextView.setText("推荐");
		recommendTextView.setVisibility(View.VISIBLE);
		recommendTextView.setTextSize(Tools.getDimenSp(this, R.dimen.sp_15));
		//收藏按钮图片
		favoriteImageView = (ImageView) findViewById(R.id.img_fav);
		favoriteTextView = (TextView) findViewById(R.id.tv_fav);
		//推荐
		recommendSubject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//跳转推荐页面
				Intent intent = new Intent(ShowSubject.this, QuanRecommend.class);
				intent.putExtra("code", subCode);
				intent.putExtra("classId", classId);
				intent.putExtra("isJingHua", isJingHua);
				ShowSubject.this.startActivity(intent);
			}
		});
		//收藏点击
		favLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//7.29新添加统计
				XHClick.mapStat(ShowSubject.this.getApplicationContext(), "a_collection", "美食贴", "");
				if (LoginManager.isLogin()) {
					AppCommon.onFavoriteClick(ShowSubject.this, "subject", subCode, new InternetCallback(ShowSubject.this.getApplicationContext()) {
						@Override
						public void loaded(int flag, String url, Object returnObj) {
							if (flag >= UtilInternet.REQ_OK_STRING) {
								XHClick.mapStat(ShowSubject.this, STATISTICS_ID, "顶部导航栏点击量", "收藏点击量");
								Map<String, String> map = UtilString.getListMapByJson(returnObj).get(0);
								boolean nowFav = map.get("type").equals("2");
								favoriteImageView.setImageResource(nowFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
								favoriteTextView.setText(nowFav ? "已收藏" : "  收藏  ");
							} else {
								String returnStr = TextUtils.isEmpty((String)returnObj) ? "" : returnObj.toString();
								if (returnStr.contains("登录")) {
									goLogin();
								}
							}
						}
					});
				} else {
					goLogin();
				}
			}
		});
		shareLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//让举报框不显示.解决举报框与分享框的冲突
				//				barSubjectReport.hide();
				XHClick.track(ShowSubject.this, "分享美食贴");
				XHClick.mapStat(ShowSubject.this, "a_share400", "生活圈", "美食贴详情页");
				if (louZhuMap.size() > 0) {
					if (isHasVideo && louZhuHeadView.getImageViewVideo() != null) {
						XHClick.mapStat(ShowSubject.this, STATISTICS_ID, "顶部导航栏点击量", "分享点击量");
						barShare = new BarShare(ShowSubject.this, "美食贴详情", "生活圈");
						String title = subjectTitle + "视频教学，一学就会哦！";
						String clickUrl = StringManager.wwwUrl + "video/subject/" + subCode;
						String content = "顶级大厨的绝密配方，好吃到哭。" + clickUrl;
						barShare.setShare(title, content, louZhuHeadView.getImageViewVideo().getBitmap(), clickUrl);
						barShare.openShare();
					} else {
						XHClick.mapStat(ShowSubject.this, STATISTICS_ID, "顶部导航栏点击量", "分享点击量");
						barShare = new BarShare(ShowSubject.this, "美食贴详情", "生活圈");
						String type = BarShare.IMG_TYPE_WEB;
						String title = subjectTitle + "_" + louZhuMap.get("nickName");
						String clickUrl = StringManager.wwwUrl + "quan/" + subCode + ".html";
						String content = subjectContent;
						if (!TextUtils.isEmpty(shareImg)) {
							barShare.setShare(type, title, content, shareImg, clickUrl);
						}else if(louZhuHeadView.getIsHasVideo() && !TextUtils.isEmpty(louZhuHeadView.getVideoImg())){
							barShare.setShare(type, title, content, louZhuHeadView.getVideoImg(), clickUrl);
						}else {
							Resources res = getResources();
							Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.share_launcher);
							barShare.setShare(title, content, bmp, clickUrl);
						}
						barShare.openShare();
					}
				} else if (louZhuMap.size() <= 0 && listDataSubjectInfo.size() > 0) {
					XHClick.mapStat(ShowSubject.this, STATISTICS_ID, "顶部导航栏点击量", "分享点击量");
					barShare = new BarShare(ShowSubject.this, "美食贴详情", "生活圈");

					String type = BarShare.IMG_TYPE_WEB;
					String title = subjectTitle;
					String clickUrl = StringManager.wwwUrl + "quan/" + subCode + ".html";
					String content = subjectContent + clickUrl;
					if (!TextUtils.isEmpty(shareImg)) {
						barShare.setShare(type, title, content, shareImg, clickUrl);
					}else if(louZhuHeadView.getIsHasVideo() && !TextUtils.isEmpty(louZhuHeadView.getVideoImg())){
						barShare.setShare(type, title, content, louZhuHeadView.getVideoImg(), clickUrl);
					}else {
						Resources res = getResources();
						Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.share_launcher);
						barShare.setShare(title, content, bmp, clickUrl);
					}
					barShare.openShare();
				} else {
					Tools.showToast(ShowSubject.this.getApplicationContext(), "正在加载...");
				}
			}
		});
		// 点击弹出举报框
		reportLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (louZhuMap.size() > 0) {
					XHClick.mapStat(ShowSubject.this.getApplicationContext(), STATISTICS_ID, "顶部导航栏点击量", "举报点击量");
					Intent intent = new Intent(ShowSubject.this, QuanReport.class);
					intent.putExtra("isQuan", "1");
					intent.putExtra("nickName", "举报此贴");
					intent.putExtra("code", subCode);
					intent.putExtra("repType", "1");
					intent.putExtra("subjectCode", subCode);
					ShowSubject.this.startActivityForResult(intent, 100);
					barSubjectReply2.keybroadShow(false);
				} else if (louZhuMap.size() <= 0 && listDataSubjectInfo.size() > 0 && destFloorNum != 0) {
					XHClick.mapStat(ShowSubject.this.getApplicationContext(), STATISTICS_ID, "顶部导航栏点击量", "举报点击量");
					Intent intent = new Intent(ShowSubject.this, QuanReport.class);
					intent.putExtra("isQuan", "1");
					intent.putExtra("nickName", "举报此贴");
					intent.putExtra("code", subCode);
					intent.putExtra("repType", "1");
					intent.putExtra("subjectCode", subCode);
					ShowSubject.this.startActivityForResult(intent, 100);
					barSubjectReply2.keybroadShow(false);
				} else {
					Tools.showToast(ShowSubject.this.getApplicationContext(), "正在加载...");
				}
			}
		});
	}

	//登录
	private void goLogin() {
		Intent intent = new Intent(this, LoginByAccout.class);
		startActivity(intent);
	}

	//初始化集合,输入框和举报框,设置输入法的隐藏
	private void initView() {
		RelativeLayout activityLayout = (RelativeLayout) findViewById(R.id.activityLayout);
		listDataSubjectInfo = new ArrayList<>();
		louZhuMap = new HashMap<>();

		listSubject = (DownRefreshList) findViewById(R.id.lv_showSubjectInfo);
		listSubject.setDivider(null);


		RelativeLayout rl_subjectVidio = (RelativeLayout) findViewById(R.id.rl_subjectVidio);
		louZhuHeadView = new BarSubjectFloorOwnerNew(this, handler, rl_subjectVidio);


		louZhuHeadViewLinearLayout = new LinearLayout(this);
		//加楼主的空布局
		listSubject.addHeaderView(louZhuHeadViewLinearLayout);
		// 被踢出去的输入框和举报框
		barSubjectReply1 = (BarSubjectReply1) findViewById(R.id.bar_subject_reply1);

		barSubjectReply2 = (BarSubjectReply2) findViewById(R.id.bar_subject_reply2);
		//		barSubjectReport = (BarSubjectReport) findViewById(R.id.bar_subject_report);
		listSubject.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 罗明
				// 设置回复输入框的变化.有键盘是为回复楼主或别人
				if (destFloorNum != 0 && commentId != null && !isMsgViewLoad) {
					barSubjectReply2.keybroadShow(false);
				} else if (destFloorNum != 0 && commentId == null && !isMsgViewLoad) {
					barSubjectReply2.keybroadShow(false);
					if (destFloorNum == 1) {
						if (barSubjectReply2.getUnicodeText().length() == 0) {
							barSubjectReply1.show();
							barSubjectReply2.hide();
							if (commentNum == 0) {
								barSubjectReply1.pinglun.setText("抢沙发");
							} else {
								barSubjectReply1.pinglun.setText(commentNum + "评论...");
							}
						}
					}
				} else {
					barSubjectReply2.keybroadShow(false);
					if (barSubjectReply2.getUnicodeText().length() == 0) {
						barSubjectReply1.show();
						barSubjectReply2.hide();
						if (commentNum == 0) {
							barSubjectReply1.pinglun.setText("抢沙发");
						} else {
							barSubjectReply1.pinglun.setText(commentNum + "评论...");
						}
					}
				}
				return false;
			}
		});
		barSubjectReply2.initView(ShowSubject.this, handler, subCode, activityLayout);
		adapter = new AdapterQuanShowSubject(this, handler, listSubject, listDataSubjectInfo, R.layout.a_quan_item_subject,
				new String[]{"num"}, new int[]{R.id.tv_sub_num}, commentId);
	}

	/*
	 * 加载数据,false加载  true 更新
	 */
	private void initData() {
		loadManager.setLoading(listSubject, adapter, true, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getSubjectInfo(false);
			}
		}, new OnClickListener() {
			@Override
			public void onClick(View v) {
				getSubjectInfo(true);
			}
		}, new LoadManager.ViewScrollCallBack() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				Log.i("zhangyujian","onScroll");
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				louZhuHeadView.viewScroll();
				Log.i("zhangyujian","onScrollStateChanged:::"+scrollState);
			}
		});
		//填充底部footer避免遮盖
		View fillFooterView = new View(this);
		fillFooterView.setMinimumHeight(Tools.getDimen(this,R.dimen.dp_45));
		listSubject.addFooterView(fillFooterView,null,false);
	}

	/**
	 * 根据FloorNum设置当前page;
	 */
	private void getPageByFloor(final boolean isNewRep) {
		String getUrl = StringManager.api_circlegetInfo + "?code=" + subCode + "&floorNum=" + destFloorNum;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					listDataSubjectInfo.clear();
					if (!isNewRep) {
						relEveryPage = 0;
					}
					ArrayList<Map<String, String>> listSubJectInfo = UtilString.getListMapByJson(returnObj);
					Map<String, String> theSubjectListMap = null;
					for (int i = 0; i < listSubJectInfo.size(); i++) {
						Map<String, String> map = listSubJectInfo.get(i);
						if (map.containsKey("theSubject")) {
							theSubjectListMap = UtilString.getListMapByJson(map.get("theSubject")).get(0);
							commentNum = Integer.parseInt(theSubjectListMap.get("commentNum"));
							subjectTitle = theSubjectListMap.get("title");

							//设置收藏按钮图片
							if (theSubjectListMap.containsKey("isFav")) {
								boolean isFav = theSubjectListMap.get("isFav").equals("2");
								favoriteImageView.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
								favoriteTextView.setText(isFav ? "已收藏" : "  收藏  ");
							}
							if (theSubjectListMap.containsKey("isLike")) {
								isLike = theSubjectListMap.get("isLike");
							}
							//获取当前的
							if (theSubjectListMap.containsKey("classId")) {
								classId = theSubjectListMap.get("classId");
							}

							barSubjectReply1.initView(ShowSubject.this, handler, subCode, subjectTitle, isLike, Integer.parseInt(theSubjectListMap.get("likeNum")), commentNum + "", classId);
						} else {
							barSubjectReply1.initView(ShowSubject.this, handler, subCode, subjectTitle, "1", 0, "0", classId);
						}
						// 设置setLoading时会自动加载一页;
						int page = 1;
						try {
							page = Integer.parseInt(map.get("page"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						currentUpPage = page;
						currentDownPage = page - 1;
						// 得到page进行数据加载
						initData();
					}
				} else if (flag > UtilInternet.REQ_STATE_ERROR) {
					ShowSubject.this.finish();
				} else {
					initData();
				}
			}
		});
	}

	/**
	 * 获取网络数据
	 *
	 * @param isForward 是否是向上加载
	 */
	private void getSubjectInfo(final boolean isForward) {
		// 向上加载/加载上一页.
		if (isForward) {
			if (currentUpPage > 1) {
				currentUpPage--;
				isRefresh = false;
				// 加载上一页时将数据放在list集合的前面
			} else {// 当前page为1的话,恢复初始提示，初始的状态
				isRefresh = true;
				currentDownPage = 1;
			}
		}
		// 向下加载;
		else {
			currentDownPage++;
		}
		// 获取加载页面
		int currentPage = isForward ? currentUpPage : currentDownPage;
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, listDataSubjectInfo.size() == 0);
		String getUrl = StringManager.api_circlegetInfo + "?code=" + subCode + "&page=" + currentPage;
		ReqInternet.in().doGet(getUrl, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				int loadCount = 0;
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if (isForward && isRefresh) {
						if (listDataSubjectInfo.size() > 0) {
							listDataSubjectInfo.clear();//如果此时的状态是刷新,那么就清空数据.
						}
						louZhuHeadViewLinearLayout.removeView(louZhuHeadView);//把楼主的viewremove掉,因为后面还会加一次,不去掉会报错.
					}
					ArrayList<Map<String, String>> listSubJectInfo = UtilString.getListMapByJson(returnObj);

					if (listSubJectInfo.size() > 0) {
						Map<String, String> map = listSubJectInfo.get(0);
						loadCount = parseInfo(map, isForward);
					}

					if (loadCount > 0) {
						adapter.notifyDataSetChanged();
					}
					//当加载上一页结束了的时候转换成下拉刷新模式
					if (currentUpPage == 1 && listSubject != null) {
						listSubject.textHide();
					}
					// 第一次加载数据时,显示对应楼层;
					if (destFloorNum != 0 &&
							destFloorNum != 1 &&
							relEveryPage == 0 &&
							(currentUpPage == currentDownPage)) {

						positionSelection();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								positionSelection();
							}
						}, 1500);
					}
				} else if (flag == UtilInternet.REQ_CODE_ERROR) {
					ShowSubject.this.finish();//当贴子被删掉时,finish
				}

				if (relEveryPage == 0) {
					relEveryPage = loadCount;
				}
				currentDownPage = loadManager.changeMoreBtn(flag, relEveryPage, loadCount, currentDownPage, listDataSubjectInfo.size() == 0);
				listSubject.onRefreshComplete();
			}
		});
	}

	/**
	 * 用于当点击消息跳转详情页时,定位到当前回复页面
	 */
	private void positionSelection() {
		if (destFloorNum > 1 && destFloorNum <= 6 && listDataSubjectInfo.size() > 0) {
			listSubject.setSelection(item + 1);// 数据的Index比楼层小1,list的HeardView两个;
			// listDataSubjectInfo.get(destFloorNum ).put("bgColor",
			// R.drawable.round_green + "");
		} else if (destFloorNum > 6 && listDataSubjectInfo.size() > 0) {
			listSubject.setSelection(item + 2);
		}
	}

	/**
	 * 解析map数据
	 * @param map
	 * @param isForward 是否是向前加载的数据
	 *
	 * @return 返回加载的总数量;
	 */
	private int parseInfo(Map<String, String> map, boolean isForward) {
		int loadCount = 0;
		Map<String, String> theSubjectListMap = null;
		if (map.containsKey("theSubject")) {
			theSubjectListMap = UtilString.getListMapByJson(map.get("theSubject")).get(0);
			commentNum = Integer.parseInt(theSubjectListMap.get("commentNum"));//评论数
			subjectTitle = theSubjectListMap.get("title");//美食贴名称
			if (theSubjectListMap.containsKey("isFav")) {
				boolean isFav = theSubjectListMap.get("isFav").equals("2");//是否收藏过
				favoriteImageView.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
				favoriteTextView.setText(isFav ? "已收藏" : "  收藏  ");
			}
			if (theSubjectListMap.containsKey("type"))//贴子类
			{
				types = theSubjectListMap.get("type");
			}
			if (theSubjectListMap.containsKey("isJingHua"))//是否是精华
			{
				isJingHua = theSubjectListMap.get("isJingHua").equals("2");
			}

			if (theSubjectListMap.containsKey("classId"))//该贴存在的模块id
			{
				classId = theSubjectListMap.get("classId");
			}
			if (theSubjectListMap.containsKey("isLike")) {
				isLike = theSubjectListMap.get("isLike");
			}
		}
		if (map.containsKey("floor")) {    //楼层
			ArrayList<Map<String, String>> floorsList = UtilString.getListMapByJson(map.get("floor"));

			if (floorsList.size() > 0) {
				for (int i = 0; i < floorsList.size(); i++) {
					//点消息进去详情页时,给map加上颜色的key,并记录下楼层在list中的item.
					if (destFloorNum > 1) {

						if (floorsList.size() >= (i + 1) && floorsList.get(i).get("num").equals(destFloorNum + "")) {

							floorsList.get(i).put("bgColor", R.drawable.bg_round_msg_yellow + "");
							item = i;
						}
					}
					//当楼层被屏蔽的时候,直接将楼层remove.不显示.
					//					if (content != null
					//							&& ("0".equals(StringManager
					//									.getListMapByJson(
					//											floorsList.get(i).get("customer"))
					//									.get(0).get("code")) || content
					//									.equals("楼层已被屏蔽。"))||content == null
					//											&& ("0".equals(StringManager
					//													.getListMapByJson(
					//															floorsList.get(i).get("customer"))
					//													.get(0).get("code")))) {
					//						if (floorsList.size() >= (i+1) && floorsList.get(i).get("num").equals("1")) {
					//							Toast.makeText(getApplicationContext(),
					//									"此贴已被举报", Toast.LENGTH_SHORT).show();
					//							ShowSubject.this.finish();
					//						}
					//						floorsList.remove(i);
					//						i = i - 1;
					//					}
					if (floorsList.get(i).containsKey("isShow")) {
						String isShowString = floorsList.get(i).get("isShow");
						if ("1".equals(isShowString)) {
							if (floorsList.size() >= (i + 1) && floorsList.get(i).get("num").equals("1")) {
								Tools.showToast(getApplicationContext(), "此贴已被举报");
								ShowSubject.this.finish();
							}
							floorsList.remove(i);
							i = i - 1;
						}
					}

					loadCount = loadCount + 1;
				}
				Map<String, String> floorsMap = null;
				if (floorsList.size() > 0) {

					floorsMap = floorsList.get(0);
					//是否是楼主
					if (floorsMap.get("num").equals("1")) {
						//拿到分享的图片
						try {
							ArrayList<Map<String, String>> imgShow = UtilString.getListMapByJson(floorsMap.get("content"));

							if (imgShow.size() > 0) {
								for (int j = 0, size = imgShow.size(); j < size; j++) {
									if (!TextUtils.isEmpty(imgShow.get(j).get("img"))) {
										shareImg = imgShow.get(j).get("img");
										break;
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							UtilLog.print("d", "获取贴子图片出错");
						}

						// 楼主设置数据
						if (theSubjectListMap != null && floorsMap != null) {
							isHasVideo = louZhuHeadView.setData(theSubjectListMap, floorsMap);
							if (isHasVideo) {
								STATISTICS_ID = "a_quan_detail_video";
							}
						}
						//当加载失败后点击按钮的时候清空数据
						if (louZhuHeadViewLinearLayout.getChildCount() != 0) {
							louZhuHeadViewLinearLayout.removeAllViews();
							listDataSubjectInfo.clear();
						}
						// 有楼主再加楼主
						louZhuHeadViewLinearLayout.addView(louZhuHeadView);
						//分享时的内容
						subjectContent = getContentText(floorsMap);

						// 回复框1 设置数据
						try {
							barSubjectReply1.initView(ShowSubject.this, handler, subCode, subjectTitle, isLike, Integer.parseInt(theSubjectListMap.get("likeNum")), theSubjectListMap.get("commentNum"), classId);
						}catch (Exception e){e.printStackTrace();}
							if (floorsMap.containsKey("customer")) {
							ArrayList<Map<String, String>> user = UtilString.getListMapByJson(floorsMap.get("customer"));
							if (user.size() > 0) {
								louZhuMap = user.get(0);
								adapter.subjectOwer = user.get(0).get("code");
							}
						}

						floorsList.remove(0);//去掉楼主的数据,加载楼层时.
					}

					// 判断如果为加载上一页时.将数据放在集合的前面.
					if (isForward && destFloorNum > 1) {
						listDataSubjectInfo.addAll(0, floorsList);
					} else {
						listDataSubjectInfo.addAll(floorsList);
					}
					//预加载所有楼的回复布局
					for (int i = 0; i < floorsList.size(); i++) {
						Map<String, String> floorMap = floorsList.get(i);
						Map<String, String> user = UtilString.getListMapByJson(floorMap.get("customer")).get(0);
						adapter.getComments(floorMap, user.get("code"), false);
					}
				}
			}
		}
		listSubject.setVisibility(View.VISIBLE);
		barSubjectReply1.show();
		//举报框和分享按钮显示
		favLayout.setVisibility(View.VISIBLE);
		shareLayout.setVisibility(View.VISIBLE);
		reportLayout.setVisibility(View.VISIBLE);
		if (LoginManager.isManager()) {//是否是管理员
			recommendSubject.setVisibility(View.VISIBLE);
		} else {
			recommendSubject.setVisibility(View.GONE);
		}
		String nickNamex_xiaoxi,floorId_xiaoxi,code_xiaoxi;
		//当点击消息跳转过来时,弹起输入框,回复对应的人
		if (destFloorNum != 0 && commentId != null && !isMsgViewLoad && isOnce) {
			isOnce = false;
			barSubjectReply1.hide();
			for (int i = 0; i < listDataSubjectInfo.size(); i++) {
				if (listDataSubjectInfo.get(i).get("num").equals(destFloorNum + "")) {
					floorId_xiaoxi = listDataSubjectInfo.get(i).get("id");
					ArrayList<Map<String, String>> comments = UtilString.getListMapByJson(listDataSubjectInfo.get(i).get("comments"));
					for (int j = 0; j < comments.size(); j++) {
						if (comments.get(j).get("id").equals(commentId)) {
							Map<String, String> comeNameMap = UtilString.getListMapByJson(comments.get(j).get("customer")).get(0);
							code_xiaoxi = comeNameMap.get("code");
							nickNamex_xiaoxi = comeNameMap.get("nickName");
							barSubjectReply2.show(floorId_xiaoxi, destFloorNum + "", code_xiaoxi, nickNamex_xiaoxi, "");
						}
					}
				}
			}
		} else if (destFloorNum != 0 && commentId == null && !isMsgViewLoad && isOnce) {
			isOnce = false;
			if (destFloorNum != 1) {
				barSubjectReply1.hide();
				for (int i = 0; i < listDataSubjectInfo.size(); i++) {
					if (listDataSubjectInfo.get(i).get("num").equals(destFloorNum + "")) {
						floorId_xiaoxi = listDataSubjectInfo.get(i).get("id");

						Map<String, String> comments = UtilString.getListMapByJson(listDataSubjectInfo.get(i).get("customer")).get(0);
						code_xiaoxi = comments.get("code");
						nickNamex_xiaoxi = comments.get("nickName");
						barSubjectReply2.show(floorId_xiaoxi, destFloorNum + "", code_xiaoxi, nickNamex_xiaoxi, "");
						break;
					}
				}
			}
		}else if(destFloorNum == 0 && isReplayFloorOwner && isOnce){
			isOnce = false;
			barSubjectReply1.hide();
			barSubjectReply2.show();
		}
		return loadCount;
	}

	/*
	 * 处理从外界穿过来的点击事件,或者消息.
	 */
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Button loadMore = loadManager.getSingleLoadMore(null);
			switch (msg.what) {
				case ZAN_LZ_OVER:
					if (isXiaoXi) {
						louZhuHeadView.likeOver(msg.obj);
					} else if (!isXiaoXi && destFloorNum <= 6) {
						louZhuHeadView.likeOver(msg.obj);
					}
					break;
				case REPORT_CLICK:
					String id = (String) msg.obj;
					Intent intent = new Intent(ShowSubject.this, QuanReport.class);
					intent.putExtra("isQuan", "1");
					intent.putExtra("nickName", "举报此贴");
					intent.putExtra("code", id);
					intent.putExtra("repType", "2");
					intent.putExtra("subjectCode", subCode);
					ShowSubject.this.startActivityForResult(intent, 100);
					barSubjectReply2.keybroadShow(false);//解决举报框和输入法的冲突
					break;
				case REPLY_LZ_CLICK:
					XHClick.mapStat(ShowSubject.this, STATISTICS_ID, "评论部分总点击量", "");
					barSubjectReply1.hide();
					barSubjectReply2.show();
					break;
				case REPLY_LZ_OVER:
					isMsgViewLoad = true;//消息已经恢复,bar1 可以出现了.
					if (isSafa != null && isSafa != "" && "yes".equals(isSafa)) {
						CircleRobSofa.removeSofaItem(subCode);
					} else if (isSafa != null && isSafa != "" && "qiang".equals(isSafa)) {

					}

					@SuppressWarnings ("unchecked") Map<String, String> lzMap = (Map<String, String>) msg.obj;
					String returnObj = lzMap.get("returnObj");
					ArrayList<Map<String, String>> floorsInfo = UtilString.getListMapByJson(returnObj);
					Map<String, String> floorsmap = floorsInfo.get(0);

					// // 当回复的是每页加载的楼层数的倍数加一,不创建假界面,直接加载下一页
					// if ((Integer.parseInt(floorsmap.get("num"))) % 6 != 0
					// && loadManager.getLoadMoreBtnState().equals("加载更多")) {
					// // getSubjectInfo(false);
					// } else
					// repNewFloor(returnObj);
					if (Integer.parseInt(floorsmap.get("num")) <= 6) {
						repNewFloor(returnObj);
					} else if ((Integer.parseInt(floorsmap.get("num"))) % 6 != 0 && loadMore != null && loadMore.isEnabled()) {
						// getSubjectInfo(false);//楼层较多,无需加载.

					} else if ((Integer.parseInt(floorsmap.get("num"))) % 6 == 0 && loadMore != null && !loadMore.isEnabled()) {
						repNewFloor(returnObj);
					} else if (Integer.parseInt(floorsmap.get("num")) % 6 != 0 && loadMore != null && !loadMore.isEnabled()) {
						repNewFloor(returnObj);
					}
					// 调用
					if (isXiaoXi) {
						louZhuHeadView.replyOver();
					} else if (!isXiaoXi && destFloorNum <= 6) {
						louZhuHeadView.replyOver();
					}
					commentNum++;

					//统计 回复贴(计算事件)
					XHClick.onEventValue(getApplicationContext(), "quanOperate", "quanOperate", "回复贴", 1);

					break;
				case REPLY_LOU_CLICK:
					XHClick.mapStat(ShowSubject.this, STATISTICS_ID, "评论部分总点击量", "");
					@SuppressWarnings ("unchecked") Map<String, String> map = (Map<String, String>) msg.obj;
					barSubjectReply1.hide();
					barSubjectReply2.show(map.get("floorId"), map.get("floorNum"), map.get("code"), map.get("nickName"), map.get("louCode"));
					break;
				case REPLY_LOU_OVER:
					isMsgViewLoad = true;//消息已经恢复,bar1 可以出现了.

					@SuppressWarnings ("unchecked") Map<String, String> map2 = (Map<String, String>) msg.obj;
					String foorId = map2.get("foorId");
					String string2 = map2.get("returnObj");
					JSONObject louZhongLouObject = null;
					try {
						louZhongLouObject = new JSONObject(string2);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					// 更新一层的评论,将数据的返回值添加到原数据中,刷新
					for (int i = 0; i < listDataSubjectInfo.size(); i++) {
						if (listDataSubjectInfo.get(i).get("id").equals(foorId)) {
							String string = listDataSubjectInfo.get(i).get("comments");
							adapter.commentLayouts.remove(foorId);
							try {
								JSONArray array2 = new JSONArray(string);
								JSONArray put = array2.put(louZhongLouObject);
								listDataSubjectInfo.get(i).put("comments", put.toString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						}
					}
					adapter.notifyDataSetChanged();
					if (isXiaoXi) {
						louZhuHeadView.replyOver();
					} else if (!isXiaoXi && destFloorNum <= 6) {
						louZhuHeadView.replyOver();
					}
					commentNum++;

					//统计 回复楼(计算事件)
					XHClick.onEventValue(getApplicationContext(), "quanOperate", "quanOperate", "回复楼", 1);

					break;
				case REPORT_LOU_CLICK:
					@SuppressWarnings ("rawtypes") Map floorMap = (Map) msg.obj;
					String report = "举报 " + floorMap.get("nickName");
					Intent intentLou = new Intent(ShowSubject.this, QuanReport.class);
					intentLou.putExtra("isQuan", "1");
					intentLou.putExtra("nickName", report);
					intentLou.putExtra("code", floorMap.get("id").toString());
					intentLou.putExtra("repType", "2");
					intentLou.putExtra("subjectCode", subCode);
					ShowSubject.this.startActivityForResult(intentLou, 100);
					barSubjectReply2.keybroadShow(false);
					break;
				case REPORT_LOUZHONGLOU_CLICK:
					@SuppressWarnings ("rawtypes") Map floorMap2 = (Map) msg.obj;
					if (floorMap2.containsKey("num") && floorMap2.containsKey("nickName") && floorMap2.containsKey("id")) {
						String report2 = "举报 " + floorMap2.get("nickName");
						Intent intentLouZhongLou = new Intent(ShowSubject.this, QuanReport.class);
						intentLouZhongLou.putExtra("isQuan", "1");
						intentLouZhongLou.putExtra("nickName", report2);
						intentLouZhongLou.putExtra("code", floorMap2.get("id").toString());
						intentLouZhongLou.putExtra("repType", "3");
						intentLouZhongLou.putExtra("subjectCode", subCode);
						ShowSubject.this.startActivityForResult(intentLouZhongLou, 100);
					}
					barSubjectReply2.keybroadShow(false);
					break;
				case UploadSubjectNew.MSG_SEND_SUBJECT_ING:
					break;
				case UploadSubjectNew.MSG_SEND_SUBJECT_OK:
					break;
				case UploadSubjectNew.MSG_SEND_SUBJECT_FAILD:
					break;
			}
		}
	};

	/**
	 * 回复消息增加回复的消息界面
	 */
	private void repNewFloor(String returnObj) {
		Map<String, String> newFloor = new HashMap<>();
		newFloor.put("floor", returnObj.toString());
		// 立即添加回复信息到当前数据界面的下面;
		parseInfo(newFloor, false);
		adapter.notifyDataSetChanged();
	}

	// 发送的handler消息标识
	public final static int ZAN_LZ_OVER = 2;// 赞楼主完成
	public final static int REPORT_CLICK = 5;// 点击举报楼主
	public final static int REPLY_LZ_CLICK = 6;// 点击回复楼主
	public final static int REPLY_LZ_OVER = 7;// 回复楼主完成
	public final static int REPLY_LOU_CLICK = 8;// 点击回复楼层
	public final static int REPLY_LOU_OVER = 9;// 回复楼层完成
	public final static int REPORT_LOU_CLICK = 22;// 举报楼层点击
	public final static int REPORT_LOUZHONGLOU_CLICK = 23;// 举报楼中楼


	@Override
	public void onBackPressed() {
		if (louZhuHeadView != null && louZhuHeadView.onBackPressed())
			return;
		isBack = true;
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!isOnceStart){
			if (louZhuHeadView != null) {
				louZhuHeadView.onResume();
			}
		}
		isOnceStart = false;
	}


	@Override
	protected void onPause() {
		super.onPause();
		if(isBack){
			if (louZhuHeadView != null) {
				louZhuHeadView.onDestroy();
			}
		}else{
			if (louZhuHeadView != null) {
				louZhuHeadView.onPause();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
		UploadSubjectControl.getInstance().setReplyCallback(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1:
				data.getStringExtra("name");
				break;
			case 100:
				Log.v("requestCode", requestCode + "::" + resultCode);
				if (100 == resultCode) {
					this.finish();
				}
				break;
			case REQUEST_CODE_QUAN_FRIEND:
				barSubjectReply2.onActivityResult(requestCode,resultCode,data);
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 获取楼主全部内容:分享时要用
	 * @param map
	 * @return
	 */
	private String getContentText(Map<String, String> map) {
		String str = "";
		if (map.containsKey("content")) {
			ArrayList<Map<String, String>> list = UtilString.getListMapByJson(map.get("content"));
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).containsKey("text")) {
					str += UtilString.getListMapByJson(list.get(i).get("text")).get(0).get("");
				}
			}
		}
		return str;
	}



	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
		}
	}

	@Override
	public void finish() {
		long nowTime=System.currentTimeMillis();
		if(startTime>0&&(nowTime-startTime)>0&&!TextUtils.isEmpty(data_type)&&!TextUtils.isEmpty(module_type)){
			XHClick.saveStatictisFile("ShowSubject",module_type,data_type,subCode,"","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
		}
		super.finish();
	}
}
