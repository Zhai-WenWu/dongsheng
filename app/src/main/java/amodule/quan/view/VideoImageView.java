package amodule.quan.view;

import android.app.Activity;
import android.os.Environment;
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
import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.VDVideoViewListeners;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.xiangha.R;

import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.video.VideoApplication;
import xh.basic.tool.UtilFile;

/**
 * 视频播放业务层封装
 */
public class VideoImageView extends RelativeLayout{
    private Activity context;
    private ImageView image_bg,load_progress,image_btn_play;
    private RelativeLayout video_layout;
    private boolean isVoice=true;
    private VDVideoView vdVideoView;
    private VDVideoInfo videoInfo;
    private boolean isbottomView=true;
    private boolean newStart=true;
    private boolean mIsCycle = true;
    private String videoUrl="";
    private LinearLayout tipLayout;
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
//        FileManager.loadShared(context,)
        //直接播放
       String temp= (String) UtilFile.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
       if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
           setShowMedia(true);
        try {
            //视频解码库初始化
            VideoApplication.getInstence().initialize(context);
        } catch (Exception e) {
            LogManager.reportError("视频软解包初始化异常", e);
            return;
        } catch (Error e) {
            return;
        }
        LayoutInflater.from(context).inflate(R.layout.view_video_image,this,true);
        image_bg= (ImageView) findViewById(R.id.image_bg);
        image_btn_play= (ImageView) findViewById(R.id.image_btn_play);
        load_progress= (ImageView) findViewById(R.id.load_progress);
        video_layout= (RelativeLayout) findViewById(R.id.video_layout);
        TextView tipMessage= (TextView) findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        tipLayout= (LinearLayout) findViewById(R.id.tip_root);
        int height = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        tipLayout.setLayoutParams(layoutParams);
        //创建视频播放器
        if (vdVideoView == null) {
            vdVideoView = new VDVideoView(context);
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
        videoInfo = new VDVideoInfo(videoUrl);
        videoInfo.mTitle = "";

        handlerListener();
    }

    /**
     * 处理view初始化
     */
    private void handlerView(){
        vdVideoView.setLayers(R.array.video_layer);
        int height = (ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_30)) * 3 / 4;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        vdVideoView.setLayoutParams(layoutParams);
        ViewGroup viewParent = (ViewGroup) vdVideoView.getParent();
        if (viewParent != null)
            viewParent.removeAllViews();
        //处理数据
        video_layout.addView(vdVideoView);
//        if(view_Tip==null){
//            initView(context);
//            video_layout.addView(view_Tip);
//        }
        vdVideoView.setVDVideoViewContainer(video_layout);
    }

    /**
     * 处理video的监听
     */
    private void handlerListener(){

        VDVideoViewController controller = VDVideoViewController.getInstance(context);
        if (controller != null) {
            controller.addOnCompletionListener(new VDVideoViewListeners.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    if (mOnPlayingCompletionListener != null) {
                        mOnPlayingCompletionListener.onPlayingCompletion();
                    }
                }
            });
        }

        vdVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
            @Override
            public void onVDVideoCompletion(VDVideoInfo info, int status) {
                if (mIsCycle) {
                    vdVideoView.play(0);
                    onResume();
                    onStart();
                }
            }
        });
        image_bg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                onBegin();
                newStart=false;
            }
        });
        vdVideoView.setErrorListener(new VDVideoExtListeners.OnVDVideoErrorListener() {
            @Override
            public void onVDVideoError(VDVideoInfo vdVideoInfo, int i, int i1) {
            }
        });
        vdVideoView.setPlayerChangeListener(new VDVideoExtListeners.OnVDVideoPlayerChangeListener() {
            @Override
            public void OnVDVideoPlayerChangeSwitch(int i, long l) {
            }
        });
        //测试该事件不是每次都会被调用
        vdVideoView.setPreparedListener(new VDVideoExtListeners.OnVDVideoPreparedListener() {
            @Override
            public void onVDVideoPrepared(VDVideoInfo vdVideoInfo) {
                load_progress.clearAnimation();
                load_progress.setVisibility(View.GONE);
                image_bg.setVisibility(View.GONE);
                image_btn_play.setVisibility(View.GONE);
            }
        });
        vdVideoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoClickCallBack!=null)videoClickCallBack.setVideoClick();
            }
        });
        vdVideoView.setPlaylistListener(new VDVideoExtListeners.OnVDVideoPlaylistListener() {
            @Override
            public void onPlaylistClick(VDVideoInfo vdVideoInfo, int i) {
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
                        UtilFile.saveShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
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
                        UtilFile.saveShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
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
        if (vdVideoView != null) {
            vdVideoView.open(context, videoInfo);
            if (VideoApplication.initSuccess) {
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
                try {
                    VDVideoViewController controller1 = VDVideoViewController.getInstance(context);
                    if(controller1!=null)controller1.notifyHideTip();
                    vdVideoView.play(0);
                } catch (Exception e) {
                    Tools.showToast(getContext(), "视频解码库加载失败，请重试");
                    FileManager.delDirectoryOrFile(Environment.getDataDirectory() + "/data/com.xiangha/libs/");
                    return;
                }
                VDVideoViewController controller = VDVideoViewController.getInstance(getContext());
                if (controller != null) {
                    controller.resume();
                    controller.start();
                }
                //不使用进度动画
//                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_anim);
//                load_progress.startAnimation(animation);
//                load_progress.setVisibility(View.GONE);
//                image_bg.setVisibility(View.GONE);
//                image_btn_play.setVisibility(View.GONE);
//                tipLayout.setVisibility(View.GONE);
        } else {
            Tools.showToast(getContext(), "加载视频解码库中...");
        }
    }
    }

    /**
     * 暂停
     */
    public void onVideoPause(){
        if (vdVideoView != null) {
            load_progress.clearAnimation();
            load_progress.setVisibility(View.GONE);
            image_bg.setVisibility(View.VISIBLE);
            image_btn_play.setVisibility(View.VISIBLE);
            onPause();
        }
    }
    public void onResume(){
        if (vdVideoView != null)
            vdVideoView.onResume();
    }
    public void onStart(){
        if (vdVideoView != null)
            vdVideoView.onStart();

    }
    public void onPause() {
        if (vdVideoView != null)
            vdVideoView.onPause();
    }

    public void onStop() {
        if (vdVideoView != null) {
            vdVideoView.onStop();
        }
    }

    protected void onDestroy() {
        if (vdVideoView != null)
            vdVideoView.release(false);
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
       return vdVideoView.getIsPlaying();
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
