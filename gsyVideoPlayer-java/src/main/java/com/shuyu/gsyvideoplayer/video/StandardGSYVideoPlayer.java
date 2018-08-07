package com.shuyu.gsyvideoplayer.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.shuyu.gsyvideoplayer.GSYTextureView;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.NetInfoModule;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import moe.codeest.enviews.ENDownloadView;
import moe.codeest.enviews.ENPlayView;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;


/**
 * 标准播放器
 * Created by shuyu on 2016/11/11.
 */

public class StandardGSYVideoPlayer extends GSYVideoPlayer {


    protected Timer mDismissControlViewTimer;

    protected ProgressBar mBottomProgressBar;

    private View mLoadingProgressBar;

    protected TextView mTitleTextView; //title

    protected RelativeLayout mThumbImageViewLayout;//封面父布局

    private View mThumbImageView; //封面

    protected Dialog mBrightnessDialog;

    protected TextView mBrightnessDialogTv;

    protected Dialog mVolumeDialog;

    protected ProgressBar mDialogVolumeProgressBar;

    protected StandardVideoAllCallBack mStandardVideoAllCallBack;//标准播放器的回调

    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    protected LockClickListener mLockClickListener;//点击锁屏的回调
    protected OnClickListener mClingClickListener;//投屏点击的回调

    protected Dialog mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;
    protected ImageView mLockScreen;
    protected ImageView mClingBtn;

    protected Drawable mBottomProgressDrawable;
    protected Drawable mBottomShowProgressDrawable;
    protected Drawable mBottomShowProgressThumbDrawable;
    protected Drawable mVolumeProgressDrawable;
    protected Drawable mDialogProgressBarDrawable;

    protected boolean mLockCurScreen;//锁定屏幕点击

    protected boolean mNeedLockFull;//是否需要锁定屏幕

    private boolean mThumbPlay;//是否点击封面播放

    private int mDialogProgressHighLightColor = -11;

    private int mDialogProgressNormalColor = -11;

    private int mDismissControlTime = 2000;

    private OnSeekToOverCallback mOnSeekToOverCallback;


    public void setStandardVideoAllCallBack(StandardVideoAllCallBack standardVideoAllCallBack) {
        this.mStandardVideoAllCallBack = standardVideoAllCallBack;
        setVideoAllCallBack(standardVideoAllCallBack);
    }

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public StandardGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public StandardGSYVideoPlayer(Context context) {
        super(context);
    }

