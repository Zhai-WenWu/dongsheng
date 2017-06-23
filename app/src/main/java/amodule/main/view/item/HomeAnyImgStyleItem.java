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
import amodule.main.Tools.ImageUtility;
import amodule.main.activity.MainHome;

/**
 * 任意图 样式：限宽不限高
 * Created by sll on 2017/6/23.
 */

public class HomeAnyImgStyleItem extends HomeItem {

    private TextView mTitleTop;
    private ImageView mImg;

    public HomeAnyImgStyleItem(Context context) {
        super(context, R.layout.home_anyimg_style_item);
    }

    public HomeAnyImgStyleItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.home_anyimg_style_item);
    }

    public HomeAnyImgStyleItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_anyimg_style_item);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitleTop = (TextView) findViewById(R.id.title_top);
        mImg = (ImageView) findViewById(R.id.img);
        addListener();
    }

    private void addListener() {
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        this.setOnClickListener(clickListener);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        boolean hasTitleTop = false;
        String titleTop = mDataMap.get("name");
        if (!TextUtils.isEmpty(titleTop)) {
            mTitleTop.setText(titleTop);
            mTitleTop.setVisibility(View.VISIBLE);
            hasTitleTop = true;
        }
        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(mDataMap.get("styleData"));
        if (datas != null && datas.size() > 0) {
            Map<String, String> imgMap = datas.get(0);
            if (imgMap != null && imgMap.size() > 0) {
                String imgUrl = imgMap.get("url");
                int[] size = new int[2];
                ImageUtility.getInstance().getImageSizeByUrl(imgUrl, size);
                if (size[0] > 0 && size[1] > 0) {
                    int fixedWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - getResources().getDimensionPixelSize(R.dimen.dp_40);
                    int newHeight = fixedWidth * size[1] / size[0];
                    RelativeLayout.LayoutParams params = (LayoutParams) mImg.getLayoutParams();
                    params.height = newHeight;
                    params.topMargin = getResources().getDimensionPixelSize(hasTitleTop ? R.dimen.dp_6 : R.dimen.dp_15);
                    requestLayout();
                    invalidate();
                }
                loadImage(imgUrl, mImg);
            }
        }

    }

    @Override
    protected void resetData() {
        super.resetData();
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitleTop))
            mTitleTop.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
    }
}
