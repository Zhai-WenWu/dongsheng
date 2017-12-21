package amodule.lesson.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.StatisticCallback;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2017/12/19 15:43:13.
 * e_mail : ztanzeyu@gmail.com
 */
public class VIPButton extends TextView implements IStatisticCallback, View.OnClickListener {

    private StatisticCallback mStatisticCallback;
    private OnClickListener mClickListener;

    public VIPButton(Context context) {
        super(context);
        initialze();
    }

    public VIPButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialze();
    }

    public VIPButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialze();
    }

    private void initialze() {
        setGravity(Gravity.CENTER);
        setTextColor(Color.WHITE);
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setTextSize(30);
        setOnClickListener(this);
    }

    @Override
    public void setStatisticCallback(StatisticCallback callback) {
        if (null == callback) return;
        mStatisticCallback = callback;
    }

    public void onResume() {

    }

    public void onDestroy() {
        mStatisticCallback = null;
        mClickListener = null;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null)
            mClickListener.onClick(v);
        if (mStatisticCallback != null) {
            mStatisticCallback.onStatistic("", "", "", -1);
        }
    }

    public void setBtnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }
}
