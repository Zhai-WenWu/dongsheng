package amodule.dish.video.View;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.xiangha.R;

import java.text.DecimalFormat;

import acore.tools.Tools;

import static acore.tools.Tools.getDimen;

/**
 *
 */
public class RangeSeekBarView extends ImageView {
    private int lineTop, lineBottom, lineLeft, lineRight;
    private int lineCorners;
    private int lineWidth;
    private Paint paint=new Paint();
    private RectF line = new RectF();
    public static final int TEXT_LATERAL_PADDING_IN_DP = 3;
    private final Bitmap thumbImage_1 = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal_1);
    private final Bitmap thumbPressedImage_1 = BitmapFactory.decodeResource(getResources(),
            R.drawable.seek_thumb_pressed_1);
    private final Bitmap thumbDisabledImage_1 = BitmapFactory.decodeResource(getResources(),
            R.drawable.seek_thumb_normal_1);
    private final Bitmap thumbImage_2 = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal_2);
    private final Bitmap thumbPressedImage_2 = BitmapFactory.decodeResource(getResources(),
            R.drawable.seek_thumb_pressed_2);
    private final Bitmap thumbDisabledImage_2 = BitmapFactory.decodeResource(getResources(),
            R.drawable.seek_thumb_normal_2);
    private int w;
    private Context context;
    private SeekBar leftSB = new SeekBar();
    private SeekBar rightSB = new SeekBar();
    private SeekBar currTouch;
    private float max= 0;
    private int heightRect;
    private float mTextSize;
    private double left_percent=0;
    private double right_percent=1;
    private float min=2;
    private float progress;
    private float start=0, end=1;
    private int select_position=0;
    private boolean isTouchState= true;//false：当前down，true为up
    private float x_progress;
    public RangeSeekBarView(Context context) {
        super(context);
        init(context);
    }

    public RangeSeekBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public RangeSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context= context;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);
        paint.setColor( Color.parseColor("#666666"));
        heightRect= getDimen(context, R.dimen.dp_30);
        mTextSize= getDimen(context,R.dimen.dp_13);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        this.w=widthSize;
        lineLeft = 0+getDimen(context,R.dimen.dp_29)/2;
        lineRight = w- getDimen(context,R.dimen.dp_29)/2;
        lineTop = heightRect;
        lineBottom = heightRect+getDimen(context,R.dimen.dp_8);
        lineWidth = lineRight - lineLeft;
        line.set(lineLeft, lineTop, lineRight, lineBottom);
        lineCorners = (int) ((lineBottom - lineTop) * 0.45f);
        int butY=heightRect- getDimen(context,R.dimen.dp_4);
        leftSB.onSizeChanged(0,butY,getDimen(context,R.dimen.dp_29),getDimen(context,R.dimen.dp_43),w,false,thumbImage_1,context);
        rightSB.onSizeChanged(w-getDimen(context,R.dimen.dp_29),butY,getDimen(context,R.dimen.dp_29),getDimen(context,R.dimen.dp_43),w,false,thumbImage_2,context);

