package amodule.user.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import amodule.dish.db.UploadDishData;

/**
 * Created by sll on 2017/5/24.
 */

public class UserHomeTxtItem extends UserHomeItem implements View.OnClickListener{

    protected LinearLayout mNameGourmet;
    protected TextView mUserName;
    protected ImageView mGourmetIcon;
    protected ImageView mDeleteIcon;

    protected TextView mTitle;
    protected RelativeLayout mImgsLayout;
    protected RelativeLayout mTxtContainer;
    protected ImageView mImg;
    protected View mLayerView;
    protected ImageView mADTag;
    protected TextView mNum1;
    protected TextView mStatusInfo;
    protected TextView mNum2;
    protected ImageView mPlayImg;

    private String mUploadType = "";

    protected OnDeleteClickCallback mDeleteClickListener;

    public UserHomeTxtItem(Context context) {
        super(context, R.layout.userhome_txtitem);
    }

    public UserHomeTxtItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.userhome_txtitem);
    }

    public UserHomeTxtItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.userhome_txtitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mTitle = (TextView) findViewById(R.id.title);
        mImgsLayout = (RelativeLayout) findViewById(R.id.imgs);
        mTxtContainer = (RelativeLayout) findViewById(R.id.txt_container);
        mDeleteIcon = findViewById(R.id.delete);
        mImg = (ImageView) findViewById(R.id.img);
        mLayerView = findViewById(R.id.layer_view);
        mADTag = (ImageView) findViewById(R.id.ad_tag);
        mNum1 = (TextView) findViewById(R.id.num1);
        mNum2 = (TextView) findViewById(R.id.num2);
        mStatusInfo = (TextView) findViewById(R.id.status_info);
        mPlayImg = (ImageView) findViewById(R.id.play_img);
        mNameGourmet = (LinearLayout) findViewById(R.id.name_gourmet);
        mUserName = (TextView) findViewById(R.id.user_name);
        mGourmetIcon = (ImageView) findViewById(R.id.gourmet_icon);

        addListener();
    }

    private void addListener() {
        this.setOnClickListener(this);
        mDeleteIcon.setOnClickListener(v -> {
            if(mDeleteClickListener != null){
                mDeleteClickListener.onDeleteClick(mDataMap);
            }
        });
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
    }

    @Override
    protected void bindData() {
        super.bindData();
        if (mIsAd) {
            if (mLayerView != null)
                mLayerView.setVisibility(View.VISIBLE);
            String desc = mDataMap.get("content");
            if (!TextUtils.isEmpty(desc) && mTitle != null) {
                mTitle.setText(desc);
                mTitle.setVisibility(View.VISIBLE);
            }
            if (mADTag != null && (!mDataMap.containsKey("adType") || !"1".equals(mDataMap.get("adType"))))
                mADTag.setVisibility(View.VISIBLE);
            mAdControlParent.onAdShow(mDataMap, this);
        }
        if (!mIsAd) {
            String name = mDataMap.get("title");
            if (!TextUtils.isEmpty(name) && mTitle != null) {
                mTitle.setText(name);
                mTitle.setVisibility(View.VISIBLE);
            }
        }
        boolean hasImg = false;
        boolean hasVideo = false;
        String fromLocal = mDataMap.get("dataFrom");
        if ("1".equals(fromLocal)) {//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
            mUploadType = mDataMap.get("uploadType");
            String statusInfo = "";
            if (mStatusInfo != null) {
                switch (mUploadType) {
                    case UploadDishData.UPLOAD_FAIL:
                        statusInfo = "上传失败，请重试";
                        mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                        break;
                    case UploadDishData.UPLOAD_PAUSE:
                        statusInfo = "暂停上传";
                        mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                        break;
                    case UploadDishData.UPLOAD_ING:
                        statusInfo = "上传中...";
                        mStatusInfo.setTextColor(Color.parseColor("#00c847"));
                        break;
                }
            }
            if (!TextUtils.isEmpty(statusInfo)) {
                mStatusInfo.setText(statusInfo);
                mStatusInfo.setVisibility(VISIBLE);
            }
            String path = null;
            ArrayList<Map<String, String>> imgs = StringManager.getListMapByJson(mDataMap.get("imgs"));
            ArrayList<Map<String, String>> videos = StringManager.getListMapByJson(mDataMap.get("videos"));
            if (imgs != null && imgs.size() > 0) {
                path = imgs.get(0).get("path");
                hasImg = true;
            } else if (videos != null && videos.size() > 0) {
                hasVideo = true;
                path = videos.get(0).get("image");
            }
            if (mImg != null && path != null) {
                hasImg = true;
                Glide.with(getContext())
                        .load(new File(path))
                        .override(getResources().getDimensionPixelSize(R.dimen.dp_110), getResources().getDimensionPixelSize(R.dimen.dp_72_5))
                        .centerCrop()
                        .into(mImg);
                mImg.setVisibility(View.VISIBLE);
            }
        } else {
            String styleData = mDataMap.get("styleData");
            ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(styleData);
            if (datas != null && !datas.isEmpty()) {
                String tempUrl = null;
                for (Map<String, String> data : datas) {
                    if (data != null && data.size() > 0) {
                        String type = data.get("type");
                        String imgUrl = data.get("url");
                        if (!TextUtils.isEmpty(type) && "1".equals(type) && !TextUtils.isEmpty(imgUrl)) {
                            hasImg = true;
                            tempUrl = imgUrl;
                            break;
                        }
                    }
                }
                if (hasImg && tempUrl != null) {
                    loadImage(tempUrl, mImg);
                } else {
                    Map<String, String> data = datas.get(0);
                    if (data != null && data.size() > 0) {
                        String imgUrl = data.get("url");
                        if (!TextUtils.isEmpty(imgUrl)) {
                            hasImg = true;
                            loadImage(imgUrl, mImg);
                        }
                        String type = data.get("type");
                        if (!TextUtils.isEmpty(type) && "2".equals(type))
                            hasVideo = true;
                    }
                }
            }

            String statusInfo = mDataMap.get("status");
            if ("1".equals(statusInfo)) {
                if (mStatusInfo != null) {
                    mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                    mStatusInfo.setText("审核未通过");
                    mStatusInfo.setVisibility(View.VISIBLE);
                }
            } else {
                String customer = mDataMap.get("customer");
                if (!TextUtils.isEmpty(customer)) {
                    ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(customer);
                    for (Map<String, String> map : maps) {
                        boolean showNameGourmet = false;
                        if (map != null) {
                            String nickName = map.get("nickName");
                            if (!TextUtils.isEmpty(nickName) && mUserName != null) {
                                mUserName.setText(nickName);
                                mUserName.setVisibility(View.VISIBLE);
                                showNameGourmet = true;
                            }
                            String isGourmet = map.get("isGourmet");
                            if (!TextUtils.isEmpty(isGourmet) && mGourmetIcon != null && Integer.parseInt(isGourmet) == 2) {
                                mGourmetIcon.setVisibility(View.VISIBLE);
                                showNameGourmet = true;
                            }
                            if (showNameGourmet && mNameGourmet != null)
                                mNameGourmet.setVisibility(View.VISIBLE);
                        }
                    }
                }
                ArrayList<Map<String, String>> numInfos = StringManager.getListMapByJson(mDataMap.get("numInfo"));
                if (numInfos != null && !numInfos.isEmpty()) {
                    for (int i = 0; i < numInfos.size(); i ++) {
                        Map<String, String> numInfo = numInfos.get(i);
                        if (numInfo != null && !numInfo.isEmpty()) {
                            String numStr = numInfo.get("");
                            if (!TextUtils.isEmpty(numStr)) {
                                switch (i) {
                                    case 0:
                                        if (mNum1 != null) {
                                            mNum1.setText(numStr);
                                            mNum1.setVisibility(View.VISIBLE);
                                        }
                                        break;
                                    case 1:
                                        if (mNum2 != null) {
                                            mNum2.setText(numStr);
                                            mNum2.setVisibility(View.VISIBLE);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (hasImg) {
            mTxtContainer.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.dp_74_5));
            if (mImgsLayout != null)
                mImgsLayout.setVisibility(View.VISIBLE);
        } else {
            mTxtContainer.setMinimumHeight(0);
        }
        if (hasVideo && mPlayImg != null) {
            mPlayImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void resetData() {
        super.resetData();
        mUploadType = "";
    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mUserName))
            mUserName.setVisibility(View.GONE);
        if (viewIsVisible(mGourmetIcon))
            mGourmetIcon.setVisibility(View.GONE);
        if (viewIsVisible(mNameGourmet))
            mNameGourmet.setVisibility(View.GONE);
        if (viewIsVisible(mImgsLayout))
            mImgsLayout.setVisibility(View.GONE);
        if (viewIsVisible(mImg))
            mImg.setVisibility(View.GONE);
        if (viewIsVisible(mLayerView))
            mLayerView.setVisibility(View.GONE);
        if (viewIsVisible(mADTag))
            mADTag.setVisibility(View.GONE);
        if (viewIsVisible(mNum1))
            mNum1.setVisibility(View.GONE);
        if (viewIsVisible(mNum2))
            mNum2.setVisibility(View.GONE);
        if (viewIsVisible(mStatusInfo))
            mStatusInfo.setVisibility(View.GONE);
        if (viewIsVisible(mPlayImg))
            mPlayImg.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null)
            mOnItemClickListener.onItemClick((UserHomeItem)v, mDataMap);
    }

    @Override
    public void notifyUploadStatusChanged(String uploadType) {
        if (!TextUtils.isEmpty(uploadType) && !mUploadType.equals(uploadType)) {
            mUploadType = uploadType;
            String statusInfo = "";
            if (mStatusInfo != null && mStatusInfo.getVisibility() == View.VISIBLE) {
                switch (mUploadType) {
                    case UploadDishData.UPLOAD_FAIL:
                        statusInfo = "上传失败，请重试";
                        mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                        break;
                    case UploadDishData.UPLOAD_PAUSE:
                        statusInfo = "暂停上传";
                        mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                        break;
                    case UploadDishData.UPLOAD_ING:
                        statusInfo = "上传中...";
                        mStatusInfo.setTextColor(Color.parseColor("#00c847"));
                        break;
                }
                if (!TextUtils.isEmpty(statusInfo)) {
                    mStatusInfo.setText(statusInfo);
                }
            }
        }
    }

    public interface OnDeleteClickCallback{
        void onDeleteClick(Map<String,String> data);
    }

    public void setDeleteClickListener(OnDeleteClickCallback deleteClickListener) {
        mDeleteClickListener = deleteClickListener;
    }
}
