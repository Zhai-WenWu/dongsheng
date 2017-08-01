package aplug.shortvideo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;
import cn.fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import cn.fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import cn.fm.jiecao.jcvideoplayer_lib.CustomView.XHVideoPlayerStandard;

/**
 * PackageName : aplug.shortvideo.activity
 * Created by MrTrying on 2016/9/27 11:35.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoFullScreenActivity extends BaseAppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_VIDEO_URL = "video_url";
    public static final String EXTRA_VIDEO_TYPE = "video_type";
    public static final String LOCAL_VIDEO = "local";
    public static final String NET_VIDEO = "net";
    /**视频播放器*/
    XHVideoPlayerStandard videoPlayerStandard;
    /**视频地址*/
    private String videoUrl = "";
    private String videoType = "";
    private VideoView mVideoView;

    boolean isBack = false;
    boolean isOnceStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.video_full_screen);
        loadIntent();
        initVideo();
    }

    /**获取videoUrl*/
    private void loadIntent() {
        Intent intent = getIntent();
        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
        videoType = intent.getStringExtra(EXTRA_VIDEO_TYPE);
        if(TextUtils.isEmpty(videoType)){
            videoType = NET_VIDEO;
        }
    }

    /**初始化视频相关*/
    private void initVideo() {
        //判断videoUrl
        if (TextUtils.isEmpty(videoUrl)) {
            Tools.showToast(this, "视频信息异常");
            finish();
            return;
        }

//        if(LOCAL_VIDEO.equals(videoType)){
//            mVideoView = (VideoView) findViewById(R.id.video_view);
//            mVideoView.setVideoPath(videoUrl);
//            mVideoView.start();
//            //静音
//            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mVideoView.resume();
//                    mVideoView.start();
//                }
//            });
//
//        }else if(NET_VIDEO.equals(videoType)){
//            初始化view，并开始播放
            initVideoView();
//        }

        //设置click
        rl.setOnClickListener(this);
    }

    /**初始化video UI*/
    private void initVideoView() {
        Log.i("tzy",this.getClass().getSimpleName() + " : videoUrl = " + videoUrl);
        videoPlayerStandard = (XHVideoPlayerStandard) findViewById(R.id.jc_video_view);
        videoPlayerStandard.setUp(videoUrl, JCVideoPlayer.SCREEN_LAYOUT_NORMAL,"");
        videoPlayerStandard.fullscreenButton.setVisibility(View.GONE);
        videoPlayerStandard.startButton.performClick();
        videoPlayerStandard.setOnClickListener(this);
        videoPlayerStandard.findViewById(R.id.surface_container).setOnClickListener(this);
        videoPlayerStandard.findViewById(R.id.surface_container).setOnTouchListener(null);
        videoPlayerStandard.setOnPlayCompleteCallback(new XHVideoPlayerStandard.OnPlayCompleteCallback() {
            @Override
            public void onComplte() {
                videoPlayerStandard.startButton.performClick();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isOnceStart){
            JCMediaManager.instance().mediaPlayer.start();
            videoPlayerStandard.onStatePlaying();
        }
        isOnceStart = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停
        if(isBack){
            Log.i("tzy","PlayVideo releaseAllVideos");
            videoPlayerStandard.release();
            JCVideoPlayer.clearSavedProgress(this, null);
        }else{
            Log.i("tzy","PlayVideo mediaPlayer onPause");
            JCMediaManager.instance().mediaPlayer.pause();
            videoPlayerStandard.onStatePause();
        }
    }

    @Override
    public void onBackPressed() {
        if(JCVideoPlayer.backPress()){
            return;
        }
        isBack = true;
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.activityLayout:
            case R.id.jc_video_view:
            case R.id.surface_container:
                onBackPressed();
                break;
        }
    }
}
