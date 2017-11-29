package acore.widget.banner;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * 无限轮播的viewPager适配器
 * Created by sivin on 2016/4/13.
 */
class SLooperAdapter extends PagerAdapter {
    private PagerAdapter mPagerAdapter;


    SLooperAdapter(PagerAdapter pagerAdapter) {
        mPagerAdapter = pagerAdapter;
    }

    @Override
    public int getCount() {
        //如果层ViewPager中有两个或两个以上的Item的时候，则映射出边界Item，否则显示与内层个数一致
        return mPagerAdapter.getCount() <= 1 ? mPagerAdapter.getCount() : mPagerAdapter.getCount() + 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return mPagerAdapter.isViewFromObject(view, object);
    }


    @Override
    public void startUpdate(ViewGroup container) {
        mPagerAdapter.startUpdate(container);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        return mPagerAdapter.instantiateItem(container, getInnerAdapterPosition(position));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        mPagerAdapter.destroyItem(container, getInnerAdapterPosition(position), object);
    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mPagerAdapter.setPrimaryItem(container, position, object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        mPagerAdapter.finishUpdate(container);
    }


    @Override
    public void notifyDataSetChanged() {
        mPagerAdapter.notifyDataSetChanged();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * 根据外层position的获取内层的position
     *
     * @param position 外层ViewPager的position
     * @return 外层viewPager当前数据位置对应的内层viewPager对应的位置。
     */
    int getInnerAdapterPosition(int position) {
        //viewPager真正的可用的个数
        int realCount = getInnerCount();
        //内层没有可用的Item则换回为零
        if (realCount == 0){

            return 0;
        }
        int realPosition = (position - 1) % realCount;
        if (realPosition < 0)
            realPosition += realCount;
        return realPosition;
    }

    /**
     * @return 内层ViewPager中可用的item个数
     */
    int getInnerCount() {
        return mPagerAdapter.getCount();
    }

    /**
     * 根据内层position的位置，返回映射后外层position的位置
     * @param position 内层position的位置
     * @return 无限轮播ViewPager的切换位置
     */
    int toLooperPosition(int position) {
        if (getInnerCount() > 1)
            return position + 1;
        else
            return position;
    }


}
