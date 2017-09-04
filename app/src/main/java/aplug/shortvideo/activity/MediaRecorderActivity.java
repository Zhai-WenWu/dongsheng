package aplug.shortvideo.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ta.utdid2.android.utils.StringUtils;
import com.xiangha.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.video.tools.MediaVideoEditor;
import aplug.recordervideo.cammer.MediaRecorderSystem;
import aplug.shortvideo.ShortVideoInit;
import aplug.shortvideo.media.DeviceUtils;
import aplug.shortvideo.media.MediaRecorderShortVideo;
import aplug.shortvideo.media.MediaRecorderTool;
import xh.basic.internet.img.transformation.RoundTransformation;

/**
 * 录制视频页面
 * 小视频
 */
public class MediaRecorderActivity extends BaseActivity implements View.OnClickListener ,ActivityCompat.OnRequestPermissionsResultCallback{

    public static final int STOP = 1;
    public static final int IMAGE_LOADOVER = 2;
    public static final int TIME_REFRESH= 3;
    public static final int VIDEO_RECORDER=4;

    /** 录制最长时间 */
    public final static int RECORD_TIME_MAX = 8 * 1000;
    /** 录制最小时间 */
    public final static int RECORD_TIME_MIN = 4 * 1000;
    /** 刷新进度条 */
    private static final int HANDLE_INVALIDATE_PROGRESS = 0;
    /** 延迟拍摄停止 */
    private static final int HANDLE_STOP_RECORD = 1;
    /** 对焦 */
    private static final int HANDLE_HIDE_RECORD_FOCUS = 2;

    /** 拍摄按钮 */
    private TextView mRecordController;
    /** 底部条 */
    private RelativeLayout mBottomLayout;
    /** 摄像头数据显示画布 */
    private SurfaceView mSurfaceView;
    /** 录制进度 */
    private ProgressBar mProgressBar;
    /** 对焦图标-带动画效果 */
    private ImageView mFocusImage;
    /** 前后摄像头切换 */
    private ImageView mCameraSwitch;
    /** 闪光灯 */
    private ImageView mRecordLed;
    /** 选择小视频 */
    private ImageView mVideoSelect;

    private TextView timeText;

    /** 对焦动画 */
    private Animation mFocusAnimation;

    /** 需要重新编译（拍摄新的或者回删） */
    private boolean mRebuild;
    /** 是否是点击状态 */
    private volatile boolean mPressedStatus;
    /** 是否已经释放 */
    private volatile boolean mReleased;
    /** 屏幕宽度 */
    private int mWindowWidth;
    /** 对焦图片宽度 */
    private int mFocusWidth;
    /** on */
    private boolean mCreated;

