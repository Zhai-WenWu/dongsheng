package third.mall.view;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.override.activity.base.BaseActivity;
import third.mall.activity.AddressActivity;
import third.mall.activity.ShoppingOrderActivity;

/**
 * 地址选择ItemView
 * @author Administrator
 *
 */
public class AddressChangeItemView extends RelativeLayout implements OnClickListener{

	private BaseActivity context;
	private TextView tv_state_defult;//默认
	private ImageView iv_change_state;
	private String address_id;
	public AddressChangeItemView(BaseActivity context) {
		super(context);
		this.context =context;
		initView();
	}
	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.view_mall_addresschange_item, this, true);
		findViewById(R.id.iv_address_edit).setOnClickListener(this);
		findViewById(R.id.rela_address_edit).setOnClickListener(this);
//		findViewById(R.id.rela_address).setOnClickListener(this);
		tv_state_defult = (TextView) findViewById(R.id.tv_state_defult);
		iv_change_state=(ImageView) findViewById(R.id.iv_change_state);
//		iv_change_state.setOnClickListener(this);
	}
	public void setChangeData(Map<String,String> map){
		TextView tv_address_name=(TextView) findViewById(R.id.tv_address_name);
		TextView tv_address_iphone=(TextView) findViewById(R.id.tv_address_iphone);
		TextView tv_address_details=(TextView) findViewById(R.id.tv_address_details);
		if(map.containsKey("address_type")&&"2".equals(map.get("address_type")))tv_state_defult.setVisibility(View.VISIBLE);
		else tv_state_defult.setVisibility(View.GONE);
		address_id = map.get("address_id");
		if(map.containsKey("consumer_name"))
			tv_address_name.setText(map.get("consumer_name"));
		if(map.containsKey("consumer_mobile"))
			tv_address_iphone.setText(map.get("consumer_mobile"));
		if(map.containsKey("address_detail"))
			tv_address_details.setText(map.get("address_detail"));
	}
	public void setChangeState(String now_address_id){
		if(TextUtils.isEmpty(address_id)){
			Log.v("地址选择页面", "address_id为null");
			return;
		}
		if(now_address_id.equals(address_id))iv_change_state.setImageResource(R.drawable.z_mall_shopcat_choose);
		else iv_change_state.setImageResource(R.drawable.z_mall_address_no_changer);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rela_address_edit:
		case R.id.iv_address_edit://去编辑
			Intent intent = new Intent(context,AddressActivity.class);
			intent.putExtra("address_id", address_id);
			context.startActivityForResult(intent, ShoppingOrderActivity.OK_ADDRESS);
			break;
		case R.id.iv_change_state:
		case R.id.rela_address:
			break;
		}
		
	}

}
