package com.xh.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.xh.dialog.R;
import com.xh.view.base.BaseButtonView;
import com.xh.view.base.BaseView;

/**
 * Dialog按钮，纵向显示，从上至下依次为Negative、Neutral、Positive三个Button
 * 如果只显示一个Button的话，建议设置Negative的，因为另外两个会有分割线的显示。
 * Created by sll on 2017/10/30.
 */

public class VButtonView extends BaseButtonView {
    public VButtonView(Context context) {
        super(context, R.layout.vbutton_layout);
    }

    public VButtonView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.vbutton_layout);
    }

    public VButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.vbutton_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.vbutton_layout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
    }
}
