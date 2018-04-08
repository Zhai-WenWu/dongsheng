package amodule.lesson.controler.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.view.info.LessonInfoHeader;
import amodule.lesson.view.info.LessonModuleView;

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
        mConentView = new LessonModuleView(mActivity);
        mGuessYouLikeView = new LessonModuleView(mActivity);

        mRvListView.addHeaderView(mInfoHeader);
        mRvListView.addFooterView(mHaFriendCommentView);
        mRvListView.addFooterView(mConentView);
        mRvListView.addFooterView(mGuessYouLikeView);

        setClickListener();
        setScrollListener();
    }

    protected void initTitle() {
        mTopbar = (RelativeLayout) mActivity.findViewById(R.id.top_bar);
        if(Tools.isShowTitle()){
            Window window = mActivity.getWindow();
            //设置Window为全透明
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            //获取父布局
            View mContentChild = mContentView.getChildAt(0);
            //不预留系统栏位置
            if (mContentChild != null) {
                ViewCompat.setFitsSystemWindows(mContentChild, false);
            }
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mTopBarHeight);
            mTopbar.setLayoutParams(layout);
            mTopbar.setPadding(0, Tools.getStatusBarHeight(mActivity), 0, 0);
        }
    }

    private void setClickListener() {
        mActivity.findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
    }

    public void setHeaderData(Map<String, String> headerData) {
        mInfoHeader.setData(headerData);
    }

    public void setHaFriendCommentData(Map<String, String> data) {
        mHaFriendCommentView.setData(data);
//        showNextItem();
    }

    public void setLessonContentData(Map<String, String> data) {
        mConentView.setData(data);
    }

    public void setGuessYouLikeData(Map<String, String> data) {
        mGuessYouLikeView.setData(data);
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
                    if(layoutManager != null && adapter != null){
                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        if (newState == RecyclerView.SCROLL_STATE_IDLE
                                && lastVisibleItemPosition + 1 >= adapter.getItemCount() - 4) {
                            if (alowLoad) {
                                alowLoad = false;
                                if (currentState == SCROLL_STATE_IDLE
                                        && mRvListView.canScrollVertically(-1)
                                        && hasNextItem) {
                                    hasNextItem = showNextItem();
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
        return mHaFriendCommentView.showNextItem()
                || mConentView.showNextItem()
                || mGuessYouLikeView.showNextItem();
    }

    private void updateTopbarBg(int dy) {
        int colorRes = (dy <= mInfoHeader.getImageHeight()/2 - mTopBarHeight) ? R.color.transparent : R.color.common_top_bg;
        mTopbar.setBackgroundResource(colorRes);
        String colors = Tools.getColorStr(mActivity, colorRes);
//        Tools.setStatusBarColor(mActivity, Color.parseColor(colors));
    }

    private void onBackPressed() {
        if (mActivity != null) {
            mActivity.onBackPressed();
        }
    }

    public RvListView getRvListView() {
        return mRvListView;
    }
}
