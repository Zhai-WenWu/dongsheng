package amodule._common.widget;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiangha.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import acore.tools.StringManager;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule._common.widget.baseview.BaseSubTitleView;

/**
 * Description :
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:49.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class CountDownSubTitleView extends BaseSubTitleView {

    private TextView mTitle1;
    private TextView mTitle2;
    private TextView mTitle3;
    private TextView mTitle4;

    private long mMillisInFuture;
    private long mMillisInterval = 1000;

    private boolean mDataReady;
    private boolean mIsAttachedToWindow;
    private boolean mIsDetachedFromWindow;
    private boolean mTaskRunning;

    private CountDownTimer mCountDownTimer;

    public CountDownSubTitleView(Context context) {
        super(context, R.layout.countdown_subtitle_relativelayout);
    }

    public CountDownSubTitleView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.countdown_subtitle_relativelayout);
    }

    public CountDownSubTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.countdown_subtitle_relativelayout);
    }

    @Override
    protected void initView() {
        mTitle1 = (TextView) findViewById(R.id.text1);
        mTitle2 = (TextView) findViewById(R.id.text2);
        mTitle3 = (TextView) findViewById(R.id.text3);
        mTitle4 = (TextView) findViewById(R.id.text4);
    }

    @Override
    protected void onDataReady(Map<String, String> map) {
        Map<String, String> titleMap = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_TITLE));
        WidgetUtility.setTextToView(mTitle1,titleMap.get("text1"));
        String millisInFuture = titleMap.get("endTime");
        if(TextUtils.isEmpty(millisInFuture)){
            findViewById(R.id.time_layout).setVisibility(GONE);
            return;
        }
        mMillisInFuture = Integer.parseInt(millisInFuture) * 1000;
        mDataReady = true;
        startCountDownTime();
    }

    public void startCountDownTime() {
        if (mTaskRunning || !mDataReady || !mIsAttachedToWindow || mIsDetachedFromWindow)
            return;
        setTimeText(mMillisInFuture);
        mCountDownTimer = new CountDownTimer(mMillisInFuture, mMillisInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mTaskRunning)
                    setTimeText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                endCountDownTime();
            }
        };
        mTaskRunning = true;
        mCountDownTimer.start();
    }

    public void endCountDownTime() {
        if (mCountDownTimer == null)
            return;
        mTaskRunning = false;
        mCountDownTimer.cancel();
        mCountDownTimer = null;
    }

    /**
     * 设置倒计时时间和间隔（ms）
     * @param millisInFuture 倒计时总时间
     * @param millisInterval 倒计时间隔
     */
    private void setCountDownTime(long millisInFuture, long millisInterval) {
        mMillisInFuture = millisInFuture;
        mMillisInterval = millisInterval;
    }

    private void setTimeText(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String formatTime = sdf.format(new Date(millis));
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
        mIsAttachedToWindow = true;
        mIsDetachedFromWindow = false;
        startCountDownTime();
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsDetachedFromWindow = true;
        mIsAttachedToWindow = false;
        if (((Activity)getContext()).isFinishing()) {
            endCountDownTime();
        }
    }

    String id,twoLevel,threeLevel;
    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel =twoLevel;
        this.threeLevel =threeLevel;
    }
}
