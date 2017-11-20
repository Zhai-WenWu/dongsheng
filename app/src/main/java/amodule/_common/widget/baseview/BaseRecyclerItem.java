package amodule._common.widget.baseview;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

import acore.tools.Tools;
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

    /**
     * 根据原始宽高和间距动态计算等比宽高
     * @param originalW 原始宽
     * @param originalH 原始高
     * @param spaceW 空白间距
     * @return 两个元素的数组，0位置表示计算后的宽，1位置表示计算后的高
     */
    protected int[] computeItemWH(int originalW, int originalH, int spaceW, int showNum) {
        int[] wh = new int[2];
        if (originalW == 0 || originalH == 0 || showNum <= 0)
            return wh;
        double w = 1.0 * (Tools.getPhoneWidth() - spaceW) / showNum;
        wh[0] = (int) w;
        wh[1] = (int) (w / originalW * originalH);
        return wh;
    }
}
