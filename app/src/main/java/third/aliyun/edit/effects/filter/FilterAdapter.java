/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.edit.effects.filter;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.struct.effect.EffectFilter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import third.aliyun.edit.effects.control.EffectInfo;
import third.aliyun.edit.effects.control.OnItemClickListener;
import third.aliyun.edit.effects.control.OnItemLongClickListener;
import third.aliyun.edit.effects.control.OnItemTouchListener;
import third.aliyun.edit.effects.control.UIEditorPage;
import third.aliyun.widget.CircularImageView;

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    private Context mContext;
    private OnItemClickListener mItemClick;
    private OnItemLongClickListener mItemLongClick;
    private OnItemTouchListener mItemTouchListener;
    private int mSelectedPos = 0;
    private FilterViewHolder mSelectedHolder;
    private List<String> mFilterList = new ArrayList<>();

    public FilterAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_resources_item_view, parent, false);
        FilterViewHolder filterViewHolder = new FilterViewHolder(view);
        return filterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FilterViewHolder filterViewHolder = (FilterViewHolder) holder;
        String name = mContext.getString(R.string.none_effect);
        String path = mFilterList.get(position);
        if (path == null || "".equals(path)) {
            Glide.with(mContext).load(R.drawable.aliyun_svideo_none).into(new ViewTarget<CircularImageView, GlideDrawable>(filterViewHolder.mImage) {
                @Override
                public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    filterViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                }
            });
        } else {
            EffectFilter effectFilter = new EffectFilter(path);
            if (effectFilter != null) {
                name = effectFilter.getName();
                if (filterViewHolder != null) {
                    Glide.with(mContext).load(effectFilter.getPath() + "/icon.png").into(new ViewTarget<CircularImageView, GlideDrawable>(filterViewHolder.mImage) {
                        @Override
                        public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            filterViewHolder.mImage.setImageBitmap(((GlideBitmapDrawable) glideDrawable).getBitmap());
                        }
                    });
                }
            }
        }

        if (mSelectedPos > mFilterList.size()) {
            mSelectedPos = 0;
        }

        if (mSelectedPos == position) {
            filterViewHolder.mImage.setSelected(true);
            filterViewHolder.imageView.setSelected(true);
            filterViewHolder.imageView.setSelected(true);
            filterViewHolder.image_current.setVisibility(View.VISIBLE);
            mSelectedHolder = filterViewHolder;
        } else {
            filterViewHolder.mImage.setSelected(false);
            filterViewHolder.imageView.setSelected(false);
            filterViewHolder.image_current.setVisibility(View.GONE);
        }
        filterViewHolder.mName.setText(name);
        filterViewHolder.itemView.setTag(holder);
        filterViewHolder.itemView.setOnClickListener(this);
        filterViewHolder.itemView.setOnLongClickListener(this);
        filterViewHolder.itemView.setOnTouchListener(this);
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    @Override
    public boolean onLongClick(View v) {
        if (mItemLongClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) v.getTag();
            int position = viewHolder.getAdapterPosition();
            mSelectedHolder.mImage.setSelected(false);
            viewHolder.mImage.setSelected(true);
            mSelectedPos = position;
            mSelectedHolder = viewHolder;

            EffectInfo effectInfo = new EffectInfo();
            effectInfo.type = UIEditorPage.FILTER_EFFECT;
            effectInfo.setPath(mFilterList.get(position));
            effectInfo.id = position;
            mItemLongClick.onItemLongClick(effectInfo, position);
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mItemTouchListener != null) {
                    FilterViewHolder viewHolder = (FilterViewHolder) v.getTag();
                    int position = viewHolder.getAdapterPosition();
                    EffectInfo effectInfo = new EffectInfo();
                    effectInfo.type = UIEditorPage.FILTER_EFFECT;
                    effectInfo.setPath(mFilterList.get(position));
                    effectInfo.id = position;
                    mItemTouchListener.onTouchEvent(OnItemTouchListener.EVENT_UP,
                            position, effectInfo);
                }
        }
        return false;
    }

    private static class FilterViewHolder extends RecyclerView.ViewHolder {


        CircularImageView mImage;
        TextView mName;
        ImageView imageView,image_current;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mImage = (CircularImageView) itemView.findViewById(R.id.resource_image_view);
            mName = (TextView) itemView.findViewById(R.id.resource_name);
            imageView= (ImageView) itemView.findViewById(R.id.image_back);
            image_current= (ImageView) itemView.findViewById(R.id.image_current);
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener li) {
        mItemLongClick = li;
    }

    public void setOnItemTouchListener(OnItemTouchListener li) {
        mItemTouchListener = li;
    }

    @Override
    public void onClick(View view) {
        if (mItemClick != null) {
            FilterViewHolder viewHolder = (FilterViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if (mSelectedPos != position && mSelectedHolder != null) {
                mSelectedHolder.mImage.setSelected(false);
                viewHolder.mImage.setSelected(true);
                mSelectedHolder.imageView.setSelected(false);
                viewHolder.imageView.setSelected(true);
                mSelectedHolder.image_current.setVisibility(View.GONE);
                viewHolder.image_current.setVisibility(View.VISIBLE);
                mSelectedPos = position;
                mSelectedHolder = viewHolder;

                EffectInfo effectInfo = new EffectInfo();
                effectInfo.type = UIEditorPage.FILTER_EFFECT;
                effectInfo.setPath(mFilterList.get(position));
                effectInfo.id = position;
                mItemClick.onItemClick(effectInfo, position);
            }
        }
    }

    public void setDataList(List<String> list) {
        mFilterList.clear();
        mFilterList.add(null);
        mFilterList.addAll(list);
    }

    public void setSelectedPos(int position) {
        mSelectedPos = position;
    }
}
