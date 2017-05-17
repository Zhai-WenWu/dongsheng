package aplug.basic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.qq.e.comm.util.Md5Util;

import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import acore.tools.FileManager;
import amodule.upload.callback.UploadListNetCallBack;

/**
 * 对于外部操作实际控制类
 * -----------------------上传开启和暂停全部在这里进行操作。
 */
public class BreakPointControl {
    private String type = "";
    private String key = "";//对于七牛存储的绝对地址，有部分组成，第一有服务端的key决定前缀，后命名由本地决定
    private double progress;//进度
    private String url = "";//成功的url
    private String filePath = "";//当前上传文件的绝对路径
    private ReqBreakPointUploadInternet breakPointUploadInternet;
    private String reqState = "0";//请求状态，0：默认，1：准备开始，2：开始上传 3:上传中，4：上传完成，5：上传失败
    private Context context;
    private UploadListNetCallBack uploadListNetCallBack;
    private String uniqueId;//当前对象的唯一标识
    private double now_progress = 0;//当前进度
    private long FileSize;//文件大小
    private double before_progress = 0;//之前的进度
    private Timer timer;

    private final int MAX_RETRY_NUM = 3;//上传失败，可重试最大次数
    private int retryNum;  //上传失败，当前重试次数
    private boolean isPause; //用户暂停上传


    /**
     * 对象
     *
     * @param context
     * @param uniqueId----文件的唯一标示
     * @param filePath----文件的路径
     * @param nowType----当前文件的类型：目前只有两种：img 和video
     */
    public BreakPointControl(Context context, String uniqueId, String filePath, String nowType) {

        loadData(filePath);
        setType(nowType);
        this.uniqueId = uniqueId;
        this.context = context;
        this.filePath = filePath;
        breakPointUploadInternet = new ReqBreakPointUploadInternet();
        BreakPointUploadManager.getInstance().addBreakPointContorl(filePath, this);
    }

    /**
     * 开启上传
     */
    public void start(UploadListNetCallBack callback) {
        this.uploadListNetCallBack = callback;
        setRetryNum(0);
        setPause(false);
        if (BreakPointUploadManager.getInstance().getTokenState(type)) {//token过期
            Log.i("zhangyujian", uniqueId + "::过期请求");
            reqState = "1";
            BreakPointUploadManager.getInstance().ReqToken(context, type);
        } else {
            startBreakPointUpload();
        }

    }

    /**
     * 严禁外部直接操作该类
     */
    private void startBreakPointUpload() {
        reqState = "2";
        setFilePath(filePath);
        Log.i("zhangyujian", uniqueId + "::start开始上传");
        Log.i("zhangyujian", uniqueId + "::filePath::" + filePath);
        Log.i("zhangyujian", uniqueId + "::key::" + key);
        FileSize = fileDataSize(filePath);
        startProgress();
        ReqBreakPointUploadInternet.isCancel = false;
        breakPointUploadInternet.breakPointUpload(filePath, key, BreakPointUploadManager.getInstance().getToken(type), new BreakPointUploadCallBack() {
            @Override
            public void loaded(int flag, String key, double progress, JSONObject jsonObject) {
                if (flag == BreakPointUploadInternet.REQ_UPLOAD) {//上传中
                    now_progress = progress;
                    reqState = "3";
                    if (progress > 0) setProgress(progress);
                    if (uploadListNetCallBack != null)
                        uploadListNetCallBack.onProgress(progress, uniqueId);
                    Log.i("zhangyujian", uniqueId + "::" + flag + "::::key:::" + key + ",\r\n percent::::" + progress);
                } else if (flag == BreakPointUploadInternet.REQ_UPLOAD_OK) {//上传完成
                    timer = null;
                    reqState = "4";
                    retryNum = 0;
                    BreakPointUploadManager.getInstance().delBreakPointUpload(filePath);
                    if (uploadListNetCallBack != null)
                        uploadListNetCallBack.onSuccess(BreakPointUploadManager.getInstance().getDomain(type) + "/" + key, uniqueId, jsonObject);
                    Log.i("zhangyujian", uniqueId + "::" + flag + "::::key:::" + key + ",\r\n jsonObject::::" + jsonObject);
                } else {//上传失败
                    if (!isPause() &&
                            getRetryNum() < MAX_RETRY_NUM) {
                        setRetryNum(getRetryNum() + 1);
                        startBreakPointUpload();
                    } else {
                        setRetryNum(0);
                        timer = null;
                        reqState = "5";
                        BreakPointUploadManager.getInstance().delBreakPointUpload(filePath);
                        if (uploadListNetCallBack != null)
                            uploadListNetCallBack.onFaild(jsonObject == null ? "未知错误" : jsonObject.toString(), uniqueId);
                        Log.i("zhangyujian", uniqueId + "::" + flag + ":::key:::" + key + ",\r\n jsonObject::::" + jsonObject);
                    }
                }
            }
        });
    }

