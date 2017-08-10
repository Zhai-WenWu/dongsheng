package third.share;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * Created by Fang Ruijiao on 2017/7/28.
 */
public class CheckDialog {
    private Dialog mDialog;
    private Window mWindow;

    private boolean mDefaultState = false;

    public CheckDialog(Context context) {
        this.mDialog = new Dialog(context,R.style.dialog);
        this.mDialog.setContentView(R.layout.a_check_dialog);
        this.mWindow = this.mDialog.getWindow();
    }

    public void show() {
        this.mDialog.show();
    }

    public void cancel() {
        this.mDialog.cancel();
    }

    public CheckDialog setTitle(String var1) {
        TextView textView = (TextView)this.mWindow.findViewById(R.id.dialog_title);
        textView.setVisibility(View.VISIBLE);
        textView.setText(var1);
        return this;
    }

    public CheckDialog setCheck(String text, boolean defaultState) {
        mDefaultState = defaultState;
        View dialogCheckLayout = mWindow.findViewById(R.id.dialog_check);
        dialogCheckLayout.setVisibility(View.VISIBLE);

        final ImageView imageView = (ImageView)this.mWindow.findViewById(R.id.dialog_check_img);
        TextView textView = (TextView)this.mWindow.findViewById(R.id.dialog_check_text);
        textView.setText(text);
        imageView.setImageResource(mDefaultState ? R.drawable.i_check_on : R.drawable.i_check_off);

        dialogCheckLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDefaultState = !mDefaultState;
                imageView.setImageResource(mDefaultState ? R.drawable.i_check_on : R.drawable.i_check_off);
            }
        });
        return this;
    }

    public CheckDialog setSureButton(String var1, final OnCheckDialogListener listener) {
        TextView textView = (TextView)mWindow.findViewById(R.id.dialog_sure);
        textView.setText(var1);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCheckChange(mDefaultState);
            }
        });
        return this;
    }

    public CheckDialog setSureButtonTextColor(String var1) {
        ((TextView)mWindow.findViewById(R.id.dialog_sure)).setTextColor(Color.parseColor(var1));
        return this;
    }

    public CheckDialog setCancelButtonTextColor(String var1) {
        ((TextView)mWindow.findViewById(R.id.dialog_cancel)).setTextColor(Color.parseColor(var1));
        return this;
    }

    public CheckDialog setNegativeButton(String var1, View.OnClickListener var2) {
        mWindow.findViewById(R.id.dialog_negative_line).setVisibility(View.VISIBLE);
        TextView textView = (TextView)this.mWindow.findViewById(R.id.dialog_negative);
        textView.setVisibility(View.VISIBLE);
        textView.setText(var1);
        textView.setOnClickListener(var2);
        return this;
    }

    public CheckDialog setCanselButton(String var1, View.OnClickListener var2) {
        mWindow.findViewById(R.id.dialog_sure_line).setVisibility(View.VISIBLE);
        TextView textView = (TextView)this.mWindow.findViewById(R.id.dialog_cancel);
        textView.setVisibility(View.VISIBLE);
        textView.setText(var1);
        textView.setOnClickListener(var2);
        return this;
    }

    public interface OnCheckDialogListener{
        public void onCheckChange(boolean isChoose);
    }
}
