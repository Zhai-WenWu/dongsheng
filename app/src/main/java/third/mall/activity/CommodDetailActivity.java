package third.mall.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.SubBitmapTarget;
import aplug.basic.LoadImage;
import aplug.feedback.activity.Feedback;
import aplug.imageselector.ShowImageActivity;
import aplug.shortvideo.activity.VideoFullScreenActivity;
import aplug.web.view.TemplateWebView;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallAddShopping;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.dialog.BuyDialog;
import third.mall.dialog.FavorableDialog;
import third.mall.tool.ToolView;
import third.mall.widget.MyScrollView;
import third.mall.widget.MyScrollView.ScrollViewInterface;
import third.mall.widget.ScrollViewContainer;
import third.mall.widget.ScrollViewContainer.ScrollviewContaninerInter;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 商品详情页
 *
 * @author yu
 */
public class CommodDetailActivity extends BaseActivity implements OnClickListener {

    private ViewPager viewpager;
    private ImageView[] imageviews;
    private WebView explain_detail_webview;
    private Map<String, String> map;
    private String code;
    private MyScrollView mall_commod_scroll;
    private Handler handler;
    private static final int SHOW_OK = 1;
    private ArrayList<Map<String, String>> images;
    private ScrollViewContainer mall_ScrollViewContainer;
    private boolean load_state = true;
    private ImageView commod_add;
    private TextView mall_news_num;
    private TextView commod_shop,commod_buy;
    private MallCommon common;
    private Rect scrollBounds;
    private RelativeLayout share_layout;
    private RelativeLayout back;
    private ImageView leftImgBtn;
    private ImageView img_share;
    private TextView mall_news_num_two;
    private Map<String, String> map_statistic = new HashMap<String, String>();
    private String actionUrl;
    private String mall_stat_statistic;

    private String data_type = "";//推荐列表过来的数据
    private String module_type = "";
    private Long startTime;//统计使用的时间

