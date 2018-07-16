/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.edit.effects.filter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import third.aliyun.edit.effects.control.BaseChooser;
import third.aliyun.edit.effects.control.EffectInfo;
import third.aliyun.edit.effects.control.OnItemClickListener;
import third.aliyun.edit.effects.control.SpaceItemDecoration;
import third.aliyun.edit.msg.Dispatcher;
import third.aliyun.edit.msg.body.SelectColorFilter;
import third.aliyun.edit.util.Common;

public class FilterChooserMediator extends BaseChooser
        implements OnItemClickListener {
    private RecyclerView mListView;
    private FilterAdapter mFilterAdapter;

    public static FilterChooserMediator newInstance() {
        FilterChooserMediator dialog = new FilterChooserMediator();
        Bundle args = new Bundle();
        return dialog;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.work_aliyun_svideo_filter_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mListView = (RecyclerView) view.findViewById(R.id.effect_list_filter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mFilterAdapter = new FilterAdapter(getContext());
        mFilterAdapter.setOnItemClickListener(this);
        mFilterAdapter.setDataList(Common.getColorFilterList());
//        mFilterAdapter.setSelectedPos(mEditorService.getEffectIndex(UIEditorPage.FILTER_EFFECT));
        mListView.setAdapter(mFilterAdapter);
        mListView.addItemDecoration(new SpaceItemDecoration(getContext().getResources().getDimensionPixelSize(R.dimen.dp_10)));
    }
    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        Dispatcher.getInstance().postMsg(new SelectColorFilter.Builder()
                .effectInfo(effectInfo)
                .index(index).build());
        return true;
    }
}
