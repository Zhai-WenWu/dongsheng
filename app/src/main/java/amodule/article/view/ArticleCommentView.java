package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.comment.activity.CommentActivity;
import amodule.comment.view.ViewCommentItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/1 14:42.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleCommentView extends ItemBaseView {

    private TextView commentNum;
    private LinearLayout commentLayout;
    private TextView commentAll;
    private TextView robsofa;

    private String code;
    private String type;

    public ArticleCommentView(Context context) {
        super(context, R.layout.a_article_comment_view);
        init();
    }

    public ArticleCommentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.layout.a_article_comment_view);
        init();
    }

    public ArticleCommentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.a_article_comment_view);
        init();
    }

    public void init() {
        commentNum = (TextView) findViewById(R.id.comment_num);
        commentAll = (TextView) findViewById(R.id.comment_all);
        commentLayout = (LinearLayout) findViewById(R.id.comment_layout);
        robsofa = (TextView) findViewById(R.id.robsofa);

        commentAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCommentActivity();
            }
        });
    }

    public void setData(Map<String, String> map) {
        if (TextUtils.isEmpty(map.get("commentNum"))
                || "0".equals(map.get("commentNum"))) {
            robsofa.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (robsofaClickListener != null) {
                        robsofaClickListener.onClick(v);
                    }
                }
            });
            findViewById(R.id.has_comment_layout).setVisibility(GONE);
        }
        setViewText(commentNum, map, "commentNum", GONE, "评论(", ")");
        String dataStr = map.get("data");
        List<Map<String, String>> data = StringManager.getListMapByJson(dataStr);
        for (final Map<String, String> dataMap : data) {
            final ViewCommentItem commentItem = new ViewCommentItem(getContext());
            commentItem.setData(dataMap);
            commentItem.setCommentItemListener(new ViewCommentItem.OnCommentItenListener() {
                @Override
                public void onShowAllReplayClick(String comment_id) {
                    StringBuilder sbuild = new StringBuilder();
                    sbuild.append("type=").append(getType()).append("&")
                            .append("code=").append(code).append("&")
                            .append("commentId=").append(comment_id).append("&")
                            .append("pagesize=").append(Integer.parseInt(dataMap.get("replay_num")) + 3).append("&");

                    ReqEncyptInternet.in().doEncypt(StringManager.api_replayList, sbuild.toString(),
                            new InternetCallback(getContext()) {
                                @Override
                                public void loaded(int flag, String url, Object obj) {
                                    if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                                        commentItem.addReplayView((String) obj);
                                    }
                                }
                            });
                }

                @Override
                public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name, String reportContent, String reportType) {

                }

                @Override
                public void onDeleteCommentClick(String comment_id, String deleteType) {

                }

                @Override
                public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String reportContent) {

                }

                @Override
                public void onDeleteReplayClick(String comment_id, String replay_id) {

                }

                @Override
                public void onPraiseClick(String comment_id) {
                    gotoCommentActivity();
                }

                @Override
                public void onContentReplayClick(String comment_id, String replay_user_code, String replay_user_name, String type) {
                    gotoCommentActivity();
                }
            });
            commentLayout.addView(commentItem);
        }
    }

    private void gotoCommentActivity() {
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra("type", getType());
        intent.putExtra("code", code);
        getContext().startActivity(intent);
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

    private OnClickListener robsofaClickListener;

    public void setRobsofaClickListener(OnClickListener listener) {
        robsofaClickListener = listener;
    }
}
