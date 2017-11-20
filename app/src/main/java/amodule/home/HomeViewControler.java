package amodule.home;

import android.annotation.SuppressLint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule._common.helper.WidgetDataHelper;
import amodule.home.view.HomeTitleLayout;
import amodule.main.Tools.BuoyControler;
import amodule.main.activity.MainHome;
import amodule.main.activity.MainHomePage;
import amodule.main.view.item.HomeItem;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:00.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeViewControler {

    public static String MODULETOPTYPE = "moduleTopType";//置顶数据的类型

    private HomeHeaderControler mHeaderControler;
    private HomeFeedHeaderControler mHomeFeedHeaderControler;

    private MainHomePage mActivity;

    private BuoyControler.Buoy mBuoy;

    private HomeTitleLayout mTitleLayout;
    private RvListView mRvListView;
    //feed头部view
    private View mHeaderView;

    protected boolean isScrollData = false;//是否滚动数据
    protected int scrollDataIndex = -1;//滚动数据的位置

    public HomeViewControler(MainHomePage activity) {
        this.mActivity = activity;
        long startTime = System.currentTimeMillis();
        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_home_header_layout, null, true);
        long endtime1 = System.currentTimeMillis() - startTime;
        Log.i("tzy","inflate time : " + (endtime1) + "ms");
    }

    public void onCreate(){
        initUI();
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        long startTime = System.currentTimeMillis();
        mHeaderControler = new HomeHeaderControler(mHeaderView);

        long endtime2 = System.currentTimeMillis() - startTime;
        Log.i("tzy","HomeHeaderControler init time : " + (endtime2) + "ms");
        mHomeFeedHeaderControler = new HomeFeedHeaderControler(mActivity);

        long endtime3 = System.currentTimeMillis() - startTime - endtime2;
        Log.i("tzy","HomeFeedHeaderControler init time : " + (endtime3) + "ms");
        mTitleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        mTitleLayout.setStatictusData(MainHomePage.STATICTUS_ID_PULISH,"顶部topbar","");
        mTitleLayout.postDelayed(()->{
            mBuoy = new BuoyControler.Buoy(mActivity,BuoyControler.TYPE_HOME);
            mBuoy.setClickCallback(() -> XHClick.mapStat(mActivity,MainHomePage.STATICTUS_ID_PULISH,"首页右侧侧边栏浮动图标",""));
        },2000);

        long endtime4 = System.currentTimeMillis() - startTime - endtime2 - endtime3;
        Log.i("tzy","HomeTitleLayout init time : " + (endtime4) + "ms");

        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
        mRvListView.addHeaderView(mHomeFeedHeaderControler.getLayout());
        mRvListView.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem) view).onClickEvent(view);
            }
        });


        //设置活动icon点击
        mTitleLayout.setOnClickActivityIconListener((v, url) -> {
            if (TextUtils.isEmpty(url)) return;
            AppCommon.openUrl(mActivity, url, true);
        });
    }

    public void addOnScrollListener(){
        RecyclerView.LayoutManager layoutManager = mRvListView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            mRvListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
//                    if(newState == RecyclerView.SCROLL_STATE_IDLE
//                            || newState == RecyclerView.SCROLL_STATE_TOUCH_SCROLL){
//                        Glide.with(getContext()).resumeRequests();
//                    }else{
//                        Glide.with(getContext()).pauseRequests();
//                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                    isScrollData = true;
                    if (scrollDataIndex < (lastVisibleItemPosition - 1)) {
                        scrollDataIndex = (lastVisibleItemPosition - 1);
                    }
                    if(mBuoy != null && mBuoy.isMove())
                        mBuoy.executeCloseAnim();
                }
            });
        }
    }

    //
    public void setHeaderData(List<Map<String, String>> data, boolean isShowCache) {
        if(data == null || data.isEmpty()){
            mHeaderControler.setVisibility(false);
            return;
        }
        Stream.of(data).forEach(map -> {
            Map<String,String> temp = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_WIDGET_DATA));
            map.put("cache","2".equals(temp.get("isCache"))?"2":"1");
        });
        mHeaderControler.setData(data, isShowCache);
    }

    public void setTopData(List<Map<String, String>> data) {
        mHomeFeedHeaderControler.setTopData(data);
    }

    //回到第一个位置
    public void returnListTop() {
        if (mRvListView != null) {
            mRvListView.scrollToPosition(0);
        }
    }

    public void refreshBouy(){
        if(mBuoy != null){
            mBuoy.refresh(true);
        }else{
            mBuoy = new BuoyControler.Buoy(mActivity,BuoyControler.TYPE_HOME);
        }
    }

    public void setFeedheaderVisibility(boolean isShow) {
        mHomeFeedHeaderControler.setFeedheaderVisibility(isShow);
    }

    /**
     * 保存刷新数据
     */
    public void setStatisticShowNum() {
        //头部统计数据存储
        if(mHeaderControler != null){
            mHeaderControler.saveStatisticData();
        }
        //列表
        if (scrollDataIndex > 0) {
            XHClick.saveStatictisFile("home", MainHome.recommedType_statictus, "", "", String.valueOf(scrollDataIndex), "list", "", "", "", "", "");
//            XHClick.saveStatictisFile("home", "horizatal", "", "", String.valueOf(scrollDataIndex), "list", "", "", "", "", "");
            scrollDataIndex = -1;
        }
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public RvListView getRvListView() {
        return mRvListView;
    }

}
