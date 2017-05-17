package amodule.quan.tool;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiangha.R;

import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.Tools;
import amodule.quan.activity.upload.UploadSubjectNew;
import third.location.LocationSys;

public class UploadSubjectLocationControl {
	private LocationSys mLocationSys;
	public Map<String,String> locationMap;
	//---------定位------------
	private TextView tv_location;
	private ProgressBar pb_location;
	private String location = "没有定位";
	private ImageView iv_location;
	//--------- end 定位------------
	private boolean isLocation = false,isOneShowLocation=true;
		
	public UploadSubjectLocationControl(UploadSubjectNew upload){
		mLocationSys = upload.mLocationSys;
		upload.findViewById(R.id.ll_location).setOnClickListener(upload);
		//获取地理信息
		iv_location = (ImageView)upload.findViewById(R.id.iv_location);
		tv_location=(TextView) upload.findViewById(R.id.tv_location);
		pb_location = (ProgressBar)upload.findViewById(R.id.pb_location);
		locationMap = new LinkedHashMap<String, String>();
	}
	
	/**
	 * 定位按钮点击处理
	 */
	public void onLocationClick(){
		if(isLocation){
			//停止定位
			mLocationSys.stopLocation();
			location = "显示地址";
			pb_location.setVisibility(View.GONE);
			iv_location.setVisibility(View.VISIBLE);
			iv_location.setBackgroundResource(R.drawable.z_quan_location_off);

			tv_location.setTextColor(Color.parseColor(Tools.getColorStr(tv_location.getContext(),R.color.comment_color)));
		}else{	
			mLocationSys.starLocation(locationCallBack);
			location = "正在定位";
			pb_location.setVisibility(View.VISIBLE);
			iv_location.setVisibility(View.GONE);
			tv_location.setTextColor(Color.GRAY);
		}
		tv_location.setText(location);
		isLocation = !isLocation;
	}
	
	/**
	 * 显示请求字符串
	 * @param str
	 */
	private void logMsg(String str) {
		try {
			pb_location.setVisibility(View.GONE);
			iv_location.setVisibility(View.VISIBLE);
			int res = R.drawable.z_quan_location_on;
			int color = Color.BLACK;
			isLocation = true;
			if(str == null || str.equals("") || str.equals("null") ){
				isLocation = false;
				if(isOneShowLocation){
					str="显示地址";
				}
				else
					str = "定位失败";
				res = R.drawable.z_quan_location_off;
				color = Color.GRAY;
			}
			
			iv_location.setBackgroundResource(res);
			tv_location.setText(str);
			tv_location.setTextColor(color);
			isOneShowLocation = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getLocationJson(){
		return Tools.map2Json(locationMap);
	}
	
	public boolean getIsLocation(){
		return isLocation;
	}
		
	private LocationSys.LocationSysCallBack locationCallBack = new LocationSys.LocationSysCallBack() {
		
		@Override
		public void onLocationFail() {
			logMsg("定位失败");
		}

		@Override
		public void onLocationSuccess(String country, String countryCode,String province, String city, String district, String lat,String lng) {
			super.onLocationSuccess(country, countryCode, province, city, district, lat, lng);
			locationMap.put("country", country);
			locationMap.put("countryCode", countryCode);
			locationMap.put("province", province);
			locationMap.put("city", city);
			locationMap.put("district", district);
			locationMap.put("lat", "" + lat);
			locationMap.put("lng", "" + lng);
			String showText;
			if(province.equals(city)){
				showText = city + " " + district;
			}else{
				showText = province + " " + city;
			}
			logMsg(showText);
		}
	};
}
