package amodule.home;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.Tools;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.StatisticCallback;
import amodule._common.plugin.WidgetVerticalLayout;
import amodule._common.utility.WidgetUtility;
import amodule.main.activity.MainHomePage;
import static third.ad.tools.AdPlayIdConfig.HOME_BANNEER_1;
/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 21:47.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeHeaderControler implements ISaveStatistic {

    private View mHeaderView, mFeedHeaderView,mLine;

    private LinearLayout mFeedLayout;

    private TextView mFeedTitle;

    private WidgetVerticalLayout[] mLayouts = new WidgetVerticalLayout[6];

    private View.OnLayoutChangeListener onLayoutChangeListener;

    private boolean hasFeedData = false;
    private boolean hasHeaderData = false;

    HomeHeaderControler(View header) {
        this.mHeaderView = header;
        //banner
        mLayouts[0] = (WidgetVerticalLayout) header.findViewById(R.id.banner_widget);
        mLayouts[0].setAdID(Arrays.asList(HOME_BANNEER_1));
        //功能导航 4按钮
        mLayouts[1] = (WidgetVerticalLayout) header.findViewById(R.id.funcnav1_widget);
        //功能导航 2按钮
        mLayouts[2] = (WidgetVerticalLayout) header.findViewById(R.id.funcnav2_widget);
        //横向滑动
        mLayouts[3] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal1_widget);
        mLayouts[4] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal2_widget);
        mLayouts[5] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal3_widget);

        mFeedHeaderView = header.findViewById(R.id.a_home_feed_title);
        mFeedTitle = (TextView) header.findViewById(R.id.feed_title);
        mLine = header.findViewById(R.id.line);
        mFeedLayout = (LinearLayout) header.findViewById(R.id.feed_title_layout);
    }

    public void setData(List<Map<String, String>> array, boolean isShowCache) {
        if (null == array || array.isEmpty()) return;
        String[] twoLevelArray = {"轮播banner", "功能入口", "功能入口", "精品厨艺", "限时抢购", "精选菜单"};
        String[] threeLevelArray = {"轮播banner位置", "", "", "精品厨艺位置", "限时抢购位置", "精选菜单位置"};
//        setVisibility(false);
        final int length = Math.min(array.size(), mLayouts.length);
        for (int i = 0; i < length; i++) {
            final int index = i;
            Map<String, String> map = array.get(index);
            if (isShowCache && "1".equals(map.get("cache"))) {
                mLayouts[index].setVisibility(View.GONE);
                continue;
            }
            mLayouts[index].setStatisticPage("home");
            mLayouts[index].setData(map);
            mLayouts[index].setStatictusData(MainHomePage.STATICTUS_ID_HOMEPAGE, twoLevelArray[index], threeLevelArray[index]);
            StatisticCallback statisticCallback = (id, twoLevel, threeLevel, position) -> {
                if(!TextUtils.isEmpty(id)&&!TextUtils.isEmpty(twoLevel)&&!TextUtils.isEmpty(threeLevel)){
                    XHClick.mapStat(mLayouts[index].getContext(),id,twoLevel,threeLevel);
                }
            };
            mLayouts[index].setStatisticCallback(statisticCallback);
            mLayouts[index].setTitleStaticCallback(statisticCallback);
            mLayouts[index].setVisibility(View.VISIBLE);
        }

        if(onLayoutChangeListener == null){
            onLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                hasHeaderData = mHeaderView.getHeight() > mFeedHeaderView.getHeight();
                int needVisibility = (hasFeedData && hasHeaderData) ? View.VISIBLE : View.GONE;
                if(needVisibility != mFeedHeaderView.getVisibility()){
                    mFeedHeaderView.setVisibility(needVisibility);
                    mLine.setVisibility(needVisibility);
                    mFeedLayout.setVisibility(needVisibility);
                }
            };
            mHeaderView.addOnLayoutChangeListener(onLayoutChangeListener);
        }
    }

    public void setVisibility(boolean isShow) {
        for (WidgetVerticalLayout itemLayout : mLayouts) {
            itemLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void saveStatisticData(String page) {
        for (WidgetVerticalLayout layout : mLayouts) {
            layout.saveStatisticData(page);
        }
    }

    void setFeedheaderVisibility(boolean hasFeedData) {
        this.hasFeedData = hasFeedData;
        mFeedHeaderView.setVisibility((hasFeedData && hasHeaderData) ? View.VISIBLE : View.GONE);
    }

    void setFeedTitleText(String text) {
        WidgetUtility.setTextToView(mFeedTitle, text);
    }

}
