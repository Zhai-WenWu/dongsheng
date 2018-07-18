package acore.widget.rvlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.xiangha.R;


public class RvStaggeredGridView extends RvListView {

    private int spanCount;
    public RvStaggeredGridView(Context context) {
        this(context,null);
    }

    public RvStaggeredGridView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RvStaggeredGridView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initializeAttr(AttributeSet attrs) {
        super.initializeAttr(attrs);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RvGridView);
        spanCount = array.getInt(R.styleable.RvGridView_spanCount, 1);
        //防止异常数据
        spanCount = spanCount > 1 ? spanCount : 1;
        array.recycle();
    }

    @Override
    public void setAdapter(@NonNull Adapter adapter) {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
        super.setAdapter(adapter);
    }

    private int getSpanSizeInternal(int position){
        if(mAdapter != null){
            if(VIEW_TYPE_HEADER == mAdapter.getItemViewType(position)
                    ||VIEW_TYPE_FOOTER == mAdapter.getItemViewType(position)
                    ||VIEW_TYPE_EMPTY == mAdapter.getItemViewType(position)){
                return spanCount;
            }
        }
        return 1;
    }

}