//        if (heightSize * 1.8f > widthSize) {
//            setMeasuredDimension(widthSize, (int) (widthSize / 1.8f));
//        } else {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor( Color.parseColor("#666666"));
        canvas.drawRoundRect(line, lineCorners, lineCorners, paint);

        //绘制中心区域
        RectF dsts = new RectF();
        float x_start= (float) (left_percent*(lineWidth - rightSB.widthSize));
        leftSB.slideX(x_start);

        float x_end= (float) (right_percent*(lineWidth - rightSB.widthSize));
        rightSB.slideX(x_end+rightSB.widthSize);
        Log.i("zhangyujian",":leftSB.left:"+leftSB.left+"::rightSB.right::"+rightSB.right);
        dsts.left = leftSB.left+getDimen(context,R.dimen.dp_29)/2;
        dsts.top =heightRect;
        dsts.right = rightSB.right-getDimen(context,R.dimen.dp_29)/2;
        dsts.bottom = heightRect+getDimen(context,R.dimen.dp_8);
        paint.setColor( Color.parseColor("#fb6652"));
        canvas.drawRoundRect(dsts,lineCorners, lineCorners,paint);

        //对绘制图标
        leftSB.draw(canvas);
        rightSB.draw(canvas);

        int offset =  dpToPx(getContext(), TEXT_LATERAL_PADDING_IN_DP);

        paint.setTextSize(mTextSize);
        paint.setColor(Color.parseColor("#fffffe"));
        DecimalFormat df = new DecimalFormat("######0.0");

        if(left_percent!=0&& max>0) {
            String left = df.format(start= (float) (left_percent * max))+"s";
            float minTextWidth = paint.measureText(left) + offset;
            canvas.drawText(left, leftSB.left + getDimen(context, R.dimen.dp_29) / 2-minTextWidth*0.5f, getDimen(context, R.dimen.dp_15), paint);
        }
        if(right_percent!=1&&max>0) {
            String right = df.format(end= (float) (right_percent * max))+"s";
            float maxTextWidth = paint.measureText(right) + offset;
            canvas.drawText(right, rightSB.left + getDimen(context, R.dimen.dp_29) / 2-maxTextWidth*0.5f, getDimen(context, R.dimen.dp_15), paint);
        }
        //接口回调
        if(numberCallBack!=null&&max>0)
            numberCallBack.getstartAndEndValue(start,end,isTouchState,select_position);

        //绘制进行\
        RectF mRect = new RectF();
        paint.setColor(Color.parseColor("#90ffffff"));
        mRect.left = leftSB.right- Tools.getDimen(context,R.dimen.dp_29)/2;
        mRect.top = heightRect;
        mRect.bottom = heightRect + getDimen(context, R.dimen.dp_8);
        if(x_progress!=0&&x_progress>(leftSB.right- Tools.getDimen(context,R.dimen.dp_29)/2)) {
            mRect.right = x_progress;
            canvas.drawRect(mRect, paint);
        }else if(x_progress==0){
            mRect.right=leftSB.right- Tools.getDimen(context,R.dimen.dp_29)/2;
            canvas.drawRect(mRect, paint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isTouchState=false;
                boolean touchResult = false;
                if (rightSB.currPercent >= 1 && leftSB.collide(event)) {
                    currTouch = leftSB;
                    touchResult = true;
                } else if (rightSB.collide(event)) {
                    currTouch = rightSB;
                    touchResult = true;
                } else if (leftSB.collide(event)) {
                    currTouch = leftSB;
                    touchResult = true;
                }
                return touchResult;
            case MotionEvent.ACTION_MOVE:
                isTouchState=false;
                float percent;
                float x = event.getX();
                if (currTouch == leftSB) {
                    percent = x  * 1f / (lineWidth - rightSB.widthSize);
//                    if(percent<=0)percent=0;
                    if(right_percent-percent<=progress){
                        left_percent=right_percent-progress;
                        x= (float) ((lineWidth - rightSB.widthSize)*left_percent);
                    }else
                        left_percent = percent;

//                    Log.i("zhangyujian",":x:"+left_percent+"::::"+right_percent);
//                    Log.i("zhangyujian",":x:"+x);
                    if(left_percent>=0)
                        leftSB.slideX(x);
                    if(left_percent<=0)left_percent=0;
                } else if (currTouch == rightSB) {
                    percent = (x - leftSB.widthSize) * 1f / (lineWidth - leftSB.widthSize);
//                    if(percent>=1)percent=1;
                    if(percent-left_percent<=progress){
                        right_percent=left_percent+progress;
                        x= (float) ((lineWidth - rightSB.widthSize)*right_percent)+leftSB.widthSize;
                    }else
                        right_percent=percent;
//                    Log.i("zhangyujian","percent::"+percent);
                    if(right_percent<=1)
                        rightSB.slideX(x);
                    if(right_percent>=1)right_percent=1;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                currTouch.materialRestore();
                isTouchState=true;
                break;
        }
        return true;
    }

    public void setStartAndEndIndex(float startValue,float endValue){
        if(startValue>=0&&endValue>0)return;
        this.start=startValue;
        this.end = endValue;
        float startProgerss=0;
        if(start>0)startProgerss= start/max;
        float endProgerss=1;
        if(end<=max) endProgerss= end/max;

        left_percent=startProgerss;
        right_percent=endProgerss;

        float x_start=startProgerss*(lineWidth - rightSB.widthSize);
        leftSB.slideX(x_start);
        float x_end=endProgerss*(lineWidth - rightSB.widthSize);
        rightSB.slideX(x_end+rightSB.widthSize);

        invalidate();

    }

    @Override
    public void invalidate() {
        super.invalidate();
        Log.i("zhangyujian",":刷新view:");
    }
    public void setAllTime(float alltime, int select_position){
        this.select_position=select_position;
        this.max=alltime;
        progress=min/max;
        setStartAndEndIndex(start,end);
    }

    /**
     * 配置当前参数
     * @param alltime
     * @param select_position
     * @param startValue
     * @param endValue
     */
    public void setTimeParams(float alltime, int select_position,float startValue,float endValue){
        this.select_position=select_position;
        this.max=alltime;
        progress=min/max;
        this.start=startValue;
        this.end = endValue;
        float startProgerss=0;
        if(start>0)startProgerss= start/max;
        float endProgerss=1;
        if(end<=max) endProgerss= end/max;

        left_percent=startProgerss;
        right_percent=endProgerss;

//        float x_start=startProgerss*(lineWidth - rightSB.widthSize);
//        leftSB.slideX(x_start);
//        float x_end=endProgerss*(lineWidth - rightSB.widthSize);
//        rightSB.slideX(x_end+rightSB.widthSize);
        invalidate();
    }
    public void setProgressVideo(float nowTime){
        Log.i("zhangyujian", "nowTime:"+nowTime);
        if(nowTime>0) {
            float now_progress = nowTime / max;
            x_progress = now_progress * (lineWidth + rightSB.widthSize);
        }else if(nowTime==-1){
            x_progress = lineWidth+rightSB.widthSize;
        }else{
            this.x_progress=0;
        }
        invalidate();
    }

    private class SeekBar {
        RadialGradient shadowGradient;
        int lineWidth;
        int widthSize, heightSize;
        float currPercent;
        int left, right, top, bottom;
        Bitmap bmp;

        float material = 0;
        ValueAnimator anim;

        void onSizeChanged(int centerX, int centerY, int waithB, int heightB, int parentLineWidth, boolean cellsMode, Bitmap bitmap, Context context) {
            SeekBar.this.bmp = bitmap;
            SeekBar.this.heightSize= heightB;
            SeekBar.this.widthSize= waithB;
            SeekBar.this.left = centerX ;
            SeekBar.this.right = centerX + widthSize ;
            SeekBar.this.top = centerY ;
            SeekBar.this.bottom = centerY + heightSize;

            if (cellsMode) {
                lineWidth = parentLineWidth;
            } else {
                lineWidth = parentLineWidth - widthSize;
            }

        }

        boolean collide(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            int offset = (int) (lineWidth * currPercent);
            return x > left + offset && x < right + offset && y > top && y < bottom;
        }

        void slide(float percent) {
            if (percent < 0) percent = 0;
            else if (percent > 1) percent = 1;
            currPercent = percent;
        }
        void slideX(float x){
            SeekBar.this.left= (int) x;
            SeekBar.this.right= (int) (x+widthSize);
        }


        void draw(Canvas canvas) {
            canvas.save();
            Rect dsts = new Rect();
            dsts.left = left;
            dsts.top =top;
            dsts.right = right;
            dsts.bottom = bottom;
            paint.setFilterBitmap(true);
            canvas.drawBitmap(bmp, null,dsts,paint);
            canvas.restore();
        }


        private void materialRestore() {
            if (anim != null) anim.cancel();
            anim = ValueAnimator.ofFloat(material, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    material = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    material = 0;
                    invalidate();
                }
            });
            anim.start();
        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return true;
    }
    public static int dpToPx(Context context, int dp) {
        int px = Math.round(dp * getPixelScaleFactor(context));
        return px;
    }
    private static float getPixelScaleFactor(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * 滑动数值变化
     */
    public interface NumberCallBack{
        public void getstartAndEndValue(float startValue,float endValue,boolean isTouchState,int position);
    }
    private NumberCallBack numberCallBack;
    public void setNumberCallBack(NumberCallBack numberCallBack){
        this.numberCallBack=numberCallBack;
    }

}
