package amodule.home.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.home.view.HomeSecondADItem;
import amodule.home.view.HomeSecondRecipeItem;
import amodule.main.adapter.HomeAdapter;
import third.ad.control.AdControlParent;

/**
 * 首页二级页面大图样式的适配器
 */
public class HomeSecondRecyclerAdapter extends HomeAdapter {

    private final int mADType = 100;

    public HomeSecondRecyclerAdapter(Activity mActivity, @Nullable List<Map<String, String>> data, AdControlParent adControlParent) {
        super(mActivity, data, adControlParent);
    }

    @Override
    public RvBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RvBaseViewHolder viewHolder = null;
        switch (viewType) {
            case type_tagImage:
                viewHolder = new HomeSecondItemViewHolder(new HomeSecondRecipeItem(mContext));
                break;
            case mADType:
                viewHolder = new HomeSecondItemGgViewHolder(new HomeSecondADItem(mContext));
                break;
        }
        return viewHolder;
    }

    public class HomeSecondItemViewHolder extends RvBaseViewHolder<Map<String, String>> {

        private HomeSecondRecipeItem mItemView;
        public HomeSecondItemViewHolder(@NonNull HomeSecondRecipeItem itemView) {
            super(itemView);
            mItemView = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (mItemView != null) {
                mItemView.setHomeModuleBean(moduleBean);
                mItemView.setAdControl(mAdControlParent);
                mItemView.setData(data, position);
                if (viewClickCallBack != null) {
                    mItemView.setRefreshTag(viewClickCallBack);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, String> data = getItem(position);
        if (data == null)
            return type_tagImage;
        return TextUtils.equals("ad", data.get("adstyle")) ? mADType : type_tagImage;
    }

    public class HomeSecondItemGgViewHolder extends RvBaseViewHolder<Map<String, String>> {

        private HomeSecondADItem mItemView;
        public HomeSecondItemGgViewHolder(@NonNull HomeSecondADItem itemView) {
            super(itemView);
            mItemView = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (mItemView != null) {
                mItemView.setHomeModuleBean(moduleBean);
                mItemView.setAdControl(mAdControlParent);
                mItemView.setData(data, position);
                if (viewClickCallBack != null)
                    mItemView.setRefreshTag(viewClickCallBack);
            }
        }
    }
}