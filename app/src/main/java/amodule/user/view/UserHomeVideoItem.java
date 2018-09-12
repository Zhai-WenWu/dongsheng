package amodule.user.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.Map;

import static amodule.dish.db.UploadDishData.UPLOAD_DRAF;
import static amodule.dish.db.UploadDishData.UPLOAD_FAIL;

/**
 * Created by sll on 2017/5/24.
 */
public class UserHomeVideoItem extends UserHomeTxtItem {
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
    }

    @Override
    protected void bindData() {
        super.bindData();
        String fromLocal = mDataMap.get("dataFrom");
        if ("1".equals(fromLocal)) {//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
            String uploadType = mDataMap.get("uploadType");
            String statusInfo = "";
            if (mStatusInfo != null) {
                mStatusInfo.setTextColor(Color.parseColor("#f23030"));
                switch (uploadType) {
                    case UPLOAD_FAIL:
                        statusInfo = "上传失败";
                        break;
                    case UPLOAD_DRAF:
                        statusInfo = "草稿箱";
                        break;
                }
            }
            if (!TextUtils.isEmpty(statusInfo)) {
                mStatusInfo.setText(statusInfo);
                mStatusInfo.setVisibility(VISIBLE);
                mNameGourmet.setVisibility(GONE);
                mNum1.setVisibility(GONE);
                mNum2.setVisibility(GONE);
            }
            String path = mDataMap.get("imgPath");
            if (mImg != null && path != null) {
                Glide.with(getContext())
                        .load(path)
                        .override(getResources().getDimensionPixelSize(R.dimen.dp_110), getResources().getDimensionPixelSize(R.dimen.dp_72_5))
                        .centerCrop()
                        .into(mImg);
                mImg.setVisibility(View.VISIBLE);
            }
            mDeleteIcon.setVisibility(VISIBLE);
            mTxtContainer.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.dp_74_5));
            if (mImgsLayout != null)
                mImgsLayout.setVisibility(View.VISIBLE);
            mPlayImg.setVisibility(View.VISIBLE);
        }else{
            mDeleteIcon.setVisibility(INVISIBLE);
        }
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