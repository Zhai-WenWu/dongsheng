package amodule.article.view;

import android.content.Context;
import android.support.v4.view.LayoutInflaterFactory;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.xiangha.R;

import org.json.JSONArray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private InputFilter emojiFilter = new InputFilter() {
        Pattern emoji = Pattern.compile(
                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                return dest.toString().substring(dstart,dend);
            }
            return null;
        }
    };

    private InputFilter htmlFilter = new InputFilter() {
        Pattern html = Pattern.compile("<(\\S*?) [^>]*>.*?</\\1>|<.*? />",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher htmlMatcher = html.matcher(source);
            if (htmlMatcher.find()) {
                return "";
            }
            return null;
        }
    };

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
        mRichText.setFilters(new InputFilter[]{emojiFilter,htmlFilter});
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

    public void setupTextLink(String url, String desc, int start, int end) {
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

    public int getSelectionStart() {
        return mRichText.getSelectionStart();
    }

    public int getSelectionEnd() {
        return mRichText.getSelectionEnd();
    }

    public OnFocusChangeCallback getOnFocusChangeCallback() {
        return mOnFocusChangeCallback;
    }

    public void setOnFocusChangeCallback(OnFocusChangeCallback mOnFocusChangeCallback) {
        this.mOnFocusChangeCallback = mOnFocusChangeCallback;
    }

    public String getSelectionText() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        return mRichText.getText()
                .subSequence(start, end)
                .toString();
    }

    /**
     * 检测是否有emoji表情
     *
     * @param source
     *
     * @return
     */
    public boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     *
     * @return
     */
    private boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }

    public interface OnFocusChangeCallback {
        public void onFocusChange(EditTextView v, boolean hasFocus);
    }
}
