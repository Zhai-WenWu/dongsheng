package amodule.main.view.item;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.home.delegate.IDataSetDelegate;
import amodule.main.activity.MainHomePage;

/**
 * 大图样式
 * Created by sll on 2017/4/18.
 */

public class HomeRecipeItem extends HomeItem {

    private IDataSetDelegate<Map<String, String>> mDataSetDelegate;

    protected TextView mTitle;
    protected TextView mTitleTop;
    protected TextView mVideoTime;
    protected ImageView mImg;
    protected ImageView mVIP;
    protected ImageView mSole;
    protected ImageView mPlayImg;
    protected LinearLayout mRecommendTag;
    protected RelativeLayout mVideoContainer;
    protected RelativeLayout mContainer;
    protected View mRecommendLine;
    protected View mLayerView;

    protected boolean mIsVideo;
    protected boolean mIsVip;

    protected int mVideoParamsH = (ToolsDevice.getWindowPx(getContext()).widthPixels - getResources().getDimensionPixelSize(R.dimen.dp_40)) * 9 / 16;

    public HomeRecipeItem(Context context) {
        this(context, null);
    }

    public HomeRecipeItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeRecipeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_recipeitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title_recipe);
        mTitleTop = (TextView) findViewById(R.id.title_top);
        mVideoTime = (TextView) findViewById(R.id.video_time);
        mVIP = (ImageView) findViewById(R.id.vip);
        mImg = (ImageView) findViewById(R.id.img);
        mSole = (ImageView) findViewById(R.id.img_sole);
        mPlayImg = (ImageView) findViewById(R.id.play_img);
        mRecommendTag = (LinearLayout) findViewById(R.id.recommend_tag);
        mVideoContainer = (RelativeLayout) findViewById(R.id.video_container);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mRecommendLine = findViewById(R.id.recommend_line);
        mLayerView = findViewById(R.id.layer_view);

    }

    public boolean getIsVip() {
        return mIsVip;
    }

    public boolean getIsVideo() {
        return mIsVideo;
    }

    public String getType() {
        return mType;
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataSetDelegate != null) {
            mDataSetDelegate.onSetData(dataMap);
            return;
        }
        if (mDataMap == null)
            return;
        if (mIsAd) {
            if (mLayerView != null)
                mLayerView.setVisibility(View.VISIBLE);
        }
        if (mDataMap.containsKey("video")) {
            String video = mDataMap.get("video");
            if (!TextUtils.isEmpty(video)) {
                Map<String, String> videoMap = StringManager.getFirstMap(video);
                String videoTime = videoMap.get("videoTime");
                if (!TextUtils.isEmpty(videoTime) && !"00:00".equals(videoTime) && mVideoTime != null) {
                    mVideoTime.setText(videoTime);
                    mVideoTime.setVisibility(View.VISIBLE);
                }
                String videoUrl = videoMap.get("videoUrl");
                if (!TextUtils.isEmpty(videoUrl)) {
                    Map<String, String> videoUrlMap = StringManager.getFirstMap(videoUrl);
                    String defUrl = videoUrlMap.get("defaultUrl");
                    if (!TextUtils.isEmpty(defUrl)) {
                        mIsVideo = true;
                    }
                }
                if (mVideoContainer != null)
                    mVideoContainer.setVisibility(View.VISIBLE);
            }
        }
        LayoutParams containerParams = (LayoutParams) mContainer.getLayoutParams();
        if (mIsVideo) {
            containerParams.height = mVideoParamsH;
        }
        String type = null;
        if (mModuleBean != null)
            type = mModuleBean.getType();
        containerParams.topMargin = getResources().getDimensionPixelSize(MainHomePage.recommedType.equals(type) ? R.dimen.dp_6 : R.dimen.dp_15);
        if (!TextUtils.isEmpty(type)) {
            switch (type) {
                case "day":
                    if (mPosition == 0)
                        containerParams.topMargin = 0;
                    if (!TextUtils.isEmpty(mDataMap.get("pastRecommed"))) {
                        mLineTop.setVisibility(View.GONE);
                        mRecommendLine.setVisibility(View.VISIBLE);
                        mRecommendTag.setVisibility(View.VISIBLE);
                    }
                    break;
                case "video":
                    if (mPosition == 0)
                        containerParams.topMargin = 0;
                    break;
            }
        }

        mSole.setVisibility("2".equals(mDataMap.get("isSole")) ? View.VISIBLE : View.GONE);

        if (!mIsAd && "2".equals(mDataMap.get("isVip"))) {
            mIsVip = true;
            mVIP.setVisibility(View.VISIBLE);
        }
        Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
        if (imgMap.size() > 0) {
            String imgUrl = imgMap.get("url");
            if (!TextUtils.isEmpty(imgUrl)) {
                mContainer.setVisibility(View.VISIBLE);
                if (mIsAd) {
                    int[] size = new int[2];
                    getADImgSize(size, mDataMap.get("style"));
                    if (size[0] > 0 && size[1] > 0) {
                        containerParams.width = size[0];
                        containerParams.height = size[1];
                    }
                    mLayerView.setVisibility(View.VISIBLE);
                }
                loadImage(imgUrl, mImg);
            }
        }
        mContainer.setLayoutParams(containerParams);
        mPlayImg.setVisibility(mIsVideo ? View.VISIBLE : View.GONE);
        String title = mIsAd ? mDataMap.get("content") : mDataMap.get("name");
        if (MainHomePage.recommedType.equals(type)) {
            mTitleTop.setText(title);
            mTitleTop.setVisibility(!TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
        } else {
            mTitle.setText(title);
            mTitle.setVisibility(!TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void resetData() {
        super.resetData();
        if (mDataSetDelegate != null) {
            mDataSetDelegate.onResetData();
            return;
        }
        mIsVideo = false;
        mIsVip = false;
        MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
        containerParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        containerParams.width = MarginLayoutParams.MATCH_PARENT;
        mContainer.setLayoutParams(containerParams);
    }

    @Override
    protected void resetView() {
        super.resetView();
        mVideoTime.setVisibility(View.GONE);
        mImg.setVisibility(View.GONE);
        mVIP.setVisibility(View.GONE);
        mRecommendTag.setVisibility(View.GONE);
        mRecommendLine.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.GONE);
        mLayerView.setVisibility(View.GONE);
        mContainer.setVisibility(View.GONE);
    }

    public void setDataDelegate(IDataSetDelegate<Map<String, String>> delegate) {
        mDataSetDelegate = delegate;
    }
}
