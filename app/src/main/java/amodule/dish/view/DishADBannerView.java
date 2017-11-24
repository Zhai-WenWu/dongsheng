package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.TencenApiAd;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.tools.TencenApiAdTools.TX_ID_DISH_DETAIL_BURDEN_TOP;

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
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),map.get("appUrl"),true);
            }
        });

    }

    public void onListViewScroll() {
//        int[] location = new int[2];
//        if (ad_layout != null)
//            ad_layout.getLocationOnScreen(location);
//        adBurden.isOnScreen(location[1] > Tools.getStatusBarHeight(activity)
//                && location[1] < Tools.getScreenHeight());
//        Log.e("DishADView ","location "+location[1]);
    }

    public interface  AdCallBack{
        public void getAdsShow(AdsShow adBurden);
    }
}
