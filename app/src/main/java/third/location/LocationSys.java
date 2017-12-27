package third.location;

import acore.tools.FileManager;
import amodule.main.Main;
import aplug.basic.XHInternetCallBack;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

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
			if(Main.mainActivity != null) {
				FileManager.saveShared(Main.mainActivity, FileManager.file_location, FileManager.file_location, lng + "#" + lat);
				XHInternetCallBack.setIsCookieChange(true);
			}
		}
	}
}
