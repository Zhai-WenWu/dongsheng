package acore.logic;

import android.support.annotation.Nullable;

import java.util.Map;

public class ConfigHelper {
    private static volatile ConfigHelper mInstance;
    private static Map<String, String> mConfigMap;
    private ConfigHelper() {}

    public static ConfigHelper getInstance() {
        if (mInstance == null) {
            synchronized (ConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new ConfigHelper();
                    mConfigMap = ConfigMannager.getConfigMapByLocal();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    @Nullable
    public Map<String, String> getConfigMap() {
        return mConfigMap;
    }

    @Nullable
    public String getConfigValueByKey(String key) {
        if (key == null || mConfigMap == null || mConfigMap.isEmpty()) {
            return null;
        }
        return mConfigMap.get(key);
    }
}
