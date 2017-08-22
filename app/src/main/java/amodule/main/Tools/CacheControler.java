package amodule.main.Tools;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;

/**
 * PackageName : amodule.main.Tools
 * Created by MrTrying on 2017/8/21 17:44.
 * E_mail : ztanzeyu@gmail.com
 */

public class CacheControler {
    public final int MAX_COUNT = 10;
    public final String FILE_PREFIX = "homecache_";
    public final int DEFAULT_STATUS = -1;
    public static final int LOAD_PREPARE = 0x1;
    public static final int LOAD_OVER = 0x2;
    public static final int LOAD_INVALID = 0x3;

    protected int status = DEFAULT_STATUS;
    protected String type = "";
    protected String backUrl = "";
    protected ArrayList<Map<String, String>> cacheData = null;

    public CacheControler(@NonNull String type) {
        if (TextUtils.isEmpty(type))
            type = "default";
        this.type = type;
        cacheData = new ArrayList<>();
    }

    /**
     * 插入数据
     *
     * @param data
     */
    public void insertCache(List<Map<String, String>> data) {
        if (data == null || data.isEmpty()) return;

        //去重添加数据
        for(int index = data.size() - 1 ; index >= 0 ; index --){
            Map<String,String> map = data.get(index);
            if(!cacheData.contains(map))
                cacheData.add(0, map);
        }
        //移除多余的数据
        while (cacheData.size() > MAX_COUNT) {
            cacheData.remove(cacheData.size() - 1);
        }
        saveCacheToFile();
    }

    /**
     * 读取数据
     * @param callback 回调
     */
    public synchronized void loadCacheData(@NonNull OnLoadCallback callback){
        Log.i("tzy","status = " + status);
        if(status != DEFAULT_STATUS)
            return;
        status = LOAD_PREPARE;
        if(callback != null){
            callback.onLoad(StringManager.getListMapByJson(getCacheData()));
        }
        status = LOAD_OVER;
    }

    /** 获取缓存数据 */
    public String getCacheData() {
        String cacheDataStr = FileManager.readFile(getCacheFilePath());
        Log.i("tzy","cacheDataStr = " + cacheDataStr);
        return cacheDataStr;
    }

    /** 保存到文件 */
    public synchronized void saveCacheToFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String cacheDataStr = StringManager.getJsonByArrayList(cacheData).toString();
                Log.i("tzy","cacheDataStr = " + cacheDataStr);
                String cacheFilePath = getCacheFilePath();
                Log.i("tzy","cacheFilePath = " + cacheFilePath);
                FileManager.saveFileToCompletePath(cacheFilePath, cacheDataStr, false);
            }
        }).start();
    }

    /** 获取cache文件路径 */
    private String getCacheFilePath(){
        return new StringBuffer(FileManager.getDataDir()).append(FILE_PREFIX).append(type).toString();
    }

    public void setLoadInvalid(){
        status = LOAD_INVALID;
    }

    public void resetStatus(){
        status = DEFAULT_STATUS;
    }

    public int getStatus() {
        return status;
    }

    public interface OnLoadCallback{
        void onLoad(List<Map<String,String>> data);
    }
}
