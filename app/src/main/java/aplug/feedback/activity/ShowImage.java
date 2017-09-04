package aplug.feedback.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.github.chrisbanes.photoview.PhotoView;
import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

/**
 * Created by Fang Ruijiao on 2017/3/15.
 */

public class ShowImage extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_show_img);
        init();
    }

    private void init(){
        final String img = getIntent().getStringExtra("img");
        if(TextUtils.isEmpty(img)){
            Tools.showToast(this,"图片预览失败");
            finish();
        }
        final PhotoView imageTouchView = (PhotoView) findViewById(R.id.a_show_img);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
                .load(img)
                .setSaveType(FileManager.save_cache)
                .build();
        if (bitmapRequest != null) {
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
//                    imageTouchView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageTouchView.setImageBitmap(bitmap);
                }
            });
        }
        findViewById(R.id.a_show_img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImage.this.finish();
            }
        });
    }
}
