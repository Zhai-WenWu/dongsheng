package third.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import cn.fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import cn.fm.jiecao.jcvideoplayer_lib.JCNetworkBroadcastReceiver;
import cn.fm.jiecao.jcvideoplayer_lib.JCUtils;
import cn.fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import cn.fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;
import cn.fm.jiecao.jcvideoplayer_lib.CustomView.XHVideoPlayerStandard;

/**
 * 1、对象唯一的问题，
 * 2、为什么滑动没有闪屏
 */
public class VideoImagePlayerController {
    private Context mContext = null;
    private XHVideoPlayerStandard videoPlayerStandard;
    private ImageViewVideo mImageView = null;
    private boolean mHasVideoInfo = false;
    private int mVideoInfoRequestNumber = 0;
    private ViewGroup mPraentViewGroup = null;
    private StatisticsPlayCountCallback mStatisticsPlayCountCallback = null;
    private String videoUrl="";
    public boolean isNetworkDisconnect = false;
    public int autoRetryCount = 0;

    public VideoImagePlayerController(Context context, ViewGroup viewGroup, String imgUrl) {
        this.mContext = context;
        this.mPraentViewGroup = viewGroup;
        videoPlayerStandard = new XHVideoPlayerStandard(mContext);
        videoPlayerStandard.setIsHideTopContainer(true);
        videoPlayerStandard.fullscreenButton.setVisibility(View.VISIBLE);
        videoPlayerStandard.addNetworkNotifyListener(new JCNetworkBroadcastReceiver.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                if(null != view_Tip){
                    view_Tip.performClick();
                    FileManager.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"0");
                }
                onResume();
            }

            @Override
            public void mobileConnected() {
                if(view_Tip==null){
                    initView(mContext);
                    mPraentViewGroup.addView(view_Tip);
                }
                onPause();
            }

