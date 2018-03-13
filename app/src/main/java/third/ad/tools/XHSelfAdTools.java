package third.ad.tools;

import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.db.bean.XHSelfNativeData;

import static acore.tools.StringManager.API_AD_GETADDATA;

/**
 * Description :
 * PackageName : third.ad.tools
 * Created by mrtrying on 2018/3/8 16:33:12.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHSelfAdTools {

    private static volatile XHSelfAdTools mInstance = null;

    private XHSelfAdTools() {
    }

    public static XHSelfAdTools getInstance() {
        if (mInstance == null) {
            synchronized (XHSelfAdTools.class) {
                if (mInstance == null) {
                    mInstance = new XHSelfAdTools();
                }
            }
        }
        return mInstance;
    }


    public void loadNativeData(final XHSelfCallback callback) {
        ReqInternet.in().doGet(API_AD_GETADDATA, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i > ReqInternet.REQ_OK_STRING) {
                    List<XHSelfNativeData> list = new ArrayList<>();
                    List<Map<String, String>> data = StringManager.getListMapByJson(o);
                    for (Map<String, String> map : data) {
                        map = StringManager.getFirstMap(map.get("data"));
                        if (!map.isEmpty()) {
                            XHSelfNativeData nativeData = new XHSelfNativeData();
                            nativeData.setId(map.get("id"));
                            nativeData.setTitle(map.get("title"));
                            nativeData.setDesc(map.get("desc"));
                            String showNumValue = map.get("showNum");
                            int showNum = TextUtils.isEmpty(showNumValue)?0:Integer.parseInt(showNumValue);
                            nativeData.setShowNum(showNum);
                            nativeData.setUrl(map.get("url"));
                            nativeData.setType(map.get("type"));
                            nativeData.setUpdateTime(map.get("updateTime"));
                            Map<String,String> imgsMap = StringManager.getFirstMap(map.get("imgs"));
                            nativeData.setBigImage(imgsMap.get("big"));
                            nativeData.setLittleImage(imgsMap.get("little"));
                            list.add(nativeData);
                        }
                    }
                    if (callback != null) {
                        callback.onNativeLoad(list);
                    }
                } else {
                    if (callback != null) {
                        callback.onNativeFail();
                    }
                }
            }
        });
    }

    public interface XHSelfCallback {
        public void onNativeLoad(List<XHSelfNativeData> list);

        public void onNativeFail();
    }
}
