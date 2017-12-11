package third.cling.control;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import third.cling.config.Intents;
import third.cling.control.callback.ControlCallback;
import third.cling.entity.ClingDevice;
import third.cling.entity.ClingDeviceList;
import third.cling.entity.DLANPlayState;
import third.cling.entity.IDevice;
import third.cling.entity.IResponse;
import third.cling.listener.BrowseRegistryListener;
import third.cling.listener.DeviceListChangedListener;
import third.cling.service.ClingUpnpService;
import third.cling.service.manager.ClingManager;
import third.cling.service.manager.DeviceManager;
import third.cling.ui.ClingDevicesPopup;
import third.cling.ui.ClingOptionView;

/**
 * Created by sll on 2017/11/28.
 */

public class ClingControl {

    private Context mRootContext;

    private static final String TAG = ClingControl.class.getSimpleName();
    /**
     * 连接设备状态: 播放状态
     */
    private final int action_play = 0xa1;
    /**
     * 连接设备状态: 暂停状态
     */
    private final int action_pause = 0xa2;
    /**
     * 连接设备状态: 停止状态
     */
    private final int action_stop = 0xa3;
    /**
     * 连接设备状态: 转菊花状态
     */
    private final int action_transtioning = 0xa4;
    /**
     * 获取进度
     */
    private final int action_position_info = 0xa5;
    /**
     * 投放失败
     */
    private final int action_error = 0xa5;
    private final int action_succ = 0xa6;

    private Handler mHandler = new InnerHandler();

    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();

    /**
     * 用于监听发现设备
     */
    private BrowseRegistryListener mBrowseRegistryListener;

    private ServiceConnection mUpnpServiceConnection;

    private ClingDevicesPopup mDevicesPopup;
    private ClingOptionView mClingOptionView;
    private OnDeviceSelectedListener mListener;
    private View.OnClickListener mOnExitClickListener;

    private boolean mServiceConnecting;
    private boolean mHasConnected;
    private boolean mConnectSucc;
    private boolean mServiceBind;

    private Map<Context, String> mDataMap;//Context 代表Activity, String 表示当前Activity播放的url

    private static volatile ClingControl mInstance;

