package com.quze.videorecordlib.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * Description :
 * PackageName : com.quze.videorecordlib.util
 * Created by mrtrying on 2018/8/17 11:38.
 * e_mail : ztanzeyu@gmail.com
 */
public class LocationUtil {

    private static final LocationUtil instance = new LocationUtil();

    public static LocationUtil instance() {
        return instance;
    }

    public void getLocation(Context context, final LocationCallback callback) {
        if (context == null) return;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        String locationProvider;
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是网络定位
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS定位
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            Log.e("xianghaTag", "没有可用的位置提供器");
            return;
        }
        //3.获取上次的位置，一般第一次运行，此值为null
        Location location = locationManager.getLastKnownLocation(locationProvider);
        // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
        locationManager.requestLocationUpdates(locationProvider, 0, 0, new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            // 如果位置发生变化，重新显示
            @Override
            public void onLocationChanged(Location location) {
                if (callback != null) {
                    callback.onLocationChanged(location);
                }
            }
        });
    }

    public interface LocationCallback {
        void onLocationChanged(Location location);
    }
}
