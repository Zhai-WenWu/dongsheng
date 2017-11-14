package amodule.home;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.home.view.HomeTitleLayout;
import amodule.main.activity.MainHomePage;
import amodule.main.adapter.HomeAdapter;
import amodule.main.view.item.HomeItem;
import third.ad.control.AdControlHomeDish;
import third.ad.control.AdControlParent;

import static third.ad.control.AdControlHomeDish.tag_yu;

/**
 * Description : //TODO
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:00.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeViewControler {

    HomeHeaderControler mHeaderControler;

    private MainHomePage mActivity;

    private HomeTitleLayout mTitleLayout;
    private RvListView mRvListView;
    private View mHeaderView;

    private HomeAdapter mAdapter;
    protected AdControlParent mAdControl;

    private ArrayList<Map<String, String>> mData = new ArrayList<>();

    public HomeViewControler(MainHomePage homePage) {
        this.mActivity = homePage;

        initUI();
    }

    private void initUI() {
        mTitleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_home_header_layout, null, true);
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
        mRvListView.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem) view).onClickEvent(view);
            }
        });
        mAdControl = AdControlHomeDish.getInstance().getTwoLoadAdData();
        mAdapter = new HomeAdapter(mActivity, mData, mAdControl);
        mAdapter.setHomeModuleBean(new HomeModuleControler().getHomeModuleByType(mActivity, null));
        mAdapter.setViewOnClickCallBack(isOnClick -> refresh());
        mHeaderControler = new HomeHeaderControler(mHeaderView);
        //TODO
        mRvListView.setAdapter(mAdapter);
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
        if (null == data || data.isEmpty()) return;
        //TODO
    }

    /**
     * 刷新广告数据
     *
     * @param isForceRefresh 是否强制刷新广告
     */
    public void isNeedRefresh(boolean isForceRefresh) {
        //条件过滤
        if (mAdControl == null
                || mData == null
                || mAdapter == null)
            return;

        boolean state = mAdControl.isNeedRefresh();
        if (isForceRefresh)
            state = isForceRefresh;//强制刷新
        //重新请求广告
        mAdControl.setAdDataCallBack((tag, nums) -> {
            if (tag >= 1 && nums > 0) {
                handlerMainThreadUIAD();
            }
        });
        mAdControl.refreshData();
        ((AdControlHomeDish) mAdControl).setAdLoadNumberCallBack(Number -> {
            if (Number > 7) {
                handlerMainThreadUIAD();
            }
        });
        //去掉全部的广告位置
        int size = mData.size();
        ArrayList<Map<String, String>> listTemp = new ArrayList<>();
        Stream.of(mData).forEach(map -> {
            if (map.containsKey("adstyle")
                    && "ad".equals(map.get("adstyle"))) {
                listTemp.add(map);
            }
        });
        Log.i(tag_yu, "删除广告");
        if (listTemp.size() > 0) {
            mData.removeAll(listTemp);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 处理广告在主线程中处理
     */
    protected void handlerMainThreadUIAD(){
        new Handler(Looper.getMainLooper()).post(() -> {
            mData = mAdControl.getNewAdData(mData, false);
            if(mAdapter!=null)
                mAdapter.notifyDataSetChanged();
        });
    }

    /** 刷新 */
    public void refresh() {
    }

    public void clearData(){

    }

    public HomeAdapter getAdapter() {
        return mAdapter;
    }

    public RvListView getRvListView() {
        return mRvListView;
    }
}
