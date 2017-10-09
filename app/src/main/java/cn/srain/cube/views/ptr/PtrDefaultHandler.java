package cn.srain.cube.views.ptr;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

public abstract class PtrDefaultHandler implements PtrHandler {

    public static boolean canChildScrollUp(View view) {
//        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else if(view instanceof RecyclerView && ((RecyclerView) view).getLayoutManager() instanceof LinearLayoutManager){
                LinearLayoutManager layoutManager = (LinearLayoutManager) ((RecyclerView) view).getLayoutManager();
                return layoutManager.getChildCount() > 0
                        && (layoutManager.findFirstVisibleItemPosition() > 0 || layoutManager.getChildAt(0).getTop() < layoutManager.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
//        } else {
//            return view.canScrollVertically(-1);
//        }
    }

    /**
     * Default implement for check can perform pull to refresh
     *
     * @param frame
     * @param content
     * @param header
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