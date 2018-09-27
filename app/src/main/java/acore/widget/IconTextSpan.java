package acore.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;

public class IconTextSpan extends ReplacementSpan {

    private Context mContext;
    private int mBgColorInt; //Icon背景颜色
    private String mText;  //Icon内文字
    private float mBgHeight;  //Icon背景高度
    private float mBgWidth;  //Icon背景宽度
    private float mRadius;  //Icon圆角半径
    private float mRightMargin; //右边距
    private float mTextSize; //文字大小

    private int mTextColorInt; //文字颜色

    public IconTextSpan(Context context, String text, float textSize) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        //初始化默认数值
        initDefaultValue(context, text, textSize);
        //计算背景的宽度
        this.mBgWidth = caculateBgWidth(text);
    }

    /**
     * 初始化默认数值
     *
     * @param context
     */
    private void initDefaultValue(Context context, String text, float textSize) {
        this.mContext = context.getApplicationContext();
        this.mText = text;
        this.mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, mContext.getResources().getDisplayMetrics());
    }

    /**
     * 计算icon背景宽度
     *
     * @param text icon内文字
     */
    private float caculateBgWidth(String text) {
        if (text.length() >= 1) {
            //多字，宽度=文字宽度+padding
            Rect textRect = new Rect();
            Paint paint = new Paint();
            paint.setTextSize(mTextSize);
            paint.getTextBounds(text, 0, text.length(), textRect);
            float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
            return textRect.width() + padding * 2;
        } else
            return 0;
    }

    public void setBgColorInt(int colorInt) {
        mBgColorInt = colorInt;
    }


    public void setTextColorInt(int textColorInt) {
        mTextColorInt = textColorInt;
    }

    public void setRadius(float radius) {
        this.mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, mContext.getResources().getDisplayMetrics());
    }

    public void setBgHeight(float bgHeight) {
        this.mBgHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bgHeight, mContext.getResources().getDisplayMetrics());
    }

    /**
     * 设置右边距
     *
     * @param rightMargin
     */
    public void steRightMargin(float rightMargin) {
        this.mRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightMargin, mContext.getResources().getDisplayMetrics());
    }

    /**
     * 设置宽度，宽度=背景宽度+右边距
     */
    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) (mBgWidth + mRightMargin);
    }

    /**
     * draw
     * @param text 完整文本
     * @param start setSpan里设置的start
     * @param end setSpan里设置的start
     * @param x
     * @param top 当前span所在行的上方y
     * @param y y其实就是metric里baseline的位置
     * @param bottom 当前span所在行的下方y(包含了行间距)，会和下一行的top重合
     * @param paint 使用此span的画笔
     */
    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        //画背景
        Paint bgPaint = new Paint();
        bgPaint.setColor(mBgColorInt);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        Paint.FontMetrics metrics = paint.getFontMetrics();

        float textHeight = metrics.descent - metrics.ascent;
        //算出背景开始画的y坐标
        float bgStartY = y + (textHeight - mBgHeight) / 2 + metrics.ascent;

        //画背景
        RectF bgRect = new RectF(x, bgStartY, x + mBgWidth, bgStartY + mBgHeight);
        canvas.drawRoundRect(bgRect, mRadius, mRadius, bgPaint);

        //把字画在背景中间
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(mTextColorInt);
        textPaint.setTextSize(mTextSize);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);  //这个只针对x有效
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textRectHeight = fontMetrics.bottom - fontMetrics.top;
        canvas.drawText(mText, x + mBgWidth / 2, bgStartY + (mBgHeight - textRectHeight) / 2 - fontMetrics.top, textPaint);
    }

    public static class Builder {
        private static Params p;

        public Builder() {
            p = new Params();
        }

        public Builder setBgColorInt(int bgColorInt) {
            p.mBgColorInt = bgColorInt;
            return this;
        }

        public Builder setBgHeight(float bgHeight) {
            p.mBgHeight = bgHeight;
            return this;
        }

        public Builder setRadius(float radius) {
            p.mRadius = radius;
            return this;
        }

        public Builder setRightMargin(float rightMargin) {
            p.mRightMargin = rightMargin;
            return this;
        }

        public Builder setTextSize(float textSize) {
            p.mTextSize = textSize;
            return this;
        }

        public Builder setText(String text) {
            p.mText = text;
            return this;
        }

        public Builder setTextColorInt(int textColorInt) {
            p.mTextColorInt = textColorInt;
            return this;
        }

        public IconTextSpan build(Context context) {
            IconTextSpan span = new IconTextSpan(context, p.mText, p.mTextSize);
            span.setBgColorInt(p.mBgColorInt);
            span.steRightMargin(p.mRightMargin);
            span.setTextColorInt(p.mTextColorInt);
            span.setRadius(p.mRadius);
            span.setBgHeight(p.mBgHeight);
            return span;
        }
    }

    private static class Params {
        private int mBgColorInt; //Icon背景颜色
        private String mText;  //Icon内文字
        private float mBgHeight;  //Icon背景高度
        private float mRadius;  //Icon圆角半径
        private float mRightMargin; //右边距
        private float mTextSize; //文字大小
        private int mTextColorInt; //文字颜色
    }
}
