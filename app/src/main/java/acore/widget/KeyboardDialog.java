package acore.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import acore.tools.StringManager;

public class KeyboardDialog extends Dialog implements View.OnClickListener {
    private int mMaxLength = Integer.MAX_VALUE;
    private View mRootView;
    private EditText mEditText;
    private TextView mSendText;
    private RelativeLayout mKeyboardBottom;

    private String mHintStr;
    private String mFinalStr;

    private View.OnClickListener mOnSendClickListener;
    private SoftKeyboardManager mSoftKeyboardManager;
    private SoftKeyboardManager.SoftKeyboardStateListener mSoftKeyboardStateListener;
    public KeyboardDialog(@NonNull Context context) {
        this(context, R.style.dialog_keyboard);
    }

    public KeyboardDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        getWindow().setWindowAnimations(0);
        init(context);
    }

    protected KeyboardDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.keyboard_layout, null);
        setContentView(mRootView);
        mSoftKeyboardManager = new SoftKeyboardManager(getWindow().getDecorView());
        Window win = getWindow();
        if (win != null) {
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.BOTTOM;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
            win.setAttributes(lp);
        }
        mEditText = mRootView.findViewById(R.id.commend_write_et);
        mSendText = mRootView.findViewById(R.id.comment_send);
        mKeyboardBottom = mRootView.findViewById(R.id.a_comment_keyboard);
        setListener();
    }

    private void setListener() {
        mRootView.setOnClickListener(this);
        mSendText.setOnClickListener(this);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {




            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > mMaxLength) {
                    mEditText.setText(s.subSequence(0, mMaxLength));
                    mEditText.setSelection(mMaxLength);
                    Toast.makeText(getContext(), String.format("最多%1d字", mMaxLength), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSendText.setEnabled(StringManager.isHasChar(String.valueOf(s)));
                mFinalStr = s.toString();
            }
        });
        mSoftKeyboardStateListener = new SoftKeyboardManager.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx, boolean rootChanged) {
//                if (rootChanged) {
//                    return;
//                }
//                mEditText.postDelayed(()->mRootView.scrollTo(0, mKeyboardBottom.getPaddingBottom()), 100);
            }

            @Override
            public void onSoftKeyboardClosed() {
//                if (KeyboardDialog.this.isShowing()) {
//                    KeyboardDialog.this.dismiss();
//                }
//                mRootView.scrollTo(0, 0);
            }
        };
//        mSoftKeyboardManager.addSoftKeyboardStateListener(mSoftKeyboardStateListener);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a_comment_keyboard_parent:
                cancel();
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
        mFinalStr = contentStr;
    }
    public String getText() {
        return mFinalStr;
    }

    public void setTextLength(int max) {
        mMaxLength = max;
    }

    @Override
    public void show() {
        super.show();
        mEditText.setHint(!TextUtils.isEmpty(mHintStr) ? mHintStr : "");
        mEditText.setText(!TextUtils.isEmpty(mFinalStr) ? mFinalStr : "");
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
//        mSoftKeyboardManager.removeSoftKeyboardStateListener(mSoftKeyboardStateListener);
    }

    public void onResume() {
    }

    public void onPause() {
        mRootView.postInvalidate();
    }
}
