package amodule._common.widget;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import acore.logic.AppCommon;
import acore.logic.stat.intefaces.OnClickListenerStat;
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
    private TextView mInvisibleText;

    private long mMillisInFuture;
    private long mMillisInterval = 1000;

    private boolean mDataReady;
    private boolean mIsAttachedToWindow;
    private boolean mIsDetachedFromWindow;
    private boolean mTaskRunning;

    private CountDownTimer mCountDownTimer;
    private SimpleDateFormat mSdf;

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
        mInvisibleText = findViewById(R.id.invisible_text);
    }

    @Override
    protected void onDataReady(Map<String, String> map) {
        Map<String, String> titleMap = StringManager.getFirstMap(map.get(WidgetDataHelper.KEY_TITLE));
        WidgetUtility.setTextToView(mTitle1,titleMap.get("text1"));
        WidgetUtility.setTextToView(mTitle3,titleMap.get("text2"));
        this.setTitle1ClickListener(v -> {
            String url1 = titleMap.get("url1");
            if (TextUtils.isEmpty(url1))
                return;
            AppCommon.openUrl((Activity) CountDownSubTitleView.this.getContext(), url1, true);
        });
        this.setTitle3ClickListener(v -> {
            String url2 = titleMap.get("url2");
            if (TextUtils.isEmpty(url2))
                return;
            AppCommon.openUrl((Activity) CountDownSubTitleView.this.getContext(), url2, true);
        });
        String millisInFuture = titleMap.get("endTime");
        if(TextUtils.isEmpty(millisInFuture)){
            return;
        }
        mMillisInFuture = (Integer.parseInt(millisInFuture) + 1) * 1000;
        if (mMillisInFuture <= 0) {
            return;
        }
        mDataReady = true;
        startCountDownTime();
    }

    public void startCountDownTime() {
        if (mTaskRunning || !mDataReady || !mIsAttachedToWindow || mIsDetachedFromWindow)
            return;
        if (mSdf == null) {
            mSdf = new SimpleDateFormat("DD:HH:mm:ss");
            mSdf.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
        }
        mCountDownTimer = new CountDownTimer(mMillisInFuture, mMillisInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mTaskRunning)
                    setTimeText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                setTimeText(0);
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
        String formatTime = mSdf.format(millis);
        if (TextUtils.isEmpty(formatTime))
            return;
        String[] times = formatTime.split(":");
        int day = Integer.parseInt(times[0]);
        String text = "";
        String invisibleText = "";
        if(1 >= day){
            text = times[1] + ":" + times[2] + ":" + times[3];
            invisibleText = "00:00:00";
        } else {
            text = (day - 1) + "天" + times[1] + ":" + times[2] + ":" + times[3];
            if (day - 1 > 9) {
                invisibleText = "00天00:00:00";
            } else {
                invisibleText = "0天00:00:00";
            }
        }
        WidgetUtility.setTextToView(mInvisibleText, invisibleText);
        WidgetUtility.setTextToView(mTitle2, text);
    }

    public void setViewVisibility(View view,int visibility){
        if(view == null || view.getVisibility() == visibility)
            return;
        view.setVisibility(visibility);
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

    public void setTitle1ClickListener(OnClickListener listener) {
        mTitle1.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });

    }

    public void setTitle3ClickListener(OnClickListener listener) {
        mTitle3.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }
}
