package third.cling.service.manager;

import android.content.Context;
import android.support.annotation.Nullable;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;

import third.cling.entity.ClingControlPoint;
import third.cling.entity.ClingDevice;
import third.cling.entity.IControlPoint;
import third.cling.entity.IDevice;
import third.cling.service.ClingUpnpService;
import third.cling.util.ListUtils;
import third.cling.util.Utils;

/**
 * 说明：所有对服务的操作都通过该类代理执行
 *
 * 日期：17/6/27 18:12
 */

public class ClingManager implements IClingManager {

//    public static final ServiceType CONTENT_DIRECTORY_SERVICE = new UDAServiceType("ContentDirectory");
    public static final ServiceType AV_TRANSPORT_SERVICE = new UDAServiceType("AVTransport");
    /** 控制服务 */
    public static final ServiceType RENDERING_CONTROL_SERVICE = new UDAServiceType("RenderingControl");
    public static final DeviceType DMR_DEVICE_TYPE = new UDADeviceType("MediaRenderer");

    private static ClingManager INSTANCE = null;

    private ClingUpnpService mUpnpService;
    private IDeviceManager mDeviceManager;

//    private SystemService mSystemService;

    private ClingManager() {
    }

    public static ClingManager getInstance() {
        if (Utils.isNull(INSTANCE)) {
            INSTANCE = new ClingManager();
        }
        return INSTANCE;
    }


    @Override
    public void searchDevices() {
        if (!Utils.isNull(mUpnpService)) {
            if(mUpnpService.getControlPoint()!=null) {
                mUpnpService.getControlPoint().search();
            }
        }
    }

    @Override
    @Nullable
    public Collection<ClingDevice> getDmrDevices() {
        if (Utils.isNull(mUpnpService)) {
            return null;
        }

        Registry registry = mUpnpService.getRegistry();
        if (registry == null)
            return null;
        Collection<Device> devices = registry.getDevices(DMR_DEVICE_TYPE);
        if (ListUtils.isEmpty(devices)) {
            return null;
        }

        Collection<ClingDevice> clingDevices = new ArrayList<>();
        for (Device device : devices) {
            ClingDevice clingDevice = new ClingDevice(device);
            clingDevices.add(clingDevice);
        }
        return clingDevices;
    }

    @Override
    @Nullable
    public IControlPoint getControlPoint() {
        if (Utils.isNull(mUpnpService)) {
            return null;
        }
        if(mUpnpService.getControlPoint()!=null)ClingControlPoint.getInstance().setControlPoint(mUpnpService.getControlPoint());

        return ClingControlPoint.getInstance();
    }

    @Override
    public Registry getRegistry() {
        if (mUpnpService == null)
            return null;
        return mUpnpService.getRegistry();
    }

    @Override
    public IDevice getSelectedDevice() {
        if (Utils.isNull(mDeviceManager)) {
            return null;
        }
        return mDeviceManager.getSelectedDevice();
    }

    @Override
    public void cleanSelectedDevice() {
        if (Utils.isNull(mDeviceManager)) {
            return;
        }
        mDeviceManager.cleanSelectedDevice();
    }

    @Override
    public void setSelectedDevice(IDevice device) {
        mDeviceManager.setSelectedDevice(device);
    }

    @Override
    public void registerAVTransport(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;
        mDeviceManager.registerAVTransport(context);
    }

    @Override
    public void registerRenderingControl(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerRenderingControl(context);
    }

    @Override
    public void setUpnpService(ClingUpnpService upnpService) {
        mUpnpService = upnpService;
    }

    @Override
    public void setDeviceManager(IDeviceManager deviceManager) {
        mDeviceManager = deviceManager;
    }

    @Override
    public void destroy() {
        if (mUpnpService != null)
            mUpnpService.onDestroy();
        if (mDeviceManager != null)
            mDeviceManager.destroy();
    }
}
