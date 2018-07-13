package aplug.basic;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import xh.basic.tool.UtilImage;

public class BlurBitmapTransformation extends BitmapTransformation {

    private int mHRadius, mVRadius, mIterators;

    public BlurBitmapTransformation(Context context, int hRadius, int vRadius, int iterators) {
        super(context);
        mHRadius = hRadius;
        mVRadius = vRadius;
        mIterators = iterators;
    }

    @Override
    protected Bitmap transform(BitmapPool bitmapPool, Bitmap bitmap, int i, int i1) {
        return UtilImage.BoxBlurFilter(bitmap, mHRadius, mVRadius, mIterators);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
