package amodule.main.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeAlbumItem;
import amodule.main.view.item.HomeAnyImgStyleItem;
import amodule.main.view.item.HomeGridADItem;
import amodule.main.view.item.HomeGridXHADItem;
import amodule.main.view.item.HomePostItem;
import amodule.main.view.item.HomeRecipeItem;
import amodule.main.view.item.HomeStaggeredGridItem;
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

    public final static int type_gridImage = 101;//网格样式
    public final static int type_gridADImage = 102;//网格样式 广告
    public final static int type_gridXHADImage = 103;//网格样式 自有广告

    protected Activity mAct;

    protected AdControlParent mAdControlParent;

    protected HomeModuleBean moduleBean;

    public final static String LIST_TYPE_LIST = "1";
    public final static String LIST_TYPE_GRID = "2";
    public final static String LIST_TYPE_STAGGERED = "3";
    private String mListType = LIST_TYPE_LIST;//网格列表

    public HomeAdapter(Activity mActivity, @Nullable List<Map<String, String>> data, AdControlParent adControlParent) {
        super(mActivity, data);
        this.mAct = mActivity;
        mAdControlParent = adControlParent;
    }

    public void setHomeModuleBean(HomeModuleBean homeModuleBean) {
        moduleBean = homeModuleBean;
    }

    @Override
    public RvBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case type_gridXHADImage:
                return new GridXHADImageViewHolder(new HomeGridXHADItem(mContext));
            case type_gridImage://网格
                return new StaggeredGridImageViewHolder(new HomeStaggeredGridItem(mContext));
            case type_gridADImage://网格广告
                return new GridADImageViewHolder(new HomeGridADItem(mContext));
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
        if (holder == null)
            return;
        holder.bindData(position, getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, String> item = getItem(position);
        int type;
        switch (mListType) {
            case LIST_TYPE_GRID:
            case LIST_TYPE_STAGGERED:
                String adStyle = "";
                if (item != null) {
                    adStyle = item.get("adStyle");
                }
                adStyle = adStyle == null ? "" : adStyle;
                switch (adStyle) {
                    case "ad":
                        type = type_gridADImage;
                        break;
                    case "xh":
                        type = type_gridXHADImage;
                        break;
                    default:
                        type = type_gridImage;
                        break;
                }
                break;
            default:
                String style = (item == null || item.size() <= 0 || !item.containsKey("style") || TextUtils.isEmpty(item.get("style"))) ? String.valueOf(type_noImage) : item.get("style");
                type = Integer.parseInt(style);
                break;
        }
        return type;
    }

    public void setListType(String listType) {
        mListType = listType;
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

    /**
     * 错落网格
     */
    public class StaggeredGridImageViewHolder extends RvBaseViewHolder<Map<String, String>> {
        public HomeStaggeredGridItem view;
        public ConstraintLayout homeStaggeredGridItemContentLayout;
        public StaggeredGridImageViewHolder(HomeStaggeredGridItem view) {
            super(view);
            this.view = view;
            homeStaggeredGridItemContentLayout = view.getContentLayout();
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
     * 网格广告
     */
    public class GridADImageViewHolder extends RvBaseViewHolder<Map<String, String>> {
        public HomeGridADItem view;
        public ConstraintLayout homeGirdAdItemContentLayout;
        public GridADImageViewHolder(HomeGridADItem view) {
            super(view);
            this.view = view;
            homeGirdAdItemContentLayout = view.getContentLayout();
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
     * 网格广告
     */
    public class GridXHADImageViewHolder extends RvBaseViewHolder<Map<String, String>> {
        public HomeGridXHADItem view;
        public ConstraintLayout homeGirdAdItemContentLayout;
        public GridXHADImageViewHolder(HomeGridXHADItem view) {
            super(view);
            this.view = view;
            homeGirdAdItemContentLayout = view.getContentLayout();
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

    public interface ViewClickCallBack {
        /*** @param isOnClick 是否被点击*/
        public void viewOnClick(boolean isOnClick);
    }

    protected ViewClickCallBack viewClickCallBack;

    public void setViewOnClickCallBack(ViewClickCallBack viewClickCallBack) {
        this.viewClickCallBack = viewClickCallBack;
    }
}