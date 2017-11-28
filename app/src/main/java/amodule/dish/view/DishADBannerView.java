package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.Tools;

/**
 * 用料上方广告处理——————api
 */

public class DishADBannerView extends ItemBaseView {
    private ImageView img_banner;
    public DishADBannerView(Context context) {
        super(context,  R.layout.view_dish_ad);
    }

    public DishADBannerView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_ad);
    }

    public DishADBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_ad);
    }
    @Override
    public void init() {
        super.init();img_banner= (ImageView) findViewById(R.id.img_banner);
    }
    public void setData(final Map<String,String> map){
        imgWidth=Tools.getPhoneWidth()-Tools.getDimen(context,R.dimen.dp_40);
        setViewImage(img_banner,map.get("img"));
        img_banner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),map.get("appUrl"),true);
            }
        });
    }
}
