package amodule.home.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.home.adapter.HomeSecondRecyclerAdapter;
import amodule.home.module.HomeSecondModule;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.ad.control.AdControlHomeDish;
import third.ad.control.AdControlNormalDish;
import third.ad.control.AdControlParent;
import third.ad.option.AdOptionParent;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.control.AdControlHomeDish.tag_yu;

/**
 * 首页二级页面（视频、每日三餐）
 */
public class HomeSecondListFragment extends Fragment {
    
    public static final String TAG = HomeSecondListFragment.class.getSimpleName();

    private static final String KEY_HOMEMODULE = "home_module";
    private static final String KEY_SECONDMODULE = "second_module";
    private static final String KEY_POSITION = "position";

    private int mScrollDataIndex=-1;//滚动数据的位置
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();
    private String mBackUrl = "";//向上拉取数据集合
    private String mNextUrl = "";//下页拉取数据集合
    private String mStatisticKey = null;
    private boolean mLoadOver = false;
    private boolean mNeedRefCurrData = false;
    private boolean mCompelClearData = false;//强制清除数据
    /** 是否初始化 */
    private boolean mIsPrepared = false;
    /** 是否显示 */
    private boolean mIsVisible;

    private AdControlParent mAdControl;
    private BaseAppCompatActivity mActivity;
    private PtrClassicFrameLayout mPtrFrameLayout;
    private RvListView mRv;
    private HomeSecondRecyclerAdapter mHomeAdapter;
    private LoadManager mLoadManager;
    
    private HomeModuleBean mModuleBean;
    private HomeSecondModule mSecondModuleBean;
    private int mPosition;

    private OnTabDataReadyCallback mCallback;
    
    public HomeSecondListFragment() {
        // Required empty public constructor
    }

