package third.aliyun.edit.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *音乐bean
 */
public class MusicBean {
    private int id;
    private String code;
    private String name;
    private String url;
    private String isDownLoad;
    private String locationUrl;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIsDownLoad() {
        return isDownLoad;
    }

    public void setIsDownLoad(String isDownLoad) {
        this.isDownLoad = isDownLoad;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }

    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return status;
    }
    public void setMap(Map<String,String> map){
        if(map!=null){
            setCode(getValue(map,"code"));
            setName(getValue(map,"name"));
            setUrl(getValue(map,"musicUrl"));
            setStatus(getValue(map,"status"));
        }
    }
    public Map<String,String> toMap(){
        Map<String,String> map = new HashMap<>();
        map.put("code",code);
        map.put("name",name);
        map.put("url",url);
        map.put("isDownLoad",isDownLoad);
        map.put("locationUrl",locationUrl);
        map.put("status",status);
        return map;
    }
    private String getValue(Map<String,String> map, String key){
        if(map!=null&&map.containsKey(key)){
            return map.get(key);
        }else return "";
    }
    

    /**
     * 判断当前是否有效，true--是已经下载，false---未成功下载
     * @return
     */
    public boolean isDownLoadState(){
        if(TextUtils.isEmpty(locationUrl)||!"2".equals(isDownLoad)){
            return false;
        }
        return true;
    }
}
