package amodule.dish.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.search.avtivity.HomeSearch;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * 菜单列表Item
 * Created by sll on 2017/6/21.
 */

public class ListDishItemView extends RelativeLayout {

    private final int TAG_ID = R.string.tag;
    private int mImgResource = R.drawable.i_nopic;
    private int mRoundImgPixels = 0, mImgWidth = 0, mImgHeight = 0,// 以像素为单位
            mRoundType = 1; // 1为全圆角，2上半部分圆角
    private boolean mImgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    private String mImgLevel = FileManager.save_cache; // 图片保存等级
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;

    private ImageView mImg;
    private ImageView mPlayImg;
    private ImageView mADTag;
    private ImageView mGourmetIcon;
    private TextView mDurationTime;
    private TextView mTitleTop;
    private TextView mDesc;
    private TextView mUserName;
    private TextView mNum1;
    private TextView mNum2;
    private RelativeLayout mIconSearch;
    private View mLayerView;

    private Map<String, String> mData;

    public ListDishItemView(Context context) {
        super(context);
        inflateView(context);
    }

    public ListDishItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    public ListDishItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    private void inflateView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.a_dish_item_menu_new, this, true);
        initView();
    }

    private void initView() {
        mImg = (ImageView) findViewById(R.id.img);
        mPlayImg = (ImageView) findViewById(R.id.play_img);
        mADTag = (ImageView) findViewById(R.id.ad_tag);
        mGourmetIcon = (ImageView) findViewById(R.id.gourmet_icon);
        mDurationTime = (TextView) findViewById(R.id.video_time);
        mTitleTop = (TextView) findViewById(R.id.title_top);
        mDesc = (TextView) findViewById(R.id.title);
        mUserName = (TextView) findViewById(R.id.user_name);
        mNum1 = (TextView) findViewById(R.id.num1);
        mNum2 = (TextView) findViewById(R.id.num2);
        mIconSearch = (RelativeLayout) findViewById(R.id.icon_search_container);
        mLayerView = findViewById(R.id.layer_view);
        addListener();
    }

    private void addListener() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.icon_search_container:
                        Intent intent = new Intent(getContext(), HomeSearch.class);
                        Bundle bundle = new Bundle();
                        String serachKey = mData.get("name");
                        bundle.putString("s", serachKey);
                        intent.putExtras(bundle);
                        getContext().startActivity(intent);
                        break;
                    case R.id.ad_tag:
                        break;
                }
            }
        };
        mIconSearch.setOnClickListener(onClickListener);
        mADTag.setOnClickListener(onClickListener);
    }

    public void setData (Map<String, String> data) {
        if (data == null)
            return;
        mData = data;
        bindView();
        loadImage();
    }

    private void bindView() {
        String title = mData.get("alias");
        if (TextUtils.isEmpty(title))
            title = mData.get("name");
        if (!TextUtils.isEmpty(title)) {
            mTitleTop.setText(title);
            mTitleTop.setVisibility(View.VISIBLE);
        } else
            mTitleTop.setVisibility(View.GONE);
        String desc = mData.get("info");
        if (!TextUtils.isEmpty(desc)) {
            mDesc.setText(desc);
            mDesc.setVisibility(View.VISIBLE);
        } else
            mDesc.setVisibility(View.GONE);
        ArrayList<Map<String, String>> customers = StringManager.getListMapByJson(mData.get("customer"));
        if (customers != null && !customers.isEmpty()) {
            Map<String, String> customer = customers.get(0);
            if (customer != null) {
                String name = customer.get("nickName");
                if (!TextUtils.isEmpty(name)) {
                    mUserName.setText(name);
                    mUserName.setVisibility(View.VISIBLE);
                } else
                    mUserName.setVisibility(View.GONE);
                String isGourmet = customer.get("isGourmet");
                if ("2".equals(isGourmet))
                    mGourmetIcon.setVisibility(View.VISIBLE);
                else
                    mGourmetIcon.setVisibility(View.GONE);
            }
        } else {
            mUserName.setVisibility(View.GONE);
            mGourmetIcon.setVisibility(View.GONE);
        }
        String allClick = mData.get("allClick");
        if (!TextUtils.isEmpty(allClick)) {
            mNum1.setText(allClick);
            mNum1.setVisibility(View.VISIBLE);
        } else
            mNum1.setVisibility(View.GONE);
        String favorites = mData.get("favorites");
        if (!TextUtils.isEmpty(favorites)) {
            mNum2.setText(favorites);
            mNum2.setVisibility(View.VISIBLE);
        } else
            mNum2.setVisibility(View.GONE);
        if(mData.containsKey("adStyle")&&"1".equals(mData.get("adStyle"))){
            mIconSearch.setVisibility(GONE);
        }else{
            mIconSearch.setVisibility(VISIBLE);
        }
    }

    private void loadImage() {
        String imgUrl = mData.get("img");
        if (!TextUtils.isEmpty(imgUrl))
            loadImage(mImg, imgUrl);
    }

    private void loadImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                mRoundImgPixels = ToolsDevice.dp2px(v.getContext(), 500);
                v.setImageResource(R.drawable.bg_round_user_icon);
            } else {
                v.setImageResource(mImgResource);
            }
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (value.length() < 10)
                return;
            v.setTag(TAG_ID, value);
            if (v.getContext() == null) return;
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(getTarget(v, value));
        }
        // 直接设置为内部图片
        else if (value.indexOf("ico") == 0) {
            InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
            Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
            bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, mRoundType, mRoundImgPixels);
            UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
        }
        // 隐藏
        else if (value.equals("hide") || value.length() == 0)
            v.setVisibility(View.GONE);
            // 直接加载本地图片
        else if (!value.equals("ignore")) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setImageResource(mImgResource);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(getTarget(v, value));
        }
    }

    private SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    v.setScaleType(mScaleType);
                    UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
                }
            }
        };
    }
}
