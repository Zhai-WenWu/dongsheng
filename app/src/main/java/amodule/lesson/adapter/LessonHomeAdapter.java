package amodule.lesson.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule._common.widget.horizontal.HorizontalRecyclerView;

/**
 * Description :
 * PackageName : amodule.lesson.adapter
 * Created by mrtrying on 2017/12/19 11:26:54.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeAdapter extends RvBaseAdapter<Map<String,String>> {
    public LessonHomeAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LessonViewHolder(new HorizontalRecyclerView(mContext));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class LessonViewHolder extends RvBaseViewHolder<Map<String,String>>{
        HorizontalRecyclerView view;

        public LessonViewHolder(@NonNull HorizontalRecyclerView itemView) {
            super(itemView);
            this.view = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            view.setData(data);
        }
    }
}
