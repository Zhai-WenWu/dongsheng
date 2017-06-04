package amodule.article.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.view.item.HomeRecipeItem;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.video.VideoImagePlayerController;
import xh.basic.tool.UtilImage;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:42.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoShowView extends BaseView implements View.OnClickListener {
    private ImageView coverImage;
    private ImageView deleteImage;
    private RelativeLayout videoLayout;

    private boolean enableEdit = false;
    private String coverImageUrl;
    private String videoUrl;
    private int position;

    public VideoShowView(Context context) {
        this(context, null);
    }

    public VideoShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_view_video, this);
        coverImage = (ImageView) findViewById(R.id.video_cover_image);
        deleteImage = (ImageView) findViewById(R.id.delete_image);
        videoLayout = (RelativeLayout) findViewById(R.id.video_layout);

        coverImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
    }

    /**
     * [video src="videoUrl" poster="coverUrl"][video]
     *
     * @return
     */
    @Override
    public JSONObject getOutputData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", VIDEO);
            jsonObject.put("videosimageurl", coverImageUrl);
            jsonObject.put("videourl", videoUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void setVideoDataFromService(String coverImageUrl, String videoUrl, int position) {
        this.position = position;
        this.coverImageUrl = coverImageUrl;
        this.videoUrl = videoUrl;
        LoadImage.with(getContext())
                .load(coverImageUrl)
                .build()
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap != null) {
                            int imageWidth = bitmap.getWidth();
                            int imageHieght = bitmap.getHeight();

                            int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                            int waith = newWaith;
                            if (imageWidth <= newWaith) waith = 0;
                            UtilImage.setImgViewByWH(coverImage, bitmap, waith, 0, false);

                        }
                    }
                });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Tools.getDimen(getContext(), R.dimen.dp_200));//
        videoLayout.setPadding(0, 0, 0, ToolsDevice.dp2px(getContext(), 5));
        videoLayout.setLayoutParams(params);
    }

    public void setVideoData(String coverImageUrl, String videoUrl) {
        this.coverImageUrl = coverImageUrl;
        this.videoUrl = videoUrl;
        LoadImage.with(getContext())
                .load(coverImageUrl)
                .build()
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap != null) {
                            int imageWidth = bitmap.getWidth();
                            int imageHieght = bitmap.getHeight();

                            int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                            int waith = newWaith;
                            if (imageWidth <= newWaith) waith = 0;
                            UtilImage.setImgViewByWH(coverImage, bitmap, waith, 0, false);
                        }
                    }
                });
    }

    public void setEnableEdit(boolean enable) {
        this.enableEdit = enable;
        deleteImage.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_cover_image:
                if (mVideoClickCallBack != null) {
                    mVideoClickCallBack.videoOnClick(position);
                    coverImage.setVisibility(GONE);
                    findViewById(R.id.video_cover_image_play).setVisibility(GONE);
                }
                if (null != mOnClickImageListener) {
                    mOnClickImageListener.onClick(v, videoUrl);
                }
                break;
            case R.id.delete_image:
                if (null != mOnRemoveCallback) {
                    mOnRemoveCallback.onRemove(this);
                }
                break;
        }
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    /** 视频view点击回调 */
    public interface VideoClickCallBack {
        public void videoOnClick(int position);
    }

    private VideoClickCallBack mVideoClickCallBack;

    public void setVideoClickCallBack(VideoClickCallBack clickCallBack) {
        mVideoClickCallBack = clickCallBack;
    }
}
