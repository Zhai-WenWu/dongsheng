package amodule.lesson.controler.view;

import android.view.View;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.StatisticCallback;
import amodule._common.plugin.WidgetVerticalLayout;

/**
 * Description :
 * PackageName : amodule.lesson.controler.view
 * Created by mrtrying on 2017/12/19 12:48:17.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeHeaderControler implements ISaveStatistic {

    private View mHeader;

    private WidgetVerticalLayout[] mLayouts = new WidgetVerticalLayout[3];

    LessonHomeHeaderControler(View header) {
        this.mHeader = header;

        //banner
        mLayouts[0] = (WidgetVerticalLayout) header.findViewById(R.id.banner_widget);
        //横向滑动
        mLayouts[1] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal1_widget);
        mLayouts[2] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal2_widget);
    }

    public void setData(List<Map<String, String>> array) {
        if (null == array || array.isEmpty()) return;
        String[] ids = {"vip_homepage", "vip_homepage", "vip_homepage"};
        String[] twoLevelArray = {"会员首页banner", "VIP最新推荐", "VIP特邀名厨"};
        String[] threeLevelArray = {"banner", "VIP最新推荐-", "VIP特邀名厨-"};
        final int length = Math.min(array.size(), mLayouts.length);
        for (int index = 0; index < length; index++) {
            Map<String, String> map = array.get(index);
            final String ID = LoginManager.isVIP() || LoginManager.isTempVip() ? ids[index] : "non" + ids[index];
            final int i = index;
            if (index == 1 || index == 2) {
                mLayouts[index].setTitleStaticCallback(
                        (id, twoLevel, threeLevel, position) -> XHClick.mapStat(mLayouts[i].getContext(), ids[i], "查看更多", "")
                );
            }
            mLayouts[index].setStatisticCallback(
                    (id, itemTwoLevel, itemThreeLevel, position) -> XHClick.mapStat(mLayouts[i].getContext(), id, itemTwoLevel,  itemThreeLevel + position)
            );
            mLayouts[index].setStatisticPage("VipHome");
            mLayouts[index].setStatictusData(ID, twoLevelArray[index], threeLevelArray[index]);
            mLayouts[index].setData(map);
            mLayouts[index].setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void saveStatisticData(String page) {
        for (WidgetVerticalLayout layout : mLayouts) {
            layout.saveStatisticData(page);
        }
    }
}
