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
        this(context,null);
    }

    public DelayLoadExtraLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DelayLoadExtraLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(List<Map<String, String>> array, boolean isOrder,boolean isCahe) {
        super.setData(array, isOrder,isCahe);
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
        if (datas == null || datas.isEmpty())
            return false;
//        for (int i = 0; i < mNextShowCount && i < datas.size(); i++) {
        for (;!datas.isEmpty();) {
            Map<String,String> map = datas.remove(0);
            updateModuleView(map,isOrder,false);
        }
        return !datas.isEmpty();
    }

    @Override
    protected boolean shouldParentHandleView() {
        return false;
    }
}
