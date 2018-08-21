package acore.widget.rvlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.xiangha.R;

import acore.tools.ToolsDevice;


public class RvStaggeredGridView extends RvListView {

    protected int spanCount;
    protected boolean ignoreHaederPaddingLR;
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
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RvStaggeredGridView);
        spanCount = array.getInt(R.styleable.RvStaggeredGridView_spanCount, 1);
        ignoreHaederPaddingLR = array.getBoolean(R.styleable.RvStaggeredGridView_ignoreHaederPaddingLR, false);
        //防止异常数据
        spanCount = spanCount > 1 ? spanCount : 1;
        array.recycle();
    }

    @Override
    public void setAdapter(@NonNull Adapter adapter) {
        if (getLayoutManager() == null) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL){

                @Override
                public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
                    if(ignoreHaederPaddingLR && child == mHeaderContainer){
                        super.measureChildWithMargins(child, widthUsed-getPaddingLeft()-getPaddingRight(), heightUsed);
                    }else{
                        super.measureChildWithMargins(child, widthUsed, heightUsed);
                    }
                }

                @Override
                public void layoutDecorated(View child, int left, int top, int right, int bottom) {
                    if(ignoreHaederPaddingLR && child == mHeaderContainer){
                        super.layoutDecorated(child, 0, top, ToolsDevice.getWindowPx(getContext()).widthPixels, bottom);
                    }else{
                        super.layoutDecorated(child, left, top, right, bottom);
                    }
                }

                @Override
                public void layoutDecoratedWithMargins(View child, int left, int top, int right, int bottom) {
                    if(ignoreHaederPaddingLR && child == mHeaderContainer){
                        super.layoutDecorated(child, 0, top, ToolsDevice.getWindowPx(getContext()).widthPixels, bottom);
                    }else{
                        super.layoutDecoratedWithMargins(child, left, top, right, bottom);
                    }
                }
            };
            setLayoutManager(layoutManager);
        }
        super.setAdapter(adapter);
    }

    public int getSpanCount() {
        return spanCount;
    }

}
