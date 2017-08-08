package amodule.quan.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.Tools;
import third.location.LocationSys;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/26 14:21.
 * E_mail : ztanzeyu@gmail.com
 */

public class CircleLocationView extends RelativeLayout {
    private LocationSys mLocationSys;
    public Map<String,String> locationMap;

    private ProgressBar pb_location;
    private ImageView iv_location;
    private TextView tv_location;

    private String location = "没有定位";

    private boolean isLocation = false,isOneShowLocation=true;

    public CircleLocationView(Context context) {
        this(context,null);
    }

    public CircleLocationView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleLocationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.circle_location_layout,this);
        init();
    }

    private void init() {
        pb_location = (ProgressBar) findViewById(R.id.pb_location);
        iv_location = (ImageView) findViewById(R.id.iv_location);
        tv_location = (TextView) findViewById(R.id.tv_location);
        //初始化定位map
        locationMap = new LinkedHashMap<String, String>();
        //初始化定位class
        mLocationSys = new LocationSys(getContext());
    }

    /** 定位按钮点击处理 */
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
