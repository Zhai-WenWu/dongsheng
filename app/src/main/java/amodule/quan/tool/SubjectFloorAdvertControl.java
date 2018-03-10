package amodule.quan.tool;

import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import amodule.quan.view.BarSubjectFloorOwnerNew;
import third.ad.BannerAd;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

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
        RelativeLayout layoutParent = (RelativeLayout) floorView.findViewById(R.id.a_subject_detail_ad);
        ArrayList<String> ads = new ArrayList<>();
        ads.add(AdPlayIdConfig.DETAIL_SUBJECT_FLOOR_BOTTOM);
        XHAllAdControl xhAllAdControl = new XHAllAdControl(ads, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                BannerAd bannerAd = new BannerAd(mAct,"community_detail", layoutParent);
//                bannerAd.onShowAd(map);
            }
        },mAct,"community_detail");

    }

}
