package amodule.home.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.Map;

import acore.tools.StringManager;
import amodule.home.delegate.IDataSetDelegate;
import amodule.main.activity.MainHomePage;
import amodule.main.view.item.HomeRecipeItem;

/**
 * 首页二级页面广告item（视频、三餐、佳作）
 * Created by sll on 2017/11/17.
 */

public class HomeSecondADItem extends HomeRecipeItem implements IDataSetDelegate<Map<String, String>> {
    public HomeSecondADItem(Context context) {
        super(context);
    }

    public HomeSecondADItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeSecondADItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onSetData(Map<String, String> map) {
        if (mDataMap == null || !mIsAd)
            return;
        mLayerView.setVisibility(View.VISIBLE);
        LayoutParams containerParams = (LayoutParams) mContainer.getLayoutParams();
        String type = null;
        if (mModuleBean != null)
            type = mModuleBean.getType();
        if (TextUtils.equals("day", type) && !TextUtils.isEmpty(mDataMap.get("pastRecommed"))) {
            mLineTop.setVisibility(View.GONE);
            mRecommendLine.setVisibility(View.VISIBLE);
            mRecommendTag.setVisibility(View.VISIBLE);
        }

        mSole.setVisibility("2".equals(mDataMap.get("isSole")) ? View.VISIBLE : View.GONE);

        String imgUrl = StringManager.getFirstMap(mDataMap.get("styleData")).get("url");
        if (!TextUtils.isEmpty(imgUrl)) {
            mContainer.setVisibility(View.VISIBLE);
            int[] size = new int[2];
            getADImgSize(size, mDataMap.get("style"));
            if (size[0] > 0 && size[1] > 0) {
                containerParams.width = size[0];
                containerParams.height = size[1];
            }
            mLayerView.setVisibility(View.VISIBLE);
            loadImage(imgUrl, mImg);
        }
        mContainer.setLayoutParams(containerParams);
        String title = mDataMap.get("content");
        if (MainHomePage.recommedType.equals(type)) {
            mTitleTop.setText(title);
            mTitleTop.setVisibility(!TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
        } else {
            mTitle.setText(title);
            mTitle.setVisibility(!TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResetData() {
        mIsVideo = false;
        mIsVip = false;
    }
}