    private TextView title, title_detail;
    private String titleState = "1";//目前状态，1：标题，2：详情
    private FavorableDialog favorableDialog;
    private int productNum = 1;//购买商品数量
    private BuyDialog buyDialog;
    private TemplateWebView middle_templateWebView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            code = bundle.getString("product_code");
            data_type = bundle.getString("data_type");
            module_type = bundle.getString("module_type");
            for (int i = 1; i < 100; i++) {
                if (!TextUtils.isEmpty(bundle.getString("fr" + i))) {
                    map_statistic.put("fr" + i, bundle.getString("fr" + i));
                    if (!TextUtils.isEmpty(bundle.getString("fr" + i + "_msg"))) {
                        map_statistic.put("fr" + i + "_msg", bundle.getString("fr" + i + "_msg"));
                    }
                } else {
                    break;
                }
            }
            if (!TextUtils.isEmpty(bundle.getString("xhcode"))) {
                map_statistic.put("xhcode", bundle.getString("xhcode"));
            }
        }
        initActivity("商品详情", 3, 0, 0, R.layout.a_mall_commod_detail);
        common = new MallCommon(this);
        initView();
        initData();
        XHClick.track(this, "浏览商品");
        startTime = System.currentTimeMillis();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        initTopView();
        initWeb();
        findViewById(R.id.linear_buy).setVisibility(View.GONE);
        findViewById(R.id.commod_shop_linear).setOnClickListener(this);
        findViewById(R.id.service_mercat).setOnClickListener(this);
        commod_buy= (TextView) findViewById(R.id.commod_buy);
        commod_buy.setOnClickListener(this);

        mall_commod_scroll = (MyScrollView) findViewById(R.id.mall_commod_scroll);
        commod_shop = (TextView) findViewById(R.id.commod_shop);
        commod_shop.setOnClickListener(this);
        commod_add = (ImageView) findViewById(R.id.commod_add);
        mall_news_num = (TextView) findViewById(R.id.mall_news_num);
        mall_news_num_two = (TextView) findViewById(R.id.mall_news_num_two);
        setShopcatNum();
        scrollBounds = new Rect();
        mall_commod_scroll.getHitRect(scrollBounds);
        mall_commod_scroll.setInterfaceSv(new ScrollViewInterface() {

            @Override
            public void setYandState(float y, boolean state) {
                Log.i("zyj", "setInterfaceSv::" + state);
            }
        });
        mall_ScrollViewContainer = (ScrollViewContainer) findViewById(R.id.mall_ScrollViewContainer);
        mall_ScrollViewContainer.setInterface(new ScrollviewContaninerInter() {
            @Override
            public void setState(int state) {
                titleState = state == 1 ? "1" : "2";
                handleTitleState();
                if (load_state) {
                    explain_detail_webview.loadUrl(MallStringManager.replaceUrl(MallStringManager.mall_web_product_detail) + "?product_code=" + code);
                    XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "上拉查看详细介绍", "");
                }
            }
            @Override
            public void changeTitleState(boolean state) {
            }
        });
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        RelativeLayout viewpager_layout = (RelativeLayout) findViewById(R.id.viewpager_layout);
        viewpager_layout.setLayoutParams(new RelativeLayout.LayoutParams(width, width));
    }

    /**
     * 初始化标题
     */
    private void initTopView() {
        back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(this);
        leftImgBtn = (ImageView) findViewById(R.id.leftImgBtn);
        img_share = (ImageView) findViewById(R.id.img_share);
        share_layout = (RelativeLayout) findViewById(R.id.share_layout);
        share_layout.setOnClickListener(this);
        findViewById(R.id.share_layout).setVisibility(View.GONE);
        back.setBackgroundColor(Color.parseColor("#00ffffff"));
        share_layout.setBackgroundColor(Color.parseColor("#00ffffff"));
        //标题
        title = (TextView) findViewById(R.id.title);
        title_detail = (TextView) findViewById(R.id.title_detail);
        handleTitleState();
        title.setOnClickListener(this);
        title_detail.setOnClickListener(this);
    }

    /**
     * 初始化web
     */
    private void initWeb(){
        //中间模版数据
        middle_templateWebView= (TemplateWebView) findViewById(R.id.middle_templateWebView);
        middle_templateWebView.initBaseData(this,loadManager);
        middle_templateWebView.setWebViewCallBack(new TemplateWebView.OnWebviewStateCallBack() {
            @Override
            public void onLoadFinish() {
                int height=middle_templateWebView.getMeasuredHeight();
                if(mall_ScrollViewContainer!=null)mall_ScrollViewContainer.setOneViewHeight(height);
            }
            @Override
            public void onLoadStart() {
            }
        });
        //底部webview
        explain_detail_webview = (WebView) findViewById(R.id.explain_detail_webview);
        explain_detail_webview.getSettings().setJavaScriptEnabled(true);// 执行角标文件
        explain_detail_webview.setScrollBarStyle(0);// 滚动条风格，为0就是不给滚动条留空间，滚动条覆盖在网页上
        explain_detail_webview.setHorizontalScrollBarEnabled(false);
        explain_detail_webview.setVerticalScrollBarEnabled(true);
        explain_detail_webview.getSettings().setDefaultTextEncodingName("UTF-8");
        //兼容https,在部分版本上资源显示不全的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            explain_detail_webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    /**
     * 处理标题状态颜色
     */
    private void handleTitleState() {
        switch (titleState) {
            case "1":
                title.setTextColor(Color.parseColor("#fffffe"));
                title_detail.setTextColor(Color.parseColor("#999999"));
                break;
            case "2":
                title.setTextColor(Color.parseColor("#999999"));
                title_detail.setTextColor(Color.parseColor("#fffffe"));
                break;
        }
    }

    /**
     * 展示购物车数量
     */
    private void setShopcatNum() {
        if (MallCommon.num_shopcat > 0) {
            if (MallCommon.num_shopcat > 9) {
                mall_news_num.setVisibility(View.GONE);
                mall_news_num_two.setVisibility(View.VISIBLE);
                if (MallCommon.num_shopcat > 99)
                    mall_news_num_two.setText("99+");
                else
                    mall_news_num_two.setText("" + MallCommon.num_shopcat);
            } else {
                mall_news_num.setVisibility(View.VISIBLE);
                mall_news_num_two.setVisibility(View.GONE);
                mall_news_num.setText("" + MallCommon.num_shopcat);
            }
        } else {
            mall_news_num.setVisibility(View.GONE);
            mall_news_num_two.setVisibility(View.GONE);
        }
    }

    /**
     * 第一次初始化数据
     */
    @SuppressLint("HandlerLeak")
    private void initData() {

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW_OK:
                        loadManager.hideProgressBar();
                        mall_commod_scroll.setVisibility(View.VISIBLE);
                        findViewById(R.id.share_layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.linear_buy).setVisibility(View.VISIBLE);
                        break;
                }
                super.handleMessage(msg);
            }
        };
        // webview 加载监听
        explain_detail_webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                load_state = true;
                findViewById(R.id.widget_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.explain_detail_but_linear).setVisibility(View.GONE);
