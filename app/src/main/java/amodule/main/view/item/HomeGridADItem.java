package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.xiangha.R;

import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import third.ad.scrollerAd.XHScrollerAdParent;

public class HomeGridADItem extends HomeItem {

    private RelativeLayout mAdContainer;
    private ImageView mImg;
    private ImageView mGDTIconImg;
    private TextView mTitle;
    private ImageView mAdHeaderImg;
    private TextView mAdName;

    private boolean isGdtHeightImg;

    private int mScreenWidth;
    private int[] mXhWH = new int[]{800, 1200};// w * h
    private int[] mGdtHeightWH = new int[]{800, 1200};//gdt 高图
    private int[] mGdtWidthWH = new int[]{1280, 720};//gdt 宽图
    private int[] mBaiduWidthWH = new int[]{1280, 720};
    private int mRecyclerViewPaddingL, mRecyclerViewPaddingR;

    private String mAdClass;

    public HomeGridADItem(Context context) {
        this(context, null);
    }

    public HomeGridADItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeGridADItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_grid_ad_item);

        mScreenWidth = Tools.getPhoneWidth();
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
        mAdClass = dataMap.get("adClass");
        Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
        if (imgMap.size() > 0) {
            String imgUrl = imgMap.get("url");
            if (!TextUtils.isEmpty(imgUrl)) {
                int imgMaxW = (mScreenWidth - mRecyclerViewPaddingL - mRecyclerViewPaddingR)/ 2 - mAdContainer.getPaddingLeft() - mAdContainer.getPaddingRight();
                int fixedW = 0, fixedH = 0;
                boolean xhHasWHTemp = false;
                switch (mAdClass) {
                    case XHScrollerAdParent.ADKEY_BANNER:
                        int index = imgUrl.lastIndexOf("?");
                        if (index > -1) {
                            String[] whStr = imgUrl.substring(index + 1).split("_");
                            if (whStr.length == 2) {
                                int originalW = Tools.parseIntOfThrow(whStr[0], 0);
                                int originalH = Tools.parseIntOfThrow(whStr[1], 0);
                                if (originalW != 0 && originalH != 0) {
                                    fixedH = imgMaxW * originalH / originalW;
                                    fixedW = imgMaxW;
                                    xhHasWHTemp = true;
                                }
                            }
                        }
                        int tempH = imgMaxW * mXhWH[1] / mXhWH[0];
                        fixedW = fixedW > 0 ? fixedW : mXhWH[0];
                        fixedH = fixedH > tempH ? tempH : fixedH;
                        break;
                    case XHScrollerAdParent.ADKEY_GDT:
                        fixedW = isGdtHeightImg ? mGdtHeightWH[0] : mGdtWidthWH[0];
                        fixedH = isGdtHeightImg ? mGdtHeightWH[1] : mGdtWidthWH[1];
                        break;
                    case XHScrollerAdParent.ADKEY_BAIDU:
                        fixedW = mBaiduWidthWH[0];
                        fixedH = mBaiduWidthWH[1];
                        break;
                }
                final boolean xhHasWH = xhHasWHTemp;
                final int computeH = imgMaxW * fixedH / fixedW;
                final int computeW = imgMaxW;
                ViewGroup.LayoutParams lp = mImg.getLayoutParams();
                lp.height = computeH;
                mImg.setTag(TAG_ID, imgUrl);
                mImg.setImageResource(R.drawable.i_nopic);
                BitmapRequestBuilder builder = LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build();
                if (builder != null) {
                    builder.into(new SubAnimTarget(mImg) {
                        @Override
                        protected void setResource(Bitmap bitmap) {
                            if (xhHasWH) {
                                mImg.setImageBitmap(bitmap);
                                return;
                            }
                            int dstH = 0, dstW = 0;
                            if (bitmap != null) {
                                if (!isGdtHeightImg) {
                                    int bmW = bitmap.getWidth();
                                    int bmH = bitmap.getHeight();
                                    dstH = imgMaxW * bmH / bmW;
                                    dstW = imgMaxW;
                                    int imgH = 0;
                                    switch (mAdClass) {
                                        case XHScrollerAdParent.ADKEY_GDT:
                                            imgH = dstH;
                                            break;
                                        case XHScrollerAdParent.ADKEY_BAIDU:
                                            imgH = dstH;
                                            break;
                                    }
                                    lp.height = imgH;
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
        mGDTIconImg.setVisibility(XHScrollerAdParent.ADKEY_GDT.equals(mAdClass) ? View.VISIBLE : View.GONE);
        String adName = mDataMap.get("name");
        mAdName.setText(!TextUtils.isEmpty(adName) ? adName : "");
        String content = mDataMap.get("content");
        mTitle.setText(TextUtils.isEmpty(content) ? "" : content);
    }

    public void setGdtHeightImg(boolean gdtHeightImg) {
        isGdtHeightImg = gdtHeightImg;
    }

    public void setParentPaddingLR(int recyclerViewPaddingL, int recyclerViewPaddingR) {
        mRecyclerViewPaddingL = recyclerViewPaddingL;
        mRecyclerViewPaddingR = recyclerViewPaddingR;
    }
}
