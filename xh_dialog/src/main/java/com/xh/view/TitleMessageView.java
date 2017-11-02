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
 * Dialog内容的类，有Title样式的内容类型
 * Created by sll on 2017/10/30.
 */

public class TitleMessageView extends BaseView {

    private TextView mMessage;
    public TitleMessageView(Context context) {
        super(context, R.layout.title_message_layout);
    }

    public TitleMessageView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.title_message_layout);
    }

    public TitleMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.title_message_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TitleMessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.title_message_layout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mMessage = (TextView) findViewById(R.id.message);
    }

    public TitleMessageView setText (@Nullable CharSequence text) {
        mMessage.setText(text);
        return this;
    }

    public TitleMessageView setText(int resId) {
        mMessage.setText(resId);
        return this;
    }

    public TitleMessageView setTextBold(boolean bold) {
        mMessage.getPaint().setFakeBoldText(bold);
        return this;
    }

    public TitleMessageView setTextColor(int color) {
        mMessage.setTextColor(color);
        return this;
    }
}
