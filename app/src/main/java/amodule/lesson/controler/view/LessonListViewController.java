package amodule.lesson.controler.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvGridView;
import acore.widget.rvlistview.RvListView;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import amodule.home.viewholder.XHBaseRvViewHolder;
import amodule.vip.VIPButton;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Description :
 * PackageName : amodule.lesson.controler.view
 * Created by mrtrying on 2017/12/19 11:23:38.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonListViewController {

    private BaseAppCompatActivity mAct;
    private TextView mTitleView;
    private RvGridView mGridView;
    private PtrClassicFrameLayout mPtrFrame;
    private VIPButton mVIPButton;

    private StatisticCallback mContentStatisticCallback;

    private int mFirstRawTopPadding;
    private int mTopPadding;
    private int mLeftPadding;
    private int mRightPadding;
    private int mBottomPadding;

    public LessonListViewController(BaseAppCompatActivity appCompatActivity, String style) {
        mAct = appCompatActivity;
        if (mAct == null)
            return;
        initData(style);
        initView();
    }

    private void initData(String style) {
        int padding_5 = Tools.getDimen(mAct, R.dimen.dp_5);
        int padding_4 = Tools.getDimen(mAct, R.dimen.dp_4);
        mFirstRawTopPadding = padding_5 * 4;
        mTopPadding = padding_5;
        mLeftPadding = padding_4;
        mRightPadding = padding_4;
        mBottomPadding = padding_5;
        if (TextUtils.isEmpty(style))
            return;
        switch (style) {
            case "2":
            case "4":
                mBottomPadding = padding_4 + padding_5;
                break;
        }
    }

    private void initView() {
        mVIPButton = (VIPButton) mAct.findViewById(R.id.vip_button);
        mVIPButton.setVisibility(View.GONE);
        mTitleView = (TextView) mAct.findViewById(R.id.title);
        mTitleView.setMaxWidth(ToolsDevice.getWindowPx(mAct).widthPixels - ToolsDevice.dp2px(mAct, 45 + 40));
        mTitleView.setVisibility(View.GONE);
        mPtrFrame = (PtrClassicFrameLayout) mAct.findViewById(R.id.ptr_frame);
        mGridView = (RvGridView) mAct.findViewById(R.id.rvGridView);
        mGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view) - mGridView.getHeaderViewsSize();
                outRect.top = (position == 0 || position == 1) ? mFirstRawTopPadding : mTopPadding;
                outRect.left = mLeftPadding;
                outRect.right = mRightPadding;
                outRect.bottom = mBottomPadding;
            }
        });
        mGridView.setOnItemClickListener((view, holder, position) -> {
            if (holder != null && holder instanceof XHBaseRvViewHolder) {
                XHBaseRvViewHolder viewHolder = (XHBaseRvViewHolder) holder;
                Map<String, String> data = viewHolder.getData();
                if (data == null || data.isEmpty())
                    return;
                String url = data.get(WidgetDataHelper.KEY_URL);
                AppCommon.openUrl(mAct, url, true);
                if (mContentStatisticCallback != null)
                    mContentStatisticCallback.onStatistic("", "", "", -1);
            }
        });
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onDestroy() {
        mGridView.stopScroll();
        mAct = null;
        mPtrFrame = null;
        mGridView = null;
        mTitleView = null;
        if (mVIPButton != null) {
            mVIPButton.onDestroy();
            mVIPButton = null;
        }
    }

    public PtrClassicFrameLayout getPtrFrame() {
        return mPtrFrame;
    }

    public RvListView getListView() {
        return mGridView;
    }

    public void setTitle(String title) {
        if (mTitleView != null)
            mTitleView.setText(title);
    }

    public void refreshComplete() {
        if (mPtrFrame != null)
            mPtrFrame.refreshComplete();
    }

    public boolean titleShowing() {
        return mTitleView != null && mTitleView.getVisibility() == View.VISIBLE;
    }

    public void setTitleViewVisibility(int visibility) {
        if (mTitleView != null)
            mTitleView.setVisibility(visibility);
    }

    public void setVIPButton(String text, int textColor, int bgColor) {
        if (mVIPButton != null) {
            mVIPButton.setText(text);
            mVIPButton.setTextColor(textColor);
            mVIPButton.setBackgroundColor(bgColor);
        }
    }

    public void setVIPButtonVisibility(int visibility) {
        if (mVIPButton != null)
            mVIPButton.setVisibility(visibility);
    }

    public void setVIPButtonClickListener(View.OnClickListener listener) {
        if (listener != null && mVIPButton != null)
            mVIPButton.setBtnClickListener(listener);
    }

    public void setContentStatisticCallback(StatisticCallback callback) {
        mContentStatisticCallback = callback;
    }

    public void setVIPBtnStatisticCallback(StatisticCallback callback) {
        if (callback != null && mVIPButton != null)
            mVIPButton.setStatisticCallback(callback);
    }
}
