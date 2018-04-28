package third.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.video.CleanVideoPlayer;
import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AdVideoConfigTool;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
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

    private static final String XML_ADVIDEO = "advideo";
    private static final String XML_ADVIDEO_VIP = "advideovip";
    private static final String KEY_DATE = "date";
    private static final String KEY_COUNT = "count";


    private Context mContext;

    private View mAdView;
    private TextView mCountDownTv;
    private CleanVideoPlayer mAdVideoPlayer;

    private OnCompleteCallback mOnCompleteCallback;
    private OnStartCallback monStartCallback;
    private OnErrorCallback mOnErrorCallback;
    private CleanVideoPlayer.OnProgressChangedCallback mOnProgressChangedCallback;
    private CleanVideoPlayer.NetworkNotifyListener mNetworkNotifyListener;
    private CleanVideoPlayer.NetworkNotifyListener mInnerListener;
    private OnSikpCallback mOnSikpCallback;

    private AdVideoConfigTool mConfigTool;

    private boolean isAvailable;
    private boolean isNetworkDisconnect = false;
    private boolean isComplete = false;
    private String currentVideo = "";
    private long startTime;
    private String staticId = "";

    public AdVideoController(@NonNull Context context) {
        startTime = System.currentTimeMillis();
        this.mContext = context;
        mConfigTool = AdVideoConfigTool.of();
        setUpIsAvailable();
        if (!isAvailable) {
           //YLKLog.i("tzy", "AdVideoController: " + (System.currentTimeMillis() - startTime));
            return;
        }
        currentVideo = mConfigTool.getVideoUrlOrPath();
        initVideoPlayer(context);
        createAdView(mConfigTool.getConfigMap());
       //YLKLog.i("tzy", "AdVideoController: " + (System.currentTimeMillis() - startTime));
    }

    public void setUpIsAvailable() {
        isAvailable = mConfigTool.isOpen()
                && mConfigTool.getMaxCount() > getCurrentPlayCount()
                && !TextUtils.isEmpty(mConfigTool.getVideoUrlOrPath());
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
                complete();
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                super.onPlayError(url, objects);
                //错误
                excuteErrorCallback();
            }
        });
        mAdVideoPlayer.setOnProgressChangedCallback(
                (progress, secProgress, currentTime, totalTime) -> {
                    //进度监听
                    if(totalTime < currentTime){
                        complete();
                        return;
                    }
                    if (mCountDownTv != null) {
                        final int time = (totalTime - currentTime) / 1000;
                        mCountDownTv.setText(String.valueOf(time) + "s");
                    }
                    if (mOnProgressChangedCallback != null) {
                        mOnProgressChangedCallback.onProgressChanged(progress, secProgress, currentTime, totalTime);
                    }
                }
        );
        if (mInnerListener == null) {
            mInnerListener = new CleanVideoPlayer.NetworkNotifyListener() {
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
            };
            mAdVideoPlayer.addListener(mInnerListener);
        }
       //YLKLog.i("tzy", "initVideoPlayer: " + (System.currentTimeMillis() - startTime));
    }

    private void complete() {
        isComplete = true;
        destroy();
        //播放完成
        excuteCompleteCallback();
    }

    private void excuteErrorCallback() {
        if (mOnErrorCallback != null) {
            mOnErrorCallback.onError();
        }
    }

    private void excuteCompleteCallback() {
        if (mOnCompleteCallback != null) {
            mOnCompleteCallback.onComplete();
        }
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
        vipLead.setOnClickListener(
                v -> {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),
                            StringManager.getVipUrl(true) + "&vipFrom=视频贴片广告会员免广告", true);
                    if(!TextUtils.isEmpty(staticId)){
                        XHClick.mapStat(mContext,staticId,"视频广告","会员去广告");
                    }
                }
        );
        skipView.setOnClickListener(v -> sikp());
        seeDetailView.setOnClickListener(v -> {
            AppCommon.openUrl(configData.get("clickUrl"), false);
            if(!TextUtils.isEmpty(staticId)){
                XHClick.mapStat(mContext,staticId,"视频广告","查看商品详情");
            }
        });

        WidgetUtility.setTextToView(seeDetailView, configData.get("title"));
        setAdLayout(view);
       //YLKLog.i("tzy", "createAdView: " + (System.currentTimeMillis() - startTime));
    }

    public void setAdLayout(@NonNull View adView) {
        this.mAdView = adView;
        if (mAdVideoPlayer != null) {
            mAdVideoPlayer.setAdLayout(adView);
        }
    }

    public void sikp() {
        destroy();
        if (mOnSikpCallback != null) {
            mOnSikpCallback.onSkip();
        }
    }

    public void destroy() {
        onPause();
        onDestroy();
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
        //更新可用状态
        setUpIsAvailable();
        if(!isAvailable){
            if(mOnSikpCallback != null){
                mOnSikpCallback.onSkip();
            }
            return;
        }
        if (null != mAdVideoPlayer && !TextUtils.isEmpty(currentVideo)) {
            setUpDateAndCount();
            if(!TextUtils.isEmpty(staticId)){
                XHClick.mapStat(mContext,staticId,"视频广告","广告播放次数");
            }
            mAdVideoPlayer.setUp(currentVideo);
            mAdVideoPlayer.startPalyVideo();
            if (monStartCallback != null) monStartCallback.onStart(isRemoteUrl());
           //YLKLog.i("tzy", "start: " + (System.currentTimeMillis() - startTime));
        } else excuteErrorCallback();
    }

    public boolean isRemoteUrl() {
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

    public int getVideoCurrentState() {
        if (mAdVideoPlayer != null) {
            return mAdVideoPlayer.getCurrentState();
        }
        return -1;
    }

    /**
     * 获取当点已播放次数，若不是今天返回 0
     * @return
     */
    private int getCurrentPlayCount(){
        int currentPlayCount = 0;
        String lastDateValue = FileManager.loadShared(XHApplication.in(),getXMLName(),KEY_DATE).toString();
        String currentDataValue = Tools.getAssignTime("yyyyMMdd",0);
        if(TextUtils.equals(lastDateValue,currentDataValue)){
            currentPlayCount = loadCurrentPlayCount();
        }else{
            cleanData(XHApplication.in());
        }
        return currentPlayCount;
    }

    /**
     *
     */
    private void setUpDateAndCount(){
        int currentPlayCount = loadCurrentPlayCount();
        currentPlayCount ++;
        String currentDataValue = Tools.getAssignTime("yyyyMMdd",0);
       //YLKLog.i("isAvailable", "setUpDateAndCount: " + currentDataValue);
        Map<String,String> map = new HashMap<>();
        map.put(KEY_COUNT,String.valueOf(currentPlayCount));
        map.put(KEY_DATE,currentDataValue);
        FileManager.saveShared(XHApplication.in(),getXMLName(),map);
    }

    public static void cleanData(Context context){
        FileManager.saveShared(context,getXMLName(),KEY_COUNT,"0");
        String currentDataValue = Tools.getAssignTime("yyyyMMdd",0);
        FileManager.saveShared(context,getXMLName(),KEY_DATE,currentDataValue);
    }

    private int loadCurrentPlayCount() {
        String lastPlayCountValue = FileManager.loadShared(mContext,getXMLName(),KEY_COUNT).toString();
        if(TextUtils.isEmpty(lastPlayCountValue)){
            lastPlayCountValue = "0";
            FileManager.saveShared(mContext,getXMLName(),KEY_COUNT,lastPlayCountValue);
        }
        return Integer.parseInt(lastPlayCountValue);
    }

    public static String getXMLName(){
        return LoginManager.isVIP() ? XML_ADVIDEO_VIP : XML_ADVIDEO;
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

    public interface OnSikpCallback {
        void onSkip();
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

    public void setOnSikpCallback(OnSikpCallback onSikpCallback) {
        mOnSikpCallback = onSikpCallback;
    }

    public void setStaticId(String staticId) {
        this.staticId = staticId;
    }
}
