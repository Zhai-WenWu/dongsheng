package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.logic.AppCommon;
import acore.logic.XHClick;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 菜谱推荐
 */

public class  DishMenuRecommend  extends ItemBaseView {
    public static String DISH_STYLE_MENU="dish_style_menu";
    private Activity activity;

    public DishMenuRecommend(Context context) {
        super(context, R.layout.view_dish_menu_recommend);
    }

    public DishMenuRecommend(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_menu_recommend);
    }

    public DishMenuRecommend(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_menu_recommend);
    }

    @Override
    public void init() {
        super.init();
    }
    public void setData(final Map<String ,String> map, final Activity activitys){
        this.activity=activitys;
        findViewById(R.id.recommend_ad_linear).setVisibility(View.GONE);
        findViewById(R.id.dish_menu_recommend_rela).setVisibility(View.VISIBLE);
        ImageView dish_menu_recommend_iv= (ImageView) findViewById(R.id.dish_menu_recommend_iv);
        TextView dish_menu_recommend_title= (TextView) findViewById(R.id.dish_menu_recommend_title_new);
        TextView dish_menu_recommend_content= (TextView) findViewById(R.id.dish_menu_recommend_content_new);
        //菜单
        if(map.containsKey("img")&& !TextUtils.isEmpty(map.get("img"))&&
                map.containsKey("title")&& !TextUtils.isEmpty(map.get("title"))){
            findViewById(R.id.dish_menu_recommend_rela).setVisibility(View.VISIBLE);
            setViewImage(dish_menu_recommend_iv,map.get("img"));
            dish_menu_recommend_title.setText(map.get("title"));
            dish_menu_recommend_content.setText(map.get("subtitle"));
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(activity, tongjiId, "广告/运营", "分享下方菜单");
                    AppCommon.openUrl(activity,map.get("url"),true);
                }
            });
            dish_menu_recommend_title.setVisibility(View.VISIBLE);
            dish_menu_recommend_content.setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.dish_menu_recommend_rela).setVisibility(View.GONE);
        }
    }

    /**
     * 处理分享下方广告位广告
     */
    public void setAD(Activity activity){
//        findViewById(R.id.recommend_ad_linear).setVisibility(View.VISIBLE);
//        findViewById(R.id.dish_menu_recommend_rela).setVisibility(View.GONE);
//        //banner广告
//        RelativeLayout dishDetailBanAdTipWeb = (RelativeLayout)findViewById(R.id.dish_detail_banner_layout);
//        BannerAd bannerAdTip = new BannerAd(activity,"菜谱详情分享下方", dishDetailBanAdTipWeb);
//        //广点通banner广告
//        RelativeLayout bannerTipLayout = (RelativeLayout)findViewById(R.id.a_dish_detail_gdt_layout);
//        GdtAdNew gdtTipAd = new GdtAdNew(activity,"菜谱详情分享下方", bannerTipLayout,0, GdtAdTools.ID_DETAIL_DISH_Tip,GdtAdNew.CREATE_AD_BANNER);
//
//        //腾讯banner广告
//        RelativeLayout ad_layout = (RelativeLayout) findViewById(R.id.dish_detail_tencent_layout);
//        TencenApiAd tencentApiAd = new TencenApiAd((Activity) context,"菜谱详情分享下方", TX_ID_DISH_DETAIL_BURDEN_BOTTOM,"1",
//                ad_layout, R.layout.ad_banner_view_second,
//                new AdParent.AdListener() {
//                    @Override
//                    public void onAdCreate() {
//                        super.onAdCreate();
//
//                    }
//                });
//        tencentApiAd.style = TencenApiAd.styleBanner;
//
//        AdParent[] adsTipParent = {tencentApiAd,gdtTipAd,bannerAdTip};
//        AdsShow adTip = new AdsShow(adsTipParent, AdPlayIdConfig.DETAIL_DISH_TIE_BOTTOM);
//        adTip.onResumeAd();
    }
}
