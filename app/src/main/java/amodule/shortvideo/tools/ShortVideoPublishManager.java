package amodule.shortvideo.tools;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadVideoSQLite;
import amodule.upload.callback.UploadListNetCallBack;
import aplug.basic.BreakPointControl;
import aplug.basic.BreakPointUploadManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * 短视频发布控制类--单例模式
 */
public class ShortVideoPublishManager {
    private boolean isUploading = false;//是否正在上传中，true上传，false不上传
    private ShortVideoPublishBean shortVideoPublishBean;
    private UploadVideoSQLite uploadVideoSQLite;

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
        Log.i("xianghaTag","startUpload::");
        if(shortVideoPublishBean==null||shortVideoPublishBean.isLocalDataEmpty()){
            return;
        }
        Log.i("xianghaTag","startUpload:11:"+shortVideoPublishBean.toJsonString());
        uploadVideoSQLite = new UploadVideoSQLite(XHApplication.in());
        UploadArticleData uploadArticleData = new UploadArticleData();
        uploadArticleData.setTitle(shortVideoPublishBean.getName());
        uploadArticleData.setImg(shortVideoPublishBean.getImagePath());
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(shortVideoPublishBean.getVideoPath());
        uploadArticleData.setVideos(jsonArray.toString());
        uploadArticleData.setExtraDataJson(shortVideoPublishBean.toJsonString());
        if(TextUtils.isEmpty(shortVideoPublishBean.getId())) {//插入数据
            //保存数据库
            int id = uploadVideoSQLite.insert(uploadArticleData);
            shortVideoPublishBean.setId(String.valueOf(id));
        }else{//更新数据
            uploadVideoSQLite.update(Integer.parseInt(shortVideoPublishBean.getId()),uploadArticleData);
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
    private void setRequstShortVideoRelease(){
        Log.i("xianghaTag","setRequstShortVideoRelease::33:");
        if(shortVideoPublishBean.isDataEmpty()){
            return;
        }
        Log.i("xianghaTag","setRequstShortVideoRelease::--:");
        String url= StringManager.API_SHORTVIDEO_RELEASE;
        String params = "name="+shortVideoPublishBean.getName()+"&imageUrl="+shortVideoPublishBean.getImageUrl()
                +"&imageSize="+shortVideoPublishBean.getImageSize()+"&videoUrl="+shortVideoPublishBean.getVideoUrl()
                +"&videoSize="+shortVideoPublishBean.getVideoSize()+"&videoTime="+shortVideoPublishBean.getVideoTime()
                +"&musicCode="+shortVideoPublishBean.getMusicCode()+"&topicCode="+shortVideoPublishBean.getTopicCode()
                +"&address="+shortVideoPublishBean.getAddress();
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
        startUploadVideo();

    }
    public void startUploadVideo(){
        Log.i("xianghaTag","startUploadVideo:::");
        String md5 = Tools.getMD5(shortVideoPublishBean.getVideoPath());
        BreakPointControl breakPointControl= new BreakPointControl(XHApplication.in(),
                md5,shortVideoPublishBean.getVideoPath(),BreakPointUploadManager.TYPE_VIDEO);
        breakPointControl.start(new UploadListNetCallBack() {
            @Override
            public void onProgress(double progress, String uniqueId) {
            }
            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                Log.i("xianghaTag","startUploadVideo:::"+url);
                if(!TextUtils.isEmpty(url)) {
                    shortVideoPublishBean.setVideoUrl(url);
                    startUploadImg();
                }
            }
            @Override
            public void onFaild(String faild, String uniqueId) {
                Log.i("xianghaTag","startUploadVideo:2::"+faild);
            }
            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {
            }
            @Override
            public void onProgressSpeed(String uniqueId, long speed) {
            }
        });
    }
    private void startUploadImg(){
        Log.i("xianghaTag","startUploadImg:::");
        String md5 = Tools.getMD5(shortVideoPublishBean.getImagePath());
        BreakPointControl breakPointControl= new BreakPointControl(XHApplication.in(),
                md5,shortVideoPublishBean.getImagePath(),BreakPointUploadManager.TYPE_IMG);
        breakPointControl.start(new UploadListNetCallBack() {
            @Override
            public void onProgress(double progress, String uniqueId) {
            }
            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                Log.i("xianghaTag","startUploadImg:::"+url);
                if(!TextUtils.isEmpty(url)){
                    shortVideoPublishBean.setImageUrl(url);
                    setRequstShortVideoRelease();
                }
            }
            @Override
            public void onFaild(String faild, String uniqueId) {
                Log.i("xianghaTag","startUploadImg::22:");
            }
            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {
            }
            @Override
            public void onProgressSpeed(String uniqueId, long speed) {
            }
        });
    }


}
