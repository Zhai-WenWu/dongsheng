package amodule._common.plugin;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule._common.widgetlib.AllWeightLibrary;
import amodule._common.delegate.IBIndString;

import static amodule._common.helper.WidgetDataHelper.KEY_DATA;
import static amodule._common.helper.WidgetDataHelper.KEY_EXTRA;
import static amodule._common.helper.WidgetDataHelper.KEY_TYPE;

/**
 * PackageName : amodule._common.plugin
 * Created by MrTrying on 2017/11/10 19:20.
 * E_mail : ztanzeyu@gmail.com
 */

public class WidgetVerticalLayout extends AbsWidgetVerticalLayout<Map<String,String>> {

    public WidgetVerticalLayout(Context context) {
        super(context);
    }

    public WidgetVerticalLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetVerticalLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> data) {
        if(null == data || data.isEmpty()) return;
        String widgetType = data.get(KEY_TYPE);
        if(!TextUtils.isEmpty(widgetType)){
            final int viewId = AllWeightLibrary.of().findWidgetViewID(widgetType);
            View view = findViewById(viewId);
            if(view instanceof IBIndString){
                ((IBIndString)view).setData(data.get(KEY_DATA));
            }
        }
        String widgetExtra = data.get(KEY_EXTRA);
        if(TextUtils.isEmpty(widgetExtra)){
            return;
        }
        Map<String,String> widgetExtraMap = StringManager.getFirstMap(widgetExtra);
        if(widgetExtraMap.isEmpty()){
            return;
        }
        addTopView(StringManager.getListMapByJson(widgetExtra));
    }

    @Override
    public void addTopView(List<Map<String, String>> array) {

    }

    @Override
    public void addBottom(List<Map<String, String>> array) {

    }

}
