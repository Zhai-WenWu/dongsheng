package aplug.player;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.xiangha.R;

public class ShortVideoPlayer extends StandardGSYVideoPlayer {

    public ShortVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public ShortVideoPlayer(Context context) {
        super(context);
    }

    public ShortVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_shortvideo_detail;
    }

    @Override
    protected boolean parentHandleBottomProgressBarEnable() {
        return false;
    }
}