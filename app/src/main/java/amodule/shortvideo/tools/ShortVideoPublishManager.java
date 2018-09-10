package amodule.shortvideo.tools;

import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * 短视频发布控制类--单例模式
 */
public class ShortVideoPublishManager {
    public boolean isUploading = false;//是否正在上传中，true上传，false不上传
    public ShortVideoPublishBean shortVideoPublishBean;
    private static ShortVideoPublishManager shortVideoPublishManager;
    public static ShortVideoPublishManager getInstance(){
        if(null == shortVideoPublishManager) {
            synchronized (ShortVideoPublishManager.class) {
                if (null == shortVideoPublishManager) {
                    shortVideoPublishManager = new ShortVideoPublishManager();
                }
            }
        }
        return shortVideoPublishManager;
    }

    /**
     * 判断是否在上传中
     * @return
     */
    public boolean isUpload(){
        return isUploading;
    }

    /**
     * 设置bean数据
     * @param bean
     */
    public void setShortVideoPublishBean(ShortVideoPublishBean bean){
        this.shortVideoPublishBean = bean;
    }

    /**
     * 开始上传
     */
    public void startUpload(){
        if(shortVideoPublishBean==null||shortVideoPublishBean.isLocalDataEmpty()){
            return;
        }
        startBeakPointUpload();
    }

    /**
     * 设置短视频回调
     * @param callBack
     */
    public void setShortVideoUploadCallBack(ShortVideoUploadCallBack callBack){
        this.shortVideoUploadCallBack= callBack;
    }
    public ShortVideoUploadCallBack shortVideoUploadCallBack;
    public interface ShortVideoUploadCallBack{
        public void onSuccess();
        public void onProgress(int progress);
        public void onFailed();
    }

    /**
     *短视频上传
     */
    public void setRequstShortVideoRelease(){
        if(shortVideoPublishBean.isDataEmpty()){
            return;
        }
        String url= StringManager.API_SHORTVIDEO_RELEASE;
        String params = "";
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if(flag>ReqEncyptInternet.REQ_OK_STRING){
                    if(shortVideoUploadCallBack!=null){
                        shortVideoUploadCallBack.onSuccess();
                    }
                }else{
                    if(shortVideoUploadCallBack!=null){
                        shortVideoUploadCallBack.onFailed();
                    }
                }
            }
        });
    }

    /**
     *开始vide上传
     */
    public void startBeakPointUpload(){

    }

}
