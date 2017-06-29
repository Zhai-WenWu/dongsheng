package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private ImageView mImg1;
    private ImageView mImg2;
    private ImageView mImg3;

    private LinearLayout mImgsContainerCenter;

    private View mLayerView1;
    private View mLayerView2;
    private View mLayerView3;

    public HomePostItem(Context context) {
        this(context, null);
    }

    public HomePostItem(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomePostItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_postitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title);
        mImg1 = (ImageView) findViewById(R.id.img1);
        mImg2 = (ImageView) findViewById(R.id.img2);
        mImg3 = (ImageView) findViewById(R.id.img3);
        mImgsContainerCenter = (LinearLayout) findViewById(R.id.imgs_container_center);
        mLayerView1 = findViewById(R.id.layer_view1);
        mLayerView2 = findViewById(R.id.layer_view2);
        mLayerView3 = findViewById(R.id.layer_view3);
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
        if (mDataMap.containsKey("styleData")) {
            String imgs = mDataMap.get("styleData");
            if (!TextUtils.isEmpty(imgs)) {
                ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(imgs);
                if (maps != null && maps.size() >= 3) {
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
                    loadImage(maps.get(0).get("url"), mImg1);
                    loadImage(maps.get(1).get("url"), mImg2);
                    loadImage(maps.get(2).get("url"), mImg3);
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
            mTitle.setText(title);
            mTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mImgsContainerCenter))
            mImgsContainerCenter.setVisibility(View.GONE);
    }
}
