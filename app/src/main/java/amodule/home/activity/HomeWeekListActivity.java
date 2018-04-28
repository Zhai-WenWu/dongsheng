package amodule.home.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvListView;
import amodule.home.HomeModuleControler;
import amodule.home.adapter.HomeSecondRecyclerAdapter;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.ad.control.AdControlNormalDish;
import third.ad.option.AdOptionParent;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.control.AdControlHomeDish.tag_yu;

/**
 * 首页的二级页面（本周佳作）
 * Created by sll on 2017/11/13.
 */

public class HomeWeekListActivity extends BaseAppCompatActivity {

    public static final String TAG = HomeWeekListActivity.class.getSimpleName();

    private int mScrollDataIndex=-1;//滚动数据的位置
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();
    private String mBackUrl = "";//向上拉取数据集合
    private String mNextUrl = "";//下页拉取数据集合
    private String mStatisticKey = null;
    private boolean mLoadOver = false;
    private boolean mNeedRefCurrData = false;
    private boolean mCompelClearData = false;//强制清除数据

    private AdControlNormalDish mAdControl;
    
    private PtrClassicFrameLayout mPtrFrameLayout;
    private RvListView mRv;
    private HomeSecondRecyclerAdapter mHomeAdapter;
    private HomeModuleBean mModuleBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("", 2, 0, R.layout.back_title_bar, R.layout.home_week_listlayout);
        initData();
        initView();
        addListener();
        requestDate();
    }

    private void initData() {
        mModuleBean = new HomeModuleControler().getHomeModuleByType(this,"dish");
        mAdControl = getAdControl();
        mAdControl.setRefreshCallback(() -> {
            mListData = mAdControl.getAutoRefreshAdData(mListData);
            if (mHomeAdapter != null){
                mHomeAdapter.notifyDataSetChanged();
            }
        });
        mActMagager.registerADController(mAdControl);
    }


    private void addListener() {
        mRv.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem)view).onClickEvent(view);
            }
        });
    }

    private void initView() {
        mRv = (RvListView) findViewById(R.id.recycler_view);
        mPtrFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        mPtrFrameLayout.disableWhenHorizontalMove(true);
        TextView titleV = (TextView) findViewById(R.id.title);
        titleV.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
        if(mModuleBean != null)
            titleV.setText(mModuleBean.getTitle());
    }

    private void requestDate() {
        mHomeAdapter = new HomeSecondRecyclerAdapter(this,mListData,mAdControl);
        mHomeAdapter.setHomeModuleBean(mModuleBean);
        mHomeAdapter.setViewOnClickCallBack(isOnClick -> refresh());
        if(!mLoadOver){
            loadManager.setLoading(mPtrFrameLayout, mRv, mHomeAdapter, true,
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
                if(!TextUtils.isEmpty(mModuleBean.getTwoType()))
                    params+="&two_type="+mModuleBean.getTwoType();
            }
        }else{//向下翻页
            if(!TextUtils.isEmpty(mNextUrl)){
                params=mNextUrl;
            }else{
                params= "type="+mModuleBean.getType();
                if(!TextUtils.isEmpty(mModuleBean.getTwoType()))params+="&two_type="+mModuleBean.getTwoType();
            }
        }
        loadData(refresh, params);
    }

    protected void loadData(final boolean refresh, String data){
        String url= StringManager.API_RECOMMEND;

        //更新加载按钮状态
        loadManager.changeMoreBtn(mRv, ReqInternet.REQ_OK_STRING, -1, -1, mLoadOver?2:1, mListData == null || mListData.isEmpty());
        mLoadOver = true;
        if (refresh) {
            mCompelClearData = true;
            if(TextUtils.isEmpty(mBackUrl)&&mListData.size()<=0)
                loadManager.showProgressBar();
            else
                loadManager.hideProgressBar();
        }
        ReqEncyptInternet.in().doEncyptAEC(url,data, new InternetCallback() {
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
                           //YLKLog.i("zyj","刷新数据：清集合");
                            isNeedRefresh(true);
                            //强制刷新，重置数据
                            if(!TextUtils.isEmpty(dataMap.get("mBackUrl")))
                                mBackUrl = dataMap.get("mBackUrl");
                            if(!TextUtils.isEmpty(dataMap.get("nexturl")))
                                mNextUrl = dataMap.get("nexturl");

                        }
                        ArrayList<Map<String, String>> listDatas = StringManager.getListMapByJson(dataMap.get("list"));
                        if (listDatas != null && listDatas.size() > 0) {
                            loadCount=listDatas.size();
                            int oldDayDishIndex = -1;
                            if (refresh && mListData.size() > 0) {
                                //如果需要加广告，插入广告
                                if (mAdControl != null) {
                                    //插入广告
                                   //YLKLog.i(tag_yu,"listDatas::111:"+listDatas.size());
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
                loadManager.hideProgressBar();
                loadManager.changeMoreBtn(mRv, flag, LoadManager.FOOTTIME_PAGE, refresh?mListData.size():loadCount, 0, refresh);
                if(refresh){
                    mPtrFrameLayout.refreshComplete();
                }
                mCompelClearData=false;//强制刷新只能使用一次，一次数据后被置回去
            }
        });
    }

    private void refresh() {
        if (mPtrFrameLayout != null) {
            mPtrFrameLayout.autoRefresh();
        }
    }

    public AdControlNormalDish getAdControl(){
        String type = mModuleBean.getType(); //当前页的type
        if(TextUtils.isEmpty(type)){
            return null;
        }
        AdOptionParent adControlParent = null;
        String[] adPlayIds = new String[0];
        boolean isSetAd = true;
        if("dish".equals(type)){ //本周佳作
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

    @Override
    protected void onResume() {
        super.onResume();
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
            mAdControl.setAdLoadNumberCallBack(Number -> {
                if(Number>7){
                    handlerMainThreadUIAD();
                }
            });

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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
