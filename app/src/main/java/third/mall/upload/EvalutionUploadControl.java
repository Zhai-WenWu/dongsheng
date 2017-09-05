package third.mall.upload;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TimerTask;

import acore.tools.Tools;
import amodule.upload.callback.UploadListNetCallBack;
import aplug.basic.BreakPointControl;
import aplug.basic.BreakPointUploadManager;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.bean.EvalutionBean;

/**
 * PackageName : third.mall.upload
 * Created by MrTrying on 2017/8/8 20:39.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionUploadControl {
    private Context context;
    /**评价数据体*/
    private EvalutionBean bean;
    /**发布状态对调*/
    private OnPublishCallback onPublishCallback;

    boolean isCanceled = false;
    boolean isPublishing = false;
    volatile boolean isRequesting = false;
    protected boolean instantlyUpload = false;

    public EvalutionUploadControl(Context context) {
        this.context = context;
        bean = new EvalutionBean();
    }

    public void uploadImage(final String filePath) {
        uploadImage(filePath, true, new SimpleUploadListNetCallBack() {
        });
    }

    private void uploadAgin(final String filePath) {
        uploadImage(filePath, false, new SimpleUploadListNetCallBack() {
        });
    }

    /**
     * 上传
     * @param filePath 文件路径
     * @param isFirst   是否是第一次
     * @param callBack 上传回调
     */
    public void uploadImage(final String filePath, boolean isFirst, @Nullable final UploadListNetCallBack callBack) {
        if (TextUtils.isEmpty(filePath))
            return;
        if (isFirst) {
            bean.addImage(filePath);
        }
        if (instantlyUpload || !isFirst) {
            BreakPointControl uploadControl = new BreakPointControl(context, filePath, filePath, "img");
            BreakPointUploadManager.getInstance().addBreakPointContorl(filePath, uploadControl);
            uploadControl.start(new UploadListNetCallBack() {
                @Override
                public void onProgress(double progress, String uniqueId) {
                    if (null != callBack)
                        callBack.onProgress(progress, uniqueId);
                }

                @Override
                public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                    //成功更新url
                    bean.replaceImage(filePath, url);
                    //回调
                    if (null != callBack)
                        callBack.onSuccess(url, uniqueId, jsonObject);
                    //如果处于发布中，再次调用发布方法
                    if (isPublishing && !isCanceled) {
                        publishEvalution();
                    }
                }

                @Override
                public void onFaild(String faild, String uniqueId) {
                    if (null != callBack)
                        callBack.onFaild(faild, uniqueId);

                    //如果发布中图片上传失败，发布直接失败
                    if (isPublishing && onPublishCallback != null) {
                        isPublishing = false;
                        onPublishCallback.onFailed("图片上传失败");
                    }
                }

                @Override
                public void onLastUploadOver(boolean flag, String responseStr) {
                    if (null != callBack)
                        callBack.onLastUploadOver(flag, responseStr);
                }

                @Override
                public void onProgressSpeed(String uniqueId, long speed) {
                    if (null != callBack)
                        callBack.onProgressSpeed(uniqueId, speed);
                }
            });
        }
    }

    /**
     * 删除上传的图片
     * @param filePath 文件路径
     */
    public void delUploadImage(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            bean.removeImage(filePath);
            BreakPointUploadManager.getInstance().delBreakPointUpload(filePath);
        }
    }

    /** 发布 */
    public synchronized void publishEvalution() {
        if(!isPublishing){
            //设置30延时取消
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isRequesting && isPublishing){
                        cancelUpload();
                        Tools.showToast(context,"发布失败，请重试");
                    }
                }
            },30 * 1000);
        }
        isPublishing = true;
        if (onPublishCallback != null)
            onPublishCallback.onStratPublish();
        if (isAllUploadOver() && !isRequesting) {
            isRequesting = true;
            //正式发布评价请求
            MallReqInternet.in().doPost(MallStringManager.mall_addComment,
                    combineParameter(),
                    new MallInternetCallback(context) {
                        @Override
                        public void loadstat(int flag, String url, Object msg, Object... stat) {
                            isRequesting = false;
                            isPublishing = false;
                            if (flag >= MallReqInternet.REQ_OK_STRING) {

                                if (onPublishCallback != null)
                                    onPublishCallback.onSuccess(msg);
                            } else {

                                if (onPublishCallback != null)
                                    onPublishCallback.onFailed("发布失败，请重试");
                            }
                        }
                    });
        } else {
            //上传未上传完成的的图片
            for (String imageUrl : bean.getImages()) {
                if (!TextUtils.isEmpty(imageUrl) && !imageUrl.startsWith("http"))
                    uploadAgin(imageUrl);
            }
        }

    }

    /**
     * 获取参数
     *
     * @return 参数的map
     */
    public LinkedHashMap<String, String> combineParameter() {
        LinkedHashMap<String, String> uploadTextData = new LinkedHashMap<>();
        uploadTextData.put("type", "6");
        uploadTextData.put("product_code", bean.getProductId());
        uploadTextData.put("score", String.valueOf(bean.getScore()));
        uploadTextData.put("order_id", bean.getOrderId());
        uploadTextData.put("is_quan", bean.isCanShare() ? "2" : "1");
        uploadTextData.put("content[0][text]", bean.getContent());
        ArrayList<String> images = bean.getImages();
        for (int i = 0; i < images.size(); i++) {
            uploadTextData.put("content[0][imgs][" + i + "]", images.get(i));
        }
        return uploadTextData;
    }

    /**
     * 是否全部上传完成
     *
     * @return 图片是否已经上传完成
     */
    private boolean isAllUploadOver() {
        ArrayList<String> images = bean.getImages();
        for (String imageUrl : images) {
            if (TextUtils.isEmpty(imageUrl) || !imageUrl.startsWith("http")) {
                return false;
            }
        }
        return true;
    }

    /**取消发布*/
    public void cancelUpload() {
        isPublishing = false;
        isRequesting = false;
        MallReqInternet.in().cancelRequset(
                new StringBuffer(MallStringManager.mall_addComment)
                        .append(combineParameter().toString()
                        ).toString());
    }

    public EvalutionUploadControl setContent(String content) {
        bean.setContent(content);
        return this;
    }

    public EvalutionUploadControl setOrderId(String orderId) {
        bean.setOrderId(orderId);
        return this;
    }

    public EvalutionUploadControl setProductId(String code) {
        bean.setProductId(code);
        return this;
    }

    public EvalutionUploadControl setScore(int score) {
        bean.setScore(score);
        return this;
    }

    public EvalutionUploadControl setCanShare(boolean canShare) {
        bean.setCanShare(canShare);
        return this;
    }

    public OnPublishCallback getOnPublishCallback() {
        return onPublishCallback;
    }

    public void setOnPublishCallback(OnPublishCallback onPublishCallback) {
        this.onPublishCallback = onPublishCallback;
    }

    /**发布状态回调*/
    public interface OnPublishCallback {
        /**开始发布*/
        void onStratPublish();
        /**发布成功*/
        void onSuccess(Object msg);
        /**发布失败*/
        void onFailed(String msg);
    }

    /**默认上传进度回调*/
    public static abstract class SimpleUploadListNetCallBack implements UploadListNetCallBack {
        @Override public void onProgress(double progress, String uniqueId) { }
        @Override public void onLastUploadOver(boolean flag, String responseStr) { }
        @Override public void onProgressSpeed(String uniqueId, long speed) { }
        @Override public void onSuccess(String url, String uniqueId, JSONObject jsonObject) { }
        @Override public void onFaild(String faild, String uniqueId) { }
    }
}
