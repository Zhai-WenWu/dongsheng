package amodule.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.RvStaggeredGridView;
import amodule._common.delegate.ISetAdController;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule.home.view.HomeBuoy;
import amodule.home.view.HomeTitleLayout;
import amodule.main.activity.MainHomePage;
import amodule.main.view.item.HomeItem;
import aplug.web.ShowWeb;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.ad.scrollerAd.XHAllAdControl;
import third.umeng.OnlineConfigControler;

/**
 * 首页view的控制
 */
public class HomeViewControler implements ISetAdController {

    static String MODULETOPTYPE = "moduleTopType";//置顶数据的类型

    private HomeHeaderControler mHeaderControler;
    private HomeFeedHeaderControler mHomeFeedHeaderControler;
    private HomeTitleLayout mTitleLayout;

    private MainHomePage mActivity;

    private HomeBuoy mBuoy;

    private PtrClassicFrameLayout mRefreshLayout;
    private RvStaggeredGridView recyclerView;

    //feed头部view
    private View mHeaderView;

    private TextView mTipMessage;

    private View mNetworkTip;

    private int scrollDataIndex = -1;//滚动数据的位置

    @SuppressLint("InflateParams")
    public HomeViewControler(MainHomePage activity) {
        this.mActivity = activity;
        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_home_header_layout, null, true);
        mHeaderView.setLayoutParams(new LinearLayout.LayoutParams(Tools.getPhoneWidth(), LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public void onCreate() {
        initUI();
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        mHeaderControler = new HomeHeaderControler(mHeaderView);

        mHomeFeedHeaderControler = new HomeFeedHeaderControler(mActivity);

        mTitleLayout = (HomeTitleLayout) mActivity.findViewById(R.id.home_title);
        mTitleLayout.setStatictusData(MainHomePage.STATICTUS_ID_PULISH, "顶部topbar", "");
        mTitleLayout.postDelayed(() -> {
            mBuoy = new HomeBuoy(mActivity);
            mBuoy.setClickCallback(() -> XHClick.mapStat(mActivity, MainHomePage.STATICTUS_ID_HOMEPAGE, "首页右侧侧边栏浮动图标", ""));
        }, 4000);

        mTipMessage = (TextView) mActivity.findViewById(R.id.tip_message);
        initNetworkTip();
        mRefreshLayout = (PtrClassicFrameLayout) mActivity.findViewById(R.id.refresh_list_view_frame);
        mRefreshLayout.disableWhenHorizontalMove(true);
        mRefreshLayout.setLoadingMinTime(300);
        recyclerView = (RvStaggeredGridView) mActivity.findViewById(R.id.recyclerView);
        recyclerView.closeDefaultAnimator();
        recyclerView.addHeaderView(mHeaderView);
        recyclerView.addHeaderView(mHomeFeedHeaderControler.getLayout());
        recyclerView.setOnItemClickListener((view, holder, position) -> {
            if (view instanceof HomeItem) {
                ((HomeItem) view).onClickEvent(view);
            }
        });
        recyclerView.addItemDecoration(new GridSpacingItemDecoration());

        //设置活动icon点击
        mTitleLayout.setOnClickActivityIconListener((v, url) -> {
            if (TextUtils.isEmpty(url)) return;
            AppCommon.openUrl(mActivity, url, true);
        });
    }

    public void addOnScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.i("xianghaTag","recyclerView::::-------------------  state = " + newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (mBuoy != null && !mBuoy.isMove()) {
                        mBuoy.executeOpenAnim();
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (mBuoy != null && mBuoy.isMove())
                        mBuoy.executeCloseAnim();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    //
    public void setHeaderData(List<Map<String, String>> data, boolean isShowCache) {
        if (data == null || data.isEmpty()) {
            return;
        }
        if (isShowCache) {
            Stream.of(data).forEach(map -> {
                Map<String, String> temp = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_WIDGET_DATA));
                map.put("cache", "2".equals(temp.get("appFixed")) ? "2" : "1");
            });
        }
        mHeaderControler.setData(data, isShowCache);
    }

    public void setTopData(List<Map<String, String>> data) {
        mHomeFeedHeaderControler.setTopData(data);
    }

    //回到第一个位置
    public void returnListTop() {
        if (recyclerView != null) {
            recyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    public void setFeedheaderVisibility(boolean isShow) {
        mHeaderControler.setFeedheaderVisibility(isShow);
    }

    public void setFeedTitleText(String text) {
        mHeaderControler.setFeedTitleText(text);
    }

    /**
     * 保存刷新数据
     */
    public void setStatisticShowNum() {
        //头部统计数据存储
        if (mHeaderControler != null) {
            mHeaderControler.saveStatisticData("home");
        }
        int[] lastPositions = null;
        StaggeredGridLayoutManager staggeredGridLayoutManager
                = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
        if (lastPositions == null) {
            lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
        }
        staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
        int lastVisibleItemPosition = Tools.findMax(lastPositions);
        if (scrollDataIndex < (lastVisibleItemPosition - 1)) {
            scrollDataIndex = (lastVisibleItemPosition - 1);
        }
        //列表
        if (scrollDataIndex > 0) {
            XHClick.saveStatictisFile("home", MainHomePage.recommedType_statictus, "", "", String.valueOf(scrollDataIndex), "list", "", "", "", "", "");
            scrollDataIndex = -1;
        }
    }

    public void setTipMessage() {
        OnlineConfigControler.getInstance().getConfigByKey(
                OnlineConfigControler.KEY_HOMENOTICE,
                this::initTipMessage
        );
    }

    //初始化tip数据
    private void initTipMessage(String configData) {
        Map<String, String> data = StringManager.getFirstMap(configData);
        if (null == data || data.isEmpty() || !"2".equals(data.get("isShow"))) {
            mTipMessage.setVisibility(View.GONE);
            return;
        }
        //获取文本
        String textValue = data.get("text");
        if (TextUtils.isEmpty(textValue)) {
            return;
        }
        mTipMessage.setText(textValue);
        //设置背景颜色
        String bgColorValue = data.get("backColor");
        mTipMessage.setBackgroundColor(WidgetUtility.parseColor(bgColorValue));
        //设置文本颜色
        String textColorValue = data.get("textColor");
        mTipMessage.setTextColor(WidgetUtility.parseColor(textColorValue));
        mTipMessage.setOnClickListener(v -> openUri(data.get("type"), data.get("clickUrl")));
        mTipMessage.setVisibility(View.VISIBLE);
        //隐藏
        hindNetworkTip();
    }

    private void openUri(String type, String clickUrl) {
        if (TextUtils.isEmpty(clickUrl) || null == mActivity) return;
        switch (type) {
            //原生打开H5
            case "1":
                Intent showWeb = new Intent(mActivity, ShowWeb.class);
                showWeb.putExtra("url", clickUrl);
                mActivity.startActivity(showWeb);
                break;
            //外部吊起
            case "2":
                AppCommon.openUrl(mActivity, clickUrl, true);
                break;
            //默认浏览器
            case "3":
                Intent intentLink = new Intent();
                intentLink.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(clickUrl);
                intentLink.setData(content_url);
                mActivity.startActivity(intentLink);
                break;
            //跳转其他App
            case "4":
                //包名
                try {
                    Intent launchIntent = mActivity.getPackageManager().getLaunchIntentForPackage(clickUrl);
                    if (null != launchIntent) {
                        mActivity.startActivity(launchIntent);
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            default:
                AppCommon.openUrl(mActivity, clickUrl, true);
        }
    }

    //初始化网络提示
    private void initNetworkTip() {
        mNetworkTip = mActivity.findViewById(R.id.network_tip_view);
        mNetworkTip.setOnClickListener(v -> gotoSetting());
    }

    //去设置
    private void gotoSetting() {
        mActivity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
    }

    //隐藏网络提示
    public void hindNetworkTip() {
        if (null != mNetworkTip) {
            mNetworkTip.setVisibility(View.GONE);
        }
    }

    //显示网络提示
    public void showNetworkTip() {
        if (null != mNetworkTip) {
            if (mTipMessage != null && mTipMessage.getVisibility() == View.VISIBLE) {
                hindNetworkTip();
                return;
            }
            mNetworkTip.setVisibility(View.VISIBLE);
        }
    }

    public void refreshComplete() {
        if (null != mRefreshLayout)
            mRefreshLayout.refreshComplete();
    }

    public void autoRefresh() {
        if (null != mRefreshLayout)
            mRefreshLayout.autoRefresh();
    }

    public void refreshBuoy() {
        if (mBuoy != null) {
            mBuoy.resetData();
        }
    }

    public void setMessage(int messageTipCount) {
        if (mTitleLayout != null) {
            mTitleLayout.setMessage(messageTipCount);
        }
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public RvListView getRvListView() {
        return recyclerView;
    }

    public PtrClassicFrameLayout getRefreshLayout() {
        return mRefreshLayout;
    }

    @Override
    public void setAdController(XHAllAdControl controller) {
        mHeaderControler.setAdController(controller);
    }

    public void setAdData(Map<String, String> map, List<String> adIDs, boolean refresh) {
        mHeaderControler.setAdID(adIDs);
        mHeaderControler.setAdData(map, refresh);
    }

    private int mLastShowPosition;

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        public GridSpacingItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

//            int spanIndex = 0;
//
//            RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
//            int paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;
//            int adapterPos = holder.getAdapterPosition();
//
//
//            if (holder instanceof HomeAdapter.StaggeredGridImageViewHolder || holder instanceof HomeAdapter.GridADImageViewHolder) {
//
//                if (mPositionSpanCache.containsKey(adapterPos)) {
//                    spanIndex = mPositionSpanCache.get(adapterPos);
//                } else {
//                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
//                    spanIndex = params.getSpanIndex();
////                if (mPositionSpanCache.size() < mCacheSize) {
//                    mPositionSpanCache.put(adapterPos, spanIndex);
////                }
//                }
//
////                if (adapterPos >= mLastShowPosition) {
////                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
////                    spanIndex = params.getSpanIndex();
////                }
//
//                if (mLR1 == 0 && mLR2 == 0 && mTB1 == 0 && mTB2 == 0) {
//                    if (holder instanceof HomeAdapter.StaggeredGridImageViewHolder) {
//                        HomeAdapter.StaggeredGridImageViewHolder staggeredGridImageViewHolder = (HomeAdapter.StaggeredGridImageViewHolder) holder;
//                        paddingLeft = staggeredGridImageViewHolder.homeStaggeredGridItemContentLayout.getPaddingLeft();
//                        paddingTop = staggeredGridImageViewHolder.homeStaggeredGridItemContentLayout.getPaddingTop();
//                        paddingRight = staggeredGridImageViewHolder.homeStaggeredGridItemContentLayout.getPaddingRight();
//                        paddingBottom = staggeredGridImageViewHolder.homeStaggeredGridItemContentLayout.getPaddingBottom();
//                    } else if (holder instanceof HomeAdapter.GridADImageViewHolder) {
//                        HomeAdapter.GridADImageViewHolder gridADImageViewHolder = (HomeAdapter.GridADImageViewHolder) holder;
//                        paddingLeft = gridADImageViewHolder.homeGirdAdItemContentLayout.getPaddingLeft();
//                        paddingTop = gridADImageViewHolder.homeGirdAdItemContentLayout.getPaddingTop();
//                        paddingRight = gridADImageViewHolder.homeGirdAdItemContentLayout.getPaddingRight();
//                        paddingBottom = gridADImageViewHolder.homeGirdAdItemContentLayout.getPaddingBottom();
//                    }
//
//                    mLR1 = Math.max(mBigSpacing - paddingLeft, 0);
//                    mLR2 = Math.max((mLittleSpacing - paddingLeft - paddingRight) / 2, 0);
//                    mTB1 = Math.max((mLittleSpacing - paddingTop - paddingBottom) / 2, 0);
//                    mTB2 = Math.max((mLittleSpacing - paddingTop - paddingBottom) / 2, 0);
//
//                }
//
//                if (holder instanceof HomeAdapter.StaggeredGridImageViewHolder) {
//                    Log.e("TAG", "getItemOffsets: " + spanIndex + "  pos = " + holder.getAdapterPosition() + "   title = " + ((HomeAdapter.StaggeredGridImageViewHolder) holder).view.getData().get("name"));
//                } else if (holder instanceof HomeAdapter.GridADImageViewHolder) {
//                    Log.e("TAG", "getItemOffsets: " + spanIndex + "  pos = " + holder.getAdapterPosition() + "   title = " + ((HomeAdapter.GridADImageViewHolder) holder).view.getData().get("name"));
//
//                }


//                switch (spanIndex) {
//                    case 0:
//                        outRect.set(mLR1, mTB1, mLR2, mTB2);
//                        break;
//                    case 1:
//                        outRect.set(mLR2, mTB1, mLR1, mTB2);
//                        break;
//                }
//            }

        }
    }

}
