package amodule.main.view.item;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import amodule.main.activity.MainHome;
import amodule.main.adapter.AdapterListView;
import aplug.web.ShowWeb;

/**
 * 右图，无图，样式
 * Created by sll on 2017/4/18.
 */

public class HomeTxtItem extends HomeItem {

    private TextView mTitle;
    private TextView mNum1;
    private TextView mNum2;
    private ImageView mImg;
    private ImageView mAdTag;
    private RelativeLayout mImgs;
    private View mLayerView;

    public HomeTxtItem(Context context) {
        super(context, R.layout.home_txtitem);
    }

    public HomeTxtItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.home_txtitem);
    }

    public HomeTxtItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_txtitem);
    }

    @Override
    public void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title);
        mNum1 = (TextView) findViewById(R.id.num1);
        mNum2 = (TextView) findViewById(R.id.num2);
        mImg = (ImageView) findViewById(R.id.img);
        mAdTag = (ImageView) findViewById(R.id.ad_tag);
        mImgs = (RelativeLayout) findViewById(R.id.imgs);
        mLayerView = findViewById(R.id.layer_view);
        addListener();
    }

    private void addListener() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAd) {
                    if (v == mAdTag) {
                        onAdHintClick();
                    } else if (v == HomeTxtItem.this) {
                        if (mAdControlParent != null) {
                            mAdControlParent.onAdClick(mDataMap);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(mTransferUrl)) {
                        if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                            if(mTransferUrl.contains("?"))mTransferUrl+="&data_type="+mDataMap.get("type");
                            else mTransferUrl+="?data_type="+mDataMap.get("type");
                            Log.i("zhangyujian","点击："+mDataMap.get("code")+":::"+mTransferUrl);
                            XHClick.saveStatictisFile("home",getModleViewType(),mDataMap.get("type"),mDataMap.get("code"),"","click","","",String.valueOf(mPosition+1),"","");
                        }
//                        AppCommon.openUrl((Activity) getContext(), mTransferUrl, false);
                        if(mTransferUrl.contains("nousInfo.app")){
                           String params= mTransferUrl.substring(mTransferUrl.indexOf("?")+1,mTransferUrl.length());
                            Log.i("zhangyujian","mTransferUrl:::"+params);
                            Map<String,String> map = StringManager.getMapByString(params,"&","=");
                            Intent intent = new Intent(XHActivityManager.getInstance().getCurrentActivity(), ShowWeb.class);
                            intent.putExtra("url",StringManager.api_nouseInfo + map.get("code"));
                            intent.putExtra("data_type",map.get("data_type"));
                            intent.putExtra("code",map.get("code"));
                            intent.putExtra("module_type",isTopTypeView()?"top_info":"info");
                            XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
                        }else{
                            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),mTransferUrl,true);
                        }
                    }
                    if (v == HomeTxtItem.this)
                        onItemClick();
                }
            }
        };
        this.setOnClickListener(onClickListener);
        if (mAdTag != null)
            mAdTag.setOnClickListener(onClickListener);
    }


    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mIsAd) {
            if (mLayerView != null)
                mLayerView.setVisibility(View.VISIBLE);
            if (mDataMap.containsKey("content")) {
                String desc = mDataMap.get("content");
                if (!TextUtils.isEmpty(desc) && mTitle != null) {
                    mTitle.setText(desc);
                    mTitle.setVisibility(View.VISIBLE);
                }
            }
            if (mAdTag != null && (!mDataMap.containsKey("adType") || !"1".equals(mDataMap.get("adType"))))
                mAdTag.setVisibility(View.VISIBLE);
            if (mAdControlParent != null && !mDataMap.containsKey("isADShow")) {
                mAdControlParent.onAdShow(mDataMap, this);
                mDataMap.put("isADShow", "1");
            }
        }
        int imgCount = 0;
        if(mDataMap.containsKey("style")&& String.valueOf(AdapterListView.type_rightImage).equals(mDataMap.get("style"))){//右图模式
            if (mDataMap.containsKey("styleData")) {
                ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
                if (datas != null && datas.size() > 0) {
                    Map<String, String> imgMap = datas.get(0);
                    if (imgMap != null && imgMap.size() > 0) {
                        String imgUrl = imgMap.get("url");
                        imgCount = 1;
                        if (mImgs != null)
                            mImgs.setVisibility(View.VISIBLE);
                        loadImage(imgUrl, mImg);
                    }
                }
            }
        }else{
            if (mImgs != null)mImgs.setVisibility(View.GONE);
        }
        if (mDataMap.containsKey("name") && !mIsAd) {
            String name = mDataMap.get("name");
            if (!TextUtils.isEmpty(name) && mTitle != null) {
                mTitle.setText(name);
                mTitle.setVisibility(View.VISIBLE);
            }
        }
        if (imgCount == 1) {
            mTitle.setLines(2);
            mTitle.setMaxLines(Integer.MAX_VALUE);
        }
        switch (imgCount) {
            case 0://无图
                if (mComNum != null && mNum1 != null) {
                    mNum1.setText(mComNum + "评论");
                    mNum1.setVisibility(View.VISIBLE);
                }
                switch (mType) {
                    case "3":
                        if (mAllClickNum != null && mNum2 != null) {
                            mNum2.setText(mAllClickNum + "浏览");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "5":
                        if (mLikeNum != null && mNum2 != null) {
                            mNum2.setText(mLikeNum + "赞");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                break;
            case 1://右图
                switch (mType) {
                    case "1":
                        if (!mIsTop) {
                            if (mFavNum != null && mNum1 != null) {
                                mNum1.setText(mFavNum + "收藏");
                                mNum1.setVisibility(View.VISIBLE);
                            }
                        }
                        if (mAllClickNum != null && mNum2 != null) {
                            mNum2.setText(mAllClickNum + "浏览");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "2":
                        if (!mIsTop) {
                            if (mFavNum != null && mNum1 != null) {
                                mNum1.setText(mFavNum + "收藏");
                                mNum1.setVisibility(View.VISIBLE);
                            }
                        }
                        if (mAllClickNum != null && mNum2 != null) {
                            mNum2.setText(mAllClickNum + "播放");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "3":
                        if (mComNum != null && mNum1 != null) {
                            mNum1.setText(mComNum + "评论");
                            mNum1.setVisibility(View.VISIBLE);
                        }
                        if (mAllClickNum != null && mNum2 != null) {
                            mNum2.setText(mAllClickNum + "浏览");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "5":
                        if (mComNum != null && mNum1 != null) {
                            mNum1.setText(mComNum + "评论");
                            mNum1.setVisibility(View.VISIBLE);
                        }
                        if (mLikeNum != null && mNum2 != null) {
                            mNum2.setText(mLikeNum + "赞");
                            mNum2.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                break;
        }
    }

    @Override
    protected void resetData() {
        super.resetData();
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (mTitle != null) {
            mTitle.setLines(1);
            mTitle.setMaxLines(2);
        }
        if (viewIsVisible(mNum1))
            mNum1.setVisibility(View.GONE);
        if (viewIsVisible(mNum2))
            mNum2.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
        if (viewIsVisible(mAdTag))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mImgs))
            mImgs.setVisibility(View.GONE);
        if (viewIsVisible(mAdTag))
            mAdTag.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
    }
}
