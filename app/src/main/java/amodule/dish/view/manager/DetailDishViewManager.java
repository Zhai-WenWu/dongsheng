package amodule.dish.view.manager;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.view.DishADBannerView;
import amodule.dish.view.DishAboutView;
import amodule.dish.view.DishExplainView;
import amodule.dish.view.DishHeaderViewNew;
import amodule.dish.view.DishHoverViewControl;
import amodule.dish.view.DishIngreDataShow;
import amodule.dish.view.DishRecommedAndAdView;
import amodule.dish.view.DishTitleViewControl;
import amodule.dish.view.DishTitleViewControlNew;

/**
 * 当前只处理View的拼装
 * 不能牵扯如何业务逻辑处理----因为当前页面业务确定，采用直接数据指向方法（不抽象不模糊）
 */
public class DetailDishViewManager {
    public static int showNumLookImage = 0;//点击展示次数
    public DishTitleViewControl dishTitleViewControl;
    public DishHoverViewControl dishHoverViewControl;
    public LinearLayout layoutHeader;
    public LinearLayout layoutFooter;
    public Activity mAct;
    //广告所用bar高度;图片/视频高度
    private int statusBarHeight = 0, headerLayoutHeight;
    private int titleHeight;//标题高度
    //头部信息
    public DishHeaderViewNew dishHeaderViewNew;
    public DishAboutView dishAboutView;
    public DishIngreDataShow dishIngreDataShow;
    public DishRecommedAndAdView dishRecommedAndAdView;
    public DishExplainView dishExplainView;

    /**
     * 对view进行基础初始化
     */
    public DetailDishViewManager(Activity activity, ListView listView,String state) {
        mAct = activity;
        dishTitleViewControl = new DishTitleViewControl(activity);
        dishTitleViewControl.initView(activity);
        dishTitleViewControl.setstate(state);

        dishHoverViewControl = new DishHoverViewControl(activity);
        dishHoverViewControl.initView();

        if (layoutHeader == null) {
            layoutHeader = new LinearLayout(activity);
            layoutHeader.setOrientation(LinearLayout.VERTICAL);
            layoutFooter = new LinearLayout(activity);
            layoutFooter.setOrientation(LinearLayout.VERTICAL);
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
        //foot
        dishExplainView = new DishExplainView(mAct);
        dishExplainView.setVisibility(View.GONE);
        dishRecommedAndAdView= new DishRecommedAndAdView(mAct);
        dishRecommedAndAdView.setVisibility(View.GONE);
        layoutFooter.addView(dishExplainView);
        layoutFooter.addView(dishRecommedAndAdView);

        listView.addHeaderView(layoutHeader);
        listView.addFooterView(layoutFooter);
        listView.setVisibility(View.VISIBLE);

    }
    /**
     * 处理标题信息数据
     */
    public void handlerTitle(Map<String, String> dishInfoMaps,String code,boolean isHasVideo,String dishState,LoadManager loadManager,String state) {
        if(dishTitleViewControl!=null){
            dishTitleViewControl.setData(dishInfoMaps,code,isHasVideo,dishState,loadManager);
            dishTitleViewControl.setstate(state);
            dishTitleViewControl.setViewState();
        }
    }
    public void handlerTitleName(String name){
        Log.i("xianghaTag","name:::"+name);
        if(dishTitleViewControl!=null){
            dishTitleViewControl.setNickName(name);
        }
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
    public void handlerExplainView(ArrayList<Map<String, String>> list) {
        if(dishExplainView!=null){
            dishExplainView.setVisibility(View.VISIBLE);
            dishExplainView.setData(list.get(0));
        }
    }

    /**
     * 处理底部推荐
     * @param list
     */
    public void handlerRecommedAndAd(ArrayList<Map<String, String>> list,String code,String name){
        if(dishRecommedAndAdView!=null){
            dishRecommedAndAdView.setVisibility(View.VISIBLE);
            dishRecommedAndAdView.initData(code,name);
            dishRecommedAndAdView.initUserDish(list);
        }
    }
    /**
     * 处理浮动推荐
     */
    public void handlerHoverViewCode(String code){
        if(dishHoverViewControl!=null){
            dishHoverViewControl.setCode(code);
        }
    }
    /**
     * 处理浮动推荐
     */
    public void handlerHoverViewLike(ArrayList<Map<String, String>> list){
        if(dishHoverViewControl!=null){
            dishHoverViewControl.initLikeState(list);
        }
    }
}
