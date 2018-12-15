package amodule.lesson.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.widget.rvlistview.RvListView;
import aplug.web.view.XHWebView;

public class SecondPagerWebAdapter extends PagerAdapter {
    private Activity mActivity;
    private List<String> mData;

    public SecondPagerWebAdapter(Context activity) {
        this.mActivity = (Activity) activity;
    }

    public void setData(List<String> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData != null && mData.size() > 0 ? mData.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        View convertView;
        if (position != 1) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.item_course_web, container, false);
            XHWebView mWebView = convertView.findViewById(R.id.webview);
            String url = mData.get(position);
//        mWebView.loadUrl(url);
            mWebView.loadUrl("https://www.baidu.com/");
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.setScrollChanged(new XHWebView.ScrollInterface() {
                @Override
                public void onSChanged(WebView webView, int l, int t, int oldl, int oldt) {
                    if (onSecondPagerScrollTopListener != null) {
                        if (webView.getScrollY() == 0) {
                            onSecondPagerScrollTopListener.onScrollToTop(true);
                        } else {
                            onSecondPagerScrollTopListener.onScrollToTop(false);
                        }
                    }
                }
            });
            container.addView(convertView);
            return convertView;
        } else {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.item_course_list, container, false);
            RvListView listView = convertView.findViewById(R.id.rv_list);
            ArrayList<Integer> askList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                askList.add(i);
            }
            listView.setAdapter(new StudyAskAdapter(mActivity, askList));
            listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (listView.canScrollVertically(-1)) {
                        onSecondPagerScrollTopListener.onScrollToTop(false);
                    } else {
                        onSecondPagerScrollTopListener.onScrollToTop(true);
                    }
                }
            });
            container.addView(convertView);
            return convertView;
        }

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private OnSecondPagerScrollTopListener onSecondPagerScrollTopListener;

    public void setOnSecondPagerScrollTopListener(OnSecondPagerScrollTopListener onSecondPagerScrollTopListener) {
        this.onSecondPagerScrollTopListener = onSecondPagerScrollTopListener;
    }

    public interface OnSecondPagerScrollTopListener {
        void onScrollToTop(boolean isScroolTop);
    }

}
