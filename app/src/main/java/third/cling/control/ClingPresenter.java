package third.cling.control;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import third.cling.config.Intents;
import third.cling.control.callback.ControlCallback;
import third.cling.entity.ClingDeviceList;
import third.cling.entity.DLANPlayState;
import third.cling.entity.IDevice;
import third.cling.entity.IResponse;
import third.cling.listener.BrowseRegistryListener;
import third.cling.listener.DeviceListChangedListener;
import third.cling.service.ClingUpnpService;
import third.cling.service.manager.ClingManager;
import third.cling.service.manager.DeviceManager;

/**
 * Created by sll on 2017/12/12.
 */

public class ClingPresenter {
    private static volatile ClingPresenter mInstance;
    private static final String TAG = ClingPresenter.class.getSimpleName();
    /**
     * 连接设备状态: 播放状态
     */
    public static final int action_play = 0xa1;
    /**
     * 连接设备状态: 暂停状态
     */
    public static final int action_pause = 0xa2;
    /**
     * 连接设备状态: 停止状态
     */
    public static final int action_stop = 0xa3;
    /**
     * 连接设备状态: 转菊花状态
     */
    public static final int action_transtioning = 0xa4;
    /**
     * 获取进度
     */
    public static final int action_position_info = 0xa5;
    /**
     * 投放失败
     */
    public static final int action_error = 0xa5;
    public static final int action_succ = 0xa6;

    public static final int action_add_device = 0xa7;
    public static final int action_remove_device = 0xa8;

    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();

    /**
     * 用于监听发现设备
     */
    private BrowseRegistryListener mBrowseRegistryListener;

    private ServiceConnection mUpnpServiceConnection;
    private Map<Context, String> mDataMap;//Context 代表Activity, String 表示当前Activity播放的url
    private boolean mServiceConnecting;
    private boolean mHasConnected;
    private boolean mConnectSucc;
    private boolean mServiceBind;

    private Context mRootContext;
    private Context mCurrContext;

    private Map<Context, ClingRunnable> mRunsMap;
    private String mPlayUrl;

