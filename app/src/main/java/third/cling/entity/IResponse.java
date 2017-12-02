package third.cling.entity;

/**
 * 说明：设备控制 返回结果
 *
 * 日期：17/7/4 10:50
 */

public interface IResponse<T> {

    T getResponse();

    void setResponse(T response);
}
