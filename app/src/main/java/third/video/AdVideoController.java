package third.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.video.CleanVideoPlayer;
import com.xiangha.R;

import java.util.Map;

import acore.logic.AdVideoConfigTool;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.helper.XHActivityManager;
import acore.widget.TagTextView;
import amodule._common.utility.WidgetUtility;

import static com.shuyu.gsyvideoplayer.GSYVideoPlayer.CURRENT_STATE_PLAYING;

/**
 * Description :
 * PackageName : third.video
 * Created by mrtrying on 2018/1/25 10:02:29.
 * e_mail : ztanzeyu@gmail.com
 */
public class AdVideoController {

    private Context mContext;

    private View mAdView;
    private TextView mCountDownTv;
    private CleanVideoPlayer mAdVideoPlayer;

    private OnCompleteCallback mOnCompleteCallback;
    private OnStartCallback monStartCallback;
    private OnErrorCallback mOnErrorCallback;
    private CleanVideoPlayer.OnProgressChangedCallback mOnProgressChangedCallback;
    private CleanVideoPlayer.NetworkNotifyListener mNetworkNotifyListener;

    private AdVideoConfigTool mConfigTool;

    private final boolean isAvailable;
    private boolean isNetworkDisconnect = false;
    private boolean isComplete = false;
    private String currentVideo = "";
    private long startTime;

    public AdVideoController(@NonNull Context context) {
        startTime = System.currentTimeMillis();
        this.mContext = context;
        mConfigTool = AdVideoConfigTool.of();
        isAvailable = LoginManager.isShowAd()
                && mConfigTool.isOpen()
                && !TextUtils.isEmpty(mConfigTool.getVideoUrlOrPath());
        if (!isAvailable) {
            Log.i("tzy", "AdVideoController: " + (System.currentTimeMillis() - startTime));
            return;
        }
        //TODO            currentVideo = mConfigTool.getVideoUrlOrPath();
        currentVideo = "http://pic.ibaotu.com/00/12/51/78w888piCCJX.mp4";
        initVideoPlayer(context);
        createAdView(mConfigTool.getConfigMap());
        Log.i("tzy", "AdVideoController: " + (System.currentTimeMillis() - startTime));
    }

    @SuppressLint("SetTextI18n")
    private void initVideoPlayer(@NonNull Context context) {
        mAdVideoPlayer = new CleanVideoPlayer(context);
        mAdVideoPlayer.setVideoAllCallBack(new SampleListener() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                if (mAdView != null) {
                    mAdView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                isComplete = true;
                onPause();
                onDestroy();
                //播放完成
                if (mOnCompleteCallback != null) {
                    mOnCompleteCallback.onComplete();
                }
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                super.onPlayError(url, objects);
                //错误
                if (mOnErrorCallback != null) {
                    mOnErrorCallback.onError();
                }
            }
        });
        mAdVideoPlayer.setOnProgressChangedCallback(
                (progress, secProgress, currentTime, totalTime) -> {
                    //进度监听
                    if (mCountDownTv != null) {
                        final int time = (totalTime - currentTime) / 1000;
                        mCountDownTv.setText(String.valueOf(time) + "s");
                    }
                    if (mOnProgressChangedCallback != null) {
                        mOnProgressChangedCallback.onProgressChanged(progress, secProgress, currentTime, totalTime);
                    }
                }
        );
        mAdVideoPlayer.addListener(new CleanVideoPlayer.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                isNetworkDisconnect = false;
                if (isRemoteUrl()) {
                    onResume();
                    if (mNetworkNotifyListener != null) {
                        mNetworkNotifyListener.wifiConnected();
                    }
                }
            }

            @Override
            public void mobileConnected() {
                isNetworkDisconnect = false;
                if (isRemoteUrl()) {
                    onResume();
                    if (mNetworkNotifyListener != null) {
                        mNetworkNotifyListener.mobileConnected();
                    }
                }
            }

