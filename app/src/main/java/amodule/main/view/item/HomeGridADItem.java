package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.xiangha.R;

import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.BlurBitmapTransformation;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;

public class HomeGridADItem extends HomeItem {

    private ConstraintLayout mAdContainer;
    private ImageView mImgBlur;
    private ImageView mImg;
    private ImageView mGDTIconImg;
    private TextView mTitle;
    private ImageView mAdHeaderImg;
    private TextView mAdName;

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
        mAdContainer = findViewById(R.id.ad_container);
        mImgBlur = findViewById(R.id.img_blur);
        mImg = findViewById(R.id.img);
        mGDTIconImg = findViewById(R.id.icon_ad_gdt);
        mTitle = findViewById(R.id.title);
        mAdHeaderImg = findViewById(R.id.ad_header_img);
        mAdName = findViewById(R.id.ad_name);
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
                mImgBlur.setTag(TAG_ID, imgUrl);
                mImgBlur.setImageResource(R.drawable.i_nopic);
                mImg.setTag(TAG_ID, imgUrl);
                mImg.setImageResource(R.drawable.i_nopic);
                BitmapRequestBuilder builder = LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build();
                if (builder != null) {
                    builder.into(new SubAnimTarget(mImg) {
                        @Override
                        protected void setResource(Bitmap bitmap) {
                            if (bitmap != null) {
                                int bmW = bitmap.getWidth();
                                int bmH = bitmap.getHeight();
                                int imgMaxH = mImgBlur.getHeight();
                                int imgMaxW = mImgBlur.getWidth();
                                int imgH = imgMaxW * bmH / bmW;
                                int dstW = imgMaxW;
                                int dstH = Math.min(imgMaxH, imgH);
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                                ConstraintSet cs = new ConstraintSet();
                                cs.constrainWidth(mImg.getId(), dstW);
                                cs.constrainHeight(mImg.getId(), dstH);
                                cs.connect(mImg.getId(), ConstraintSet.TOP, mImgBlur.getId(), ConstraintSet.TOP);
                                cs.connect(mImg.getId(), ConstraintSet.BOTTOM, mImgBlur.getId(), ConstraintSet.BOTTOM);
                                cs.connect(mImg.getId(), ConstraintSet.END, mImgBlur.getId(), ConstraintSet.END);
                                cs.connect(mImg.getId(), ConstraintSet.START, mImgBlur.getId(), ConstraintSet.START);
                                cs.applyTo(mAdContainer);
                                mImg.setImageBitmap(scaledBitmap);
                            }
                        }
                    });
                }
                BitmapRequestBuilder builder1 = LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().transform(new BlurBitmapTransformation(getContext(), 16, 16, 2));
                if (builder1 != null) {
                    builder1.into(mImgBlur);
                }
            }
        }
        String iconUrl = dataMap.get("iconUrl");
        mAdHeaderImg.setTag(TAG_ID, iconUrl);
        mAdHeaderImg.setImageResource(R.drawable.i_nopic);
        BitmapRequestBuilder headerBuilder = LoadImage.with(getContext()).load(iconUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build();
        if (headerBuilder != null) {
            headerBuilder.into(mAdHeaderImg);
        }
        mGDTIconImg.setVisibility("sdk_gdt".equals(mDataMap.get("adClass")) ? View.VISIBLE : View.GONE);
        String adName = mDataMap.get("name");
        if (!TextUtils.isEmpty(adName)) {
            mAdName.setText(adName);
        } else {
            mTitle.setText("");
        }
        String content = mDataMap.get("content");
        mTitle.setText(TextUtils.isEmpty(content) ? "" : content);
    }

    public ConstraintLayout getContentLayout() {
        return mAdContainer;
    }
}
