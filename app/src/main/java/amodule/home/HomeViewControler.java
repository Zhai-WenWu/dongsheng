package amodule.home;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.home.view.HomeTitleLayout;
import amodule.main.activity.MainHomePage;
import amodule.main.adapter.HomeAdapter;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeAlbumItem;
import amodule.main.view.item.HomeAnyImgStyleItem;
import amodule.main.view.item.HomeItem;
import amodule.main.view.item.HomePostItem;
import amodule.main.view.item.HomeRecipeItem;
import amodule.main.view.item.HomeTxtItem;
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

    private MainHomePage mActivity;
    private HomeModuleBean mHomeModuleBean;

    private HomeTitleLayout mTitleLayout;
    private RvListView mRvListView;
    private View mHeaderView;
    //feed头部view
    private LinearLayout layout,linearLayoutOne,linearLayoutTwo,linearLayoutThree;
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

        initFeedHeaderView();

        mTitleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
        mRvListView.addHeaderView(layout);
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

    /**
     * 初始化header布局
     */
    private void initFeedHeaderView(){
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
        linearLayoutThree.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        int size = data.size();
        for(int i = 0 ; i < size ; i++){
            HomeItem view = handlerTopView(data.get(i),i);
            if(view != null){
                linearLayoutThree.addView(view);
                linearLayoutThree.addView(inflater.inflate(R.layout.view_home_show_line,null));
            }
        }
        linearLayoutThree.setVisibility(View.VISIBLE);
    }

    /**
     * 处理置顶数据View类型
     * @param map
     * @return
     */
    private HomeItem handlerTopView(Map<String,String> map,int position){
        HomeItem viewTop=null;
        if(map.containsKey("style")&&!TextUtils.isEmpty(map.get("style"))){
            int type=TextUtils.isEmpty(map.get("style")) ? HomeAdapter.type_noImage : Integer.parseInt(map.get("style"));
            switch (type){
                case HomeAdapter.type_tagImage:
                    viewTop= new HomeRecipeItem(mActivity);
                    break;
                case HomeAdapter.type_levelImage:
                    viewTop= new HomeAlbumItem(mActivity);
                    break;
                case HomeAdapter.type_threeImage:
                    viewTop= new HomePostItem(mActivity);
                    break;
                case HomeAdapter.type_anyImage:
                    viewTop= new HomeAnyImgStyleItem(mActivity);
                    break;
                case HomeAdapter.type_rightImage:
                case HomeAdapter.type_noImage:
                default:
                    viewTop= new HomeTxtItem(mActivity);
                    break;
            }
            viewTop.setViewType(MODULETOPTYPE);
            viewTop.setHomeModuleBean(mHomeModuleBean);
            viewTop.setData(map,position);
        }
        if (viewTop != null) {
            viewTop.setOnClickListener(v -> ((HomeItem)v).onClickEvent(v));
        }
        return viewTop;
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
