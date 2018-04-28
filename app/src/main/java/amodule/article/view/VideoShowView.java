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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import acore.tools.FileManager;
import acore.tools.ImgManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.recordervideo.tools.FileToolsCammer;
import aplug.recordervideo.tools.ToolsCammer;
import xh.basic.tool.UtilImage;

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
    private String coverImageUrl,oldCoverImageUrl;
    private String videoUrl;
    private boolean isWrapContent = true;
    private int position;
    private String idStr = "";

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
            jsonObject.put("oldCoverImageUrl", oldCoverImageUrl);
            jsonObject.put("videourl", videoUrl);
            if(!TextUtils.isEmpty(idStr))
                jsonObject.put("id", idStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void resetData(){
        coverImageUrl = "";
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
        if(videoUrl.startsWith("http")){
            this.coverImageUrl = coverImageUrl;
        }else{
            this.coverImageUrl = FileToolsCammer.getImgPath(videoUrl);
        }
        this.videoUrl = videoUrl;
        setVideoImage(false,this.coverImageUrl);
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
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap != null) {
                            if(isCut){
                                handlerCutImage(bitmap,imgUrl);
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

    public void handlerCutImage(final Bitmap bm, final String imgUrl){
        Bitmap newBitmap = null;
        if(!videoUrl.startsWith("http") && !imgUrl.startsWith("http")){
            Bitmap bitmap = ToolsCammer.getFrameAtTime(videoUrl);
            if(bitmap == null && !TextUtils.isEmpty(coverImageUrl)){
                bitmap = BitmapFactory.decodeFile(coverImageUrl);
            }
            //需要裁剪，按照视频尺寸裁剪
            newBitmap = ImgManager.centerScaleBitmap(bitmap,bm);
        }
        if (newBitmap == null || newBitmap.isRecycled())
            return;
        //保存图片
        coverImageUrl = FileToolsCammer.getVideoCatchPath() + Tools.getMD5(imgUrl) + ".jpg";
        FileManager.saveImgToCompletePath(newBitmap, coverImageUrl, Bitmap.CompressFormat.JPEG);
        if (isWrapContent) {
            int newWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
            UtilImage.setImgViewByWH(coverImage, newBitmap, newWidth, 0, false);
        }else {
            setImageToCoverImage(newBitmap);
        }
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
//               //YLKLog.i("tzy","coverImageUrl = " + coverImageUrl);
//               //YLKLog.i("tzy","oldCoverImageUrl = " + oldCoverImageUrl);
                this.coverImageUrl = oldCoverImageUrl;
                oldCoverImageUrl = null;
                findViewById(R.id.video_delete_cover_img).setVisibility(View.GONE);
                setVideoImage(false,this.coverImageUrl);
//               //YLKLog.i("tzy","setVideoImage::coverImageUrl = " + coverImageUrl);
                break;

        }
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getoldCoverImageUrl(){
        return oldCoverImageUrl;
    }

    public void setNewCoverImageUrl(String newCoverImageUrl) {
        this.oldCoverImageUrl = coverImageUrl;
        this.coverImageUrl = newCoverImageUrl;
//       //YLKLog.i("tzy","setNewCoverImageUrl::newCoverImageUrl = " + newCoverImageUrl);
//       //YLKLog.i("tzy","setNewCoverImageUrl::coverImageUrl = " + coverImageUrl);
//       //YLKLog.i("tzy","setNewCoverImageUrl::oldCoverImageUrl = " + oldCoverImageUrl);
        findViewById(R.id.video_delete_cover_img).setVisibility(View.VISIBLE);
        setVideoImage(true,coverImageUrl);
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
