package aplug.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.xiangha.R;

public class ShortVideoPlayer extends StandardGSYVideoPlayer {

    private ImageView mPlayBtn;

    public ShortVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public ShortVideoPlayer(Context context) {
        super(context);
    }

    public ShortVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mNeedShowWifiTip = false;
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mPlayBtn = findViewById(R.id.start_btn);
        mPlayBtn.setOnClickListener(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_shortvideo_detail;
    }

    @Override
    protected boolean parentHandleBottomProgressBarEnable() {
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_btn) {
            changePlayBtnState(false);
            handleStartClick();
        } else {
            super.onClick(v);
        }
    }

    public void changePlayBtnState(boolean show) {
        mPlayBtn.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public boolean playBtnVisible() {
        return mPlayBtn.getVisibility() == View.VISIBLE;
    }
}