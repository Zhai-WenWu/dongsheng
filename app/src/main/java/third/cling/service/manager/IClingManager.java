package third.cling.service.manager;

import org.fourthline.cling.registry.Registry;
import third.cling.service.ClingUpnpService;

/**
 * 说明：
 *
 * 日期：17/6/28 16:30
 */

public interface IClingManager extends IDLNAManager {

    void setUpnpService(ClingUpnpService upnpService);

    void setDeviceManager(IDeviceManager deviceManager);

    Registry getRegistry();
}
