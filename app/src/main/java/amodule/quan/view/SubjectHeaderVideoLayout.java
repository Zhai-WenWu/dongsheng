package amodule.quan.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import third.video.VideoImagePlayerController;
import third.video.VideoPlayerController;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 19:25.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderVideoLayout extends RelativeLayout {
    private BaseAppCompatActivity mAct;
    private int num = 4;
    private Thread mThread;
    private XHAllAdControl xhAllAdControl;
    private String videoImg = "";

    private VideoImagePlayerController videoPlayerController;
    public VideoPlayerController mVideoPlayerController = null;
    private View rootView;
    private FrameLayout adLayout;
    private  Map<String,String> mapAd;
    private  String key= AdPlayIdConfig.SUBJECT_CAIPU_MEDIA;//菜谱视频广告id
    private boolean isAutoPaly=false;//默认自动播放

    public SubjectHeaderVideoLayout(Context context) {
        this(context,null);
    }

    public SubjectHeaderVideoLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderVideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_video_layout,this);
        final int dp_15 = Tools.getDimen(context,R.dimen.dp_15);
        setPadding(dp_15,0,dp_15,0);
        post(new Runnable() {
            @Override
            public void run() {
                getLayoutParams().height = (ToolsDevice.getWindowPx(getContext()).widthPixels - dp_15 * 2) * 3 / 4;
            }
        });
    }

    public boolean setData(Map<String,String> map, Activity activity){
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        boolean isHasVideo = false;
        rootView=LayoutInflater.from(activity).inflate(R.layout.view_subject_video,null);

        if (map.containsKey("selfVideo")) {
            key=AdPlayIdConfig.SUBJECT_USER_MEDIA;
            isHasVideo = true;
            setVisibility(VISIBLE);
            setVideoPlayerData(map.get("selfVideo"),activity);
            addView(rootView);
        }else if(map.containsKey("video")){
            key=AdPlayIdConfig.SUBJECT_CAIPU_MEDIA;
            isHasVideo = true;
            setDishVideo(map.get("video"));
            addView(rootView);
            setVisibility(VISIBLE);
        }
        return isHasVideo;
    }

    private void setVideoPlayerData(String videoData,Activity activity){
        final Map<String, String> videoInfo = StringManager.getFirstMap(videoData);
        adLayout = (FrameLayout) rootView.findViewById(R.id.video_ad_layout);
        if (videoInfo != null) {
            videoImg = videoInfo.get("sImgUrl");
            RelativeLayout dishVidio = (RelativeLayout) rootView.findViewById(R.id.video_layout);
            dishVidio.setVisibility(View.VISIBLE);
            videoPlayerController = new VideoImagePlayerController(activity,dishVidio,videoImg);
            handlerADData();
            videoPlayerController.initVideoView2(videoInfo.get("videoUrl"),"");
            videoPlayerController.setMediaViewCallBack(new VideoPlayerController.MediaViewCallBack() {
                @Override
                public void onclick() {
                    setVideoAdData(mapAd,adLayout);
                }
            });
            setVisibility(VISIBLE);
            if(isAutoPaly)videoPlayerController.setOnClick();
        }
    }

    /**
     * view滚动暂停播放
     */
    public void viewScroll(){
        if (videoPlayerController != null) {
            videoPlayerController.setOnStop();
        }
    }
    public String getVideoImg(){
        return videoImg;
    }


    public ImageViewVideo getImageViewVideo() {
        return null;
    }

    /**
     * 设置菜谱视频数据
     *
     * @param videoData
     */
    private void setDishVideo(String videoData) {
        Map<String, String> videoInfo = StringManager.getFirstMap(videoData);
        if (videoInfo != null) {
            String uu = videoInfo.get("uu");
            String vu = videoInfo.get("vu");
            String img = videoInfo.get("img");
            String name = videoInfo.get("name");
            addVideoView(uu, vu, name, img);
        }
    }

    private void addVideoView(String uu, String vu, String name, String imgValue) {
        adLayout = (FrameLayout) rootView.findViewById(R.id.video_ad_layout);
        RelativeLayout dishVidio = (RelativeLayout) rootView.findViewById(R.id.video_layout);
        dishVidio.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                Tools.getDimen(getContext(), R.dimen.dp_220));//
        dishVidio.setPadding(0, 0, 0, ToolsDevice.dp2px(getContext(), 5));
        dishVidio.setLayoutParams(params);
        adLayout.setLayoutParams(params);
