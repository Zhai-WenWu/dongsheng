package third.location;

import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.XHInternetCallBack;

/**
 * Description :
 * PackageName : acore.logic
 * Created by mrtrying on 2018/6/4 14:39.
 * e_mail : ztanzeyu@gmail.com
 */
public class LocationHelper {
    private static volatile LocationHelper instance = null;

    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationClientOption;
    private AMapLocation mCurrentLocation;

    private List<LocationListener> mListeners;

    private LocationHelper() {
        mLocationClientOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        mLocationClientOption.setInterval(5 * 1000 * 60);
        mLocationClientOption.setNeedAddress(true);
        mListeners = new ArrayList<>();
    }

    public static LocationHelper getInstance() {
        if (null == instance) {
            synchronized (LocationHelper.class) {
                if (null == instance) {
                    instance = new LocationHelper();
                }
            }
        }
        return instance;
    }

    public void registerLocationListener(LocationListener listener) {
        synchronized (LocationHelper.class) {
            if (listener != null && !mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    public void unregisterLocationListener(LocationListener listener) {
        synchronized (LocationHelper.class) {
            if (listener != null) {
                mListeners.remove(listener);
            }
        }
    }

    public void startLocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(XHApplication.in());
            mLocationClient.setLocationOption(mLocationClientOption);
            mLocationClient.setLocationListener(aMapLocation -> {
                Log.i("tzy", "startLocation: ");
                mCurrentLocation = aMapLocation;
                GeocodeSearch geocodeSearch = new GeocodeSearch(XHApplication.in());
                geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                        // TODO: 2018/7/25 获取详细地址信息，包括乡镇 注意判空
                        if(geocodeSearchCallBack!=null){
                            geocodeSearchCallBack.onRegeocodeSearched(regeocodeResult);
                        }
//                    Log.i("TAG", "onRegeocodeSearched: " + regeocodeResult.getRegeocodeAddress().getTownship() + "  code = " + regeocodeResult.getRegeocodeAddress().getTowncode());
                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                    }
                });
                LatLonPoint latLonPoint = new LatLonPoint(getLatitude(), getLongitude());
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20, GeocodeSearch.AMAP);
                geocodeSearch.getFromLocationAsyn(query);
                Iterator<LocationListener> iterator = mListeners.iterator();
                while (iterator.hasNext()) {
                    LocationListener listener = iterator.next();
                    if (listener != null) {
                        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                            listener.onSuccess(aMapLocation);
                            onLocationSuccess(aMapLocation);
                        } else {
                            listener.onFailed();
                        }
                    }
                }
            });
        }
        mLocationClient.stopLocation();
        mLocationClient.startLocation();
    }

    public boolean isStarted() {
        if (mLocationClient == null)
            return false;
        return mLocationClient.isStarted();
    }

    public void stopLocation() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
    }

    public void destroy() {
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
    }

    public double getLatitude() {
        return mCurrentLocation != null ? mCurrentLocation.getLatitude() : 0.0;
    }

    public double getLongitude() {
        return mCurrentLocation != null ? mCurrentLocation.getLongitude() : 0.0;
    }

    public interface LocationListener {
        void onSuccess(AMapLocation value);
        void onFailed();
    }

    private void onLocationSuccess(AMapLocation aMapLocation){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> directCities = new ArrayList<>(4);
                directCities.add("北京");
                directCities.add("上海");
                directCities.add("天津");
                directCities.add("重庆");
                String city = aMapLocation.getCity();
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
                    for (Map<String, String> cityMap : cityMaps) {
                        if (cityMap != null) {
                            if (isDirectCity) {
                                String cityId = cityMap.get(city);
                                if (!TextUtils.isEmpty(cityId)) {
                                    firstId = cityId;
                                    Map<String, String> districtMap = StringManager.getFirstMap(cityMap.get(cityId));
                                    String districtId = districtMap.get(aMapLocation.getDistrict());
                                    if (!TextUtils.isEmpty(districtId)) {
                                        secondId = districtId;
                                        break;
                                    }
                                }
                            } else {
                                String provinceId = cityMap.get(aMapLocation.getProvince());
                                if (!TextUtils.isEmpty(provinceId)) {
                                    firstId = provinceId;
                                    Map<String, String> tempCityMap = StringManager.getFirstMap(cityMap.get(provinceId));
                                    String cityId = tempCityMap.get(city);
                                    if (!TextUtils.isEmpty(cityId)) {
                                        secondId = cityId;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    FileManager.saveShared(XHApplication.in(), FileManager.file_location, FileManager.file_location, aMapLocation.getLongitude() + "#" + aMapLocation.getLatitude() + "#" + (TextUtils.isEmpty(firstId) ? "" : firstId) + "#" + (TextUtils.isEmpty(secondId) ? "" : secondId));
                    XHInternetCallBack.setIsCookieChange(true);
                }
            }
        }).start();
    }
    public interface  GeocodeSearchCallBack{
        void onRegeocodeSearched(RegeocodeResult regeocodeResult);
    }
    private GeocodeSearchCallBack geocodeSearchCallBack;
    public void setGeocodeSearchCallBack(GeocodeSearchCallBack callBack){
        this.geocodeSearchCallBack= callBack;
    }
}
