package aplug.shortvideo.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import com.xianghatest.R;
import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;

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
    StandardGSYVideoPlayer videoPlayer;
    /**视频地址*/
    private String videoUrl = "";
    private String videoType = "";
    private VideoView mVideoView;

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
        videoPlayer = (StandardGSYVideoPlayer) findViewById(R.id.video_player);
        Resources resources = getResources();
        videoPlayer.setBottomProgressBarDrawable(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setDialogVolumeProgressBar(resources.getDrawable(R.drawable.video_new_volume_progress_bg));
        videoPlayer.setDialogProgressBar(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setBottomShowProgressBarDrawable(resources.getDrawable(R.drawable.video_new_seekbar_progress),
                resources.getDrawable(R.drawable.video_new_seekbar_thumb));

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false);
        videoPlayer.setIsTouchWigetFull(true);
        videoPlayer.setUp(videoUrl, false,"");
        videoPlayer.getFullscreenButton().setVisibility(View.GONE);
        videoPlayer.startPlayLogic();
        videoPlayer.setOnClickListener(this);
        videoPlayer.findViewById(R.id.surface_container).setOnClickListener(this);
        videoPlayer.setStandardVideoAllCallBack(new SampleListener(){
            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                videoPlayer.startPlayLogic();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(videoPlayer != null){
            videoPlayer.onVideoResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停
        if(videoPlayer != null){
            videoPlayer.onVideoPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoPlayer != null){
            videoPlayer.release();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.activityLayout:
            case R.id.surface_container:
                onBackPressed();
                break;
        }
    }
}
