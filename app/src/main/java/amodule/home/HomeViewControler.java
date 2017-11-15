package amodule.home;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.home.view.HomeTitleLayout;
import amodule.main.activity.MainHomePage;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeItem;
import third.ad.control.AdControlHomeDish;

/**
 * Description : //TODO
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:00.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeViewControler {

    public static String MODULETOPTYPE="moduleTopType";//置顶数据的类型

    private HomeHeaderControler mHeaderControler;
    private HomeFeedHeaderControler mHomeFeedHeaderControler;

    private MainHomePage mActivity;
    private HomeModuleBean mHomeModuleBean;

    private HomeTitleLayout mTitleLayout;
    private RvListView mRvListView;
    private View mHeaderView;
    //feed头部view

    //广告控制器
    private AdControlHomeDish mAdControl;

    public HomeViewControler(MainHomePage activity) {
        this.mActivity = activity;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(activity, null);
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

        //设置活动icon点击
        mTitleLayout.setOnClickActivityIconListener((v, url) -> {
            if (TextUtils.isEmpty(url)) return;
            AppCommon.openUrl(mActivity, url, true);
        });
    }

    //
    public void setHeaderData(List<Map<String, String>> data, boolean isShowCache) {
        Stream.of(data).forEach(map -> {
            String widgetData = map.get("widgetData");
            Map<String, String> dataMap = StringManager.getFirstMap(widgetData);
            dataMap = StringManager.getFirstMap(dataMap.get("paramter"));
            String isCache = dataMap.get("isCache");
            map.put("isCache", TextUtils.isEmpty(isCache) ? "1" : isCache);
        });
        mHeaderControler.setData(data, isShowCache);
    }

    public void setTopData(List<Map<String, String>> data) {
        mHomeFeedHeaderControler.setTopData(data);
    }

    /** 刷新 */
    public void refresh() {

    }

    //回到第一个位置
    public void returnListTop() {
        if (mRvListView != null) {
            mRvListView.scrollToPosition(0);
        }
    }

    //刷新广告index
    public void refreshADIndex(){
        if(mAdControl == null)
            return;
        mAdControl.refrush();
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public AdControlHomeDish getAdControl() {
        return mAdControl;
    }

    public RvListView getRvListView() {
        return mRvListView;
    }

}
