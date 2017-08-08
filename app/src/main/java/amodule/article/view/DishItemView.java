package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.Map;

import acore.override.view.ItemBaseView;

/**
 * PackageName : amodule.article.view.richtext
 * Created by MrTrying on 2017/5/25 11:38.
 * E_mail : ztanzeyu@gmail.com
 */

public class DishItemView extends ItemBaseView {

    private ImageView dishImage;
    private TextView dishName;
    private TextView dishBrowse;
    private TextView dishFavorite;
    private TextView dishCustomerName;

    public DishItemView(Context context) {
        super(context, R.layout.a_article_dish_item);
    }

    public DishItemView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.a_article_dish_item);
    }

    public DishItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.a_article_dish_item);
    }

    @Override
    public void init() {
        dishImage = (ImageView) findViewById(R.id.dish_image);
        dishName = (TextView) findViewById(R.id.dish_name);
        dishBrowse = (TextView) findViewById(R.id.dish_browse);
        dishFavorite = (TextView) findViewById(R.id.dish_favorite);
        dishCustomerName = (TextView) findViewById(R.id.dish_customer_name);
    }

    /**
     * 设置数据
     *
     * @param data
     */
    public void setData(Map<String, String> data) {
        setViewImage(dishImage, data.get("img"));
        setViewText(dishName,data,"name");
        setViewText(dishCustomerName,data,"nickName");
        setViewTextWithSuffix(dishFavorite,data,"favorites","收藏");
        setViewTextWithSuffix(dishBrowse,data,"allClick","浏览");
    }
}
