package amodule.dish.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.DetailDish;
import amodule.quan.view.CircleItemBaseRelativeLayout;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;
import xh.basic.tool.UtilString;

/**
 * 今日佳作itemView
 * 
 * @author Administrator
 *
 */
public class TodayGoodView extends CircleItemBaseRelativeLayout {

	private Context context;
	private final int USER_TYPE=1;
	private final int DISH_TYPE=2;
	private String user_code;
	public TodayGoodView(Context context) {
		super(context);
		this.context= context;
		LayoutInflater.from(context).inflate(R.layout.a_home_todaygood_item, this);
	}

	public void setData(Map<String, String> map) {
		if (map.containsKey("isPast") && "1".equals(map.get("isPast"))) {
			findViewById(R.id.content_rela).setVisibility(View.GONE);
			findViewById(R.id.des_rela).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.content_rela).setVisibility(View.VISIBLE);
			findViewById(R.id.des_rela).setVisibility(View.GONE);
			ImageView item_img = (ImageView) findViewById(R.id.item_img);
			ImageViewVideo item_model_video = (ImageViewVideo) findViewById(R.id.item_model_video);
			ImageView iv_userImg = (ImageView) findViewById(R.id.iv_userImg);
			ImageView iv_userType = (ImageView) findViewById(R.id.iv_userType);
			TextView user_name = (TextView) findViewById(R.id.user_name);
			TextView item_title = (TextView) findViewById(R.id.item_title_tv);
			TextView item_score = (TextView) findViewById(R.id.item_score);
			TextView tv_location_in = (TextView) findViewById(R.id.tv_location_in);
			setViewText(item_title, map.get("name"));
			if(map.containsKey("score")&&!TextUtils.isEmpty(map.get("score"))){
				if(Integer.parseInt(map.get("score"))>0){
					item_score.setVisibility(View.VISIBLE);
					setViewText(item_score, map.get("score")+"分");
				}else item_score.setVisibility(View.INVISIBLE);
			}else
				item_score.setVisibility(View.INVISIBLE);
			//是否是视频帖hasVideo=2
			if(map.containsKey("hasVideo")&&"2".equals(map.get("hasVideo"))){
				item_img.setVisibility(View.GONE);
				item_model_video.setVisibility(View.VISIBLE);
				item_model_video.parseItemImg(ScaleType.CENTER_CROP, map.get("img"), "2", false, R.drawable.i_nopic, LoadImage.SAVE_CACHE);
				item_model_video.playImgWH=Tools.getDimen(context, R.dimen.dp_50);
			}else{
				item_img.setVisibility(View.VISIBLE);
				item_model_video.setVisibility(View.GONE);
				setViewImage(item_img, map.get("img"));
			}
			ArrayList<Map<String, String>> list = UtilString.getListMapByJson(map.get("customers"));
			tv_location_in.setText(list.get(0).containsKey("nickName")?list.get(0).get("nickName"):"");
			setViewText(user_name, list.get(0).get("nickName"));
			setViewImage(iv_userImg, list.get(0).get("img"));
			user_code=list.get(0).get("code");
			if(list.get(0).containsKey("isGourmet")){
				AppCommon.setUserTypeImage(Integer.parseInt(list.get(0).get("isGourmet")), iv_userType);
			}
			RelativeLayout user_rela=(RelativeLayout) findViewById(R.id.user_rela);
			
			setListener(map, user_rela, USER_TYPE);
			setListener(map, item_img, DISH_TYPE);
			setListener(map, item_model_video, DISH_TYPE);
		}
	}
	
	/**
	 * 设置监听
	 * @param map
	 * @param view
	 * @param type
	 */
	private void setListener(final Map<String,String> map,View view ,final int type){
		
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (type) {
				case USER_TYPE:
					Intent intent = new Intent(context, FriendHome.class);
					intent.putExtra("code",user_code);
					context.startActivity(intent);
					break;

				case DISH_TYPE:
					Intent intentdish = new Intent(context, DetailDish.class);
					intentdish.putExtra("code",map.get("code"));
					intentdish.putExtra("name",map.get("name"));
					context.startActivity(intentdish);
					break;
				
				default:
					break;
				}
			}
		});
	}
}
