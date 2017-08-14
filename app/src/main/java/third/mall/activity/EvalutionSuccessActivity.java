package third.mall.activity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.xianghatest.R;

import acore.logic.XHClick;
import amodule.main.Main;
import aplug.web.ShowWeb;
import aplug.web.view.XHWebView;

/**
 * PackageName : third.mall.activity
 * Created by MrTrying on 2017/8/12 10:01.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionSuccessActivity extends ShowWeb {

    @Override
    protected void initTitleView() {
        super.initTitleView();
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
    protected void initWeb() {
        super.initWeb();
        webview.setOnWebNumChangeCallback(new XHWebView.OnWebNumChangeCallback() {
            @Override
            public void onChange(int num) {
                Log.i("tzy","num = " + num);
                Log.i("tzy","webview = " + webview.getWebViewNum());
                findViewById(R.id.leftImgBtn).setVisibility(num > 1 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void finish() {
        Main.colse_level = 6;
        super.finish();
    }
}
