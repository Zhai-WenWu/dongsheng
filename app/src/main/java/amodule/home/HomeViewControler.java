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


    protected AdControlParent mAdControl;



    public HomeViewControler(MainHomePage homePage) {
        this.mActivity = homePage;

        initUI();
    }

    private void initUI() {
        mAdControl = AdControlHomeDish.getInstance().getTwoLoadAdData();

        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_home_header_layout, null, true);
        mHeaderControler = new HomeHeaderControler(mHeaderView);

        mTitleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
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
        if (null == data || data.isEmpty()) return;
        //TODO
    }

    /** 刷新 */
    public void refresh() {
    }

    public void clearData(){

    }

    public AdControlParent getAdControl() {
        return mAdControl;
    }

    public RvListView getRvListView() {
        return mRvListView;
    }
}
