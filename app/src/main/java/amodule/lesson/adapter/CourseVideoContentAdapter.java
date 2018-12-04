package amodule.lesson.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

public class CourseVideoContentAdapter extends RvBaseAdapter {
    public CourseVideoContentAdapter(Context context, @Nullable List data) {
        super(context, data);
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_courde_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class VideoViewHolder extends RvBaseViewHolder {
        TextView textView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Object data) {

        }

    }
}
