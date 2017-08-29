package third.mall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.tools.PageStatisticsUtils;
import amodule.main.Main;
import aplug.web.ShowTemplateWeb;
import aplug.web.tools.XHTemplateManager;
import aplug.web.view.XHWebView;

import static third.mall.override.MallBaseActivity.PAGE_FROM;
import static third.mall.override.MallBaseActivity.PAGE_FROM_TWO;

/**
 * PackageName : third.mall.activity
 * Created by MrTrying on 2017/8/12 10:01.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionSuccessActivity extends ShowTemplateWeb {
    public static final String EXTRAS_POSITION = "position";
    public static final String EXTRAS_ID = "id";

    int id = -1;
    int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent  = getIntent();
        intent.putExtra(EvalutionSuccessActivity.REQUEST_METHOD, XHTemplateManager.DSSUCCESSCOMMENT);
        setIntent(intent);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        title.setText("评价成功");
        ImageView close = (ImageView) findViewById(R.id.leftClose);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(EvalutionSuccessActivity.this,"a_publish_comachi","点击X","");
                EvalutionSuccessActivity.this.finish();
            }
        });
        close.setVisibility(View.VISIBLE);
        findViewById(R.id.leftImgBtn).setVisibility(View.GONE);
    }

    @Override
    protected void initExtras() {
        super.initExtras();
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
            id = bundle.getInt(EXTRAS_ID,id);
            position = bundle.getInt(EXTRAS_POSITION,position);
        }
    }

    @Override
    protected void initWeb() {
        super.initWeb();
        templateWebView.setOnWebNumChangeCallback(new XHWebView.OnWebNumChangeCallback() {
            @Override
            public void onChange(int num) {
                findViewById(R.id.leftImgBtn).setVisibility(num > 1 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void finish() {
        Main.colse_level = 6;
        if(id != -1 && position != -1){
            setResult(OrderStateActivity.result_comment_success, new Intent());
        }
        super.finish();
    }
}
