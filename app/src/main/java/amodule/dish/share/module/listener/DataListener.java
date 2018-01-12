package amodule.dish.share.module.listener;

/**
 * Created by sll on 2018/1/3.
 */

public interface DataListener {
    void onLoadData();
    void onDataReady(int flag, String type, Object object);
}
