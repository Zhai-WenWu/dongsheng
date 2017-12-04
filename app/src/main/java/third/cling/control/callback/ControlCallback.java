package third.cling.control.callback;

import third.cling.entity.IResponse;

/**
 * 说明：设备控制操作 回调
 *
 * 日期：17/7/4 10:56
 */

public interface ControlCallback<T> {

    void success(IResponse<T> response);

    void fail(IResponse<T> response);
}
