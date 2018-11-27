package amodule.search.view;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.widget.ImageViewVideo;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;


/**
 * Created by ：fei_teng on 2017/3/16 16:12.
 */

public class SearchResultAdViewGenerater {

    /**
     * 搜索结果页，列表广告
     *
     * @param mActivity
     * @param xhAllAdControl
     * @param adData
     * @return
     */
    public static View generateListAdView(Activity mActivity, final XHAllAdControl xhAllAdControl,
                                          Map<String, String> adData, int adIndex) {
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(mActivity)
                .inflate(R.layout.c_search_result_ad_item, null);

        setAdView(view, adData);
        setViewListener(xhAllAdControl, view, adData, "" + (adIndex + 1));
        view.setVisibility(View.VISIBLE);
        return view;
    }

    private static void setViewListener(final XHAllAdControl xhAllAdControl, final View view,
                                        final Map<String, String> dataMap,
                                        final String indexOnShow) {
        view.setOnClickListener(v -> xhAllAdControl.onAdClick(view,Integer.valueOf(dataMap.get("index")), indexOnShow));

        xhAllAdControl.onAdBind(Integer.valueOf(dataMap.get("index")), view, indexOnShow);
    }


    /**
     * 设置广告view
     *
     * @param adView
     * @param dataMap
     */
    private static void setAdView(View adView, Map<String, String> dataMap) {
        Log.i("tzy", "setAdView: "+dataMap.toString());
        String title = dataMap.get("title");
        String desc = dataMap.get("desc");
        String iconUrl = dataMap.get("iconUrl");
        String imageUrl = dataMap.get("imgUrl");
//        String allClick = dataMap.get("allClick");
        if (adView != null) {
            ImageViewVideo cover_img = (ImageViewVideo) adView.findViewById(R.id.iv_adCover);
            TextView tv_ad_name = (TextView) adView.findViewById(R.id.tv_ad_name);
            TextView tv_ad_decrip = (TextView) adView.findViewById(R.id.tv_ad_decrip);
            ImageView icon_gdt = (ImageView) adView.findViewById(ID_AD_ICON_GDT);
            View view = adView.findViewById(R.id.tv_ad_tag);
            if(view != null){
                view.setVisibility("1".equals(dataMap.get("adType"))?View.GONE:View.VISIBLE);
            }
            if(icon_gdt != null){
                icon_gdt.setVisibility(ADKEY_GDT.equals(dataMap.get("type"))?View.VISIBLE:View.GONE);
            }
            setViewImage(cover_img, TextUtils.isEmpty(imageUrl) ? iconUrl : imageUrl);

            if (TextUtils.isEmpty(desc)) {
                setViewText(tv_ad_name, title);
            } else {
                setViewText(tv_ad_name, desc);
            }

        }
    }

    private static void setViewImage(final ImageViewVideo v, String value) {
        v.parseItemImg(ImageView.ScaleType.CENTER_CROP, value,
                "1", true, R.drawable.i_nopic, FileManager.save_cache);
    }

    private static void setViewText(TextView v, String text) {
        if (text == null || text.length() == 0 || text.equals("hide"))
            v.setVisibility(View.GONE);
        else {
            v.setVisibility(View.VISIBLE);
            v.setText(text.trim());
        }
    }

}
