package acore.logic.stat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import acore.widget.rvlistview.holder.RvBaseViewHolder;

import static acore.logic.stat.StatConf.STAT_TAG;

/**
 * Description :
 * PackageName : acore.logic.statistics
 * Created by mrtrying on 2018/8/1 18:48.
 * e_mail : ztanzeyu@gmail.com
 */
public abstract class RvBaseViewHolderStat<T> extends RvBaseViewHolder<T> {
    protected String p, m, f1;
    public RvBaseViewHolderStat(@NonNull View itemView, View parent) {
        super(itemView);
        this.p = itemView.getContext().getClass().getSimpleName();
        this.m = getValueByView(parent);
    }
    public RvBaseViewHolderStat(@NonNull View itemView, String m) {
        super(itemView);
        this.p = itemView.getContext().getClass().getSimpleName();
        this.m = m;
    }
    public RvBaseViewHolderStat(@NonNull View itemView, String m, String f1) {
        super(itemView);
        this.p = itemView.getContext().getClass().getSimpleName();
        this.m = m;
        this.f1 = f1;
    }

    @Override
    public void bindData(int position, @Nullable T data) {
        overrideBindData(position, data);
        statData(position, data);
    }

    protected void statData(int position, @Nullable T data) {
        if (!isShown(data)) {
            hasShown(data);
            onPreStat(itemView);
            onStat(position,data);
        }
    }

    protected void onPreStat(View v) {
        //获取相应参数
        if (v != null && TextUtils.isEmpty(p)) {
            this.p = v.getContext().getClass().getSimpleName();
        }
        if (v != null && v.getParent() != null && TextUtils.isEmpty(m)) {
            this.m = getValueByView((View) v.getParent());
            if(TextUtils.isEmpty(m)){
                this.m = this.p;
            }
        }
    }

    protected void onStat(int position, T data) {
        if (!TextUtils.isEmpty(p) && canStat()) {
            StatisticsManager.listShow(p, m,  String.valueOf(position + 1), f1, getStatJson(data));
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

    public boolean canStat(){
        return true;
    }

    public abstract boolean isShown(T data);
    public abstract void hasShown(T data);
    public abstract String getStatJson(T data);

    public abstract void overrideBindData(int position, @Nullable T data);
}
