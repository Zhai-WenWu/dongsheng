package com.quze.videorecordlib;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.qupai.editor.AliyunICompose;
import com.aliyun.qupai.import_core.AliyunIImport;
import com.aliyun.qupai.import_core.AliyunImportCreator;
import com.aliyun.recorder.AliyunRecorderCreator;
import com.aliyun.recorder.supply.AliyunIClipManager;
import com.aliyun.recorder.supply.AliyunIRecorder;
import com.aliyun.recorder.supply.RecordCallback;
import com.aliyun.struct.common.AliyunDisplayMode;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.effect.EffectFilter;
import com.aliyun.struct.encoder.VideoCodecs;
import com.aliyun.struct.recorder.CameraParam;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.recorder.FlashType;
import com.aliyun.struct.recorder.MediaInfo;
import com.aliyun.struct.snap.AliyunSnapVideoParam;
import com.qu.preview.callback.OnFrameCallBack;
import com.qu.preview.callback.OnTextureIdCallBack;
import com.quze.videorecordlib.util.AlbumNotifyHelper;
import com.quze.videorecordlib.util.CameraUtil;
import com.quze.videorecordlib.util.ComposeFactory;
import com.quze.videorecordlib.util.LocationUtil;
import com.quze.videorecordlib.util.Util;
import com.quze.videorecordlib.widget.AliyunSVideoGlSurfaceView;
import com.quze.videorecordlib.widget.RecordTimeCircleView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.aliyun.struct.snap.AliyunSnapVideoParam.RATIO_MODE_9_16;
import static com.quze.videorecordlib.util.CameraUtil.CAMERA_FACING_FRONT;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoRecorder extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    public static final String EXTRA_BASE_OUTPUT_PATH = "baseOutputPath";

    private static final int MAX_SWITCH_VELOCITY = 2000;
    private static final float FADE_IN_START_ALPHA = 0.3f;
    private static final int FILTER_ANIMATION_DURATION = 1000;

    public static final String NEED_GALLERY = "need_gallery";
    public static final String NEED_DRAFT = "need_draft";
    public static final String RESULT_TYPE = "result_type";
    private static final int REQUEST_CROP = 2001;
    public static final int RESULT_TYPE_CROP = 4001;
    public static final int RESULT_TYPE_RECORD = 4002;

    //阿里云短视频片段合成工具
    private AliyunICompose mCompose;
    private int mResolutionMode = 3;
    private int mMinDuration = 3000;
    private int mMaxDuration = 30000;
    private int mGop = 5;
    private int mBitrate = 25;
    private int mRecordMode;
    private VideoQuality mVideoQuality = VideoQuality.SSD;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private int mRatioMode = RATIO_MODE_9_16;
    private AliyunIRecorder mRecorder;
    private AliyunIClipManager mClipManager;
    private AliyunSVideoGlSurfaceView mGlSurfaceView;
    private boolean isSelected = false;

    private RecordTimeCircleView mRecordTimelineView;
    private LinearLayout mSwitchCameraBtn;
    private LinearLayout mSwitchLightBtn;
    private LinearLayout mDownload;
    private ImageView mBackBtn;
    private ImageView mRecordBtn;
    private ImageView mDeleteBtn;
    private ImageView mCompleteBtn;
    private View mRecordPoint;
    //合成progress布局
    private RelativeLayout progressBayLayout;
    //相册
    private RelativeLayout mAliyunAblum;
    //草稿
    private RelativeLayout mDraftLayout;
    private ProgressBar progressBar;
    private TextView mRecordTimeTxt;
    private LinearLayout mRecordTimeLayout;
    private FlashType mFlashType = FlashType.OFF;
    private CameraType mCameraType = CameraType.BACK;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float mScaleFactor;
    private float mLastScaleFactor;
    private float mExposureCompensationRatio = 0.5f;
    private boolean isOnMaxDuration;
    private boolean isOpenFailed;
    private boolean isRecording = false;
    private AliyunVideoParam mVideoParam;
    private int mTintColor, mTimelineDelBgColor, mTimelineBgColor, mLightSwitchRes;
    private long mDownTime;
    private String[] mFilterList;
    private int mFilterIndex = 0;
    private TextView mFilterTxt;
    private boolean isNeedGallery;
    private boolean isNeedDraft;
    private boolean isRecordError;
    private boolean isFinishRecording;
    private MediaScannerConnection msc;

    private int mFrame = 25;
    private ScaleMode mCropMode = ScaleMode.PS;
    int time;    //当前录制时长，单位s

    private String mOutputPath;   //视频合成路径
    private String baseOutputPath;
    public double latitude = Double.MAX_VALUE;
    public double longitude = Double.MAX_VALUE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_record);
        getStyleParam();
        getData();
        initView();
        initSDK();
        initMsc();
        getLocation();
        VideoRecorderCommon.instance().addActivity(this);
    }

    private void getLocation() {
        LocationUtil.instance().getLocation(getApplicationContext(), new LocationUtil.LocationCallback() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    private void initMsc() {
        msc = new MediaScannerConnection(this, null);
        msc.connect();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static void startRecord(Context context, AliyunSnapVideoParam param, String baseOutputPath, boolean isNeedDraft, boolean isNeedGallery) {
        Intent intent = new Intent(context, VideoRecorder.class);
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.RECORD_MODE, param.getRecordMode());
        intent.putExtra(AliyunSnapVideoParam.FILTER_LIST, param.getFilterList());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, param.getBeautyLevel());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_STATUS, param.getBeautyStatus());
        intent.putExtra(AliyunSnapVideoParam.CAMERA_TYPE, param.getCameraType());
        intent.putExtra(AliyunSnapVideoParam.FLASH_TYPE, param.getFlashType());
        intent.putExtra(AliyunSnapVideoParam.NEED_CLIP, param.isNeedClip());
        intent.putExtra(AliyunSnapVideoParam.MAX_DURATION, param.getMaxDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_DURATION, param.getMinDuration());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, param.getVideoBitrate());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, param.getVideoCodec());

        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, param.getScaleMode());
        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, param.getMinCropDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, param.getMinVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, param.getMaxVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());

        intent.putExtra(EXTRA_BASE_OUTPUT_PATH, baseOutputPath);
        intent.putExtra(NEED_GALLERY, isNeedGallery);
        intent.putExtra(NEED_DRAFT, isNeedDraft);

        context.startActivity(intent);
    }

    private void getStyleParam() {
        TypedArray a = obtainStyledAttributes(new int[]{
                R.attr.qusnap_tint_color, R.attr.qusnap_timeline_del_backgound_color,
                R.attr.qusnap_timeline_backgound_color, R.attr.qusnap_time_line_pos_y,
                R.attr.qusnap_switch_light_icon_disable, R.attr.qusnap_switch_light_icon,
                R.attr.qusnap_gallery_icon_visibility});
        mTintColor = a.getResourceId(0, R.color.aliyun_record_fill_progress);
        mTimelineDelBgColor = a.getResourceId(1, R.color.aliyun_record_fill_progress_select);
        mTimelineBgColor = a.getResourceId(2, R.color.aliyun_record_fill_progress_back);
        mLightSwitchRes = a.getResourceId(5, R.drawable.aliyun_svideo_switch_light_selector);
        a.recycle();
    }


    private void initView() {
        FrameLayout topbar = findViewById(R.id.aliyun_tools_bar);
//        topbar.setBackground(
//                ScrimUtil.makeCubicGradientScrimDrawable(
//                        Color.parseColor("#80000000"), //顏色
//                        8, //漸層數
//                        Gravity.TOP)); //起始方向
        topbar.getLayoutParams().height += Util.getStatusBarHeight(this);
        topbar.setPadding(0, Util.getStatusBarHeight(this), 0, 0);
        mBackBtn = findViewById(R.id.aliyun_back);
        mBackBtn.setOnClickListener(this);

        mGlSurfaceView = findViewById(R.id.aliyun_preview);
        mGlSurfaceView.setOnTouchListener(this);

        mSwitchCameraBtn = findViewById(R.id.aliyun_switch_camera_layout);
        mSwitchCameraBtn.setOnClickListener(this);

        mDownload = findViewById(R.id.aliyun_download_layout);
        mDownload.setOnClickListener(this);

        mSwitchLightBtn = findViewById(R.id.aliyun_switch_light_layout);
        ImageView swichLighticon = findViewById(R.id.aliyun_switch_light);
        swichLighticon.setImageResource(mLightSwitchRes);
        mSwitchLightBtn.setOnClickListener(this);
        setFlashType(mFlashType);

        mRecordBtn = findViewById(R.id.aliyun_record_btn);
//        mRecordBtn.setOnTouchListener(this);
        mRecordBtn.setOnClickListener(this);

        mRecordTimeLayout = findViewById(R.id.time_layout);
        mRecordTimeTxt = findViewById(R.id.aliyun_record_time);
        mDeleteBtn = findViewById(R.id.aliyun_delete_btn);
        mDeleteBtn.setOnClickListener(this);

        mCompleteBtn = findViewById(R.id.aliyun_complete_btn);
        mCompleteBtn.setOnClickListener(this);

        mRecordPoint = findViewById(R.id.aliyun_record_point);

        mRecordTimelineView = findViewById(R.id.aliyun_record_timeline);
        mRecordTimelineView.setColor(mTintColor, mTimelineDelBgColor, android.R.color.white, mTimelineBgColor);

        mAliyunAblum = findViewById(R.id.aliyun_ablum);
        mAliyunAblum.setOnClickListener(this);
        showGallery(true);
        mDraftLayout = findViewById(R.id.aliyun_draft);
        mDraftLayout.setOnClickListener(this);
        showDraftLayout(true);

        mFilterTxt = findViewById(R.id.aliyun_filter_txt);
        mFilterTxt.setVisibility(View.GONE);

        scaleGestureDetector = new ScaleGestureDetector(this, this);
        gestureDetector = new GestureDetector(this, this);

        progressBayLayout = findViewById(R.id.progressBar_layout);
        //设置点击事件的原因是为了屏蔽合成视频时用户的各种点击事件
        progressBayLayout.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        ImageView mGalleryBtn = findViewById(R.id.aliyun_icon_default);
        Bitmap b = Util.getFirstVideoBitmap(this);
        if (b != null) {
            mGalleryBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mGalleryBtn.setImageBitmap(b);
            findViewById(R.id.aliyun_icon_default_line).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.aliyun_icon_default_line).setVisibility(View.INVISIBLE);
        }
    }

    private void showGallery(boolean isShow) {
        mAliyunAblum.setVisibility(isShow && isNeedGallery ? View.VISIBLE : View.GONE);
    }

    private void showDraftLayout(boolean isShow) {
        //TODO 草稿箱
        mDraftLayout.setVisibility(isShow && isNeedDraft ? View.GONE : View.GONE);
    }

    private void initSDK() {
        mRecorder = AliyunRecorderCreator.getRecorderInstance(this);
        mRecorder.setDisplayView(mGlSurfaceView);
        mRecorder.setOnFrameCallback(new OnFrameCallBack() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {
                isOpenFailed = false;
            }

            @Override
            public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes, Camera.Size preferredPreviewSizeForVideo) {
                return null;
            }

            @Override
            public void openFailed() {
                isOpenFailed = true;
            }
        });
        mRecorder.setOnTextureIdCallback(new OnTextureIdCallBack() {
            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                return textureId;
            }

            @Override
            public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
                return scaledId;
            }
        });
        mClipManager = mRecorder.getClipManager();
        mClipManager.setMinDuration(mMinDuration);
        mClipManager.setMaxDuration(mMaxDuration);
        mRecordTimelineView.setMaxDuration(mClipManager.getMaxDuration());
        mRecordTimelineView.setMinDuration(mClipManager.getMinDuration());
        int[] resolution = getResolution();
        final MediaInfo info = new MediaInfo();
        info.setVideoWidth(resolution[0]);
        info.setVideoHeight(resolution[1]);
        info.setVideoCodec(mVideoCodec);
        info.setCrf(25);
        mRecorder.setMediaInfo(info);
