package third.ad.tools;

import android.text.TextUtils;

import com.annimon.stream.Stream;

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


    public void loadNativeData(List<String> ads,final XHSelfCallback callback) {
        if(ads == null || ads.isEmpty()){
            if (callback != null) {
                callback.onNativeFail();
            }
            return;
        }
        StringBuffer params = new StringBuffer(API_AD_GETADDATA);
        params.append("?ids=");
        Stream.of(ads).forEach(value -> params.append(value).append(","));
        params.replace(params.length()-1,params.length(),"");
        ReqInternet.in().doGet(params.toString(), new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<XHSelfNativeData> list = new ArrayList<>();
                    Map<String, String> data = StringManager.getFirstMap(o);
                    for (String key : ads) {
                        if(data.containsKey(key)){
                            Map<String,String> map = StringManager.getFirstMap(data.get(key));
                            if (!map.isEmpty() && !TextUtils.isEmpty(map.get("id"))) {
                                XHSelfNativeData nativeData = new XHSelfNativeData();
                                nativeData.setId(map.get("id"));
                                nativeData.setPositionId(map.get("position_id"));
                                nativeData.setTitle(map.get("title"));
                                nativeData.setDesc(map.get("desc"));
                                nativeData.setBrandName(map.get("brandName"));
                                String showNumValue = map.get("showNum");
                                int showNum = TextUtils.isEmpty(showNumValue)?0:Integer.parseInt(showNumValue);
                                nativeData.setShowNum(showNum);
                                nativeData.setUrl(map.get("andUrl"));
                                nativeData.setAdType(map.get("adType"));
                                nativeData.setDbType(map.get("dbType"));
                                nativeData.setLogoImage(map.get("logoImg"));
                                nativeData.setUpdateTime(map.get("updateTime"));
                                Map<String,String> bigImageMap = StringManager.getFirstMap(map.get("big"));
                                nativeData.setBigImage(bigImageMap.get(""));
                                Map<String,String> littleImageMap = StringManager.getFirstMap(map.get("little"));
                                nativeData.setLittleImage(littleImageMap.get(""));
                                list.add(nativeData);
                            }else{
                                list.add(null);
                            }
                        }else{
                            list.add(null);
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
        void onNativeLoad(ArrayList<XHSelfNativeData> list);

        void onNativeFail();
    }
}
