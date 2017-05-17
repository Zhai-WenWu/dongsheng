package third.video;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.sina.sinavideo.sdk.utils.VDPlayerSoundManager;
import com.sina.sinavideo.sdk.widgets.VDVideoFullScreenButton;
import com.sina.sinavideo.sdk.widgets.VDVideoPlaySeekBar;
import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.CPUTool;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import xh.basic.tool.UtilFile;

/**
 * 1、对象唯一的问题，
 * 2、为什么滑动没有闪屏
 */
public class VideoImagePlayerController {
    private Context mContext = null;
    private VDVideoView mVDVideoView = null;
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
        try {
            //视频解码库初始化
            VideoApplication.getInstence().initialize(context);
        } catch (Exception e) {
            statisticsInitVideoError(context);
            LogManager.reportError("视频软解包初始化异常", e);
            return;
        } catch (Error e) {
            statisticsInitVideoError(context);
            return;
        }

        mVDVideoView = new VDVideoView(mContext);
        mVDVideoView.setLayers(R.array.my_videoview_layers);
        //去掉全屏按钮
        VDVideoFullScreenButton fullscreen1 = (VDVideoFullScreenButton) mVDVideoView.findViewById(R.id.fullscreen1);
        fullscreen1.setVisibility(View.GONE);

        VDVideoPlaySeekBar playerseek2 = (VDVideoPlaySeekBar) mVDVideoView.findViewById(R.id.playerseek2);
        //设置滑动条位置
        RelativeLayout.LayoutParams playerseekParam = (RelativeLayout.LayoutParams) playerseek2.getLayoutParams();
        playerseekParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        VDPlayerSoundManager.setMute(context, true, false);
        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(mVDVideoView);
        mVDVideoView.setVDVideoViewContainer((ViewGroup) mVDVideoView.getParent());
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
        mVDVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
            @Override
            public void onVDVideoCompletion(VDVideoInfo vdVideoInfo, int i) {
                Log.i("zhangyujian2","setCompletionListener::::");
                mVDVideoView.play(0);
                VDVideoViewController controller = VDVideoViewController.getInstance(mContext);
                if (controller != null) {
                    controller.resume();
                    controller.start();
                }

            }
        });
        mVDVideoView.setErrorListener(new VDVideoExtListeners.OnVDVideoErrorListener() {
            @Override
            public void onVDVideoError(VDVideoInfo vdVideoInfo, int i, int i1) {
                Log.i("zhangyujian2","setErrorListener::::");
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
            if (VideoApplication.initSuccess) {
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
                try {
                    VDVideoViewController controller1 = VDVideoViewController.getInstance(mContext);
                    if(controller1!=null)controller1.notifyHideTip();
                    mVDVideoView.play(0);
                } catch (Exception e) {
                    isError = true;
                    Tools.showToast(mContext, "视频解码库加载失败，请重试");
                    FileManager.delDirectoryOrFile(Environment.getDataDirectory() + "/data/com.xiangha/libs/");
                    return;
                }
                VDVideoViewController controller = VDVideoViewController.getInstance(mContext);
                if (controller != null) {
                    controller.resume();
                    controller.start();
                }
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
                Tools.showToast(mContext, "加载视频解码库中...");
            }
        } else {
            Tools.showToast(mContext, "努力获取视频信息中...");
        }

    }

    /**
     * 处理view显示
     */
    public void setOnStop(){
        VDVideoViewController controller = VDVideoViewController.getInstance(mContext);
        if (controller != null) {
            controller.stop();
        }
        mPraentViewGroup.removeAllViews();
        mPraentViewGroup.addView(mImageView);


    }
    /**
     * 初始化视频播放数据,直接使用url
     */
    public void initVideoView2( String url,String title) {

        mHasVideoInfo = true;
        if (ToolsDevice.isNetworkAvailable(XHApplication.in())){
            VideoApplication.getInstence().initialize(XHApplication.in());
        }
        videoUrl=url;
        VDVideoInfo videoInfo = new VDVideoInfo(url);
        videoInfo.mTitle = title;
        mVDVideoView.open(mContext, videoInfo);
    }
    /**
     * 外部重写keyDown()时调用
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onVDKeyDown(int keyCode, KeyEvent event) {
        if (mVDVideoView != null) {
            return mVDVideoView.onVDKeyDown(keyCode, event);
        }
        return true;
    }

    public VDVideoView getVDVideoView() {
        return mVDVideoView;
    }

    public void setVDVideoView(VDVideoView mVDVideoView) {
        this.mVDVideoView = mVDVideoView;
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
        if (mVDVideoView != null) {
            return mVDVideoView.getIsPlaying();
        }
        return false;
    }

    public void onResume() {
        if (mHasVideoInfo) {
            if (mVDVideoView != null) {
                mVDVideoView.onResume();
            }
        }
    }

    public void onPause() {
        if (mVDVideoView != null) {
            mVDVideoView.onPause();
        }
    }

    public void onStart() {
        if (mVDVideoView != null) {
            mVDVideoView.onStart();
        }
    }
    public void onStop() {
        if (mVDVideoView != null) {
            mVDVideoView.onStop();
        }
    }

    public void onDestroy() {
        if (mVDVideoView != null) {
            mVDVideoView.release(false);
        }
    }

    /**
     * 设置时候全屏
     *
     * @param isFullScreen true ? 全屏 : 不全屏
     */
    public void setIsFullScreen(boolean isFullScreen) {
        if (mVDVideoView != null) {
            mVDVideoView.setIsFullScreen(isFullScreen);
        }
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
