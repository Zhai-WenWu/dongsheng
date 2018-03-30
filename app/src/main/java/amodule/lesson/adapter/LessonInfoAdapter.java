package amodule.lesson.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * Description :
 * PackageName : amodule.vip.adapter
 * Created by tanze on 2018/3/29 17:08.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoAdapter extends RvBaseAdapter<Map<String,String>> {

    public LessonInfoAdapter(Context context, @Nullable List<Map<String,String>> data) {
        super(context, data);
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