    public static HomeSecondListFragment newInstance(HomeModuleBean moduleBean, int position, HomeSecondModule secondModule) {
        if (moduleBean == null || secondModule == null)
            return null;
        HomeSecondListFragment fragment = new HomeSecondListFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_HOMEMODULE, moduleBean);
        args.putInt(KEY_POSITION, position);
        args.putSerializable(KEY_SECONDMODULE, secondModule);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mModuleBean = (HomeModuleBean) getArguments().getSerializable(KEY_HOMEMODULE);
            mSecondModuleBean = (HomeSecondModule) getArguments().getSerializable(KEY_SECONDMODULE);
            mPosition = getArguments().getInt(KEY_POSITION);
        }
        mAdControl = getAdControl();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_second_list, container, false);
        mPtrFrameLayout = (PtrClassicFrameLayout) view.findViewById(R.id.refresh_list_view_frame);
        mPtrFrameLayout.disableWhenHorizontalMove(true);
        mRv = (RvListView) view.findViewById(R.id.recycler_view);
        addListener();
        mLoadOver = false;
        mIsPrepared = true;
        mHomeAdapter = new HomeSecondRecyclerAdapter(mActivity,mListData,mAdControl);
        mHomeAdapter.setHomeModuleBean(mModuleBean);
        mHomeAdapter.setViewOnClickCallBack(isOnClick -> refresh());
        requestData();
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            mIsVisible = true;
            onVisible();
            if (mNeedRefCurrData) {
                refresh();
            }
        } else {
            mIsVisible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        preLoad();
        if (mLoadManager != null && mRv != null) {
            if( mListData.size() == 0){
                mLoadManager.showProgressBar();
            }else mLoadManager.hideProgressBar();
        }
    }
    protected void onInvisible() {
    }

    public int getPosition() {
        return mPosition;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseAppCompatActivity) context;
        mLoadManager = mActivity.loadManager;
    }

    private void addListener() {
        mRv.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem)view).onClickEvent(view);
            }
        });
    }

    protected void preLoad() {
        if (!mIsPrepared || !mIsVisible) {
            return;
        }
        //填充各控件的数据
        //防止二次生成
        if (!mLoadOver) {
            requestData();
        }
    }
    private void requestData() {
        if(!mLoadOver && mIsVisible){
            mLoadManager.setLoading(mPtrFrameLayout, mRv, mHomeAdapter, true, mIsVisible,
                    v -> entryptData(true),
                    v -> entryptData(!mLoadOver));
            RecyclerView.LayoutManager layoutManager = mRv.getLayoutManager();
            if(layoutManager != null && layoutManager instanceof LinearLayoutManager){
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                mRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                        if(mScrollDataIndex<(lastVisibleItemPosition-1)) {
                            mScrollDataIndex = (lastVisibleItemPosition-1);
                        }
                    }
                });
            }
        }
    }

    protected void entryptData(final boolean refresh){
        if(refresh){
            isNeedRefresh(false);
        }
        if (mNeedRefCurrData) {//需要刷新当前数据
            mNeedRefCurrData = false;
            mBackUrl = "";//重置backUrl
            mListData.clear();
            if (mHomeAdapter != null)
                mHomeAdapter.notifyDataSetChanged();
        }
        String params="";
        mLoadOver = true;
        if(refresh){//向上翻页
            if(mAdControl != null)
                mAdControl.refrush();
            if(!TextUtils.isEmpty(mBackUrl)){
                params=mBackUrl;
            }else{
                params= "type="+mModuleBean.getType();
                if(!TextUtils.isEmpty(mSecondModuleBean.getType()))
                    params+="&two_type="+mSecondModuleBean.getType();
            }
        }else{//向下翻页
            if(!TextUtils.isEmpty(mNextUrl)){
                params=mNextUrl;
            }else{
                params= "type="+mModuleBean.getType();
                if(!TextUtils.isEmpty(mSecondModuleBean.getType()))params+="&two_type="+mSecondModuleBean.getType();
            }
        }
        loadData(refresh, params);
    }

    protected void loadData(final boolean refresh, String data){
        String url= StringManager.API_RECOMMEND;

        //更新加载按钮状态
        mLoadManager.changeMoreBtn(mRv, ReqInternet.REQ_OK_STRING, -1, -1, mLoadOver?2:1, refresh && mIsVisible);
        mLoadOver = true;
        ReqEncyptInternet.in().doEncyptAEC(url,data, new InternetCallback(mActivity) {
            @Override
            public void loaded(int flag, String url, Object object) {
                int loadCount = 0;
                if(flag>=ReqInternet.REQ_OK_STRING){
                    //Log.i("FRJ","获取  服务端   数据回来了-------------");

                    Map<String,String> dataMap = StringManager.getFirstMap(object);
                    //当前数据有问题，直接return数据
                    if(!(!dataMap.containsKey("list")
                            ||StringManager.getListMapByJson(dataMap.get("list")).size()<=0)){
                        //上拉数据，下拉数据
                        if (TextUtils.isEmpty(mBackUrl) || (!TextUtils.isEmpty(dataMap.get("mBackUrl")) && refresh))
                            mBackUrl = dataMap.get("mBackUrl");
                        if (TextUtils.isEmpty(mNextUrl) || !TextUtils.isEmpty(dataMap.get("nexturl")) && !refresh)
                            mNextUrl = dataMap.get("nexturl");
                        //当前只有向上刷新，并且服务端确认可以刷新数据
                        if (mCompelClearData || (refresh && !TextUtils.isEmpty(dataMap.get("reset")) && "2".equals(dataMap.get("reset")))) {
                            mListData.clear();
                            Log.i("zyj","刷新数据：清集合");
                            isNeedRefresh(true);
                            //强制刷新，重置数据
                            if(!TextUtils.isEmpty(dataMap.get("mBackUrl")))
                                mBackUrl = dataMap.get("mBackUrl");
                            if(!TextUtils.isEmpty(dataMap.get("nexturl")))
                                mNextUrl = dataMap.get("nexturl");

                        }
                        //初始化二级
                        if (mCallback != null) {
                            mCallback.onTabDataReady(dataMap.get("trigger_two_type"));
                        }
                        ArrayList<Map<String, String>> listDatas = StringManager.getListMapByJson(dataMap.get("list"));
                        if (listDatas != null && listDatas.size() > 0) {
                            loadCount=listDatas.size();
                            if (refresh && mListData.size() > 0) {
                                //如果需要加广告，插入广告
                                if (mAdControl != null) {
                                    //插入广告
                                    Log.i(tag_yu,"listDatas::111:"+listDatas.size());
                                    listDatas = mAdControl.getNewAdData(listDatas, true);
                                }
                                mListData.addAll(0, listDatas);//插入到第一个位置
                            } else {
                                mListData.addAll(listDatas);//顺序插入
                                //如果需要加广告，插入广告
                                if (mAdControl != null) {
                                    mListData = mAdControl.getNewAdData(mListData, false);
                                }
                            }
                        }
                        mHomeAdapter.notifyDataSetChanged();
                    }
                }
                mLoadManager.hideProgressBar();
                mLoadManager.changeMoreBtn(mRv, flag, LoadManager.FOOTTIME_PAGE, refresh?mListData.size():loadCount, 0, refresh && mIsVisible);
                if(refresh){
                    mPtrFrameLayout.refreshComplete();
                }
                mCompelClearData=false;//强制刷新只能使用一次，一次数据后被置回去
            }
        });
    }

    public void refresh() {
        if (mRv != null)
            mRv.scrollToPosition(0);
        if (mPtrFrameLayout != null) {
            mPtrFrameLayout.autoRefresh();
        }
    }

    public AdControlParent getAdControl(){
        String type = mModuleBean.getType(); //当前页的type
        //Log.i("FRJ","type:" + type);
        if(TextUtils.isEmpty(type)){
            return null;
        }
        AdOptionParent adControlParent = null;
        String[] adPlayIds = new String[0];
        boolean isSetAd = true;
        if("video".equals(type)){ //视频
            mStatisticKey = "sp_list";
            adPlayIds = AdPlayIdConfig.MAIN_HOME_VIDEO_LIST;
        }else if("article".equals(type)){ //涨知识
            mStatisticKey = "other_top_list";
            adPlayIds = AdPlayIdConfig.MAIN_HOME_ZHISHI_LIST;
        }else if("day".equals(type)){ //每日三餐
            mStatisticKey = "sc_list";
            adPlayIds = AdPlayIdConfig.COMMEND_THREE_MEALS;
        }else if("dish".equals(type)){ //本周佳作
            mStatisticKey = "jz_list";
            adPlayIds = AdPlayIdConfig.MAIN_HOME_WEEK_GOOD_LIST;
        }else{
            isSetAd = false;
        }
        if(isSetAd && adControlParent == null){

            return new AdControlNormalDish(mStatisticKey,adPlayIds);
        }
        return null;
    }

    /**
     * 刷新广告数据
     * @param isForceRefresh 是否强制刷新广告
     */
    public void isNeedRefresh(boolean isForceRefresh){
        if(mAdControl==null||mListData==null||mHomeAdapter==null)return;//条件过滤
        boolean state=mAdControl.isNeedRefresh();
        if(isForceRefresh)
            state=isForceRefresh;//强制刷新
        if(state){
            //重新请求广告
            mAdControl.setAdDataCallBack((tag, nums) -> {
                if(tag>=1&&nums>0) {
                    handlerMainThreadUIAD();
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
                ((AdControlNormalDish)mAdControl).setAdLoadNumberCallBack(Number -> {
                    if(Number>7){
                        handlerMainThreadUIAD();
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
            if(listTemp.size()>0){
                mListData.removeAll(listTemp);
            }
            mHomeAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 处理广告在主线程中处理
     */
    protected void handlerMainThreadUIAD(){
        new Handler(Looper.getMainLooper()).post(() -> {
            mListData = mAdControl.getNewAdData(mListData, false);
            if(mHomeAdapter!=null)
                mHomeAdapter.notifyDataSetChanged();
        });
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setOnTabDataReadyCallback(OnTabDataReadyCallback callback) {
        mCallback = callback;
    }

    public interface OnTabDataReadyCallback {
        void onTabDataReady(String selectedType);
    }
}
