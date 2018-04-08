package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule.user.view.module.ModuleItemS0View;

import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_DATA;

/**
 * Description :
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/30 11:00.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonModuleView extends LessonParentLayout {

    String mTitleText = "";
    //    List<Map<String,String>> mDatas = new ArrayList<>();
    boolean isOnce = true;
    private boolean isUseDefaultBottomPadding = false;

    public LessonModuleView(Context context) {
        super(context);
    }

    public LessonModuleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LessonModuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> data) {
        isOnce = true;
        Map<String, String> widgetDataMap = StringManager.getFirstMap(data.get(KEY_WIDGET_DATA));
        mTitleText = widgetDataMap.get("text1");
        Log.i("tzy", "setData: " + mTitleText);
//        mDatas = StringManager.getListMapByJson(widgetDataMap.get("data"));
        super.setData(data);
    }

    @Override
    protected boolean showInnerNextItem() {
        if (isOnce) {
            isOnce = false;
            ItemTitle title = new ItemTitle(getContext());
            title.setTitle(mTitleText);
            addView(title);
        }
        for (; !mDatas.isEmpty();) {
            if (mDatas.isEmpty()) {
                return false;
            }
            Map<String, String> data = mDatas.remove(0);
            if (data != null) {
                ModuleItemS0View moduleItemS0View = new ModuleItemS0View(getContext());
                moduleItemS0View.setUseDefaultBottomPadding(isUseDefaultBottomPadding);
                moduleItemS0View.initData(data);
                addView(moduleItemS0View);
            }
        }
        return !mDatas.isEmpty();
    }

    public void setUseDefaultBottomPadding(boolean useDefaultBottomPadding) {
        isUseDefaultBottomPadding = useDefaultBottomPadding;
    }
}
