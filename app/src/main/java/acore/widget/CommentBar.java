package acore.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xianghatest.R;

import acore.tools.Tools;
import acore.tools.ToolsDevice;

/**
 * PackageName : acore.widget
 * Created by MrTrying on 2017/8/10 14:16.
 * E_mail : ztanzeyu@gmail.com
 */

public class CommentBar extends RelativeLayout {
    public CommentBar(Context context) {
        super(context);
        initialize();
    }

    public CommentBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CommentBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CommentBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    EditText commentEdittext;
    ProgressBar sendingProgress;
    TextView sendComment;

    OnPublishCommentCallback onPublishCommentCallback;

    int minLength = -1, maxLength = -1;

    public void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.v_comment_bar, this);
        setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {}
        });
        commentEdittext = (EditText) findViewById(R.id.commend_write_et);
        sendingProgress = (ProgressBar) findViewById(R.id.comment_send_progress);
        sendComment = (TextView) findViewById(R.id.comment_send);
        sendComment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                publishComment();
            }
        });

        commentEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int currentLength = s.length();
                boolean hasText = currentLength > 0;
                sendComment.setEnabled(hasText);
                sendComment.setTextColor(Color.parseColor(hasText ? "#333333" : "#CCCCCC"));

                if (maxLength >= 0) {
                    int value = currentLength - maxLength;
                    if (value > 0) {
                        commentEdittext.setText(s.subSequence(0, s.length() - value));
                        commentEdittext.setSelection(commentEdittext.getText().length());
                        Tools.showToast(getContext(), "内容最多" + maxLength + "字");
                        ToolsDevice.keyboardControl(false, getContext(), commentEdittext);
                    }

                }
            }
        });
        //无法正常修改enter样式，不实现该功能了
//        commentEdittext.setImeOptions(EditorInfo.IME_ACTION_SEND);
//        commentEdittext.setOnKeyListener(new OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                switch (keyCode){
//                    case KeyEvent.KEYCODE_ENTER:
//                        if(event.getAction() == KeyEvent.ACTION_DOWN){
//                            publishComment();
//                            return true;
//                        }
//                        break;
//                }
//                return false;
//            }
//        });
    }

    /**
     * 设置默认文字
     *
     * @param hintText
     */
    public void setEditTextHint(String hintText) {
        if (TextUtils.isEmpty(hintText)) return;
        commentEdittext.setHint(hintText);
    }

    /** 点击发布 */
    private void publishComment() {
        if (onPublishCommentCallback != null
                && onPublishCommentCallback.onPrePublishComment())
            return;

        String content = commentEdittext.getText().toString();
        if (content.replace(" ", "").replace("\n", "").length() <= 0) {
            Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minLength >= 0 && content.length() < minLength) {
            Toast.makeText(getContext(), "内容最少" + minLength + "字", Toast.LENGTH_SHORT).show();
            return;
        }
        if (onPublishCommentCallback != null) {
            onPublishCommentCallback.onPublishComment(content);
        }
    }

    public void hide(){
        setVisibility(GONE);
        ToolsDevice.keyboardControl(false, getContext(), commentEdittext);
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public OnPublishCommentCallback getOnPublishCommentCallback() {
        return onPublishCommentCallback;
    }

    public void setOnPublishCommentCallback(OnPublishCommentCallback onPublishCommentCallback) {
        this.onPublishCommentCallback = onPublishCommentCallback;
    }

    /** 发布相关Callback */
    public interface OnPublishCommentCallback {
        boolean onPrePublishComment();

        void onPublishComment(String content);
    }

}
