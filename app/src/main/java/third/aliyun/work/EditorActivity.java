
package third.aliyun.work;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.common.media.ShareableBitmap;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupai.editor.AliyunICanvasController;
import com.aliyun.qupai.editor.AliyunICompose;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.qupai.editor.AliyunIPlayer;
import com.aliyun.qupai.editor.AliyunIThumbnailFetcher;
import com.aliyun.qupai.editor.AliyunPasterController;
import com.aliyun.qupai.editor.AliyunPasterManager;
import com.aliyun.qupai.editor.AliyunThumbnailFetcherFactory;
import com.aliyun.qupai.editor.OnAnimationFilterRestored;
import com.aliyun.qupai.editor.OnPlayCallback;
import com.aliyun.qupai.editor.OnPreparedListener;
import com.aliyun.qupai.editor.impl.AliyunEditorFactory;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoDisplayMode;
import com.aliyun.struct.effect.EffectBean;
import com.aliyun.struct.effect.EffectFilter;
import com.aliyun.struct.effect.EffectPicture;
import com.xiangha.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import acore.tools.FileManager;
import third.aliyun.edit.effects.control.BottomAnimation;
import third.aliyun.edit.effects.control.EditorService;
import third.aliyun.edit.effects.control.EffectInfo;
import third.aliyun.edit.effects.control.OnEffectChangeListener;
import third.aliyun.edit.effects.control.OnTabChangeListener;
import third.aliyun.edit.effects.control.TabGroup;
import third.aliyun.edit.effects.control.TabViewStackBinding;
import third.aliyun.edit.effects.control.UIEditorPage;
import third.aliyun.edit.effects.control.VideoCallBack;
import third.aliyun.edit.effects.control.ViewStack;
import third.aliyun.edit.msg.Dispatcher;
import third.aliyun.edit.msg.body.FilterTabClick;
import third.aliyun.edit.msg.body.LongClickAnimationFilter;
import third.aliyun.edit.msg.body.LongClickUpAnimationFilter;
import third.aliyun.edit.msg.body.SelectColorFilter;
import third.aliyun.edit.util.Common;
import third.aliyun.edit.util.ComposeFactory;


