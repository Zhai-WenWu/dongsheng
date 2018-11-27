package aplug.basic;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import acore.tools.ImgManager;
import xh.basic.tool.UtilImage;

public class RSBlurBitmapTransformation extends BitmapTransformation {

    private Context mContext;
    private int mRadius;

    public RSBlurBitmapTransformation(Context context, int radius) {
        super(context);
        this.mContext = context;
        this.mRadius = radius;
    }

    @Override
    protected Bitmap transform(BitmapPool bitmapPool, Bitmap bitmap, int i, int i1) {
        return ImgManager.RSBlur(mContext,bitmap,mRadius);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
