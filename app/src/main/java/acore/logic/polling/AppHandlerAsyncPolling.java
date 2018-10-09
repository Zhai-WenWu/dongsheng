package acore.logic.polling;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import java.util.HashMap;
import java.util.Map;

public class AppHandlerAsyncPolling {

    private HandlerThread mHandlerThread;
    private Handler mAsyncHandler;
    private Map<Integer, PollingConfig> mMap;

    private static volatile AppHandlerAsyncPolling mInstance;

    private AppHandlerAsyncPolling() {

        mHandlerThread = new HandlerThread("AppHandlerAsyncPolling", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mAsyncHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int type = msg.what;
                PollingConfig pollingConfig = mMap.get(type);
                if (pollingConfig == null) {
                    return;
                }
                IHandleMessage handleMessage = pollingConfig.getIHandleMessage();
                if (handleMessage != null) {
                    handleMessage.onHandleMessage(msg);
                }
                if (mAsyncHandler != null) {
                    mAsyncHandler.sendEmptyMessageDelayed(pollingConfig.getType(), pollingConfig.getMillis());
                }
            }
        };
        mMap = new HashMap<>();
    }

    public static synchronized AppHandlerAsyncPolling getInstance() {
        if (mInstance == null) {
            synchronized (AppHandlerAsyncPolling.class) {
                if (mInstance == null) {
                    mInstance = new AppHandlerAsyncPolling();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    public void startPolling(PollingConfig pollingConfig) {
        if (pollingConfig == null || mMap.containsKey(pollingConfig.getType()) || (mAsyncHandler != null && mAsyncHandler.hasMessages(pollingConfig.getType()))) {
            return;
        }
        stopPolling(pollingConfig);
        mMap.put(pollingConfig.getType(), pollingConfig);
        if (mAsyncHandler != null) {
            mAsyncHandler.sendEmptyMessageDelayed(pollingConfig.getType(), pollingConfig.getMillis());
        }
    }

    public void startPollingImmediately(PollingConfig pollingConfig) {
        if (pollingConfig == null || mMap.containsKey(pollingConfig.getType()) || (mAsyncHandler != null && mAsyncHandler.hasMessages(pollingConfig.getType()))) {
            return;
        }
        stopPolling(pollingConfig);
        mMap.put(pollingConfig.getType(), pollingConfig);
        if (mAsyncHandler != null) {
            mAsyncHandler.sendEmptyMessage(pollingConfig.getType());
        }
    }

    public void stopPolling(PollingConfig pollingConfig) {
        if (pollingConfig == null || !mMap.containsKey(pollingConfig.getType())) {
            return;
        }
        if (mAsyncHandler != null) {
            mAsyncHandler.removeMessages(pollingConfig.getType());
            if (mMap != null) {
                mMap.remove(pollingConfig.getType());
            }
        }
    }

    public void destroyPolling() {
        if (mAsyncHandler != null) {
            mAsyncHandler.removeCallbacksAndMessages(null);
            mAsyncHandler = null;
        }
        if (mMap != null) {
            mMap.clear();
            mMap = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread = null;
        }
        mInstance = null;
    }

}
