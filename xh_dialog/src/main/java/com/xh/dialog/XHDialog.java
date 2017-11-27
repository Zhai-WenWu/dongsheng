package com.xh.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xh.deleget.DialogLifecycleDelegate;

/**
 * Created by sll on 2017/10/26.
 */

public class XHDialog extends Dialog {

    private Context mContext;

    private DialogLifecycleDelegate mDelegate;

    private RelativeLayout mRootContainer;
    private LinearLayout mInnerContainer;
    private LinearLayout mInnerMsgContainer;
    private LinearLayout mInnerBtnContainer;
    private View mEmpty1;
    private View mEmpty2;

    public XHDialog(@Nullable Context context, DialogLifecycleDelegate delegate) {
        this(context);
        mDelegate = delegate;
    }

    public XHDialog(@NonNull Context context) {
        this(context, R.style.XHDialog);
    }

    public XHDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initView(context);
    }

    protected XHDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mDelegate != null) {
            mDelegate.onCreate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDelegate != null) {
            mDelegate.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDelegate != null) {
            mDelegate.onStop();
        }
    }

    /**
     * 初始化
     */
    private void initView(Context context) {
        mContext = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_content_layout, null);
        setContentView(contentView);
        mRootContainer = (RelativeLayout) contentView.findViewById(R.id.root_container);
        mInnerContainer = (LinearLayout) contentView.findViewById(R.id.inner_container);
        mInnerMsgContainer = (LinearLayout) contentView.findViewById(R.id.inner_msg_container);
        mInnerBtnContainer = (LinearLayout) contentView.findViewById(R.id.inner_btn_container);
        mEmpty1 = findViewById(R.id.empty1);
        mEmpty2 = findViewById(R.id.empty2);
    }

    /**
     * 去掉原有的纵向间距
     */
    public void noPadding() {
        mEmpty1.setVisibility(View.GONE);
        mEmpty2.setVisibility(View.GONE);
    }

    public LinearLayout getInnerContainer() {
        return mInnerContainer;
    }

    public LinearLayout getInnerMsgContainer() {
        return mInnerMsgContainer;
    }

    public LinearLayout getInnerBtnContainer() {
        return mInnerBtnContainer;
    }

    public RelativeLayout getRootContainer() {
        return mRootContainer;
    }

}
