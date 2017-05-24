package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.MoreImageShow;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import third.video.VideoPlayerController;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.tongjiId;
import static com.xiangha.R.id.dishvideo_img;

/**
 * Created by Administrator on 2016/9/21.
 */

public class DishHeaderView extends LinearLayout {
    private Context context;
    private Activity activity;
    //头部的view
    private DishAboutView dishAboutView;
    private DishDataShow dishDataShow;
    private View view_oneImage;
    private RelativeLayout dishVidioLayout;
    private DishADView dishADView;

    private boolean isHasVideo = false;//
    private VideoPlayerController mVideoPlayerController = null;//视频控制器
    private DishHeaderVideoCallBack callBack;
    private  FrameLayout adLayout;
    private  Map<String,String> mapAd;//广告数据
    private boolean isAutoPaly=false;//默认自动播放
    private boolean isOnResuming=false;//默认自动播放

    public DishHeaderView(Context context) {
        super(context);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public DishHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public DishHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * 初始化view
     *
     * @param activitys
     */
    public void initView(Activity activitys) {
        this.activity = activitys;
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        //大图处理
        view_oneImage = LayoutInflater.from(activity).inflate(R.layout.view_dish_header_oneimage, null);
        dishVidioLayout = (RelativeLayout) view_oneImage.findViewById(R.id.dishVidio);

        //处理简介
        dishAboutView = new DishAboutView(activity);
        //广告
         dishADView = new DishADView(activity);
        dishADView.setData(activity);
        //主辅料
        dishDataShow = new DishDataShow(activity);
        //头部加载view
        this.addView(view_oneImage);
        this.addView(dishAboutView);
        this.addView(dishADView);
        this.addView(dishDataShow);
    }

    /**
     * 设置数据
     *
     * @param list
     */
    public void setData(ArrayList<Map<String, String>> list, DishHeaderVideoCallBack callBacks) {
        if (callBacks == null) {
            callBack = new DishHeaderVideoCallBack() {
                @Override
                public void videoImageOnClick() {}
                @Override
                public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayout, View view_oneImage) {}
            };
        } else this.callBack = callBacks;
        String title = list.get(0).get("name");

        try {
            if(list.get(0).containsKey("selfVideo")&&!TextUtils.isEmpty(list.get(0).get("selfVideo"))
                    && !"[]".equals(list.get(0).get("selfVideo"))){
            String selfVideo = list.get(0).get("selfVideo");
            if(!TextUtils.isEmpty(selfVideo)
                    && !"[]".equals(selfVideo)){
                boolean urlValid = setSelfVideo(title, list.get(0).get("selfVideo"), list.get(0).get("img"));
                if (!urlValid) {
                    setVideo(list.get(0).get("hasVideo"), list.get(0).get("video"), list.get(0).get("img"));
                }
            }else{
                setVideo(list.get(0).get("hasVideo"), list.get(0).get("video"), list.get(0).get("img"));
            }
        }catch (Exception e){
            Toast.makeText(context,"视频播放失败",Toast.LENGTH_SHORT).show();
        }
        dishAboutView.setData(list.get(0), activity);
        dishDataShow.setData(list, activity);
    }

    private void initVideoAd(){
        adLayout = (FrameLayout) view_oneImage.findViewById(R.id.dishvideo_ad);
        int distance = Tools.getDimen(activity, R.dimen.dp_45);
        if (Tools.isShowTitle()) {
            distance += Tools.getStatusBarHeight(activity);
        }
        adLayout.setPadding(0,distance,0,0);

        ArrayList<String> list= new ArrayList<>();
        String key= AdPlayIdConfig.DISH_MEDIA;
        list.add(key);

        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> maps) {
                String temp = maps.get(AdPlayIdConfig.DISH_MEDIA);
                mapAd= StringManager.getFirstMap(temp);
                Log.i("tzy","needVideoControl time = " + System.currentTimeMillis());
                if(mapAd != null && mapAd.size()>0
                        && mVideoPlayerController != null){
                    mVideoPlayerController.setShowAd(true);
                }
                if(isAutoPaly && mVideoPlayerController != null)
                    mVideoPlayerController.setOnClick();
            }
        },activity,"result_media");

    }
    private int num = 4;
    private Thread mThread;
    private XHAllAdControl xhAllAdControl;

    /**
     * 处理广告展示
     * @param map
     * @param view
     */
    private void setVideoAdData(final Map<String,String> map, final View view){
        xhAllAdControl.onAdBind(0,view,"");
        final TextView mNum = (TextView) view.findViewById(R.id.ad_gdt_video_num);
        final ImageView mImageView = (ImageView) view.findViewById(R.id.ad_video_img);
        String imgUrl = null;
        if(map.containsKey("imgUrl")) imgUrl= map.get("imgUrl");
        if(TextUtils.isEmpty(imgUrl))return;

        view.findViewById(R.id.ad_gdt_video_hint_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                mVideoPlayerController.setShowAd(false);
                mVideoPlayerController.setOnClick();
            }
        });
        view.findViewById(R.id.ad_vip_lead).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(activity,StringManager.api_openVip,true);
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
                    view.setVisibility(View.GONE);
                    mVideoPlayerController.setShowAd(false);
                    if(isOnResuming) mVideoPlayerController.setOnClick();
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

    private boolean setSelfVideo(final String title, final String selfVideoJson, final String img) {
        initVideoAd();
        boolean isUrlVaild = false;
        tongjiId = "a_menu_detail_video430";
        if(!TextUtils.isEmpty(selfVideoJson)
        &&!"[]".equals(selfVideoJson)){
            Map<String, String> selfVideoMap = UtilString.getListMapByJson(selfVideoJson).get(0);
            String videoUrl = selfVideoMap.get("url");
            if (!TextUtils.isEmpty(videoUrl)
                    && videoUrl.startsWith("http")) {
                LinearLayout dishvideo_img = (LinearLayout) view_oneImage.findViewById(R.id.dishvideo_img);
                int distance = Tools.getDimen(activity, R.dimen.dp_45);
                if (Tools.isShowTitle()) {
                    distance += Tools.getStatusBarHeight(activity);
                }
                dishVidioLayout.setPadding(0, distance, 0, 0);
//                dishvideo_img.addView(new DishVideoImageView(activity).setData(img, selfVideoMap.get("duration")));
                mVideoPlayerController = new VideoPlayerController(activity, dishVidioLayout, img);
                DishVideoImageView dishVideoImageView = new DishVideoImageView(activity);
                dishVideoImageView.setData(img, selfVideoMap.get("duration"));
                mVideoPlayerController.setNewView(dishVideoImageView);
                mVideoPlayerController.initVideoView2(videoUrl, title, dishVideoImageView);
                mVideoPlayerController.setStatisticsPlayCountCallback(new VideoPlayerController.StatisticsPlayCountCallback() {
                    @Override
                    public void onStatistics() {
                        XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "视频播放按钮点击");
                        callBack.videoImageOnClick();
                    }
                });
                //被点击回调
                mVideoPlayerController.setMediaViewCallBack(new VideoPlayerController.MediaViewCallBack() {
                    @Override
                    public void onclick() {
                        setVideoAdData(mapAd,adLayout);
                    }
                });
                dishvideo_img.setVisibility(View.GONE);
                callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, view_oneImage);
                callBack.videoImageOnClick();
//                mVideoPlayerController.setOnClick();
                isUrlVaild = true;
            }
        }
        return isUrlVaild;
    }


    /**
     * 展示顶图view,是大图还是视频
     *
     * @param hasVideo  》标记是否是视频字段 1---不是 2---是
     * @param videoJson 》视频数据
     * @param img       》图片链接
     */
    private void setVideo(String hasVideo, final String videoJson, final String img) {
        if ("2".equals(hasVideo)) {
            isHasVideo = true;
            initVideoAd();
            tongjiId = "a_menu_detail_video430";
            if(!TextUtils.isEmpty(videoJson)
            &&!"[]".equals(videoJson)){
                Map<String, String> dishBurden = UtilString.getListMapByJson(videoJson).get(0);
                String videoUnique = dishBurden.get("vu");
                String userUnique = dishBurden.get("uu");
                LinearLayout dishvideo_img = (LinearLayout) view_oneImage.findViewById(R.id.dishvideo_img);
                int distance = Tools.getDimen(activity, R.dimen.dp_45);
                if (Tools.isShowTitle()) {
                    distance += Tools.getStatusBarHeight(activity);
                }
                dishVidioLayout.setPadding(0, distance, 0, 0);
//                dishvideo_img.addView(new DishVideoImageView(activity).setData(img, dishBurden.get("duration")));
                mVideoPlayerController = new VideoPlayerController(activity, dishVidioLayout, img);
                DishVideoImageView dishVideoImageView = new DishVideoImageView(activity);
                dishVideoImageView.setData(img, dishBurden.get("duration"));
                mVideoPlayerController.setNewView(dishVideoImageView);
                mVideoPlayerController.initVideoView(videoUnique, userUnique, dishVideoImageView);
                mVideoPlayerController.setStatisticsPlayCountCallback(new VideoPlayerController.StatisticsPlayCountCallback() {
                    @Override
                    public void onStatistics() {
                        XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "视频播放按钮点击");
                        callBack.videoImageOnClick();
                    }
                });
                //被点击回调
                mVideoPlayerController.setMediaViewCallBack(new VideoPlayerController.MediaViewCallBack() {
                    @Override
                    public void onclick() {
                        setVideoAdData(mapAd,adLayout);
                    }
                });
                dishvideo_img.setVisibility(View.GONE);
                callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, view_oneImage);
                Log.i("tzy","getVideoControl time = " + System.currentTimeMillis());
                callBack.videoImageOnClick();

            }else{
                Toast.makeText(context,"视频地址信息错误",Toast.LENGTH_SHORT).show();
            }
        } else {
            findViewById(dishvideo_img).setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final ImageViewVideo imvv = new ImageViewVideo(activity);
            imvv.parseItemImg(ImageView.ScaleType.CENTER_CROP, img, false, false, R.drawable.i_nopic, FileManager.save_cache);
            imvv.setLayoutParams(params);
            dishVidioLayout.removeAllViews();
            dishVidioLayout.addView(imvv);
            dishVidioLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dishVidioLayout.setClickable(false);
                    XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "菜谱大图点击");
                    ArrayList<Map<String, String>> listmap = new ArrayList<Map<String, String>>();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("img", img);
                    map.put("info", "");
                    map.put("num", "1");
                    listmap.add(map);

                    Intent intent = new Intent(activity, MoreImageShow.class);
                    intent.putExtra("data", listmap);
                    intent.putExtra("index", 0);
                    intent.putExtra("isShowAd", false);
                    dishVidioLayout.setClickable(true);
                    activity.startActivity(intent);
                }
            });
        }
//        setViewOneState();
        callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, view_oneImage);
    }

    public void onResume() {
        isOnResuming = true;
    }
    public void onPause() {
        isOnResuming = false;
    }

    /**
     * 获取简介View
     *
     * @return
     */
    public DishAboutView getdDshAboutView() {
        return dishAboutView;
    }

    public void onListViewScroll() {
        dishADView.onListViewScroll();
    }

    /**
     * video回调
     */
    public interface DishHeaderVideoCallBack {
        /***
         * 视频被点击回调
         */
        public void videoImageOnClick();

        /**
         * 获取视频控制器
         *
         * @param mVideoPlayerController
         */
        public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayout, View view_oneImage);
    }
}
