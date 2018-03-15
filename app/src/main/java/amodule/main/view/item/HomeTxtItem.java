package amodule.main.view.item;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import amodule.main.activity.MainHomePage;
import amodule.main.adapter.HomeAdapter;

/**
 * 右图，无图，样式, 带有标题，描述等信息
 * Created by sll on 2017/4/18.
 */

public class HomeTxtItem extends HomeItem {

    private TextView mTitle;
    private TextView mDesc;
    private ImageView mImg;
    private ImageView mVIP;
    private ImageView mPlayImg;
    private RelativeLayout mImgs;
    private RelativeLayout mContainer;
    private View mLayerView;

    private boolean mIsVideo;

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
        mTitle = (TextView) findViewById(R.id.title_txt);
        mDesc = (TextView) findViewById(R.id.desc);
        mVIP = (ImageView) findViewById(R.id.vip);
        mImg = (ImageView) findViewById(R.id.img);
        mPlayImg = (ImageView) findViewById(R.id.play_img);
        mImgs = (RelativeLayout) findViewById(R.id.imgs);
        mContainer = (RelativeLayout) findViewById(R.id.txt_container);
        mLayerView = findViewById(R.id.layer_view);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        String name = mIsAd ? mDataMap.get("content") : mDataMap.get("name");
        mTitle.setText(name);
        mTitle.setVisibility(!TextUtils.isEmpty(name) ? View.VISIBLE : View.GONE);
        mLayerView.setVisibility(mIsAd ? View.VISIBLE : View.GONE);
        String video = mDataMap.get("video");
        Map<String, String> videoMap = StringManager.getFirstMap(video);
        String videoUrl = videoMap.get("videoUrl");
        Map<String, String> videoUrlMap = StringManager.getFirstMap(videoUrl);
        String defVideoUrl = videoUrlMap.get("defaultUrl");
        if (!TextUtils.isEmpty(defVideoUrl))
            mIsVideo = true;
        mPlayImg.setVisibility(mIsVideo ? View.VISIBLE : View.GONE);
        int imgCount = 0;
        if (String.valueOf(HomeAdapter.type_rightImage).equals(mDataMap.get("style"))) {//右图模式
            Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
            String imgUrl = imgMap.get("url");
            if (!TextUtils.isEmpty(imgUrl))
                imgCount = 1;
            mImgs.setVisibility(View.VISIBLE);
            if (mModuleBean != null && MainHomePage.recommedType.equals(mModuleBean.getType()) && !mIsAd && mVIP != null && "2".equals(mDataMap.get("isVip")))
                mVIP.setVisibility(View.VISIBLE);
            loadImage(imgUrl, mImg);
        } else {
            mImgs.setVisibility(View.GONE);
        }
        String content = mDataMap.get("content");
        mDesc.setText(content);
        mDesc.setVisibility(!TextUtils.isEmpty(content) && !mIsAd ? View.VISIBLE : View.GONE);
        switch (imgCount) {
            case 0:
                mContainer.setMinimumHeight(0);
                break;
            case 1:
                if (TextUtils.isEmpty(content))
                    mTitle.setLines(2);
                mTitle.setMaxLines(Integer.MAX_VALUE);
                mContainer.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.dp_74_5));
                break;
        }
    }

    @Override
    protected void resetData() {
        super.resetData();
        mIsVideo = false;
    }

    @Override
    protected void resetView() {
        super.resetView();
        mTitle.setLines(1);
        mTitle.setMaxLines(2);
        mVIP.setVisibility(View.GONE);
        mImg.setVisibility(View.GONE);
        mImgs.setVisibility(View.GONE);
        mDesc.setVisibility(View.GONE);
    }
}
