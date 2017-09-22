package com.popdialog.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.mrtrying.xh_popdialog.R;

public class XhDialog extends Dialog{

    public XhDialog(Context c) {
        super(c,R.style.dialog);
        setContentView(R.layout.xh_dialog);
    }

    public XhDialog setOnCancelListener(final OnXhDialogListener listen) {
        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                listen.onCancel(XhDialog.this);
            }
        });
        return this;
    }

    /**
     * 设置标题
     *
     * @param text ： 文字
     *
     * @return
     */
    public XhDialog setTitle(String text) {
        TextView tv = (TextView) findViewById(R.id.dialog_title);
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
        return this;
    }

    /**
     * 设置信息
     *
     * @param text ： 文字
     *
     * @return
     */
    public XhDialog setMessage(String text) {
        TextView tv = (TextView) findViewById(R.id.dialog_message);
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
        return this;
    }

    /**
     * 设置确定按钮
     *
     * @param text     ： 文字
     * @param listener ： 监听
     *
     * @return
     */
    public XhDialog setSureButton(String text, View.OnClickListener listener) {
        TextView tv = (TextView) findViewById(R.id.dialog_sure);
//		tv.setVisibility(View.VISIBLE);
        tv.setText(text);
        tv.setOnClickListener(listener);
        return this;
    }

    /**
     * 设置确定按钮文字颜色
     *
     * @param color
     *
     * @return
     */
    public XhDialog setSureButtonTextColor(String color) {
        TextView tv = (TextView) findViewById(R.id.dialog_sure);
        tv.setTextColor(Color.parseColor(color));
        return this;
    }

    /**
     * 设置取消按钮文字颜色
     *
     * @param color
     *
     * @return
     */
    public XhDialog setCancelButtonTextColor(String color) {
        TextView tv = (TextView) findViewById(R.id.dialog_cancel);
        tv.setTextColor(Color.parseColor(color));
        return this;
    }

    /**
     * 设置中立按钮
     *
     * @param text     ： 文字
     * @param listener ： 监听
     *
     * @return
     */
    public XhDialog setNegativeButton(String text, View.OnClickListener listener) {
        findViewById(R.id.dialog_negative_line).setVisibility(View.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.dialog_negative);
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
        tv.setOnClickListener(listener);
        return this;
    }

    /**
     * 设置取消按钮
     *
     * @param text     ： 文字
     * @param listener ： 监听
     *
     * @return
     */
    public XhDialog setCanselButton(String text, View.OnClickListener listener) {
        findViewById(R.id.dialog_sure_line).setVisibility(View.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.dialog_cancel);
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
        tv.setOnClickListener(listener);
        return this;
    }

    public interface OnXhDialogListener {
        void onCancel(XhDialog xhDialog);
    }
}

