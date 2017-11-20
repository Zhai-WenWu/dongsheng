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
import android.view.animation.AlphaAnimation;
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
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
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
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * Created by Administrator on 2016/9/21.
 */
public class DishHeaderViewNew extends LinearLayout {
    private Context context;
    private Activity activity;
    //头部的view
    private View videoViewGroup;
    private RelativeLayout dishVidioLayout;
    private LinearLayout dishvideo_img ;

    private VideoPlayerController mVideoPlayerController = null;//视频控制器
    private DishHeaderVideoCallBack callBack;
    private FrameLayout adLayout;
    private Map<String, String> mapAd;//广告数据
    private boolean isAutoPaly = false;//默认自动播放
    private boolean isOnResuming = false;//默认自动播放

    private int distance;
    private boolean isLoadImg=false;

    public DishHeaderViewNew(Context context) {
        super(context);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public DishHeaderViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public DishHeaderViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public void reset(){

    }

    /**
     * 初始化view
     *
     * @param activity
     */
    public void initView(Activity activity,int videoHeight) {
        this.activity = activity;
        distance = Tools.getDimen(activity, R.dimen.dp_45) + Tools.getStatusBarHeight(getContext());
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        //大图处理
        videoViewGroup = LayoutInflater.from(activity).inflate(R.layout.view_dish_header_oneimage, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,videoHeight);
        dishVidioLayout = (RelativeLayout) videoViewGroup.findViewById(R.id.video_layout);
        dishVidioLayout.setLayoutParams(params);
        dredgeVipLayout = (RelativeLayout) videoViewGroup.findViewById(R.id.video_dredge_vip_layout);
        dredgeVipLayout.setLayoutParams(params);
        dishvideo_img = (LinearLayout) videoViewGroup.findViewById(R.id.video_img_layout);
        dishvideo_img.setLayoutParams(params);
        adLayout = (FrameLayout) videoViewGroup.findViewById(R.id.video_ad_layout);
        adLayout.setLayoutParams(params);

        //处理简介
        //头部加载view
        this.addView(videoViewGroup);
//        INVisibiHeaderView();
    }
    public void setDistance(int distances){
        this.distance = distances;
    }
    public RelativeLayout getViewLayout(){
        return  dishVidioLayout;
    }
    /**
     * 设置当前header的callback回调
     */
    public void setDishCallBack(DishHeaderVideoCallBack callBacks){
        if (callBacks == null) {
            callBack = new DishHeaderVideoCallBack() {
                @Override
                public void videoImageOnClick() {
                }

                @Override
                public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayout, View view_oneImage) {
                }
            };
        } else this.callBack = callBacks;
    }
    /**
     * 设置数据
     *
     * @param list
     * @param permissionMap
     */
    public void setData(ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        Map<String, String> videoMap = list.get(0);
        String title = videoMap.get("title");
//        try {
            String selfVideo = videoMap.get("video");
            String img = videoMap.get("img");
            String type = videoMap.get("type");

            if ("2".equals(type) && !TextUtils.isEmpty(selfVideo) && !"[]".equals(selfVideo)) {
                if (!setSelfVideo(title, selfVideo, img, permissionMap))
                    Toast.makeText(context, "视频播放失败222", Toast.LENGTH_SHORT).show();
            } else {
                if(isLoadImg) {
                    handlerImage(img);
                }else{
                    setImg(img);
                }

            }
//        } catch (Exception e) {
//            Toast.makeText(context, "视频播放失败111", Toast.LENGTH_SHORT).show();
//        }
    }

