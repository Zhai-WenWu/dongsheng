package amodule.dish.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.List;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.video.module.ShortVideoDetailModule;
import amodule.dish.view.ShortVideoItemView;

/**
 * item adapter
 */
public class RvVericalVideoItemAdapter extends RvBaseAdapter<ShortVideoDetailModule>{
    private boolean once = false;
    public RvVericalVideoItemAdapter(Context context, @Nullable List<ShortVideoDetailModule> data) {
        super(context, data);
    }
    @Override
    public RvBaseViewHolder<ShortVideoDetailModule> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short_video_parent, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder<ShortVideoDetailModule> holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setTag(position);
    }

    @Override
    public void onViewAttachedToWindow(RvBaseViewHolder<ShortVideoDetailModule> holder) {
        super.onViewAttachedToWindow(holder);
//        mCurViewHolder = (ItemViewHolder) holder;
//        if (holder.getAdapterPosition() == 0 && !once) {
//            once = true;
//            startCurVideoView();
//        }
    }

    public class ItemViewHolder extends RvBaseViewHolder<ShortVideoDetailModule>{
        public ShortVideoItemView shortVideoItemView;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            shortVideoItemView= itemView.findViewById(R.id.videoItem);
        }

        @Override
        public void bindData(int position, @Nullable ShortVideoDetailModule data) {
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

    public void notifyGotoUser() {
        if (mCurViewHolder != null && mCurViewHolder.shortVideoItemView != null) {
            mCurViewHolder.shortVideoItemView.gotoUser();
        }
    }
}
