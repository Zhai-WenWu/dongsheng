package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
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
import amodule.article.view.VideoShowView;
import amodule.nous.activity.NousInfo;
import amodule.quan.view.VideoImageView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.share.BarShare;
import xh.windowview.XhDialog;

import static amodule.article.adapter.ArticleDetailAdapter.Type_articleinfo;
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
    private ArrayList<Map<String, String>> allDataListMap = new ArrayList<>();//评论列表和推荐列表对数据集合
    private ArticleCommentBar mArticleCommentBar;
    private boolean isKeyboradShow = false;
    private ListView listview;
    private LinearLayout layout, linearLayoutOne, linearLayoutTwo, linearLayoutThree;//头部view
    private ImageView rightButton;
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
        if (TextUtils.isEmpty(code))
            code = "520";

        Log.i("tzy", "code = " + code);
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
        //初始化title
        rightButton = (ImageView) findViewById(R.id.rightImgBtn2);

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
        detailAdapter = new ArticleDetailAdapter(this, allDataListMap, getType(), code);
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

    private String htmlData = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta name=\"copyright\" content=\"Copyright © 2014 XiangHa.com\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=no\"/><link rel=\"dns-prefetch\" href=\"//upload.xiangha.com\"/><link rel=\"dns-prefetch\" href=\"//static.xiangha.com\"/><link rel=\"shortcut icon\" href=\"http://f1.xiangha.com/img/xiangha.ico\" type=\"image/x-icon\" /><title>香哈头条</title><meta name=\"keywords\" content=\"简单易上手的6种剩饺子皮的做法,美食知识,香哈网\" /><meta name=\"description\" content=\"平时我们在家包饺子，有时候馅用完了，饺子皮却还剩一部分。剩余的饺子皮怎么处理，是一个让人头疼的问题。今天，小编教你6种剩饺子皮的做法，不仅不浪费还...\" /><link rel=\"shortcut icon\" href=\"http://f1.xiangha.com/img/xiangha.ico\" type=\"image/x-icon\" /><script src=\"//cdn.bootcss.com/jquery/1.7.2/jquery.min.js\"></script><script src=\"//res.wx.qq.com/open/js/jweixin-1.0.0.js\"></script><script type=\"text/javascript\"> var DOMAIN = 'http://www.xiangha.com'; var APPWEB = 'http://appweb.xiangha.com'; </script><link type=\"text/css\" rel=\"stylesheet\" href=\"//f1.xiangha.com/css/app5.css?vs=20170603171713\" /></head><body ><div class=\"knowledge\"><div class=\"zhishi\" style=\"padding-top:10px;\"><h1>简单易上手的6种剩饺子皮的做法</h1><div class=\"zhishi_bar\">2017-05-09&nbsp;&nbsp;&nbsp;香哈&nbsp;&nbsp;&nbsp;<span>烹饪技巧</span></div><div class=\"zhishi_main\"><div class=\"content\" style=\"overflow: hidden;\"><section data-role=\"outer\" label=\"Powered by 135editor.com\" style=\"font-family: 微软雅黑;\"><section data-role=\"outer\" label=\"Powered by 135editor.com\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">平时我们在家包饺子，有时候馅用完了，饺子皮却还剩一部分。剩余的饺子皮怎么处理，是一个让人头疼的问题。今天，小编教你6种剩饺子皮的做法，不仅不浪费还简单易上手，保管香的你口水直流，感兴趣的快来学学吧。</span></p><section class=\"_135editor\" data-tools=\"135编辑器\" data-id=\"88098\" style=\"border: 0px none; padding: 0px; position: relative; box-sizing: border-box;\"><section style=\"text-align: center;\"><section style=\"margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;\"><section style=\"margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);\">饺子皮小馅饼</span></p></section></section></section></section><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/21235425850.jpg/NjAwX2MxXzQwMA\"/><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>by 一点靓妆</strong></span></p><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>配料:</strong><br/>饺子皮16张、猪肉适量、韭菜适量、盐适量、生抽适量、植物油适量、鸡精少许 <br/><br/><strong>烹饪步骤:</strong><br/>1.准备好饺子皮 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354262458.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">2.猪肉洗净剁成末，加适量盐、生抽、鸡精拌匀；韭菜洗净切成切碎，加入猪肉末中一起搅拌均匀 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354272146.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">3.取一张饺子皮，放上适量猪肉韭菜馅 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354279311.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">4.再盖上一张饺子皮 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354289762.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">5.边缘用叉子压出纹路 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354296565.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">6.依次完成 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354304881.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">7.平底锅放少许油，放入饼小火慢煎至两面黄亮即可 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354311287.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">8.装盘趁热吃，切开一个香气扑鼻啊！ <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201702/2123/212354319107.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><strong><span style=\"font-family: arial, helvetica, sans-serif;\">菜谱小贴士:&nbsp;</span></strong></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">饺子皮很容易熟，小火慢煎，保证肉馅熟透。&nbsp;</span></p><section class=\"_135editor\" data-tools=\"135编辑器\" data-id=\"88098\" style=\"border: 0px none; padding: 0px; box-sizing: border-box;\"><section style=\"text-align: center;\"><section style=\"margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;\"><section style=\"margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);\">mini饺子皮芝士披萨</span></p></section></section></section></section><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><img src=\"http://s1.cdn.xiangha.com/caipu/201608/1011/101105234363.jpg/NjAwX2MxXzQwMA\"/></p><h3 style=\"font-size: 16px; line-height: 1.6em;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>by 一食半刻</strong></span></h3><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>配料:</strong><br/>饺子皮4片、小番茄1个、火腿1片、菠萝片3片、马苏里拉适量、洋葱适量、青红椒适量、水少量、番茄酱适量 <br/><br/><strong>烹饪步骤:</strong><br/>1.准备好材料，洋葱、青红椒切条，火腿、菠萝切片 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201608/1011/10110524913.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">2.多士炉预热，花式平铺饺子皮，在锅边放少点水 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201608/1011/101105244709.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">3.涂抹上番茄酱 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201608/1011/101105248254.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">4.盖上一层马苏里拉，后依次放入菠萝片、洋葱条、火腿片、青红椒，最后再盖一层马苏里拉 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201608/1011/101105251992.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">5.盖上盖子3分钟直至马苏里拉融化，就可以出锅啦～ <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201608/1011/101105255787.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><strong><span style=\"font-family: arial, helvetica, sans-serif;\">菜谱小贴士:&nbsp;</span></strong></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">友友们可以放辣酱或者海鲜酱哦～，还可以放爱的她喜欢的搭配～记得要放点水哦，这样饺子皮才完全熟透了呢～&nbsp;</span></p><section class=\"_135editor\" data-tools=\"135编辑器\" data-id=\"88098\" style=\"border: 0px none; padding: 0px; box-sizing: border-box;\"><section style=\"text-align: center;\"><section style=\"margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;\"><section style=\"margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);\">饺子皮版的菜饼</span></p></section></section></section></section><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201167172.jpg/NjAwX2MxXzQwMA\"/></p><h3 style=\"font-size: 16px; line-height: 1.6em;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>by 倍儿奶奶</strong></span></h3><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>配料:</strong><br/>饺子皮五个、胡萝卜小半个、葱花一小段、盐少许、酱油一汤勺、料酒半汤勺、香油小半勺、番茄酱适量 <br/><br/><strong>烹饪步骤:</strong><br/>1.备料，胡萝卜切丝，葱白切丝 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201173611.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">2.准备五个饺子皮 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201179507.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">3.调入老抽酱油一勺，料酒半勺，盐少许，香油小半勺，一起拌匀腌一会 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201185203.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">4.取一个饺子皮<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201189810.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">5.中间放腌ze的胡萝卜丝，不需要太多馅<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201193632.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">6.盖上一层饺子皮</span></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201197501.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">7.第二张饺子上继续放胡萝卜丝 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201202079.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">8.然后盖上第三张饺子皮，就这样以此类推做完，最后盖上第五张饺子皮。稍微擀一下，擀薄一点。别擀漏馅就可以<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201206850.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">9.备平底锅，烧热放饼，盖上锅盖小火烙<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201211579.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">10.小火烙至上色翻面，烙至两面金黄色可以 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201216132.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">11.出锅切上几刀，挤上番茄酱既可食用 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201610/3012/301201228074.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><strong><span style=\"font-family: arial, helvetica, sans-serif;\">菜谱小贴士:&nbsp;</span></strong></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">1.小火慢烙，火大容易烙胡，买成品饺子皮一样。自己做的这个饺子皮比较大个。&nbsp;</span></p><section class=\"_135editor\" data-tools=\"135编辑器\" data-id=\"88098\" style=\"border: 0px none; padding: 0px; box-sizing: border-box;\"><section style=\"text-align: center;\"><section style=\"margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;\"><section style=\"margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);\">饺子皮春饼</span></p></section></section></section></section><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808172366.jpg/NjAwX2MxXzQwMA\"/><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>by 玫瑰姐_2Eqx</strong></span></p><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>配料:</strong><br/>饺子皮适量、油少许 <br/><br/><strong>烹饪步骤:</strong><br/>1.每一层饺子皮都刷上油，7~8张摞在一起 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808185266.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">2.擀成饼，能擀多薄擀多薄 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808196539.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">3.上锅蒸，饼变色就熟了 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808211618.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">4.成品 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808222015.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">5.好薄啊 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808233610.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">6.卷上菜菜就可以吃啦 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201704/0508/050808244819.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/></p><p style=\"white-space: normal;\"><strong><span style=\"font-family: arial, helvetica, sans-serif;\">菜谱小贴士:&nbsp;</span></strong></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">刷油的时候要均匀 每一个地方都刷到 以免粘连&nbsp;</span></p><section class=\"_135editor\" data-tools=\"135编辑器\" data-id=\"88098\" style=\"border: 0px none; padding: 0px; box-sizing: border-box;\"><section style=\"text-align: center;\"><section style=\"margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;\"><section style=\"margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);\">饺子皮蛋挞</span></p></section></section></section></section><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811531666.jpg/NjAwX2MxXzQwMA\"/><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>by 紫陌_红尘</strong></span></p><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>配料:</strong><br/>饺子皮8个、蛋黄2个、牛奶80克、动物性淡奶油110克、细砂糖40克、葡萄干若干、注意蛋挞液是9个蛋挞量 <br/><br/><strong>烹饪步骤:</strong><br/>1.这是包饺子剩下的饺子皮 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811538302.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">2.将饺子皮放入蛋挞模中捏出形状<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811543511.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">3.将细砂糖放入热牛奶中<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/05181155528.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">4.搅拌均匀<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811556234.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">5.加入淡奶油<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811562672.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">6.再加入蛋黄<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811568069.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">7.搅拌均匀 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811573497.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">8.过筛两遍成细腻蛋挞液 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/05181158462.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">9.葡萄干放入饺子蛋挞皮中 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811584690.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">10.倒入七分满蛋挞液<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811589791.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">11.放入烤箱中层，上下火200度烤二十分钟<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051811596083.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">12.饺子皮蛋挞就烤好啦~来一个吧~(๑•͈ᴗ•͈)❀送蛋挞给你~ <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201612/0518/051812001276.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><strong><span style=\"font-family: arial, helvetica, sans-serif;\">菜谱小贴士:</span></strong></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">&nbsp;亲们还是去买蛋挞皮吧╮(‵▽′)╭&nbsp;</span></p><section class=\"_135editor\" data-tools=\"135编辑器\" data-id=\"88098\" style=\"border: 0px none; padding: 0px; box-sizing: border-box;\"><section style=\"text-align: center;\"><section style=\"margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;\"><section style=\"margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;\"><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);\">饺子皮葱油饼</span></p></section></section></section></section><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301346596874.jpg/NjAwX2MxXzQwMA\"/></p><h3 style=\"font-size: 16px; line-height: 1.6em;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>by 粗茶淡饭总相宜</strong></span></h3><p style=\"white-space: normal;\"><br/></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><strong>配料:</strong><br/>饺子皮20张、葱花适量、白芝麻适量、十三香适量、花生油适量、盐适量 <br/><br/><strong>烹饪步骤:</strong><br/>1.准备好材料<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347003855.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">2.先把葱花用盐、十三香和白芝麻拌匀 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/30134701177.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">3.5张饺子皮1组摊开 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347016266.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">4.把4张饺子皮放上拌好的葱花，剩下1张不用放<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347021660.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">5.先2张叠放再1层1层码好 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347029730.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">6.没放葱花的放在最上面盖住，用手压一下使饺子皮贴一些<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347036441.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">7.接着用擀面杖转圈地压<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/30134704846.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">8.擀得薄一些</span></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\"><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347046794.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">9.锅烧热倒入油继续加热后再转小火<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347053048.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">10.放入饺子皮煎制 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347059252.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">11.煎至金黄色即可 <br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347067330.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">12.再翻面煎另一面至金黄色，就做好了<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347073507.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><span style=\"font-family: arial, helvetica, sans-serif;\">13.香喷喷地，脆脆地，我共做了4组，一起叠放，开吃了<br/><br/></span><img src=\"http://s1.cdn.xiangha.com/caipu/201703/3013/301347079994.jpg/NjAwX2MxXzQwMA\" style=\"width:500px;\"/><br/><br/><strong><span style=\"font-family: arial, helvetica, sans-serif;\">菜谱小贴士:</span></strong><span style=\"font-family: arial, helvetica, sans-serif;\">&nbsp;</span></p><p style=\"white-space: normal;\"><span style=\"font-family: arial, helvetica, sans-serif;\">煎时要先把油烧热再转小火，慢煎</span></p><p style=\"white-space: normal;\"><br/></p></section></section><p><br/></p></div><p class=\"weixin-follow\">微信搜索 香哈菜谱 关注公众号，菜谱、窍门、养生、营养课堂全在这！</p></div><if condition=\"$theActivity\"><div class=\"zhishi_act\"><a href=\"\"><img src=\"\" /></a></div></if></div><div class=\"recomment-articles\"><h4 class=\"recomment-title\">推荐</h4><ul class=\"list-comments\"><li class=\"item-comment clearfix recommendNous\" nousCode='239782'><img class=\"article-left\" src=\"http://s1.cdn.xiangha.com/zhishi/201705/072024287395.jpg/MTYweDEyMA.webp\" alt=\"美食知识\"><div class=\"article-right\"><p class=\"article-title\"> 这8款茄子烧法太诱人了！ </p><p class=\"article-con clearfix\"><span class=\"fl\">香哈</span><span class=\"fr\">124949浏览</span></p></div></li><li class=\"item-comment clearfix recommendNous\" nousCode='239776'><img class=\"article-left\" src=\"http://s1.cdn.xiangha.com/zhishi/201705/072008449045.jpg/MTYweDEyMA.webp\" alt=\"美食知识\"><div class=\"article-right\"><p class=\"article-title\"> 无烧烤不夏天，烧烤秘籍你接住了 </p><p class=\"article-con clearfix\"><span class=\"fl\">香哈</span><span class=\"fr\">83544浏览</span></p></div></li><li class=\"item-comment clearfix recommendNous\" nousCode='239770'><img class=\"article-left\" src=\"http://s1.cdn.xiangha.com/zhishi/201705/062239082556.jpg/MTYweDEyMA.webp\" alt=\"美食知识\"><div class=\"article-right\"><p class=\"article-title\"> 5种快手米线做法，特别适合天热的时候吃 </p><p class=\"article-con clearfix\"><span class=\"fl\">香哈</span><span class=\"fr\">69650浏览</span></p></div></li></ul></div><p class=\"text-review\"><span>阅读 83747</span>&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"xiangha://welcome?url=subjectInfo.app?code=23398552\" class=\"com-img\">19</a><a class=\"complaint\" href=\"xiangha://welcome?url=dialog.app\">投诉</a></p><div class=\"block-comments\"><h4 class=\"select-message\"><span>精选留言</span></h4><ul class=\"list-comments\"><li class=\"item-comment clearfix\"><div class=\"item-left\"><a href=\"xiangha://welcome?url=userIndex.app?code=55377212030\"><img src=\"http://wx.qlogo.cn/mmopen/FgAo3KzY9Ka80Zkyo1ggK1ZMVSPegFhj941t9sSiaSmYARqsl8A2ibz7zwyicMVSnpGwmZrEfSphicDPSCzKDLzDcB7BRsVlia1tj/0\" class=\"user-head\"></a></div><div class=\"item-right\"><p class=\"user-name\"> sarah_ </p><a href=\"xiangha://welcome?url=subjectInfo.app?code=23398552\"><p class=\"content\"> 懒人创意多，真的不错！ </p><p class=\"time\">05-09 20:16</p></a></div></li><li class=\"item-comment clearfix\"><div class=\"item-left\"><a href=\"xiangha://welcome?url=userIndex.app?code=67536108387\"><img src=\"http://s1.cdn.xiangha.com/i/201705/1308/591658277adaa.jpg/MTAweDEwMA.webp\" class=\"user-head\"></a></div><div class=\"item-right\"><p class=\"user-name\"> 微韵百羚策划 </p><a href=\"xiangha://welcome?url=subjectInfo.app?code=23398552\"><p class=\"content\"> 厉害哦 </p><p class=\"time\">05-13 08:55</p></a></div></li><li class=\"item-comment clearfix\"><div class=\"item-left\"><a href=\"xiangha://welcome?url=userIndex.app?code=12590439958\"><img src=\"http://wx.qlogo.cn/mmopen/78EkX665csD3ibic4TDHGTPLBsaxXvxA9glL89N9AbicKVhg08FLKPHic1FCsZq9Q8sBia2szMpYap3duABlPbINUf6r5IZKkk0I9/0\" class=\"user-head\"></a></div><div class=\"item-right\"><p class=\"user-name\"> 都教授_MmPp </p><a href=\"xiangha://welcome?url=subjectInfo.app?code=23398552\"><p class=\"content\"> 可是为什么煎的时候要脱层呢？ </p><p class=\"time\">05-19 17:48</p></a></div></li></ul><p class=\"select-message leave-message\"><a class=\"btn-comment\" href=\"xiangha://welcome?url=subjectInfo.app?code=23398552\">写留言</a></p></div><div class=\"block-space\">&nbsp;</div></div><script> $(function(){  $(\".recommendNous\").click(function(){ var nousCode = $(this).attr('nousCode'); window.location.href = \"http://appweb.xiangha.com/zhishi/nousInfo?code=\"+nousCode; }); $('.recommendNous').each(function(){ if($(this).text().length <= 20){ $(this).remove(); } }); var code='239788'; var isFav = '1'; var share_title = '简单易上手的6种剩饺子皮的做法'; var share_con = '平时我们在家包饺子，有时候馅用完了，饺子皮却还剩一部分。剩余的饺子皮怎么处理，是一个让人头疼的问题。今天，小编教你6种剩饺子皮的做法，不仅不浪费还简单易上手，保管香的你口水直流，感兴趣的快来学学吧。饺'; var share_img = 'http://s1.cdn.xiangha.com/zhishi/201705/082242174581.jpg/MTUweDE1MA'; var share_url = 'http://m.xiangha.com/zhishi/239788.html'; var mark = '知识详情'; var allClick = '83747'; var content = '&lt;section data-role=&quot;outer&quot; label=&quot;Powered by 135editor.com&quot; style=&quot;font-family: 微软雅黑;&quot;&gt;&lt;section data-role=&quot;outer&quot; label=&quot;Powered by 135editor.com&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;平时我们在家包饺子，有时候馅用完了，饺子皮却还剩一部分。剩余的饺子皮怎么处理，是一个让人头疼的问题。今天，小编教你6种剩饺子皮的做法，不仅不浪费还简单易上手，保管香的你口水直流，感兴趣的快来学学吧。&lt;/span&gt;&lt;/p&gt;&lt;section class=&quot;_135editor&quot; data-tools=&quot;135编辑器&quot; data-id=&quot;88098&quot; style=&quot;border: 0px none; padding: 0px; position: relative; box-sizing: border-box;&quot;&gt;&lt;section style=&quot;text-align: center;&quot;&gt;&lt;section style=&quot;margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;&quot;&gt;&lt;section style=&quot;margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);&quot;&gt;饺子皮小馅饼&lt;/span&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/21235425850.jpg/NjAwX2MxXzQwMA&quot;/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;by 一点靓妆&lt;/strong&gt;&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;配料:&lt;/strong&gt;&lt;br/&gt;饺子皮16张、猪肉适量、韭菜适量、盐适量、生抽适量、植物油适量、鸡精少许 &lt;br/&gt;&lt;br/&gt;&lt;strong&gt;烹饪步骤:&lt;/strong&gt;&lt;br/&gt;1.准备好饺子皮 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354262458.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;2.猪肉洗净剁成末，加适量盐、生抽、鸡精拌匀；韭菜洗净切成切碎，加入猪肉末中一起搅拌均匀 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354272146.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;3.取一张饺子皮，放上适量猪肉韭菜馅 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354279311.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;4.再盖上一张饺子皮 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354289762.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;5.边缘用叉子压出纹路 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354296565.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;6.依次完成 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354304881.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;7.平底锅放少许油，放入饼小火慢煎至两面黄亮即可 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354311287.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;8.装盘趁热吃，切开一个香气扑鼻啊！ &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201702/2123/212354319107.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;菜谱小贴士:&amp;nbsp;&lt;/span&gt;&lt;/strong&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;饺子皮很容易熟，小火慢煎，保证肉馅熟透。&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;section class=&quot;_135editor&quot; data-tools=&quot;135编辑器&quot; data-id=&quot;88098&quot; style=&quot;border: 0px none; padding: 0px; box-sizing: border-box;&quot;&gt;&lt;section style=&quot;text-align: center;&quot;&gt;&lt;section style=&quot;margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;&quot;&gt;&lt;section style=&quot;margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);&quot;&gt;mini饺子皮芝士披萨&lt;/span&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201608/1011/101105234363.jpg/NjAwX2MxXzQwMA&quot;/&gt;&lt;/p&gt;&lt;h3 style=&quot;font-size: 16px; line-height: 1.6em;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;by 一食半刻&lt;/strong&gt;&lt;/span&gt;&lt;/h3&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;配料:&lt;/strong&gt;&lt;br/&gt;饺子皮4片、小番茄1个、火腿1片、菠萝片3片、马苏里拉适量、洋葱适量、青红椒适量、水少量、番茄酱适量 &lt;br/&gt;&lt;br/&gt;&lt;strong&gt;烹饪步骤:&lt;/strong&gt;&lt;br/&gt;1.准备好材料，洋葱、青红椒切条，火腿、菠萝切片 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201608/1011/10110524913.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;2.多士炉预热，花式平铺饺子皮，在锅边放少点水 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201608/1011/101105244709.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;3.涂抹上番茄酱 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201608/1011/101105248254.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;4.盖上一层马苏里拉，后依次放入菠萝片、洋葱条、火腿片、青红椒，最后再盖一层马苏里拉 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201608/1011/101105251992.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;5.盖上盖子3分钟直至马苏里拉融化，就可以出锅啦～ &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201608/1011/101105255787.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;菜谱小贴士:&amp;nbsp;&lt;/span&gt;&lt;/strong&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;友友们可以放辣酱或者海鲜酱哦～，还可以放爱的她喜欢的搭配～记得要放点水哦，这样饺子皮才完全熟透了呢～&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;section class=&quot;_135editor&quot; data-tools=&quot;135编辑器&quot; data-id=&quot;88098&quot; style=&quot;border: 0px none; padding: 0px; box-sizing: border-box;&quot;&gt;&lt;section style=&quot;text-align: center;&quot;&gt;&lt;section style=&quot;margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;&quot;&gt;&lt;section style=&quot;margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);&quot;&gt;饺子皮版的菜饼&lt;/span&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201167172.jpg/NjAwX2MxXzQwMA&quot;/&gt;&lt;/p&gt;&lt;h3 style=&quot;font-size: 16px; line-height: 1.6em;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;by 倍儿奶奶&lt;/strong&gt;&lt;/span&gt;&lt;/h3&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;配料:&lt;/strong&gt;&lt;br/&gt;饺子皮五个、胡萝卜小半个、葱花一小段、盐少许、酱油一汤勺、料酒半汤勺、香油小半勺、番茄酱适量 &lt;br/&gt;&lt;br/&gt;&lt;strong&gt;烹饪步骤:&lt;/strong&gt;&lt;br/&gt;1.备料，胡萝卜切丝，葱白切丝 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201173611.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;2.准备五个饺子皮 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201179507.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;3.调入老抽酱油一勺，料酒半勺，盐少许，香油小半勺，一起拌匀腌一会 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201185203.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;4.取一个饺子皮&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201189810.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;5.中间放腌ze的胡萝卜丝，不需要太多馅&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201193632.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;6.盖上一层饺子皮&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201197501.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;7.第二张饺子上继续放胡萝卜丝 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201202079.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;8.然后盖上第三张饺子皮，就这样以此类推做完，最后盖上第五张饺子皮。稍微擀一下，擀薄一点。别擀漏馅就可以&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201206850.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;9.备平底锅，烧热放饼，盖上锅盖小火烙&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201211579.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;10.小火烙至上色翻面，烙至两面金黄色可以 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201216132.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;11.出锅切上几刀，挤上番茄酱既可食用 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201610/3012/301201228074.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;菜谱小贴士:&amp;nbsp;&lt;/span&gt;&lt;/strong&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;1.小火慢烙，火大容易烙胡，买成品饺子皮一样。自己做的这个饺子皮比较大个。&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;section class=&quot;_135editor&quot; data-tools=&quot;135编辑器&quot; data-id=&quot;88098&quot; style=&quot;border: 0px none; padding: 0px; box-sizing: border-box;&quot;&gt;&lt;section style=&quot;text-align: center;&quot;&gt;&lt;section style=&quot;margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;&quot;&gt;&lt;section style=&quot;margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);&quot;&gt;饺子皮春饼&lt;/span&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808172366.jpg/NjAwX2MxXzQwMA&quot;/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;by 玫瑰姐_2Eqx&lt;/strong&gt;&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;配料:&lt;/strong&gt;&lt;br/&gt;饺子皮适量、油少许 &lt;br/&gt;&lt;br/&gt;&lt;strong&gt;烹饪步骤:&lt;/strong&gt;&lt;br/&gt;1.每一层饺子皮都刷上油，7~8张摞在一起 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808185266.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;2.擀成饼，能擀多薄擀多薄 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808196539.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;3.上锅蒸，饼变色就熟了 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808211618.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;4.成品 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808222015.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;5.好薄啊 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808233610.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;6.卷上菜菜就可以吃啦 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201704/0508/050808244819.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;菜谱小贴士:&amp;nbsp;&lt;/span&gt;&lt;/strong&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;刷油的时候要均匀 每一个地方都刷到 以免粘连&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;section class=&quot;_135editor&quot; data-tools=&quot;135编辑器&quot; data-id=&quot;88098&quot; style=&quot;border: 0px none; padding: 0px; box-sizing: border-box;&quot;&gt;&lt;section style=&quot;text-align: center;&quot;&gt;&lt;section style=&quot;margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;&quot;&gt;&lt;section style=&quot;margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);&quot;&gt;饺子皮蛋挞&lt;/span&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811531666.jpg/NjAwX2MxXzQwMA&quot;/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;by 紫陌_红尘&lt;/strong&gt;&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;配料:&lt;/strong&gt;&lt;br/&gt;饺子皮8个、蛋黄2个、牛奶80克、动物性淡奶油110克、细砂糖40克、葡萄干若干、注意蛋挞液是9个蛋挞量 &lt;br/&gt;&lt;br/&gt;&lt;strong&gt;烹饪步骤:&lt;/strong&gt;&lt;br/&gt;1.这是包饺子剩下的饺子皮 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811538302.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;2.将饺子皮放入蛋挞模中捏出形状&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811543511.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;3.将细砂糖放入热牛奶中&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/05181155528.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;4.搅拌均匀&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811556234.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;5.加入淡奶油&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811562672.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;6.再加入蛋黄&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811568069.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;7.搅拌均匀 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811573497.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;8.过筛两遍成细腻蛋挞液 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/05181158462.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;9.葡萄干放入饺子蛋挞皮中 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811584690.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;10.倒入七分满蛋挞液&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811589791.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;11.放入烤箱中层，上下火200度烤二十分钟&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051811596083.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;12.饺子皮蛋挞就烤好啦~来一个吧~(๑•͈ᴗ•͈)❀送蛋挞给你~ &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201612/0518/051812001276.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;菜谱小贴士:&lt;/span&gt;&lt;/strong&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&amp;nbsp;亲们还是去买蛋挞皮吧╮(‵▽′)╭&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;section class=&quot;_135editor&quot; data-tools=&quot;135编辑器&quot; data-id=&quot;88098&quot; style=&quot;border: 0px none; padding: 0px; box-sizing: border-box;&quot;&gt;&lt;section style=&quot;text-align: center;&quot;&gt;&lt;section style=&quot;margin-top: 10px; margin-bottom: 10px; display: inline-block; border-radius: 50px; color: rgb(255, 255, 255); border: 1px solid rgb(117, 117, 118); box-sizing: border-box;&quot;&gt;&lt;section style=&quot;margin: -1px 4px; display: inline-block; border-radius: 50px; border: 1px solid rgb(117, 117, 118); padding: 5px 15px; box-sizing: border-box;&quot;&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif; color: rgb(0, 0, 0);&quot;&gt;饺子皮葱油饼&lt;/span&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;/section&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301346596874.jpg/NjAwX2MxXzQwMA&quot;/&gt;&lt;/p&gt;&lt;h3 style=&quot;font-size: 16px; line-height: 1.6em;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;by 粗茶淡饭总相宜&lt;/strong&gt;&lt;/span&gt;&lt;/h3&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;strong&gt;配料:&lt;/strong&gt;&lt;br/&gt;饺子皮20张、葱花适量、白芝麻适量、十三香适量、花生油适量、盐适量 &lt;br/&gt;&lt;br/&gt;&lt;strong&gt;烹饪步骤:&lt;/strong&gt;&lt;br/&gt;1.准备好材料&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347003855.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;2.先把葱花用盐、十三香和白芝麻拌匀 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/30134701177.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;3.5张饺子皮1组摊开 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347016266.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;4.把4张饺子皮放上拌好的葱花，剩下1张不用放&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347021660.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;5.先2张叠放再1层1层码好 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347029730.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;6.没放葱花的放在最上面盖住，用手压一下使饺子皮贴一些&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347036441.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;7.接着用擀面杖转圈地压&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/30134704846.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;8.擀得薄一些&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347046794.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;9.锅烧热倒入油继续加热后再转小火&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347053048.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;10.放入饺子皮煎制 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347059252.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;11.煎至金黄色即可 &lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347067330.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;12.再翻面煎另一面至金黄色，就做好了&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347073507.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;13.香喷喷地，脆脆地，我共做了4组，一起叠放，开吃了&lt;br/&gt;&lt;br/&gt;&lt;/span&gt;&lt;img src=&quot;http://s1.cdn.xiangha.com/caipu/201703/3013/301347079994.jpg/NjAwX2MxXzQwMA&quot; style=&quot;width:500px;&quot;/&gt;&lt;br/&gt;&lt;br/&gt;&lt;strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;菜谱小贴士:&lt;/span&gt;&lt;/strong&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;span style=&quot;font-family: arial, helvetica, sans-serif;&quot;&gt;煎时要先把油烧热再转小火，慢煎&lt;/span&gt;&lt;/p&gt;&lt;p style=&quot;white-space: normal;&quot;&gt;&lt;br/&gt;&lt;/p&gt;&lt;/section&gt;&lt;/section&gt;&lt;p&gt;&lt;br/&gt;&lt;/p&gt;';  window.appCommon.initShare(share_title,share_con,share_img,share_url,mark,'');  window.appCommon.initFav(code,isFav,'http://api.xiangha.com//main3/caipu/setDishInfo','type=nous&code='+code,'a_collection','香哈头条');  window.appCommon.setNouseHistory(code,share_title,content,share_img,allClick); }); </script><div id=\"tongji\" style=\"display:none\"><script src=\"https://s11.cnzz.com/z_stat.php?id=1253999706&web_id=1253999706\" language=\"JavaScript\"></script><script type=\"text/javascript\" src=\"//tajs.qq.com/stats?sId=53763964\" charset=\"UTF-8\"></script><script> (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){ (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o), m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m) })(window,document,'script','//www.google-analytics.com/analytics.js','ga'); ga('create', 'UA-35039954-1', 'auto'); ga('send', 'pageview'); </script></div></body></html>";
    /**
     * 解析文章数据
     *
     * @param mapArticle
     */
    private void analysArticleData(@NonNull final Map<String, String> mapArticle) {
        //TODO webview
        WebviewManager manager = new WebviewManager(this,loadManager,true);
        WebView webView = manager.createWebView(0);
        linearLayoutTwo.addView(webView);
        linearLayoutTwo.setVisibility(View.VISIBLE);
        webView.loadDataWithBaseURL("http://www.xiangha.com/zhishi/239788.html", htmlData, "text/html", "utf-8", null);

//        if (mapArticle.isEmpty()) return;
//        findViewById(R.id.rightImgBtn2).setVisibility(View.VISIBLE);
//        ArticleHeaderView headerView = new ArticleHeaderView(ArticleDetailActivity.this);
//        headerView.setData(mapArticle);
//        linearLayoutOne.addView(headerView);
//        linearLayoutOne.setVisibility(View.VISIBLE);
//        detailAdapter.notifyDataSetChanged();
//        listview.setVisibility(View.VISIBLE);
//        commentNum = mapArticle.get("commentNumber");
//        String content = mapArticle.get("content");
//        analysArticleContent(content);
//        //转自和时间数据
//        Map<String,String> map = new HashMap<>();
//        map.put("datatype",String.valueOf(Type_articleinfo));
//        map.put("repAddress",mapArticle.get("repAddress"));
//        map.put("allClick",mapArticle.get("allClick"));
//        map.put("addTime",mapArticle.get("addTime"));
//        allDataListMap.add(map);
//
//        detailAdapter.notifyDataSetChanged();
//        mArticleCommentBar.setPraiseAPI(getPraiseAPI());
//        mArticleCommentBar.setData(mapArticle);
//
//        final Map<String, String> customerData = StringManager.getFirstMap(mapArticle.get("customer"));
//        final String userCode = customerData.get("code");
//        final boolean isAuthor = LoginManager.isLogin()
//                && !TextUtils.isEmpty(LoginManager.userInfo.get("code"))
//                && !TextUtils.isEmpty(userCode)
//                && userCode.equals(LoginManager.userInfo.get("code"));
//        rightButton.setImageResource(isAuthor ? R.drawable.i_ad_more : R.drawable.z_z_topbar_ico_share);
//        rightButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isAuthor)
//                    showBottomDialog();
//                else
//                    openShare();
//            }
//        });
//        detailAdapter.setOnReportClickCallback(new ArticleContentBottomView.OnReportClickCallback() {
//            @Override
//            public void onReportClick() {
//                Intent intent = new Intent(ArticleDetailActivity.this,ReportActivity.class);
//                intent.putExtra("type",getType());
//                intent.putExtra("code",code);
//                intent.putExtra("usercode",userCode);
//                intent.putExtra("reportName",customerData.get("nickName"));
//                intent.putExtra("reportContent",mapArticle.get("title"));
//                intent.putExtra("reportType","1");
//                startActivity(intent);
//            }
//        });
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
                allDataListMap.add(map);
            } else if ("image".equals(type) || "gif".equals(type)) {//图片
                String imageUrl = map.get("gif".equals(type) ? "gifurl" : "imageurl");
                map.put("datatype", String.valueOf("gif".equals(type) ? Type_gif : Type_image));
                map.put("imageUrl", imageUrl);
                allDataListMap.add(map);
            } else if ("video".equals(type)) {//视频
                Map<String, String> videoMap = StringManager.getFirstMap(map.get("video"));
                String videoUrl = videoMap.get("url");
                String videoImageUrl = videoMap.get("videoImg");
                map.put("datatype", String.valueOf(Type_video));
                map.put("videoUrl", videoUrl);
                map.put("videoImageUrl", videoImageUrl);
                allDataListMap.add(map);
            } else if ("xiangha".equals(type)) {//自定义演示。ds，电商，caipu，菜谱
                String json = map.get("json");
                if (!TextUtils.isEmpty(json)) {
                    final Map<String, String> jsonMap = StringManager.getFirstMap(json);
                    if (jsonMap.containsKey("type") && !TextUtils.isEmpty(jsonMap.get("type"))) {
                        String datatype = jsonMap.get("type");
                        if ("ds".equals(datatype)) {
                            jsonMap.put("datatype", String.valueOf(Type_ds));
                            allDataListMap.add(jsonMap);
                        } else if ("caipu".equals(datatype)) {
                            jsonMap.put("datatype", String.valueOf(Type_caipu));
                            allDataListMap.add(jsonMap);
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
                    allDataListMap.add(map);
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
        allDataListMap.addAll(ArrayRelate);

        detailAdapter.notifyDataSetChanged();
    }

    private void showBottomDialog() {
        BottomDialog dialog = new BottomDialog(this);
        dialog.addButton("分享", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShare();
            }
        }).addButton("编辑", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArticleDetailActivity.this, getIntentClass());
                intent.putExtra("code", code);
                startActivity(intent);
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
                    }
                }).show();
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
        if (parentView == null || position < 0 || position >= allDataListMap.size())
            return;
        if (allDataListMap.get(position).containsKey("video") && !TextUtils.isEmpty(allDataListMap.get(position).get("video"))) {
            Map<String, String> videoData = StringManager.getFirstMap(allDataListMap.get(position).get("video"));
            if (mVideoImageView == null)
                mVideoImageView = new VideoImageView(this, false);
            mVideoImageView.setImageBg(allDataListMap.get(position).get("img"));
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

    public Class<?> getIntentClass() {
        return ArticleEidtActiivty.class;
    }

    //TODO 添加统计
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
