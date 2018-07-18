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

package acore.widget.rvlistview.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

import acore.widget.rvlistview.Config;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * 基础adapter
 */
public abstract class RvBaseAdapter<T> extends RecyclerView.Adapter<RvBaseViewHolder<T>> {

    public final static String TAG = Config.TAG + " :: " + RvBaseAdapter.class.getSimpleName();

    protected Context mContext;

    protected List<T> mData;

    protected OnItemShow mOnItemShow;

    public RvBaseAdapter(Context context, @Nullable List<T> data) {
        this.mContext = context;
        setData(data);
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder<T> holder, int position) {
        holder.bindData(position, getItem(position));
        if(mOnItemShow != null){
            mOnItemShow.onItemShow(getItem(position),position);
        }
    }

    @Override
    public int getItemCount() {
        return null == mData ? 0 : mData.size();
    }

    @Nullable
    public T getItem(int position) {
        return null == mData || position >= mData.size() ? null : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract int getItemViewType(int position);

    public boolean addData(@NonNull T t) {
        if (null != mData
                && null != t) {
            if (mData.add(t)) {
                notifyItemInserted(mData.size() - 1);
                return true;
            }
        }
        return false;
    }

    public boolean addData(int index, @NonNull T t) {
        if (null != mData
                && null != t) {
            if (index >= 0
                    && index <= mData.size()) {
                mData.add(index, t);
                notifyItemInserted(index);
                return true;
            } else {
                Log.i(TAG, " insertData :: " + outOfBoundsMsg(index));
            }
        }
        return false;
    }

    public boolean removeData(T t) {
        if (null != mData
                && null != t
                && mData.contains(t)) {
            final int index = mData.indexOf(t);
            if (mData.remove(t)) {
                notifyItemRemoved(index);
                return true;
            }
        }
        return false;
    }

    public T removeData(final int index) {
        if (null != mData) {
            if (index >= 0
                    && index < mData.size()) {
                T oldT = mData.remove(index);
                notifyItemRemoved(index);
                return oldT;
            }else{
                Log.i(TAG, " removeData :: " + outOfBoundsMsg(index));
            }
        }
        return null;
    }

    public void updateData(List<T> newData){
        if (null != mData && null != newData) {
            mData.clear();
            mData.addAll(newData);
            notifyDataSetChanged();
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + getItemCount();
    }

    /*------------------------------------------------------- Get&Set -------------------------------------------------------*/

    public Context getContext() {
        return mContext;
    }

    public List<T> getData() {
        return mData;
    }

    public void setData(@NonNull List<T> data) {
        this.mData = null == data ? new ArrayList<T>() : data;
    }

    public void setOnItemShow(OnItemShow onItemShow) {
        mOnItemShow = onItemShow;
    }

   /*------------------------------------------------------- interface -------------------------------------------------------*/

    public interface OnItemShow<T>{
        void onItemShow(T t,int position);
    }
}
