package third.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;
import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
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

import static com.shuyu.gsyvideoplayer.GSYVideoPlayer.CURRENT_STATE_PLAYING;

public class VideoPlayerController {
    protected Context mContext = null;
    //乐视的secretkey
    @SuppressWarnings("FieldCanBeLocal")
    private final String secretkey = "5e172624924a79f81d60cb2c28f66c4d";
    private String mVideoUnique = "", mUserUnique = "";
    protected ImageViewVideo mImageView = null;
    private boolean mHasVideoInfo = false;
    private int mVideoInfoRequestNumber = 0;
    protected ViewGroup mPraentViewGroup = null;
    private StatisticsPlayCountCallback mStatisticsPlayCountCallback = null;
    protected OnPlayingCompletionListener onPlayingCompletionListener = null;
    protected String mImgUrl = "";
    protected String mVideoUrl = "";
    protected View view_dish;
    protected View view_Tip;
    private boolean isAutoPaly = false;//是否是wifi状态
    private boolean isShowMedia = false;//true：直接播放，false,可以被其他因素控制
    public boolean isNetworkDisconnect = false;
    public int autoRetryCount = 0;
    public boolean isPortrait = false;

    protected StandardGSYVideoPlayer videoPlayer;
    protected OrientationUtils orientationUtils;

    public VideoPlayerController(Context context) {
        this.mContext = context;
    }

