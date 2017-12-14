package com.shuyu.gsyvideoplayer;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.Md5FileNameGenerator;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.model.GSYModel;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

//import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 视频管理，单例
 * 目前使用的是IJK封装的谷歌EXOPlayer
 * Created by shuyu on 2016/11/11.
 */

public class GSYVideoManager implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnInfoListener, CacheListener {

    public static String TAG = "GSYVideoManager";

//    private static GSYVideoManager videoManager;

    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_SETDISPLAY = 1;
    public static final int HANDLER_RELEASE = 2;

    public static final int BUFFER_TIME_OUT_ERROR = -192;//外部超时错误码

    private AbstractMediaPlayer mediaPlayer;
    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;
    private Handler mainThreadHandler;

    private WeakReference<GSYMediaPlayerListener> listener;
    private WeakReference<GSYMediaPlayerListener> lastListener;

    // 单例模式实在不好给instance()加参数，还是直接设为静态变量吧
    private static IjkLibLoader ijkLibLoader; //自定义so包加载类

    private List<VideoOptionModel> optionModelList;//配置ijk option

    private HttpProxyCacheServer proxy; //视频代理

    private File cacheFile;

    private String playTag = ""; //播放的tag，防止错位置，因为普通的url也可能重复

    private Context context;

    private int currentVideoWidth = 0; //当前播放的视频宽的高

    private int currentVideoHeight = 0; //当前播放的视屏的高

    public static boolean canChange = true;

    private int lastState;//当前视频的最后状态

    private int playPosition = -22; //播放的tag，防止错位置，因为普通的url也可能重复

    private int buffterPoint;

    private int timeOut = 8 * 1000;

    private int videoType = GSYVideoType.IJKPLAYER;

    private boolean needMute = false; //是否需要静音

    private boolean needTimeOutOther; //是否需要外部超时判断


//    public static synchronized GSYVideoManager instance() {
//        if (videoManager == null) {
//            videoManager = new GSYVideoManager(ijkLibLoader);
//        }
//        return videoManager;
//    }

    /**
     * 设置自定义so包加载类
     * 需要在instance之前设置
     */
    public void setIjkLibLoader(IjkLibLoader libLoader) {
        ijkLibLoader = libLoader;
    }

    public IjkLibLoader getIjkLibLoader() {
       return ijkLibLoader;
    }

    /**
     * 获取缓存代理服务
     */
//    public static HttpProxyCacheServer getProxy(Context context) {
//        HttpProxyCacheServer proxy = GSYVideoManager.instance().proxy;
//        return proxy == null ? (GSYVideoManager.instance().proxy =
//                GSYVideoManager.instance().newProxy(context)) : proxy;
//    }

    /**
     * 删除默认所有缓存文件
     */
    public static void clearAllDefaultCache(Context context) {
        String path = StorageUtils.getIndividualCacheDirectory
                (context.getApplicationContext()).getAbsolutePath();
        if (TextUtils.isEmpty(path))
            return;
        FileUtils.deleteFiles(new File(path));
    }

    /**
     * 删除url对应默认缓存文件
     */
    public static void clearDefaultCache(Context context, String url) {
        Md5FileNameGenerator md5FileNameGenerator = new Md5FileNameGenerator();
        String name = md5FileNameGenerator.generate(url);
        String pathTmp = StorageUtils.getIndividualCacheDirectory
                (context.getApplicationContext()).getAbsolutePath()
                + File.separator + name + ".download";
        String path = StorageUtils.getIndividualCacheDirectory
                (context.getApplicationContext()).getAbsolutePath()
                + File.separator + name;
        CommonUtil.deleteFile(pathTmp);
        CommonUtil.deleteFile(path);

    }

