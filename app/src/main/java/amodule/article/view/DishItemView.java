package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * PackageName : amodule.article.view.richtext
 * Created by MrTrying on 2017/5/25 11:38.
 * E_mail : ztanzeyu@gmail.com
 */

public class DishItemView extends RelativeLayout {

    private ImageView dishImage;
    private TextView dishName;
    private TextView dishBrowse;
    private TextView dishFavorite;
    private TextView dishCustomerName;

    public DishItemView(Context context) {
        super(context);
        init();
    }

    public DishItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DishItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_dish_item,this);
        dishImage = (ImageView) findViewById(R.id.dish_image);
        dishName= (TextView) findViewById(R.id.dish_name);
        dishBrowse= (TextView) findViewById(R.id.dish_browse);
        dishFavorite= (TextView) findViewById(R.id.dish_favorite);
        dishCustomerName= (TextView) findViewById(R.id.dish_customer_name);
    }
}
