package amodule.quan.tool;

import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.view.BarSubjectFloorOwnerNew;
import third.ad.BannerAd;
import third.ad.scrollerAd.XHAllAdControl;

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
    ImageView imageView;

    /** 初始化广告 */
    public void initAd() {
        imageView = (ImageView) floorView.findViewById(R.id.ad_banner_item_iv_single);
        ArrayList<String> ads = new ArrayList<>();
        ads.add(DETAIL_SUBJECT_FLOOR_BOTTOM);
        xhAllAdControl = new XHAllAdControl(ads, mAct, "community_detail");
        xhAllAdControl.start((isRefresh, map)  -> {
            BannerAd bannerAd = new BannerAd(mAct, xhAllAdControl, imageView);
            map = StringManager.getFirstMap(map.get(DETAIL_SUBJECT_FLOOR_BOTTOM));
            bannerAd.onShowAd(map);
        });
        xhAllAdControl.registerRefreshCallback();
    }

    boolean canAdBind = true;
    public void onAdShow(){
        if(imageView == null){
            return;
        }
        if (Tools.inScreenAdView(imageView)) {
            if(xhAllAdControl !=null && canAdBind){
                xhAllAdControl.onAdBind(0, imageView, "");
            }
            canAdBind = false;
        }else{
            canAdBind = true;
        }

    }

}
