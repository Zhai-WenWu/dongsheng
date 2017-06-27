package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.StringManager;
import amodule.main.activity.MainHome;

/**
 * 蒙版--样式
 * Created by sll on 2017/4/18.
 */

public class HomeAlbumItem extends HomeItem {

    private ImageView mVIP;
    private ImageView mImg;
    private TextView mTitle;
    private TextView mNum1;
    private View mLayerView;

    public HomeAlbumItem(Context context) {
        this(context, null);
    }

    public HomeAlbumItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeAlbumItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_albumitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mVIP = (ImageView) findViewById(R.id.vip);
        mImg = (ImageView) findViewById(R.id.img);
        mTitle = (TextView) findViewById(R.id.title);
        mNum1 = (TextView) findViewById(R.id.num1);
        mLayerView = findViewById(R.id.layer_view);
        addListener();
    }

    private void addListener() {
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mTransferUrl)) {
                    if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                        if(mTransferUrl.contains("?"))mTransferUrl+="&data_type="+mDataMap.get("type");
                        else mTransferUrl+="?data_type="+mDataMap.get("type");
                        mTransferUrl+="&module_type="+(isTopTypeView()?"top_info":"info");
                        XHClick.saveStatictisFile("home",getModleViewType(),mDataMap.get("type"),mDataMap.get("code"),"","click","","",String.valueOf(mPosition+1),"","");
                    }
                    AppCommon.openUrl((Activity) getContext(), mTransferUrl, false);
                }
                onItemClick();
            }
        });
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mModuleBean != null && !mIsAd && mVIP != null && "2".equals(mDataMap.get("isVip")))
            mVIP.setVisibility(View.VISIBLE);
        if (mDataMap.containsKey("styleData")) {
            ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
            if (datas != null && datas.size() > 0) {
                Map<String, String> imgMap = datas.get(0);
                if (imgMap != null && imgMap.size() > 0) {
                    String imgUrl = imgMap.get("url");
                    loadImage(imgUrl, mImg);
                    mLayerView.setVisibility(View.VISIBLE);
                }
            }
        } else findViewById(R.id.layer_view).setVisibility(View.GONE);
        if (mDataMap.containsKey("name") && mTitle != null) {
            String albumName = mDataMap.get("name");
            if (!TextUtils.isEmpty(albumName)) {
                mTitle.setText(albumName);
                mTitle.setVisibility(View.VISIBLE);
            }
        }
        if (mDataMap.containsKey("dishNum") && mNum1 != null) {
            String favorites = handleNumber(mDataMap.get("dishNum"));
            if (!TextUtils.isEmpty(favorites)) {
                mNum1.setText(favorites + "道菜");
                mNum1.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mNum1))
            mNum1.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
        if (viewIsVisible(mVIP))
            mVIP.setVisibility(View.GONE);

    }
}
