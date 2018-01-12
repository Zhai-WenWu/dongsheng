package third.push;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.widget.RemoteViews;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import acore.tools.FileManager;
import aplug.basic.LoadImage;

/**
 * Created by admain on 2018/1/11.
 */

public class ImageRemoteViews extends RemoteViews {
    public ImageRemoteViews(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public ImageRemoteViews(RemoteViews landscape, RemoteViews portrait) {
        super(landscape, portrait);
    }

    public ImageRemoteViews(Parcel parcel) {
        super(parcel);
    }

    public void loadImage(String url, Context context, Callback callback) {
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(context)
                .load(url)
                .setSaveType(FileManager.save_cache)
                .build();
        if (bitmapRequest != null){
            bitmapRequest.into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (callback != null)
                        callback.callback(bitmap);
                }
            });
        }
    }

    public interface Callback{
        void callback(Bitmap bitmap);
    }

}
