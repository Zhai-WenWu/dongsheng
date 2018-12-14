package amodule.lesson.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import amodule.lesson.view.StudyFirstPager;
import amodule.lesson.view.StudySecondPager;

public class VerticalAdapter extends PagerAdapter {
    private StudyFirstPager mStudyFirstPager;
    private StudySecondPager mStudySecondPager;


    @Override
    public int getCount() {
        return mStudyFirstPager != null && mStudySecondPager != null ? 2 : 0;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        if (position == 0 && mStudyFirstPager != null) {
            container.addView(mStudyFirstPager);
            return mStudyFirstPager;
        } else {
            container.addView(mStudySecondPager);
            return mStudySecondPager;
        }
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 将当前位置的View移除
        container.removeView((View) object);
    }

    public void setView(StudyFirstPager studyFirstPager, StudySecondPager studySecondPager) {
        this.mStudyFirstPager = studyFirstPager;
        this.mStudySecondPager = studySecondPager;
    }

}
