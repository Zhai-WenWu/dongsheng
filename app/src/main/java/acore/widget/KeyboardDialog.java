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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;

public class KeyboardDialog extends Dialog implements View.OnClickListener {
    private int mMaxLength = Integer.MAX_VALUE;
    private View mRootView;
    private EditText mEditText;
    private TextView mSendText;
    private RelativeLayout mKeyboardBottom;

    private String mHintStr;
    private String mFinalStr;

    private View.OnClickListener mOnSendClickListener;
    private int phoneHeight;
    private boolean isAlearyShow= false;
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
        phoneHeight = ToolsDevice.getWindowPx(getContext()).heightPixels;
        setContentView(mRootView);
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
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(KeyboardDialog.this.isShowing()&&getContext()!=null) {
                    int[] location = new int[2];
                    mEditText.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
                    mEditText.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
                    int y = location[1];
                    if(isAlearyShow) {
                        if (phoneHeight - y < 300) {
                            KeyboardDialog.this.dismiss();
                        }
                    }else{
                        if(phoneHeight - y>300){
                            isAlearyShow=true;
                        }
                    }
                }
            }
        });
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
