package amodule.lesson.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

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
        View convertView = mActivity.getLayoutInflater().inflate(R.layout.item_course_web, container, false);
        XHWebView mWebView = convertView.findViewById(R.id.webview);
        String url = mData.get(position);
//        mWebView.loadUrl(url);
        mWebView.loadUrl("https://www.baidu.com/");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setScrollChanged(new XHWebView.ScrollInterface() {
            @Override
            public void onSChanged(WebView webView, int l, int t, int oldl, int oldt) {
                if (mScrollInterface != null) {
                    mScrollInterface.onSChanged(webView, l, t, oldl, oldt);
                }
            }
        });
        container.addView(convertView);

        return convertView;

    }

    private XHWebView.ScrollInterface mScrollInterface;

    public void setmScrollInterface(XHWebView.ScrollInterface mScrollInterface) {
        this.mScrollInterface = mScrollInterface;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
