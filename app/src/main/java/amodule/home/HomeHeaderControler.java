package amodule.home;

import android.util.Log;
import android.view.View;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import amodule._common.plugin.WidgetVerticalLayout;

/**
 * Description : //TODO
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 21:47.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeHeaderControler {

    private View mHeaderView;

    WidgetVerticalLayout[] mLayouts = new WidgetVerticalLayout[6];

    public HomeHeaderControler(View header){
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

    }

    public void setData(List<Map<String,String>> array,boolean isShowCache){
        if(null == array || array.isEmpty()) return;
        final int length = Math.min(array.size(),mLayouts.length);
        for(int index = 0 ; index < length ; index ++){
            Map<String,String> map = array.get(index);
            if(isShowCache && "1".equals(map.get("isCache"))){
                mLayouts[index].setVisibility(View.GONE);
                continue;
            }
            mLayouts[index].setData(map);
            mLayouts[index].setVisibility(View.VISIBLE);
        }
    }
}
