package acore.logic;

import android.support.annotation.Nullable;

import java.util.Map;

import acore.tools.StringManager;

public class ConfigHelper {
    private static volatile ConfigHelper mInstance;
    private static Map<String, String> mConfigMap;
    private ConfigHelper() {}

    public static synchronized ConfigHelper getInstance() {
        if (mInstance == null) {
            synchronized (ConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new ConfigHelper();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    @Nullable
    public synchronized Map<String, String> getConfigMap() {
        if (mConfigMap == null || mConfigMap.isEmpty()) {
            synchronized (ConfigHelper.class) {
                mConfigMap = ConfigMannager.getConfigMapByLocal();
                return mConfigMap;
            }
        }
        return mConfigMap;
    }

    @Nullable
    public String getConfigValueByKey(String key) {
        Map<String, String> confMap = getConfigMap();
        if (key == null || confMap == null || confMap.isEmpty()) {
            return null;
        }
        return confMap.get(key);
    }

    public void updateConfigData(String str) {
        if (mConfigMap != null) {
            mConfigMap.clear();
        }
        mConfigMap = StringManager.getFirstMap(str);
    }
}