    private ClingControl(Context context) {
        mRootContext = context;
        mDataMap = new HashMap<>();
        mBrowseRegistryListener = new BrowseRegistryListener();
        mUpnpServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.e(TAG, "mUpnpServiceConnection onServiceConnected");
                mServiceConnecting = false;
                mHasConnected = true;
                mConnectSucc = true;
                ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
                ClingUpnpService beyondUpnpService = binder.getService();

                ClingManager clingUpnpServiceManager = ClingManager.getInstance();
                clingUpnpServiceManager.setUpnpService(beyondUpnpService);
                DeviceManager manager = new DeviceManager();
                manager.setActionCallback(action -> {
                    if (TextUtils.isEmpty(action))
                        return;
                    switch (action) {
                        case Intents.ACTION_PLAYING:
                            mHandler.sendEmptyMessage(action_play);
                            break;
                        case Intents.ACTION_PAUSED_PLAYBACK:
                            mHandler.sendEmptyMessage(action_pause);
                            break;
                        case Intents.ACTION_STOPPED:
                            mHandler.sendEmptyMessage(action_stop);
                            break;
                        case Intents.ACTION_TRANSITIONING:
                            mHandler.sendEmptyMessage(action_transtioning);
                            break;
                    }
                });
                clingUpnpServiceManager.setDeviceManager(manager);

                clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
                //Search on service created.
                clingUpnpServiceManager.searchDevices();

            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");
                mServiceConnecting = false;
                mHasConnected = true;
                mConnectSucc = false;
                ClingManager.getInstance().setUpnpService(null);
            }
        };
    }

    public synchronized static ClingControl getInstance(Context context) {
        synchronized (ClingControl.class) {
            if (mInstance == null)
                mInstance = new ClingControl(context);
        }
        return mInstance;
    }

    public void onCreate(Context context) {
        mDataMap.put(context, "");
        addListener();
        bindServices();
    }

    private void initView() {
        if (mDevicesPopup == null) {
            mDevicesPopup = new ClingDevicesPopup(mRootContext);
            mDevicesPopup.setOnDeviceSelected(device -> {
                initClingOptionView();
                mClingOptionView.onTranstioning();
                if (mListener != null)
                    mListener.onDeviceSelected(device);
                play(mDevicesPopup.getContext());
            });
        }
    }

    private void addListener() {

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                mHandler.post(() -> {
                    if (mDevicesPopup != null)
                        mDevicesPopup.addDevice(device);
                });
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                mHandler.post(() -> {
                    if (mDevicesPopup != null)
                        mDevicesPopup.removeDevice(device);
                });
            }
        });
    }

    public void onStart() {
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onDestroy(Context context) {
        if (context == null)
            return;
        if (context != mRootContext) {
            mDataMap.remove(context);
            return;
        }
        Log.e(TAG, "onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        // Unbind UPnP service
        if (mServiceBind)
            mRootContext.unbindService(mUpnpServiceConnection);

        ClingManager.getInstance().destroy();
        ClingDeviceList.getInstance().destroy();
        if (mDevicesPopup != null)
            mDevicesPopup.destroyPopup();
        mDevicesPopup = null;
        mClingOptionView = null;
        mListener = null;
        mOnExitClickListener = null;
        mServiceConnecting = false;
        mHasConnected = false;
        mConnectSucc = false;
        mServiceBind = false;
        mInstance = null;
        mRootContext = null;
    }

    private void bindServices() {
        if (!mServiceBind) {
            mServiceConnecting = true;
            // Bind UPnP service
            Intent upnpServiceIntent = new Intent(mRootContext, ClingUpnpService.class);
            mRootContext.bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
            mServiceBind = true;
        }
    }

    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case action_play://正在投放
                    Log.i(TAG, "Execute action_play");
                    mClingPlayControl.setCurrentState(DLANPlayState.PLAY);
                    if (mClingOptionView != null)
                        mClingOptionView.onPlaying();
                    break;
                case action_pause:
                    Log.i(TAG, "Execute action_pause");
                    mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);
                    if (mClingOptionView != null)
                        mClingOptionView.onPause();
                    break;
                case action_stop:
                    Log.i(TAG, "Execute action_stop");
                    mClingPlayControl.setCurrentState(DLANPlayState.STOP);
                    if (mClingOptionView != null)
                        mClingOptionView.onStop();
                    break;
                case action_transtioning://正在连接
                    Log.i(TAG, "Execute action_transtioning");
                    if (mClingOptionView != null)
                        mClingOptionView.onTranstioning();
                    break;
                case action_error://投放失败
                    Log.e(TAG, "Execute action_error");
                    if (mClingOptionView != null)
                        mClingOptionView.onError();
                    break;
                case action_succ://投放成功
                    Log.e(TAG, "Execute action_succ");
                    if (mClingOptionView != null)
                        mClingOptionView.onSucc();
                    break;
            }
        }
    }

    /**
     * 播放视频
     */
    private void play(Context context) {
        @DLANPlayState.DLANPlayStates int currentState = mClingPlayControl.getCurrentState();

        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */
        String url = mDataMap.get(context);
        if (TextUtils.isEmpty(url))
            return;
        if (currentState == DLANPlayState.STOP) {
            mClingPlayControl.playNew(url, new ControlCallback() {

                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");

                    mHandler.sendEmptyMessage(action_succ);
                    ClingManager.getInstance().registerAVTransport(mRootContext);
                    ClingManager.getInstance().registerRenderingControl(mRootContext);
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(action_error);
                }
            });
        } else {
            mClingPlayControl.play(new ControlCallback() {
                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                    mHandler.sendEmptyMessage(action_succ);
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(action_error);
                }
            });
        }
    }

    /**
     * 暂停
     */
    private void pause() {
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    /**
     * 停止
     */
    private void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "stop success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "stop fail");
            }
        });
    }

    public ClingOptionView getClingOptionView() {
        initClingOptionView();
        return mClingOptionView;
    }

    private void initClingOptionView() {
        if (mClingOptionView == null) {
            mClingOptionView = new ClingOptionView(mRootContext);
            mClingOptionView.setOnOptionListener(v -> {
                mClingOptionView.onTranstioning();
                play(mClingOptionView.getCurrContext());
            }, v -> {
                stop();
                mDevicesPopup.destroySelectedDevice();
                if (mOnExitClickListener != null)
                    mOnExitClickListener.onClick(v);
            });
        }
    }

    public void showPopup(Context context) {
        if (context == null || !(context instanceof Activity))
            return;
        Activity activity = (Activity) context;
        if (mServiceConnecting || activity.isFinishing() || !activity.hasWindowFocus()) {
            Toast.makeText(context, "正在获取可投屏设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mHasConnected && !mConnectSucc) {
            Toast.makeText(context, "投屏设备获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mDevicesPopup != null && mDevicesPopup.isShowing())
            return;
        initView();
        initClingOptionView();
        mClingOptionView.setContext(context);
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

    public void setOnDeviceSelected(OnDeviceSelectedListener listener) {
        mListener = listener;
    }

    public void setOnExitClickListener(View.OnClickListener listener) {
        mOnExitClickListener = listener;
    }

    public void setPlayUrl (Context context, String playUrl) {
        if (context == null || TextUtils.isEmpty(playUrl))
            return;
        mDataMap.put(context, playUrl.replace("https", "http"));
    }

}
