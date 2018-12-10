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
import amodule.lesson.view.ItemCourseIntroduce;

/**
 * Description :
 * PackageName : amodule.lesson.adapter
 * Created by mrtrying on 2018/12/4 18:20.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroductionAdapter extends RvBaseAdapter<Map<String,String>> {
    public CourseIntroductionAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCourseIntroduce itemView = new ItemCourseIntroduce(mContext);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ItemViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    static class ItemViewHolder extends RvBaseViewHolder<Map<String,String>>{
        ItemCourseIntroduce mItemCourseIntroduce;

        ItemViewHolder(@NonNull ItemCourseIntroduce itemView) {
            super(itemView);
            mItemCourseIntroduce = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            mItemCourseIntroduce.setData(data);
        }
    }
}
