package amodule.user.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import acore.widget.TagTextView;

import static amodule.dish.db.UploadDishData.UPLOAD_DRAF;
import static amodule.dish.db.UploadDishData.UPLOAD_FAIL;

/**
 * Created by sll on 2017/5/24.
 */
public class UserHomeVideoItem extends UserHomeItem {
    public static final int LAYOUT_ID = R.layout.item_personal_video;

    ImageView imageVew;
    ImageView deleteIcon;
    TagTextView uplaodState;
    TagTextView descText;

    public UserHomeVideoItem(Context context) {
        super(context, LAYOUT_ID);
    }

    public UserHomeVideoItem(Context context, AttributeSet attrs) {
        super(context, attrs, LAYOUT_ID);
    }

    public UserHomeVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, LAYOUT_ID);
    }

    @Override
    protected void initView() {
        super.initView();
        imageVew = findViewById(R.id.image);
        deleteIcon = findViewById(R.id.delete_icon);
        uplaodState = findViewById(R.id.upload_state);
        descText = findViewById(R.id.desc_text);

        addListener();
    }

    private void addListener() {
        setOnClickListener(v -> {
            if(mOnItemClickListener != null){
                mOnItemClickListener.onItemClick(UserHomeVideoItem.this,mDataMap);
            }
        });
        deleteIcon.setOnClickListener(v -> {
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
        //处理上传状态
        String fromLocal = mDataMap.get("dataFrom");
        if ("1".equals(fromLocal)) {
            String path = mDataMap.get("imgPath");
            if (imageVew != null && path != null) {
                Glide.with(getContext())
                        .load(path)
                        .centerCrop()
                        .into(imageVew);
                imageVew.setVisibility(View.VISIBLE);
            }
            String uploadType = mDataMap.get("uploadType");
            if (UPLOAD_FAIL.equals(uploadType)) {
                uplaodState.setText("上传失败");
                uplaodState.setDrawableL(R.drawable.icon_upload_fail);
            } else if (UPLOAD_DRAF.equals(uploadType)) {
                uplaodState.setText("草稿箱");
                uplaodState.setDrawableL(R.drawable.icon_draf);
            }

            uplaodState.setVisibility(View.VISIBLE);
            deleteIcon.setVisibility(View.VISIBLE);
            descText.setVisibility(View.GONE);
        } else {
            Map<String,String> imgMap = StringManager.getFirstMap(mDataMap.get("image"));
            String path = imgMap.get("url");
            if (imageVew != null && path != null) {
                Glide.with(getContext())
                        .load(path)
                        .centerCrop()
                        .into(imageVew);
                imageVew.setVisibility(View.VISIBLE);
            }
            //网络数据
            deleteIcon.setVisibility(View.GONE);
            String statusInfo = mDataMap.get("status");
            if ("1".equals(statusInfo)) {
                uplaodState.setText("审核未通过");
                uplaodState.setDrawableL(R.drawable.icon_not_release);
                uplaodState.setVisibility(View.VISIBLE);
            } else {
                uplaodState.setVisibility(View.GONE);
            }
            boolean isMe = "2".equals(mDataMap.get("isMe"));
            if (uplaodState.getVisibility() == View.GONE) {
                //正常数据显示
                if(isMe){
                    descText.setText(mDataMap.get("clickNum"));//likeNumber	String	0
                }else {
                    String text = mDataMap.get("likeNum");
                    if(!TextUtils.isEmpty(text) && !"0".equals(text)){
                        descText.setText(text);
                    }else{
                        descText.setText("");
                    }
                }
                descText.setDrawableL(isMe ? R.drawable.icon_play : R.drawable.icon_likex);
                descText.setVisibility(View.VISIBLE);
            }else{
                descText.setVisibility(View.GONE);
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
    }

}