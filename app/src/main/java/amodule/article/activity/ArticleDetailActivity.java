package amodule.article.activity;

import android.os.Bundle;
import android.text.TextUtils;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

/**
 * 文章详情
 */
public class ArticleDetailActivity extends BaseActivity {
    private boolean initUiSuccess=false;//ui初始化完成
    private String code="";//请求数据的code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle= this.getIntent().getExtras();
        if(bundle!=null){
            code= bundle.getString("code");
        }
        init();
    }

    /**初始化**/
    private void init(){
        initView();
    }
    /**View部分初始化**/
    private void initView(){

    }
    /**数据初始化**/
    private void initData(){
        if(TextUtils.isEmpty(code)){
            Tools.showToast(this,"当前数据错误，请重新请求");
            return;
        }

    }
    /**请求网络**/
    private void requestData(){
        String url= StringManager.api_getArticleInfo;
        String params= TextUtils.isEmpty(code)?"":"code="+code;
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if(flag>= ReqInternet.REQ_OK_STRING){

                }else{
                    toastFaildRes(flag,true,object);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus&&!initUiSuccess){
            initUiSuccess=true;
        }
    }

}
