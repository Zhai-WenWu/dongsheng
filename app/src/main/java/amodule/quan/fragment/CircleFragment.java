package amodule.quan.fragment;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.AppCommon;
import acore.logic.load.AutoLoadMore;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.activity.CircleHome;
import amodule.quan.adapter.AdapterCircle;
import amodule.quan.db.PlateData;
import amodule.quan.db.SubjectData;
import amodule.quan.tool.QuanAdvertControl;
import amodule.quan.view.CircleHeaderView;
import amodule.quan.view.CircleHeaderView.ItemCallback;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;
import amodule.quan.view.VideoImageView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * 用于不同的刷新Fragment
 * 自己有另外的刷新逻辑，框架的刷新需要禁用
 *
 * @author Eva
 */
public class CircleFragment extends Fragment {
    /** 保存板块信息的key */
    protected static final String PLATEDATA = "plate_data";
    public static final String CIRCLENAME = "circle_name";
    /** 依附的Activity */
    private BaseAppCompatActivity mActivity;
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
    private ListView mListview;
    /** Fragment 的 root view */
    private View mView;

    /** 当前page */
    private int mCurrentPage = 0;
    /** 每页的数据数量 */
    private int mEveryPageNum = 0;
    /** 贴子的数据集合 */
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();
    /** 置顶和公告的数据集合 */
    private ArrayList<Map<String, String>> mRobRoNoticeData = new ArrayList<>();
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
    private AdapterCircle mAdapter;
    String mPageTime = "";
    private String noDataNotice_1 = "", noDataNotice_2 = "", noDataUrl = "";
    private int index_size = 0;

    private ConnectionChangeReceiver connectionChangeReceiver;
    private boolean isAutoPaly = false;

    public CircleFragment() {
        super();
    }
    private boolean isLoadAd= true;//是否加载广告
    private QuanAdvertControl quanAdvertControl;
    //对视频的处理
    private LinearLayout video_layout;
    private VideoImageView videoImageView;
    private int headerCount=0;//存在listview头数据

