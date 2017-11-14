package amodule._common.widget.horizontal;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.Map;

import acore.widget.rvlistview.RvListView;
import amodule._common.delegate.IBindMap;
import amodule._common.widget.SubTitleView;

/**
 * Description :
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HorizontalRecyclerView extends RelativeLayout implements IBindMap {

    private RvListView mRecyclerView;
    private SubTitleView mSubTitleView;
    public HorizontalRecyclerView(Context context) {
        super(context);
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> map) {

    }
}
