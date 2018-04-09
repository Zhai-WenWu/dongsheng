package amodule.lesson.controler.view;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule._common.utility.WidgetUtility;
import amodule.lesson.view.info.LessonInfoHeader;
import amodule.lesson.view.info.LessonModuleView;
import amodule.vip.VIPButton;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

/**
 * Description :
 * PackageName : amodule.vip.controller.view
 * Created by tanze on 2018/3/29 17:10.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoUIMananger {

    private final BaseAppCompatActivity mActivity;

    private RelativeLayout mTopbar;
    private RvListView mRvListView;
    private LessonInfoHeader mInfoHeader;
    private LessonModuleView mHaFriendCommentView;
    private LessonModuleView mConentView;
    private LessonModuleView mGuessYouLikeView;
    private VIPButton mVIPButton;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private int mTopBarHeight = 0;
    private boolean hasNextItem = true;

    public LessonInfoUIMananger(BaseAppCompatActivity activity) {
        this.mActivity = activity;
        initializeData();
        initializeUI();

    }

    private void initializeData() {
        mTopBarHeight = Tools.getDimen(mActivity, R.dimen.dp_49) + Tools.getStatusBarHeight(mActivity);
    }

    private void initializeUI() {
        initTitle();
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mInfoHeader = new LessonInfoHeader(mActivity);
        mHaFriendCommentView = new LessonModuleView(mActivity);
        mHaFriendCommentView.setUseDefaultBottomPadding(true);
        mConentView = new LessonModuleView(mActivity);
        mGuessYouLikeView = new LessonModuleView(mActivity);
        mVIPButton = (VIPButton) mActivity.findViewById(R.id.vip_button);

        mRvListView.addHeaderView(mInfoHeader);
        mRvListView.addFooterView(mHaFriendCommentView);
        mRvListView.addFooterView(mConentView);
        mRvListView.addFooterView(mGuessYouLikeView);

        setClickListener();
        setScrollListener();
    }

    protected void initTitle() {
        mTopbar = (RelativeLayout) mActivity.findViewById(R.id.top_bar);
        String colorValue = mActivity.getResources().getString(R.color.common_top_bg);
       Tools.setStatusBarColor(mActivity,Color.parseColor(colorValue));
    }

    private void setClickListener() {
        mActivity.findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
    }

    public void setHeaderData(Map<String, String> headerData) {
        mInfoHeader.setData(headerData);
//        showNextItem();
    }

    public void setHaFriendCommentData(Map<String, String> data) {
        mHaFriendCommentView.setData(data);
    }

    public void setLessonContentData(Map<String, String> data) {
        mConentView.setData(data);
    }

    public void setGuessYouLikeData(Map<String, String> data) {
        mGuessYouLikeView.setData(data);
    }

    public void setVipButton(Map<String, String> data) {
        if (data == null || data.isEmpty() || mVIPButton == null) {
            return;
        }
        boolean isShow = "2".equals(data.get("vipButtonIsShow"));
        if (!isShow) {
            mVIPButton.setVisibility(View.GONE);
            return;
        }
        Map<String, String> vipButtonConfig = StringManager.getFirstMap(data.get("vipButton"));
        String titleValue = vipButtonConfig.get("title");
        String colorValue = vipButtonConfig.get("color");
        String bgColorValue = vipButtonConfig.get("bgColor");
        String url = vipButtonConfig.get("url");
        if (TextUtils.isEmpty(titleValue)
                || TextUtils.isEmpty(colorValue)
                || TextUtils.isEmpty(bgColorValue)) {
            return;
        }
        WidgetUtility.setTextToView(mVIPButton, titleValue);
        mVIPButton.setTextColor(Color.parseColor(colorValue));
        mVIPButton.setBackgroundColor(Color.parseColor(bgColorValue));
        mVIPButton.setOnClickListener(v -> AppCommon.openUrl(mActivity, url, true));
    }

    /** 设置滑动监听 */
    private void setScrollListener() {
        if (mOnScrollListener == null) {
            mOnScrollListener = new RecyclerView.OnScrollListener() {
                boolean alowLoad = true;
                int currentState;
                int totalDy = 0;

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    currentState = newState;
                    RecyclerView.Adapter adapter = mRvListView.getAdapter();
                    final LinearLayoutManager layoutManager = (LinearLayoutManager) mRvListView.getLayoutManager();
                    if (layoutManager != null && adapter != null) {
                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        if (newState == RecyclerView.SCROLL_STATE_IDLE
                                && lastVisibleItemPosition + 1 >= adapter.getItemCount() - 4) {
                            if (alowLoad) {
                                alowLoad = false;
                                if (currentState == SCROLL_STATE_IDLE
                                        && hasNextItem) {
//                                    hasNextItem = showNextItem();
                                }
                                new Handler().postDelayed(() -> alowLoad = true, 500);
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //更新topbar颜色
                    totalDy += dy;
                    updateTopbarBg(totalDy);
                }
            };
            mRvListView.addOnScrollListener(mOnScrollListener);
        }
    }

    private boolean showNextItem() {
        hasNextItem = mHaFriendCommentView.showNextItem()
                || mConentView.showNextItem()
                || mGuessYouLikeView.showNextItem();
        return hasNextItem;
    }

    private void updateTopbarBg(int dy) {
        Log.i("tzy", "updateTopbarBg: ");
        boolean isShow = dy <= mInfoHeader.getImageHeight() / 2 - mTopBarHeight;
        int colorRes = isShow ? R.color.transparent : R.color.common_top_bg;
        mTopbar.setBackgroundResource(colorRes);
    }

    private void onBackPressed() {
        if (mActivity != null) {
            mActivity.onBackPressed();
        }
    }

    public RvListView getRvListView() {
        return mRvListView;
    }

    public void refresh() {
        setHaFriendCommentData(new HashMap<>());
    }
}
