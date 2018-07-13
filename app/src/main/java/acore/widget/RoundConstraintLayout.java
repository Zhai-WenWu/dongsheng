package acore.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

import com.xiangha.R;


public class RoundConstraintLayout extends ConstraintLayout {

    /**
     * view四个圆角对应的半径大小
     */
    private float topLeftRadius = 0;
    private float topRightRadius = 0;
    private float bottomLeftRadius = 0;
    private float bottomRightRadius = 0;

    private Path roundedPath = null;

    public RoundConstraintLayout(Context context) {
        this(context,null,0);
    }
    public RoundConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public RoundConstraintLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundStyle);
        if (ta.hasValue(R.styleable.RoundStyle_android_radius)) {
            float radius = ta.getDimensionPixelSize(R.styleable.RoundStyle_android_radius, 0);
            if (radius >= 0) {
                topLeftRadius = radius;
                topRightRadius = radius;
                bottomLeftRadius = radius;
                bottomRightRadius = radius;
            }
            ta.recycle();
            return;
        }
        topLeftRadius = ta.getDimensionPixelSize(R.styleable.RoundStyle_android_topLeftRadius, 0);
        topRightRadius = ta.getDimensionPixelSize(R.styleable.RoundStyle_android_topRightRadius, 0);
        bottomLeftRadius = ta.getDimensionPixelSize(R.styleable.RoundStyle_android_bottomLeftRadius, 0);
        bottomRightRadius = ta.getDimensionPixelSize(R.styleable.RoundStyle_android_bottomRightRadius, 0);
        ta.recycle();
    }

    public void setRadius(float radius) {
        if (radius >= 0) {
            setRadius(radius, radius, radius, radius);
        }
    }

    public void setRadius(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        if (topLeftRadius >= 0) {
            this.topLeftRadius = topLeftRadius;
        }
        if (topRightRadius >= 0) {
            this.topRightRadius = topRightRadius;
        }
        if (bottomLeftRadius >= 0) {
            this.bottomLeftRadius = bottomLeftRadius;
        }
        if (bottomRightRadius >= 0) {
            this.bottomRightRadius = bottomRightRadius;
        }
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        updateRoundedPath();
        if (null != roundedPath) {
            canvas.clipPath(roundedPath);
        }
        super.dispatchDraw(canvas);
    }

    private void updateRoundedPath() {
        roundedPath = new Path();
        roundedPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()),
                new float[]{topLeftRadius, topLeftRadius, topRightRadius, topRightRadius,
                        bottomLeftRadius, bottomLeftRadius, bottomRightRadius, bottomRightRadius},
                Path.Direction.CW);
    }
}