            @Override
            public void nothingConnected() {
                isNetworkDisconnect = true;
            }
        });
        videoPlayerStandard.setOnPlayErrorCallback(new JCVideoPlayerStandard.OnPlayErrorCallback() {
            @Override
            public boolean onError() {
                if(ToolsDevice.isNetworkAvailable(mContext)
                        && isNetworkDisconnect
                        && autoRetryCount < 3){
                    autoRetryCount++;
                    JCUtils.saveProgress(mContext,videoUrl,videoPlayerStandard.getCurrentPositionWhenPlaying());
                    videoPlayerStandard.startVideo();
                    return true;
                }
                return false;
            }
        });
        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(videoPlayerStandard);
        if (!TextUtils.isEmpty(imgUrl)) {
            if(view_Tip==null){
                initView(mContext);
                mPraentViewGroup.addView(view_Tip);
            }
            mImageView = new ImageViewVideo(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
            mImageView.parseItemImg(ScaleType.CENTER_CROP, imgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
            mImageView.setLayoutParams(params);
            mPraentViewGroup.addView(mImageView);
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnClick();
                }
            });
        }
        String temp= (String) FileManager.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
        if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
            setShowMedia(true);
        videoPlayerStandard.setOnPlayCompleteCallback(new XHVideoPlayerStandard.OnPlayCompleteCallback() {
            @Override
            public void onComplte() {
                Log.i("zhangyujian2","setCompletionListener::::");
                videoPlayerStandard.startButton.performClick();
            }
        });
    }

    /**
     * 外部点击事件
     */
    public void setOnClick() {
        if(TextUtils.isEmpty(videoUrl)){
            Tools.showToast(mContext,"转码中...请稍后再试");
        }
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(mContext));
        if (mHasVideoInfo) {
                if(isShowAd){
                    if(mediaViewCallBack!=null)mediaViewCallBack.onclick();
                    return;
                }
                Log.i("zhangyujian","isShowMedia:::"+isShowMedia);
                if(!isShowMedia){
                    Log.i("zhangyujian","isAutoPaly:::"+isAutoPaly);
                    if(isAutoPaly){//当前wifi
                        if(view_Tip!=null){
                            mPraentViewGroup.removeView(view_Tip);
                            view_Tip=null;
                        }
                    }else{
                        if(mImageView!=null){
                            mPraentViewGroup.removeView(mImageView);
                            mImageView=null;
                        }
                        return;
                    }
                }
                videoPlayerStandard.startButton.performClick();

                if(mImageView!=null)
                    mPraentViewGroup.removeView(mImageView);
                if(view_Tip!=null){
                    mPraentViewGroup.removeView(view_Tip);
                    view_Tip=null;
                }
                if (mStatisticsPlayCountCallback != null) {
                    mStatisticsPlayCountCallback.onStatistics();
                }
        } else {
            Tools.showToast(mContext, "努力获取视频信息中...");
        }

    }

    /**
     * 处理view显示
     */
    public void setOnStop(){
        onDestroy();
        mPraentViewGroup.removeView(mImageView);
        mPraentViewGroup.addView(mImageView);
    }

    /**
     * 初始化视频播放数据,直接使用url
     */
    public void initVideoView2( String url,String title) {
        this.videoUrl = url;
        videoPlayerStandard.setUp(url, JCVideoPlayer.SCREEN_LAYOUT_LIST);
        mHasVideoInfo = true;
    }

    public boolean onBackPressed(){
        return JCVideoPlayer.backPress();
    }

    public ImageViewVideo getVideoImageView() {
        return mImageView;
    }

    /**
     * 是否正在播放
     *
     * @return true：正在播放 ，false反之
     */
    public boolean isPlaying() {
        if (JCMediaManager.instance().mediaPlayer != null) {
            try{
                return JCMediaManager.instance().mediaPlayer.isPlaying();
            }catch (Exception e){
                return false;
            }
        }
        return false;
    }

    public void onResume() {
        if (mHasVideoInfo) {
            if (videoPlayerStandard != null) {
                JCMediaManager.instance().mediaPlayer.start();
                videoPlayerStandard.onStatePlaying();
            }
        }
    }

    public void onPause() {
        if (videoPlayerStandard != null) {
            JCMediaManager.instance().mediaPlayer.pause();
            videoPlayerStandard.onStatePause();
        }
    }

    public void onDestroy() {
        if(null != videoPlayerStandard)
        videoPlayerStandard.release();
        JCVideoPlayer.clearSavedProgress(mContext,videoUrl);
    }

    /**
     * 播放统计接口
     *
     * @author Administrator
     */
    public interface StatisticsPlayCountCallback {
        void onStatistics();
    }

    /**
     * 设置播放统计监听
     *
     * @param callback 回调
     */
    public void setStatisticsPlayCountCallback(StatisticsPlayCountCallback callback) {
        this.mStatisticsPlayCountCallback = callback;
    }

    //是否显示广告
    private boolean isShowAd=false;
    private View view_Tip;
    private boolean isAutoPaly = false;//是否是wifi状态
    private boolean isShowMedia = false;//true：直接播放，false,可以被其他因素控制

    private VideoPlayerController.MediaViewCallBack mediaViewCallBack;
    /**
     * 设置广告点击回调
     * @param mediaViewCallBack
     */
    public void setMediaViewCallBack(VideoPlayerController.MediaViewCallBack mediaViewCallBack){
        this.mediaViewCallBack= mediaViewCallBack;
    }
    public boolean isShowAd() {
        return isShowAd;
    }

    public void setShowAd(boolean showAd) {
        isShowAd = showAd;
    }

    /**
     * 初始化
     * @param context
     */
    private void initView(Context context){
        LayoutParams layoutParams= new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        view_Tip= LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
        view_Tip.setLayoutParams(layoutParams);
        final TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        final Button btnCloseTip = (Button) view_Tip.findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("继续播放");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(onClickListener);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(onClickListener);
    }
    private OnClickListener onClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            setShowMedia(true);
            setOnClick();
            FileManager.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
        }
    };

    public boolean isShowMedia() {
        return isShowMedia;
    }

    public void setShowMedia(boolean showMedia) {
        isShowMedia = showMedia;
    }
}
