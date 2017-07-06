package amodule.main.view.item;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

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
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mLayerView = findViewById(R.id.layer_view);
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
        if (mModuleBean != null && !mIsAd && mVIP != null && "2".equals(mDataMap.get("isVip"))) {
            mVIP.setVisibility(View.VISIBLE);
        }
        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
        if (datas != null && datas.size() > 0) {
            Map<String, String> imgMap = datas.get(0);
            if (imgMap != null && imgMap.size() > 0) {
                String imgUrl = imgMap.get("url");
                if (mIsAd) {
                    if (mLayerView != null)
                        mLayerView.setVisibility(View.VISIBLE);
                    MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
                    int[] size = new int[2];
                    getADImgSize(size, mDataMap.get("style"));
                    if (size[0] > 0 && size[1] > 0) {
                        containerParams.width = size[0];
                        containerParams.height = size[1];
                    }
                    mContainer.requestLayout();
                    mContainer.invalidate();
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
                }
                loadImage(imgUrl, mImg);
            }
        }

    }

    @Override
    protected void resetData() {
        super.resetData();
        if (mContainer == null)
            return;
        MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
        containerParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        containerParams.width = MarginLayoutParams.MATCH_PARENT;
        mContainer.requestLayout();
        mContainer.invalidate();
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
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
    }
}
