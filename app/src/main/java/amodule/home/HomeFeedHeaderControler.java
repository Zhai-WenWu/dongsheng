package amodule.home;

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
    Context mContext;

    private View mFeedTitleView;
    private LinearLayout layout, linearLayoutOne, linearLayoutTwo, linearLayoutThree;
    private HomeModuleBean mHomeModuleBean;

    public HomeFeedHeaderControler(Context context) {
        this.mContext = context;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(mContext, null);
        initFeedHeaderView();
    }

    /**
     * 初始化header布局
     */
    private void initFeedHeaderView() {
        //initHeaderView
        layout = new LinearLayout(mContext);

        layout.setOrientation(LinearLayout.VERTICAL);
        mFeedTitleView = LayoutInflater.from(mContext).inflate(R.layout.a_home_feed_title, null, true);
        mFeedTitleView.setVisibility(View.GONE);
        layout.addView(mFeedTitleView);

        linearLayoutOne = new LinearLayout(mContext);
        linearLayoutOne.setOrientation(LinearLayout.VERTICAL);
        linearLayoutOne.setVisibility(View.GONE);
        layout.addView(linearLayoutOne);

        linearLayoutTwo = new LinearLayout(mContext);
        linearLayoutTwo.setOrientation(LinearLayout.VERTICAL);
        linearLayoutTwo.setVisibility(View.GONE);
        layout.addView(linearLayoutTwo);

        linearLayoutThree = new LinearLayout(mContext);
        linearLayoutThree.setOrientation(LinearLayout.VERTICAL);
        linearLayoutThree.setVisibility(View.GONE);
        layout.addView(linearLayoutThree);
    }

    public void setTopData(List<Map<String, String>> data) {
        linearLayoutThree.removeAllViews();
        if (null == data || data.isEmpty()) return;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int size = data.size();
        for (int i = 0; i < size; i++) {
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
     * @return
     */
    private HomeItem handlerTopView(Map<String, String> map, int position) {
        HomeItem viewTop = null;
        if (map.containsKey("style") && !TextUtils.isEmpty(map.get("style"))) {
            int type = TextUtils.isEmpty(map.get("style")) ? HomeAdapter.type_noImage : Integer.parseInt(map.get("style"));
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
        }
        if (viewTop != null) {
            viewTop.setOnClickListener(v -> ((HomeItem) v).onClickEvent(v));
        }
        return viewTop;
    }

    public LinearLayout getLayout() {
        return layout;
    }

    public void setFeedheaderVisibility(boolean feedheaderVisibility) {
        mFeedTitleView.setVisibility(feedheaderVisibility ? View.VISIBLE : View.GONE);
    }
}
