package amodule.dish.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.BaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.video.module.ShortVideoDetailADModule;
import amodule.dish.video.module.ShortVideoDetailModule;
import amodule.dish.view.ShortVideoADItemView;
import amodule.dish.view.ShortVideoItemView;
import third.ad.tools.AdPlayIdConfig;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * item adapter
 */
public class RvVericalVideoItemAdapter extends BaseAdapter<ShortVideoDetailModule, RvVericalVideoItemAdapter.ItemViewHolder<ShortVideoDetailModule>> {
    public static final int VIEW_NORMAL = 1;
    public static final int VIEW_AD = 2;

    private ItemViewHolder<ShortVideoDetailModule> mCurrentViewHolder;
    private ShortVideoItemView.AttentionResultCallback mAttentionResultCallback;
    private ShortVideoItemView.GoodResultCallback mGoodResultCallback;
    private ShortVideoItemView.OnDeleteCallback mOnDeleteCallback;

    private List<ShortVideoDetailADModule> mADData;

    public RvVericalVideoItemAdapter(Context context, @Nullable List<ShortVideoDetailModule> data) {
        super(context, data);
        mADData = new ArrayList<>();
    }

    @Override
    public ItemViewHolder<ShortVideoDetailModule> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short_video_parent, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == VIEW_AD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short_video_ad_parent, parent, false);
            return new ADItemViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof ShortVideoDetailADModule ? 2 : 1;
    }

    @Nullable
    @Override
    public ShortVideoDetailModule getItem(int position) {
        if (!mADData.isEmpty()) {
            for (int i = 0; i < mADData.size(); i++) {
                if (mADData.get(i).adPositionInData == position){
                    if(super.getItem(position) instanceof ShortVideoDetailADModule) {
                        return super.getItem(position);
                    }else{
                        mADData.get(i).isGetData = true;
                        return mADData.get(i);
                    }
                }
            }
            int currentPosition = position;
            for (int i = 0; i < mADData.size(); i++) {
                if(mADData.get(i).adPositionInData >= position){
                    currentPosition--;
                }else{
                    return super.getItem(currentPosition);
                }
            }
        }
        return super.getItem(position);
    }

    @Override
    public void onViewRecycled(ItemViewHolder<ShortVideoDetailModule> holder) {
//        holder.stopVideo();
    }

    @Override
    public boolean onFailedToRecycleView(ItemViewHolder<ShortVideoDetailModule> holder) {
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(ItemViewHolder<ShortVideoDetailModule> holder) {
        super.onViewAttachedToWindow(holder);

    }

    @Override
    public void onViewDetachedFromWindow(ItemViewHolder<ShortVideoDetailModule> holder) {
        super.onViewDetachedFromWindow(holder);

        if(mRecyclerView != null && mRecyclerView.getScrollState() == SCROLL_STATE_IDLE){
//            holder.stopVideo();
        }else{
            holder.stopVideo();
        }
    }
    RecyclerView mRecyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void setAttentionResultCallback(ShortVideoItemView.AttentionResultCallback attentionResultCallback) {
        mAttentionResultCallback = attentionResultCallback;
    }

    public void setGoodResultCallback(ShortVideoItemView.GoodResultCallback goodResultCallback) {
        mGoodResultCallback = goodResultCallback;
    }

    public void setOnDeleteCallback(ShortVideoItemView.OnDeleteCallback onDeleteCallback) {
        mOnDeleteCallback = onDeleteCallback;
    }

    public class ItemViewHolder<T extends ShortVideoDetailModule> extends RvBaseViewHolder<T>{
        private ShortVideoItemView shortVideoItemView;
        public ShortVideoDetailModule data;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable T data) {
            shortVideoItemView = itemView.findViewById(R.id.videoItem);
            itemView.setTag(position);
            this.data = data;
            shortVideoItemView.setData(data,position);
            shortVideoItemView.setAttentionResultCallback(success -> {
                if (mAttentionResultCallback != null) {
                    mAttentionResultCallback.onResult(success);
                }
            });
            shortVideoItemView.setGoodResultCallback(success -> {
                if (mGoodResultCallback != null) {
                    mGoodResultCallback.onResult(success);
                }
            });
            shortVideoItemView.setOnDeleteCallback((module, position1) -> {
                if (mOnDeleteCallback != null) {
                    mOnDeleteCallback.onDelete(module, position1);
                }
            });
        }

        public int getPlayState() {
            if (shortVideoItemView != null)
                return shortVideoItemView.getPlayState();
            return -1;
        }

        public void startVideo() {
            shortVideoItemView.prepareAsync();
        }

        public void resumeVideo() {
            shortVideoItemView.resumeVideo();
        }

        public void pauseVideo() {
            shortVideoItemView.pauseVideo();
        }

        public void stopVideo() {
            shortVideoItemView.releaseVideo();
        }

        public void gotoUser() {
            shortVideoItemView.gotoUser();
        }

        public void updateShareNum() {
            shortVideoItemView.updateShareNum();
        }

        public void updateLikeState() {
            shortVideoItemView.updateLikeState();
        }

        public void updateLikeNum() {
            shortVideoItemView.updateLikeNum();
        }

        public void updateCommentNum() {
            shortVideoItemView.updateCommentNum();
        }

        public void updateAttentionState() {
            shortVideoItemView.updateAttentionState();
        }

        public void updateFavoriteState() {
            shortVideoItemView.updateFavoriteState();
        }
    }

    public class ADItemViewHolder extends ItemViewHolder {
        private ShortVideoADItemView shortVideoItemView;
        public ShortVideoDetailADModule data;

        public ADItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable ShortVideoDetailModule data) {
            shortVideoItemView = itemView.findViewById(R.id.videoItem);
            itemView.setTag(position);
            this.data = (ShortVideoDetailADModule) data;
            shortVideoItemView.setData((ShortVideoDetailADModule) data, position);
            shortVideoItemView.setOnADClickCallback(mOnADClickCallback);
            shortVideoItemView.setOnADShowCallback(mOnADShowCallback);
            shortVideoItemView.setOnAdHintClickListener(mOnAdHintClickListener);
        }

        public int getPlayState() {
//            if (shortVideoItemView != null)
//                return shortVideoItemView.getPlayState();
            return -1;
        }

        @Override
        public void startVideo() {
            Log.i("tzy", "startVideo:");
            shortVideoItemView.prepareAsync();
            //广告展示统计
            AdPlayIdConfig.shown(this.data.adId);
            //真实刷新数据
            refreshData();
        }

        private void refreshData() {
            if (!mADData.isEmpty()) {
                boolean needRefresh = false;
                for (int i = 0; i < mADData.size(); i++) {
                    ShortVideoDetailModule module = mADData.get(i);
                    ShortVideoDetailADModule adModule = (ShortVideoDetailADModule) module;
                    if (adModule.adPositionInData >= 0 && adModule.adPositionInData < mData.size()) {
                        if (!(mData.get(adModule.adPositionInData) instanceof ShortVideoDetailADModule)) {
                            needRefresh = true;
                            mData.add(adModule.adPositionInData, module);
                            mADData.remove(module);
                            i--;
                        }
                    }
                }
                if(needRefresh){
                    Log.i("tzy", "startVideo: notifyDataSetChanged");
                    notifyDataSetChanged();
                }
            }
        }

        public void resumeVideo() {
            shortVideoItemView.resumeVideo();
        }

        public void pauseVideo() {
            shortVideoItemView.pauseVideo();
        }

        public void stopVideo() {
            shortVideoItemView.releaseVideo();
        }

        public void gotoUser() {
            shortVideoItemView.gotoUser();
        }

    }
    public void notifyGotoUser() {
        if (mCurrentViewHolder != null) {
            mCurrentViewHolder.gotoUser();
        }
    }

    public ItemViewHolder<ShortVideoDetailModule> getCurrentViewHolder() {
        return mCurrentViewHolder;
    }

    public void setCurrentViewHolder(ItemViewHolder<ShortVideoDetailModule> currentViewHolder) {
        mCurrentViewHolder = currentViewHolder;
    }

    public void onResume() {
        if (mCurrentViewHolder != null) {
            mCurrentViewHolder.resumeVideo();
        }
    }

    public void onPause() {
        if (mCurrentViewHolder != null) {
            mCurrentViewHolder.pauseVideo();
        }
    }

    public void onDestroy() {
        if (mCurrentViewHolder != null) {
            mCurrentViewHolder.stopVideo();
        }
    }

    public int getPlayState() {
        if (mCurrentViewHolder != null) {
            return mCurrentViewHolder.getPlayState();
        }
        return -1;
    }

    public void setADData(List<ShortVideoDetailADModule> ADData) {
        mADData = ADData;
    }
    private ShortVideoADItemView.OnADShowCallback mOnADShowCallback;
    public void setOnADShowCallback(ShortVideoADItemView.OnADShowCallback onADShowCallback) {
        mOnADShowCallback = onADShowCallback;
    }
    private ShortVideoADItemView.OnADClickCallback mOnADClickCallback;
    public void setOnADClickCallback(ShortVideoADItemView.OnADClickCallback onADClickCallback) {
        mOnADClickCallback = onADClickCallback;
    }

    private ShortVideoADItemView.OnAdHintClickListener mOnAdHintClickListener;
    public void setOnAdHintClickListener(ShortVideoADItemView.OnAdHintClickListener onAdHintClickListener) {
        mOnAdHintClickListener = onAdHintClickListener;
    }
}
