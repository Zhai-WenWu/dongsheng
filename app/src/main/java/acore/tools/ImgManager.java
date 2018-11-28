package acore.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;
import acore.override.XHApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import aplug.basic.LoadImage;

import com.bumptech.glide.Glide;
import com.xiangha.R;

public class ImgManager extends UtilImage {

    /**
     * 删除长期存储图片
     *
     * @param imgUrl
     */
    public static void delImg(String imgUrl) {
        if (imgUrl.length() == 0)
            return;
        String name = UtilString.toMD5(imgUrl, false);
        FileManager.delDirectoryOrFile(FileManager.getSDDir() + LoadImage.SAVE_LONG + "/" + name, 0);
    }

    /**
     * 长期存储图片到本地，需在多线程中调用
     *
     * @param imgUrl
     * @param type
     */
    public static void saveImg(String imgUrl, String type) {
        if (TextUtils.isEmpty(imgUrl))
            return;
        String name = UtilString.toMD5(imgUrl, false);
        // 图片不存在则下载
        if (FileManager.ifFileModifyByCompletePath(FileManager.getSDDir() + type + "/" + name, -1) == null) {
            LoadImage.with(XHApplication.in())
                    .load(imgUrl)
                    .setSaveType(type)
                    .preload();
        }
    }

    public static void saveImgLong(String imgUrl){
        saveImg(imgUrl,LoadImage.SAVE_LONG);
    }

    public static void loadLongImage(ImageView imageView, String imgUrl){
        loadImage(imageView,imgUrl,LoadImage.SAVE_LONG);
    }

    public static void loadCacheImage(ImageView imageView, String imgUrl){
        loadImage(imageView,imgUrl,LoadImage.SAVE_CACHE);
    }

    public static void loadImage(ImageView imageView, String imgUrl, String type){
        if (TextUtils.isEmpty(imgUrl) || null == imageView)
            return;
        String name = UtilString.toMD5(imgUrl, false);
        final String imagePath = FileManager.getSDDir() + type + "/" + name;
        if (FileManager.ifFileModifyByCompletePath(imagePath, -1) == null) {
            LoadImage.with(XHApplication.in())
                    .load(imgUrl)
                    .setSaveType(type)
                    .build()
                    .into(imageView);
        }else{
            try{
                InputStream inputStream = FileManager.loadFile(imagePath);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            }catch (Exception e){
                LoadImage.with(XHApplication.in())
                        .load(imgUrl)
                        .setSaveType(type)
                        .build()
                        .into(imageView);
            }
        }
    }