    private void initVideoAd() {
        adLayout.setPadding(0, distance, 0, 0);

        ArrayList<String> list = new ArrayList<>();
        String key = AdPlayIdConfig.DISH_MEDIA;
        list.add(key);

        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> maps) {
                String temp = maps.get(AdPlayIdConfig.DISH_MEDIA);
                mapAd = StringManager.getFirstMap(temp);
                Log.i("tzy", "needVideoControl time = " + System.currentTimeMillis());
                if (mapAd != null && mapAd.size() > 0
                        && mVideoPlayerController != null) {
                    mVideoPlayerController.setShowAd(true);
                }
                if (isAutoPaly && mVideoPlayerController != null)
                    mVideoPlayerController.setOnClick();
            }
        }, activity, "result_media");

    }

    private int num = 4;
    private XHAllAdControl xhAllAdControl;

    /**
     * 处理广告展示
     *
     * @param map
     * @param view
     */
    private void setVideoAdData(final Map<String, String> map, final View view) {
        xhAllAdControl.onAdBind(0, view, "");
        final TextView mNum = (TextView) view.findViewById(R.id.ad_gdt_video_num);
        final ImageView mImageView = (ImageView) view.findViewById(R.id.ad_video_img);
        String imgUrl = null;
        if (map.containsKey("imgUrl")) imgUrl = map.get("imgUrl");
        if (TextUtils.isEmpty(imgUrl)) return;

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
                AppCommon.openUrl(activity, StringManager.getVipUrl(true) + "&vipFrom=视频贴片广告会员免广告", true);
            }
        });

        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(0, "");
            }
        });
        //初始化倒计时
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mNum.setText("" + msg.what);
                if (msg.what == 0) {
                    view.setVisibility(View.GONE);
                    if (!mVideoPlayerController.isPlaying()) {
                        mVideoPlayerController.setShowAd(false);
                        if (isOnResuming) mVideoPlayerController.setOnClick();
                    }
                }
            }
        };
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
                            for (; num > 0; num--) {
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

    boolean isContinue = false;
    boolean isHaspause = false;
    long currentTime = 0;
    int limitTime = 0;
    private boolean setSelfVideo(final String title, final String selfVideoJson, final String img, Map<String, String> permissionMap) {
        boolean isUrlVaild = false;
        tongjiId = "a_menu_detail_video";
        Map<String, String> selfVideoMap = UtilString.getListMapByJson(selfVideoJson).get(0);
        String videoUrl = selfVideoMap.get("url");
        if (!TextUtils.isEmpty(videoUrl) && videoUrl.startsWith("http")) {
            dishVidioLayout.setPadding(0, distance, 0, 0);
            mVideoPlayerController = new VideoPlayerController(activity, dishVidioLayout, img);
            mVideoPlayerController.showFullScrren();
            if(permissionMap != null && permissionMap.containsKey("video")){

                Map<String,String> videoPermionMap = StringManager.getFirstMap(permissionMap.get("video"));
                Map<String,String> commonMap = StringManager.getFirstMap(videoPermionMap.get("common"));
                Map<String,String> timeMap = StringManager.getFirstMap(videoPermionMap.get("fields"));
                if(!TextUtils.isEmpty(timeMap.get("time"))){
                    limitTime = Integer.parseInt(timeMap.get("time"));
                    Log.i("tzy","limitTime = " + limitTime);
                    setVipPermision(commonMap);
                }
            }else{
                isContinue = true;
                isHaspause = false;
                dredgeVipLayout.setVisibility(GONE);
            }
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
                    setVideoAdData(mapAd, adLayout);
                }
            });
            dishvideo_img.setVisibility(View.GONE);
            callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, videoViewGroup);
            callBack.videoImageOnClick();
            isUrlVaild = true;
        }
        initVideoAd();
        return isUrlVaild;
    }

    private RelativeLayout dredgeVipLayout;
    private VideoDredgeVipView vipView;

    private void setVipPermision(final Map<String, String> common){
        if(!StringManager.getBooleanByEqualsValue(common,"isShow")){
            Log.i("tzy","common = " + common.toString());
            final String url = common.get("url");
            if(TextUtils.isEmpty(url)) return;
            mVideoPlayerController.hideFullScreen();
            vipView = new VideoDredgeVipView(context);
            dredgeVipLayout.addView(vipView);
            vipView.setTipMessaText(common.get("text"));
            vipView.setDredgeVipText(common.get("button1"));
            vipView.setDredgeVipClick(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!TextUtils.isEmpty(url)){
                        AppCommon.openUrl(activity,url,true);
                        return;
                    }

                }
            });
            dredgeVipLayout.setPadding(0, distance, 0, 0);
            mVideoPlayerController.setOnProgressChangedCallback(new GSYVideoPlayer.OnProgressChangedCallback() {
                @Override
                public void onProgressChanged(int progress, int secProgress, int currentTime, int totalTime) {
                    int currentS = Math.round(currentTime / 1000f);
                    int durationS = Math.round(totalTime / 1000f);
                    if (currentS >= 0 && durationS >= 0) {
                        if (isHaspause) {
                            mVideoPlayerController.onPause();
                            return;
                        }
                        if ((currentS > limitTime
//                            || limitTime > durationS
                        ) && !isContinue) {
                            dredgeVipLayout.setVisibility(VISIBLE);
                            mVideoPlayerController.onPause();
                            isHaspause = true;
                        }
                    }
                }
            });
        }
    }


    /**
     * 展示顶图view,是大图还是视频
     * @param img          》图片链接
     */
    public void setImg(final String img) {
        Log.i("wyl","img:___:::"+img);
        isLoadImg=true;
        dishvideo_img.setVisibility(View.GONE);
        int waith = ToolsDevice.getWindowPx(activity).widthPixels *5/6;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams params_rela = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,waith);
        final ImageViewVideo imvv = new ImageViewVideo(activity);
        imvv.parseItemImg(ImageView.ScaleType.CENTER_CROP, img, false, false, R.drawable.i_nopic, FileManager.save_cache);
        imvv.setLayoutParams(params);
        dishVidioLayout.removeAllViews();
        dishVidioLayout.setLayoutParams(params_rela);
        dishVidioLayout.addView(imvv);
        dishVidioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dishVidioLayout.setClickable(false);
                XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "菜谱大图点击");
                ArrayList<Map<String, String>> listmap = new ArrayList<>();
                Map<String, String> map = new HashMap<>();
                map.put("img", img);
                map.put("info", "");
                map.put("num", "1");
                listmap.add(map);

                Intent intent = new Intent(activity, MoreImageShow.class);
                intent.putExtra("data", listmap);
                intent.putExtra("from", "dish");
                intent.putExtra("index", 0);
                intent.putExtra("isShowAd", false);
                dishVidioLayout.setClickable(true);
                activity.startActivity(intent);
            }
        });
        if(callBack!=null) {
            callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, videoViewGroup);
        }
    }

    public void setLoginStatus(){
        if(vipView != null)
            vipView.setLogin();
    }

    public void onResume() {
        isOnResuming = true;
        if(mVideoPlayerController != null
                && (dredgeVipLayout == null || dredgeVipLayout.getVisibility() == GONE))
            mVideoPlayerController.onResume();
    }

    public void onPause() {
        isOnResuming = false;
        if(mVideoPlayerController != null)
        mVideoPlayerController.onPause();
    }

    public boolean onBackPressed(){
        return mVideoPlayerController != null ? mVideoPlayerController.onBackPressed() : false;
    }

    public View getVideoView(){
        return videoViewGroup;
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

    /**
     * 展示view
     */
    public void showHeaderView(){
        this.setVisibility(VISIBLE);
    }

    /**
     * 不显示view
     */
    public void INVisibiHeaderView(){
        this.setVisibility(INVISIBLE);
    }

    private void handlerImage(String url){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);
        dishVidioLayout.addView(imageView);
        Log.i("wyl","处理图片：：："+url);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
                .load(url)
                .setSaveType(FileManager.save_cache)
                .build();
        if (bitmapRequest != null) {
            bitmapRequest.into(getTarget(imageView));
        }
    }
    public SubBitmapTarget getTarget(final ImageView v) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                if (v != null && bitmap != null) {
                    // 图片圆角和宽高适应
                    v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    UtilImage.setImgViewByWH(v, bitmap, 0, 0, false);
                    //当前显示动画
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                    alphaAnimation.setDuration(150);
                    v.setAnimation(alphaAnimation);
                }
            }
        };
    }
    /**
     * 页面销毁时调用
     */
    public void onDestroy(){
        if(mVideoPlayerController!=null){
            mVideoPlayerController.onDestroy();
//            mVideoPlayerController=null;
        }
    }

}
