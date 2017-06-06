package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.AutoLoadMore;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.edit.ArticleEidtActiivty;
import amodule.article.adapter.ArticleDetailAdapter;
import amodule.article.view.ArticleCommentBar;
import amodule.article.view.ArticleContentBottomView;
import amodule.article.view.ArticleHeaderView;
import amodule.article.view.BottomDialog;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.ad.scrollerAd.XHAllAdControl;
import third.share.BarShare;
import xh.windowview.XhDialog;

import static amodule.article.adapter.ArticleDetailAdapter.Type_comment;
import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;
import static third.ad.tools.AdPlayIdConfig.DETAIL_DISH_MAKE;

/** 文章详情 */
public class ArticleDetailActivity extends BaseActivity {
    public static final String TYPE_ARTICLE = "1";
    public static final String TYPE_VIDEO = "2";
    private String code = "";//请求数据的code
    private int page = 0;//相关推荐的page
    private ArticleDetailAdapter detailAdapter;
    private ArrayList<Map<String, String>> allDataListMap = new ArrayList<>();//评论列表和推荐列表对数据集合
    private ArticleCommentBar mArticleCommentBar;
    private boolean isKeyboradShow = false;
    private ListView listview;
    private LinearLayout layout, linearLayoutOne, linearLayoutTwo, linearLayoutThree;//头部view
    private ImageView rightButton;
    private ArticleContentBottomView articleContentBottomView;
    private XHAllAdControl xhAllAdControl;

    private String commentNum;
    private Map<String, String> adDataMap;
    private boolean isAdShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            code = bundle.getString("code");
        if (TextUtils.isEmpty(code)) code = "1078";

