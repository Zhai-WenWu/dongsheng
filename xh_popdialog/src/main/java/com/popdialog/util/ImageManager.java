package com.popdialog.util;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/19 17:24.
 * E_mail : ztanzeyu@gmail.com
 */

public class ImageManager {
    /**
     * 等比缩小图片并设置到imageView中，zoom强制等比适应宽或高
     *
     * @param img
     * @param bitmap
     * @param width
     * @param height
     * @param zoom
     * @return
     */
    public static ViewGroup.LayoutParams setImgViewByWH(ImageView img, Bitmap bitmap, int width, int height, boolean zoom) {
        ViewGroup.LayoutParams lp = img.getLayoutParams();
        if (bitmap == null)
            return lp;
        if (height > 0 && width > 0 && zoom) {
            lp.height = height;
            lp.width = width;
        } else if (width > 0 && bitmap.getWidth() > 0) {
            lp.height = bitmap.getHeight() * width / bitmap.getWidth();
            lp.width = width;
        } else if (height > 0 && bitmap.getHeight() > 0) {
            lp.height = height;
            lp.width = bitmap.getWidth() * height / bitmap.getHeight();
        }
        if (height > 0 || width > 0) {
            img.setLayoutParams(lp);
        }
        img.setImageBitmap(bitmap);
        return lp;
    }

    /**
     * 删除长期存储图片
     *
     * @param imgUrl
     */
    public static void delImg(String imgUrl) {
        if (imgUrl.length() == 0)
            return;
        String name = StringManager.toMD5(imgUrl, false);
        FileManager.delDirectoryOrFile(FileManager.getSDDir() + "long/" + name, 0);
    }
}
