package amodule.lesson.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule.lesson.adapter.SecondPagerWebAdapter;
import aplug.web.view.XHWebView;

public class StudySecondPager extends RelativeLayout {

    private Context mContext;
    private View view;
    private XHWebView mWebView;
    private ViewPager mViewPager;
    private SecondPagerWebAdapter secondPagerWebAdapter;
    private List<String> mDataList = new ArrayList<>();

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public StudySecondPager(Context context) {
        this(context, null);
    }

    public StudySecondPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StudySecondPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        secondPagerWebAdapter = new SecondPagerWebAdapter(mContext);
        view = LayoutInflater.from(context).inflate(R.layout.view_second_pager, this, true);
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(secondPagerWebAdapter);
    }

    public void initData(Map<String, Map<String, String>> mData) {
        List<Map<String, String>> labelDataList = StringManager.getListMapByJson(mData.get("lessonInfo").get("labelData"));
        for (Map<String, String> label : labelDataList) {
            mDataList.add(label.get("url"));
        }
        secondPagerWebAdapter.setData(mDataList);
        secondPagerWebAdapter.notifyDataSetChanged();
    }

    public void setSelect(int currentItem) {
        mViewPager.setCurrentItem(currentItem);
    }

    public SecondPagerWebAdapter getSecondPagerWebAdapter() {
        return secondPagerWebAdapter;
    }
}
