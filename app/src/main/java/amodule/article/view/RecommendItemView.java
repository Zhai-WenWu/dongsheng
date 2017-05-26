package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import java.util.Map;

import acore.override.view.ItemBaseView;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/25 14:38.
 * E_mail : ztanzeyu@gmail.com
 */

public class RecommendItemView extends ItemBaseView {

    private ImageView recImage;
    private TextView recTitle;
    private TextView recCustomerName;
    private TextView recBrowse;
    private TextView recComment;

    public RecommendItemView(Context context) {
        super(context, R.layout.a_article_recommend_item);
    }

    public RecommendItemView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.a_article_recommend_item);
    }

    public RecommendItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.a_article_recommend_item);
    }

    @Override
    public void init() {
        recImage = (ImageView) findViewById(R.id.rec_image);
        recTitle = (TextView) findViewById(R.id.rec_title);
        recCustomerName = (TextView) findViewById(R.id.rec_customer_name);
        recBrowse = (TextView) findViewById(R.id.rec_browse);
        recComment = (TextView) findViewById(R.id.rec_comment);
    }

    public void setData(Map<String,String> map){

    }
}
