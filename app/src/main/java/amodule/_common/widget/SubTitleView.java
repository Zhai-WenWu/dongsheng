package amodule._common.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Description : //TODO
 * PackageName : amodule._common.widget.horizontal
 * Created by MrTrying on 2017/11/13 15:49.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class SubTitleView extends RelativeLayout {

    protected TextView mTitle1;
    protected TextView mTitle2;
    public SubTitleView(Context context) {
        super(context);
        inflateLayout(context);
    }

    public SubTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateLayout(context);
    }

    public SubTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
    }

    protected void inflateLayout(Context context) {
        inflate(context, R.layout.subtitle_relativelayout, this);
    }

    protected void initView() {
        mTitle1 = (TextView) findViewById(R.id.text1);
        mTitle2 = (TextView) findViewById(R.id.text2);
    }

    public void setTitle1ClickListener(OnClickListener listener) {
        if (mTitle1 != null && listener != null) {
            mTitle1.setOnClickListener(listener);
        }
    }

    public void setTitle2ClickListener(OnClickListener listener) {
        if (mTitle2 != null && listener != null) {
            mTitle2.setOnClickListener(listener);
        }
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

    public void setTitle2Text(@Nullable CharSequence text) {
        if (mTitle2 != null) {
            mTitle2.setText(text);
        }
    }

    public void setTitle2Text(int resid) {
        if (mTitle2 != null) {
            mTitle2.setText(resid);
        }
    }

    public void setTitle2TextColor(int color) {
        if (mTitle2 != null) {
            mTitle2.setTextColor(color);
        }
    }
}
