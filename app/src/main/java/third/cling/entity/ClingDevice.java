package third.cling.entity;

import org.fourthline.cling.model.meta.Device;

/**
 * 此类包含投屏设备的所有信息
 */

public class ClingDevice  implements IDevice<Device> {

    private Device mDevice;
    /** 是否已选中 */
    private boolean isSelected;

    public ClingDevice(Device device) {
        this.mDevice = device;
    }

    @Override
    public Device getDevice() {
        return mDevice;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}