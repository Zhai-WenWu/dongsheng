package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.override.view.ItemBaseView;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/25 10:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class CommodityItemView extends ItemBaseView{

    private ImageView commodityImage;
    private TextView commodityTitle;
    private TextView commodityPriceUnit;
    private TextView commodityPrice;
    private TextView commoditySaledNum;
    private TextView commodityStatus;

    public CommodityItemView(Context context) {
        super(context,R.layout.a_article_commodity_item);
    }

    public CommodityItemView(Context context, AttributeSet attrs) {
        super(context, attrs,R.layout.a_article_commodity_item);
    }

    public CommodityItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,R.layout.a_article_commodity_item);
    }

    @Override
    public void init(){
        commodityImage = (ImageView) findViewById(R.id.commodity_image);
        commodityTitle = (TextView) findViewById(R.id.commodity_title);
        commodityStatus = (TextView) findViewById(R.id.commodity_status);
        commodityPriceUnit = (TextView) findViewById(R.id.price_unit);
        commodityPrice = (TextView) findViewById(R.id.price);
        commoditySaledNum = (TextView) findViewById(R.id.saled_num);
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(Map<String,String> data){
        setViewImage(commodityImage,data.get("img"));
        setViewText(commodityTitle,data,"name");
        setViewText(commodityPrice,data,"price");
        setViewTextWithPrefix(commoditySaledNum,data,"soldNumber","已售");
    }

}
