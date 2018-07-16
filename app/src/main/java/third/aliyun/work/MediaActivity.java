package third.aliyun.work;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.qupai.import_core.AliyunIImport;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleView;
import com.xiangha.R;

import acore.tools.Tools;
import third.aliyun.edit.util.Common;
import third.aliyun.media.GalleryDirChooser;
import third.aliyun.media.GalleryMediaChooser;
import third.aliyun.media.MediaDir;
import third.aliyun.media.MediaInfo;
import third.aliyun.media.MediaStorage;
import third.aliyun.media.ThumbnailGenerator;

/**
 * 视频选择
 */
public class MediaActivity extends Activity implements View.OnClickListener {
    public static final int[][] resolutions = new int[][]{new int[]{540, 720}, new int[]{540, 540}, new int[]{540, 960}};
    private static final int REQUEST_CODE_VIDEO_CROP = 1;
    private static final int REQUEST_CODE_IMAGE_CROP = 2;
    private MediaStorage storage;
    private GalleryDirChooser galleryDirChooser;
    private ThumbnailGenerator thumbnailGenerator;
    private GalleryMediaChooser galleryMediaChooser;
    private RecyclerView galleryView;
    private TextView title;
    private int mRatio;
    private ScaleMode scaleMode = ScaleMode.LB;
    private int frameRate;
    private int gop;
    private int mBitrate;
    private VideoQuality quality = VideoQuality.SSD;
    private AliyunIImport mImport;
    private MediaInfo mCurrMediaInfo;
    private int mCropPosition;
    private boolean mIsReachedMaxDuration = false;
    private AliyunVideoParam mVideoParam;
    private int[] mOutputResolution = null;

    private int requestWidth;
    private int requestHeight;
    private ImageView close;
    private RelativeLayout gallery_actionBar_rela;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_aliyun_svideo_import_activity_media);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getData();
        Tools.setStatusBarTrans(this);
        init();
//        Common.requestMusic();
        AliyunCommon.getInstance().addActivity(this);
        Log.i("xianghaTag","MediaActivity::::111111");
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
        galleryView = (RecyclerView) findViewById(R.id.gallery_media);
        title = (TextView) findViewById(R.id.gallery_title);
        title.setText(R.string.gallery_all_media);
        close= (ImageView) findViewById(R.id.close);
        close.setOnClickListener(this);
        storage = new MediaStorage(this, new JSONSupportImpl());
        thumbnailGenerator = new ThumbnailGenerator(this);
        galleryDirChooser = new GalleryDirChooser(this, findViewById(R.id.topPanel), thumbnailGenerator, storage);
        galleryMediaChooser = new GalleryMediaChooser(galleryView, galleryDirChooser, storage, thumbnailGenerator);
        storage.setSortMode(MediaStorage.SORT_MODE_VIDEO);
        storage.startFetchmedias();
        storage.setOnMediaDirChangeListener(new MediaStorage.OnMediaDirChange() {
            @Override
            public void onMediaDirChanged() {
                MediaDir dir = storage.getCurrentDir();
                if (dir.id == -1) {
                    title.setText(getString(R.string.gallery_all_media));
                } else {
                    title.setText(dir.dirName);
                }
                galleryMediaChooser.changeMediaDir(dir);
            }
        });
        storage.setOnCurrentMediaInfoChangeListener(new MediaStorage.OnCurrentMediaInfoChange() {
            @Override
            public void onCurrentMediaInfoChanged(MediaInfo info) {
                //到裁剪页面
                if(info.duration<3000){
                    dialogShow();
                    return;
                }
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


            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //首先回调的方法 返回int表示是否监听该方向
                int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;//拖拽
                int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//侧滑删除
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //滑动事件
//                mTransCoder.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //是否可拖拽
                return true;
            }
        });
        gallery_actionBar_rela= (RelativeLayout) findViewById(R.id.gallery_actionBar_rela);
        initTitle();
    }
    private void initTitle(){
        if (Tools.isShowTitle()) {
            int height = Tools.getStatusBarHeight(this);
            gallery_actionBar_rela.setPadding(0, height, 0, 0);
        }
    }
    private void dialogShow(){
        DialogManager dialogManager = new DialogManager(this);
        dialogManager.createDialog(new ViewManager(dialogManager)
        .setView(new TitleView(this).setText("请选择大于3秒的视频"))
        .setView(new HButtonView(this).setNegativeText("确定", v -> dialogManager.cancel()))).show();
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
    protected void onDestroy() {
        super.onDestroy();
        storage.saveCurrentDirToCache();
        storage.cancelTask();
        thumbnailGenerator.cancelAllTask();
    }

    @Override
    public void onClick(View v) {
        if (v == close) {
            finish();
        }
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
