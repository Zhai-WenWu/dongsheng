package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.Tools.ImageUtility;

/**
 * 任意图 样式：限宽不限高
 * Created by sll on 2017/6/23.
 */

public class HomeAnyImgStyleItem extends HomeItem {

    private TextView mTitleTop;
    private ImageView mImg;
    private ImageView mVIP;
    private ImageView mADTag;
    private RelativeLayout mContainer;
    private View mLayerView;

    public HomeAnyImgStyleItem(Context context) {
        this(context, null);
    }

    public HomeAnyImgStyleItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeAnyImgStyleItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_anyimg_style_item);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitleTop = (TextView) findViewById(R.id.title_top);
        mVIP = (ImageView) findViewById(R.id.vip);
        mImg = (ImageView) findViewById(R.id.img);
        mADTag = (ImageView) findViewById(R.id.ad_tag);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mLayerView = findViewById(R.id.layer_view);
        addListener();
    }

    private void addListener() {
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAd) {
                    if (v == mADTag) {
                        onAdHintClick();
                    } else if (v == HomeAnyImgStyleItem.this) {
                        if (mAdControlParent != null) {
                            mAdControlParent.onAdClick(mDataMap);
                        }
                    }
                } else {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mTransferUrl, true);
                    onItemClick();
                }
            }
        };
        this.setOnClickListener(clickListener);
        if (mADTag != null)
            mADTag.setOnClickListener(clickListener);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        boolean hasTitleTop = false;
        String titleTop = mDataMap.get("name");
        if (!TextUtils.isEmpty(titleTop)) {
            mTitleTop.setText(titleTop);
            mTitleTop.setVisibility(View.VISIBLE);
            hasTitleTop = true;
        }
        if (mIsAd && mADTag != null)
            mADTag.setVisibility(View.VISIBLE);
        if (mModuleBean != null && !mIsAd && mVIP != null && "2".equals(mDataMap.get("isVip"))) {
            mVIP.setVisibility(View.VISIBLE);
        }
        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
        if (datas != null && datas.size() > 0) {
            Map<String, String> imgMap = datas.get(0);
            if (imgMap != null && imgMap.size() > 0) {
                String imgUrl = imgMap.get("url");
                if (mIsAd) {
                    loadImage(imgUrl, mImg, new ADImageLoadCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            if (bitmap == null)
                                return;
                            int bitmapWidth = bitmap.getWidth();
                            int bitmapHeight = bitmap.getHeight();
                            int imgWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - getContext().getResources().getDimensionPixelSize(R.dimen.dp_40);
                            int imgHeight = bitmapHeight * imgWidth / bitmapWidth;
                            mImg.setScaleType(ImageView.ScaleType.FIT_XY);
                            mImg.setImageBitmap(bitmap);
                            if (mContainer != null) {
                                MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
                                containerParams.height = imgHeight;
                                mContainer.setLayoutParams(containerParams);
                            }
                            MarginLayoutParams adImgParams = (MarginLayoutParams) mImg.getLayoutParams();
                            adImgParams.height = imgHeight;
                            mImg.setLayoutParams(adImgParams);

                            if (mLayerView != null) {
                                MarginLayoutParams layerParams = (MarginLayoutParams) mLayerView.getLayoutParams();
                                layerParams.height = imgHeight;
                                mLayerView.setLayoutParams(layerParams);
                                mLayerView.setVisibility(View.VISIBLE);
                            }
                            if (mContainer != null) {
                                mContainer.requestLayout();
                                mContainer.invalidate();
                            }
                        }
                    });
                } else {
                    int[] size = new int[2];
                    ImageUtility.getInstance().getImageSizeByUrl(imgUrl, size);
                    if (size[0] > 0 && size[1] > 0) {
                        int fixedWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - getResources().getDimensionPixelSize(R.dimen.dp_40);
                        int newHeight = fixedWidth * size[1] / size[0];
                        RelativeLayout.LayoutParams params = (LayoutParams) mImg.getLayoutParams();
                        params.height = newHeight;
                        if (mContainer != null) {
                            MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
                            containerParams.topMargin = getResources().getDimensionPixelSize(hasTitleTop ? R.dimen.dp_6 : R.dimen.dp_15);
                            containerParams.height = newHeight;
                        }
                        requestLayout();
                        invalidate();
                    }
                    loadImage(imgUrl, mImg);
                }
            }
        }

    }

    @Override
    protected void resetData() {
        super.resetData();
        MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
        containerParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        mContainer.setLayoutParams(containerParams);
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitleTop))
            mTitleTop.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
        if (viewIsVisible(mVIP))
            mVIP.setVisibility(View.GONE);
        if (viewIsVisible(mADTag))
            mADTag.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
    }
}
