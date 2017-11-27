package amodule.answer.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xh.view.base.BaseView;
import com.xiangha.R;

/**
 * 上传转圈的弹框View
 * Created by sll on 2017/10/31.
 */

public class UploadingView extends BaseView {

    private TextView mTitleView;
    public UploadingView(Context context) {
        super(context, R.layout.uploadingview_layout);
    }

    public UploadingView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.uploadingview_layout);
    }

    public UploadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.uploadingview_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UploadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.uploadingview_layout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mTitleView = (TextView) findViewById(R.id.msg);
    }

    public UploadingView setText(@Nullable CharSequence text) {
        mTitleView.setText(text);
        return this;
    }

    public UploadingView setText(int resId) {
        mTitleView.setText(resId);
        return this;
    }
}
