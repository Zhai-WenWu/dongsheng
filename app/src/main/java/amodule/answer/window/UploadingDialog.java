package amodule.answer.window;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import com.xiangha.R;

/**
 * Created by sll on 2017/9/5.
 */

public class UploadingDialog extends Dialog {

    private int mContentResId = R.layout.ask_upload_dialoglayout;

    public UploadingDialog(@NonNull Context context) {
        this(context, R.style.dialog);
    }

    public UploadingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected UploadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (layoutResID != 0)
            mContentResId = layoutResID;
        super.setContentView(mContentResId);
    }

    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(true);
    }

    public void setContentView() {
        setContentView(0);
    }
}
