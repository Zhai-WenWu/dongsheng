package amodule.article.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.tools.FileToolsCammer;

/**
 * Created by sll on 2017/6/2.
 */

public class VideoPreviewActivity extends BaseActivity {
    private RelativeLayout mRootView;
    private RelativeLayout mVideoContainer;
    private VideoView mVideoView;
    private ImageView mVideoBack;

    private String mVideoPath;
    private int mSeekTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initActivity("", 2, 0, 0, R.layout.video_preview_layout);
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(MediaStore.Video.Media.DATA);
        initView();
        addListener();
        if (TextUtils.isEmpty(mVideoPath))
            return;
        mVideoContainer.setVisibility(View.VISIBLE);
        mVideoView.setVideoPath(mVideoPath);
    }

    private void initView() {
        mRootView = (RelativeLayout) findViewById(R.id.activityLayout);
        mVideoContainer = (RelativeLayout) findViewById(R.id.video_container);
        mVideoView = (VideoView) findViewById(R.id.article_pre_videoview);
        mVideoBack = (ImageView) findViewById(R.id.video_back);
    }

    private void addListener() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.activityLayout:
                    case R.id.video_back:
                        finish();
                        break;
                }
            }
        };
        mRootView.setOnClickListener(onClickListener);
        mVideoBack.setOnClickListener(onClickListener);

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!TextUtils.isEmpty(mVideoPath)) {
                    Intent intent = new Intent();
                    intent.putExtra(MediaStore.Video.Media.DATA, mVideoPath);
                    intent.putExtra(RecorderVideoData.video_img_path, FileToolsCammer.getImgPath(mVideoPath));
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mVideoPath)) {
            mVideoView.seekTo(mSeekTo);
            mVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
        mSeekTo = mVideoView.getCurrentPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }
}
