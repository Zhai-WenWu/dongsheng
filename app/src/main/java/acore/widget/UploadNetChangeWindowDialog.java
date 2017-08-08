package acore.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * Created by ：fei_teng on 2016/11/15 21:22.
 */

public class UploadNetChangeWindowDialog {

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private UploadNetChangeWindowDialog.NetChangeCallback mCallback;

    public UploadNetChangeWindowDialog(Context context, UploadNetChangeWindowDialog.NetChangeCallback callback) {
        mContext = context;
        mCallback = callback;
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mView = inflater.inflate(R.layout.c_net_changed_upload, null);
        TextView dialog_message = (TextView) mView.findViewById(R.id.dialog_message);

        dialog_message.setText("您现在不是WiFi，还继续上传视频菜谱吗？");

        TextView dialog_sure = (TextView) mView.findViewById(R.id.dialog_sure);
        dialog_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClickSure();
                closePopWindowDialog();
            }
        });


        TextView dialog_negative = (TextView) mView.findViewById(R.id.dialog_negative);
        dialog_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClickNegative();
                closePopWindowDialog();
            }
        });

        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
//        //设置window的type
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //位置
        mLayoutParams.gravity = Gravity.CENTER;
    }


    public void show() {
        mWindowManager.addView(mView, mLayoutParams);
    }


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


    public interface NetChangeCallback {

        void onClickSure();

        void onClickNegative();


    }

}
