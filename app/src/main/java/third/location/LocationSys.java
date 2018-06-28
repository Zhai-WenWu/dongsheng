package third.location;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import java.net.FileNameMap;
import java.util.ArrayList;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.XHInternetCallBack;
import xh.basic.tool.UtilFile;

public class LocationSys implements BDLocationListener {

    public LocationClient mLocationClient;
    private LocationSysCallBack mCallBack;

    public LocationSys(Context context){
        //初始化地理位置控制器
        mLocationClient = new LocationClient(context);
        mLocationClient.registerLocationListener(this);
        initLocation();
    }

    //开始定位
    private void initLocation(){
        //设置相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); //打开gps
        option.setLocationMode(LocationMode.Hight_Accuracy);//获取地理位置的模式
        option.setCoorType("gcj02");//设置获取为的类型,例如:高精度 、低功耗 、仅设备；("bd09ll")为百度自己的经纬度坐标系格式
        option.setIsNeedAddress(true); //需要知道地理位置
        int span = 15 * 60 * 1000;
        option.setScanSpan(span);//设置发起定位请求的间隔时间为60000ms
        mLocationClient.setLocOption(option);
    }

    public void starLocation(LocationSysCallBack callBack){
        mCallBack = callBack;
        mLocationClient.start();
    }
    public void stopLocation(){
        mLocationClient.stop();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        String province = location.getProvince();
        String city = location.getCity();
        String district = location.getDistrict();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if(TextUtils.isEmpty(province) || TextUtils.isEmpty(city) || TextUtils.isEmpty(district) || latitude < 0 || longitude < 0){
            if(mCallBack != null) mCallBack.onLocationFail();
            return;
        }
        if(mCallBack != null)
            mCallBack.onLocationSuccess("", "", province, city, district,String.valueOf(latitude), String.valueOf(longitude));
    }
    public static abstract class LocationSysCallBack{
        /**
         * 定位失败
         */
        public abstract void onLocationFail();
        /**
         * 定位成功,经纬度放入cookie里
         * @param country ： 国家
         * @param countryCode ： 国家code
         * @param province ：省份
         * @param city ： 市
         * @param district ：区
         * @param lat ：维度
         * @param lng ：经度
         */
        public void onLocationSuccess(String country,String countryCode,String province,String city,String district,String lat,String lng){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String locationInfoJson = UtilFile.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, FileManager.location_info).toString();
                    if (!TextUtils.isEmpty(locationInfoJson)) {
                        Map<String, String> locationInfoMap = StringManager.getFirstMap(locationInfoJson);
                        String firstAddressId = locationInfoMap.get("firstAddressId");
                        String secondAddressId = locationInfoMap.get("secondAddressId");
                        FileManager.saveShared(XHApplication.in(), FileManager.file_location, FileManager.file_location, lng + "#" + lat + "#" + (TextUtils.isEmpty(firstAddressId) ? "" : firstAddressId) + "#" + (TextUtils.isEmpty(secondAddressId) ? "" : secondAddressId));
                        XHInternetCallBack.setIsCookieChange(true);
                    } else {
                        ArrayList<String> directCities = new ArrayList<>(4);
                        directCities.add("北京");
                        directCities.add("上海");
                        directCities.add("天津");
                        directCities.add("重庆");
                        if (city != null) {
                            boolean isDirectCity = false;
                            for (String directCity : directCities) {
                                if (city.contains(directCity)) {
                                    isDirectCity = true;
                                    break;
                                }
                            }
                            String cityJson = FileManager.getFromAssets(XHApplication.in(), "city.json");
                            ArrayList<Map<String, String>> cityMaps = StringManager.getListMapByJson(cityJson);
                            String firstId = null;
                            String secondId = null;
                            for (Map<String, String> cityMap : cityMaps){
                                if (cityMap != null) {
                                    if (isDirectCity) {
                                        String cityId = cityMap.get(city);
                                        if (!TextUtils.isEmpty(cityId)) {
                                            firstId = cityId;
                                            Map<String, String> districtMap = StringManager.getFirstMap(cityMap.get(cityId));
                                            String districtId = districtMap.get(district);
                                            if (!TextUtils.isEmpty(districtId)) {
                                                secondId = districtId;
                                                break;
                                            }
                                        }
                                    } else {
                                        String provinceId = cityMap.get(province);
                                        if (!TextUtils.isEmpty(provinceId)) {
                                            firstId = provinceId;
                                            Map<String, String> tempCityMap = StringManager.getFirstMap(cityMap.get(provinceId));
                                            String cityId = tempCityMap.get(city);
                                            if (!TextUtils.isEmpty(cityId)) {
                                                secondId  = cityId;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            FileManager.saveShared(XHApplication.in(), FileManager.file_location, FileManager.file_location, lng + "#" + lat + "#" + (TextUtils.isEmpty(firstId) ? "" : firstId) + "#" + (TextUtils.isEmpty(secondId) ? "" : secondId));
                            XHInternetCallBack.setIsCookieChange(true);
                        }
                    }
                }
            }).start();
        }
    }
}