    public StandardGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.bottom_progressbar);
        mTitleTextView = (TextView) findViewById(R.id.title);
        mThumbImageViewLayout = (RelativeLayout) findViewById(R.id.thumb);
        mLockScreen = (ImageView) findViewById(R.id.lock_screen);
        mClingBtn = (ImageView) findViewById(R.id.cling);

        mLoadingProgressBar = findViewById(R.id.loading);

        mThumbImageViewLayout.setVisibility(GONE);
        mThumbImageViewLayout.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        if (mThumbImageView != null && !mIfCurrentIsFullscreen) {
            mThumbImageViewLayout.removeAllViews();
            resolveThumbImage(mThumbImageView);
        }


        if (mBottomProgressDrawable != null) {
            mBottomProgressBar.setProgressDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressDrawable != null) {
            mProgressBar.setProgressDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressThumbDrawable != null) {
            mProgressBar.setThumb(mBottomShowProgressThumbDrawable);
        }

        if (mClingBtn != null)
            mClingBtn.setVisibility(GONE);
        mLockScreen.setVisibility(GONE);

        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.lock_screen) {
                    if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE ||
                            mCurrentState == CURRENT_STATE_ERROR) {
                        return;
                    }
                    lockTouchLogic();
                    if (mLockClickListener != null) {
                        mLockClickListener.onClick(v, mLockCurScreen);
                    }
                } else if (v.getId() == R.id.cling) {
                    if (mClingClickListener != null)
                        mClingClickListener.onClick(v);
                }
            }
        };

        mLockScreen.setOnClickListener(clickListener);
        if (mClingBtn != null)
            mClingBtn.setOnClickListener(clickListener);

    }

    /**
     * 创建网络监听
     */
    @Override
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

    @Override
    protected void releaseNetWorkState() {
        super.releaseNetWorkState();
        destoryListener();
    }

    @Override
    protected void addTextureView() {
        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }
        mTextureView = null;
        mTextureView = new GSYTextureView(getContext());
        mTextureView.setGSYVideoManager(mGSYVideoManager);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setRotation(mRotate);

        int widthParams = getTextureWidthParams();
        int heightParams = getTextureHeightParams();


        if (mTextureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(widthParams, heightParams);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mTextureViewContainer.addView(mTextureView, layoutParams);
        } else if (mTextureViewContainer instanceof FrameLayout) {
            LayoutParams layoutParams = new LayoutParams(widthParams, heightParams);
            layoutParams.gravity = Gravity.CENTER;
            mTextureViewContainer.addView(mTextureView, layoutParams);
        }
    }

    public int type = GSYVideoType.SCREEN_TYPE_DEFAULT;
    private int getTextureWidthParams() {
        switch (type){
            case GSYVideoType.SCREEN_TYPE_DEFAULT:
            case GSYVideoType.SCREEN_MATCH_WIDTH:
                return ViewGroup.LayoutParams.MATCH_PARENT;
            default:
                return ViewGroup.LayoutParams.WRAP_CONTENT;
        }
//        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
//        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

    private int getTextureHeightParams() {
        switch (type){
            case GSYVideoType.SCREEN_TYPE_DEFAULT:
                return ViewGroup.LayoutParams.MATCH_PARENT;
            case GSYVideoType.SCREEN_MATCH_WIDTH:
            default:
                return ViewGroup.LayoutParams.WRAP_CONTENT;
        }
//        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
//        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param title         title
     *
     * @return
     */
    @Override
    public boolean setUp(String url, boolean cacheWithPlay, String title) {
        return setUp(url, cacheWithPlay, (File) null, title);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param title         title
     *
     * @return
     */
    @Override
    public boolean setUp(String url, boolean cacheWithPlay, File cachePath, String title) {
        if (super.setUp(url, cacheWithPlay, cachePath, title)) {
            if (title != null) {
                mTitleTextView.setText(title);
            }
            if (mIfCurrentIsFullscreen) {
                mFullscreenButton.setImageResource(getShrinkImageRes());
            } else {
                mFullscreenButton.setImageResource(getEnlargeImageRes());
                mBackButton.setVisibility(View.GONE);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_standard;
    }

    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                changeUiToNormal();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_PREPAREING:
                changeUiToPrepareingShow();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PLAYING:
                changeUiToPlayingShow();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PAUSE:
                changeUiToPauseShow();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_ERROR:
                changeUiToError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                changeUiToCompleteShow();
                cancelDismissControlViewTimer();
                mBottomProgressBar.setProgress(100);
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiToPlayingBufferingShow();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
                    if (mChangePosition) {
                        int duration = getDuration();
                        int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
                        mBottomProgressBar.setProgress(progress);
                    }
                    if (!mChangePosition && !mChangeVolume && !mBrightness) {
                        onClickUiToggle();
                    }
                    break;
            }
        } else if (id == R.id.progress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    cancelDismissControlViewTimer();
                    break;
                case MotionEvent.ACTION_UP:
                    if(mOnSeekToOverCallback != null){
                        mOnSeekToOverCallback.onSeekToOver();
                    }
                    startDismissControlViewTimer();
                    break;
            }
        }

        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            return true;
        }

        return super.onTouch(v, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.thumb) {
            if (!mThumbPlay) {
                return;
            }
            if (TextUtils.isEmpty(mUrl)) {
                Toast.makeText(getActivityContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentState == CURRENT_STATE_NORMAL) {
                if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getActivityContext()) && mNeedShowWifiTip) {
                    showWifiDialog();
                    return;
                }
                startPlayLogic();
            } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                onClickUiToggle();
            }
        } else if (i == R.id.surface_container) {
            if (mStandardVideoAllCallBack != null && isCurrentMediaListener()) {
                if (mIfCurrentIsFullscreen) {
                    Debuger.printfLog("onClickBlankFullscreen");
                    mStandardVideoAllCallBack.onClickBlankFullscreen(mOriginUrl, mTitle, StandardGSYVideoPlayer.this);
                } else {
                    Debuger.printfLog("onClickBlank");
                    mStandardVideoAllCallBack.onClickBlank(mOriginUrl, mTitle, StandardGSYVideoPlayer.this);
                }
            }
            startDismissControlViewTimer();
        }
    }

    @Override
    public void showWifiDialog() {
        super.showWifiDialog();
        if (!NetworkUtils.isAvailable(mContext)) {
            Toast.makeText(mContext, getResources().getString(R.string.no_net), Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPlayLogic();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    @Override
    public void startPlayLogic() {
        if (mStandardVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb");
            mStandardVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, StandardGSYVideoPlayer.this);
        }
        prepareVideo();
        startDismissControlViewTimer();
    }

    @Override
    protected void onClickUiToggle() {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            mLockScreen.setVisibility(VISIBLE);
            return;
        }
        if (mCurrentState == CURRENT_STATE_PREPAREING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPrepareingClear();
            } else {
                changeUiToPrepareingShow();
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
                changeUiToPlayingShow();
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear();
            } else {
                changeUiToPauseShow();
            }
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToCompleteClear();
            } else {
                changeUiToCompleteShow();
            }
        } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (mBottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingBufferingClear();
            } else {
                changeUiToPlayingBufferingShow();
            }
        }
    }

    @Override
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime);
        if (progress != 0)
            mBottomProgressBar.setProgress(progress);
        if (secProgress != 0 && !mCacheFile)
            mBottomProgressBar.setSecondaryProgress(secProgress);
        mBottomProgressBar.invalidate();
    }

    @Override
    protected void resetProgressAndTime() {
        super.resetProgressAndTime();
        mBottomProgressBar.setProgress(0);
        mBottomProgressBar.setSecondaryProgress(0);
    }

    //Unified management Ui
    private void changeUiToNormal() {
        Debuger.printfLog("changeUiToNormal");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToPrepareingShow() {
        Debuger.printfLog("changeUiToPrepareingShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        onBottomContainerVisibilityChange(VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToPrepareingClear() {
        Debuger.printfLog("changeUiToPrepareingClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToPlayingShow() {
        Debuger.printfLog("changeUiToPlayingShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        onBottomContainerVisibilityChange(VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToPlayingClear() {
        Debuger.printfLog("changeUiToPlayingClear");
        changeUiToClear();
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.VISIBLE);
    }

    private void changeUiToPauseShow() {
        Debuger.printfLog("changeUiToPauseShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        onBottomContainerVisibilityChange(VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        //mCoverImageView.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
        updatePauseCover();
    }

    private void changeUiToPauseClear() {
        Debuger.printfLog("changeUiToPauseClear");
        changeUiToClear();
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.VISIBLE);
        updatePauseCover();
    }

    private void changeUiToPlayingBufferingShow() {
        Debuger.printfLog("changeUiToPlayingBufferingShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        onBottomContainerVisibilityChange(VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToPlayingBufferingClear() {
        Debuger.printfLog("changeUiToPlayingBufferingClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.VISIBLE);
        mLockScreen.setVisibility(GONE);
        updateStartImage();
    }

    private void changeUiToClear() {
        Debuger.printfLog("changeUiToClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility(GONE);
    }

    private void changeUiToCompleteShow() {
        Debuger.printfLog("changeUiToCompleteShow");
        mTopContainer.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        onBottomContainerVisibilityChange(VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToCompleteClear() {
        Debuger.printfLog("changeUiToCompleteClear");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.VISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.VISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    private void changeUiToError() {
        Debuger.printfLog("changeUiToError");
        mTopContainer.setVisibility(View.INVISIBLE);
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        mThumbImageViewLayout.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        mLockScreen.setVisibility((mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);
        updateStartImage();
    }

    protected void updateStartImage() {
        if (mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.video_click_pause_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.video_play_normal);
            } else {
                imageView.setImageResource(R.drawable.video_play_normal);
            }
        } else if (mStartButton instanceof ENPlayView) {
            ENPlayView enPlayView = (ENPlayView) mStartButton;
            enPlayView.setDuration(500);
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                enPlayView.play();
//                mStartButton.setImageResource(R.drawable.video_click_pause_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                enPlayView.pause();
//                mStartButton.setImageResource(R.drawable.video_click_error_selector);
            } else {
                enPlayView.pause();
//                mStartButton.setImageResource(R.drawable.video_play_normal);
            }
        }
    }


    private void updatePauseCover() {
        if ((mFullPauseBitmap == null || mFullPauseBitmap.isRecycled()) && mShowPauseCover) {
            try {
                initCover();
            } catch (Exception e) {
                e.printStackTrace();
                mFullPauseBitmap = null;
            }
        }
        //showPauseCover();
    }

    @Override
    @SuppressWarnings("ResourceType")
    protected void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(R.layout.video_progress_dialog, null);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            if (mDialogProgressBarDrawable != null) {
                mDialogProgressBar.setProgressDrawable(mDialogProgressBarDrawable);
            }
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = new Dialog(getActivityContext(), R.style.video_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            mProgressDialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
            mProgressDialog.getWindow().addFlags(32);
            mProgressDialog.getWindow().addFlags(16);
            mProgressDialog.getWindow().setLayout(getWidth(), getHeight());
            if (mDialogProgressNormalColor != -11) {
                mDialogTotalTime.setTextColor(mDialogProgressNormalColor);
            }
            if (mDialogProgressHighLightColor != -11) {
                mDialogSeekTime.setTextColor(mDialogProgressHighLightColor);
            }
            WindowManager.LayoutParams localLayoutParams = mProgressDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.width = getWidth();
            localLayoutParams.height = getHeight();
            int location[] = new int[2];
            getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            mProgressDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        if (totalTimeDuration > 0)
            mDialogProgressBar.setProgress(seekTimePosition * 100 / totalTimeDuration);
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.video_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.video_backward_icon);
        }

    }

    @Override
    protected void dismissProgressDialog() {
        super.dismissProgressDialog();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void showVolumeDialog(float deltaY, int volumePercent) {
        super.showVolumeDialog(deltaY, volumePercent);
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(R.layout.video_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            if (mVolumeProgressDrawable != null) {
                mDialogVolumeProgressBar.setProgressDrawable(mVolumeProgressDrawable);
            }
            mVolumeDialog = new Dialog(getActivityContext(), R.style.video_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            mVolumeDialog.getWindow().addFlags(8);
            mVolumeDialog.getWindow().addFlags(32);
            mVolumeDialog.getWindow().addFlags(16);
            mVolumeDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mVolumeDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            localLayoutParams.width = getWidth();
            localLayoutParams.height = getHeight();
            int location[] = new int[2];
            getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            mVolumeDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }

        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    protected void dismissVolumeDialog() {
        super.dismissVolumeDialog();
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
            mVolumeDialog = null;
        }
    }

    @Override
    protected void showBrightnessDialog(float percent) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(getActivityContext()).inflate(R.layout.video_brightness, null);
            mBrightnessDialogTv = (TextView) localView.findViewById(R.id.app_video_brightness);
            mBrightnessDialog = new Dialog(getActivityContext(), R.style.video_style_dialog_progress);
            mBrightnessDialog.setContentView(localView);
            mBrightnessDialog.getWindow().addFlags(8);
            mBrightnessDialog.getWindow().addFlags(32);
            mBrightnessDialog.getWindow().addFlags(16);
            mBrightnessDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mBrightnessDialog.getWindow().getAttributes();
            localLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            localLayoutParams.width = getWidth();
            localLayoutParams.height = getHeight();
            int location[] = new int[2];
            getLocationOnScreen(location);
            localLayoutParams.x = location[0];
            localLayoutParams.y = location[1];
            mBrightnessDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        if (mBrightnessDialogTv != null)
            mBrightnessDialogTv.setText((int) (percent * 100) + "%");
    }

    @Override
    protected void dismissBrightnessDialog() {
        super.dismissVolumeDialog();
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
            mBrightnessDialog = null;
        }
    }

    @Override
    protected void loopSetProgressAndTime() {
        super.loopSetProgressAndTime();
        mBottomProgressBar.setProgress(0);
    }


    @Override
    public void onBackFullscreen() {
        clearFullscreenLayout();
    }


    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        if (mLockCurScreen) {
            lockTouchLogic();
            mLockScreen.setVisibility(GONE);
        }
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
        if (mLockCurScreen) {
            lockTouchLogic();
            mLockScreen.setVisibility(GONE);
        }
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            StandardGSYVideoPlayer gsyVideoPlayer = (StandardGSYVideoPlayer) gsyBaseVideoPlayer;
            gsyVideoPlayer.setStandardVideoAllCallBack(mStandardVideoAllCallBack);
            gsyVideoPlayer.setLockClickListener(mLockClickListener);
            gsyVideoPlayer.setNeedLockFull(isNeedLockFull());
            initFullUI(gsyVideoPlayer);
            //比如你自定义了返回案件，但是因为返回按键底层已经设置了返回事件，所以你需要在这里重新增加的逻辑
        }
        return gsyBaseVideoPlayer;
    }


    @Override
    public GSYBaseVideoPlayer showSmallVideo(Point size, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.showSmallVideo(size, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            StandardGSYVideoPlayer gsyVideoPlayer = (StandardGSYVideoPlayer) gsyBaseVideoPlayer;
            gsyVideoPlayer.setIsTouchWiget(false);//小窗口不能点击
            gsyVideoPlayer.setStandardVideoAllCallBack(mStandardVideoAllCallBack);
        }
        return gsyBaseVideoPlayer;
    }

    @Override
    protected void setSmallVideoTextureView(View.OnTouchListener onTouchListener) {
        super.setSmallVideoTextureView(onTouchListener);
        //小窗口播放停止了也可以移动
        mThumbImageViewLayout.setOnTouchListener(onTouchListener);
    }

    /**
     * 处理锁屏屏幕触摸逻辑
     */
    private void lockTouchLogic() {
        if (mLockCurScreen) {
            mLockScreen.setImageResource(R.drawable.unlock);
            mLockCurScreen = false;
            if (mOrientationUtils != null)
                mOrientationUtils.setEnable(mRotateViewAuto);
        } else {
            mLockScreen.setImageResource(R.drawable.lock);
            mLockCurScreen = true;
            if (mOrientationUtils != null)
                mOrientationUtils.setEnable(false);
            hideAllWidget();
        }
    }

    /** 初始化为正常状态 */
    public void initUIState() {
        setStateAndUi(CURRENT_STATE_NORMAL);
    }

    public void setPortraitPlay(boolean isLandscapeVideo){
        if(mOrientationUtils != null){
            boolean isLand = mOrientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            if(mTextureView.getSizeH() > mTextureView.getSizeW() && isLand){
                mTextureView.setRotation(mTextureView.getRotation() - 90);
                mTextureView.requestLayout();
            }
        }
    }

    public void rotateVideo(){
        if (!mHadPlay) {
            return;
        }
        if ((mTextureView.getRotation() - mRotate) == 270) {
            mTextureView.setRotation(mRotate);
            mTextureView.requestLayout();
        } else {
            mTextureView.setRotation(mTextureView.getRotation() + 90);
            mTextureView.requestLayout();
        }
    }

    /** 全屏的UI逻辑 */
    private void initFullUI(StandardGSYVideoPlayer standardGSYVideoPlayer) {

        if (mBottomProgressDrawable != null) {
            standardGSYVideoPlayer.setBottomProgressBarDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressDrawable != null && mBottomShowProgressThumbDrawable != null) {
            standardGSYVideoPlayer.setBottomShowProgressBarDrawable(mBottomShowProgressDrawable,
                    mBottomShowProgressThumbDrawable);
        }

        if (mVolumeProgressDrawable != null) {
            standardGSYVideoPlayer.setDialogVolumeProgressBar(mVolumeProgressDrawable);
        }

        if (mDialogProgressBarDrawable != null) {
            standardGSYVideoPlayer.setDialogProgressBar(mDialogProgressBarDrawable);
        }

        if (mDialogProgressHighLightColor >= 0 && mDialogProgressNormalColor >= 0) {
            standardGSYVideoPlayer.setDialogProgressColor(mDialogProgressHighLightColor, mDialogProgressNormalColor);
        }
    }

    private void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        mDismissControlViewTimer = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        mDismissControlViewTimer.schedule(mDismissControlViewTimerTask, mDismissControlTime);
    }

    private void cancelDismissControlViewTimer() {
        if (mDismissControlViewTimer != null) {
            mDismissControlViewTimer.cancel();
            mDismissControlViewTimer = null;
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
            mDismissControlViewTimerTask = null;
        }

    }

    protected class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getActivityContext() != null) {
                    ((Activity) getActivityContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideAllWidget();
                            mLockScreen.setVisibility(GONE);
                            if (mHideKey && mIfCurrentIsFullscreen && mShowVKey) {
                                hideNavKey(mContext);
                            }
                        }
                    });
                }
            }
        }
    }

    public void hideAllWidget() {
        mBottomContainer.setVisibility(View.INVISIBLE);
        onBottomContainerVisibilityChange(INVISIBLE);
        mTopContainer.setVisibility(View.INVISIBLE);
        if (parentHandleBottomProgressBarEnable())
            mBottomProgressBar.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
    }

    private void resolveThumbImage(View thumb) {
        mThumbImageViewLayout.addView(thumb);
        ViewGroup.LayoutParams layoutParams = thumb.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        thumb.setLayoutParams(layoutParams);
    }

    /** 设置封面 */
    public void setThumbImageView(View view) {
        if (mThumbImageViewLayout != null) {
            mThumbImageView = view;
            resolveThumbImage(view);
        }
    }

    /** 清除封面 */
    public void clearThumbImageView() {
        if (mThumbImageViewLayout != null) {
            mThumbImageViewLayout.removeAllViews();
        }
    }

    /** 回去title */
    public TextView getTitleTextView() {
        return mTitleTextView;
    }


    /**
     * 底部进度条-弹出的
     */
    public void setBottomShowProgressBarDrawable(Drawable drawable, Drawable thumb) {
        mBottomShowProgressDrawable = drawable;
        mBottomShowProgressThumbDrawable = thumb;
        if (mProgressBar != null) {
            mProgressBar.setProgressDrawable(drawable);
            mProgressBar.setThumb(thumb);
        }
    }

    /** 底部进度条-非弹出 */
    public void setBottomProgressBarDrawable(Drawable drawable) {
        mBottomProgressDrawable = drawable;
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setProgressDrawable(drawable);
        }
    }

    /** 声音进度条 */
    public void setDialogVolumeProgressBar(Drawable drawable) {
        mVolumeProgressDrawable = drawable;
    }


    /** 中间进度条 */
    public void setDialogProgressBar(Drawable drawable) {
        mDialogProgressBarDrawable = drawable;
    }

    /** 中间进度条字体颜色 */
    public void setDialogProgressColor(int highLightColor, int normalColor) {
        mDialogProgressHighLightColor = highLightColor;
        mDialogProgressNormalColor = normalColor;
    }

    /** 是否点击封面可以播放 */
    public void setThumbPlay(boolean thumbPlay) {
        this.mThumbPlay = thumbPlay;
    }

    /** 封面布局 */
    public RelativeLayout getThumbImageViewLayout() {
        return mThumbImageViewLayout;
    }


    public boolean isNeedLockFull() {
        return mNeedLockFull;
    }

    /**
     * 是否需要全屏锁定屏幕功能
     * 如果单独使用请设置setIfCurrentIsFullscreen为true
     */
    public void setNeedLockFull(boolean needLoadFull) {
        this.mNeedLockFull = needLoadFull;
    }

    /** 锁屏点击 */
    public void setLockClickListener(LockClickListener lockClickListener) {
        this.mLockClickListener = lockClickListener;
    }

    public void setClingClickListener(OnClickListener clickListener) {
        this.mClingClickListener = clickListener;
    }

    public void showClingBtn(boolean show) {
        if (mClingBtn != null)
            mClingBtn.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 设置触摸显示控制ui的消失时间
     *
     * @param dismissControlTime 毫秒，默认2500
     */
    public void setDismissControlTime(int dismissControlTime) {
        this.mDismissControlTime = dismissControlTime;
    }

    public int getDismissControlTime() {
        return mDismissControlTime;
    }


    public final static String TYPE_WIFI = "wifi";
    public final static String TYPE_MOBILE = "mobile";
    public final static String TYPE_NOTHING = "null";

    public static final String TAG = "NetworkBroadcastReceiver";
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
        Log.d(TAG, "wifiNotify");
//        Log.d(TAG, "mNotifyListeners = " + mNotifyListeners.toString());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.wifiConnected();
        }
    }

    public void mobileNotify() {
        Log.d(TAG, "mobileNotify");
//        Log.d(TAG, "mNotifyListeners = " + mNotifyListeners.toString());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.mobileConnected();
        }
    }

    public void nothingNotify() {
        Log.d(TAG, "nothingNotify");
//        Log.d(TAG, "mNotifyListeners = " + mNotifyListeners.toString());
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

    protected boolean parentHandleBottomProgressBarEnable() {
        return true;
    }

    public interface NetworkNotifyListener {

        void wifiConnected();

        void mobileConnected();

        void nothingConnected();
    }

    public interface OnBottomContainerVisibilityChangeCallback{
        void onBottomContainerVisibilityChange(int visibility);
    }

    public interface OnSeekToOverCallback{
        void onSeekToOver();
    }

    private OnBottomContainerVisibilityChangeCallback mOnBottomContainerVisibilityChangeCallback;

    public void setOnBottomContainerVisibilityChangeCallback(OnBottomContainerVisibilityChangeCallback onBottomContainerVisibilityChangeCallback) {
        mOnBottomContainerVisibilityChangeCallback = onBottomContainerVisibilityChangeCallback;
    }

    private void onBottomContainerVisibilityChange(int visibility){
        if(mOnBottomContainerVisibilityChangeCallback != null){
            mOnBottomContainerVisibilityChangeCallback.onBottomContainerVisibilityChange(visibility);
        }
    }

    public void setOnSeekToOverCallback(OnSeekToOverCallback onSeekToOverCallback) {
        mOnSeekToOverCallback = onSeekToOverCallback;
    }
}
