package amodule.upload.callback;

import org.json.JSONObject;

/**
 * Created by ：fei_teng on 2016/10/30 16:56.
 */

public interface UploadListNetCallBack {

    public void onProgress(double progress, String uniqueId);

    public void onSuccess(String url, String uniqueId, JSONObject jsonObject);

    public void onFaild(String faild, String uniqueId);

    public void onLastUploadOver(boolean flag, String responseStr);

    public void onProgressSpeed(String uniqueId,long speed);
}
