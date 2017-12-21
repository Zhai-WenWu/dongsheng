package amodule.lesson.controler.view;

import android.view.View;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import amodule._common.delegate.StatisticCallback;
import amodule._common.plugin.WidgetVerticalLayout;

/**
 * Description :
 * PackageName : amodule.lesson.controler.view
 * Created by mrtrying on 2017/12/19 12:48:17.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeHeaderControler {

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
        String[] ids = {"vip_homepage_banner", "vip_recommend", "vip_chief_list"};
        String[] twoLevelArray = {"banner", "最新推荐-", "VIP名厨-"};
        final int length = Math.min(array.size(), mLayouts.length);
        for (int index = 0; index < length; index++) {
            Map<String, String> map = array.get(index);
            final String ID = LoginManager.isVIP() || LoginManager.isTempVip() ? ids[index] : "non" + ids[index];
            if (index == 1 || index == 2) {
                final int i = index;
                mLayouts[index].setTitleStaticCallback(
                        (id, twoLevel, threeLevel, position) -> XHClick.mapStat(mLayouts[i].getContext(), ids[i], "查看更多", "")
                );
            }
            mLayouts[index].setStatictusData(ID, twoLevelArray[index], "");
            mLayouts[index].setData(map);
            mLayouts[index].setVisibility(View.VISIBLE);
        }
    }
}
