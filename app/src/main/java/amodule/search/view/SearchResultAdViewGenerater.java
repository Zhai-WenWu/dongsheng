package amodule.search.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.widget.ImageViewVideo;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;


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
        RelativeLayout view;
        String type = dataMap.get("type");

//        type = XHScrollerAdParent.ADKEY_API;
//        dataMap.put("stype","301");

        if (XHScrollerAdParent.ADKEY_BANNER.equals(type)) {
            view = createSelfAdView(mActivity, dataMap);
        } else if (XHScrollerAdParent.ADKEY_API.equals(type)) {
            view = createTencentAdView(mActivity, dataMap);
        } else {
            view = createOtherAdView(mActivity, dataMap);
        }

        if (view != null) {
            View adHint = view.findViewById(R.id.ad_hint);
            AppCommon.setAdHintClick(mActivity,adHint,adControl,Integer.valueOf(dataMap.get("index")),"0","a_searesult_adver","顶部广告");
            setViewListener(adControl, view, dataMap, "0");
            view.setVisibility(View.VISIBLE);
        }
        return view;
    }


    private static RelativeLayout createOtherAdView(Activity mActivity, Map<String, String> dataMap) {
        RelativeLayout view = null;
        view = (RelativeLayout) LayoutInflater.from(mActivity)
                .inflate(R.layout.c_search_result_ad_top1, null);
        dataMap.put("imgUrl", dataMap.get("imgUrl"));
        setAdView(view, dataMap);
        return view;
    }


    private static RelativeLayout createTencentAdView(Activity mActivity, Map<String, String> dataMap) {
        RelativeLayout view = null;

        if ("101".equals(dataMap.get("stype"))) {
            view = (RelativeLayout) LayoutInflater.from(mActivity)
                    .inflate(R.layout.c_search_result_ad_top1, null);
            dataMap.put("imgUrl", dataMap.get("imgUrl"));
            setAdView(view, dataMap);
        } else if ("202".equals(dataMap.get("stype"))) {
            view = (RelativeLayout) LayoutInflater.from(mActivity)
                    .inflate(R.layout.c_search_result_ad_top2, null);
            ImageViewVideo iv_adCover = (ImageViewVideo) view.findViewById(R.id.iv_adCover);
            setAdTextInfo(view, dataMap.get("title"), dataMap.get("desc"));
            view.findViewById(R.id.ad_hint).setVisibility(View.VISIBLE);
            setViewImage(iv_adCover, dataMap.get("imgUrl"));
        } else if ("301".equals(dataMap.get("stype"))) {
            final ArrayList<String> tempList = new ArrayList<>();
            if (!TextUtils.isEmpty(dataMap.get("imgs"))) {
                ArrayList<Map<String, String>> imgList = StringManager.getListMapByJson(dataMap.get("imgs"));
                SyntaxTools.loop(imgList, new SyntaxTools.LooperCallBack() {
                    @Override
                    public boolean loop(int i, Object object) {
                        Map<String, String> imgMap = (Map<String, String>) object;
                        String imgUrl = imgMap.get("");
                        if (!TextUtils.isEmpty(imgUrl) && imgUrl.startsWith("http")) {
                            tempList.add(imgUrl);
                        }
                        return false;
                    }
                });
            }

            if (tempList.size() == 3) {
                view = (RelativeLayout) LayoutInflater.from(mActivity)
                        .inflate(R.layout.c_search_result_ad_top3, null);
                ImageViewVideo iv_ad1 = (ImageViewVideo) view.findViewById(R.id.iv_ad1);
                ImageViewVideo iv_ad2 = (ImageViewVideo) view.findViewById(R.id.iv_ad2);
                ImageViewVideo iv_ad3 = (ImageViewVideo) view.findViewById(R.id.iv_ad3);

                setViewImage(iv_ad1, tempList.get(0));
                setViewImage(iv_ad2, tempList.get(1));
                setViewImage(iv_ad3, tempList.get(2));

                setAdTextInfo(view, dataMap.get("title"), dataMap.get("desc"));
                view.findViewById(R.id.ad_hint).setVisibility(View.VISIBLE);
            } else if (tempList.size() > 0) {
                view = (RelativeLayout) LayoutInflater.from(mActivity)
                        .inflate(R.layout.c_search_result_ad_top1, null);
                dataMap.put("imgUrl", tempList.get(0));
                setAdView(view, dataMap);
            }
        }
        return view;
    }


    private static RelativeLayout createSelfAdView(Activity mActivity, Map<String, String> dataMap) {

        RelativeLayout view = null;
        if (dataMap.get("appSearchImg").startsWith("http")) {
            view = (RelativeLayout) LayoutInflater.from(mActivity)
                    .inflate(R.layout.c_search_result_ad_top1, null);
            dataMap.put("imgUrl", dataMap.get("appSearchImg"));
            setAdView(view, dataMap);
        }
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

        if(XHScrollerAdParent.ADKEY_BANNER.equals(adData.get("type"))){
            adData.put("imgUrl",adData.get("appSearchImg"));
        }
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