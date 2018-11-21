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
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;

public class HomeGridADItem extends HomeItem {

    private ConstraintLayout mAdContainer;
    private ImageView mImg;
    private ImageView mGDTIconImg;
    private TextView mTitle;
    private ImageView mAdHeaderImg;
    private TextView mAdName;

    private boolean isHeightImg;

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
                int imgMaxW = mAdContainer.getWidth() - mAdContainer.getPaddingLeft() - mAdContainer.getPaddingRight();
                int bmW = isHeightImg ? 800 : 1280;
                int bmH = isHeightImg ? 1200 : 720;
                final int computeH = imgMaxW * bmH / bmW;
                final int computeW = imgMaxW;
                setImgLayoutParams(computeW, computeH);
                mImg.setTag(TAG_ID, imgUrl);
                mImg.setImageResource(R.drawable.i_nopic);
                BitmapRequestBuilder builder = LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build();
                if (builder != null) {
                    builder.into(new SubAnimTarget(mImg) {
                        @Override
                        protected void setResource(Bitmap bitmap) {
                            int dstH = 0, dstW = 0;
                            if (bitmap != null) {
                                if (!isHeightImg) {
                                    int bmW = bitmap.getWidth();
                                    int bmH = bitmap.getHeight();
                                    int imgMaxW = mAdContainer.getWidth() - mAdContainer.getPaddingLeft() - mAdContainer.getPaddingRight();
                                    dstH = imgMaxW * bmH / bmW;
                                    dstW = imgMaxW;
                                    setImgLayoutParams(dstW, dstH);
                                } else {
                                    dstH = computeH;
                                    dstW = computeW;
                                }
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                                mImg.setImageBitmap(scaledBitmap);
                            }
                        }
                    });
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
        mAdName.setText(!TextUtils.isEmpty(adName) ? adName : "");
        String content = mDataMap.get("content");
        mTitle.setText(TextUtils.isEmpty(content) ? "" : content);
    }

    private void setImgLayoutParams(int w, int h) {
        ConstraintSet set = new ConstraintSet();
        set.constrainWidth(mImg.getId(), w);
        set.constrainHeight(mImg.getId(), h);
        set.connect(mImg.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(mImg.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(mImg.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.connect(mImg.getId(), ConstraintSet.BOTTOM, mTitle.getId(), ConstraintSet.TOP);
        set.applyTo(mAdContainer);
    }

    public void setHeightImg(boolean heightImg) {
        isHeightImg = heightImg;
    }

    public ConstraintLayout getContentLayout() {
        return mAdContainer;
    }
}
