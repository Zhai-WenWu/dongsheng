package acore.tools;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sll on 2017/8/30.
 */

public class ObserverManager {

    private static Object mLockObj = new Object();
    private static ObserverManager mObserverManager;
    private HashMap<String, ArrayList<IObserver>> mObservers;

    private ObserverManager() {
        mObservers = new HashMap<String, ArrayList<IObserver>>();
    }

    public static ObserverManager getInstence() {
        synchronized (mLockObj) {
            if (mObserverManager == null)
                mObserverManager = new ObserverManager();
            return mObserverManager;
        }
    }

    public void registerObserver(String name, IObserver observer) {
        synchronized (mLockObj) {
            if (TextUtils.isEmpty(name) || observer == null)
                return;
            ArrayList<IObserver> observers;
            if (!mObservers.containsKey(name)) {
                observers = new ArrayList<IObserver>();
                mObservers.put(name, observers);
            } else {
                observers = mObservers.get(name);
            }
            if (!observers.contains(observer))
                observers.add(observer);
        }
    }

    public void unRegisterObserver(String name) {
        synchronized (mLockObj) {
            if (!TextUtils.isEmpty(name))
                mObservers.remove(name);
        }
    }

    public void unRegisterObserver(IObserver observer) {
        synchronized (mLockObj) {
            for (String name : mObservers.keySet()) {
                ArrayList<IObserver> observers = mObservers.get(name);
                observers.remove(observer);
            }
        }
    }

    public void notify(String name, Object sender, Object data) {
        synchronized (mLockObj) {
            if (mObservers.containsKey(name)) {
                for (IObserver observer : mObservers.get(name)) {
                    observer.notify(name, sender, data);
                }
            }
        }
    }
}
