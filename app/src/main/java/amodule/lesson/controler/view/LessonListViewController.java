package amodule.lesson.controler.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
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
import amodule._common.helper.WidgetDataHelper;
import amodule.home.viewholder.XHBaseRvViewHolder;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Description : //TODO
 * PackageName : amodule.lesson.controler.view
 * Created by mrtrying on 2017/12/19 11:23:38.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonListViewController {

    private BaseAppCompatActivity mAct;
    private TextView mTitleView;
    private RvGridView mGridView;
    private PtrClassicFrameLayout mPtrFrame;

    public LessonListViewController(BaseAppCompatActivity appCompatActivity) {
        mAct = appCompatActivity;
        if (mAct == null)
            return;
        initView();
    }

    private void initView() {
        mTitleView = (TextView) mAct.findViewById(R.id.title);
        mTitleView.setMaxWidth(ToolsDevice.getWindowPx(mAct).widthPixels - ToolsDevice.dp2px(mAct, 45 + 40));
        mPtrFrame = (PtrClassicFrameLayout) mAct.findViewById(R.id.ptr_frame);
        mGridView = (RvGridView) mAct.findViewById(R.id.rvGridView);
        final int padding_5 = Tools.getDimen(mAct, R.dimen.dp_5);
        final int padding_4 = Tools.getDimen(mAct, R.dimen.dp_4);
        mGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view) - mGridView.getHeaderViewsSize();
                outRect.top = (position == 0 || position == 1) ? padding_5 * 4 : padding_5;
                outRect.left = padding_4;
                outRect.right = padding_4;
                outRect.bottom = padding_5;
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
}
