package third.aliyun.work;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.quze.videorecordlib.media.MediaInfo;
import com.xiangha.R;

import acore.tools.Tools;
import third.aliyun.edit.util.Common;
import third.aliyun.views.MediaSelectView;

/**
 * 视频选择
 */
public class MediaActivity extends Activity {
    public static final int[][] resolutions = new int[][]{new int[]{720, 960}, new int[]{720, 720}, new int[]{720, 1280}};
    private static final int REQUEST_CODE_VIDEO_CROP = 1;
    private static final int REQUEST_CODE_IMAGE_CROP = 2;
    private int mRatio;
    private ScaleMode scaleMode = ScaleMode.LB;
    private int frameRate;
    private int gop;
    private int mBitrate;
    private VideoQuality quality = VideoQuality.SSD;
    private MediaInfo mCurrMediaInfo;
    private int mCropPosition;
    private boolean mIsReachedMaxDuration = false;
    private AliyunVideoParam mVideoParam;
    private int[] mOutputResolution = null;

    private int requestWidth,requestHeight;
    private MediaSelectView mediaSelectView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_aliyun_svideo_import_activity_media);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getData();
        Tools.setStatusBarTrans(this);
        init();
        Common.requestMusic();
        AliyunCommon.getInstance().addActivity(this);
    }

    /**
     * 处理基础化参数
     */
    private void getData() {
        mRatio = getIntent().getIntExtra(CropKey.VIDEO_RATIO, CropKey.RATIO_MODE_9_16);
        scaleMode = (ScaleMode) getIntent().getSerializableExtra(CropKey.VIDEO_SCALE);
        if (scaleMode == null) {
            scaleMode = ScaleMode.LB;
        }
        frameRate = getIntent().getIntExtra(CropKey.VIDEO_FRAMERATE, 25);
        gop = getIntent().getIntExtra(CropKey.VIDEO_GOP, 125);
        mBitrate = getIntent().getIntExtra(CropKey.VIDEO_BITRATE, 0);
        quality = (VideoQuality) getIntent().getSerializableExtra(CropKey.VIDEO_QUALITY);
        if (quality == null) {
            quality = VideoQuality.SSD;
        }
        mOutputResolution = resolutions[mRatio];
        mVideoParam = new AliyunVideoParam.Builder()
                .frameRate(frameRate)
                .gop(gop)
                .bitrate(mBitrate)
                .videoQuality(quality)
                .scaleMode(scaleMode)
                .outputWidth(mOutputResolution[0])
                .outputHeight(mOutputResolution[1])
                .build();
        try {
            requestWidth = Integer.parseInt(getIntent().getStringExtra("width"));
        } catch (Exception e) {
            requestWidth = 0;
        }
        try {
            requestHeight = Integer.parseInt(getIntent().getStringExtra("height"));
        } catch (Exception e) {
            requestHeight = 0;
        }
    }

    private void init() {
        mediaSelectView = findViewById(R.id.mediaSelectView);
        mediaSelectView.setOnCloseClickCallback(view -> onBackPressed());
//        mediaSelectView.setOnRightClickListener(v -> VideoDraftActivity.startActivity(MediaActivity.this));
        mediaSelectView.setOnSelectMediaCallback(info -> {
            //跳转裁剪
            Intent intent = new Intent(MediaActivity.this, AliyunVideoCrop.class);
            intent.putExtra(CropKey.VIDEO_PATH, info.filePath);
            intent.putExtra(CropKey.VIDEO_DURATION, info.duration);
            intent.putExtra("videoInfo",info);
            intent.putExtra(CropKey.VIDEO_RATIO, mRatio);
            intent.putExtra(CropKey.VIDEO_SCALE, scaleMode);
            intent.putExtra(CropKey.VIDEO_QUALITY, quality);
            intent.putExtra(CropKey.VIDEO_GOP, gop);
            intent.putExtra(CropKey.VIDEO_BITRATE, mBitrate);
            intent.putExtra(CropKey.VIDEO_FRAMERATE, frameRate);
            intent.putExtra(CropKey.ACTION, CropKey.ACTION_TRANSCODE);//是否真裁剪
            startActivityForResult(intent, REQUEST_CODE_VIDEO_CROP);
        });
        initTitle();
    }

    private void updateDraftText() {
//        int draftSize = PublishDraftDBHelper.getInstance(this).draftSize();
//        mediaSelectView.setRightText(draftSize > 0 ? "草稿箱("+draftSize +")":"");
    }

    private void initTitle(){
//        if (Tools.isShowTitle()) {
//            LinearLayout rootLayout = findViewById(R.id.rootLayout);
//            rootLayout.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra(CropKey.RESULT_KEY_CROP_PATH);
            switch (requestCode) {
                case REQUEST_CODE_VIDEO_CROP:
//                    long duration = data.getLongExtra(CropKey.RESULT_KEY_DURATION, 0);
//                    long startTime = data.getLongExtra(CropKey.RESULT_KEY_START_TIME, 0);
//                    if (!TextUtils.isEmpty(path) && duration > 0 && mCurrMediaInfo != null) {
//                        int index = mTransCoder.removeMedia(mCurrMediaInfo);
//                        mCurrMediaInfo.filePath = path;
//                        mCurrMediaInfo.startTime = startTime;
//                        mCurrMediaInfo.duration = (int) duration;
//                        mTransCoder.addMedia(index, mCurrMediaInfo);
//                    }
                    break;
                case REQUEST_CODE_IMAGE_CROP:
//                    if (!TextUtils.isEmpty(path) && mCurrMediaInfo != null) {
//                        int index = mTransCoder.removeMedia(mCurrMediaInfo);
//                        mCurrMediaInfo.filePath = path;
//                        mTransCoder.addMedia(index, mCurrMediaInfo);
//                    }
                    break;
            }

        }
    }

    private String convertDuration2Text(long duration) {
        int sec = Math.round(((float) duration) / 1000);
        int hour = sec / 3600;
        int min = (sec % 3600) / 60;
        sec = (sec % 60);
        return String.format(getString(R.string.video_duration),
                hour,
                min,
                sec);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDraftText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaSelectView.destroy();
    }

    //    private EditText frameRateEdit//帧率
//            , gopEdit//关键帧间隔
//            , mBitrateEdit;//码率
//    private ImageView back;
//    private int mRatio;//分辨率
//    private static VideoQuality videoQuality=VideoQuality.HD;;//视频质量
//    private static ScaleMode scaleMode = CropKey.SCALE_CROP;//画面模式选择，crop--裁剪，fill--填充
    @Override
    public void finish() {
        super.finish();
        AliyunCommon.getInstance().finishActivity(this);
    }
}
