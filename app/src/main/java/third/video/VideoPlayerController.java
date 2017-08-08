package third.video;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.sinavideo.sdk.VDVideoExtListeners.OnVDVideoCompletionListener;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.VDVideoViewListeners;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.sina.sinavideo.sdk.utils.VDPlayerSoundManager;
import com.sina.sinavideo.sdk.widgets.VDVideoFullScreenButton;
import com.sina.sinavideo.sdk.widgets.VDVideoPlaySeekBar;
import com.xianghatest.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.CPUTool;
import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.VideoShowWeb;
import xh.basic.tool.UtilFile;

public class VideoPlayerController {
    protected Context mContext = null;
    //乐视的secretkey
    private final String secretkey = "5e172624924a79f81d60cb2c28f66c4d";
    protected VDVideoView mVDVideoView = null;
    protected ImageViewVideo mImageView = null;
    private String mVideoUnique = "", mUserUnique = "";
    private boolean mHasVideoInfo = false;
    private int mVideoInfoRequestNumber = 0;
    protected ViewGroup mPraentViewGroup = null;
    private StatisticsPlayCountCallback mStatisticsPlayCountCallback = null;
    protected String mImgUrl = "";
    public boolean isError = false;
    protected View view_dish;
    protected View view_Tip;
    private boolean isAutoPaly = false;//是否是wifi状态
    private boolean isShowMedia = false;//true：直接播放，false,可以被其他因素控制
    private VDVideoViewListeners.OnProgressUpdateListener onProgressUpdateListener;

    public VideoPlayerController(Context context) {
        this.mContext = context;
    }

