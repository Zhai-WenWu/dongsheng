package amodule.home;

import android.annotation.SuppressLint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.home.view.HomeTitleLayout;
import amodule.main.activity.MainHome;
import amodule.main.activity.MainHomePage;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeItem;
import third.ad.control.AdControlHomeDish;

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

    private HomeTitleLayout mTitleLayout;
    private RvListView mRvListView;
    private View mHeaderView;
    //feed头部view

    //广告控制器
    private AdControlHomeDish mAdControl;
    protected boolean isScrollData = false;//是否滚动数据
    protected int scrollDataIndex = -1;//滚动数据的位置

    public HomeViewControler(MainHomePage activity) {
        this.mActivity = activity;
        initUI();
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        mAdControl = AdControlHomeDish.getInstance().getTwoLoadAdData();

        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_home_header_layout, null, true);
        mHeaderControler = new HomeHeaderControler(mHeaderView);
        mHomeFeedHeaderControler = new HomeFeedHeaderControler(mActivity);

        mTitleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
        mRvListView.addHeaderView(mHomeFeedHeaderControler.getLayout());
        mRvListView.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem) view).onClickEvent(view);
            }
        });
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
                    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                    isScrollData = true;
                    if (scrollDataIndex < (lastVisibleItemPosition - 1)) {
                        scrollDataIndex = (lastVisibleItemPosition - 1);
                    }
                }
            });
        }

        //设置活动icon点击
        mTitleLayout.setOnClickActivityIconListener((v, url) -> {
            if (TextUtils.isEmpty(url)) return;
            AppCommon.openUrl(mActivity, url, true);
        });
    }

    //
    public void setHeaderData(List<Map<String, String>> data, boolean isShowCache) {
        if(data == null || data.isEmpty()){
            mHeaderControler.setVisibility(false);
            return;
        }
        int length = data.size();
        for(int index = 0 ; index < length ; index ++){
            Map<String,String> map = data.get(index);
            String widgetData = map.get("widgetData");
            Map<String, String> dataMap = StringManager.getFirstMap(widgetData);
            dataMap = StringManager.getFirstMap(dataMap.get("parameter"));
            String isCache = dataMap.get("isCache");
            map.put("isCache", TextUtils.isEmpty(isCache) ? "1" : isCache);
        }
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

    //刷新广告index
    public void refreshADIndex() {
        if (mAdControl == null)
            return;
        mAdControl.refrush();
    }

    public void setFeedheaderVisibility(boolean isShow) {
        mHomeFeedHeaderControler.setFeedheaderVisibility(isShow);
    }

    /**
     * 保存刷新数据
     */
    public void setStatisticShowNum() {
        if (scrollDataIndex > 0) {
            XHClick.saveStatictisFile("home", MainHome.recommedType_statictus, "", "", String.valueOf(scrollDataIndex), "list", "", "", "", "", "");
            scrollDataIndex = -1;
        }
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public AdControlHomeDish getAdControl() {
        return mAdControl;
    }

    public RvListView getRvListView() {
        return mRvListView;
    }

}
