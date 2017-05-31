package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.adapter.ArticleDetailAdapter;
import amodule.article.view.ArticleCommentBar;
import amodule.article.view.ArticleHeaderView;
import amodule.article.view.CommodityItemView;
import amodule.article.view.DishItemView;
import amodule.article.view.ImageShowView;
import amodule.article.view.VideoShowView;
import amodule.article.view.richtext.RichParser;
import amodule.article.view.richtext.RichURLSpan;
import amodule.comment.activity.CommentActivity;
import amodule.comment.view.ViewCommentItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.share.BarShare;

import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;

/**
 * 文章详情
 */
public class ArticleDetailActivity extends BaseActivity {
    public static final String TYPE_ARTICLE = "1";
    public static final String TYPE_VIDEO = "2";
    private boolean initUiSuccess = false;//ui初始化完成
    private String code = "";//请求数据的code
    private int page = 0;//相关推荐的page
    private ArticleDetailAdapter detailAdapter;
    private ArrayList<Map<String, String>> otherListMap = new ArrayList<>();//评论列表和推荐列表对数据集合
    private ArticleCommentBar mArticleCommentBar;
    private VideoShowView videoShowView;
    private boolean isKeyboradShow = false;
    private ListView listview;
    private LinearLayout layout, linearLayoutOne, linearLayoutTwo, linearLayoutThree;//头部view
    private TextView title;

