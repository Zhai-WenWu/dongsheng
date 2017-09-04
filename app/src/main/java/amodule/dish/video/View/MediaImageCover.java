package amodule.dish.video.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.io.File;
import java.io.FileInputStream;

import acore.override.view.ItemBaseView;
import xh.basic.tool.UtilImage;

/**
 * 地址
 */
public class MediaImageCover extends ItemBaseView {
    public MediaImageCover(Context context, int layoutId) {
        super(context, R.layout.media_cover_url);
    }

    public MediaImageCover(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, R.layout.media_cover_url);
    }

    public MediaImageCover(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, R.layout.media_cover_url);
    }

    public View setCoverView(String imgUrl, int waith, int height){
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(waith, height);
        setLayoutParams(layoutParams);
        setDrawingCacheEnabled(true);
        RelativeLayout rela_cover= (RelativeLayout) findViewById(R.id.rela_cover);
        rela_cover.setLayoutParams(layoutParams);
        ImageView image= (ImageView) findViewById(R.id.image);
        Bitmap bitmap=getBitmapFromPath(imgUrl);
        if(bitmap!=null){
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            UtilImage.setImgViewByWH(image, bitmap, waith, height, false);
            return this;
        }
        return null;
    }

    public Bitmap getBitmapFromPath(String path) {

        if (!new File(path).exists()) {
            return null;
        }
        byte[] buf = new byte[1024 * 1024];// 1M
        Bitmap bitmap = null;

        try {
            FileInputStream fis = new FileInputStream(path);
            int len = fis.read(buf, 0, buf.length);
            bitmap = BitmapFactory.decodeByteArray(buf, 0, len);
            if (bitmap == null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
