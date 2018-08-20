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

import com.xiangha.R;

import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.BlurBitmapTransformation;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;

public class HomeGridXHADItem extends HomeItem {

    private ConstraintLayout mAdContainer;
    private ImageView mImgBlur;
    private ImageView mImg;
    private ImageView mGDTIconImg;
    private TextView mTitle;
    private ImageView mAdHeaderImg;
    private TextView mAdName;

    private final int IMG_VERTICAL = 1;
    private final int IMG_HORIZONTAL = 2;

    private int mImgMinHeight, mImgMaxHeight;

    public HomeGridXHADItem(Context context) {
        this(context, null);
    }

    public HomeGridXHADItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeGridXHADItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_grid_xh_ad_item);
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

        mImgMinHeight = (Tools.getPhoneWidth() - getResources().getDimensionPixelSize(R.dimen.dp_51)) / 2 * 4 / 5;
        mImgMaxHeight = getResources().getDimensionPixelSize(R.dimen.dp_260);
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
                int imgWidth = 0, imgHeight = 0;
                String w_h = imgUrl.substring(imgUrl.lastIndexOf("?"));
                if (!TextUtils.isEmpty(w_h) && w_h.contains("_")) {
                    String[] whs = w_h.split("_");
                    imgWidth = Integer.parseInt(whs[0]);
                    imgHeight = Integer.parseInt(whs[1]);
                }
                int orientation = IMG_HORIZONTAL;
                if (imgWidth <= imgHeight) {
                    orientation = IMG_VERTICAL;
                }
                switch (orientation) {
                    case IMG_HORIZONTAL:
                        mImgBlur.setImageResource(R.drawable.i_nopic);
                        ConstraintSet cs = new ConstraintSet();
                        cs.constrainWidth(mImgBlur.getId(), ConstraintSet.MATCH_CONSTRAINT);
                        cs.constrainHeight(mImgBlur.getId(), mImgMaxHeight);
                        cs.constrainMinHeight(mImgBlur.getId(), mImgMinHeight);
                        cs.applyTo(mAdContainer);
                        mImg.setImageResource(R.drawable.i_nopic);
                        mImg.setVisibility(View.VISIBLE);
                        LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().into(new SubAnimTarget(mImg) {
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
                        LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().transform(new BlurBitmapTransformation(getContext(), 6, 6, 6)).into(mImgBlur);
                        break;
                    case IMG_VERTICAL:
                        mImg.setVisibility(View.GONE);
                        mImg.setImageResource(R.drawable.i_nopic);
                        int realWidth = (Tools.getPhoneWidth() - getResources().getDimensionPixelSize(R.dimen.dp_51)) / 2;
                        int realHeight = realWidth * imgHeight / imgWidth;
                        if (realHeight < mImgMinHeight) {
                            realHeight = mImgMinHeight;
                        } else if (realHeight > mImgMaxHeight) {
                            realHeight = mImgMaxHeight;
                        }

                        ConstraintSet cs2 = new ConstraintSet();
                        cs2.constrainWidth(mImgBlur.getId(), ConstraintSet.MATCH_CONSTRAINT);
                        cs2.constrainHeight(mImgBlur.getId(), realHeight);
                        cs2.constrainMinHeight(mImgBlur.getId(), mImgMinHeight);
                        cs2.applyTo(mAdContainer);

                        LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().into(mImgBlur);
                        break;
                }






            }
        }
        LoadImage.with(getContext()).load(dataMap.get("iconUrl")).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().into(mAdHeaderImg);
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
