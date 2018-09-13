package third.aliyun.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.jasonparse.JSONSupportImpl;
import com.quze.videorecordlib.VideoRecorderCommon;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleView;
import com.xiangha.R;

import acore.override.XHApplication;
import acore.tools.Tools;
import third.aliyun.media.GalleryDirChooser;
import third.aliyun.media.GalleryMediaChooser;
import third.aliyun.media.MediaDir;
import third.aliyun.media.MediaInfo;
import third.aliyun.media.MediaStorage;
import third.aliyun.media.ThumbnailGenerator;

/**
 * Description :
 * PackageName : third.aliyun.views
 * Created by mrtrying on 2018/8/13 15:08.
 * e_mail : ztanzeyu@gmail.com
 */
public class MediaSelectView extends RelativeLayout {
    private RelativeLayout topbar;
    private TextView title, rightText,closeText;
    private ImageView close;
    private RecyclerView galleryView;

    private MediaStorage storage;
    private GalleryDirChooser galleryDirChooser;
    private ThumbnailGenerator thumbnailGenerator;
    private GalleryMediaChooser galleryMediaChooser;

    private OnCloseClickCallback onCloseClickCallback;
    private OnSelectMediaCallback onSelectMediaCallback;
    private MediaPlayer mediaPlayer;

    public MediaSelectView(Context context) {
        this(context, null);
    }

    public MediaSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.work_aliyun_svideo_import_view_media, null);
        addView(view,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        initializeUI();
        initializeData();
    }

    private void initializeUI() {
        topbar = findViewById(R.id.gallery_actionBar_rela);
        title = findViewById(R.id.gallery_title);
        rightText = findViewById(R.id.gallery_right_text);
        close = findViewById(R.id.close);
        closeText = findViewById(R.id.close_text);
        galleryView = findViewById(R.id.gallery_media);
        //设置数据和listener
        title.setText(R.string.gallery_all_media);
        close.setOnClickListener(this::handleCloseCallback);
        closeText.setOnClickListener(this::handleCloseCallback);
    }

    private void initializeData() {
        if(mediaPlayer!=null)
            mediaPlayer = new MediaPlayer();
        storage = new MediaStorage(getContext(), new JSONSupportImpl());
        storage.setSortMode(MediaStorage.SORT_MODE_VIDEO);
        storage.startFetchmedias();
        storage.setOnMediaDirChangeListener(() -> {
            MediaDir dir = storage.getCurrentDir();
            title.setText(dir.id != -1 ? dir.dirName : getContext().getString(R.string.gallery_all_media));
            galleryMediaChooser.changeMediaDir(dir);
        });
        storage.setOnCurrentMediaInfoChangeListener(new MediaStorage.OnCurrentMediaInfoChange() {
            @Override
            public void onCurrentMediaInfoChanged(MediaInfo info) {
                try {
                    if(info.width!=0&&info.height>0){
                        if(info.width>1080&&info.height>1080){
                            Tools.showToast(XHApplication.in(),"视频过长的：视频过大，暂不支持");
                            return;
                        }
                    }else {
                        if (mediaPlayer != null) {
                            mediaPlayer.setDataSource(info.filePath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            if(mediaPlayer.getVideoWidth()>1080&&mediaPlayer.getVideoHeight()>1080){
                                Tools.showToast(XHApplication.in(),"视频过长的：视频过大，暂不支持");
                                mediaPlayer.release();
                                return;
                            }
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                if (info.duration < 3000) {
                    dialogShow();
                    return;
                }
                handleSelectMediaCallback(info);
            }
        });

        thumbnailGenerator = new ThumbnailGenerator(getContext());
        galleryDirChooser = new GalleryDirChooser(getContext(), findViewById(R.id.topPanel), thumbnailGenerator, storage);
        galleryMediaChooser = new GalleryMediaChooser(galleryView, galleryDirChooser, storage, thumbnailGenerator);
    }


    private void dialogShow() {
        DialogManager dialogManager = new DialogManager(getContext());
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleView(getContext()).setText("请选择大于3秒的视频"))
                .setView(new HButtonView(getContext())
                        .setNegativeText("确定", v -> dialogManager.cancel())))
                .show();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    public void destroy() {
        if (storage != null) {
            storage.saveCurrentDirToCache();
            storage.cancelTask();
        }
        if (thumbnailGenerator != null) {
            thumbnailGenerator.cancelAllTask();
        }
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    private void handleCloseCallback(View v) {
        if (onCloseClickCallback != null) {
            onCloseClickCallback.onCloseClick(v);
        }
        VideoRecorderCommon.instance().statictisEvent("a_shoot_choose","取消按钮","");
    }

    private void handleSelectMediaCallback(MediaInfo info) {
        if (onSelectMediaCallback != null) {
            onSelectMediaCallback.onSelectMedia(info);
        }
    }

    public void setRightText(String text) {
        rightText.setText(TextUtils.isEmpty(text) ? "" : text);
        rightText.setVisibility(TextUtils.isEmpty(text)?GONE:VISIBLE);
    }

    public void setOnRightClickListener(OnClickListener listener){
        if(listener != null){
            rightText.setOnClickListener(listener);
        }
    }

    public void setOnCloseClickCallback(OnCloseClickCallback onCloseClickCallback) {
        this.onCloseClickCallback = onCloseClickCallback;
    }

    public void setOnSelectMediaCallback(OnSelectMediaCallback onSelectMediaCallback) {
        this.onSelectMediaCallback = onSelectMediaCallback;
    }

    public interface OnCloseClickCallback {
        void onCloseClick(View view);
    }

    public interface OnSelectMediaCallback {
        void onSelectMedia(MediaInfo info);
    }
}
