package amodule.lesson.listener;

/**
 * Created by sll on 2017/12/19.
 */

public interface IDataListener<T> {
    void onGetData(boolean refresh);
    void onDataReady(T t, boolean refresh);
}
