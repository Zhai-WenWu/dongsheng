package amodule.user.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.RvBaseAdapter;
import acore.widget.rvlistview.RvBaseViewHolder;
import amodule.main.adapter.HomeAdapter;
import amodule.user.view.module.ModuleItemS0View;

/**
 * ModuleS的标准adapter
 */
public class AdapterModuleS0 extends RvBaseAdapter<Map<String, String>> {
    private String statisticId = "";//统计id
    public AdapterModuleS0(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }
    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ModuleS0ViewHolder(new ModuleItemS0View(mContext));
    }
    @Override
    public void onBindViewHolder(RvBaseViewHolder holder, int position) {
        holder.bindData(position, getItem(position));
    }
    @Override
    public int getItemViewType(int position) {
        return Integer.parseInt(getItemType(position));
    }

    public String getItemType(int position) {
        return "0";
    }
    /**
     * 模块数据
     */
    public class ModuleS0ViewHolder extends RvBaseViewHolder<Map<String, String>> {
        ModuleItemS0View view;
        public ModuleS0ViewHolder(ModuleItemS0View view) {
            super(view);
            this.view = view;
        }
        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.initData(data);
            }
        }
    }

    public String getStatisticId() {
        return statisticId;
    }

    public void setStatisticId(String statisticId) {
        this.statisticId = statisticId;
    }
}
