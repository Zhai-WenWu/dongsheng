package third.cling.control;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.Collection;
import java.util.Map;

import third.cling.entity.ClingDevice;
import third.cling.entity.ClingDeviceList;
import third.cling.entity.DLANPlayState;
import third.cling.entity.IDevice;
import third.cling.service.manager.ClingManager;
import third.cling.ui.ClingDevicesPopup;
import third.cling.ui.ClingOptionView;

/**
 * Created by sll on 2017/11/28.
 */

public class ClingControl {

    private Context mContext;

    private static final String TAG = ClingControl.class.getSimpleName();
    private ClingOptionView mClingOptionView;
    private View.OnClickListener mOnExitClickListener;

    private ClingDevicesPopup mDevicesPopup;
    private OnDeviceSelectedListener mListener;

    private Handler mMainHandler;
    private ClingRunnable mRun;

    private String mPlayUrl;

    public ClingControl(Context context) {
        mContext = context;
        mMainHandler = new InnerHandler(Looper.getMainLooper());
        mRun = new ClingRunnable() {
            @Override
            public void run(Message msg) {
                mMainHandler.sendMessage(msg);
            }
        };
    }

    public void onCreate() {
        ClingPresenter.getInstance().onCreate(mContext, mRun);
        initView();
    }

    private void initView() {
        if (mDevicesPopup == null) {
            mDevicesPopup = new ClingDevicesPopup(mContext);
            mDevicesPopup.setOnDeviceSelected(device -> {
                initClingOptionView();
                mClingOptionView.onTranstioning();
                if (mListener != null)
                    mListener.onDeviceSelected(device);
                ClingPresenter.getInstance().play(mContext);
            });
        }
    }

    public void onStart() {
        ClingPresenter.getInstance().onStart();
    }

    public void onResume(Context context) {
        ClingPresenter.getInstance().onResume(context);
    }

    public void onPause() {
        ClingPresenter.getInstance().onPause();
    }

    public void onStop() {
        ClingPresenter.getInstance().onStop();
    }

    public void onDestroy(Context context) {
       //YLKLog.i(TAG, "onDestroy_context = " + context);
        ClingPresenter.getInstance().onDestroy(context);
        if (mDevicesPopup != null)
            mDevicesPopup.destroyPopup();
        mDevicesPopup = null;
        mClingOptionView = null;
        mListener = null;
        mOnExitClickListener = null;
        mContext = null;
    }

    private final class InnerHandler extends Handler {

        public InnerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object obj = msg.obj;
            if (obj != null && obj instanceof Map) {
                String url = ((Map<String, String>)obj).get("url");
                if (!TextUtils.equals(url, mPlayUrl))
                    return;
            }
            
           //YLKLog.i(TAG, "handleMessage_msg = " + msg.toString());
            
            switch (msg.what) {
                case ClingPresenter.action_play://正在投放
                   //YLKLog.i(TAG, "Execute action_play");
                    ClingPresenter.getInstance().setCurrentState(DLANPlayState.PLAY);
                    if (mClingOptionView != null)
                        mClingOptionView.onPlaying();
                    break;
                case ClingPresenter.action_pause:
                   //YLKLog.i(TAG, "Execute action_pause");
                    ClingPresenter.getInstance().setCurrentState(DLANPlayState.PAUSE);
                    if (mClingOptionView != null)
                        mClingOptionView.onPause();
                    break;
                case ClingPresenter.action_stop:
                   //YLKLog.i(TAG, "Execute action_stop");
                    ClingPresenter.getInstance().setCurrentState(DLANPlayState.STOP);
                    if (mClingOptionView != null)
                        mClingOptionView.onStop();
                    break;
                case ClingPresenter.action_transtioning://正在连接
                   //YLKLog.i(TAG, "Execute action_transtioning");
                    if (mClingOptionView != null)
                        mClingOptionView.onTranstioning();
                    break;
                case ClingPresenter.action_error://投放失败
                   //YLKLog.i(TAG, "Execute action_error");
                    if (mClingOptionView != null)
                        mClingOptionView.onError();
                    break;
                case ClingPresenter.action_succ://投放成功
                   //YLKLog.i(TAG, "Execute action_succ");
                    if (mClingOptionView != null)
                        mClingOptionView.onSucc();
                    break;
                case ClingPresenter.action_add_device://添加设备
                   //YLKLog.i(TAG, "Execute action_add_device");
                    if (mDevicesPopup != null && obj != null && obj instanceof IDevice)
                        mDevicesPopup.addDevice((IDevice) obj);
                    break;
                case ClingPresenter.action_remove_device://删除设备
                   //YLKLog.i(TAG, "Execute action_remove_device");
                    Object obj2 = msg.obj;
                    if (mDevicesPopup != null && obj2 != null && obj2 instanceof IDevice)
                        mDevicesPopup.removeDevice((IDevice) obj2);
                    break;
            }
        }
    }

    public ClingOptionView getClingOptionView() {
        initClingOptionView();
        return mClingOptionView;
    }

    private void initClingOptionView() {
        if (mContext == null)
            return;
        if (mClingOptionView == null) {
            mClingOptionView = new ClingOptionView(mContext);
            mClingOptionView.setOnOptionListener(v -> {
                mClingOptionView.onTranstioning();
                ClingPresenter.getInstance().play(mContext);
            }, v -> {
                ClingPresenter.getInstance().stop();
                destroySelectedDevice();
                if (mOnExitClickListener != null)
                    mOnExitClickListener.onClick(v);
            });
        }
    }

    public void destroySelectedDevice() {
        if (mDevicesPopup != null)
            mDevicesPopup.destroySelectedDevice();
    }

    public void setOnDeviceSelected(OnDeviceSelectedListener listener) {
        mListener = listener;
    }

    public void setOnExitClickListener(View.OnClickListener listener) {
        mOnExitClickListener = listener;
    }

    public void setPlayUrl (String playUrl) {
        if (TextUtils.isEmpty(playUrl))
            return;
        mPlayUrl = playUrl.replace("https", "http");
        ClingPresenter.getInstance().setPlayUrl(mContext, playUrl);
    }


    public void showPopup(Context context) {
        if (context == null || !(context instanceof Activity))
            return;
        Activity activity = (Activity) context;
        if (ClingPresenter.getInstance().isServiceConnecting() || activity.isFinishing() || !activity.hasWindowFocus()) {
            Toast.makeText(context, "正在获取可投屏设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ClingPresenter.getInstance().isHasConnected() && !ClingPresenter.getInstance().isConnectSucc()) {
            Toast.makeText(context, "投屏设备获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mDevicesPopup != null && mDevicesPopup.isShowing())
            return;
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            mDevicesPopup.clear();
            mDevicesPopup.addAll(devices);
        }
        View rootView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        rootView.post(() -> {
            if (mDevicesPopup != null && activity != null) {
                mDevicesPopup.setContext(context);
                mDevicesPopup.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
            }
        });
    }

}
