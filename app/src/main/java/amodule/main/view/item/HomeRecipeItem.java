package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.activity.MainHome;

/**
 * 大图样式
 * Created by sll on 2017/4/18.
 */

public class HomeRecipeItem extends HomeItem {

    private TextView mTitle;
    private TextView mVideoTime;
    private TextView mNum1;
    private TextView mNum2;
    private ImageView mImg;
    private ImageView mSole;
    private ImageView mAdTag;
    private ImageView mPlayImg;
    private LinearLayout mRecommendTag;
    private RelativeLayout mVideoContainer;
    private RelativeLayout mContainer;
    private View mRecommendLine;
    private View mLayerView;

    private boolean mIsVideo;

    public HomeRecipeItem(Context context) {
        super(context, R.layout.home_recipeitem);
    }

    public HomeRecipeItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.home_recipeitem);
    }

    public HomeRecipeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_recipeitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title);
        mNum1 = (TextView) findViewById(R.id.num1);
        mNum2 = (TextView) findViewById(R.id.num2);
        mVideoTime = (TextView) findViewById(R.id.video_time);
        mImg = (ImageView) findViewById(R.id.img);
        mSole = (ImageView) findViewById(R.id.img_sole);
        mAdTag = (ImageView) findViewById(R.id.ad_tag);
        mPlayImg = (ImageView) findViewById(R.id.play_img);
        mRecommendTag = (LinearLayout) findViewById(R.id.recommend_tag);
        mVideoContainer = (RelativeLayout) findViewById(R.id.video_container);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mRecommendLine = findViewById(R.id.recommend_line);
        mLayerView = findViewById(R.id.layer_view);
        addListener();
    }

    private void addListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAd) {
                    if (v == mAdTag) {
                        onAdHintClick();
                    } else if (v == HomeRecipeItem.this) {
                        if (mAdControlParent != null) {
                            mAdControlParent.onAdClick(mDataMap);
                        }
                    }
                } else {
                    //视频列表下点击视频图片部分直接播放，其他部分则跳转到视频菜谱详情页
                    if (v == mVideoContainer && !TextUtils.isEmpty(mType) && mType.equals("2") && mIsVideo && mModuleBean != null && "video".equals(mModuleBean.getType())) { //表示当前tab是视频菜谱
                        if (mVideoClickCallBack != null) {
                            mVideoClickCallBack.videoOnClick(mPosition);
                            XHClick.mapStat((Activity)getContext(), "a_video", "进入详情/列表播放", "点击视频直接播放");
                        }
                    } else if (!TextUtils.isEmpty(mTransferUrl)) { //非视频菜谱tab的，其他tab下的图文菜谱和视频菜谱处理
                        if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                            if(mTransferUrl.contains("?"))mTransferUrl+="&data_type="+mDataMap.get("type");
                            else mTransferUrl+="?data_type="+mDataMap.get("type");
                            mTransferUrl+="&module_type="+(isTopTypeView()?"top_info":"info");
                            Log.i("zhangyujian","点击："+mDataMap.get("code")+":::"+mTransferUrl);
                            XHClick.saveStatictisFile("home",getModleViewType(),mDataMap.get("type"),mDataMap.get("code"),"","click","","",String.valueOf(mPosition+1),"","");
                        }
                        AppCommon.openUrl((Activity) getContext(), mTransferUrl, false);
                        XHClick.mapStat((Activity)getContext(), "a_video", "进入详情/列表播放", "点击文字信息进入详情");
                    }
                    onItemClick();
                }
            }
        };
        this.setOnClickListener(clickListener);
        if (mVideoContainer != null)
            mVideoContainer.setOnClickListener(clickListener);
        if (mAdTag != null)
            mAdTag.setOnClickListener(clickListener);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mIsAd) {
            if (mLayerView != null)
                mLayerView.setVisibility(View.VISIBLE);
            if (mAdTag != null && (!mDataMap.containsKey("adType") || !"1".equals(mDataMap.get("adType"))))
                mAdTag.setVisibility(View.VISIBLE);
            if (mAdControlParent != null && !mDataMap.containsKey("isADShow")) {
                mAdControlParent.onAdShow(mDataMap, this);
                mDataMap.put("isADShow", "1");
            }
        }
        LayoutParams containerParams = (LayoutParams) mContainer.getLayoutParams();
        containerParams.topMargin = getResources().getDimensionPixelSize(R.dimen.dp_7);
        if (mModuleBean != null) {
            String type = mModuleBean.getType();
            if (!TextUtils.isEmpty(type)) {
                if ("day".equals(type)) {
                    if (mPosition == 0)
                        containerParams.topMargin = 0;
                    if (mDataMap.containsKey("pastRecommed") && !TextUtils.isEmpty(mDataMap.get("pastRecommed"))) {
                        if (mLineTop != null)
                            mLineTop.setVisibility(View.GONE);
                        if (mRecommendLine != null)
                            mRecommendLine.setVisibility(View.VISIBLE);
                        if (mRecommendTag != null)
                            mRecommendTag.setVisibility(View.VISIBLE);
                    }
                } else if ("video".equals(type) && mPosition == 0)
                    containerParams.topMargin = 0;

            }
        }
        mContainer.setLayoutParams(containerParams);

        if (mDataMap.containsKey("isSole")) {
            String isSole = mDataMap.get("isSole");
            if (!TextUtils.isEmpty(isSole) && "2".equals(isSole) && mSole != null)
                mSole.setVisibility(View.VISIBLE);
        }
        if (mDataMap.containsKey("video")) {
            String video = mDataMap.get("video");
            if (!TextUtils.isEmpty(video)) {
                ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(video);
                for (Map<String, String> map : maps) {
                    if (map != null) {
                        if (map.containsKey("videoTime")) {
                            String videoTime = map.get("videoTime");
                            if (!TextUtils.isEmpty(videoTime) && !"00:00".equals(videoTime) && mVideoTime != null) {
                                mVideoTime.setText(videoTime);
                                mVideoTime.setVisibility(View.VISIBLE);
                            }
                        }
                        if (map.containsKey("isVideo")) {
                            String isVideo = map.get("isVideo");
                            if (!TextUtils.isEmpty(isVideo) && isVideo.equals("2"))
                                mIsVideo = true;
                        }
                    }
                }
                if (mVideoContainer != null)
                    mVideoContainer.setVisibility(View.VISIBLE);
            }
        }

        if (mDataMap.containsKey("styleData")) {
            ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
            if (datas != null && datas.size() > 0) {
                Map<String, String> imgMap = datas.get(0);
                if (imgMap != null && imgMap.size() > 0) {
                    String imgUrl = imgMap.get("url");
                    loadImage(imgUrl, mImg, mIsAd ? new ADImageLoadCallback() {
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
                                containerParams.width = MarginLayoutParams.MATCH_PARENT;
                                containerParams.height = imgHeight;
                                mContainer.setLayoutParams(containerParams);
                            }
                            MarginLayoutParams adImgParams = (MarginLayoutParams) mImg.getLayoutParams();
                            adImgParams.height = imgHeight;
                            mImg.setLayoutParams(adImgParams);
                            mLayerView.requestLayout();
                            mLayerView.requestLayout();
                            if (mLayerView != null) {
                                MarginLayoutParams layerParams = (MarginLayoutParams) mLayerView.getLayoutParams();
                                layerParams.width = MarginLayoutParams.MATCH_PARENT;
                                layerParams.height = imgHeight;
                                mLayerView.setLayoutParams(layerParams);
                            }
                        }
                    } : null);
                }
            }
        }
        if (mIsVideo && mPlayImg != null)
            mPlayImg.setVisibility(View.VISIBLE);
        String desc = "";
        if (mIsAd && mDataMap.containsKey("content")) {
            desc = mDataMap.get("content");
        }
        String title = "";
        if (mDataMap.containsKey("name")) {
            title = mDataMap.get("name");
        }
        String lineText = (TextUtils.isEmpty(desc) || TextUtils.isEmpty(title) ? "" : " | ");
        String titleText = title + lineText + desc;
        if (!TextUtils.isEmpty(titleText) && mTitle != null) {
            mTitle.setText(titleText);
            mTitle.setVisibility(View.VISIBLE);
        }
        if (mDataMap.containsKey("allClick")) {
            String allClick = handleNumber(mDataMap.get("allClick"));
            if (!TextUtils.isEmpty(allClick) && mNum1 != null) {
                mNum1.setText(allClick + (mIsVideo ? "播放" : "浏览"));
                mNum1.setVisibility(View.VISIBLE);
            }
        }
        if (mDataMap.containsKey("favorites")) {
            String likeNum = handleNumber(mDataMap.get("favorites"));
            if (!TextUtils.isEmpty(likeNum) && mNum2 != null) {
                mNum2.setText(likeNum + "收藏");
                mNum2.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void resetData() {
        super.resetData();
        mIsVideo = false;
        MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
        containerParams.width = MarginLayoutParams.MATCH_PARENT;
        containerParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        mContainer.setLayoutParams(containerParams);
        MarginLayoutParams layerParams = (MarginLayoutParams) mLayerView.getLayoutParams();
        layerParams.width = MarginLayoutParams.MATCH_PARENT;
        layerParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        mLayerView.setLayoutParams(layerParams);
        MarginLayoutParams adImgParams = (MarginLayoutParams) mImg.getLayoutParams();
        adImgParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        mImg.setLayoutParams(adImgParams);
        mLayerView.requestLayout();
        mLayerView.requestLayout();
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mVideoTime))
            mVideoTime.setVisibility(View.GONE);
        if (viewIsVisible(mNum1))
            mNum1.setVisibility(View.GONE);
        if (viewIsVisible(mNum2))
            mNum2.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
        if (viewIsVisible(mSole))
            mSole.setVisibility(View.GONE);
        if (viewIsVisible(mAdTag))
            mAdTag.setVisibility(View.GONE);
        if (viewIsVisible(mPlayImg))
            mPlayImg.setVisibility(View.GONE);
        if (viewIsVisible(mRecommendTag))
            mRecommendTag.setVisibility(View.GONE);
        if (viewIsVisible(mRecommendLine))
            mRecommendLine.setVisibility(View.GONE);
        if (viewIsVisible(mVideoContainer))
            mVideoContainer.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
    }

    /**
     * 视频view点击回调
     */
    public interface VideoClickCallBack {
        public void videoOnClick(int position);
    }

    private VideoClickCallBack mVideoClickCallBack;

    public void setVideoClickCallBack(VideoClickCallBack clickCallBack) {
        mVideoClickCallBack = clickCallBack;
    }
}
