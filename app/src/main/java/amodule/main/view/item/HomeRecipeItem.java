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

import java.util.ArrayList;
import java.util.Map;

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
    private TextView mTitleTop;
    private TextView mVideoTime;
    private ImageView mImg;
    private ImageView mVIP;
    private ImageView mSole;
    private ImageView mPlayImg;
    private LinearLayout mRecommendTag;
    private RelativeLayout mVideoContainer;
    private RelativeLayout mContainer;
    private View mRecommendLine;
    private View mLayerView;

    private boolean mIsVideo;
    private boolean mIsVip;

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
        mTitle = (TextView) findViewById(R.id.title);
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
        addListener();
    }

    private void addListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //视频列表下点击视频图片部分直接播放，其他部分则跳转到视频菜谱详情页
                if (v == mVideoContainer && !TextUtils.isEmpty(mType) && mType.equals("2") && mIsVideo && mModuleBean != null && "video".equals(mModuleBean.getType()) && !mIsVip) { //表示当前tab是视频菜谱
                    if (mVideoClickCallBack != null) {
                        mVideoClickCallBack.videoOnClick(mPosition);
                        XHClick.mapStat(getContext(), "a_video", "进入详情/列表播放", "点击视频直接播放");
                    }
                } else {
                    XHClick.mapStat(getContext(), "a_video", "进入详情/列表播放", "点击文字信息进入详情");
                    HomeRecipeItem.super.onClick(v);
                }
            }
        };
        if (mVideoContainer != null)
            mVideoContainer.setOnClickListener(clickListener);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mIsAd) {
            if (mLayerView != null)
                mLayerView.setVisibility(View.VISIBLE);
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
        LayoutParams containerParams = (LayoutParams) mContainer.getLayoutParams();
        if (mIsVideo) {
            int fixedH = 9, fixedW = 16;
            int w = ToolsDevice.getWindowPx(getContext()).widthPixels - getResources().getDimensionPixelSize(R.dimen.dp_40);
            int h = w * fixedH / fixedW;
            containerParams.height = h;
        }
        String type = null;
        if (mModuleBean != null)
            type = mModuleBean.getType();
        containerParams.topMargin = getResources().getDimensionPixelSize(MainHome.recommedType.equals(type) ? R.dimen.dp_6 : R.dimen.dp_15);
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
        mContainer.setLayoutParams(containerParams);

        if (mDataMap.containsKey("isSole")) {
            String isSole = mDataMap.get("isSole");
            if (!TextUtils.isEmpty(isSole) && "2".equals(isSole) && mSole != null)
                mSole.setVisibility(View.VISIBLE);
        }
        if (mVIP != null && !mIsAd && "2".equals(mDataMap.get("isVip"))) {
            mIsVip = true;
            mVIP.setVisibility(View.VISIBLE);
        }
        if (mDataMap.containsKey("styleData")) {
            ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
            if (datas != null && datas.size() > 0) {
                Map<String, String> imgMap = datas.get(0);
                if (imgMap != null && imgMap.size() > 0) {
                    String imgUrl = imgMap.get("url");
                    if (!TextUtils.isEmpty(imgUrl)) {
                        if (mContainer != null)
                            mContainer.setVisibility(View.VISIBLE);
                        if (mIsAd) {
                            int[] size = new int[2];
                            getADImgSize(size, mDataMap.get("style"));
                            if (size[0] > 0 && size[1] > 0) {
                                containerParams.width = size[0];
                                containerParams.height = size[1];
                            }
                            mContainer.requestLayout();
                            mContainer.invalidate();
                        }
                        loadImage(imgUrl, mImg);
                    }
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
        if (MainHome.recommedType.equals(type)) {
            if (!TextUtils.isEmpty(titleText) && mTitleTop != null) {

                mTitleTop.setText(titleText);
                mTitleTop.setVisibility(View.VISIBLE);
            }
        } else if (!TextUtils.isEmpty(titleText) && mTitle != null) {
            mTitle.setText(titleText);
            mTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void resetData() {
        super.resetData();
        mIsVideo = false;
        mIsVip = false;
        MarginLayoutParams containerParams = (MarginLayoutParams) mContainer.getLayoutParams();
        containerParams.height = getResources().getDimensionPixelSize(R.dimen.dp_190);
        containerParams.width = MarginLayoutParams.MATCH_PARENT;
        mContainer.setLayoutParams(containerParams);
        mContainer.requestLayout();
        mContainer.invalidate();
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitleTop))
            mTitleTop.setVisibility(View.GONE);
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mVideoTime))
            mVideoTime.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
        if (viewIsVisible(mVIP))
            mVIP.setVisibility(View.GONE);
        if (viewIsVisible(mSole))
            mSole.setVisibility(View.GONE);
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
        if (viewIsVisible(mContainer))
            mContainer.setVisibility(View.GONE);
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
