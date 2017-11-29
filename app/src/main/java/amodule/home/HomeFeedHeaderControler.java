package amodule.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import amodule.main.adapter.HomeAdapter;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeAlbumItem;
import amodule.main.view.item.HomeAnyImgStyleItem;
import amodule.main.view.item.HomeItem;
import amodule.main.view.item.HomePostItem;
import amodule.main.view.item.HomeRecipeItem;
import amodule.main.view.item.HomeTxtItem;

import static amodule.home.HomeViewControler.MODULETOPTYPE;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/15 16:39.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeFeedHeaderControler {
    private Context mContext;

    private LinearLayout layout, linearLayoutThree;
    private HomeModuleBean mHomeModuleBean;

    HomeFeedHeaderControler(Context context) {
        this.mContext = context;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(mContext, null);
        initFeedHeaderView();
    }

    /**
     * 初始化header布局
     */
    @SuppressLint("InflateParams")
    private void initFeedHeaderView() {
        //initHeaderView
        layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        linearLayoutThree = new LinearLayout(mContext);
        linearLayoutThree.setOrientation(LinearLayout.VERTICAL);
        linearLayoutThree.setVisibility(View.GONE);
        layout.addView(linearLayoutThree);
    }

    @SuppressLint("InflateParams")
    void setTopData(List<Map<String, String>> data) {
        linearLayoutThree.removeAllViews();
        if (null == data || data.isEmpty()) return;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        for (int i = 0 , size = data.size(); i < size; i++) {
            HomeItem view = handlerTopView(data.get(i), i);
            if (view != null) {
                linearLayoutThree.addView(view);
                linearLayoutThree.addView(inflater.inflate(R.layout.view_home_show_line, null));
            }
        }
        linearLayoutThree.setVisibility(View.VISIBLE);
    }

    /**
     * 处理置顶数据View类型
     *
     * @param map 置顶数据
     *
     * @return 对应的View
     */
    private HomeItem handlerTopView(Map<String, String> map, int position) {
        HomeItem viewTop;
        String styleValue = map.get("style");
        int type = HomeAdapter.type_noImage;
        if (!TextUtils.isEmpty(styleValue))
            try {
                type = Integer.parseInt(styleValue);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        switch (type) {
            case HomeAdapter.type_tagImage:
                viewTop = new HomeRecipeItem(mContext);
                break;
            case HomeAdapter.type_levelImage:
                viewTop = new HomeAlbumItem(mContext);
                break;
            case HomeAdapter.type_threeImage:
                viewTop = new HomePostItem(mContext);
                break;
            case HomeAdapter.type_anyImage:
                viewTop = new HomeAnyImgStyleItem(mContext);
                break;
            case HomeAdapter.type_rightImage:
            case HomeAdapter.type_noImage:
            default:
                viewTop = new HomeTxtItem(mContext);
                break;
        }
        viewTop.setViewType(MODULETOPTYPE);
        viewTop.setHomeModuleBean(mHomeModuleBean);
        viewTop.setData(map, position);
        viewTop.setOnClickListener(v -> ((HomeItem) v).onClickEvent(v));
        return viewTop;
    }

    public LinearLayout getLayout() {
        return layout;
    }

}
