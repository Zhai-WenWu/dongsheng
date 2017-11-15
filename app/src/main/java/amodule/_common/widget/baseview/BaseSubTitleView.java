package amodule._common.widget.baseview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.delegate.IBindMap;

/**
 * Description : //TODO
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:49.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class BaseSubTitleView extends RelativeLayout implements IBindMap {

    protected Map<String, String> mData;
    public BaseSubTitleView(Context context, int layoutId) {
        super(context);
        inflateLayout(context, layoutId);
    }

    public BaseSubTitleView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        inflateLayout(context, layoutId);
    }

    public BaseSubTitleView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context, layoutId);
    }

    private void inflateLayout(Context context, int layoutId) {
        inflate(context, layoutId, this);
        initView();
    }

    protected abstract void initView();

    protected abstract void onDataReady(Map<String, String> map);

    @Override
    public void setData(Map<String, String> map) {
        mData = map;
        if (mData != null)
            onDataReady(mData);
    }

    public Map<String, String> getData() {
        return mData;
    }
}
