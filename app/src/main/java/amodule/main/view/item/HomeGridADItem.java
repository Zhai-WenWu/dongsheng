package amodule.main.view.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.MessageQueue;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.logging.Handler;

import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.BlurBitmapTransformation;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import xh.basic.tool.UtilImage;

public class HomeGridADItem extends HomeItem {

    private ConstraintLayout mAdContainer;
    private ImageView mImgBlur;
    private ImageView mImg;
    private TextView mTitle;
    private TextView mHeaderText;
    private TextView mAdName;

    public HomeGridADItem(Context context) {
        this(context, null);
    }

    public HomeGridADItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeGridADItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_grid_ad_item);
    }

    @Override
    protected void initView() {
        super.initView();
        mAdContainer = findViewById(R.id.ad_container);
        mImgBlur = findViewById(R.id.img_blur);
        mImg = findViewById(R.id.img);
        mTitle = findViewById(R.id.title);
        mHeaderText = findViewById(R.id.header_text);
        mAdName = findViewById(R.id.ad_name);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if(mDataMap == null)
            return;
        Map<String, String> imgMap = StringManager.getFirstMap(mDataMap.get("styleData"));
        if (imgMap.size() > 0) {
            String imgUrl = imgMap.get("url");
            if (!TextUtils.isEmpty(imgUrl)) {
                mImgBlur.setImageResource(R.drawable.i_nopic);
                LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().into(new SubAnimTarget(mImg) {
                    @Override
                    protected void setResource(Bitmap bitmap) {
                        if (bitmap != null) {
                            int bmW = bitmap.getWidth();
                            int bmH = bitmap.getHeight();
                            int imgMaxH = mImgBlur.getHeight();
                            int imgMaxW = mImgBlur.getWidth();
                            int imgH = imgMaxW * bmH / bmW;
                            int dstW = imgMaxW;
                            int dstH = Math.min(imgMaxH, imgH);
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
                            ConstraintSet cs = new ConstraintSet();
                            cs.constrainWidth(mImg.getId(), dstW);
                            cs.constrainHeight(mImg.getId(), dstH);
                            cs.connect(mImg.getId(), ConstraintSet.TOP, mImgBlur.getId(), ConstraintSet.TOP);
                            cs.connect(mImg.getId(), ConstraintSet.BOTTOM, mImgBlur.getId(), ConstraintSet.BOTTOM);
                            cs.connect(mImg.getId(), ConstraintSet.END, mImgBlur.getId(), ConstraintSet.END);
                            cs.connect(mImg.getId(), ConstraintSet.START, mImgBlur.getId(), ConstraintSet.START);
                            cs.applyTo(mAdContainer);
                            mImg.setImageBitmap(scaledBitmap);
                        }
                    }
                });
                LoadImage.with(getContext()).load(imgUrl).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build().transform(new BlurBitmapTransformation(getContext(), 6, 6, 6)).into(mImgBlur);
            }
        }
        String adName = mDataMap.get("name");
        if (!TextUtils.isEmpty(adName)) {
            mAdName.setText(adName);
            mHeaderText.setText(adName.substring(0, 1));
        } else {
            mTitle.setText("");
            mHeaderText.setText("");
        }
        String content = mDataMap.get("content");
        mTitle.setText(TextUtils.isEmpty(content) ? "" : content);
    }
}
