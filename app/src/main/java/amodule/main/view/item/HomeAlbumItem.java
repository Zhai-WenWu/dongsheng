package amodule.main.view.item;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;

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
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mModuleBean != null && !mIsAd && "2".equals(mDataMap.get("isVip")))
            mVIP.setVisibility(View.VISIBLE);
        if (mDataMap.containsKey("styleData")) {
            Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
            mLayerView.setVisibility(imgMap.size() > 0 ? View.VISIBLE : View.GONE);
            loadImage(imgMap.get("url"), mImg);
        } else
            mLayerView.setVisibility(View.GONE);
        String albumName = mDataMap.get("name");
        mTitle.setText(albumName);
        mTitle.setVisibility(!TextUtils.isEmpty(albumName) ? View.VISIBLE : View.GONE);
        String favorites = handleNumber(mDataMap.get("dishNum"));
        mNum1.setText(favorites + "道菜");
        mNum1.setVisibility(!TextUtils.isEmpty(favorites) ? View.VISIBLE : View.GONE);

    }

    @Override
    protected void resetView() {
        super.resetView();
        mVIP.setVisibility(View.GONE);

    }
}
