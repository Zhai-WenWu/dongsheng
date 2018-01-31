package com.shuyu.gsyvideoplayer.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.GSYTextureView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.NetInfoModule;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Description :
 * PackageName : com.shuyu.gsyvideoplayer.video
 * Created by mrtrying on 2018/1/24 14:14:49.
 * e_mail : ztanzeyu@gmail.com
 */
public class CleanVideoPlayer extends FrameLayout implements GSYMediaPlayerListener, TextureView.SurfaceTextureListener {
    public static final String TAG = "CleanVideo";

    public static final int CURRENT_STATE_NORMAL = 0; //正常
    public static final int CURRENT_STATE_PREPAREING = 1; //准备中
    public static final int CURRENT_STATE_PLAYING = 2; //播放中
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3; //开始缓冲
    public static final int CURRENT_STATE_PAUSE = 5; //暂停
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6; //自动播放结束
    public static final int CURRENT_STATE_ERROR = 7; //错误状态

    public static boolean IF_RELEASE_WHEN_ON_PAUSE = true;

    protected int mCurrentState = -1; //当前的播放状态

    protected Context mContext;

    protected String mOriginUrl; //原来的url

    protected long mPauseTime; //保存暂停时的时间

    protected long mCurrentPosition; //当前的播放位置

    protected NetInfoModule mNetInfoModule;
    protected boolean mNetChanged = false; //是否发送了网络改变
    protected String mNetSate = "NORMAL";

    protected GSYTextureView mTextureView;

    protected Surface mSurface;

    private Handler mHandler = new Handler();
    protected static int mBackUpPlayingBufferState = -1;
    protected int mPlayPosition = -22; //播放的"tzy"，防止错误，因为普通的url也可能重复
    protected ViewGroup mTextureViewContainer; //渲染控件父类
    private RelativeLayout mAdLayout;
    protected AudioManager mAudioManager; //音频焦点的监听
    protected GSYVideoManager mGSYVideoManager;
    protected VideoAllCallBack mVideoAllCallBack;

    protected Timer updateProcessTimer;

    protected ProgressTimerTask mProgressTimerTask;

    protected OnProgressChangedCallback onProgressChangedCallback = null;//播放进度callback

    public CleanVideoPlayer(Context context) {
        super(context);
        mGSYVideoManager = new GSYVideoManager();
        initialize(context);
    }

