package amodule.main.view.item;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

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
        String titleTop = mDataMap.get("name");
        boolean hasTitleTop = !TextUtils.isEmpty(titleTop);
        mTitleTop.setText(titleTop);
        mTitleTop.setVisibility(hasTitleTop ? View.VISIBLE : View.GONE);
        if (mModuleBean != null && !mIsAd && "2".equals(mDataMap.get("isVip"))) {
            mVIP.setVisibility(View.VISIBLE);
        }
        Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
        if (imgMap.size() > 0) {
            String imgUrl = imgMap.get("url");
            MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
            if (mIsAd) {
                mLayerView.setVisibility(View.VISIBLE);
                int[] size = new int[2];
                getADImgSize(size, mDataMap.get("style"));
                if (size[0] > 0 && size[1] > 0) {
                    containerParams.width = size[0];
                    containerParams.height = size[1];
                }
            } else {
                int[] size = new int[2];
                ImageUtility.getInstance().getImageSizeByUrl(imgUrl, size);
                if (size[0] > 0 && size[1] > 0) {
                    int fixedWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - getResources().getDimensionPixelSize(R.dimen.dp_40);
                    int newHeight = fixedWidth * size[1] / size[0];
                    RelativeLayout.LayoutParams params = (LayoutParams) mImg.getLayoutParams();
                    params.height = newHeight;
                    containerParams.height = newHeight;
                }
            }
            containerParams.topMargin = getResources().getDimensionPixelSize(hasTitleTop ? R.dimen.dp_6 : R.dimen.dp_15);
            mContainer.setLayoutParams(containerParams);
            loadImage(imgUrl, mImg);
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
        mContainer.setLayoutParams(containerParams);
    }

    @Override
    protected void resetView() {
        super.resetView();
        mImg.setVisibility(View.GONE);
        mVIP.setVisibility(View.GONE);
        mLayerView.setVisibility(View.GONE);
    }
}
