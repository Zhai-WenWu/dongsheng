package amodule.topic.style;

import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class CustomClickableSpan extends ClickableSpan {

    private @ColorInt int mTextColor;
    private boolean mHasUnderline = true;

    private View.OnClickListener mOnClickListener;
    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
    }

    public void setHasUnderline(boolean hasUnderline) {
        mHasUnderline = hasUnderline;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mTextColor);
        ds.setUnderlineText(mHasUnderline);
    }

    @Override
    public void onClick(View widget) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(widget);
        }
    }
}
