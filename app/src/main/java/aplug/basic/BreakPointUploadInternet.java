package aplug.basic;

import android.text.TextUtils;
import android.util.Log;

import com.qiniu.android.common.AutoZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.Recorder;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;
import com.qq.e.comm.util.Md5Util;

import org.json.JSONObject;

import java.io.File;

import xh.basic.BasicConf;
import xh.basic.tool.UtilFile;

import static xh.basic.internet.UtilInternet.REQ_EXP;
import static xh.basic.internet.UtilInternet.REQ_STRING_ERROR;

/**
 *
 */
public class BreakPointUploadInternet {

    public static int CHUCKSIZE=256*1024;//设置分片上传，每片大小为256kb
    public static int PUTTHRESHHOLD=256*1024;// 启用分片上传阀值。默认512K
    public static int server_timeout=60;// 服务器响应超时。默认60秒

    public static final int REQ_UPLOAD_Error = 40;//上传视频
    public static final int REQ_UPLOAD_STOP=45;//点击暂停成功
    /** 请求成功 */
    public static final int REQ_UPLOAD = 50;//上传中
    public static final int REQ_UPLOAD_OK = 60;//上传完成

//    public String key="";//<指定七牛服务上的文件名，或 null>;
//    public String token = "";//<从服务端SDK获取>;
    private  String dirPath = UtilFile.getSDDir()+"/cache/";//<断点记录文件保存的文件夹位置>
    public BreakPointUploadInternet(){}
    private Recorder recorder;
    private  UploadManager uploadManager;
    public static boolean isCancel=false;//当前状态
    /**
     *断点上传-----一个上传对应一个方法一个上传对象。
     * @param filePath // 文件路径
     * @param callBack
     */
    public void breakPointUpload(final String filePath, String key, String token, final BreakPointUploadCallBack callBack) {
        try {

            if(recorder==null) recorder = new FileRecorder(dirPath);
            //默认使用key的url_safe_base64编码字符串作为断点记录文件的文件名
            //避免记录文件冲突（特别是key指定为null时），也可自定义文件名(下方为默认实现)：
            KeyGenerator keyGen = new KeyGenerator() {
                public String gen(String key, File file) {
                    // 不必使用url_safe_base64转换，uploadManager内部会处理
                    // 该返回值可替换为基于key、文件内容、上下文的其它信息生成的文件名
                    return key + "_._" + new StringBuffer(file.getAbsolutePath()).reverse();
                }
            };
              boolean https = true;
              Zone z1 = new AutoZone(https, null);
            //初始化操作
            Configuration configuratio = new Configuration.Builder()
                    .zone(Zone.zone0)//华东地区
                    .chunkSize(CHUCKSIZE)
                    .putThreshhold(PUTTHRESHHOLD)
                    .connectTimeout(BasicConf.net_timeout)
                    .responseTimeout(server_timeout)
                    // recorder分片上传时，已上传片记录器
                    // keyGen分片上传时，生成标识符，用于片记录器区分是哪个文件的上传记录
                    .recorder(recorder, keyGen)
                    .build();
            // 重用uploadManager。一般地，只需要创建一个uploadManager对象
            if(uploadManager==null){
                uploadManager = new UploadManager(configuratio);
            }

            uploadManager.put(filePath, key, token,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            Log.i("zhangyujian","key::"+key+":::info::"+info.toString());
                            //res包含hash、key等信息，具体字段取决于上传策略的设置。
                            if(res!=null&&res.has("key")&&res.has("hash")){//包含两个参数标示成功
                                callBack.loaded(REQ_UPLOAD_OK,key,100,res);
                            }else if(res!=null&&res.has("error")){//有错误日志
                                callBack.loaded(REQ_UPLOAD_Error,key,100,res);
                            }else if("cancelled by user".equals(!TextUtils.isEmpty(info.error)?info.error:"")){//无法识别错误
                                callBack.loaded(REQ_UPLOAD_STOP,key,100,res);
                            }else if(!TextUtils.isEmpty(key)){//重复上传
                                try {
                                    if (res == null) {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("hash", Md5Util.encode(key));
                                        res=jsonObject;
                                    }
                                    callBack.loaded(REQ_UPLOAD_OK, key, 100, res);
                                }catch (Exception e){e.printStackTrace();}
                            }else{
                                callBack.loaded(REQ_STRING_ERROR,key,100,res);
                            }
                        }
                    },
                    new UploadOptions(null, null, false,
                            new UpProgressHandler() {
                                public void progress(String key, double percent) {//上传进度
                                    callBack.loaded(REQ_UPLOAD, key, percent,  null);
                                }
                            },
                            new UpCancellationSignal() {
                            @Override
                            public boolean isCancelled() {
                                Log.i("zhangyujian","isCancell()::"+isCancel);
                                return isCancel;
                            }
                    }));
        }catch (Exception e){
            e.printStackTrace();
            callBack.loaded(REQ_EXP,key,0,null);
        }
    }
    /**
     * 断点上传。多区域位置：目前是华东地区,
     *
     * //  国内https上传
     //  boolean https = true;
     //  Zone z1 = new AutoZone(https, null);
     //  Configuration config = new Configuration.Builder().zone(z1).build();

     //  华东
     Configuration config = new Configuration.Builder().zone(Zone.zone0).build();

     //华北
     //  Configuration config = new Configuration.Builder().zone(Zone.zone1).build();

     //华南
     //  Configuration config = new Configuration.Builder().zone(Zone.zone2).build();

     //北美
     //  Configuration config = new Configuration.Builder().zone(Zone.zoneNa0).build();


     //  海外https上传
     //  String[] upIps = {"115.231.97.46"};
     //  ServiceAddress up = new ServiceAddress("https://upload.qbox.me", upIps);
     //  Zone z0 = new FixedZone(up, null);
     //  Configuration config = new Configuration.Builder().zone(z0) .build();
     */


}
