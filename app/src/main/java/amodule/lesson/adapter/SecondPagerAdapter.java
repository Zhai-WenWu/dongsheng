package amodule.lesson.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;

import com.xiangha.R;

import java.util.List;

import acore.widget.DownRefreshList;
import amodule.lesson.view.CourseCommentView;
import aplug.web.view.XHWebView;

public class SecondPagerAdapter extends PagerAdapter {
    private Activity mActivity;
    private List<String> mData;
    private DownRefreshList listView;
    private int mCommentIndex;
    private CourseCommentView mCourseCommentView;

    public SecondPagerAdapter(Context activity, CourseCommentView courseCommentView) {
        this.mActivity = (Activity) activity;
        this.mCourseCommentView = courseCommentView;
    }

    public void setData(List<String> data, int commentIndex) {
        mData = data;
        mCommentIndex = commentIndex;
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
        if (position != mCommentIndex) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.item_course_web, container, false);
            XHWebView mWebView = convertView.findViewById(R.id.webview);
            String url = mData.get(position);
            mWebView.loadUrl(url);
//            mWebView.loadUrl("https://www.baidu.com/");
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

            listView = mCourseCommentView.getListView();
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (listView.canScrollVertically(-1)) {
                        onSecondPagerScrollTopListener.onScrollToTop(false);
                    } else {
                        onSecondPagerScrollTopListener.onScrollToTop(true);
                    }
                }
            });
            container.addView(mCourseCommentView);
            return mCourseCommentView;
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
