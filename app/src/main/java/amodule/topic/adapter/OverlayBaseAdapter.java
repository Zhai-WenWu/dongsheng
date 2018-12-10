package amodule.topic.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class OverlayBaseAdapter<T> extends PagerAdapter {
    public List<T> mData = new ArrayList<>();

    public void setData(List<T> data) {
        mData = data;
    }

    public List<T> getmData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
//            int i = position % mData.size();
        int i = position;
        return overWriteInstantiateItem(container, i);
    }

    abstract public Object overWriteInstantiateItem(ViewGroup container, int position);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 将当前位置的View移除
        container.removeView((View) object);
    }

}
