package acore.logic.stat;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import java.util.Map;

import static acore.logic.stat.StatisticsManager.IS_STAT;
import static acore.logic.stat.StatisticsManager.STAT_DATA;
import static acore.logic.stat.StatisticsManager.TRUE_VALUE;

/**
 * Description :
 * PackageName : acore.logic.statistics
 * Created by mrtrying on 2018/8/1 18:48.
 * e_mail : ztanzeyu@gmail.com
 */
public abstract class RvMapViewHolderStat extends RvBaseViewHolderStat<Map<String,String>> {

    public RvMapViewHolderStat(@NonNull View itemView, View parent) {
        super(itemView, parent);
    }

    public RvMapViewHolderStat(@NonNull View itemView, String m) {
        super(itemView, m);
    }

    public RvMapViewHolderStat(@NonNull View itemView, String m, String f1) {
        super(itemView, m, f1);
    }

    public boolean canStat(){
        return true;
    }

    @Override
    public boolean isShown(Map<String,String> data) {
        return TextUtils.equals(TRUE_VALUE,data.get(IS_STAT));
    }

    @Override
    public void hasShown(Map<String,String> data) {
        data.put(IS_STAT,TRUE_VALUE);
    }

    @Override
    public String getStatJson(Map<String, String> data) {
        return data.get(STAT_DATA);
    }
}
