package third.cling.entity;

import android.support.annotation.Nullable;

import org.fourthline.cling.model.meta.Device;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 说明：单例设备列表, 保证全局只有一个设备列表
 *
 * 日期：17/6/30 11:25
 */

public class ClingDeviceList {

    /**
     * 投屏设备列表 都是引用该 list
     */
    private Collection<ClingDevice> mClingDeviceList;

    private ClingDeviceList(){
        mClingDeviceList = new CopyOnWriteArrayList<>();
    }

    public static ClingDeviceList getInstance() {
        return SingletonHolder.mInstance;
    }

    public void removeDevice(ClingDevice device){
        if (mClingDeviceList != null)
            mClingDeviceList.remove(device);
    }

    public void addDevice(ClingDevice device){
        if (mClingDeviceList != null)
            mClingDeviceList.add(device);
    }

    @Nullable
    public ClingDevice getClingDevice(Device device){
        if (mClingDeviceList == null)
            return null;
        Iterator<ClingDevice> iterator = mClingDeviceList.iterator();
        while (iterator != null && iterator.hasNext()) {
            ClingDevice clingDevice = iterator.next();
            if (clingDevice != null) {
                Device deviceTemp = clingDevice.getDevice();
                if (deviceTemp != null && deviceTemp.equals(device)){
                    return clingDevice;
                }
            }
        }
        return null;
    }

    public boolean contain(Device device){
        if (mClingDeviceList == null)
            return false;
        for (ClingDevice clingDevice : mClingDeviceList){
            Device deviceTemp = clingDevice.getDevice();
            if (deviceTemp != null && deviceTemp.equals(device)){
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Collection<ClingDevice> getClingDeviceList(){
        return mClingDeviceList;
    }

    public void setClingDeviceList(Collection<ClingDevice> clingDeviceList) {
        mClingDeviceList = clingDeviceList;
    }

    public void destroy(){
        mClingDeviceList = null;
    }

    private static class SingletonHolder {
        private static final ClingDeviceList mInstance = new ClingDeviceList();
    }
}