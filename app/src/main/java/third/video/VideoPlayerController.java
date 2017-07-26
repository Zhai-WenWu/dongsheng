package third.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jiecaovideoplayer.CustomView.XHVideoPlayerStandard;
import xh.basic.tool.UtilFile;

public class VideoPlayerController {
    protected Context mContext = null;
    //乐视的secretkey
    private final String secretkey = "5e172624924a79f81d60cb2c28f66c4d";
    private String mVideoUnique = "", mUserUnique = "";
    protected XHVideoPlayerStandard  videoPlayerStandard = null;
    protected ImageViewVideo mImageView = null;
    private boolean mHasVideoInfo = false;
    private int mVideoInfoRequestNumber = 0;
    protected ViewGroup mPraentViewGroup = null;
    private StatisticsPlayCountCallback mStatisticsPlayCountCallback = null;
    private OnPlayingCompletionListener onPlayingCompletionListener = null;
    protected String mImgUrl = "";
    protected View view_dish;
    protected View view_Tip;
    private boolean isAutoPaly = false;//是否是wifi状态
    private boolean isShowMedia = false;//true：直接播放，false,可以被其他因素控制

    public VideoPlayerController(Context context) {
        this.mContext = context;
    }

    /**
     * 初始化操作：
     * @param context 上下文
     * @param viewGroup---布局容器
     * @param imgUrl---图片路径
     */
    public VideoPlayerController(Context context, ViewGroup viewGroup, String imgUrl) {
        this.mContext = context;
        this.mPraentViewGroup = viewGroup;
        this.mImgUrl = imgUrl;

        videoPlayerStandard = new XHVideoPlayerStandard(context);
        videoPlayerStandard.setOnPlayStartCallback(new XHVideoPlayerStandard.OnPlayStartCallback() {
            @Override
            public void onStart() {
//                if(view_dish != null)
//                    view_dish.setVisibility(View.GONE);
            }
        });
        JCVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        JCVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
            mImageView.parseItemImg(ScaleType.CENTER_CROP, imgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
            mImageView.setLayoutParams(params);
            mPraentViewGroup.addView(mImageView);
            this.view_dish=mImageView;
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
    }

    /**
     * 重新设置布局样式
     * @param view:视频浮动的view
     */
    public void setNewView(View view) {
        this.view_dish = view;
        initView(mContext);
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(mContext));
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.removeAllViews();
        mPraentViewGroup.addView(videoPlayerStandard);
        if(view_Tip!=null)mPraentViewGroup.addView(view_Tip);
        mPraentViewGroup.addView(view_dish);
        view_dish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setOnClick();
            }
        });
    }

    /**
     * 广告点击事件
     */
    public void setOnClick() {
        Log.i("zhangyujian","广告点:::"+mHasVideoInfo);
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
                        mPraentViewGroup.removeView(view_Tip);
                        view_Tip=null;
                    }else{
                        if(view_dish!=null){
                            mPraentViewGroup.removeView(view_dish);
                            view_dish=null;
                        }
                        return;
                    }
                }
                if(view_dish!=null){
                    mPraentViewGroup.removeView(view_dish);
                    view_dish=null;
                    mImageView.setVisibility(View.GONE);
                }
                if(view_Tip!=null){
                    mPraentViewGroup.removeView(view_Tip);
                    view_Tip=null;
                }
                    videoPlayerStandard.startVideo();
                if (mStatisticsPlayCountCallback != null) {
                    mStatisticsPlayCountCallback.onStatistics();
                }
        } else {
            Tools.showToast(mContext, "努力获取视频信息中...");
            initVideoView(mVideoUnique, mUserUnique);
        }

    }

    /**
     * 初始化视频播放数据
     *
     * @param videoUnique vu
     * @param userUnique uu
     */
    public void initVideoView(final String videoUnique, final String userUnique) {
        this.mUserUnique = userUnique;
        this.mVideoUnique = videoUnique;
        String url = getUrl(videoUnique, userUnique);//cebb8cfcb9-----grpbim18nn
        ReqInternet.in().doGet(url, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING && msg != null) {
                    List<Map<String, String>> list = StringManager.getListMapByJson(msg);
                    if ("0".equals(getData(list, "code"))) {
                        list = StringManager.getListMapByJson(getData(list, "data"));
                        list = StringManager.getListMapByJson(getData(list, "video_list"));
                        list = StringManager.getListMapByJson(getData(list, "video_2"));
                        String main_url = getData(list, "main_url");
                        if (!TextUtils.isEmpty(main_url)) {
                            byte[] bytes = Base64.decode(main_url, Base64.DEFAULT);
                            String vedioUrl = new String(bytes);
                            videoPlayerStandard.setUp(vedioUrl, JCVideoPlayer.SCREEN_LAYOUT_LIST, "");
                            mHasVideoInfo = true;
                            if (mVideoInfoRequestNumber > 1) {
                                mPraentViewGroup.removeView(view_dish);
                                setOnClick();
                            }

                        } else {
                            Tools.showToast(mContext, "获取视频url地址为空");
                        }
                    } else {
                        Tools.showToast(mContext, "获取视频url地址失败");
                    }
                } else {
                    Tools.showToast(mContext, "请求视频url地址失败");
                }
            }

            @Override
            public Map<String, String> getReqHeader(Map<String, String> header, String url,Map<String, String> params) {
                mVideoInfoRequestNumber++;
                return new HashMap<>();
            }
        });
    }

    /**
     * 初始化视频播放数据
     *
     * @param videoUnique vu
     * @param userUnique uu
     * @param view view
     */
    public void initVideoView(final String videoUnique, final String userUnique, final View view) {
        this.mUserUnique = userUnique;
        this.mVideoUnique = videoUnique;
        String url = getUrl(videoUnique, userUnique);//cebb8cfcb9-----grpbim18nn
        ReqInternet.in().doGet(url, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING && msg != null) {
                    List<Map<String, String>> list = StringManager.getListMapByJson(msg);
                    if ("0".equals(getData(list, "code"))) {
                        list = StringManager.getListMapByJson(getData(list, "data"));
                        list = StringManager.getListMapByJson(getData(list, "video_list"));
                        list = StringManager.getListMapByJson(getData(list, "video_2"));
                        String main_url = getData(list, "main_url");
                        if (!TextUtils.isEmpty(main_url)) {
                            byte[] bytes = Base64.decode(main_url, Base64.DEFAULT);
                            String vedioUrl = new String(bytes);
                            videoPlayerStandard.setUp(vedioUrl, JCVideoPlayer.SCREEN_LAYOUT_LIST, "");
                            mHasVideoInfo = true;
                            if (mVideoInfoRequestNumber > 1) {

                                mImageView.setVisibility(View.GONE);
                                setOnClick();
                            }
                        } else {
                            Tools.showToast(mContext, "获取视频信息失败");
                        }
                    } else {
                        Tools.showToast(mContext, "获取视频信息失败");
                    }
                } else {
                    Tools.showToast(mContext, "获取视频信息失败");
                }
            }

            @Override
            public Map<String, String> getReqHeader(Map<String, String> header, String url,
                                                    Map<String, String> params) {
                mVideoInfoRequestNumber++;
                return new HashMap<>();
            }
        });
    }

    /**
     *  初始化视频播放数据,直接使用url
     * @param url
     * @param title
     * @param view
     */
    public void initVideoView2(final String url,String title, final View view) {
        videoPlayerStandard.setUp(url, JCVideoPlayer.SCREEN_LAYOUT_LIST, "");
        mHasVideoInfo = true;
    }

    private String getData(List<Map<String, String>> list, String key) {
        if (list.size() > 0) {
            Map<String, String> map = list.get(0);
            return map.get(key);
        }
        return "";
    }

    /**
     * 拼接获取视频播放地址的url
     *
     * @param videoUnique vu
     * @param userUnique uu
     * @return 视频url
     */
    private String getUrl(String videoUnique, String userUnique) {
        String url ;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(StringManager.api_getVideoUrl);
        stringBuffer.append("?");
        String tsParam = "ts=" + (System.currentTimeMillis() / 1000);
        stringBuffer.append(tsParam);
        stringBuffer.append("&");
        String userParam = "user=" + userUnique;
        stringBuffer.append(userParam);
        stringBuffer.append("&");
        String videoParam = "video=" + videoUnique;
        stringBuffer.append(videoParam);
        stringBuffer.append("&");
        String vtypeParam = "vtype=mp4";
        stringBuffer.append(vtypeParam);
        stringBuffer.append("&");
        String signParam = getSign(tsParam, userParam, videoParam, vtypeParam);
        stringBuffer.append(signParam);
        url = stringBuffer.toString();
        return url;
    }

    /**
     * 获取sign参数
     *
     * @param tsParam
     * @param userParam
     * @param videoParam
     * @param vtypeParam
     * @return
     */
    private String getSign(String tsParam, String userParam, String videoParam, String vtypeParam) {
        String signParam = "";
        signParam = tsParam.replace("=", "") + userParam.replace("=", "") + videoParam.replace("=", "") + vtypeParam.replace("=", "");
        signParam += secretkey;
        return "sign=" + StringManager.stringToMD5(signParam);
    }

    public boolean onBackPressed(){
        return JCVideoPlayer.backPress();
    }

    public ImageViewVideo getVideoImageView() {
        return mImageView;
    }

    public void setOnProgressChangedCallback(JCVideoPlayer.OnProgressChangedCallback callback){
        if(videoPlayerStandard != null)
            videoPlayerStandard.setOnProgressChangedCallback(callback);
    }

    public long getCurrentPositionWhenPlaying(){
        if(videoPlayerStandard != null){
            videoPlayerStandard.getCurrentPositionWhenPlaying();
        }
        return -1;
    }

    public long getDuration(){
        if(videoPlayerStandard != null){
            videoPlayerStandard.getDuration();
        }
        return -1;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        if (JCMediaManager.instance().mediaPlayer != null) {
            return JCMediaManager.instance().mediaPlayer.isPlaying();
        }
        return false;
    }

    public void onStart(){
        if(JCMediaManager.instance().mediaPlayer != null){
            JCMediaManager.instance().mediaPlayer.start();
            if(videoPlayerStandard != null)
                videoPlayerStandard.onStatePlaying();
        }
    }

    public void onResume() {
        if (mHasVideoInfo) {
            onStart();
        } else if (JCMediaManager.instance().mediaPlayer != null && !JCMediaManager.instance().mediaPlayer.isPlaying()) {
            initVideoView(mVideoUnique, mUserUnique);
        }
    }

    public void onPause() {
        if (JCMediaManager.instance().mediaPlayer != null) {
            JCMediaManager.instance().mediaPlayer.pause();
            videoPlayerStandard.onStatePause();
        }
    }

    public void onDestroy() {
        JCVideoPlayer.releaseAllVideos();
        JCVideoPlayer.clearSavedProgress(mContext, null);
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

    /**
     * 隐藏全屏按钮
     */
    public void hideFullScreen(){
        if(videoPlayerStandard!=null) {
            //去掉全屏按钮
            videoPlayerStandard.fullscreenButton.setVisibility(View.GONE);
        }
    }

    public void setMute(boolean isMuted, boolean needNotify) {
//        VDPlayerSoundManager.setMute(mContext, isMuted, needNotify);
    }

    //是否显示广告
    private boolean isShowAd=false;

    /**
     * 处理视频被点击回调
     */
    public interface MediaViewCallBack{
        public void onclick();
    }
    private MediaViewCallBack mediaViewCallBack;

    /**
     * 设置广告点击回调
     * @param mediaViewCallBack
     */
    public void setMediaViewCallBack(MediaViewCallBack mediaViewCallBack){
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
    protected void initView(Context context){
        LayoutParams layoutParams= new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        view_Tip=LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UtilFile.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
                }
            }).start();
        }
    };

    public boolean isShowMedia() {
        return isShowMedia;
    }

    public void setShowMedia(boolean showMedia) {
        isShowMedia = showMedia;
    }

    public void setOnPlayingCompletionListener(OnPlayingCompletionListener listener){
        this.onPlayingCompletionListener = listener;
        if(videoPlayerStandard != null && onPlayingCompletionListener != null)
            videoPlayerStandard.setOnPlayCompleteCallback(new XHVideoPlayerStandard.OnPlayCompleteCallback() {
                @Override
                public void onComplte() {
//                    mImageView.setVisibility(View.VISIBLE);
                    onPlayingCompletionListener.onPlayingCompletion();
                }
            });
    }

    public void removePlayingCompletionListener(){
        this.onPlayingCompletionListener = null;
    }

    public interface OnPlayingCompletionListener {
        public void onPlayingCompletion();
    }

}
