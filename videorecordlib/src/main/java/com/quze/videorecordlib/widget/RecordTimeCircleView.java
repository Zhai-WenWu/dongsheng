/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.quze.videorecordlib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 圆环时间轴
 */
public class RecordTimeCircleView extends View {
    private int maxDuration;
    private int minDuration;
    private CopyOnWriteArrayList<DrawInfo> clipDurationList = new CopyOnWriteArrayList<>();
    private DrawInfo currentClipDuration = new DrawInfo();
    private Paint paint;
    // 画圆环的画笔背景色
    private Paint mRingPaintBg;
    private int durationColor;
    private int selectColor;
    private int offsetColor;
    private int backgroundColor;
    private boolean isSelected = false;

    // 圆心x坐标
    private int mXCenter;
    // 圆心y坐标
    private int mYCenter;
    // 半径
    private float mRadius;
    // 圆环半径
    private float mRingRadius;
    // 圆环宽度
    private float mStrokeWidth;

    public RecordTimeCircleView(Context context) {
        super(context);
    }

    public RecordTimeCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public RecordTimeCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    //属性
    private void initAttrs(Context context, AttributeSet attrs) {
//        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
//                R.styleable.TasksCompletedView, 0, 0);
//        mRadius = typeArray.getDimension(R.styleable.TasksCompletedView_radius, 80);
//        mStrokeWidth = typeArray.getDimension(R.styleable.TasksCompletedView_strokeWidth, 10);
//        mRadius = 80;
//        mStrokeWidth =10;
//        mRingRadius = mRadius + mStrokeWidth / 2;
    }

    private void init(){
        if(paint == null){
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(mStrokeWidth);
        }
        //外圆弧背景
        if(mRingPaintBg == null){
            mRingPaintBg  = new Paint();
            mRingPaintBg.setAntiAlias(true);
            mRingPaintBg.setColor(0x88FFFFFF);
            mRingPaintBg.setStyle(Paint.Style.STROKE);
            mRingPaintBg.setStrokeWidth(mStrokeWidth);
            //mRingPaint.setStrokeCap(Paint.Cap.ROUND)
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init();
        if(backgroundColor != 0){
            canvas.drawColor(getResources().getColor(backgroundColor));
        }
        mXCenter = getWidth()/2;
        mYCenter = getHeight()/2;
        RectF oval = new RectF();
        oval.left = (mXCenter - mRingRadius);
        oval.top = (mYCenter - mRingRadius);
        oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
        oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);

        //外圆弧背景
        RectF oval1 = new RectF();
        oval1.left = (mXCenter - mRingRadius);
        oval1.top = (mYCenter - mRingRadius);
        oval1.right = mRingRadius * 2 + (mXCenter - mRingRadius);
        oval1.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
        canvas.drawArc(oval1, 0, 360, false, mRingPaintBg);

        int lastTotalDuration = 0;
        for(int i = 0 ;i < clipDurationList.size() ;i++){
            DrawInfo info = clipDurationList.get(i);
            switch (info.drawType){
                case OFFSET:
                    paint.setColor(getResources().getColor(offsetColor));
                    break;
                case DURATION:
                    paint.setColor(getResources().getColor(durationColor));
                    break;
                case SELECT:
                    paint.setColor(getResources().getColor(selectColor));
                    break;
            }
            canvas.drawArc(oval,lastTotalDuration/(float)maxDuration*360 - 90,(info.length) / (float)maxDuration*360,false,paint);
            lastTotalDuration += info.length;
        }
        if(currentClipDuration != null && currentClipDuration.length != 0){
            paint.setColor(getResources().getColor(durationColor));
            canvas.drawArc(oval,lastTotalDuration/(float)maxDuration*360 - 90,(currentClipDuration.length) / (float)maxDuration*360,false,paint);
        }
        if(lastTotalDuration + currentClipDuration.length < minDuration){
            paint.setColor(getResources().getColor(offsetColor));
            canvas.drawArc(oval,minDuration/(float)maxDuration*360 - 90,(maxDuration / 200) / (float)maxDuration*360,false,paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int h = this.getMeasuredHeight();
        int w = this.getMeasuredWidth();
        int curSquareDim = Math.min(w,h);
        setMeasuredDimension(curSquareDim, curSquareDim);
        mStrokeWidth =10;
        mRadius = curSquareDim/2 - 10;
        mRingRadius = mRadius + mStrokeWidth / 2;
    }

    public void clipComplete(){
        clipDurationList.add(currentClipDuration);
        DrawInfo info = new DrawInfo();
        info.length = maxDuration / 400;
        info.drawType = DrawType.OFFSET;
        clipDurationList.add(info);
        currentClipDuration = new DrawInfo();
        invalidate();
    }

    public void deleteLast(){
        if(clipDurationList.size() >= 2){
            clipDurationList.remove(clipDurationList.size() - 1);
            clipDurationList.remove(clipDurationList.size() - 1);
        }
        invalidate();
    }

    public void deteleAll(){
        clipDurationList.clear();
        invalidate();
    }

    public void selectLast(){
        if(clipDurationList.size() >= 2){
            DrawInfo info = clipDurationList.get(clipDurationList.size() - 2);
            info.drawType = DrawType.SELECT;
            invalidate();
            isSelected = true;
        }
    }

    public void cancleSelectLast(){
        if(clipDurationList.size() >= 2){
            DrawInfo info = clipDurationList.get(clipDurationList.size() - 2);
            info.drawType = DrawType.DURATION;
            invalidate();
            isSelected = false;
        }
    }


    public void setMaxDuration(int maxDuration){
        this.maxDuration = maxDuration;
    }

    public void setMinDuration(int minDuration){
        this.minDuration = minDuration;
    }

    public void setDuration(int duration){
        if(isSelected){
            for(DrawInfo info : clipDurationList){
                if(info.drawType == DrawType.SELECT){
                    info.drawType = DrawType.DURATION;
                    isSelected = false;
                    break;
                }
            }
        }
        this.currentClipDuration.drawType = DrawType.DURATION;
        this.currentClipDuration.length = duration;
        invalidate();
    }

    public void setColor(int duration,int select,int offset,int backgroundColor){
        this.durationColor = duration;
        this.selectColor = select;
        this.offsetColor = offset;
        this.backgroundColor = backgroundColor;

    }

    class DrawInfo{
        int length;
        DrawType drawType = DrawType.DURATION;
    }
    enum  DrawType{
        OFFSET,
        DURATION,
        SELECT
    }
}
