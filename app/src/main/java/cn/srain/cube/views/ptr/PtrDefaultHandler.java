package cn.srain.cube.views.ptr;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import acore.widget.rvlistview.RvListView;

public abstract class PtrDefaultHandler implements PtrHandler {

    public static boolean canChildScrollUp(View view) {
        if (view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            return absListView.getChildCount() > 0
                    && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                    .getTop() < absListView.getPaddingTop());
        } else if (view instanceof RvListView) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) ((RecyclerView) view).getLayoutManager();
//            Log.i("tzy", "findFirstCompletelyVisibleItemPosition :: " + layoutManager.findFirstCompletelyVisibleItemPosition());
//            Log.i("tzy", "findFirstVisibleItemPosition :: " + layoutManager.findFirstVisibleItemPosition());
//            Log.i("tzy", "HeaderViewsSize :: " + ((RvListView) view).getHeaderViewsSize());
//            Log.i("tzy", "boolean :: " + (layoutManager.findFirstVisibleItemPosition() > (((RvListView) view).getHeaderViewsSize() == 0 ? 1 : 0))+" - false 能刷新");
            return layoutManager.getChildCount() > 0
                    && (layoutManager.findFirstVisibleItemPosition() > 0
                                || ((RecyclerView)view).canScrollVertically(-1));
        } else {
            return view.getScrollY() > 0;
        }
    }

    /**
     * Default implement for check can perform pull to refresh
     *
     * @param frame
     * @param content
     * @param header
     *
     * @return
     */
    public static boolean checkContentCanBePulledDown(PtrFrameLayout frame, View content, View header) {
        return !canChildScrollUp(content);
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return checkContentCanBePulledDown(frame, content, header);
    }
}
