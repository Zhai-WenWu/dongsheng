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
 * 我的收藏配件
 */
public class AdapterMyFavorite extends RvBaseAdapter<Map<String, String>> {
    public AdapterMyFavorite(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("xianghaTag","viewType:::"+viewType);

//        switch (viewType) {
//            case HomeAdapter.type_tagImage://大图
//                return new BigViewHolder(new FavoriteItemBaseView(mContext,R.layout.favorite_big));
////            case HomeAdapter.type_levelImage://蒙版
////                return new MaskViewHolder(new FavoriteItemBaseView(mContext));
//            case HomeAdapter.type_rightImage://右图
//            case HomeAdapter.type_noImage://无图
//            default://找不到样式类型，指定默认-----无图样式
//                return new RightViewHolder(new FavoriteItemBaseView(mContext, R.layout.favorite_right));
//        }
        return new RightViewHolder(new ModuleItemS0View(mContext));
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
        Map<String, String> item = getItem(position);
        return (item == null || item.size() <= 0 || !item.containsKey("style") || TextUtils.isEmpty(item.get("style"))) ? String.valueOf(HomeAdapter.type_noImage) : item.get("style");
    }



    /**
     * 大图
     */
    public class BigViewHolder extends RvBaseViewHolder<Map<String, String>> {
        ModuleItemS0View view;

        public BigViewHolder(ModuleItemS0View view) {
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
    /**
     * 右图
     */
    public class RightViewHolder extends RvBaseViewHolder<Map<String, String>> {
        ModuleItemS0View view;

        public RightViewHolder(ModuleItemS0View view) {
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
    /**
     * 蒙版
     */
    public class MaskViewHolder extends RvBaseViewHolder<Map<String, String>> {
        ModuleItemS0View view;

        public MaskViewHolder(ModuleItemS0View view) {
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
    /**
     * 多图
     */
    public class MoreViewHolder extends RvBaseViewHolder<Map<String, String>> {
        ModuleItemS0View view;

        public MoreViewHolder(ModuleItemS0View view) {
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
}
