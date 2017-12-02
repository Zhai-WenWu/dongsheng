package third.cling.control;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private Activity mActivity;

    private static final String TAG = ClingControl.class.getSimpleName();
    public static final String PLAY_URL = "play_url";
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

    private String mPlayUrl;
    private Handler mHandler = new InnerHandler();
    private BroadcastReceiver mTransportStateBroadcastReceiver;

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

    private static volatile ClingControl mInstance;

    private ClingControl(Activity activity) {
        mActivity = activity;
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
                clingUpnpServiceManager.setDeviceManager(new DeviceManager());

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

    public synchronized static ClingControl getInstance(Activity activity) {
        return mInstance == null ? (mInstance = new ClingControl(activity)) : mInstance;
    }

    public void onCreate() {
        addListener();
        bindServices();
        registerReceivers();
    }

    private void initView() {
        if (mDevicesPopup == null) {
            mDevicesPopup = new ClingDevicesPopup(mActivity);
            mDevicesPopup.setOnDeviceSelected(new OnDeviceSelectedListener() {
                @Override
                public void onDeviceSelected(ClingDevice device) {
                    initClingOptionView();
                    mClingOptionView.onTranstioning();
                    if (mListener != null)
                        mListener.onDeviceSelected(device);
                    play();
                }
            });
        }
    }

    private void addListener() {

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                if (mActivity != null)
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (mDevicesPopup != null)
                                mDevicesPopup.addDevice(device);
                        }
                    });
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                if (mActivity != null)
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (mDevicesPopup != null)
                                mDevicesPopup.removeDevice(device);
                        }
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

    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        // Unbind UPnP service
        mActivity.unbindService(mUpnpServiceConnection);
        // Unbind System service
        //        unbindService(mSystemServiceConnection);
        // UnRegister Receiver
        mActivity.unregisterReceiver(mTransportStateBroadcastReceiver);

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
        mInstance = null;
        mActivity = null;
    }

    public void onNewIntent(Intent intent) {
        if (mActivity == null)
            return;
        mActivity.setIntent(intent);
        initData();
    }

    private void initData() {
        Intent intent = mActivity.getIntent();
        if (intent != null && !TextUtils.isEmpty(intent.getStringExtra(PLAY_URL))) {
            String url = intent.getStringExtra(PLAY_URL);
            mPlayUrl = url.replace("https", "http");
        }
    }

    private void bindServices() {
        mServiceConnecting = true;
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(mActivity, ClingUpnpService.class);
        mActivity.bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        mActivity.registerReceiver(mTransportStateBroadcastReceiver, filter);
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
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(action_play);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(action_pause);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(action_stop);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(action_transtioning);
            }
        }
    }

    /**
     * 播放视频
     */
    private void play() {
        @DLANPlayState.DLANPlayStates int currentState = mClingPlayControl.getCurrentState();

        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */

        if (currentState == DLANPlayState.STOP) {
            mClingPlayControl.playNew(mPlayUrl, new ControlCallback() {

                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");

                    mHandler.sendEmptyMessage(action_succ);
                    ClingManager.getInstance().registerAVTransport(mActivity);
                    ClingManager.getInstance().registerRenderingControl(mActivity);
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
            mClingOptionView = new ClingOptionView(mActivity);
            mClingOptionView.setOnOptionListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClingOptionView.onTranstioning();
                    play();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stop();
                    mDevicesPopup.destroySelectedDevice();
                    if (mOnExitClickListener != null)
                        mOnExitClickListener.onClick(v);
                }
            });
        }
    }

    public void showPopup() {
        if (mServiceConnecting || mActivity.isFinishing() || !mActivity.hasWindowFocus()) {
            Toast.makeText(mActivity, "正在获取可投屏设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mHasConnected && !mConnectSucc) {
            Toast.makeText(mActivity, "投屏设备获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mDevicesPopup != null && mDevicesPopup.isShowing())
            return;
        initData();
        initView();
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            mDevicesPopup.clear();
            mDevicesPopup.addAll(devices);
        }
        View rootView = mActivity.findViewById(Window.ID_ANDROID_CONTENT);
        rootView.post(new Runnable() {
            @Override
            public void run() {
                if (mDevicesPopup != null && mActivity != null)
                    mDevicesPopup.showAtLocation(mActivity.findViewById(Window.ID_ANDROID_CONTENT), Gravity.BOTTOM, 0, 0);
            }
        });
    }

    public void setOnDeviceSelected(OnDeviceSelectedListener listener) {
        mListener = listener;
    }

    public void setOnExitClickListener(View.OnClickListener listener) {
        mOnExitClickListener = listener;
    }

}
