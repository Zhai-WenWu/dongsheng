package aplug.web;

import android.os.Bundle;
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
    protected String url = "", htmlData = "";
    private long startTime=0;
    private String data_type="";
    private String code="";//code--首页使用功能
    private String module_type="";
    private String userCode = "";

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

    }

    private void initUI() {
        initTitleView();
        initWeb();
    }

    private void initExtras(){
        Bundle bundle = this.getIntent().getExtras();
        // 正常调用
        if (bundle != null) {
            url = bundle.getString("requestmethod");
            startTime=System.currentTimeMillis();
            data_type = bundle.getString("data_type");
            code= bundle.getString("code");
            module_type= bundle.getString("module_type");
            JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";
        }
    }
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
    private void initWeb(){
        templateWebView= (TemplateWebView) findViewById(R.id.TemplateWebView);
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