    /**
     * 获取缓存代理服务,带文件目录的
     */
//    public static HttpProxyCacheServer getProxy(Context context, File file) {
//
//        //如果为空，返回默认的
//        if (file == null) {
//            return getProxy(context);
//        }
//
//        //如果已经有缓存文件路径，那么判断缓存文件路径是否一致
//        if (GSYVideoManager.instance().cacheFile != null
//                && !GSYVideoManager.instance().cacheFile.getAbsolutePath().equals(file.getAbsolutePath())) {
//            //不一致先关了旧的
//            HttpProxyCacheServer proxy = GSYVideoManager.instance().proxy;
//
//            if (proxy != null) {
//                proxy.shutdown();
//            }
//            //开启新的
//            return (GSYVideoManager.instance().proxy =
//                    GSYVideoManager.instance().newProxy(context, file));
//        } else {
//            //还没有缓存文件的或者一致的，返回原来
//            HttpProxyCacheServer proxy = GSYVideoManager.instance().proxy;
//
//            return proxy == null ? (GSYVideoManager.instance().proxy =
//                    GSYVideoManager.instance().newProxy(context, file)) : proxy;
//        }
//    }

    /**
     * 创建缓存代理服务,带文件目录的.
     */
    private HttpProxyCacheServer newProxy(Context context, File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        HttpProxyCacheServer.Builder builder = new HttpProxyCacheServer.Builder(context);
        builder.cacheDirectory(file);
        cacheFile = file;
        return builder.build();
    }


