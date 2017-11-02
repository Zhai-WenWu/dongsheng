package acore.logic.load.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xh.view.base.BaseView;
import com.xiangha.R;

/**
 * Dialog显示的加载转圈的View
 * Created by sll on 2017/10/31.
 */

public class LoadingView extends BaseView {

    private TextView mTitle;
    public LoadingView(Context context) {
        super(context, R.layout.loadingview_layout);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.loadingview_layout);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.loadingview_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.loadingview_layout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mTitle = (TextView) findViewById(R.id.dialog_title);
    }

    public LoadingView setText(@Nullable CharSequence text) {
        mTitle.setText(text);
        mTitle.setVisibility(View.VISIBLE);
        return this;
    }

    public LoadingView setText(int resId) {
        mTitle.setText(resId);
        mTitle.setVisibility(View.VISIBLE);
        return this;
    }
}