//        if(!TextUtils.isEmpty(baseOutputPath)){
//            mOutputPath = baseOutputPath + "record_" + System.currentTimeMillis() + ".mp4";
//            mRecorder.setOutputPath(mOutputPath);
//        }
        if (mRecorder.getCameraCount() == 1) {
            mCameraType = CameraType.BACK;
            showSwitchCamera(false);
            mSwitchLightBtn.setVisibility(View.VISIBLE);
        } else if (CameraUtil.isSupportSize(CAMERA_FACING_FRONT, resolution[0], resolution[1])) {
            if (mCameraType == CameraType.FRONT) {
                mSwitchLightBtn.setVisibility(View.INVISIBLE);
            }
        }
        mRecorder.setCamera(mCameraType);
        mRecorder.setGop(mGop);
        mRecorder.setVideoBitrate(mBitrate);
        mRecorder.setVideoQuality(mVideoQuality);

        mRecorder.setRecordCallback(new RecordCallback() {
            @Override
            public void onComplete(boolean validClip, long clipDuration) {
                handleRecordCallback(validClip, clipDuration);
                if (isOnMaxDuration) {
                    isOnMaxDuration = false;
//                    mRecorder.finishRecording();
                }
            }

            @Override
            public void onFinish(String outputPath) {
                handleRecordStop();
                isShowEdit(false);
                isFinishRecording = true;
                mOutputPath = outputPath;
                toEditor();
            }

            @Override
            public void onProgress(final long duration) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordTimelineView.setDuration((int) duration);
                        updateRecordTime(duration);
                    }
                });
            }

            @Override
            public void onMaxDuration() {
                isOnMaxDuration = true;
            }

            @Override
            public void onError(int errorCode) {
                isRecordError = true;
                handleRecordCallback(false, 0);
            }

            @Override
            public void onInitReady() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFilterList != null && mFilterList.length > mFilterIndex) {
                            EffectFilter effectFilter = new EffectFilter(mFilterList[mFilterIndex]);
                            mRecorder.applyFilter(effectFilter);
                        }
                    }
                });
            }

            @Override
            public void onDrawReady() {

            }

            @Override
            public void onPictureBack(Bitmap bitmap) {

            }

            @Override
            public void onPictureDataBack(byte[] data) {

            }
        });

        setRecordMode(getIntent().getIntExtra(AliyunSnapVideoParam.RECORD_MODE, AliyunSnapVideoParam.RECORD_MODE_AUTO));
        setFilterList(getIntent().getStringArrayExtra(AliyunSnapVideoParam.FILTER_LIST));
        int level = getIntent().getIntExtra(AliyunSnapVideoParam.BEAUTY_LEVEL,80);
        mRecorder.setBeautyLevel(level);
        setCameraType((CameraType) getIntent().getSerializableExtra(AliyunSnapVideoParam.CAMERA_TYPE));
        setFlashType((FlashType) getIntent().getSerializableExtra(AliyunSnapVideoParam.FLASH_TYPE));
        mRecorder.setExposureCompensationRatio(mExposureCompensationRatio);
        mRecorder.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE);
    }

    @SuppressLint("DefaultLocale")
    private void updateRecordTime(long appendTime) {
        time = (int) (mClipManager.getDuration() + appendTime) / 1000;
        int min = time / 60;
        int sec = time % 60;
        mRecordTimeTxt.setText(String.format("%1$02d:%2$02d", min, sec));
        if (mRecordTimeLayout.getVisibility() != View.VISIBLE) {
            mRecordTimeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //部分android4.4机型会出现跳转Activity gl为空的问题，如果不需要适配，显示视图代码可以去掉
        mGlSurfaceView.setVisibility(View.VISIBLE);
        mRecorder.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            mRecorder.cancelRecording();
            isRecording = false;
        }
        mRecorder.stopPreview();
        //部分android4.4机型会出现跳转Activity gl为空的问题，如果不需要适配，隐藏视图代码可以去掉
        mGlSurfaceView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void getData() {
        mResolutionMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, 4);
        mMinDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_DURATION, mMinDuration);
        mMaxDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_DURATION, mMaxDuration);
        mRatioMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RATIO, mRatioMode);
        mGop = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_GOP, mGop);
        mBitrate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_BITRATE, mBitrate);
        mVideoQuality = (VideoQuality) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_QUALITY);
        if (mVideoQuality == null) {
            mVideoQuality = VideoQuality.HD;
        }
        mVideoCodec = (VideoCodecs) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_CODEC);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
        isNeedGallery = getIntent().getBooleanExtra(NEED_GALLERY, true);
        isNeedDraft = getIntent().getBooleanExtra(NEED_GALLERY, false);
        //裁剪参数
        mFrame = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 25);
        mVideoParam = new AliyunVideoParam.Builder()
                .gop(mGop)
                .bitrate(mBitrate)
                .frameRate(mFrame)
                .videoQuality(mVideoQuality)
                .videoCodec(mVideoCodec)
                .outputWidth(720)
                .outputHeight(1280)
                .build();
        mCropMode = (ScaleMode) getIntent().getSerializableExtra(AliyunSnapVideoParam.CROP_MODE);
        if (mCropMode == null) {
            mCropMode = ScaleMode.PS;
        }
        baseOutputPath = getIntent().getStringExtra(EXTRA_BASE_OUTPUT_PATH);
        isNeedGallery = getIntent().getBooleanExtra(NEED_GALLERY, isNeedGallery);
        isNeedDraft = getIntent().getBooleanExtra(NEED_DRAFT, isNeedDraft);
    }

    public void setRecordMode(int recordMode) {
        this.mRecordMode = recordMode;
    }

    public void setFilterList(String[] filterList) {
        this.mFilterList = filterList;
    }

    public void setCameraType(CameraType cameraType) {
        if (cameraType == null) {
            return;
        }
        mCameraType = cameraType;
        if (mRecorder != null)
            mRecorder.setCamera(cameraType);
    }

    public void setFlashType(FlashType flashType) {
        if (flashType == null) {
            return;
        }
        if (mCameraType == CameraType.FRONT) {
            showSwitchLight(false);
            return;
        } else if (mCameraType == CameraType.BACK) {
            showSwitchLight(true);
        }
        mFlashType = flashType;
        switch (mFlashType) {
            case ON:
                switchOpenLight();
                break;
            case OFF:
                switchCloseLight();
                break;
            default:
                break;
        }
    }

    private int[] getResolution() {
        int[] resolution = new int[2];
        int width = 0;
        int height = 0;
        switch (mResolutionMode) {
            case AliyunSnapVideoParam.RESOLUTION_360P:
                width = 360;
                break;
            case AliyunSnapVideoParam.RESOLUTION_480P:
                width = 480;
                break;
            case AliyunSnapVideoParam.RESOLUTION_540P:
                width = 540;
                break;
            case AliyunSnapVideoParam.RESOLUTION_720P:
                width = 720;
                break;
            case 4:
                width = 1080;
            default:
                break;
        }
        switch (mRatioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                height = width;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                height = width * 4 / 3;
                break;
            case RATIO_MODE_9_16:
                height = width * 16 / 9;
                break;
        }
        resolution[0] = width;
        resolution[1] = height;
        return resolution;
    }


    @Override
    public void onBackPressed() {
        if (isRecording) {
            return;
        }
        if (hasRecorderClipPart()) {   //判断拍摄视频片段个数
            showBackDialog();
        } else {
            finish();
        }
    }

    private boolean hasRecorderClipPart() {
        return mRecorder != null && mRecorder.getClipManager().getPartCount() > 0;
    }

    AlertDialog backDialog;

    private void showBackDialog() {
        if (backDialog == null) {
            backDialog = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确认退出？")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backDialog.cancel();
                        }
                    })
                    .setNeutralButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mRecorder != null) {
                                mRecorder.getClipManager().deleteAllPart();//删除所有临时文件
                            }
                            finish();
                        }
                    })
                    .setPositiveButton("重新拍摄", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetRecorder();
                        }
                    }).create();
        }
        backDialog.show();
    }

    private void resetRecorder() {
        if (mRecorder != null) {
            mRecorder.getClipManager().deleteAllPart();//删除所有临时文件
            mRecordTimelineView.deteleAll();
            mRecordTimeTxt.setText("");
            handleUIChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backDialog != null && backDialog.isShowing()) {
            backDialog.dismiss();
        }
        mRecorder.getClipManager().deleteAllPart();//删除所有临时文件
        mRecorder.destroy();
        msc.disconnect();
        AliyunRecorderCreator.destroyRecorderInstance();
    }

    private void switchBackCamera() {
        mCameraType = CameraType.BACK;
        setCameraType(mCameraType);
    }

    private void switchFrontCamera() {
        mCameraType = CameraType.FRONT;
        setCameraType(mCameraType);
    }

    private void showSwitchCamera(boolean isShow) {
        mSwitchCameraBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mSwitchCameraBtn.setEnabled(isShow);
    }

    private void switchOpenLight() {
        mFlashType = FlashType.ON;
        mSwitchLightBtn.setSelected(true);
        if (mRecorder != null)
            mRecorder.setLight(mFlashType);
        showSwitchLight(true);
    }

    private void switchCloseLight() {
        mFlashType = FlashType.OFF;
        mSwitchLightBtn.setSelected(false);
        if (mRecorder != null)
            mRecorder.setLight(mFlashType);
        showSwitchLight(true);
    }

    private void showSwitchLight(boolean isShow) {
        mSwitchLightBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mSwitchLightBtn.setEnabled(isShow);
    }

    @Override
    public void onClick(View v) {
        if (v == mSwitchCameraBtn) {
            //如果点击了翻转摄像头
            int type = mRecorder.switchCamera();
            if (type == CameraType.BACK.getType()) {
                switchBackCamera();
                showSwitchLight(true);
            } else if (type == CameraType.FRONT.getType()) {
                switchFrontCamera();
                showSwitchLight(false);
            }
        } else if (v == mSwitchLightBtn) {
            //切换闪光灯
            if (mFlashType == FlashType.OFF) {
                switchOpenLight();
            } else if (mFlashType == FlashType.ON || mFlashType == FlashType.TORCH) {
                switchCloseLight();
            }
        } else if (v == mDownload) {
            saveToLocal();
        } else if (v == mBackBtn) {
            onBackPressed();
            VideoRecorderCommon.instance().statictisEvent("a_preshoot","取消按钮","");
        } else if (v == mCompleteBtn) {
            handleComplete();
            VideoRecorderCommon.instance().statictisEvent("a_shoot_pause","下一步","");
        } else if (v == mDeleteBtn) {
            handleDelete();
            VideoRecorderCommon.instance().statictisEvent("a_shoot_pause","回删按钮","");
        } else if (v == mDraftLayout) {
            VideoRecorderCommon.instance().handleStartDarftActivityCallback();
        } else if (v == mAliyunAblum) {
            VideoRecorderCommon.instance().handleStartMediaActivityCallback();
            VideoRecorderCommon.instance().statictisEvent("a_preshoot","本地上传按钮","");
        } else if (v == mRecordBtn) {
            if (!isRecording) {
                if (!checkIfStartRecording()) {
                    return;
                }
                mRecordBtn.setHovered(true);
                startRecording();
                VideoRecorderCommon.instance().statictisEvent("a_preshoot","开拍按钮","");
                isRecording = true;
            } else {
                VideoRecorderCommon.instance().statictisEvent("a_shooting","暂停按钮","");
                stopRecording();
                isRecording = false;
            }
        }
    }

    private void handleDelete() {
        if (!isSelected) {
            mRecordTimelineView.selectLast();
            mDeleteBtn.setActivated(true);
            isSelected = true;
            showDeleteCropDialog();
        }
    }

    private void handleComplete() {
        if (mClipManager.getDuration() < mClipManager.getMinDuration()) {
            Toast.makeText(this, "视频不能小于" + mMinDuration / 1000 + "s", Toast.LENGTH_SHORT).show();
            return;
        }
        mRecorder.finishRecording();
    }

    /** 显示删除对话框 */
    private void showDeleteCropDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mRecorder.getClipManager().getPartCount() > 0) {
            builder.setTitle("提示");
            builder.setMessage("确定删除上一段视频？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteLastCrop();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mRecordTimelineView.cancleSelectLast();
                    mDeleteBtn.setActivated(false);
                    isSelected = false;
                }
            });
            builder.create().show();
        }
    }

    /** 删除最后一段视频片段 */
    private void deleteLastCrop() {
        mRecordTimelineView.deleteLast();
        mDeleteBtn.setActivated(false);
        mClipManager.deletePart();
        isSelected = false;
        showComplete();
        if (mClipManager.getDuration() == 0) {
            showGallery(true);
            showDraftLayout(true);
            mCompleteBtn.setVisibility(View.GONE);
            mDeleteBtn.setVisibility(View.GONE);
        }
        updateRecordTime(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            data.putExtra(RESULT_TYPE, RESULT_TYPE_CROP);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    private int getPictureRotation() {
        int rotation = 90;
//        if ((orientation >= 45) && (orientation < 135)) {
//            rotation = 180;
//        }
//        if ((orientation >= 135) && (orientation < 225)) {
//            rotation = 270;
//        }
//        if ((orientation >= 225) && (orientation < 315)) {
//            rotation = 0;
//        }
        if (mCameraType == CameraType.FRONT) {
            if (rotation != 0) {
                rotation = 360 - rotation;
            }
        }
        return rotation;
    }

    private void toEditor() {
        isShowEdit(true);
        if (mCompose == null) {
            mCompose = ComposeFactory.INSTANCE.getInstance();
            mCompose.init(this);
        }
        Uri projectUri = mRecorder.finishRecordingForEdit();
        composeVideo(projectUri);
    }

    /**
     * 合成视频
     *
     * @param projectUri
     */
    private void composeVideo(final Uri projectUri) {
        //判断是否直接进入裁剪界面
        if (!TextUtils.isEmpty(mOutputPath) && isFinishRecording) {
            isShowEdit(false);
            toVideoCrop();
        } else {
            mOutputPath = baseOutputPath + "v_" + System.currentTimeMillis() + ".mp4";
            Log.i("xianghaTag", "mOutputPath::" + mOutputPath);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCompose.compose(projectUri.getPath(), mOutputPath, mCallback);
                }
            });
        }
    }

    /** 合成进度回掉 */
    private final AliyunICompose.AliyunIComposeCallBack mCallback = new AliyunICompose.AliyunIComposeCallBack() {
        @Override
        public void onComposeError(int errorCode) {
            Log.i("xianghaTag", "合成错误：：" + errorCode);
            ToastUtil.showToast(VideoRecorder.this, "合成错误，请重试");
            isShowEdit(false);
        }

        @Override
        public void onComposeProgress(final int progress) {
            Log.i("xianghaTag", "合成进度：：" + progress);
            progressBar.setProgress(progress);
        }

        @Override
        public void onComposeCompleted() {
            Log.i("xianghaTag", "合成完成：：");
            if (isFinishing()) return;
            //进行视频图片截取。
            isShowEdit(false);
            ToastUtil.showToast(VideoRecorder.this, "合成完成");
            toVideoCrop();
        }
    };

    private void saveToLocal() {
        Log.i("xianghaTag", "mOutputPath::" + mOutputPath);
        if (mRecorder == null || mRecorder.getClipManager().getDuration() < mMinDuration) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isShowEdit(true);
                if (mCompose == null) {
                    mCompose = ComposeFactory.INSTANCE.getInstance();
                    mCompose.init(VideoRecorder.this);
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_hhMMss");
                final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/quze_" + simpleDateFormat.format(new Date()) + ".mp4";
                Uri projectUri = mRecorder.finishRecordingForEdit();
                mCompose.compose(projectUri.getPath(), mOutputPath, new AliyunICompose.AliyunIComposeCallBack() {
                    @Override
                    public void onComposeError(int errorCode) {
                        Log.i("xianghaTag", "合成错误：：" + errorCode);
                        ToastUtil.showToast(VideoRecorder.this, "合成错误，请重试");
                        isShowEdit(false);
                    }

                    @Override
                    public void onComposeProgress(final int progress) {
                        Log.i("xianghaTag", "合成进度：：" + progress);
                        progressBar.setProgress(progress);
                    }

                    @Override
                    public void onComposeCompleted() {
                        Log.i("xianghaTag", "合成完成：：");
                        if (isFinishing()) return;
                        //进行视频图片截取。
                        isShowEdit(false);
                        try {
                            ToastUtil.showToast(VideoRecorder.this, "已保存");
                            copyFile(mOutputPath, filePath);
                            AlbumNotifyHelper.insertVideoToMediaStore(VideoRecorder.this, filePath, System.currentTimeMillis(), 0, 0,
                                    mRecorder.getClipManager().getDuration(), latitude, longitude);
                            //重置拍摄状态
                            resetRecorder();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /** 跳转进入视频裁剪界面 */
    private void toVideoCrop() {
        com.quze.videorecordlib.media.MediaInfo info = new com.quze.videorecordlib.media.MediaInfo();
        Log.i("xianghaTag", "toVideoCrop::mOutputPath = " + mOutputPath);
        info.filePath = mOutputPath;
        if (latitude != Double.MAX_VALUE && longitude != Double.MAX_VALUE) {
            info.latitude = latitude;
            info.longitude = longitude;
        }
        info.duration = mRecorder.getClipManager().getDuration();
        info.mimeType = "video/mp4";
        AliyunIImport mImport = AliyunImportCreator.getImportInstance(VideoRecorder.this);
        mImport.setVideoParam(mVideoParam);
        mImport.addVideo(info.filePath, info.startTime, info.startTime + info.duration, 0, AliyunDisplayMode.DEFAULT);
        String projectJsonPath = mImport.generateProjectConfigure();
        Log.i("xianghaTag", "projectJsonPath:::" + projectJsonPath);
        Bundle bundle = new Bundle();
        bundle.putSerializable("video_param", mVideoParam);
        bundle.putString("project_json_path", projectJsonPath);
        bundle.putString("videoPath", mOutputPath);
        bundle.putSerializable("videoInfo", info);
        VideoRecorderCommon.instance().handleStartEditActivityCallback(bundle);
    }

    /**
     * 是否在合成中
     *
     * @param state true 在合成中，false未在
     */
    private void isShowEdit(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state) {
                    progressBayLayout.setVisibility(View.VISIBLE);
                } else {
                    progressBayLayout.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                }
            }
        });
    }

    private void toStitch() {
        mRecorder.finishRecording();
        AliyunIClipManager mClipManager = mRecorder.getClipManager();
        mClipManager.deleteAllPart();//删除所有的临时文件
    }

    private Handler handler = new Handler();
    private Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            mRecordPoint.setVisibility(mRecordPoint.isShown() ? View.INVISIBLE : View.VISIBLE);
            handler.postDelayed(switchRunnable, 500);
        }
    };

    /** 开始录制 */
    private void startRecording() {
//        String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.quze.lbsvideo/long/aliyun/" + System.currentTimeMillis() + ".mp4";
        String videoPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + System.currentTimeMillis() + ".mp4";
        mRecorder.setOutputPath(videoPath);
        handleRecordStart();
        mRecorder.setRotation(getPictureRotation());
        isRecordError = false;
        mRecorder.startRecording();
        if (mFlashType == FlashType.ON && mCameraType == CameraType.BACK) {
            mRecorder.setLight(FlashType.TORCH);
        }

    }

    /** 结束录制 */
    private void stopRecording() {
        mRecorder.stopRecording();
        handleRecordStop();
    }

    /**
     * 核对磁盘空间是否充足
     *
     * @return
     */
    private boolean checkIfStartRecording() {
        if (mRecordBtn.isActivated()) {
            return false;
        }
        if (CommonUtil.SDFreeSize() < 100 * 1024 * 1024) {
            Toast.makeText(this, R.string.aliyun_no_free_memory, Toast.LENGTH_SHORT).show();
            return false;
        } else if (CommonUtil.SDFreeSize() < 500 * 1024 * 1024) {
            Toast.makeText(this, R.string.aliyun_will_no_free_memory, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showFilter(String name) {
        if (name == null || name.isEmpty()) {
            name = getString(R.string.aliyun_filter_null);
        }
        mFilterTxt.animate().cancel();
        mFilterTxt.setText(name);
        mFilterTxt.setVisibility(View.VISIBLE);
        mFilterTxt.setAlpha(FADE_IN_START_ALPHA);
        txtFadeIn();
    }

    private void txtFadeIn() {
        mFilterTxt.animate().alpha(1).setDuration(FILTER_ANIMATION_DURATION / 2).setListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        txtFadeOut();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void txtFadeOut() {
        mFilterTxt.animate().alpha(0).setDuration(FILTER_ANIMATION_DURATION / 2).start();
        mFilterTxt.animate().setListener(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mRecordBtn) {
            if (isOpenFailed) {
                Toast.makeText(this, R.string.aliyun_camera_permission_tip, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (mRecordMode == AliyunSnapVideoParam.RECORD_MODE_TOUCH) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isRecording) {
                        if (!checkIfStartRecording()) {
                            return false;
                        }
                        mRecordBtn.setHovered(true);
                        startRecording();
                        isRecording = true;
                    } else {
                        stopRecording();
                        isRecording = false;
                    }
                }
            } else if (mRecordMode == AliyunSnapVideoParam.RECORD_MODE_PRESS) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!checkIfStartRecording()) {
                        return false;
                    }
                    mRecordBtn.setSelected(true);
                    startRecording();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    stopRecording();
                }
            } else if (mRecordMode == AliyunSnapVideoParam.RECORD_MODE_AUTO) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDownTime = System.currentTimeMillis();
                    if (!isRecording) {
                        if (!checkIfStartRecording()) {
                            return false;
                        }
                        mRecordBtn.setPressed(true);
                        startRecording();
                        if (mRecordBtn.isPressed()) {
                            mRecordBtn.setSelected(true);
                            mRecordBtn.setHovered(false);
                        }
                        isRecording = true;
                    } else {
                        stopRecording();
                        isRecording = false;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    long timeOffset = System.currentTimeMillis() - mDownTime;
                    mRecordBtn.setPressed(false);
                    if (timeOffset > 1000) {
                        stopRecording();
                        isRecording = false;
                    } else {
                        if (!isRecordError) {
                            mRecordBtn.setSelected(false);
                            mRecordBtn.setHovered(true);
                        } else {
                            isRecording = false;
                        }
                    }
                }
            }
        } else if (v.equals(mGlSurfaceView)) {
            if (event.getPointerCount() >= 2) {
                scaleGestureDetector.onTouchEvent(event);
            } else if (event.getPointerCount() == 1) {
                gestureDetector.onTouchEvent(event);
            }
        }
        return true;
    }

    /** 开始录制 */
    private void handleRecordStart() {
        mRecordBtn.setActivated(true);
        showGallery(false);
        showDraftLayout(false);
        mCompleteBtn.setVisibility(View.GONE);
        mDeleteBtn.setVisibility(View.GONE);
        mBackBtn.setVisibility(View.GONE);
        mDownload.setVisibility(View.GONE);
        showSwitchCamera(false);
        showSwitchLight(false);
        mCompleteBtn.setEnabled(false);
        mDeleteBtn.setEnabled(false);
        mDeleteBtn.setActivated(false);
        isSelected = false;
        handler.post(switchRunnable);
    }

    /** 停止录制 */
    private void handleRecordStop() {
        mCompleteBtn.setVisibility(View.VISIBLE);
        mDeleteBtn.setVisibility(View.VISIBLE);
        mBackBtn.setVisibility(View.VISIBLE);
        //TODO 无网暂存
        mDownload.setVisibility(View.GONE);
        if (mRecorder.getCameraCount() > 1) {
            showSwitchCamera(true);
            showSwitchLight(mCameraType == CameraType.BACK);
        } else {
            showSwitchLight(true);
        }
        if (mFlashType == FlashType.ON && mCameraType == CameraType.BACK) {
            mRecorder.setLight(FlashType.OFF);
        }
        handler.removeCallbacks(switchRunnable);
        mRecordPoint.setVisibility(View.GONE);
    }

    private void handleUIChanged() {
        mBackBtn.setVisibility(View.VISIBLE);
        if (mRecorder.getClipManager().getPartCount() > 0) {
            mRecordTimeLayout.setVisibility(View.VISIBLE);
            mDeleteBtn.setVisibility(View.VISIBLE);
            mCompleteBtn.setVisibility(View.VISIBLE);
            //TODO 无网暂存
            mDownload.setVisibility(View.GONE);
            showGallery(false);
            showDraftLayout(false);
        } else {
            mRecordTimeLayout.setVisibility(View.GONE);
            mDeleteBtn.setVisibility(View.GONE);
            mCompleteBtn.setVisibility(View.GONE);
            mDownload.setVisibility(View.GONE);
            showGallery(true);
            showDraftLayout(true);
        }
        if (mRecorder.getCameraCount() > 1) {
            showSwitchCamera(true);
            if (mCameraType == CameraType.BACK) {
                mSwitchLightBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mSwitchLightBtn.setVisibility(View.VISIBLE);
        }
        if (mFlashType == FlashType.ON && mCameraType == CameraType.BACK) {
            mRecorder.setLight(FlashType.OFF);
        }
        handler.removeCallbacks(switchRunnable);
        mRecordPoint.setVisibility(View.GONE);
    }

    private void showComplete() {
        mCompleteBtn.setActivated(mClipManager.getDuration() > mClipManager.getMinDuration());
        mCompleteBtn.setEnabled(mClipManager.getDuration() > mClipManager.getMinDuration());
    }


    private void handleRecordCallback(final boolean validClip, final long clipDuration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordBtn.setActivated(false);
                mRecordBtn.setHovered(false);
                mRecordBtn.setSelected(false);
                if (validClip) {
                    mRecordTimelineView.setDuration((int) clipDuration);
                    mRecordTimelineView.clipComplete();
                } else {
                    mRecordTimelineView.setDuration(0);
                }
                mRecordTimeLayout.setVisibility(View.VISIBLE);
                mSwitchLightBtn.setEnabled(true);
                showComplete();
                mDeleteBtn.setEnabled(true);
                isRecording = false;
                handleUIChanged();
            }
        });
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float factorOffset = detector.getScaleFactor() - mLastScaleFactor;
        mScaleFactor += factorOffset;
        mLastScaleFactor = detector.getScaleFactor();
        if (mScaleFactor < 0) {
            mScaleFactor = 0;
        }
        if (mScaleFactor > 1) {
            mScaleFactor = 1;
        }
        mRecorder.setZoom(mScaleFactor);
        return false;

    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mLastScaleFactor = detector.getScaleFactor();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        mRecorder.setFocus(x / mGlSurfaceView.getWidth(), y / mGlSurfaceView.getHeight());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) > 20) {
            return false;
        }
        mExposureCompensationRatio += (distanceY / mGlSurfaceView.getHeight());
        if (mExposureCompensationRatio > 1) {
            mExposureCompensationRatio = 1;
        }
        if (mExposureCompensationRatio < 0) {
            mExposureCompensationRatio = 0;
        }
        mRecorder.setExposureCompensationRatio(mExposureCompensationRatio);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mFilterList == null || mFilterList.length == 0) {
            return true;
        }
        if (mRecordBtn.isActivated()) {
            return true;
        }
        if (velocityX > MAX_SWITCH_VELOCITY) {
            mFilterIndex++;
            if (mFilterIndex >= mFilterList.length) {
                mFilterIndex = 0;
            }
        } else if (velocityX < -MAX_SWITCH_VELOCITY) {
            mFilterIndex--;
            if (mFilterIndex < 0) {
                mFilterIndex = mFilterList.length - 1;
            }
        } else {
            return true;
        }
        EffectFilter effectFilter = new EffectFilter(mFilterList[mFilterIndex]);
        mRecorder.applyFilter(effectFilter);
        showFilter(effectFilter.getName());
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        VideoRecorderCommon.instance().finishActivity(this);
        VideoRecorderCommon.deletePath(mOutputPath);
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     *
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

}

//@line 1476