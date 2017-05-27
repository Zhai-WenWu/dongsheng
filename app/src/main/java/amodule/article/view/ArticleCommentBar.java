package amodule.article.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.ArticleDetailActivity;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/27 09:57.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleCommentBar extends RelativeLayout implements View.OnClickListener {

    private EditText editText;
    private LinearLayout praiseButton;
    private TextView sendComment;
    private TextView praiseText;
    private ProgressBar progressBar;

    private String code = "";
    private String type = "1";
    private int praiseNum = 0;

    public ArticleCommentBar(Context context) {
        super(context);
        init();
    }

    public ArticleCommentBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArticleCommentBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_comment_bar, this);

        editText = (EditText) findViewById(R.id.commend_write_et);
        praiseButton = (LinearLayout) findViewById(R.id.praise_button);
        praiseText = (TextView) findViewById(R.id.subject_zan);
        sendComment = (TextView) findViewById(R.id.comment_send);
        progressBar = (ProgressBar) findViewById(R.id.comment_send_progress);

        sendComment.setOnClickListener(this);
        findViewById(R.id.comment_edit_fake).setOnClickListener(this);
        findViewById(R.id.praise_button).setOnClickListener(this);
    }

    public void setData(Map<String, String> map) {
        code = map.get("code");
        String isLike = map.get("isLike");
        if ("2".equals(isLike)) {
            praiseButton.setEnabled(false);
            praiseButton.setBackgroundResource(R.drawable.bg_article_praise_unenable);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_edit_fake:
                doComment("评论文章");
                break;
            case R.id.praise_button:
                if (praiseButton.isEnabled()) {
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

    private void doComment(String hintText) {
        findViewById(R.id.comment_bar_fake).setVisibility(View.INVISIBLE);
        findViewById(R.id.comment_bar_real).setVisibility(View.VISIBLE);
        editText.setHint(hintText);
        editText.requestFocus();
        ToolsDevice.keyboardControl(true, getContext(), editText);
    }

    /** 点赞 */
    private void doPraise() {
        String url = "";
        switch (type){
            case ArticleDetailActivity.TYPE_ARTICLE:
                url = StringManager.api_likeForum;
                break;
            case ArticleDetailActivity.TYPE_VIDEO:
                url = StringManager.api_likeVideo;
                break;
            default:
                return;
        }

        //发请求
        ReqEncyptInternet.in().doEncypt(url, "code=" + code,
                new InternetCallback(getContext()) {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                    }
                });

        praiseNum++;
        praiseText.setText("赞" + praiseNum);
        praiseButton.setEnabled(false);
        praiseButton.setBackgroundResource(R.drawable.bg_article_praise_unenable);

    }

    /** 发评论 */
    private void sentComment() {
        String text = editText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            Tools.showToast(getContext(), "内容不能为空");
            return;
        }

        sendComment.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);

        StringBuilder sbuild = new StringBuilder();
        sbuild.append("type=").append(type).append("&")
                .append("code=").append(code).append("&")
                .append("content=").append(getContent());
        ReqEncyptInternet.in().doEncypt(StringManager.api_addForum, sbuild.toString(),
                new InternetCallback(getContext()) {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        if(flag >= ReqEncyptInternet.REQ_OK_STRING){
                            editText.setText("");
                            editText.clearFocus();
                            ToolsDevice.keyboardControl(true, getContext(), editText);
                            Tools.showToast(context,"评论成功");
                        }else{
                            Tools.showToast(context,"评论失败，请重试");
                        }
                        sendComment.setVisibility(VISIBLE);
                        progressBar.setVisibility(GONE);
                    }
                });
    }

    public String getContent() {
        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
        Map<String, String> content = new HashMap<>();
        content.put("text", editText.getText().toString());
        content.put("imgs", "[]");
        contentArray.add(content);
        return Tools.list2JsonArray(contentArray).toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
