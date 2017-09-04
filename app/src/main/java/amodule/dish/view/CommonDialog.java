package amodule.dish.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by ：airfly on 2016/10/25 14:42.
 */

public class CommonDialog {

    private Context mCon;
    private Dialog dialog;
    private Window window;

    public CommonDialog(Context c) {
        mCon = c;
        dialog= new Dialog(c,R.style.dialog);
        dialog.setContentView(R.layout.c_loading_dialog);
        window = dialog.getWindow();
    }

    public CommonDialog(Context c, int contentResId) {
        mCon = c;
        dialog = new Dialog(c, R.style.dialog);
        dialog.setContentView(contentResId);
        window = dialog.getWindow();
    }

    public void show() {
        dialog.show();
    }

    public void cancel() {
        dialog.cancel();
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }

    /**
     * 设置标题
     *
     * @param text ： 文字
     * @return
     */
    public CommonDialog setTitle(String text) {
        TextView tv = (TextView) window.findViewById(R.id.dialog_title);
        if (tv != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
        return this;
    }

    /**
     * 设置信息
     *
     * @param text ： 文字
     * @return
     */
    public CommonDialog setMessage(String text) {
        TextView tv = (TextView) window.findViewById(R.id.dialog_message);
        if (tv != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
        return this;
    }

    /**
     * 设置确定按钮
     *
     * @param text     ： 文字
     * @param listener ： 监听
     * @return
     */
    public CommonDialog setSureButton(String text, View.OnClickListener listener) {
        TextView tv = (TextView) window.findViewById(R.id.dialog_sure);
        if (tv != null) {
            tv.setText(text);
            tv.setVisibility(View.VISIBLE);
            tv.setOnClickListener(listener);
        }
        return this;
    }

    /**
     * 设置确定按钮文字颜色
     *
     * @param color
     * @return
     */
    public CommonDialog setSureButtonTextColor(String color) {
        TextView tv = (TextView) window.findViewById(R.id.dialog_sure);
        if (tv != null)
            tv.setTextColor(Color.parseColor(color));
        return this;
    }

    /**
     * 设置中立按钮
     *
     * @param text     ： 文字
     * @param listener ： 监听
     * @return
     */
    public CommonDialog setNegativeButton(String text, View.OnClickListener listener) {
        window.findViewById(R.id.dialog_negative_line).setVisibility(View.VISIBLE);
        TextView tv = (TextView) window.findViewById(R.id.dialog_negative);
        if (tv != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(listener);
        }
        return this;
    }

    /**
     * 设置取消按钮
     *
     * @param text     ： 文字
     * @param listener ： 监听
     * @return
     */
    public CommonDialog setCanselButton(String text, View.OnClickListener listener) {
        window.findViewById(R.id.dialog_sure_line).setVisibility(View.VISIBLE);
        TextView tv = (TextView) window.findViewById(R.id.dialog_cancel);
        if (tv != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(listener);
        }
        return this;
    }

    public CommonDialog setProgress(int progress){
        ProgressBar progressBar = (ProgressBar) window.findViewById(R.id.load_progress);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
        }
        return  this;
    }
}
