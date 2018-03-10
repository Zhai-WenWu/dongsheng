package amodule.quan.tool;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import amodule.quan.view.BarSubjectFloorOwnerNew;
import third.ad.BannerAd;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.tools.AdPlayIdConfig.DETAIL_SUBJECT_FLOOR_BOTTOM;

/**
 * PackageName : amodule.quan.tool
 * Created by MrTrying on 2016/9/28 15:46.
 * E_mail : ztanzeyu@gmail.com
 */
public class SubjectFloorAdvertControl {
    private BaseAppCompatActivity mAct;
    private BarSubjectFloorOwnerNew floorView;
    private String tongjiId = "";

    public SubjectFloorAdvertControl(BaseAppCompatActivity mAct, BarSubjectFloorOwnerNew floorView, String tongjiId) {
        this.mAct = mAct;
        this.floorView = floorView;
        this.tongjiId = tongjiId;
    }

    XHAllAdControl xhAllAdControl;

    /** 初始化广告 */
    public void initAd() {
        ImageView imageView = (ImageView) floorView.findViewById(R.id.ad_banner_item_iv_single);
        ArrayList<String> ads = new ArrayList<>();
        ads.add(DETAIL_SUBJECT_FLOOR_BOTTOM);
        xhAllAdControl = new XHAllAdControl(ads,
                map -> {
                    BannerAd bannerAd = new BannerAd(mAct, xhAllAdControl, imageView);
                    map = StringManager.getFirstMap(map.get(DETAIL_SUBJECT_FLOOR_BOTTOM));
                    bannerAd.onShowAd(map);
                    xhAllAdControl.onAdBind(0, imageView, "");
                },
                mAct, "community_detail");

    }

}
