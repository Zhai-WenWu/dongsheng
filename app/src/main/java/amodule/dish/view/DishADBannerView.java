package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import amodule.dish.activity.DetailDishNew;

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
//        map.put("img","http://s1.cdn.xiangha.com/caipu/201609/0518/052119348809.jpg/NjQwX3J3MTcwN19jXzEtM18w");
        imgWidth=Tools.getPhoneWidth()-Tools.getDimen(context,R.dimen.dp_40);
        setViewImage(img_banner,map.get("img"));
        img_banner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), DetailDishNew.tongjiId_detail, "用料上方banner位", "banner位点击量");
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),map.get("appUrl"),true);
            }
        });
    }
}
