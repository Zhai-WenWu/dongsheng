package third.cling.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.registry.Registry;

/**
 * 说明：
 *
 * 日期：17/6/28 16:11
 */

public class ClingUpnpService extends AndroidUpnpServiceImpl {
    private LocalDevice mLocalDevice = null;

    @Override
    public void onCreate() {
        try {
            super.onCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
       //YLKLog.i("xianghaTag","ClingUpnpService:::onCreate");
        //LocalBinder instead of binder
        binder = new LocalBinder();
    }

    @Override
    public void onDestroy() {
       //YLKLog.i("xianghaTag","ClingUpnpService:::onBind");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
       //YLKLog.i("xianghaTag","ClingUpnpService:::onBind");
        return binder;
    }

    public LocalDevice getLocalDevice() {
        return mLocalDevice;
    }

    public UpnpServiceConfiguration getConfiguration() {
        return upnpService.getConfiguration();
    }

    public Registry getRegistry() {
        if(upnpService==null)return null;
        return upnpService.getRegistry();
    }

    public ControlPoint getControlPoint() {
        if(upnpService==null)return null;
        return upnpService.getControlPoint();
    }

    public class LocalBinder extends Binder {
        public ClingUpnpService getService() {
            return ClingUpnpService.this;
        }
    }
}
