package amodule.lesson.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.widget.DownRefreshList;
import acore.widget.KeyboardDialog;
import acore.widget.rvlistview.RvListView;
import amodule.comment.CommentListSave;
import amodule.comment.view.ViewCommentItem;
import amodule.lesson.view.CourseCommentItem;
import amodule.lesson.view.SecondPagerCommentView;
import amodule.user.activity.login.LoginByAccout;
import aplug.web.view.XHWebView;

public class SecondPagerAdapter extends PagerAdapter {
    private Activity mActivity;
    private List<String> mData;
    private ArrayList<Map<String, String>> mCommentList;
    private final int KEYBOARD_OPTION_COMMENT = 1;
    private final int KEYBOARD_OPTION_REPLAY = 2;
    private String currentUrl;
    private int mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
    private DownRefreshList listView;
    private String mReplayText;
    private String mCommentText;
    private SecondPagerCommentView mSecondPagerCommentView;

    public SecondPagerAdapter(Context activity, SecondPagerCommentView secondPagerCommentView) {
        this.mActivity = (Activity) activity;
        this.mSecondPagerCommentView = secondPagerCommentView;
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

            listView = mSecondPagerCommentView.getListView();
            mSecondPagerCommentView.getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
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
            container.addView(mSecondPagerCommentView);
            return mSecondPagerCommentView;
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
