package amodule.home.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.xiangha.R;

import acore.widget.rvlistview.RvGridView;
import amodule.main.adapter.HomeAdapter;

public class HomeGridView extends RvGridView {

    private String mListType;
    private GridLayoutManager mGridLayoutManager;
    private static final int FIXED_COLUMN_COUNT = 2;

    public HomeGridView(Context context) {
        super(context);
    }

    public HomeGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeGridView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initialize() {
        super.initialize();
        spanCount = FIXED_COLUMN_COUNT;
        mGridLayoutManager = new GridLayoutManager(getContext(), spanCount);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int size = getSpanSizeInternal(position);
                return size;
            }
        });
        setHasFixedSize(true);
        setLayoutManager(mGridLayoutManager);
        addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (TextUtils.equals(mListType, HomeAdapter.LIST_TYPE_GRID)) {
                    int pos = getChildAdapterPosition(view);
                    boolean hasHeader = getHeaderViewsSize() > 0;
                    int itemViewType = mAdapter.getItemViewType(pos);
                    boolean isCommon = itemViewType != VIEW_TYPE_HEADER && itemViewType != VIEW_TYPE_FOOTER && itemViewType != VIEW_TYPE_EMPTY;
                    int px20 = getPxFromDp(R.dimen.dp_10);
                    if (isCommon) {
                        outRect.set(hasHeader ? (pos % FIXED_COLUMN_COUNT == 1 ? px20 : px20 / 2) : (pos % FIXED_COLUMN_COUNT == 0 ? px20 : px20 / 2),
                                hasHeader ? (pos <= FIXED_COLUMN_COUNT ? px20 * 2 : px20 / 2) : (pos < FIXED_COLUMN_COUNT ? px20 * 2 : px20 / 2),
                                hasHeader ? (pos % FIXED_COLUMN_COUNT == 1 ? px20 / 2 : px20) : (pos % FIXED_COLUMN_COUNT == 0 ? px20 / 2 : px20),
                                px20 / 2);
                    } else {
                        outRect.set(0, 0, 0, 0);
                    }

                }
            }
        });
    }

    @Override
    public void setAdapter(@NonNull Adapter adapter) {
        super.setAdapter(adapter);
    }

    public void setListType(String listType) {
        mListType = listType;
    }

    @Override
    protected int getSpanSizeInternal(int position) {
        if (TextUtils.isEmpty(mListType))
            return spanCount;
        switch (mListType) {
            case HomeAdapter.LIST_TYPE_LIST:
                return spanCount;
            case HomeAdapter.LIST_TYPE_GRID:
                return super.getSpanSizeInternal(position);
            default:
                return spanCount;
        }
    }

    private int getPxFromDp(int resDp) {
        return getResources().getDimensionPixelSize(resDp);
    }
}
