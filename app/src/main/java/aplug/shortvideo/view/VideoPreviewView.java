package aplug.shortvideo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.Map;

import acore.tools.Tools;

/**
 * 单个视频控件
 */

public class VideoPreviewView extends RelativeLayout implements View.OnClickListener {
    /** image path key */
    public static final String IMAGE_PATH = "image";
    /** video path key */
    public static final String VIDEO_PATH = "video";
    /** 是否删除 */
    public static final String IS_DELETE = "isdelete";
    /** 状态key */
    public static final String STATUS = "status";
    /** 默认状态 */
    public static final String DEFAULT = "default";
    /** 选中 */
    public static final String SELECTED = "selected";
    /** 未选中 */
    public static final String UNSELECTED = "unselected";

    /** 预览的ImageView */
    private ImageView mPreImage;
    /** 预览的VideoView */
    private VideoView mPreVideo;
    /** 删除按钮 */
    private ImageView mDeleteImage;
    /** 选中提示框 */
    private TextView mSelectedText;
    /** 未选中阴影 */
    private View mShadowView;

    private ImageView mFakeImageView_1;
    private RelativeLayout mFakeLayout;

    private Map<String, String> mData;

    private boolean isSelected = false;

    private OnSelectListener mOnSelectListener = null;
    private OnReselectListener mOnReselectListener = null;
    private OnUnselectListener mOnUnselectListener = null;
    private OnDeleteListener mOnDeleteListener = null;

    private int roundPx = 0;

    private int position = -1;

    public VideoPreviewView(Context context) {
        this(context, null, 0);
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context, attrs, defStyleAttr);

        roundPx = Tools.getDimen(context, R.dimen.dp_5);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.short_video_select_item, this);

        mPreImage = (ImageView) findViewById(R.id.preview_image);
        mPreVideo = (VideoView) findViewById(R.id.preview_video_view);
        mDeleteImage = (ImageView) findViewById(R.id.delete_image);
        mSelectedText = (TextView) findViewById(R.id.select_tip);
        mShadowView = findViewById(R.id.shadow_view);
        mFakeImageView_1 = (ImageView) findViewById(R.id.fake_image_1);
        mFakeLayout = (RelativeLayout) findViewById(R.id.fake_layout);

        mPreImage.setOnClickListener(this);
        mPreVideo.setOnClickListener(this);
        mDeleteImage.setOnClickListener(this);
        mSelectedText.setOnClickListener(this);
        mShadowView.setOnClickListener(this);
    }

    public void setData(Map<String, String> data) {
        this.mData = data;
        //加载图片
        Glide.with(getContext())
                .load(data.get(IMAGE_PATH))
                .into(mPreImage);
        //加载视频
        mPreVideo.setVideoPath(data.get(VIDEO_PATH));

        setDelete(Boolean.valueOf(data.get(IS_DELETE)));
        switch (data.get(STATUS)) {
            case DEFAULT:
                resetStatus(true);
                break;
            case SELECTED:
                select(true);
                break;
            case UNSELECTED:
                unselect(true);
                break;
        }
    }

    public void setDelete(boolean flag) {
        resetStatus(true);
        mDeleteImage.setVisibility(flag ? VISIBLE : GONE);
    }

    /** 还原状态 */
    public void resetStatus(boolean auto) {
        isSelected = false;
        mFakeImageView_1.setVisibility(isSelected ? GONE : VISIBLE);
        mFakeLayout.setVisibility(isSelected ? VISIBLE : GONE);
        mPreVideo.pause();
        mPreVideo.setVisibility(GONE);
        mSelectedText.setVisibility(GONE);
        mShadowView.setVisibility(GONE);
    }

    /** 选中 */
    public void select(boolean auto) {
        isSelected = true;
        mFakeImageView_1.setVisibility(isSelected ? GONE : VISIBLE);
        mFakeLayout.setVisibility(isSelected ? VISIBLE : GONE);
        mPreVideo.resume();
        mPreVideo.start();
        mPreVideo.setVisibility(VISIBLE);
        mSelectedText.setVisibility(VISIBLE);
        mShadowView.setVisibility(GONE);
        //静音
        mPreVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
            }
        });
        mPreVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPreVideo.resume();
                mPreVideo.start();
            }
        });

        if (!auto && mOnSelectListener != null) {
            mOnSelectListener.onSelect(position, this, mData);
        }
    }

    public void reselect() {
        if (mOnReselectListener != null) {
            mOnReselectListener.onReselect(position, this, mData);
        }
    }

    /** 未选中 */
    public void unselect(boolean auto) {
        isSelected = false;
        mFakeImageView_1.setVisibility(isSelected ? GONE : VISIBLE);
        mFakeLayout.setVisibility(isSelected ? VISIBLE : GONE);
        mPreVideo.pause();
        mPreVideo.setVisibility(GONE);
        mSelectedText.setVisibility(GONE);
        mShadowView.setVisibility(VISIBLE);

        if (!auto && mOnUnselectListener != null) {
            mOnUnselectListener.onUnselect(position, this, mData);
        }
    }

    public void delete() {
        if (mOnDeleteListener != null) {
            mOnDeleteListener.onDelete(position, this, mData);
        }
    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.shadow_view:
            case R.id.preview_image:
                if (Boolean.valueOf(mData.get(IS_DELETE))) {
                    break;
                }
                if (isSelected) {
                    reselect();
                } else {
                    select(false);
                }
                break;
            case R.id.select_tip:
            case R.id.preview_video_view:
                if (Boolean.valueOf(mData.get(IS_DELETE))) {
                    break;
                }
                reselect();
                break;
            case R.id.delete_image:
                delete();
                break;
            default:
                break;
        }
    }

    /** 选中回调 */
    public interface OnSelectListener {
        public void onSelect(int position, View view, Map<String, String> data);
    }

    public interface OnReselectListener {
        public void onReselect(int position, View view, Map<String, String> data);
    }

    /** 未选中回调 */
    public interface OnUnselectListener {
        public void onUnselect(int position, View view, Map<String, String> data);
    }

    /** 删除回调 */
    public interface OnDeleteListener {
        public void onDelete(int position, View view, Map<String, String> data);
    }

    public OnDeleteListener getmOnDeleteListener() {
        return mOnDeleteListener;
    }

    public void setmOnDeleteListener(OnDeleteListener mOnDeleteListener) {
        this.mOnDeleteListener = mOnDeleteListener;
    }

    public OnUnselectListener getmOnUnselectListener() {
        return mOnUnselectListener;
    }

    public void setmOnUnselectListener(OnUnselectListener mOnUnselectListener) {
        this.mOnUnselectListener = mOnUnselectListener;
    }

    public OnSelectListener getmOnSelectListener() {
        return mOnSelectListener;
    }

    public void setmOnSelectListener(OnSelectListener mOnSelectListener) {
        this.mOnSelectListener = mOnSelectListener;
    }

    public OnReselectListener getmOnReselectListener() {
        return mOnReselectListener;
    }

    public void setmOnReselectListener(OnReselectListener mOnReselectListener) {
        this.mOnReselectListener = mOnReselectListener;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
