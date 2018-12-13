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
    private List<String> mData = new ArrayList<>();
    public ViewHolder viewHolder;

    public SecondPagerWebAdapter(Context activity) {
        this.mActivity = (Activity) activity;
    }

    public void setData(List<String> data) {
        mData = data;
    }

    @Override
    public int getCount() {
//        return mData.size() > 0 ? mData.size() : 0;
        return 3;
    }

    public ViewHolder getViewHolder() {
        return viewHolder;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        View convertView = null;
        if (mData.size() == 0) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.item_course_web, container, false);
            XHWebView mWebView = convertView.findViewById(R.id.webview);
            viewHolder = new ViewHolder();
            viewHolder.mWebView = mWebView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String url = "https://www.baidu.com/";
        viewHolder.mWebView.loadUrl(url);
        viewHolder.mWebView.setWebViewClient(new WebViewClient());
        viewHolder.mWebView.setScrollChanged(new XHWebView.ScrollInterface() {
            @Override
            public void onSChanged(WebView webView,int l, int t, int oldl, int oldt) {
                if(mScrollInterface !=null){
                    mScrollInterface.onSChanged(webView,l, t, oldl, oldt);
                }
            }
        });
        ViewHolder finalViewHolder = viewHolder;
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


    public class ViewHolder {
        public XHWebView mWebView;

        public XHWebView getmWebView() {
            return mWebView;
        }
    }
}
