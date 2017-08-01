package amodule.quan.view;

import android.app.Activity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCNetworkBroadcastReceiver;
import fm.jiecao.jcvideoplayer_lib.JCUtils;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;
import fm.jiecao.jiecaovideoplayer.CustomView.XHVideoPlayerStandard;
import xh.basic.tool.UtilFile;

/**
 * 视频播放业务层封装
 */
public class VideoImageView extends RelativeLayout{
    private Activity context;
    private ImageView image_bg,load_progress,image_btn_play;
    private RelativeLayout video_layout;
    private boolean isVoice=true;
    private XHVideoPlayerStandard videoPlayerStandard;
    private boolean isbottomView=true;
    private boolean newStart=true;
    private boolean mIsCycle = true;
    private String videoUrl="";
    private LinearLayout tipLayout;
    public boolean isNetworkDisconnect = false;
    public int autoRetryCount = 0;

    public VideoImageView(Activity context,boolean isbottomView) {
        super(context);
        this.context = context;
        this.isbottomView= isbottomView;
        initView();
    }

    public VideoImageView(Activity context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public VideoImageView(Activity context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        //直接播放
       String temp= (String) FileManager.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
       if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
           setShowMedia(true);
        LayoutInflater.from(context).inflate(R.layout.view_video_image,this,true);
        image_bg= (ImageView) findViewById(R.id.image_bg);
        image_btn_play= (ImageView) findViewById(R.id.image_btn_play);
        load_progress= (ImageView) findViewById(R.id.load_progress);
        video_layout= (RelativeLayout) findViewById(R.id.video_layout);
        final TextView tipMessage= (TextView) findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        tipLayout= (LinearLayout) findViewById(R.id.tip_root);
        int height = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        tipLayout.setLayoutParams(layoutParams);
        //创建视频播放器
        if (videoPlayerStandard == null) {
            videoPlayerStandard = new XHVideoPlayerStandard(context);
            videoPlayerStandard.setIsHideTopContainer(true);
            videoPlayerStandard.addNetworkNotifyListener(new JCNetworkBroadcastReceiver.NetworkNotifyListener() {
                @Override
                public void wifiConnected() {
                    if(null != tipLayout){
                        tipLayout.performClick();
                        FileManager.saveShared(getContext(),FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"0");
                    }
                    onResume();
                }

                @Override
                public void mobileConnected() {
                    onPause();
                    tipLayout.setVisibility(VISIBLE);
                }

                @Override
                public void nothingConnected() {
                    isNetworkDisconnect = true;
                }
            });
            videoPlayerStandard.setOnPlayErrorCallback(new JCVideoPlayerStandard.OnPlayErrorCallback() {
                @Override
                public boolean onError() {
                    if(ToolsDevice.isNetworkAvailable(getContext())
                            && isNetworkDisconnect
                            && autoRetryCount < 3){
                        autoRetryCount++;
                        JCUtils.saveProgress(getContext(),videoUrl,videoPlayerStandard.getCurrentPositionWhenPlaying());
                        videoPlayerStandard.startVideo();
                        return true;
                    }
                    return false;
                }
            });
            handlerView();
        }
    }

    /**
     * 背景
     * @param imgUrl
     */
    public void setImageBg(String imgUrl){
        int height = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        image_bg.setLayoutParams(layoutParams);
        Glide.with(context).load(imgUrl).into(image_bg);
        image_bg.setVisibility(View.VISIBLE);
        image_btn_play.setVisibility(View.VISIBLE);
    }

    /**
     *设置视频数据
     * @param videoUrl
     */
    public void setVideoData(String videoUrl){
        newStart=true;
        this.videoUrl=videoUrl;
        //视频播放信息info
        videoPlayerStandard.setUp(videoUrl, JCVideoPlayer.SCREEN_LAYOUT_LIST);
//        JCVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//        JCVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        handlerListener();
    }

    /**
     * 处理view初始化
     */
    private void handlerView(){
        int height = (ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        videoPlayerStandard.setLayoutParams(layoutParams);
        ViewGroup viewParent = (ViewGroup) videoPlayerStandard.getParent();
        if (viewParent != null)
            viewParent.removeAllViews();
        //处理数据
        video_layout.addView(videoPlayerStandard);
    }

    /**
     * 处理video的监听
     */
    private void handlerListener(){

        videoPlayerStandard.setOnPlayCompleteCallback(new XHVideoPlayerStandard.OnPlayCompleteCallback() {
            @Override
            public void onComplte() {
                if (mIsCycle) {
                    videoPlayerStandard.startButton.performClick();
                }
            }
        });
        videoPlayerStandard.fullscreenButton.setVisibility(GONE);
        //测试该事件不是每次都会被调用
        videoPlayerStandard.setOnPlayPreparedCallback(new XHVideoPlayerStandard.OnPlayPreparedCallback() {
            @Override
            public void onPrepared() {
                load_progress.clearAnimation();
                load_progress.setVisibility(View.GONE);
                image_bg.setVisibility(View.GONE);
                image_btn_play.setVisibility(View.GONE);
            }
        });
        image_bg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBegin();
                newStart=false;
            }
        });

        tipLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowMedia(true);
                onBegin();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileManager.saveShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
                    }
                }).start();

            }
        });
        findViewById(R.id.btnCloseTip).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowMedia(true);
                onBegin();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileManager.saveShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
                    }
                }).start();
            }
        });
    }

    /**
     * 开始播放
     */
    public void onBegin() {
        if(TextUtils.isEmpty(videoUrl)){
            Tools.showToast(context,"转码中...请稍后再试");
            return;
        }
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(context));
        if (videoPlayerStandard != null) {
            videoPlayerStandard.setUp(videoUrl,JCVideoPlayer.SCREEN_LAYOUT_LIST);
            load_progress.setVisibility(View.GONE);
            image_bg.setVisibility(View.GONE);
            image_btn_play.setVisibility(View.GONE);
            Log.i("zhangyujian","isShowMedia:::"+isShowMedia);
            if(!isShowMedia){
                Log.i("zhangyujian","isAutoPaly:::"+isAutoPaly);
                if(isAutoPaly){//当前wifi
                    tipLayout.setVisibility(View.GONE);
                }else{
                    tipLayout.setVisibility(View.VISIBLE);
                    return;
                }
            }else tipLayout.setVisibility(View.GONE);
            //TODO notifyHideTip 需要实现
            videoPlayerStandard.startButton.performClick();
            //不使用进度动画
//                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_anim);
//                load_progress.startAnimation(animation);
//                load_progress.setVisibility(View.GONE);
//                image_bg.setVisibility(View.GONE);
//                image_btn_play.setVisibility(View.GONE);
//                tipLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 暂停
     */
    public void onVideoPause(){
        if (videoPlayerStandard != null) {
            load_progress.clearAnimation();
            load_progress.setVisibility(View.GONE);
            image_bg.setVisibility(View.VISIBLE);
            image_btn_play.setVisibility(View.VISIBLE);
            onPause();
        }
    }
    public void onResume(){
        if (videoPlayerStandard != null){
            JCMediaManager.instance().mediaPlayer.start();
            videoPlayerStandard.onStatePlaying();
        }
    }

    public void onPause() {
        if (videoPlayerStandard != null){
            JCMediaManager.instance().mediaPlayer.pause();
            videoPlayerStandard.onStatePause();
        }
    }

    public void onDestroy() {
        if(videoPlayerStandard != null){
            videoPlayerStandard.release();
        }
        JCVideoPlayer.clearSavedProgress(context, null);
    }

    private VideoClickCallBack videoClickCallBack;
    public void setVideoClickCallBack(VideoClickCallBack videoClickCallBack){
        this.videoClickCallBack= videoClickCallBack;
    }
    /**
     * 视频点击事件
     */
    public interface  VideoClickCallBack{
        public void setVideoClick();
    }
    public boolean getIsPlaying(){
        if (JCMediaManager.instance().mediaPlayer != null) {
            try{
                return JCMediaManager.instance().mediaPlayer.isPlaying();
            }catch (IllegalStateException e){
                return false;
            }
        }
       return false;
    }

    //是否显示广告
    private boolean isShowAd=false;
    private boolean isAutoPaly = false;//是否是wifi状态
    private boolean isShowMedia = false;//true：直接播放，false,可以被其他因素控制

    public boolean isShowMedia() {
        return isShowMedia;
    }

    public void setShowMedia(boolean showMedia) {
        isShowMedia = showMedia;
    }

    public interface OnPlayingCompletionListener {
        void onPlayingCompletion();
    }
    private OnPlayingCompletionListener mOnPlayingCompletionListener;
    public void setOnPlayingCompletionListener(OnPlayingCompletionListener playingCompletionListener) {
        mOnPlayingCompletionListener = playingCompletionListener;
    }
    public void setVideoCycle(boolean cycle) {
        mIsCycle = cycle;
    }


}
