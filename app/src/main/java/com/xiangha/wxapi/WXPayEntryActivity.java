package com.xiangha.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiangha.R;

import third.mall.activity.OrderStateActivity;
import third.mall.activity.PaySuccedActvity;
import third.mall.aplug.MallCommon;
import acore.logic.PayCallback;

/**
 * 微信支付回调页面
 * @author Administrator
 *
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{

	private IWXAPI api;
	public static final String app_id="wx2b582fbe26ef8993";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_mall_wx_pay_result);
		
		api = WXAPIFactory.createWXAPI(this, app_id);
        api.handleIntent(getIntent(), this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}
	
	@Override
	public void onReq(BaseReq arg0) {
		
	}

	@Override
	public void onResp(BaseResp arg0) {
//		Toast.makeText(this,  String.valueOf(arg0.errCode)+String.valueOf(arg0.errStr), 0).show();
		if(PayCallback.getPayCallBack() != null){
			String data = "";
			if (arg0.errCode == 0) {
				data = "支付成功";
			}else{
				data = "支付失败";
			}
			PayCallback.getPayCallBack().onPay(arg0.errCode == 0,data);
		}else {
			if (arg0.errCode == 0) {
				Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this, PaySuccedActvity.class);
				intent.putExtra("amt", "");
				this.startActivity(intent);

			} else {
				if (!TextUtils.isEmpty(MallCommon.payment_order_id)) {//我的订单
					Intent intent = new Intent();
					intent.setClass(this, OrderStateActivity.class);
					intent.putExtra("order_id", MallCommon.payment_order_id);
					intent.putExtra("order_satus", "payment_order");
					this.startActivity(intent);
				}
			}
		}
		this.finish();
	}

}
