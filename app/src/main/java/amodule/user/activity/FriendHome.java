package amodule.user.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.LayoutScroll;
import acore.widget.TextViewLimitLine;
import amodule._common.conf.GlobalAttentionModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.article.activity.ArticleDetailActivity;
import amodule.article.activity.ArticleUploadListActivity;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadParentSQLite;
import amodule.dish.db.UploadDishData;
import amodule.main.Main;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import amodule.shortvideo.activity.ShortPublishActivity;
import amodule.shortvideo.tools.ShortVideoPublishManager;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.view.TabContentView;
import amodule.user.view.UserHomeDish;
import amodule.user.view.UserHomeItem;
import amodule.user.view.UserHomeSubject;
import amodule.user.view.UserHomeTitle;
import amodule.user.view.UserHomeTxt;
import amodule.user.view.UserHomeVideo;
import aplug.basic.DefaultInternetCallback;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static acore.tools.ObserverManager.NOTIFY_SAVE_VIDEO_DRAF;
import static amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver.ACTION_ATT;
import static amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver.ACTION_DEL;
import static amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver.DATA_TYPE;
import static amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver.SECONDE_EDIT;
import static amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver.STATE_KEY;

@SuppressLint("CutPasteId")
public class FriendHome extends BaseActivity implements IObserver {
    public static final String TYPE_VIDEO = "1";
    public static final String TYPE_ARTICLE = "2";
    public static final String TYPE_SUBJECT = "-1";
    public static final String TYPE_DISH = "0";

    private TabHost tabHost;
    private LinearLayout activityLayout_show, tabMainMyself;

    private ArrayList<View> mTabViews = new ArrayList<>();
    private ArrayList<View> mTabViewsFloat = new ArrayList<>();
    private ArrayList<TabContentView> mTabContentViews = new ArrayList<>();
    private boolean[] mIsLoadeds = null;
    private String userCode = "";
    private CommonBottomView mCommonBottomView;

    private UserHomeTitle mUserHomeTitle;
    public LayoutScroll scrollLayout;
    public LinearLayout backLayout;
    public LinearLayout tabMainMyselfFloat;
    public TextViewLimitLine friend_info;
    private TextView mProgressTv;
    private RelativeLayout mProgressLayout;

    private int tabIndex = 0;
    private String tongjiId = "a_user";
    //外部传入舒适化类型
    private String extraType = "";//当前选择type类型
    public static boolean isAlive = false;

    private boolean mIsMySelf;

    private Set<String> mTabRefreshTypes = new HashSet<>();
    private boolean mIsResumming = false;
    private boolean mIsFromPause = false;

