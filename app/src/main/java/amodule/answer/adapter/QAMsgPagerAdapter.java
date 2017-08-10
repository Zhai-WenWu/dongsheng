package amodule.answer.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;

import acore.widget.PagerSlidingTabStrip;
import amodule.answer.fragment.QAMsgListFragment;
import amodule.answer.model.QAMsgModel;

/**
 * Created by sll on 2017/7/28.
 */

public class QAMsgPagerAdapter extends FragmentStatePagerAdapter implements PagerSlidingTabStrip.CustomTabProvider {

    private ArrayList<View> mTabViews = new ArrayList<View>();
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

    @Override
    public void onRemoveAllTabView() {
        mTabViews.clear();
    }

    @Override
    public View getCustomTabView(ViewGroup parent, int position) {
        TabHolder tabHolder = new TabHolder(parent.getContext());
        tabHolder.setData(position);
        mTabViews.add(tabHolder.mTabView);
        return tabHolder.mTabView;
    }

    @Override
    public void tabSelected(View tab) {

    }

    @Override
    public void tabUnselected(View tab) {

    }

    public void onPageSelected(int position) {
        if (mTabViews.isEmpty() || mTabViews.size() <= position || position < 0)
            return;
        TabHolder holder = (TabHolder) mTabViews.get(position).getTag();
        if (holder.mNumTextView.getVisibility() == View.VISIBLE)
            holder.mNumTextView.setVisibility(View.INVISIBLE);
    }

    private class TabHolder {
        public View mTabView;
        public TextView mNumTextView;
        public int mPosition;

        public TabHolder(Context context) {
            this.mTabView = LayoutInflater.from(context).inflate(R.layout.tab_strip_numlayout, null, false);
            mNumTextView = (TextView) this.mTabView.findViewById(R.id.num);
            this.mTabView.setTag(this);
        }

        public void setData(int position) {
            mPosition = position;
            if (mDatas != null && mDatas.size() > position) {
                String numStr = mDatas.get(position).getmMsgNum();
                try {
                    int num = Integer.parseInt(numStr);
                    if (num > 99) {
                        mNumTextView.setText(numStr + "+");
                    } else if (num > 0) {
                        mNumTextView.setText(numStr);
                    }
                    if (mDatas.get(position).ismIsSelect())
                        mNumTextView.setVisibility(View.INVISIBLE);
                    else
                        mNumTextView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
