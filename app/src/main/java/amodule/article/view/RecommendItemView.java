package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.article.activity.ArticleDetailActivity;

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

    public void setData(final Map<String, String> map) {
        findViewById(R.id.hander).setVisibility(map.containsKey("showheader") ? View.VISIBLE : View.GONE);
        setViewImage(recImage, map.get("img"));
        setViewText(recTitle, map, "title", View.INVISIBLE);
        setViewText(recBrowse, map, "clickAll");
        setViewText(recComment, map, "commentNumber");
        if (map.containsKey("customer")) {
            Map<String, String> customer = StringManager.getFirstMap(map.get("customer"));
            setViewText(recCustomerName, customer, "nickName");
            findViewById(R.id.gourmet_icon)
                    .setVisibility(customer.containsKey("isGourmet") && "2".equals(customer.get("isGourmet")) ? View.VISIBLE : View.GONE);
        }
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启文章
                if(map.containsKey("code") && !TextUtils.isEmpty(map.get("code"))){
                    Intent intent = new Intent(getContext(), ArticleDetailActivity.class);
                    intent.putExtra("code",map.get("code"));
                    getContext().startActivity(intent);
                }
            }
        });
    }
}
