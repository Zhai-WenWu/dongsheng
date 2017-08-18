package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.ArticleDetailActivity;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

import static amodule.article.activity.ArticleDetailActivity.TYPE_ARTICLE;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/27 09:57.
 * E_mail : ztanzeyu@gmail.com
 */
public class CommentBar extends RelativeLayout implements View.OnClickListener {

    private EditText editText;
    private LinearLayout praiseButton;
    private TextView sendComment;
    private TextView hintComment;
    private TextView praiseText;
    private ProgressBar progressBar;

    private String code = "";
    private String type = TYPE_ARTICLE;
    private String praiseAPI = "";
    private int praiseNum = 0;
    private int commentNum = 0;
    private boolean needPlus = true;
    private boolean isSofa = false;

    public CommentBar(Context context) {
        super(context);
        init();
    }

    public CommentBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommentBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_comment_bar, this);
        setBackgroundResource(R.color.transparent);

        editText = (EditText) findViewById(R.id.commend_write_et);
        praiseButton = (LinearLayout) findViewById(R.id.praise_button);
        praiseText = (TextView) findViewById(R.id.subject_zan);
        sendComment = (TextView) findViewById(R.id.comment_send);
        hintComment = (TextView) findViewById(R.id.comment_hint_fake);
        progressBar = (ProgressBar) findViewById(R.id.comment_send_progress);

        sendComment.setOnClickListener(this);
        findViewById(R.id.comment_edit_fake).setOnClickListener(this);
        findViewById(R.id.comment_bar_fake).setOnClickListener(this);
        findViewById(R.id.praise_button).setOnClickListener(this);
        findViewById(R.id.comment_bar_real).setOnClickListener(this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                boolean hasText = StringManager.isHasChar(String.valueOf(s));
                sendComment.setEnabled(hasText);
                sendComment.setTextColor(Color.parseColor(hasText?"#333333":"#CCCCCC"));
            }
        });
    }

    public void setData(Map<String, String> map) {
        code = map.get("code");
        String isLike = map.get("isLike");
        if ("2".equals(isLike)) {
            praiseButton.setEnabled(false);
            praiseButton.setBackgroundResource(R.drawable.bg_article_praise_unenable);
        }
        try{
            praiseNum = Integer.parseInt(map.get("likeNumber"));
            praiseText.setText(praiseNum == 0 ? "赞" : "" + praiseNum);
        }catch (NumberFormatException e){
            needPlus = false;
            praiseText.setText(map.get("likeNumber"));
        }


        String commentNumStr = map.get("commentNumber");
        if(!TextUtils.isEmpty(commentNumStr))
            commentNum = Integer.parseInt(commentNumStr);
        isSofa = "0".equals(commentNumStr);
        hintComment.setText(isSofa ? "抢沙发" : getTextHint());

    }

    public void resetHintComment() {
        isSofa = false;
        hintComment.setText(isSofa ? "抢沙发" : getTextHint());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_bar_fake:
            case R.id.comment_edit_fake:
                doComment(isSofa ? "抢沙发" : getTextHint());
                statistics("底部栏", "评论输入框");
                break;
            case R.id.praise_button:
                if (praiseButton.isEnabled()) {
                    statistics("底部栏", "点赞");
                    doPraise();
                }
                break;
            case R.id.comment_send:
                sentComment();
                break;
            default:
                break;
        }
    }

    public void doComment(String hintText) {
        setEditTextShow(true);
        editText.setHint(hintText);
        editText.requestFocus();
        ToolsDevice.keyboardControl(true, getContext(), editText);
    }

    public void setEditTextShow(boolean isShow) {
        findViewById(R.id.comment_bar_fake).setVisibility(isShow ? GONE : VISIBLE);
        findViewById(R.id.comment_bar_real).setVisibility(isShow ? VISIBLE : GONE);
    }

    /** 点赞 */
    private void doPraise() {
        if (TextUtils.isEmpty(praiseAPI)) {
            return;
        }
        if (!LoginManager.isLogin()) {
            ToolsDevice.keyboardControl(false, getContext(), editText);
            getContext().startActivity(new Intent(getContext(), LoginByAccout.class));
            return;
        }

        //发请求
        ReqEncyptInternet.in().doEncypt(praiseAPI, "code=" + code,
                new InternetCallback(getContext()) {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                    }
                });
        if(needPlus){
            praiseNum++;
            praiseText.setText(String.valueOf(praiseNum));
        }
        praiseButton.setEnabled(false);
        praiseButton.setBackgroundResource(R.drawable.bg_article_praise_unenable);

    }

    /** 发评论 */
    private void sentComment() {
        if (!LoginManager.isLogin()) {
            ToolsDevice.keyboardControl(false, getContext(), editText);
            getContext().startActivity(new Intent(getContext(), LoginByAccout.class));
            return;
        }
        String text = editText.getText().toString();
        if (!StringManager.isHasChar(text)) {
            return;
        }

        if(text.length() > 2000){
            Tools.showToast(getContext(),"发送内容不能超过2000字");
            return;
        }

        sendComment.setVisibility(INVISIBLE);
        progressBar.setVisibility(VISIBLE);

        StringBuilder sbuild = new StringBuilder();
        sbuild.append("type=").append(type).append("&")
                .append("code=").append(code).append("&")
                .append("content=").append(getContent());
//        Log.i("tzy", sbuild.toString());
        ReqEncyptInternet.in().doEncypt(StringManager.api_addForum, sbuild.toString(),
                new InternetCallback(getContext()) {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                            commentNum++;
                            resetHintComment();
                            if (isSofa) {
                                isSofa = !isSofa;
                                editText.setHint(getTextHint());
                            }
                            //回调
                            if (onCommentSuccessCallback != null) {
                                onCommentSuccessCallback.onCommentSuccess(isSofa, obj);
                            }
                        } else {
                            Tools.showToast(context, "评论失败，请重试");
                        }
                        sendComment.setVisibility(VISIBLE);
                        progressBar.setVisibility(GONE);
                        editText.setText("");
                        editText.clearFocus();
                        ToolsDevice.keyboardControl(false, getContext(), editText);
                    }
                });
    }

    private String getTextHint() {
        return commentNum + "评论...";
    }

    public String getContent() {
        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
        Map<String, String> content = new HashMap<>();
        content.put("text", editText.getText().toString());
        content.put("imgs", "[]");
        contentArray.add(content);
        return Tools.list2JsonArray(contentArray).toString();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPraiseAPI() {
        return praiseAPI;
    }

    public void setPraiseAPI(String praiseAPI) {
        this.praiseAPI = praiseAPI;
    }

    public EditText getEditText() {
        return editText;
    }

    public boolean isSofa() {
        return isSofa;
    }

    public void setSofa(boolean sofa) {
        isSofa = sofa;
    }

    private OnCommentSuccessCallback onCommentSuccessCallback;

    public interface OnCommentSuccessCallback {
        public void onCommentSuccess(boolean isSofa, Object obj);
    }

    public void setOnCommentSuccessCallback(OnCommentSuccessCallback onCommentSuccessCallback) {
        this.onCommentSuccessCallback = onCommentSuccessCallback;
    }

    //统计
    private void statistics(String twoLevel, String threeLevel) {
        String eventId = "";
        if (TextUtils.isEmpty(getType()))
            return;
        switch (getType()) {
            case TYPE_ARTICLE:
                eventId = "a_ArticleDetail";
                break;
            case ArticleDetailActivity.TYPE_VIDEO:
                eventId = "a_ShortVideoDetail";
                break;
        }
        if (TextUtils.isEmpty(eventId))
            return;
        XHClick.mapStat(getContext(), eventId, twoLevel, threeLevel);
    }
}
