package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import amodule.main.activity.MainHome;

/**
 * 广告Item
 * Created by sll on 2017/4/18.
 */

public class HomeADItem extends HomeItem {

    private ImageView mADImg;
    private View mLayerView;

    public HomeADItem(Context context) {
        super(context, R.layout.home_aditem);
    }

    public HomeADItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.home_aditem);
    }

    public HomeADItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_aditem);
    }

    @Override
    protected void initView() {
        super.initView();
        mADImg = (ImageView) findViewById(R.id.ad);
        mLayerView = findViewById(R.id.layer_view);
        if (mADImg != null)
            mADImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdControlParent != null)
                        mAdControlParent.onAdClick(mDataMap);
                    if (!TextUtils.isEmpty(mTransferUrl)) {
                        if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                            if(mTransferUrl.contains("?"))mTransferUrl+="&data_type="+mDataMap.get("type");
                            else mTransferUrl+="?data_type="+mDataMap.get("type");
                            Log.i("zhangyujian","点击："+mDataMap.get("code")+":::"+mTransferUrl);
                            XHClick.saveStatictisFile("home","recom",mDataMap.get("type"),mDataMap.get("code"),"","click","","",String.valueOf(mPosition+1),"","");
                        }
                    }
                }
            });
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
        if (mDataMap.containsKey("img") && !TextUtils.isEmpty(mDataMap.get("img"))) {
            loadImage(mDataMap.get("img"), mADImg);
            if (mIsAd && mAdControlParent != null)
                mAdControlParent.onAdShow(mDataMap, mADImg);
            if (mLayerView != null && !viewIsVisible(mLayerView))
                mLayerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mADImg))
            mADImg.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
    }
}
