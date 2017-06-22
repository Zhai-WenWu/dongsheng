package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
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
import amodule.main.activity.MainHome;

/**
 * 贴子Item，只处理3图模式的。
 * Created by sll on 2017/4/18.
 */

public class HomePostItem extends HomeItem {

    private TextView mTitle;
    private TextView mNum1;
    private TextView mNum2;

    private ImageView mImg1;
    private ImageView mImg2;
    private ImageView mImg3;

    private RelativeLayout mPostContainer;
    private RelativeLayout mImgsContainerRight;
    private LinearLayout mImgsContainerCenter;
    private LinearLayout mUserContainer;
    private ImageView mAdTagCenter;
    private ImageView mAdTagRight;
    private ImageView mImgRight;

    private View mLayerView;
    private View mLayerView1;
    private View mLayerView2;
    private View mLayerView3;

    public HomePostItem(Context context) {
        super(context, R.layout.home_postitem);
    }

    public HomePostItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.home_postitem);
    }

    public HomePostItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_postitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title);
        mNum1 = (TextView) findViewById(R.id.num1);
        mNum2 = (TextView) findViewById(R.id.num2);
        mImg1 = (ImageView) findViewById(R.id.img1);
        mImg2 = (ImageView) findViewById(R.id.img2);
        mImg3 = (ImageView) findViewById(R.id.img3);
        mImgsContainerCenter = (LinearLayout) findViewById(R.id.imgs_container_center);
        mUserContainer = (LinearLayout) findViewById(R.id.user_container);
        mPostContainer = (RelativeLayout) findViewById(R.id.post_container);
        mImgsContainerRight = (RelativeLayout) findViewById(R.id.imgs_container_right);
        mAdTagCenter = (ImageView) findViewById(R.id.ad_tag_center);
        mAdTagRight = (ImageView) findViewById(R.id.ad_tag_right);
        mImgRight = (ImageView) findViewById(R.id.img_right);
        mLayerView = findViewById(R.id.layer_view);
        mLayerView1 = findViewById(R.id.layer_view1);
        mLayerView2 = findViewById(R.id.layer_view2);
        mLayerView3 = findViewById(R.id.layer_view3);
        addListener();
    }

    private void addListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAd) {
                    if (v == mAdTagCenter || v == mAdTagRight) {
                        onAdHintClick();
                    } else if (v == HomePostItem.this) {
                        if (mAdControlParent != null) {
                            mAdControlParent.onAdClick(mDataMap);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(mTransferUrl)) {
                        if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                            if(mTransferUrl.contains("?"))mTransferUrl+="&data_type="+mDataMap.get("type");
                            else mTransferUrl+="?data_type="+mDataMap.get("type");
                            mTransferUrl+="&module_type="+(isTopTypeView()?"top_info":"info");
                            Log.i("zhangyujian","点击："+mDataMap.get("code")+":::"+mTransferUrl);
                            XHClick.saveStatictisFile("home",getModleViewType(),mDataMap.get("type"),mDataMap.get("code"),"","click","","",String.valueOf(mPosition+1),"","");
                        }
                        AppCommon.openUrl((Activity) getContext(), mTransferUrl, false);
                    }
                    if (v == HomePostItem.this)
                        onItemClick();
                }
            }
        };
        this.setOnClickListener(clickListener);
        if (mAdTagRight != null)
            mAdTagRight.setOnClickListener(clickListener);
        if (mAdTagCenter != null)
            mAdTagCenter.setOnClickListener(clickListener);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mIsAd) {
            if (mAdControlParent != null && !mDataMap.containsKey("isADShow")) {
                mAdControlParent.onAdShow(mDataMap, this);
                mDataMap.put("isADShow", "1");
            }
        }
        int imgCount = 0;
        if (mDataMap.containsKey("styleData")) {
            String imgs = mDataMap.get("styleData");
            if (!TextUtils.isEmpty(imgs)) {
                ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(imgs);
                if (maps != null) {
                    imgCount = maps.size();
                    RelativeLayout.LayoutParams postContainerParams = (LayoutParams) mPostContainer.getLayoutParams();
                    if (imgCount == 0) {
                        mPostContainer.setMinimumHeight(0);
                        postContainerParams.height = LayoutParams.WRAP_CONTENT;
                        postContainerParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.dp_13);
                        postContainerParams.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.dp_13);
                    } else if (imgCount < 3) {
                        if (mPostContainer != null) {
                            mPostContainer.setMinimumHeight(getContext().getResources().getDimensionPixelSize(R.dimen.dp_74_5));
                            postContainerParams.height = LayoutParams.WRAP_CONTENT;
                            postContainerParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.dp_13);
                            postContainerParams.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.dp_15);
                            mPostContainer.setLayoutParams(postContainerParams);
                        }
                        if (mImgsContainerRight != null) {
                            mImgsContainerRight.setVisibility(View.VISIBLE);
                        }
                        loadImage(maps.get(0).get("url"), mImgRight);
                        if (mIsAd && mLayerView != null)
                            mLayerView.setVisibility(View.VISIBLE);
                        if (mIsAd && mAdTagRight != null && (!mDataMap.containsKey("adType") || !"1".equals(mDataMap.get("adType"))))
                            mAdTagRight.setVisibility(View.VISIBLE);
                    } else {
                        if (mPostContainer != null) {
                            mPostContainer.setMinimumHeight(0);
                            postContainerParams.height = LayoutParams.WRAP_CONTENT;
                            postContainerParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.dp_13);
                            postContainerParams.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.dp_13);
                            mPostContainer.setLayoutParams(postContainerParams);
                        }
                        if (mImgsContainerCenter != null)
                            mImgsContainerCenter.setVisibility(View.VISIBLE);
                        if (mIsAd) {
                            if (mLayerView1 != null)
                                mLayerView1.setVisibility(View.VISIBLE);
                            if (mLayerView2 != null)
                                mLayerView2.setVisibility(View.VISIBLE);
                            if (mLayerView3 != null)
                                mLayerView3.setVisibility(View.VISIBLE);
                        }
                        if (mIsAd && mAdTagCenter != null && (!mDataMap.containsKey("adType") || !"1".equals(mDataMap.get("adType"))))
                            mAdTagCenter.setVisibility(View.VISIBLE);
                        loadImage(maps.get(0).get("url"), mImg1);
                        loadImage(maps.get(1).get("url"), mImg2);
                        loadImage(maps.get(2).get("url"), mImg3);
                    }
                }
            }
        }
        String title = "";
        if (mIsAd) {
            if (mDataMap.containsKey("content")) {
                title = mDataMap.get("content");
            }
        } else if (mDataMap.containsKey("name")) {
            title = mDataMap.get("name");
        }
        if (!TextUtils.isEmpty(title) && mTitle != null) {
            LayoutParams titleParams = (LayoutParams) mTitle.getLayoutParams();
            titleParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_6);
            mTitle.setText(title);
            if (imgCount == 0) {
                titleParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_9);
                mTitle.setMaxLines(Integer.MAX_VALUE);
            } else if (imgCount >= 3) {
                mTitle.setMaxLines(2);
            } else {
                mTitle.setLines(2);
                mTitle.setMaxLines(Integer.MAX_VALUE);
                titleParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_5);
            }
            mTitle.setLayoutParams(titleParams);
            mTitle.setVisibility(View.VISIBLE);
        }

        switch (imgCount) {
            case 0://无图
                switch (mType) {
                    case "3":
                    case "5":
                        if (mAllClickNum != null && mNum1 != null) {
                            mNum1.setText(mAllClickNum + "浏览");
                            mNum1.setVisibility(View.VISIBLE);
                        }
                        if (mComNum != null && mNum2 != null) {
                            mNum2.setText(mComNum + "评论");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                break;
            case 1://右图
            case 2:
                switch (mType) {
                    case "1":
                        if (mAllClickNum != null && mNum1 != null) {
                            mNum1.setText(mAllClickNum + "浏览");
                            mNum1.setVisibility(View.VISIBLE);
                        }
                        if (!mIsTop) {
                            if (mFavNum != null && mNum2 != null) {
                                mNum2.setText(mFavNum + "收藏");
                                mNum2.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    case "2":
                        if (mAllClickNum != null && mNum1 != null) {
                            mNum1.setText(mAllClickNum + "播放");
                            mNum1.setVisibility(View.VISIBLE);
                        }
                        if (!mIsTop) {
                            if (mFavNum != null && mNum2 != null) {
                                mNum2.setText(mFavNum + "收藏");
                                mNum2.setVisibility(View.VISIBLE);
                            }
                        }

                        break;
                    case "3":
                    case "5":
                        if (mAllClickNum != null && mNum1 != null) {
                            mNum1.setText(mAllClickNum + "浏览");
                            mNum1.setVisibility(View.VISIBLE);
                        }
                        if (mComNum != null && mNum2 != null) {
                            mNum2.setText(mComNum + "评论");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                break;
            default://3图
                if (mAllClickNum != null && mNum1 != null) {
                    mNum1.setText(mAllClickNum + "浏览");
                    mNum1.setVisibility(View.VISIBLE);
                }
                if (mComNum != null && mNum2 != null) {
                    mNum2.setText(mComNum + "评论");
                    mNum2.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle)) {
            mTitle.setVisibility(View.GONE);
        }
        if (mTitle != null) {
            mTitle.setLines(1);
            mTitle.setMaxLines(2);
        }
        if (viewIsVisible(mNum1))
            mNum1.setVisibility(View.GONE);
        if (viewIsVisible(mNum2))
            mNum2.setVisibility(View.GONE);
        if (viewIsVisible(mImgsContainerRight))
            mImgsContainerRight.setVisibility(View.GONE);
        if (viewIsVisible(mImgsContainerCenter))
            mImgsContainerCenter.setVisibility(View.GONE);
        if (viewIsVisible(mAdTagCenter))
            mAdTagCenter.setVisibility(View.GONE);
        if (viewIsVisible(mAdTagRight))
            mAdTagRight.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView1))
            mLayerView1.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView2))
            mLayerView2.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView3))
            mLayerView3.setVisibility(View.GONE);
    }
}