    /**
     * 创建缓存代理服务
     */
    private HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer(context.getApplicationContext());
    }


    public GSYMediaPlayerListener listener() {
        if (listener == null)
            return null;
        return listener.get();
    }

    public GSYMediaPlayerListener lastListener() {
        if (lastListener == null)
            return null;
        return lastListener.get();
    }

    public void setListener(GSYMediaPlayerListener listener) {
        if (listener == null)
            this.listener = null;
        else
            this.listener = new WeakReference<>(listener);
    }

    public void setLastListener(GSYMediaPlayerListener lastListener) {
        if (lastListener == null)
            this.lastListener = null;
        else
            this.lastListener = new WeakReference<>(lastListener);
    }

    //public GSYVideoManager() {
        //this(null);
    //}
    public GSYVideoManager(){
        this(null);
    }

    /***
     * @param libLoader 是否使用外部动态加载so
     * */
    public GSYVideoManager(IjkLibLoader libLoader) {
        mediaPlayer = (libLoader == null) ? new IjkMediaPlayer() : new IjkMediaPlayer(libLoader);
        ijkLibLoader = libLoader;
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler((mMediaHandlerThread.getLooper()));
        mainThreadHandler = new Handler();
    }

    public class MediaHandler extends Handler {
        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    initVideo(msg);
                    break;
                case HANDLER_SETDISPLAY:
                    showDisplay(msg);
                    break;
                case HANDLER_RELEASE:
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    setNeedMute(false);
                    if (proxy != null) {
                        proxy.unregisterCacheListener(GSYVideoManager.this);
                    }
                    buffterPoint = 0;
                    cancelTimeOutBuffer();
                    break;
            }
        }

    }

    private void initVideo(Message msg) {
        try {

            currentVideoWidth = 0;
            currentVideoHeight = 0;
            mediaPlayer.release();

            if (videoType == GSYVideoType.IJKPLAYER) {
                initIJKPlayer(msg);
//            } else if (videoType == GSYVideoType.IJKEXOPLAYER) {
//                initEXOPlayer(msg);
            }
            setNeedMute(needMute);
            mediaPlayer.setOnCompletionListener(GSYVideoManager.this);
            mediaPlayer.setOnBufferingUpdateListener(GSYVideoManager.this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(GSYVideoManager.this);
            mediaPlayer.setOnSeekCompleteListener(GSYVideoManager.this);
            mediaPlayer.setOnErrorListener(GSYVideoManager.this);
            mediaPlayer.setOnInfoListener(GSYVideoManager.this);
            mediaPlayer.setOnVideoSizeChangedListener(GSYVideoManager.this);
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initIJKPlayer(Message msg) {
        mediaPlayer = (ijkLibLoader == null) ? new IjkMediaPlayer() : new IjkMediaPlayer(ijkLibLoader);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            if (GSYVideoType.isMediaCodec()) {
                Debuger.printfLog("enable mediaCodec");
                ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            }
            ((IjkMediaPlayer) mediaPlayer).setDataSource(((GSYModel) msg.obj).getUrl(), ((GSYModel) msg.obj).getMapHeadData());
            mediaPlayer.setLooping(((GSYModel) msg.obj).isLooping());
            if (((GSYModel) msg.obj).getSpeed() != 1 && ((GSYModel) msg.obj).getSpeed() > 0) {
                ((IjkMediaPlayer) mediaPlayer).setSpeed(((GSYModel) msg.obj).getSpeed());
            }
            initIJKOption((IjkMediaPlayer) mediaPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void initEXOPlayer(Message msg) {
//        mediaPlayer = new IjkExoMediaPlayer(context);
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        try {
//            mediaPlayer.setDataSource(context, Uri.parse(((GSYModel) msg.obj).getUrl()), ((GSYModel) msg.obj).getMapHeadData());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void initIJKOption(IjkMediaPlayer ijkMediaPlayer) {
        if (optionModelList != null && optionModelList.size() > 0) {
            for (VideoOptionModel videoOptionModel : optionModelList) {
                if (videoOptionModel.getValueType() == VideoOptionModel.VALUE_TYPE_INT) {
                    ijkMediaPlayer.setOption(videoOptionModel.getCategory(),
                            videoOptionModel.getName(), videoOptionModel.getValueInt());
                } else {
                    ijkMediaPlayer.setOption(videoOptionModel.getCategory(),
                            videoOptionModel.getName(), videoOptionModel.getValueString());
                }
            }
        }
    }


    /**
     * 启动十秒的定时器进行 缓存操作
     */
    private void startTimeOutBuffer() {
        // 启动定时
        Debuger.printfError("startTimeOutBuffer");
        mainThreadHandler.postDelayed(mTimeOutRunnable, timeOut);

    }

    /**
     * 取消 十秒的定时器进行 缓存操作
     */
    private void cancelTimeOutBuffer() {
        Debuger.printfError("cancelTimeOutBuffer");
        // 取消定时
        if (needTimeOutOther)
            mainThreadHandler.removeCallbacks(mTimeOutRunnable);
    }


    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            GSYMediaPlayerListener listener = listener();
            if (listener != null) {
                Debuger.printfError("time out for error listener");
                listener.onError(BUFFER_TIME_OUT_ERROR, BUFFER_TIME_OUT_ERROR);
            }
        }
    };


    private void showDisplay(Message msg) {
        if (msg.obj == null && mediaPlayer != null) {
            mediaPlayer.setSurface(null);
        } else {
            Surface holder = (Surface) msg.obj;
            if (mediaPlayer != null && holder.isValid()) {
                mediaPlayer.setSurface(holder);
            }
//            if (mediaPlayer instanceof IjkExoMediaPlayer) {
//                if (mediaPlayer != null && mediaPlayer.getDuration() > 30
//                        && mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration()) {
//                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 20);
//                }
//            }
        }
    }


    public void prepare(final String url, final Map<String, String> mapHeadData, boolean loop, float speed) {
        if (TextUtils.isEmpty(url)) return;
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        GSYModel fb = new GSYModel(url, mapHeadData, loop, speed);
        msg.obj = fb;
        mMediaHandler.sendMessage(msg);
        if (needTimeOutOther) {
            startTimeOutBuffer();
        }
    }

    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
        playTag = "";
        playPosition = -22;
    }

    public void setDisplay(Surface holder) {
        Message msg = new Message();
        msg.what = HANDLER_SETDISPLAY;
        msg.obj = holder;
        mMediaHandler.sendMessage(msg);
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    listener.onPrepared();
                }
            }
        });
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    listener.onAutoCompletion();
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, final int percent) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    if (percent > buffterPoint) {
                        listener.onBufferingUpdate(percent);
                    } else {
                        listener.onBufferingUpdate(buffterPoint);
                    }
                }
            }
        });
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    listener.onSeekComplete();
                }
            }
        });
    }

    @Override
    public boolean onError(IMediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    listener.onError(what, extra);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (needTimeOutOther) {
                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        startTimeOutBuffer();
                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        cancelTimeOutBuffer();
                    }
                }
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    listener.onInfo(what, extra);
                }
            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        if(canChange){
            currentVideoWidth = mp.getVideoWidth();
            currentVideoHeight = mp.getVideoHeight();
        }
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                GSYMediaPlayerListener listener = listener();
                if (listener != null) {
                    listener.onVideoSizeChanged();
                }
            }
        });
    }


    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        buffterPoint = percentsAvailable;
    }

    /**
     * 暂停播放
     */
    public void onPause() {
        if (listener() != null) {
            listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public void onResume() {
        if (listener() != null) {
            listener().onVideoResume();
        }
    }


    public AbstractMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getCurrentVideoWidth() {
        return currentVideoWidth;
    }

    public int getCurrentVideoHeight() {
        return currentVideoHeight;
    }

    public int getLastState() {
        return lastState;
    }

    public void setLastState(int lastState) {
        this.lastState = lastState;
    }

    public void setCurrentVideoHeight(int currentVideoHeight) {
        this.currentVideoHeight = currentVideoHeight;
    }

    public void setCurrentVideoWidth(int currentVideoWidth) {
        this.currentVideoWidth = currentVideoWidth;
    }

    public String getPlayTag() {
        return playTag;
    }

    public void setPlayTag(String playTag) {
        this.playTag = playTag;
    }

    public int getPlayPosition() {
        return playPosition;
    }

    public void setPlayPosition(int playPosition) {
        this.playPosition = playPosition;
    }


    public int getVideoType() {
        return videoType;
    }

    public List<VideoOptionModel> getOptionModelList() {
        return optionModelList;
    }

    /**
     * 设置IJK视频的option
     */
    public void setOptionModelList(List<VideoOptionModel> optionModelList) {
        this.optionModelList = optionModelList;
    }

    /**
     * 设置了视频的播放类型
     * GSYVideoType IJKPLAYER = 0 or IJKEXOPLAYER = 1;
     */
    public void setVideoType(Context context, int videoType) {
        this.context = context.getApplicationContext();
        this.videoType = videoType;
    }

    public boolean isNeedMute() {
        return needMute;
    }

    /**
     * 是否需要静音
     */
    public void setNeedMute(boolean needMute) {
        this.needMute = needMute;
        if (mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }

    public int getTimeOut() {
        return timeOut;
    }

    public boolean isNeedTimeOutOther() {
        return needTimeOutOther;
    }

    /**
     * 是否需要在buffer缓冲时，增加外部超时判断
     *
     * 超时后会走onError接口，播放器通过onPlayError回调出
     *
     * 错误码为 ： BUFFER_TIME_OUT_ERROR = -192
     *
     * 由于onError之后执行GSYVideoPlayer的OnError，如果不想触发错误，
     * 可以重载onError，在super之前拦截处理。
     *
     * public void onError(int what, int extra){
     *      do you want before super and return;
     *      super.onError(what, extra)
     * }
     *
     * @param timeOut          超时时间，毫秒 默认8000
     * @param needTimeOutOther 是否需要延时设置，默认关闭
     */
    public void setTimeOut(int timeOut, boolean needTimeOutOther) {
        this.timeOut = timeOut;
        this.needTimeOutOther = needTimeOutOther;
    }

}