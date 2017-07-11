package aplug.basic;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import xh.basic.internet.InterCallback;
import xh.basic.internet.UtilInternet;
import xh.basic.internet.UtilInternetImg;

public class ReqInternet extends UtilInternet {
    private static ReqInternet instance = null;
    private static Context initContext = null;

    private ReqInternet(Context context) {
        super(context);
    }

    public static ReqInternet init(Context context) {
        initContext = context;
        return in();
    }

    public static ReqInternet in() {
        if (instance == null)
            instance = new ReqInternet(initContext);
        return instance;
    }

    /**
     * 单独获取标准header头
     *
     * @param context
     *
     * @return
     */
    public Map<String, String> getHeader(Context context) {
        InternetCallback callback = new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object msg) {
            }
        };
        Map<String, String> header = callback.getReqHeader(new HashMap<String, String>(), "", new LinkedHashMap<String, String>());
        callback.finish();
        return header;
    }

    @Override
    public void doGet(String url, InterCallback callback) {
        url=StringManager.httpState ? url.replace("https","http") : url;

        url = StringManager.replaceUrl(url);
        super.doGet(url, callback);
    }

    @Override
    public void doPost(String actionUrl, String param, InterCallback callback) {
        actionUrl=StringManager.httpState ? actionUrl.replace("https","http") : actionUrl;
        actionUrl = StringManager.replaceUrl(actionUrl);
        super.doPost(actionUrl, param, callback);
    }

    /**
     * 上传图片接口，不同是，上传图片的超时时间变了
     *
     * @param actionUrl
     * @param map
     * @param callback
     */
    public void doPostImg(String actionUrl, LinkedHashMap<String, String> map, InterCallback callback) {
        actionUrl = StringManager.replaceUrl(actionUrl);
        UtilInternetImg.in().doPost(actionUrl, map, callback);
    }

    @Override
    public void doPost(String actionUrl, LinkedHashMap<String, String> map, InterCallback callback) {
        actionUrl=StringManager.httpState ? actionUrl.replace("https","http") : actionUrl;
        actionUrl = StringManager.replaceUrl(actionUrl);
        super.doPost(actionUrl, map, callback);
    }

    @Override
    public void upLoadMP4(String actionUrl, String key, String path, InterCallback interCallback) {
        actionUrl = StringManager.replaceUrl(actionUrl);
        super.upLoadMP4(actionUrl, key, path, interCallback);
    }

    @Override
    public void getInputStream(String httpUrl, final InterCallback callback) {
        String url = StringManager.stringToMD5(httpUrl);
        final String filePath = FileManager.getSDDir() + url;
        final Handler handler = new Handler(initContext.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case REQ_OK_IS:
                        InputStream is = FileManager.loadFile(msg.obj.toString());
                        callback.loaded(REQ_OK_IS, msg.obj.toString(), is);//
                        break;
                }
                return false;
            }
        });
        super.getInputStream(httpUrl, new InterCallback(initContext) {
            @Override
            public void loaded(int flag, final String url, final Object msg) {
                if (flag >= REQ_OK_IS) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FileManager.saveFileToCompletePath(filePath, (InputStream) msg, false);
                            Message message = handler.obtainMessage(REQ_OK_IS, filePath);
                            handler.sendMessage(message);
                        }
                    }).start();
                } else if (callback != null) {
                    callback.loaded(flag, url, msg);
                }
            }
        });
    }

}