    private MediaRecorderShortVideo mediaRecorderShortVideo;
    private String pathDirs;
    private String videoPath;
    private String audioPath;
    private String key;
    private String imagePath;
    private MediaVideoEditor videoEditor= new MediaVideoEditor();
    public static WeakReference<Activity> rediaRecWeakRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCreated = false;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        loadIntent();
//        initViews();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        initActivity("", 2, 0, 0, R.layout.short_recorder_activity);
        initTitles();
        mCreated = true;
        rediaRecWeakRef = new WeakReference<Activity>(this);
//        ActivityCompat.requestPermissions(MediaRecorderActivity.this,
//                new String[]{"android.permission.RECORD_AUDIO","android.permission.WRITE_EXTERNAL_STORAGE","android.permission.CAMERA"},
//                10000);
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
        initMediaRecorder();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaRecorderShortVideo.releaseMediaRecorder();
        mediaRecorderShortVideo.releaseCamera();
        mReleased = false;
    }

    private void loadIntent() {
        mWindowWidth = ToolsDevice.getWindowPx(this).widthPixels;
        mFocusWidth = Tools.getDimen(this, R.dimen.dp_40);
    }

    private void initViews() {
        mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
        mRecordController = (TextView) findViewById(R.id.record_controller);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mCameraSwitch = (ImageView) findViewById(R.id.record_camera_switcher);
        mRecordLed = (ImageView) findViewById(R.id.record_camera_led);
        mFocusImage = (ImageView) findViewById(R.id.record_focusing);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mVideoSelect = (ImageView) findViewById(R.id.local_video);
        timeText = (TextView) findViewById(R.id.time_text);
        mVideoSelect.setVisibility(View.INVISIBLE);
        setLocalVideoImage();

        mProgressBar.setMax(RECORD_TIME_MAX / 10);

        // ~~~ 绑定事件
        if (DeviceUtils.hasICS())
            mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);

        findViewById(R.id.title_back).setOnClickListener(this);
        mVideoSelect.setOnClickListener(this);
        mRecordController.setOnTouchListener(mOnVideoControllerTouchListener);


        // ~~~ 设置数据

        //是否支持前置摄像头
        if (MediaRecorderTool.isSupportFrontCamera()) {
            mCameraSwitch.setOnClickListener(this);
        } else {
            mCameraSwitch.setVisibility(View.GONE);
        }
        //是否支持闪光灯
        if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
            mRecordLed.setOnClickListener(this);
        } else {
            mRecordLed.setVisibility(View.GONE);
        }

        try {
            mFocusImage.setImageResource(R.drawable.video_focus);
        } catch (OutOfMemoryError e) {
        }

        initSurfaceView();
    }

    private void setLocalVideoImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File cacheFile = new File(ShortVideoInit.path_short);
                File[] files = cacheFile.listFiles();
                if(files == null){
                    Message msg = handler.obtainMessage(IMAGE_LOADOVER,"");
                    handler.sendMessage(msg);
                    return;
                }
                for (File path : files) {
                    if (path.isDirectory()) {
                        File[] dirAboutList = path.listFiles();
                        for (int index = dirAboutList.length - 1 ; index >= 0 ; index --) {
                            File dir = dirAboutList[index];
                            if(dir.listFiles().length != 2){
                                deleteDir(dir);
                                continue;
                            }
                            String image = null,video = null;
                            for (File file : dir.listFiles()) {
                                if (file.getName().endsWith(SelectVideoActivity.SUFFIX_IMAGE)) {
                                    image = file.getAbsolutePath();
                                } else if (file.getName().endsWith(SelectVideoActivity.SUFFIX_VIDEO) || file.getName().endsWith(SelectVideoActivity.SUFFIX_VIDEO_2)) {
                                    video = file.getAbsolutePath();
                                }
                            }
                            if (!TextUtils.isEmpty(image) && !TextUtils.isEmpty(video)
                                    && (image.replace(SelectVideoActivity.SUFFIX_IMAGE, "").equals(video.replace(SelectVideoActivity.SUFFIX_VIDEO, ""))
                                        || image.replace(SelectVideoActivity.SUFFIX_IMAGE, "").equals(video.replace(SelectVideoActivity.SUFFIX_VIDEO_2, "")))) {
                                Message msg = handler.obtainMessage(IMAGE_LOADOVER,image);
                                handler.sendMessage(msg);
                                break;
                            }
                        }
                    }
                }

            }
        }).start();
    }

    private void initTitles() {
        if (Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);

            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }

    /** 初始化画布 */
    private void initSurfaceView() {
        final int w = DeviceUtils.getScreenWidth(this);
        ((RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = w;
        int width = w;
        int height = w * 4 / 3;
        //
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mSurfaceView.setLayoutParams(lp);

        RelativeLayout.LayoutParams bottomLayoutParams = (RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams();
        bottomLayoutParams.setMargins(0, w / 4 * 3, 0, 0);
        mBottomLayout.setLayoutParams(bottomLayoutParams);
    }

    /** 初始化拍摄SDK */
    private void initMediaRecorder() {
        mRecordLed.setSelected(false);
        if(timeText != null){
            timeText.setText("0.0秒");
        }
        final int w = DeviceUtils.getScreenWidth(this);

        mediaRecorderShortVideo = new MediaRecorderShortVideo(this,mSurfaceView.getHolder());
        mediaRecorderShortVideo.setWaithAndHeight(0,0);
        mediaRecorderShortVideo.prepare();
        mRebuild = true;
    }

    /** 手动对焦 */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean checkCameraFocus(MotionEvent event) {
        mFocusImage.setVisibility(View.GONE);
        float x = event.getX();
        float y = event.getY();
        float touchMajor = event.getTouchMajor();
        float touchMinor = event.getTouchMinor();

        Rect touchRect = new Rect((int) (x - touchMajor / 2), (int) (y - touchMinor / 2), (int) (x + touchMajor / 2), (int) (y + touchMinor / 2));
        //The direction is relative to the sensor orientation, that is, what the sensor sees. The direction is not affected by the rotation or mirroring of setDisplayOrientation(int). Coordinates of the rectangle range from -1000 to 1000. (-1000, -1000) is the upper left point. (1000, 1000) is the lower right point. The width and height of focus areas cannot be 0 or negative.
        //No matter what the zoom level is, (-1000,-1000) represents the top of the currently visible camera frame
        if (touchRect.right > 1000)
            touchRect.right = 1000;
        if (touchRect.bottom > 1000)
            touchRect.bottom = 1000;
        if (touchRect.left < 0)
            touchRect.left = 0;
        if (touchRect.right < 0)
            touchRect.right = 0;

        if (touchRect.left >= touchRect.right || touchRect.top >= touchRect.bottom)
            return false;

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(touchRect, 1000));
        if (!mediaRecorderShortVideo.manualFocus(new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                //				if (success) {
                mFocusImage.setVisibility(View.GONE);
                //				}
            }
        }, focusAreas)) {
            mFocusImage.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage.getLayoutParams();
        int left = touchRect.left - (mFocusWidth / 2);//(int) x - (focusingImage.getWidth() / 2);
        int top = touchRect.top - (mFocusWidth / 2);//(int) y - (focusingImage.getHeight() / 2);
        if (left < 0)
            left = 0;
        else if (left + mFocusWidth > mWindowWidth)
            left = mWindowWidth - mFocusWidth;
        if (top + mFocusWidth > mWindowWidth)
            top = mWindowWidth - mFocusWidth;

        lp.leftMargin = left;
        lp.topMargin = top;
        mFocusImage.setLayoutParams(lp);
        mFocusImage.setVisibility(View.VISIBLE);

        if (mFocusAnimation == null)
            mFocusAnimation = AnimationUtils.loadAnimation(this, R.anim.record_focus);

        mFocusImage.startAnimation(mFocusAnimation);

        return true;
    }

    int mTimeCount = 0;
    Timer mTimer = null;

    /** 开始录制 */
    private void startRecord() {
        if (mediaRecorderShortVideo != null) {
            //开始录制：.
            key = String.valueOf(System.currentTimeMillis());
            pathDirs= ShortVideoInit.path_short+"cache/"+key;
            File dir = new File(pathDirs);
            // 如果目录不中存在，创建这个目录
            if (!dir.exists())
                dir.mkdir();
            videoPath= pathDirs+"/"+key+".mp4";
            audioPath=pathDirs+"/"+key+".raw";
            imagePath= pathDirs+"/"+key+".jpg";
            mediaRecorderShortVideo.setAudioPath(audioPath);

            mediaRecorderShortVideo.startRecording(videoPath, new MediaRecorderSystem.OnRecorderCallback() {
                @Override
                public void onStarSucess() {
                    startTimer();
                }

                @Override
                public void onStarFail() {
                    stopViewState();
                    Toast.makeText(MediaRecorderActivity.this, "录制异常", Toast.LENGTH_SHORT).show();
                }
            });

            //如果使用MediaRecorderSystem，不能在中途切换前后摄像头，否则有问题
            if (mediaRecorderShortVideo instanceof MediaRecorderShortVideo) {
                mCameraSwitch.setVisibility(View.GONE);
            }
        }

        mRebuild = true;
        mPressedStatus = true;
        mRecordController.setText("");
        mRecordController.setSelected(mPressedStatus);

        mCameraSwitch.setEnabled(false);
        mRecordLed.setEnabled(false);
    }

    private void startTimer(){
        mTimeCount = 0;// 时间计数器重新赋值
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mTimeCount++;
                Message msg = handler.obtainMessage(TIME_REFRESH,mTimeCount,0);
                handler.sendMessage(msg);
                mProgressBar.setProgress(mTimeCount);// 设置进度条
                if (mTimeCount == RECORD_TIME_MAX / 10) {// 达到指定时间，停止拍摄
                    handler.sendEmptyMessage(STOP);
                }
            }
        }, 0, 10);
    }
    /**
     * 初始化view 显示
     */
    private void stopViewState(){
        mPressedStatus = false;
        mRecordController.setText(getResources().getString(R.string.short_video_recorder));
        mRecordController.setSelected(mPressedStatus);

        if (mediaRecorderShortVideo != null) {
            mediaRecorderShortVideo.stopRecording();
        }

        mCameraSwitch.setEnabled(true);
        mRecordLed.setEnabled(true);

        if (mTimer != null) {
            mTimer.cancel();
//            mTimer.purge();
        }
    }

    /** 停止录制 */
    private void stopRecord() {
        mPressedStatus = false;
        mRecordController.setText(getResources().getString(R.string.short_video_recorder));
        mRecordController.setSelected(mPressedStatus);

        if (mediaRecorderShortVideo != null) {
            mediaRecorderShortVideo.stopRecording();
        }

        mCameraSwitch.setEnabled(true);
        mRecordLed.setEnabled(true);

        if (mTimer != null) {
            mTimer.cancel();
//            mTimer.purge();
        }
        checkStatus();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case STOP:
                    stopRecord();
                    break;
                case IMAGE_LOADOVER:
                    if(mVideoSelect != null && msg.obj != null){
                        String imagePath = msg.obj.toString();
                        if(TextUtils.isEmpty(imagePath)){
                            mVideoSelect.setVisibility(View.INVISIBLE);
                        }else{
                            Glide.with(MediaRecorderActivity.this)
                                    .load(msg.obj.toString())
                                    .transform(new RoundTransformation(MediaRecorderActivity.this,Tools.getDimen(MediaRecorderActivity.this,R.dimen.dp_2)))
                                    .into(mVideoSelect);
                            mVideoSelect.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case TIME_REFRESH:
                    timeText.setText((msg.arg1/10)/10f + "秒");
                    break;
                case VIDEO_RECORDER:
                    onEncodeComplete();
                    break;
            }
        }
    };

    /** 检查录制时间 */
    private int checkStatus() {
        int duration = 0;
        try {

            if (!isFinishing()&&!TextUtils.isEmpty(videoPath)&&new File(videoPath).exists()) {
               if(!new File(audioPath).exists()){
                   Tools.showToast(this, "没有录制声音,请打开权限后重新录制");
                   return duration;
               }
//                MediaInfo mediaInfo= new MediaInfo(videoPath);
//                mediaInfo.prepare();
//                duration= (int) (mediaInfo.vDuration*1000);
//                if (duration < RECORD_TIME_MIN) {
//                    if (duration == 0) {
//                        mCameraSwitch.setVisibility(View.VISIBLE);
//                    } else {
//                        MediaHandleControl.deleteFile(videoPath);
//                        Tools.showToast(this, "录制时间太短");
//
//                    }
//                } else {
//                    Tools.showToast(this, "录制完成");
//                    //转码去
//                    startHandlerVideo();
//
//                }
            }
            return duration;
        }catch (Exception e){
            e.printStackTrace();
        }
        return duration;
    }

    /** 点击屏幕录制 */
    private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mediaRecorderShortVideo == null || !mCreated) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //检测是否手动对焦
                    if (checkCameraFocus(event))
                        return true;
                    break;
            }
            return true;
        }

    };

    /** 点击屏幕录制 */
    private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mediaRecorderShortVideo == null) {
                return false;
            }
            XHClick.mapStat(MediaRecorderActivity.this,"a_record_shortvideo","拍摄按钮","");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecord();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    // 暂停
                    if (mPressedStatus) {
                        stopRecord();
                    }
                    break;
            }
            return true;
        }

    };


    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.title_back:
                XHClick.mapStat(MediaRecorderActivity.this,"a_record_shortvideo","取消","");
                onBackPressed();
                break;
            case R.id.record_camera_switcher:// 前后摄像头切换
                XHClick.mapStat(MediaRecorderActivity.this,"a_record_shortvideo","摄像头切换","");
                if (mRecordLed.isSelected()) {
                    if (mediaRecorderShortVideo != null) {
                        mediaRecorderShortVideo.toggleFlashMode();
                    }
                    mRecordLed.setSelected(false);
                }

                if (mediaRecorderShortVideo != null) {
                    mediaRecorderShortVideo.switchCamera();
                }

                if (mediaRecorderShortVideo.isFrontCamera()) {
                    mRecordLed.setEnabled(false);
                } else {
                    mRecordLed.setEnabled(true);
                }
                break;
            case R.id.record_camera_led://闪光灯
                //开启前置摄像头以后不支持开启闪光灯
                if (mediaRecorderShortVideo != null) {
                    if (mediaRecorderShortVideo.isFrontCamera()) {
                        return;
                    }
                }
                XHClick.mapStat(MediaRecorderActivity.this,"a_record_shortvideo","闪光灯","");
                if (mediaRecorderShortVideo != null) {
                    boolean success = mediaRecorderShortVideo.toggleFlashMode();
                    if(success){
                        mRecordLed.setSelected(!mRecordLed.isSelected());
                    }
                }
                break;
            case R.id.local_video:
                XHClick.mapStat(MediaRecorderActivity.this,"a_record_shortvideo","视频选择按钮","");
                Intent selectVideo = new Intent(this,SelectVideoActivity.class);
                startActivityForResult(selectVideo,100);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100
                && resultCode == RESULT_OK
                && data != null){
            String imagePath = data.getStringExtra(SelectVideoActivity.EXTRAS_IMAGE_PATH);
            String videoPath = data.getStringExtra(SelectVideoActivity.EXTRAS_VIDEO_PATH);
            next(imagePath,videoPath);
        }
    }

    /** 转码完成 */
    public void onEncodeComplete() {
        hideProgress();
        Tools.showToast(this, "转码完成");
        mProgressBar.setProgress(0);

        if(!TextUtils.isEmpty(imagePath)&&!TextUtils.isEmpty(videoPath)
                &&new File(imagePath).exists()&&new File(videoPath).exists()){
            next(imagePath,videoPath);
        }

    }

    public void next(String imagePath,String videoPath){
        if(TextUtils.isEmpty(imagePath) || TextUtils.isEmpty(videoPath)){
            return;
        }
        Log.i("zhangyujian","Intent跳转pulishvideo页面");
        Intent intent = new Intent(this,PulishVideo.class);
        intent.putExtra(SelectVideoActivity.EXTRAS_IMAGE_PATH,imagePath);
        intent.putExtra(SelectVideoActivity.EXTRAS_VIDEO_PATH,videoPath);
        startActivity(intent);
//        finish();
    }

    private static final int rate=2*10000*100;
    /**
     * 开始处理
     */
    private void startHandlerVideo(){
        showProgress("","正在转码...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //转aac音频
                handlerAudio();
                //裁剪视频
                handlerPapersVideo();
                //音频视频合成
                handlerAudioVideo();
                //获取第一帧图片
                handlerParperImage();
                handler.sendEmptyMessage(VIDEO_RECORDER);

            }
        }).start();
    }

    /**
     * 处理音频
     */
    private void handlerAudio(){

//        videoEditor.executePcmEncodeAac(audioPath,44100,2,audioPath.replace("raw","aac"));
//        MediaHandleControl.deleteFile(audioPath);
//        audioPath= audioPath.replace("raw","aac");
    }

    /**
     * 画面裁剪
     */
    private void handlerPapersVideo(){
//        MediaInfo mediaInfo = new MediaInfo(videoPath);
//        mediaInfo.prepare();
//        Log.i("zhangyujian",mediaInfo.vWidth+":::::::"+mediaInfo.vHeight+"::::::"+mediaInfo.vCodecName);
//        Log.i("zhangyujian",mediaInfo.vHeight*3/4+":::::::"+mediaInfo.vHeight);
//        Log.i("zhangyujian",checkVideoSize(mediaInfo.vHeight*3/4)+":::::::"+checkVideoSize(mediaInfo.vHeight));
//        videoEditor.executeVideoFrameCrop(videoPath,checkVideoSize(mediaInfo.vHeight*3/4),checkVideoSize(mediaInfo.vHeight),0,0,pathDirs+"/parper.mp4",mediaInfo.vCodecName,(int) (mediaInfo.vBitRate*1.2f));
//        MediaInfo mediaInfo_parper = new MediaInfo(pathDirs+"/parper.mp4");
//        mediaInfo_parper.prepare();
//        if(Build.MODEL.startsWith("SM"))//锤子手机特殊处理
//            videoEditor.executeVideoRotate90Clockwise(pathDirs+"/parper.mp4","h264", (int) (mediaInfo_parper.vBitRate*1.2f),pathDirs+"/no.mp4");
//        else
//            videoEditor.executeVideoRotate90Clockwise(pathDirs+"/parper.mp4",mediaInfo_parper.vCodecName, (int) (mediaInfo_parper.vBitRate*1.2f),pathDirs+"/no.mp4");
//        MediaHandleControl.deleteFile(videoPath);
//        MediaHandleControl.deleteFile(pathDirs+"/parper.mp4");
////        Log.i("zhangyujian",checkVideoSize(mediaInfo.vHeight*3/4)+":::::::"+checkVideoSize(mediaInfo.vHeight));
    }
    /**哦
     * 给视频加音频
     */
    private void handlerAudioVideo(){
//        videoEditor.executeVideoMergeAudio(pathDirs+"/no.mp4",audioPath,videoPath);
//        MediaHandleControl.deleteFile(audioPath);
//        MediaHandleControl.deleteFile(pathDirs+"/no.mp4");
    }

    /**
     * 截取第一帧图片
     */
    private void handlerParperImage(){
//        MediaInfo mediaInfo = new MediaInfo(videoPath);
//        mediaInfo.prepare();
//        videoEditor.executeGetOneFrame(videoPath,mediaInfo.vCodecName,0.1f,480,360,imagePath);
    }

    protected ProgressDialog mProgressDialog;

    public ProgressDialog showProgress(String title, String message) {
        return showProgress(title, message, -1);
    }

    public ProgressDialog showProgress(String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(this, theme);
            else
                mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        hideProgress();
        mProgressDialog = null;
    }

    public static void deleteDir(File f) {
        if (f != null && f.exists() && f.isDirectory()) {
            for (File file : f.listFiles()) {
                if (file.isDirectory())
                    deleteDir(file);
                file.delete();
            }
            f.delete();
        }
    }

    /**
     * 处理16倍数视频参数
     * @param size
     * @return
     */
    private int checkVideoSize(int size){
        return size;
//        int num=size%16;
//        if(num==0){
//            return size;
//        }else{
//            int mulriple= size/16;
//            int two= mulriple%2;
//            if(two==0)
//            return mulriple*16;
//            else return (mulriple-1)*16;
//        }
    }

    public static boolean checkPermission(Context context,String permission){
        PackageManager pm = context.getPackageManager();
        boolean permission_state = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permission, "com.xiangha"));
        return permission_state;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10000:

                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //已获取权限
                }else{
                    //权限被拒绝
                    Tools.showToast(MediaRecorderActivity.this,"没有录音功能,请到设置-权限管理中开启11");
                }

                break;
        }
    }
}
