package third.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.tools.CPUTool;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jiecaovideoplayer.CustomView.XHVideoPlayerStandard;
import xh.basic.tool.UtilFile;

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
    public boolean isError = false;
    private String videoUrl="";

    public VideoImagePlayerController(Context context, ViewGroup viewGroup, String imgUrl) {
        this.mContext = context;
        this.mPraentViewGroup = viewGroup;
        videoPlayerStandard = new XHVideoPlayerStandard(mContext);
        videoPlayerStandard.setIsHideTopContainer(true);
        Tools.setMute(context);


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
        String temp= (String) UtilFile.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
        if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
            setShowMedia(true);
        videoPlayerStandard.setOnPlayCompleteCallback(new XHVideoPlayerStandard.OnPlayCompleteCallback() {
            @Override
            public void onComplte() {
                Log.i("zhangyujian2","setCompletionListener::::");
                videoPlayerStandard.startVideo();
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
                videoPlayerStandard.startVideo();

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
     * @return
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
        JCVideoPlayer.releaseAllVideos();
        JCVideoPlayer.clearSavedProgress(mContext,videoUrl);
    }

    /**
     * 播放统计接口
     *
     * @author Administrator
     */
    public interface StatisticsPlayCountCallback {
        public void onStatistics();
    }

    /**
     * 设置播放统计监听
     *
     * @param callback
     */
    public void setStatisticsPlayCountCallback(StatisticsPlayCountCallback callback) {
        this.mStatisticsPlayCountCallback = callback;
    }

    //统计视频初始化错误
    private void statisticsInitVideoError(Context context) {
        isError = true;
//		Tools.showToast(context, "您的手机暂时不支持播放视频");
        XHClick.mapStat(context, "init_video_error", "CPU型号", "" + CPUTool.getCpuName());
        XHClick.mapStat(context, "init_video_error", "手机型号", android.os.Build.BRAND + "_" + android.os.Build.MODEL);
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
        TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(onClickListener);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(onClickListener);
    }
    private OnClickListener onClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            setShowMedia(true);
            setOnClick();
            UtilFile.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
        }
    };

    public boolean isShowMedia() {
        return isShowMedia;
    }

    public void setShowMedia(boolean showMedia) {
        isShowMedia = showMedia;
    }
}