public class EditorActivity extends AppCompatActivity implements
        OnTabChangeListener, OnEffectChangeListener, BottomAnimation, View.OnClickListener, OnAnimationFilterRestored,VideoCallBack {
    private static final String TAG = "EditorActivity";
    public static final String KEY_VIDEO_PARAM = "video_param";
    public static final String KEY_PROJECT_JSON_PATH = "project_json_path";
    public static final String KEY_TEMP_FILE_LIST = "temp_file_list";

    private LinearLayout mBottomLinear;
    private SurfaceView mSurfaceView;
    private TabGroup mTabGroup;
    private ViewStack mViewStack;
    private EditorService mEditorService;

    private AliyunIEditor mAliyunIEditor;
    private AliyunIPlayer mAliyunIPlayer;
    private AliyunPasterManager mPasterManager;
    private FrameLayout resCopy;

    private FrameLayout mPasterContainer;
    private FrameLayout mGlSurfaceContainer;
    private Uri mUri;
    private EffectPicture mPicture;
    private int mScreenWidth;
    private TextView aliyun_next;
    private LinearLayout mBarLinear;
    private ImageView mPlayImage;
    private AliyunVideoParam mVideoParam;
    private boolean mIsComposing = false; //当前是否正在合成视频
    private boolean isFullScreen = false; //导入视频是否全屏显示
    private ProgressDialog dialog;
    private MediaScannerConnection mMediaScanner;
    private RelativeLayout mEditor;
    private AliyunICanvasController mCanvasController;
    private ArrayList<String> mTempFilePaths = null;
    private AliyunIThumbnailFetcher mThumbnailFetcher;
    private String videoPath;
    private boolean isCoverFullSreen= true;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dispatcher.getInstance().register(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        setContentView(R.layout.work_aliyun_svideo_activity_editor);
        Intent intent = getIntent();
        if (intent.getStringExtra(KEY_PROJECT_JSON_PATH) != null) {
            mUri = Uri.fromFile(new File(intent.getStringExtra(KEY_PROJECT_JSON_PATH)));
        }
        if (intent.getSerializableExtra(KEY_VIDEO_PARAM) != null) {
            mVideoParam = (AliyunVideoParam) intent.getSerializableExtra(KEY_VIDEO_PARAM);
        }
        if(intent.getStringExtra("videoPath")!=null){
            videoPath = intent.getStringExtra("videoPath");
        }
        mTempFilePaths = intent.getStringArrayListExtra(KEY_TEMP_FILE_LIST);
        initView();
        initListView();
        add2Control();
        initEditor();
        mMediaScanner = new MediaScannerConnection(this, null);
        mMediaScanner.connect();
        copyAssets();
        mCompose = ComposeFactory.INSTANCE.getInstance();
        mCompose.init(EditorActivity.this);
        AliyunCommon.getInstance().addActivity(this);
    }

    private void initView() {
        mEditor = (RelativeLayout) findViewById(R.id.activity_editor);
        resCopy = (FrameLayout) findViewById(R.id.copy_res_tip);
        mBarLinear = (LinearLayout) findViewById(R.id.bar_linear);
        mBarLinear.bringToFront();
        aliyun_next = (TextView) findViewById(R.id.aliyun_next);
        mGlSurfaceContainer = (FrameLayout) findViewById(R.id.glsurface_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.play_view);
        mBottomLinear = (LinearLayout) findViewById(R.id.edit_bottom_tab);

        mPasterContainer = (FrameLayout) findViewById(R.id.pasterView);
        progressBar= (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        mPlayImage = (ImageView) findViewById(R.id.play_button);
        mPlayImage.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorActivity.this.finish();
            }
        });
        findViewById(R.id.progressBar_layout).setOnClickListener(v->{});
        ((ImageView)findViewById(R.id.leftImgBtn)).setImageResource(R.drawable.z_z_topbar_ico_back_white);

    }

    private void initGlSurfaceView() {
        if (mAliyunIPlayer != null) {
            if (mVideoParam == null) {
                return;
            }
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mGlSurfaceContainer.getLayoutParams();
            int rotation = mAliyunIPlayer.getRotation();
            int outputWidth = mVideoParam.getOutputWidth();
            int outputHeight = mVideoParam.getOutputHeight();
            Log.i("xianghaTag","rotation::"+rotation+"::outputWidth:"+outputWidth+"::outputHeight:"+outputHeight+":::::"+mScreenWidth);

            if ((rotation == 90 || rotation == 270)) {
                int temp = outputWidth;
                outputWidth = outputHeight;
                outputHeight = temp;
            }

//            if (outputWidth >= outputHeight) {
//                percent = (float) outputWidth / outputHeight;
            double percent= Float.valueOf(mScreenWidth)/ Float.valueOf(outputWidth);
            double nowHeight= percent* Float.valueOf(outputHeight);
                layoutParams.height = (int) Math.round(nowHeight);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//            } else {
//                percent = (float) outputHeight / outputWidth;
//            }
//            if (percent < 1.5 || (rotation == 90 || rotation == 270)) {
//                layoutParams.height = Math.round((float) outputHeight * mScreenWidth / outputWidth);
//                layoutParams.addRule(RelativeLayout.BELOW, R.id.bar_linear);
//            } else {
//                layoutParams.height = Math.round((float) outputHeight * mScreenWidth / outputWidth);
//                isFullScreen = true;
//            }
            isCoverFullSreen=true;
            mGlSurfaceContainer.setLayoutParams(layoutParams);
        }
    }

    private void initListView() {
        mEditorService = new EditorService();
        mTabGroup = new TabGroup();
        mViewStack = new ViewStack(this);
        mViewStack.setEditorService(mEditorService);
        mViewStack.setEffectChange(this);
        mViewStack.setBottomAnimation(this);
        mViewStack.setVideopath(videoPath);
        mViewStack.setVideoCallBack(this);

        mTabGroup.addView(findViewById(R.id.tab_effect_filter));
        mTabGroup.addView(findViewById(R.id.tab_effect_audio_mix));
        mTabGroup.addView(findViewById(R.id.tab_cover));
    }

    private void add2Control() {
        TabViewStackBinding tabViewStackBinding = new TabViewStackBinding();
        tabViewStackBinding.setViewStack(mViewStack);
        mTabGroup.setOnCheckedChangeListener(tabViewStackBinding);
        mTabGroup.setOnTabChangeListener(this);
    }

    private void initEditor() {
        mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(mUri);
        mAliyunIEditor.init(mSurfaceView);
        mAliyunIPlayer = mAliyunIEditor.createAliyunPlayer();
        if (mAliyunIPlayer == null) {
            ToastUtil.showToast(this, "Create AliyunPlayer failed");
            finish();
            return;
        }
        if(isCoverFullSreen) {
            initGlSurfaceView();
        }
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.fromConfigJson(mUri.getPath());
        mEditorService.setFullScreen(true);
        mEditorService.addTabEffect(UIEditorPage.FILTER_EFFECT, mAliyunIEditor.getFilterLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, mAliyunIEditor.getMusicLastApplyId());
        mEditorService.setPaint(mAliyunIEditor.getPaintLastApply());
        mPasterManager = mAliyunIEditor.createPasterManager();
        mAliyunIPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared() {
                mAliyunIPlayer.start();
                mViewStack.setVideoDurtion(mAliyunIPlayer.getDuration());
                mAliyunIEditor.setAnimationRestoredListener(EditorActivity.this);
                ScaleMode mode = mVideoParam.getScaleMode();
                Log.i("xianghaTag","mode:::"+mode);
                mAliyunIPlayer.setDisplayMode(VideoDisplayMode.SCALE);
                mAliyunIPlayer.setFillBackgroundColor(Color.BLACK);

//                mAudioTimePicker = new AudioTimePicker(getApplicationContext(),
//                        mPicker, mTimelineBar, mAliyunIPlayer.getDuration());
                mPasterManager.setDisplaySize(mPasterContainer.getWidth(),
                        mPasterContainer.getHeight());
//                mPasterManager.setOnPasterRestoreListener(mOnPasterRestoreListener);
//                mAnimationFilterController = new AnimationFilterController(getApplicationContext(), mTimelineBar,
//                        mAliyunIEditor, mAliyunIPlayer);
            }
        });
        mAliyunIPlayer.setOnPlayCallbackListener(new OnPlayCallback() {

            @Override
            public void onPlayStarted() {
                Log.d("xxx", "AliyunIPlayer onPlayStarted");
//                if (mWatermarkFile.exists()) {
//                    if (mWatermarkBitmap == null) {
//                        mWatermarkBitmap = BitmapFactory.decodeFile(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png");
//                    }
//                    /**
//                     * 水印例子 水印的大小为 ：水印图片的宽高和显示区域的宽高比，注意保持图片的比例，不然显示不完全  水印的位置为 ：以水印图片中心点为基准，显示区域宽高的比例为偏移量，0,0为左上角，1,1为右下角
//                     */
//                    mAliyunIEditor.applyWaterMark(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png",
//                            (float) mWatermarkBitmap.getWidth() * 0.5f * 0.8f / mSurfaceView.getWidth(),
//                            (float) mWatermarkBitmap.getHeight() * 0.5f * 0.8f / mSurfaceView.getHeight(),
//                            1f - (float) mWatermarkBitmap.getWidth() / 1.5f / mSurfaceView.getWidth() / 2,
//                            0f + (float) mWatermarkBitmap.getHeight() / 1.5f / mSurfaceView.getHeight() / 2);
//                }
            }

            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                        ToastUtil.showToast(EditorActivity.this, R.string.not_supported_audio);
                        finish();
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                        ToastUtil.showToast(EditorActivity.this, R.string.not_supported_video);
                        finish();
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_PIXEL_FORMAT:
                        ToastUtil.showToast(EditorActivity.this, R.string.not_supported_pixel_format);
                        finish();
                        break;
                    default:
                        ToastUtil.showToast(EditorActivity.this, R.string.play_video_error);
                        break;
                }
//                mPlayImage.setEnabled(true);
                mAliyunIPlayer.stop();
//                finish();
                mPlayImage.setEnabled(true);
            }

            @Override
            public void onSeekDone() {
                mPlayImage.setEnabled(true);
            }

            @Override
            public void onPlayCompleted() {
                //重播时必须先掉stop，再调用start
//                mAliyunIPlayer.stop();
                mAliyunIPlayer.start();
//                Log.d(TimelineBar.TAG, "TailView aliyun_svideo_play restart");
            }

            @Override
            public int onTextureIDCallback(int txtID, int txtWidth, int txtHeight) {
//                Log.d(TAG, "onTextureIDCallback: txtID "+txtID+", txtWidth "+txtWidth+", txtHeight "+txtHeight);
                return 0;
            }
        });

        aliyun_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.i("xianghaTag","mUri.getPath():::"+mUri.getPath());
                isShowEdit(true);
                onPause();
                composeVideo(mUri.getPath());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAliyunIPlayer.resume();
        mPlayImage.setSelected(false);
        mAliyunIEditor.onResume();
