package acore.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.xiangha.R;

import acore.override.XHApplication;

/**
 * Created by ：fei_teng on 2016/11/8 15:58.
 */

public class UploadFailPopWindowDialog {

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private String mDishName;
    private String mDishPath;
    private int mDraftId;
    private LinearLayout ll_more_info;
    private UploadFailDialogCallback mCallback;

    public UploadFailPopWindowDialog(Context context, String dishName, String dishPath,
                                     int draftId,UploadFailDialogCallback callback) {
        mContext = context;
        mDishName = dishName;
        mDishPath = dishPath;
        mDraftId = draftId;
        mCallback = callback;
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mView = inflater.inflate(R.layout.d_popwindow_upload_fail, null);
        TextView tv_dish_name = (TextView) mView.findViewById(R.id.tv_dish_name);
        tv_dish_name.setText(mDishName);
        mView.setOnClickListener(onCloseListener);
        mView.findViewById(R.id.d_popwindow_close).setOnClickListener(onCloseListener);

        ImageView dish_cover = (ImageView) mView.findViewById(R.id.iv_dish_cover);
        Glide.with(XHApplication.in()).load(mDishPath).priority(Priority.IMMEDIATE)
                .error(R.drawable.mall_recommed_product_backgroup)
                .placeholder(R.drawable.mall_recommed_product_backgroup).crossFade()
                .into(dish_cover);

        ll_more_info = (LinearLayout) mView.findViewById(R.id.ll_more_info);
        ll_more_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.callback(mDraftId);
                closePopWindowDialog();
            }
        });
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        //设置window的type
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        else
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
//        mLayoutParams.flags = 0x40000000;
        //位置
        mLayoutParams.gravity = Gravity.CENTER;
    }


    public void show() {
        mView.setFocusable(true);
        mView.setFocusable(true);
        mView.setFocusableInTouchMode(true);
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_MENU:
                    case KeyEvent.KEYCODE_HOME:
                        closePopWindowDialog();
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

        mWindowManager.addView(mView, mLayoutParams);
    }


    private View.OnClickListener onCloseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            closePopWindowDialog();
        }
    };

    public void closePopWindowDialog() {
        if (mWindowManager != null) {
            if (mView != null)
                mWindowManager.removeView(mView);
            mWindowManager = null;
        }
    }


    /**
     * 获取当前分享Dialog是否还在显示
     *
     * @return true:显示 fase：不显示
     */
    public boolean isHasShow() {
        return mWindowManager != null;
    }

    public void onPause() {
        mView.setVisibility(View.GONE);
    }

    public void onResume() {
        mView.setVisibility(View.VISIBLE);
    }


    public interface UploadFailDialogCallback {
        void callback(int draftId);
    }

}