    /**
     * 开启准备状态的下请求类
     */
    public void startReCallback(String nowType) {
        Log.i("zhangyujian", "reqState::" + reqState);
        if ("1".equals(reqState) && type.equals(nowType)) {
            startBreakPointUpload();
        }
    }

    /**
     * 暂停上传
     * 严禁外部直接操作该类
     */
    public void stop() {
        ReqBreakPointUploadInternet.isCancel = true;
        setPause(true);
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(int retryNum) {
        this.retryNum = retryNum;
    }


    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    /**
     * 设置路径，并对key进行处理
     *
     * @param filePath
     */
    public void setFilePath(String filePath) {
        String temp = "";
        if (filePath.contains(".")) {
            temp = filePath.substring(filePath.lastIndexOf("."), filePath.length());
        }
        setKey(BreakPointUploadManager.getInstance().getKey(type) + Md5Util.encode(filePath) + temp);

        this.filePath = filePath;
    }

    /***
     * 读取数据--并进行初始化
     */
    private void loadData(String filePath) {
        try {
            String json = (String) FileManager.loadShared(context, FileManager.BREAKPOINT_UPLOAD_DATA, filePath);
            if (!TextUtils.isEmpty(json)) {
                //对数据进行更新
                JSONObject jsonObject = new JSONObject(json);
                setKey(jsonObject.getString("key"));
                setProgress(jsonObject.getDouble("progress"));
                setUrl(jsonObject.getString("url"));
                setFilePath(jsonObject.getString("filePath"));
            }
        } catch (Exception e) {
        }
    }

    /***
     * 保存数据
     */
    public void saveData() {
        FileManager.saveShared(context, FileManager.BREAKPOINT_UPLOAD_DATA, filePath, getJson().toString());
    }

    /***
     * 删除数据
     */
    public void delData() {
        FileManager.delShared(context, FileManager.BREAKPOINT_UPLOAD_DATA, filePath);
    }

    /**
     * 获取当前数据json
     *
     * @return
     */
    private JSONObject getJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", key);
            jsonObject.put("progress", progress);
            jsonObject.put("url", url);
            jsonObject.put("filePath", filePath);
            return jsonObject;
        } catch (Exception e) {
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReqState() {
        return reqState;
    }

    public void setReqState(String reqState) {
        this.reqState = reqState;
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    private long fileDataSize(String filePath) {
        File f = new File(filePath);
        if (f.exists() && f.isFile()) {
            Log.i("zhangyujian", uniqueId + "::" + f.length() + "：：lenght");
            return f.length();
        }
        return 0;
    }

    private void startProgress() {
        if (timer == null)
            timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (now_progress > before_progress && FileSize > 0 && uploadListNetCallBack != null) {
                    double progress = now_progress - before_progress;
                    long size = (long) (FileSize * progress);
                    Log.i("zhangyujian", uniqueId + "::速度::" + size);
                    before_progress = now_progress;
                    uploadListNetCallBack.onProgressSpeed(uniqueId, size);
                } else return;

            }
        }, 1000, 1000);
    }
}
