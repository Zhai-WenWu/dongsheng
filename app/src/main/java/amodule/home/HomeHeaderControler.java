package amodule.home;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import amodule._common.delegate.ISaveStatistic;
import amodule._common.plugin.WidgetVerticalLayout;
import amodule._common.utility.WidgetUtility;
import amodule.main.activity.MainHomePage;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 21:47.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeHeaderControler implements ISaveStatistic {

    private View mHeaderView,mFeedHeaderView;

    private TextView mFeedTitle;

    private WidgetVerticalLayout[] mLayouts = new WidgetVerticalLayout[6];

    HomeHeaderControler(View header){
        this.mHeaderView = header;
        //banner
        mLayouts[0] = (WidgetVerticalLayout) header.findViewById(R.id.banner_widget);
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
    }

    public void setData(List<Map<String,String>> array,boolean isShowCache){
        if(null == array || array.isEmpty()) return;
        String[] twoLevelArray = {"轮播banner","功能入口","功能入口","精品厨艺","限时抢购","精选菜单"};
        String[] threeLevelArray = {"轮播banner位置","","","精品厨艺位置","限时抢购位置","精选菜单位置"};
        setVisibility(true);
        final int length = Math.min(array.size(),mLayouts.length);
        for(int index = 0 ; index < length ; index ++){
            Map<String,String> map = array.get(index);
            if(isShowCache && "1".equals(map.get("cache"))){
                mLayouts[index].setVisibility(View.GONE);
                continue;
            }
            mLayouts[index].setData(map);
            mLayouts[index].setStatictusData(MainHomePage.STATICTUS_ID_HOMEPAGE,twoLevelArray[index],threeLevelArray[index]);
            mLayouts[index].setVisibility(View.VISIBLE);
        }
    }

    public void setVisibility(boolean isShow) {
        for(WidgetVerticalLayout itemLayout:mLayouts){
            itemLayout.setVisibility(isShow?View.VISIBLE:View.GONE);
        }
    }

    @Override
    public void saveStatisticData() {
        for(WidgetVerticalLayout layout:mLayouts)
            layout.saveStatisticData();
    }

    void setFeedheaderVisibility(boolean feedheaderVisibility) {
        mFeedHeaderView.setVisibility(feedheaderVisibility ? View.VISIBLE : View.GONE);
    }

    void setFeedTitleText(String text){
        if(null == mFeedTitle || TextUtils.isEmpty(text)){
            return;
        }
        mFeedTitle.setText(text);
    }
}
