package amodule.main.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * 引导页
 * Created by sll on 2017/4/20.
 */

public class HomeGuidancePage extends RelativeLayout {
    private TextView mGuidanceBtn;
    private ImageView mGuidancePost;
    private ImageView mGuidanceHealth;
    private RelativeLayout mGuidanceRefresh;

//    测试数据
    View[] mViews = new View[3];
    private int mCurrentIndex = 0;
    private View mLastShowView;
    public HomeGuidancePage(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.home_guidancepage, this, true);
        initView();
    }

    public HomeGuidancePage(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.home_guidancepage, this, true);
        initView();
    }

    public HomeGuidancePage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.home_guidancepage, this, true);
        initView();
    }

    private void initView() {
        mGuidanceBtn = (TextView) findViewById(R.id.guidance_btn);
        mGuidancePost = (ImageView) findViewById(R.id.guidance_post);
        mGuidanceHealth = (ImageView) findViewById(R.id.guidance_health);
        mGuidanceRefresh = (RelativeLayout) findViewById(R.id.guidance_refresh);
        mViews[0] = mGuidanceHealth;
        mViews[1] = mGuidancePost;
        mViews[2] = mGuidanceRefresh;
        hide();
        addListener();
        next(mCurrentIndex);
    }

    private void addListener() {
        if (mGuidanceBtn != null)
            mGuidanceBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentIndex < (mViews.length - 1)) {
                        next(mCurrentIndex + 1);
                    } else {
                        hide();
                    }
                    ++mCurrentIndex;
                }
            });
    }

    private void next(int index) {
        if (index >= 0 && index < mViews.length) {
            if (mLastShowView != null)
                mLastShowView.setVisibility(View.GONE);
            mViews[index].setVisibility(View.VISIBLE);
            mLastShowView = mViews[index];
        }
    }

    public void show() {
        this.setVisibility(View.VISIBLE);
    }

    public void hide() {
        this.setVisibility(View.GONE);
        if (mFinishListener != null) {
            mViews = null;
            mFinishListener.onGuidancePageFinish();
        }
    }

    public boolean isShowing() {
        return this.getVisibility() == View.VISIBLE;
    }

    public interface OnGuidancePageFinishListener {
        public abstract void onGuidancePageFinish();
    }

    private OnGuidancePageFinishListener mFinishListener;
    public void setOnGuidancePageFinishListener(OnGuidancePageFinishListener finishListener) {
        mFinishListener = finishListener;
    }
}
