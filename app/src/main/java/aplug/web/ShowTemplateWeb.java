package aplug.web;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import acore.override.activity.base.WebActivity;
import acore.widget.CommentBar;
import aplug.web.tools.JSAction;
import aplug.web.view.TemplateWebView;

/**
 * 展示模版url
 */
public class ShowTemplateWeb extends WebActivity{
    public static final String ORIGINDATA ="originData";//替换原始数据的集合key
    public static final String NOWDATA ="nowData";//替换后数据的集合的key
    public static final String REQUESTMETHOD ="requestmethod";//替换后数据的集合的key
    protected String requestmethod = "";
    private String[] originData, nowData;

    protected Button rightBtn;
    protected ImageView favoriteNousImageView;
    protected RelativeLayout shareLayout;
    protected RelativeLayout favLayout,homeLayout;
    protected TextView favoriteNousTextView,title;
    private RelativeLayout shopping_layout;
    private TextView mall_news_num,mall_news_num_two;
    private TemplateWebView templateWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtras();
        initActivity("", 3, 0, R.layout.c_view_bar_nouse_title, R.layout.xh_template_webview);
        initUI();
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    /**
     * 获取外部的参数
     */
    private void initExtras(){
        Bundle bundle = this.getIntent().getExtras();
        // 正常调用
        if (bundle != null) {
            requestmethod = bundle.getString(REQUESTMETHOD);
            originData=bundle.getStringArray(ORIGINDATA);
            nowData=bundle.getStringArray(NOWDATA);
            JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";
        }
    }
    /**
     * 初始化ui
     */
    private void initUI() {
        initTitleView();
        initWeb();
    }
    /**
     * 初始化标题
     */
    protected void initTitleView() {
        title = (TextView) findViewById(R.id.title);
        rightBtn = (Button) findViewById(R.id.rightBtn1);
        shareLayout = (RelativeLayout) findViewById(R.id.shar_layout);
        favLayout = (RelativeLayout) findViewById(R.id.fav_layout);
        homeLayout = (RelativeLayout) findViewById(R.id.home_layout);
//		收藏按钮图片
        favoriteNousImageView = (ImageView) findViewById(R.id.img_fav);
        favoriteNousTextView = (TextView) findViewById(R.id.tv_fav);
        shopping_layout = (RelativeLayout) findViewById(R.id.shopping_layout);
        mall_news_num = (TextView) findViewById(R.id.mall_news_num);
        mall_news_num_two = (TextView) findViewById(R.id.mall_news_num_two);
    }

    /**
     * web初始化
     */
    private void initWeb(){
        templateWebView= (TemplateWebView) findViewById(R.id.TemplateWebView);
        templateWebView.initBaseData(this,loadManager);
        templateWebView.setWebViewCallBack(new TemplateWebView.OnWebviewStateCallBack() {
            @Override
            public void onLoadFinish() {
            }
            @Override
            public void onLoadStart() {
            }
        });
    }

    @Override
    public void loadData() {
        templateWebView.loadData(requestmethod,originData,nowData);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
