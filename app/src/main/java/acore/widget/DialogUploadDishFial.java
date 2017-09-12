package acore.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * Created by XiangHa on 2016/10/31.
 */

public class DialogUploadDishFial {

    private Context mCon;
    private Dialog dialog;
    private Window window;

    public DialogUploadDishFial(Context con){
        mCon = con;
        dialog = new Dialog(con, R.style.dialog);
        dialog.setContentView(R.layout.xh_hint_upload_fial_dialog);
        window = dialog.getWindow();
    }

    public void setCancelable(boolean var1) {
        dialog.setCancelable(var1);
    }

    public DialogUploadDishFial setImage(String imgUrl){
        ImageView imageView = (ImageView) window.findViewById(R.id.dialog_hint_upload_img);
        // 根据类型和位序来设置图片
        if (imgUrl != null && imgUrl.length() > 0) {
            if (imgUrl.indexOf("http") == 0) {
                BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(imageView.getContext())
                        .load(imgUrl)
                        .build();
                if(bitmapRequest != null)
                    bitmapRequest.into(getTarget(imageView,0,0));
            } else {
                dealLocalImg(imageView,imgUrl);
            }
        }
        return this;
    }

    public DialogUploadDishFial setCancelListener(View.OnClickListener onClickListener){
        window.findViewById(R.id.dialog_hint_upload_close).setOnClickListener(onClickListener);
        return this;
    }

    public DialogUploadDishFial setSureListener(View.OnClickListener onClickListener){
        window.findViewById(R.id.dialog_hint_upload_sure).setOnClickListener(onClickListener);
        return this;
    }

    public void cancel(){
        dialog.cancel();
    }
    public void show(){
        dialog.show();
    }

    private void dealLocalImg(final ImageView imageView, final String imgUrl){
        final Handler handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bmp = (Bitmap)msg.obj;
                if(bmp != null){
                    imageView.setImageBitmap(bmp);
                }
            }
        };
        new Thread(new Runnable() {

            @Override
            public void run() {
                Bitmap bmp = UtilImage.imgPathToBitmap(imgUrl, 0, 0, false, null);
                Message msg = new Message();
                msg.obj = bmp;
                handler.sendMessage(msg);

            }
        }).start();
    }

    private SubBitmapTarget getTarget(final ImageView v, final int width_dp, final int height_dp){
        return new SubBitmapTarget(){
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = v;
                if (img != null && bitmap != null) {
                    UtilImage.setImgViewByWH(img, bitmap, ToolsDevice.dp2px(mCon, width_dp), height_dp, false);
                }
            }};
    }

}
