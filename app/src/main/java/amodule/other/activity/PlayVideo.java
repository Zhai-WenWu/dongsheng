package amodule.other.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.CPUTool;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.video.VideoApplication;

public class PlayVideo extends BaseActivity {

    private String url, img, name;
    private LinearLayout dishVidioLayout;
    private VDVideoView mVDVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        initActivity(name, 2, 0, 0, R.layout.a_other_play_video);
        init();
    }

    private void init() {
//        initStatusBar();
        findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo.this.onBackPressed();
            }
        });

        //设置播放界面16:9
        dishVidioLayout = (LinearLayout) findViewById(R.id.video_layout);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dishVidioLayout.getLayoutParams();
        params.height = Tools.getPhoneHeight();
        params.width = Tools.getPhoneWidth();
        dishVidioLayout.setLayoutParams(params);
        intiVideoView();
        startPlay();
    }

    /**
     * 初始化状态栏
     */
    private void initStatusBar() {
        if (Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);
            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.bar_title);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }

    private void intiVideoView() {
        if (ToolsDevice.isNetworkAvailable(this)) {
            try {
                VideoApplication.getInstence().initialize(this);
            } catch (Exception e) {
                statisticsInitVideoError(this);
            }
        }
        mVDVideoView = new VDVideoView(this);
        mVDVideoView.setLayers(R.array.my_videoview_layers);

        mVDVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
            @Override
            public void onVDVideoCompletion(VDVideoInfo info, int status) {
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        dishVidioLayout.addView(mVDVideoView, layoutParams);
        mVDVideoView.setVDVideoViewContainer(dishVidioLayout);
    }

    private void startPlay() {

        if (!ToolsDevice.isNetworkAvailable(this)) {
            Tools.showToast(this, "网络故障，请检查网络是否可用");
            return;
        }

        if (TextUtils.isEmpty(url)) {
            Tools.showToast(this, "视频地址为空");
            return;
        }

        if (!url.startsWith("http")) {
            Tools.showToast(this, "视频地址错误");
            return;
        }

        if (VideoApplication.getInstence().isInitSuccess()) {
            try {
                VDVideoInfo videoInfo = new VDVideoInfo(url);
                videoInfo.mTitle = "";
                mVDVideoView.open(this, videoInfo);
                mVDVideoView.play(0);
            } catch (Exception e) {
                Tools.showToast(this, "视频解码库加载失败，请重试");
                FileManager.delDirectoryOrFile(Environment.getDataDirectory() + "/data/com.xiangha/libs/");
                VideoApplication.getInstence().initialize(this);
                return;
            }
        } else {
            Tools.showToast(this, "加载视频解码库中...");
            VideoApplication.getInstence().initialize(this);
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mVDVideoView != null
                && !mVDVideoView.onVDKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVDVideoView == null)
            return;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mVDVideoView.setIsFullScreen(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVDVideoView.setIsFullScreen(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVDVideoView != null) {
            mVDVideoView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVDVideoView != null) {
            mVDVideoView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVDVideoView != null) {
            mVDVideoView.release(false);
        }
    }

    //统计视频初始化错误
    private void statisticsInitVideoError(Context context) {
        XHClick.mapStat(context, "init_video_error", "CPU型号", "" + CPUTool.getCpuName());
        XHClick.mapStat(context, "init_video_error", "手机型号", android.os.Build.BRAND + "_" + android.os.Build.MODEL);
    }
}