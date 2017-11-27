package amodule.main.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.RvBaseAdapter;
import acore.widget.rvlistview.RvBaseViewHolder;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeAlbumItem;
import amodule.main.view.item.HomeAnyImgStyleItem;
import amodule.main.view.item.HomePostItem;
import amodule.main.view.item.HomeRecipeItem;
import amodule.main.view.item.HomeTxtItem;
import third.ad.control.AdControlParent;

/**
 * 首页对个类型item
 */
public class HomeAdapter extends RvBaseAdapter<Map<String, String>> {
    //*********item样式，两个位置使用，1、当前adapter中，2、homefragment中的置顶数据****************
    public final static int type_tagImage = 1;//大图
    public final static int type_rightImage = 2;//右图
    public final static int type_threeImage = 3;//三图
    public final static int type_noImage = 4;//无图
    public final static int type_levelImage = 5;//蒙版图
    public final static int type_anyImage = 6;//任意图 限宽不限高

    private Activity mAct;
    private HomeModuleBean moduleBean;

    private AdControlParent mAdControlParent;

    public HomeAdapter(Activity mActivity, @Nullable List<Map<String, String>> data, AdControlParent adControlParent) {
        super(mActivity, data);
        this.mAct = mActivity;
        mAdControlParent = adControlParent;
    }

    public void setHomeModuleBean(HomeModuleBean moduleBean) {
        this.moduleBean = moduleBean;
    }

    @Override
    public RvBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case type_tagImage://大图
                return new ViewDishViewHolder(new HomeRecipeItem(mContext));
            case type_levelImage://蒙版
                return new ViewAlbumViewHolder(new HomeAlbumItem(mContext));
            case type_threeImage://美食贴
                return new ViewTiziViewHolder(new HomePostItem(mContext));
            case type_anyImage:
                return new ViewAnyImgViewHolder(new HomeAnyImgStyleItem(mContext));
            case type_rightImage://右图
            case type_noImage://无图
            default://找不到样式类型，指定默认-----无图样式
                return new ViewTxtViewHolder(new HomeTxtItem(mContext));
        }
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
        return (item == null || item.size() <= 0 || !item.containsKey("style") || TextUtils.isEmpty(item.get("style"))) ? String.valueOf(type_noImage) : item.get("style");
    }

    /**
     * 专辑
     */
    public class ViewAlbumViewHolder extends RvBaseViewHolder<Map<String, String>> {
        HomeAlbumItem view;

        public ViewAlbumViewHolder(HomeAlbumItem view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(data, position);
                if (viewClickCallBack != null) view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    /**
     * 贴子
     */
    public class ViewTiziViewHolder extends RvBaseViewHolder<Map<String, String>> {
        HomePostItem view;

        public ViewTiziViewHolder(HomePostItem view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(data, position);
                if (viewClickCallBack != null)
                    view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    /**
     * 菜谱
     */
    public class ViewDishViewHolder extends RvBaseViewHolder<Map<String, String>> {
        HomeRecipeItem view;

        public ViewDishViewHolder(HomeRecipeItem view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(data, position);
                if (viewClickCallBack != null) view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    /**
     * 文章
     */
    public class ViewTxtViewHolder extends RvBaseViewHolder<Map<String, String>> {
        HomeTxtItem view;

        public ViewTxtViewHolder(HomeTxtItem view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(data, position);
                if (viewClickCallBack != null) view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    /**
     * 任意图 限宽不限高
     */
    public class ViewAnyImgViewHolder extends RvBaseViewHolder<Map<String, String>> {
        HomeAnyImgStyleItem view;

        public ViewAnyImgViewHolder(HomeAnyImgStyleItem view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(data, position);
                if (viewClickCallBack != null) view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    public interface ViewClickCallBack {
        /*** @param isOnClick 是否被点击*/
        public void viewOnClick(boolean isOnClick);
    }

    private ViewClickCallBack viewClickCallBack;

    public void setViewOnClickCallBack(ViewClickCallBack viewClickCallBack) {
        this.viewClickCallBack = viewClickCallBack;
    }
}