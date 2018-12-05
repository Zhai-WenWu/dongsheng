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
        TextView tilteTv;
        TextView subtilteTv;
        ImageView img;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tilteTv = itemView.findViewById(R.id.tv_title);
            subtilteTv = itemView.findViewById(R.id.tv_subtitle);
            img = itemView.findViewById(R.id.iv_img);
        }

        @Override
        public void bindData(int position, @Nullable Object data) {
            switch (position) {
                case 1:
                    tilteTv.setVisibility(View.GONE);
                    subtilteTv.setVisibility(View.VISIBLE);
                    img.setVisibility(View.GONE);
                    break;
                case 0:
                    tilteTv.setVisibility(View.VISIBLE);
                    subtilteTv.setVisibility(View.VISIBLE);
                    img.setVisibility(View.GONE);
                    break;
                case 2:
                    tilteTv.setVisibility(View.GONE);
                    subtilteTv.setVisibility(View.VISIBLE);
                    img.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    tilteTv.setVisibility(View.GONE);
                    subtilteTv.setVisibility(View.GONE);
                    img.setVisibility(View.VISIBLE);
                    break;
            }
        }

    }
}
