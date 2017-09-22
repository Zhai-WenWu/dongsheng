package com.popdialog.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mrtrying.xh_popdialog.R;


/**
 * Created by XiangHa on 2017/2/24.
 */
public class PopDialog extends Dialog {

    private OnClickStatisticsCallback onClickStatisticsCallback;

    public PopDialog(Context context, int layoutId) {
        super(context, R.style.dialog);
        setContentView(layoutId);
    }

    /**
     * 设置标题
     *
     * @param title
     * @param titleColor
     *
     * @return
     */
    public PopDialog setTitle(String title, String titleColor) {
        TextView titleTv = (TextView) findViewById(R.id.dialog_title);
        titleTv.setText(title);
        if (!TextUtils.isEmpty(titleColor)) {
            titleTv.setTextColor(Color.parseColor(titleColor));
        }
        return this;
    }

    /**
     * 设置文本
     *
     * @param message
     * @param messageColor
     *
     * @return
     */
    public PopDialog setMessage(String message, String messageColor) {
        TextView messageTv = (TextView) findViewById(R.id.dialog_message);
        messageTv.setText(message);
        if (!TextUtils.isEmpty(messageColor)) {
            messageTv.setTextColor(Color.parseColor(messageColor));
        }
        return this;
    }

    /**
     * 设置取消
     *
     * @param text      文本
     * @param textColor 文本颜色
     * @param isBold    是否加粗
     * @param listener  点击
     *
     * @return
     */
    public PopDialog setCanselButton(final String text, String textColor, boolean isBold, final View.OnClickListener listener) {
        TextView cancelTv = (TextView) findViewById(R.id.dialog_cancel);
        cancelTv.setText(text);
        if (!TextUtils.isEmpty(textColor)) {
            cancelTv.setTextColor(Color.parseColor(textColor));
        }
        if (isBold) {
            TextPaint paint = cancelTv.getPaint();
            paint.setFakeBoldText(true);
        }
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                if (onClickStatisticsCallback != null) {
                    onClickStatisticsCallback.onCannelStatistics(text);
                }
            }
        });
        return this;
    }

    /**
     * 设置确认
     *
     * @param text      文本
     * @param textColor 文本颜色
     * @param isBold    是否加粗
     * @param listener  点击
     *
     * @return
     */
    public PopDialog setSureButton(final String text, String textColor, boolean isBold, final View.OnClickListener listener) {
        TextView sureTv = (TextView) findViewById(R.id.dialog_sure);
        sureTv.setText(text);
        if (!TextUtils.isEmpty(textColor)) {
            sureTv.setTextColor(Color.parseColor(textColor));
        }
        if (isBold) {
            TextPaint paint = sureTv.getPaint();
            paint.setFakeBoldText(true);
        }
        sureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                if (onClickStatisticsCallback != null) {
                    onClickStatisticsCallback.onSureStatistics(text);
                }
            }
        });
        return this;
    }

    public interface OnClickStatisticsCallback {
        void onSureStatistics(String text);

        void onCannelStatistics(String text);
    }

    /*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public OnClickStatisticsCallback getOnClickStatisticsCallback() {
        return onClickStatisticsCallback;
    }

    public void setOnClickStatisticsCallback(OnClickStatisticsCallback onClickStatisticsCallback) {
        this.onClickStatisticsCallback = onClickStatisticsCallback;
    }
}
