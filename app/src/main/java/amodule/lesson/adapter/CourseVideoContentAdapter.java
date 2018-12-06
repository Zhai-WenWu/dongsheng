package amodule.lesson.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import aplug.basic.LoadImage;

public class CourseVideoContentAdapter extends RvBaseAdapter<Map<String, String>> {


    public CourseVideoContentAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_courde_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class VideoViewHolder extends RvBaseViewHolder<Map<String, String>> {
        TextView tilteTv;
        TextView subtilteTv;
        ImageView imageView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tilteTv = itemView.findViewById(R.id.tv_title);
            subtilteTv = itemView.findViewById(R.id.tv_subtitle);
            imageView = itemView.findViewById(R.id.iv_img);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (data == null)
                return;

            tilteTv.setText(data.get("subTitle"));
            subtilteTv.setText(data.get("content"));
            LoadImage.with(mContext).load(data.get("img")).build().into(imageView);
        }

    }
}
