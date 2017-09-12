package amodule.dish.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;

/**
 * 图片预览单个view
 * Created by XiangHa on 2016/8/16.
 */
public class ImageMoreAdView extends ImageMoreView implements View.OnClickListener {

    private final XHAllAdControl mXhadControl;
    private Activity mAct;
    private List<? extends Object>  mData;
    private View mParentView,mAdHint;
    private RelativeLayout mContentParentRl;

    private TextView contentTv;
    private ImageView imageTouchView;

    private String mAdPlayId,mAdPlayKey;

    /**
     *
     * @param con
     * @param data
     * @param adPlayId : 广告位id
     * @param adPlayKey ：广告类型
     */
    public ImageMoreAdView(XHAllAdControl xhadControl, Activity con, List<? extends Object> data, String adPlayId, String adPlayKey){
        mXhadControl = xhadControl;
        mAct = con;
        mData = data;
        mAdPlayId = adPlayId;
        mAdPlayKey = adPlayKey;
        LayoutInflater inflater = LayoutInflater.from(con);
        mParentView = inflater.inflate(R.layout.a_dish_more_imageview_item_ad,null);
        init();
    }

    private void init(){
        imageTouchView = (ImageView) mParentView.findViewById(R.id.view_ad_img);
        contentTv = (TextView)mParentView.findViewById(R.id.view_ad_text);
        mContentParentRl = (RelativeLayout) mParentView.findViewById(R.id.a_dish_more_img_item_content_parent_rl);
        mParentView.findViewById(R.id.a_dish_more_img_item_close).setOnClickListener(this);
        mAdHint = mParentView.findViewById(R.id.ad_hint);
    }

    private void initAd(){
        if("ad".equals(mAdPlayKey)){
            Map<String,String> map = (Map<String, String>) mData.get(0);
            setAdView(map.get("title"), map.get("desc"), map.get("imgUrl"));
            if("1".equals(map.get("adType"))){
                mAdHint.setVisibility(View.INVISIBLE);
            }else{
                mAdHint.setVisibility(View.VISIBLE);
            }
        }
        mContentParentRl.setOnClickListener(this);
        AppCommon.setAdHintClick(mAct,mAdHint,mXhadControl,0,"");
    }

    private void setAdView(String title, final String desc, String imageUrl){
        contentTv.setText(title + "，" + desc);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
                .load(imageUrl)
                .setSaveType(FileManager.save_cache)
                .build();
        if (bitmapRequest != null) {
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    DisplayMetrics metrics = ToolsDevice.getWindowPx(mAct);
                    int windW = metrics.widthPixels;
                    int imgH = (720 * windW) / 1280;
                    imageTouchView.setImageBitmap(bitmap);
                    RelativeLayout.LayoutParams params =  new RelativeLayout.LayoutParams(windW,imgH);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    imageTouchView.setLayoutParams(params);
                }
            });
        }
        imageTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdClick(imageTouchView);
            }
        });
    }


    public void onShow(){

        if("ad".equals(mAdPlayKey)){
            Map<String,String> map = (Map<String, String>) mData.get(0);
            if(!"2".equals(map.get("isShow"))){
                mXhadControl.onAdBind(0,mParentView,"");
                map.put("isShow","2");
            }
        }
    }

    @Override
    public void setOnClick() {
        imageTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdClick(imageTouchView);
            }
        });
    }


    private void onAdClick(View v){
        mXhadControl.onAdClick(0,"");
    }


    @Override
    public void onPageChange(){
        initAd();
    }

    @Override
    public View getImageMoreView(){
        return mParentView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.a_dish_more_img_item_close:
                mAct.finish();
                break;
            case R.id.a_dish_more_img_item_content_parent_rl:
                onAdClick(mParentView.findViewById(R.id.a_dish_more_img_item_content_parent_rl));
                break;
        }
    }
}