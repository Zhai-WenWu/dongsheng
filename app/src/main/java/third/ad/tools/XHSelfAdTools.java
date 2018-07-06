package third.ad.tools;

import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.ConfigHelper;
import acore.logic.ConfigMannager;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
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
    private static boolean mParamsShow;
    private static ArrayList<Map<String, String>> mParamsDatas;
    private static String mIMEI;
    private XHSelfAdTools() {
    }

    public static XHSelfAdTools getInstance() {
        if (mInstance == null) {
            synchronized (XHSelfAdTools.class) {
                if (mInstance == null) {
                    mInstance = new XHSelfAdTools();
                    String value = ConfigHelper.getInstance().getConfigValueByKey("adExt");
                    if (!TextUtils.isEmpty(value)) {
                        Map<String, String> paramsMap = StringManager.getFirstMap(value);
                        mParamsShow = TextUtils.equals(paramsMap.get("isShow"), "2");
                        mParamsDatas = StringManager.getListMapByJson(paramsMap.get("data"));
                        mIMEI =  ToolsDevice.getXhIMEI(XHApplication.in());
                    }
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
        Stream.of(ads)
                .filter(value -> !TextUtils.isEmpty(value))
                .forEach(value -> params.append(value).append(","));
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
                                nativeData.setAdType(map.get("adType"));
                                int showNum = TextUtils.isEmpty(showNumValue)?0:Integer.parseInt(showNumValue);
                                nativeData.setShowNum(showNum);
                                final String dbType = map.get("dbType");
                                nativeData.setDbType(dbType);
                                final String andUrl = map.get("andUrl");
                                final String andShowUrl = map.get("andShowUrl");
                                nativeData.setUrl(TextUtils.equals(dbType, "2") ? combineParams(andUrl, "1") : andUrl);
                                nativeData.setShowUrl(combineParams(andShowUrl, "2"));

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

    /**
     * 组装拼接参数
     * @param originalUrl 点击统计链接
     * @param type 1.点击，2.展示
     * @return 返回拼接后的url
     */
    private String combineParams(String originalUrl, String type) {
        if (mParamsShow && mParamsDatas != null) {
            for (Map<String, String> dataMap : mParamsDatas) {
                ArrayList<Map<String, String>> domains = StringManager.getListMapByJson(dataMap.get("domains"));
                final String clickParams = dataMap.get("clickParams");
                final String showParams = dataMap.get("showParams");
                boolean shouldBreak = false;
                for (Map<String, String> domainMap : domains) {
                    final String domain = domainMap.get("");
                    if (!TextUtils.isEmpty(domain)) {
                        if (!TextUtils.isEmpty(originalUrl) && originalUrl.contains(domain)) {
                            String params = null;
                            switch (type) {
                                case "1":
                                    params = clickParams.replaceAll("##IMEI##",  mIMEI);
                                    break;
                                case "2":
                                    params = showParams.replaceAll("##IMEI##",  mIMEI);
                                    break;
                            }
                            params = !TextUtils.isEmpty(params) && params.startsWith("&") ? params : ("&" + params);
                            originalUrl = originalUrl + params;
                            shouldBreak = true;
                            break;
                        }
                    }
                }
                if (shouldBreak) {
                    break;
                }
            }
        }
        return originalUrl;
    }
}
