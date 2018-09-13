package acore.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

/**
 * PackageName : com.mrtrying.tagtextview.widget
 * Created by MrTrying on 2016/7/18 13:55.
 * E_mail : ztanzeyu@gmail.com
 */
@SuppressLint("AppCompatCustomView")
public class TagTextView extends TextView {

    private static final int[] NATIVE_ATTRIBUTE = new int[]{android.R.attr.drawableLeft};

    private int mDrawableLeftW, mDrawableLeftH;
    /** 所有角 */
    private int mRadius = 0;
    /** 左上角 */
    private int mTopLeftRadius = 0;
    /** 右上角 */
    private int mTopRighttRadius = 0;
    /** 左下角 */
    private int mBottomLeftRadius = 0;
    /** 右下角 */
    private int mBottomRighttRadius = 0;
    /** 边框宽 */
    private int mSideWidth = 0;
    /** 边框虚线宽 */
    private int mSideDashWidth = 0;
    /** 边框虚线间隔 */
    private int mSideDashGap = 0;
    /** 边框颜色 */
    private int mSideColor;
    /** 背景颜色 */
    private int mBackgroundColor;

    private int mDrawableLeftRes;
    /***/
    GradientDrawable gradientDrawable;

    Drawable mDrawableL,mDrawableT,mDrawableR,mDrawableB;

    public TagTextView(Context context) {
        this(context, null);
    }

    public TagTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化原生属性
        initializeNativeAttrs(context, attrs);
        //初始化tagtext属性
        initializeTagTextAttrs(context, attrs);

