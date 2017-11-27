package amodule.dish.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xh.view.base.BaseView;
import com.xiangha.R;

/**
 * 带标题进度条的弹窗View
 * Created by sll on 2017/11/1.
 */

public class TitleProgressView extends BaseView {

    private TextView mTitle;
    private ProgressBar mProgressBar;
    public TitleProgressView(Context context) {
        super(context, R.layout.dialog_progresslayout);
    }

    public TitleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.dialog_progresslayout);
    }

    public TitleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.dialog_progresslayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TitleProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.dialog_progresslayout);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        mTitle = (TextView) findViewById(R.id.dialog_message);
        mProgressBar = (ProgressBar) findViewById(R.id.load_progress);
    }

    public TitleProgressView setTitle(@Nullable CharSequence title) {
        mTitle.setText(title);
        return this;
    }

    public TitleProgressView setTitle(int resId) {
        mTitle.setText(resId);
        return this;
    }

    public TitleProgressView setProgress(int progress) {
        mProgressBar.setProgress(progress);
        mProgressBar.setVisibility(View.VISIBLE);
        return this;
    }

}
