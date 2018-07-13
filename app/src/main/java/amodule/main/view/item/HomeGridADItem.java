package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import aplug.basic.BlurBitmapTransformation;

public class HomeGridADItem extends HomeItem {

    private ImageView mImgBlur;
    private ImageView mImg;
    private ConstraintLayout mContentLayout;
    private TextView mTitle;

    public HomeGridADItem(Context context) {
        this(context, null);
    }

    public HomeGridADItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeGridADItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_grid_ad_item);
    }

    @Override
    protected void initView() {
        super.initView();
        mImgBlur = (ImageView) findViewById(R.id.blur);
        mImg = (ImageView) findViewById(R.id.img);
        mContentLayout = (ConstraintLayout) findViewById(R.id.content_layout);
        mTitle = (TextView) findViewById(R.id.title);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if(mDataMap == null)
            return;
        Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
        if (imgMap.size() > 0) {
            String imgUrl = imgMap.get("url");
            if (!TextUtils.isEmpty(imgUrl)) {
                loadImage(imgUrl, mImg, new ADImageLoadCallback() {
                    @Override
                    public void callback(Bitmap bitmap) {
                        if (bitmap != null) {
                            int bmW = bitmap.getWidth();
                            int bmH = bitmap.getHeight();
                            int itemW = getWidth();
                            int itemH = getHeight();
                            float scaledItemWH = itemW * 1.0f / itemH;
                            float scaledBitmapWH = bmW * 1.0f / bmH;
                            int dstW, dstH;
                            if (scaledBitmapWH > scaledItemWH) {
                                dstW = itemW;
                                dstH = bmH * itemW / bmW;
                            } else {
                                dstH = itemH;
                                dstW = itemW * itemH / bmH;
                            }
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(mImg.getLayoutParams());
                            params.width = dstW;
                            params.height = dstH;
                            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                            mImg.setLayoutParams(params);
                            mImg.setImageBitmap(scaledBitmap);
                            invalidate();
                        } else {
                            mImg.setImageResource(0);
                        }
                    }
                });
                Glide.with(mImgBlur.getContext()).load(imgUrl).transform(new BlurBitmapTransformation(mImgBlur.getContext(), 9, 9, 9)).into(mImgBlur);
            }
        }
        String title = mDataMap.get("name");
        mTitle.setText(title);
        mTitle.setVisibility(!TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
        mContentLayout.setVisibility(!TextUtils.isEmpty(title) || (mUserName != null && !TextUtils.isEmpty(mUserName.getText())) ? View.VISIBLE : View.GONE);
    }
}
