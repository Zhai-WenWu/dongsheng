package amodule._common.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IHandlerClickEvent;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.IStatictusData;
import amodule._common.utility.WidgetUtility;
import amodule.home.view.HomeFuncNavView2;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/14 10:19.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class FuncNavView2 extends HomeFuncNavView2 implements IBindMap, IStatictusData,ISaveStatistic,IHandlerClickEvent {
    public FuncNavView2(Context context) {
        super(context);
    }

    public FuncNavView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FuncNavView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void setData(Map<String, String> data) {
//        Log.i("tzy","data = " + data);
        if (null == data || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        Map<String, String> dataMap = StringManager.getFirstMap(data.get("data"));
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dataMap.get("list"));
        if (arrayList.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        //设置左侧数据
        final Map<String, String> leftMap = arrayList.get(0);
        WidgetUtility.setTextToView(getTextView(R.id.text_left_1), leftMap.get("text1"));
        WidgetUtility.setTextToView(getTextView(R.id.text_left_2), leftMap.get("text2"));
        ImageView leftIcon = getImageView(R.id.icon_left_1);
        if (leftIcon != null) {
            if(!TextUtils.isEmpty(leftMap.get("img")))
                Glide.with(getContext()).load(leftMap.get("img")).into(leftIcon);
            String leftUrl = leftMap.get("url");
            mLeftView.setOnClickListener(v -> {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), leftUrl, true);
                if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)) {
                    XHClick.mapStat(getContext(), id, twoLevel, leftMap.get("text1"));
                }
            });
        }

        //设置右侧数据
        if (arrayList.size() > 1) {
            final Map<String, String> rightMap = arrayList.get(1);
            WidgetUtility.setTextToView(getTextView(R.id.text_right_1), rightMap.get("text1"));
            WidgetUtility.setTextToView(getTextView(R.id.text_right_2), rightMap.get("text2"));
            ImageView rightIcon = getImageView(R.id.icon_right_1);
            if (rightIcon != null) {
                if(!TextUtils.isEmpty(rightMap.get("img")))
                    Glide.with(getContext()).load(rightMap.get("img")).into(rightIcon);
                String rightUrl = rightMap.get("url");
                mRightView.setOnClickListener(v -> {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), rightUrl, true);
                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)) {
                        XHClick.mapStat(getContext(), id, twoLevel, rightMap.get("text1"));
                    }
                });
            }
        }

        setVisibility(VISIBLE);
    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }

    @Override
    public void saveStatisticData() {

    }

    @Override
    public boolean handlerClickEvent(String url, String moduleType, String dataType, int position) {
        return false;
    }
}
