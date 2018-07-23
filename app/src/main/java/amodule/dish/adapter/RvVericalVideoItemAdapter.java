package amodule.dish.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.bugly.crashreport.BuglyLog;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.view.ShortVideoItemView;

/**
 * item adapter
 */
public class RvVericalVideoItemAdapter extends RvBaseAdapter<Map<String,String>>{
    private boolean once = false;
    public RvVericalVideoItemAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }
    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short_video_parent, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder<Map<String, String>> holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setTag(position);
    }

    @Override
    public void onViewAttachedToWindow(RvBaseViewHolder<Map<String, String>> holder) {
        super.onViewAttachedToWindow(holder);
//        mCurViewHolder = (ItemViewHolder) holder;
//        if (holder.getAdapterPosition() == 0 && !once) {
//            once = true;
//            startCurVideoView();
//        }
    }

    public class ItemViewHolder extends RvBaseViewHolder<Map<String,String>>{
        public ShortVideoItemView shortVideoItemView;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            shortVideoItemView= itemView.findViewById(R.id.videoItem);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            shortVideoItemView.setData(data,position);
        }
    }


    private ItemViewHolder mCurViewHolder;
    public void setCurViewHolder(ItemViewHolder viewHolder) {
        mCurViewHolder = viewHolder;
    }
    public void startCurVideoView() {
        if (mCurViewHolder != null && mCurViewHolder.shortVideoItemView != null
                && !mCurViewHolder.shortVideoItemView.isPlaying()
                ) {
            mCurViewHolder.shortVideoItemView.prepareAsync();
        }
    }

    public void rumeseVideoView(){
        if (mCurViewHolder != null && mCurViewHolder.shortVideoItemView != null) {
            mCurViewHolder.shortVideoItemView.resumeVideoView();
        }
    }
    public void pauseVideoView() {
        if (mCurViewHolder != null && mCurViewHolder.shortVideoItemView != null) {
            mCurViewHolder.shortVideoItemView.pauseVideoView();
        }
    }

    public void stopCurVideoView() {
        if (mCurViewHolder != null && mCurViewHolder.shortVideoItemView != null) {
            mCurViewHolder.shortVideoItemView.releaseVideoView();
        }
    }
}
