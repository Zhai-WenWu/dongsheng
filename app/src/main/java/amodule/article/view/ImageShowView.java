package amodule.article.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

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
    private ImageView showImage;
    private ImageView deleteImage;

    private boolean enableEdit = false;
    private String imageUrl = null;
    private int imageWidth = 0;
    private int imageHieght = 0;
    private String type = IMAGE;

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
            jsonObject.put("imageWidth", imageWidth);
            jsonObject.put("imageHieght", imageHieght);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if (imageUrl.endsWith(".gif")) {
            type = IMAGE_GIF;
            if (enableEdit)
                Glide.with(getContext()).load(imageUrl).asBitmap().into(showImage);
            else
                Glide.with(getContext())
                        .load(imageUrl)
                        .asGif()
                        .placeholder(R.drawable.i_nopic)
                        .error(R.drawable.i_nopic)
                        .into(showImage);
        } else {
            type = IMAGE;
            LoadImage.with(getContext())
                    .load(imageUrl)
                    .build()
                    .into(new SubBitmapTarget() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            if(bitmap != null){
                                imageWidth = bitmap.getWidth();
                                imageHieght = bitmap.getHeight();

                                int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                                int waith = newWaith;
                                if (imageWidth <= newWaith)
                                    waith = 0;
                                UtilImage.setImgViewByWH(showImage, bitmap, waith, 0, false);

                            }
                        }
                    });
        }
    }

    public void showImage(String imageUrl,String type){
        switch (type){
            case "gif":
                Glide.with(getContext()).load(imageUrl).asGif().into(showImage);
                break;
            default:
                LoadImage.with(getContext())
                        .load(imageUrl)
                        .build()
                        .into(new SubBitmapTarget() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                imageWidth = bitmap.getWidth();
                                imageHieght = bitmap.getHeight();

                                int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_20) * 2;
                                int waith = newWaith;
                                if (imageWidth <= newWaith)
                                    waith = 0;
                                UtilImage.setImgViewByWH(showImage, bitmap, waith, 0, false);
                            }
                        });
                    break;
        }
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
                if (null != mOnRemoveCallback) {
                    mOnRemoveCallback.onRemove(this);
                }
                break;
        }
    }
}