//                findViewById(R.id.explain_detail_linear).setVisibility(View.GONE);
                findViewById(R.id.explain_detail_webview).setVisibility(View.GONE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                load_state = false;
                findViewById(R.id.widget_progress).setVisibility(View.GONE);
                findViewById(R.id.explain_detail_but_linear).setVisibility(View.VISIBLE);
//                findViewById(R.id.explain_detail_linear).setVisibility(View.VISIBLE);
                findViewById(R.id.explain_detail_webview).setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }
        });
        loadManager.setLoading(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setRequest();
            }
        });
    }

    /**
     * 请求网络数据
     */
    private void setRequest() {
        actionUrl = MallStringManager.mall_api_product_info_v3 + "?product_code=" + code;
        for (String key : map_statistic.keySet()) {
            actionUrl += "&" + key + "=" + map_statistic.get(key);
        }
        MallReqInternet.in().doGet(actionUrl, new MallInternetCallback(this) {

            @Override
            public void loadstat(int flag, String url, Object msg, Object... stat) {
                loadManager.loadOver(flag, 1, true);
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
                    parseInfo(listMapByJson);
                    if (stat != null && stat.length > 0 && !TextUtils.isEmpty((String) stat[0])) {
                        mall_stat_statistic = (String) stat[0];
                    }
                    handler.sendEmptyMessage(SHOW_OK);
                } else {

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MallCommon.num_shopcat > 0) {
            mall_news_num.setVisibility(View.VISIBLE);
            if (MallCommon.num_shopcat > 99)
                mall_news_num.setText("99+");
            else
                mall_news_num.setText("" + MallCommon.num_shopcat);
        } else {
            mall_news_num.setVisibility(View.GONE);
        }
    }

    /**
     * 解析数据
     *
     * @param listMapByJson
     */
    private void parseInfo(ArrayList<Map<String, String>> listMapByJson) {
        map = listMapByJson.get(0);
        //设置商品状态
        setStatus();
        // 设置商品标题
        setProductTitle();
        // 设置价格
        setProductPrice();
        //设置领券
        setGetFavorable();

        // 轮转图
        images = UtilString.getListMapByJson(map.get("resource"));
        initViewPager(images);
//        if (map.containsKey("product_introduce_flag") && "2".equals(map.get("product_introduce_flag"))) {
        mall_ScrollViewContainer.setState_two(false);
        middle_templateWebView.loadData("XhDish",new String[]{"<{code}>"},new String[]{"94888485"});

    }

    /**
     * 设置获取优惠券
     */
    private void setGetFavorable() {
        RelativeLayout view_getfavorable = (RelativeLayout) findViewById(R.id.view_getfavorable);
        if (map.containsKey("coupon_desc") && !TextUtils.isEmpty(map.get("coupon_desc")) && !"[]".equals(map.get("coupon_desc"))) {
            view_getfavorable.setVisibility(View.VISIBLE);
            TextView favor_title = (TextView) findViewById(R.id.favor_title);
            favor_title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LoginManager.isLogin()) {
                        XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "领券", "");
                        if (favorableDialog == null) {
                            favorableDialog = new FavorableDialog(CommodDetailActivity.this, map.get("shop_code"));
                            favorableDialog.setCallBack(new FavorableDialog.showCallBack() {
                                @Override
                                public void setShow() {
                                    favorableDialog.show();
                                }
                            });
                        } else {
                            favorableDialog.show();
                        }
                    } else {
                        Intent intent_user = new Intent(CommodDetailActivity.this, LoginByAccout.class);
                        CommodDetailActivity.this.startActivity(intent_user);
                    }
                }
            });

            favor_title.setText(map.get("coupon_desc"));
        } else view_getfavorable.setVisibility(View.GONE);
    }

    /**
     * 设置当前商品状态
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setStatus() {
        // 销售
        if ("0".equals(map.get("saleable_num")) || !"2".equals(map.get("status"))) {
            TextView product_lose_tv = (TextView) findViewById(R.id.product_lose_tv);
            if (!"2".equals(map.get("status")))
                product_lose_tv.setText("已下架");
            else if ("0".equals(map.get("saleable_num")))
                product_lose_tv.setText("暂时无货，非常抱歉!");

            commod_shop.setEnabled(false);
            commod_buy.setEnabled(false);

            commod_shop.setTextColor(Color.parseColor("#70ffffff"));
            commod_shop.setBackgroundColor(Color.parseColor("#999999"));
            commod_buy.setTextColor(Color.parseColor("#70ffffff"));
            commod_buy.setBackgroundColor(Color.parseColor("#999999"));
            findViewById(R.id.product_lose_rela).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.product_lose_rela).setVisibility(View.GONE);
            commod_shop.setEnabled(true);
            commod_buy.setEnabled(true);

            commod_shop.setTextColor(Color.parseColor("#ffffff"));
            commod_shop.setBackgroundColor(Color.parseColor("#febf14"));
            commod_buy.setTextColor(Color.parseColor("#ffffff"));
            commod_buy.setBackgroundColor(Color.parseColor("#f23030"));
        }
    }


    /**
     * 设置标题内容
     */
    private void setProductTitle() {
        // 标题内容
        TextView title_commod_content = (TextView) findViewById(R.id.title_commod_content);
        title_commod_content.setText(map.get("title"));
        // 标题描述
        TextView title_commod_content_sub = (TextView) findViewById(R.id.title_commod_content_sub);
        if (map.containsKey("sub_title") && !TextUtils.isEmpty(map.get("sub_title"))) {
            title_commod_content_sub.setVisibility(View.VISIBLE);
            title_commod_content_sub.setText(map.get("sub_title"));
        } else {
            title_commod_content_sub.setVisibility(View.GONE);
        }
    }

    /**
     * 设置价格
     */
    private void setProductPrice() {
        // 促销价
        TextView title_commod_price_now = (TextView) findViewById(R.id.title_commod_price_now);
        title_commod_price_now.setText("¥" + map.get("discount_price"));
        // 正常价格
        TextView title_commod_price_before = (TextView) findViewById(R.id.title_commod_price_before);
        title_commod_price_before.setText("¥" + map.get("price"));
        title_commod_price_before.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        if (map.get("price").equals(map.get("discount_price"))) {
            title_commod_price_before.setVisibility(View.GONE);
        } else {
            title_commod_price_before.setVisibility(View.VISIBLE);
        }
        // 邮费
        TextView title_commod_price_postage = (TextView) findViewById(R.id.title_commod_price_postage);
        title_commod_price_postage.setText(map.get("product_postage_desc"));
    }

    /**
     * 初始化viewpager
     *
     * @param images
     */
    private void initViewPager(ArrayList<Map<String, String>> images) {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        LinearLayout point_linear = (LinearLayout) findViewById(R.id.point_linear);
        List<View> views = new ArrayList<View>();
        for (int i = 0; i < images.size(); i++) {
            View topView = LayoutInflater.from(this).inflate(R.layout.v_product_top_view, null);
            if (images.get(i).containsKey("type") && "1".equals(images.get(i).get("type"))) {
                topView.findViewById(R.id.image_video).setVisibility(View.VISIBLE);
            } else topView.findViewById(R.id.image_video).setVisibility(View.GONE);
            ImageView iv = (ImageView) topView.findViewById(R.id.image);
            iv.setScaleType(ScaleType.FIT_XY);
            setImageView(iv, images.get(i).get("img"), false);
            views.add(topView);
        }
        imageviews = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            ImageView iv = new ImageView(this);
            int dp_2_5 = Tools.getDimen(this, R.dimen.dp_2_5);
            iv.setPadding(dp_2_5, 0, dp_2_5, 0);
            imageviews[i] = iv;
            imageviews[i].setImageResource(R.drawable.z_home_banner_bg_pic_white);
            if (i == 0) {
                imageviews[i].setImageResource(R.drawable.z_home_banner_bg_pic_active);
            }
            if (imageviews.length > 1) {
                point_linear.setVisibility(View.VISIBLE);
            }
            point_linear.addView(imageviews[i]);
        }
        viewpager.setAdapter(new MyViewPagerAdapter(views));
        viewpager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                for (int i = 0; i < imageviews.length; i++) {
                    imageviews[arg0].setImageResource(R.drawable.z_home_banner_bg_pic_active);
                    if (arg0 != i) {
                        imageviews[i].setImageResource(R.drawable.z_home_banner_bg_pic_white);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    /**
     * 加载图片
     *
     * @param iv
     * @param imageUrl
     * @param state    true 圆形，false正常
     */
    private void setImageView(final ImageView iv, String imageUrl, final boolean state) {
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
                .load(imageUrl)
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    if (state)
                        iv.setImageBitmap(ToolView.toRoundBitmap(bitmap));
                    else
                        iv.setImageBitmap(bitmap);
                }
            });
    }

    /**
     * viewpager adpater
     *
     * @author yu
     */
    private class MyViewPagerAdapter extends PagerAdapter {

        private List<View> list_iv;

        public MyViewPagerAdapter(List<View> list_iv) {
            super();
            this.list_iv = list_iv;
        }

        @Override
        public int getCount() {
            return list_iv.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(View container, final int position) {
            ((ViewPager) container).addView(list_iv.get(position), 0);
            View view = list_iv.get(position);

            // 点击
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    if (images.get(position).containsKey("type") && "1".equals(images.get(position).get("type"))) {
                        intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_URL, StringManager.getFirstMap(images.get(position).get("video")).get("default_url"));
                        intent.setClass(CommodDetailActivity.this, VideoFullScreenActivity.class);
                    } else {
                        intent.putExtra("url", images.get(position).get("img"));
                        intent.setClass(CommodDetailActivity.this, ShowImageActivity.class);

                    }
                    startActivity(intent);
                }
            });
            return list_iv.get(position);
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(list_iv.get(position));
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.service_mercat://客服
                Intent intentmark = new Intent(this, Feedback.class);
                intentmark.putExtra("backData", map.get("m_url"));
                this.startActivity(intentmark);
                break;
            case R.id.commod_buy:
                if (LoginManager.isLogin()) {
                    if (buyDialog == null) {
                        buyDialog = new BuyDialog(this, map);
                        buyDialog.setBuyDialogCallBack(new BuyDialog.BuyDialogCallBack() {
                            @Override
                            public void dialogDismiss(int productNum) {
                                CommodDetailActivity.this.productNum = productNum;
                            }
                        });
                    }
                    buyDialog.initProductNum(productNum);
                    buyDialog.show();
                } else {
                    Intent intent_user = new Intent(this, LoginByAccout.class);
                    startActivity(intent_user);
                    return;
                }

                break;
            case R.id.commod_shop_linear:// 购物车
                XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "底部导航", "购物车");
                if (LoginManager.isLogin()) {
                    MallClickContorl.getInstance().setStatisticUrl(actionUrl, null, mall_stat_statistic, this);
                    this.startActivity(new Intent(this, ShoppingActivity.class));
                } else {
                    Intent intent_user = new Intent(this, LoginByAccout.class);
                    startActivity(intent_user);
                    return;
                }
                break;

            case R.id.back:
                this.finish();
                break;
            case R.id.share_layout:
                XHClick.track(CommodDetailActivity.this, "分享商品");
                XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "分享", "");
                doshare();
                break;
            case R.id.commod_shop:// 加入购物车
                if (LoginManager.isLogin()) {
                    // addShoppingCommod(map.get("product_code"), "1");
                    common.addShoppingcat(this, map.get("product_code"), new InterfaceMallAddShopping() {

                        @Override
                        public void addProduct(int state) {
                            if (state >= 50) {
                                XHClick.mapStat(CommodDetailActivity.this, "a_mail_goods", "底部导航", "加入购物车");
                                setAddAnimation();
                            }
                        }
                    });
                } else {
                    Intent intent_user = new Intent(this, LoginByAccout.class);
                    startActivity(intent_user);
                    return;
                }
                break;
            case R.id.title:
                mall_ScrollViewContainer.setViewIndex("1");
                break;
            case R.id.title_detail:
                mall_ScrollViewContainer.setViewIndex("2");
                break;
        }
    }

    private void doshare() {
        barShare = new BarShare(this, "商品详情页", "");
        String type = BarShare.IMG_TYPE_WEB;
        String title = map.get("product_share_title");
        String clickUrl = map.get("product_share_url");
        String content = map.get("product_share_desc");
        String imgUrl = map.get("buy_img");
        //分享添加路径统计
        Object msg = UtilFile.loadShared(this, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT);
        ArrayList<Map<String, String>> list = UtilString.getListMapByJson(msg);
        String url_temp = MallStringManager.mall_api_product_info.replace(MallStringManager.mall_apiUrl, "");
        if (list != null && list.size() > 0 && list.get(0).containsKey(url_temp)) {
            clickUrl += "&fr1=" + list.get(0).containsKey(url_temp) + "_share&fr1_msg=" + code;
        }
        barShare.setShare(type, title, content, imgUrl, clickUrl);
        barShare.openShare();
    }

    /**
     * 设置文字变化
     *
     * @param view
     * @param size
     */
    private void setTextViewColor(TextView view, int size) {
        SpannableStringBuilder builder = new SpannableStringBuilder(view.getText().toString());
        String color = Tools.getColorStr(this, R.color.comment_color);
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor(color));
        builder.setSpan(redSpan, 0, size, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(builder);
    }

    /**
     * 执行动画
     */
    private void setAddAnimation() {
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0);
        alpha.setDuration(700);
        alpha.setFillAfter(true);
        set.addAnimation(alpha);
        ScaleAnimation scale = new ScaleAnimation(1f, 0, 1f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(700);
        set.addAnimation(scale);
        commod_add.startAnimation(set);

        alpha.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                commod_add.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                commod_add.setVisibility(View.GONE);
                commod_add.clearAnimation();

            }
        });
        MallCommon.num_shopcat++;

        setShopcatNum();
        ScaleAnimation scale_text = new ScaleAnimation(1f, 1.1f, 1f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale_text.setDuration(700);
        if (MallCommon.num_shopcat < 9)
            mall_news_num.startAnimation(scale_text);
        else
            mall_news_num_two.startAnimation(scale_text);
    }

    /**
     * 获取值得买每行的字数
     *
     * @return
     */
    private int setTextViewNum() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_14);
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);
        int img_waith = (int) this.getResources().getDimension(R.dimen.dp_45);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 3 - img_waith;
        int tv_pad = ToolView.dip2px(this, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {//是否ui资源

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        long nowTime = System.currentTimeMillis();
        if (startTime > 0 && (nowTime - startTime) > 0 && !TextUtils.isEmpty(data_type) && !TextUtils.isEmpty(module_type)) {
            XHClick.saveStatictisFile("CommodDetail", module_type, data_type, code, "", "stop", String.valueOf((nowTime - startTime) / 1000), "", "", "", "");
        }
        super.onDestroy();
        mall_ScrollViewContainer = null;
        common = null;
        System.gc();
    }
}
