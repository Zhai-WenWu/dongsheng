package amodule.article.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import org.json.JSONException;
import org.json.JSONObject;

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.recordervideo.tools.ToolsCammer;
import xh.basic.tool.UtilImage;

import static aplug.recordervideo.tools.FileToolsCammer.VIDEO_CATCH;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:42.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoShowView extends BaseView implements View.OnClickListener {
    private ImageView coverImage;
    private ImageView deleteImage;
    private LinearLayout defaultLayout;

    private boolean enableEdit = false;
    private boolean isSecondEdit = false;
    private String coverImageUrl,chooseCoverImageUrl,oldCoverImageUrl;
    private String videoUrl;
    private boolean isWrapContent = true;
    private int position;
    private String idStr = "";

    private double imgWhB;

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
        defaultLayout = (LinearLayout) findViewById(R.id.default_layout);
        int width = ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(),R.dimen.dp_20) * 2;
        int height = width * 9 / 16;
        defaultLayout.setLayoutParams(new LayoutParams(width,height));
        coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverImage.setLayoutParams(new LayoutParams(width,height));
        defaultLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoDefaultClickCallback != null){
                    videoDefaultClickCallback.defaultClick();
                }
            }
        });
        findViewById(R.id.video_choose_cover_img).setOnClickListener(this);
        findViewById(R.id.video_delete_cover_img).setOnClickListener(this);

        coverImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
    }

    /**
     * [video src="videoUrl" poster="coverUrl"][video]
     * @return
     */
    @Override
    public JSONObject getOutputData() {
        JSONObject jsonObject = new JSONObject();
        if(TextUtils.isEmpty(coverImageUrl) && TextUtils.isEmpty(videoUrl))
            return null;
        try {
            jsonObject.put("type", VIDEO);
            jsonObject.put("videosimageurl", coverImageUrl);
            jsonObject.put("oldVideoSimageUrl", oldCoverImageUrl);
            jsonObject.put("chooseCoverImageUrl", chooseCoverImageUrl);
            jsonObject.put("videourl", videoUrl);
            if(!TextUtils.isEmpty(idStr))
                jsonObject.put("id", idStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void resetData(){
        imgWhB = 0;
        coverImageUrl = "";
        chooseCoverImageUrl = "";
        oldCoverImageUrl = "";
        videoUrl = "";
        defaultLayout.setVisibility(VISIBLE);
        findViewById(R.id.video_delete_cover_img).setVisibility(View.GONE);
        coverImage.setImageResource(R.drawable.i_nopic);
    }

    public void setVideoData(String coverImageUrl, String videoUrl) {
        if(TextUtils.isEmpty(coverImageUrl) || TextUtils.isEmpty(videoUrl)){
            return;
        }
        resetData();
        defaultLayout.setVisibility(GONE);
        findViewById(R.id.image_layout).setVisibility(VISIBLE);
        this.coverImageUrl = coverImageUrl;
        this.videoUrl = videoUrl;
        setVideoImage(false,coverImageUrl);
    }

    /**
     * 设置视频上的第一帧图片
     * @param isCut ：是否按照16：9 裁剪图片
     * @param imgUrl ：第一帧图片路径
     */
    private void setVideoImage(final boolean isCut, final String imgUrl){
        LoadImage.with(getContext())
                .load(imgUrl)
                .build()
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap != null) {
                            if(isCut){ //需要剪切图片，按照比例16：9，并且保存起来
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final int imageWidth = bitmap.getWidth();
                                        final int imageHieght = bitmap.getHeight();
                                        Bitmap newBitmap = bitmap;
                                        if(imgWhB <= 0){
                                            Bitmap bitmap = ToolsCammer.getFrameAtTime(videoUrl);
                                            if(bitmap == null && !TextUtils.isEmpty(oldCoverImageUrl)){
                                                bitmap = BitmapFactory.decodeFile(oldCoverImageUrl);
                                            }
                                            if(bitmap != null)imgWhB = bitmap.getWidth() * 1.0 / bitmap.getHeight();
                                        }
                                        Log.i("viewShowView","imgWhb:" + imgWhB);
                                        if(imgWhB > 0 &&  imageHieght * imgWhB != imageWidth) {
                                            int newImgW = imageWidth;
                                            int newImgH = (int) (imageWidth / imgWhB);
                                            if (newImgH > imageHieght) {
                                                newImgH = imageHieght;
                                                newImgW = (int) (imageHieght * imgWhB);
                                                newBitmap = Bitmap.createBitmap(newBitmap, (imageWidth - newImgW) / 2, 0, newImgW, newImgH);
                                            } else {
                                                newBitmap = Bitmap.createBitmap(newBitmap, 0, (imageHieght - newImgH) / 2, newImgW, newImgH);
                                            }
                                        }

//                                        if(imageWidth * 9 != imageHieght * 16){
//                                            int newImgW = imageWidth;
//                                            int newImgH = (int) (newImgW * 9.0 / 16);
//                                            if (newImgH > imageHieght) {
//                                                newImgH = imageHieght;
//                                                newImgW = (int) (imageHieght * 16.0 / 9);
//                                                newBitmap = Bitmap.createBitmap(newBitmap, (imageWidth - newImgW) / 2, 0, newImgW, newImgH);
//                                            } else {
//                                                newBitmap = Bitmap.createBitmap(newBitmap, 0, (imageHieght - newImgH) / 2, newImgW, newImgH);
//                                            }
//                                            chooseCoverImageUrl = FileManager.getSDDir() + VIDEO_CATCH + Tools.getMD5(imgUrl) + ".jpg";
//                                            coverImageUrl = chooseCoverImageUrl;
//                                            FileManager.saveImgToCompletePath(newBitmap, chooseCoverImageUrl, Bitmap.CompressFormat.JPEG);
//                                        }
                                        final Bitmap finalBitmap = newBitmap;
                                        coverImage.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isWrapContent) {
                                                    int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                                                    UtilImage.setImgViewByWH(coverImage, finalBitmap, newWaith, 0, false);
                                                }else {
                                                    setImageToCoverImage(finalBitmap);
                                                }
                                            }
                                        });
                                    }
                                }).start();
                            }else {
                                if (isWrapContent) {
                                    int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                                    UtilImage.setImgViewByWH(coverImage, bitmap, newWaith, 0, false);
                                } else {
                                    setImageToCoverImage(bitmap);
                                }
                            }
                        }
                    }
                });
    }

    private void setImageToCoverImage(Bitmap bitmap){
        int imageWidth = bitmap.getWidth();
        int imageHieght = bitmap.getHeight();
        int width = ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_20) * 2;
        int height = width * 9 / 16;
        coverImage.setLayoutParams(new LayoutParams(width, height));
        coverImage.setScaleType(imageWidth > imageHieght ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        coverImage.setBackgroundColor(Color.parseColor("#000000"));
        coverImage.setImageBitmap(bitmap);
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
            case R.id.video_choose_cover_img:
                if(mOnChooseImageListener != null) mOnChooseImageListener.onClick(VideoShowView.this);
                break;
            case R.id.video_delete_cover_img:
                chooseCoverImageUrl = null;
                coverImageUrl = oldCoverImageUrl;
                findViewById(R.id.video_delete_cover_img).setVisibility(View.GONE);
                setVideoImage(false,coverImageUrl);
                break;

        }
    }

    public void setOldCoverImageUrl(String url){
        Log.i("viewShowView","setOldCoverImageUrl() url:" + url);
        if(TextUtils.isEmpty(oldCoverImageUrl))
            oldCoverImageUrl = url;
        Log.i("viewShowView","setOldCoverImageUrl() oldCoverImageUrl:" + oldCoverImageUrl);
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setChooseCoverImageUrl(String chooseCoverImageUrl) {
        this.chooseCoverImageUrl = chooseCoverImageUrl;
        setOldCoverImageUrl(coverImageUrl);
        coverImageUrl = chooseCoverImageUrl;
        findViewById(R.id.video_delete_cover_img).setVisibility(View.VISIBLE);
        setVideoImage(true,chooseCoverImageUrl);
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

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String id) {
        this.idStr = id;
    }

    /** 视频view点击回调 */
    public interface VideoClickCallBack {
        public void videoOnClick(int position);
    }

    private VideoClickCallBack mVideoClickCallBack;

    public void setVideoClickCallBack(VideoClickCallBack clickCallBack) {
        mVideoClickCallBack = clickCallBack;
    }
    private VideoDefaultClickCallback videoDefaultClickCallback;
    public interface VideoDefaultClickCallback{
        public void defaultClick();
    }

    public void setVideoDefaultClickCallback(VideoDefaultClickCallback videoDefaultClickCallback) {
        this.videoDefaultClickCallback = videoDefaultClickCallback;
    }

    public boolean isWrapContent() {
        return isWrapContent;
    }

    public void setWrapContent(boolean wrapContent) {
        isWrapContent = wrapContent;
    }

    public boolean isSecondEdit() {
        return isSecondEdit;
    }

    public void setSecondEdit(boolean secondEdit) {
        isSecondEdit = secondEdit;
    }
}