    private String commentNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            code = bundle.getString("code");
        }
        //TODO 测试
        code = "175";
        init();
    }

    //**********************************************Activity生命周期方法**************************************************

    @Override
    public void onResume() {
        super.onResume();
        if (videoShowView != null) {
            videoShowView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoShowView != null) {
            videoShowView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoShowView != null) {
            videoShowView.onDestroy();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !initUiSuccess) {
            initUiSuccess = true;
        }
    }

    /** 初始化 **/
    private void init() {
        initActivity(getTitleText(), 2, 0, 0, R.layout.a_article_detail);
        initView();
        initData();
    }

    /** View部分初始化 **/
    private void initView() {
        //处理状态栏引发的问题
        if (Tools.isShowTitle()) {
            final RelativeLayout bottomBarLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
            rl.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                            Rect r = new Rect();
                            rl.getWindowVisibleDisplayFrame(r);
                            int screenHeight = rl.getRootView().getHeight();
                            int heightDifference = screenHeight - (r.bottom - r.top);
                            isKeyboradShow = heightDifference > 200;
                            heightDifference = isKeyboradShow ? heightDifference - heightDiff : 0;
                            bottomBarLayout.setPadding(0, 0, 0, heightDifference);
                        }
                    });
        }
        String color = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(color));
        ImageView share = (ImageView) findViewById(R.id.rightImgBtn2);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barShare = new BarShare(ArticleDetailActivity.this, "精选菜单", "菜单");
                String type = BarShare.IMG_TYPE_RES;
                String shareImg = "" + R.drawable.umen_share_launch;
                String title = "精选菜单大全，强烈推荐！";
                String clickUrl = StringManager.wwwUrl + "caipu/caidan";
                String content = "最近一直在用香哈菜谱，内容好、分类全，还可以离线下载菜谱~";
                barShare.setShare(type, title, content, shareImg, clickUrl);
                barShare.openShare();
            }
        });
        TextView title = (TextView) findViewById(R.id.title);
        listview = (ListView) findViewById(R.id.listview);
        initHeaderView();
        listview.addHeaderView(layout);
        mArticleCommentBar = (ArticleCommentBar) findViewById(R.id.acticle_comment_bar);

        mArticleCommentBar.setCode(code);
        mArticleCommentBar.setType(getType());
    }

    /**
     * 初始化header布局
     */
    private void initHeaderView() {
        //initHeaderView
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        linearLayoutOne = new LinearLayout(this);
        linearLayoutOne.setOrientation(LinearLayout.VERTICAL);

        linearLayoutTwo = new LinearLayout(this);
        linearLayoutTwo.setOrientation(LinearLayout.VERTICAL);

        linearLayoutThree = new LinearLayout(this);
        linearLayoutThree.setOrientation(LinearLayout.VERTICAL);

        linearLayoutOne.setVisibility(View.GONE);
        linearLayoutTwo.setVisibility(View.GONE);
        linearLayoutThree.setVisibility(View.GONE);
        layout.addView(linearLayoutOne);
        layout.addView(linearLayoutTwo);
        layout.addView(linearLayoutThree);
    }

    /** 数据初始化 **/
    private void initData() {
        if (TextUtils.isEmpty(code)) {
            Tools.showToast(this, "当前数据错误，请重新请求");
            return;
        }
        detailAdapter = new ArticleDetailAdapter(otherListMap);
        loadManager.setLoading(listview, detailAdapter, true,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (page >= 1) {
                            requestRelateData();
                        }
                    }
                });
        requestArticleData();
    }

    /** 请求网络 */
    private void requestArticleData() {
        String url = getInfoAPI();
        String params = TextUtils.isEmpty(code) ? "" : "code=" + code;
        loadManager.showProgressBar();
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMap = StringManager.getListMapByJson(object);
                    analysArticleData(listMap.get(0));
                } else {
                    toastFaildRes(flag, true, object);
                }
                //请求
                requestForumData();
                loadManager.hideProgressBar();
            }
        });
    }

    /**
     * 解析文章数据
     *
     * @param mapArticle
     */
    private void analysArticleData(@NonNull Map<String, String> mapArticle) {
        if (mapArticle.isEmpty()) return;
        findViewById(R.id.rightImgBtn2).setVisibility(View.VISIBLE);
        ArticleHeaderView headerView = new ArticleHeaderView(ArticleDetailActivity.this);
        headerView.setData(mapArticle);
        linearLayoutOne.addView(headerView);
        linearLayoutOne.setVisibility(View.VISIBLE);
        detailAdapter.notifyDataSetChanged();
        listview.setVisibility(View.VISIBLE);
        commentNum = mapArticle.get("commentNumber");
        String content = mapArticle.get("content");
        analysArticleContent(content);
        mArticleCommentBar.setPraiseAPI(getPraiseAPI());
        mArticleCommentBar.setData(mapArticle);
    }

    /**
     * 解析图文混排数据
     *
     * @param content
     */
    private void analysArticleContent(String content) {
        int dp_20 = Tools.getDimen(this, R.dimen.dp_20);
        if (TextUtils.isEmpty(content)) return;
        ArrayList<Map<String, String>> listContent = StringManager.getListMapByJson(content);
        int size = listContent.size();
        if (size > 0) linearLayoutTwo.setVisibility(View.VISIBLE);
        for (int i = 0; i < size; i++) {
            String type = listContent.get(i).get("type");
            if ("text".equals(type)) {//文章
                String html = listContent.get(i).get("html");
                if (!TextUtils.isEmpty(html)) {
                    TextView textView = new TextView(this);
                    textView.setPadding(dp_20, 0, dp_20, 0);
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(RichParser.fromHtml(html));
                    URLSpan[] urlSpans = builder.getSpans(0, builder.length(), URLSpan.class);
                    for (URLSpan span : urlSpans) {
                        int spanStart = builder.getSpanStart(span);
                        int spanEnd = builder.getSpanEnd(span);
                        builder.removeSpan(span);
                        builder.setSpan(new RichURLSpan(span.getURL(), Color.parseColor("#0872dd"), false), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textView.setText(builder);
                    linearLayoutTwo.addView(textView);
                }
            } else if ("image".equals(type) || "gif".equals(type)) {//图片
                String imageUrl = listContent.get(i).get("gif".equals(type) ? "gifurl" : "imageurl");
                if (!TextUtils.isEmpty(imageUrl)) {
                    ImageShowView imageShowView = new ImageShowView(this);
                    imageShowView.setEnableEdit(false);
                    imageShowView.showImage(imageUrl,type);
                    linearLayoutTwo.addView(imageShowView);
                }
            } else if ("video".equals(type)) {//视频
                Map<String,String> videoMap = StringManager.getFirstMap(listContent.get(i).get("video"));
                String videoUrl = videoMap.get("url");
                String videoimageurl = videoMap.get("videoImg");
                if (!TextUtils.isEmpty(videoUrl) && !TextUtils.isEmpty(videoimageurl)) {
                    videoShowView = new VideoShowView(this);
                    videoShowView.setVideoData(videoimageurl, videoUrl);
                    linearLayoutTwo.addView(videoShowView);
                }

            } else if ("xiangha".equals(type)) {//自定义演示。ds，电商，caipu，菜谱
                String json = listContent.get(i).get("json");
                if (!TextUtils.isEmpty(json)) {
                    final Map<String, String> jsonMap = StringManager.getFirstMap(json);
                    if (jsonMap.containsKey("type") && !TextUtils.isEmpty(jsonMap.get("type"))) {
                        String datatype = jsonMap.get("type");
                        if ("ds".equals(datatype)) {
                            CommodityItemView commodityItemView = new CommodityItemView(this);
                            commodityItemView.setData(jsonMap);
                            commodityItemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!TextUtils.isEmpty(jsonMap.get("url"))){
                                        AppCommon.openUrl(ArticleDetailActivity.this,jsonMap.get("url"),true);
                                    }
                                }
                            });
                            linearLayoutTwo.addView(commodityItemView);
                        } else if ("caipu".equals(datatype)) {
                            DishItemView dishItemView = new DishItemView(this);
                            dishItemView.setData(jsonMap);
                            dishItemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!TextUtils.isEmpty(jsonMap.get("url"))){
                                        AppCommon.openUrl(ArticleDetailActivity.this,jsonMap.get("url"),true);
                                    }
                                }
                            });
                            linearLayoutTwo.addView(dishItemView);
                        }
                    }
                }
            }
        }
        detailAdapter.notifyDataSetChanged();
    }

    /**
     * 请求评论列表
     */
    private void requestForumData() {
        String url = StringManager.api_forumList;
        String param = "type="+getType()+"&code=" + code + "&page=1&pagesize=3";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMap = StringManager.getListMapByJson(object);
                    analysForumData(listMap);
                } else {
                    toastFaildRes(flag, true, object);
                }
                requestRelateData();
            }
        });
    }

    private void analysForumData(@NonNull ArrayList<Map<String, String>> arrayFourm) {
        if (arrayFourm.isEmpty()) return;
        addLineView();
        int dp_20 = Tools.getDimen(this, R.dimen.dp_20);
        TextView textView = new TextView(this);
        textView.setTextColor(Color.parseColor("#535353"));
        textView.setPadding(dp_20, 0, dp_20, 0);
        textView.setText("评论(" + commentNum + ")");
        linearLayoutThree.addView(textView);
        for (final Map<String, String> map : arrayFourm) {
            final ViewCommentItem commentItem = new ViewCommentItem(this);
            commentItem.setData(map);
            commentItem.setCommentItemListener(new ViewCommentItem.OnCommentItenListener() {
                @Override
                public void onShowAllReplayClick(String comment_id) {
                    StringBuilder sbuild = new StringBuilder();
                    sbuild.append("type=").append(getType()).append("&")
                            .append("code=").append(code).append("&")
                            .append("commentId=").append(comment_id).append("&")
                            .append("pagesize=").append(Integer.parseInt(map.get("replay_num")) + 3).append("&");

                    ReqEncyptInternet.in().doEncypt(StringManager.api_replayList, sbuild.toString(),
                            new InternetCallback(ArticleDetailActivity.this) {
                                @Override
                                public void loaded(int flag, String url, Object obj) {
                                    if (flag >= -ReqEncyptInternet.REQ_OK_STRING) {
                                        commentItem.addReplayView((String) obj);
                                    }
                                }
                            });
                }

                @Override
                public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name) {

                }

                @Override
                public void onDeleteCommentClick(String comment_id) {

                }

                @Override
                public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name) {

                }

                @Override
                public void onDeleteReplayClick(String comment_id, String replay_id) {

                }

                @Override
                public void onPraiseClick(String comment_id) {
                    Intent intent = new Intent(ArticleDetailActivity.this, CommentActivity.class);
                    intent.putExtra("type", getType());
                    intent.putExtra("code", code);
                    startActivity(intent);
                }

                @Override
                public void onContentReplayClick(String comment_id, String replay_user_code, String replay_user_name) {
                    Intent intent = new Intent(ArticleDetailActivity.this, CommentActivity.class);
                    intent.putExtra("type", getType());
                    intent.putExtra("code", code);
                    startActivity(intent);
                }
            });
            linearLayoutThree.addView(commentItem);
        }
        //查看所有评论
        TextView allComment = new TextView(this);
        allComment.setTextColor(Color.parseColor("#333333"));
        allComment.setGravity(Gravity.CENTER);
        allComment.setTextSize(Tools.getDimenSp(this, R.dimen.sp_15));
        allComment.setText("查看所有评论>");
        linearLayoutThree.addView(allComment, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.getDimen(this, R.dimen.dp_56)));
        allComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArticleDetailActivity.this, CommentActivity.class);
                intent.putExtra("type", getType());
                intent.putExtra("code", code);
                startActivity(intent);
            }
        });
        addLineView();
        linearLayoutThree.setVisibility(View.VISIBLE);
    }

    private void addLineView() {
        View view = new View(this);
        view.setBackgroundResource(R.color.common_bg);
        linearLayoutThree.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.getDimen(this, R.dimen.dp_11)));
    }

    /**
     * 请求推荐列表
     */
    private void requestRelateData() {
        String url = getRelatedAPI();
        String param = "page=" + ++page + "&pagesize=10";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMap = StringManager.getListMapByJson(object);
                    int size = listMap.size();
                    for (int i = 0; i < size; i++) {
                        listMap.get(i).put("datatype", String.valueOf(Type_recommed));
                    }
                    analysRelateData(listMap);
                } else {
                    toastFaildRes(flag, true, object);
                }
            }
        });
    }

    /**
     * 解析推荐数据
     *
     * @param ArrayRelate
     */
    private void analysRelateData(@NonNull ArrayList<Map<String, String>> ArrayRelate) {
        if (ArrayRelate.isEmpty()) return;
        for (Map<String, String> map : ArrayRelate) {
            map.put("clickAll", map.get("clickAll") + "浏览");
            map.put("commentNumber", map.get("commentNumber") + "评论");
        }
        otherListMap.addAll(ArrayRelate);

        if (otherListMap.size() > 0)
            otherListMap.get(0).put("showheader", "1");
        detailAdapter.notifyDataSetChanged();
    }

    public String getType(){
        return TYPE_ARTICLE;
    }

    public String getTitleText(){
        return "文章详情页";
    }

    public String getInfoAPI(){
        return StringManager.api_getArticleInfo;
    }

    public String getRelatedAPI(){
        return StringManager.api_getArticleRelated;
    }

    public String getPraiseAPI(){
        return StringManager.api_likeForum;
    }

}
