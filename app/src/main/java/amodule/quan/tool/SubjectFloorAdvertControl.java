package amodule.quan.tool;

import android.widget.RelativeLayout;

import com.xianghatest.R;

import acore.override.activity.base.BaseAppCompatActivity;
import amodule.quan.view.BarSubjectFloorOwnerNew;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.TencenApiAd;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.tools.TencenApiAdTools.TX_ID_QUAN_DETAIL;

/**
 * PackageName : amodule.quan.tool
 * Created by MrTrying on 2016/9/28 15:46.
 * E_mail : ztanzeyu@gmail.com
 */
public class SubjectFloorAdvertControl {
    private BaseAppCompatActivity mAct;
    private BarSubjectFloorOwnerNew floorView;
    private String tongjiId ="";

    public SubjectFloorAdvertControl(BaseAppCompatActivity mAct,BarSubjectFloorOwnerNew floorView,String tongjiId){
        this.mAct = mAct;
        this.floorView = floorView;
        this.tongjiId = tongjiId;
    }

    /**初始化广告*/
    public void initAd() {
//        //广点通banner广告
//        RelativeLayout bannerLayout = (RelativeLayout) floorView.findViewById(R.id.a_subject_detail_ad_banner_bd_layout);
//        GdtAdNew gdtAd = new GdtAdNew(mAct,"美食圈美食贴详情", bannerLayout, 0, GdtAdTools.ID_QUAN_BANNER, GdtAdNew.CREATE_AD_BANNER);


        //腾讯banner广告
        RelativeLayout ad_layout = (RelativeLayout) floorView.findViewById(R.id.a_subject_detail_ad_banner_tencent_layout);
        TencenApiAd tencentApiAd = new TencenApiAd(mAct,"community_detail", TX_ID_QUAN_DETAIL,"1",
                ad_layout, R.layout.ad_banner_view_second,
                new AdParent.AdListener() {
                    @Override
                    public void onAdCreate() {
                        super.onAdCreate();
                    }
                });
        tencentApiAd.style = TencenApiAd.styleBanner;


        RelativeLayout layoutParent = (RelativeLayout) floorView.findViewById(R.id.a_subject_detail_ad);
        BannerAd bannerAd = new BannerAd(mAct,"community_detail", layoutParent);

        AdParent[] adsBottom = {tencentApiAd, bannerAd};
        AdsShow adBottom = new AdsShow(adsBottom, AdPlayIdConfig.DETAIL_SUBJECT_FLOOR_BOTTOM);
        mAct.mAds = new AdsShow[]{adBottom};
    }

}
