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
import amodule.main.activity.MainHome;
import amodule.main.adapter.AdapterListView;

/**
 * 右图，无图，样式, 带有标题，描述等信息
 * Created by sll on 2017/4/18.
 */

public class HomeTxtItem extends HomeItem {

    private TextView mTitle;
    private TextView mDesc;
    private ImageView mImg;
    private ImageView mVIP;
    private RelativeLayout mImgs;
    private RelativeLayout mContainer;
    private View mLayerView;

    public HomeTxtItem(Context context) {
        this(context, null);
    }

    public HomeTxtItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeTxtItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_txtitem);
    }

    @Override
    public void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title);
        mDesc = (TextView) findViewById(R.id.desc);
        mVIP = (ImageView) findViewById(R.id.vip);
        mImg = (ImageView) findViewById(R.id.img);
        mImgs = (RelativeLayout) findViewById(R.id.imgs);
        mContainer = (RelativeLayout) findViewById(R.id.txt_container);
        mLayerView = findViewById(R.id.layer_view);
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
                        if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType()) && !mIsAd && mVIP != null && "2".equals(mDataMap.get("isVip")))
                            mVIP.setVisibility(View.VISIBLE);
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
        RelativeLayout.LayoutParams containerParams = (LayoutParams) mContainer.getLayoutParams();
        if (imgCount <= 0) {
            containerParams.height = LayoutParams.WRAP_CONTENT;
            mContainer.setMinimumHeight(0);
        } else {
            mContainer.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.dp_74_5));
        }
        String content = mDataMap.get("content");
        if (!TextUtils.isEmpty(content) && !mIsAd) {
            mDesc.setText(content);
            mDesc.setVisibility(View.VISIBLE);
        }
        if (imgCount == 1) {
            if (TextUtils.isEmpty(content))
                mTitle.setLines(2);
            mTitle.setMaxLines(Integer.MAX_VALUE);
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
        if (viewIsVisible(mVIP))
            mVIP.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
        if (viewIsVisible(mImgs))
            mImgs.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
        if (viewIsVisible(mDesc))
            mDesc.setVisibility(View.GONE);
    }
}
