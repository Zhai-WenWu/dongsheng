package amodule.main.view.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.widget.ScrollLinearListLayout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import xh.basic.tool.UtilImage;

/**
 * Created by ：fei_teng on 2017/3/28 16:24.
 */

public class HomeToutiaoAdControl {

    private static HomeToutiaoAdControl instance;
    private  XHAllAdControl allAdControl;
    private  Map<String, String> nouseMap;
    private View adView;
    private AtomicBoolean hasFillData = new AtomicBoolean(false);
    private RelativeLayout viewParent;
    private Activity context;

    private HomeToutiaoAdControl() {
    }

    public static HomeToutiaoAdControl getInstance() {
        if (instance == null) {
            synchronized (HomeToutiaoAdControl.class) {
                if (instance == null) {
                    instance = new HomeToutiaoAdControl();
                }
            }
        }
        return instance;
    }

    public  void getAdData(Activity mActivity) {

        ArrayList<String> adPosList = new ArrayList<>();
        adPosList.add(AdPlayIdConfig.NOUSE_BELOW_TOUTIAO);
        allAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                String adData = map.get(AdPlayIdConfig.NOUSE_BELOW_TOUTIAO);
                Log.i("zhangyujian",AdPlayIdConfig.NOUSE_BELOW_TOUTIAO+":::"+adData);
                if (adData != null && adData.length() > 0) {
                    ArrayList<Map<String, String>> list = StringManager.getListMapByJson(adData);
                    if (list != null && list.size() > 0) {
                        nouseMap = list.get(0);
                        synchronized (HomeToutiaoAdControl.this){
                            if (nouseMap == null || nouseMap.size() < 1) {
                                return;
                            }else if(!hasFillData.get()){
                                hasFillData.set(true);
                                SyntaxTools.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fillDataToView(context,viewParent);
                                    }
                                });
                            }
                        }

                    }
                }

            }
        }, mActivity, "index_top");
    }

    public void setToutiaoAdView(final Activity mActivity, final RelativeLayout viewParent) {
        this.context = mActivity;
        this.viewParent = viewParent;
        synchronized (this) {
            if (nouseMap == null || nouseMap.size() < 1) {
                hasFillData.set(false);
                return;
            } else {
                hasFillData.set(true);
                fillDataToView(mActivity,viewParent);
            }
        }
    }

    private void fillDataToView(final Activity mActivity, final RelativeLayout viewParent) {

        if(mActivity == null || viewParent == null)
            return;

        viewParent.removeAllViews();
        adView = LayoutInflater.from(mActivity).inflate(R.layout.a_ad_view_home_main_nous, viewParent);
        adView.setVisibility(View.GONE);
        final String title = nouseMap.get("title");
        final String content = nouseMap.get("desc");

        String imgUrl = nouseMap.get("imgUrl");
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                .load(imgUrl)
                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                        return false;
                    }

                    @Override
                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                        return false;
                    }
                }).build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    final ImageView imageView = (ImageView) adView.findViewById(R.id.view_ad_img);
                    UtilImage.setImgViewByWH(imageView, bitmap, 0, 0, false);
                    final TextView textView = (TextView) adView.findViewById(R.id.view_ad_text);
                    if (textView != null) {
                        if (TextUtils.isEmpty(title)) textView.setText(content);
                        else textView.setText(title + "，" + content);
                    }
                    initPin(adView);
                    adView.setVisibility(View.VISIBLE);
                    adView.setOnClickListener(ScrollLinearListLayout.getOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            allAdControl.onAdClick(0, "");
                        }
                    }));

                    if (!"2".equals(nouseMap.get("isShow"))) {
                        allAdControl.onAdBind(0, adView, "");
                        nouseMap.put("isShow", "2");
                    }
                }
            });

    }


    private void initPin(View viewParent) {
        TextView tv = (TextView) viewParent.findViewById(R.id.view_ad_ping);
        Random random = new Random();
        int v = random.nextInt(5001) + 30000;
        tv.setText(v + "浏览");
        TextView tv_comment = (TextView) viewParent.findViewById(R.id.view_ad_comment);
        int v_comment = random.nextInt(6) + 5;
        tv_comment.setText(v_comment + "评论");

        if ("1".equals(nouseMap.get("adType"))) {
            ((TextView) viewParent.findViewById(R.id.view_ad_tag)).setText("香哈");
        } else {
            ((TextView) viewParent.findViewById(R.id.view_ad_tag)).setText("广告");
        }
    }
}

