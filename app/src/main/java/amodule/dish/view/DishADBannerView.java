package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;

import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import third.ad.BannerAd;
import third.ad.scrollerAd.XHAllAdControl;

import static third.ad.tools.AdPlayIdConfig.DISH_YONGLIAO;

/**
 * 用料上方广告处理——————api
 */

public class DishADBannerView extends ItemBaseView {
    private ImageView img_banner;
    private XHAllAdControl xhAllAdControl;
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
        super.init();
        img_banner= (ImageView) findViewById(R.id.img_banner);
        imgWidth=Tools.getPhoneWidth()-Tools.getDimen(context,R.dimen.dp_40);
        ArrayList<String> list = new ArrayList<>();
        list.add(DISH_YONGLIAO);
        Activity activity = null;
        if (getContext() instanceof Activity) {
            activity = (Activity) getContext();
        } else {
            activity = XHActivityManager.getInstance().getCurrentActivity();
        }
        xhAllAdControl = new XHAllAdControl(list, (isRefresh, map) -> {
            if(map.containsKey(DISH_YONGLIAO)){
                BannerAd bannerAd = new BannerAd(XHActivityManager.getInstance().getCurrentActivity(), xhAllAdControl, img_banner);
                bannerAd.setOnBannerListener(new BannerAd.OnBannerListener() {
                    @Override
                    public void onShowAd() {
                        setVisibility(VISIBLE);
                    }

                    @Override
                    public void onClickAd() {
                        XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), DetailDish.tongjiId_detail, "用料上方banner位", "banner位点击量");
                    }

                    @Override
                    public void onImgShow(int imgH) {

                    }
                });
                map = StringManager.getFirstMap(map.get(DISH_YONGLIAO));
                bannerAd.onShowAd(map);
            }
        },activity,"");
        xhAllAdControl.registerRefreshCallback();
    }

    public void onAdShow(){
        if(xhAllAdControl != null){
            xhAllAdControl.onAdBind(0,img_banner,"");
        }
    }
}
