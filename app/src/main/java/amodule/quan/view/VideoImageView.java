package amodule.quan.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.xianghatest.R;

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;

import static com.shuyu.gsyvideoplayer.GSYVideoPlayer.CURRENT_STATE_PLAYING;

/**
 * 视频播放业务层封装
 */
public class VideoImageView extends RelativeLayout{
    private Activity context;
    private ImageView image_bg,load_progress,image_btn_play;
    private RelativeLayout video_layout;
    private boolean isVoice=true;
    private StandardGSYVideoPlayer videoPlayer;
    protected OrientationUtils orientationUtils;
    private boolean newStart=true;
    private boolean mIsCycle = true;
    private String videoUrl="";
    private LinearLayout tipLayout;
    public boolean isNetworkDisconnect = false;
    public int autoRetryCount = 0;

    public VideoImageView(Activity context) {
        super(context);
        this.context = context;
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
        tipLayout= (LinearLayout) findViewById(R.id.tip_root);
        initNormalTipLayout();
        int height = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        tipLayout.setLayoutParams(layoutParams);
        //创建视频播放器
        if (videoPlayer == null) {
            videoPlayer = new StandardGSYVideoPlayer(context);
            videoPlayer.getFullscreenButton().setVisibility(GONE);
            orientationUtils = new OrientationUtils(context,videoPlayer);
            videoPlayer.setStandardVideoAllCallBack(new SampleListener(){
                @Override
                public void onAutoComplete(String url, Object... objects) {
                    super.onAutoComplete(url, objects);
                    if (mIsCycle) {
                        videoPlayer.startPlayLogic();
                    }
                }

                @Override
                public void onPrepared(String url, Object... objects) {
                    super.onPrepared(url, objects);
                    setNetworkCallback();
                }

                @Override
                public void onEnterFullscreen(String url, Object... objects) {
                    super.onEnterFullscreen(url, objects);
                    orientationUtils.resolveByClick();
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    super.onQuitFullscreen(url, objects);
                    orientationUtils.resolveByClick();
                }
            });

            Resources resources = context.getResources();
            videoPlayer.setBottomProgressBarDrawable(resources.getDrawable(R.drawable.video_new_progress));
            videoPlayer.setDialogVolumeProgressBar(resources.getDrawable(R.drawable.video_new_volume_progress_bg));
            videoPlayer.setDialogProgressBar(resources.getDrawable(R.drawable.video_new_progress));
            videoPlayer.setBottomShowProgressBarDrawable(resources.getDrawable(R.drawable.video_new_seekbar_progress),
                    resources.getDrawable(R.drawable.video_new_seekbar_thumb));

            //是否可以滑动调整
            videoPlayer.setIsTouchWiget(false);
            videoPlayer.setIsTouchWigetFull(true);
            handlerView();
        }
    }

    private void initNormalTipLayout(){
        final TextView tipMessage= (TextView) findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        Button btnCloseTip = (Button) findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("继续播放");
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
    }

    private void initNoNetwork(){
        final TextView tipMessage= (TextView) findViewById(R.id.tipMessage);
        tipMessage.setText("网络未连接，请检查网络设置");
        Button btnCloseTip = (Button) findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("去设置");
        tipLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

    private void setNetworkCallback(){
        videoPlayer.addListener(new StandardGSYVideoPlayer.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                if(null != tipLayout){
                    tipLayout.setVisibility(GONE);
                }
                onResume();
            }

            @Override
            public void mobileConnected() {
                initNormalTipLayout();
                tipLayout.setVisibility(VISIBLE);
                onPause();
            }

            @Override
            public void nothingConnected() {
                initNoNetwork();
                tipLayout.setVisibility(VISIBLE);
                onPause();
                isNetworkDisconnect = true;
            }
        });
    }

    /**
     * 背景
     * @param imgUrl 图片链接
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
     * @param videoUrl 视频链接
     */
    public void setVideoData(String videoUrl){
        newStart=true;
        this.videoUrl=videoUrl;
        //视频播放信息info
        videoPlayer.setUp(videoUrl, false,"");

        handlerListener();
    }

    /**
     * 处理view初始化
     */
    private void handlerView(){
        int height = (ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        videoPlayer.setLayoutParams(layoutParams);
        ViewGroup viewParent = (ViewGroup) videoPlayer.getParent();
        if (viewParent != null)
            viewParent.removeAllViews();
        //处理数据
        video_layout.addView(videoPlayer);
    }

    /**
     * 处理video的监听
     */
    private void handlerListener(){

        //测试该事件不是每次都会被调用
        image_bg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBegin();
                newStart=false;
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
        if (videoPlayer != null) {
            videoPlayer.setUp(videoUrl,false,"");
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
            videoPlayer.startPlayLogic();
        }
    }

    /** 暂停 */
    public void onVideoPause(){
        if (videoPlayer!= null) {
            load_progress.clearAnimation();
            load_progress.setVisibility(View.GONE);
            image_bg.setVisibility(View.VISIBLE);
            image_btn_play.setVisibility(View.VISIBLE);
            onPause();
        }
    }
    public void onResume(){
        if (videoPlayer != null){
            videoPlayer.onVideoResume();
        }
    }

    public void onPause() {
        if (videoPlayer != null){
            videoPlayer.onVideoPause();
        }
    }

    public void onDestroy() {
        if (videoPlayer != null){
            videoPlayer.release();
        }
    }

    private VideoClickCallBack videoClickCallBack;
    public void setVideoClickCallBack(VideoClickCallBack videoClickCallBack){
        this.videoClickCallBack= videoClickCallBack;
    }
    /** 视频点击事件 */
    public interface  VideoClickCallBack{
        void setVideoClick();
    }
    public boolean getIsPlaying(){
        return null != videoPlayer && CURRENT_STATE_PLAYING==videoPlayer.getCurrentState();
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

    public void setVideoCycle(boolean cycle) {
        mIsCycle = cycle;
    }


}
