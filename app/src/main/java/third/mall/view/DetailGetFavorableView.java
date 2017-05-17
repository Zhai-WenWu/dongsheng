package third.mall.view;

import android.app.Activity;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import amodule.user.activity.login.LoginByAccout;
import third.mall.dialog.FavorableDialog;
import third.mall.dialog.FavorableDialog.showCallBack;
/**
 * 优惠券view
 * @author yujian
 *
 */
public class DetailGetFavorableView extends RelativeLayout{

	private Activity context;
	private String shop_code;
	private TextView tv_favorable_item_1,tv_favorable_item_2,tv_favorable_item_3;
	private FavorableDialog dialog;

	public DetailGetFavorableView(Activity context, AttributeSet attrs) {
		super(context, attrs);
		this.context= context;
		initView();
	}
	public DetailGetFavorableView(Activity context) {
		super(context);
		this.context= context;
		initView();
	}
	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.view_mall_detail_get_favorable, this,true);
		tv_favorable_item_1=(TextView) findViewById(R.id.tv_favorable_item_1);
		tv_favorable_item_2=(TextView) findViewById(R.id.tv_favorable_item_2);
		tv_favorable_item_3=(TextView) findViewById(R.id.tv_favorable_item_3);
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(LoginManager.isLogin()){
					XHClick.mapStat(context, "a_mail_goods", "领券", "");
					if(dialog==null){
						dialog= new FavorableDialog(context,shop_code);
						dialog.setCallBack(new showCallBack() {
							
							@Override
							public void setShow() {
								dialog.show();
							}
						});
					}else{
						dialog.show();
					}
				}else{
					Intent intent_user = new Intent(context, LoginByAccout.class);
					context.startActivity(intent_user);
				}
			}
		});
	}
	public void setdata(ArrayList<String> list,String shop_code){
		this.shop_code= shop_code;
		if(list.size()==1){
			tv_favorable_item_1.setText(list.get(0));
			tv_favorable_item_1.setVisibility(View.VISIBLE);
			tv_favorable_item_2.setVisibility(View.GONE);
			tv_favorable_item_3.setVisibility(View.GONE);
		}else if(list.size()==2){
			tv_favorable_item_1.setText(list.get(0));
			tv_favorable_item_2.setText(list.get(1));
			tv_favorable_item_1.setVisibility(View.VISIBLE);
			tv_favorable_item_2.setVisibility(View.VISIBLE);
			tv_favorable_item_3.setVisibility(View.GONE);
			
		}else if(list.size()>=3){
			tv_favorable_item_1.setText(list.get(0));
			tv_favorable_item_2.setText(list.get(1));
			tv_favorable_item_3.setText(list.get(2));
			tv_favorable_item_1.setVisibility(View.VISIBLE);
			tv_favorable_item_2.setVisibility(View.VISIBLE);
			tv_favorable_item_3.setVisibility(View.VISIBLE);
		}
	}

}
