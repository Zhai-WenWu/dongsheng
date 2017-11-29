package amodule.home.delegate;

/**
 * 数据设置代理
 * Created by sll on 2017/11/17.
 */

public interface IDataSetDelegate<T> {
    void onSetData(T t);
    void onResetData();
}
