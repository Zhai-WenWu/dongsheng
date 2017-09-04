package aplug.recordervideo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.xiangha.R;

import acore.tools.Tools;
import aplug.recordervideo.tools.FileToolsCammer;

/**
 * PackageName : aplug.shortvideo.view
 * Created by MrTrying on 2016/9/22 09:41.
 * E_mail : ztanzeyu@gmail.com
 */

public class RecorderVideoPreviewView extends RelativeLayout implements View.OnClickListener {
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

    private boolean isSelected = false, mIsDelete = false;

    private OnSelectListener mOnSelectListener = null;
    private OnReselectListener mOnReselectListener = null;
    private OnUnselectListener mOnUnselectListener = null;
    private OnDeleteListener mOnDeleteListener = null;

    private int roundPx = 0;

    private int position = -1;

    public RecorderVideoPreviewView(Context context) {
        this(context, null, 0);
    }

    public RecorderVideoPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderVideoPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);

        roundPx = Tools.getDimen(context, R.dimen.dp_5);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.a_video_choose_item_preview, this);
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

    public void setData(String videoPath,String isDelete,String state,boolean isShowSelectHint) {
        mIsDelete = TextUtils.isEmpty(state) ? false : Boolean.parseBoolean(isDelete);
        //加载图片
        mPreImage.setImageBitmap(FileToolsCammer.getBitmapByImgPath(videoPath));
        //加载视频
        mPreVideo.setVideoPath(videoPath);
        if(isShowSelectHint){
            findViewById(R.id.select_tip_parent_layout).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.select_tip_parent_layout).setVisibility(View.GONE);
        }
        setDelete();
        switch (state) {
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

    public void setDelete() {
        resetStatus(true);
        mDeleteImage.setVisibility(mIsDelete ? VISIBLE : GONE);
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
            mOnSelectListener.onSelect();
        }
    }

    public void reselect() {
        if (mOnReselectListener != null) {
            mOnReselectListener.onReselect(position);
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
            mOnUnselectListener.onUnselect(position);
        }
    }

    public void delete() {
        if (mOnDeleteListener != null) {
            mOnDeleteListener.onDelete(position);
        }
    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.shadow_view:
            case R.id.preview_image:
                if (Boolean.valueOf(mIsDelete)) {
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
                if (Boolean.valueOf(mIsDelete)) {
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
        public void onSelect();
    }

    public interface OnReselectListener {
        public void onReselect(int position);
    }

    /** 未选中回调 */
    public interface OnUnselectListener {
        public void onUnselect(int position);
    }

    /** 删除回调 */
    public interface OnDeleteListener {
        public void onDelete(int position);
    }

    public OnDeleteListener getOnDeleteListener() {
        return mOnDeleteListener;
    }

    public void setOnDeleteListener(OnDeleteListener mOnDeleteListener) {
        this.mOnDeleteListener = mOnDeleteListener;
    }

    public OnUnselectListener getOnUnselectListener() {
        return mOnUnselectListener;
    }

    public void setOnUnselectListener(OnUnselectListener mOnUnselectListener) {
        this.mOnUnselectListener = mOnUnselectListener;
    }

    public OnSelectListener getOnSelectListener() {
        return mOnSelectListener;
    }

    public void setOnSelectListener(OnSelectListener mOnSelectListener) {
        this.mOnSelectListener = mOnSelectListener;
    }

    public OnReselectListener getOnReselectListener() {
        return mOnReselectListener;
    }

    public void setOnReselectListener(OnReselectListener mOnReselectListener) {
        this.mOnReselectListener = mOnReselectListener;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
