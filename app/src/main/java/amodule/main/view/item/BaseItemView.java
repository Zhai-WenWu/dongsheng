package amodule.main.view.item;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.Map;

/**
 * Created by sll on 2017/4/18.
 */

public class BaseItemView extends RelativeLayout {

    protected Map<String, String> mDataMap;
    public String viewType="";
    protected int mPosition = 0;
    public BaseItemView(Context context) {
        super(context);
    }

    public BaseItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(Map<String, String> dataMap, int position) {
        if (dataMap == null || dataMap.size() < 1)
            return;
        mDataMap = dataMap;
        mPosition = position;
    }

    /**
     * 设置数据类型
     * 必须在setData方法之前调用
     */
    public void setViewType(String type){
        this.viewType= type;
    }
    public Map<String, String> getData() {
        return mDataMap;
    }

    public interface OnItemClickListener{
        void onItemClick();
    }
}
