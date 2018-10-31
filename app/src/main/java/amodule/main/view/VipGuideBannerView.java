package amodule.main.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

public class VipGuideBannerView extends LinearLayout {

    private ImageView mGuideIcon;
    private ImageView mGuideArrow;
    private TextView mGuideTitle;
    private TextView mGuideDesc;
    private TextView mGuidePoint;
    private TextView mGuideSubtitle;
    public VipGuideBannerView(Context context) {
        super(context);
    }

    public VipGuideBannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VipGuideBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VipGuideBannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mGuideIcon = findViewById(R.id.guide_icon);
        mGuideArrow = findViewById(R.id.guide_arrow);
        mGuideTitle = findViewById(R.id.guide_title);
        mGuideDesc = findViewById(R.id.guide_desc);
        mGuidePoint = findViewById(R.id.guide_point);
        mGuideSubtitle = findViewById(R.id.guide_subtitle);
    }

    public void setTitle(String title) {
        mGuideTitle.setText(TextUtils.isEmpty(title) ? "" : title);
        mGuideTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        mGuidePoint.setVisibility(!TextUtils.isEmpty(mGuideSubtitle.getText()) ? View.VISIBLE : View.GONE);
    }

    public void setSubtitle(String subtitle) {
        mGuideSubtitle.setText(TextUtils.isEmpty(subtitle) ? "" : subtitle);
        mGuideSubtitle.setVisibility(TextUtils.isEmpty(subtitle) ? View.GONE : View.VISIBLE);
        mGuidePoint.setVisibility(!TextUtils.isEmpty(mGuideTitle.getText()) ? View.VISIBLE : View.GONE);
    }

    public void setDesc(String desc) {
        mGuideDesc.setText(desc);
    }
}
