package third.aliyun.work;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.common.global.Version;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.crop.AliyunCropCreator;
import com.aliyun.crop.struct.CropParam;
import com.aliyun.crop.supply.AliyunICrop;
import com.aliyun.crop.supply.CropCallback;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupai.import_core.AliyunIImport;
import com.aliyun.qupai.import_core.AliyunImportCreator;
import com.aliyun.struct.common.AliyunDisplayMode;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.snap.AliyunSnapVideoParam;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleView;
import com.xiangha.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.Tools;
import third.aliyun.media.FrameExtractor10;
import third.aliyun.media.MediaInfo;
import third.aliyun.media.VideoTrimAdapter;
import third.aliyun.widget.HorizontalListView;
import third.aliyun.widget.SizeChangedNotifier;
import third.aliyun.widget.VideoSliceSeekBar;
import third.aliyun.widget.VideoTrimFrameLayout;

import static third.aliyun.work.MediaActivity.resolutions;


/**
 *视频编辑
 */
public class AliyunVideoCrop extends Activity implements TextureView.SurfaceTextureListener,
        VideoSliceSeekBar.SeekBarChangeListener, HorizontalListView.OnScrollCallBack, SizeChangedNotifier.Listener,
        MediaPlayer.OnVideoSizeChangedListener, VideoTrimFrameLayout.OnVideoScrollCallBack, View.OnClickListener, CropCallback, Handler.Callback {
    public static final ScaleMode SCALE_CROP = ScaleMode.PS;
    public static final ScaleMode SCALE_FILL = ScaleMode.LB;
    private static final int PLAY_VIDEO = 1000;
    private static final int PAUSE_VIDEO = 1001;
    private static final int END_VIDEO = 1003;
    private int playState = END_VIDEO;
    private static int OUT_STROKE_WIDTH;
    private AliyunICrop crop;
    private HorizontalListView listView;
    private VideoTrimFrameLayout frame;
    private TextureView textureview;
    private Surface mSurface;

    private MediaPlayer mPlayer;
    private TextView nextBtn;
    private TextView dirationTxt;
    private VideoTrimAdapter adapter;

    private VideoSliceSeekBar seekBar;

    private long videoPos;
    private long lastVideoSeekTime;
    private String path;
    private String outputPath;
    private long duration;
    private int resolutionMode;
    private int ratioMode;
    private VideoQuality quality = VideoQuality.HD;
    private int frameRate;
    private int gop;
    private int mBitrate;

    private int screenWidth;
    private int screenHeight;
    private int frameWidth;
    private int frameHeight;
    private int mScrollX;
    private int mScrollY;
    private int videoWidth;
    private int videoHeight;
    private int cropDuration = 3000;
    private ScaleMode cropMode = ScaleMode.PS;

    private long mStartTime;
    private long mEndTime;

    private int maxDuration = Integer.MAX_VALUE;

    private FrameExtractor10 kFrame;
    private Handler playHandler = new Handler(this);
    private int currentPlayPos;

    private boolean isPause = false;
    private boolean isCropping = false;
    private boolean needPlayStart = false;
    private boolean isUseGPU = false;

    private int mAction = CropKey.ACTION_TRANSCODE;
    private Transcoder mTransCoder;
    private AliyunIImport mImport;
    private AliyunVideoParam mVideoParam;
    private ScaleMode scaleMode = ScaleMode.LB;
    private int mRatio;
    private int[] mOutputResolution = null;
    private boolean mIsReachedMaxDuration = false;
    private MediaInfo mCurrMediaInfo;
    private String corp_durtion= Tools.getStringToId(R.string.aliyun_corp_durtion);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.work_aliyun_svideo_activity_crop);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        crop = AliyunCropCreator.getCropInstance(this);
        crop.setCropCallback(this);
        getData();
        init();
        initView();
        initSurface();
        AliyunCommon.getInstance().addActivity(this);
    }

    private void getData() {
        if(AliyunCommon.getRecordTime("recordMinTime")>0){
            cropDuration=AliyunCommon.getRecordTime("recordMinTime")*1000;
        }
        mCurrMediaInfo= (MediaInfo) getIntent().getExtras().getSerializable("videoInfo");
        mRatio = getIntent().getIntExtra(CropKey.VIDEO_RATIO, CropKey.RATIO_MODE_9_16);
        mAction = getIntent().getIntExtra(CropKey.ACTION, CropKey.ACTION_TRANSCODE);
        path = getIntent().getStringExtra(CropKey.VIDEO_PATH);
        try {
            duration = crop.getVideoDuration(path) / 1000;
        } catch (Exception e) {
            ToastUtil.showToast(this, R.string.aliyun_video_crop_error);
        }//获取精确的视频时间
        resolutionMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, AliyunSnapVideoParam.RESOLUTION_540P);
        quality = (VideoQuality) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_QUALITY);
        if (quality == null) {
            quality = VideoQuality.HD;
        }
        gop = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_GOP, 5);
        mBitrate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_BITRATE, 0);
        frameRate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 25);
        cropDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, 3000);
        isUseGPU = getIntent().getBooleanExtra(AliyunSnapVideoParam.CROP_USE_GPU, false);
        mOutputResolution = resolutions[2];
        mVideoParam = new AliyunVideoParam.Builder()
                .frameRate(frameRate)
                .gop(gop)
                .bitrate(mBitrate)
                .videoQuality(quality)
                .scaleMode(scaleMode)
                .outputWidth(mOutputResolution[0])
                .outputHeight(mOutputResolution[1])
                .build();
    }
    public void init(){
        mTransCoder = new Transcoder();
        mTransCoder.init(this);
        if(mCurrMediaInfo!=null)mTransCoder.addMedia(mCurrMediaInfo);
        mTransCoder.setTransCallback(new Transcoder.TransCallback() {
            @Override
            public void onError(Throwable e, final int errorCode) {
                isShowCorp(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                                ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_not_supported_audio);
                                break;
                            case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                                ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_video_crop_error);
                                break;
                            case AliyunErrorCode.ERROR_UNKNOWN:
                            default:
                                ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_video_error);
                        }
                    }
                });

            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onComplete(List<MediaInfo> resultVideos) {
                if(isFinishing()){return;}

                long startCropTimestamp = System.currentTimeMillis();
                Log.d("xianghaTag", "start : 2:" + (startCropTimestamp-startTime));
                mImport = AliyunImportCreator.getImportInstance(AliyunVideoCrop.this);
                mImport.setVideoParam(mVideoParam);
                for (int i = 0; i < resultVideos.size(); i++) {
                    MediaInfo mediaInfo = resultVideos.get(i);
                    if (i == 0) {
                        if (mediaInfo.mimeType.startsWith("video")) {
                            mImport.addVideo(mediaInfo.filePath, mediaInfo.startTime, mediaInfo.startTime + mediaInfo.duration, 0, AliyunDisplayMode.DEFAULT);
                        } else if (mediaInfo.mimeType.startsWith("image")) {
                            mImport.addImage(mediaInfo.filePath, 0, 5000, AliyunDisplayMode.DEFAULT);
                        }
                    } else {
                        if (mediaInfo.mimeType.startsWith("video")) {
                            mImport.addVideo(mediaInfo.filePath, mediaInfo.startTime, mediaInfo.startTime + mediaInfo.duration, 600, AliyunDisplayMode.DEFAULT);
                        } else if (mediaInfo.mimeType.startsWith("image")) {
                            mImport.addImage(mediaInfo.filePath, 600, 5000, AliyunDisplayMode.DEFAULT);
                        }
                    }

                }
                String projectJsonPath = mImport.generateProjectConfigure();
                Log.i("xianghaTag","projectJsonPath:::"+projectJsonPath);
                isShowCorp(false);
                if (projectJsonPath != null) {
                    Intent intent = new Intent(AliyunVideoCrop.this, EditorActivity.class);
                    intent.putExtra("video_param", mVideoParam);
                    intent.putExtra("project_json_path", projectJsonPath);
                    intent.putExtra("videoPath", outputPath);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelComplete() {
                isShowCorp(false);
            }
        });
    }
    public static final String getVersion() {
        return Version.VERSION;
    }

    private void initView() {
        OUT_STROKE_WIDTH = DensityUtil.dip2px(this, 5);
        kFrame = new FrameExtractor10();
        kFrame.setDataSource(path);
        seekBar = (VideoSliceSeekBar) findViewById(R.id.aliyun_seek_bar);
        seekBar.setSeekBarChangeListener(this);
        int minDiff = (int) (cropDuration / (float) duration * 100) + 1;
        seekBar.setProgressMinDiff(minDiff > 100 ? 100 : minDiff);
        listView = (HorizontalListView) findViewById(R.id.aliyun_video_tailor_image_list);
        listView.setOnScrollCallBack(this);
        adapter = new VideoTrimAdapter(this, duration, maxDuration, kFrame, seekBar);
        listView.setAdapter(adapter);
        nextBtn = (TextView) findViewById(R.id.aliyun_next);
        nextBtn.setOnClickListener(this);
        dirationTxt = (TextView) findViewById(R.id.aliyun_duration_txt);
        dirationTxt.setText(corp_durtion+" "+setDurationStyleView((float) duration / 1000));
        setListViewHeight();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AliyunVideoCrop.this.finish();
            }
        });
        findViewById(R.id.progressBar_layout).setOnClickListener(v->{});
        handleShowHint();
        findViewById(R.id.show_hint_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.show_hint).setVisibility(View.GONE);
                FileManager.saveShared(XHApplication.in(),FileManager.video_corp_show_hint,FileManager.video_corp_show_hint,"1");
            }
        });
        ((ImageView)findViewById(R.id.leftImgBtn)).setImageResource(R.drawable.z_z_topbar_ico_back_white);
    }

    private void setListViewHeight() {
        LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
        layoutParams.height = screenWidth / 8;
        listView.setLayoutParams(layoutParams);
        seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, screenWidth / 8));
    }

    public void initSurface() {
        frame = (VideoTrimFrameLayout) findViewById(R.id.aliyun_video_surfaceLayout);
        frame.setOnSizeChangedListener(this);
        frame.setOnScrollCallBack(this);
        resizeFrame();
        textureview = (TextureView) findViewById(R.id.aliyun_video_textureview);
        textureview.setSurfaceTextureListener(this);
    }

    private void resizeFrame() {
        Log.i("xianghaTag","ratioMode:::"+ratioMode);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) frame.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth * 16 / 9;
        frame.setLayoutParams(layoutParams);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i("xianghaTag","onSurfaceTextureAvailable-------------");
        if (mPlayer == null) {
            mSurface = new Surface(surface);
            mPlayer = new MediaPlayer();
            mPlayer.setSurface(mSurface);
            try {
                mPlayer.setDataSource(path);
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (!isPause) {
                            playVideo();
                            playState = PLAY_VIDEO;
                        } else {
                            isPause = false;
                            mPlayer.start();
                            mPlayer.seekTo(currentPlayPos);
                            playHandler.sendEmptyMessageDelayed(PAUSE_VIDEO, 100);
                        }
                    }
                });
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.setOnVideoSizeChangedListener(this);

        }
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            mSurface = null;
        }
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void SeekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
        long seekPos = 0;
        if (whitchSide == 0) {
            seekPos = (long) (duration * leftThumb / 100);
            mStartTime = seekPos;
        } else if (whitchSide == 1) {
            seekPos = (long) (duration * rightThumb / 100);
            mEndTime = seekPos;
        }
        dirationTxt.setText(corp_durtion+" "+setDurationStyleView((float) (mEndTime - mStartTime) / 1000));
        if(mPlayer != null){
            mPlayer.seekTo((int) seekPos);
        }
    }

    @Override
    public void onSeekStart() {
        pauseVideo();
    }

    @Override
    public void onSeekEnd() {
        needPlayStart = true;
        if (playState == PLAY_VIDEO) {
            playVideo();
        }
    }

    private void resetScroll() {
        mScrollX = 0;
        mScrollY = 0;
    }

    @Override
    public void onScrollDistance(Long count, int distanceX) {}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (playState == PLAY_VIDEO) {
            pauseVideo();
            playState = PAUSE_VIDEO;
        }
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        msc.disconnect();
        mTransCoder.release();
        AliyunCropCreator.destroyCropInstance();
    }
    private void scaleCrop(int videoWidth, int videoHeight) {
        LayoutParams layoutParams = (LayoutParams) textureview.getLayoutParams();
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager manager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) manager.getDefaultDisplay().getMetrics(metric);
        layoutParams.width=videoWidth;
        layoutParams.height=videoHeight;
        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_CROP;
        resetScroll();
    }
    private void scaleFill(){
        LayoutParams layoutParams = (LayoutParams) textureview.getLayoutParams();
        int s = Math.min(videoWidth, videoHeight);
        int b = Math.max(videoWidth, videoHeight);
        float videoRatio = (float) b / s;
        float ratio = (float) 16 / 9;
        if (videoWidth > videoHeight) {
            layoutParams.width = frameWidth;
            layoutParams.height = frameWidth * videoHeight / videoWidth;
        } else {
            if (videoRatio >= ratio) {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * videoWidth / videoHeight;
            } else {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * videoHeight / videoWidth;
            }
        }
        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_FILL;
        resetScroll();
    }


    private void scanFile() {
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{outputPath}, new String[]{"video/mp4"}, null);
    }

    private void playVideo() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.seekTo((int) mStartTime);
        mPlayer.start();
        videoPos = mStartTime;
        lastVideoSeekTime = System.currentTimeMillis();
        playHandler.sendEmptyMessage(PLAY_VIDEO);
    }

    private void pauseVideo() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.pause();
        playHandler.removeMessages(PLAY_VIDEO);
        seekBar.showFrameProgress(false);
        seekBar.invalidate();
    }

    private void resumeVideo() {
        if (mPlayer == null) {
            return;
        }
        if (needPlayStart) {
            playVideo();
            needPlayStart = false;
            return;
        }
        mPlayer.start();
        playHandler.sendEmptyMessage(PLAY_VIDEO);
    }

    @Override
    public void onBackPressed() {
        if (isCropping) {
            crop.cancel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        frameWidth = frame.getWidth();
        frameHeight = frame.getHeight();
        Log.i("xianghaTag","frame.getWidth():_---------------:::"+frame.getWidth());
        videoWidth = width;
        videoHeight = height;
        mStartTime = 0;
        if (crop != null) {
            try {
                mEndTime = (long) (crop.getVideoDuration(path) * 1.0f / 1000);
            } catch (Exception e) {
                ToastUtil.showToast(this, R.string.aliyun_video_crop_error);
            }
        } else {
            mEndTime = Integer.MAX_VALUE;
        }
//        scaleCrop(width, height);
        mVideoParam.setOutputWidth(videoWidth);
        mVideoParam.setOutputHeight(videoHeight);
        scaleFill();
    }

    @Override
    public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
    }

    @Override
    public void onVideoScroll(float distanceX, float distanceY) {
        LayoutParams lp = (LayoutParams) textureview.getLayoutParams();
        int width = lp.width;
        int height = lp.height;

        if (width > frameWidth || height > frameHeight) {
            int maxHorizontalScroll = width - frameWidth;
            int maxVerticalScroll = height - frameHeight;
            if (maxHorizontalScroll > 0) {
                maxHorizontalScroll = maxHorizontalScroll / 2;
                mScrollX += distanceX;
                if (mScrollX > maxHorizontalScroll) {
                    mScrollX = maxHorizontalScroll;
                }
                if (mScrollX < -maxHorizontalScroll) {
                    mScrollX = -maxHorizontalScroll;
                }
            }
            if (maxVerticalScroll > 0) {
                maxVerticalScroll = maxVerticalScroll / 2;
                mScrollY += distanceY;
                if (mScrollY > maxVerticalScroll) {
                    mScrollY = maxVerticalScroll;
                }
                if (mScrollY < -maxVerticalScroll) {
                    mScrollY = -maxVerticalScroll;
                }
            }
            lp.setMargins(0, 0, mScrollX, mScrollY);
        }

        textureview.setLayoutParams(lp);
    }

    @Override
    public void onVideoSingleTapUp() {
        if (playState == END_VIDEO) {
            playVideo();
            playState = PLAY_VIDEO;
        } else if (playState == PLAY_VIDEO) {
            pauseVideo();
            playState = PAUSE_VIDEO;
        } else if (playState == PAUSE_VIDEO) {
            resumeVideo();
            playState = PLAY_VIDEO;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == nextBtn) {
            Log.i("xianghaTag","nextBtn:::");
            switch (mAction) {
                case CropKey.ACTION_TRANSCODE:
                    startCrop();
                    break;
                case CropKey.ACTION_SELECT_TIME:
                    if (!TextUtils.isEmpty(path) && duration > 0 && mCurrMediaInfo != null) {
                        isShowCorp(true);
                        int index = mTransCoder.removeMedia(mCurrMediaInfo);
                        mCurrMediaInfo.filePath = path;
                        mCurrMediaInfo.startTime = mStartTime;
                        mCurrMediaInfo.duration = (int) (mEndTime - mStartTime);
                        mTransCoder.addMedia(index, mCurrMediaInfo);
                        Log.i("xianghaTag","duration::"+mCurrMediaInfo.duration+"::startTime:"+mCurrMediaInfo.startTime);
                        videoTrans();
                    }
                    break;
            }
        }
    }
    private void videoTrans(){
        if (mIsReachedMaxDuration) {
            ToastUtil.showToast(this, R.string.message_max_duration_import);
            return;
        }
        //对于大于720P的视频需要走转码流程
        int videoCount = mTransCoder.getVideoCount();
        Log.i("xianghaTag","videoCount::"+videoCount);
        if (videoCount > 0) {
            mTransCoder.init(this);
            mTransCoder.setTransResolution(0, 0);
            mTransCoder.transcode(mOutputResolution, quality, scaleMode);
        } else {
            ToastUtil.showToast(this, R.string.please_select_video);
        }
    }

    private long startTime;
    private void startCrop() {
        if((mEndTime-mStartTime)>20000){
            dialogShow();
            return;
        }
        if (frameWidth == 0 || frameHeight == 0) {
            ToastUtil.showToast(this, R.string.aliyun_video_crop_error);
            isCropping = false;
            return;
        }
        if (isCropping) {
            return;
        }
        isShowCorp(true);
        if(videoWidth>1080){//对大于1080p--同比压缩到1080p
            float percet= 1080/(float)videoWidth;
            videoWidth= (int) (Float.valueOf(videoWidth)*percet);
            videoHeight = (int) (Float.valueOf(videoHeight)*percet);
            mVideoParam.setOutputWidth(videoWidth);
            mVideoParam.setOutputHeight(videoHeight);
        }
        Log.i("xianghaTag","videoWidth:::"+videoWidth+":::videoHeight::"+videoHeight);
//        outputPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "crop_" + System.currentTimeMillis() + ".mp4";
        outputPath =FileManager.getSDCacheDir()+"crop_"+System.currentTimeMillis() + ".mp4";
        CropParam cropParam = new CropParam();
        cropParam.setOutputPath(outputPath);
        cropParam.setInputPath(path);
        cropParam.setOutputWidth(videoWidth);
        cropParam.setOutputHeight(videoHeight);
        Rect cropRect = new Rect(0, 0, videoWidth, videoHeight);
        cropParam.setCropRect(cropRect);
        cropParam.setStartTime(mStartTime * 1000);
        cropParam.setEndTime(mEndTime * 1000);
        cropParam.setScaleMode(cropMode);
        cropParam.setFrameRate(frameRate);
        cropParam.setGop(gop);
        cropParam.setVideoBitrate(mBitrate);
        cropParam.setQuality(quality);
        cropParam.setFillColor(Color.BLACK);
        cropParam.setUseGPU(isUseGPU);
        crop.setCropParam(cropParam);

        AliyunCommon.corpPath=outputPath;
        startTime= System.currentTimeMillis();
        int ret = crop.startCrop();
        if (ret < 0) {
            ToastUtil.showToast(this, getString(R.string.aliyun_video_crop_error) + "错误码 ：" + ret);
            return;
        }
        long startCropTimestamp = System.currentTimeMillis();
        isCropping = true;
        seekBar.setSliceBlocked(true);
    }
    long startCropTimestamp;
    private void deleteFile() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                FileUtils.deleteFile(outputPath);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void dialogShow(){
        DialogManager dialogManager = new DialogManager(this);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleView(this).setText("裁剪的视频要不大于20秒"))
                .setView(new HButtonView(this).setNegativeText("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogManager.cancel();
                    }
                }))).show();
    }

    @Override
    public void onProgress(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
        Log.i("xianghaTag","percent::"+percent);
    }

    @Override
    public void onError(final int code) {
        Log.d("CROP_COST", "crop failed : " + code);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setSliceBlocked(false);
                switch (code) {
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                        ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_video_crop_error);
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                        ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_not_supported_audio);
                        break;
                }
                setResult(Activity.RESULT_CANCELED, getIntent());
            }
        });
        isCropping = false;

    }

    @Override
    public void onComplete(long duration) {
        long time = System.currentTimeMillis();
        Log.d("CROP_COST", "completed : " + (time - startCropTimestamp));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isFinishing()){return;}
                long startCropTimestamp = System.currentTimeMillis();
                Log.d("xianghaTag", "start : " + (startCropTimestamp-startTime));
                seekBar.setSliceBlocked(false);
                scanFile();
                if (!TextUtils.isEmpty(outputPath)  && mCurrMediaInfo != null) {
                    int index = mTransCoder.removeMedia(mCurrMediaInfo);
                    mCurrMediaInfo.filePath = outputPath;
                    mCurrMediaInfo.startTime = 0;
                    mCurrMediaInfo.duration = (int) (mEndTime - mStartTime);
                    mTransCoder.addMedia(index, mCurrMediaInfo);
                    Log.i("xianghaTag","duration::"+mCurrMediaInfo.duration+"::startTime:"+mCurrMediaInfo.startTime);
                    videoTrans();
                }