//        checkAndRemovePaster();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAliyunIEditor.onPause();
        mAliyunIPlayer.pause();

        mPlayImage.setSelected(true);
        if (dialog != null && dialog.isShowing()) {
            mIsComposing = false;
            dialog.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAliyunIEditor != null) {
            mAliyunIEditor.onDestroy();
        }
        if (mMediaScanner != null) {
            mMediaScanner.disconnect();
        }

        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }

        if (mCanvasController != null) {
            mCanvasController.release();
        }

        //退出编辑界面，将编辑生成的文件（编辑添加的文字图片会保存为文件存在project相应目录）及project config配置删除，如果后续还有合成该视频的需求则不应该删除
//        String path = mUri.getPath();
//        File f = new File(path);
//        if(!f.exists()){
//            return ;
//        }
//        FileUtils.deleteDirectory(f.getParentFile());
        //删除录制生成的临时文件
//        deleteTempFiles();由于返回依然可以接着录，因此现在不能删除
    }

    @Override
    public void onTabChange() {
        //tab切换时通知
        hideBottomView();
        UIEditorPage index = UIEditorPage.get(mTabGroup.getCheckedIndex());
        int ix = mEditorService.getEffectIndex(index);
        switch (index) {
            case FILTER_EFFECT:
                break;
//            case OVERLAY:
//                break;
            default:
                break;
        }
        Log.e("editor", "====== onTabChange " + ix + " " + index);
    }

    @Override
    public void onEffectChange(EffectInfo effectInfo) {
        Log.e("editor", "====== onEffectChange ");
        //返回素材属性

        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        effect.setPath(effectInfo.getPath());
        UIEditorPage type = effectInfo.type;
        AliyunPasterController c;
        Log.d(TAG, "effect path " + effectInfo.getPath());
        switch (type) {
            case AUDIO_MIX:
                mAliyunIEditor.applyMusicMixWeight(effectInfo.musicWeight);
                if (!effectInfo.isAudioMixBar) {
                    mAliyunIEditor.applyMusic(effect);
                    mPlayImage.setSelected(false);
                }
                break;
            case FILTER_EFFECT:
                if (effect.getPath().contains("Vertigo")) {
                    EffectFilter filter = new EffectFilter(effect.getPath());
                    filter.setStartTime(0);
                    filter.setDuration(5000);
                    mAliyunIEditor.addAnimationFilter(filter);
                } else {
                    mAliyunIEditor.applyFilter(effect);
                }
                break;
            default:
                break;
        }
    }

    protected void playingPause() {
        if (mAliyunIPlayer.isPlaying()) {
            mAliyunIPlayer.pause();
            mPlayImage.setSelected(true);
        }
    }

    protected void playingResume() {
        if (!mAliyunIPlayer.isPlaying()) {
            mAliyunIPlayer.resume();
            mPlayImage.setSelected(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewStack.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        aliyun_next.setEnabled(true);
    }

    @Override
    public void showBottomView() {
        mBottomLinear.setVisibility(View.VISIBLE);
        mPlayImage.setVisibility(View.GONE);
        aliyun_next.setVisibility(View.VISIBLE);
        findViewById(R.id.back).setVisibility(View.VISIBLE);
        mAliyunIPlayer.resume();
    }

    @Override
    public void hideBottomView() {
        mBottomLinear.setVisibility(View.GONE);
        mPlayImage.setVisibility(View.GONE);
        aliyun_next.setVisibility(View.GONE);
        findViewById(R.id.back).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayImage && mAliyunIPlayer != null) {
            if (mAliyunIPlayer.isPlaying()) {
                playingPause();
            } else {
                playingResume();
            }
        }
    }
    @Override
    public void animationFilterRestored(List<EffectFilter> animationFilters) {
//        mAnimationFilterController.restoreAnimationFilters(animationFilters);
    }

    StringBuilder mDurationText = new StringBuilder(5);

    private String convertDuration2Text(long duration) {
        mDurationText.delete(0, mDurationText.length());
        int sec = Math.round(((float) duration) / (1000 * 1000));// us -> s
        int min = (sec % 3600) / 60;
        sec = (sec % 60);
        //TODO:优化内存,不使用String.format
        if (min >= 10) {
            mDurationText.append(min);
        } else {
            mDurationText.append("0").append(min);
        }
        mDurationText.append(":");
        if (sec >= 10) {
            mDurationText.append(sec);
        } else {
            mDurationText.append("0").append(sec);
        }
        return mDurationText.toString();
    }

    private void copyAssets() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Common.copyAll(EditorActivity.this, resCopy);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
//                resCopy.setVisibility(View.GONE);
            }
        }.execute();
    }

    public AliyunIPlayer getPlayer() {
        return this.mAliyunIPlayer;
    }

    public void showMessage(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(id);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }



    private void deleteTempFiles() {
        if (mTempFilePaths != null) {
            for (String path : mTempFilePaths) {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 背景颜色选择。
     * @param selectColorFilter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventColorFilterSelected(SelectColorFilter selectColorFilter) {
        EffectInfo effectInfo = selectColorFilter.getEffectInfo();
        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        effect.setPath(effectInfo.getPath());
        Log.i("xianghaTag","effectInfo::::"+effectInfo.getPath());
        mAliyunIEditor.applyFilter(effect);
    }

    /**
     * 长按时需要恢复播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterLongClick(LongClickAnimationFilter filter) {
        if (!mAliyunIPlayer.isPlaying()) {
            playingResume();
        }
    }

    /**
     * 长按抬起手指需要暂停播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterClickUp(LongClickUpAnimationFilter filter) {
        if (mAliyunIPlayer.isPlaying()) {
            playingPause();
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventFilterTabClick(FilterTabClick ft) {
        //切换到特效的tab需要暂停播放，切换到滤镜的tab需要恢复播放
        if (mAliyunIPlayer != null) {
            switch (ft.getPosition()) {
                case FilterTabClick.POSITION_ANIMATION_FILTER:
                    if (mAliyunIPlayer.isPlaying()) {
                        playingPause();
                    }
                    break;
                case FilterTabClick.POSITION_COLOR_FILTER:
                    if (!mAliyunIPlayer.isPlaying()) {
                        playingResume();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private AliyunICompose mCompose;
    private String mOutputPath = FileManager.getSDLongDir() +Common.QU_NAME+ File.separator + "v_"+ System.currentTimeMillis()+".mp4";
    private void  composeVideo(String mConfig){
        Log.i("xianghaTag","mOutputPath::"+mOutputPath);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCompose.compose(mUri.getPath(), mOutputPath, mCallback);
            }
        });
    }
    private final AliyunICompose.AliyunIComposeCallBack mCallback = new AliyunICompose.AliyunIComposeCallBack() {
        @Override

        public void onComposeError(int errorCode) {
            Log.i("xianghaTag","合成错误：："+errorCode);
            isShowEdit(false);
        }

        @Override
        public void onComposeProgress(final int progress) {
            Log.i("xianghaTag","合成进度：："+progress);
            progressBar.setProgress(progress);
        }

        @Override
        public void onComposeCompleted() {
            Log.i("xianghaTag","合成完成：：");
            AliyunCommon.videoPath=mOutputPath;
            if(isFinishing())return;
            //进行视频图片截取。
            coverImg();

        }
    };

    private void coverImg(){
        Log.i("xianghaTag","裁剪图片：："+mOutputPath);
        AliyunIThumbnailFetcher mCoverFetcher =  AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mCoverFetcher.addVideoSource(mOutputPath, 0, Integer.MAX_VALUE);
        if(mAliyunIPlayer==null){
            return;
        }
        Log.i("xianghaTag","开始裁剪：："+mAliyunIPlayer.getVideoWidth()+"：：：："+mAliyunIPlayer.getVideoHeight());
        mCoverFetcher.setParameters(mAliyunIPlayer.getVideoWidth(), mAliyunIPlayer.getVideoHeight(), AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 2);
        mCoverFetcher.requestThumbnailImage(new long[]{coverImgPosition/1000}, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
            @Override
            public void onThumbnailReady(ShareableBitmap shareableBitmap, long l) {
                String path = FileManager.getSDLongDir() +Common.QU_NAME+ File.separator  + "img_"+ System.currentTimeMillis()+".jpeg";
                try {
                    shareableBitmap.getData().compress(Bitmap.CompressFormat.JPEG, 100,
                            new FileOutputStream(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isShowEdit(false);
                AliyunCommon.imgPath=path;
                Log.i("xianghaTag","裁剪完成：："+path);
                if(AliyunCommon.getInstance().aliyunVideoDataCallBack!=null){
                    AliyunCommon.getInstance().aliyunVideoDataCallBack.videoCallBack(mOutputPath,path, String.valueOf(mAliyunIPlayer.getDuration()/1000));
                }
            }

            @Override
            public void onError(int i) {
                Log.i("xianghaTag","裁剪异常：："+i);
            }
        });

    }
    private long coverImgPosition= 1;
    @Override
    public void getSeekPostion(long postion) {
        Log.i("xiangTag","postion:::"+postion);
        if(postion>0&&mAliyunIPlayer!=null) {
            mAliyunIPlayer.pause();
            coverImgPosition= postion;
            mAliyunIPlayer.seek(postion);
        }
    }

    @Override
    public void zoomLayoutParams() {
//        setZoomScreen();
//        initEditor();
    }

    /**
     * 是否在裁剪中
     * @param state true 在裁剪中，false未在
     */
    private void isShowEdit(boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state) {
                    findViewById(R.id.progressBar_layout).setVisibility(View.VISIBLE);
                    aliyun_next.setEnabled(false);
                } else {
                    findViewById(R.id.progressBar_layout).setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    aliyun_next.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        AliyunCommon.getInstance().finishActivity(this);
    }
}
