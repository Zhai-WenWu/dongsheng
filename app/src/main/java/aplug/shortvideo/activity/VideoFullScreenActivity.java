package aplug.shortvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.VDVideoViewListeners;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.LogManager;
import acore.tools.Tools;
import third.video.VideoApplication;

/**
 * PackageName : aplug.shortvideo.activity
 * Created by MrTrying on 2016/9/27 11:35.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoFullScreenActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_VIDEO_URL = "video_url";
    public static final String EXTRA_VIDEO_TYPE = "video_type";
    public static final String LOCAL_VIDEO = "local";
    public static final String NET_VIDEO = "net";
    /**视频播放器*/
    VDVideoView mVDVideoView = null;
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

        if(LOCAL_VIDEO.equals(videoType)){
            mVideoView = (VideoView) findViewById(R.id.video_view);
            mVideoView.setVideoPath(videoUrl);
            mVideoView.start();
            //静音
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mVideoView.resume();
                    mVideoView.start();
                }
            });

        }else if(NET_VIDEO.equals(videoType)){
            //视频解码库初始化
            try {
                VideoApplication.getInstence().initialize(this);
            } catch (Exception e) {
                Tools.showToast(this, "视频初始化异常");
                LogManager.reportError("视频软解包初始化异常", e);
                finish();
                return;
            } catch (Error e) {
                Tools.showToast(this, "视频初始化异常");
                finish();
                return;
            }
            //初始化view
            initVideoView();
            //设置video数据
            setVideInfo();
        }

        //设置click
        rl.setOnClickListener(this);
    }

    /**初始化video UI*/
    private void initVideoView() {
        mVDVideoView = new VDVideoView(this);
        mVDVideoView.setLayers(R.array.video_layer);
        mVDVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
            @Override
            public void onVDVideoCompletion(VDVideoInfo info, int status) {
                VDVideoViewController controller = VDVideoViewController.getInstance(VideoFullScreenActivity.this);
                if (controller != null) {
                    controller.resume();
                    controller.start();
                }
            }
        });

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rl.addView(mVDVideoView, layoutParams);
        mVDVideoView.setVDVideoViewContainer((ViewGroup) mVDVideoView.getParent());
    }

    /**设置video数据*/
    private void setVideInfo() {
        VDVideoInfo videoInfo = new VDVideoInfo(videoUrl);
        videoInfo.mTitle = "";
        mVDVideoView.open(this, videoInfo);
        mVDVideoView.play(0);
        //播放
        final VDVideoViewController controller = VDVideoViewController.getInstance(this);
        if (controller != null) {
            controller.resume();
            controller.start();
            controller.addOnCompletionListener(new VDVideoViewListeners.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    controller.start();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //还原
        VDVideoViewController controller = VDVideoViewController.getInstance(this);
        if (controller != null) {
            controller.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停
        VDVideoViewController controller = VDVideoViewController.getInstance(this);
        if (controller != null) {
            controller.onPause();
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.activityLayout:
                onBackPressed();
                break;
        }
    }
}
