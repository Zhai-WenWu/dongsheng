package acore.logic.stat.intefaces;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import acore.logic.stat.StatisticsManager;
import acore.override.XHApplication;
import acore.tools.Tools;

import static acore.logic.stat.StatConf.STAT_TAG;

/**
 * Description :
 * PackageName : acore.logic.stat
 * Created by tanzeyu on 2018/9/25 11:33.
 * e_mail : ztanzeyu@gmail.com
 */
public abstract class OnClickListenerStat implements View.OnClickListener, OnClickStatCallback {

    protected String p, m, viewName;

    public OnClickListenerStat(){}

    public OnClickListenerStat(String m) {
        this.m = m;
    }

    public OnClickListenerStat(View moduleView) {
        this.m = getValueByView(moduleView);
    }

    public OnClickListenerStat(Context context, String m, String viewName) {
        if (context != null) {
            this.p = context.getClass().getSimpleName();
        }
        this.m = m;
        this.viewName = viewName;
    }

    @Override
    public void onClick(View v) {
        //点击事件
        onClicked(v);
        //统计
        onStat(v);
    }

    protected void onStat(View v) {
        //开启debug模式
        if (Tools.isDebug(XHApplication.in())) {
            debug();
        }
        //获取相应参数
        if (v != null && TextUtils.isEmpty(p)) {
            this.p = v.getContext().getClass().getSimpleName();
        }
        if (v != null && v.getParent() != null && TextUtils.isEmpty(m)) {
            this.m = getValueByView((View) v.getParent());
        }
        if (!TextUtils.isEmpty(p) && canStat()) {
            StatisticsManager.btnClick(p, m, getValueByView(v));
        }
    }

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