    //接收菜谱视频上传状态
    UploadStateChangeBroadcasterReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initData();
        initView();
        addListener();
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            userCode = bundle.getString("code");
            tabIndex = bundle.getInt("index");
            extraType = bundle.getString("type");
            //消息是否读过
            if (bundle.getString("newsId") != null) {
                String params = "type=news&p1=" + bundle.getString("newsId");
                ReqInternet.in().doPost(StringManager.api_setUserData, params, new DefaultInternetCallback());
            }
        }
        mIsMySelf = !TextUtils.isEmpty(userCode) && userCode.equals(LoginManager.userInfo.get("code"));
        if (mIsMySelf) {
            tongjiId = "a_my";
        }
        String className = this.getComponentName().getClassName();
        CommonBottonControl control = new CommonBottonControl();
        setContentView(control.setCommonBottonView(className, this, R.layout.a_my_friend_home));
        XHClick.track(this, "浏览个人主页");
        mCommonBottomView = control.mCommonBottomView;
        level = 4;
        setCommonStyle();
        handlerType();
        registePublishCallback();
        ObserverManager.getInstance().registerObserver(this,NOTIFY_SAVE_VIDEO_DRAF);
    }

    private void registePublishCallback() {
        if (LoginManager.isSlef(userCode)) {
            ShortVideoPublishManager.getInstance().setShortVideoUploadCallBack(new ShortVideoPublishManager.ShortVideoUploadCallBack() {
                @Override
                public void onSuccess(int id, Object msg) {
                    mProgressLayout.setVisibility(View.GONE);
                    refreTab(0);
                    Map<String, String> shareData = StringManager.getFirstMap(msg);
                    if (!shareData.isEmpty()) {
                        BarShare barShare = new BarShare(FriendHome.this, "", "");
                        barShare.setShare(BarShare.IMG_TYPE_WEB, shareData.get("title"), shareData.get("content"),
                                shareData.get("img"), shareData.get("url"));
                        barShare.openShare();
                    }
                }

                @Override
                public void onProgress(int progress, int id) {
                    if (mProgressLayout.getVisibility() == View.GONE) {
                        mProgressLayout.setVisibility(View.VISIBLE);
                        if (mTabContentViews.size() > 0 && mTabContentViews.get(0) != null
                                && mTabContentViews.get(0) instanceof UserHomeVideo) {
                            ((UserHomeVideo) mTabContentViews.get(0)).deleteById(id);
                        }
                    }
                    mProgressTv.setText(progress + "%");
                }

                @Override
                public void onFailed(int id) {
                    mProgressLayout.setVisibility(View.GONE);
                    refreTab(0);
                }
            });
        }
    }

    private void refreTab(int tabIndex) {
        if (tabIndex >= 0 && mTabContentViews != null
                && mTabContentViews.size() > tabIndex && mTabContentViews.get(tabIndex) != null) {
            mTabContentViews.get(tabIndex).initLoad();
        }
    }

    private void handlerType() {
        if (TextUtils.isEmpty(extraType)) return;
        if ("video".equals(extraType)) {
            tabIndex = 0;
        } else if ("dish".equals(extraType)) {
            tabIndex = 1;
        } else if ("subject".equals(extraType)) {
            tabIndex = 2;
        } else if ("article".equals(extraType)) {
            tabIndex = 3;
        }
    }

    private void initView() {
        scrollLayout = findViewById(R.id.scroll_body);
        // 滑动设置
        backLayout = findViewById(R.id.a_user_home_title);
        friend_info = findViewById(R.id.a_user_home_title_info);
        activityLayout_show = findViewById(R.id.a_user_home_title);
        activityLayout_show.setVisibility(View.INVISIBLE);
        mProgressLayout = findViewById(R.id.progress_layout);
        mProgressTv = findViewById(R.id.progress_text);
        //设置当向上滑动，浮动tab出现时，假状态栏的高度
        if (Tools.isShowTitle()) {
            View view = findViewById(R.id.a_user_home_float_title_view);
            LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.height = Tools.getStatusBarHeight(this);
        } else {
            findViewById(R.id.a_user_home_float_title_view).setVisibility(View.GONE);
        }

        mUserHomeTitle = new UserHomeTitle(this, findViewById(R.id.a_user_home_title), userCode);
    }

    private void addListener() {
        findViewById(R.id.a_user_home_title_back).setOnClickListener(v -> finish());
        loadManager.setLoading(v -> getData());
        friend_info.addOnClick((v, isNeedRefrash) -> {
            if (isNeedRefrash) doReload();
        });

        isAlive = true;
        registerBrocaster();
    }

    private boolean mFirstDataReady;
    private boolean mFirstLoaded;
    private boolean mSecondDataReady;
    private boolean mSecondLoaded;
    private boolean mShowErrorMsg = true;
    private ArrayList<Map<String, String>> mTabs;

    private void getData() {
        initDefTabData();
        String getUrl = StringManager.api_getUserInfoByCode + "?code=" + userCode;
        ReqInternet.in().doGet(getUrl, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
                    Map<String, String> userinfo_map = UtilString.getListMapByJson(list.get(0).get("userinfo")).get(0);
                    mTabs.get(1).put("num", userinfo_map.get("subjectCount"));
                    mTabs.get(2).put("num", userinfo_map.get("dishCount"));
                    mUserHomeTitle.setUserData(list.get(0).get("userinfo"));
                    mFirstDataReady = true;
                    onDataReady();
                } else {
                    onDataFailure(this, flag, returnObj);
                }
                mFirstLoaded = true;
                onLoaded(flag);
            }
        });
        ReqEncyptInternet.in().doEncypt(StringManager.API_USERMAIN_LEVEL, "code=" + userCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> list = StringManager.getListMapByJson(o);
                    Log.i("tzy", "loaded: list=" + list.toString());
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, String> map = list.get(i);
                        if (TYPE_VIDEO.equals(map.get("type"))) {
                            mTabs.get(0).put("num", map.get("num"));
                        } else if (TYPE_ARTICLE.equals(map.get("type"))) {
                            mTabs.get(3).put("num", map.get("num"));
                        }
                    }
                    mSecondDataReady = true;
                    onDataReady();
                } else {
                    onDataFailure(this, flag, o);
                }
                mSecondLoaded = true;
                onLoaded(flag);
            }
        });
    }

    private void initDefTabData() {
        mTabs = new ArrayList<>();
        mTabs.add(createTabData("视频",TYPE_VIDEO));
        mTabs.add(createTabData("晒美食",TYPE_SUBJECT));
        mTabs.add(createTabData("菜谱",TYPE_DISH));
        mTabs.add(createTabData("文章",TYPE_ARTICLE));
    }

    private Map<String,String> createTabData(String title,String type){
        Map<String, String> tabData = new HashMap<>();
        tabData.put("title", title);
        tabData.put("type", type);
        tabData.put("num", "");
        return tabData;
    }

    private void onLoaded(int flag) {
        if (mFirstLoaded && mSecondLoaded)
            loadManager.loadOver(flag);
    }

    private void onDataFailure(InternetCallback callback, int flag, Object returnObj) {
        if (callback == null || !mShowErrorMsg)
            return;
        mShowErrorMsg = false;
        callback.finish();
    }

    private void onDataReady() {
        if (!mFirstDataReady || !mSecondDataReady) {
            return;
        }
        handleTabsData();
        showUserAndTabUI();
        setTabHost();
    }

    private void showUserAndTabUI() {
        if (activityLayout_show != null)
            activityLayout_show.setVisibility(View.VISIBLE);
    }

    private void handleTabsData() {
        if (mTabs == null)
            return;
//        Iterator<Map<String, String>> iterator = mTabs.iterator();
//        while (iterator.hasNext()) {
//            Map<String, String> map = iterator.next();
//            if (map != null && map.size() > 0) {
//                String num = map.get("num");
//                String type = map.get("type");
//                if ((TYPE_VIDEO.equals(type) || TYPE_ARTICLE.equals(type))) {
//                    iterator.remove();
//                }
//            }
//        }
    }

    private void setTabHost() {
        if (mTabs == null || mTabs.size() <= 0)
            return;
        //获取控件高度
        tabHost = findViewById(R.id.tabhost);
        if (Main.allMain == null || Main.allMain.getLocalActivityManager() == null) {
            Tools.showToast(getApplicationContext(), "加载失败，请稍后重试");
            finish();
            return;
        }
        tabHost.setup(Main.allMain.getLocalActivityManager());
        tabMainMyself = findViewById(R.id.a_user_home_title_tab);
        tabMainMyselfFloat = findViewById(R.id.tab_float_mainMyself);
        if (mIsLoadeds == null)
            mIsLoadeds = new boolean[mTabs.size()];
        for (int i = 0; i < mTabs.size(); i++) {
            Map<String, String> tabMap = mTabs.get(i);
            if (tabMap != null && tabMap.size() > 0 && tabMap.containsKey("type")) {
                String title = tabMap.get("title");
                String num = tabMap.get("num");
                View tabView = getTabWidget(title, num, getTabClicker(i));
                View tabViewFloat = getTabWidget(title, num, getTabClicker(i));
                TabContentView tabContentView = null;
                String type = tabMap.get("type");

                switch (type) {
                    case TYPE_VIDEO://视频
                        tabContentView = new UserHomeVideo(this, userCode);
                        ((UserHomeVideo) tabContentView).setOnItemClickListener((itemView, dataMap) ->
                                onItemClickListener(itemView, dataMap, TYPE_VIDEO)
                        );
                        break;
                    case TYPE_ARTICLE://文章
                        tabContentView = new UserHomeTxt(this, userCode);
                        ((UserHomeTxt) tabContentView).setOnItemClickListener((itemView, dataMap) ->
                                onItemClickListener(itemView, dataMap, TYPE_ARTICLE)
                        );
                        break;
//                    case "3"://问答
//                        tabContentView = new UserHomeAnswer(this, userCode);
//                        ((UserHomeAnswer) tabContentView).setOnItemClickListener(new UserHomeItem.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(UserHomeItem itemView, Map<String, String> dataMap) {
//                                onItemClickListener(itemView, dataMap, "3");
//                            }
//                        });
//                        break;
                    case TYPE_SUBJECT://晒美食
                        tabContentView = new UserHomeSubject(this, userCode);
                        break;
                    case TYPE_DISH://菜谱
                        tabContentView = new UserHomeDish(this, userCode);
                        break;

                }
                if (tabContentView != null) {
                    tabContentView.setDataMap(tabMap);
                }
                mIsLoadeds[i] = false;
                mTabViews.add(tabView);
                mTabViewsFloat.add(tabViewFloat);
                mTabContentViews.add(tabContentView);
                tabHost.addTab(tabHost.newTabSpec(i + "").setIndicator(title).setContent(tabContentView));
                tabMainMyself.addView(tabView);
                tabMainMyselfFloat.addView(tabViewFloat);
            }
        }
        tabChanged(tabIndex);
    }

    private OnClickListener getTabClicker(final int i) {
        return v -> tabChanged(i);
    }

    private void onItemClickListener(final UserHomeItem itemView, final Map<String, String> dataMap, String listType) {
        if (dataMap == null || dataMap.size() < 1 || itemView == null)
            return;
        String dataFrom = dataMap.get("dataFrom");
        if ("1".equals(dataFrom)) {//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
            String uploadType = dataMap.get("uploadType");
            String hasMedia = dataMap.get("hasMedia");
            Map<String, String> tabMap = mTabContentViews.get(tabIndex).getDataMap();
            if (tabMap != null && tabMap.size() > 0) {
                final String type = tabMap.get("type");
                switch (type) {
                    case TYPE_VIDEO://视频列表
                        Intent publishIntent = new Intent(this, ShortPublishActivity.class);
                        publishIntent.putExtra("id", dataMap.get("id"));
                        publishIntent.putExtra("extraDataJson", dataMap.get("extraDataJson"));
                        startActivity(publishIntent);
                        break;
                    case TYPE_ARTICLE://文章列表
                        String id = dataMap.get("id");
                        if (TextUtils.isEmpty(id))
                            return;
                        UploadArticleSQLite parentSQL = new UploadArticleSQLite(this);
                        int dataType = EditParentActivity.DATA_TYPE_ARTICLE;
                        final UploadArticleData articleData = parentSQL.selectById(Integer.parseInt(id));
                        if ("2".equals(hasMedia)) { //如果包括多媒体资源，点击则进入上传列表页面
                            Intent intent = new Intent(FriendHome.this, ArticleUploadListActivity.class);
                            intent.putExtra("draftId", articleData.getId());
                            intent.putExtra("dataType", dataType);
                            intent.putExtra("coverPath", articleData.getImg());
                            if (UploadDishData.UPLOAD_ING.equals(uploadType))
                                intent.putExtra("isAutoUpload", true);
                            String videoPath = "";
                            ArrayList<Map<String, String>> videoArray = articleData.getVideoArray();
                            if (videoArray != null && videoArray.size() > 0) {
                                videoPath = videoArray.get(0).get("video");
                            }
                            intent.putExtra("finalVideoPath", videoPath);
                            FriendHome.this.startActivity(intent);
                        } else if (UploadDishData.UPLOAD_FAIL.equals(uploadType)) {
                            if ("2".equals(type)) { //如果是无多媒体资源文章并且上传失败，点击进入编辑页面
                                Intent intent = new Intent(FriendHome.this, ArticleEidtActivity.class);
                                intent.putExtra("draftId", articleData.getId());
                                startActivity(intent);
                                return;
                            }
                            //如果是无多媒体资源视频并且上传失败，点击重新上传
                            dataMap.put("uploadType", UploadDishData.UPLOAD_ING);
                            itemView.notifyUploadStatusChanged(UploadDishData.UPLOAD_ING);
                            articleData.setUploadType(UploadDishData.UPLOAD_ING);
                            parentSQL.update(articleData.getId(), articleData);
                            final UploadParentSQLite mySql = parentSQL;
                            articleData.upload(StringManager.api_videoAdd, new InternetCallback() {
                                @Override
                                public void loaded(int i, String s, Object o) {
                                    if (i >= UtilInternet.REQ_OK_STRING) {
                                        dataMap.put("uploadType", UploadDishData.UPLOAD_SUCCESS);
                                        itemView.notifyUploadStatusChanged(UploadDishData.UPLOAD_SUCCESS);
                                        mySql.deleteById(articleData.getId());
                                    } else {
                                        dataMap.put("uploadType", UploadDishData.UPLOAD_FAIL);
                                        itemView.notifyUploadStatusChanged(UploadDishData.UPLOAD_FAIL);
                                        articleData.setUploadType(UploadDishData.UPLOAD_FAIL);
                                        mySql.update(articleData.getId(), articleData);
                                    }
                                }
                            });
                        } else if (UploadDishData.UPLOAD_PAUSE.equals(uploadType)) {
                            //如果是无多媒体资源视频或文章并且暂停上传，点击重新上传
                            dataMap.put("uploadType", UploadDishData.UPLOAD_ING);
                            itemView.notifyUploadStatusChanged(UploadDishData.UPLOAD_ING);
                            articleData.setUploadType(UploadDishData.UPLOAD_ING);
                            parentSQL.update(articleData.getId(), articleData);
                            final UploadParentSQLite mySql = parentSQL;
                            String url = StringManager.api_articleAdd;
                            articleData.upload(url, new InternetCallback() {
                                @Override
                                public void loaded(int i, String s, Object o) {
                                    if (i >= UtilInternet.REQ_OK_STRING) {
                                        dataMap.put("uploadType", UploadDishData.UPLOAD_SUCCESS);
                                        mySql.deleteById(articleData.getId());
                                        sendBroadcast(true, type);
                                    } else {
                                        dataMap.put("uploadType", UploadDishData.UPLOAD_FAIL);
                                        itemView.notifyUploadStatusChanged(UploadDishData.UPLOAD_FAIL);
                                        articleData.setUploadType(UploadDishData.UPLOAD_FAIL);
                                        mySql.update(articleData.getId(), articleData);
                                    }
                                }
                            });
                        }
                        break;
                }
            }
        } else {
            if (TYPE_VIDEO.equals(listType)) {
                String gotoUrl = dataMap.get("url");
                if (!TextUtils.isEmpty(gotoUrl)) {
                    gotoUrl = gotoUrl + "&userCode=" + userCode;
                    AppCommon.openUrl(gotoUrl, false);
                }
            } else if (TYPE_ARTICLE.equals(listType)) {
                String code = dataMap.get("code");
                if (!TextUtils.isEmpty(code)) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("code", code);
                    intent.putExtras(bundle);
                    intent.setClass(this, ArticleDetailActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    private void sendBroadcast(boolean flag, String type) {
        Intent broadIntent = new Intent();
        broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
        if (!TextUtils.isEmpty(type))
            broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, type);
        broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY,
                flag ? UploadStateChangeBroadcasterReceiver.STATE_SUCCESS : UploadStateChangeBroadcasterReceiver.STATE_FAIL);
        Main.allMain.sendBroadcast(broadIntent);

    }


    // 获取tab标签卡
    private View getTabWidget(String title, String num, OnClickListener clicker) {
        View view = View.inflate(this, R.layout.tab_item_img_text, null);
        TextView tv = view.findViewById(R.id.tab_title);
        tv.setText(title);
        TextView data = view.findViewById(R.id.tab_data);
        data.setText(num);
        view.setOnClickListener(clicker);
        LayoutParams lp = new LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        view.setLayoutParams(lp);
        return view;
    }

    // 切换tab
    private void tabChanged(int tabIndex) {
        this.tabIndex = tabIndex;
        //统计
        switch (tabIndex) {
            case 0:
                XHClick.mapStat(this, tongjiId, "导航", "视频");
                break;
            case 1:
                XHClick.mapStat(this, tongjiId, "导航", "美食贴");
                break;
            case 2:
                XHClick.mapStat(this, tongjiId, "导航", "菜谱");
                break;
            case 3:
                XHClick.mapStat(this, tongjiId, "导航", "文章");
                break;
        }
        String tag = mTabContentViews.get(tabHost.getCurrentTab()).onPause();
        tabHost.setCurrentTab(tabIndex);
        int tabNum = tabHost.getTabWidget().getChildCount();
        for (int i = 0; i < tabNum; i++) {
            tabSelectStyle(mTabViews.get(i), i == tabHost.getCurrentTab());
            tabSelectStyle(mTabViewsFloat.get(i), i == tabHost.getCurrentTab());
        }
        TabContentView currTabView = mTabContentViews.get(tabIndex);
        String tabType = currTabView.getDataMap().get("type");
        if (mTabRefreshTypes.contains(tabType)) {
            mIsLoadeds[tabIndex] = false;
            mTabRefreshTypes.remove(tabType);
        }
        if (!mIsLoadeds[tabIndex]) {
            currTabView.initLoad();
            mIsLoadeds[tabIndex] = true;
        }
        currTabView.onResume(tag);
    }

    // 设置tab选中的样式
    private void tabSelectStyle(View tabView, boolean isSelect) {
        TextView tv = tabView.findViewById(R.id.tab_title);
        String color = Tools.getColorStr(this, R.color.comment_color);
        if (isSelect)
            tv.setTextColor(Color.parseColor(color));
        else
            tv.setTextColor(Color.parseColor("#333333"));
    }

    // 重载
    public void doReload() {
        for (int i = 0; i < mIsLoadeds.length; i++)
            mIsLoadeds[i] = false;
        tabChanged(tabHost.getCurrentTab());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumming = true;
        if (mIsFromPause) {
            mIsFromPause = false;
            if (mIsRefreshUserInfo)
                updateUserAttention(true);
            if (mTabContentViews != null && tabIndex >= 0 && mTabContentViews.size() > tabIndex && mTabContentViews.get(tabIndex) != null) {
                TabContentView currTabView = mTabContentViews.get(tabIndex);
                String tabType = currTabView.getDataMap().get("type");
                if (!TextUtils.isEmpty(tabType) && mTabRefreshTypes.contains(tabType)) {
                    mTabRefreshTypes.remove(tabType);
                    CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
                    currTabView.onResume("resume");
                }
            }

            GlobalAttentionModule module = GlobalVariableConfig.containsAttentionModule(userCode);
            if (module != null) {
                mUserHomeTitle.notifyAttentionInfo(module.isAttention());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResumming = false;
        mIsFromPause = true;
        //view失焦点
        for (int i = 0; i < mTabContentViews.size(); i++) {
            if (mTabContentViews.get(i) instanceof UserHomeSubject) {
                ((UserHomeSubject) mTabContentViews.get(i)).onViewPause();
            }
        }
    }

    @Override
    public void finish() {
        for (int i = 0; i < mTabContentViews.size(); i++) {
            if (mTabContentViews.get(i) != null) mTabContentViews.get(i).finish();
        }
        super.finish();
        isAlive = false;
    }

    private void registerBrocaster() {
        receiver = new UploadStateChangeBroadcasterReceiver(
                new UploadStateChangeBroadcasterReceiver.ReceiveBack() {
                    @Override
                    public void onGetReceive(Intent intent) {
                        String isSecondEdit = intent.getStringExtra(SECONDE_EDIT);
                        String state = intent.getStringExtra(STATE_KEY);
                        String dataType = intent.getStringExtra(DATA_TYPE);
                        String actionDel = intent.getStringExtra(ACTION_DEL);
                        String actionAtt = intent.getStringExtra(ACTION_ATT);
                        if (!TextUtils.isEmpty(dataType)) {
                            if (mTabContentViews != null && tabIndex >= 0 && mTabContentViews.size() > tabIndex) {
                                TabContentView currTabView = mTabContentViews.get(tabIndex);
                                if (currTabView != null) {
                                    String tabType = currTabView.getDataMap().get("type");
                                    if (mTabRefreshTypes != null) {
                                        if (dataType.equals(tabType) && mIsResumming) {
                                            mTabRefreshTypes.remove(tabType);
                                            CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);
                                            currTabView.onResume("resume");
                                        } else if (!mTabRefreshTypes.contains(dataType)) {
                                            mTabRefreshTypes.add(dataType);
                                        }
                                    }
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(dataType) && !"-1".equals(dataType)) {
                            if (!TextUtils.isEmpty(actionDel) && "2".equals(actionDel))
                                updateTabNum(true);
                            else if (!TextUtils.isEmpty(state) && UploadStateChangeBroadcasterReceiver.STATE_SUCCESS.equals(state) && !"2".equals(isSecondEdit))
                                updateTabNum(false);
                        }
                        //文章、视频详情页的关注，并且是成功了才会有这个值。
                        if (!TextUtils.isEmpty(actionAtt)) {
                            mIsRefreshUserInfo = true;
                        }
                    }
                }
        );
        receiver.register(this);
    }

    private void updateTabNum(boolean isDel) {
        if (tabMainMyself == null || tabMainMyselfFloat == null)
            return;
        View view1 = tabMainMyself.getChildAt(tabIndex);
        View view2 = tabMainMyselfFloat.getChildAt(tabIndex);
        TextView tv = view1.findViewById(R.id.tab_data);
        int num1 = Integer.parseInt(tv.getText().toString());
        if (isDel)
            --num1;
        else
            ++num1;
        if (num1 > 0)
            tv.setText(num1 + "");
        else
            tv.setText(0 + "");
        TextView tv2 = view2.findViewById(R.id.tab_data);
        int num2 = Integer.parseInt(tv2.getText().toString());
        if (isDel)
            --num2;
        else
            ++num2;
        if (num2 > 0)
            tv2.setText(num2 + "");
        else
            tv2.setText(0 + "");
    }

    private boolean mIsRefreshUserInfo = false;

    private void updateUserAttention(boolean attention) {
        mIsRefreshUserInfo = false;
        if (mUserHomeTitle != null) {
            mUserHomeTitle.notifyAttentionInfo(attention);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if(TextUtils.equals(NOTIFY_SAVE_VIDEO_DRAF,name)){
            refreTab(0);
        }
    }
}
