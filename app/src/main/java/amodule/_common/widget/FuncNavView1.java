package amodule._common.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
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
import amodule.home.view.HomeFuncNavView1;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/14 11:40.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class FuncNavView1 extends HomeFuncNavView1 implements IBindMap,IStatictusData,ISaveStatistic,IHandlerClickEvent {
    public FuncNavView1(Context context) {
        super(context);
    }

    public FuncNavView1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FuncNavView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        setDataToView(arrayList);
    }

    protected void setDataToView(List<Map<String,String>> data){
        if(null == data || data.isEmpty()){
            setVisibility(GONE);
            return;
        }
        final int length = Math.min(navIds.size(),data.size());
        for(int index = 0 ; index < length ; index ++){
            View navView = findViewById(navIds.get(index));
            setMapToView(navView,data.get(index));
        }
        setVisibility(VISIBLE);
    }

    private boolean setMapToView(View itemView,Map<String,String> data){
        if(null == itemView || null == data || data.isEmpty()) return false;

        TextView textView = (TextView) itemView.findViewById(R.id.text_1);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.icon);

        WidgetUtility.setTextToView(textView,data.get("text1"),false);
        if(null != imageView){
            if(!TextUtils.isEmpty(data.get("img")))
                Glide.with(getContext()).load(data.get("img")).into(imageView);
            imageView.setOnClickListener(v->{
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),data.get("url"),true);
                if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                    XHClick.mapStat(getContext(),id,twoLevel,data.get("text1"));
                }
            });
        }
        return true;
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