    public static CircleFragment newInstance(PlateData plateData) {
        CircleFragment fragment = new CircleFragment();
        fragment.setPosition(plateData.getPosition());
        fragment.setIsLocation(plateData.isLocation() ? PlateData.LOCATION : PlateData.UNLOCATION);
        fragment.setmPlateData(plateData);
        return (CircleFragment) setArgumentsToFragment(fragment, plateData);
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
        mActivity = (BaseAppCompatActivity) activity;
        super.onAttach(activity);
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        registnetworkListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistnetworkListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.circle_fragment_list, null);
        mCircleHeaderView = new CircleHeaderView(mActivity);
        //设置头部发贴失败布局点击事件
        mCircleHeaderView.setFakeDelCallback(new ItemCallback() {
            @Override
            public void onClick(String content) {
                //remove成功的假数据
                if (mActivity != null && mActivity instanceof CircleHome) {
                    CircleHome circleHome = (CircleHome) mActivity;
                    circleHome.removeFailedSubjec(Integer.parseInt(content));
                }
            }
        });
        refreshLayout = (PtrClassicFrameLayout) mView.findViewById(R.id.refresh_list_view_frame);
        mListview = (ListView) mView.findViewById(R.id.v_scroll);
        mListview.addHeaderView(mCircleHeaderView);
        headerCount++;
        mLoadManager = mActivity.loadManager;
        LoadOver = false;
        isPrepared = true;
        preLoad();

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //处理发贴假界面
        if (mActivity instanceof CircleHome) {
            CircleHome circleHome = (CircleHome) mActivity;
            updateCircleHeader(circleHome.mSubjectDataArray);
        }
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
        if (mLoadManager != null && mListview != null) {
            Button loadMore = mLoadManager.getSingleLoadMore(mListview);
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
        mAdapter = new AdapterCircle(mActivity, mListview, mListData);
        mAdapter.setModuleName((mPlateData!=null&&!TextUtils.isEmpty(mPlateData.getName()))?mPlateData.getName():"");
        mAdapter.setCircleName(mCircleName);
        if (!LoadOver) {
            //设置加载，并传入PlaceHoderHeaderLayout设置滑动加载
            mLoadManager.setLoading(refreshLayout, mListview, mAdapter, true,
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getData(true);
                            isLoadAd=true;
                            quanAdvertControl.getAdData(mActivity);
                        }
                    }, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getData(!LoadOver);
                        }
                    },new AutoLoadMore.OnListScrollListener() {
                        final int topRedundant = Tools.getDimen(getContext(),R.dimen.dp_45) + Tools.getStatusBarHeight(getContext());
                        final int bottomRedundant = Tools.getDimen(getContext(),R.dimen.dp_50);
                        final int Min = topRedundant;
                        final int Max = (ToolsDevice.getWindowPx(getContext()).heightPixels  - topRedundant - bottomRedundant) *4 / 5 + topRedundant;
                        int currentPlayPosition = -1;

                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                            if(!isAutoPaly){
                                return;
                            }
                            final int length = view.getChildCount();
                            if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                                int count = 0;
                                int index = 0;
                                for(; index < length ; index ++){
                                    View itemView = view.getChildAt(index);
                                    int top = itemView.getTop();
                                    int height = itemView.getHeight();
                                    final int value = height * 4 / 7 + top;
                                    if(itemView instanceof NormalContentView){
                                        if(value <= Max && value >= Min){
//                                            ((NormalContentView)itemView).startVideoView();
                                            currentPlayPosition = view.getPositionForView(itemView);
                                            Log.i("zhangyujian","自动数据的位置:::"+((NormalContentView)itemView).getPositionNow());
                                            setVideoLayout(itemView,((NormalContentView)itemView).getPositionNow());
                                        }else{
                                            count++;
//                                            ((NormalContentView)itemView).stopVideoView();
                                        }
                                    }
                                }
                                if(count == index){
                                    mAdapter.setCurrentPlayPosition(-1);
                                }
                            }
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        }
                    });
            LoadOver = true;
        }
        quanAdvertControl= new QuanAdvertControl(mActivity);
        quanAdvertControl.getAdData(mActivity);
        quanAdvertControl.setCallBack(new QuanAdvertControl.DataCallBack() {
            @Override
            public void dataBack() {
                if(isLoadAd){
                    index_size=0;
                    if (mPlateData != null)
                        mListData =quanAdvertControl.getAdvertAndQuanData(mListData, mPlateData.getCid(), mPlateData.getMid(), index_size);
                    mAdapter.notifyDataSetChanged();
                    index_size=mListData.size();
                }else{
                }
            }
        });
        mAdapter.setQuanAdvertControl(quanAdvertControl);
        //视频被点击事件
        mAdapter.setVideoClickCallBack(new NormarlContentItemImageVideoView.VideoClickCallBack() {
            @Override
            public void videoImageOnClick(int position) {
                int firstVisiPosi = mListview.getFirstVisiblePosition();
                //要获得listview的第n个View,则需要n减去第一个可见View的位置。+1是因为有header
                View parentView = mListview.getChildAt(position-firstVisiPosi + headerCount);
                setVideoLayout(parentView,position);
            }
        });
    }
    /**
     * 处理view,video
     * @param parentView
     * @param position
     */
    private void setVideoLayout(View parentView, final int position){
        if(mListData.get(position).containsKey("selfVideo") && !TextUtils.isEmpty(mListData.get(position).get("selfVideo"))) {
            Map<String, String> videoData = StringManager.getFirstMap(mListData.get(position).get("selfVideo"));
            if(videoImageView==null)
                videoImageView = new VideoImageView(mActivity);
            videoImageView.setImageBg(videoData.get("sImgUrl"));
            videoImageView.setVideoData(videoData.get("videoUrl"));
            videoImageView.setVisibility(View.VISIBLE);
            if (video_layout != null && video_layout.getChildCount() > 0) {
                video_layout.removeAllViews();
            }

            video_layout = (LinearLayout) parentView.findViewById(R.id.video_layout);
            video_layout.addView(videoImageView);
            videoImageView.onBegin();
            videoImageView.setVideoClickCallBack(new VideoImageView.VideoClickCallBack() {
                @Override
                public void setVideoClick() {
                    stopVideo();
                    goNextActivity(position);
                }
            });
        }
    }

    /**
     * 暂停播放
     */
    private void stopVideo(){
        if(videoImageView!=null){
            videoImageView.onVideoPause();
            videoImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 详情页
     */
    private void goNextActivity(int position){
        String isSafa="";
        Map<String,String> map = mListData.get(position);
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
        }
        mCurrentPage++;
        if(mPlateData == null)
            return;
        String url = StringManager.api_circleSubjectList + "?cid=" + mPlateData.getCid() + "&mid=" + mPlateData.getMid();
        String param = "&page=" + mCurrentPage + "&pageTime=" + mPageTime;
        //更新加载按钮状态
        mLoadManager.changeMoreBtn(mListview, ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, isRefresh);
        if (isRefresh) {
            mLoadManager.hideProgressBar();
        }
        ReqInternet.in().doGet(url + param, new InternetCallback(mActivity.getApplication()) {
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
                        String dataType = returnData.get(0).get("dataType");
                        returnData = StringManager.getListMapByJson(returnData.get(0).get("data"));
                        //判断列表数据类型-----------------1：贴子 2：用户
                        if ("2".equals(dataType)) {
                            for (int index = 0, length = returnData.size(); index < length; index++) {
                                Map<String, String> map = returnData.get(index);
                                String upInfo = map.get("upInfo");//upInfo	更新说明
                                mListview.setSelection(0);
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
                                    if (style.equals("2")
                                            || style.equals("3")
                                            || style.equals("4")) {
                                        mRobRoNoticeData.add(map);
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
                int size= mListData.size();
                mListData =quanAdvertControl.getAdvertAndQuanData(mListData, mPlateData.getCid(), mPlateData.getMid(), index_size);
                if(size<mListData.size())isLoadAd=false;
                index_size = mListData.size();
                if (mEveryPageNum == 0) {
                    mEveryPageNum = loadCount;
                }
                //刷新数据
                //添加此判断，是因为此处有adapter的 NullPointerException
                //不为null则刷新，为null则尝试从listview.getAdapter()获取adapter并刷新数据
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                } else if (mListview != null && mListview.getAdapter() != null) {
                    mAdapter = (AdapterCircle) mListview.getAdapter();
                    mAdapter.notifyDataSetChanged();
                }
                mCurrentPage = mLoadManager.changeMoreBtn(mListview, flag, LoadManager.FOOTTIME_PAGE, loadCount, mCurrentPage, isRefresh);
                //判断是否刷新
                if (isRefresh) {
                    refreshComplate();
                    //添加置顶和公告data
                    mCircleHeaderView.initMiddleView(mRobRoNoticeData);
                    refreshLayout.refreshComplete();
                }
                //如果没有数据显示提示
                if (mListData.size() == 0) {
                    mCircleHeaderView.showNoDataView(noDataNotice_1, noDataNotice_2, new ItemCallback() {
                        @Override
                        public void onClick(String content) {
                            if (!TextUtils.isEmpty(noDataUrl)) {
                                AppCommon.openUrl(mActivity, noDataUrl, true);
                            }
                        }
                    });
                } else {
                    mCircleHeaderView.hideNoDataView();
                }
            }
        });
    }

    private void registnetworkListener(){
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
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(connectionChangeReceiver,filter);
    }

    public void unregistnetworkListener(){
        if(connectionChangeReceiver != null){
            getContext().unregisterReceiver(connectionChangeReceiver);
        }
    }

    public void refresh() {
        refreshLayout.autoRefresh();
    }

    public void returnListTop() {
        if (mListview != null) {
            mListview.setSelection(0);
        }
    }

    /** 刷新完成 */
    private void refreshComplate() {
        //remove成功的假数据
        if (mActivity instanceof CircleHome) {
            CircleHome circleHome = (CircleHome) mActivity;
            circleHome.removeAllSuccessSubject(mPlateData.getMid());
            updateCircleHeader(circleHome.mSubjectDataArray);
        }
    }

    /** 刷新header的数据，更新UI */
    public void updateCircleHeader(ArrayList<SubjectData> dataArray) {
        ArrayList<SubjectData> mSubjectFake = new ArrayList<>();
        ArrayList<SubjectData> mSubjectFailed = new ArrayList<>();
        final int length = dataArray.size();
        for (int index = 0; index < length; index++) {
            SubjectData subjectData = dataArray.get(index);
            Log.i("shortVideo","subjectData:::subjectData:"+subjectData.getVideoLocalPath());
            String mid = subjectData.getMid();
            if ((TextUtils.isEmpty(mid) && position == 0)
                    || mid.equals(mPlateData.getMid())) {
                if (SubjectData.UPLOAD_ING == subjectData.getUploadState()
                        || SubjectData.UPLOAD_SUCCESS == subjectData.getUploadState()) {
                    mSubjectFake.add(subjectData);
                } else if (SubjectData.UPLOAD_FAIL == subjectData.getUploadState()) {
                    mSubjectFailed.add(subjectData);
                }
            }
        }
        //调用circleHeader更新数据
        mCircleHeaderView.initFakeContentView(mSubjectFake, mSubjectFailed);
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

    @Override
    public void onPause() {
        super.onPause();
        stopVideo();
    }
}