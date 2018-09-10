package amodule.user.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import amodule.dish.db.UploadDishData;

import static amodule.dish.db.UploadDishData.UPLOAD_DRAF;
import static amodule.dish.db.UploadDishData.UPLOAD_FAIL;
import static amodule.dish.db.UploadDishData.UPLOAD_PAUSE;

/**
 * Created by sll on 2017/5/24.
 */

public class UserHomeVideoItem extends UserHomeTxtItem {
    private String mUploadType = "";
    public UserHomeVideoItem(Context context) {
        super(context);
    }

    public UserHomeVideoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserHomeVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        boolean hasImg = false;
        boolean hasVideo = false;
        String fromLocal = mDataMap.get("dataFrom");
        if ("1".equals(fromLocal)) {//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
            mUploadType = mDataMap.get("uploadType");
        mUploadType = UPLOAD_FAIL;
            String statusInfo = "";
            if (mStatusInfo != null) {
                switch (mUploadType) {
                    case UPLOAD_FAIL:
                        statusInfo = "上传失败，请重试";
                        mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                        break;
                    case UPLOAD_DRAF:
                        statusInfo = "草稿箱";
                        mStatusInfo.setTextColor(Color.parseColor("#999999"));
                        break;
                }
            }
            if (!TextUtils.isEmpty(statusInfo)) {
                mStatusInfo.setText(statusInfo);
                mStatusInfo.setVisibility(VISIBLE);
                //TODO
                mNameGourmet.setVisibility(GONE);
                mNum1.setVisibility(GONE);
                mNum2.setVisibility(GONE);
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
        }
    }

    @Override
    protected void bindData() {
        super.bindData();

    }

    @Override
    protected void resetData() {
        super.resetData();
    }

    @Override
    protected void resetView() {
        super.resetView();
    }



}
