package amodule.article.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import acore.override.helper.XHActivityManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:41.
 * E_mail : ztanzeyu@gmail.com
 */

public class ImageShowView extends BaseView implements View.OnClickListener {
    public static final int TAG_ID = R.string.tag;
    private ImageView showImage;
    private ImageView showImageGif;
    private ImageView deleteImage;
    private ImageView loadProgress;
    private ImageView itemGifHint;

    private boolean enableEdit = false;
    private boolean isSecondEdit = false;
    private String imageUrl = null;
    private int imageWidth = 0;
    private int imageHieght = 0;
    private String type = IMAGE;
    private String idStr = "";
    private boolean isWrapContent = false;
    private RequestListener<GlideUrl, Bitmap> bitmapRequestListener = new RequestListener<GlideUrl, Bitmap>() {
        @Override
        public boolean onException(Exception e, GlideUrl glideUrl, Target<Bitmap> target, boolean b) {
            removeThis();
            Tools.showToast(getContext(), "文件已损坏");
            return true;
        }

        @Override
        public boolean onResourceReady(Bitmap bitmap, GlideUrl glideUrl, Target<Bitmap> target, boolean b, boolean b1) {
            return false;
        }
    };
    private RequestListener<File, Bitmap> fileRequestListener = new RequestListener<File, Bitmap>() {
        @Override
        public boolean onException(Exception e, File file, Target<Bitmap> target, boolean b) {
            removeThis();
            Tools.showToast(getContext(), "文件已损坏");
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap bitmap, File file, Target<Bitmap> target, boolean b, boolean b1) {
            return false;
        }
    };
    private SubBitmapTarget subBitmapTarget = new SubBitmapTarget() {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
            if (bitmap != null) {
                imageWidth = bitmap.getWidth();
                imageHieght = bitmap.getHeight();

                int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                if (isWrapContent) {
                    if (imageWidth <= newWaith)
                        newWaith = 0;
                }
                UtilImage.setImgViewByWH(showImage, bitmap, newWaith, 0, false);
            }
        }
    };

    public ImageShowView(Context context) {
        this(context, null);
    }

    public ImageShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_view_image, this);
        showImage = (ImageView) findViewById(R.id.image);
        deleteImage = (ImageView) findViewById(R.id.delete_image);
        showImageGif = (ImageView) findViewById(R.id.image_gif);
        loadProgress = (ImageView) findViewById(R.id.load_progress);
        itemGifHint = (ImageView) findViewById(R.id.dish_step_gif_hint);

        showImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
    }

    /**
     * <a href="图片跳转链接">
     * <img src="imageUrl" width="1242" height="2208">
     * </a>
     *
     * @return
     */
    @Override
    public JSONObject getOutputData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", type);
            jsonObject.put(IMAGE_GIF.equals(type) ? "gifurl" : "imageurl", imageUrl);
            jsonObject.put("width", imageWidth);
            jsonObject.put("height", imageHieght);
            if (!TextUtils.isEmpty(idStr))
                jsonObject.put("id", idStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if (imageUrl.endsWith(".gif")) {
            type = IMAGE_GIF;
            if (enableEdit) {
                if (imageUrl.startsWith("http")) {
                    LoadImage.with(getContext())
                            .load(imageUrl)
                            .build()
                            .listener(bitmapRequestListener)
                            .into(subBitmapTarget);

                } else {
                    File file = new File(imageUrl);
                    Glide.with(getContext())
                            .load(file)
                            .asBitmap()
                            .listener(fileRequestListener)
                            .into(subBitmapTarget);
                }

                itemGifHint.setVisibility(VISIBLE);
            } else
                Glide.with(getContext())
                        .load(imageUrl)
                        .asGif()
                        .placeholder(R.drawable.i_nopic)
                        .error(R.drawable.i_nopic)
                        .into(showImage);
        } else {
            type = IMAGE;
            if (imageUrl.startsWith("http")) {
                LoadImage.with(getContext())
                        .load(imageUrl)
                        .build()
                        .listener(bitmapRequestListener)
                        .into(subBitmapTarget);
            } else {
                File file = new File(imageUrl);
                Glide.with(getContext())
                        .load(file)
                        .asBitmap()
                        .listener(fileRequestListener)
                        .into(subBitmapTarget);
            }
        }
    }

    public void showImage(final String imageUrl, String type) {
        itemGifHint.setVisibility(View.GONE);
        showImage.setVisibility(View.VISIBLE);
        loadProgress.clearAnimation();
        loadProgress.setVisibility(View.GONE);

        loadImg(imageUrl);
        if ("gif".equals(type)) {
            this.type = IMAGE_GIF;
            showImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadGif(imageUrl);
                }
            });
            showImageGif.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemGifHint.setVisibility(View.VISIBLE);
                    showImage.setVisibility(View.VISIBLE);
                }
            });
            if ("wifi".equals(ToolsDevice.getNetWorkType(getContext()))) {
                loadGif(imageUrl);
            }
        }
    }

    public void stopGif() {
        itemGifHint.setVisibility(View.VISIBLE);
        showImage.setVisibility(View.VISIBLE);
    }

    private void loadImg(String imgUrl) {
        if (!TextUtils.isEmpty(imgUrl)) {
            showImage.setImageResource(R.drawable.i_nopic);
            showImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            showImage.setTag(TAG_ID, imgUrl);
            if (!XHActivityManager.getInstance().getCurrentActivity().isFinishing()) {
                BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(XHActivityManager.getInstance().getCurrentActivity())
                        .load(imgUrl)
                        .setPlaceholderId(R.drawable.i_nopic)
                        .setErrorId(R.drawable.i_nopic)
                        .build();
                if (requestBuilder != null) {
                    itemGifHint.setVisibility(View.GONE);
                    itemGifHint.setImageResource(R.drawable.i_dish_detail_gif_hint);
                    requestBuilder
                            .listener(bitmapRequestListener)
                            .into(subBitmapTarget);
                }
            }
        }
    }

    /**
     * 设置GIF
     *
     * @param gifUrl
     */
    private void loadGif(final String gifUrl) {
        if (!TextUtils.isEmpty(gifUrl)) {
            if (showImageGif.getTag() == null)
                showImageGif.setTag(TAG_ID, gifUrl);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_anim);
            loadProgress.startAnimation(animation);
            loadProgress.setVisibility(VISIBLE);
            showImage.setVisibility(VISIBLE);
            itemGifHint.setVisibility(View.GONE);
            if (!XHActivityManager.getInstance().getCurrentActivity().isFinishing()) {
                final GifRequestBuilder requestBuilder = Glide.with(XHActivityManager.getInstance().getCurrentActivity())
                        .load(gifUrl)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(new RequestListener<String, GifDrawable>() {
                            @Override
                            public boolean onException(Exception e, String s, Target<GifDrawable> target, boolean b) {
                                removeThis();
                                Tools.showToast(getContext(), "文件已损坏");
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable gifDrawable, String s, Target<GifDrawable> target, boolean b, boolean b1) {
                                if (showImageGif.getTag(TAG_ID).equals(gifUrl)) {
                                    loadProgress.clearAnimation();
                                    loadProgress.setVisibility(GONE);
                                    showImage.setVisibility(View.GONE);
                                    itemGifHint.setVisibility(View.GONE);
                                    setImageWH(showImageGif, showImage.getHeight());
                                }
                                return false;
                            }
                        });
                if (showImageGif != null) {
                    if (showImageGif.getTag(TAG_ID).equals(gifUrl)) {
                        requestBuilder.into(showImageGif);
                    }
                }
            }
        }
    }

    private void setImageWH(ImageView imgView, int imgHeight) {
        imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int dp_290 = Tools.getDimen(getContext(), R.dimen.dp_290);
        LayoutParams layoutParams;
        if (IMAGE_GIF.equals(type)) {
            imgView.setMinimumHeight(0);
            layoutParams = new LayoutParams((int) (imgHeight / 9.0 * 16), imgHeight);
        } else
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, imgHeight > dp_290 ? dp_290 : imgHeight);
        int dp_12 = Tools.getDimen(getContext(), R.dimen.dp_12);
        int dp_23 = Tools.getDimen(getContext(), R.dimen.dp_23);
        layoutParams.setMargins(0, dp_12, dp_23, 0);
        imgView.setLayoutParams(layoutParams);
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setEnableEdit(boolean enable) {
        this.enableEdit = enable;
        deleteImage.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                if (null != mOnClickImageListener) {
                    mOnClickImageListener.onClick(v, imageUrl);
                }
                break;
            case R.id.delete_image:
                removeThis();
                break;
        }
    }

    public void removeThis() {
        if (null != mOnRemoveCallback) {
            mOnRemoveCallback.onRemove(this);
        }
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public boolean isSecondEdit() {
        return isSecondEdit;
    }

    public void setSecondEdit(boolean secondEdit) {
        isSecondEdit = secondEdit;
    }

    public boolean isWrapContent() {
        return isWrapContent;
    }

    public void setWrapContent(boolean wrapContent) {
        isWrapContent = wrapContent;
    }
}
