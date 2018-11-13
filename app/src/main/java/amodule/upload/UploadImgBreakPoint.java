package amodule.upload;

import org.json.JSONObject;

import acore.override.helper.XHActivityManager;
import amodule.upload.callback.UploadListNetCallBack;
import aplug.basic.BreakPointControl;
import aplug.basic.BreakPointUploadManager;

/**
 * 此处上传图片类型对数据
 */
public class UploadImgBreakPoint {

    private UploadImgFileCallBack callback;
    public String mPath;
    public int state;
    private double mProgress;
    private long mSpeed;
    private BreakPointControl breakPointControl;
    public UploadImgBreakPoint(String path, UploadImgFileCallBack uploadSubImgCallback){
        this.mPath = path;
        this.callback = uploadSubImgCallback;
        state = UploadImg.UPLOAD;
        breakPointControl= new BreakPointControl(XHActivityManager.getInstance().getCurrentActivity(),"",mPath
        ,BreakPointUploadManager.TYPE_IMG);
    }
    public void uploadImg(){

        breakPointControl.start(new UploadListNetCallBack() {
            @Override
            public void onProgress(double progress, String uniqueId) {
                mProgress=progress;
                if(callback!=null){
                    callback.uploaded(false,UploadImg.UPLOAD,"",mProgress);
                }
            }

            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                state=UploadImg.SUCCES;
                if(callback!=null){
                    callback.uploaded(true,UploadImg.SUCCES,url,mProgress);
                }
            }

            @Override
            public void onFaild(String faild, String uniqueId) {
                state=UploadImg.FAIL;
                if(callback!=null){
                    callback.uploaded(false,UploadImg.FAIL,"",mProgress);
                }
            }

            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {
            }

            @Override
            public void onProgressSpeed(String uniqueId, long speed) {
                mSpeed=speed;
                if(callback!=null){
                    callback.uploadedSpeed(false,UploadImg.UPLOAD,mSpeed);
                }
            }
        });
    }
    public interface UploadImgFileCallBack{
        /**
         * 上传文件
         * @param isSuccess 是否成功
         * @param state 状态
         * @param url 成功后对文件路径地址
         * @param progress 当前进度
         */
        public void uploaded(boolean isSuccess,int state,String url,double progress);
        public void uploadedSpeed(boolean isSuccess,int state,long speed);
    }
}
