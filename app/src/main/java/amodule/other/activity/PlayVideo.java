package amodule.other.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jiecaovideoplayer.CustomView.XHVideoPlayerStandard;

public class PlayVideo extends BaseAppCompatActivity {

    private String url, img, name;
    private XHVideoPlayerStandard myJCVideoPlayerStandard;
    boolean isBack = false;
    boolean onceStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            name = bundle.getString("name");
            url = getIntent().getStringExtra("url");
            img = getIntent().getStringExtra("img");
        } else {
            Tools.showToast(this, "此视频不存在");
            finish();
            return;
        }
        initActivity(name,2,0,0,R.layout.a_other_play_video);
        init();
    }

    private void init() {
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));

        findViewById(R.id.ll_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo.this.onBackPressed();
            }
        });

        intiVideoView();
    }

    private void intiVideoView() {
        myJCVideoPlayerStandard = ((XHVideoPlayerStandard) findViewById(R.id.jc_video));
        myJCVideoPlayerStandard.setUp(url, JCVideoPlayer.SCREEN_LAYOUT_NORMAL, "");
        JCVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        JCVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        myJCVideoPlayerStandard.startVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBack = false;
        if(!onceStart){
            JCMediaManager.instance().mediaPlayer.start();
            myJCVideoPlayerStandard.onStatePlaying();
        }
        onceStart = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isBack){
            Log.i("tzy","PlayVideo releaseAllVideos");
            JCVideoPlayer.releaseAllVideos();
            JCVideoPlayer.clearSavedProgress(this, null);
        }else{
            Log.i("tzy","PlayVideo mediaPlayer onPause");
            JCMediaManager.instance().mediaPlayer.pause();
            myJCVideoPlayerStandard.onStatePause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        isBack = true;
        super.onBackPressed();
    }
}