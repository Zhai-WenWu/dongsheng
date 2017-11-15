package amodule._common.widget.baseview;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

import amodule.main.view.item.BaseItemView;

/**
 * Created by sll on 2017/11/14.
 */

public abstract class BaseRecyclerItem extends BaseItemView {

    public BaseRecyclerItem(Context context, int layoutId) {
        super(context);
        inflateLayout(layoutId);
    }

    public BaseRecyclerItem(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        inflateLayout(layoutId);
    }

    public BaseRecyclerItem(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        inflateLayout(layoutId);
    }

    private void inflateLayout(int layoutId) {
        inflate(getContext(), layoutId, this);
        initView();
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap != null)
            onDataReady(dataMap);
    }

    protected abstract void initView();

    protected abstract void onDataReady(Map<String, String> data);
}
