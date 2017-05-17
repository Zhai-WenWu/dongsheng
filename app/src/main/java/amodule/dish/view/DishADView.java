package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.xiangha.R;

import acore.tools.Tools;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.TencenApiAd;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.tools.TencenApiAdTools.TX_ID_DISH_DETAIL_BURDEN_TOP;

/**
 * 广告处理——————合并View
 */

public class DishADView extends DishBaseView{
    private Activity activity;
    private RelativeLayout adBurdenLayout;
    private RelativeLayout bannerGdtLayout;
    private  RelativeLayout ad_layout;
    private  AdsShow adBurden;

    public DishADView(Context context) {
        super(context,  R.layout.view_dish_ad);
    }

    public DishADView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_ad);
    }

    public DishADView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_ad);
    }

    @Override
    public void init() {
        super.init();
    }
    public void setData(Activity activitys){
        this.activity =activitys;
        /***用料上方广告***********/
        //香哈banner广告
        RelativeLayout dishDetailBanAdBurdenWeb = (RelativeLayout) findViewById(R.id.a_dish_detail_burden_ad_banner_bottom);
        BannerAd bannerAdBurden = new BannerAd(activity,"result_material", dishDetailBanAdBurdenWeb);

        //广点通banner广告
//        bannerGdtLayout = (RelativeLayout) findViewById(R.id.dish_detail_burden_ad_banner_layout);
//        GdtAdNew gdtAd = new GdtAdNew(activity, "菜谱详情用料上方", bannerGdtLayout, 0, GdtAdTools.ID_DETAIL_DISH_Burden, GdtAdNew.CREATE_AD_BANNER);
//        gdtAd.isNeedOnScreen = true;


        //腾讯banner广告
        ad_layout = (RelativeLayout) findViewById(R.id.dish_detail_burden_tencent_banner_layout);
        TencenApiAd tencentApiAd = new TencenApiAd((Activity) context,"result_material", TX_ID_DISH_DETAIL_BURDEN_TOP,"1",
                ad_layout, R.layout.ad_banner_view_second,
                new AdParent.AdListener() {
                    @Override
                    public void onAdCreate() {
                        super.onAdCreate();

                    }
                });
        tencentApiAd.style = TencenApiAd.styleBanner;

        AdParent[] adsBurdenParent = {tencentApiAd, bannerAdBurden};
        adBurden = new AdsShow(adsBurdenParent, AdPlayIdConfig.DISH_YONGLIAO);
        adBurden.onResumeAd();
//        callBack.getAdsShow(adBurden);

    }

    public void onListViewScroll() {
        int[] location = new int[2];
		if (ad_layout != null)
        	ad_layout.getLocationOnScreen(location);
        adBurden.isOnScreen(location[1] > Tools.getStatusBarHeight(activity)
                && location[1] < Tools.getScreenHeight());
        Log.e("DishADView ","location "+location[1]);
    }

    public interface  AdCallBack{
        public void getAdsShow(AdsShow adBurden);
    }
}
