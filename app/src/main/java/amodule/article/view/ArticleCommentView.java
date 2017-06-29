package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.article.activity.ArticleDetailActivity;
import amodule.article.activity.ReportActivity;
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
    private LinearLayout robsofa;

    private String code;
    private String type;
    private boolean isSofa = false;

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
        robsofa = (LinearLayout) findViewById(R.id.robsofa);

        commentAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCommentActivity(null, null);
            }
        });
    }

    public void setData(Map<String, String> map) {
        String dataStr = map.get("data");
        Map<String, String> commentMap = StringManager.getFirstMap(dataStr);
        List<Map<String, String>> data = StringManager.getListMapByJson(commentMap.get("list"));
        if (data.size() > 0) {
            if (!TextUtils.isEmpty(map.get("commentNum"))) {
                setViewText(commentNum, map, "commentNum", GONE, "评论(", ")");
            }
            final int length = data.size() > 3 ? 3 : data.size();
            for (int index = 0; index < length; index++) {
                final Map<String, String> dataMap = data.get(index);
                final ViewCommentItem commentItem = new ViewCommentItem(getContext());
                Log.i("tzy", "comment dataMap = " + dataMap.toString());
                if ("0".equals(dataMap.get("fabulous_num")))
                    dataMap.put("fabulous_num", "");
                commentItem.setData(dataMap);
                commentItem.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoCommentActivity(dataMap.get("comment_id"), null);
                    }
                });
                commentItem.setCommentItemListener(new ViewCommentItem.OnCommentItenListener() {
                    @Override
                    public void onShowAllReplayClick(String comment_id) {
                        StringBuilder sbuild = new StringBuilder();
                        sbuild.append("type=").append(getType()).append("&")
                                .append("code=").append(code).append("&")
                                .append("commentId=").append(comment_id).append("&")
                                .append("pagesize=").append(Integer.parseInt(dataMap.get("replay_num")) + 3);

                        ReqEncyptInternet.in().doEncypt(StringManager.api_replayList, sbuild.toString(),
                                new InternetCallback(getContext()) {
                                    @Override
                                    public void loaded(int flag, String url, Object obj) {
                                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                                            commentItem.addReplayView((String) obj, true);
                                        }
                                    }
                                });
                        statistics("评论", "查看更多回复");
                    }

                    @Override
                    public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name, String reportContent, String reportType) {
                        Intent intent = new Intent(context, ReportActivity.class);
                        intent.putExtra("code", code);
                        intent.putExtra("type", getType());
                        intent.putExtra("userCode", comment_user_code);
                        intent.putExtra("commentId", comment_id);
                        intent.putExtra("reportName", comment_user_name);
                        intent.putExtra("reportContent", reportContent);
                        intent.putExtra("reportType", "2");
                        context.startActivity(intent);
                    }

                    @Override
                    public void onDeleteCommentClick(String comment_id, String deleteType) {
                    }

                    @Override
                    public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String reportContent) {
                        Intent intent = new Intent(context, ReportActivity.class);
                        intent.putExtra("code", code);
                        intent.putExtra("type", getType());
                        intent.putExtra("userCode", replay_user_code);
                        intent.putExtra("replayId", replay_id);
                        intent.putExtra("reportName", replay_user_name);
                        intent.putExtra("reportContent", reportContent);
                        intent.putExtra("reportType", "3");
                        context.startActivity(intent);
                    }

                    @Override
                    public void onDeleteReplayClick(String comment_id, String replay_id) {
                    }

                    @Override
                    public void onPraiseClick(String comment_id) {
                        gotoCommentActivity(comment_id, null);
                    }

                    @Override
                    public void onContentReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String type, boolean isShowKeyBoard, boolean isMyselft) {
                        gotoCommentActivity(comment_id, replay_id);
                        statistics("评论", type);
                    }
                });
                commentLayout.addView(commentItem);
            }
            findViewById(R.id.has_comment_layout).setVisibility(VISIBLE);
            findViewById(R.id.robsofa).setVisibility(GONE);
            commentAll.setVisibility(data.size() > 3 ? VISIBLE : GONE);
            setVisibility(VISIBLE);
        } else {
            isSofa = true;
            robsofa.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (robsofaClickListener != null) {
                        robsofaClickListener.onClick(v);
                        statistics("评论", "抢沙发");
                    }
                }
            });
            findViewById(R.id.has_comment_layout).setVisibility(GONE);
            findViewById(R.id.robsofa).setVisibility(GONE);
            setVisibility(GONE);
        }
        findViewById(R.id.above_line).setVisibility("2".equals(getType()) ? VISIBLE : GONE);
        findViewById(R.id.bottom_line).setVisibility("2".equals(getType()) ? GONE : VISIBLE);
    }

    private void gotoCommentActivity(String commentId, String replayId) {
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra("from", "2");
        intent.putExtra("type", getType());
        intent.putExtra("code", code);
        if (!TextUtils.isEmpty(commentId))
            intent.putExtra("commentId", commentId);
        if (!TextUtils.isEmpty(replayId))
            intent.putExtra("replayId", replayId);
        getContext().startActivity(intent);
        statistics("评论", "查看所有评论");
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

    //统计
    private void statistics(String twoLevel, String threeLevel) {
        String eventId = "";
        if (TextUtils.isEmpty(getType()))
            return;
        switch (getType()) {
            case ArticleDetailActivity.TYPE_ARTICLE:
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
