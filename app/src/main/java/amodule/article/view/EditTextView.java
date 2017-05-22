package amodule.article.view;

import android.content.Context;
import android.support.v4.view.LayoutInflaterFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.xiangha.R;

import org.json.JSONArray;

import amodule.article.view.richtext.RichText;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:42.
 * E_mail : ztanzeyu@gmail.com
 */

public class EditTextView extends BaseView {

    private RichText mRichText;

    private OnFocusChangeCallback mOnFocusChangeCallback;

    //CENTER_HORIZONTAL
    private boolean isCenterHorizontal = false;

    public EditTextView(Context context) {
        this(context, null);
    }

    public EditTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_article_view_edit, this);
        init();
    }

    @Override
    public void init() {
        mRichText = (RichText) findViewById(R.id.rich_text);
        mRichText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mOnFocusChangeCallback != null) {
                    mOnFocusChangeCallback.onFocusChange(EditTextView.this, hasFocus);
                }
            }
        });
    }

    @Override
    public String getOutputData() {
        return mRichText.toHtml();
    }

    /**
     * @param isInput 是否弹起键盘
     */
    public void setEditTextFocus(boolean isInput) {
        mRichText.setFocusable(true);
        mRichText.setFocusableInTouchMode(true);
        mRichText.requestFocus();
        // 手动弹出键盘
        if (isInput) {
            InputMethodManager inputManager = (InputMethodManager) mRichText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(mRichText, 0);
        }
    }

    public void setupTextLink(String url, String desc) {
        int start = mRichText.getSelectionStart();
        int end = mRichText.getSelectionEnd();

        if (!TextUtils.isEmpty(desc)) {
            mRichText.getText().replace(start, end, desc);
            end = start + desc.length();
        }
        // When KnifeText lose focus, use this method
        mRichText.link(url, start, end);

    }

    public void setupTextBold() {
        mRichText.bold(!mRichText.contains(RichText.FORMAT_BOLD));
    }

    public void setupUnderline() {
        mRichText.underline(!mRichText.contains(RichText.FORMAT_UNDERLINED));
    }

    public void setupTextCenter() {
        mRichText.setGravity(isCenterHorizontal ?
                Gravity.TOP | Gravity.START : Gravity.CENTER_HORIZONTAL);
        isCenterHorizontal = !isCenterHorizontal;
    }

    public OnFocusChangeCallback getOnFocusChangeCallback() {
        return mOnFocusChangeCallback;
    }

    public void setOnFocusChangeCallback(OnFocusChangeCallback mOnFocusChangeCallback) {
        this.mOnFocusChangeCallback = mOnFocusChangeCallback;
    }

    public interface OnFocusChangeCallback {
        public void onFocusChange(EditTextView v, boolean hasFocus);
    }
}
