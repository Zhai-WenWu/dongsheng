package aplug.basic;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016/10/24.
 */

public abstract class BreakPointUploadCallBack {
    /**
     * 加载完毕
     * @param flag
     */
    public abstract void loaded(int flag, String key, double percent, JSONObject res);

}
