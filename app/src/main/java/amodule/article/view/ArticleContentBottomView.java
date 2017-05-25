package amodule.article.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.view.ItemBaseView;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/25 11:56.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleContentBottomView extends ItemBaseView implements View.OnClickListener{

    private RelativeLayout adLayout;
    private TextView articleRepintSource;
    private TextView articlePublishDate;
    private TextView articleBrowse;

    public ArticleContentBottomView(Context context) {
        super(context,R.layout.a_article_content_bottom);
        init();
    }

    public ArticleContentBottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,R.layout.a_article_content_bottom);
        init();
    }

    public ArticleContentBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,R.layout.a_article_content_bottom);
        init();
    }

    public void init(){

        articleRepintSource = (TextView) findViewById(R.id.artilce_repint_source);
        articlePublishDate = (TextView) findViewById(R.id.article_publish_date);
        articleBrowse = (TextView) findViewById(R.id.article_browse);
        adLayout = (RelativeLayout) findViewById(R.id.ad_layout);

        findViewById(R.id.article_report).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //举报
            case R.id.article_report:

                break;
            default:
                break;
        }
    }
}
