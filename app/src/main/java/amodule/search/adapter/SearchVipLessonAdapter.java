package amodule.search.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import acore.logic.stat.RvMapViewHolderStat;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * Description :
 * PackageName : amodule.search.adapter
 * Created by mrtrying on 2018/11/22 11:28.
 * e_mail : ztanzeyu@gmail.com
 */
public class SearchVipLessonAdapter extends RvBaseAdapter<Map<String,String>> {

    @Override
    public void overrideBindData(int position, @Nullable Map<String, String> data) {

    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