    private ClingPresenter() {
        mRunsMap = new HashMap<>();
        mDataMap = new HashMap<>();
        mBrowseRegistryListener = new BrowseRegistryListener();
        mUpnpServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
               //YLKLog.i(TAG, "mUpnpServiceConnection onServiceConnected");
                mServiceConnecting = false;
                mHasConnected = true;
                mConnectSucc = true;
                if (!(service instanceof ClingUpnpService.LocalBinder))
                    return;
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
                           //YLKLog.i(TAG, "action_play");
                            Message msg = new Message();
                            Map<String, String> map = new HashMap<>();
                            map.put("url", mPlayUrl);
                            msg.obj = map;
                            msg.what = action_play;
                            sendMessage(msg);
                            break;
                        case Intents.ACTION_PAUSED_PLAYBACK:
                           //YLKLog.i(TAG, "action_pause");
                            Message msg2 = new Message();
                            Map<String, String> map2 = new HashMap<>();
                            map2.put("url", mPlayUrl);
                            msg2.obj = map2;
                            msg2.what = action_pause;
                            sendMessage(msg2);
                            break;
                        case Intents.ACTION_STOPPED:
                           //YLKLog.i(TAG, "action_stop");
                            Message msg3 = new Message();
                            Map<String, String> map3 = new HashMap<>();
                            map3.put("url", mPlayUrl);
                            msg3.obj = map3;
                            msg3.what = action_stop;
                            sendMessage(msg3);
                            break;
                        case Intents.ACTION_TRANSITIONING:
                           //YLKLog.i(TAG, "action_transtioning");
                            Message msg4 = new Message();
                            Map<String, String> map4 = new HashMap<>();
                            map4.put("url", mPlayUrl);
                            msg4.obj = map4;
                            msg4.what = action_transtioning;
                            sendMessage(msg4);
                            break;
                    }
                });
                clingUpnpServiceManager.setDeviceManager(manager);

                if(clingUpnpServiceManager.getRegistry()!=null) {
                    clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
                }
                //Search on service created.
                clingUpnpServiceManager.searchDevices();

            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
               //YLKLog.i(TAG, "mUpnpServiceConnection onServiceDisconnected");
                mServiceConnecting = false;
                mHasConnected = true;
                mConnectSucc = false;
                ClingManager.getInstance().setUpnpService(null);
            }
        };
    }

    public static ClingPresenter getInstance() {
        synchronized (ClingPresenter.class) {
            if (mInstance == null)
                mInstance = new ClingPresenter();
            return mInstance;
        }
    }

    public void onCreate(Context context, ClingRunnable runnable) {
        if (mRootContext == null)
            mRootContext = context;
        mCurrContext = context;
       //YLKLog.i(TAG, "onCreate_add_run = " + runnable);
        mRunsMap.put(context, runnable);
        addListener();
        bindServices();
    }

    public void onStart() {
    }

    public void onResume(Context context) {
        mCurrContext = context;
    }

    public void onPause() {

    }

    public void onStop() {

    }

    private void addListener() {

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                Message msg = new Message();
                msg.obj = device;
                msg.what = action_add_device;
               //YLKLog.i(TAG, "action_add_device");
                sendMessage(msg);
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                Message msg = new Message();
                msg.obj = device;
                msg.what = action_remove_device;
               //YLKLog.i(TAG, "action_remove_device");
                sendMessage(msg);
            }
        });
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

    private void sendMessage(Message msg) {
        if (mRunsMap.isEmpty())
            return;
        if (mCurrContext != null) {
            ClingRunnable runnable = mRunsMap.get(mCurrContext);
            if (runnable != null)
                runnable.run(msg);
        }
    }

    /**
     * 播放视频
     */
    public void play(Context context) {
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
                   //YLKLog.i(TAG, "play success");
                    Map<String, String> map = new HashMap<>();
                    map.put("url", url);
                    Message msg = new Message();
                    msg.what = action_succ;
                    msg.obj = map;
                    sendMessage(msg);
                    ClingManager.getInstance().registerAVTransport(mRootContext);
                    ClingManager.getInstance().registerRenderingControl(mRootContext);
                }

                @Override
                public void fail(IResponse response) {
                   //YLKLog.i(TAG, "play fail");
                    Map<String, String> map = new HashMap<>();
                    map.put("url", url);
                    Message msg = new Message();
                    msg.what = action_error;
                    msg.obj = map;
                    sendMessage(msg);
                }
            });
        } else {
            mClingPlayControl.play(new ControlCallback() {
                @Override
                public void success(IResponse response) {
                   //YLKLog.i(TAG, "play success");
                    Map<String, String> map = new HashMap<>();
                    map.put("url", url);
                    Message msg = new Message();
                    msg.what = action_succ;
                    msg.obj = map;
                    sendMessage(msg);
                }

                @Override
                public void fail(IResponse response) {
                   //YLKLog.i(TAG, "play fail");
                    Map<String, String> map = new HashMap<>();
                    map.put("url", url);
                    Message msg = new Message();
                    msg.what = action_error;
                    msg.obj = map;
                    sendMessage(msg);
                }
            });
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
               //YLKLog.i(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
               //YLKLog.i(TAG, "pause fail");
            }
        });
    }

    /**
     * 停止
     */
    public void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
               //YLKLog.i(TAG, "stop success");
            }

            @Override
            public void fail(IResponse response) {
               //YLKLog.i(TAG, "stop fail");
            }
        });
    }

    public void onDestroy(Context context) {
        mRunsMap.remove(context);
       //YLKLog.i(TAG, "onDestroy_removeRun context = " + context);
        if (context != mRootContext) {
           //YLKLog.i(TAG, "onDestroy_removeUrl context = " + context);
            mDataMap.remove(context);
            return;
        }
       //YLKLog.i(TAG, "onDestroy");
        // Unbind UPnP service
        if (mServiceBind)
            mRootContext.unbindService(mUpnpServiceConnection);

        ClingManager.getInstance().destroy();
        ClingDeviceList.getInstance().destroy();
        mInstance = null;
        mRootContext = null;
        mCurrContext = null;
    }

    public void setPlayUrl (Context context, String playUrl) {
        mPlayUrl = playUrl;
        if (context == null || TextUtils.isEmpty(playUrl))
            return;
        mDataMap.put(context, playUrl.replace("https", "http"));
    }

    public void setCurrentState(int state) {
        if (mClingPlayControl != null)
            mClingPlayControl.setCurrentState(state);
    }

    public boolean isServiceConnecting() {
        return mServiceConnecting;
    }

    public boolean isHasConnected() {
        return mHasConnected;
    }

    public boolean isConnectSucc() {
        return mConnectSucc;
    }
}
