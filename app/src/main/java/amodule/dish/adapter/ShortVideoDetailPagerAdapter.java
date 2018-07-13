package amodule.dish.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

import amodule.dish.activity.ShortVideoDetailFragment;


public class ShortVideoDetailPagerAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    private ArrayList<Map<String, String>> mDatas;

    private ShortVideoDetailFragment.OnPlayPauseClickListener mOnPlayPauseListener;
    private ShortVideoDetailFragment.OnSeekBarTrackingTouchListener mOnSeekBarTrackingTouchListener;

    public ShortVideoDetailPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mDatas = new ArrayList<>();
    }

    public void setData(ArrayList<Map<String, String>> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        ShortVideoDetailFragment fragment = (ShortVideoDetailFragment) Fragment.instantiate(mContext, ShortVideoDetailFragment.class.getName());
        fragment.setData(mDatas.get(position));
        fragment.setPos(position);
        fragment.setOnPlayPauseListener(mOnPlayPauseListener);
        fragment.setOnSeekBarTrackingTouchListener(mOnSeekBarTrackingTouchListener);
        return fragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return super.isViewFromObject(view, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    public void setOnPlayPauseListener(ShortVideoDetailFragment.OnPlayPauseClickListener onPlayPauseListener) {
        mOnPlayPauseListener = onPlayPauseListener;
    }

    public void setOnSeekBarTrackingTouchListener(ShortVideoDetailFragment.OnSeekBarTrackingTouchListener onSeekBarTrackingTouchListener) {
        mOnSeekBarTrackingTouchListener = onSeekBarTrackingTouchListener;
    }

}
