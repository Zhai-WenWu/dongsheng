package acore.tools;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created shiliangliang.
 */

public class ObserverManager {

    public static final String NOTIFY_LOAD_HOMEDATA = "notify_load_homedata";


    private Object mLockObj = null;
    private HashMap<String, ArrayList<IObserver>> mMap;
    private static ObserverManager mObserverManager = null;

    private ObserverManager() {
        mLockObj = new Object();
        mMap = new HashMap<String, ArrayList<IObserver>>();
    }

    public synchronized static ObserverManager getInstance() {
        if (mObserverManager == null)
            mObserverManager = new ObserverManager();
        return mObserverManager;
    }

    public void registerObserver(String name, IObserver observer) {
        synchronized (mLockObj) {
            if (TextUtils.isEmpty(name) || observer == null)
                return;
            ArrayList<IObserver> observers;
            if (!mMap.containsKey(name)) {
                observers = new ArrayList<IObserver>();
                mMap.put(name, observers);
            } else {
                observers = mMap.get(name);
            }
            if (!observers.contains(observer))
                observers.add(observer);
        }
    }

    public void unRegisterObserver(String name) {
        synchronized (mLockObj) {
            if (!TextUtils.isEmpty(name))
                mMap.remove(name);
        }
    }

    public void unRegisterObserver(IObserver observer) {
        synchronized (mLockObj) {
            for (String name : mMap.keySet()) {
                ArrayList<IObserver> observers = mMap.get(name);
                observers.remove(observer);
            }
        }
    }

    public void notify(String name, Object sender, Object data) {
        synchronized (mLockObj) {
            if (mMap.containsKey(name)) {
                for (IObserver observer : mMap.get(name)) {
                    observer.notify(name, sender, data);
                }
            }
        }
    }
}
