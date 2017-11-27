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
 * Dialog内容的类，有自己的内容类型的样式
 * Created by sll on 2017/10/30.
 */

public class MessageView extends BaseView {

    private TextView mMessage;
    public MessageView(Context context) {
        super(context, R.layout.message_layout);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.message_layout);
    }

    public MessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.message_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.message_layout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mMessage = (TextView) findViewById(R.id.message);
    }

    public MessageView setText (@Nullable CharSequence text) {
        mMessage.setText(text);
        return this;
    }

    public MessageView setText(int resId) {
        mMessage.setText(resId);
        return this;
    }

    public MessageView setTextBold(boolean bold) {
        mMessage.getPaint().setFakeBoldText(bold);
        return this;
    }

    public MessageView setTextColor(int color) {
        mMessage.setTextColor(color);
        return this;
    }
}
