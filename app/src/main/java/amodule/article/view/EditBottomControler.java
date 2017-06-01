package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.xiangha.R;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:44.
 * E_mail : ztanzeyu@gmail.com
 */

public class EditBottomControler extends LinearLayout implements View.OnClickListener {

    private LinearLayout textEditlayout;

    private OnSelectImageCallback mOnSelectImageCallback;
    private OnSelectVideoCallback mOnSelectVideoCallback;
    private OnKeyboardControlCallback mOnKeyboardControlCallback;
    private OnAddLinkCallback mOnAddLinkCallback;
    private OnTextEditCallback mOnTextEidtCallback;

    public EditBottomControler(Context context) {
        this(context, null);
    }

    public EditBottomControler(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditBottomControler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_edit_controler_view, this);
        init();
    }

    private void init() {
        textEditlayout = (LinearLayout) findViewById(R.id.text_edit_layout);

        //设置监听
        findViewById(R.id.select_image).setOnClickListener(this);
        findViewById(R.id.select_video).setOnClickListener(this);
        findViewById(R.id.edit_text).setOnClickListener(this);
        findViewById(R.id.add_link).setOnClickListener(this);
        findViewById(R.id.keyboard_control).setOnClickListener(this);
        findViewById(R.id.text_bold).setOnClickListener(this);
        findViewById(R.id.text_underline).setOnClickListener(this);
        findViewById(R.id.text_center).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //选择视频
            case R.id.select_image:
                if (null != mOnSelectImageCallback) {
                    mOnSelectImageCallback.onSelectImage();
                }
                break;
            //选择视频
            case R.id.select_video:
                if (null != mOnSelectVideoCallback) {
                    mOnSelectVideoCallback.onSelectVideo();
                }
                break;
            //控制文本控制Layout显示
            case R.id.edit_text:
                boolean isVisibility = textEditlayout.getVisibility() == View.VISIBLE;
                textEditlayout.setVisibility(isVisibility ? View.GONE : View.VISIBLE);
                if (null != mOnTextEidtCallback) {
                    mOnTextEidtCallback.onEditControlerShow(textEditlayout.getVisibility() == View.VISIBLE);
                }
                break;
            //添加链接
            case R.id.add_link:
                if(null != mOnAddLinkCallback){
                    mOnAddLinkCallback.onAddLink();
                }
                break;
            //控制键盘
            case R.id.keyboard_control:
                if (null != mOnKeyboardControlCallback) {
                    mOnKeyboardControlCallback.onKeyboardControlSwitch();
                }
                break;
            //文本加粗
            case R.id.text_bold:
                if (null != mOnTextEidtCallback) {
                    mOnTextEidtCallback.onTextBold();
                }
                break;
            //文本下划线
            case R.id.text_underline:
                if (null != mOnTextEidtCallback) {
                    mOnTextEidtCallback.onTextUnderLine();
                }
                break;
            //段落居中
            case R.id.text_center:
                if (null != mOnTextEidtCallback) {
                    mOnTextEidtCallback.onTextCenter();
                }
                break;
            default:
                break;
        }
    }

    public void setOnSelectImageCallback(OnSelectImageCallback callback) {
        this.mOnSelectImageCallback = callback;
        findViewById(R.id.select_image).setVisibility(null != mOnSelectImageCallback ? View.VISIBLE : View.GONE);
    }

    public void setOnSelectVideoCallback(OnSelectVideoCallback callback) {
        this.mOnSelectVideoCallback = callback;
        findViewById(R.id.select_video).setVisibility(null != mOnSelectVideoCallback ? View.VISIBLE : View.GONE);
    }

    public void setOnAddLinkCallback(OnAddLinkCallback callback){
        this.mOnAddLinkCallback = callback;
        findViewById(R.id.add_link).setVisibility(null != callback ? View.VISIBLE : View.GONE);
    }

    public void setOnKeyboardControlCallback(OnKeyboardControlCallback callback) {
        this.mOnKeyboardControlCallback = callback;
        findViewById(R.id.keyboard_control).setVisibility(null != mOnKeyboardControlCallback ? View.VISIBLE : View.GONE);

    }

    public void setOnTextEidtCallback(OnTextEditCallback callback) {
        this.mOnTextEidtCallback = callback;
        findViewById(R.id.edit_text).setVisibility(null != mOnTextEidtCallback ? View.VISIBLE : View.GONE);
    }

    public interface OnSelectImageCallback {
        public void onSelectImage();
    }

    public interface OnSelectVideoCallback {
        public void onSelectVideo();
    }

    public interface OnAddLinkCallback {
        public void onAddLink();
    }

    public interface OnKeyboardControlCallback {
        public void onKeyboardControlSwitch();
    }

    public interface OnTextEditCallback {
        public void onEditControlerShow(boolean isShow);

        public void onTextBold();

        public void onTextUnderLine();

        public void onTextCenter();
    }
}
