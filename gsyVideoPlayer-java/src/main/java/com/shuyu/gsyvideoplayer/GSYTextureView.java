package com.shuyu.gsyvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;

/**
 * 用于显示video的，做了横屏与竖屏的匹配，还有需要rotation需求的
 * Created by shuyu on 2016/11/11.
 */

public class GSYTextureView extends TextureView {

    private MeasureHelper measureHelper;

    private GSYVideoManager mGSYVideoManager;

    public GSYTextureView(Context context) {
        super(context);
        init();
    }

    public GSYTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        measureHelper = new MeasureHelper(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int videoWidth = mGSYVideoManager.getCurrentVideoWidth();
        int videoHeight = mGSYVideoManager.getCurrentVideoHeight();

        int videoSarNum = 0,videoSarDen = 0;
        if(mGSYVideoManager.getMediaPlayer() != null){
            videoSarNum = mGSYVideoManager.getMediaPlayer().getVideoSarNum();
            videoSarDen = mGSYVideoManager.getMediaPlayer().getVideoSarDen();
        }

        if (videoWidth > 0 && videoHeight > 0) {
            measureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            measureHelper.setVideoSize(videoWidth, videoHeight);
        }
        measureHelper.setVideoRotation((int)getRotation());
        measureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }

    public int getSizeH() {
        return measureHelper.getMeasuredHeight();
    }

    public int getSizeW() {
        return measureHelper.getMeasuredWidth();
    }

    public GSYVideoManager getGSYVideoManager() {
        return mGSYVideoManager;
    }

    public void setGSYVideoManager(GSYVideoManager GSYVideoManager) {
        mGSYVideoManager = GSYVideoManager;
    }
}