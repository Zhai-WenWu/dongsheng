package com.popdialog.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mrtrying.xh_popdialog.R;
import com.popdialog.util.ImageManager;
import com.popdialog.util.ToolsDevice;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于显示广告的view
 *
 * @author Eva
 */
public class XHADView extends RelativeLayout {
    private static XHADView mADView;
    //必须为activity的context
    private Context mContext = null;
    private Activity mActivity = null;
    //创建广告是添加在此WindowManager中
    private WindowManager mWindowManager;
    //广告的ImageView
    private ImageView mADImage = null;
    //关闭广告
    private ImageView mClose = null;
    private View bgAnimView = null;
    private RelativeLayout animLayout = null;

    private boolean onceMeasure = false;
    private boolean onceAddWindow = false;
    private static boolean isClosed = false;
    private int mScreenHeight = 0;
    private boolean once = false;
    //计时器
    private Timer mTimer;
    final Handler handler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                if (msg.arg1 >= 0)
                    show(msg.arg1);
            } catch (Exception e) {
                Log.w("tzy", "" + e.getMessage());
            }
            return false;
        }
    });
    private BackRelativeLayout mLayout;
    private OnManualCloseStatisticsCallback onManualCloseStatisticsCallback;

    public static XHADView getInstence(Activity context) {
        if (isClosed) {
            return null;
        }
        if (mADView == null) {
            mADView = new XHADView(context);
        }
        return mADView;
    }

    public XHADView(Context context) {
        this(context, null);
    }

    public XHADView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XHADView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //初始化默认的mContext
        this.mContext = context;

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!once) {
                    once = true;
                    WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    windowManager.getDefaultDisplay().getMetrics(outMetrics);
                    mScreenHeight = outMetrics.heightPixels;
                }
            }
        });

        initUI(context);
    }

    //添加对应的UI
    private void initUI(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_full_screen_ad, this);
        mClose = (ImageView) findViewById(R.id.close_ad);
        mADImage = (ImageView) findViewById(R.id.image_ad);
        bgAnimView = findViewById(R.id.anim_view);
        animLayout = (RelativeLayout) findViewById(R.id.anim_layout);

        mClose.setClickable(true);
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (onManualCloseStatisticsCallback != null) {
                    onManualCloseStatisticsCallback.onManualClose();
                }
            }
        });
    }

    /**
     * 必须初始化的方法
     *
     * @param delay
     * @param displayTime
     */
    public void initTimer(int delay, final int displayTime) {
        if (delay < 0 || displayTime < 0) {
            return;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.arg1 = displayTime;
                handler.sendMessage(msg);
            }
        };
        mTimer = new Timer(true);
        mTimer.schedule(task, delay);
        //timer.cancel(); //退出计时器
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!onceMeasure) {
            this.getLayoutParams().height = mScreenHeight;
            onceMeasure = true;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //添加到WindowManager中，并显示
    private void show(int displayTime) {
        //displayTime = 0 不显示
        if (displayTime == 0) {
            return;
        }
        isClosed = false;
        if (mActivity != null) {
            if (mActivity.isFinishing()) {//mActivity.isDestroyed() API-level-17
                return;
            }
            mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams windowLayoutParas = new WindowManager.LayoutParams();
            windowLayoutParas.type = WindowManager.LayoutParams.TYPE_APPLICATION;
            windowLayoutParas.format = PixelFormat.RGBA_8888;

            if (!onceAddWindow) {
                mLayout = new BackRelativeLayout(mContext);
                mLayout.setOnBackListener(new BackRelativeLayout.OnBackListener() {
                    @Override
                    public void onBack(View v) {
                        if (onManualCloseStatisticsCallback != null) {
                            onManualCloseStatisticsCallback.onManualClose();
                        }
                    }
                });
                mLayout.addView(mADView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mWindowManager.addView(mLayout, windowLayoutParas);
                onceAddWindow = true;
            }
            setVisibility(View.VISIBLE);
            AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
            alpha.setDuration(500);
            bgAnimView.startAnimation(alpha);
            Animation animStart = AnimationUtils.loadAnimation(getContext(), R.anim.translate_start);
            animLayout.startAnimation(animStart);
            //不自动关闭
//			new Handler().postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					 hide();
//				}
//			}, displayTime);
        }
    }

    //隐藏并销毁
    public void hide() {
        if (!isClosed) {
            isClosed = true;
        }
        try {
            if (isClosed) {
                onDestroy();
            }
        } catch (Exception ignored) {

        }
    }

    private void onDestroy() {
        this.setVisibility(View.GONE);
        if (mWindowManager != null
                && mActivity != null
                && !mActivity.isFinishing()
                && mLayout != null) {
            mWindowManager.removeView(mLayout);
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        mADView = null;
    }

    /**
     * 刷新自己所持有的context，以此来刷新显示之后所依附的Activity
     *
     * @param act
     */
    public void refreshContext(Activity act) {
        this.mActivity = act;
    }

    /**
     * 设置ADImage的click事件
     *
     * @param clickListener
     */
    public void setADClickListener(OnClickListener clickListener) {
        if (mADImage != null && clickListener != null) {
            mADImage.setOnClickListener(clickListener);
        }
    }

    /**
     * 设置ADImage的图片
     *
     * @param bitmap
     */
    public void setImage(Bitmap bitmap) {
        if (mADImage != null && bitmap != null) {
            int newWaith = ToolsDevice.getWindowPx(getContext()).widthPixels - (int) getContext().getResources().getDimension(R.dimen.dp_47) * 2;
            ImageManager.setImgViewByWH(mADImage, bitmap, newWaith, 0, false);
        }
    }

    public interface OnManualCloseStatisticsCallback {
        void onManualClose();
    }

	/*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public OnManualCloseStatisticsCallback getOnManualCloseStatisticsCallback() {
        return onManualCloseStatisticsCallback;
    }

    public void setOnManualCloseStatisticsCallback(OnManualCloseStatisticsCallback onManualCloseStatisticsCallback) {
        this.onManualCloseStatisticsCallback = onManualCloseStatisticsCallback;
    }
}
