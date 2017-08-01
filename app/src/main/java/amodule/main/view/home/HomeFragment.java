package amodule.main.view.home;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.load.AutoLoadMore;
import acore.logic.load.LoadManager;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.activity.MainHome;
import amodule.main.adapter.AdapterHome;
import amodule.main.adapter.AdapterListView;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.HomeTabHScrollView;
import amodule.main.view.ReplayAndShareView;
import amodule.main.view.item.HomeAlbumItem;
import amodule.main.view.item.HomeAnyImgStyleItem;
import amodule.main.view.item.HomeItem;
import amodule.main.view.item.HomePostItem;
import amodule.main.view.item.HomeRecipeItem;
import amodule.main.view.item.HomeTxtItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import cn.fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import third.ad.control.AdControlHomeDish;
import third.ad.control.AdControlNormalDish;
import third.ad.control.AdControlParent;
import third.ad.option.AdOptionParent;
import third.ad.tools.AdPlayIdConfig;
import third.share.BarShare;
import third.video.SimpleVideoPlayerController;
import third.video.VideoPlayerController;

import static amodule.main.activity.MainHome.tag;
import static com.xiangha.R.id.return_top;
import static third.ad.control.AdControlHomeDish.tag_yu;

public class HomeFragment extends BaseHomeFragment{

    public static String MODULETOPTYPE="moduleTopType";//置顶数据的类型

    private HomeModuleBean homeModuleBean;//数据的结构
    private LoadManager mLoadManager = null;
    private MainBaseActivity mActivity;
    private View mView;
    private PtrClassicFrameLayout refreshLayout;
    private ListView mListview;
    private ImageView returnTop;
    private View homeHeaderDataNum;//数据条数view
    private TextView show_num_tv;

    private boolean isAutoPaly = false;//是否是wifi状态
    /** 是否加载完成 */
    private boolean LoadOver = false;
    /** 是否初始化 */
    protected boolean isPrepared = false;
    /** 是否显示 */
    protected boolean isVisible;
    /** 当前在viewpager中位置 */
    private int position=-1;
    /** 当前二级内容中位置 */
    private int twoPosition=-1;
    /** 贴子的数据集合 */
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();

    private String backUrl= "";//向上拉取数据集合
    private String nextUrl="";//下页拉取数据集合
//    private AdapterHome adapterHome;
    private AdapterListView adapterListView;
    private boolean isloadTwodata=false;//是否加载过二级数据
    private LinearLayout layout,linearLayoutOne,linearLayoutTwo,linearLayoutThree;//头部view
    private String reset_time="";//向上刷新的时间戳
    private String backUrlBefore="";//之前的数据体---目前只有推荐使用了

    private AdControlParent mAdControl;
    private int beforNum = 0;
    private boolean isRecoment = false,isDayDish = false,isSetIndex = false;
    private static final Integer[] AD_INSTERT_INDEX = new Integer[]{3,9,16,24,32,40,48,56,64,72};

    private RelativeLayout mVideoLayout;
    private ReplayAndShareView mReplayAndShareView;

    private String statisticKey = null;
    private int mHeaderCount;
    /**正在播放的位置，默认-1，即没有正在播放的*/
    private int mPlayPosition = -1;
    private View mPlayParentView = null;
    private boolean compelClearData= false;//强制清楚数据
    private boolean isScrollData= false;//是否滚动数据
    private int scrollDataIndex=-1;//滚动数据的位置
    private boolean isRecom=false;//是否是推荐
    private long statrTime= -1;//开始的时间戳
    private boolean isNextUrl=true;//执行数据有问题时，数据请求，只执行一次。
    private int upDataSize = 0;//向上刷新数据集合大小

