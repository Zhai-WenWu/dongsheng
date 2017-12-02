/*
 * Copyright (C) 2017 mrtrying
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package acore.widget.rvlistview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;


/**
 * Description :
 * PackageName : acore.widget.rvlistview
 * Created by MrTrying on 2017/9/26 19:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class RvListView extends RecyclerView {

    private static final String TAG = Config.TAG + " :: " + RvListView.class.getSimpleName();

    static final int VIEW_TYPE_HEADER = -1;
    static final int VIEW_TYPE_FOOTER = -2;
    static final int VIEW_TYPE_EMPTY = Integer.MAX_VALUE - 1;

    protected LinearLayout mHeaderContainer;
    protected LinearLayout mFooterContainer;

    protected View mEmptyView;

    private EmptyHandler mEmptyHandler;

    protected RvHeaderAndFooterViewAdapter mAdapter;

    private OnItemClickListener mOnItemClickListener;

    private OnItemLongClickListener mOnItemLongClickListener;
    /**Default implement for check can show EmptyView*/
    public final EmptyHandler DefaultEmptyHandler = new EmptyHandler() {
        @Override
        public boolean checkCanShowEmptyView(RecyclerView view, int totalCount, int originalCount) {
            return 0 == originalCount;
        }
    };

    public RvListView(Context context) {
        this(context, null, 0);
    }

    public RvListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RvListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeAttr(attrs);
        initializeStyle(defStyle);
        initialize();
    }

    protected void initializeAttr(AttributeSet attrs) {
    }

    protected void initializeStyle(int defStyle) {
    }

    protected void initialize() {
        mHeaderContainer = new LinearLayout(getContext());
        mFooterContainer = new LinearLayout(getContext());
        Log.d("tzy", "Constructor execute.");
    }

    @Override
    public Adapter getAdapter() {
        if(null != mAdapter)
            return mAdapter.getOriginalAdapter();
        return super.getAdapter();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if(getLayoutManager() == null){
            Log.i("tzy","setLinearLayoutManager");
            //默认使用LinearLayoutManager，并且处置布局
            setLayoutManager(new LinearLayoutManager(getContext()));
        }
        mAdapter = new RvHeaderAndFooterViewAdapter(adapter);
        super.setAdapter(mAdapter);
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(null, removeAndRecycleExistingViews);
        mAdapter = new RvHeaderAndFooterViewAdapter(adapter);
//        if (adapter != null) {
//            mAdapter.setHasStableIds(adapter.hasStableIds());
//        } else {
//            mAdapter.setHasStableIds(false);
//        }
        super.swapAdapter(mAdapter, removeAndRecycleExistingViews);
    }

    public void notifyItemViewRemove(int position){
        if(null != mAdapter){
            mAdapter.notifyItemViewRemove(position);
        }
    }

    /*------------------------------------------------------- Header -------------------------------------------------------*/

    public LinearLayout getHeaderContainer() {
        return mHeaderContainer;
    }

    public int getHeaderViewsSize() {
        return mHeaderContainer.getChildCount();
    }

    public void addHeaderView(@NonNull View view) {
        addHeaderView(view, -1);
    }

    public void addHeaderView(@NonNull View view, int index) {
        mHeaderContainer.addView(view, index);
        if (null != mAdapter)
            mAdapter.notifyHeaderInserted();
    }

    public void removeHeaderView(@NonNull View view) {
        mHeaderContainer.removeView(view);
        if (null != mAdapter)
            mAdapter.notifyHeaderRemoved();
    }

    public void removeHeaderView(int index) {
        mHeaderContainer.removeViewAt(index);
        if (null != mAdapter)
            mAdapter.notifyHeaderRemoved();
    }

    /*------------------------------------------------------- Footer -------------------------------------------------------*/

    public LinearLayout getFooterContainer() {
        return mFooterContainer;
    }

    public int getFooterViewsSize() {
        return mFooterContainer.getChildCount();
    }

    public void addFooterView(@NonNull View view) {
        addFooterView(view, -1);
    }

    public void addFooterView(@NonNull View view, int index) {
        mFooterContainer.addView(view, index);
        if (null != mAdapter)
            mAdapter.notifyFooterInserted();
    }

    public void removeFooterView(@NonNull View view) {
        mFooterContainer.removeView(view);
        if (null != mAdapter)
            mAdapter.notifyFooterRemoved();
    }

    public void removeFooterView(int index) {
        mFooterContainer.removeViewAt(index);
        if (null != mAdapter)
            mAdapter.notifyFooterRemoved();
    }

    /*------------------------------------------------------- EmptyView -------------------------------------------------------*/

    @Nullable
    public View getEmptyView() {
        return mEmptyView;
    }

    public void setEmptyView(View emptyView) {
        if (null == emptyView) {
            return;
        }
        this.mEmptyView = emptyView;
    }

    /**
     * 默认不显示
     *      mEmptyView != null && mEmptyHandler != null 才进行处理
     * @return
     */
    public boolean canShowEmptyView() {
        final int originalCount = (null != mAdapter && null != mAdapter.getOriginalAdapter()) ? mAdapter.getOriginalAdapter().getItemCount() : 0;
        final int totalCount = null != mAdapter ? mAdapter.getHeaderViewHolderCount() + mAdapter.getFooterViewHolderCount() + originalCount : 0;
        return null != mEmptyView
                && null != mEmptyHandler
                && mEmptyHandler.checkCanShowEmptyView(this, totalCount, totalCount);
    }

    void adjustFixedViewContainerLayoutParamsAndOrientation(@NonNull LinearLayout fixedViewContainer) {
        if (getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
            LayoutParams layoutParams;
            int orientation;
            if (fixedViewContainer.getLayoutParams() instanceof LayoutParams) {
                layoutParams = (LayoutParams) fixedViewContainer.getLayoutParams();
                if (linearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    orientation = LinearLayout.VERTICAL;
                } else {
                    layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    orientation = LinearLayout.HORIZONTAL;
                }
            } else {
                if (linearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                    layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    orientation = LinearLayout.VERTICAL;
                } else {
                    layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    orientation = LinearLayout.HORIZONTAL;
                }
            }
            fixedViewContainer.setLayoutParams(layoutParams);
            fixedViewContainer.setOrientation(orientation);
        }
    }


    /*------------------------------------------------------- Inner Adapter -------------------------------------------------------*/

    /** 支持Header&Footer的Adapter */
    final class RvHeaderAndFooterViewAdapter extends RecyclerView.Adapter {

        private RecyclerView.Adapter mOriginalAdapter;

        /*------------------------------------------------------- Inner Observer -------------------------------------------------------*/

        private final RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart + getPositionOffset(), itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                notifyItemRangeChanged(positionStart + getPositionOffset(), itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(positionStart + getPositionOffset(), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(positionStart + getPositionOffset(), itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                if (itemCount == 1) {
                    notifyItemMoved(fromPosition + getPositionOffset(), toPosition + getPositionOffset());
                } else {
                    notifyDataSetChanged();
                }
            }

        };

        /** 构造器 */
        RvHeaderAndFooterViewAdapter(RecyclerView.Adapter adapter) {
            if (this.mOriginalAdapter != null) {
                this.mOriginalAdapter.unregisterAdapterDataObserver(adapterDataObserver);
                this.mOriginalAdapter.onDetachedFromRecyclerView(RvListView.this);
            }
            this.mOriginalAdapter = adapter;
            if (mOriginalAdapter != null) {
                mOriginalAdapter.registerAdapterDataObserver(adapterDataObserver);
                mOriginalAdapter.onAttachedToRecyclerView(RvListView.this);
            }
        }

        @Override
        public int getItemCount() {
            int itemCount = 0;
            if (null != mOriginalAdapter)
                itemCount = mOriginalAdapter.getItemCount();
            //加上其他各种View
            if (canShowEmptyView()) {
                itemCount++;
            }
            itemCount += getHeaderViewHolderCount();
            itemCount += getFooterViewHolderCount();
            return itemCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeaderViewPos(position)) {
                return VIEW_TYPE_HEADER;
            } else if (isEmptyViewPos(position)) {
                return VIEW_TYPE_EMPTY;
            } else if (isFooterViewPos(position)) {
                return VIEW_TYPE_FOOTER;
            } else if (null != mOriginalAdapter) {
                int itemType = mOriginalAdapter.getItemViewType(position - getPositionOffset());
                if (itemType == VIEW_TYPE_HEADER) {
                    throw new RuntimeException("This ItemViewType is already uesd view type HeaderView , please replace another value . \n ItemViewType = " + itemType);
                } else if (itemType == VIEW_TYPE_EMPTY) {
                    throw new RuntimeException("This ItemViewType is already uesd view type EmptyView , please replace another value . \n ItemViewType = " + itemType);
                } else if (itemType == VIEW_TYPE_FOOTER) {
                    throw new RuntimeException("This ItemViewType is already uesd view type FooterView , please replace another value . \n ItemViewType = " + itemType);
                }
                return itemType;
            } else {
                return 0;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    return createViewHolder(getHeaderContainer());
                case VIEW_TYPE_EMPTY:
                    return createViewHolder(getEmptyView());
                case VIEW_TYPE_FOOTER:
                    return createViewHolder(getFooterContainer());
                default:
                    if (null != mOriginalAdapter) {
                        RecyclerView.ViewHolder holder = mOriginalAdapter.onCreateViewHolder(parent, viewType);
                        setItemListener(parent, holder, viewType);
                        return holder;
                    } else {
                        return null;
                    }
            }
        }

        public RecyclerView.ViewHolder createViewHolder(View itemView) {
            return new ViewHolder(itemView) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (isHeaderViewPos(position)) {
                adjustFixedViewContainerLayoutParamsAndOrientation(getHeaderContainer());
            } else if (isFooterViewPos(position)) {
                adjustFixedViewContainerLayoutParamsAndOrientation(getFooterContainer());
            } else if (isEmptyViewPos(position)) {
                //do nothing
            } else if (null != mOriginalAdapter) {
                mOriginalAdapter.onBindViewHolder(holder, position - getPositionOffset());
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, List payloads) {
            if (isHeaderViewPos(position)) {
                adjustFixedViewContainerLayoutParamsAndOrientation(getHeaderContainer());
            } else if (isFooterViewPos(position)) {
                adjustFixedViewContainerLayoutParamsAndOrientation(getFooterContainer());
            } else if (isEmptyViewPos(position)) {
                //do nothing
            } else if (null != mOriginalAdapter) {
                mOriginalAdapter.onBindViewHolder(holder, position - getPositionOffset(), payloads);
            }
        }

        @Override
        public long getItemId(int position) {
            if (null != mOriginalAdapter
                    && !isHeaderViewPos(position)
                    && !isFooterViewPos(position)
                    && !isEmptyViewPos(position)) {
                return mOriginalAdapter.getItemId(position);
            } else {
                return super.getItemId(position);
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (null != mOriginalAdapter
                    && VIEW_TYPE_HEADER != holder.getItemViewType()
                    && VIEW_TYPE_FOOTER != holder.getItemViewType()
                    && VIEW_TYPE_EMPTY != holder.getItemViewType()
                    ) {
                mOriginalAdapter.onViewRecycled(holder);
            }
        }

        @Override
        public boolean onFailedToRecycleView(ViewHolder holder) {
            return null != mOriginalAdapter
                    && VIEW_TYPE_HEADER != holder.getItemViewType()
                    && VIEW_TYPE_FOOTER != holder.getItemViewType()
                    && VIEW_TYPE_EMPTY != holder.getItemViewType()
                    && mOriginalAdapter.onFailedToRecycleView(holder);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            if (null != mOriginalAdapter
                    && VIEW_TYPE_HEADER != holder.getItemViewType()
                    && VIEW_TYPE_FOOTER != holder.getItemViewType()
                    && VIEW_TYPE_EMPTY != holder.getItemViewType()
                    ) {
                mOriginalAdapter.onViewAttachedToWindow(holder);
            }
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if (null != mOriginalAdapter
                    && VIEW_TYPE_HEADER != holder.getItemViewType()
                    && VIEW_TYPE_FOOTER != holder.getItemViewType()
                    && VIEW_TYPE_EMPTY != holder.getItemViewType()
                    ) {
                mOriginalAdapter.onViewDetachedFromWindow(holder);
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            if (!RvListView.this.equals(recyclerView)) {
                throw new RuntimeException("RvHeaderAndFooterViewAdapter can not be attached to other RecyclerView.");
            }
        }

        private boolean isHeaderViewPos(int position) {
            return position == 0 && isShowHeaderView();
        }

        private boolean isShowHeaderView() {
            return getHeaderViewsSize() > 0;
        }

        private boolean isFooterViewPos(int position) {
            return position == getItemCount() - 1 && isShowFooterView();
        }

        private boolean isShowFooterView() {
            return getFooterViewsSize() > 0;
        }

        private boolean isEmptyViewPos(int position) {
            return position == getPositionOffset()//only one header
                    && canShowEmptyView();
        }

        private int getHeaderViewHolderCount() {
            return isShowHeaderView() ? 1 : 0;
        }

        private int getFooterViewHolderCount() {
            return isShowFooterView() ? 1 : 0;
        }

        private int getPositionOffset() {
            return getHeaderViewHolderCount();
        }

        final void notifyHeaderInserted() {
            if (getHeaderViewsSize() == 1) {
                notifyItemInserted(0);
            }
        }

        final void notifyHeaderRemoved() {
            if (getHeaderViewsSize() == 0) {
                notifyItemRemoved(0);
            }
        }

        final void notifyFooterInserted() {
            if (getFooterViewsSize() == 1) {
                notifyItemInserted(getItemCount() - 1);
            }
        }

        final void notifyFooterRemoved() {
            if (getFooterViewsSize() == 0) {
                notifyItemRemoved(getItemCount());
            }
        }

        public void notifyItemViewRemove(int position) {
            if(position <= getItemCount()){
                notifyItemRemoved(position);
            }
        }

        protected boolean isEnabled(int viewType) {
            return true;
        }

        /**
         * 设置监听
         *
         * @param parent
         * @param viewHolder
         * @param viewType
         *
         * @return
         */
        protected ViewHolder setItemListener(final ViewGroup parent, final ViewHolder viewHolder, int viewType) {
            if (null != viewHolder && !isEnabled(viewType))
                return viewHolder;
            //设置itemClick
            if (null != mOnItemClickListener) {
                viewHolder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = viewHolder.getAdapterPosition();
                        mOnItemClickListener.onItemClick(v, viewHolder, position);
                    }
                });
            }
            //设置itemLongClick
            if (null != mOnItemLongClickListener) {
                viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = viewHolder.getAdapterPosition();
                        return mOnItemLongClickListener.onItemLongClick(v, viewHolder, position);
                    }
                });
            }
            return viewHolder;
        }
        /*------------------------------------------------------- ViewHolder -------------------------------------------------------*/

//        class FixedViewHolder extends ViewHolder {
//
//            FixedViewHolder(View itemView) {
//                super(itemView);
//            }
//        }

        /*------------------------------------------------------- Get&Set -------------------------------------------------------*/

        Adapter getOriginalAdapter() {
            return mOriginalAdapter;
        }
    }

    /*------------------------------------------------------- Inner Interface -------------------------------------------------------*/

    public interface OnItemClickListener {
        void onItemClick(View view, ViewHolder holder, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, ViewHolder holder, int position);
    }

    public interface EmptyHandler {
        boolean checkCanShowEmptyView(final RecyclerView view, final int totalCount, final int originalCount);
    }

    /*------------------------------------------------------- Common Get&Set -------------------------------------------------------*/

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    /**
     * 设置EmptyView显示策略
     * @param emptyHandler
     *          如果为 null 则使用 DefaultEmptyHandler 处理 EmptyView 的显示
     */
    public void setEmptyHandler(EmptyHandler emptyHandler) {
        this.mEmptyHandler = null == emptyHandler ? DefaultEmptyHandler : emptyHandler;
    }
}
