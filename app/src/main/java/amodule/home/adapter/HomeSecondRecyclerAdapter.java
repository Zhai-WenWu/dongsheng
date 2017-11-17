package amodule.home.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.main.adapter.HomeAdapter;
import amodule.main.view.item.HomeRecipeItem;
import third.ad.control.AdControlParent;

/**
 * 首页二级页面大图样式的适配器
 */
public class HomeSecondRecyclerAdapter extends HomeAdapter {

    public HomeSecondRecyclerAdapter(Activity mActivity, @Nullable List<Map<String, String>> data, AdControlParent adControlParent) {
        super(mActivity, data, adControlParent);
    }

    @Override
    public RvBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewDishViewHolder(new HomeRecipeItem(mContext));
    }
}