    public CleanVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGSYVideoManager = new GSYVideoManager();
        initialize(context);
    }

    public CleanVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGSYVideoManager = new GSYVideoManager(null);
        initialize(context);
    }

    protected void initialize(Context context) {
        if (getActivityContext() != null) {
            this.mContext = getActivityContext();
        } else {
            this.mContext = context;
        }

        initInflate(mContext);

        mTextureViewContainer = (ViewGroup) findViewById(R.id.surface_container);
        mAdLayout = (RelativeLayout) findViewById(R.id.ad_container);

        mAdLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });

        mAudioManager = (AudioManager) getActivityContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        createNetWorkState();
        listenerNetWorkState();

    }

    public Context getActivityContext() {
        return CommonUtil.getActivityContext(getContext());
    }

    private void initInflate(Context context) {
        try {
            View.inflate(context, R.layout.video_layout_clean, this);
        } catch (InflateException e) {

        }
    }

    /**
     * 设置播放URL
     *
     * @param url 播放url
     * @return
     */
    public boolean setUp(String url) {
        mOriginUrl = url;
        mCurrentState = CURRENT_STATE_NORMAL;
        setStateAndUi(CURRENT_STATE_NORMAL);
        return true;
    }

    public void startPalyVideo(){
        if (mVideoAllCallBack != null && mCurrentState == CURRENT_STATE_NORMAL) {
            Debuger.printfLog("onClickStartIcon");
            mVideoAllCallBack.onClickStartIcon(mOriginUrl, "", CleanVideoPlayer.this);
        } else if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartError");
            mVideoAllCallBack.onClickStartError(mOriginUrl, "", CleanVideoPlayer.this);
        }
        prepareVideo();
    }

    /**
     * 开始状态视频播放
     */
    protected void prepareVideo() {
        Log.d("CleanVideo","prepareVideo");
        if (mGSYVideoManager.listener() != null) {
            mGSYVideoManager.listener().onCompletion();
        }
        mGSYVideoManager.setListener(this);
        mGSYVideoManager.setPlayTag(UUID.randomUUID().toString());
        mGSYVideoManager.setPlayPosition(mPlayPosition);
        addTextureView();
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBackUpPlayingBufferState = -1;
        mGSYVideoManager.prepare(mOriginUrl, new HashMap(), false, 1);
        Log.i("CleanVideo","prepareVideo");
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

    /**
     * 创建网络监听
     */
    protected void createNetWorkState() {
        if (mNetInfoModule == null) {
            mNetInfoModule = new NetInfoModule(getActivityContext().getApplicationContext(), new NetInfoModule.NetChangeListener() {
                @Override
                public void changed(String state) {
                    if (!mNetSate.equals(state)) {
                        Debuger.printfError("******* change network state ******* " + state);
                        mNetChanged = true;
                        Log.i(TAG, "******* change network state ******* " + state);
                    }
                    mNetSate = state;
                    handlerNetWorkState(getContext());
                }
            });
            mNetSate = mNetInfoModule.getCurrentConnectionType();
            currentType = getNetWorkState(getContext());
        }
    }

    /**
     * 监听网络状态
     */
    protected void listenerNetWorkState() {
        if (mNetInfoModule != null) {
            mNetInfoModule.onHostResume();
        }
    }

    /**
     * 取消网络监听
     */
    protected void unListenerNetWorkState() {
        if (mNetInfoModule != null) {
            mNetInfoModule.onHostPause();
        }
    }

    /**
     * 释放网络监听
     */
    protected void releaseNetWorkState() {
        if (mNetInfoModule != null) {
            mNetInfoModule.onHostPause();
            mNetInfoModule = null;
        }
    }

    /**
     * 添加播放的view
     */
    protected void addTextureView() {
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }
        mTextureView = null;
        mTextureView = new GSYTextureView(getContext());
        mTextureView.setGSYVideoManager(mGSYVideoManager);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setRotation(0);

        int params = getTextureParams();

        if (mTextureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mTextureViewContainer.addView(mTextureView, layoutParams);
        } else if (mTextureViewContainer instanceof FrameLayout) {
            LayoutParams layoutParams = new LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            mTextureViewContainer.addView(mTextureView, layoutParams);
        }
    }

    protected int getTextureParams() {
        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

    /**
     * 设置播放显示状态
     *
     * @param state
     */
    protected void setStateAndUi(int state) {
        mCurrentState = state;
        Log.i("CleanVideo", "video setStateAndUi :: mCurrentState = " + mCurrentState);
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                if (isCurrentMediaListener()) {
                    cancelProgressTimer();
                    mGSYVideoManager.releaseMediaPlayer();
                }
                if (mAudioManager != null) {
                    mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                }
                releaseNetWorkState();
                break;
            case CURRENT_STATE_PREPAREING:
                resetProgressAndTime();
                break;
            case CURRENT_STATE_PLAYING:
                startProgressTimer();
                break;
            case CURRENT_STATE_PAUSE:
                startProgressTimer();
                break;
            case CURRENT_STATE_ERROR:
                if (isCurrentMediaListener()) {
                    mGSYVideoManager.releaseMediaPlayer();
                }
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                cancelProgressTimer();
                completeProgressAndTime();
                break;
        }
    }

    /**
     * 监听是否有外部其他多媒体开始播放
     */
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("CleanVideo", "onAudioFocusChangeListener");
                            releaseAllVideos();
                        }
                    });
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        if (mGSYVideoManager.getMediaPlayer() != null
                                && mGSYVideoManager.getMediaPlayer().isPlaying()) {
                            mGSYVideoManager.getMediaPlayer().pause();
                        }
                    } catch (Exception e) {
                        onVideoReset();
                        e.printStackTrace();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public void releaseAllVideos() {
        Log.d("CleanVideo", "releaseAllVideos");
        if (IF_RELEASE_WHEN_ON_PAUSE) {
            if (mGSYVideoManager.listener() != null) {
                mGSYVideoManager.listener().onCompletion();
            }
            mGSYVideoManager.releaseMediaPlayer();
        } else {
            IF_RELEASE_WHEN_ON_PAUSE = true;
        }
    }

    /**
     * 重置
     */
    public void onVideoReset() {
        Log.i("CleanVideo", this.getClass().getSimpleName() + "::onVideoReset");
        setStateAndUi(CURRENT_STATE_NORMAL);
    }

    protected boolean isCurrentMediaListener() {
        return mGSYVideoManager.listener() != null
                && mGSYVideoManager.listener() == this;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        mGSYVideoManager.setDisplay(mSurface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mGSYVideoManager.setDisplay(null);
        surface.release();
        cancelProgressTimer();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onPrepared() {
        if (mCurrentState != CURRENT_STATE_PREPAREING) return;

        try{
            if (mGSYVideoManager.getMediaPlayer() != null) {
                mGSYVideoManager.getMediaPlayer().start();
            }

//            if (mGSYVideoManager.getMediaPlayer() != null && mSeekToInAdvance != -1) {
//                mGSYVideoManager.getMediaPlayer().seekTo(mSeekToInAdvance);
//                mSeekToInAdvance = -1;
//            }
        }catch (Exception e){
            onVideoReset();
            e.printStackTrace();
        }

        startProgressTimer();
        Log.i("CleanVideo","onPrepared");
        setStateAndUi(CURRENT_STATE_PLAYING);

        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onPrepared");
            mVideoAllCallBack.onPrepared(mOriginUrl, "", CleanVideoPlayer.this);
        }

        try{
//            if (mGSYVideoManager.getMediaPlayer() != null && mSeekOnStart > 0) {
//                mGSYVideoManager.getMediaPlayer().seekTo(mSeekOnStart);
//                mSeekOnStart = 0;
//            }
        }catch (Exception e){
            onVideoReset();
            e.printStackTrace();
        }


//        mHadPlay = true;
    }

    @Override
    public void onAutoCompletion() {
        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (mGSYVideoManager.lastListener() != null) {
            mGSYVideoManager.lastListener().onAutoCompletion();
        }
        mGSYVideoManager.setLastListener(null);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        releaseNetWorkState();
        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onAutoComplete");
            mVideoAllCallBack.onAutoComplete(mOriginUrl, "", CleanVideoPlayer.this);
        }
    }

    @Override
    public void onCompletion() {
//make me normal first
        Log.i("CleanVideo",this.getClass().getSimpleName() + "::onCompletion");
        setStateAndUi(CURRENT_STATE_NORMAL);
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (mGSYVideoManager.lastListener() != null) {
            mGSYVideoManager.lastListener().onCompletion();//回到上面的onAutoCompletion
        }
        mGSYVideoManager.setListener(null);
        mGSYVideoManager.setLastListener(null);
        mGSYVideoManager.setCurrentVideoHeight(0);
        mGSYVideoManager.setCurrentVideoWidth(0);

        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        releaseNetWorkState();
    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {
//        Log.i("tzy","onError");
//        if (mNetChanged) {
//            mNetChanged = false;
//            netWorkErrorLogic();
//            if (mVideoAllCallBack != null) {
//                mVideoAllCallBack.onPlayError(mOriginUrl, mTitle, GSYVideoPlayer.this);
//            }
//            return;
//        }

        if (what != 38 && what != -38) {
            setStateAndUi(CURRENT_STATE_ERROR);
//            deleteCacheFileWhenError();
            if (mVideoAllCallBack != null) {
                mVideoAllCallBack.onPlayError(mOriginUrl, "", CleanVideoPlayer.this);
            }
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        Log.d("CleanVideo","onInfo::what = " + what + " ; extra = " + extra);
//        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
//            mBackUpPlayingBufferState = mCurrentState;
//            Log.i("CleanVideo","onInfo::mBackUpPlayingBufferState = " + mBackUpPlayingBufferState);
//            Log.i("CleanVideo","onInfo::mHadPlay = " + mHadPlay);
//            //避免在onPrepared之前就进入了buffering，导致一只loading
//            if (mHadPlay && mCurrentState != CURRENT_STATE_PREPAREING && mCurrentState > 0)
//                setStateAndUi(CURRENT_STATE_PLAYING_BUFFERING_START);
//
//        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
//            if (mBackUpPlayingBufferState != -1) {
//
//                if (mHadPlay && mCurrentState != CURRENT_STATE_PREPAREING && mCurrentState > 0)
//                    setStateAndUi(mBackUpPlayingBufferState);
//
//                mBackUpPlayingBufferState = -1;
//            }
//        } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
//            mRotate = extra;
//            if (mTextureView != null)
//                mTextureView.setRotation(mRotate);
//        }
    }

    @Override
    public void onVideoSizeChanged() {
        int mVideoWidth = mGSYVideoManager.getCurrentVideoWidth();
        int mVideoHeight = mGSYVideoManager.getCurrentVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            mTextureView.requestLayout();
        }
    }

    @Override
    public void onBackFullscreen() {

    }

    @Override
    public void onVideoPause() {
        Log.i("CleanVideo","onVideoPause");
        try{
            if (mGSYVideoManager.getMediaPlayer() != null
                    && mGSYVideoManager.getMediaPlayer().isPlaying()) {
                setStateAndUi(CURRENT_STATE_PAUSE);
                mPauseTime = System.currentTimeMillis();
                mCurrentPosition = mGSYVideoManager.getMediaPlayer().getCurrentPosition();
                mGSYVideoManager.getMediaPlayer().pause();
            }
        }catch (Exception e){
            onVideoReset();
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoResume() {
        try{
            mPauseTime = 0;
            Log.i("CleanVideo","onVideoResume");
//            if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mGSYVideoManager.getMediaPlayer() != null
                    && !mGSYVideoManager.getMediaPlayer().isPlaying()) {
                if (mCurrentPosition > 0 && mGSYVideoManager.getMediaPlayer() != null) {
                    setStateAndUi(CURRENT_STATE_PLAYING);
                    mGSYVideoManager.getMediaPlayer().start();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mGSYVideoManager.getMediaPlayer().seekTo(mCurrentPosition);
                        }
                    } ,200);

                    Log.i("CleanVideo","mCurrentPosition = " + mCurrentPosition);
                }
            }
        }catch (Exception igored){
            onVideoReset();
            igored.printStackTrace();
            Log.i("CleanVideo","Exception = " + igored.getMessage());
        }
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public void release() {
        Log.d("tzy","release");
        if (isCurrentMediaListener()){
            releaseAllVideos();
            destoryListener();
        }
    }

    /* ------------------------------------------------------------------ ProgressTimerTask ------------------------------------------------------------------ */

    protected class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setTextAndProgress(0);
                    }
                });
            }
        }
    }

    protected void startProgressTimer() {
        cancelProgressTimer();
        updateProcessTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProcessTimer.schedule(mProgressTimerTask, 0, 300);
    }

    protected void cancelProgressTimer() {
        if (updateProcessTimer != null) {
            updateProcessTimer.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }

    }

    /**
     * 获取当前播放进度
     */
    public int getCurrentPositionWhenPlaying() {
        int position = 0;
        if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
            try {
                position = (int) mGSYVideoManager.getMediaPlayer().getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    /**
     * 获取当前总时长
     */
    public int getDuration() {
        int duration = 0;
        try {
            duration = (int) mGSYVideoManager.getMediaPlayer().getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    protected void setTextAndProgress(int secProgress) {
        int position = getCurrentPositionWhenPlaying();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        setProgressAndTime(progress, secProgress, position, duration);
    }

    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        if (null != onProgressChangedCallback)
            onProgressChangedCallback.onProgressChanged(progress, secProgress, currentTime, totalTime);
    }

    protected void resetProgressAndTime() {}

    protected void completeProgressAndTime(){}

    /* ------------------------------------------------------------------ 回调部分 ------------------------------------------------------------------ */
    public interface OnProgressChangedCallback {
        void onProgressChanged(int progress, int secProgress, int currentTime, int totalTime);
    }

    /* ------------------------------------------------------------------ Set & Get ------------------------------------------------------------------ */

    public OnProgressChangedCallback getOnProgressChangedCallback() {
        return onProgressChangedCallback;
    }

    public void setOnProgressChangedCallback(OnProgressChangedCallback onProgressChangedCallback) {
        this.onProgressChangedCallback = onProgressChangedCallback;
    }

    /**
     * 设置播放过程中的回调
     *
     * @param mVideoAllCallBack
     */
    public void setVideoAllCallBack(VideoAllCallBack mVideoAllCallBack) {
        this.mVideoAllCallBack = mVideoAllCallBack;
    }

    public int getPlayPosition() {
        return mPlayPosition;
    }

    /**
     * 设置播放位置防止错位
     */
    public void setPlayPosition(int playPosition) {
        this.mPlayPosition = playPosition;
    }


    public void setAdLayout(View view){
        if(mAdLayout != null && view != null){
            if(mAdLayout.getChildCount() > 0){
                mAdLayout.removeAllViews();
            }
            mAdLayout.addView(view);
        }
    }

    public RelativeLayout getAdLayout(){
        return  mAdLayout;
    }

    public final static String TYPE_WIFI = "wifi";
    public final static String TYPE_MOBILE = "mobile";
    public final static String TYPE_NOTHING = "null";

    private List<NetworkNotifyListener> mNotifyListeners = new ArrayList<>();
    private String currentType = "";

    /**
     * @param context
     *
     * @return
     */
    private String getNetWorkState(Context context) {
        String netWorkState = NetworkUtils.getNetWorkSimpleType(context);
        switch (netWorkState) {
            case TYPE_WIFI:
                return TYPE_WIFI;
            case TYPE_NOTHING:
                return TYPE_NOTHING;
            default:
                return TYPE_MOBILE;
        }
    }

    public void handlerNetWorkState(Context context) {
        NetworkInfo.State wifiState = null;
        NetworkInfo.State mobileState = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.w(TAG, "onReceive -- ConnectivityManager is null!");
            return;
        }
        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            wifiState = networkInfo.getState();
        }
        try {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            mobileState = networkInfo.getState();
        }
        Log.d(TAG, "onReceive -- wifiState = " + wifiState + " -- mobileState = " + mobileState);
        Log.d(TAG, "currentType = " + currentType);
        if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
            Log.d(TAG, "onReceive -- 手机网络连接成功");
            // 手机网络连接成功
            if (!TYPE_MOBILE.equals(currentType))
                mobileNotify();
            currentType = TYPE_MOBILE;
        } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState
                && NetworkInfo.State.CONNECTED != mobileState) {
            Log.d(TAG, "onReceive -- 手机没有任何的网络");
            // 手机没有任何的网络
            if (!TYPE_NOTHING.equals(currentType))
                nothingNotify();
            currentType = TYPE_NOTHING;
        } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
            Log.d(TAG, "onReceive -- 无线网络连接成功");
            // 无线网络连接成功
            if (!TYPE_WIFI.equals(currentType))
                wifiNotify();
            currentType = TYPE_WIFI;
        }
    }

    public void wifiNotify() {
        Log.d("tzy", "wifiNotify");
//        Log.d("tzy", "mNotifyListeners = " + mNotifyListeners.toString());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.wifiConnected();
        }
    }

    public void mobileNotify() {
        Log.d("tzy", "mobileNotify");
//        Log.d("tzy", "mNotifyListeners = " + mNotifyListeners.toString());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.mobileConnected();
        }
    }

    public void nothingNotify() {
        Log.d("tzy", "nothingNotify");
//        Log.d("tzy", "mNotifyListeners = " + mNotifyListeners.toString());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.nothingConnected();
        }
    }

    public void addListener(NetworkNotifyListener listener) {
        mNotifyListeners.add(listener);
    }

    public void removeListener(NetworkNotifyListener listener) {
        mNotifyListeners.remove(listener);
    }

    public void destoryListener() {
        mNotifyListeners.clear();
    }

    public interface NetworkNotifyListener {

        void wifiConnected();

        void mobileConnected();

        void nothingConnected();
    }
}
