package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import acore.override.XHApplication;
import acore.widget.ScrollLinearListLayout;
import aplug.basic.LoadImage;
import third.ad.tools.AdeazAdTools;

/**
 * Created by XiangHa on 2016/8/29.
 */
public class AdeazAdCreate extends AdParent {

    private Activity mAct;
    private RelativeLayout mAdLayout;
    private ImageView mImg;
    private TextView mAdTitle;


    private ArrayList<Map<String,String>> array;
    private AdListener mAdListener;
    private String mAdId;
    private String mFrom;


    public AdeazAdCreate(Activity con,String from,String adId, RelativeLayout adLayout, int[] resoucesId,AdListener adListener) {
        Random random = new Random();
        int randIndex = random.nextInt(resoucesId.length);
        init(con,from,adLayout,resoucesId[randIndex],adId,adListener);
    }

    public AdeazAdCreate(Activity con,String from,String adId, RelativeLayout adLayout,int resouceId,AdListener adListener) {
        init(con,from,adLayout,resouceId,adId,adListener);
    }

    private void init(Activity con,String from, RelativeLayout adLayout,int resouceId,String adId,AdListener adListener){
        mAct = con;
        mFrom = from;
        mAdLayout = adLayout;
        mAdId = adId;
        mAdListener = adListener;
        LayoutInflater inflater = LayoutInflater.from(mAct);
        View mAdParentView = inflater.inflate(resouceId,adLayout);
        mAdListener.onAdCreate();
        mImg = (ImageView) mAdParentView.findViewById(R.id.view_ad_img);
        mAdTitle = (TextView) mAdParentView.findViewById(R.id.view_ad_text);
        array = new ArrayList<>();
    }

    @Override
    public boolean isShowAd(String adPlayId, final AdIsShowListener listener) {
        final boolean isShow = super.isShowAd(adPlayId, listener);
        //判断数据是否正常
        if (isShow && mAdLayout!=null) {
            AdeazAdTools.getSmsAdData(mAct,mAdId,new AdeazAdTools.OnSmsAdCallback() {
                @Override
                public void onAdShow(ArrayList<Map<String, String>> listReturn) {
                    array.add(listReturn.get(0));
                    listener.onIsShowAdCallback(AdeazAdCreate.this,true);
                }

                @Override
                public void onAdFail() {
                    listener.onIsShowAdCallback(AdeazAdCreate.this,false);
                }
            });
        } else {
            listener.onIsShowAdCallback(AdeazAdCreate.this, isShow);
        }
        return isShow;
    }



    @Override
    public void onResumeAd() {
        if(array.size() > 0){
            mAdLayout.setVisibility(View.VISIBLE);
            onAdShow(mFrom,TONGJI_JD);
            final Map<String,String> map = array.get(0);
            AdeazAdTools.onShowAd(mAct,map);
            mAdTitle.setText(map.get("title"));
            LoadImage.with(XHApplication.in()).load(map.get("src"))
                    .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                        @Override
                        public boolean onResourceReady(Bitmap bitmap, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                            mImg.setImageBitmap(bitmap);
                            return false;
                        }

                        @Override
                        public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                            mAdLayout.setVisibility(View.GONE);
                            return false;
                        }
                    }).preload();
            mAdLayout.setOnClickListener(ScrollLinearListLayout.getOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdeazAdTools.onAdClick(mAct,array.get(0));
                }
            }));
        }else mAdLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onDestroyAd() {

    }
}