    /**
     * 初始化操作：
     *
     * @param context
     * @param viewGroup---布局容器
     * @param imgUrl---图片路径
     */
    public VideoPlayerController(Context context, ViewGroup viewGroup, String imgUrl) {
        this.mContext = context;
        this.mPraentViewGroup = viewGroup;
        this.mImgUrl = imgUrl;
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
        mVDVideoView.setCompletionListener(new OnVDVideoCompletionListener() {
            //暂时未实现
            @Override
            public void onVDVideoCompletion(VDVideoInfo info, int status) {
            }
        });
        setControlLayerVisibility(false);
        VDVideoViewController controller = VDVideoViewController.getInstance(context);
        controller.pause();
        if (controller != null) {
            controller.addOnCompletionListener(new VDVideoViewListeners.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    if (mOnPlayingCompletionListener != null) {
                        mOnPlayingCompletionListener.onPlayingCompletion();
                    }
                }
            });
//            controller.touchScreenHorizonScrollEvent();
            controller.setSeekPause(true);
            controller.addOnSeekCompleteListener(new VDVideoViewListeners.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete() {

                }
            });

            controller.addOnProgressUpdateListener(new VDVideoViewListeners.OnProgressUpdateListener() {
                @Override
                public void onProgressUpdate(long current, long duration) {
                    if (onProgressUpdateListener != null)
                        onProgressUpdateListener.onProgressUpdate(current, duration);
                }

                @Override
                public void onDragProgess(long current, long duration) {
                    if (onProgressUpdateListener != null)
                        onProgressUpdateListener.onDragProgess(current, duration);
                }
            });
        }
        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(mVDVideoView);
        mVDVideoView.setVDVideoViewContainer((ViewGroup) mVDVideoView.getParent());
        if (!TextUtils.isEmpty(imgUrl)) {
            if (view_Tip == null) {
                initView(mContext);
                mPraentViewGroup.addView(view_Tip);
            }
            mImageView = new ImageViewVideo(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
            mImageView.parseItemImg(ScaleType.CENTER_CROP, imgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
            mImageView.setLayoutParams(params);
            mPraentViewGroup.addView(mImageView);
            this.view_dish = mImageView;
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnClick();
                }
            });
        }
        String temp = (String) UtilFile.loadShared(context, FileManager.SHOW_NO_WIFI, FileManager.SHOW_NO_WIFI);
        if (!TextUtils.isEmpty(temp) && "1".equals(temp))
            setShowMedia(true);
    }

    /**
     * 重新设置布局样式
     *
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
        mPraentViewGroup.addView(mVDVideoView);
        if (view_Tip != null) mPraentViewGroup.addView(view_Tip);
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
        Log.i("zhangyujian", "广告点:::" + mHasVideoInfo);
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(mContext));
        if (mHasVideoInfo) {
            if (VideoApplication.initSuccess) {
                if (isShowAd) {
                    if (mediaViewCallBack != null) mediaViewCallBack.onclick();
                    return;
                }
                Log.i("zhangyujian", "isShowMedia:::" + isShowMedia);
                if (!isShowMedia) {
                    Log.i("zhangyujian", "isAutoPaly:::" + isAutoPaly);
                    if (isAutoPaly) {//当前wifi
                        mPraentViewGroup.removeView(view_Tip);
                        view_Tip = null;
                    } else {
                        if (view_dish != null) {
                            mPraentViewGroup.removeView(view_dish);
                            view_dish = null;
                        }
                        return;
                    }
                }
                if (view_dish != null) {
                    mPraentViewGroup.removeView(view_dish);
                    view_dish = null;
                }
                if (view_Tip != null) {
                    mPraentViewGroup.removeView(view_Tip);
                    view_Tip = null;
                }
                try {
                    Log.i("zhangyujian", "开始播放:::");
                    VDVideoViewController controller1 = VDVideoViewController.getInstance(mContext);
                    if (controller1 != null) controller1.notifyHideTip();
                    mVDVideoView.play(0);
                } catch (Exception e) {
                    isError = true;
                    Tools.showToast(mContext, "视频解码库加载失败，请重试");
                    FileManager.delDirectoryOrFile(Environment.getDataDirectory() + "/data/com.xianghatest/libs/");
                    initVideoView(mVideoUnique, mUserUnique);
                    return;
                }
                VDVideoViewController controller = VDVideoViewController.getInstance(mContext);
                if (controller != null) {
                    controller.resume();
                    controller.start();
                }

                if (mStatisticsPlayCountCallback != null) {
                    mStatisticsPlayCountCallback.onStatistics();
                }
            } else {
                Tools.showToast(mContext, "加载视频解码库中...");
            }
        } else {
            Tools.showToast(mContext, "努力获取视频信息中...");
            initVideoView(mVideoUnique, mUserUnique);
        }

    }

    /**
     * 初始化视频播放数据
     *
     * @param videoUnique
     * @param userUnique
     */
    public void initVideoView(final String videoUnique, final String userUnique) {
        if (isError) {
            mImageView = new ImageViewVideo(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
            mImageView.parseItemImg(ScaleType.CENTER_CROP, mImgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
            mImageView.setLayoutParams(params);
            mPraentViewGroup.addView(mImageView);
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String url = "http://m.xiangha.com/Dish/video?uu=" + userUnique + "&vu=" + videoUnique + "&height=320&width=100%&pu=b871295ff4&text-align=%27center%27&auto_play=1&extend=0";
                    Intent intent = new Intent(mContext, VideoShowWeb.class);
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                }
            });
            return;
        }
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
                        String title = getData(list, "video_name");
                        list = StringManager.getListMapByJson(getData(list, "video_list"));
                        list = StringManager.getListMapByJson(getData(list, "video_2"));
                        String main_url = getData(list, "main_url");
                        if (!TextUtils.isEmpty(main_url)) {
                            byte[] bytes = Base64.decode(main_url, Base64.DEFAULT);
                            String vedioUrl = new String(bytes);
                            VDVideoInfo videoInfo = new VDVideoInfo(vedioUrl);
                            videoInfo.mTitle = title;
                            mVDVideoView.open(mContext, videoInfo);
                            mHasVideoInfo = true;
                            if (mVideoInfoRequestNumber > 1) {
                                mPraentViewGroup.removeView(mImageView);
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
            public Map<String, String> getReqHeader(Map<String, String> header, String url,
                                                    Map<String, String> params) {
                mVideoInfoRequestNumber++;
                return new HashMap<String, String>();
            }
        });
    }

    /**
     * 初始化视频播放数据
     *
     * @param videoUnique
     * @param userUnique
     */
    public void initVideoView(final String videoUnique, final String userUnique, final View view) {
        if (isError) {
            mPraentViewGroup.addView(view);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String url = "http://m.xiangha.com/Dish/video?uu=" + userUnique + "&vu=" + videoUnique + "&height=320&width=100%&pu=b871295ff4&text-align=%27center%27&auto_play=1&extend=0";
                    Intent intent = new Intent(mContext, VideoShowWeb.class);
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                }
            });
            return;
        }
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
                        String title = getData(list, "video_name");
                        list = StringManager.getListMapByJson(getData(list, "video_list"));
                        list = StringManager.getListMapByJson(getData(list, "video_2"));
                        String main_url = getData(list, "main_url");
                        if (!TextUtils.isEmpty(main_url)) {
                            byte[] bytes = Base64.decode(main_url, Base64.DEFAULT);
                            String vedioUrl = new String(bytes);
                            VDVideoInfo videoInfo = new VDVideoInfo(vedioUrl);
                            videoInfo.mTitle = title;
                            mVDVideoView.open(mContext, videoInfo);
                            mHasVideoInfo = true;
                            if (mVideoInfoRequestNumber > 1) {
                                mPraentViewGroup.removeView(view);
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
     * 初始化视频播放数据,直接使用url
     */
    public void initVideoView2(final String url, String title, final View view) {

        mHasVideoInfo = true;
        if (ToolsDevice.isNetworkAvailable(XHApplication.in())) {
            VideoApplication.getInstence().initialize(XHApplication.in());
        }
        VDVideoInfo videoInfo = new VDVideoInfo(url);
        videoInfo.mTitle = title;
        mVDVideoView.open(mContext, videoInfo);
    }

    public void setControlLayerVisibility(boolean isShow) {
        if (mVDVideoView != null)
            mVDVideoView.findViewById(R.id.controlLayout1).setVisibility(isShow ? View.VISIBLE : View.GONE);
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
     * @param videoUnique
     * @param userUnique
     * @return
     */
    private String getUrl(String videoUnique, String userUnique) {
        String url = "";
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

    public void onStart() {
        if (mVDVideoView != null) {
            mVDVideoView.onStart();
        }
    }

    public void onResume() {
        if (mHasVideoInfo) {
            if (mVDVideoView != null) {
                mVDVideoView.onResume();
            }
        } else if (mVDVideoView != null && !mVDVideoView.getIsPlaying()) {
            initVideoView(mVideoUnique, mUserUnique);
        }
    }

    public void onPause() {
        if (mVDVideoView != null) {
            mVDVideoView.onPause();
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
    protected void statisticsInitVideoError(Context context) {
        isError = true;
//		Tools.showToast(context, "您的手机暂时不支持播放视频");
        XHClick.mapStat(context, "init_video_error", "CPU型号", "" + CPUTool.getCpuName());
        XHClick.mapStat(context, "init_video_error", "手机型号", android.os.Build.BRAND + "_" + android.os.Build.MODEL);
    }

    /**
     * 隐藏全屏按钮
     */
    public void hideFullScreen() {
        if (mVDVideoView != null) {
            //去掉全屏按钮
            VDVideoFullScreenButton fullscreen1 = (VDVideoFullScreenButton) mVDVideoView.findViewById(R.id.fullscreen1);
            fullscreen1.setVisibility(View.GONE);

            VDVideoPlaySeekBar playerseek2 = (VDVideoPlaySeekBar) mVDVideoView.findViewById(R.id.playerseek2);
            //设置滑动条位置
            RelativeLayout.LayoutParams playerseekParam = (RelativeLayout.LayoutParams) playerseek2.getLayoutParams();
            playerseekParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        }
    }

    public void setMute(boolean isMuted, boolean needNotify) {
        VDPlayerSoundManager.setMute(mContext, isMuted, needNotify);
    }

    //是否显示广告
    private boolean isShowAd = false;

    /**
     * 处理视频被点击回调
     */
    public interface MediaViewCallBack {
        public void onclick();
    }

    private MediaViewCallBack mediaViewCallBack;

    /**
     * 设置广告点击回调
     *
     * @param mediaViewCallBack
     */
    public void setMediaViewCallBack(MediaViewCallBack mediaViewCallBack) {
        this.mediaViewCallBack = mediaViewCallBack;
    }

    public boolean isShowAd() {
        return isShowAd;
    }

    public void setShowAd(boolean showAd) {
        isShowAd = showAd;
    }

    /**
     * 初始化
     *
     * @param context
     */
    protected void initView(Context context) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view_Tip = LayoutInflater.from(context).inflate(R.layout.tip_layout, null);
        view_Tip.setLayoutParams(layoutParams);
        TextView tipMessage = (TextView) view_Tip.findViewById(R.id.tipMessage);
        tipMessage.setText("现在是非WIFI，看视频要花费流量了");
        view_Tip.findViewById(R.id.tipLayout).setOnClickListener(onClickListener);
        view_Tip.findViewById(R.id.btnCloseTip).setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setShowMedia(true);
            setOnClick();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UtilFile.saveShared(mContext, FileManager.SHOW_NO_WIFI, FileManager.SHOW_NO_WIFI, "1");
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
//    private void play()

    public interface OnPlayingCompletionListener {
        void onPlayingCompletion();
    }

    protected OnPlayingCompletionListener mOnPlayingCompletionListener;

    public void setOnPlayingCompletionListener(OnPlayingCompletionListener playingCompletionListener) {
        mOnPlayingCompletionListener = playingCompletionListener;
    }

    public void removePlayingCompletionListener() {
        mOnPlayingCompletionListener = null;
    }

    public void setOnProgressUpdateListener(VDVideoViewListeners.OnProgressUpdateListener onProgressUpdateListener) {
        this.onProgressUpdateListener = onProgressUpdateListener;
    }
}
