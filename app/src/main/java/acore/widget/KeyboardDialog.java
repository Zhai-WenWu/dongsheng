package acore.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.StringManager;

public class KeyboardDialog extends Dialog implements View.OnClickListener {

    private View mRootView;
    private EditText mEditText;
    private TextView mSendText;

    private String mHintStr;
    private String mContentStr;

    private View.OnClickListener mOnSendClickListener;
    public KeyboardDialog(@NonNull Context context) {
        this(context, R.style.dialog_keyboard);
    }

    public KeyboardDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected KeyboardDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.keyboard_layout, null);
        setContentView(contentView);
        mRootView = contentView.findViewById(R.id.a_comment_keyboard_parent);
        Window win = getWindow();
        if (win != null) {
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            win.setAttributes(lp);
        }

        mEditText = (EditText) contentView.findViewById(R.id.commend_write_et);
        mSendText = (TextView) contentView.findViewById(R.id.comment_send);

        addListener();
    }

    private void addListener() {
        mRootView.setOnClickListener(this);
        mSendText.setOnClickListener(this);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mSendText.setEnabled(StringManager.isHasChar(String.valueOf(s)));
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a_comment_keyboard_parent:
                dismiss();
                break;
            case R.id.comment_send:
                if (mOnSendClickListener != null)
                    mOnSendClickListener.onClick(v);
                break;
        }
    }

    public void setOnSendClickListener(View.OnClickListener sendClick) {
        mOnSendClickListener = sendClick;
    }

    public void setHintStr(String hintStr) {
        mHintStr = hintStr;
    }

    public void setContentStr(String contentStr) {
        mContentStr = contentStr;
    }
    public String getText() {
        if (mEditText != null && mEditText.getText() != null)
            return mEditText.getText().toString();
        return null;
    }

    @Override
    public void show() {
        super.show();
        mEditText.setHint(!TextUtils.isEmpty(mHintStr) ? mHintStr : "");
        mEditText.setText(!TextUtils.isEmpty(mContentStr) ? mContentStr : "");
        mEditText.requestFocus();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mEditText.clearFocus();
    }

    public void onResume() {
    }

    public void onPause() {
        mRootView.postInvalidate();
    }
}
