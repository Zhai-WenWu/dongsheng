package amodule.main.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.xianghatest.R;

/**
 * Created by sll on 2017/4/28.
 */

public class ReplayAndShareView extends RelativeLayout {

    private RelativeLayout mReplayLayout;
    private RelativeLayout mShareLayout;

    public ReplayAndShareView(Context context) {
        super(context);
        initView();
    }

    public ReplayAndShareView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ReplayAndShareView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.replay_share_layout, this, true);
        mReplayLayout = (RelativeLayout) findViewById(R.id.video_replay);
        mShareLayout = (RelativeLayout) findViewById(R.id.video_share);
        addListener();
    }

    private void addListener() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.video_replay:
                        if (mReplayClickListener != null)
                            mReplayClickListener.onReplayClick();
                        break;
                    case R.id.video_share:
                        if (mShareClickListener != null)
                            mShareClickListener.onShareClick();
                        break;
                    default:
                        break;
                }
            }
        };
        if (mReplayLayout != null)
            mReplayLayout.setOnClickListener(onClickListener);
        if (mShareLayout != null)
            mShareLayout.setOnClickListener(onClickListener);
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    public interface OnReplayClickListener {
        void onReplayClick();
    }

    private OnReplayClickListener mReplayClickListener;
    public void setOnReplayClickListener(OnReplayClickListener replayClickListener) {
        mReplayClickListener = replayClickListener;
    }

    public interface OnShareClickListener {
        void onShareClick();
    }

    private OnShareClickListener mShareClickListener;
    public void setOnShareClickListener(OnShareClickListener shareClickListener) {
        mShareClickListener = shareClickListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
