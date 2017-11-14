package amodule._common.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Description : //TODO
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:49.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class CountDownSubTitleView extends SubTitleView {

    protected TextView mTitle1;
    protected TextView mTitle2;
    protected TextView mTitle3;
    protected TextView mTitle4;

    private long mMillisInFuture;
    private long mMillisInterval;

    private CountDownTimer mCountDownTimer;

    public CountDownSubTitleView(Context context) {
        super(context);
    }

    public CountDownSubTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CountDownSubTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.countdown_subtitle_relativelayout, this);
    }

    @Override
    protected void initView() {
        mTitle1 = (TextView) findViewById(R.id.text1);
        mTitle2 = (TextView) findViewById(R.id.text2);
        mTitle3 = (TextView) findViewById(R.id.text3);
        mTitle4 = (TextView) findViewById(R.id.text4);
    }

    public void setTitle1Text(@Nullable CharSequence text) {
        if (mTitle1 != null) {
            mTitle1.setText(text);
        }
    }

    public void setTitle1Text(int resid) {
        if (mTitle1 != null) {
            mTitle1.setText(resid);
        }
    }

    public void setTitle1TextColor(int color) {
        if (mTitle1 != null) {
            mTitle1.setTextColor(color);
        }
    }

    public void startCountDownTime() {
        if (mMillisInFuture <= 0 || mMillisInterval <= 0 || mMillisInFuture < mMillisInterval || mCountDownTimer != null)
            return;
        mCountDownTimer = new CountDownTimer(mMillisInFuture, mMillisInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimeText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                mCountDownTimer = null;
            }
        };
        mCountDownTimer.start();
    }

    /**
     * 设置倒计时时间和间隔（ms）
     * @param millisInFuture 倒计时总时间
     * @param millisInterval 倒计时间隔
     */
    public void setCountDownTime(long millisInFuture, long millisInterval) {
        mMillisInFuture = millisInFuture;
        mMillisInterval = millisInterval;
    }

    private void setTimeText(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String formatTime = sdf.format(new Date(millis));

        Log.e("SLL", "formatTime = " + formatTime);
        if (TextUtils.isEmpty(formatTime))
            return;
        String[] times = formatTime.split(":");
        mTitle2.setText(times[0]);
        mTitle3.setText(times[1]);
        mTitle4.setText(times[2]);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startCountDownTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }
}
