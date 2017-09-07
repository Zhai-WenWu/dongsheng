//package third.ad;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.BitmapRequestBuilder;
//import com.bumptech.glide.load.model.GlideUrl;
//import com.bumptech.glide.request.RequestListener;
//import com.bumptech.glide.request.animation.GlideAnimation;
//import com.bumptech.glide.request.target.Target;
//import com.qq.e.ads.nativ.MediaListener;
//import com.qq.e.ads.nativ.MediaView;
//import com.qq.e.ads.nativ.NativeMediaAD;
//import com.qq.e.ads.nativ.NativeMediaADData;
//import R;
//
//import java.util.List;
//
//import acore.override.XHApplication;
//import aplug.basic.LoadImage;
//import aplug.basic.SubBitmapTarget;
//
///**
// * Created by XiangHa on 2017/2/13.
// */
//
//public class GdtVideoAd extends AdParent{
//
//    private String APP_ID="1150004142";
//    private String POS_ID="4050314544600754";
//
//    private Context mCon;
//    private AdIsShowListener mListener;
//    private NativeMediaAD mNativeMediaAD;
//    private NativeMediaADData mAdData;
//    private FrameLayout mLayoutParent;
//    private MediaView mMediaView;
//    private TextView mNum,mClose;
//    private ImageView mImageView;
//    private OnVideoAdListener mVideoAdListener;
//    private Thread mThread;
//    private boolean isVideoAd;
//    private int num = 5;
//
//    public GdtVideoAd(Context con, FrameLayout layoutParent,OnVideoAdListener listener){
//        mCon = con;
//        mLayoutParent = layoutParent;
//        mVideoAdListener = listener;
//    }
//
//    @Override
//    public boolean isShowAd(String adPlayId, AdIsShowListener listener) {
//        boolean isShow = super.isShowAd(adPlayId, listener);
//        isShow = true;
//        if (isShow) {
//            mListener = listener;
//            init();
//            mNativeMediaAD.loadAD(1);
//        } else {
//            setLoadAdDataState(isShow);
//        }
//        return isShow;
//    }
//
//    @Override
//    public void onResumeAd() {
//        // 广告加载成功 渲染UI
//        String imgUrl = mAdData.getImgUrl();
//        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
//                .load(imgUrl)
//                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
//                    @Override
//                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
//                        mImageView.setVisibility(View.GONE);
//                        return false;
//                    }
//                })
//                .build();
//        if (bitmapRequest != null)
//            bitmapRequest.into(new SubBitmapTarget() {
//                @Override
//                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
//                    mLayoutParent.setVisibility(View.VISIBLE);
//                    mImageView.setVisibility(View.VISIBLE);
//                    bitmap.getHeight();
//                    mImageView.setImageBitmap(bitmap);
//                    if(!isVideoAd){
//                        /**
//                         * 特别注意：和普通图文类原生广告一样，渲染带有视频素材的原生广告时，也需要开发者调用曝光接口onExposured来曝光广告，否则onClicked点击接口将无效
//                         */
//                        mAdData.onExposured(mLayoutParent);
//                        mThread.start();
//                    }
//                }
//            });
//        if(isVideoAd){
//            mLayoutParent.setVisibility(View.VISIBLE);
//            mAdData.bindView(mMediaView, false);
//            mAdData.setMediaListener(new MediaListener() {
//                //视频播放器初始化完成，准备好可以播放了，videoDuration是视频素材的时间长度，单位为ms
//                @Override
//                public void onVideoReady(long duration) {
//                    num = (int) (duration / 1000);
//                }
//                @Override
//                public void onVideoStart() {
//                    mImageView.setVisibility(View.GONE);
//                    mThread.start();
//                }
//                @Override
//                public void onVideoPause() {}
//                @Override
//                public void onVideoComplete() {}
//                @Override
//                public void onVideoError(int i) {}
//                @Override
//                public void onReplayButtonClicked() {}
//                @Override
//                public void onADButtonClicked() {}
//                @Override
//                public void onFullScreenChanged(boolean b) {}
//            });
//            mAdData.play();
//            //设置视频是否静音，flag为true表示有声音，false表示无声。
//            mAdData.setVolumeOn(true);
//            /**
//             * 特别注意：和普通图文类原生广告一样，渲染带有视频素材的原生广告时，也需要开发者调用曝光接口onExposured来曝光广告，否则onClicked点击接口将无效
//             */
//            mAdData.onExposured(mLayoutParent);
//        }
//
//        // 加载视频
//        if (mAdData.isVideoAD()) {
//            mAdData.preLoadVideo();
//        }
//    }
//
//    @Override
//    public void onPsuseAd() {
//
//    }
//
//    @Override
//    public void onDestroyAd() {
//
//    }
//
//
//
//    private void init(){
//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAdData.onClicked(v);
//            }
//        };
//        View.OnClickListener closeListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLayoutParent.setVisibility(View.GONE);
//            }
//        };
//        mMediaView = (MediaView) mLayoutParent.findViewById(R.id.ad_gdt_video_media_view);
//        mNum = (TextView) mLayoutParent.findViewById(R.id.ad_gdt_video_num);
//        mClose = (TextView) mLayoutParent.findViewById(R.id.ad_gdt_video_close);
//        mImageView = (ImageView) mLayoutParent.findViewById(R.id.ad_video_img);
//
//        mMediaView.setOnClickListener(clickListener);
//        mImageView.setOnClickListener(clickListener);
//        mClose.setOnClickListener(closeListener);
//
//
//        mNativeMediaAD = new NativeMediaAD(mCon, APP_ID, POS_ID, new NativeMediaAD.NativeMediaADListener() {
//            /**
//             * 广告数据获取成功时回调
//             * @param ads
//             */
//            @Override
//            public void onADLoaded(List<NativeMediaADData> ads) {
//                //Log.i("FRJ","onADLoaded size:" + ads.size());
//                Toast.makeText(mCon,"加载了数据数：" + ads.size(),Toast.LENGTH_SHORT).show();
//                if (ads.size() > 0) {
//                    mAdData = ads.get(0);
//                    isVideoAd = mAdData.isVideoAD();
//                    setLoadAdDataState(true);
//                }else{
//                    setLoadAdDataState(false);
//                }
//            }
//
//            /**
//             * 广告数据获取失败时回调
//             * @param i
//             */
//            @Override
//            public void onNoAD(int i) {
//                //Log.i("FRJ","onNoAD");
//                setLoadAdDataState(false);
//            }
//
//            /**
//             * 广告数据更新状态时的回调
//             * @param nativeMediaADData
//             */
//            @Override
//            public void onADStatusChanged(NativeMediaADData nativeMediaADData) {
//                //Log.i("FRJ","onADStatusChanged");
//            }
//
//            /**
//             * 广告在曝光、点击、加载视频素材的接口被调用发生错误时的回调
//             * @param nativeMediaADData
//             * @param i
//             */
//            @Override
//            public void onADError(NativeMediaADData nativeMediaADData, int i) {
//                //Log.i("FRJ","onADError");
//                mVideoAdListener.onNoAd();
//            }
//
//            /**
//             * 广告在成功加载加载视频素材时的回调
//             * @param nativeMediaADData
//             */
//            @Override
//            public void onADVideoLoaded(NativeMediaADData nativeMediaADData) {
//                //Log.i("FRJ","onADVideoLoaded");
//                mLayoutParent.setVisibility(View.VISIBLE);
//            }
//
//            /**
//             * 广告曝光的回调
//             * @param nativeMediaADData
//             */
//            @Override
//            public void onADExposure(NativeMediaADData nativeMediaADData) {
//                //Log.i("FRJ","onADExposure");
//            }
//
//            /**
//             * 广告点击的回调
//             * @param nativeMediaADData
//             */
//            @Override
//            public void onADClicked(NativeMediaADData nativeMediaADData) {
//                //Log.i("FRJ","onADClicked");
//            }
//        });
//        final Handler handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                mNum.setText("0" + msg.what);
//                if(msg.what == 0){
//                    mLayoutParent.setVisibility(View.GONE);
//                    mVideoAdListener.onAdOver();
//                }
//            }
//        };
//        mThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for(; num > 0; num--) {
//                    handler.sendEmptyMessage(num);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                handler.sendEmptyMessage(0);
//            }
//        });
//    }
//
//    private void setLoadAdDataState(boolean isSuccess){
//        if(isSuccess){
//            mListener.onIsShowAdCallback(GdtVideoAd.this, true);
//        }else{
//            mLayoutParent.setVisibility(View.GONE);
//            mListener.onIsShowAdCallback(GdtVideoAd.this, false);
//            mVideoAdListener.onNoAd();
//        }
//    }
//
//    public interface OnVideoAdListener{
//        public void onAdOver();
//        public void onNoAd();
//    }
//
//}
