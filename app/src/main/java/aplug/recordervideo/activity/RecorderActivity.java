package aplug.recordervideo.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import amodule.dish.tools.DeviceUtilDialog;
import aplug.recordervideo.cammer.MediaRecorderBase;
import aplug.recordervideo.cammer.MediaRecorderSystem;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.db.RecorderVideoSqlite;
import aplug.recordervideo.tools.AudioTools;
import aplug.recordervideo.tools.FileToolsCammer;
import aplug.recordervideo.tools.ToolsCammer;
import aplug.shortvideo.media.DeviceUtils;

public class RecorderActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 摄像头数据显示画布
     */
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorderSystem mediaRecorderSystem;
    private MediaRecorderBase.OnErrorListener onErrorListener;

    private ImageView mXiangceIv, mRecordSwitchIv;
    private CheckBox mActionChb, mRecordLed,mPhotoChb;
    private TextView mTimeTv, mCountdownTv;
    private View mReadPoint;

    private boolean isActionRecorder = false;
    private Handler handler;
    private int maxTimeS = 30;

    private RecorderVideoData uploadDishData;
    private Bitmap bitmap = null;
    public static final String parentPath = FileManager.getSDDir() + "/XiangHa/";
    private int currentVolume;

    private final static String tongjiId = "a_record_dishvideo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        setContentView(R.layout.a_recorder_video);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置相册图标
        new Thread(new Runnable() {
            @Override
            public void run() {
                RecorderVideoData recorderVideoData = RecorderVideoSqlite.getInstans().selectLastTimeData();
                if(TextUtils.isEmpty(recorderVideoData.getVideoPath())){
                    ChooseVideoActivity.isRefrushData = true;
                    bitmap = null;
                }else {
                    bitmap = FileToolsCammer.getBitmapByImgPath(recorderVideoData.getVideoPath());
                }
                handler.sendEmptyMessage(4);
            }
        }).start();
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
    }


    private void init() {
        handler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        int ss = msg.arg1;
                        if (uploadDishData != null) uploadDishData.setVideoLongTime(ss);
                        int ff = 0;
                        if (ss > 59) {
                            ff = ss / 60;
                            ss = ss % 60;
                        }
                        String strS = "" + ss, strF = "" + ff;
                        if (ss < 10) {
                            strS = "0" + ss;
                        }
                        if (ff < 10) {
                            strF = "0" + ff;
                        }
                        if (isStar) mTimeTv.setText(strF + ":" + strS);
                        break;
                    case 3:
                        stopCamera();
                        break;
                    case 4:
                        if (bitmap == null) {
                            mXiangceIv.setVisibility(View.GONE);
                        } else {
                            mXiangceIv.setImageBitmap(bitmap);
                            if(!isActionRecorder)
                                mXiangceIv.setVisibility(View.VISIBLE);
                        }
                        break;
                }

            }
        };
        DeviceUtilDialog deviceUtilDialog = new DeviceUtilDialog(this);
        deviceUtilDialog.deviceShootState(true, new DeviceUtilDialog.DeviceCallBack() {
            @Override
            public void backResultState(Boolean state) {
                if (state) {
                    RecorderActivity.this.finish();
                }
            }
        });

        findViewById(R.id.a_video_recorder_back).setOnClickListener(this);

        mRecordLed = (CheckBox) findViewById(R.id.a_video_recorder_led);
        mSurfaceView = (SurfaceView) findViewById(R.id.a_video_recorder_surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mActionChb = (CheckBox) findViewById(R.id.a_video_recorder_action);
        mPhotoChb = (CheckBox) findViewById(R.id.a_video_recorder_photo);
        mXiangceIv = (ImageView) findViewById(R.id.a_video_recorder_play_ce);
//        mRecordSwitchIv = (ImageView) findViewById(R.id.a_video_recorder_switcher);
        mTimeTv = (TextView) findViewById(R.id.a_video_recorder_time);
        mCountdownTv = (TextView) findViewById(R.id.a_video_recorder_number);
        mReadPoint = findViewById(R.id.a_video_recorder_action_point);

        //是否支持前置摄像头
//        if (com.yixia.camera.MediaRecorderBase.isSupportFrontCamera() && ToolsCammer.checkSuporRecorder(false)) {
//            mRecordSwitchIv.setOnClickListener(this);
//        } else {
//            mRecordSwitchIv.setVisibility(View.GONE);
//        }
        //是否支持闪光灯
        if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
            mRecordLed.setOnClickListener(this);
        } else {
            mRecordLed.setVisibility(View.GONE);
        }
        mActionChb.setOnClickListener(this);
        mPhotoChb.setOnClickListener(this);
        mXiangceIv.setOnClickListener(this);
        mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);

        mediaRecorderSystem = new MediaRecorderSystem(this, mSurfaceHolder);
        onErrorListener = new MediaRecorderBase.OnErrorListener() {
            @Override
            public void onVideoError(int what, int extra) {
                switch (what) {
                    /** 预览画布设置错误 */
                    case MediaRecorderSystem.MEDIA_ERROR_CAMERA_SET_PREVIEW_DISPLAY:
                        /** 预览错误 */
                    case MediaRecorderSystem.MEDIA_ERROR_CAMERA_PREVIEW:
                        /** 自动对焦错误 */
                    case MediaRecorderSystem.MEDIA_ERROR_CAMERA_AUTO_FOCUS:
                        resetAll();
                        break;

                }
            }
        };

        initSurfaceView();
    }

    /**
     * 初始化画布
     */
    private void initSurfaceView() {
        final int h = ToolsCammer.getScreenHeight(this);
        int height = h;
        int width = h / 9 * 16;
        //
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.width = width;
        lp.height = height;

        mSurfaceView.setLayoutParams(lp);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.a_video_recorder_led://闪光灯
                XHClick.mapStat(this, tongjiId,"打开闪光灯","");
                mediaRecorderSystem.toggleFlashMode();
                break;
            case R.id.a_video_recorder_switcher://前后摄像头切换
                XHClick.mapStat(this, tongjiId,"切换摄像头","");
                //如果开着闪关灯，则会关闭，此时需要改变闪关灯按钮状态
                if (mediaRecorderSystem.toggleIsOpen()) {
                    mRecordLed.setChecked(false);
                }
                mediaRecorderSystem.switchCamera();
                if (mediaRecorderSystem.isBackCamera()) {
                    mRecordLed.setVisibility(View.VISIBLE);
                } else {
                    mRecordLed.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.a_video_recorder_action:
                if (!isActionRecorder) {
                    DeviceUtilDialog deviceUtilDialog = new DeviceUtilDialog(this);
                    deviceUtilDialog.deviceStorageSpaceState(0, 300, new DeviceUtilDialog.DeviceCallBack() {
                        @Override
                        public void backResultState(Boolean state) {
                            if (state) {
                                RecorderActivity.this.finish();
                            } else {
                                starCamera();
                            }
                        }
                    });
                } else {
                    stopCamera();
                }
                break;
            case R.id.a_video_recorder_photo:
                mediaRecorderSystem.takePicture();
                break;
            case R.id.a_video_recorder_play_ce:
                XHClick.mapStat(this, tongjiId,"查看相册","");
                Intent intent = new Intent(RecorderActivity.this, ChooseVideoActivity.class);
                intent.putExtra("isCanEdit", true);
                startActivity(intent);
                break;
            case R.id.a_video_recorder_back:
                XHClick.mapStat(this, tongjiId,"退出按钮","");
                onBackPressed();
                break;
        }
    }

    private boolean isStar = false;
    private void starCamera() {
        findViewById(R.id.a_video_recorder_back).setVisibility(View.GONE);
        findViewById(R.id.a_video_recorder_play_ce).setVisibility(View.GONE);
//        findViewById(R.id.a_video_recorder_switcher).setVisibility(View.GONE);
        findViewById(R.id.a_video_recorder_right_layout_back).setVisibility(View.GONE);
        isActionRecorder = true;
        isStar = true;
        final long currentTime = System.currentTimeMillis();
        final String path = parentPath + currentTime + ".mp4";
        mediaRecorderSystem.startRecording(path, new MediaRecorderSystem.OnRecorderCallback() {
            @Override
            public void onStarSucess() {
                uploadDishData = new RecorderVideoData();
                uploadDishData.setVideoAddTime(currentTime);
                uploadDishData.setVideoPath(path);
                mRecordLed.setVisibility(View.GONE);
                mPhotoChb.setVisibility(View.VISIBLE);
                findViewById(R.id.a_video_recorder_time_margin_view).setVisibility(View.GONE);
                startFlick(mReadPoint);

                AudioTools.play(RecorderActivity.this, new AudioTools.OnPlayAudioListener() {
                    @Override
                    public void playOver() {
//                        Toast.makeText(RecorderActivity.this,"播放完毕",Toast.LENGTH_SHORT).show();
                    }
                }, R.raw.recorver_star);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int time = 0;
                        while (isStar) {
                            if (maxTimeS - time < 0) {
                                handler.sendEmptyMessage(3);
                                break;
                            }
                            Message message = new Message();
                            message.arg1 = time;
                            message.what = 1;
                            handler.sendMessage(message);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            time++;
                        }
                    }
                }).start();
            }

            @Override
            public void onStarFail() {
                resetAll();
                Toast.makeText(RecorderActivity.this, "录制异常", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    private void stopCamera() {
        isStar = false;
        findViewById(R.id.a_video_recorder_back).setVisibility(View.VISIBLE);
        findViewById(R.id.a_video_recorder_play_ce).setVisibility(View.VISIBLE);
//        findViewById(R.id.a_video_recorder_switcher).setVisibility(View.VISIBLE);
        findViewById(R.id.a_video_recorder_right_layout_back).setVisibility(View.VISIBLE);
        stopFlick(mReadPoint);
        mediaRecorderSystem.stopRecording();
        AudioTools.play(RecorderActivity.this, new AudioTools.OnPlayAudioListener() {
            @Override
            public void playOver() {
//                Toast.makeText(RecorderActivity.this,"播放完毕",Toast.LENGTH_SHORT).show();
            }
        }, R.raw.recorver_end);
        Toast.makeText(RecorderActivity.this, "已保存", Toast.LENGTH_SHORT).show();
        if (uploadDishData != null) {
            final String vidiePath = uploadDishData.getVideoPath();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    bitmap = ToolsCammer.getFrameAtTime(vidiePath);
                    Map<String, String> map = FileToolsCammer.data(vidiePath);
                    RecorderVideoSqlite.getInstans().insert(map);
                    handler.sendEmptyMessage(4);
                }
            }).start();
        }
        resetAll();
    }

    private void resetAll() {
        isStar = false;
        isActionRecorder = false;
        uploadDishData = null;
        mActionChb.setChecked(false);
        mPhotoChb.setChecked(false);
        mPhotoChb.setVisibility(View.GONE);
        if (mediaRecorderSystem.isBackCamera())mRecordLed.setVisibility(View.VISIBLE);
        else mRecordLed.setVisibility(View.INVISIBLE);
        findViewById(R.id.a_video_recorder_time_margin_view).setVisibility(View.VISIBLE);
        findViewById(R.id.a_video_recorder_action_point).setVisibility(View.INVISIBLE);
        mCountdownTv.setText("");
        mTimeTv.setText("00:00");
    }

    /**
     * 点击屏幕录制
     */
    private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean checkCameraFocus(MotionEvent event) {
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
        if (!mediaRecorderSystem.manualFocus(new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {

            }
        }, focusAreas)) {
//            mFocusImage.setVisibility(View.GONE);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isActionRecorder)
            stopCamera();
        mediaRecorderSystem.releaseMediaRecorder();
        mediaRecorderSystem.releaseCamera();
    }

    /**
     * 开启View闪烁效果
     */
    private void startFlick(View view) {
        if (null == view) {
            return;
        }
        view.setVisibility(View.VISIBLE);
        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(alphaAnimation);
    }

    /**
     * 取消View闪烁效果
     */
    private void stopFlick(View view) {
        if (null == view) {
            return;
        }
        view.setVisibility(View.GONE);
        view.clearAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, currentVolume, 0);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN || keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            if (!isActionRecorder) {
                DeviceUtilDialog deviceUtilDialog = new DeviceUtilDialog(RecorderActivity.this);
                deviceUtilDialog.deviceStorageSpaceState(0, 100, new DeviceUtilDialog.DeviceCallBack() {
                    @Override
                    public void backResultState(Boolean state) {
                        if (state) {
                            RecorderActivity.this.finish();
                        } else {
                            starCamera();
                        }
                    }
                });
            } else {
                stopCamera();
            }
            return true;
        }
        else return super.onKeyUp(keyCode, event);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN || keyCode==KeyEvent.KEYCODE_VOLUME_UP) return true;
        else return super.onKeyDown(keyCode, event);
    }
}
