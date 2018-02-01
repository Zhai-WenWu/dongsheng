package amodule.dish.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
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
import com.shuyu.gsyvideoplayer.video.CleanVideoPlayer;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
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
import third.video.AdVideoController;
import third.video.VideoPlayerController;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDishWeb.tongjiId;
import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;

/**
 * Created by Administrator on 2016/9/21.
 */
public class DishHeaderViewNew extends LinearLayout {
    private Context context;
    private Activity activity;
    //头部的view
    private View videoViewGroup;
    private RelativeLayout dishVidioLayout,ad_type_video;
    private LinearLayout dishvideo_img ;

    private VideoPlayerController mVideoPlayerController = null;//视频控制器
    private DishHeaderVideoCallBack callBack;
    private FrameLayout adLayout;
    private Map<String, String> mapAd;//广告数据
    private boolean isAutoPaly = false;//默认自动播放
    private boolean isOnResuming = false;//默认自动播放

    private int distance;
    private boolean isLoadImg=false;
    private boolean mShowClingBtn;
    private String AdType="0";//0--无广告，1--图片广告，2--视频广告。
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
        distance = Tools.getDimen(activity, R.dimen.topbar_height) + Tools.getStatusBarHeight(getContext());
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        //大图处理
        videoViewGroup = LayoutInflater.from(activity).inflate(R.layout.view_dish_header_oneimage, null);
        dishVidioLayout = (RelativeLayout) videoViewGroup.findViewById(R.id.video_layout);
        dredgeVipLayout = (RelativeLayout) videoViewGroup.findViewById(R.id.video_dredge_vip_layout);
        dishvideo_img = (LinearLayout) videoViewGroup.findViewById(R.id.video_img_layout);
        adLayout = (FrameLayout) videoViewGroup.findViewById(R.id.video_ad_layout);
        ad_type_video= (RelativeLayout) videoViewGroup.findViewById(R.id.ad_type_video);
        paramsLayout(videoHeight);
        //处理简介
        //头部加载view
        this.addView(videoViewGroup);
//        INVisibiHeaderView();
    }
    public void paramsLayout(int videoHeight){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,videoHeight);
        dishVidioLayout.setLayoutParams(params);
        dredgeVipLayout.setLayoutParams(params);
        dishvideo_img.setLayoutParams(params);
        adLayout.setLayoutParams(params);
        ad_type_video.setLayoutParams(params);
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
            if(isAutoPaly)isAutoPaly = "2".equals(videoMap.get("isAutoPlay"));

            if ("2".equals(type) && !TextUtils.isEmpty(selfVideo) && !"[]".equals(selfVideo)) {
                if (!setSelfVideo(title, selfVideo, img, permissionMap))
                    Toast.makeText(context, "视频播放失败", Toast.LENGTH_SHORT).show();
            } else {
                if(isLoadImg) {
                    handlerImage(img);
                }else{
                    setImg(img,0);
                }

            }
