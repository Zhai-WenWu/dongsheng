package third.cling.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by sll on 2017/11/28.
 */

public class ClingOptionView extends RelativeLayout implements View.OnClickListener {

    private TextView mTitleTextView;
    private LinearLayout mTryAgainLayout;
    private LinearLayout mExitLayout;

    private OnClickListener mOnTryClick;
    private OnClickListener mOnExitClick;

    public ClingOptionView(Context context) {
        this(context, null);
    }

    public ClingOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClingOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.cling_options_layout, this, true);

        initView();
        addListener();
    }

    private void addListener() {
        mTryAgainLayout.setOnClickListener(this);
        mExitLayout.setOnClickListener(this);
    }

    private void initView() {
        mTitleTextView = (TextView) findViewById(R.id.title);
        mTryAgainLayout = (LinearLayout) findViewById(R.id.try_again);
        mExitLayout = (LinearLayout) findViewById(R.id.exit_connection);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.try_again:
                if (mOnTryClick != null)
                    mOnTryClick.onClick(v);
                break;
            case R.id.exit_connection:
                if (mOnExitClick != null)
                    mOnExitClick.onClick(v);
                break;
        }
    }

    public void setOnOptionListener(OnClickListener tryAgainListener, OnClickListener exitListener) {
        mOnTryClick = tryAgainListener;
        mOnExitClick = exitListener;
    }

    public void onPlaying() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onTranstioning() {
        mTitleTextView.setText("正在连接…");
        mTitleTextView.setTextColor(getResources().getColor(R.color.c_white_text));
        if (mTryAgainLayout.getVisibility() == View.VISIBLE)
            mTryAgainLayout.setVisibility(View.GONE);
    }

    public void onError() {
        mTitleTextView.setText("连接失败");
        mTitleTextView.setTextColor(getResources().getColor(R.color.c_white_text));
        if (mTryAgainLayout.getVisibility() != View.VISIBLE)
            mTryAgainLayout.setVisibility(View.VISIBLE);
    }

    public void onSucc() {
        mTitleTextView.setText("投放中");
        mTitleTextView.setTextColor(Color.parseColor("#1db01d"));
        if (mTryAgainLayout.getVisibility() == View.VISIBLE)
            mTryAgainLayout.setVisibility(View.GONE);
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }
}
