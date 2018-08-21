package amodule._common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import acore.tools.Tools;
import acore.tools.ToolsDevice;

public class ShadowBannerView extends BannerView {

    private int mShadowTopPadding, mShadowBottomPadding, mShadowLeftPadding, mShadowRightPadding;
    private int mFixedLRImageToScreenSpacing;

    public ShadowBannerView(Context context) {
        super(context);
    }

    public ShadowBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShadowBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        mInflater = LayoutInflater.from(context);
        View shadowView = getView(0);
        View shadow = shadowView.findViewById(R.id.shadow);
        mShadowLeftPadding = shadow.getPaddingLeft();
        mShadowRightPadding = shadow.getPaddingRight();
        mShadowTopPadding = shadow.getPaddingTop();
        mShadowBottomPadding = shadow.getPaddingBottom();
        mFixedPadding = 0;
    }

    @Override
    protected void setViewSize(Context context) {
        updatePadding(0, 0, 0, mFixedPadding);
        mFixedLRImageToScreenSpacing = getResources().getDimensionPixelSize(R.dimen.dp_20);
        imageWidth = ToolsDevice.getWindowPx(context).widthPixels - mFixedLRImageToScreenSpacing - mFixedLRImageToScreenSpacing;
        imageHeight = imageWidth * 320 / 750;
        contentHeight = imageHeight + mFixedPadding + mShadowTopPadding + mShadowBottomPadding;
        setTargetWH(contentWidth, contentHeight);
        setVisibility(VISIBLE);
        showMinH = Tools.getStatusBarHeight(context) + Tools.getDimen(context, R.dimen.topbar_height) - contentHeight;
        showMaxH = ToolsDevice.getWindowPx(getContext()).heightPixels - Tools.getDimen(context, R.dimen.dp_50);
    }

    @Override
    protected View getView(int position) {
        View view = mInflater.inflate(R.layout.widget_shadow_banner_item, null, true);
        RelativeLayout.LayoutParams lp = (LayoutParams) view.findViewById(R.id.shadow).getLayoutParams();
        lp.leftMargin = mFixedLRImageToScreenSpacing - mShadowLeftPadding;
        lp.rightMargin = mFixedLRImageToScreenSpacing - mShadowRightPadding;
        return view;
    }

    @Override
    protected int computeTopPadding() {
        return (mShowIndex != -1 && mShowIndex != 0) ? mFixedPadding - mShadowTopPadding : 0;
    }
}