//        } catch (Exception e) {
//            Toast.makeText(context, "视频播放失败111", Toast.LENGTH_SHORT).show();
//        }
    }

    private void initAdTypeImg() {
        adLayout.setPadding(0, distance, 0, 0);

        ArrayList<String> list = new ArrayList<>();
        String key = AdPlayIdConfig.DISH_MEDIA;
        list.add(key);

        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> maps) {
                String temp = maps.get(AdPlayIdConfig.DISH_MEDIA);
                mapAd = StringManager.getFirstMap(temp);
                if (mapAd != null && mapAd.size() > 0
                        && mVideoPlayerController != null) {
                    mVideoPlayerController.setShowAd(true);
                    AdType="1";
                }
                if (isAutoPaly && mVideoPlayerController != null && isShowActivity())
                    mVideoPlayerController.setOnClick();
            }
        }, activity, "result_media");

    }
    public void initVideoAd(){
        if(mVideoPlayerController != null){
            mVideoPlayerController.setShowAd(false);
        }
        if(!initAdTypeVideo()){
            initAdTypeImg();
        }
    }
    private AdVideoController adVideoController;
    private boolean initAdTypeVideo(){
        removeTipView();
        ad_type_video.setVisibility(View.GONE);
        if(adVideoController!=null){
            adVideoController.destroy();
        }
        adVideoController= new AdVideoController(context);
        Log.i("xianghaTag","initAdTypeVideo:::"+adVideoController.isAvailable()+"::"+(adVideoController.getAdVideoPlayer()!=null));
        if(adVideoController.isAvailable()&&adVideoController.getAdVideoPlayer()!=null){
            adVideoController.setStaticId("a_menu_detail_video");
            ad_type_video.addView(adVideoController.getAdVideoPlayer());
            if(mVideoPlayerController != null){
                mVideoPlayerController.setShowAd(true);
            }
            AdType="2";
            handleTypeVideoCallBack();
            return true;
        }else{
            return false;
        }
    }
    private void handleTypeVideoCallBack(){
        if (isAutoPaly && mVideoPlayerController != null && isShowActivity()) {
            mVideoPlayerController.setShowAd(true);
            if(!ToolsDevice.getNetActiveState(context)){//无网络
               return;
            }
            ad_type_video.setVisibility(View.VISIBLE);
            adVideoController.start();
        }
        adVideoController.setOnStartCallback(isRemoteUrl -> ad_type_video.setVisibility(View.VISIBLE));
        adVideoController.setOnCompleteCallback(this::preparePlayVideo);
        adVideoController.setOnErrorCallback(this::preparePlayVideo);
        adVideoController.setOnSikpCallback(()->{
            preparePlayVideo();
            XHClick.mapStat(activity,"a_menu_detail_video","视频广告","跳过");
        });
        adVideoController.setNetworkNotifyListener(new CleanVideoPlayer.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                removeTipView();
                int state=adVideoController.getVideoCurrentState();
                if(state<0||state>6) {
                    adVideoController.start();
                }else{
                    adVideoController.onResume();
                }
                isNetworkDisconnect = false;
            }
            @Override
            public void mobileConnected() {
                if(!"1".equals(FileManager.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI).toString())){
                    if(!isNetworkDisconnect){
                        removeTipView();
                        if(view_Tip==null){
                            initNoWIFIView(context);
                            ad_type_video.addView(view_Tip);
                        }
                        adVideoController.onPause();
                    }
                }else if(adVideoController.getAdVideoPlayer().getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PAUSE){
                    removeTipView();
                    adVideoController.onResume();
                }
                isNetworkDisconnect = false;
            }
            @Override
            public void nothingConnected() {
                removeTipView();
                if(view_Tip == null){
                    initNoNetwork(context);
                    ad_type_video.addView(view_Tip);
                }
                adVideoController.onPause();
                isNetworkDisconnect = true;
            }
        });
    }

    private void preparePlayVideo() {
        if(ad_type_video!=null) {
            ad_type_video.removeAllViews();
            ad_type_video.setVisibility(View.GONE);
        }
        if(mVideoPlayerController!=null){
            dishvideo_img.setVisibility(View.GONE);
            mVideoPlayerController.setShowAd(false);
            mVideoPlayerController.setOnClick();
        }
    }

    private void handlerVideoState(){
        ad_type_video.setVisibility(View.VISIBLE);
        if(ToolsDevice.getNetActiveState(context)) {
            int netType = ToolsDevice.getNetWorkSimpleNum(context);
            if(netType>1 &&
                    !"1".equals(FileManager.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI).toString())){
                adVideoController.onPause();
                removeTipView();
                if(view_Tip==null){
                    initNoWIFIView(context);
                    ad_type_video.addView(view_Tip);
                }
            }else{
                adVideoController.start();
            }
        }else{
            adVideoController.onPause();
            removeTipView();
            if(view_Tip==null){
                initNoNetwork(context);
                ad_type_video.addView(view_Tip);
            }

        }
    }
    private boolean  isShowActivity(){
        try {
            if ("amodule.dish.activity.DetailDish".equals(XHActivityManager.getInstance().getCurrentActivity().getComponentName().getClassName()))
                return true;
        }catch (Exception e){return false;}
        return false;
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

        View gdtIcon = view.findViewById(ID_AD_ICON_GDT);
        if(gdtIcon != null){
            gdtIcon.setVisibility(ADKEY_GDT.equals(map.get("type"))?VISIBLE:GONE);
        }

        view.findViewById(R.id.ad_gdt_video_hint_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(activity,"a_menu_detail_video","图文广告","跳过");
                view.setVisibility(View.GONE);
                if(mVideoPlayerController != null){
                    mVideoPlayerController.setShowAd(false);
                    mVideoPlayerController.setOnClick();
                }
            }
        });
        view.findViewById(R.id.ad_vip_lead).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(activity, StringManager.getVipUrl(true) + "&vipFrom=视频贴片广告会员免广告", true);
                XHClick.mapStat(activity,"a_menu_detail_video","图文广告","会员去广告");
            }
        });

        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(0, "");
            }
        });
        //初始化倒计时
        @SuppressLint("HandlerLeak")
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mNum.setText("" + msg.what);
                if (msg.what == 0) {
                    view.setVisibility(View.GONE);
                    if (mVideoPlayerController != null && !mVideoPlayerController.isPlaying()) {
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
    int limitTime = 0;
    private boolean setSelfVideo(final String title, final String selfVideoJson, final String img, Map<String, String> permissionMap) {
        Log.i("xianghaTag","setSelfVideo");
        boolean isUrlVaild = false;
        isContinue=false;
        isHaspause=false;
        if(dredgeVipLayout!=null){
            dredgeVipLayout.removeAllViews();
            dredgeVipLayout.setVisibility(View.GONE);
        }
        tongjiId = "a_menu_detail_video";
        Map<String, String> selfVideoMap = UtilString.getListMapByJson(selfVideoJson).get(0);
        String videoUrl = selfVideoMap.get("url");
        if (!TextUtils.isEmpty(videoUrl) && videoUrl.startsWith("http")) {
            dishVidioLayout.setPadding(0, distance, 0, 0);
            mVideoPlayerController = new VideoPlayerController(activity, dishVidioLayout, img);
            mVideoPlayerController.setStaticId("a_menu_detail_video");
            mVideoPlayerController.setOnVideoCanPlay(mOnVideoCanPlayCallback);
            mVideoPlayerController.showFullScrren();
            mVideoPlayerController.showClingBtn(mShowClingBtn);
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
                    if(!ToolsDevice.getNetActiveState(context)){//无网络
                        return;
                    }
                    if("1".equals(AdType)&&mapAd!=null){
                        setVideoAdData(mapAd, adLayout);
                    }else if("2".equals(AdType)&&adVideoController!=null){
                        if(adVideoController.isRemoteUrl()){
                            handlerVideoState();
                        }else{
                            adVideoController.start();
                        }
                    }

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

    public void handleVipState (boolean isVip) {
        mShowClingBtn = isVip;
        if (mVideoPlayerController != null)
            mVideoPlayerController.showClingBtn(mShowClingBtn);
    }

    private RelativeLayout dredgeVipLayout;
    private VideoDredgeVipView vipView;

    private void setVipPermision(final Map<String, String> common){
        if(!StringManager.getBooleanByEqualsValue(common,"isShow")){
//            Log.i("tzy","common = " + common.toString());
            final String url = common.get("url");
            if(TextUtils.isEmpty(url)) return;
            vipView = new VideoDredgeVipView(context);
            dredgeVipLayout.addView(vipView);
            vipView.setTipMessaText(common.get("text"));
            vipView.setDredgeVipText(common.get("button1"));
            vipView.setDredgeVipClick(v -> {
                if(!TextUtils.isEmpty(url)){
                    String currentUrl = url + "&vipFrom=" +Uri.encode(LoginManager.isLogin() ? "视频菜谱会员按钮（登录后）" : "视频菜谱会员按钮（登录前）");
                    AppCommon.openUrl(activity,currentUrl,true);
                }
            });
            dredgeVipLayout.setPadding(0, distance, 0, 0);
            if(mVideoPlayerController != null){
                mVideoPlayerController.hideFullScreen();
                mVideoPlayerController.setOnProgressChangedCallback((progress, secProgress, currentTime, totalTime) -> {
                    int currentS = Math.round(currentTime / 1000f);
                    int durationS = Math.round(totalTime / 1000f);
                    if (currentS >= 0 && durationS >= 0) {
                        if (isHaspause) {
                            onPause();
                            return;
                        }
                        if ((currentS > limitTime
//                            || limitTime > durationS
                        ) && !isContinue) {
                            dredgeVipLayout.setVisibility(VISIBLE);
                            onPause();
                            isHaspause = true;
                        }
                    }
                });
            }
        }
    }

  private String oneImgUrl="";
    /**
     * 展示顶图view,是大图还是视频
     * @param img          》图片链接
     */
    public void setImg(final String img,int height) {
        Log.i("wyl","img:___:::"+img);
        oneImgUrl=img;
        isLoadImg=true;
        dishvideo_img.setVisibility(View.GONE);
        int waith = height>0?height:ToolsDevice.getWindowPx(activity).widthPixels *5/6;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams params_rela = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,waith);
        final ImageViewVideo imvv = new ImageViewVideo(activity);
        imvv.parseItemImg(ImageView.ScaleType.CENTER_CROP, img, false, false, R.drawable.i_nopic, FileManager.save_cache);
        imvv.setLayoutParams(params);
        dishVidioLayout.removeAllViews();
        dishVidioLayout.setLayoutParams(params_rela);
        dishVidioLayout.addView(imvv);
        dishVidioLayout.setOnClickListener(v -> {
            dishVidioLayout.setClickable(false);
            XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "菜谱大图点击");
            ArrayList<Map<String, String>> listmap = new ArrayList<>();
            Map<String, String> map = new HashMap<>();
            map.put("img", oneImgUrl);
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
                && (dredgeVipLayout == null || dredgeVipLayout.getVisibility() == GONE)) {
            mVideoPlayerController.onResume();
            Log.i("xianghaTag","onResume:::header");
        }
        if(adVideoController!=null) {
            adVideoController.onResume();
        }
    }

    public void onPause() {
        isOnResuming = false;
        if(mVideoPlayerController != null)
        mVideoPlayerController.onPause();
        Log.i("xianghaTag","onPause:::header");
        if(adVideoController!=null) {
            adVideoController.onPause();
        }
    }

    public boolean onBackPressed(){
        return mVideoPlayerController != null ? mVideoPlayerController.onBackPressed() : false;
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
        oneImgUrl = url;
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
            mVideoPlayerController=null;
        }
        if(adVideoController!=null) {
            adVideoController.onDestroy();
            adVideoController=null;
        }
    }
    public void onReset(){
        if(mVideoPlayerController!=null){
            mVideoPlayerController.onReset();
        }
    }

    public String getVideoUrl() {
        return mVideoPlayerController == null ? null : mVideoPlayerController.getVideoUrl();
    }

    public int getPlayState() {
        if (mVideoPlayerController != null)
            return mVideoPlayerController.videoPlayer.getCurrentState();
        return -1;//状态的初始化
    }

    private VideoPlayerController.OnVideoCanPlayCallback mOnVideoCanPlayCallback;
    public void setOnVideoCanPlay(VideoPlayerController.OnVideoCanPlayCallback callback){
        mOnVideoCanPlayCallback = callback;
    }
    protected View view_Tip;
    public boolean isNetworkDisconnect = false;
    /**
     * 初始化
     * @param context 上下文
     */
    @SuppressLint({"SetTextI18n", "InflateParams"})
    protected void initNoWIFIView(Context context){
        ViewGroup.LayoutParams layoutParams= new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view_Tip=LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
        view_Tip.setLayoutParams(layoutParams);
        TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        Button btnCloseTip = (Button) view_Tip.findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("继续播放");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(onClickListener);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(onClickListener);
    }

    protected void initNoNetwork(Context context){
        ViewGroup.LayoutParams layoutParams= new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view_Tip=LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
        view_Tip.setLayoutParams(layoutParams);
        TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("网络未连接，请检查网络设置");
        Button btnCloseTip = (Button) view_Tip.findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("去设置");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(disconnectClick);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(disconnectClick);
    }
    private OnClickListener disconnectClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    };

    private OnClickListener onClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            removeTipView();
            int state=adVideoController.getVideoCurrentState();
            if(state<0||state>6){
                adVideoController.start();
            }else{
                adVideoController.onResume();
            }
            new Thread(() -> FileManager.saveShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1")).start();
        }
    };

    protected void removeTipView(){
        if(view_Tip!=null){
            ad_type_video.removeView(view_Tip);
            view_Tip=null;
        }
    }

}
