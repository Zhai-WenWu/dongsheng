package third.mall.aplug;

import acore.tools.FileManager;
import acore.tools.Tools;
import android.content.Context;
import android.text.TextUtils;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiangha.wxapi.WXPayEntryActivity;
/**
 * 支付方式的选择和操作
 * @author Administrator
 *
 */
public class MallPayType {
	private IWXAPI api;
	private Context context;
	public static String pay_type= "0";//0:用户没有选择，1：用户安装微信，默认 ，2：用户选择支付宝支付,3:当前手机不支持微信，只能用支付宝
	public MallPayType(Context context){
		this.context= context;
		api = WXAPIFactory.createWXAPI(context, WXPayEntryActivity.app_id);
		getPayType();
	}
	
	/**
	 * 获取当前选择的方式
	 * @return
	 */
	public String getPayType(){
		pay_type=(String) FileManager.loadShared(context, FileManager.MALL_PAYTYPE, FileManager.MALL_PAYTYPE);
		if(TextUtils.isEmpty(pay_type)||"0".equals(pay_type)){//没有选择
			boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;//微信是否支持支付
			if(isPaySupported){
				pay_type="1";
			}else{//不支持微信，只能选择支付宝
				pay_type="3";
			}
		}else{//用户选择了
			if("1".equals(pay_type)){//避免用户卸载微信
				boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;//微信是否支持支付
				if(!isPaySupported)
					pay_type="3";
			}
		}
		//没有支付宝，只能为微信1；
//		pay_type="1";
		FileManager.saveShared(context, FileManager.MALL_PAYTYPE, FileManager.MALL_PAYTYPE, pay_type);
		return pay_type;
	}
	
	/**
	 * 设置用户支付方式
	 * @param state true:成功，false:失败
	 * @return
	 */
	public boolean setPayType(String type){
		pay_type=(String) FileManager.loadShared(context, FileManager.MALL_PAYTYPE, FileManager.MALL_PAYTYPE);
		
		if("3".equals(pay_type)){//不支持微信
			
			if("1".equals(type)){//选择微信
				boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;//微信是否支持支付
				if(isPaySupported){
					pay_type="1";
				}else{
					Tools.showToast(context, "当前手机微信不支持支付功能");
					return false;
				}
			}else if("2".equals(type)){
				return false;
			}
		}else{
			if(type.equals(pay_type)){
				return false;
			}else{
				pay_type=type;
			}
		}
		FileManager.saveShared(context, FileManager.MALL_PAYTYPE, FileManager.MALL_PAYTYPE, pay_type);
		return true;
	}
}
