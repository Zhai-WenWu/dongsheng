package amodule.article.activity;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.utils.VDPlayPauseHelper;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.load.AutoLoadMore;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.adapter.ArticleDetailAdapter;
import amodule.article.view.ArticleCommentBar;
import amodule.article.view.ArticleHeaderView;
import amodule.article.view.VideoShowView;
import amodule.quan.view.VideoImageView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.share.BarShare;

import static amodule.article.adapter.ArticleDetailAdapter.Type_caipu;
import static amodule.article.adapter.ArticleDetailAdapter.Type_comment;
import static amodule.article.adapter.ArticleDetailAdapter.Type_ds;
import static amodule.article.adapter.ArticleDetailAdapter.Type_gif;
import static amodule.article.adapter.ArticleDetailAdapter.Type_image;
import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;
import static amodule.article.adapter.ArticleDetailAdapter.Type_text;
import static amodule.article.adapter.ArticleDetailAdapter.Type_video;

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
    private boolean isKeyboradShow = false;
    private ListView listview;
    private LinearLayout layout, linearLayoutOne, linearLayoutTwo, linearLayoutThree;//头部view
    private int mHeaderCount;

    private String commentNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            code = bundle.getString("code");
        }
        //TODO 测试
        if(TextUtils.isEmpty(code))
            code = "520";
        init();
    }

    //**********************************************Activity生命周期方法**************************************************

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
        findViewById(R.id.back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReqEncyptInternet.in().doEncypt(StringManager.api_articleDel, "code=" + code, new InternetCallback(ArticleDetailActivity.this) {
                    @Override
                    public void loaded(int i, String s, Object o) {
                    }
                });
            }
        });
        findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.showToast(ArticleDetailActivity.this,"编辑");
            }
        });
        listview = (ListView) findViewById(R.id.listview);
        listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        ToolsDevice.keyboardControl(false, ArticleDetailActivity.this, mArticleCommentBar.getEditText());
                        break;
                }
                return false;
            }
        });
        initHeaderView();
        listview.addHeaderView(layout);
        mHeaderCount++;
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
        detailAdapter = new ArticleDetailAdapter(otherListMap, getType(), code);
        detailAdapter.setVideoClickCallBack(new VideoShowView.VideoClickCallBack() {
            @Override
            public void videoOnClick(int position) {
                int firstVisiPosi = listview.getFirstVisiblePosition();
                View parentView = listview.getChildAt(position - firstVisiPosi + mHeaderCount);
                setVideoLayout(parentView, position);
            }
        });
        loadManager.setLoading(listview, detailAdapter, true,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (page >= 1) {
                            requestRelateData();
                        }
                    }
                }, new AutoLoadMore.OnListScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        stopVideo();
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        isScrollData = true;
                        if (scrollDataIndex < (firstVisibleItem + visibleItemCount - 1)) {
                            scrollDataIndex = (firstVisibleItem + visibleItemCount - 1);
                        }
                        if (mPlayPosition != -1) {
                            //正在播放的视频滑出屏幕
                            if ((mPlayPosition + mHeaderCount) < firstVisibleItem || (mPlayPosition + mHeaderCount) > (firstVisibleItem + visibleItemCount - 1)) {
                                stopVideo();
                            }
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

        //TODO 测试注释
//        if(LoginManager.isLogin()){
//            Map<String,String> customerData = StringManager.getFirstMap(mapArticle.get("customer"));
//            if(TextUtils.isEmpty(LoginManager.userInfo.get("code"))
//                    && TextUtils.isEmpty(customerData.get("code"))
//                    && customerData.get("code").equals(LoginManager.userInfo.get("code"))){
                findViewById(R.id.delete).setVisibility(View.VISIBLE);
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
//            }
//
//        }

    }

    /**
     * 解析图文混排数据
     *
     * @param content
     */
    private void analysArticleContent(String content) {
        if (TextUtils.isEmpty(content)) return;
        ArrayList<Map<String, String>> listContent = StringManager.getListMapByJson(content);
        int size = listContent.size();
        if (size > 0) linearLayoutTwo.setVisibility(View.VISIBLE);
        for (int i = 0; i < size; i++) {
            Map<String, String> map = listContent.get(i);
            String type = map.get("type");
            if ("text".equals(type)) {//文章
                map.put("datatype", String.valueOf(Type_text));
                otherListMap.add(map);
            } else if ("image".equals(type) || "gif".equals(type)) {//图片
                String imageUrl = map.get("gif".equals(type) ? "gifurl" : "imageurl");
                map.put("datatype", String.valueOf("gif".equals(type) ? Type_gif : Type_image));
                map.put("imageUrl", imageUrl);
                otherListMap.add(map);
            } else if ("video".equals(type)) {//视频
                Map<String, String> videoMap = StringManager.getFirstMap(map.get("video"));
                String videoUrl = videoMap.get("url");
                String videoImageUrl = videoMap.get("videoImg");
                map.put("datatype", String.valueOf(Type_video));
                map.put("videoUrl", videoUrl);
                map.put("videoImageUrl", videoImageUrl);
                otherListMap.add(map);
            } else if ("xiangha".equals(type)) {//自定义演示。ds，电商，caipu，菜谱
                String json = map.get("json");
                if (!TextUtils.isEmpty(json)) {
                    final Map<String, String> jsonMap = StringManager.getFirstMap(json);
                    if (jsonMap.containsKey("type") && !TextUtils.isEmpty(jsonMap.get("type"))) {
                        String datatype = jsonMap.get("type");
                        if ("ds".equals(datatype)) {
                            jsonMap.put("datatype", String.valueOf(Type_ds));
                            otherListMap.add(jsonMap);
                        } else if ("caipu".equals(datatype)) {
                            jsonMap.put("datatype", String.valueOf(Type_caipu));
                            otherListMap.add(jsonMap);
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
        String param = "type=" + getType() + "&code=" + code + "&page=1&pagesize=3";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> map = new HashMap<>();
                    map.put("datatype", String.valueOf(Type_comment));
                    map.put("data", object.toString());
                    map.put("commentNum", commentNum);
                    otherListMap.add(map);
                } else {
                    toastFaildRes(flag, true, object);
                }
                requestRelateData();
            }
        });
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
        if (page == 1)
            ArrayRelate.get(0).put("showheader", "1");
        otherListMap.addAll(ArrayRelate);

        detailAdapter.notifyDataSetChanged();
    }

    private VideoImageView mVideoImageView;
    private RelativeLayout mVideoLayout;
    /** 正在播放的位置，默认-1，即没有正在播放的 */
    private int mPlayPosition = -1;
    private View mPlayParentView = null;

    private VDPlayPauseHelper mVDPlayPauseHelper;

    private boolean isScrollData = false;//是否滚动数据
    private int scrollDataIndex = -1;//滚动数据的位置

    /**
     * 处理view,video
     *
     * @param parentView
     * @param position
     */
    private void setVideoLayout(final View parentView, final int position) {
        if (parentView == null || position < 0 || position >= otherListMap.size())
            return;
        if (otherListMap.get(position).containsKey("video") && !TextUtils.isEmpty(otherListMap.get(position).get("video"))) {
            Map<String, String> videoData = StringManager.getFirstMap(otherListMap.get(position).get("video"));
            if (mVideoImageView == null)
                mVideoImageView = new VideoImageView(this, false);
            mVideoImageView.setImageBg(otherListMap.get(position).get("img"));
            if (videoData != null) {
                String videoUrl = videoData.get("videoUrl");
                if (!TextUtils.isEmpty(videoUrl)) {
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(videoUrl);
                    if (maps != null && maps.size() > 0) {
                        String videoD = "";
                        int width = ToolsDevice.getWindowPx(this).widthPixels;
                        for (Map<String, String> map : maps) {
                            if (map != null) {
                                if (width <= 480 && map.containsKey("D480p")) {
                                    videoD = map.get("D480p");
                                } else if (width > 720 && map.containsKey("D1080p")) {
                                    videoD = map.get("D1080p");
                                } else if (map.containsKey("D720p")) {
                                    videoD = map.get("D720p");
                                }
                            }
                        }
                        mVideoImageView.setVideoData(videoD);
                    }
                }
            }
            mVideoImageView.setVideoCycle(false);
            mVideoImageView.setVisibility(View.VISIBLE);
            if (mVideoLayout != null && mVideoLayout.getChildCount() > 0) {
                mVideoLayout.removeAllViews();
            }

            mVideoLayout = (RelativeLayout) parentView.findViewById(R.id.video_layout);
            mVideoLayout.addView(mVideoImageView);
            mVideoImageView.onBegin();
            mPlayPosition = position;
            mPlayParentView = parentView;
            final View resumeView = parentView.findViewById(R.id.video_cover_image_play);
            mVideoImageView.setVideoClickCallBack(new VideoImageView.VideoClickCallBack() {
                @Override
                public void setVideoClick() {
                    if (resumeView != null)
                        resumeView.setVisibility(isPlaying() ? View.VISIBLE : View.GONE);
                    playPause();
                }
            });
        }
    }

    /**
     * 暂停播放
     */
    public void stopVideo() {
        if (mVideoImageView != null) {
            mPlayPosition = -1;
            if (mPlayParentView != null) {
                View resumeView = mPlayParentView.findViewById(R.id.resume_img);
                if (resumeView != null && resumeView.getVisibility() != View.GONE)
                    resumeView.setVisibility(View.GONE);
            }
            mPlayParentView = null;
            mVideoImageView.onVideoPause();
            if (mVideoLayout != null)
                mVideoLayout.removeAllViews();
        }
    }

    /**
     * 重播
     */
    private void restartVideo() {
        VDVideoViewController controller = VDVideoViewController.getInstance(this);
        if (controller != null) {
            controller.resume();
            controller.start();
        }
    }

    /**
     * 播放/暂停
     */
    private void playPause() {
        if (mVDPlayPauseHelper == null)
            mVDPlayPauseHelper = new VDPlayPauseHelper(this);
        mVDPlayPauseHelper.doClick();
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    private boolean isPlaying() {
        return mVideoImageView == null ? false : mVideoImageView.getIsPlaying();
    }

    public String getType() {
        return TYPE_ARTICLE;
    }

    public String getTitleText() {
        return "文章详情页";
    }

    public String getInfoAPI() {
        return StringManager.api_getArticleInfo;
    }

    public String getRelatedAPI() {
        return StringManager.api_getArticleRelated;
    }

    public String getPraiseAPI() {
        return StringManager.api_likeArticle;
    }

}
