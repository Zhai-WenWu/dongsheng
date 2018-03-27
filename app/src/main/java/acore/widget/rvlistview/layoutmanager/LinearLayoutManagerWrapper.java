package acore.widget.rvlistview.layoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Description :
 * PackageName : acore.widget.rvlistview
 * Created by MrTrying on 2017/11/6 18:25.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class LinearLayoutManagerWrapper extends LinearLayoutManager {

    private boolean isCareshed = false;
    public LinearLayoutManagerWrapper(Context context) {
        super(context);
    }

    public LinearLayoutManagerWrapper(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public LinearLayoutManagerWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try{
            super.onLayoutChildren(recycler, state);
        }catch (Exception igroned){
            isCareshed = true;
            igroned.printStackTrace();
        }
    }

    @Override
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        try{
            super.measureChildWithMargins(child, widthUsed, heightUsed);
        }catch (Exception igorned){
            isCareshed = true;
            igorned.printStackTrace();
        }
    }

    public boolean isCareshed() {
        return isCareshed;
    }
}
