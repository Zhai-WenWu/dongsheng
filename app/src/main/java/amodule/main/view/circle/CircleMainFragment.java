package amodule.main.view.circle;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.AppCommon;
import acore.logic.load.LoadManager;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvListView;
import amodule.quan.adapter.AdapterMainCircle;
import amodule.quan.db.PlateData;
import amodule.quan.tool.QuanAdvertControl;
import amodule.quan.view.CircleHeaderView;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.VideoImageView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.ad.BannerAd;

import static com.xiangha.R.id.return_top;

/**
 * PackageName : amodule.main.view.circle
 * Created by MrTrying on 2016/8/24 18:34.
 * E_mail : ztanzeyu@gmail.com
 */
public class CircleMainFragment extends Fragment {
    /** 保存板块信息的key */
    protected static final String PLATEDATA = "plate_data";
    public static final String CIRCLENAME = "circle_name";
    /** 依附的Activity */
    private MainBaseActivity mActivity;
    /** 是否加载完成 */
    private boolean LoadOver = false;
    private LoadManager mLoadManager = null;
    /**
     * 圈子列表的头部局
     * 包含置顶，公告，活动，发贴界面，发贴失败界面
     */
    private CircleHeaderView mCircleHeaderView;
    private PtrClassicFrameLayout refreshLayout;
    /** 展示列表 */
    private RvListView mListView;
    /** Fragment 的 root view */
    private View mView;

    private ImageView returnTop;
    /** 当前page */
    private int mCurrentPage = 0;
    /** 每页的数据数量 */
    private int mEveryPageNum = 0;
    /** 贴子的数据集合 */
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();
    /** 圈子数据 */
    private ArrayList<Map<String, String>> mCircleData = new ArrayList<>();
    /** 置顶和公告的数据集合 */
    private ArrayList<Map<String, String>> mRobRoNoticeData = new ArrayList<>();
    /** 推荐用户 */
    private ArrayList<Map<String, String>> mRecUserData = new ArrayList<>();
    /** 当前板块的信息 */
    private PlateData mPlateData;
    /** fragment在fragmentManager管理的集合中index */
    int position = 0;
    String isLocation = "1";
    /** 是否初始化 */
    protected boolean isPrepared = false;
    /** 是否显示 */
    protected boolean isVisible;
    /** 板块的mid */
    protected String mid = "";
    protected String mCircleName = "";
    /**  */
    private AdapterMainCircle mAdapter;
    String mPageTime = "";
    String mStartTime = "";
    private String noDataNotice_1 = "", noDataNotice_2 = "", noDataUrl = "";
    private int index_size = 0;

    private ConnectionChangeReceiver connectionChangeReceiver;
    private boolean isAutoPaly = false;
    private int firstVisibleItems = 0;

    public CircleMainFragment() {
        super();
    }

    private boolean isLoadAd = true;//是否加载广告
    private QuanAdvertControl quanAdvertControl;
    //对视频的处理
    private LinearLayout video_layout;
    private VideoImageView videoImageView;
    private int headerCount = 0;//存在listview头数据

    private CircleHeaderAD mCircleHeaderAD;
    private boolean mAdBannerViewIsInScreen = false;

    public static CircleMainFragment newInstance(PlateData plateData) {
        CircleMainFragment fragment = new CircleMainFragment();
        fragment.setPosition(plateData.getPosition());
        fragment.setIsLocation(plateData.isLocation() ? PlateData.LOCATION : PlateData.UNLOCATION);
        fragment.setmPlateData(plateData);
        return (CircleMainFragment) setArgumentsToFragment(fragment, plateData);
    }

