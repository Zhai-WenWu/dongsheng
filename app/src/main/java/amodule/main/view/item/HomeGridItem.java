package amodule.main.view.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.multifunction.IconTextSpan;
import aplug.basic.SubAnimTarget;

public class HomeGridItem extends HomeItem {

    private ImageView mImg;
    private ImageView mPlayIcon;
    private ConstraintLayout mContentLayout;
    private TextView mTitle;

    private boolean mIsVideo;

    public HomeGridItem(Context context) {
        this(context, null);
    }

    public HomeGridItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeGridItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_grid_item);
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void initView() {
        super.initView();
        mImg = (ImageView) findViewById(R.id.img);
        mPlayIcon = (ImageView) findViewById(R.id.icon_play);
        mContentLayout = (ConstraintLayout) findViewById(R.id.content_layout);
        mTitle = (TextView) findViewById(R.id.title);
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
                loadImage(imgUrl, mImg);
            }
        }
        if (mDataMap.containsKey("video")) {
            String video = mDataMap.get("video");
            if (!TextUtils.isEmpty(video)) {
                Map<String, String> videoMap = StringManager.getFirstMap(video);
                String videoUrl = videoMap.get("videoUrl");
                if (!TextUtils.isEmpty(videoUrl)) {
                    Map<String, String> videoUrlMap = StringManager.getFirstMap(videoUrl);
                    String defUrl = videoUrlMap.get("defaultUrl");
                    if (!TextUtils.isEmpty(defUrl)) {
                        mIsVideo = true;
                    }
                }
            }
        }
        mPlayIcon.setVisibility(mIsVideo ? View.VISIBLE : View.GONE);
        mTitle.setText("");
        String title = mDataMap.get("name");
        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.VISIBLE);

            // TODO: 2018/5/29 测试代码-------
            mDataMap.put("isEssence", "2");
            //TODO:---------


            if (TextUtils.equals(mDataMap.get("isEssence"), "2")) {

                IconTextSpan.Builder ib = new IconTextSpan.Builder(getContext());
                ib.setBgColorInt(getResources().getColor(R.color.icon_text_bg));
                ib.setTextColorInt(getResources().getColor(R.color.c_white_text));
                ib.setText("精选");
                ib.setRadius(2f);
                ib.setRightMargin(3);
                ib.setBgHeight(14f);
                ib.setTextSize(10f);
                StringBuffer sb = new StringBuffer(" ");
                sb.append(title);
                SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
                ssb.setSpan(ib.build(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTitle.setText(ssb);
            } else {
                mTitle.setText(title);
            }
        } else {
            mTitle.setVisibility(View.GONE);
        }

        mContentLayout.setVisibility(!TextUtils.isEmpty(title) || (mUserName != null && !TextUtils.isEmpty(mUserName.getText())) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void resetData() {
        super.resetData();
        mIsVideo = false;
    }

    @Override
    protected SubAnimTarget getSubAnimTarget(ImageView v, String url) {
        return new SubAnimTarget(v) {
            @Override
            protected void setResource(Bitmap bitmap) {
                if (bitmap != null && v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(url)) {
                    v.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable drawable) {
                super.onLoadFailed(e, drawable);
                BuglyLog.i("image", "url = " + url + "  netStatus = " + ToolsDevice.getNetWorkSimpleType(getContext()));
                CrashReport.postCatchedException(e);
            }
        };
    }
}
