package amodule.lesson.controler.view;

import android.view.View;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import amodule._common.plugin.WidgetVerticalLayout;

/**
 * Description :
 * PackageName : amodule.lesson.controler.view
 * Created by mrtrying on 2017/12/19 12:48:17.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeHeaderControler {

    private View mHeader;

    private WidgetVerticalLayout[] mLayouts = new WidgetVerticalLayout[4];

    LessonHomeHeaderControler(View header){
        this.mHeader = header;

        //banner
        mLayouts[0] = (WidgetVerticalLayout) header.findViewById(R.id.banner_widget);
        //横向滑动
        mLayouts[1] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal1_widget);
        mLayouts[2] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal2_widget);
        mLayouts[3] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal3_widget);
    }

    public void setData(List<Map<String, String>> array) {
        if (null == array || array.isEmpty()) return;
//        String[] twoLevelArray = {"轮播banner", "功能入口", "功能入口", "精品厨艺", "限时抢购", "精选菜单"};
//        String[] threeLevelArray = {"轮播banner位置", "", "", "精品厨艺位置", "限时抢购位置", "精选菜单位置"};
//        setVisibility(false);
        final int length = Math.min(array.size(), mLayouts.length);
        for (int index = 0; index < length; index++) {
            Map<String, String> map = array.get(index);
            mLayouts[index].setData(map);
            mLayouts[index].setVisibility(View.VISIBLE);
        }
    }
}
