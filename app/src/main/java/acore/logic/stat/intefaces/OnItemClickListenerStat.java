package acore.logic.stat.intefaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import acore.logic.stat.StatisticsManager;
import acore.override.XHApplication;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;

import static acore.logic.stat.StatConf.STAT_TAG;

/**
 * Description :
 * PackageName : acore.logic.stat.intefaces
 * Created by tanzeyu on 2018/9/25 11:37.
 * e_mail : ztanzeyu@gmail.com
 */
public abstract class OnItemClickListenerStat implements AdapterView.OnItemClickListener,OnItemClickListenerStatCallback {

    //页面名，模块名称，按钮名称
    protected String p, m, viewName;

    public OnItemClickListenerStat(){}

    public OnItemClickListenerStat(String m) {
        this.m = m;
    }

    public OnItemClickListenerStat(View moduleView) {
        this.m = getValueByView(moduleView);
    }

    public OnItemClickListenerStat(Context context, String m, String viewName) {
        if (context != null) {
            this.p = context.getClass().getSimpleName();
        }
        this.m = m;
        this.viewName = viewName;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onItemClicked(parent,view,position,id);
        //统计
        onInnerStat(parent,view,position,getStatData(position));
    }

    protected void onInnerStat(AdapterView<?> parent,View v,int position,String statJsonStr) {
        //开启debug模式
        if (Tools.isDebug(XHApplication.in())) {
            debug();
        }
        onPreStat(parent, v);
        if (!TextUtils.isEmpty(p) && canStat()) {
            onStat(position, statJsonStr);
        }
    }

    protected void onStat(int position, String statJsonStr) {
        StatisticsManager.listClick(p, m, String.valueOf(position + 1), "",statJsonStr);
    }

    protected void onPreStat(AdapterView<?> parent, View v) {
        //获取相应参数
        if (v != null && TextUtils.isEmpty(p)) {
            this.p = v.getContext().getClass().getSimpleName();
        }
        if (parent != null && TextUtils.isEmpty(m)) {
            this.m = getValueByView(parent);
        }
    }

    protected abstract String getStatData(int position);

    protected String getValueByView(View v) {
        String value = "";
        if (v != null) {
            if (v.getTag(STAT_TAG) != null) {
                value = (String) v.getTag(STAT_TAG);
            }
            if (TextUtils.isEmpty(value)
                    && !(v instanceof ImageView) && v.getTag() != null) {
                value = (String) v.getTag();
            }
            if (TextUtils.isEmpty(value) && v instanceof TextView) {
                value = ((TextView) v).getText().toString();
            }
        }
        return value;
    }

    /** 调试 */
    protected void debug() {
//        Log.i(StatisticsManager.TAG, "debug: " + toString());
    }

    protected boolean canStat() {
        return true;
    }
}