    /**
     * 将Bitmap转换成InputStream
     *
     * @param bitmap
     *
     * @return
     */
    public static InputStream bitmapToInputStream(Bitmap bitmap, int kb) {
        int options = 99;
        InputStream is = null;
        byte[] theByte = null;
        int num = 1;
        Bitmap oldBitmap = bitmap;
        // 压缩
        while (bitmap != null && options >= 0) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                boolean isOk = bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                while (!isOk && num < 2) {
                    UtilLog.reportError("bitmap.compress error:" + num, null);
                    options = 99;
                    bitmap = oldBitmap;
                    isOk = bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                    num++;
                }
                if (!isOk) {
                    oldBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                }
                theByte = baos.toByteArray();
                // LogManager.print("d", bitmap.getWidth() + "*" + bitmap.getHeight() + "正在压缩质量"
                // + options + "结果" + (theByte.length / 1024));
                if (theByte.length / 1024 < kb || kb == 0 || options <= 3 || !isOk) {
                    is = new ByteArrayInputStream(theByte);
                    baos.close();
                    break;
                }
                baos.close();
                options -= 1000 / options;
                if (options <= 0)
                    options = 3;
            } catch (Exception e) {
                UtilLog.reportError("图片压缩", e);
                break;
            }
        }
        return is;
    }

    /**
     * @param bitmap 原图
     * @param coverBitmap 选图
     *
     * @return 根据原图尺寸居中裁剪选图
     */
    public static Bitmap centerScaleBitmap(Bitmap bitmap, Bitmap coverBitmap) {
        if (null == bitmap) {
            return null;
        }
        return scaleBitmap(coverBitmap,bitmap.getWidth(),bitmap.getHeight());
    }

    /**
     * @param coverBitmap 选图
     *
     * @return 16:9裁剪选图
     */
    public static Bitmap defaultScaleBitmap(Bitmap coverBitmap){
        return scaleBitmap(coverBitmap,16.0f,9.0f);
    }

    /**
     * 按狂傲裁剪图片
     * @param coverBitmap 需要裁剪的图片
     * @param widht 宽
     * @param height 高
     * @return 裁剪后的图片
     */
    public static Bitmap scaleBitmap(Bitmap coverBitmap,float widht,float height){
        if(coverBitmap == null || coverBitmap.isRecycled()){
            return null;
        }

        final int imageWidth = coverBitmap.getWidth();
        final int imageHieght = coverBitmap.getHeight();
        Bitmap newBitmap = coverBitmap;
        if(imageWidth * height != imageHieght * widht){
            int newImgW = imageWidth;
            int newImgH = (int) (newImgW * height / widht);
            if (newImgH > imageHieght) {
                newImgH = imageHieght;
                newImgW = (int) (imageHieght * widht / height);
                return Bitmap.createBitmap(newBitmap, (imageWidth - newImgW) / 2, 0, newImgW, newImgH);
            } else {
                return Bitmap.createBitmap(newBitmap, 0, (imageHieght - newImgH) / 2, newImgW, newImgH);
            }
        }
        return coverBitmap;
    }

    @Nullable
    public static Bitmap RSBlur(Context context,Bitmap source,int radius){
        return RSBlur(context,source,radius,1/8f);
    }

    public static Bitmap RSBlur(Context context,Bitmap source,int radius,float scale){

        Log.i("tzy","origin size:"+source.getWidth()+"*"+source.getHeight());
        int width = Math.round(source.getWidth() * scale);
        int height = Math.round(source.getHeight() * scale);

        Bitmap inputBmp = Bitmap.createScaledBitmap(source,width,height,false);

        RenderScript renderScript =  RenderScript.create(context);

        Log.i("tzy","scale size:"+inputBmp.getWidth()+"*"+inputBmp.getHeight());

        // Allocate memory for Renderscript to work with

        final Allocation input = Allocation.createFromBitmap(renderScript,inputBmp);
        final Allocation output = Allocation.createTyped(renderScript,input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setInput(input);

        // Set the blur radius
        scriptIntrinsicBlur.setRadius(radius);

        // Start the ScriptIntrinisicBlur
        scriptIntrinsicBlur.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(inputBmp);


        renderScript.destroy();
        return inputBmp;
    }


    public static void tailorImageByUrl(Context context, String url, int imgWidth, int imgHeight, int tailorHeight, OnResourceCallback callback) {
        if (context == null || TextUtils.isEmpty(url) || tailorHeight <= 0) {
            if (callback != null) {
                callback.onResource(null);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = Glide.with(context).load(url).downloadOnly(imgWidth, imgHeight).get();
                    if (callback != null) {
                        callback.onResource(tailorImage(file, tailorHeight));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static ArrayList<Bitmap> tailorImage(File file, int tailorHeight) {
        if (file == null && !file.exists()) {
            return null;
        }
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file), 1024);
            BitmapRegionDecoder brd = BitmapRegionDecoder.newInstance(bis, true);
            final int imgWidth = brd.getWidth();
            final int imgHeight = brd.getHeight();
            final int count = (int) Math.ceil(imgHeight * 1.00 / tailorHeight);
            BitmapFactory.Options bfo = new BitmapFactory.Options();
            Rect rect = new Rect();
            int top = 0;
            int bottom = 0;
            for (int i = 0; i < count; i ++) {
                top = tailorHeight * i;
                bottom = top + tailorHeight;
                bottom = bottom >  imgHeight ? imgHeight : bottom;
                rect.set(0, top, imgWidth, bottom);
                Bitmap bitmap = brd.decodeRegion(rect, bfo);
                if (bitmaps == null) {
                    bitmaps = new ArrayList<>();
                }
                bitmaps.add(bitmap);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmaps;
    }

    public interface OnResourceCallback {
        void onResource (ArrayList<Bitmap> bitmaps);
    }
}
