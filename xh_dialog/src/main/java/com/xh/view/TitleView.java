package com.xh.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xh.dialog.R;
import com.xh.view.base.BaseView;

/**
 * Dialog的标题类
 * Created by sll on 2017/10/30.
 */

public class TitleView extends BaseView {

    private TextView mTitle;
    public TitleView(Context context) {
        super(context, R.layout.title_layout);
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.title_layout);
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.title_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.title_layout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mTitle = (TextView) findViewById(R.id.title);
    }

    public TitleView setText (@Nullable CharSequence text) {
        mTitle.setText(text);
        return this;
    }

    public TitleView setText(int resId) {
        mTitle.setText(resId);
        return this;
    }

    public TitleView setTextBold(boolean bold) {
        mTitle.getPaint().setFakeBoldText(bold);
        return this;
    }

    public TitleView setTextColor(int color) {
        mTitle.setTextColor(color);
        return this;
    }
}
