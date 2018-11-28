package amodule.shortvideo.tools;

import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.UploadDishData;
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
    private UploadArticleData uploadArticleData;

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
        uploadArticleData=null;
        if(shortVideoPublishBean==null||shortVideoPublishBean.isLocalDataEmpty()){
            return;
        }
        uploadVideoSQLite = new UploadVideoSQLite(XHApplication.in());
        uploadArticleData = new UploadArticleData();
        uploadArticleData.setTitle(shortVideoPublishBean.getName());
        uploadArticleData.setImg(shortVideoPublishBean.getImagePath());
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(shortVideoPublishBean.getVideoPath());
        uploadArticleData.setVideos(jsonArray.toString());
        uploadArticleData.setExtraDataJson(shortVideoPublishBean.toJsonString());
        if(TextUtils.isEmpty(shortVideoPublishBean.getId())) {//插入数据
            //保存数据库
            uploadArticleData.setUploadType(UploadDishData.UPLOAD_ING);
            int id = uploadVideoSQLite.insert(uploadArticleData);
            shortVideoPublishBean.setId(String.valueOf(id));
        }else{//更新数据
            uploadArticleData.setUploadType(UploadDishData.UPLOAD_ING);
            uploadVideoSQLite.update(Integer.parseInt(shortVideoPublishBean.getId()),uploadArticleData);
        }
        startBeakPointUpload();
    }

    /**
     * 添加回调
     * @param callBack
     */
    public void addShortVideoUploadCallBack(ShortVideoUploadCallBack callBack){
        checkCallbacksNonNull();
        if(callBack != null && !mShortVideoUploadCallBacks.contains(callBack)){
            mShortVideoUploadCallBacks.add(callBack);
        }
    }

    private void checkCallbacksNonNull() {
        if(mShortVideoUploadCallBacks == null){
            mShortVideoUploadCallBacks = new ArrayList<>();
        }
    }

    private List<ShortVideoUploadCallBack> mShortVideoUploadCallBacks = new ArrayList<>();

    private void notifySucces(int sqlId, Object msg){
        checkCallbacksNonNull();
        Stream.of(mShortVideoUploadCallBacks)
                .filter(value -> value != null)
                .forEach(value -> value.onSuccess(sqlId, msg));
        mShortVideoUploadCallBacks.clear();
    }

    private void notifyProgress(int progress,int sqlId){
        checkCallbacksNonNull();
        Stream.of(mShortVideoUploadCallBacks)
                .filter(value -> value != null)
                .forEach(value -> value.onProgress(progress, sqlId));
    }

    private void notifyFailed(int sqlId){
        checkCallbacksNonNull();
        Stream.of(mShortVideoUploadCallBacks)
                .filter(value -> value != null)
                .forEach(value -> value.onFailed(sqlId));
        mShortVideoUploadCallBacks.clear();
    }

    public interface ShortVideoUploadCallBack{
        public void onSuccess(int sqlId,Object msg);
        public void onProgress(int progress,int sqlId);
        public void onFailed(int sqlId);
    }

    /**
     *短视频上传
     */
    private void setRequstShortVideoRelease(){
        if(shortVideoPublishBean.isDataEmpty()){
            return;
        }
        String url= StringManager.API_SHORTVIDEO_RELEASE;
        //TODO
//        shortVideoPublishBean.setVideoUrl("");
        String params = "name="+shortVideoPublishBean.getName()+"&imageUrl="+shortVideoPublishBean.getImageUrl()
                +"&imageSize="+shortVideoPublishBean.getImageSize()+"&videoUrl="+shortVideoPublishBean.getVideoUrl()
                +"&videoSize="+shortVideoPublishBean.getVideoSize()+"&videoTime="+shortVideoPublishBean.getVideoTime()
                +"&musicCode="+shortVideoPublishBean.getMusicCode()+"&topicCode="+shortVideoPublishBean.getTopicCode()
                +"&address="+shortVideoPublishBean.getAddress();
        ReqEncyptInternet.in().doGetEncypt(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if(flag >= ReqEncyptInternet.REQ_OK_STRING){
                    UploadState(UploadDishData.UPLOAD_SUCCESS);
                    notifySucces(Integer.parseInt(shortVideoPublishBean.getId()),msg);
                }else{
                    UploadState(UploadDishData.UPLOAD_FAIL);
                    notifyFailed(Integer.parseInt(shortVideoPublishBean.getId()));
                }
            }
        });
    }

    /** 开始vide上传*/
    public void startBeakPointUpload(){
        isUploading=true;
        startUploadVideo();

    }
    public void startUploadVideo(){
        String md5 = Tools.getMD5(shortVideoPublishBean.getVideoPath());
        BreakPointControl breakPointControl= new BreakPointControl(XHApplication.in(),
                md5,shortVideoPublishBean.getVideoPath(),BreakPointUploadManager.TYPE_VIDEO);
        breakPointControl.start(new UploadListNetCallBack() {
            @Override
            public void onProgress(double progress, String uniqueId) {
                notifyProgress((int) (progress*90),Integer.parseInt(shortVideoPublishBean.getId()));
            }
            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                if(!TextUtils.isEmpty(url)) {
                    shortVideoPublishBean.setVideoUrl(url);
                    startUploadImg();
                }
            }
            @Override
            public void onFaild(String faild, String uniqueId) {
                UploadState(UploadDishData.UPLOAD_FAIL);
                notifyFailed(Integer.parseInt(shortVideoPublishBean.getId()));
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
        String md5 = Tools.getMD5(shortVideoPublishBean.getImagePath());
        BreakPointControl breakPointControl= new BreakPointControl(XHApplication.in(),
                md5,shortVideoPublishBean.getImagePath(),BreakPointUploadManager.TYPE_IMG);
        breakPointControl.start(new UploadListNetCallBack() {
            @Override
            public void onProgress(double progress, String uniqueId) {
                notifyProgress(99 ,Integer.parseInt(shortVideoPublishBean.getId()));
            }
            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                if(!TextUtils.isEmpty(url)){
                    shortVideoPublishBean.setImageUrl(url);
                    setRequstShortVideoRelease();
                }
            }
            @Override
            public void onFaild(String faild, String uniqueId) {
                UploadState(UploadDishData.UPLOAD_FAIL);
                notifyFailed(Integer.parseInt(shortVideoPublishBean.getId()));
            }
            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {
            }
            @Override
            public void onProgressSpeed(String uniqueId, long speed) {
            }
        });
    }

    /**
     * 更新数据库状态
     * @param key
     */
    private void UploadState(String key){
        if(uploadArticleData!=null&&uploadVideoSQLite!=null) {
            isUploading=false;
            if(UploadDishData.UPLOAD_FAIL.equals(key)&&uploadVideoSQLite.checkOver(UploadDishData.UPLOAD_FAIL)){
                Tools.showToast(XHApplication.in(),"您已有10个内容发布失败，小哈已经无法为您存储更多了～");
                uploadVideoSQLite.deleteById(Integer.parseInt(shortVideoPublishBean.getId()));
                return;
            }
            if(UploadDishData.UPLOAD_SUCCESS.equals(key)){//成功删除数据
                uploadVideoSQLite.deleteById(Integer.parseInt(shortVideoPublishBean.getId()));
                return;
            }
            uploadArticleData.setUploadType(key);
            uploadVideoSQLite.update(Integer.parseInt(shortVideoPublishBean.getId()), uploadArticleData);
        }
    }

}
