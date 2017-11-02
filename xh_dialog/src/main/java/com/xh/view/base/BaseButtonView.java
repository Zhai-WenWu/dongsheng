package com.xh.view.base;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.xh.dialog.R;

/**
 * 弹框固有的Button基类，有三个按钮，分别是Positive、Neutral、Negative
 * Created by sll on 2017/10/30.
 */

public class BaseButtonView extends BaseView {

    private LinearLayout mPositiveContainer;
    private LinearLayout mNeutralContainer;
    private LinearLayout mNegativeContainer;
    private Button mPositiveBtn;
    private Button mNeutralBtn;
    private Button mNegativeBtn;
    private View mLine;

    public BaseButtonView(Context context, int layoutId) {
        super(context, layoutId);
    }

    public BaseButtonView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, layoutId);
    }

    public BaseButtonView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, layoutId);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int layoutId) {
        super(context, attrs, defStyleAttr, defStyleRes, layoutId);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mPositiveContainer = (LinearLayout) findViewById(R.id.positive_container);
        mNeutralContainer = (LinearLayout) findViewById(R.id.neutral_container);
        mNegativeContainer = (LinearLayout) findViewById(R.id.negative_container);
        mPositiveBtn = (Button) findViewById(R.id.positive_btn);
        mNeutralBtn = (Button) findViewById(R.id.neutral_btn);
        mNegativeBtn = (Button) findViewById(R.id.negative_btn);
        mLine = findViewById(R.id.line);
    }

    /**
     * 设置右侧（横向按钮）或者底部（纵向按钮）按钮文字及点击监听
     * @param text
     * @param positiveClickListener
     * @return
     */
    public BaseButtonView setPositiveText(@Nullable CharSequence text, OnClickListener positiveClickListener) {
        mPositiveBtn.setText(text);
        mPositiveContainer.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        if (positiveClickListener != null)
            mPositiveBtn.setOnClickListener(positiveClickListener);
        return this;
    }

    /**
     * 设置中间按钮的文字及点击监听
     * @param text
     * @param neutralClickListener
     * @return
     */
    public BaseButtonView setNeutralText(@Nullable CharSequence text, OnClickListener neutralClickListener) {
        mNeutralBtn.setText(text);
        mNeutralContainer.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        if (neutralClickListener != null)
            mNeutralBtn.setOnClickListener(neutralClickListener);
        return this;
    }

    /**
     *
     * @param text
     * @param negativeClickListener
     * @return
     */
    public BaseButtonView setNegativeText(@Nullable CharSequence text, OnClickListener negativeClickListener) {
        mNegativeBtn.setText(text);
        mNegativeContainer.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        if (negativeClickListener != null)
            mNegativeBtn.setOnClickListener(negativeClickListener);
        return this;
    }

    public BaseButtonView setPositiveText(int resId, OnClickListener positiveClickListener) {
        mPositiveBtn.setText(resId);
        mPositiveContainer.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        if (positiveClickListener != null)
            mPositiveBtn.setOnClickListener(positiveClickListener);
        return this;
    }

    public BaseButtonView setNeutralText(int resId, OnClickListener neutralClickListener) {
        mNeutralBtn.setText(resId);
        mNeutralContainer.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        if (neutralClickListener != null)
            mNeutralBtn.setOnClickListener(neutralClickListener);
        return this;
    }

    public BaseButtonView setNegativeText(int resId, OnClickListener negativeClickListener) {
        mNegativeBtn.setText(resId);
        mNegativeContainer.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        if (negativeClickListener != null)
            mNegativeBtn.setOnClickListener(negativeClickListener);
        return this;
    }

    public BaseButtonView setPositiveTextColor(int color) {
        mPositiveBtn.setTextColor(color);
        return this;
    }

    public BaseButtonView setNeutralTextColor(int color) {
        mNeutralBtn.setTextColor(color);
        return this;
    }

    public BaseButtonView setNegativeTextColor(int color) {
        mNegativeBtn.setTextColor(color);
        return this;
    }

    public BaseButtonView setPositiveTextBold(boolean bold) {
        mPositiveBtn.getPaint().setFakeBoldText(bold);
        return this;
    }

    public BaseButtonView setNeutralTextBold(boolean bold) {
        mNeutralBtn.getPaint().setFakeBoldText(bold);
        return this;
    }

    public BaseButtonView setNegativeTextBold(boolean bold) {
        mNegativeBtn.getPaint().setFakeBoldText(bold);
        return this;
    }
}
