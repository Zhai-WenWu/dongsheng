package amodule.lesson.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.List;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.adapter.RvBaseSimpleAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

class StudyAskAdapter extends RvBaseAdapter {
    public StudyAskAdapter(Context context, @Nullable List data) {
        super(context, data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StudyAskItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.study_ask_item_layout, parent,false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class StudyAskItemHolder extends RvBaseViewHolder{
        public StudyAskItemHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Object data) {

        }
    }
}
