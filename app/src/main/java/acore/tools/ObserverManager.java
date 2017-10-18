package acore.tools;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 通知管理器
 * 使用多对多结构，单点刷新
 * 外部使用：注册不同key，对应key的数据变化进行通知
 */
public class ObserverManager {

    //用于通知的tag
    public static final String NOTIFY_UPLOADOVER = "notify_uploadover";
    public static final String NOTIFY_LOGIN = "notify_login";
    public static final String NOTIFY_LOGOUT = "notify_logout";
    public static final String NOTIFY_FOLLOW = "notify_follow";
    public static final String NOTIFY_PAYFINISH = "notify_payfinish";
    public static final String NOTIFY_SHARE = "notify_share";
    public static final String NOTIFY_COMMENT_SUCCESS = "notify_comment_success";
    public static final String NOTIFY_YIYUAN_BIND = "notify_yiyuan_bind";

    private static volatile ObserverManager mObserverManager;
    private HashMap<String, ArrayList<IObserver>> mObservers;

    private ObserverManager() {
        mObservers = new HashMap<>();
    }

    public static ObserverManager getInstence() {
        synchronized (ObserverManager.class) {
            if (mObserverManager == null){
                mObserverManager = new ObserverManager();
            }
        }
        return mObserverManager;
    }

    /**
     * 注册观察者
     * @param observer
     * @param names tag数组
     */
    public void registerObserver(IObserver observer,String... names) {
        synchronized (ObserverManager.class) {
            if (names == null || observer == null)
                return;
            for(String name : names){
                if(TextUtils.isEmpty(name)){
                    continue;
                }
                ArrayList<IObserver> observers;
                if (!mObservers.containsKey(name)) {
                    observers = new ArrayList<>();
                    mObservers.put(name, observers);
                } else {
                    observers = mObservers.get(name);
                }
                if (!observers.contains(observer))
                    observers.add(observer);
            }
        }
    }

    /**
     * 反注册 by tag
     * @param names
     */
    public void unRegisterObserver(String... names) {
        synchronized (ObserverManager.class) {
            if(names == null){
                return;
            }
            for(String name:names){
                if (!TextUtils.isEmpty(name))
                    mObservers.remove(name);
            }
        }
    }

    /**
     * 反注册 by observer
     * @param observer
     */
    public void unRegisterObserver(IObserver observer) {
        synchronized (ObserverManager.class) {
            for (String name : mObservers.keySet()) {
                ArrayList<IObserver> observers = mObservers.get(name);
                observers.remove(observer);
            }
        }
    }

    /**
     * 通知
     * @param name 事件对应的tag
     * @param sender
     * @param data
     */
    public void notify(String name, Object sender, Object data) {
        synchronized (ObserverManager.class) {
            if (mObservers.containsKey(name)) {
                for (IObserver observer : mObservers.get(name)) {
                    observer.notify(name, sender, data);
                }
            }
        }
    }
}