//                progressDialog.dismiss();
            }
        });
        isCropping = false;
    }

    @Override
    public void onCancelComplete() {
        //取消完成
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setSliceBlocked(false);
            }
        });
        deleteFile();
        setResult(Activity.RESULT_CANCELED);
        finish();
        isCropping = false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case PAUSE_VIDEO:
                pauseVideo();
                playState = PAUSE_VIDEO;
                break;
            case PLAY_VIDEO:
                if (mPlayer != null) {
                    currentPlayPos = (int) (videoPos + System.currentTimeMillis() - lastVideoSeekTime);
                    if (currentPlayPos < mEndTime) {
                        seekBar.showFrameProgress(true);
                        seekBar.setFrameProgress(currentPlayPos / (float) duration);
                        playHandler.sendEmptyMessageDelayed(PLAY_VIDEO, 100);
                    } else {
                        playVideo();
                    }
                }
                break;
        }
        return false;
    }

    /**
     * 是否在裁剪中
     * @param state true 在裁剪中，false未在
     */
    private void isShowCorp(boolean state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(state){
                    findViewById(R.id.progressBar_layout).setVisibility(View.VISIBLE);
                    nextBtn.setEnabled(false);
                }else{
                    findViewById(R.id.progressBar_layout).setVisibility(View.GONE);
                    nextBtn.setEnabled(true);
                }
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        AliyunCommon.getInstance().finishActivity(this);
        AliyunCommon.getInstance().deleteCropVideo();
    }

    public String setDurationStyleView(float duration){
        String time="";
        if(duration>0){
            if(duration>60){
                int index= (int) (duration/60);
                int indexTwo= (int) (duration-index*60);
                time = index>=10?String.valueOf(index):"0"+index;
                time +=":"+(indexTwo>=10?indexTwo:"0"+indexTwo);
            }else{
                time="00:"+(duration>=10?((int)duration):"0"+((int)duration));
            }
        }
        return time;
    }

    private void handleShowHint(){
        String hint = (String) FileManager.loadShared(XHApplication.in(),FileManager.video_corp_show_hint,FileManager.video_corp_show_hint);
        if(TextUtils.isEmpty(hint)){
            findViewById(R.id.show_hint).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.show_hint).setVisibility(View.GONE);
        }
    }
}
