package amodule.answer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import amodule.answer.fragment.QAMsgListFragment;
import amodule.answer.model.QAMsgModel;

/**
 * Created by sll on 2017/7/28.
 */

public class QAMsgPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<QAMsgModel> mDatas;

    public QAMsgPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setData(ArrayList<QAMsgModel> datas) {
        if (datas == null)
            return;
        mDatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mDatas.get(position).getmTitle();
    }

    @Override
    public Fragment getItem(int position) {
        if (mDatas == null || mDatas.isEmpty())
            return null;
        return QAMsgListFragment.newInstance(mDatas.get(position));
    }

    @Override
    public int getCount() {
        return (mDatas == null || mDatas.isEmpty()) ? 0 : mDatas.size();
    }
}