//        initAdView();
        //必须使用avtivity的context
        mVideoPlayerController = new VideoPlayerController(mAct, dishVidio, imgValue);
        handlerADData();
        mVideoPlayerController.initVideoView(vu, uu);
        mVideoPlayerController.setStatisticsPlayCountCallback(new VideoPlayerController.StatisticsPlayCountCallback() {
            @Override
            public void onStatistics() {
                XHClick.mapStat(getContext(), "a_post_detail_video", "贴子部分点击量", "播放点击量");
            }
        });
        mVideoPlayerController.hideFullScreen();
        //点击回调
        mVideoPlayerController.setMediaViewCallBack(new VideoPlayerController.MediaViewCallBack() {
            @Override
            public void onclick() {
                setVideoAdData(mapAd,adLayout);
            }
        });


    }
    public void onResume() {
        if (videoPlayerController != null) {
            videoPlayerController.onResume();
        }
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onResume();
        }
    }

    public void onPause() {
        if (videoPlayerController != null) {
            videoPlayerController.onPause();
        }
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onPause();
        }
    }

    public void onDestroy() {
        if (videoPlayerController != null) {
            videoPlayerController.onDestroy();
        }
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onDestroy();
        }
    }

    public boolean onBackPressed(){
        if (videoPlayerController != null) {
            return videoPlayerController.onBackPressed();
        }
        if (mVideoPlayerController != null) {
            return mVideoPlayerController.onBackPressed();
        }
        return false;
    }

    public VideoImagePlayerController getVideoPlayerController(){
        return videoPlayerController;
    }

    /**
     * 处理视频贴片广告数据
     */
    private void handlerADData(){
        ArrayList<String> list= new ArrayList<>();
        list.add(key);
        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {
                if(isRefresh){
                    return;
                }
                String temp= map.get(key);
                if(!TextUtils.isEmpty(temp)) {
                    mapAd = StringManager.getFirstMap(temp);
                    if (mVideoPlayerController != null){
                        mVideoPlayerController.setShowAd(true);
                    }
                    if(videoPlayerController!=null){
                        videoPlayerController.setShowAd(true);
                    }
                }
                //wifi自动播放
               //YLKLog.i("zhangyujian","isAutoPaly::"+isAutoPaly);
                if(isAutoPaly&&mVideoPlayerController!=null){
                    mVideoPlayerController.setOnClick();
                }

            }
        },mAct,"community_media");
        xhAllAdControl.registerRefreshCallback();
    }

    /**
     * 设置广告所需的activity
     * @param mAct
     */
    public void setmAct(BaseAppCompatActivity mAct){
        this.mAct=mAct;
    }
    /**
     * 处理广告展示
     * @param map
     * @param view
     */
    private void setVideoAdData(final Map<String,String> map,  final View view){
        xhAllAdControl.onAdBind(0,view,"");
        if(view==null){
            Tools.showToast(mAct,"view为null");
        }
        final TextView mNum = (TextView) view.findViewById(R.id.ad_gdt_video_num);
        final ImageView mImageView = (ImageView) view.findViewById(R.id.ad_video_img);
        String imgUrl = null;
        if(map.containsKey("imgUrl")) imgUrl= map.get("imgUrl");
        if(TextUtils.isEmpty(imgUrl))return;
        view.findViewById(R.id.ad_gdt_video_hint_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                if(mVideoPlayerController!=null) {
                    mVideoPlayerController.setShowAd(false);
                    mVideoPlayerController.setOnClick();
                }
                if(videoPlayerController!=null) {
                    videoPlayerController.setShowAd(false);
                    videoPlayerController.setOnClick();
                }
            }
        });
        view.findViewById(R.id.ad_vip_lead).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(mAct,StringManager.getVipUrl(true) + "&vipFrom=视频贴片广告会员免广告",true);
            }
        });

        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(0,"");
            }
        });
        //初始化倒计时
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mNum.setText("" + msg.what);
                if(msg.what == 0){
                   //YLKLog.i("zhangyujian","");
                    view.setVisibility(View.GONE);
                    if(mVideoPlayerController!=null) {
                        mVideoPlayerController.setShowAd(false);
                        mVideoPlayerController.setOnClick();
                    }
                    if(videoPlayerController!=null) {
                        videoPlayerController.setShowAd(false);
                        videoPlayerController.setOnClick();
                    }
                }
            }
        };
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
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                .load(imgUrl)
                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                        return false;
                    }

                    @Override
                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                        mImageView.setVisibility(View.GONE);
                        return false;
                    }
                })
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    view.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    bitmap.getHeight();
                    mImageView.setImageBitmap(bitmap);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(; num > 0; num--) {
                                handler.sendEmptyMessage(num);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(0);
                        }
                    }).start();
                }
            });
    }
}
