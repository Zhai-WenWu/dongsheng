package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.ScrollLinearListLayout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.tools.TencenApiAdTools;
import xh.basic.tool.UtilImage;

/**
 * Created by Fang Ruijiao on 2016/12/6.
 */
public class TencenApiAd extends AdParent{
    private Activity mAct;
    private Handler handler;
    private String mAdId,mLoid;
    private RelativeLayout mAdLayout;
    private int mResouceId;
    private AdListener mListener;
    private Map<String,String> mAdMap;
    private String imgUrl,title,content,tjShowUrl,tjClickUrl,clickUrl;
    /**
     * 首页的点击事件比较特殊，故加标记特效处理
     */
    public boolean isMain = false;
    private String mFrom;
    private AdIsShowListener mAdIsShowListener;

    public String style = null;
    public int maginLeft = 0,maginRight = 0;

    public static final String styleBanner = "styleBanner";
    private String StatisticKey;
    private String ad_show;//展示一级统计
    private String ad_click;//点击一级统一
    private String twoData;//二级统计
    private String key="api_tfp";


    public TencenApiAd(Activity context,String from, String adId,String loid, RelativeLayout adLayout, int resouceId, AdListener listener){
        mAct = context;
        mFrom = from;
        StatisticKey=from;
        mAdId = adId;
        mLoid = loid;
        mAdLayout = adLayout;
        mResouceId = resouceId;
        mListener = listener;
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    mAdIsShowListener.onIsShowAdCallback(TencenApiAd.this,true);
                }else{
                    mAdIsShowListener.onIsShowAdCallback(TencenApiAd.this,false);
                }
            }
        };
        getStiaticsData();
    }

    @Override
    public boolean isShowAd(String adPlayId, final AdIsShowListener listener) {
        boolean isShow = LoginManager.isShowAd();//无效创建，到这个位置一定是展示的，在数据层已经进行区分
        mAdIsShowListener = listener;
        ///判断数据是否正常
        if (isShow) {
            TencenApiAdTools.getTencenApiAdTools().getApiAd(mAct,mAdId,mLoid,new TencenApiAdTools.OnTencenAdCallback() {
                @Override
                public void onAdShow(ArrayList<Map<String, String>> array) {
                    if(array.size() > 0) {
                        mAdMap = array.get(0);
                        handler.sendEmptyMessage(1);
                    }else{
                        handler.sendEmptyMessage(0);
                    }
                }
                @Override
                public void onAdFail() {
                    handler.sendEmptyMessage(0);
                }
            });
        } else {
            handler.sendEmptyMessage(0);
        }
        return isShow;
    }

    @Override
    public void onResumeAd() {
//        onAdShow(mFrom,TONGJI_TX_API);
        onAdShow(ad_show,twoData,key,key,"0");//更改统计
        View view;
        mAdLayout.setVisibility(View.VISIBLE);
        if (mAdLayout.getChildCount() > 0) {
            view = mAdLayout.getChildAt(0);
        } else {
            view = LayoutInflater.from(mAct).inflate(mResouceId, mAdLayout);
            view.setVisibility(View.GONE);
            if (mListener != null) mListener.onAdCreate();
        }
        initAd(view);
    }

    private void initAd(final View adView) {
        String seatbid = mAdMap.get("seatbid");
        ArrayList<Map<String,String>> array = StringManager.getListMapByJson(seatbid);
        if(array.size() > 0) {
            Map<String,String> map = array.get(0);
            String bid = map.get("bid");
            array = StringManager.getListMapByJson(bid);
            if(array.size() > 0) {
                map = array.get(0);
                String ext = map.get("ext");
                array = StringManager.getListMapByJson(ext);
                if(array.size() > 0) {
                    map = array.get(0);
                    String aurl = map.get("aurl");
                    array = StringManager.getListMapByJson(aurl);
                    if(array.size() > 0) {
                        Map<String,String> aurlMap = array.get(0);
                        imgUrl = aurlMap.get("");
                    }
                    //点击的监测地址数组(最多三个）
                    tjClickUrl = map.get("cmurl");
                    // 点击后跳转地址 【宏替换】
                    clickUrl = map.get("curl");
                    //曝光监测地址数组（最多五个）
                    tjShowUrl = map.get("murl");
                    title = map.get("title");
                    content = map.get("text");
                }
            }
        }
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
                    if(textView != null) {
                        if (TextUtils.isEmpty(title)) textView.setText(content);
                        else textView.setText(title + "，" + content);
                    }
                    if(styleBanner.equals(style)){
                       int imgViewWidth = ToolsDevice.getWindowPx(mAct).widthPixels - maginLeft - maginLeft;
                       int imgHeight = imgViewWidth * bitmap.getHeight() / bitmap.getWidth();
                       imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                       UtilImage.setImgViewByWH(imageView, bitmap, imgViewWidth, imgHeight, true);
                    }
                    adView.setVisibility(View.VISIBLE);
                    TencenApiAdTools.onShowAd(XHApplication.in(),tjShowUrl);
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TencenApiAdTools.onClickAd(mAct,clickUrl,tjClickUrl);
                            XHClick.track(mAct,"点击广告");
                            onAdClick(mFrom,TONGJI_TX_API);
                            onAdClick(ad_show,twoData,key,key,"0");
                        }
                    };
                    if(isMain){
                        adView.setOnClickListener(ScrollLinearListLayout.getOnClickListener(listener));
                    }else{
                        adView.setOnClickListener(listener);
                    }
                }
            });
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onDestroyAd() {

    }
    /**
     * 获取广告统计层级数据
     */
    private void getStiaticsData(){
        String msg= FileManager.getFromAssets(mAct, "adStatistics");
        ArrayList<Map<String,String>> listmap = StringManager.getListMapByJson(msg);
        if(listmap.get(0).containsKey(StatisticKey)){
            ArrayList<Map<String,String>> stiaticsList= StringManager.getListMapByJson(listmap.get(0).get(StatisticKey));
            ad_show = stiaticsList.get(0).get("show");
            ad_click = stiaticsList.get(0).get("click");
            twoData =stiaticsList.get(0).get("twoData");
        }
    }
}
