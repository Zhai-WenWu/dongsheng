package amodule.home.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import amodule._common.utility.WidgetUtility;
import amodule.home.delegate.IDataSetDelegate;
import amodule.main.activity.MainHome;
import amodule.main.view.item.HomeRecipeItem;

/**
 * 首页二级页面菜谱（大图样式）Item（视频、三餐、佳作）
 * Created by sll on 2017/11/17.
 */

public class HomeSecondRecipeItem extends HomeRecipeItem implements IDataSetDelegate<Map<String, String>> {
    public HomeSecondRecipeItem(Context context) {
        this(context, null);
    }

    public HomeSecondRecipeItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeSecondRecipeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDataDelegate(this);
    }

    @Override
    public void onSetData(Map<String, String> map) {
        if (mDataMap == null)
            return;
        LayoutParams containerParams = (LayoutParams) mContainer.getLayoutParams();
        containerParams.topMargin = (mPosition == 0 && mModuleBean != null && (TextUtils.equals("day", mModuleBean.getType()) || TextUtils.equals("video", mModuleBean.getType()))) ? 0 : getResources().getDimensionPixelSize(R.dimen.dp_15);
        Map<String, String> videoMap = StringManager.getFirstMap(mDataMap.get("video"));
        String videoTime = videoMap.get("videoTime");
        if (!TextUtils.isEmpty(videoTime) && !"00:00".equals(videoTime) && mVideoTime != null) {
            WidgetUtility.setTextToView(mVideoTime, videoTime);
        }
        Map<String, String> videoUrlMap = StringManager.getFirstMap(videoMap.get("videoUrl"));
        String defUrl = videoUrlMap.get("defaultUrl");
        if (!TextUtils.isEmpty(defUrl)) {
            mIsVideo = true;
        }
        mVideoContainer.setVisibility(View.VISIBLE);

        if (mModuleBean != null && TextUtils.equals("day", mModuleBean.getType()) && !TextUtils.isEmpty(mDataMap.get("pastRecommed"))) {
            mLineTop.setVisibility(View.GONE);
            mRecommendLine.setVisibility(View.VISIBLE);
            mRecommendTag.setVisibility(View.VISIBLE);
        }
        mSole.setVisibility("2".equals(mDataMap.get("isSole")) ? View.VISIBLE : View.GONE);
        mIsVip = "2".equals(mDataMap.get("isVip"));
        mVIP.setVisibility(mIsVip ? View.VISIBLE : View.GONE);
        Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
        String imgUrl = imgMap.get("url");
        mContainer.setVisibility(View.VISIBLE);
        loadImage(imgUrl, mImg);
        mPlayImg.setVisibility(mIsVideo ? View.VISIBLE : View.GONE);
        String title = mDataMap.get("name");
        String type = mModuleBean == null ? null : mModuleBean.getType();
        if (MainHome.recommedType.equals(type)) {
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
