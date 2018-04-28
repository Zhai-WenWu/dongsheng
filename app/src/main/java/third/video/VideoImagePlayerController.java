package third.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.xiangha.R;
import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;

import static com.shuyu.gsyvideoplayer.GSYVideoPlayer.CURRENT_STATE_PLAYING;

/**
 * 1、对象唯一的问题，
 * 2、为什么滑动没有闪屏
 */
public class VideoImagePlayerController {
    private Context mContext = null;
    private StandardGSYVideoPlayer videoPlayer;
    private OrientationUtils orientationUtils;
    private ImageViewVideo mImageView = null;
    private boolean mHasVideoInfo = false;
    private int mVideoInfoRequestNumber = 0;
    private ViewGroup mPraentViewGroup = null;
    private StatisticsPlayCountCallback mStatisticsPlayCountCallback = null;
    private String videoUrl="";
    private String imgUrl="";
    public boolean isNetworkDisconnect = false;
    public int autoRetryCount = 0;
    public boolean isPortrait = false;

    public VideoImagePlayerController(Activity context, ViewGroup viewGroup, String imgUrl) {
        this.mContext = context;
        this.mPraentViewGroup = viewGroup;
        this.imgUrl = imgUrl;
        videoPlayer = new StandardGSYVideoPlayer(mContext);
        //设置旋转
        orientationUtils = new OrientationUtils(context, videoPlayer);
        orientationUtils.setEnable(false);
        orientationUtils.setRotateWithSystem(false);
        videoPlayer.setShowFullAnimation(true);
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                videoPlayer.startWindowFullscreen(mContext, true, true);
            }
        });
        videoPlayer.setNeedShowWifiTip(true);
        videoPlayer.setStandardVideoAllCallBack(new SampleListener(){

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                videoPlayer.startPlayLogic();
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                if(url.startsWith("http"))
                    setNetworkCallback();
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                if(!isPortrait)
                    orientationUtils.resolveByClick();
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                orientationUtils.backToProtVideo();
            }
        });


        Resources resources = mContext.getResources();
        videoPlayer.setBottomProgressBarDrawable(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setDialogVolumeProgressBar(resources.getDrawable(R.drawable.video_new_volume_progress_bg));
        videoPlayer.setDialogProgressBar(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setBottomShowProgressBarDrawable(resources.getDrawable(R.drawable.video_new_seekbar_progress),
                resources.getDrawable(R.drawable.video_new_seekbar_thumb));

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false);
        videoPlayer.setIsTouchWigetFull(true);

        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(videoPlayer);
        if (!TextUtils.isEmpty(imgUrl)) {
            if(view_Tip==null){
                initView(mContext);
                mPraentViewGroup.addView(view_Tip);
            }
            initImageView(context);
            mPraentViewGroup.addView(mImageView);
        }
        String temp= (String) FileManager.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
        if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
            setShowMedia(true);
    }

    private void initImageView(Context context){
        mImageView = new ImageViewVideo(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
        mImageView.parseItemImg(ScaleType.CENTER_CROP, imgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
        mImageView.setLayoutParams(params);
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setOnClick();
            }
        });
    }

    private void setNetworkCallback(){
        videoPlayer.addListener(new StandardGSYVideoPlayer.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                removeTipView();
                onResume();
                isNetworkDisconnect = false;
            }

            @Override
            public void mobileConnected() {
                if(!"1".equals(FileManager.loadShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI).toString())){
                    if(isNetworkDisconnect){
                        removeTipView();
                        if(view_Tip==null){
                            initView(mContext);
                            mPraentViewGroup.addView(view_Tip);
                        }
                        onPause();
                    }
                }else if(videoPlayer.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PAUSE){
                    removeTipView();
                    onResume();
                }
                isNetworkDisconnect = false;
            }

            @Override
            public void nothingConnected() {
                if(view_Tip == null){
                    initNoNetwork(mContext);
                    mPraentViewGroup.addView(view_Tip);
                }
                onPause();
                isNetworkDisconnect = true;
            }
        });
    }

    /**
     * 外部点击事件
     */
    public void setOnClick() {
        if(TextUtils.isEmpty(videoUrl)){
            Tools.showToast(mContext,"转码中...请稍后再试");
        }
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(mContext));
        if (mHasVideoInfo) {
                if(isShowAd){
                    if(mediaViewCallBack!=null)mediaViewCallBack.onclick();
                    return;
                }
               //YLKLog.i("zhangyujian","isShowMedia:::"+isShowMedia);
                if(!isShowMedia){
                   //YLKLog.i("zhangyujian","isAutoPaly:::"+isAutoPaly);
                    if(isAutoPaly){//当前wifi
                        removeTipView();
                    }else{
                        removeImaegView();
                        return;
                    }
                }
                videoPlayer.startPlayLogic();

                removeImaegView();
                removeTipView();
                if (mStatisticsPlayCountCallback != null) {
                    mStatisticsPlayCountCallback.onStatistics();
                }
        } else {
            Tools.showToast(mContext, "努力获取视频信息中...");
        }

    }

    protected void removeTipView(){
        if(view_Tip!=null){
            mPraentViewGroup.removeView(view_Tip);
            view_Tip=null;
        }
    }

    protected void removeImaegView(){
        if(mImageView!=null){
            mPraentViewGroup.removeView(mImageView);
//            mImageView=null;
        }
    }

    /**
     * 处理view显示
     */
    public void setOnStop(){
        onDestroy();
        if(mImageView == null){
            initImageView(mContext);
        }
        mPraentViewGroup.removeView(mImageView);
        mPraentViewGroup.addView(mImageView);
    }

    /**
     * 初始化视频播放数据,直接使用url
     */
    public void initVideoView2( String url,String title) {
        this.videoUrl = url;
        videoPlayer.setUp(url,false,"");
        mHasVideoInfo = true;
    }

    public boolean onBackPressed(){
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || isPortrait) {
            return videoPlayer != null ? videoPlayer.backFromWindowFull(mContext) : false;
        }
        return false;
    }

    public ImageViewVideo getVideoImageView() {
        return mImageView;
    }

    /**
     * 是否正在播放
     *
     * @return true：正在播放 ，false反之
     */
    public boolean isPlaying() {
        return null != videoPlayer && CURRENT_STATE_PLAYING==videoPlayer.getCurrentState();
    }

    public void onResume() {
        if (mHasVideoInfo && videoPlayer != null && !isNetworkDisconnect) {
            videoPlayer.onVideoResume();
        }
    }

    public void onPause() {
        if (videoPlayer != null) {
            videoPlayer.onVideoPause();
        }
    }

    public void onDestroy() {
        if(null != videoPlayer)
            videoPlayer.release();
    }

    /**
     * 播放统计接口
     *
     * @author Administrator
     */
    public interface StatisticsPlayCountCallback {
        void onStatistics();
    }

    /**
     * 设置播放统计监听
     *
     * @param callback 回调
     */
    public void setStatisticsPlayCountCallback(StatisticsPlayCountCallback callback) {
        this.mStatisticsPlayCountCallback = callback;
    }

    //是否显示广告
    private boolean isShowAd=false;
    private View view_Tip;
    private boolean isAutoPaly = false;//是否是wifi状态
    private boolean isShowMedia = false;//true：直接播放，false,可以被其他因素控制

    private VideoPlayerController.MediaViewCallBack mediaViewCallBack;
    /**
     * 设置广告点击回调
     * @param mediaViewCallBack
     */
    public void setMediaViewCallBack(VideoPlayerController.MediaViewCallBack mediaViewCallBack){
        this.mediaViewCallBack= mediaViewCallBack;
    }
    public boolean isShowAd() {
        return isShowAd;
    }

    public void setShowAd(boolean showAd) {
        isShowAd = showAd;
    }

    /**
     * 初始化
     * @param context
     */
    private void initView(Context context){
        LayoutParams layoutParams= new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        view_Tip= LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
        view_Tip.setLayoutParams(layoutParams);
        final TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        final Button btnCloseTip = (Button) view_Tip.findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("继续播放");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(onClickListener);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(onClickListener);
    }
    private OnClickListener onClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            setShowMedia(true);
            setOnClick();
            FileManager.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
        }
    };

    protected void initNoNetwork(Context context){
        LayoutParams layoutParams= new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
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
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    };

    public boolean isShowMedia() {
        return isShowMedia;
    }

    public void setShowMedia(boolean showMedia) {
        isShowMedia = showMedia;
    }
}
