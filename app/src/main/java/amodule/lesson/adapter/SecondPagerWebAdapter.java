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
import amodule.user.activity.login.LoginByAccout;
import aplug.web.view.XHWebView;

public class SecondPagerWebAdapter extends PagerAdapter {
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
            convertView = mActivity.getLayoutInflater().inflate(R.layout.item_course_list, container, false);
            DownRefreshList listView = convertView.findViewById(R.id.comment_listview);
            initList(listView);
            TextView writeCommentTv = convertView.findViewById(R.id.commend_write_tv);

            writeCommentTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentUrl = StringManager.api_addForum;
                    mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
                    showCommentEdit();
                }
            });

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                    if (firstVisibleItem == 1) {
//                        onSecondPagerScrollTopListener.onScrollToTop(true);
//                    } else {
//                        onSecondPagerScrollTopListener.onScrollToTop(false);
//                    }
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

    private void initList(DownRefreshList listView) {
        mCommentList = CommentListSave.mList;
        AdapterSimple adapterSimple = new AdapterSimple(listView, mCommentList, R.layout.a_course_comment_item, new String[]{}, new int[]{}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CourseCommentItem viewCommentItem = (CourseCommentItem) view.findViewById(R.id.comment_item);

//                    viewCommentItem.setCommentItemListener(getCommentItenListener(viewCommentItem,position));
//                    viewCommentItem.setUserInforListenr(getUserInforListener());
                viewCommentItem.setData(mCommentList.get(position));
                return view;
            }
        };
        listView.setAdapter(adapterSimple);
        adapterSimple.notifyDataSetChanged();
    }

    private void showCommentEdit() {
        KeyboardDialog keyboardDialog = new KeyboardDialog(mActivity);
        keyboardDialog.init(R.layout.course_comment_keyboard_layout);
        keyboardDialog.setTextLength(50);
        if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
            keyboardDialog.setContentStr(mReplayText);
            if (TextUtils.isEmpty(mReplayText)) {
                keyboardDialog.setHintStr("回复:.....");
            }
        } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
            keyboardDialog.setContentStr(mCommentText);
        }
        keyboardDialog.setOnSendClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDialog.cancel();
                String sendText = keyboardDialog.getText();
                if (LoginManager.isLogin()) {
                    keyboardDialog.setContentStr(null);
                }
                sendData(sendText);
            }
        });
        keyboardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                String finalStr = keyboardDialog.getText();
                if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
                    mCommentText = finalStr;
                } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
                    mReplayText = finalStr;
                }
            }
        });
        keyboardDialog.show();
    }


    private void sendData(String sendText) {
        if (!LoginManager.isLogin()) {
            Intent intent = new Intent(mActivity, LoginByAccout.class);
            mActivity.startActivity(intent);
            return;
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
