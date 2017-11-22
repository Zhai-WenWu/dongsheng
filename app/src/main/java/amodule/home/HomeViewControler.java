package amodule.home;

import android.annotation.SuppressLint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule.home.view.HomeTitleLayout;
import amodule.main.Tools.BuoyControler;
import amodule.main.activity.MainHome;
import amodule.main.activity.MainHomePage;
import amodule.main.view.item.HomeItem;
import third.umeng.OnlineConfigControler;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:00.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeViewControler {

    static String MODULETOPTYPE = "moduleTopType";//置顶数据的类型

    private HomeHeaderControler mHeaderControler;
    private HomeFeedHeaderControler mHomeFeedHeaderControler;

    private MainHomePage mActivity;

    private BuoyControler.Buoy mBuoy;

    private RvListView mRvListView;

    //feed头部view
    private View mHeaderView;

    private boolean isScrollData = false;//是否滚动数据
    private int scrollDataIndex = -1;//滚动数据的位置

    public HomeViewControler(MainHomePage activity) {
        this.mActivity = activity;
        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_home_header_layout, null, true);
    }

    public void onCreate(){
        initUI();
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        mHeaderControler = new HomeHeaderControler(mHeaderView);

        mHomeFeedHeaderControler = new HomeFeedHeaderControler(mActivity);

        HomeTitleLayout titleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        titleLayout.setStatictusData(MainHomePage.STATICTUS_ID_PULISH,"顶部topbar","");
        titleLayout.postDelayed(()->{
            mBuoy = new BuoyControler.Buoy(mActivity,BuoyControler.TYPE_HOME);
            mBuoy.setClickCallback(() -> XHClick.mapStat(mActivity,MainHomePage.STATICTUS_ID_PULISH,"首页右侧侧边栏浮动图标",""));
        },4000);

        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
        mRvListView.addHeaderView(mHomeFeedHeaderControler.getLayout());
        mRvListView.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem) view).onClickEvent(view);
            }
        });

        //设置活动icon点击
        titleLayout.setOnClickActivityIconListener((v, url) -> {
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
                    if(RecyclerView.SCROLL_STATE_IDLE == newState){
                        if(mBuoy != null && !mBuoy.isMove())
                            mBuoy.executeOpenAnim();
                    }
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
        long startTime = System.currentTimeMillis();
        if(data == null || data.isEmpty()){
            mHeaderControler.setVisibility(false);
            return;
        }
        Stream.of(data).forEach(map -> {
            Map<String,String> temp = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_WIDGET_DATA));
            map.put("cache","2".equals(temp.get("isCache"))?"2":"1");
        });
        Log.i("tzy","setHeaderData handler data time = " + (System.currentTimeMillis() - startTime) + "ms");
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
        mHeaderControler.setFeedheaderVisibility(isShow);
    }

    public void setFeedTitleText(String text){
        mHeaderControler.setFeedTitleText(text);
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
            scrollDataIndex = -1;
        }
    }

    private TextView mTipMessage;
    public void setTipMessage(){
        OnlineConfigControler.getInstance().getConfigByKey(
                OnlineConfigControler.KEY_HOMENOTICE,
                value -> initTipMessage(value)
        );
    }

    private void initTipMessage(String configData){
        Map<String, String> data = StringManager.getFirstMap(configData);
        if(null == data || data.isEmpty() || !"2".equals(data.get("isShow"))){
            if(null != mTipMessage){
                mTipMessage.setVisibility(View.GONE);
            }
            return;
        }
        if(null == mTipMessage){
            mTipMessage = new TextView(mActivity);
            mTipMessage.setGravity(Gravity.CENTER);
            mTipMessage.setTextSize(16);
            int padding = Tools.getDimen(mActivity,R.dimen.dp_20);
            mTipMessage.setPadding(padding,padding,padding,padding);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW,R.id.home_title);
            mTipMessage.setLayoutParams(layoutParams);
        }
        //获取文本
        String textValue = data.get("text");
        if(TextUtils.isEmpty(textValue)){
            return;
        }
        mTipMessage.setText(textValue);
        //设置背景颜色
        String bgColorValue = data.get("backColor");
        mTipMessage.setBackgroundColor(WidgetUtility.parseColor(bgColorValue));
        //设置文本颜色
        String textColorValue = data.get("textColor");
        mTipMessage.setTextColor(WidgetUtility.parseColor(textColorValue));
        if(null != mActivity && null != mActivity.rl){
            if(mActivity.rl.indexOfChild(mTipMessage) < 0)
                mActivity.rl.addView(mTipMessage);
        }
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public RvListView getRvListView() {
        return mRvListView;
    }

}