    public static HomeFragment newInstance(HomeModuleBean moduleBean) {
        HomeFragment fragment = new HomeFragment();
        fragment.setPosition(moduleBean.getPosition());
        fragment.setmoduleBean(moduleBean);
        return (HomeFragment) setArgumentsToFragment(fragment, moduleBean);
    }
    /** 将储块信息存板到Argument中 */
    public static Fragment setArgumentsToFragment(Fragment fragment, HomeModuleBean moduleBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MODULEDATA, moduleBean);
        fragment.setArguments(bundle);
        return fragment;
    }
    public HomeModuleBean getCurrentModuleData() {
        HomeModuleBean moduleBean = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            moduleBean = (HomeModuleBean) bundle.getSerializable(MODULEDATA);
        }
        return moduleBean;
    }
    @Override
    public void onAttach(Activity activity) {
        mActivity = (MainBaseActivity) activity;
        super.onAttach(activity);
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取数据
        homeModuleBean=getCurrentModuleData();
        mAdControl = getAdControl();
    }

    public AdControlParent getAdControl(){
        String type = homeModuleBean.getType(); //当前页的type
        Log.i("FRJ","type:" + type);
        if(TextUtils.isEmpty(type)){
            return null;
        }
        if(MainHome.recommedType.equals(type)){ //推荐
            isRecoment = true;
            return AdControlHomeDish.getInstance().getTwoLoadAdData();
        }else{
            AdOptionParent adControlParent = null;
            String[] adPlayIds = new String[0];
            Integer[] adIndexs = AD_INSTERT_INDEX;
            boolean isSetAd = true;
            if("video".equals(type)){ //视频
                statisticKey = "sp_list";
                adPlayIds = AdPlayIdConfig.MAIN_HOME_VIDEO_LIST;
            }else if("article".equals(type)){ //涨知识
                statisticKey = "other_top_list";
                adPlayIds = AdPlayIdConfig.MAIN_HOME_ZHISHI_LIST;
            }else if("day".equals(type)){ //每日三餐
                statisticKey = "sc_list";
                isDayDish = true;
                adPlayIds = AdPlayIdConfig.COMMEND_THREE_MEALS;
                adIndexs = new Integer[]{};
            }else if("dish".equals(type)){ //本周佳作
                statisticKey = "jz_list";
                adPlayIds = AdPlayIdConfig.MAIN_HOME_WEEK_GOOD_LIST;
            }else{
                isSetAd = false;
            }
            if(isSetAd && adControlParent == null){

                return new AdControlNormalDish(statisticKey,adPlayIds);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.home_fragment, null);
        //header布局
        initHeaderView();
        refreshLayout = (PtrClassicFrameLayout) mView.findViewById(R.id.refresh_list_view_frame);
        //解决横滑冲突
        refreshLayout.disableWhenHorizontalMove(true);
        homeHeaderDataNum=mView.findViewById(R.id.homeHeaderNum);
        show_num_tv= (TextView) mView.findViewById(R.id.show_num_tv);
        homeHeaderDataNum.setVisibility(View.GONE);
        mListview = (ListView) mView.findViewById(R.id.v_scroll);
        mListview.addHeaderView(layout);
        mHeaderCount++;
        returnTop = (ImageView) mView.findViewById(return_top);
        mLoadManager = mActivity.loadManager;
        LoadOver = false;
        isPrepared = true;
        preLoad();
        return mView;
    }

    /**
     * 初始化header布局
     */
    private void initHeaderView(){
        //initHeaderView
        layout= new LinearLayout(mActivity);
        layout.setOrientation(LinearLayout.VERTICAL);
        linearLayoutOne= new LinearLayout(mActivity);
        linearLayoutOne.setOrientation(LinearLayout.VERTICAL);
        linearLayoutTwo= new LinearLayout(mActivity);
        linearLayoutTwo.setOrientation(LinearLayout.VERTICAL);
        linearLayoutThree= new LinearLayout(mActivity);
        linearLayoutThree.setOrientation(LinearLayout.VERTICAL);
        linearLayoutOne.setVisibility(View.GONE);
        linearLayoutTwo.setVisibility(View.GONE);
        linearLayoutThree.setVisibility(View.GONE);
        layout.addView(linearLayoutOne);
        layout.addView(linearLayoutTwo);
        layout.addView(linearLayoutThree);
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
            if( mListData.size() == 0){
                mLoadManager.showProgressBar();
            }else mLoadManager.hideProgressBar();
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
            statrTime=System.currentTimeMillis();//第一次设置开始时间戳
            initData();
        }
    }


    /**
     * 初始化数据
     */
    private void initData() {
        adapterListView = new AdapterListView(mListview,mActivity,mListData,mAdControl);
        adapterListView.setHomeModuleBean(homeModuleBean);
        adapterListView.setViewOnClickCallBack(new AdapterHome.ViewClickCallBack() {
            @Override
            public void viewOnClick(boolean isOnClick) {
                refresh();
            }
        });
        adapterListView.setVideoClickCallBack(new HomeRecipeItem.VideoClickCallBack() {
            @Override
            public void videoOnClick(int position) {
                int firstVisiPosi = mListview.getFirstVisiblePosition();
                View parentView = mListview.getChildAt(position-firstVisiPosi + mHeaderCount);
                setVideoLayout(parentView,position);
            }
        });
        if(!LoadOver){
                mLoadManager.setLoading(refreshLayout, mListview, adapterListView, true, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if(!TextUtils.isEmpty(statisticKey)){
//                            mAdControl.getAdData(mActivity,statisticKey);
//                        }
                        EntryptData(true);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EntryptData(!LoadOver);
                    }
                }, new AutoLoadMore.OnListScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }
                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        isScrollData=true;
                        if(scrollDataIndex<(firstVisibleItem+visibleItemCount-1)) {
                            scrollDataIndex = (firstVisibleItem + visibleItemCount-1);
                        }
                        if (mPlayPosition != -1) {
                            //正在播放的视频滑出屏幕
                            if ((mPlayPosition + mHeaderCount) < firstVisibleItem || (mPlayPosition + mHeaderCount) > (firstVisibleItem + visibleItemCount - 1)) {
                                stopVideo();
                            }
                        }
                    }
                });
        }
        //处理推荐的置顶数据
        if(MainHome.recommedType.equals(homeModuleBean.getType())) {
            String url = StringManager.API_RECOMMEND_TOP;
            ReqEncyptInternet.in().doEncyptAEC(url, "", new InternetCallback(mActivity) {
                @Override
                public void loaded(int flag, String url, Object object) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        handlerTopData(object);
                    }
                }
            });
        }

    }

    /**
     * 请求数据入口
     * @param refresh，是否刷新
     */
    private void EntryptData(final boolean refresh){
        if(refresh){
            isNeedRefresh(false);
        }
        String params="";
        LoadOver = true;
//        mLoadManager.showProgressBar();
        if(refresh){//向上翻页
            if(!isRecoment && mAdControl != null) mAdControl.refrush();
            setStatisticShowNum();
            if(!TextUtils.isEmpty(backUrl)){
                params=backUrl;
            }else{
                if(isRecom()&&!TextUtils.isEmpty((String) FileManager.loadShared(mActivity,homeModuleBean.getType(),"backUrl"))){
                    params=(String) FileManager.loadShared(mActivity,homeModuleBean.getType(),"backUrl");
                }else{
                    params= "type="+homeModuleBean.getType();
                    if(!TextUtils.isEmpty(homeModuleBean.getTwoType()))params+="&two_type="+homeModuleBean.getTwoType();
                }
            }
        }else{//向下翻页
            if(!TextUtils.isEmpty(nextUrl)){
               params=nextUrl;
            }else{
                params= "type="+homeModuleBean.getType();
                if(!TextUtils.isEmpty(homeModuleBean.getTwoType()))params+="&two_type="+homeModuleBean.getTwoType();
            }
        }
        loadData(refresh,params);
    }
    /**
     * 获取数据
     */
    private void loadData(final boolean refresh, String data){
        Log.i("zhangyujian","refresh::"+refresh+"::data:"+data);
        if (homeModuleBean != null && MainHome.recommedType.equals(homeModuleBean.getType()) && refresh)
            XHClick.mapStat(mActivity, "a_recommend", "刷新效果", "下拉刷新");
        linearLayoutOne.removeAllViews();
        String url= StringManager.API_RECOMMEND;

        //更新加载按钮状态
        mLoadManager.changeMoreBtn(mListview, ReqInternet.REQ_OK_STRING, -1, -1, LoadOver?2:1, refresh);
        LoadOver = true;
        if (refresh) {
            if(isRecom()) {
                mLoadManager.hideProgressBar();
                returnListTop();
            }else{
                if(TextUtils.isEmpty(backUrl)&&mListData.size()<=0)mLoadManager.showProgressBar();
                else  mLoadManager.hideProgressBar();
            }
        }
        ReqEncyptInternet.in().doEncyptAEC(url,data, new InternetCallback(mActivity) {
            @Override
            public void loaded(int flag, String url, Object object) {
                int loadCount = 0;
                if(flag>=ReqInternet.REQ_OK_STRING){
                    Log.i("FRJ","获取  服务端   数据回来了-------------");
                    boolean isRecom=false;//是否是推荐
                    //只处理推荐列表
                    if(homeModuleBean.getType().equals(MainHome.recommedType))isRecom=true;

                    ArrayList<Map<String,String>> listmaps= StringManager.getListMapByJson(object);
                    //当前数据有问题，直接return数据
                    if(!(listmaps==null||listmaps.size()<=0||!listmaps.get(0).containsKey("list")
                            ||StringManager.getListMapByJson(listmaps.get(0).get("list"))==null
                            ||StringManager.getListMapByJson(listmaps.get(0).get("list")).size()<=0)){
                        //存储当前backurl，
                        if (!TextUtils.isEmpty(backUrl) && refresh && isRecom) {
                            FileManager.saveShared(mActivity, homeModuleBean.getType(), "backUrl", backUrl);
                        }
                        //上拉数据，下拉数据
                        if (TextUtils.isEmpty(backUrl) || (!TextUtils.isEmpty(listmaps.get(0).get("backurl")) && refresh))
                            backUrl = listmaps.get(0).get("backurl");
                        if (TextUtils.isEmpty(nextUrl) || !TextUtils.isEmpty(listmaps.get(0).get("nexturl")) && !refresh)
                            nextUrl = listmaps.get(0).get("nexturl");
                        //当前只有向上刷新，并且服务端确认可以刷新数据
                        if (compelClearData || (refresh && !TextUtils.isEmpty(listmaps.get(0).get("reset")) && "2".equals(listmaps.get(0).get("reset")))) {
                            mListData.clear();
                            Log.i("wyj","刷新数据：清集合");
                            isNeedRefresh(true);
                            //强制刷新，重置数据
                            if(!TextUtils.isEmpty(listmaps.get(0).get("backurl")))
                                backUrl = listmaps.get(0).get("backurl");
                            if(!TextUtils.isEmpty(listmaps.get(0).get("nexturl")))
                                nextUrl = listmaps.get(0).get("nexturl");

                        }
                        //初始化二级
                        initContextView(listmaps.get(0).get("trigger_two_type"));
                        if (listmaps != null && listmaps.size() > 0) {
                            ArrayList<Map<String, String>> listDatas = StringManager.getListMapByJson(listmaps.get(0).get("list"));
                            if (listDatas != null && listDatas.size() > 0) {
                                loadCount=listDatas.size();
                                if (refresh && isRecom) {
                                    int size = listDatas.size();
                                    //创建数据条数header
                                    if(mListData.size()>0)
                                        handlerHeaderView(size);
                                }
                                int oldDayDishIndex = -1;
                                if (refresh && mListData.size() > 0) {
                                    //如果需要加广告，插入广告
                                    if (mAdControl != null) {
                                        //插入广告
                                        Log.i(tag_yu,"listDatas::111:"+listDatas.size());
                                        listDatas = mAdControl.getNewAdData(listDatas, true);

                                        upDataSize+=listDatas.size();
                                    }
                                    mListData.addAll(0, listDatas);//插入到第一个位置
                                } else {
                                    //查询往期推荐的index：如果当前是每日推荐，并且还未给AdControl设置过加入的位置，则查询往期推荐的index，广告插到此条上面
//                                    if (isDayDish && !isSetIndex) {
//                                        int index = 0;
//                                        for (Map<String, String> map : listDatas) {
//                                            if (!TextUtils.isEmpty(map.get("pastRecommed"))) {
//                                                oldDayDishIndex = index;
//                                                break;
//                                            }
//                                            index++;
//                                        }
//                                        //如果当前是每日推荐，并且还未给AdControl设置过加入的位置，则设置
//                                        if (oldDayDishIndex > 0 && mAdControl != null) {
//                                            isSetIndex = true;
////                                            mAdControl.setIndexs(new Integer[]{oldDayDishIndex});
//                                        }
//                                    }
                                    mListData.addAll(listDatas);//顺序插入
                                    //如果需要加广告，插入广告
                                    if (mAdControl != null) {
                                        //插入广告
                                        Log.i(tag_yu,"mListData::222:"+mListData.size()+"::"+upDataSize);
                                        if(upDataSize>0 && isRecom)
                                            mAdControl.setLimitNum(upDataSize);
                                        mListData = mAdControl.getNewAdData(mListData, false);
                                    }
                                }
                            }
                        }
                        //读取历史记录
                        if (isRecom) {//是首页并且刷新
                            String historyUrl = (String) FileManager.loadShared(mActivity, homeModuleBean.getType(), homeModuleBean.getType());
                            if (!TextUtils.isEmpty(historyUrl)) {
                                Map<String, String> map = StringManager.getMapByString(historyUrl, "&", "=");
                                //设置显示参数
                                int size = mListData.size();
                                for (int i = 0; i < size; i++) {
                                    if (i != 0 && mListData.get(i).containsKey("mark") && mListData.get(i).get("mark").equals(map.get("mark"))) {
                                        String time = map.get("reset_time");
                                        Log.i("zhangyujian", "mak:" + mListData.get(i).get("mark") + "::;" + mListData.get(i).get("name"));
                                        Map<String, String> backMap = StringManager.getMapByString(backUrl, "&", "=");
                                        String nowTime = backMap.get("reset_time");
                                        mListData.get(i).put("refreshTime", Tools.getTimeDifferent(Long.parseLong(nowTime), Long.parseLong(time)));
                                    } else {
                                        mListData.get(i).put("refreshTime", "");
                                    }
                                }
                            }
                        }
//                    beforNum += listmaps.size();

                        //只有推荐，刷新数据才进行保存历史记录，第一次刷新不存储，下一次存储上次的数据，
                        if (isRecom && refresh) {
                            backUrlBefore = backUrl;
                            if (!TextUtils.isEmpty(backUrlBefore)) {
                                FileManager.saveShared(mActivity, homeModuleBean.getType(), homeModuleBean.getType(), backUrlBefore);
                            }
                        }
                        adapterListView.notifyDataSetChanged();
                        //自动请求下一页数据
                        if(isRecom&&mListData.size()<=4){//推荐列表：低于等5的数据自动请求数据
                            Log.i("zhangyujian","自动下次请求:::"+mListData.size());
                            EntryptData(false);
                        }
                    }else if(isRecom){//置状态---刷新按钮
                        int size = mListData.size();
                        if(size>0){//推荐列表:有数据置状态
                            for (int i = 0; i < size; i++) {
                                mListData.get(i).put("refreshTime", "");
                            }
                            adapterListView.notifyDataSetChanged();
                        }else{//无数据时---请求下一页数据
                            if(listmaps!=null&&listmaps.size()>0&&listmaps.get(0).containsKey("nexturl")&&isNextUrl){
                                nextUrl = listmaps.get(0).get("nexturl");
                                isNextUrl=false;
                                EntryptData(false);
                            }
                        }

                    }
                }
                mLoadManager.hideProgressBar();
                mLoadManager.changeMoreBtn(mListview, flag, LoadManager.FOOTTIME_PAGE, refresh?mListData.size():loadCount, 0, refresh);
                if(refresh){
                    refreshLayout.refreshComplete();
                }
                compelClearData=false;//强制刷新只能使用一次，一次数据后被置回去
            }
        });
    }

    /**
     * 初始化二级内容视图
     * @param type 选中的类型
     */
    private void initContextView(String type){

        if(!isloadTwodata&&!TextUtils.isEmpty(homeModuleBean.getTwoData())&&!TextUtils.isEmpty(type)){
            linearLayoutTwo.removeAllViews();
            isloadTwodata=true;
            HomeTabHScrollView homeTabHScrollView = new HomeTabHScrollView(mActivity);
            //处理二级数据体
            ArrayList<Map<String,String>> listMaps= StringManager.getListMapByJson(homeModuleBean.getTwoData());
            if(listMaps!=null&&listMaps.size()>0){
                homeModuleBean.setTwoType(type);
            }else return;
            for(int i=0;i<listMaps.size();i++){
                listMaps.get(i).put("position",String.valueOf(i));
            }
            homeTabHScrollView.setHomeModuleBean(homeModuleBean);
            homeTabHScrollView.setData(listMaps);
            homeTabHScrollView.setCallback(new HomeTabHScrollView.HomeDataChangeCallBack() {
                @Override
                public void indexChanged(Map<String, String> map) {
                    stopVideo();
                    if(map.get("two_type").equals(homeModuleBean.getTwoType())){
                        //是否刷新操作
                    }else{
                        homeModuleBean.setTwoType(map.get("two_type"));
                        homeModuleBean.setTwoTitle(map.get("title"));
                        homeModuleBean.setTwoTypeIndex(Integer.parseInt(map.get("position")));
                        adapterListView.setHomeModuleBean(homeModuleBean);
                        compelClearData=true;
                        //请求数据
                        backUrl="";
                        nextUrl="";
                        EntryptData(true);
//                        refresh();
                    }
                }
            });
            linearLayoutTwo.addView(homeTabHScrollView);
            linearLayoutTwo.setVisibility(View.VISIBLE);
            homeTabHScrollView.setVisibility(View.VISIBLE);
        }

    }
    /**
     * 设置刷新
     */
    private void setRefresh(){
    }

    @Override
    public void onResume() {
        super.onResume();
        if(statrTime<=0&& isRecom()){
            statrTime=System.currentTimeMillis();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopVideo();
    }

    public HomeModuleBean getmoduleBean() {
        return homeModuleBean;
    }

    public void setmoduleBean(HomeModuleBean mPlateData) {
        this.homeModuleBean = mPlateData;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    /**
     * 刷新
     */
    public void refresh() {
        Log.i(tag,"HomeFragment refresh:::"+homeModuleBean.getPosition());
        if (refreshLayout != null) {
            refreshLayout.autoRefresh();
        }
    }
    /**
     * 滚动到顶部
     */
    public void returnListTop() {
        if (mListview != null) {
            mListview.setSelection(0);
        }
    }
    /**
     * 设置二次参数数据
     * @param twoTitle
     * @param twoType
     */
    public void setTwoModuledata(String twoTitle,String twoType){
        if(TextUtils.isEmpty(twoTitle))homeModuleBean.setTwoTitle(twoTitle);
        if(TextUtils.isEmpty(twoType))homeModuleBean.setTwoType(twoType);
    }
    private void handlerTopDatas(ArrayList<Map<String,String>> listmaps){
        if(listmaps!=null&&listmaps.size()>0){
            int size= listmaps.size();
            for(int i=0;i<size;i++){
                Map<String,String> map = listmaps.get(i);
                //进行类型区分，判断数据
            }
        }
    }

    private SimpleVideoPlayerController mPlayerController;

    /**
     * 处理view,video
     * @param parentView
     * @param position
     */
    private void setVideoLayout(final View parentView, final int position){
        if (parentView == null || position < 0 || position >= mListData.size())
            return;
        if(mListData.get(position).containsKey("video") && !TextUtils.isEmpty(mListData.get(position).get("video"))) {
            Map<String, String> dataMap = mListData.get(position);
            if (dataMap == null || dataMap.size() <= 0)
                return;
            if (mVideoLayout != null && mVideoLayout.getChildCount() > 0) {
                mVideoLayout.removeAllViews();
            }
            if (mPlayerController != null) {
                mPlayerController.removePlayingCompletionListener();
                mPlayerController.onDestroy();
            }
            mVideoLayout = (RelativeLayout) parentView.findViewById(R.id.video_container);
            Log.i("tzy","mPlayerController = " + mPlayerController);
            if (mPlayerController == null)
                mPlayerController = new SimpleVideoPlayerController(mActivity);
            mPlayerController.setViewGroup(mVideoLayout);
            mPlayerController.setImgUrl(dataMap.get("img"));
            mPlayerController.initView();
            Map<String, String> videoData = StringManager.getFirstMap(dataMap.get("video"));
            if (videoData != null) {
                String videoUrl = videoData.get("videoUrl");
                if (!TextUtils.isEmpty(videoUrl)) {
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(videoUrl);
                    if (maps != null && maps.size() > 0) {
                        String videoD = "";
                        int width = ToolsDevice.getWindowPx(getContext()).widthPixels;
                        for (Map<String, String> map : maps) {
                            if (map != null) {
                                videoD = map.get("defaultUrl");
                                if (TextUtils.isEmpty(videoD)) {
                                    videoD = map.get("D480p");
                                } else
                                    break;
                            }
                        }
                        mPlayerController.initVideoView2(videoD, dataMap.get("name"), null);
                    }
                }
            }
            mPlayerController.hideFullScreen();
            mPlayerController.setOnClick();
            mPlayerController.setOnPlayingCompletionListener(new VideoPlayerController.OnPlayingCompletionListener() {
                @Override
                public void onPlayingCompletion() {
                    showReplayShareView();
                }
            });
            mPlayPosition = position;
            mPlayParentView = parentView;
        }
    }

    /**
     * 暂停播放
     */
    public void stopVideo(){
        if (mPlayerController != null) {
            mPlayPosition = -1;
            if (mPlayParentView != null) {
                View resumeView = mPlayParentView.findViewById(R.id.resume_img);
                if (resumeView != null && resumeView.getVisibility() != View.GONE)
                    resumeView.setVisibility(View.GONE);
            }
            mPlayerController.onPause();
            mPlayParentView = null;
            if (mVideoLayout != null)
                mVideoLayout.removeAllViews();
        }
    }

    /**
     * 重播
     */
    private void restartVideo() {
        JCMediaManager.instance().mediaPlayer.start();
    }

    /**
     * 显示重播、分享界面
     */
    private void showReplayShareView() {
        if (mVideoLayout == null)
            return;
        if (mReplayAndShareView == null)
            mReplayAndShareView = new ReplayAndShareView(mActivity);
        mReplayAndShareView.setOnReplayClickListener(new ReplayAndShareView.OnReplayClickListener() {
            @Override
            public void onReplayClick() {
                if(mVideoLayout != null)
                    mVideoLayout.removeView(mReplayAndShareView);
                restartVideo();
            }
        });
        mReplayAndShareView.setOnShareClickListener(new ReplayAndShareView.OnShareClickListener() {
            @Override
            public void onShareClick() {
                if (mPlayParentView == null || !(mPlayParentView instanceof HomeRecipeItem))
                    return;
                BarShare barShare = new BarShare(mActivity, "视频列表分享视频", "菜谱");
                String type = "", title = "", clickUrl = "", content = "", imgUrl = "",isVideo="";
                //是否是自己区分数据
                Map<String, String> dataMap = ((HomeRecipeItem)mPlayParentView).getData();
                if (dataMap == null)
                    return;
                title = "【香哈菜谱】看了" + dataMap.get("name") + "的教学视频，我已经学会了，味道超赞！";
                clickUrl = StringManager.wwwUrl + "caipu/" + dataMap.get("code") + ".html";
                content = "顶级大厨的做菜视频，讲的真是太详细啦！想吃就赶快进来免费学习吧~ ";
                imgUrl = dataMap.get("img");
                type = BarShare.IMG_TYPE_WEB;
                barShare.setShare(type, title, content, imgUrl, clickUrl);
                barShare.openSharePopup();
            }
        });
        if (mVideoLayout.indexOfChild(mReplayAndShareView) == -1){
            mVideoLayout.addView(mReplayAndShareView);
        }
        mReplayAndShareView.setVisibility(View.VISIBLE);
        mVideoLayout.requestLayout();
        mVideoLayout.invalidate();
    }

    private void hideReplayShareView() {
        if (mVideoLayout != null && mReplayAndShareView != null && mReplayAndShareView.isShowing()) {
            mVideoLayout.removeView(mReplayAndShareView);
        }
    }

    /**
     * 处理置顶数据
     */
    private void handlerTopData(Object object){
        linearLayoutThree.removeAllViews();
        ArrayList<Map<String,String>> listmaps= StringManager.getListMapByJson(object);
        if(listmaps!=null&&listmaps.size()>0){
            int size= listmaps.size();
            for(int i=0;i<size;i++){
                listmaps.get(i).put("isTop","2");
                HomeItem view= handlerTopView(listmaps.get(i),i);
                if(view!=null){
                    linearLayoutThree.addView(view);
                    linearLayoutThree.addView(LayoutInflater.from(mActivity).inflate(R.layout.view_home_show_line,null));
                }
            }
            linearLayoutThree.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 处理置顶数据View类型
     * @param map
     * @return
     */
    private HomeItem handlerTopView(Map<String,String> map,int position){
        HomeItem viewTop=null;
        if(map.containsKey("style")&&!TextUtils.isEmpty(map.get("style"))){
            String type=map.get("style");
            switch (type){
                case AdapterListView.type_tagImage:
                    viewTop= new HomeRecipeItem(mActivity);
                    break;

                case AdapterListView.type_levelImage:
                    viewTop= new HomeAlbumItem(mActivity);
                    break;
                case AdapterListView.type_threeImage:
                    viewTop= new HomePostItem(mActivity);
                    break;
                case AdapterListView.type_anyImage:
                    viewTop= new HomeAnyImgStyleItem(mActivity);
                    break;
                case AdapterListView.type_rightImage:
                case AdapterListView.type_noImage:
                    default:
                    viewTop= new HomeTxtItem(mActivity);
                    break;
            }
            viewTop.setViewType(MODULETOPTYPE);
            viewTop.setHomeModuleBean(homeModuleBean);
            viewTop.setData(map,position);
        }
        return viewTop;
    }
    public boolean isScrollData() {
        return isScrollData;
    }

    public void setScrollData(boolean scrollData) {
        isScrollData = scrollData;
    }

    public int getScrollDataIndex() {
        return scrollDataIndex;
    }

    public void setScrollDataIndex(int scrollDataIndex) {
        this.scrollDataIndex = scrollDataIndex;
    }

    public boolean isRecom() {
        return homeModuleBean.getType().equals(MainHome.recommedType);
    }

    public long getStatrTime() {
        return statrTime;
    }

    public void setStatrTime(long statrTime) {
        this.statrTime = statrTime;
    }
    private void handlerHeaderView(int size){
        if(homeHeaderDataNum!=null&&size>0){
            show_num_tv.setText("有"+size+"条新内容");
            homeHeaderDataNum.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.translate_uptodown);
            animation.setStartOffset(500);
            homeHeaderDataNum.startAnimation(animation );
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    homeHeaderDataNum.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            homeHeaderDataNum.clearAnimation();
//                            ScaleAnimation scaleAnimation = new ScaleAnimation(1,0.5f,1,0.5f,
//                                    Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                            AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
                            //3秒完成动画
                            alphaAnimation.setDuration(1000);
//                            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.translate_downtoup);
                            homeHeaderDataNum.startAnimation(alphaAnimation);
                            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    homeHeaderDataNum.clearAnimation();
                                    homeHeaderDataNum.setVisibility(View.GONE);
                                }
                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                        }
                    },1000*2);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    /**
     * 保存刷新数据
     */
    public void setStatisticShowNum(){
        if(scrollDataIndex>0&&isRecom()){
            XHClick.saveStatictisFile("home",MainHome.recommedType_statictus,"","",String.valueOf(scrollDataIndex),"list","","","","","");
            scrollDataIndex=-1;
        }
    }

    /**
     * 刷新广告数据
     * @param isForceRefresh 是否强制刷新广告
     */
    public void isNeedRefresh(boolean isForceRefresh){
        if(mAdControl==null||mListData==null||adapterListView==null)return;//条件过滤
        boolean state=mAdControl.isNeedRefresh();
        Log.i(tag_yu,"isNeedRefresh::::"+state+":::"+homeModuleBean.getTitle()+"：：：isForceRefresh：："+isForceRefresh);
        if(isForceRefresh)state=isForceRefresh;//强制刷新
//        state=true;
        if(state){
            //重新请求广告
            mAdControl.setAdDataCallBack(new AdOptionParent.AdDataCallBack() {
                @Override
                public void adDataBack(int tag, int nums) {
                    if(tag>=1&&nums>0) {
                        handlerMainThreadUIAD();
                    }
                }
            });
            mAdControl.refreshData();
            if(mAdControl instanceof AdControlHomeDish){//推荐首页
                ((AdControlHomeDish)mAdControl).setAdLoadNumberCallBack(new AdOptionParent.AdLoadNumberCallBack() {
                    @Override
                    public void loadNumberCallBack(int Number) {
                        if(Number>7){
                            handlerMainThreadUIAD();
                        }
                    }
                });
            }else if(mAdControl instanceof  AdControlNormalDish){//其他标准列表结构
                ((AdControlNormalDish)mAdControl).setAdLoadNumberCallBack(new AdOptionParent.AdLoadNumberCallBack() {
                    @Override
                    public void loadNumberCallBack(int Number) {
                        if(Number>7){
                            handlerMainThreadUIAD();
                        }
                    }
                });
            }

            //去掉全部的广告位置
            int size= mListData.size();
            ArrayList<Map<String,String>> listTemp = new ArrayList<>();
            for(int i=0;i<size;i++){
                if(mListData.get(i).containsKey("adstyle")&&"ad".equals(mListData.get(i).get("adstyle"))){
                    listTemp.add(mListData.get(i));
                }
            }
            Log.i(tag_yu,"删除广告");
            if(listTemp.size()>0){
                mListData.removeAll(listTemp);
            }
            adapterListView.notifyDataSetChanged();
        }
    }

    /**
     * 处理广告在主线程中处理
     */
    private void handlerMainThreadUIAD(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mListData = mAdControl.getNewAdData(mListData, false);
                if(adapterListView!=null)adapterListView.notifyDataSetChanged();
            }
        });
    }
}
