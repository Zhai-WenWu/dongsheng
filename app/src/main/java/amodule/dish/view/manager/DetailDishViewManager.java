package amodule.dish.view.manager;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.view.DishADBannerView;
import amodule.dish.view.DishAboutView;
import amodule.dish.view.DishHeaderViewNew;
import amodule.dish.view.DishIngreDataShow;
import amodule.dish.view.DishTitleViewControlNew;

/**
 * 当前只处理View的拼装
 * 不能牵扯如何业务逻辑处理----因为当前页面业务确定，采用直接数据指向方法（不抽象不模糊）
 */
public class DetailDishViewManager {
    public static int showNumLookImage = 0;//点击展示次数
    public DishTitleViewControlNew dishTitleViewControlNew;
    public LinearLayout layoutHeader;
    public Activity mAct;
    //广告所用bar高度;图片/视频高度
    private int statusBarHeight = 0, headerLayoutHeight;
    private int titleHeight;//标题高度
    //头部信息
    public DishHeaderViewNew dishHeaderViewNew;
    public DishAboutView dishAboutView;
    public DishIngreDataShow dishIngreDataShow;

    /**
     * 对view进行基础初始化
     */
    public DetailDishViewManager(Activity activity, ListView listView) {
        mAct = activity;
        dishTitleViewControlNew = new DishTitleViewControlNew(activity);
        dishTitleViewControlNew.initView(activity);
        if (layoutHeader == null) {
            layoutHeader = new LinearLayout(activity);
            layoutHeader.setOrientation(LinearLayout.VERTICAL);
        }
        titleHeight = Tools.getDimen(mAct, R.dimen.dp_45);
        statusBarHeight = Tools.getStatusBarHeight(mAct);
        headerLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight;
        //图片视频信息
        dishHeaderViewNew = new DishHeaderViewNew(mAct);
        dishHeaderViewNew.initView(mAct, headerLayoutHeight);
        //用户信息和菜谱基础信息
        dishAboutView= new DishAboutView(mAct);
        dishAboutView.setVisibility(View.GONE);
        //用料
        dishIngreDataShow= new DishIngreDataShow(mAct);
        dishIngreDataShow.setVisibility(View.GONE);
        //banner
        DishADBannerView dishADBannerView= new DishADBannerView(mAct);
        dishADBannerView.setData(mAct);

        layoutHeader.addView(dishHeaderViewNew);
        layoutHeader.addView(dishAboutView);
        layoutHeader.addView(dishADBannerView);
        layoutHeader.addView(dishIngreDataShow);

        listView.addHeaderView(layoutHeader);
        listView.setVisibility(View.VISIBLE);

    }
    /**
     * 处理标题信息数据
     */
    public void handlerTitle() {

    }
    /**
     * 处理header图片，和视频数据
     */
    public void handlerHeaderView(ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        if (dishHeaderViewNew != null)
            dishHeaderViewNew.setData(list, permissionMap);
    }
    /**
     * 处理菜谱基本信息
     */
    public void handlerDishData(ArrayList<Map<String, String>> list) {
        if(dishAboutView!=null) {
            dishAboutView.setVisibility(View.VISIBLE);
            dishAboutView.setData(list.get(0), mAct);
        }
    }
    /**
     * 处理用户信息
     */
    public void handlerUserData(ArrayList<Map<String, String>> list) {
        if(dishAboutView!=null) {
            dishAboutView.setVisibility(View.VISIBLE);
            dishAboutView.setUserData(list.get(0),mAct);
        }
    }
    /**
     * 处理用料
     */
    public void handlerIngreView(ArrayList<Map<String, String>> list) {
        if(dishIngreDataShow!=null) {
            dishIngreDataShow.setVisibility(View.VISIBLE);
            dishIngreDataShow.setData(list);
        }
    }
    /**
     * 处理广告信息
     */
    public void handlerAdDataView() {

    }
    /**
     * 处理小贴士信息
     */
    public void handlerExplainView() {

    }

}
