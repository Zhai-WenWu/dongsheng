package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.Tools;
import amodule.user.activity.FriendHome;
import amodule.user.view.UserIconView;
/**
 * 用户榜item
 * @author Administrator
 *
 */
public class UserRankView extends CircleItemBaseRelativeLayout{

	private Context context;
	public UserRankView(Context context){
		super(context);
		this.context= context;
		LayoutInflater.from(context).inflate(R.layout.circle_userrank, this, true);
	}
	/**
	 * 初始化数据
	 * @param map
	 */
	public void initView(Map<String,String> map) {
		TextView user_num=(TextView) findViewById(R.id.user_num);
		
		ImageView iv_userImg=(ImageView) findViewById(R.id.iv_userImg);
		TextView user_name=(TextView) findViewById(R.id.user_name);
		TextView user_hot=(TextView) findViewById(R.id.user_hot);
		UserIconView usericonview=(UserIconView) findViewById(R.id.usericonview);
		//处理数据
		setViewImage(iv_userImg, map.get("img"));
		setViewText(user_name, map.get("nickName"));
		ImageView iv_userType=(ImageView) findViewById(R.id.iv_userType);
		if(map.containsKey("isGourmet"))
			AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), iv_userType);
		
		if(map.containsKey("liveness"))
			setViewText(user_hot, "活跃度"+map.get("liveness"));
		usericonview.setData(map.get("sex"), map.get("lv"), map.get("city"));
		final String userCode = map.get("code");
		//事件處理
		findViewById(R.id.quan_userrank).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FriendHome.class);
				Bundle bundle = new Bundle();
				bundle.putString("code", userCode);
				intent.putExtras(bundle);
				context.startActivity(intent);
			}
		});
		if(map.containsKey("customer_count")){
			user_num.setVisibility(View.VISIBLE);
			int customer_count = Integer.parseInt(map.get("customer_count"));
			user_num.setText(map.get("customer_count"));
			switch (customer_count) {
			case 1:
				String color = Tools.getColorStr(context,R.color.comment_color);
				user_num.setTextColor(Color.parseColor(color));
				break;
			case 2:
				user_num.setTextColor(Color.parseColor("#e59100"));
				break;
			case 3:
				user_num.setTextColor(Color.parseColor("#fde200"));
				break;
			default:
				user_num.setTextColor(Color.parseColor("#cccccc"));
				break;
			}
		}else user_num.setVisibility(View.GONE);
	}
	
}
