package amodule.user.view;

import android.content.Context;
import android.util.AttributeSet;

import com.xiangha.R;

import java.util.Map;

/**
 * Created by sll on 2017/5/24.
 */

public class UserHomeVideoItem extends UserHomeTxtItem {
    public UserHomeVideoItem(Context context) {
        super(context);
    }

    public UserHomeVideoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserHomeVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        if (dataMap != null)
            dataMap.put("itemType", mItemType1);
        super.setData(dataMap, position);
    }

    @Override
    protected void bindData() {
        super.bindData();

    }

    @Override
    protected void resetData() {
        super.resetData();
    }

    @Override
    protected void resetView() {
        super.resetView();
    }
}
