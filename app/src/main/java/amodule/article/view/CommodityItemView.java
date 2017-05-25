package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/25 10:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class CommodityItemView extends RelativeLayout implements View.OnClickListener{

    private ImageView commodityImage;
    private TextView commodityTitle;
    private TextView commodityPriceUnit;
    private TextView commodityPrice;
    private TextView commoditySaledNum;
    private TextView commodityStatus;

    public CommodityItemView(Context context) {
        super(context);
        init();
    }

    public CommodityItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommodityItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_commodity_item,this);
        commodityImage = (ImageView) findViewById(R.id.commodity_image);
        commodityTitle = (TextView) findViewById(R.id.commodity_title);
        commodityStatus = (TextView) findViewById(R.id.commodity_status);
        commodityPriceUnit = (TextView) findViewById(R.id.price_unit);
        commodityPrice = (TextView) findViewById(R.id.price);
        commoditySaledNum = (TextView) findViewById(R.id.saled_num);

        findViewById(R.id.commodity_layout).setOnClickListener(this);
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(Map<String,String> data){

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.commodity_layout:
                //添加事件
                break;
            default:
                break;
        }
    }
}
