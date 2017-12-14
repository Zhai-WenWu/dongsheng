package amodule.home.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;

import java.util.ArrayList;

import amodule.home.fragment.HomeSecondListFragment;
import amodule.home.module.HomeSecondModule;
import amodule.main.bean.HomeModuleBean;

/**
 * Created by sll on 2017/11/15.
 */

public class HomeSecondListPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<HomeSecondModule> mModules;
    private HomeModuleBean mHomeModuleBean;
    public HomeSecondListPagerAdapter(FragmentManager fm, ArrayList<HomeSecondModule> modules, HomeModuleBean homeModuleBean) {
        super(fm);
        mHomeModuleBean = homeModuleBean;
        mModules = modules;
    }

    @Override
    public Fragment getItem(int i) {
        HomeSecondListFragment fragment = HomeSecondListFragment.newInstance(mHomeModuleBean, i, mModules.get(i));
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mHomeModuleBean != null && TextUtils.equals("day", mHomeModuleBean.getType()))
            return "";
        return mModules.get(position).getTitle();
    }

    @Override
    public int getCount() {
        return mModules == null ? 0 : mModules.size();
    }

}