    /**
     * 初始化操作：
     * @param context 上下文
     * @param viewGroup---布局容器
     * @param imgUrl---图片路径
     */
    public VideoPlayerController(final Activity context, ViewGroup viewGroup, String imgUrl) {
        this.mContext = context;
        this.mPraentViewGroup = viewGroup;
        this.mImgUrl = imgUrl;

        videoPlayer = new StandardGSYVideoPlayer(context);
        //设置旋转
        orientationUtils = new OrientationUtils(context, videoPlayer);
        orientationUtils.setEnable(false);
        videoPlayer.setShowFullAnimation(false);
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                videoPlayer.startWindowFullscreen(context, true, true);
            }
        });
        videoPlayer.setStandardVideoAllCallBack(new SampleListener(){
            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                if(onPlayingCompletionListener != null)
                    onPlayingCompletionListener.onPlayingCompletion();
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                if(url.startsWith("http"))
                    setNetworkCallback();
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                if(!isPortrait)
                    orientationUtils.resolveByClick();
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                orientationUtils.backToProtVideo();
            }
        });

        Resources resources = context.getResources();
        videoPlayer.setBottomProgressBarDrawable(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setDialogVolumeProgressBar(resources.getDrawable(R.drawable.video_new_volume_progress_bg));
        videoPlayer.setDialogProgressBar(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setBottomShowProgressBarDrawable(resources.getDrawable(R.drawable.video_new_seekbar_progress),
                resources.getDrawable(R.drawable.video_new_seekbar_thumb));

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false);
        videoPlayer.setIsTouchWigetFull(true);

//        videoPlayerStandard.setOnPlayErrorCallback(new JCVideoPlayerStandard.OnPlayErrorCallback() {
//            @Override
//            public boolean onError() {
//                if(ToolsDevice.isNetworkAvailable(mContext)
//                        && isNetworkDisconnect
//                        && autoRetryCount < 3){
//                    autoRetryCount++;
//                    JCUtils.saveProgress(mContext,mImgUrl,videoPlayerStandard.getCurrentPositionWhenPlaying());
//                    videoPlayerStandard.startVideo();
//                    return true;
//                }
//                return false;
//            }
//        });
        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(videoPlayer);
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
        String temp= (String) FileManager.loadShared(context,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
        if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
            setShowMedia(true);
    }

    private void setNetworkCallback(){
        videoPlayer.addListener(new StandardGSYVideoPlayer.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                removeTipView();
                onResume();
                isNetworkDisconnect = false;
            }

            @Override
            public void mobileConnected() {
                if(!"1".equals(FileManager.loadShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI).toString())){
                    if(isNetworkDisconnect){
                        removeTipView();
                        if(view_Tip==null){
                            initView(mContext);
                            mPraentViewGroup.addView(view_Tip);
                        }
                        onPause();
                    }
                }else if(videoPlayer.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PAUSE){
                    removeTipView();
                    onResume();
                }
                isNetworkDisconnect = false;
            }

            @Override
            public void nothingConnected() {
                if(view_Tip == null){
                    initNoNetwork(mContext);
                    mPraentViewGroup.addView(view_Tip);
                }
                onPause();
                isNetworkDisconnect = true;
            }
        });
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
        mPraentViewGroup.addView(videoPlayer);
        if(view_Tip != null)
            mPraentViewGroup.addView(view_Tip);
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
        Log.i("tzy","广告点:::"+mHasVideoInfo);
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(mContext));
        if (mHasVideoInfo) {
                if(isShowAd){
                    if(mediaViewCallBack != null)
                        mediaViewCallBack.onclick();
                    return;
                }
                Log.i("tzy","isShowMedia:::"+isShowMedia);
                if(!isShowMedia){
                    Log.i("tzy","isAutoPaly:::"+isAutoPaly);
                    if(isAutoPaly){//当前wifi
                        removeTipView();
                    }else{
                        removeDishView();
                        return;
                    }
                }
                removeDishView();
                removeTipView();
                videoPlayer.startPlayLogic();
                if (mStatisticsPlayCountCallback != null) {
                    mStatisticsPlayCountCallback.onStatistics();
                }
        } else {
            Tools.showToast(mContext, "努力获取视频信息中...");
            initVideoView(mVideoUnique, mUserUnique);
        }
    }

    protected void removeDishView(){
        if(view_dish!=null){
            mPraentViewGroup.removeView(view_dish);
            view_dish=null;
        }
    }

    protected void removeTipView(){
        if(view_Tip!=null){
            mPraentViewGroup.removeView(view_Tip);
            view_Tip=null;
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
                            mVideoUrl = new String(bytes);
                            videoPlayer.setUp(mVideoUrl,false,"");
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
                            mVideoUrl = new String(bytes);
                            videoPlayer.setUp(mVideoUrl,false,"");
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
     * @param url 视频链接
     * @param title 标题
     * @param view view
     */
    public void initVideoView2(final String url,String title, final View view) {
        this.mVideoUrl = url;
        videoPlayer.setUp(mVideoUrl,false,"");
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
        stringBuffer.append(StringManager.api_getVideoUrl).append("?");
        String tsParam = "ts=" + (System.currentTimeMillis() / 1000);
        stringBuffer.append(tsParam).append("&");
        String userParam = "user=" + userUnique;
        stringBuffer.append(userParam).append("&");
        String videoParam = "video=" + videoUnique;
        stringBuffer.append(videoParam).append("&");
        String vtypeParam = "vtype=mp4";
        stringBuffer.append(vtypeParam).append("&");
        String signParam = getSign(tsParam, userParam, videoParam, vtypeParam);
        stringBuffer.append(signParam);
        url = stringBuffer.toString();
        return url;
    }

    /**
     * 获取sign参数
     *
     * @param tsParam 时间戳
     * @param userParam uu
     * @param videoParam vu
     * @param vtypeParam 视频格式
     * @return 签名
     */
    private String getSign(String tsParam, String userParam, String videoParam, String vtypeParam) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(tsParam.replace("=", ""))
                .append(userParam.replace("=", ""))
                .append(videoParam.replace("=", ""))
                .append(vtypeParam.replace("=", ""))
                .append(secretkey);
        return "sign=" + StringManager.stringToMD5(stringBuffer.toString());
    }

    public boolean onBackPressed(){
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || isPortrait) {
            return StandardGSYVideoPlayer.backFromWindowFull(mContext);
        }
        return false;
    }

    public ImageViewVideo getVideoImageView() {
        return mImageView;
    }

    public void setOnProgressChangedCallback(GSYVideoPlayer.OnProgressChangedCallback callback){
        if(videoPlayer != null)
            videoPlayer.setOnProgressChangedCallback(callback);
    }

    public int getCurrentPositionWhenPlaying(){
        if(videoPlayer != null){
            return videoPlayer.getCurrentPositionWhenPlaying();
        }
        return -1;
    }

    public int getDuration(){
        if(videoPlayer != null){
            return videoPlayer.getDuration();
        }
        return -1;
    }

    /**
     * 是否正在播放
     *
     * @return 是否在播放
     */
    public boolean isPlaying() {
        return null != videoPlayer && CURRENT_STATE_PLAYING==videoPlayer.getCurrentState();
    }

    public void onStart(){
        if(null != videoPlayer)
            videoPlayer.startPlayLogic();
    }

    public void onResume() {
        if(null != videoPlayer && !isNetworkDisconnect)
            videoPlayer.onVideoResume();
    }

    public void onPause() {
        if(null != videoPlayer)
            videoPlayer.onVideoPause();
    }

    public void onDestroy() {
        if(null != videoPlayer){
            //释放所有
            videoPlayer.setStandardVideoAllCallBack(null);
            videoPlayer.release();
        }
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

    /**
     * 隐藏全屏按钮
     */
    public void hideFullScreen(){
        if(null != videoPlayer && null != videoPlayer.getFullscreenButton()){
            videoPlayer.getFullscreenButton().setVisibility(View.GONE);
        }
    }

    //是否显示广告
    private boolean isShowAd=false;

    /** 处理视频被点击回调 */
    public interface MediaViewCallBack{
        void onclick();
    }
    private MediaViewCallBack mediaViewCallBack;

    /**
     * 设置广告点击回调
     * @param mediaViewCallBack 回调
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
     * @param context 上下文
     */
    @SuppressLint({"SetTextI18n", "InflateParams"})
    protected void initView(Context context){
        LayoutParams layoutParams= new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        view_Tip=LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
        view_Tip.setLayoutParams(layoutParams);
        TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        Button btnCloseTip = (Button) view_Tip.findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("继续播放");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(onClickListener);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(onClickListener);
    }

    protected void initNoNetwork(Context context){
        LayoutParams layoutParams= new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        view_Tip=LayoutInflater.from(context).inflate(R.layout.tip_layout,null);
        view_Tip.setLayoutParams(layoutParams);
        TextView tipMessage= (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("网络未连接，请检查网络设置");
        Button btnCloseTip = (Button) view_Tip.findViewById(R.id.btnCloseTip);
        btnCloseTip.setText("去设置");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(disconnectClick);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(disconnectClick);
    }

    private OnClickListener disconnectClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mContext.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    };

    private OnClickListener onClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            setShowMedia(true);
            setOnClick();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileManager.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"1");
                }
            }).start();
        }
    };

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public boolean isShowMedia() {
        return isShowMedia;
    }

    public void setShowMedia(boolean showMedia) {
        isShowMedia = showMedia;
    }

    public void setOnPlayingCompletionListener(OnPlayingCompletionListener listener){
        this.onPlayingCompletionListener = listener;
    }

    public void removePlayingCompletionListener(){
        this.onPlayingCompletionListener = null;
    }

    public interface OnPlayingCompletionListener {
        void onPlayingCompletion();
    }
    protected OnPlayingCompletionListener mOnPlayingCompletionListener;

    public static boolean isPortraitVideo(float videoW,float videoH){
        if(videoW <= 0 || videoH <= 0)
            return false;
        //视频比例大于3：4则为竖屏视频
        return videoW/videoH <= 3/4;
    }
}
