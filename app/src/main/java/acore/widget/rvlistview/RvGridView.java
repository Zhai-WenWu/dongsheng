package acore.widget.rvlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

import com.xiangha.R;

/**
 * Description :
 * PackageName : acore.widget.rvlistview
 * Created on 2017/12/1 15:50.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class RvGridView extends RvListView {

    private int spanCount = 1;
    public RvGridView(Context context) {
        this(context,null);
    }

    public RvGridView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RvGridView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RvGridView);

        spanCount = array.getInt(R.styleable.RvGridView_spanCount, 1);
        //防止异常数据
        spanCount = spanCount > 1 ? spanCount : 1;

        array.recycle();
    }

    @Override
    public void setAdapter(@NonNull Adapter adapter) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),spanCount);
        setLayoutManager(layoutManager);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getSpanSizeInternal(position);
            }
        });
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
