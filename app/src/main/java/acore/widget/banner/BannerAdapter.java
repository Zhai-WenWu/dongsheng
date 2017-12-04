package acore.widget.banner;

import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * bannerAdapter
 * Created by sivin on 2016/5/1.
 */
public abstract class BannerAdapter<T> {
    private List<T> mDataList;

    List<T> getDataList() {
        return mDataList;
    }

    protected BannerAdapter(List<T> dataList) {
        mDataList = dataList;
    }

    void setViewSource(View view, int position) {
        bindView(view, mDataList.get(position));
    }

    void selectTips(TextView tv, int position) {
        if (mDataList != null && mDataList.size() > 0)
            bindTips(tv, mDataList.get(position));
    }

    protected abstract void bindTips(TextView tv, T t);

    public abstract void bindView(View View, T t);

    public abstract View getView(int position);

}
