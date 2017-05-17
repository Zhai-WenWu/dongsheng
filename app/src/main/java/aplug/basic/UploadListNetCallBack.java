package aplug.basic;

/**
 *上传池回调接口
 */

public interface UploadListNetCallBack {
    public void onProgress(double progress,String uniqueId);
    public void onSuccess(String url,String uniqueId);
    public void onFaild(String faild,String uniqueId);
}
