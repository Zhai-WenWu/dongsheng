package amodule._common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.xiangha.R;

public class ShadowBannerView extends BannerView {
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
    protected View getView(int position) {
        return mInflater.inflate(R.layout.widget_shadow_banner_item, null, true);
    }
}
