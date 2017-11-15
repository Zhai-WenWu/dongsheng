package amodule._common.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import amodule._common.helper.WidgetDataHelper;
import amodule.home.view.HomeFuncNavView2;
import amodule._common.delegate.IBindMap;

/**
 * Description : //TODO
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/14 10:19.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class FuncNavView2 extends HomeFuncNavView2 implements IBindMap{
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
        setVisibility(GONE);
    }

    @Override
    public void setData(Map<String, String> data) {
//        Log.i("tzy","data = " + data);
        if (null == data || data.isEmpty()){
            setVisibility(GONE);
            return;
        }

        Map<String,String> dataMap = StringManager.getFirstMap(data.get("data"));
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dataMap.get("list"));
        if (arrayList.isEmpty()){
            setVisibility(GONE);
            return;
        }

        //设置左侧数据
        Map<String,String> map = arrayList.get(0);
        WidgetDataHelper.setTextToView(getTextView(R.id.text_left_1),map.get("text1"));
        WidgetDataHelper.setTextToView(getTextView(R.id.text_left_2),map.get("text2"));
        ImageView leftIcon = getImageView(R.id.icon_left_1);
        if(leftIcon != null){
            Glide.with(getContext()).load(map.get("img")).into(leftIcon);
        }
        String leftUrl = map.get("url");
        mLeftView.setOnClickListener(v ->
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),leftUrl,true)
        );

        //设置右侧数据
        if(arrayList.size() > 1){
            map = arrayList.get(1);
            WidgetDataHelper.setTextToView(getTextView(R.id.text_right_1),map.get("text1"));
            WidgetDataHelper.setTextToView(getTextView(R.id.text_right_2),map.get("text2"));
            ImageView rightIcon = getImageView(R.id.icon_right_1);
            if(rightIcon != null){
                Glide.with(getContext()).load(map.get("img")).into(rightIcon);
            }
            String rightUrl = map.get("url");
            mLeftView.setOnClickListener(v ->
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),rightUrl,true)
            );
        }

        setVisibility(VISIBLE);
    }
}