            @Override
            public void nothingConnected() {
                isNetworkDisconnect = true;
                if (isRemoteUrl()) {
                    onPause();
                    if (mNetworkNotifyListener != null) {
                        mNetworkNotifyListener.nothingConnected();
                    }
                }
            }
        });
        Log.i("tzy", "initVideoPlayer: " + (System.currentTimeMillis() - startTime));
    }

    private void createAdView(Map<String, String> configData) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(mContext).inflate(R.layout.ad_hint_layout, null);
        view.setVisibility(View.GONE);
        TagTextView vipLead = (TagTextView) view.findViewById(R.id.ad_vip_lead);
        TagTextView skipView = (TagTextView) view.findViewById(R.id.ad_skip);
        TagTextView seeDetailView = (TagTextView) view.findViewById(R.id.see_detail);
        mCountDownTv = (TextView) view.findViewById(R.id.ad_gdt_video_num);
        //设置去除vip点击
        AppCommon.setAdHintClick(XHActivityManager.getInstance().getCurrentActivity(), vipLead, null, 0, "");
        skipView.setOnClickListener(v -> sikp());
        seeDetailView.setOnClickListener(v -> AppCommon.openUrl(configData.get("clickUrl"), false));

        WidgetUtility.setTextToView(seeDetailView, configData.get("title"));
        setAdLayout(view);
        Log.i("tzy", "createAdView: " + (System.currentTimeMillis() - startTime));
    }

    public void setAdLayout(@NonNull View adView) {
        this.mAdView = adView;
        if (mAdVideoPlayer != null) {
            mAdVideoPlayer.setAdLayout(adView);
        }
    }

    public void sikp() {
        onPause();
        onDestroy();
        if (mOnCompleteCallback != null) {
            mOnCompleteCallback.onComplete();
        }
    }

    public int getDuration() {
        if (mAdVideoPlayer != null) {
            return mAdVideoPlayer.getDuration();
        }
        return -1;
    }

    /**
     * 是否正在播放
     *
     * @return 是否在播放
     */
    public boolean isPlaying() {
        return null != mAdVideoPlayer
                && CURRENT_STATE_PLAYING == mAdVideoPlayer.getCurrentState();
    }

    public void start() {
        if (null != mAdVideoPlayer && !TextUtils.isEmpty(currentVideo)) {
            mAdVideoPlayer.setUp(currentVideo);
            mAdVideoPlayer.startPalyVideo();
            if(monStartCallback!=null )monStartCallback.onStart(isRemoteUrl());
            Log.i("tzy", "start: " + (System.currentTimeMillis() - startTime));
        }else if (mOnErrorCallback != null) {
            mOnErrorCallback.onError();
        }
    }

    private boolean isRemoteUrl() {
        return !TextUtils.isEmpty(currentVideo) && currentVideo.startsWith("http");
    }

    public void onResume() {
        if (null != mAdVideoPlayer
                && !isComplete
                ) {
            mAdVideoPlayer.onVideoResume();
        }
    }

    public void onPause() {
        if (null != mAdVideoPlayer) {
            mAdVideoPlayer.onVideoPause();
        }
    }

    public void onDestroy() {
        if (null != mAdVideoPlayer) {
            //释放所有
            mAdVideoPlayer.setVideoAllCallBack(null);
            mAdVideoPlayer.release();
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    @Nullable
    public CleanVideoPlayer getAdVideoPlayer() {
        return mAdVideoPlayer;
    }

    public interface OnCompleteCallback {
        void onComplete();
    }

    public interface OnErrorCallback {
        void onError();
    }
    public interface OnStartCallback {
        void onStart(boolean isRemoteUrl);
    }

    public void setOnCompleteCallback(OnCompleteCallback onCompleteCallback) {
        mOnCompleteCallback = onCompleteCallback;
    }
    public void setOnStartCallback(OnStartCallback onStartCallback) {
        monStartCallback = onStartCallback;
    }

    public void setOnErrorCallback(OnErrorCallback onErrorCallback) {
        mOnErrorCallback = onErrorCallback;
    }

    public void setOnProgressChangedCallback(CleanVideoPlayer.OnProgressChangedCallback onProgressChangedCallback) {
        mOnProgressChangedCallback = onProgressChangedCallback;
    }

    public void setNetworkNotifyListener(CleanVideoPlayer.NetworkNotifyListener networkNotifyListener) {
        mNetworkNotifyListener = networkNotifyListener;
    }
}
