package amodule.lesson.controler.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.view.info.HaFriendCommentView;
import amodule.lesson.view.info.LessonConentView;
import amodule.lesson.view.info.LessonGuessYouLikeView;
import amodule.lesson.view.info.LessonInfoHeader;

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
    private HaFriendCommentView mHaFriendCommentView;
    private LessonConentView mConentView;
    private LessonGuessYouLikeView mGuessYouLikeView;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private boolean hasNextItem = true;

    public LessonInfoUIMananger(BaseAppCompatActivity activity) {
        this.mActivity = activity;
        initializeUI();
    }

    private void initializeUI() {
        mTopbar = (RelativeLayout) mActivity.findViewById(R.id.top_bar);
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mInfoHeader = new LessonInfoHeader(mActivity);
        mHaFriendCommentView = new HaFriendCommentView(mActivity);
        mConentView = new LessonConentView(mActivity);
        mGuessYouLikeView = new LessonGuessYouLikeView(mActivity);

        mRvListView.addHeaderView(mInfoHeader);
        mRvListView.addFooterView(mHaFriendCommentView);
        mRvListView.addFooterView(mConentView);
        mRvListView.addFooterView(mGuessYouLikeView);

        setClickListener();
        setScrollListener();
    }

    private void setClickListener() {
        mActivity.findViewById(R.id.back_ll).setOnClickListener(v -> onBackPressed());
    }

    public void setHeaderData(Map<String, String> headerData) {
        mInfoHeader.setData(headerData);
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

    /** 设置滑动监听 */
    private void setScrollListener() {
        if (mOnScrollListener == null) {
            mOnScrollListener = new RecyclerView.OnScrollListener() {
                int currentState;

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    currentState = newState;
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (currentState == SCROLL_STATE_IDLE
                            && mRvListView.canScrollVertically(-1)
                            && hasNextItem) {
                        hasNextItem = showNextItem();
                    }
                    //更新topbar颜色
                    updateTopbarBg(dy);
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
        int colorRes = (dy <= 200) ? R.color.transparent : R.color.common_top_bg;
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
}
