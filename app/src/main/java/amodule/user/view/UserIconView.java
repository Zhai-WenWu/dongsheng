package amodule.user.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;

/**
 * 用户图标控件
 * @author FangRuijiao
 */
public class UserIconView extends LinearLayout{

	private Context mContext;
	private View mParentView;

	public UserIconView(Context context) {
		super(context);
		mContext = context;
		init(context);
	}
	
	public UserIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(context);
	}
	
	/**
	 * 初始化UI
	 */
	public void init(Context context){
		setGravity(Gravity.CENTER);
		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater inflater = LayoutInflater.from(context);
		mParentView =  inflater.inflate(R.layout.a_user_icon_view, null);
		addView(mParentView);
		
	}
	
	/**
	 * 设置数据
	 * @param userData : 用户的json数据
	 */
	public void setData(String userData){
		 ArrayList<Map<String, String>>  array = StringManager.getListMapByJson(userData);
		 if(null != array && array.size() > 0){
			 String sex = "",lv = "",city = "";
			 Map<String, String> map = array.get(0);
			 if(map.containsKey("sex")){
				 sex = map.get("sex");
			 }
			 if(map.containsKey("lv")){
				lv = map.get("lv");
			 }
			 if(map.containsKey("city")){
				city = map.get("city");
			 }
			 setData(sex,lv,city);
		 }else{
			 this.setVisibility(View.GONE);
		 }
	}
	
	public int setData(String sex,String lv,String city){
		int showNum = 0;
		boolean isHavaData = false;
		if(!TextUtils.isEmpty(sex)){
			ImageView iv_userSex = (ImageView)mParentView.findViewById(R.id.a_user_icon_sex);
			if("2".equals(sex)){ //男
				iv_userSex.setVisibility(View.VISIBLE);
				iv_userSex.setImageResource(R.drawable.z_z_ico_boy);
				isHavaData = true;
				showNum++;
			}else if("3".equals(sex)){ //女
				iv_userSex.setVisibility(View.VISIBLE);
				iv_userSex.setImageResource(R.drawable.z_z_ico_girl);
				isHavaData = true;
				showNum++;
			}else{
				iv_userSex.setVisibility(View.GONE);
			}
		}else{
			mParentView.findViewById(R.id.a_user_icon_sex).setVisibility(View.GONE);
		}
		if(!TextUtils.isEmpty(lv) && !"null".equals(lv)){
			 ImageView iv_userLeve = (ImageView)mParentView.findViewById(R.id.a_user_icon_leve);
			 isHavaData = AppCommon.setLvImage(Integer.parseInt(lv), iv_userLeve);
			if(isHavaData)showNum++;
		}else{
			mParentView.findViewById(R.id.a_user_icon_leve).setVisibility(View.GONE);
		}
		if(!TextUtils.isEmpty(city)){
			TextView mLocation = (TextView) mParentView.findViewById(R.id.a_user_icon_location);
			mLocation.setVisibility(View.VISIBLE);
			mLocation.setText(city);
			isHavaData = true;
			showNum += city.length()/2;
		}else{
			mParentView.findViewById(R.id.a_user_icon_location).setVisibility(View.GONE);
		}
		if(isHavaData)
			this.setVisibility(View.VISIBLE);
		else 
			this.setVisibility(View.GONE);
		return showNum;
	}

}