        //初始化drawable
        initDrawable();
        //默认设置文字剧中显示
//		setGravity(Gravity.CENTER);
    }

    private void initializeNativeAttrs(Context context, AttributeSet attrs) {
        TypedArray nativeTypedArray = context.obtainStyledAttributes(attrs,NATIVE_ATTRIBUTE);
        for(int index = 0 ; index < NATIVE_ATTRIBUTE.length ; index++){
            switch (index){
                case 0:
                    mDrawableLeftRes = nativeTypedArray.getResourceId(index,0);
                    break;
                    default:break;
            }
        }
        nativeTypedArray.recycle();
    }

    private void initializeTagTextAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagTextView);
        mRadius = typedArray.getDimensionPixelSize(R.styleable.TagTextView_radius, mRadius);
        mTopLeftRadius = typedArray.getDimensionPixelSize(R.styleable.TagTextView_topLeftRadius, mRadius);
        mTopRighttRadius = typedArray.getDimensionPixelSize(R.styleable.TagTextView_topRightRadius, mRadius);
        mBottomLeftRadius = typedArray.getDimensionPixelSize(R.styleable.TagTextView_bottomLeftRadius, mRadius);
        mBottomRighttRadius = typedArray.getDimensionPixelSize(R.styleable.TagTextView_bottomRightRadius, mRadius);
        mSideWidth = typedArray.getDimensionPixelSize(R.styleable.TagTextView_sideWidth, mSideWidth);
        mSideDashWidth = typedArray.getDimensionPixelSize(R.styleable.TagTextView_sideDashWidth, mSideDashWidth);
        mSideDashGap = typedArray.getDimensionPixelSize(R.styleable.TagTextView_sideDashGap, mSideDashGap);
        mSideColor = typedArray.getColor(R.styleable.TagTextView_sideColor, mSideColor);
        mBackgroundColor = typedArray.getColor(R.styleable.TagTextView_backgroundColor, mBackgroundColor);
        mDrawableLeftW = typedArray.getDimensionPixelSize(R.styleable.TagTextView_drawableLeftWidth,mDrawableLeftW);
        mDrawableLeftH = typedArray.getDimensionPixelSize(R.styleable.TagTextView_drawableLeftHeight,mDrawableLeftH);
        typedArray.recycle();
    }

    /** 初始化drawable */
    public void initDrawable() {
        //初始化背景
        if (gradientDrawable == null) {
            gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(mBackgroundColor);
            gradientDrawable.setCornerRadius(mRadius);
            float[] radii = new float[]{mTopLeftRadius, mTopLeftRadius,
                    mTopRighttRadius, mTopRighttRadius,
                    mBottomRighttRadius, mBottomRighttRadius,
                    mBottomLeftRadius, mBottomLeftRadius};
            gradientDrawable.setCornerRadii(radii);
            gradientDrawable.setStroke(mSideWidth, mSideColor, mSideDashWidth, mSideDashGap);
            setBackgroundDrawable(gradientDrawable);
        }
        //初始化左侧
        initializeDrawableLeft();
        setCompoundDrawables(mDrawableL, null, null, null);
    }

    private void initializeDrawableLeft() {
        if(mDrawableLeftRes > 0 && mDrawableLeftW > 0 && mDrawableLeftH > 0){
            mDrawableL = getResources().getDrawable(mDrawableLeftRes);
            mDrawableL.setBounds(0, 0, mDrawableLeftW, mDrawableLeftH);
        }
    }

    /**
     * 设置所有圆角
     *
     * @param radius
     */
    public void setRadius(int radius) {
        this.mRadius = radius;
        this.mTopLeftRadius = radius;
        this.mTopRighttRadius = radius;
        this.mBottomLeftRadius = radius;
        this.mBottomRighttRadius = radius;
        setRadii(getRadii());
    }

    /**
     * 设置↖圆角
     *
     * @param radius
     */
    public void setTopLeftRadius(int radius) {
        this.mTopLeftRadius = radius;
        setRadii(getRadii());
    }

    /**
     * 设置↙圆角
     *
     * @param radius
     */
    public void setTopRighttRadius(int radius) {
        this.mTopRighttRadius = radius;
        setRadii(getRadii());
    }

    /**
     * 设置↗圆角
     *
     * @param radius
     */
    public void setBottomLeftRadius(int radius) {
        this.mBottomLeftRadius = radius;
        setRadii(getRadii());
    }

    /**
     * 设置↘圆角
     *
     * @param radius
     */
    public void setBottomRighttRadius(int radius) {
        this.mBottomRighttRadius = radius;
        setRadii(getRadii());
    }

    /**
     * 设置边框线宽
     *
     * @param mSideWidth
     */
    public void setSideWidth(int mSideWidth) {
        this.mSideWidth = mSideWidth;
        setStroke();
    }

    /**
     * 设置边框线虚线宽
     *
     * @param mSideDashWidth
     */
    public void setSideDashWidth(int mSideDashWidth) {
        this.mSideDashWidth = mSideDashWidth;
        setStroke();
    }

    /**
     * 设置边框线虚线间隔宽
     *
     * @param mSideDashGap
     */
    public void setSideDashGap(int mSideDashGap) {
        this.mSideDashGap = mSideDashGap;
        setStroke();
    }

    /**
     * 设置边框线颜色
     *
     * @param mSideColor
     */
    public void setSideColor(int mSideColor) {
        this.mSideColor = mSideColor;
        setStroke();
    }

    /**
     * 设置背景颜色
     *
     * @param mBackgroundColor
     */
    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
        gradientDrawable.setColor(mBackgroundColor);
        setBackgroundDrawable(gradientDrawable);
    }

    /**
     * 设置圆角
     *
     * @param radii
     */
    public void setRadii(float[] radii) {
        gradientDrawable.setCornerRadii(radii);
        setBackgroundDrawable(gradientDrawable);
    }

    /**
     * 设置边框
     */
    private void setStroke() {
        gradientDrawable.setStroke(mSideWidth, mSideColor, mSideDashWidth, mSideDashGap);
        setBackgroundDrawable(gradientDrawable);
    }

    public void setDrawableL(int drawableLeftRes){
        this.mDrawableLeftRes = drawableLeftRes;
        initializeDrawableLeft();
        setCompoundDrawables(mDrawableL, null, null, null);
    }

    public void setDrawableL(Drawable drawableL){
        mDrawableL = drawableL;
        if(mDrawableL != null && mDrawableLeftW > 0 && mDrawableLeftH > 0){
            mDrawableL.setBounds(0, 0, mDrawableLeftW, mDrawableLeftH);
        }
        setCompoundDrawables(mDrawableL, null, null, null);
    }

    public void setDrawableLeftSize(int width,int height){
        if(width > 0){
            mDrawableLeftW = width;
        }
        if(height > 0){
            mDrawableLeftH = height;
        }
        if(mDrawableL != null && mDrawableLeftW > 0 && mDrawableLeftH > 0){
            mDrawableL.setBounds(0, 0, mDrawableLeftW, mDrawableLeftH);
        }
        setCompoundDrawables(mDrawableL, null, null, null);
    }

    /**
     * @return
     */
    private float[] getRadii() {
        return new float[]{mTopLeftRadius, mTopLeftRadius,
                mTopRighttRadius, mTopRighttRadius,
                mBottomRighttRadius, mBottomRighttRadius,
                mBottomLeftRadius, mBottomLeftRadius};
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            gradientDrawable = null;
            setBackgroundDrawable(null);
        } else {
            initDrawable();
            setBackgroundDrawable(gradientDrawable);
        }
    }
}
