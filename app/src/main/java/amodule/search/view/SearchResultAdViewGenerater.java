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
     * 搜索结果页，顶部广告
     * @param mActivity
     * @param adControl
     * @param dataMap
     * @return
     */
    public static RelativeLayout generateTopAdView(Activity mActivity, XHAllAdControl adControl,
                                                   Map<String, String> dataMap) {
        RelativeLayout view = createOtherAdView(mActivity, dataMap);
        if (view != null) {
            View adHint = view.findViewById(R.id.ad_hint);
            AppCommon.setAdHintClick(mActivity,adHint,adControl,Integer.valueOf(dataMap.get("index")),"0","a_searesult_adver","顶部广告");
            setViewListener(adControl, view, dataMap, "0");
            adHint.setVisibility("1".equals(dataMap.get("adType")) ? View.GONE : View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }
        return view;
    }


    private static RelativeLayout createOtherAdView(Activity mActivity, Map<String, String> dataMap) {
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(mActivity)
                .inflate(R.layout.c_search_result_ad_top1, null);
        dataMap.put("imgUrl", dataMap.get("imgUrl"));
        setAdView(view, dataMap);
        return view;
    }

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


    private static void setAdTextInfo(View view, String title, String desc) {
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        View v_cuteline = (View) view.findViewById(R.id.v_cuteline);

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(desc)) {
            v_cuteline.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(title)) {
            tv_title.setText(desc.replaceAll("^(.{16}).*$", "$1\\.\\.\\."));
            v_cuteline.setVisibility(View.GONE);
        } else if (TextUtils.isEmpty(desc)) {
            tv_title.setText(title.replaceAll("(.{16}).*$", "$1\\.\\.\\."));
            v_cuteline.setVisibility(View.GONE);
        } else {

            if (title.length() < 16) {
                tv_title.setText(title);
                int length = desc.length();
                if (length + title.length() > 16) {
                    desc = desc.substring(0, 16 - title.length()) + "...";
                }
                tv_desc.setText(desc);
                v_cuteline.setVisibility(View.VISIBLE);
            } else {
                tv_title.setText(title.replaceAll("^(.{16}).*$", "$1\\.\\.\\."));
                v_cuteline.setVisibility(View.GONE);
            }
        }
    }


    private static void setViewListener(final XHAllAdControl xhAllAdControl, final View view,
                                        final Map<String, String> dataMap,
                                        final String indexOnShow) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(view,Integer.valueOf(dataMap.get("index")), indexOnShow);
            }
        });

        if (!"2".equals(dataMap.get("isShow"))) {
            xhAllAdControl.onAdBind(Integer.valueOf(dataMap.get("index")), view, indexOnShow);
            dataMap.put("isShow", "2");
        }
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
        String allClick = dataMap.get("allClick");
        if (adView != null) {
            ImageViewVideo cover_img = (ImageViewVideo) adView.findViewById(R.id.iv_adCover);
            TextView tv_ad_name = (TextView) adView.findViewById(R.id.tv_ad_name);
            TextView tv_ad_decrip = (TextView) adView.findViewById(R.id.tv_ad_decrip);
            TextView tv_ad_observed = (TextView) adView.findViewById(R.id.tv_ad_observed);
            ImageView icon_gdt = (ImageView) adView.findViewById(ID_AD_ICON_GDT);
            View view = adView.findViewById(R.id.tv_ad_tag);
            if(view != null){
                view.setVisibility("1".equals(dataMap.get("adType"))?View.GONE:View.VISIBLE);
            }
            if(icon_gdt != null){
                icon_gdt.setVisibility(ADKEY_GDT.equals(dataMap.get("type"))?View.VISIBLE:View.GONE);
            }
            setViewImage(cover_img, TextUtils.isEmpty(imageUrl) ? iconUrl : imageUrl);

            if (TextUtils.isEmpty(title)) {
                setViewText(tv_ad_name, desc);
                setViewText(tv_ad_decrip, desc);
            } else {
                setViewText(tv_ad_name, title);
                setViewText(tv_ad_decrip, desc);
            }

            if(TextUtils.isEmpty(allClick))
                allClick = "5189";
            setViewText(tv_ad_observed, allClick + "浏览");
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