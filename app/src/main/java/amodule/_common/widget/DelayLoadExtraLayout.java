package amodule._common.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.List;
import java.util.Map;

import amodule._common.widget.baseWidget.BaseExtraLinearLayout;

/**
 * Created by xiangha_android on 2018/3/30.
 */

public class DelayLoadExtraLayout extends BaseExtraLinearLayout {

    private boolean mIsDelayLoadData;
    private int mNextShowCount;
    public DelayLoadExtraLayout(Context context) {
        super(context);
    }

    public DelayLoadExtraLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayLoadExtraLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DelayLoadExtraLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setData(List<Map<String, String>> array, boolean isOrder) {
        // TODO: 2018/3/30 处理其他数据
        super.setData(array, isOrder);
    }

    public void setDelayLoadData(boolean delayed) {
        mIsDelayLoadData = delayed;
    }

    public void setNextShowCount(int count) {
        mNextShowCount = count;
    }

    public boolean showNextItem() {
        boolean ret = false;
        if (!mIsDelayLoadData)
            return ret;
        else if (hasNextData())
            ret = true;
        return ret;
    }

    private boolean hasNextData() {
        if (mDatas == null || mDatas.isEmpty())
            return false;
        for (int i = 0; i < mNextShowCount && i < mDatas.size(); i++) {
            Map<String,String> map = mDatas.remove(0);
            updateModuleView(map,true);
        }
        return !mDatas.isEmpty();
    }

    @Override
    protected boolean shouldParentHandleView() {
        return false;
    }
}