        Log.i("tzy", "code = " + code);
        init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (loadManager != null)
            loadManager.hideProgressBar();
    }

    /** 初始化 **/
    private void init() {
        initActivity(getTitleText(), 2, 0, 0, R.layout.a_article_detail);
        initView();
        initData();
    }

    private View adView;

    private void showAD(Map<String, String> dataMap) {
        if (articleContentBottomView == null) return;
        adView = LayoutInflater.from(this).inflate(R.layout.a_article_detail_ad, null);
        //加载图片
        ImageView imageView = (ImageView) adView.findViewById(R.id.img);
        int width = ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_20) * 2;
        int height = width * 312 / 670;//312 670
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        Glide.with(this).load(dataMap.get("imgUrl")).centerCrop().into(imageView);
        //加载title
        TextView title = (TextView) adView.findViewById(R.id.title);
        title.setText(new StringBuilder().append(dataMap.get("title")).append(" | ").append(dataMap.get("desc")));
        //设置ad点击
        adView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(adView, 0, "0");
            }
        });
        adView.findViewById(R.id.ad_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.setAdHintClick(ArticleDetailActivity.this, adView.findViewById(R.id.ad_tag), xhAllAdControl, 0, "0");
            }
        });
        articleContentBottomView.addViewToAdLayout(adView);
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
        //初始化title
        rightButton = (ImageView) findViewById(R.id.rightImgBtn2);
        ImageView leftImage = (ImageView) findViewById(R.id.leftImgBtn);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) leftImage.getLayoutParams();
        layoutParams.setMargins(Tools.getDimen(this, R.dimen.dp_15), 0, 0, 0);
        leftImage.setLayoutParams(layoutParams);

        findViewById(R.id.back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
        //初始化listview
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
        mArticleCommentBar = (ArticleCommentBar) findViewById(R.id.acticle_comment_bar);
        mArticleCommentBar.setCode(code);
        mArticleCommentBar.setType(getType());
        mArticleCommentBar.setOnCommentSuccessCallback(new ArticleCommentBar.OnCommentSuccessCallback() {
            @Override
            public void onCommentSuccess(boolean isSofa) {
                if (isSofa)
                    requestForumData(true);
            }
        });
    }

    /** 初始化header布局 */
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
        detailAdapter = new ArticleDetailAdapter(this, allDataListMap, getType(), code);
        detailAdapter.setOnRabSofaCallback(new ArticleDetailAdapter.OnRabSofaCallback() {
            @Override
            public void onRabSoaf() {
                mArticleCommentBar.doComment("抢沙发");
            }
        });
        loadManager.setLoading(listview, detailAdapter, true,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (page >= 1)
                            requestRelateData();
                    }
                }, new AutoLoadMore.OnListScrollListener() {
                    int srceenHeight = ToolsDevice.getWindowPx(ArticleDetailActivity.this).heightPixels;
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (adView != null && !isAdShow) {
                            int[] location = new int[2];
                            adView.getLocationOnScreen(location);
                            if (location[1] > 0
                                    && location[1] < srceenHeight * 3 / 4) {
                                isAdShow = true;
                                xhAllAdControl.onAdBind(0, adView, "0");
                            }
                        }
                    }
                });
        requestArticleData();
        //请求广告数据
        requestAdData();
    }

    private void requestAdData() {
        final String[] ads = new String[]{DETAIL_DISH_MAKE};
        ArrayList<String> adData = new ArrayList<>();
        adData.add(DETAIL_DISH_MAKE);
        xhAllAdControl = new XHAllAdControl(adData,
                new XHAllAdControl.XHBackIdsDataCallBack() {
                    @Override
                    public void callBack(Map<String, String> map) {
                        for (String key : ads) {
                            String adStr = map.get(key);
                            if (!TextUtils.isEmpty(adStr)) {
                                adDataMap = StringManager.getFirstMap(adStr);
                                if (adDataMap != null && adDataMap.size() > 0) {
                                    showAD(adDataMap);
                                    detailAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }, this, "统计id");
    }

    /** 请求网络 */
    private void requestArticleData() {
        String url = getInfoAPI();
        String params = TextUtils.isEmpty(code) ? "" : "code=" + code;
        params += "&type=HTML";
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
                loadManager.hideProgressBar();
            }
        });
    }

    /**
     * 解析文章数据
     *
     * @param mapArticle
     */
    private void analysArticleData(@NonNull final Map<String, String> mapArticle) {
        if (mapArticle.isEmpty()) return;
        findViewById(R.id.rightImgBtn2).setVisibility(View.VISIBLE);
        ArticleHeaderView headerView = new ArticleHeaderView(ArticleDetailActivity.this);
        headerView.setType(getType());
        headerView.setData(mapArticle);
        linearLayoutOne.addView(headerView);
        linearLayoutOne.setVisibility(View.VISIBLE);
        detailAdapter.notifyDataSetChanged();
        listview.setVisibility(View.VISIBLE);
        commentNum = mapArticle.get("commentNumber");
        mArticleCommentBar.setPraiseAPI(getPraiseAPI());
        mArticleCommentBar.setData(mapArticle);

        WebviewManager manager = new WebviewManager(this, loadManager, true);
        manager.setOnWebviewLoadFinish(new WebviewManager.OnWebviewLoadFinish() {
            @Override
            public void onLoadFinish() {
                requestForumData(false);//请求
                linearLayoutThree.setVisibility(View.VISIBLE);
            }
        });
        XHWebView webView = manager.createWebView(0);
        manager.setJSObj(webView, new JsAppCommon(this, webView, loadManager, barShare));
        linearLayoutTwo.addView(webView);
        linearLayoutTwo.setVisibility(View.VISIBLE);
        webView.loadDataWithBaseURL("", mapArticle.get("html"), "text/html", "utf-8", null);

        final Map<String, String> customerData = StringManager.getFirstMap(mapArticle.get("customer"));
        final String userCode = customerData.get("code");
        final boolean isAuthor = LoginManager.isLogin()
                && !TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                && !TextUtils.isEmpty(userCode)
                && userCode.equals(LoginManager.userInfo.get("code"));
        rightButton.setImageResource(isAuthor ? R.drawable.i_ad_more : R.drawable.z_z_topbar_ico_share);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAuthor) {
                    showBottomDialog();
                } else {
                    openShare();
                    statistics("分享", "");
                }
            }
        });

        articleContentBottomView = new ArticleContentBottomView(this);
        articleContentBottomView.setData(mapArticle);
        linearLayoutThree.addView(articleContentBottomView);

        if (!isAuthor) {
            articleContentBottomView.setOnReportClickCallback(new ArticleContentBottomView.OnReportClickCallback() {
                @Override
                public void onReportClick() {
                    Intent intent = new Intent(ArticleDetailActivity.this, ReportActivity.class);
                    intent.putExtra("code", code);
                    intent.putExtra("type", getType());
                    intent.putExtra("userCode", userCode);
                    intent.putExtra("reportName", customerData.get("nickName"));
                    intent.putExtra("reportContent", mapArticle.get("title"));
                    intent.putExtra("reportType", "1");
                    startActivity(intent);
                }
            });
        }
        if (adDataMap != null)
            showAD(adDataMap);

        detailAdapter.notifyDataSetChanged();
    }

    /** 请求评论列表 */
    private void requestForumData(final boolean isRefresh) {
        String url = StringManager.api_forumList;
        String param = "from=1&type=" + getType() + "&code=" + code + "&pageSize=3";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> map = StringManager.getFirstMap(object);
                    map.put("datatype", String.valueOf(Type_comment));
                    map.put("data", object.toString());
                    map.put("commentNum", commentNum);
                    if (isRefresh) {
                        int commentCount = Integer.parseInt(commentNum);
                        map.put("commentNum", "" + ++commentCount);
                        allDataListMap.set(0, map);
                    } else
                        allDataListMap.add(map);
                    detailAdapter.notifyDataSetChanged();
                } else
                    toastFaildRes(flag, true, object);
                if (page < 1)
                    requestRelateData();
            }
        });
    }

    /** 请求推荐列表 */
    private void requestRelateData() {
        String url = getRelatedAPI();
        String param = "code=" + code + "&page=" + ++page + "&pagesize=10";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMap = StringManager.getListMapByJson(object);
                    int size = listMap.size();
                    for (int i = 0; i < size; i++)
                        listMap.get(i).put("datatype", String.valueOf(Type_recommed));
                    analysRelateData(listMap);
                } else
                    toastFaildRes(flag, true, object);
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
        allDataListMap.addAll(ArrayRelate);

        detailAdapter.notifyDataSetChanged();
    }

    private void showBottomDialog() {
        BottomDialog dialog = new BottomDialog(this);
        dialog.addButton("分享", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShare();
                statistics("更多", "分享");
            }
        }).addButton("编辑", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArticleDetailActivity.this, getIntentClass());
                intent.putExtra("code", code);
                startActivity(intent);
                statistics("更多", "编辑");
            }
        }).addButton("删除", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteDialog();
            }
        });
        dialog.show();
    }

    private void openShare() {
        barShare = new BarShare(ArticleDetailActivity.this, "精选菜单", "菜单");
        String type = BarShare.IMG_TYPE_RES;
        String shareImg = "" + R.drawable.umen_share_launch;
        String title = "精选菜单大全，强烈推荐！";
        String clickUrl = StringManager.wwwUrl + "caipu/caidan";
        String content = "最近一直在用香哈菜谱，内容好、分类全，还可以离线下载菜谱~";
        barShare.setShare(type, title, content, shareImg, clickUrl);
        barShare.openShare();
    }

    private void openDeleteDialog() {
        final XhDialog dialog = new XhDialog(this);
        dialog.setMessage("确定删除这篇文章吗？")
                .setCanselButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                })
                .setSureButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        ReqEncyptInternet.in().doEncypt(StringManager.api_articleDel, "code=" + code,
                                new InternetCallback(ArticleDetailActivity.this) {
                                    @Override
                                    public void loaded(int flag, String url, Object obj) {
                                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                                            //自动关闭
                                            ArticleDetailActivity.this.finish();
                                        } else {
                                            toastFaildRes(flag, true, obj);
                                        }
                                    }
                                });
                        statistics("更多", "删除");
                    }
                }).show();
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

    public Class<?> getIntentClass() {
        return ArticleEidtActiivty.class;
    }

    private void statistics(String twoLevel, String threeLevel) {
        String eventId = "";
        switch (getType()) {
            case TYPE_ARTICLE:
                eventId = "a_ArticleDetail";
                break;
            case TYPE_VIDEO:
                eventId = "a_ShortVideoDetail";
                break;
        }
        if (TextUtils.isEmpty(eventId))
            return;
        XHClick.mapStat(this, eventId, twoLevel, threeLevel);
    }
}