    /** 将储块信息存板到Argument中 */
    public static Fragment setArgumentsToFragment(Fragment fragment, PlateData plateData) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PLATEDATA, plateData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = (MainBaseActivity) activity;
        super.onAttach(activity);
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        registnetworkListener();
        mPlateData = (PlateData) getArguments().getSerializable(PLATEDATA);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistnetworkListener();
        mActivity.getActMagager().unregisterADController(quanAdvertControl);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.circle_main_fragment_list, null);
        mCircleHeaderView = new CircleHeaderView(mActivity);
        mCircleHeaderView.setStiaticID(mPlateData.getStiaticID());
        refreshLayout = (PtrClassicFrameLayout) mView.findViewById(R.id.refresh_list_view_frame);
        mListView = (RvListView) mView.findViewById(R.id.rvListview);
        mListView.getItemAnimator().setChangeDuration(0);
        returnTop = (ImageView) mView.findViewById(return_top);
        returnTop.setOnClickListener(v -> {
            returnTop.clearAnimation();
            RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(500);
            animation.setFillAfter(true);// 特效animation设置
            returnTop.startAnimation(animation);
            returnListTop();
            refresh();
        });
        //初始化AD
        initAd();
        mListView.addHeaderView(mCircleHeaderView);
        headerCount++;
        mLoadManager = mActivity.loadManager;
        LoadOver = false;
        isPrepared = true;
        preLoad();
        return mView;
    }

    private void initAd() {
        PlateData plateData = getCurrentPlateData();
        if (plateData != null && plateData.isShowAd()) {
            RelativeLayout headerLayout = new RelativeLayout(getContext());
            mCircleHeaderAD = new CircleHeaderAD(mActivity);
            mCircleHeaderAD.setStiaticID(mPlateData.getStiaticID());
            mCircleHeaderAD.init(mActivity, new BannerAd.OnBannerListener() {
                @Override
                public void onShowAd() {
                    statisticBanner();
                }

                @Override
                public void onClickAd() {

                }

                @Override
                public void onImgShow(int imgH) {

                }
            });
            headerLayout.addView(mCircleHeaderAD);
            mListView.addHeaderView(headerLayout);
            headerCount++;
        }
    }

    public PlateData getCurrentPlateData() {
        PlateData plateData = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            plateData = (PlateData) bundle.getSerializable(PLATEDATA);
        }
        return plateData;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void registerAdController() {
        if(mActivity != null
                && mActivity.getActMagager() != null
                && quanAdvertControl != null){
            mActivity.getActMagager().registerADController(quanAdvertControl);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopVideo();
    }

    /**
     * 在这里实现Fragment数据的缓加载.
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        preLoad();
        if (mLoadManager != null && mListView != null) {
            Button loadMore = mLoadManager.getSingleLoadMore(mListView);
            if (loadMore != null) {
                loadMore.setVisibility(mListData.size() == 0 ? View.INVISIBLE : View.VISIBLE);
            }
        }
    }

    protected void onInvisible() {
    }

    protected void preLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        //填充各控件的数据
        //防止二次生成
        if (!LoadOver) {
            init();
        }
    }

    private void init() {
        //更新ListView的空header的高度
        mAdapter = new AdapterMainCircle(mActivity, mListData);
        mAdapter.setStiaticKey(mPlateData.getStiaticID());
        mAdapter.setModuleName(mPlateData.getName());
        mAdapter.setCircleName(mCircleName);
        mAdapter.setmRecommendCutomerCallBack(() -> getSpareUser(false));
        if (!LoadOver) {
            //设置加载，并传入PlaceHoderHeaderLayout设置滑动加载
            mLoadManager.setLoading(refreshLayout, mListView, mAdapter, true,
                    v -> {
                        getData(true);
                        //刷新广告
                        quanAdvertControl.getAdData(mActivity);
                    },
                    v -> getData(!LoadOver));
            mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                final int topRedundant = Tools.getDimen(getContext(), R.dimen.topbar_height) + Tools.getStatusBarHeight(getContext());
                final int bottomRedundant = Tools.getDimen(getContext(), R.dimen.dp_50);
                final int Min = topRedundant;
                final int Max = (ToolsDevice.getWindowPx(getContext()).heightPixels - topRedundant - bottomRedundant) * 3 / 5 + topRedundant;
                int currentPlayPosition = -1;
                LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) mListView.getLayoutManager();

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (videoImageView != null && videoImageView.getIsPlaying()) {//滑动暂停
                        stopVideo();
                    }
                    if (!isAutoPaly) {
                        return;
                    }
                    final int length = mLinearLayoutManager.getChildCount();
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        int count = 0;
                        int index = 0;
                        for (; index < length; index++) {
                            View itemView = mLinearLayoutManager.getChildAt(index);
                            int top = itemView.getTop();
                            int height = itemView.getHeight();
                            final int value = height * 4 / 7 + top;
                            if (itemView instanceof NormalContentView) {
                                if (value <= Max && value >= Min) {
                                    ((NormalContentView) itemView).startVideoView();
                                    currentPlayPosition = mLinearLayoutManager.getPosition(itemView);
                                    Log.i("zhangyujian", "自动数据的位置:::" + ((NormalContentView) itemView).getPositionNow());
                                    setVideoLayout(itemView, ((NormalContentView) itemView).getPositionNow());
                                } else {
                                    count++;
                                }
                            }
                        }
                        if (count == index) {
                            mAdapter.setCurrentPlayPosition(-1);
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        firstVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition();
                        setQuanmCurrentPage();
                    }

                    statisticBanner();
                }
            });
            LoadOver = true;
        }
        quanAdvertControl = new QuanAdvertControl(mActivity);
        registerAdController();
        quanAdvertControl.setCallBack(isRefresh -> {
                index_size = 0;
                mListData = quanAdvertControl.getAdvertAndQuanData(isRefresh,mListData, mPlateData.getCid(), mPlateData.getMid(), index_size);
                //Log.i("FRJ","广告数据回来刷新adapter:::集合大小："+mListData.size());
                safeNotifyItemRangeChanged();
                index_size = mListData.size();
        });
        quanAdvertControl.getAdData(mActivity);
        mAdapter.setQuanAdvertControl(quanAdvertControl);
        //视频被点击事件
        mAdapter.setVideoClickCallBack(position -> {
            int firstVisiPosi = ((LinearLayoutManager) mListView.getLayoutManager()).findFirstVisibleItemPosition();
            //要获得listview的第n个View,则需要n减去第一个可见View的位置。+1是因为有header
            View parentView = mListView.getChildAt(position - firstVisiPosi + headerCount);
            if (parentView == null)
                return;
            setVideoLayout(parentView, position);
        });
    }

    private void statisticBanner() {
        if (mCircleHeaderAD == null || !"xh".equals(mCircleHeaderAD.getAdType()))
            return;
        boolean visibleToUser = onBannerViewVisibleToUser();
        if(visibleToUser){
            mCircleHeaderAD.onAdBind();
        }
    }

    private boolean onBannerViewVisibleToUser() {
        boolean inScreen = adBannerOnScreen();
        if (inScreen && !mAdBannerViewIsInScreen) {
            mAdBannerViewIsInScreen = inScreen;
            return true;
        } else if (!inScreen){
            mAdBannerViewIsInScreen = false;
        }
        return false;
    }

    private boolean adBannerOnScreen() {
        if (mCircleHeaderAD != null && mCircleHeaderAD.getVisibility() == View.VISIBLE) {
            int bannerViewHeight = mCircleHeaderAD.getHeight();
            int[] location = new int[2];
            int titleHeight = Tools.getDimen(mActivity, R.dimen.topbar_height);
            mCircleHeaderAD.getLocationOnScreen(location);
            if ((location[1] + bannerViewHeight) > titleHeight
                    && location[1] < (titleHeight + mListView.getHeight())) {
                return true;
            }
        }
        return false;
    }

    private void safeNotifyItemRangeChanged() {
        new Handler().post(() -> {
            if(mAdapter != null){
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 处理view,video
     *
     * @param parentView
     * @param position
     */
    private void setVideoLayout(View parentView, final int position) {
        if (mListData.get(position).containsKey("selfVideo") && !TextUtils.isEmpty(mListData.get(position).get("selfVideo"))) {
            Map<String, String> videoData = StringManager.getFirstMap(mListData.get(position).get("selfVideo"));
            if (videoImageView == null)
                videoImageView = new VideoImageView(mActivity);
            videoImageView.setImageBg(videoData.get("sImgUrl"));
            videoImageView.setVideoData(videoData.get("videoUrl"));
            videoImageView.setVisibility(View.VISIBLE);
            if (video_layout != null && video_layout.getChildCount() > 0) {
                video_layout.removeAllViews();
            }

            video_layout = (LinearLayout) parentView.findViewById(R.id.video_layout);
            if (videoImageView != null && video_layout != null) {
                video_layout.addView(videoImageView);
                videoImageView.onBegin();
                videoImageView.setVideoClickCallBack(() -> {
                    stopVideo();
                    goNextActivity(position);
                });
            }
        }
    }

    /**
     * 暂停播放
     */
    private void stopVideo() {
        if (videoImageView != null) {
            videoImageView.onVideoPause();
            videoImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 详情页
     */
    private void goNextActivity(int position) {
        String isSafa = "";
        Map<String, String> map = mListData.get(position);
        if (map.containsKey("isSafa"))
            isSafa = map.get("isSafa");
        if (map.containsKey("style") && map.get("style").equals("6"))
            isSafa = "qiang";
        AppCommon.openUrl(mActivity, "subjectInfo.app?code=" + map.get("code") + "&isSafa=" + isSafa, true);
    }

    /**
     * 获取数据
     *
     * @param isRefresh
     */
    protected void getData(final boolean isRefresh) {
        if (isRefresh) {
            mCurrentPage = 0;
            mEveryPageNum = 0;
            mPageTime = "";
            mStartTime = "";
            getSpareUser(isRefresh);
            getTopUser();
            refreshAdData();
        }
        mCurrentPage++;
        String url = StringManager.api_circleSubjectList + "?cid=" + mPlateData.getCid() + "&mid=" + mPlateData.getMid();
        String param = "&page=" + mCurrentPage + "&pageTime=" + mPageTime;
        if (!TextUtils.isEmpty(mStartTime)) {
            param += "&startTime=" + mStartTime;
        }

        setQuanmCurrentPage();

        //更新加载按钮状态
        mLoadManager.loading(mListView,  isRefresh);
        if (isRefresh) {
            mLoadManager.hideProgressBar();
        }
        ReqInternet.in().doGet(url + param, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                int loadCount = 0;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (isRefresh) {
                        index_size = 0;
                        mRobRoNoticeData.clear();
                        mListData.clear();
                    }
                    List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);
                    if (returnData.size() > 0) {
                        String promptJson = returnData.get(0).get("prompt");
                        List<Map<String, String>> promptData = StringManager.getListMapByJson(promptJson);
                        if (promptData.size() > 0) {
                            Map<String, String> prompt = promptData.get(0);
                            noDataNotice_1 = prompt.get("desc");
                            noDataNotice_2 = prompt.get("aStr");
                            noDataUrl = prompt.get("aUrl");
                        }
                        String startTime = returnData.get(0).get("startTime");
                        if (!TextUtils.isEmpty(startTime)) {
                            mStartTime = startTime;
                        }
                        String dataType = returnData.get(0).get("dataType");
                        returnData = StringManager.getListMapByJson(returnData.get(0).get("data"));
                        //判断列表数据类型-----------------1：贴子 2：用户
                        if ("2".equals(dataType)) {
                            for (int index = 0, length = returnData.size(); index < length; index++) {
                                Map<String, String> map = returnData.get(index);
                                String upInfo = map.get("upInfo");//upInfo	更新说明
                                mListView.scrollToPosition(0);
                                if (!TextUtils.isEmpty(upInfo) && !"null".equals(upInfo)) {
                                    mCircleHeaderView.initTopView(upInfo, "#ffffff", "#b6b6b6", true);
                                }
                                List<Map<String, String>> customersData = StringManager.getListMapByJson(map.get("customers"));
                                if (customersData.size() > 0) {
                                    for (int i = 0, customersLength = customersData.size(); i < customersLength; i++) {
                                        Map<String, String> customer = customersData.get(i);
                                        customer.put("dataType", "2");
                                        customer.put("customer_count", String.valueOf(i + 1));
                                    }
                                    mListData.addAll(customersData);
                                    loadCount = customersData.size();
                                }
                            }
                        } else {
                            for (int index = 0, length = returnData.size(); index < length; index++) {
                                Map<String, String> map = returnData.get(index);
                                //请求的第一页数据中，包含公告、置顶、活动的item数据是不记录在每页的数据count中的
                                String style = map.get("style");
                                if (style != null) {
                                    if (style.equals("2") || style.equals("3") || style.equals("4")) {
                                        mRobRoNoticeData.add(map);
                                    } else if (style.equals("7")) {
                                        mCircleData = StringManager.getListMapByJson(map.get("list"));
                                    } else {
                                        map.put("dataType", "1");
                                        //添加是否定位字段
                                        map.put("isLocation", String.valueOf(isLocation));
                                        mListData.add(map);
                                        if (!style.equals("5") && !style.equals("6")) {
                                            loadCount++;
                                        }
                                    }
                                    if (!style.equals("6")) {
                                        mPageTime = map.get("floorTime");
                                    }
                                }
                            }
                        }
                    }
                }
                int size = mListData.size();
                mListData = quanAdvertControl.getAdvertAndQuanData(mListData, mPlateData.getCid(), mPlateData.getMid(), index_size);
                if (size < mListData.size()) isLoadAd = false;
                index_size = mListData.size();
                if (mEveryPageNum == 0) {
                    mEveryPageNum = loadCount;
                }
                //刷新数据
                //添加此判断，是因为此处有adapter的 NullPointerException
                //不为null则刷新，为null则尝试从listview.getAdapter()获取adapter并刷新数据
                if (mAdapter != null) {
//                    mAdapter.notifyDataSetChanged();
                    safeNotifyItemRangeChanged();
                } else if (mListView != null && mListView.getAdapter() != null) {
                    mAdapter = (AdapterMainCircle) mListView.getAdapter();
//                    mAdapter.notifyDataSetChanged();
                    safeNotifyItemRangeChanged();
                }
                mLoadManager.loadOver(flag,mListView,loadCount);
                //判断是否刷新
                if (isRefresh) {
                    //添加置顶和公告data
                    PlateData plateData = getCurrentPlateData();
                    if (plateData.isShowAllQuan()) {
                        mCircleHeaderView.initAllQuan(mCircleData);
                    }
                    if (plateData.isShowScrollTop()) {
                        mCircleHeaderView.initNewSticky(mRobRoNoticeData);
                    } else {
                        mCircleHeaderView.initMiddleView(mRobRoNoticeData);
                    }
                    refreshLayout.refreshComplete();
                }
                //如果没有数据显示提示
                if (mListData.size() == 0) {
                    mCircleHeaderView.showNoDataView(noDataNotice_1, noDataNotice_2, content -> {
                        if (!TextUtils.isEmpty(noDataUrl)) {
                            AppCommon.openUrl(mActivity, noDataUrl, true);
                        }
                    });
                    mView.findViewById(R.id.return_top_rela).setVisibility(View.GONE);
                    returnTop.setVisibility(View.GONE);
                } else {
                    if (mCurrentPage >= 3) {
                        mView.findViewById(R.id.return_top_rela).setVisibility(View.VISIBLE);
                        returnTop.setVisibility(View.VISIBLE);
                    } else {
                        mView.findViewById(R.id.return_top_rela).setVisibility(View.GONE);
                        returnTop.setVisibility(View.GONE);
                    }
                    mCircleHeaderView.hideNoDataView();
                }
            }
        });
    }

    private void getTopUser() {
        if (mPlateData.isShowRecUser()) {
            ReqInternet.in().doGet(StringManager.api_topCustomer + "?cid=" + mPlateData.getCid() + "&mid=" + mPlateData.getMid(), new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object msg) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        mRecUserData = StringManager.getListMapByJson(msg);
                        mCircleHeaderView.initRecUser(mRecUserData);
                    }
                }
            });
        }
    }

    private List<Map<String, String>> recCustomerArray = new ArrayList<>();

    private void getSpareUser(final boolean isRefresh) {
        if (isRefresh) {
            recCustomerArray.clear();
            mAdapter.clearRecCutomerArray();
        }
        ReqInternet.in().doGet(StringManager.api_recCustomer + "?cid=" + mPlateData.getCid() + "&mid=" + mPlateData.getMid(),
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object msg) {
                        if (flag >= ReqInternet.REQ_OK_STRING) {
                            recCustomerArray = StringManager.getListMapByJson(msg);
                            handlerCutomersData(recCustomerArray);
                            mAdapter.setmRecCutomerArray(recCustomerArray);
//                            mAdapter.notifyDataSetChanged();
                            safeNotifyItemRangeChanged();
                        }
                    }
                });
    }

    private void handlerCutomersData(List<Map<String, String>> recCustomers) {
        if (recCustomers.size() == 0 || recCustomers.size() > 100) {
            return;
        }
        for (int index = 0; index < recCustomers.size(); index++) {
            Map<String, String> customer = recCustomers.get(index);
            if ("2".equals(customer.get("folState"))) {
                continue;
            } else {
                recCustomers.remove(customer);
                index--;
            }
        }
    }

    private void registnetworkListener() {
        connectionChangeReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {
                isAutoPaly = false;
            }

            @Override
            public void wifi() {
                isAutoPaly = true;
            }

            @Override
            public void mobile() {
                isAutoPaly = false;
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(connectionChangeReceiver, filter);
    }

    public void unregistnetworkListener() {
        if (connectionChangeReceiver != null) {
            getContext().unregisterReceiver(connectionChangeReceiver);
        }
    }

    public void refresh() {
        if (refreshLayout != null) {
            refreshLayout.autoRefresh();
        }
    }

    public void returnListTop() {
        if (mListView != null) {
            mListView.scrollToPosition(0);
        }
    }

    /**
     * 设置当前页面加载页面位置
     */
    public void setQuanmCurrentPage() {

    }

    public PlateData getmPlateData() {
        return mPlateData;
    }

    public void setmPlateData(PlateData mPlateData) {
        this.mPlateData = mPlateData;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getIsLocation() {
        return isLocation;
    }

    public void setIsLocation(String isLocation) {
        this.isLocation = isLocation;
    }

    /**
     * 刷新广告数据
     */
    public void refreshAdData() {
        if(quanAdvertControl==null)return;
        boolean state= quanAdvertControl.isNeedRefresh();
        if(state){
            //删除集合中的广告，
            int size= mListData.size();
            ArrayList<Map<String,String>> listTemp = new ArrayList<>();
            for(int i=0;i<size;i++){
                if(mListData.get(i).containsKey("isPromotion")&&"1".equals(mListData.get(i).get("isPromotion"))){
                    listTemp.add(mListData.get(i));
                }
            }
            if(listTemp.size()>0){
                mListData.removeAll(listTemp);
            }
        }

    }

}
